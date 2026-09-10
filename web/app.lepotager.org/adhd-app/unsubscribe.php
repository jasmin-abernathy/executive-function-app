<?php

declare(strict_types=1);
require __DIR__ . '/includes/bootstrap.php';

$token = clean_text($_GET['token'] ?? $_POST['token'] ?? '', 200);
$status = 'idle';
$message = 'Use the button below to unsubscribe from all project participation messages.';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && $token !== '') {
    try {
        $pdo = db();
        $stmt = $pdo->prepare('SELECT id, status FROM participation_contacts WHERE unsubscribe_token_hash = ? LIMIT 1');
        $stmt->execute([token_hash($token)]);
        $contact = $stmt->fetch();
        if (!$contact) {
            $status = 'error';
            $message = 'This unsubscribe link is invalid.';
        } elseif ($contact['status'] === 'unsubscribed') {
            $status = 'success';
            $message = 'This address is already unsubscribed.';
        } else {
            $stmt = $pdo->prepare("UPDATE participation_contacts SET status = 'unsubscribed', unsubscribed_at = NOW(), updated_at = NOW() WHERE id = ?");
            $stmt->execute([$contact['id']]);
            $status = 'success';
            $message = 'You have been unsubscribed from all project participation messages.';
        }
    } catch (Throwable $error) {
        error_log('[ADHD survey unsubscribe] ' . $error->getMessage());
        $status = 'error';
        $message = 'A technical error occurred. Please contact us if the problem continues.';
    }
}
?><!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="robots" content="noindex,nofollow">
  <title>Unsubscribe — Le Potager Lab</title>
  <link rel="stylesheet" href="assets/css/app.css?v=1">
</head>
<body>
<main class="page-card">
  <p class="eyebrow">Le Potager Lab</p>
  <h1>Project messages</h1>
  <div class="notice <?= $status === 'success' ? 'is-success' : ($status === 'error' ? 'is-error' : '') ?>"><?= htmlspecialchars($message, ENT_QUOTES, 'UTF-8') ?></div>
  <?php if ($status === 'idle' && $token !== ''): ?>
    <form method="post">
      <input type="hidden" name="token" value="<?= htmlspecialchars($token, ENT_QUOTES, 'UTF-8') ?>">
      <button class="btn btn-danger" type="submit">Unsubscribe me</button>
    </form>
  <?php elseif ($token === ''): ?>
    <p>Please use the unsubscribe link included in a project email or contact <a href="mailto:<?= htmlspecialchars((string) config('operator.contact_email'), ENT_QUOTES, 'UTF-8') ?>"><?= htmlspecialchars((string) config('operator.contact_email'), ENT_QUOTES, 'UTF-8') ?></a>.</p>
  <?php endif; ?>
  <div class="action-row"><a class="btn btn-secondary" href="<?= htmlspecialchars(rtrim((string) config('site_url'), '/') . '/', ENT_QUOTES, 'UTF-8') ?>">Return to the project</a></div>
</main>
</body>
</html>
