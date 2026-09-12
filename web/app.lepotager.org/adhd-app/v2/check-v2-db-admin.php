<?php
declare(strict_types=1);

/**
 * ADHD Survey V2 — DB/admin diagnostic
 * Place in /adhd-app/v2/
 * READ ONLY: no DB writes, no application file modifications.
 */

const ACCESS_KEY = 'dUu08EFbQl_nBsOlA5Gg1xVvotZDjQ';

header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('Referrer-Policy: no-referrer');
header('Cache-Control: no-store');

$key = (string)($_GET['key'] ?? '');
if ($key === '' || !hash_equals(ACCESS_KEY, $key)) {
    http_response_code(404);
    exit('Not found.');
}

error_reporting(E_ALL);
ini_set('display_errors', '0');

function h(mixed $v): string {
    return htmlspecialchars((string)$v, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

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

$configPath = is_string($home) && $home !== ''
    ? rtrim($home, '/') . '/private/executive-function-app/config.php'
    : $root . '/config.php';
$bootstrapPath = $root . '/includes/bootstrap.php';
$results = [];
$fatal = null;

function test(string $label, callable $fn): void {
    global $results;
    try {
        $value = $fn();
        $results[] = ['ok' => true, 'label' => $label, 'value' => $value === null ? 'OK' : (string)$value];
    } catch (Throwable $e) {
        $results[] = [
            'ok' => false,
            'label' => $label,
            'value' => get_class($e) . ': ' . $e->getMessage()
        ];
        throw $e;
    }
}

try {
    test('Private shared configuration', function() use ($configPath) {
        if (!is_file($configPath)) throw new RuntimeException('Missing private application configuration');
        if (!is_readable($configPath)) throw new RuntimeException('Private application configuration is not readable');
        return 'found / readable';
    });

    test('Bootstrap', function() use ($bootstrapPath) {
        if (!is_file($bootstrapPath)) throw new RuntimeException('Missing includes/bootstrap.php');
        require_once $bootstrapPath;
        return 'loaded';
    });

    test('PDO MySQL driver', function() {
        if (!extension_loaded('pdo_mysql')) throw new RuntimeException('pdo_mysql is not loaded');
        return 'loaded';
    });

    $pdo = null;
    test('MariaDB connection', function() use (&$pdo) {
        $pdo = db();
        return 'connected';
    });

    test('survey_v2_sessions', function() use (&$pdo) {
        return 'rows=' . (int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions")->fetchColumn();
    });

    test('survey_v2_answers', function() use (&$pdo) {
        return 'rows=' . (int)$pdo->query("SELECT COUNT(*) FROM survey_v2_answers")->fetchColumn();
    });

    test('survey_v2_guardian_consents', function() use (&$pdo) {
        return 'rows=' . (int)$pdo->query("SELECT COUNT(*) FROM survey_v2_guardian_consents")->fetchColumn();
    });

    // Exact queries used by admin dashboard:
    test('Admin query: status', function() use (&$pdo) {
        $pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE status IN ('core_submitted','completed')")->fetchColumn();
        return 'OK';
    });

    test('Admin query: cohort', function() use (&$pdo) {
        $pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE cohort='adult' AND status IN ('core_submitted','completed')")->fetchColumn();
        return 'OK';
    });

    test('Admin query: current_step', function() use (&$pdo) {
        $pdo->query("SELECT current_step,status,COUNT(*) n FROM survey_v2_sessions GROUP BY current_step,status ORDER BY n DESC")->fetchAll();
        return 'OK';
    });

    test('Admin query: guardian status', function() use (&$pdo) {
        $pdo->query("SELECT status,COUNT(*) n FROM survey_v2_guardian_consents GROUP BY status")->fetchAll();
        return 'OK';
    });

    test('Admin query: answers JOIN', function() use (&$pdo) {
        $pdo->query("SELECT a.question_id,a.answer_json FROM survey_v2_answers a JOIN survey_v2_sessions s ON s.id=a.session_id WHERE s.status IN ('core_submitted','completed')")->fetchAll();
        return 'OK';
    });

} catch (Throwable $e) {
    $fatal = $e;
}

$allOk = $fatal === null;
?>
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>V2 DB / admin check</title>
<style>
body{font-family:system-ui,-apple-system,sans-serif;background:#f2f6f2;color:#203025;margin:0;padding:20px}
main{max-width:820px;margin:4vh auto;background:#fff;border:1px solid #d6e2d7;border-radius:18px;padding:24px;box-shadow:0 12px 36px rgba(0,0,0,.07)}
h1{margin-top:0;font-size:1.45rem}.row{display:grid;grid-template-columns:minmax(180px,260px) 1fr;gap:12px;padding:12px;border-bottom:1px solid #e4e9e4}
.ok{color:#185b2a}.bad{color:#852a2a}.banner{padding:14px 16px;border-radius:12px;margin:16px 0}.banner.ok{background:#e4f5e7}.banner.bad{background:#fde7e7}
code{background:#edf2ee;padding:.1em .35em;border-radius:6px}.value{word-break:break-word}
small{display:block;margin-top:18px;opacity:.75;line-height:1.5}
@media(max-width:620px){.row{grid-template-columns:1fr;gap:4px}}
</style>
</head><body><main>
<h1>ADHD Survey V2 — DB/admin check 🔎</h1>
<div class="banner <?= $allOk ? 'ok' : 'bad' ?>">
<strong><?= $allOk ? 'Database + admin queries are OK.' : 'Failure found.' ?></strong>
<?php if (!$allOk): ?>
<br>The first failing check below is the direct cause to fix.
<?php endif; ?>
</div>
<?php foreach($results as $r): ?>
<div class="row">
  <strong class="<?= $r['ok']?'ok':'bad' ?>"><?= $r['ok']?'✓':'✕' ?> <?= h($r['label']) ?></strong>
  <span class="value"><?= h($r['value']) ?></span>
</div>
<?php endforeach; ?>
<small>
Read-only check. It uses the real shared configuration stored outside the public web root.
It does not display database credentials and performs only SELECT queries.<br>
Delete this file after sending the result.
</small>
</main></body></html>
