<?php
declare(strict_types=1);

require dirname(__DIR__) . '/_common.php';
require __DIR__ . '/analytics.php';
require __DIR__ . '/export-utils.php';

header('Cache-Control: private, no-store, max-age=0');
header('Pragma: no-cache');
header('X-Content-Type-Options: nosniff');
header('Referrer-Policy: no-referrer');

session_set_cookie_params([
    'httponly' => true,
    'secure' => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'samesite' => 'Strict',
]);
session_start();

if (empty($_SESSION['v2_admin'])) {
    header('Location: ./', true, 302);
    exit;
}

$format = $_GET['format'] ?? 'json';
if (!is_string($format) || !in_array($format, ['json', 'csv'], true)) {
    http_response_code(400);
    header('Content-Type: text/plain; charset=utf-8');
    echo 'Format non pris en charge.';
    exit;
}
$lang = ($_GET['lang'] ?? 'fr') === 'en' ? 'en' : 'fr';

try {
    $pdo = db();
    if (!v2_tables_ready($pdo)) {
        throw new RuntimeException('Survey database not ready');
    }

    $summary = [
        'started' => (int) $pdo->query("SELECT COUNT(*) FROM survey_v2_sessions")->fetchColumn(),
        'core' => (int) $pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE status IN ('core_submitted','completed')")->fetchColumn(),
        'done' => (int) $pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE status='completed'")->fetchColumn(),
        'minor' => (int) $pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE cohort='minor' AND status IN ('core_submitted','completed')")->fetchColumn(),
        'adult' => (int) $pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE cohort='adult' AND status IN ('core_submitted','completed')")->fetchColumn(),
    ];
    $progress = $pdo->query('SELECT current_step,status,COUNT(*) n FROM survey_v2_sessions GROUP BY current_step,status ORDER BY n DESC')->fetchAll();
    $answers = $pdo->query("SELECT a.question_id,a.answer_json FROM survey_v2_answers a JOIN survey_v2_sessions s ON s.id=a.session_id WHERE s.status IN ('core_submitted','completed')")->fetchAll();
    $catalog = v2_admin_catalog(dirname(__DIR__));
    $payload = v2_admin_export_payload($catalog, v2_admin_aggregate($answers), $summary, $progress, $lang);
} catch (Throwable $e) {
    error_log('V2 admin export failed: ' . $e->getMessage());
    http_response_code(503);
    header('Content-Type: text/plain; charset=utf-8');
    echo $lang === 'fr' ? 'Export temporairement indisponible.' : 'Export temporarily unavailable.';
    exit;
}

$filename = 'adhd-v2-resultats-' . gmdate('Y-m-d') . '.' . $format;
header('Content-Disposition: attachment; filename="' . $filename . '"');

if ($format === 'json') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT | JSON_THROW_ON_ERROR);
    exit;
}

header('Content-Type: text/csv; charset=utf-8');
$stream = fopen('php://output', 'wb');
if ($stream === false) {
    http_response_code(503);
    exit;
}
fwrite($stream, "\xEF\xBB\xBF"); // Excel-friendly UTF-8
v2_admin_write_csv($stream, $payload);
fclose($stream);
