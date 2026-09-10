<?php

declare(strict_types=1);

require dirname(__DIR__) . '/includes/bootstrap.php';

const V2_CONSENT_VERSION = 'v2-2026-08-14-region-routing-v1.2.2';
const V2_MIN_AGE = 13;

function v2_h(mixed $value): string
{
    return htmlspecialchars((string) $value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function v2_base_url(string $path = ''): string
{
    return rtrim((string) config('site_url'), '/') . '/v2/' . ltrim($path, '/');
}

function v2_tables_ready(PDO $pdo): bool
{
    try {
        $pdo->query('SELECT 1 FROM survey_v2_sessions LIMIT 1');
        $pdo->query('SELECT 1 FROM survey_v2_answers LIMIT 1');
        $pdo->query('SELECT 1 FROM survey_v2_guardian_consents LIMIT 1');
        return true;
    } catch (Throwable) {
        return false;
    }
}

function v2_session_from_token(PDO $pdo, string $token): ?array
{
    if ($token === '') return null;
    $stmt = $pdo->prepare(
        "SELECT * FROM survey_v2_sessions
         WHERE resume_token_hash = ?
           AND status <> 'deleted'
         LIMIT 1"
    );
    $stmt->execute([token_hash($token)]);
    $row = $stmt->fetch();
    return $row ?: null;
}

function v2_allowed_questions(): array
{
    return [
        'core_first_help',
        'core_today_view',
        'core_keep_using',
        'core_capture',
        'core_timer',
        'core_return',
        'core_must_have',
        'core_uninstall',
        'core_one_thing',
        'today_next_action',
        'today_energy',
        'today_breakdown',
        'capture_fields',
        'capture_inbox',
        'capture_future',
        'timer_drawer',
        'timer_transition',
        'timer_hyperfocus_capture',
        'timer_hyperfocus_end',
        'return_after_gap',
        'low_energy_mode',
        'progress_without_streak',
        'routine_style',
        'reminder_style',
        'calendar_role',
        'companion_visibility',
        'companion_tone',
        'room_progress',
        'reward_style',
        'privacy_default',
        'export_control',
        'pc_relay',
        'survey_length',
        'survey_emotion',
        'survey_clarity',
        'recent_difficulty',
        'adaptive_start',
        'adaptive_focus',
        'adaptive_planning',
        'visual_home_density',
        'today_model',
        'visual_quick_capture',
        'inbox_review',
        'visual_return_screen',
        'progress_style',
        'abandon_risks',
        'mvp_top5',
        'mvp_one',
        'survey_ease',
        'focus_session_style',
        'timer_drawer_actions',
        'timer_tap_pause',
        'hyperfocus_behavior',
        'hyperfocus_checkins',
        'reward_after_25',
        'reward_choice_timing',
        'reward_keep_one',
        'companion_interest',
        'companion_species',
        'companion_presence',
        'routine_model',
        'calendar_role_v120',
        'calendar_role_v122',
        'calendar_read',
        'weekly_tools',
        'setup_tolerance',
        'abandoned_tool_reason',
        'survey_friction_v2',
    ];
}

function v2_validate_answer(string $questionId, mixed $answer): mixed
{
    if (!in_array($questionId, v2_allowed_questions(), true)) {
        throw new InvalidArgumentException('Unknown questionnaire item.');
    }

    if (is_string($answer)) {
        $answer = clean_text($answer, 100);
        if ($answer === '') {
            throw new InvalidArgumentException('Empty answer.');
        }
        return $answer;
    }

    if (is_array($answer)) {
        $clean = [];
        foreach ($answer as $item) {
            if (!is_scalar($item)) continue;
            $value = clean_text($item, 100);
            if ($value !== '') $clean[] = $value;
        }
        $clean = array_values(array_unique($clean));
        if ($clean === []) {
            throw new InvalidArgumentException('Empty answer.');
        }
        if (count($clean) > 5) {
            throw new InvalidArgumentException('Too many choices.');
        }
        return $clean;
    }

    throw new InvalidArgumentException('Invalid answer type.');
}

function v2_save_answer(PDO $pdo, string $sessionId, string $questionId, mixed $answer): void
{
    $answer = v2_validate_answer($questionId, $answer);
    $json = json_encode($answer, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    if ($json === false) throw new RuntimeException('Could not encode answer.');

    $stmt = $pdo->prepare(
        "INSERT INTO survey_v2_answers
            (session_id, question_id, answer_json, created_at, updated_at)
         VALUES (?, ?, ?, NOW(), NOW())
         ON DUPLICATE KEY UPDATE
            answer_json = VALUES(answer_json),
            updated_at = NOW()"
    );
    $stmt->execute([$sessionId, $questionId, $json]);
}

function v2_load_answers(PDO $pdo, string $sessionId): array
{
    $stmt = $pdo->prepare(
        "SELECT question_id, answer_json
         FROM survey_v2_answers
         WHERE session_id = ?"
    );
    $stmt->execute([$sessionId]);
    $answers = [];
    foreach ($stmt->fetchAll() as $row) {
        $answers[(string) $row['question_id']] =
            json_decode((string) $row['answer_json'], true);
    }
    return $answers;
}

function v2_create_session(PDO $pdo, string $cohort, ?string $guardianConsentId = null): array
{
    if (!in_array($cohort, ['adult', 'minor'], true)) {
        throw new InvalidArgumentException('Invalid cohort.');
    }

    $id = uuid_v4();
    $token = random_token();
    $stmt = $pdo->prepare(
        "INSERT INTO survey_v2_sessions
            (id, resume_token_hash, cohort, status, current_step,
             guardian_consent_id, consent_text_version,
             participant_consent_at, created_at, updated_at)
         VALUES (?, ?, ?, 'active', 'recent_difficulty', ?, ?, NOW(), NOW(), NOW())"
    );
    $stmt->execute([
        $id,
        token_hash($token),
        $cohort,
        $guardianConsentId,
        V2_CONSENT_VERSION,
    ]);

    if ($guardianConsentId !== null) {
        $stmt = $pdo->prepare(
            "UPDATE survey_v2_guardian_consents
             SET survey_session_id = ?, updated_at = NOW()
             WHERE id = ?"
        );
        $stmt->execute([$id, $guardianConsentId]);
    }

    return ['id' => $id, 'token' => $token];
}


/**
 * Optional one-shot email reminders for adult V2 participants.
 * The email address is encrypted and kept outside the answer table.
 * This table is deliberately not part of v2_tables_ready(), so an older
 * installation keeps working even before somebody schedules a reminder.
 */
function v2_ensure_resume_reminders_table(PDO $pdo): void
{
    $pdo->exec(
        "CREATE TABLE IF NOT EXISTS survey_v2_resume_reminders (
          id CHAR(36) NOT NULL PRIMARY KEY,
          session_id CHAR(36) NOT NULL,
          email_encrypted TEXT NOT NULL,
          email_hash CHAR(64) NOT NULL,
          resume_token_encrypted TEXT NOT NULL,
          language ENUM('fr','en') NOT NULL DEFAULT 'en',
          reminder_at DATETIME NOT NULL,
          created_at DATETIME NOT NULL,
          updated_at DATETIME NOT NULL,
          UNIQUE KEY uniq_v2_reminder_session (session_id),
          INDEX idx_v2_reminder_due (reminder_at),
          INDEX idx_v2_reminder_email_hash (email_hash),
          CONSTRAINT fk_v2_reminder_session
            FOREIGN KEY (session_id)
            REFERENCES survey_v2_sessions(id)
            ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
    );
}
