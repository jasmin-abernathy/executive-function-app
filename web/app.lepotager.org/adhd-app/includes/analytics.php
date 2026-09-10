<?php

declare(strict_types=1);

/**
 * Privacy-first analytics helpers.
 *
 * No contact table is read here.
 * Free-text and demographic answers are deliberately excluded from
 * individual snapshots and public results.
 */

function analytics_path_labels(): array
{
    return [
        'start' => 'Getting started',
        'planning' => 'Planning / deciding what’s next',
        'time' => 'Time & transitions',
        'focus' => 'Focus & hyperfocus',
        'recovery' => 'Recovery after interruption',
        'low_energy' => 'Low-energy days',
        'capture' => 'Quick capture',
        'none' => 'None',
    ];
}

function analytics_question_labels(): array
{
    return [
        'recent_difficulty' => [
            'beginning' => 'Beginning the activity',
            'deciding' => 'Deciding what to do first',
            'staying' => 'Staying with it',
            'time' => 'Keeping track of time',
            'switching' => 'Stopping or switching',
            'returning' => 'Returning after an interruption',
            'remembering' => 'Remembering it at the right time',
            'other' => 'Something else',
            'unsure' => 'No recent example',
        ],
        'visual_home_density' => [
            'one' => 'One next action',
            'few' => 'One action + alternatives',
            'list' => 'Short daily list',
            'custom' => 'Configurable view',
        ],
        'visual_focus_surface' => [
            'timer_only' => 'Timer only',
            'task_timer' => 'Task + timer',
            'veil_capture' => 'Colour veil + quick capture',
            'tap_to_show' => 'Mostly hidden controls',
        ],
        'visual_quick_capture' => [
            'text_only' => 'Instant text note',
            'note_task' => 'Choose note or task',
            'inbox' => 'Temporary inbox',
            'full_form' => 'Full form',
        ],
        'visual_return_screen' => [
            'resume' => 'Resume only',
            'resume_or_fresh' => 'Resume or start fresh',
            'summary' => 'Gentle summary',
            'question' => 'Fresh support prompt',
        ],
        'progress_style' => [
            'home' => 'A room or home gradually expanding',
            'objects' => 'New objects or details appearing',
            'companion' => 'A companion reacting to progress',
            'abstract' => 'A simple abstract indicator',
            'history' => 'A written history',
            'none' => 'No visible progression system',
            'disable' => 'Disable or change it',
        ],
        'personality' => [
            'calm' => 'Calm companion',
            'practical' => 'Practical assistant',
            'gentle' => 'Gentle guide',
            'energetic' => 'Energetic coach',
            'discreet' => 'Discreet tool',
            'adaptive' => 'Adaptive personality',
            'other' => 'Something else',
        ],
        'name_first' => [
            'astelle' => 'Astelle',
            'tramelia' => 'Tramelia',
            'sillage' => 'Sillage',
            'none' => 'None of them',
            'unsure' => 'Could not choose yet',
        ],
        'name_final' => [
            'astelle' => 'Astelle',
            'tramelia' => 'Tramelia',
            'sillage' => 'Sillage',
            'none' => 'None of them',
            'unsure' => 'Still could not choose',
        ],
        'survey_ease' => [
            'very_easy' => 'Very easy',
            'easy' => 'Rather easy',
            'difficult' => 'Rather difficult',
            'very_difficult' => 'Very difficult',
        ],
        'survey_save_helpful' => [
            'yes_lot' => 'Considerably easier to begin',
            'yes_little' => 'Slightly easier to begin',
            'no_difference' => 'No difference',
            'more_complicated' => 'Made it feel more complicated',
            'not_notice' => 'Did not notice the option',
        ],
        'survey_save_clear' => [
            'very_clear' => 'Very clear',
            'clear' => 'Rather clear',
            'unclear' => 'Rather unclear',
            'very_unclear' => 'Very unclear',
        ],
        'survey_friction' => [
            'too_many' => 'Too many questions',
            'too_much_text' => 'Too much text',
            'too_many_choices' => 'Too many choices',
            'remember' => 'Hard to remember previous options',
            'choose' => 'Hard to choose an answer',
            'repetition' => 'Felt repetitive',
            'explanations' => 'Not enough explanation',
            'none' => 'No particular difficulty',
            'other' => 'Something else',
        ],
        'suggestions_discomfort' => [
            'why' => 'Not knowing why a suggestion appeared',
            'ordered' => 'Feeling ordered around',
            'unsuitable' => 'Repeated unsuitable suggestions',
            'same' => 'Seeing the same action repeatedly',
            'control' => 'Losing control over the plan',
            'none' => 'Nothing in particular',
        ],
        'reward_discomfort' => [
            'loss' => 'Losing progress',
            'repetitive' => 'Repetitive rewards',
            'watched' => 'Feeling watched or evaluated',
            'praise' => 'Excessive praise',
            'pressure' => 'Pressure to open the app daily',
            'compare' => 'Comparison with other users',
            'any' => 'Any reward system',
        ],
        'companion_discomfort' => [
            'overwhelmed' => 'When already overwhelmed',
            'focus' => 'During focus sessions',
            'missed' => 'Comments on missed activities',
            'animations' => 'Animations that cannot be disabled',
            'emotional' => 'Overly emotional language',
            'none' => 'Would not mind a companion',
            'prefer_none' => 'Would prefer no companion',
        ],
    ];
}

