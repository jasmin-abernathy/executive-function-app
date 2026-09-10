<?php

declare(strict_types=1);
require dirname(__DIR__) . '/includes/bootstrap.php';
require_once dirname(__DIR__) . '/includes/analytics.php';

session_set_cookie_params([
    'httponly' => true,
    'secure' => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'samesite' => 'Strict',
]);
session_start();

if (empty($_SESSION['admin_csrf'])) {
    $_SESSION['admin_csrf'] = random_token(24);
}

$error = '';
$notice = '';

if (isset($_GET['logout'])) {
    $_SESSION = [];
    session_destroy();
    header('Location: ./');
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['login'])) {
    $username = trim((string) ($_POST['username'] ?? ''));
    $password = (string) ($_POST['password'] ?? '');

    if (hash_equals((string) config('admin.username'), $username)
        && password_verify($password, (string) config('admin.password_hash'))) {
        session_regenerate_id(true);
        $_SESSION['admin_authenticated'] = true;
        header('Location: ./');
        exit;
    }

    usleep(500000);
    $error = 'Invalid username or password.';
}

$authenticated = !empty($_SESSION['admin_authenticated']);

function h(mixed $value): string
{
    return htmlspecialchars((string) $value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function panel_labels(): array
{
    return [
        'research' => 'User research panel',
        'codesign' => 'Co-design group',
        'beta' => 'Beta-testing group',
        'accessibility' => 'Accessibility testing',
        'opensource' => 'Open-source contributions',
        'translation' => 'Translation & localisation',
        'professional' => 'Professional advisory group',
        'crowdfunding' => 'Crowdfunding updates',
        'updates' => 'Occasional project updates',
        'future_youth' => 'Future age-appropriate research',
    ];
}

function decoded_panels(string $json): array
{
    $values = json_decode($json, true);
    if (!is_array($values)) {
        return [];
    }

    $labels = panel_labels();
    $out = [];
    foreach ($values as $value) {
        if (!is_scalar($value)) {
            continue;
        }
        $key = (string) $value;
        $out[] = $labels[$key] ?? $key;
    }
    return $out;
}

function admin_funnel_step_label(string $step): string
{
    $labels = [
        'welcome' => 'Welcome screen',
        'readiness' => 'Readiness / self-check',
        'come_back_later' => 'Chose to come back later',
        'consent' => 'Consent',
        'age_eligibility' => '18+ eligibility',
        'recent_difficulty' => 'Recent everyday difficulty',
        'current_strategy' => 'Current strategy (optional)',
        'relationship' => 'ADHD relationship (optional)',
        'age_range' => 'Adult age range (optional)',
        'existing_tools' => 'Existing tools (optional)',
        'primary_path' => 'Main support area',
        'secondary_path' => 'Second support area (optional)',
        'core_review' => 'Required-part review / submit',
        'optional_hub' => 'Optional topics hub',
        'review' => 'Legacy review',
        'completed' => 'Questionnaire finished',
        'ineligible' => 'Under-18 ineligible screen',
    ];
    if (isset($labels[$step])) return $labels[$step];
    $pretty = preg_replace('/[_\\-]+/', ' ', trim($step));
    $pretty = preg_replace('/\\s+/', ' ', (string) $pretty);
    return ucfirst($pretty !== '' ? $pretty : 'Unknown step');
}

function admin_funnel_test_exclusion(string $alias = 's'): string
{
    return "NOT EXISTS (
        SELECT 1
        FROM survey_answers it
        WHERE it.session_id = {$alias}.id
          AND it.question_id = 'survey_comment'
          AND LOWER(TRIM(BOTH '\"' FROM CAST(it.answer_json AS CHAR))) = 'test de jasmin'
    )";
}

function unlink_contacts_from_responses(PDO $pdo): int
{
    // Direct linkage is intentionally destroyed. The contact system still works:
    // confirmation/unsubscribe use the contact's own tokens, not the survey session.
    $stmt = $pdo->prepare(
        'UPDATE participation_contacts '
        . 'SET session_id = NULL, link_to_response = 0 '
        . 'WHERE session_id IS NOT NULL OR link_to_response <> 0'
    );
    $stmt->execute();
    return $stmt->rowCount();
}

function export_csv(string $type): never
{
    $pdo = db();
    header('Content-Type: text/csv; charset=utf-8');
    echo "\xEF\xBB\xBF";
    $out = fopen('php://output', 'wb');

    if ($type === 'contacts') {
        // Contact export is deliberately impossible to join to survey exports:
        // confirmed email + selected panels only.
        header('Content-Disposition: attachment; filename="confirmed-contact-panels-' . date('Y-m-d-His') . '.csv"');
        fputcsv($out, ['email', 'panels']);

        $rows = $pdo->query(
            "SELECT email_encrypted, interests_json
             FROM participation_contacts
             WHERE status = 'active'"
        )->fetchAll();

        foreach ($rows as $row) {
            $email = decrypt_string($row['email_encrypted']);
            $panels = decoded_panels((string) $row['interests_json']);
            if ($email === '') {
                continue;
            }
            fputcsv($out, [$email, implode(' | ', $panels)]);
        }

        fclose($out);
        exit;
    }

    // Anonymous survey export:
    // - completed responses only
    // - no stored UUID
    // - no date/time
    // - response labels are re-randomised on every export
    header('Content-Disposition: attachment; filename="anonymous-survey-responses-' . date('Y-m-d-His') . '.csv"');

    $filteredSessions = analytics_load_completed($pdo);
    $sessionIds = array_keys($filteredSessions);

    for ($i = count($sessionIds) - 1; $i > 0; $i--) {
        $j = random_int(0, $i);
        [$sessionIds[$i], $sessionIds[$j]] = [$sessionIds[$j], $sessionIds[$i]];
    }

    fputcsv($out, ['anonymous_response', 'question_id', 'answer_json']);

    $answerStmt = $pdo->prepare(
        'SELECT question_id, answer_json
         FROM survey_answers
         WHERE session_id = ?
         ORDER BY id ASC'
    );

    foreach ($sessionIds as $index => $sessionId) {
        $anonymous = 'response_' . str_pad((string) ($index + 1), 3, '0', STR_PAD_LEFT);
        $answerStmt->execute([$sessionId]);
        foreach ($answerStmt->fetchAll() as $row) {
            fputcsv($out, [$anonymous, $row['question_id'], $row['answer_json']]);
        }
    }

    fclose($out);
    exit;
}

if ($authenticated) {
    $pdo = db();
    unlink_contacts_from_responses($pdo);
}

if ($authenticated && isset($_GET['export'])) {
    export_csv((string) $_GET['export']);
}

if ($authenticated && $_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['cleanup'])) {
    if (!hash_equals((string) $_SESSION['admin_csrf'], (string) ($_POST['csrf'] ?? ''))) {
        $error = 'The dashboard session expired.';
    } else {
        $pdo = db();
        $unfinishedDays = max(1, (int) config('retention.unfinished_days', 90));
        $completedMonths = max(1, (int) config('retention.completed_months', 24));

        $stmt = $pdo->prepare("DELETE FROM survey_sessions WHERE status = 'active' AND updated_at < DATE_SUB(NOW(), INTERVAL {$unfinishedDays} DAY)");
        $stmt->execute();
        $activeDeleted = $stmt->rowCount();

        $stmt = $pdo->prepare("DELETE FROM survey_sessions WHERE status = 'completed' AND completed_at < DATE_SUB(NOW(), INTERVAL {$completedMonths} MONTH)");
        $stmt->execute();
        $completedDeleted = $stmt->rowCount();

        $pendingDeleted = $pdo->exec("DELETE FROM participation_contacts WHERE status = 'pending' AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)");
        $notice = "Cleanup complete: {$activeDeleted} unfinished sessions, {$completedDeleted} completed sessions and {$pendingDeleted} pending contacts deleted.";
    }
}

