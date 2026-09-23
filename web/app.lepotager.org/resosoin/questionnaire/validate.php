<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

$catalog = json_decode(
    (string) file_get_contents(__DIR__ . '/questions.json'),
    true,
    512,
    JSON_THROW_ON_ERROR
);

if (!is_array($catalog)) {
    throw new RuntimeException('Invalid questionnaire catalog.');
}

$maps = [];

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
        } else {
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

                $exclusive = $question['exclusive'] ?? [];
                if ($exclusive !== [] && !is_array($exclusive)) {
                    throw new RuntimeException(
                        "Invalid exclusive choices for {$id}"
                    );
                }
            }
        }

        $condition = $question['show_if'] ?? null;
        if ($condition !== null) {
            if (!is_array($condition)) {
                throw new RuntimeException("Invalid show_if for {$id}");
            }

            $parent = (string) ($condition['question'] ?? '');
            if ($parent === '' || !isset($seen[$parent])) {
                throw new RuntimeException(
                    "show_if must reference an earlier question for {$id}"
                );
            }

            $op = (string) ($condition['op'] ?? '');
            if (!in_array($op, ['contains', 'in', 'equals', 'not_equals'], true)) {
                throw new RuntimeException("Invalid show_if operator for {$id}");
            }
        }

        $seen[$id] = $question;
    }

    if ($usageCount < $minimumUsage || $conceptCount < 3) {
        throw new RuntimeException(
            "Concept is introduced too early for {$audience}"
        );
    }

    $maps[$audience] = $seen;
}

foreach (
    [
        $maps['doctor']['doctor_automation'] ?? null,
        $maps['patient']['patient_automation'] ?? null,
    ] as $question
) {
    if (!is_array($question)) {
        throw new RuntimeException('Automation question missing.');
    }

    $wording = strtolower(
        (string) ($question['title'] ?? '')
        . ' '
        . (string) ($question['help'] ?? '')
    );

    if (
        !str_contains($wording, 'sans ia')
        || !str_contains($wording, 'automatis')
    ) {
        throw new RuntimeException(
            'The no-AI versus automation distinction is missing.'
        );
    }
}

if (($maps['doctor']['doctor_satisfaction']['allow_na'] ?? false) !== true) {
    throw new RuntimeException(
        'Doctor satisfaction must offer a non-applicable answer.'
    );
}

if (($maps['doctor']['doctor_pilot']['required'] ?? true) !== false) {
    throw new RuntimeException('Doctor pilot interest must be optional.');
}

if (($maps['patient']['patient_pilot']['required'] ?? true) !== false) {
    throw new RuntimeException('Patient pilot interest must be optional.');
}

fwrite(
    STDOUT,
    sprintf(
        "RésoSoin catalog OK: %d doctor questions, %d patient questions.\n",
        count($catalog['doctor']['questions']),
        count($catalog['patient']['questions'])
    )
);