function analytics_is_excluded_internal_test(array $answers): bool
{
    $comment = $answers['survey_comment'] ?? null;
    if (!is_scalar($comment)) {
        return false;
    }

    $normalized = preg_replace('/\s+/u', ' ', trim((string) $comment));
    if ($normalized === null) {
        return false;
    }

    return mb_strtolower($normalized, 'UTF-8') === 'test de jasmin';
}

function analytics_load_completed(PDO $pdo): array
{
    $stmt = $pdo->query(
        "SELECT sa.session_id, sa.question_id, sa.answer_json
         FROM survey_answers sa
         INNER JOIN survey_sessions ss ON ss.id = sa.session_id
         WHERE ss.status = 'completed'
         ORDER BY sa.id ASC"
    );

    $sessions = [];
    foreach ($stmt->fetchAll() as $row) {
        $sid = (string) $row['session_id'];
        if (!isset($sessions[$sid])) {
            $sessions[$sid] = [];
        }
        $decoded = json_decode((string) $row['answer_json'], true);
        $sessions[$sid][(string) $row['question_id']] = $decoded;
    }
    foreach ($sessions as $sessionId => $answers) {
        if (analytics_is_excluded_internal_test($answers)) {
            unset($sessions[$sessionId]);
        }
    }

    return $sessions;
}

function analytics_count_values(array $sessions, string $questionId): array
{
    $counts = [];
    foreach ($sessions as $answers) {
        if (!array_key_exists($questionId, $answers)) {
            continue;
        }
        $value = $answers[$questionId];
        $values = is_array($value) && array_is_list($value) ? $value : [$value];
        foreach ($values as $item) {
            if (!is_scalar($item) || $item === '') {
                continue;
            }
            $key = (string) $item;
            $counts[$key] = ($counts[$key] ?? 0) + 1;
        }
    }
    arsort($counts);
    return $counts;
}

function analytics_universe_fit(array $sessions, string $questionId): array
{
    $ratings = [];
    $words = [];
    foreach ($sessions as $answers) {
        $value = $answers[$questionId] ?? null;
        if (!is_array($value)) {
            continue;
        }
        if (isset($value['fit']) && is_numeric($value['fit'])) {
            $ratings[] = (float) $value['fit'];
        }
        if (!empty($value['words']) && is_array($value['words'])) {
            foreach ($value['words'] as $word) {
                if (!is_scalar($word)) {
                    continue;
                }
                $key = (string) $word;
                $words[$key] = ($words[$key] ?? 0) + 1;
            }
        }
    }
    arsort($words);
    return [
        'n' => count($ratings),
        'average' => $ratings ? array_sum($ratings) / count($ratings) : null,
        'words' => $words,
    ];
}

