<?php

declare(strict_types=1);
require __DIR__ . '/includes/bootstrap.php';

require_same_origin();

$action = clean_text($_GET['action'] ?? '', 40);
$input = read_json_body();
$pdo = db();

try {
    switch ($action) {
        case 'start':
            $sessionId = uuid_v4();
            $token = random_token();
            $locale = in_array(($input['locale'] ?? 'en'), ['en', 'fr'], true) ? $input['locale'] : 'en';
            $stmt = $pdo->prepare('INSERT INTO survey_sessions (id, resume_token_hash, current_step, locale, consent_at) VALUES (?, ?, ?, ?, NOW())');
            $stmt->execute([$sessionId, token_hash($token), 'age_eligibility', $locale]);
            json_response(['ok' => true, 'token' => $token, 'session_id' => $sessionId]);

        case 'load':
            $token = clean_text($input['token'] ?? '', 200);
            $session = session_from_token($pdo, $token);
            if (!$session) {
                json_response(['ok' => false, 'error' => 'This resume link is invalid or has been deleted.'], 404);
            }
            $answers = [];
            if ($session['current_step'] !== 'completed') {
                $stmt = $pdo->prepare('SELECT question_id, answer_json FROM survey_answers WHERE session_id = ? ORDER BY id');
                $stmt->execute([$session['id']]);
                foreach ($stmt->fetchAll() as $row) {
                    $answers[$row['question_id']] = json_decode((string) $row['answer_json'], true);
                }
            }
            json_response([
                'ok' => true,
                'session' => [
                    'id' => $session['id'],
                    'status' => $session['status'],
                    'current_step' => $session['current_step'],
                    'primary_path' => $session['primary_path'],
                    'secondary_path' => $session['secondary_path'],
                    'updated_at' => $session['updated_at'],
                ],
                'answers' => $answers,
            ]);

        case 'save':
            $token = clean_text($input['token'] ?? '', 200);
            $session = session_from_token($pdo, $token);
            if (!$session || !survey_session_can_edit($session)) {
                json_response(['ok' => false, 'error' => 'This questionnaire session is no longer editable.'], 409);
            }
            $questionId = clean_text($input['question_id'] ?? '', 100);
            if (!preg_match('/^[a-z0-9_\-]{1,100}$/', $questionId)) {
                json_response(['ok' => false, 'error' => 'Invalid question identifier.'], 422);
            }
            $answerJson = json_encode($input['answer'] ?? null, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            if ($answerJson === false || strlen($answerJson) > 16000) {
                json_response(['ok' => false, 'error' => 'This answer is too large.'], 422);
            }
            $currentStep = valid_step($input['current_step'] ?? 'age_eligibility');
            $primary = valid_path($input['primary_path'] ?? null);
            $secondary = valid_path($input['secondary_path'] ?? null);

            $pdo->beginTransaction();
            $stmt = $pdo->prepare('INSERT INTO survey_answers (session_id, question_id, answer_json) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE answer_json = VALUES(answer_json), updated_at = NOW()');
            $stmt->execute([$session['id'], $questionId, $answerJson]);
            $stmt = $pdo->prepare('UPDATE survey_sessions SET current_step = ?, primary_path = ?, secondary_path = ?, updated_at = NOW() WHERE id = ?');
            $stmt->execute([$currentStep, $primary, $secondary, $session['id']]);
            $pdo->commit();
            json_response(['ok' => true]);

        case 'sync':
            $token = clean_text($input['token'] ?? '', 200);
            $session = session_from_token($pdo, $token);
            if (!$session || !survey_session_can_edit($session)) {
                json_response(['ok' => false, 'error' => 'This questionnaire session is no longer editable.'], 409);
            }
            $answers = is_array($input['answers'] ?? null) ? $input['answers'] : [];
            if (count($answers) > 120) {
                json_response(['ok' => false, 'error' => 'Too many answer fields.'], 422);
            }
            $pdo->beginTransaction();
            $delete = $pdo->prepare('DELETE FROM survey_answers WHERE session_id = ?');
            $delete->execute([$session['id']]);
            $stmt = $pdo->prepare('INSERT INTO survey_answers (session_id, question_id, answer_json) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE answer_json = VALUES(answer_json), updated_at = NOW()');
            foreach ($answers as $questionId => $answer) {
                $questionId = clean_text($questionId, 100);
                if (!preg_match('/^[a-z0-9_\-]{1,100}$/', $questionId)) {
                    continue;
                }
                $answerJson = json_encode($answer, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
                if ($answerJson === false || strlen($answerJson) > 16000) {
                    continue;
                }
                $stmt->execute([$session['id'], $questionId, $answerJson]);
            }
            $update = $pdo->prepare('UPDATE survey_sessions SET current_step = ?, primary_path = ?, secondary_path = ?, updated_at = NOW() WHERE id = ?');
            $update->execute([
                valid_step($input['current_step'] ?? 'age_eligibility'),
                valid_path($input['primary_path'] ?? null),
                valid_path($input['secondary_path'] ?? null),
                $session['id'],
            ]);
            $pdo->commit();
            json_response(['ok' => true]);

        case 'set_step':
            $token = clean_text($input['token'] ?? '', 200);
            $session = session_from_token($pdo, $token);
            if (!$session || !survey_session_can_edit($session)) {
                json_response(['ok' => false, 'error' => 'This questionnaire session is no longer editable.'], 409);
            }
            $stmt = $pdo->prepare('UPDATE survey_sessions SET current_step = ?, updated_at = NOW() WHERE id = ?');
            $stmt->execute([valid_step($input['current_step'] ?? 'age_eligibility'), $session['id']]);
            json_response(['ok' => true]);

        case 'pause':
            $token = clean_text($input['token'] ?? '', 200);
            $session = session_from_token($pdo, $token);
            if (!$session || !survey_session_can_edit($session)) {
                json_response(['ok' => false, 'error' => 'This questionnaire session is no longer editable.'], 409);
            }
            $email = mb_strtolower(trim((string) ($input['email'] ?? '')));
            if (!valid_email($email)) {
                json_response(['ok' => false, 'error' => 'Enter a valid email address.'], 422);
            }
            $reminderAt = null;
            if (!empty($input['reminder_at'])) {
                try {
                    $date = new DateTimeImmutable((string) $input['reminder_at']);
                } catch (Throwable) {
                    json_response(['ok' => false, 'error' => 'The reminder date is invalid.'], 422);
                }
                $now = new DateTimeImmutable('now');
                if ($date <= $now || $date > $now->modify('+1 year')) {
                    json_response(['ok' => false, 'error' => 'Choose a future reminder within one year.'], 422);
                }
                $reminderAt = $date->setTimezone(new DateTimeZone('UTC'))->format('Y-m-d H:i:s');
            }

            $stmt = $pdo->prepare(
                'INSERT INTO resume_contacts (session_id, email_encrypted, email_hash, resume_token_encrypted, reminder_at, reminder_sent_at, resume_email_sent_at) '
                . 'VALUES (?, ?, ?, ?, ?, NULL, NOW()) '
                . 'ON DUPLICATE KEY UPDATE email_encrypted = VALUES(email_encrypted), email_hash = VALUES(email_hash), resume_token_encrypted = VALUES(resume_token_encrypted), reminder_at = VALUES(reminder_at), reminder_sent_at = NULL, resume_email_sent_at = NOW(), updated_at = NOW()'
            );
            $stmt->execute([$session['id'], encrypt_string($email), email_hash($email), encrypt_string($token), $reminderAt]);

            $resumeUrl = format_site_url('?resume=' . rawurlencode($token));
            $title = 'Your private questionnaire resume link';
            $body = '<p>Your progress is saved. You can return to the same questionnaire whenever it suits you.</p>'
                . button_html('Resume the questionnaire', $resumeUrl)
                . '<p style="font-size:13px;color:#5f6f63">This email does not subscribe you to project updates.</p>';
            send_app_email($email, $title, email_layout($title, $body), "Your progress is saved. Resume here:\n{$resumeUrl}\n\nThis email does not subscribe you to project updates.");
            json_response(['ok' => true]);

        case 'submit':
            $token = clean_text($input['token'] ?? '', 200);
            $session = session_from_token($pdo, $token);
            if (!$session) {
                json_response(['ok' => false, 'error' => 'This questionnaire session could not be found.'], 404);
            }

            $phase = clean_text($input['phase'] ?? 'final', 20);

            if ($phase === 'core') {
                if ($session['current_step'] !== 'completed') {
                    $stmt = $pdo->prepare(
                        "UPDATE survey_sessions
                         SET status = 'completed',
                             current_step = 'optional_hub',
                             completed_at = COALESCE(completed_at, NOW()),
                             updated_at = NOW()
                         WHERE id = ?"
                    );
                    $stmt->execute([$session['id']]);
                }
                json_response(['ok' => true, 'phase' => 'core']);
            }

            $pdo->beginTransaction();
            $stmt = $pdo->prepare(
                "UPDATE survey_sessions
                 SET status = 'completed',
                     current_step = 'completed',
                     completed_at = COALESCE(completed_at, NOW()),
                     updated_at = NOW()
                 WHERE id = ?"
            );
            $stmt->execute([$session['id']]);

            $stmt = $pdo->prepare('DELETE FROM resume_contacts WHERE session_id = ?');
            $stmt->execute([$session['id']]);
            $pdo->commit();

            json_response(['ok' => true, 'phase' => 'final']);

        case 'participate':
            $email = mb_strtolower(trim((string) ($input['email'] ?? '')));
            if (!valid_email($email)) {
                json_response(['ok' => false, 'error' => 'Enter a valid email address.'], 422);
            }
            $allowedInterests = ['research','codesign','beta','accessibility','opensource','translation','professional','crowdfunding','updates','future_youth'];
            $interests = array_values(array_unique(array_filter(
                is_array($input['interests'] ?? null) ? $input['interests'] : [],
                static fn ($value) => in_array($value, $allowedInterests, true)
            )));
            if ($interests === []) {
                json_response(['ok' => false, 'error' => 'Choose at least one participation category.'], 422);
            }
            $preferredName = clean_text($input['preferred_name'] ?? '', 120);
            $preferredLanguage = in_array(($input['preferred_language'] ?? 'en'), ['en','fr','other'], true) ? $input['preferred_language'] : 'en';
            $details = is_array($input['details'] ?? null) ? $input['details'] : [];
            $detailsJson = json_encode($details, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            if ($detailsJson === false || strlen($detailsJson) > 20000) {
                json_response(['ok' => false, 'error' => 'The optional details are too large.'], 422);
            }
            // Privacy rule: participation contacts are never linked to survey responses.
            // Ignore any legacy/client-provided link_to_response value.
            $linkToResponse = false;
            $sessionId = null;

            $contactId = uuid_v4();
            $verifyToken = random_token();
            $unsubscribeToken = random_token();
            $stmt = $pdo->prepare(
                'INSERT INTO participation_contacts '
                . '(id, session_id, email_encrypted, email_hash, preferred_name_encrypted, preferred_language, interests_json, details_json, link_to_response, status, verification_token_hash, unsubscribe_token_hash, unsubscribe_token_encrypted, consent_at) '
                . "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending', ?, ?, ?, NOW())"
            );
            $stmt->execute([
                $contactId,
                $sessionId,
                encrypt_string($email),
                email_hash($email),
                encrypt_string($preferredName),
                $preferredLanguage,
                json_encode($interests, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
                $detailsJson,
                $linkToResponse ? 1 : 0,
                token_hash($verifyToken),
                token_hash($unsubscribeToken),
                encrypt_string($unsubscribeToken),
            ]);

            $confirmUrl = format_site_url('confirm.php?token=' . rawurlencode($verifyToken));

            if ($interests === ['future_youth']) {
                $title = 'Confirm future research notification';
                $body = '<p>Please confirm that you want to be notified if a future age-appropriate research round opens.</p>'
                    . '<p>No questionnaire response, age, name or other survey information is attached to this contact preference.</p>'
                    . button_html('Confirm this notification', $confirmUrl)
                    . '<p style="font-size:13px;color:#5f6f63">If you did not request this, you can ignore this email. The pending record will be removed automatically.</p>';
                send_app_email(
                    $email,
                    $title,
                    email_layout($title, $body),
                    "Confirm that you want to be notified about a future age-appropriate research round:\n{$confirmUrl}\n\nIf you did not request this, ignore this email."
                );
            } else {
                $title = 'Confirm your project participation preferences';
                $body = '<p>Please confirm that you want to receive messages about the project categories you selected.</p>'
                    . button_html('Confirm my preferences', $confirmUrl)
                    . '<p style="font-size:13px;color:#5f6f63">If you did not request this, you can ignore this email. The pending record will be removed automatically.</p>';
                send_app_email($email, $title, email_layout($title, $body), "Confirm your project participation preferences:\n{$confirmUrl}\n\nIf you did not request this, ignore this email.");
            }

            json_response(['ok' => true]);

        case 'delete':
            $token = clean_text($input['token'] ?? '', 200);
            $session = session_from_token($pdo, $token);
            if ($session) {
                $stmt = $pdo->prepare('DELETE FROM survey_sessions WHERE id = ?');
                $stmt->execute([$session['id']]);
            }
            json_response(['ok' => true]);

        default:
            json_response(['ok' => false, 'error' => 'Unknown API action.'], 404);
    }
} catch (Throwable $error) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    error_log('[ADHD survey] ' . $error->getMessage());
    json_response(['ok' => false, 'error' => config('app_env') === 'development' ? $error->getMessage() : 'A technical error occurred. Please try again.'], 500);
}


function survey_session_can_edit(array $session): bool
{
    if (($session['status'] ?? '') === 'active') {
        return true;
    }

    return ($session['status'] ?? '') === 'completed'
        && ($session['current_step'] ?? '') !== 'completed';
}

function valid_step(mixed $value): string
{
    $step = clean_text($value, 100);
    return preg_match('/^[a-z0-9_\-]{1,100}$/', $step) ? $step : 'age_eligibility';
}

function valid_path(mixed $value): ?string
{
    if ($value === null || $value === '') {
        return null;
    }
    $path = clean_text($value, 50);
    return in_array($path, ['start','planning','time','focus','recovery','low_energy','capture'], true) ? $path : null;
}
