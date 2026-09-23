<?php

declare(strict_types=1);

require __DIR__ . '/_common.php';

header('Cache-Control: no-store');
resosoin_require_same_origin();

$contentLength = (int) ($_SERVER['CONTENT_LENGTH'] ?? 0);
if ($contentLength > 16384) {
    json_response(
        ['ok' => false, 'error' => 'Requête trop volumineuse.'],
        413
    );
}

$pdo = db();
if (!resosoin_tables_ready($pdo)) {
    json_response(
        ['ok' => false, 'error' => 'Étude temporairement indisponible.'],
        503
    );
}

$input = read_json_body();
$action = (string) ($input['action'] ?? '');

try {
    if ($action === 'start') {
        if (($input['adult'] ?? false) !== true) {
            throw new DomainException(
                'Cette étude est réservée aux personnes de 18 ans ou plus.'
            );
        }
        if (($input['consent'] ?? false) !== true) {
            throw new DomainException(
                'Votre accord est nécessaire pour commencer.'
            );
        }
        if (resosoin_start_guard_active()) {
            json_response(
                [
                    'ok' => false,
                    'error' => 'Un questionnaire vient déjà d’être créé. Réessayez dans quelques secondes.',
                ],
                429
            );
        }

        $audience = (string) ($input['audience'] ?? '');
        if (!in_array($audience, ['doctor', 'patient'], true)) {
            throw new DomainException(
                'Profil de questionnaire invalide.'
            );
        }

        $source = (string) ($input['source'] ?? 'direct');
        $allowedSources = [
            'direct',
            'home',
            'resosoin_page',
            'neutral_invite',
        ];
        if (!in_array($source, $allowedSources, true)) {
            $source = 'direct';
        }

        $token = random_token(32);
        $sessionId = uuid_v4();
        $questions = resosoin_question_map($audience);
        $firstQuestion = (string) (
            array_key_first($questions) ?? ''
        );

        $stmt = $pdo->prepare(
            'INSERT INTO resosoin_survey_sessions(
                id,
                audience,
                resume_token_hash,
                current_step,
                survey_version,
                recruitment_source,
                consent_at
             ) VALUES(?, ?, ?, ?, ?, ?, NOW())'
        );
        $stmt->execute([
            $sessionId,
            $audience,
            resosoin_token_hash($token),
            $firstQuestion,
            RESOSOIN_SURVEY_VERSION,
            $source,
        ]);

        resosoin_set_start_guard();

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
        json_response(
            [
                'ok' => false,
                'error' => 'Session introuvable ou supprimée.',
            ],
            401
        );
    }

    $audience = (string) $session['audience'];
    $questions = resosoin_question_map($audience);

    if ($action === 'resume') {
        $answers = resosoin_prune_hidden_answers(
            $pdo,
            (string) $session['id'],
            $audience
        );
        $visible = resosoin_visible_question_ids(
            $audience,
            $answers
        );
        $currentStep = (string) $session['current_step'];
        if (
            $session['status'] === 'active'
            && !in_array($currentStep, $visible, true)
        ) {
            $currentStep = $visible[0] ?? '';
            $pdo->prepare(
                'UPDATE resosoin_survey_sessions
                 SET current_step = ?, updated_at = NOW()
                 WHERE id = ?'
            )->execute([$currentStep, $session['id']]);
        }

        json_response([
            'ok' => true,
            'session' => [
                'audience' => $audience,
                'status' => $session['status'],
                'current_step' => $currentStep,
                'answers' => $answers,
            ],
        ]);
    }

    if ($action === 'save') {
        if ($session['status'] !== 'active') {
            throw new DomainException(
                'Ce questionnaire est déjà envoyé.'
            );
        }

        $questionId = (string) (
            $input['question_id'] ?? ''
        );
        $question = $questions[$questionId] ?? null;
        if (!is_array($question)) {
            throw new DomainException('Question inconnue.');
        }

        $answer = $input['answer'] ?? null;
        $isRequired = !empty($question['required']);

        if ($answer === null && !$isRequired) {
            $pdo->prepare(
                'DELETE FROM resosoin_survey_answers
                 WHERE session_id = ?
                   AND question_id = ?'
            )->execute([$session['id'], $questionId]);
        } else {
            $answer = resosoin_validate_answer(
                $question,
                $answer
            );
            $json = json_encode(
                $answer,
                JSON_UNESCAPED_UNICODE
                | JSON_UNESCAPED_SLASHES
            );
            if ($json === false) {
                throw new RuntimeException(
                    'Impossible d’enregistrer la réponse.'
                );
            }

            $stmt = $pdo->prepare(
                'INSERT INTO resosoin_survey_answers(
                    session_id,
                    question_id,
                    answer_json
                 ) VALUES(?, ?, ?)
                 ON DUPLICATE KEY UPDATE
                    answer_json = VALUES(answer_json),
                    updated_at = NOW()'
            );
            $stmt->execute([
                $session['id'],
                $questionId,
                $json,
            ]);
        }

        $answers = resosoin_prune_hidden_answers(
            $pdo,
            (string) $session['id'],
            $audience
        );
        $direction = ($input['direction'] ?? '') === 'back'
            ? 'back'
            : 'forward';
        $currentStep = resosoin_adjacent_step(
            $audience,
            $answers,
            $questionId,
            $direction
        );

        $pdo->prepare(
            'UPDATE resosoin_survey_sessions
             SET current_step = ?, updated_at = NOW()
             WHERE id = ?'
        )->execute([$currentStep, $session['id']]);

        json_response([
            'ok' => true,
            'current_step' => $currentStep,
            'answers' => $answers,
        ]);
    }

    if ($action === 'submit') {
        if ($session['status'] !== 'active') {
            json_response([
                'ok' => true,
                'already_submitted' => true,
            ]);
        }

        $answers = resosoin_prune_hidden_answers(
            $pdo,
            (string) $session['id'],
            $audience
        );
        $visibleIds = array_fill_keys(
            resosoin_visible_question_ids(
                $audience,
                $answers
            ),
            true
        );

        $missing = [];
        foreach ($questions as $questionId => $question) {
            if (
                isset($visibleIds[$questionId])
                && !empty($question['required'])
                && !array_key_exists($questionId, $answers)
            ) {
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
             SET status = 'submitted',
                 submitted_at = COALESCE(submitted_at, NOW()),
                 updated_at = NOW()
             WHERE id = ?"
        )->execute([$session['id']]);

        json_response(['ok' => true]);
    }

    if ($action === 'delete') {
        $pdo->prepare(
            'DELETE FROM resosoin_survey_sessions
             WHERE id = ?'
        )->execute([$session['id']]);

        json_response(['ok' => true]);
    }

    json_response(
        ['ok' => false, 'error' => 'Action inconnue.'],
        404
    );
} catch (DomainException $exception) {
    json_response(
        ['ok' => false, 'error' => $exception->getMessage()],
        422
    );
} catch (Throwable $exception) {
    error_log(
        '[RésoSoin survey] ' . $exception->getMessage()
    );
    json_response(
        ['ok' => false, 'error' => 'Erreur interne.'],
        500
    );
}