$tabs = ['summary', 'funnel', 'responses', 'contacts', 'exports'];
$tab = in_array((string) ($_GET['tab'] ?? 'summary'), $tabs, true)
    ? (string) ($_GET['tab'] ?? 'summary')
    : 'summary';

$stats = [];
$sessions = [];
$individuals = [];
$contacts = [];
$sections = [];
$universeStats = [];
$funnel = [
    'started' => 0,
    'required_submitted' => 0,
    'optional_open' => 0,
    'finished' => 0,
    'active_recent' => 0,
    'active_30m_1h' => 0,
    'active_1h_24h' => 0,
    'active_24h_plus' => 0,
    'started_24h' => 0,
    'required_24h' => 0,
    'finished_24h' => 0,
    'required_rate' => 0.0,
    'required_rate_24h' => 0.0,
];
$dropoffSteps = [];

if ($authenticated) {
    $pdo = db();

    $stats = [
        'active' => (int) $pdo->query("SELECT COUNT(*) FROM survey_sessions WHERE status = 'active'")->fetchColumn(),
        'completed' => 0,
        'contacts_active' => (int) $pdo->query("SELECT COUNT(*) FROM participation_contacts WHERE status = 'active'")->fetchColumn(),
        'contacts_pending' => (int) $pdo->query("SELECT COUNT(*) FROM participation_contacts WHERE status = 'pending'")->fetchColumn(),
    ];

    $sessions = analytics_load_completed($pdo);
    $stats['completed'] = count($sessions);
    $individuals = analytics_randomized_individuals($sessions);

    $funnelExclude = admin_funnel_test_exclusion('s');

    $funnelStmt = $pdo->query(
        "SELECT
            COUNT(*) AS started,
            SUM(CASE WHEN s.status = 'completed' THEN 1 ELSE 0 END) AS required_submitted,
            SUM(CASE WHEN s.status = 'completed' AND s.current_step <> 'completed' THEN 1 ELSE 0 END) AS optional_open,
            SUM(CASE WHEN s.status = 'completed' AND s.current_step = 'completed' THEN 1 ELSE 0 END) AS finished,
            SUM(CASE WHEN s.status = 'active' AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 30 MINUTE) THEN 1 ELSE 0 END) AS active_recent,
            SUM(CASE WHEN s.status = 'active' AND s.updated_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE) AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR) THEN 1 ELSE 0 END) AS active_30m_1h,
            SUM(CASE WHEN s.status = 'active' AND s.updated_at < DATE_SUB(NOW(), INTERVAL 1 HOUR) AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 ELSE 0 END) AS active_1h_24h,
            SUM(CASE WHEN s.status = 'active' AND s.updated_at < DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 ELSE 0 END) AS active_24h_plus,
            SUM(CASE WHEN s.created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 ELSE 0 END) AS started_24h,
            SUM(CASE WHEN s.status = 'completed' AND s.completed_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 ELSE 0 END) AS required_24h,
            SUM(CASE WHEN s.status = 'completed' AND s.current_step = 'completed' AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 ELSE 0 END) AS finished_24h
         FROM survey_sessions s
         WHERE {$funnelExclude}"
    );
    $funnelRow = $funnelStmt->fetch() ?: [];
    foreach (array_keys($funnel) as $key) {
        if (array_key_exists($key, $funnelRow) && !str_contains($key, 'rate')) {
            $funnel[$key] = (int) $funnelRow[$key];
        }
    }
    if ($funnel['started'] > 0) {
        $funnel['required_rate'] = round(($funnel['required_submitted'] / $funnel['started']) * 100, 1);
    }
    if ($funnel['started_24h'] > 0) {
        $funnel['required_rate_24h'] = round(($funnel['required_24h'] / $funnel['started_24h']) * 100, 1);
    }

    $dropoffStmt = $pdo->query(
        "SELECT
            s.current_step,
            COUNT(*) AS total_open,
            SUM(CASE WHEN s.updated_at >= DATE_SUB(NOW(), INTERVAL 30 MINUTE) THEN 1 ELSE 0 END) AS under_30m,
            SUM(CASE WHEN s.updated_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE) AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR) THEN 1 ELSE 0 END) AS between_30m_1h,
            SUM(CASE WHEN s.updated_at < DATE_SUB(NOW(), INTERVAL 1 HOUR) AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 ELSE 0 END) AS between_1h_24h,
            SUM(CASE WHEN s.updated_at < DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 ELSE 0 END) AS over_24h
         FROM survey_sessions s
         WHERE s.status = 'active'
           AND {$funnelExclude}
         GROUP BY s.current_step
         ORDER BY
            SUM(CASE WHEN s.updated_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE) THEN 1 ELSE 0 END) DESC,
            COUNT(*) DESC,
            s.current_step ASC"
    );
    $dropoffSteps = $dropoffStmt->fetchAll();

    $contactRows = $pdo->query(
        "SELECT email_encrypted, interests_json
         FROM participation_contacts
         WHERE status = 'active'"
    )->fetchAll();

    foreach ($contactRows as $row) {
        $email = decrypt_string($row['email_encrypted']);
        if ($email === '') {
            continue;
        }
        $contacts[] = [
            'email' => $email,
            'panels' => decoded_panels((string) $row['interests_json']),
        ];
    }

    usort($contacts, fn(array $a, array $b): int => strcasecmp($a['email'], $b['email']));

    $sections = [
        'Needs emerging from the pre-test' => [
            ['recent_difficulty', 'Recent difficulty'],
            ['primary_path', 'Primary support area'],
            ['secondary_path', 'Optional second area'],
        ],
        'Interface preferences' => [
            ['visual_home_density', 'Home-screen density'],
            ['visual_focus_surface', 'Focus-session surface'],
            ['visual_quick_capture', 'Quick capture'],
            ['visual_return_screen', 'Returning after time away'],
            ['progress_style', 'Long-term progress'],
            ['personality', 'App personality'],
        ],
        'Potential drawbacks / guardrails' => [
            ['suggestions_discomfort', 'Automatic suggestions'],
            ['reward_discomfort', 'Reward systems'],
            ['companion_discomfort', 'Companion'],
        ],
        'Name research' => [
            ['name_first', 'First impression'],
            ['name_final', 'Final choice'],
        ],
        'Questionnaire usability' => [
            ['survey_ease', 'Ease of use'],
            ['survey_save_helpful', 'Save & return helpfulness'],
            ['survey_save_clear', 'Save & return clarity'],
            ['survey_friction', 'Reported friction'],
        ],
    ];

    foreach (['astelle', 'tramelia', 'sillage'] as $name) {
        $universeStats[$name] = analytics_universe_fit($sessions, 'name_rate_' . $name);
    }
}
?><!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="robots" content="noindex,nofollow">
  <title>Research dashboard — Le Potager Lab</title>
  <style>
:root {
  color-scheme: light;
  --bg: #f5f3ed;
  --surface: #fffdf8;
  --surface-strong: #ffffff;
  --text: #233229;
  --muted: #627067;
  --border: #d9ddd7;
  --border-strong: #b9c3bb;
  --primary: #315f45;
  --primary-hover: #244c36;
  --primary-soft: #e4eee7;
  --danger: #8b3e3e;
  --danger-soft: #f7e7e5;
  --success: #356247;
  --focus: #1f6feb;
  --shadow: 0 18px 45px rgba(44, 55, 47, 0.08);
  --peach: #f3d7c7;
  --blue: #d9e9ef;
  --lavender: #e5ddf0;
  --yellow: #f2e7b9;
  --green: #dce9d5;
  --lilac: #e8e0ea;
  --pink: #efd9e2;
  --neutral-theme: #e7e9e4;
}

* { box-sizing: border-box; }

html { scroll-behavior: smooth; }

body {
  margin: 0;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  font-size: 16px;
  line-height: 1.55;
}

button, input, select, textarea { font: inherit; }
button { cursor: pointer; }
a { color: var(--primary); }
img { max-width: 100%; }

