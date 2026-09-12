<?php
declare(strict_types=1);
require __DIR__ . '/_common.php';

require_same_origin();
$pdo = db();

if (!v2_tables_ready($pdo)) {
    json_response(['ok' => false, 'error' => 'V2 database tables are not installed yet.'], 503);
}

$action = clean_text($_GET['action'] ?? '', 60);
$input = read_json_body();

try {
    switch ($action) {
        case 'start_participant':
            if (($input['participant_agreement'] ?? false) !== true) {
                json_response(['ok' => false, 'error' => 'Participant agreement is required.'], 422);
            }

            $cohort = clean_text($input['cohort'] ?? '', 20);
            if (!in_array($cohort, ['adult', 'minor'], true)) {
                json_response(['ok' => false, 'error' => 'Invalid participant cohort.'], 422);
            }

            if ($cohort === 'minor') {
                $route = clean_text($input['minor_route'] ?? '', 40);
                $allowedRoutes = [
                    'eu_supported',
                    'us_13_17',
                    'ca_other_13_17',
                    'ca_qc_14_17',
                    'ch_14_17',
                ];
                if (!in_array($route, $allowedRoutes, true)) {
                    json_response(['ok' => false, 'error' => 'This minor participation route is not currently supported.'], 422);
                }
            }

            // The minor route is deliberately validated but NOT persisted.
            $session = v2_create_session($pdo, $cohort);
            json_response(['ok' => true, 'token' => $session['token']]);

        case 'start_adult':
            if (($input['consent'] ?? false) !== true) {
                json_response(['ok' => false, 'error' => 'Consent is required.'], 422);
            }
            $session = v2_create_session($pdo, 'adult');
            json_response(['ok' => true, 'token' => $session['token']]);

        case 'guardian_request':
            // v1.2.2: no new parent/guardian email is collected.
            json_response([
                'ok' => false,
                'error' => 'The parent/guardian email route is no longer used for new participants.'
            ], 410);

        case 'guardian_status':
            $poll = clean_text($input['poll_token'] ?? '', 200);
            if ($poll === '') json_response(['ok' => false, 'error' => 'Missing permission token.'], 422);

            $stmt = $pdo->prepare(
                "SELECT * FROM survey_v2_guardian_consents
                 WHERE poll_token_hash = ?
                 LIMIT 1"
            );
            $stmt->execute([token_hash($poll)]);
            $consent = $stmt->fetch();
            if (!$consent) json_response(['ok' => false, 'error' => 'Permission request not found.'], 404);

            if ($consent['status'] === 'pending' && strtotime((string)$consent['expires_at']) < time()) {
                $pdo->prepare(
                    "UPDATE survey_v2_guardian_consents
                     SET status='expired', email_encrypted=NULL, updated_at=NOW()
                     WHERE id=?"
                )->execute([$consent['id']]);
                $consent['status'] = 'expired';
            }

            if ($consent['status'] === 'confirmed') {
                if (!empty($consent['survey_session_id'])) {
                    json_response(['ok' => true, 'status' => 'confirmed', 'already_started' => true]);
                }
                $session = v2_create_session($pdo, 'minor', (string)$consent['id']);
                json_response(['ok' => true, 'status' => 'confirmed', 'token' => $session['token']]);
            }

            json_response(['ok' => true, 'status' => $consent['status']]);

        case 'save':
            $token = clean_text($input['token'] ?? '', 200);
            $session = v2_session_from_token($pdo, $token);
            if (!$session || $session['status'] === 'completed') {
                json_response(['ok' => false, 'error' => 'This questionnaire session is not editable.'], 409);
            }
            $questionId = clean_text($input['question_id'] ?? '', 100);
            v2_save_answer($pdo, (string)$session['id'], $questionId, $input['answer'] ?? null);
            $step = clean_text($input['current_step'] ?? $questionId, 100);
            $pdo->prepare(
                "UPDATE survey_v2_sessions SET current_step=?, updated_at=NOW() WHERE id=?"
            )->execute([$step, $session['id']]);
            json_response(['ok' => true]);

        case 'schedule_reminder':
            $token = clean_text($input['token'] ?? '', 200);
            $session = v2_session_from_token($pdo, $token);
            if (!$session || !in_array($session['status'], ['active','core_submitted'], true)) {
                json_response(['ok' => false, 'error' => 'This questionnaire session cannot receive a reminder.'], 409);
            }
            if (($session['cohort'] ?? '') !== 'adult') {
                // Safeguard kept from the current V2 research protocol:
                // do not collect a young participant's email address.
                json_response(['ok' => false, 'error' => 'Email reminders are available only to adult participants in this research round.'], 403);
            }

            $email = mb_strtolower(trim((string) ($input['email'] ?? '')));
            if (!valid_email($email)) {
                json_response(['ok' => false, 'error' => 'Enter a valid email address.'], 422);
            }

            $rawReminder = clean_text($input['reminder_at'] ?? '', 80);
            try {
                $reminder = new DateTimeImmutable($rawReminder);
            } catch (Throwable) {
                json_response(['ok' => false, 'error' => 'The reminder date is invalid.'], 422);
            }
            $utc = new DateTimeZone('UTC');
            $now = new DateTimeImmutable('now', $utc);
            $reminderUtc = $reminder->setTimezone($utc);
            if ($reminderUtc <= $now->modify('+5 minutes') || $reminderUtc > $now->modify('+1 year')) {
                json_response(['ok' => false, 'error' => 'Choose a reminder at least five minutes from now and within one year.'], 422);
            }

            $language = (($input['lang'] ?? 'en') === 'fr') ? 'fr' : 'en';
            v2_ensure_resume_reminders_table($pdo);
            $stmt = $pdo->prepare(
                "INSERT INTO survey_v2_resume_reminders
                    (id, session_id, email_encrypted, email_hash, resume_token_encrypted, language, reminder_at, created_at, updated_at)
                 VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                 ON DUPLICATE KEY UPDATE
                    email_encrypted=VALUES(email_encrypted),
                    email_hash=VALUES(email_hash),
                    resume_token_encrypted=VALUES(resume_token_encrypted),
                    language=VALUES(language),
                    reminder_at=VALUES(reminder_at),
                    updated_at=NOW()"
            );
            $stmt->execute([
                uuid_v4(),
                $session['id'],
                encrypt_string($email),
                email_hash($email),
                encrypt_string($token),
                $language,
                $reminderUtc->format('Y-m-d H:i:s'),
            ]);
            json_response(['ok' => true, 'reminder_at' => $reminderUtc->format(DATE_ATOM)]);

        case 'cancel_reminder':
            $token = clean_text($input['token'] ?? '', 200);
            $session = v2_session_from_token($pdo, $token);
            if (!$session) json_response(['ok' => true]);
            try {
                v2_ensure_resume_reminders_table($pdo);
                $pdo->prepare('DELETE FROM survey_v2_resume_reminders WHERE session_id=?')->execute([$session['id']]);
            } catch (Throwable $reminderError) {
                error_log('[ADHD survey V2 reminder cancel] ' . $reminderError->getMessage());
            }
            json_response(['ok' => true]);

        case 'submit_core':
            $token = clean_text($input['token'] ?? '', 200);
            $session = v2_session_from_token($pdo, $token);
            if (!$session) json_response(['ok' => false, 'error' => 'Session not found.'], 404);

            $answers = v2_load_answers($pdo, (string)$session['id']);
            $required = [
                'recent_difficulty',
                'visual_home_density',
                'today_model',
                'visual_quick_capture',
                'inbox_review',
                'visual_return_screen',
                'progress_style',
                'abandon_risks',
                'mvp_top5',
                'mvp_one',
                'survey_ease'
            ];
            foreach ($required as $q) {
                if (!array_key_exists($q, $answers)) {
                    json_response(['ok' => false, 'error' => 'A required answer is missing.'], 422);
                }
            }

            $difficulty = (string)($answers['recent_difficulty'] ?? '');
            $adaptiveId = null;
            if ($difficulty === 'beginning') {
                $adaptiveId = 'adaptive_start';
            } elseif (in_array($difficulty, ['staying','time','switching'], true)) {
                $adaptiveId = 'adaptive_focus';
            } elseif (in_array($difficulty, ['deciding','returning','remembering'], true)) {
                $adaptiveId = 'adaptive_planning';
            }
            if ($adaptiveId !== null && !array_key_exists($adaptiveId, $answers)) {
                json_response(['ok' => false, 'error' => 'The adaptive required answer is missing.'], 422);
            }

            $topFive = $answers['mvp_top5'] ?? [];
            if (!is_array($topFive) || count(array_unique($topFive)) !== 5) {
                json_response(['ok' => false, 'error' => 'Choose exactly five MVP priorities.'], 422);
            }
            $one = (string)($answers['mvp_one'] ?? '');
            if ($one === '' || !in_array($one, $topFive, true)) {
                json_response(['ok' => false, 'error' => 'The single MVP priority must be one of the five selected choices.'], 422);
            }

            $pdo->prepare(
                "UPDATE survey_v2_sessions
                 SET status='core_submitted', current_step='optional_hub',
                     core_submitted_at=COALESCE(core_submitted_at,NOW()), updated_at=NOW()
                 WHERE id=?"
            )->execute([$session['id']]);
            json_response(['ok' => true]);

        case 'finish':
            $token = clean_text($input['token'] ?? '', 200);
            $session = v2_session_from_token($pdo, $token);
            if (!$session) json_response(['ok' => false, 'error' => 'Session not found.'], 404);
            if (!in_array($session['status'], ['core_submitted','completed'], true)) {
                json_response(['ok' => false, 'error' => 'Submit the required section first.'], 409);
            }
            $pdo->prepare(
                "UPDATE survey_v2_sessions
                 SET status='completed', current_step='completed',
                     completed_at=COALESCE(completed_at,NOW()), updated_at=NOW()
                 WHERE id=?"
            )->execute([$session['id']]);
            try {
                v2_ensure_resume_reminders_table($pdo);
                $pdo->prepare('DELETE FROM survey_v2_resume_reminders WHERE session_id=?')->execute([$session['id']]);
            } catch (Throwable $reminderError) {
                error_log('[ADHD survey V2 reminder cleanup] ' . $reminderError->getMessage());
            }
            json_response(['ok' => true]);

        case 'resume':
            $token = clean_text($input['token'] ?? '', 200);
            $session = v2_session_from_token($pdo, $token);
            if (!$session) json_response(['ok' => false, 'error' => 'Session not found.'], 404);
            json_response([
                'ok' => true,
                'session' => [
                    'status' => $session['status'],
                    'current_step' => $session['current_step'],
                    'cohort' => $session['cohort'],
                ],
                'answers' => v2_load_answers($pdo, (string)$session['id'])
            ]);

        case 'delete':
            $token = clean_text($input['token'] ?? '', 200);
            $session = v2_session_from_token($pdo, $token);
            if ($session) {
                $pdo->prepare("DELETE FROM survey_v2_sessions WHERE id=?")->execute([$session['id']]);
            }
            json_response(['ok' => true]);

        default:
            json_response(['ok' => false, 'error' => 'Unknown V2 API action.'], 404);
    }
} catch (Throwable $e) {
    error_log('[ADHD survey V2] ' . $e->getMessage());
    json_response([
        'ok' => false,
        'error' => config('app_env') === 'development' ? $e->getMessage() : 'A technical error occurred. Please try again.'
    ], 500);
}
