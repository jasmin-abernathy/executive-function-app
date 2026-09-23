<?php

declare(strict_types=1);

require_once dirname(__DIR__, 2) . '/adhd-app/includes/bootstrap.php';

const RESOSOIN_SURVEY_VERSION = '2026-09-23-v2';
const RESOSOIN_START_GUARD_SECONDS = 3;

function resosoin_questions(): array
{
    static $questions = null;
    if (is_array($questions)) {
        return $questions;
    }

    $path = __DIR__ . '/questions.json';
    $decoded = json_decode((string) file_get_contents($path), true);
    if (!is_array($decoded)) {
        throw new RuntimeException('Questionnaire RésoSoin invalide.');
    }

    $questions = $decoded;
    return $questions;
}

function resosoin_question_map(string $audience): array
{
    $all = resosoin_questions();
    $items = $all[$audience]['questions'] ?? null;
    if (!is_array($items)) {
        return [];
    }

    $map = [];
    foreach ($items as $question) {
        if (is_array($question) && isset($question['id'])) {
            $map[(string) $question['id']] = $question;
        }
    }

    return $map;
}

function resosoin_condition_matches(?array $condition, array $answers): bool
{
    if ($condition === null || $condition === []) {
        return true;
    }

    $questionId = (string) ($condition['question'] ?? '');
    if ($questionId === '') {
        return true;
    }

    $answer = $answers[$questionId] ?? null;
    $operation = (string) ($condition['op'] ?? 'equals');

    if ($operation === 'contains') {
        return is_array($answer)
            && in_array($condition['value'] ?? null, $answer, true);
    }

    if ($operation === 'in') {
        $values = $condition['values'] ?? [];
        return is_array($values)
            && in_array($answer, $values, true);
    }

    if ($operation === 'equals') {
        return $answer === ($condition['value'] ?? null);
    }

    if ($operation === 'not_equals') {
        return $answer !== ($condition['value'] ?? null);
    }

    return false;
}

function resosoin_visible_question_ids(
    string $audience,
    array $answers
): array {
    $visible = [];
    foreach (resosoin_question_map($audience) as $questionId => $question) {
        $condition = is_array($question['show_if'] ?? null)
            ? $question['show_if']
            : null;

        if (resosoin_condition_matches($condition, $answers)) {
            $visible[] = $questionId;
        }
    }

    return $visible;
}

function resosoin_prune_hidden_answers(
    PDO $pdo,
    string $sessionId,
    string $audience
): array {
    $answers = resosoin_answer_map($pdo, $sessionId);
    $visible = array_fill_keys(
        resosoin_visible_question_ids($audience, $answers),
        true
    );

    $hidden = [];
    foreach (array_keys($answers) as $questionId) {
        if (!isset($visible[$questionId])) {
            $hidden[] = $questionId;
        }
    }

    if ($hidden !== []) {
        $placeholders = implode(',', array_fill(0, count($hidden), '?'));
        $stmt = $pdo->prepare(
            "DELETE FROM resosoin_survey_answers
             WHERE session_id = ?
               AND question_id IN ({$placeholders})"
        );
        $stmt->execute(array_merge([$sessionId], $hidden));
        $answers = resosoin_answer_map($pdo, $sessionId);
    }

    return $answers;
}

function resosoin_adjacent_step(
    string $audience,
    array $answers,
    string $questionId,
    string $direction
): string {
    $visible = resosoin_visible_question_ids($audience, $answers);
    $index = array_search($questionId, $visible, true);
    if ($index === false) {
        return $visible[0] ?? $questionId;
    }

    $offset = $direction === 'back' ? -1 : 1;
    $targetIndex = $index + $offset;

    return $visible[$targetIndex] ?? $questionId;
}

function resosoin_tables_ready(PDO $pdo): bool
{
    try {
        $sessions = $pdo->query(
            "SHOW TABLES LIKE 'resosoin_survey_sessions'"
        );
        $answers = $pdo->query(
            "SHOW TABLES LIKE 'resosoin_survey_answers'"
        );

        return (bool) $sessions->fetchColumn()
            && (bool) $answers->fetchColumn();
    } catch (Throwable) {
        return false;
    }
}

function resosoin_token_hash(string $token): string
{
    return hash_hmac('sha256', $token, app_key_bytes());
}

function resosoin_session_from_token(PDO $pdo, string $token): ?array
{
    if ($token === '') {
        return null;
    }

    $stmt = $pdo->prepare(
        "SELECT *
         FROM resosoin_survey_sessions
         WHERE resume_token_hash = ?
           AND status <> 'deleted'
         LIMIT 1"
    );
    $stmt->execute([resosoin_token_hash($token)]);
    $row = $stmt->fetch();

    return $row ?: null;
}

