<?php
declare(strict_types=1);

/**
 * Build a portable, aggregate-only report. Never include session IDs, raw
 * submissions, resume tokens, guardian contacts or individual timestamps.
 */
function v2_admin_export_payload(
    array $catalog,
    array $aggregates,
    array $summary,
    array $progress,
    string $lang = 'fr'
): array {
    $lang = $lang === 'en' ? 'en' : 'fr';
    $groups = [];

    foreach ($catalog['groups'] as $group) {
        $questions = [];
        foreach ($group['questions'] as $entry) {
            $id = (string) $entry['id'];
            $question = $catalog['questions'][$id] ?? $entry;
            $stats = $aggregates[$id] ?? [];
            $n = (int) ($stats['n'] ?? 0);
            $counts = $stats['counts'] ?? [];
            $options = [];
            $hiddenLegacyCodes = [];

            foreach ($question[$lang]['options'] ?? [] as $option) {
                $options[(string) $option[0]] = $option;
            }
            // Historical or unexpected values may contain user-entered text.
            // Keep their aggregate counts, but never reveal their raw contents.
            foreach ($counts as $code => $count) {
                if (!isset($options[(string) $code])) {
                    $alias = 'historical_unknown_' . (count($hiddenLegacyCodes) + 1);
                    $hiddenLegacyCodes[(string) $code] = $alias;
                    $options[(string) $code] = [
                        (string) $code,
                        $lang === 'fr' ? 'Choix historique non reconnu (code masqué)' : 'Unrecognized historical choice (code hidden)',
                    ];
                }
            }

            $choices = [];
            foreach ($options as $option) {
                $code = (string) $option[0];
                $count = (int) ($counts[$code] ?? 0);
                $choices[] = [
                    'code' => $hiddenLegacyCodes[$code] ?? $code,
                    'label' => (string) $option[1],
                    'description' => (string) ($option[2] ?? ''),
                    'selections' => $count,
                    'percentage_of_respondents' => $n > 0 ? round(100 * $count / $n, 1) : 0.0,
                ];
            }

            usort($choices, static fn(array $a, array $b): int =>
                ($b['selections'] <=> $a['selections']) ?: strcmp($a['code'], $b['code'])
            );

            $type = (string) ($question['type'] ?? '');
            $questions[] = [
                'id' => $id,
                'title' => (string) ($question[$lang]['title'] ?? $id),
                'help' => (string) ($question[$lang]['help'] ?? ''),
                'type' => $type,
                'multiple_choice' => str_starts_with($type, 'multi') || $type === 'exact5',
                'respondents' => $n,
                'choices' => $choices,
            ];
        }

        $groups[] = [
            'title' => (string) ($group[$lang] ?? ''),
            'questions' => $questions,
        ];
    }

    $steps = [
        'completed' => ['fr' => 'Questionnaire terminé', 'en' => 'Questionnaire completed'],
        'optional_hub' => ['fr' => 'Modules facultatifs', 'en' => 'Optional modules'],
        'core_submitted' => ['fr' => 'Partie obligatoire envoyée', 'en' => 'Core submitted'],
    ];
    $dropoff = [];
    foreach ($progress as $row) {
        $step = (string) $row['current_step'];
        $knownStep = isset($catalog['questions'][$step]) || isset($steps[$step]);
        $dropoff[] = [
            'step' => $knownStep ? $step : 'unknown_step',
            'step_label' => $knownStep
                ? (string) ($catalog['questions'][$step][$lang]['title'] ?? $steps[$step][$lang] ?? $step)
                : ($lang === 'fr' ? 'Étape inconnue' : 'Unknown step'),
            'status' => (string) $row['status'],
            'sessions' => (int) $row['n'],
        ];
    }

    return [
        'format_version' => 1,
        'questionnaire' => 'adhd-app-v2',
        'exported_at_utc' => gmdate('c'),
        'language' => $lang,
        'scope' => 'aggregated_results_only',
        'included_answers' => 'core_submitted_or_completed_sessions',
        'notes' => [
            'No individual response, participant identifier, contact information, IP address or individual timestamp is included.',
            'Each question uses its own number of respondents as the denominator.',
            'For multiple-choice questions, percentages may add up to more than 100%.',
            'Unanswered questions and unselected choices are retained with zero counts.',
        ],
        'summary' => [
            'started' => (int) ($summary['started'] ?? 0),
            'core_submitted' => (int) ($summary['core'] ?? 0),
            'fully_completed' => (int) ($summary['done'] ?? 0),
            'adult_submitted' => (int) ($summary['adult'] ?? 0),
            'minor_submitted' => (int) ($summary['minor'] ?? 0),
        ],
        'progress' => $dropoff,
        'groups' => $groups,
    ];
}

/** Guard against spreadsheet formula injection, including legacy answer codes. */
function v2_admin_csv_safe(mixed $value): string
{
    $text = (string) $value;
    if (preg_match('/^[\\s\\x{FEFF}\\x{200B}]*[=+\\-@]/u', $text) || preg_match('/^[\\t\\r\\n]/', $text)) {
        return "'" . $text;
    }
    return $text;
}

/** CSV is deliberately a flat choice-level table; use JSON for the full funnel. */
function v2_admin_write_csv($stream, array $payload): void
{
    $fr = ($payload['language'] ?? 'fr') === 'fr';
    $header = $fr
        ? ['Groupe', 'ID question', 'Question', 'Type', 'Répondants', 'Code choix', 'Choix', 'Description', 'Sélections', 'Pourcentage', 'Choix multiples']
        : ['Group', 'Question ID', 'Question', 'Type', 'Respondents', 'Choice code', 'Choice', 'Description', 'Selections', 'Percentage', 'Multiple choice'];
    fputcsv($stream, $header, ';', '"', '');

    foreach ($payload['groups'] as $group) {
        foreach ($group['questions'] as $question) {
            foreach ($question['choices'] as $choice) {
                $row = [
                    $group['title'], $question['id'], $question['title'],
                    $question['type'], $question['respondents'], $choice['code'],
                    $choice['label'], $choice['description'], $choice['selections'],
                    number_format((float) $choice['percentage_of_respondents'], 1, $fr ? ',' : '.', ''),
                    $question['multiple_choice'] ? ($fr ? 'oui' : 'yes') : ($fr ? 'non' : 'no'),
                ];
                fputcsv($stream, array_map('v2_admin_csv_safe', $row), ';', '"', '');
            }
        }
    }
}
