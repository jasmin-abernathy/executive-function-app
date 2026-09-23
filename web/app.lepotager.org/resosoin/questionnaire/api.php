<?php

declare(strict_types=1);

require __DIR__ . '/_common.php';

header('Cache-Control: no-store');
resosoin_require_same_origin();

$pdo = db();
if (!resosoin_tables_ready($pdo)) {
    json_response(['ok' => false, 'error' => 'Étude temporairement indisponible.'], 503);
}

$input = read_json_body();
$action = (string) ($input['action'] ?? '');

try {
    if ($action === 'start') {
        $audience = (string) ($input['audience'] ?? '');
        if (!in_array($audience, ['doctor', 'patient'], true)) {
            throw new DomainException('Profil de questionnaire invalide.');
        }

        $token = random_token(32);
        $sessionId = uuid_v4();
        $questions = resosoin_question_map($audience);
        $firstQuestion = (string) (array_key_first($questions) ?? '');

        $stmt = $pdo->prepare(
            'INSERT INTO resosoin_survey_sessions(id, audience, resume_token_hash, current_step)
             VALUES(?, ?, ?, ?)'
        );
        $stmt->execute([
            $sessionId,
            $audience,
            resosoin_token_hash($token),
            $firstQuestion,
        ]);

        json_response([
            'ok' => true,
            'token' => $token,
            'session' => [
                'audience' => $audience,
                'status' => 'active',
                'current_step' => $firstQuestion,
                'answers' => [],
            ],
        ], 201);
    }

    $token = resosoin_bearer();
    $session = resosoin_session_from_token($pdo, $token);
    if (!$session) {
        json_response(['ok' => false, 'error' => 'Session introuvable ou supprimée.'], 401);
    }

    $audience = (string) $session['audience'];
    $questions = resosoin_question_map($audience);

    if ($action === 'resume') {
        json_response([
            'ok' => true,
            'session' => [
                'audience' => $audience,
                'status' => $session['status'],
                'current_step' => $session['current_step'],
                'answers' => resosoin_answer_map($pdo, (string) $session['id']),
            ],
        ]);
    }

    if ($action === 'save') {
        if ($session['status'] !== 'active') {
            throw new DomainException('Ce questionnaire est déjà envoyé.');
        }
        $questionId = (string) ($input['question_id'] ?? '');
        $question = $questions[$questionId] ?? null;
        if (!is_array($question)) {
            throw new DomainException('Question inconnue.');
        }
        $answer = resosoin_validate_answer($question, $input['answer'] ?? null);
        $currentStep = (string) ($input['current_step'] ?? $questionId);
        if ($currentStep !== '' && !array_key_exists($currentStep, $questions)) {
            $currentStep = $questionId;
        }

        $stmt = $pdo->prepare(
            'INSERT INTO resosoin_survey_answers(session_id, question_id, answer_json)
             VALUES(?, ?, ?)
             ON DUPLICATE KEY UPDATE answer_json = VALUES(answer_json), updated_at = NOW()'
        );
        $stmt->execute([
            $session['id'],
            $questionId,
            json_encode($answer, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        ]);
        $pdo->prepare(
            'UPDATE resosoin_survey_sessions SET current_step = ?, updated_at = NOW() WHERE id = ?'
        )->execute([$currentStep, $session['id']]);

        json_response(['ok' => true]);
    }

    if ($action === 'submit') {
        if ($session['status'] !== 'active') {
            json_response(['ok' => true, 'already_submitted' => true]);
        }
        $answers = resosoin_answer_map($pdo, (string) $session['id']);
        $missing = [];
        foreach ($questions as $questionId => $question) {
            if (!empty($question['required']) && !array_key_exists($questionId, $answers)) {
                $missing[] = $questionId;
            }
        }
        if ($missing !== []) {
            json_response([
                'ok' => false,
                'error' => 'Certaines réponses obligatoires manquent.',
                'missing' => $missing,
            ], 422);
        }

        $pdo->prepare(
            "UPDATE resosoin_survey_sessions
             SET status = 'submitted', submitted_at = NOW(), updated_at = NOW()
             WHERE id = ?"
        )->execute([$session['id']]);
        json_response(['ok' => true]);
    }

    if ($action === 'delete') {
        $pdo->prepare('DELETE FROM resosoin_survey_sessions WHERE id = ?')
            ->execute([$session['id']]);
        json_response(['ok' => true]);
    }

    json_response(['ok' => false, 'error' => 'Action inconnue.'], 404);
} catch (DomainException $exception) {
    json_response(['ok' => false, 'error' => $exception->getMessage()], 422);
} catch (Throwable $exception) {
    error_log('[RésoSoin survey] ' . $exception->getMessage());
    json_response(['ok' => false, 'error' => 'Erreur interne.'], 500);
}
