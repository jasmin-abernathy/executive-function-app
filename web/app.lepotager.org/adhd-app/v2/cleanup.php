<?php
declare(strict_types=1);
require __DIR__ . '/_common.php';
$pdo=db();
$pdo->beginTransaction();

// Pending guardian requests expire after 7 days; erase encrypted email.
$pdo->exec(
    "UPDATE survey_v2_guardian_consents
     SET status='expired', email_encrypted=NULL, updated_at=NOW()
     WHERE status='pending' AND expires_at < NOW()"
);

// Unfinished sessions inactive for 30 days are removed.
$pdo->exec(
    "DELETE FROM survey_v2_sessions
     WHERE status='active' AND updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY)"
);

// Submitted responses older than 12 months are removed from this research-cycle store.
$pdo->exec(
    "DELETE FROM survey_v2_sessions
     WHERE status IN ('core_submitted','completed')
       AND COALESCE(completed_at,core_submitted_at,updated_at) < DATE_SUB(NOW(), INTERVAL 12 MONTH)"
);

$pdo->commit();
echo "V2 cleanup completed.\n";
