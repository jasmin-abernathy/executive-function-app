<?php

declare(strict_types=1);

session_set_cookie_params([
    'httponly' => true,
    'secure' => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'samesite' => 'Strict',
]);
session_start();

$root = dirname(__DIR__);

$home = getenv('HOME');

if (!is_string($home) || $home === '') {
    $serverHome = $_SERVER['HOME'] ?? '';
    if (is_string($serverHome) && $serverHome !== '') {
        $home = $serverHome;
    }
}

if (!is_string($home) || $home === '') {
    $marker = '/public_html/';
    $position = strpos(__DIR__, $marker);
    if ($position !== false) {
        $home = substr(__DIR__, 0, $position);
    }
}

$configFile = is_string($home) && $home !== ''
    ? rtrim($home, '/') . '/private/executive-function-app/config.php'
    : $root . '/config.php';

$installed = is_file($configFile);
$error = '';
$success = false;
$generatedConfig = '';

if (empty($_SESSION['setup_csrf'])) {
    $_SESSION['setup_csrf'] = bin2hex(random_bytes(24));
}

function field(string $name, string $default = ''): string
{
    return htmlspecialchars((string) ($_POST[$name] ?? $default), ENT_QUOTES, 'UTF-8');
}

if (!$installed && $_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (!hash_equals((string) $_SESSION['setup_csrf'], (string) ($_POST['csrf'] ?? ''))) {
            throw new RuntimeException('The installer session expired. Reload the page and try again.');
        }

        $required = ['site_url','db_host','db_name','db_user','admin_username','admin_password','from_email','operator_legal_name','operator_address'];
        foreach ($required as $name) {
            if (trim((string) ($_POST[$name] ?? '')) === '') {
                throw new RuntimeException('Complete every required field.');
            }
        }

        $siteUrl = rtrim(trim((string) $_POST['site_url']), '/');
        if (!filter_var($siteUrl, FILTER_VALIDATE_URL) || !str_starts_with($siteUrl, 'https://')) {
            throw new RuntimeException('The public site URL must be a valid HTTPS URL.');
        }

        $fromEmail = mb_strtolower(trim((string) $_POST['from_email']));
        if (!filter_var($fromEmail, FILTER_VALIDATE_EMAIL)) {
            throw new RuntimeException('Enter a valid sender email address.');
        }

        if (strlen((string) $_POST['admin_password']) < 12) {
            throw new RuntimeException('Use an admin password of at least 12 characters.');
        }

        if (!extension_loaded('pdo_mysql')) {
            throw new RuntimeException('The PHP pdo_mysql extension is required.');
        }
        if (!extension_loaded('mbstring')) {
            throw new RuntimeException('The PHP mbstring extension is required.');
        }
        if (!function_exists('sodium_crypto_secretbox') && !function_exists('openssl_encrypt')) {
            throw new RuntimeException('PHP Sodium or OpenSSL is required to encrypt contact details.');
        }

        $dbPort = (int) ($_POST['db_port'] ?? 3306);
        $dsn = sprintf(
            'mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4',
            trim((string) $_POST['db_host']),
            $dbPort,
            trim((string) $_POST['db_name'])
        );
        $pdo = new PDO($dsn, trim((string) $_POST['db_user']), (string) ($_POST['db_password'] ?? ''), [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]);

        $schema = file_get_contents($root . '/sql/schema.sql');
        if ($schema === false) {
            throw new RuntimeException('The SQL schema file could not be read.');
        }
        $statements = preg_split('/;\s*(?:\r?\n|$)/', trim($schema));
        foreach ($statements as $statement) {
            if (trim($statement) !== '') {
                $pdo->exec($statement);
            }
        }

        $mailMode = in_array(($_POST['mail_mode'] ?? 'mail'), ['mail','smtp'], true) ? $_POST['mail_mode'] : 'mail';
        $smtpEncryption = in_array(($_POST['smtp_encryption'] ?? 'tls'), ['tls','ssl','none'], true) ? $_POST['smtp_encryption'] : 'tls';

        $config = [
            'app_env' => 'production',
            'site_url' => $siteUrl,
            'site_name' => 'Le Potager Lab — ADHD App Research',
            'app_key' => base64_encode(random_bytes(32)),
            'cron_key' => rtrim(strtr(base64_encode(random_bytes(32)), '+/', '-_'), '='),
            'db' => [
                'host' => trim((string) $_POST['db_host']),
                'port' => $dbPort,
                'name' => trim((string) $_POST['db_name']),
                'user' => trim((string) $_POST['db_user']),
                'password' => (string) ($_POST['db_password'] ?? ''),
                'charset' => 'utf8mb4',
            ],
            'mail' => [
                'mode' => $mailMode,
                'from_email' => $fromEmail,
                'from_name' => trim((string) ($_POST['from_name'] ?? 'Le Potager Lab')),
                'reply_to' => mb_strtolower(trim((string) ($_POST['reply_to'] ?? $fromEmail))),
                'smtp' => [
                    'host' => trim((string) ($_POST['smtp_host'] ?? '')),
                    'port' => (int) ($_POST['smtp_port'] ?? 587),
                    'encryption' => $smtpEncryption,
                    'username' => trim((string) ($_POST['smtp_username'] ?? '')),
                    'password' => (string) ($_POST['smtp_password'] ?? ''),
                ],
            ],
            'operator' => [
                'name' => trim((string) ($_POST['operator_name'] ?? 'Le Potager du Web')),
                'legal_name' => trim((string) $_POST['operator_legal_name']),
                'postal_address' => trim((string) $_POST['operator_address']),
                'contact_email' => mb_strtolower(trim((string) ($_POST['contact_email'] ?? $fromEmail))),
            ],
            'admin' => [
                'username' => trim((string) $_POST['admin_username']),
                'password_hash' => password_hash((string) $_POST['admin_password'], PASSWORD_DEFAULT),
            ],
            'retention' => [
                'unfinished_days' => 90,
                'completed_months' => 24,
                'reminder_days_after_send' => 30,
            ],
        ];

        $generatedConfig = "<?php\n\ndeclare(strict_types=1);\n\nreturn " . var_export($config, true) . ";\n";

        if (@file_put_contents($configFile, $generatedConfig, LOCK_EX) === false) {
            throw new RuntimeException('The database was created, but the private configuration file could not be written.');
        }
        @chmod($configFile, 0600);
        $success = true;
        $installed = true;
    } catch (Throwable $exception) {
        $error = $exception->getMessage();
    }
}
?><!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="robots" content="noindex,nofollow">
  <title>Install the ADHD questionnaire</title>
  <link rel="stylesheet" href="../assets/css/app.css?v=1">
