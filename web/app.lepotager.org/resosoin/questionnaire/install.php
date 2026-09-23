<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

require __DIR__ . '/_common.php';

function resosoin_column_exists(PDO $pdo, string $table, string $column): bool
{
    $stmt = $pdo->prepare(
        'SELECT COUNT(*)
         FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE()
           AND TABLE_NAME = ?
           AND COLUMN_NAME = ?'
    );
    $stmt->execute([$table, $column]);
    return (int) $stmt->fetchColumn() > 0;
}

$pdo = db();
$sql = (string) file_get_contents(__DIR__ . '/schema.sql');

foreach (
    array_filter(
        array_map(
            'trim',
            preg_split('/;\s*(?:\r?\n|$)/', $sql) ?: []
        )
    ) as $statement
) {
    $pdo->exec($statement);
}

if (
    !resosoin_column_exists(
        $pdo,
        'resosoin_survey_sessions',
        'survey_version'
    )
) {
    $pdo->exec(
        "ALTER TABLE resosoin_survey_sessions
         ADD survey_version VARCHAR(40) NOT NULL
         DEFAULT '2026-09-23-v1'
         AFTER current_step"
    );
}

if (
    !resosoin_column_exists(
        $pdo,
        'resosoin_survey_sessions',
        'consent_at'
    )
) {
    $pdo->exec(
        "ALTER TABLE resosoin_survey_sessions
         ADD consent_at DATETIME NOT NULL
         DEFAULT CURRENT_TIMESTAMP
         AFTER survey_version"
    );
}

fwrite(STDOUT, "RésoSoin survey tables ready.\n");
