<?php

declare(strict_types=1);

require_once dirname(__DIR__, 2) . '/adhd-app/includes/bootstrap.php';

const RESOSOIN_SURVEY_VERSION = '2026-09-23-v1';

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

function resosoin_tables_ready(PDO $pdo): bool
{
    try {
        $sessions = $pdo->query("SHOW TABLES LIKE 'resosoin_survey_sessions'");
        $answers = $pdo->query("SHOW TABLES LIKE 'resosoin_survey_answers'");
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
        "SELECT * FROM resosoin_survey_sessions
         WHERE resume_token_hash = ? AND status <> 'deleted'
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
    if (!preg_match('/^Bearer\s+([A-Za-z0-9._~-]{20,})$/', $header, $match)) {
        json_response(['ok' => false, 'error' => 'Jeton de reprise requis.'], 401);
    }
    return $match[1];
}

function resosoin_validate_answer(array $question, mixed $answer): mixed
{
    $type = (string) ($question['type'] ?? 'single');
    $options = is_array($question['options'] ?? null) ? $question['options'] : [];

    if ($type === 'single') {
        if (!is_string($answer) || !array_key_exists($answer, $options)) {
            throw new DomainException('Réponse invalide.');
        }
        return $answer;
    }

    if ($type === 'multi') {
        if (!is_array($answer) || $answer === []) {
            throw new DomainException('Sélectionnez au moins une réponse.');
        }
        $normalized = [];
        foreach ($answer as $value) {
            if (!is_string($value) || !array_key_exists($value, $options)) {
                throw new DomainException('Réponse invalide.');
            }
            $normalized[$value] = true;
        }
        $values = array_keys($normalized);
        $maxChoices = (int) ($question['max'] ?? count($options));
        if ($maxChoices > 0 && count($values) > $maxChoices) {
            throw new DomainException(
                "Sélectionnez au maximum {$maxChoices} réponses."
            );
        }
        foreach (['none', 'nothing'] as $exclusive) {
            if (in_array($exclusive, $values, true) && count($values) > 1) {
                throw new DomainException('« Aucun / rien » doit être sélectionné seul.');
            }
        }
        return $values;
    }

    if ($type === 'scale') {
        if (!is_int($answer) && !ctype_digit((string) $answer)) {
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

    throw new RuntimeException('Type de question non pris en charge.');
}

function resosoin_answer_map(PDO $pdo, string $sessionId): array
{
    $stmt = $pdo->prepare(
        'SELECT question_id, answer_json FROM resosoin_survey_answers WHERE session_id = ?'
    );
    $stmt->execute([$sessionId]);
    $answers = [];
    foreach ($stmt->fetchAll() as $row) {
        $answers[(string) $row['question_id']] = json_decode((string) $row['answer_json'], true);
    }
    return $answers;
}

function resosoin_h(string $value): string
{
    return htmlspecialchars($value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}
