<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

require __DIR__ . '/_common.php';

function resosoin_column_exists(
    PDO $pdo,
    string $table,
    string $column
): bool {
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

function resosoin_index_exists(
    PDO $pdo,
    string $table,
    string $index
): bool {
    $stmt = $pdo->prepare(
        'SELECT COUNT(*)
         FROM information_schema.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE()
           AND TABLE_NAME = ?
           AND INDEX_NAME = ?'
    );
    $stmt->execute([$table, $index]);

    return (int) $stmt->fetchColumn() > 0;
}

$pdo = db();
$sql = (string) file_get_contents(
    __DIR__ . '/schema.sql'
);

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
         DEFAULT '2026-09-26-v3'
         AFTER current_step"
    );
}

$pdo->exec(
    "ALTER TABLE resosoin_survey_sessions
     MODIFY survey_version VARCHAR(40) NOT NULL
     DEFAULT '2026-09-26-v3'"
);

if (
    !resosoin_column_exists(
        $pdo,
        'resosoin_survey_sessions',
        'recruitment_source'
    )
) {
    $pdo->exec(
        "ALTER TABLE resosoin_survey_sessions
         ADD recruitment_source VARCHAR(32) NOT NULL
         DEFAULT 'direct'
         AFTER survey_version"
    );
}

if (
    !resosoin_column_exists(
        $pdo,
        'resosoin_survey_sessions',
        'professional_verified'
    )
) {
    $pdo->exec(
        "ALTER TABLE resosoin_survey_sessions
         ADD professional_verified TINYINT(1) NOT NULL
         DEFAULT 0
         AFTER recruitment_source"
    );
}

if (
    !resosoin_column_exists(
        $pdo,
        'resosoin_survey_sessions',
        'professional_verification_method'
    )
) {
    $pdo->exec(
        "ALTER TABLE resosoin_survey_sessions
         ADD professional_verification_method VARCHAR(64) NULL
         AFTER professional_verified"
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
         AFTER professional_verification_method"
    );
}

if (
    !resosoin_index_exists(
        $pdo,
        'resosoin_survey_sessions',
        'idx_resosoin_source'
    )
) {
    $pdo->exec(
        'ALTER TABLE resosoin_survey_sessions
         ADD INDEX idx_resosoin_source (recruitment_source)'
    );
}

fwrite(
    STDOUT,
    "RésoSoin survey tables ready.\n"
);
