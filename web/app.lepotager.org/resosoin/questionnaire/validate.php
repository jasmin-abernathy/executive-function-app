<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

require __DIR__ . '/_common.php';

$catalog = resosoin_questions();

foreach (['doctor' => 12, 'patient' => 9] as $audience => $minimumUsage) {
    $questions = $catalog[$audience]['questions'] ?? null;
    if (!is_array($questions) || $questions === []) {
        throw new RuntimeException("Missing {$audience} questions.");
    }

    $seen = [];
    $usageCount = 0;
    $conceptCount = 0;
    $conceptStarted = false;

    foreach ($questions as $question) {
        if (!is_array($question)) {
            throw new RuntimeException('Invalid question definition.');
        }

        $id = (string) ($question['id'] ?? '');
        if ($id === '' || isset($seen[$id])) {
            throw new RuntimeException("Duplicate or empty question id: {$id}");
        }
        $seen[$id] = true;

        $phase = (string) ($question['phase'] ?? '');
        if ($phase === 'concept') {
            $conceptStarted = true;
            $conceptCount++;
        } elseif ($phase === 'usage') {
            if ($conceptStarted) {
                throw new RuntimeException(
                    "Usage question appears after concept: {$id}"
                );
            }
            $usageCount++;
        } else {
            throw new RuntimeException("Invalid phase for {$id}");
        }

        $type = (string) ($question['type'] ?? '');
        if (!in_array($type, ['single', 'multi', 'scale'], true)) {
            throw new RuntimeException(
                "Open or unsupported answer type for {$id}"
            );
        }

        if ($type === 'scale') {
            $min = (int) ($question['min'] ?? 0);
            $max = (int) ($question['max'] ?? 0);
            if ($max <= $min) {
                throw new RuntimeException("Invalid scale for {$id}");
            }
            continue;
        }

        $options = $question['options'] ?? null;
        if (!is_array($options) || count($options) < 2) {
            throw new RuntimeException("Not enough options for {$id}");
        }

        if ($type === 'multi') {
            $maxChoices = (int) ($question['max'] ?? 0);
            if ($maxChoices < 1 || $maxChoices > count($options)) {
                throw new RuntimeException(
                    "Missing or invalid max choices for {$id}"
                );
            }
        }
    }

    if ($usageCount < $minimumUsage || $conceptCount < 3) {
        throw new RuntimeException(
            "Concept is introduced too early for {$audience}"
        );
    }
}

$doctorAutomation = resosoin_question_map('doctor')['doctor_automation'] ?? null;
$patientAutomation = resosoin_question_map('patient')['patient_automation'] ?? null;

foreach ([$doctorAutomation, $patientAutomation] as $question) {
    if (!is_array($question)) {
        throw new RuntimeException('Automation question missing.');
    }
    $wording = mb_strtolower(
        (string) ($question['title'] ?? '')
        . ' '
        . (string) ($question['help'] ?? '')
    );
    if (
        !str_contains($wording, 'sans ia')
        || (
            !str_contains($wording, 'automatisation')
            && !str_contains($wording, 'automatis')
        )
    ) {
        throw new RuntimeException(
            'The no-AI versus automation distinction is missing.'
        );
    }
}

fwrite(
    STDOUT,
    sprintf(
        "RésoSoin catalog OK: %d doctor questions, %d patient questions.\n",
        count($catalog['doctor']['questions']),
        count($catalog['patient']['questions'])
    )
);