</head>
<body>
<main class="page-card">
  <p class="eyebrow">Le Potager Lab</p>
  <h1>Questionnaire installer</h1>

  <?php if ($installed && !$success): ?>
    <div class="notice is-success">The questionnaire is already installed. For security, delete or rename the <code>setup</code> folder.</div>
    <div class="action-row"><a class="btn btn-primary" href="../">Open the questionnaire</a><a class="btn btn-secondary" href="../admin/">Open the dashboard</a></div>
  <?php else: ?>
    <?php if ($error !== ''): ?><div class="notice is-error"><?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?></div><?php endif; ?>
    <?php if ($success): ?>
      <div class="notice is-success"><strong>Installation complete.</strong> Delete or rename the <code>setup</code> folder, then configure the hourly reminder cron described in the deployment checklist.</div>
      <div class="action-row"><a class="btn btn-primary" href="../">Open the questionnaire</a><a class="btn btn-secondary" href="../admin/">Open the dashboard</a></div>
    <?php else: ?>
      <p class="lead">Upload the kit, create an empty MariaDB database and user in o2switch, then complete this form once.</p>
      <form method="post" autocomplete="off">
        <input type="hidden" name="csrf" value="<?= htmlspecialchars((string) $_SESSION['setup_csrf'], ENT_QUOTES, 'UTF-8') ?>">

        <h2>Public address</h2>
        <label class="field-label" for="site_url">Full HTTPS URL</label>
        <input class="text-field" id="site_url" name="site_url" type="url" required value="<?= field('site_url', 'https://lab.lepotager.org/adhd-app') ?>">

        <h2>MariaDB database</h2>
        <label class="field-label" for="db_host">Database host</label>
        <input class="text-field" id="db_host" name="db_host" required value="<?= field('db_host', 'localhost') ?>">
        <label class="field-label" for="db_port" style="margin-top:12px">Port</label>
        <input class="text-field" id="db_port" name="db_port" type="number" required value="<?= field('db_port', '3306') ?>">
        <label class="field-label" for="db_name" style="margin-top:12px">Database name</label>
        <input class="text-field" id="db_name" name="db_name" required value="<?= field('db_name') ?>">
        <label class="field-label" for="db_user" style="margin-top:12px">Database user</label>
        <input class="text-field" id="db_user" name="db_user" required value="<?= field('db_user') ?>">
        <label class="field-label" for="db_password" style="margin-top:12px">Database password</label>
        <input class="text-field" id="db_password" name="db_password" type="password" value="">

        <h2>Email delivery</h2>
        <label class="field-label" for="mail_mode">Delivery method</label>
        <select class="select-field" id="mail_mode" name="mail_mode"><option value="mail">PHP mail() — simplest on o2switch</option><option value="smtp">Authenticated SMTP</option></select>
        <label class="field-label" for="from_email" style="margin-top:12px">Sender email</label>
        <input class="text-field" id="from_email" name="from_email" type="email" required value="<?= field('from_email', 'contact@lepotager.org') ?>">
        <label class="field-label" for="from_name" style="margin-top:12px">Sender name</label>
        <input class="text-field" id="from_name" name="from_name" value="<?= field('from_name', 'Le Potager Lab') ?>">
        <label class="field-label" for="reply_to" style="margin-top:12px">Reply-to email</label>
        <input class="text-field" id="reply_to" name="reply_to" type="email" value="<?= field('reply_to', 'contact@lepotager.org') ?>">

        <div class="conditional-panel">
          <h3>SMTP fields — only used if SMTP is selected</h3>
          <label class="field-label" for="smtp_host">SMTP host</label>
          <input class="text-field" id="smtp_host" name="smtp_host" value="<?= field('smtp_host', 'mail.lepotager.org') ?>">
          <label class="field-label" for="smtp_port" style="margin-top:12px">SMTP port</label>
          <input class="text-field" id="smtp_port" name="smtp_port" type="number" value="<?= field('smtp_port', '587') ?>">
          <label class="field-label" for="smtp_encryption" style="margin-top:12px">Encryption</label>
          <select class="select-field" id="smtp_encryption" name="smtp_encryption"><option value="tls">STARTTLS</option><option value="ssl">Implicit SSL/TLS</option><option value="none">None</option></select>
          <label class="field-label" for="smtp_username" style="margin-top:12px">SMTP username</label>
          <input class="text-field" id="smtp_username" name="smtp_username" value="<?= field('smtp_username') ?>">
          <label class="field-label" for="smtp_password" style="margin-top:12px">SMTP password</label>
          <input class="text-field" id="smtp_password" name="smtp_password" type="password">
        </div>

        <h2>Data controller details</h2>
        <label class="field-label" for="operator_name">Public trading name</label>
        <input class="text-field" id="operator_name" name="operator_name" value="<?= field('operator_name', 'Le Potager du Web') ?>">
        <label class="field-label" for="operator_legal_name" style="margin-top:12px">Legal identity</label>
        <input class="text-field" id="operator_legal_name" name="operator_legal_name" required value="<?= field('operator_legal_name', 'Jasmin Lévêque — Entrepreneur individuel') ?>">
        <label class="field-label" for="operator_address" style="margin-top:12px">Professional postal address</label>
        <textarea class="text-field" id="operator_address" name="operator_address" required><?= field('operator_address') ?></textarea>
        <label class="field-label" for="contact_email" style="margin-top:12px">Privacy contact email</label>
        <input class="text-field" id="contact_email" name="contact_email" type="email" value="<?= field('contact_email', 'contact@lepotager.org') ?>">

        <h2>Private dashboard</h2>
        <label class="field-label" for="admin_username">Admin username</label>
        <input class="text-field" id="admin_username" name="admin_username" required value="<?= field('admin_username', 'jasmin') ?>">
        <label class="field-label" for="admin_password" style="margin-top:12px">Admin password</label>
        <input class="text-field" id="admin_password" name="admin_password" type="password" required minlength="12">
        <span class="field-help">Use at least 12 characters and a unique password.</span>

        <div class="action-row"><button class="btn btn-primary" type="submit">Install the questionnaire</button></div>
      </form>
      <?php if ($generatedConfig !== '' && !$success): ?>
        <h2>Generated config.php</h2>
        <p>The installer could not write the file automatically. Save this configuration in the private configuration file outside the public web root:</p>
        <textarea class="text-field" rows="18" readonly><?= htmlspecialchars($generatedConfig, ENT_QUOTES, 'UTF-8') ?></textarea>
      <?php endif; ?>
    <?php endif; ?>
  <?php endif; ?>
</main>
</body>
</html>
