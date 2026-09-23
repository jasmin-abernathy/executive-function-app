<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

require __DIR__ . '/_common.php';
$pdo = db();
$sql = (string) file_get_contents(__DIR__ . '/schema.sql');
foreach (array_filter(array_map('trim', preg_split('/;\s*(?:\r?\n|$)/', $sql) ?: [])) as $statement) {
    $pdo->exec($statement);
}
fwrite(STDOUT, "RésoSoin survey tables ready.\n");