function resosoin_require_same_origin(): void
{
    require_same_origin();
}

function resosoin_bearer(): string
{
    $header = (string) ($_SERVER['HTTP_AUTHORIZATION'] ?? '');
    if (
        !preg_match(
            '/^Bearer\s+([A-Za-z0-9._~-]{20,})$/',
            $header,
            $match
        )
    ) {
        json_response(
            ['ok' => false, 'error' => 'Jeton de reprise requis.'],
            401
        );
    }

    return $match[1];
}

function resosoin_validate_answer(array $question, mixed $answer): mixed
{
    $type = (string) ($question['type'] ?? 'single');
    $options = is_array($question['options'] ?? null)
        ? $question['options']
        : [];

    if ($type === 'single') {
        if (
            !is_string($answer)
            || !array_key_exists($answer, $options)
        ) {
            throw new DomainException('Réponse invalide.');
        }

        return $answer;
    }

    if ($type === 'multi') {
        if (!is_array($answer) || $answer === []) {
            throw new DomainException(
                'Sélectionnez au moins une réponse.'
            );
        }

        $normalized = [];
        foreach ($answer as $value) {
            if (
                !is_string($value)
                || !array_key_exists($value, $options)
            ) {
                throw new DomainException('Réponse invalide.');
            }
            $normalized[$value] = true;
        }

        $values = array_keys($normalized);
        $maxChoices = (int) (
            $question['max'] ?? count($options)
        );
        if ($maxChoices > 0 && count($values) > $maxChoices) {
            throw new DomainException(
                "Sélectionnez au maximum {$maxChoices} réponses."
            );
        }

        $exclusive = is_array($question['exclusive'] ?? null)
            ? $question['exclusive']
            : ['none', 'nothing'];

        if (count($values) > 1) {
            foreach ($exclusive as $exclusiveValue) {
                if (
                    in_array(
                        (string) $exclusiveValue,
                        $values,
                        true
                    )
                ) {
                    throw new DomainException(
                        'Cette réponse doit être sélectionnée seule.'
                    );
                }
            }
        }

        return $values;
    }

    if ($type === 'scale') {
        if (
            ($question['allow_na'] ?? false) === true
            && $answer === 'not_applicable'
        ) {
            return 'not_applicable';
        }

        if (
            !is_int($answer)
            && !ctype_digit((string) $answer)
        ) {
            throw new DomainException('Valeur invalide.');
        }

        $value = (int) $answer;
        $min = (int) ($question['min'] ?? 0);
        $max = (int) ($question['max'] ?? 10);
        if ($value < $min || $value > $max) {
            throw new DomainException('Valeur hors limites.');
        }

        return $value;
    }

    throw new RuntimeException(
        'Type de question non pris en charge.'
    );
}

function resosoin_answer_map(PDO $pdo, string $sessionId): array
{
    $stmt = $pdo->prepare(
        'SELECT question_id, answer_json
         FROM resosoin_survey_answers
         WHERE session_id = ?'
    );
    $stmt->execute([$sessionId]);

    $answers = [];
    foreach ($stmt->fetchAll() as $row) {
        $answers[(string) $row['question_id']] =
            json_decode((string) $row['answer_json'], true);
    }

    return $answers;
}

function resosoin_start_guard_active(): bool
{
    $cookie = (string) (
        $_COOKIE['resosoin_start_guard'] ?? ''
    );
    if (
        !preg_match(
            '/^(\d{10})\.([a-f0-9]{64})$/',
            $cookie,
            $match
        )
    ) {
        return false;
    }

    $timestamp = (int) $match[1];
    $signature = hash_hmac(
        'sha256',
        (string) $timestamp,
        app_key_bytes()
    );

    if (!hash_equals($signature, $match[2])) {
        return false;
    }

    return time() - $timestamp < RESOSOIN_START_GUARD_SECONDS;
}

function resosoin_set_start_guard(): void
{
    $timestamp = time();
    $value = $timestamp
        . '.'
        . hash_hmac(
            'sha256',
            (string) $timestamp,
            app_key_bytes()
        );

    setcookie('resosoin_start_guard', $value, [
        'expires' => $timestamp + RESOSOIN_START_GUARD_SECONDS,
        'path' => '/resosoin/questionnaire/',
        'secure' => !empty($_SERVER['HTTPS'])
            && $_SERVER['HTTPS'] !== 'off',
        'httponly' => true,
        'samesite' => 'Lax',
    ]);
}

function resosoin_h(string $value): string
{
    return htmlspecialchars(
        $value,
        ENT_QUOTES | ENT_SUBSTITUTE,
        'UTF-8'
    );
}