.skip-link {
  position: fixed;
  top: 8px;
  left: 8px;
  z-index: 999;
  transform: translateY(-160%);
  background: var(--surface-strong);
  color: var(--text);
  border: 2px solid var(--focus);
  padding: 10px 14px;
  border-radius: 10px;
}
.skip-link:focus { transform: translateY(0); }

.site-shell {
  width: min(100% - 28px, 980px);
  margin: 0 auto;
  padding: 18px 0 42px;
}

.site-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  color: var(--text);
  text-decoration: none;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  background: var(--primary-soft);
  font-size: 21px;
}

.brand strong,
.brand span { display: block; }
.brand strong { font-size: 15px; font-weight: 700; }
.brand span { color: var(--muted); font-size: 12px; }

.header-actions { display: flex; align-items: center; gap: 8px; }

.survey-frame {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 26px;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.survey-top {
  padding: 18px 22px 16px;
  border-bottom: 1px solid var(--border);
  background: rgba(255,255,255,.55);
}

.save-status {
  min-height: 22px;
  color: var(--muted);
  font-size: 13px;
  text-align: right;
}

.stage-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 7px;
  margin: 12px 0 0;
  padding: 0;
  list-style: none;
}

.stage-item {
  min-width: 0;
  color: var(--muted);
  font-size: 12px;
}

.stage-track {
  display: block;
  width: 100%;
  height: 5px;
  margin-bottom: 6px;
  border-radius: 999px;
  background: #e4e6e1;
}

.stage-item.is-current { color: var(--text); font-weight: 650; }
.stage-item.is-current .stage-track,
.stage-item.is-complete .stage-track { background: var(--primary); }

.survey-body { padding: clamp(24px, 5vw, 52px); }

.question-wrap {
  max-width: 760px;
  margin: 0 auto;
}

.eyebrow {
  margin: 0 0 9px;
  color: var(--muted);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: .055em;
  text-transform: uppercase;
}

h1, h2, h3 { line-height: 1.2; }
h1 { margin: 0 0 14px; font-size: clamp(30px, 6vw, 52px); letter-spacing: -.035em; }
h2 { margin: 0 0 13px; font-size: clamp(25px, 4vw, 36px); letter-spacing: -.025em; }
h3 { margin: 0 0 10px; font-size: 20px; }

.lead {
  max-width: 680px;
  margin: 0 0 25px;
  color: var(--muted);
  font-size: 18px;
}

.help-text { margin: 0 0 22px; color: var(--muted); }
.small-text { color: var(--muted); font-size: 13px; }

.theme-banner {
  --theme-color: var(--neutral-theme);
  margin: 0 0 24px;
  padding: 18px 20px;
  border: 1px solid color-mix(in srgb, var(--theme-color) 72%, var(--border-strong));
  border-left: 7px solid var(--theme-color);
  border-radius: 18px;
  background: color-mix(in srgb, var(--theme-color) 36%, var(--surface));
}
.theme-banner p { margin: 0; }

.options-grid { display: grid; gap: 11px; }
.options-grid.path-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }

.option-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  min-height: 56px;
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 15px;
  background: var(--surface-strong);
  color: var(--text);
  text-align: left;
  transition: border-color .14s ease, background .14s ease, transform .14s ease;
}

.option-card:hover { transform: translateY(-1px); border-color: var(--border-strong); }
.option-card:focus-within { outline: 3px solid color-mix(in srgb, var(--focus) 28%, transparent); border-color: var(--focus); }
.option-card.is-selected {
  border-color: var(--primary);
  background: var(--primary-soft);
}

.option-card input {
  width: 18px;
  height: 18px;
  margin: 3px 0 0;
  accent-color: var(--primary);
  flex: 0 0 auto;
}

.option-copy { flex: 1; }
.option-copy strong { display: block; margin-bottom: 3px; }
.option-copy small { display: block; color: var(--muted); }

.path-card {
  --theme-color: var(--neutral-theme);
  border-left: 6px solid var(--theme-color);
  background: color-mix(in srgb, var(--theme-color) 18%, var(--surface-strong));
}
.path-card.is-selected { background: color-mix(in srgb, var(--theme-color) 50%, var(--surface-strong)); }

