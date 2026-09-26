<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

require __DIR__ . '/_common.php';
$pdo = db();
$pdo->beginTransaction();
$pdo->exec(
    "DELETE FROM resosoin_survey_sessions
     WHERE status = 'active'
       AND updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY)"
);
$pdo->exec(
    "DELETE FROM resosoin_survey_sessions
     WHERE status = 'submitted'
       AND COALESCE(submitted_at, updated_at) < DATE_SUB(NOW(), INTERVAL 12 MONTH)"
);
$pdo->commit();
fwrite(STDOUT, "RésoSoin survey cleanup completed.\n");
