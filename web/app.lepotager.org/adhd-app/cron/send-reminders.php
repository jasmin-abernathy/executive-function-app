<?php

declare(strict_types=1);
require dirname(__DIR__) . '/includes/bootstrap.php';

if (PHP_SAPI !== 'cli') {
    $key = clean_text($_GET['key'] ?? '', 200);
    if ($key === '' || !hash_equals((string) config('cron_key'), $key)) {
        http_response_code(403);
        exit('Forbidden');
    }
    header('Content-Type: text/plain; charset=utf-8');
}

$pdo = db();
$sent = 0;
$failed = 0;
$deletedSessions = 0;
$deletedContacts = 0;

$stmt = $pdo->query(
    "SELECT rc.id, rc.email_encrypted, rc.resume_token_encrypted, rc.reminder_at "
    . "FROM resume_contacts rc "
    . "INNER JOIN survey_sessions ss ON ss.id = rc.session_id "
    . "WHERE ss.status = 'active' AND rc.reminder_at IS NOT NULL AND rc.reminder_at <= NOW() AND rc.reminder_sent_at IS NULL "
    . "ORDER BY rc.reminder_at ASC LIMIT 100"
);

foreach ($stmt->fetchAll() as $row) {
    try {
        $email = decrypt_string($row['email_encrypted']);
        $token = decrypt_string($row['resume_token_encrypted']);
        if (!$email || !$token || !valid_email($email)) {
            throw new RuntimeException('Reminder contact could not be decrypted.');
        }
        $resumeUrl = format_site_url('?resume=' . rawurlencode($token));
        $title = 'A gentle reminder about your saved questionnaire';
        $body = '<p>You asked for one reminder about the ADHD-friendly app questionnaire you paused.</p>'
            . '<p>Your answers are still saved, and there is no deadline.</p>'
            . button_html('Continue the questionnaire', $resumeUrl)
            . '<p style="font-size:13px;color:#5f6f63">This is the only scheduled reminder for this saved session.</p>';
        send_app_email($email, $title, email_layout($title, $body), "You asked for one reminder about your saved questionnaire.\n\nContinue here:\n{$resumeUrl}\n\nThere is no deadline.");
        $update = $pdo->prepare('UPDATE resume_contacts SET reminder_sent_at = NOW(), updated_at = NOW() WHERE id = ?');
        $update->execute([$row['id']]);
        $sent++;
    } catch (Throwable $error) {
        $failed++;
        error_log('[ADHD survey reminder] ' . $error->getMessage());
    }
}

$unfinishedDays = max(1, (int) config('retention.unfinished_days', 90));
$completedMonths = max(1, (int) config('retention.completed_months', 24));
$reminderDays = max(1, (int) config('retention.reminder_days_after_send', 30));

$stmt = $pdo->prepare("DELETE FROM survey_sessions WHERE status = 'active' AND updated_at < DATE_SUB(NOW(), INTERVAL {$unfinishedDays} DAY)");
$stmt->execute();
$deletedSessions += $stmt->rowCount();

$stmt = $pdo->prepare("DELETE FROM survey_sessions WHERE status = 'completed' AND completed_at < DATE_SUB(NOW(), INTERVAL {$completedMonths} MONTH)");
$stmt->execute();
$deletedSessions += $stmt->rowCount();

$stmt = $pdo->prepare("DELETE FROM resume_contacts WHERE reminder_sent_at IS NOT NULL AND reminder_sent_at < DATE_SUB(NOW(), INTERVAL {$reminderDays} DAY)");
$stmt->execute();

$stmt = $pdo->exec("DELETE FROM participation_contacts WHERE status = 'pending' AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)");
$deletedContacts += (int) $stmt;

printf(
    "ADHD survey cron complete\nReminders sent: %d\nReminder failures: %d\nExpired survey sessions deleted: %d\nExpired pending contacts deleted: %d\n",
    $sent,
    $failed,
    $deletedSessions,
    $deletedContacts
);