.question-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0 0 12px;
}
.question-meta-chip {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 5px 10px;
  border: 1px solid var(--border-strong);
  border-radius: 999px;
  background: color-mix(in srgb, var(--surface-strong) 92%, transparent);
  color: var(--muted);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.25;
}
.progress-chip {
  color: var(--text);
}
.progress-chip.is-pending {
  font-weight: 600;
}
.optional-chip {
  border-color: color-mix(in srgb, #8f7ab0 42%, var(--border));
  background: color-mix(in srgb, #e8def2 52%, var(--surface-strong));
  color: color-mix(in srgb, #5d4f70 86%, var(--text));
}
.theme-banner.is-optional-question {
  border-style: dashed;
  border-color: color-mix(in srgb, #9d8ab8 45%, var(--border));
}

.milestone-card {
  position: relative;
  display: grid;
  grid-template-columns: 78px minmax(0, 1fr);
  gap: 16px;
  margin: 0 0 20px;
  padding: 20px 22px;
  overflow: hidden;
  border: 1px solid #b9d4c0;
  border-radius: 20px;
  background:
    radial-gradient(circle at 10% 15%, rgba(243, 215, 199, .55) 0 7%, transparent 8%),
    radial-gradient(circle at 95% 18%, rgba(229, 221, 240, .58) 0 8%, transparent 9%),
    linear-gradient(135deg, #eef8ef 0%, #fbfaf2 58%, #f5eff9 100%);
  color: #233229;
  box-shadow: 0 10px 26px rgba(49, 95, 69, .08);
  animation: milestone-arrive .42s ease-out both;
}
.milestone-card::after {
  content: '';
  position: absolute;
  right: -34px;
  bottom: -48px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(242, 231, 185, .38);
  pointer-events: none;
}
.milestone-card h3 {
  margin: 0 0 8px;
  color: #233229;
  font-size: clamp(21px, 3vw, 28px);
}
.milestone-card p { color: #233229; }
.milestone-card .eyebrow { color: #4f6858; }
.milestone-card p:last-child { margin-bottom: 0; }
.milestone-copy { position: relative; z-index: 2; align-self: center; }
.milestone-actions {
  position: relative;
  z-index: 2;
  grid-column: 2;
  margin-top: 2px;
}
.btn-large { min-height: 52px; padding: 12px 18px; }

.milestone-decoration {
  position: relative;
  z-index: 2;
  width: 78px;
  min-height: 84px;
  align-self: center;
}
.milestone-plant {
  position: absolute;
  left: 13px;
  bottom: 2px;
  width: 54px;
  height: 70px;
}
.plant-pot {
  position: absolute;
  left: 13px;
  bottom: 0;
  width: 30px;
  height: 23px;
  border-radius: 4px 4px 10px 10px;
  background: #c88765;
  box-shadow: inset 0 5px 0 rgba(255,255,255,.18);
}
.plant-stem {
  position: absolute;
  left: 27px;
  bottom: 20px;
  width: 4px;
  height: 34px;
  border-radius: 999px;
  background: #4f8a5f;
  transform-origin: bottom center;
  animation: plant-sway 1.1s ease-out both;
}
.plant-leaf {
  position: absolute;
  width: 19px;
  height: 12px;
  border-radius: 100% 0 100% 0;
  background: #70a879;
}
.plant-leaf-left { left: 9px; bottom: 37px; transform: rotate(26deg); }
.plant-leaf-right { left: 30px; bottom: 45px; transform: scaleX(-1) rotate(18deg); }
.plant-leaf-top { left: 23px; bottom: 56px; width: 16px; height: 11px; transform: rotate(-25deg); }
.milestone-plant.is-grown .plant-stem { height: 41px; }
.milestone-plant.is-grown .plant-leaf-left { bottom: 40px; transform: scale(1.12) rotate(26deg); }
.milestone-plant.is-grown .plant-leaf-right { bottom: 50px; transform: scaleX(-1.12) scaleY(1.12) rotate(18deg); }
.milestone-plant.is-grown .plant-leaf-top { bottom: 64px; transform: scale(1.15) rotate(-25deg); }

.confetti {
  position: absolute;
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 2px;
  opacity: .9;
  animation: confetti-pop .48s ease-out both;
}
.confetti-a { left: 2px; top: 7px; background: #e7a982; transform: rotate(22deg); }
.confetti-b { right: 3px; top: 3px; width: 7px; height: 12px; background: #a99ac6; transform: rotate(-19deg); animation-delay: .05s; }
.confetti-c { left: 7px; top: 36px; width: 10px; height: 5px; background: #e7cf70; transform: rotate(-32deg); animation-delay: .09s; }
.confetti-d { right: 5px; top: 35px; width: 6px; height: 6px; border-radius: 50%; background: #8bb6ca; animation-delay: .12s; }

.milestone-motion-quiet { animation: none; }
.milestone-motion-quiet .plant-stem { animation: none; }
.milestone-motion-soft { animation-duration: .32s; }
.milestone-motion-celebrate { animation-duration: .46s; }

.milestone-path-complete {
  border-color: #bdd8c5;
  background:
    radial-gradient(circle at 96% 18%, rgba(243, 215, 199, .48) 0 8%, transparent 9%),
    linear-gradient(135deg, #eef8ef 0%, #fbfaf4 100%);
}
.milestone-optional-complete {
  border-color: #c7c2db;
  background:
    radial-gradient(circle at 10% 16%, rgba(195, 216, 230, .45) 0 8%, transparent 9%),
    linear-gradient(135deg, #f4f7fa 0%, #f4eff8 100%);
}
.milestone-name-complete {
  border-color: #c9c0dc;
  background:
    radial-gradient(circle at 10% 15%, rgba(242, 231, 185, .58) 0 7%, transparent 8%),
    radial-gradient(circle at 94% 18%, rgba(239, 217, 226, .58) 0 8%, transparent 9%),
    linear-gradient(135deg, #f6f1fa 0%, #fbf8eb 100%);
}
.milestone-core-complete {
  border-color: #a9cdb4;
  background:
    radial-gradient(circle at 10% 15%, rgba(242, 231, 185, .68) 0 8%, transparent 9%),
    radial-gradient(circle at 94% 18%, rgba(239, 217, 226, .64) 0 9%, transparent 10%),
    linear-gradient(135deg, #eaf7ed 0%, #fff9e8 48%, #f2edf8 100%);
  box-shadow: 0 12px 30px rgba(49, 95, 69, .11);
}

@keyframes milestone-arrive {
  from { opacity: 0; transform: translateY(7px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes confetti-pop {
  from { opacity: 0; transform: translateY(8px) scale(.65) rotate(0deg); }
  to { opacity: .9; }
}
@keyframes plant-sway {
  0% { transform: rotate(-4deg) scaleY(.94); }
  60% { transform: rotate(2deg) scaleY(1.02); }
  100% { transform: rotate(0deg) scaleY(1); }
}

.visual-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.visual-card { align-items: stretch; }
.wireframe-preview {
  display: block;
  margin-top: 12px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: color-mix(in srgb, var(--theme-color, var(--neutral-theme)) 10%, var(--surface-strong));
}
.preview-shell {
  display: grid;
  gap: 7px;
  min-height: 110px;
  padding: 6px;
  border-radius: 10px;
  background: rgba(255,255,255,.65);
}
.preview-shell.center { place-items: center; align-content: center; }
.preview-shell.veil { background: color-mix(in srgb, var(--theme-color, var(--neutral-theme)) 18%, rgba(255,255,255,.8)); }
.preview-line, .preview-card, .preview-chip, .preview-button, .preview-grid, .preview-circle, .preview-dot-row {
  display: block;
}
.preview-line { height: 10px; border-radius: 999px; background: #d7ddd6; }
.preview-line.short { width: 65%; }
.preview-line.tiny { width: 42%; }
.preview-card { height: 22px; border-radius: 10px; border: 1px solid #d4d8d2; background: #fbfcfa; }
.preview-card.big { height: 44px; }
.preview-card.mini { height: 18px; }
.preview-row { display: grid; grid-template-columns: 1fr 1fr; gap: 7px; }
.preview-chip { height: 14px; border-radius: 999px; background: #e4e8e3; }
.preview-button { width: 100%; height: 24px; border-radius: 12px; background: #dfeadf; border: 1px solid #cadeca; }
.preview-button.small { height: 18px; }
.preview-grid { height: 42px; border-radius: 12px; background-image: linear-gradient(#e1e5df 1px, transparent 1px), linear-gradient(90deg, #e1e5df 1px, transparent 1px); background-size: 12px 12px; background-color: #fcfdfb; }
.preview-circle { width: 48px; height: 48px; border-radius: 50%; border: 8px solid #dce5db; }
.preview-circle.small { width: 36px; height: 36px; border-width: 7px; }
.preview-dot-row { width: 48px; height: 8px; border-radius: 999px; background: radial-gradient(circle, #d8ddd8 2px, transparent 3px) left center / 12px 8px repeat-x; }

.text-field,
.select-field {
  display: block;
  width: 100%;
  min-height: 48px;
  border: 1px solid var(--border-strong);
  border-radius: 13px;
  background: var(--surface-strong);
  color: var(--text);
  padding: 12px 14px;
}

textarea.text-field { min-height: 145px; resize: vertical; }
.text-field:focus, .select-field:focus { outline: 3px solid color-mix(in srgb, var(--focus) 25%, transparent); border-color: var(--focus); }
.field-label { display: block; margin-bottom: 8px; font-weight: 650; }
.field-help { display: block; margin-top: 7px; color: var(--muted); font-size: 13px; }

.action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 26px;
}
.action-group { display: flex; flex-wrap: wrap; gap: 10px; }

.question-actions .btn-save-later {
  min-width: 190px;
}


.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
  border: 1px solid transparent;
  border-radius: 13px;
  padding: 10px 16px;
  text-decoration: none;
  font-weight: 700;
}
.btn:focus { outline: 3px solid color-mix(in srgb, var(--focus) 28%, transparent); outline-offset: 2px; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover { background: var(--primary-hover); }
.btn-secondary { border-color: var(--border-strong); background: var(--surface-strong); color: var(--text); }
.btn-save-later {
  border-color: color-mix(in srgb, var(--primary) 58%, var(--border-strong));
  background: var(--primary-soft);
  color: var(--text);
}
.btn-save-later:hover {
  border-color: var(--primary);
  background: color-mix(in srgb, var(--primary-soft) 72%, var(--surface-strong));
}
.btn-quiet { background: transparent; color: var(--muted); }
.btn-danger { border-color: #d6adad; background: var(--danger-soft); color: var(--danger); }
.btn[disabled] { cursor: not-allowed; opacity: .5; }

.notice {
  margin: 18px 0;
  padding: 15px 17px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: #f8faf7;
}
.notice.is-error { border-color: #d9aaaa; background: var(--danger-soft); color: #6f2929; }
.notice.is-success { border-color: #b9d0bf; background: #eaf3eb; color: #234c34; }

.consent-list { display: grid; gap: 12px; margin: 23px 0; }
.consent-row {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--surface-strong);
}
.consent-row input { width: 19px; height: 19px; margin-top: 3px; accent-color: var(--primary); }

.rating-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; }
.rating-button {
  min-height: 54px;
  border: 1px solid var(--border);
  border-radius: 13px;
  background: var(--surface-strong);
  color: var(--text);
  font-weight: 700;
}
.rating-button.is-selected { border-color: var(--primary); background: var(--primary-soft); }
.rating-labels { display: flex; justify-content: space-between; gap: 12px; margin-top: 8px; color: var(--muted); font-size: 12px; }
.word-grid { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 17px; }
.word-chip {
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface-strong);
  color: var(--text);
  padding: 8px 12px;
}
.word-chip.is-selected { border-color: var(--primary); background: var(--primary-soft); }

.summary-grid { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 12px; margin: 22px 0; }
.summary-card { padding: 16px; border: 1px solid var(--border); border-radius: 15px; background: var(--surface-strong); }
.summary-card small { display: block; margin-bottom: 5px; color: var(--muted); }
.summary-card strong { display: block; }

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(27, 37, 31, .55);
}
.modal-backdrop[hidden] { display: none; }
.modal {
  width: min(100%, 620px);
  max-height: calc(100vh - 36px);
  overflow: auto;
  border: 1px solid var(--border);
  border-radius: 22px;
  background: var(--surface);
  box-shadow: 0 22px 80px rgba(20,30,23,.24);
  padding: 24px;
}
.modal-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.modal-close { border: 0; background: transparent; color: var(--muted); font-size: 24px; line-height: 1; }
.modal-section { margin-top: 20px; padding-top: 20px; border-top: 1px solid var(--border); }

.participation-grid { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 10px; }
.conditional-panel { margin-top: 18px; padding: 18px; border: 1px solid var(--border); border-radius: 16px; background: #faf9f4; }

.page-card {
  width: min(100% - 28px, 880px);
  margin: 38px auto;
  padding: clamp(24px, 5vw, 48px);
  border: 1px solid var(--border);
  border-radius: 24px;
  background: var(--surface);
  box-shadow: var(--shadow);
}
.page-card h1 { font-size: clamp(30px, 5vw, 44px); }
.page-card h2 { margin-top: 30px; font-size: 24px; }
.page-card li { margin-bottom: 8px; }

.admin-table-wrap { overflow-x: auto; }
.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th, .admin-table td { padding: 10px; border-bottom: 1px solid var(--border); text-align: left; vertical-align: top; }
.admin-table th { font-size: 13px; color: var(--muted); }

[hidden] { display: none !important; }

@media (max-width: 720px) {
  .site-shell { width: min(100% - 18px, 980px); padding-top: 9px; }
  .site-header { align-items: flex-start; }
  .brand span { display: none; }
  .survey-frame { border-radius: 19px; }
  .survey-top { padding: 14px; }
  .survey-body { padding: 25px 18px 30px; }
  .stage-item span:last-child { display: none; }
  .stage-item.is-current span:last-child { display: inline; }
  .options-grid.path-grid,
  .summary-grid,
  .participation-grid,
  .visual-grid { grid-template-columns: 1fr; }
  .action-row { align-items: stretch; }
  .action-group { width: 100%; }
  .action-group .btn, .action-row > .btn { flex: 1; }
  .question-actions .btn-save-later { flex-basis: 100%; min-width: 0; order: 3; }
  .question-actions #question-continue { order: 2; }
  .question-actions #question-skip { order: 1; }
  .milestone-card { grid-template-columns: 58px minmax(0, 1fr); gap: 12px; padding: 17px; }
  .milestone-decoration { width: 58px; transform: scale(.86); transform-origin: left center; }
  .milestone-actions { grid-column: 1 / -1; }
  .milestone-actions .btn { width: 100%; }
  .rating-row { gap: 5px; }
  .rating-button { min-height: 48px; padding: 5px; }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after { scroll-behavior: auto !important; transition: none !important; animation: none !important; }
}

@media (prefers-color-scheme: dark) {
  :root {
    color-scheme: dark;
    --bg: #172019;
    --surface: #202b23;
    --surface-strong: #263229;
    --text: #edf3ee;
    --muted: #b5c1b8;
    --border: #3b493e;
    --border-strong: #526157;
    --primary: #82b693;
    --primary-hover: #9bc6a9;
    --primary-soft: #30483a;
    --danger: #f0b2b2;
    --danger-soft: #4d2c2c;
    --focus: #72a7ff;
    --shadow: 0 18px 45px rgba(0,0,0,.18);
    --peach: #745446;
    --blue: #405f6a;
    --lavender: #5e516f;
    --yellow: #72653a;
    --green: #506646;
    --lilac: #65586a;
    --pink: #6c4d5a;
    --neutral-theme: #4a524b;
  }
  .survey-top { background: rgba(20,27,22,.35); }
  .notice { background: #253329; }
  .conditional-panel { background: #253028; }
  .optional-chip {
    border-color: #766a88;
    background: #342e3d;
    color: #e8def2;
  }
  .theme-banner.is-optional-question {
    border-color: #665c74;
  }

  .milestone-card {
    background:
      radial-gradient(circle at 10% 15%, rgba(243, 215, 199, .48) 0 7%, transparent 8%),
      radial-gradient(circle at 95% 18%, rgba(229, 221, 240, .5) 0 8%, transparent 9%),
      linear-gradient(135deg, #eef8ef 0%, #fbfaf2 58%, #f5eff9 100%);
    border-color: #b9d4c0;
    color: #233229;
  }
  .milestone-card h3,
  .milestone-card p { color: #233229; }
  .milestone-card .eyebrow { color: #4f6858; }
}

.theme-neutral { --theme-color: var(--neutral-theme); }
.theme-peach { --theme-color: var(--peach); }
.theme-blue { --theme-color: var(--blue); }
.theme-lavender { --theme-color: var(--lavender); }
.theme-yellow { --theme-color: var(--yellow); }
.theme-green { --theme-color: var(--green); }
.theme-lilac { --theme-color: var(--lilac); }
.theme-pink { --theme-color: var(--pink); }


/* v0.2.7 — privacy-first analytics and participant recap */
.admin-dashboard,
.public-results { width: min(100% - 28px, 1180px); }

.analytics-section {
  margin-top: 34px;
  padding-top: 4px;
}
.analytics-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.analytics-card,
.anonymous-card,
.participant-summary {
  border: 1px solid var(--border);
  border-radius: 18px;
  background: var(--surface-strong);
  padding: 18px;
}
.analytics-card h3,
.anonymous-card h3 { margin-top: 0; }

.result-bars { display: grid; gap: 12px; }
.result-row-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}
.result-track {
  height: 8px;
  margin-top: 5px;
  overflow: hidden;
  border-radius: 999px;
  background: color-mix(in srgb, var(--border) 65%, transparent);
}
.result-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--accent);
}
.universe-score strong { font-size: 1.7rem; }
.universe-score span { color: var(--muted); }
.chip-cloud { display: flex; flex-wrap: wrap; gap: 8px; }
.result-chip {
  display: inline-flex;
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 6px 9px;
  font-size: 13px;
  background: var(--surface);
}

.privacy-notice {
  border-style: dashed;
}
.anonymous-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.anonymous-card dl { margin: 0; display: grid; gap: 9px; }
.anonymous-card dl > div {
  display: grid;
  grid-template-columns: minmax(110px, .75fr) 1.25fr;
  gap: 12px;
  border-bottom: 1px solid var(--border);
  padding-bottom: 8px;
}
.anonymous-card dl > div:last-child { border-bottom: 0; padding-bottom: 0; }
.anonymous-card dt { color: var(--muted); font-size: 13px; }
.anonymous-card dd { margin: 0; font-weight: 650; }

.participant-summary {
  margin: 22px 0;
  background: linear-gradient(145deg, color-mix(in srgb, var(--theme-green) 35%, var(--surface-strong)), var(--surface-strong));
}
.participant-summary h3 { margin-top: 0; }
.participant-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0,1fr));
  gap: 10px;
  margin-top: 16px;
}
.participant-summary-item {
  padding: 12px 13px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--surface-strong);
}
.participant-summary-item small {
  display: block;
  color: var(--muted);
  margin-bottom: 4px;
}
.participant-summary-item strong { display: block; }

.public-trend-card small { display:block; color:var(--muted); margin-bottom:8px; }
.public-trend-card > strong { display:block; font-size:1.15rem; }
.public-trend-card p { margin-bottom:0; color:var(--muted); }
.privacy-panel {
  margin: 28px 0;
  padding: 18px;
  border: 1px dashed var(--border);
  border-radius: 18px;
  background: var(--surface);
}

@media (max-width: 760px) {
  .analytics-grid,
  .anonymous-grid,
  .participant-summary-grid { grid-template-columns: 1fr; }
  .anonymous-card dl > div { grid-template-columns: 1fr; gap: 2px; }
}

@media (prefers-reduced-motion: reduce) {
  .analytics-card,
  .anonymous-card,
  .participant-summary { scroll-behavior: auto; }
}


/* v0.2.8 — anonymous qualitative analysis + separated contact panel */
.admin-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 24px 0 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}
.admin-tabs a {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 8px 13px;
  border: 1px solid var(--border);
  border-radius: 999px;
  text-decoration: none;
  color: var(--text);
  background: var(--surface);
  font-weight: 650;
}
.admin-tabs a:hover,
.admin-tabs a:focus-visible {
  border-color: var(--accent);
}
.admin-tabs a.is-active {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
}

.free-text-block {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed var(--border);
}
.free-text-block h4 {
  margin: 0 0 12px;
}
.free-text-answer {
  padding: 12px 13px;
  margin-top: 10px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--surface);
}
.free-text-answer small {
  display: block;
  color: var(--muted);
  font-weight: 650;
  margin-bottom: 5px;
}
.free-text-answer p {
  margin: 0;
  white-space: normal;
  overflow-wrap: anywhere;
}

.contact-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}
.contact-card {
  display: grid;
  grid-template-columns: minmax(220px, .8fr) 1.2fr;
  gap: 18px;
  align-items: center;
  padding: 15px 16px;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: var(--surface-strong);
}
.contact-email {
  font-weight: 750;
  overflow-wrap: anywhere;
}

@media (max-width: 760px) {
  .admin-tabs {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
  .admin-tabs a {
    justify-content: center;
    border-radius: 14px;
    text-align: center;
  }
  .contact-card {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}


/* v0.3.0 — make pre-test age eligibility visible before starting */
.eligibility-banner {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 14px;
  align-items: start;
  margin: 20px 0;
  padding: 16px 17px;
  border: 1px solid color-mix(in srgb, var(--accent) 45%, var(--border));
  border-radius: 18px;
  background: color-mix(in srgb, var(--theme-yellow) 45%, var(--surface-strong));
}
.eligibility-banner strong {
  display: block;
  margin-bottom: 4px;
}
.eligibility-banner p {
  margin: 0;
  color: var(--text);
  line-height: 1.45;
}
.eligibility-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  padding: 5px 9px;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  font-size: .78rem;
  font-weight: 800;
  letter-spacing: .04em;
  white-space: nowrap;
}

@media (max-width: 560px) {
  .eligibility-banner {
    grid-template-columns: 1fr;
    gap: 9px;
  }
  .eligibility-badge {
    justify-self: start;
  }
}


/* v0.3.1 — under-18 future age-appropriate research notification */
.future-youth-card {
  margin: 24px 0;
  padding: 18px;
  border: 1px solid color-mix(in srgb, var(--accent) 35%, var(--border));
  border-radius: 20px;
  background: linear-gradient(
    145deg,
    color-mix(in srgb, var(--theme-green) 35%, var(--surface-strong)),
    var(--surface-strong)
  );
}
.future-youth-card h3 {
  margin-top: 0;
}
.future-youth-card > p:not(.eyebrow):not(.small-text) {
  line-height: 1.55;
}
.future-youth-consent {
  margin-top: 14px;
}


/* v0.4.0 — required submission checkpoint + optional topic hub */
.core-submit-card,
.optional-hub-intro {
  position: relative;
  overflow: hidden;
  margin: 0 0 20px;
  padding: 22px;
  border: 1px solid color-mix(in srgb, var(--accent) 35%, var(--border));
  border-radius: 22px;
  background:
    radial-gradient(circle at 90% 10%, color-mix(in srgb, var(--theme-yellow) 45%, transparent), transparent 30%),
    linear-gradient(145deg, color-mix(in srgb, var(--theme-green) 40%, var(--surface-strong)), var(--surface-strong));
}

.core-submit-card h2,
.optional-hub-intro h2 {
  margin-top: 0;
}

.optional-hub-status {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px 18px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
}
.optional-hub-status span {
  color: var(--muted);
}

.optional-topic-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 20px 0;
}

.optional-topic-card {
  display: flex;
  min-height: 190px;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  border: 1px solid var(--border);
  border-radius: 18px;
  background: var(--surface-strong);
}

.optional-topic-card.is-complete {
  background: color-mix(in srgb, var(--theme-green) 20%, var(--surface-strong));
}

.optional-topic-card h3 {
  margin: 6px 0 8px;
}

.optional-topic-card p {
  margin: 0;
  color: var(--muted);
  line-height: 1.48;
}

.optional-topic-status {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 5px 8px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface);
  color: var(--muted);
  font-size: .78rem;
  font-weight: 750;
}

.optional-topic-card.is-complete .optional-topic-status {
  border-color: color-mix(in srgb, var(--accent) 45%, var(--border));
  color: var(--text);
}

.optional-hub-actions {
  margin-top: 20px;
}

@media (max-width: 760px) {
  .optional-topic-grid {
    grid-template-columns: 1fr;
  }
  .optional-topic-card {
    min-height: auto;
  }
}


/* v0.4.1 — standalone future age-appropriate research notification */
.standalone-future-page {
  max-width: 920px;
}

.standalone-future-page h1 {
  margin-top: 0;
}

.privacy-mini-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 16px 0 22px;
}

.privacy-mini-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 13px 14px;
  border: 1px solid var(--border);
  border-radius: 15px;
  background: var(--surface-strong);
}

.privacy-mini-item span {
  color: var(--muted);
  line-height: 1.45;
}

.future-direct-link {
  margin-top: 14px;
}

@media (max-width: 680px) {
  .privacy-mini-grid {
    grid-template-columns: 1fr;
  }
}


/* v0.4.2 — gentle readiness notice before consent */
.readiness-card {
  display: grid;
  gap: 12px;
  margin: 18px 0 20px;
}

.readiness-item {
  padding: 16px 17px;
  border: 1px solid var(--border);
  border-radius: 18px;
  background: var(--surface-strong);
}

.readiness-item strong {
  display: block;
  margin-bottom: 5px;
}

.readiness-item p {
  margin: 0;
  color: var(--muted);
  line-height: 1.5;
}

</style>
<style>
/* v0.4.7 — funnel styles are intentionally embedded only in admin/index.php */
.funnel-admin-section h3{margin-top:28px}.admin-funnel-grid .summary-card{min-height:118px}.admin-funnel-note{display:block;margin-top:6px;color:var(--muted);font-size:.78rem;line-height:1.38}.admin-funnel-state{position:relative;overflow:hidden}.admin-funnel-state:before{content:"";position:absolute;inset:0 auto 0 0;width:4px;background:var(--border)}.admin-funnel-state.state-recent:before{background:color-mix(in srgb,var(--theme-green) 72%,var(--text))}.admin-funnel-state.state-watch:before{background:color-mix(in srgb,var(--theme-yellow) 72%,var(--text))}.admin-funnel-state.state-drop:before,.admin-funnel-state.state-old:before{background:color-mix(in srgb,var(--theme-peach) 72%,var(--text))}.admin-funnel-table-wrap{overflow-x:auto;margin-top:14px;border:1px solid var(--border);border-radius:18px;background:var(--surface-strong)}.admin-funnel-table{width:100%;min-width:680px;border-collapse:collapse}.admin-funnel-table th,.admin-funnel-table td{padding:12px 14px;text-align:left;vertical-align:top;border-bottom:1px solid var(--border)}.admin-funnel-table th{color:var(--muted);font-size:.78rem;font-weight:800}.admin-funnel-table tbody tr:last-child td{border-bottom:0}.admin-funnel-table td code{display:block;width:fit-content;margin-top:4px;color:var(--muted);font-size:.72rem}.admin-funnel-table tr.has-dropoff td:first-child{box-shadow:inset 3px 0 0 color-mix(in srgb,var(--theme-peach) 72%,var(--text))}@media(max-width:720px){.admin-funnel-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:480px){.admin-funnel-grid{grid-template-columns:1fr}}
</style>
</head>
<body>
<main class="page-card admin-dashboard">
  <p class="eyebrow">Le Potager Lab</p>
  <h1>Research dashboard</h1>

  <?php if (!$authenticated): ?>
    <p class="lead">Private access to aggregate research results and exports.</p>
    <?php if ($error !== ''): ?><div class="notice is-error"><?= h($error) ?></div><?php endif; ?>

    <form method="post" style="max-width:460px">
      <input type="hidden" name="login" value="1">
      <label class="field-label" for="username">Username</label>
      <input class="text-field" id="username" name="username" autocomplete="username" required>

      <label class="field-label" for="password" style="margin-top:14px">Password</label>
      <input class="text-field" id="password" name="password" type="password" autocomplete="current-password" required>

      <div class="action-row">
        <button class="btn btn-primary" type="submit">Sign in</button>
      </div>
    </form>
  <?php else: ?>

    <?php if ($error !== ''): ?><div class="notice is-error"><?= h($error) ?></div><?php endif; ?>
    <?php if ($notice !== ''): ?><div class="notice is-success"><?= h($notice) ?></div><?php endif; ?>

    <div class="action-row">
      <div class="action-group">
        <a class="btn btn-primary" href="../results.php" target="_blank" rel="noopener">Open shareable results</a>
      </div>
      <a class="btn btn-quiet" href="?logout=1">Sign out</a>
    </div>

    <div class="summary-grid admin-kpi-grid">
      <div class="summary-card"><small>Unfinished sessions</small><strong><?= $stats['active'] ?></strong></div>
      <div class="summary-card"><small>Required responses submitted</small><strong><?= $stats['completed'] ?></strong></div>
      <div class="summary-card"><small>Confirmed contacts</small><strong><?= $stats['contacts_active'] ?></strong></div>
      <div class="summary-card"><small>Awaiting confirmation</small><strong><?= $stats['contacts_pending'] ?></strong></div>
    </div>

    <nav class="admin-tabs" aria-label="Research dashboard sections">
      <a class="<?= $tab === 'summary' ? 'is-active' : '' ?>" href="?tab=summary">Results</a>
      <a class="<?= $tab === 'funnel' ? 'is-active' : '' ?>" href="?tab=funnel">Drop-off / funnel</a>
      <a class="<?= $tab === 'responses' ? 'is-active' : '' ?>" href="?tab=responses">Anonymous responses</a>
      <a class="<?= $tab === 'contacts' ? 'is-active' : '' ?>" href="?tab=contacts">People to contact</a>
      <a class="<?= $tab === 'exports' ? 'is-active' : '' ?>" href="?tab=exports">Exports & retention</a>
    </nav>

    <?php if ($tab === 'summary'): ?>
      <div class="notice">
        <strong>Interpretation note.</strong> This is an early pre-test sample, not a representative estimate of people with ADHD. Read these results as signals about these respondents and about the questionnaire itself.
      </div>

      <?php foreach ($sections as $sectionTitle => $questions): ?>
        <section class="analytics-section">
          <h2><?= h($sectionTitle) ?></h2>
          <div class="analytics-grid">
            <?php foreach ($questions as [$qid, $title]): ?>
              <?php $rows = analytics_labeled_counts($sessions, $qid); ?>
              <article class="analytics-card">
                <h3><?= h($title) ?></h3>
                <?php if (!$rows): ?>
                  <p class="small-text">No completed answers yet.</p>
                <?php else: ?>
                  <?php $maxCount = max(array_column($rows, 'count')); ?>
                  <div class="result-bars">
                    <?php foreach ($rows as $row): ?>
                      <?php $pct = $maxCount > 0 ? max(5, (int) round(($row['count'] / $maxCount) * 100)) : 0; ?>
                      <div class="result-row">
                        <div class="result-row-head">
                          <span><?= h($row['label']) ?></span>
                          <strong><?= (int) $row['count'] ?></strong>
                        </div>
                        <div class="result-track" aria-hidden="true"><span style="width:<?= $pct ?>%"></span></div>
                      </div>
                    <?php endforeach; ?>
                  </div>
                <?php endif; ?>
              </article>
            <?php endforeach; ?>
          </div>
        </section>
      <?php endforeach; ?>

      <section class="analytics-section">
        <h2>Name-universe ratings</h2>
        <div class="analytics-grid">
          <?php foreach ($universeStats as $name => $info): ?>
            <article class="analytics-card">
              <h3><?= h(ucfirst($name)) ?></h3>
              <?php if ($info['average'] === null): ?>
                <p class="small-text">No rating yet.</p>
              <?php else: ?>
                <p class="universe-score">
                  <strong><?= h(number_format((float) $info['average'], 1)) ?>/5</strong>
                  <span>average fit · n=<?= (int) $info['n'] ?></span>
                </p>
                <?php if ($info['words']): ?>
                  <div class="chip-cloud">
                    <?php foreach (array_slice($info['words'], 0, 5, true) as $word => $count): ?>
                      <span class="result-chip"><?= h(str_replace('_', ' ', ucfirst($word))) ?> · <?= (int) $count ?></span>
                    <?php endforeach; ?>
                  </div>
                <?php endif; ?>
              <?php endif; ?>
            </article>
          <?php endforeach; ?>
        </div>
      </section>

    <?php elseif ($tab === 'funnel'): ?>
      <section class="analytics-section funnel-admin-section">
        <h2>Completion funnel & probable drop-off</h2>
        <div class="notice">
          <strong>An open session is not automatically an abandonment.</strong>
          Activity under 30 minutes is treated as probably still in progress. Once the required part has been submitted, leaving the optional hub does not make the questionnaire incomplete.
        </div>

        <div class="summary-grid admin-funnel-grid">
          <div class="summary-card"><small>Sessions started</small><strong><?= (int) $funnel['started'] ?></strong><span class="admin-funnel-note">Created after consent</span></div>
          <div class="summary-card"><small>Required part submitted</small><strong><?= (int) $funnel['required_submitted'] ?></strong><span class="admin-funnel-note"><?= h(number_format((float) $funnel['required_rate'], 1)) ?>% of started sessions</span></div>
          <div class="summary-card"><small>Optional part still open</small><strong><?= (int) $funnel['optional_open'] ?></strong><span class="admin-funnel-note">Already valid responses</span></div>
          <div class="summary-card"><small>Fully finished</small><strong><?= (int) $funnel['finished'] ?></strong><span class="admin-funnel-note">Explicit Finish action</span></div>
        </div>

        <h3>Open required-part sessions</h3>
        <div class="summary-grid admin-funnel-grid">
          <div class="summary-card admin-funnel-state state-recent"><small>Recent activity</small><strong><?= (int) $funnel['active_recent'] ?></strong><span class="admin-funnel-note">&lt;30 min · probably still in progress</span></div>
          <div class="summary-card admin-funnel-state state-watch"><small>Idle 30–60 min</small><strong><?= (int) $funnel['active_30m_1h'] ?></strong><span class="admin-funnel-note">Pause / early drop-off signal</span></div>
          <div class="summary-card admin-funnel-state state-drop"><small>Idle 1–24 h</small><strong><?= (int) $funnel['active_1h_24h'] ?></strong><span class="admin-funnel-note">Probable drop-off, return still possible</span></div>
          <div class="summary-card admin-funnel-state state-old"><small>Idle 24 h+</small><strong><?= (int) $funnel['active_24h_plus'] ?></strong><span class="admin-funnel-note">Strongest abandonment signal</span></div>
        </div>

        <h3>Last 24 hours</h3>
        <div class="summary-grid admin-funnel-grid">
          <div class="summary-card"><small>Started</small><strong><?= (int) $funnel['started_24h'] ?></strong></div>
          <div class="summary-card"><small>Required submitted</small><strong><?= (int) $funnel['required_24h'] ?></strong></div>
          <div class="summary-card"><small>Fully finished</small><strong><?= (int) $funnel['finished_24h'] ?></strong></div>
          <div class="summary-card"><small>Required-submit snapshot</small><strong><?= h(number_format((float) $funnel['required_rate_24h'], 1)) ?>%</strong><span class="admin-funnel-note">Snapshot, not cohort survival</span></div>
        </div>

        <h3>Where open sessions currently stop</h3>
        <p class="small-text">Aggregated only: no session ID, IP address, email, individual timestamp or individual history is displayed.</p>
        <?php if (!$dropoffSteps): ?>
          <div class="notice is-success">No unfinished required-part sessions are currently stored.</div>
        <?php else: ?>
          <div class="admin-funnel-table-wrap">
            <table class="admin-funnel-table">
              <thead><tr><th>Current step</th><th>Open</th><th>&lt;30m</th><th>30–60m</th><th>1–24h</th><th>24h+</th></tr></thead>
              <tbody>
                <?php foreach ($dropoffSteps as $row): ?>
                  <?php $probableDropoff=(int)$row['between_30m_1h']+(int)$row['between_1h_24h']+(int)$row['over_24h']; ?>
                  <tr class="<?= $probableDropoff > 0 ? 'has-dropoff' : '' ?>">
                    <td><strong><?= h(admin_funnel_step_label((string)$row['current_step'])) ?></strong><code><?= h((string)$row['current_step']) ?></code></td>
                    <td><strong><?= (int)$row['total_open'] ?></strong></td>
                    <td><?= (int)$row['under_30m'] ?></td>
                    <td><?= (int)$row['between_30m_1h'] ?></td>
                    <td><?= (int)$row['between_1h_24h'] ?></td>
                    <td><?= (int)$row['over_24h'] ?></td>
                  </tr>
                <?php endforeach; ?>
              </tbody>
            </table>
          </div>
        <?php endif; ?>

        <div class="notice" style="margin-top:22px">
          <strong>Best signal:</strong> if several idle sessions concentrate on the same step, inspect that question or transition first. One unfinished session alone is not enough to call a question problematic.
        </div>
      </section>

    <?php elseif ($tab === 'responses'): ?>
      <section class="analytics-section">
        <h2>Anonymous individual responses</h2>

        <div class="notice privacy-notice">
          <strong>Answers stay grouped for analysis, but identities do not.</strong>
          Each card keeps the selected answers and free-text comments from the same questionnaire together.
          Cards are reshuffled on every page load and contain no response ID, date, time, contact details,
          demographic/diagnostic fields or stable row number.
          The internal test response marked <code>test de Jasmin</code> is excluded automatically.
        </div>

        <div class="notice">
          <strong>Free-text warning.</strong> A participant can identify themselves inside what they write.
          The system does not add identity data to these comments, but treat free text as private research material.
        </div>

        <?php if (!$individuals): ?>
          <p>No completed responses yet.</p>
        <?php else: ?>
          <div class="anonymous-grid">
            <?php
            $titles = [
                'recent_difficulty' => 'Recent difficulty',
                'primary_path' => 'Primary area',
                'secondary_path' => 'Second area',
                'visual_home_density' => 'Home',
                'visual_focus_surface' => 'Focus view',
                'visual_quick_capture' => 'Quick capture',
                'visual_return_screen' => 'Return screen',
                'progress_style' => 'Progress',
                'personality' => 'Personality',
                'name_first' => 'First name',
                'name_final' => 'Final name',
                'survey_ease' => 'Survey ease',
                'survey_save_helpful' => 'Save later',
                'survey_save_clear' => 'Save clarity',
            ];
            ?>

            <?php foreach ($individuals as $index => $snapshot): ?>
              <article class="anonymous-card">
                <h3>Anonymous response <?= $index + 1 ?></h3>

                <?php if (!empty($snapshot['choices'])): ?>
                  <dl>
                    <?php foreach ($titles as $qid => $label): ?>
                      <?php if (!isset($snapshot['choices'][$qid])) continue; ?>
                      <div>
                        <dt><?= h($label) ?></dt>
                        <dd><?= h($snapshot['choices'][$qid]) ?></dd>
                      </div>
                    <?php endforeach; ?>
                  </dl>
                <?php endif; ?>

                <?php if (!empty($snapshot['free_text'])): ?>
                  <div class="free-text-block">
                    <h4>Free-text answers</h4>
                    <?php foreach ($snapshot['free_text'] as $item): ?>
                      <div class="free-text-answer">
                        <small><?= h($item['label']) ?></small>
                        <p><?= nl2br(h($item['text'])) ?></p>
                      </div>
                    <?php endforeach; ?>
                  </div>
                <?php endif; ?>
              </article>
            <?php endforeach; ?>
          </div>
        <?php endif; ?>
      </section>

    <?php elseif ($tab === 'contacts'): ?>
      <section class="analytics-section">
        <h2>People who chose to be contacted</h2>

        <div class="notice privacy-notice">
          <strong>Separate contact list.</strong>
          This tab contains only confirmed email addresses and the panels people selected.
          Survey session links are not displayed and are actively removed from the database when the admin dashboard is opened.
        </div>

        <?php if ($stats['contacts_pending'] > 0): ?>
          <p class="small-text"><?= $stats['contacts_pending'] ?> additional contact request(s) are awaiting email confirmation and are not listed here yet.</p>
        <?php endif; ?>

        <?php if (!$contacts): ?>
          <p>No confirmed contact preferences yet.</p>
        <?php else: ?>
          <div class="contact-list">
            <?php foreach ($contacts as $contact): ?>
              <article class="contact-card">
                <div class="contact-email"><?= h($contact['email']) ?></div>
                <div class="chip-cloud">
                  <?php foreach ($contact['panels'] as $panel): ?>
                    <span class="result-chip"><?= h($panel) ?></span>
                  <?php endforeach; ?>
                </div>
              </article>
            <?php endforeach; ?>
          </div>
        <?php endif; ?>
      </section>

    <?php elseif ($tab === 'exports'): ?>
      <section class="analytics-section">
        <h2>Exports</h2>

        <div class="analytics-grid">
          <article class="analytics-card">
            <h3>Anonymous survey export</h3>
            <p>Completed questionnaire answers remain grouped by a temporary response number, but the export contains no stored session UUID and no dates or times. The numbering is randomised on every export.</p>
            <a class="btn btn-secondary" href="?tab=exports&amp;export=responses">Export anonymous survey CSV</a>
          </article>

          <article class="analytics-card">
            <h3>Confirmed contact panels</h3>
            <p>Contains only confirmed email addresses and selected panels. It contains no survey response identifier, dates, names or participant notes.</p>
            <a class="btn btn-secondary" href="?tab=exports&amp;export=contacts">Export contacts CSV</a>
          </article>
        </div>

        <h2 style="margin-top:32px">Retention cleanup</h2>
        <p>Deletes expired unfinished responses, completed responses beyond the configured retention period and unconfirmed participation requests older than 30 days.</p>

        <form method="post">
          <input type="hidden" name="csrf" value="<?= h((string) $_SESSION['admin_csrf']) ?>">
          <button class="btn btn-danger" type="submit" name="cleanup" value="1">Run cleanup now</button>
        </form>
      </section>
    <?php endif; ?>

  <?php endif; ?>
</main>
</body>
</html>