function analytics_label(string $questionId, mixed $value): string
{
    if ($questionId === 'primary_path' || $questionId === 'secondary_path') {
        return analytics_path_labels()[(string) $value] ?? (string) $value;
    }
    $labels = analytics_question_labels();
    return $labels[$questionId][(string) $value] ?? (string) $value;
}

function analytics_labeled_counts(array $sessions, string $questionId): array
{
    $raw = analytics_count_values($sessions, $questionId);
    $out = [];
    foreach ($raw as $value => $count) {
        $out[] = [
            'value' => $value,
            'label' => analytics_label($questionId, $value),
            'count' => (int) $count,
        ];
    }
    return $out;
}

function analytics_free_text_labels(): array
{
    return [
        'start_other_text' => 'Getting started — other difficulty',
        'planning_other_text' => 'Planning — other difficulty',
        'time_other_text' => 'Time & transitions — other difficulty',
        'focus_other_text' => 'Focus / hyperfocus — other difficulty',
        'recovery_other_text' => 'Returning after interruption — other difficulty',
        'low_energy_other_text' => 'Low-energy days — other difficulty',
        'capture_other_text' => 'Quick capture — other need',
        'name_association' => 'Name association',
        'survey_comment' => 'Final comment / suggestion',
    ];
}

function analytics_randomized_individuals(array $sessions): array
{
    // Intentionally discard session IDs before shuffling.
    $rows = array_values($sessions);

    for ($i = count($rows) - 1; $i > 0; $i--) {
        $j = random_int(0, $i);
        [$rows[$i], $rows[$j]] = [$rows[$j], $rows[$i]];
    }

    $safeQuestionIds = [
        'recent_difficulty',
        'primary_path',
        'secondary_path',
        'visual_home_density',
        'visual_focus_surface',
        'visual_quick_capture',
        'visual_return_screen',
        'progress_style',
        'personality',
        'name_first',
        'name_final',
        'survey_ease',
        'survey_save_helpful',
        'survey_save_clear',
    ];

    $freeTextLabels = analytics_free_text_labels();
    $safe = [];

    foreach ($rows as $answers) {
        $snapshot = [
            'choices' => [],
            'free_text' => [],
        ];

        foreach ($safeQuestionIds as $qid) {
            if (!array_key_exists($qid, $answers)) {
                continue;
            }
            $value = $answers[$qid];
            if (!is_scalar($value) || trim((string) $value) === '') {
                continue;
            }
            $snapshot['choices'][$qid] = analytics_label($qid, $value);
        }

        foreach ($freeTextLabels as $qid => $label) {
            if (!array_key_exists($qid, $answers)) {
                continue;
            }
            $value = $answers[$qid];
            if (!is_scalar($value)) {
                continue;
            }
            $text = trim((string) $value);
            if ($text === '') {
                continue;
            }
            $snapshot['free_text'][] = [
                'question_id' => $qid,
                'label' => $label,
                'text' => $text,
            ];
        }

        $safe[] = $snapshot;
    }

    return $safe;
}

function analytics_public_trend(array $sessions, string $questionId, int $minCount = 2): ?array
{
    $rows = analytics_labeled_counts($sessions, $questionId);
    if (!$rows) {
        return null;
    }

    $eligible = array_values(array_filter($rows, fn(array $row): bool => $row['count'] >= $minCount));
    if (!$eligible) {
        return null;
    }

    $topCount = $eligible[0]['count'];
    $tops = array_values(array_filter($eligible, fn(array $row): bool => $row['count'] === $topCount));

    return [
        'labels' => array_column($tops, 'label'),
        'count' => $topCount,
    ];
}
