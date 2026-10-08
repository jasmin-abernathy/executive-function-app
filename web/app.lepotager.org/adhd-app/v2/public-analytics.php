<?php
declare(strict_types=1);

require_once __DIR__ . '/admin/analytics.php';

// Public percentages are released only for questions with a sufficiently large base.
// The threshold and counts never appear in the public HTML.
const V2_PUBLIC_MIN_RESPONSES = 6;

// Survey-usability feedback is retained for private research only.
const V2_PUBLIC_EXCLUDED_QUESTION_IDS = ['survey_ease', 'survey_friction_v2'];

/** Prepare only labeled, aggregated percentages for the public view. */
function v2_public_groups(array $catalog, array $aggregates, string $lang): array
{
    $lang = $lang === 'en' ? 'en' : 'fr';
    $groups = [];
    foreach ($catalog['groups'] as $index => $group) {
        $questions = [];
        foreach ($group['questions'] as $entry) {
            $question = $catalog['questions'][$entry['id']] ?? $entry;
            $id = (string) $question['id'];
            if (in_array($id, V2_PUBLIC_EXCLUDED_QUESTION_IDS, true)) continue;
            $base = (int) ($aggregates[$id]['n'] ?? 0);
            if ($base < V2_PUBLIC_MIN_RESPONSES) continue;

            $options = [];
            foreach ($question[$lang]['options'] ?? [] as $option) {
                $code = (string) ($option[0] ?? '');
                $label = (string) ($option[1] ?? '');
                if ($code === '' || $label === '') continue;
                $count = (int) ($aggregates[$id]['counts'][$code] ?? 0);
                $options[] = ['label' => $label, 'percent' => (int) round(100 * $count / $base)];
            }
            // Historical/unknown values are intentionally not exposed on a public page.
            usort($options, static fn(array $a, array $b): int => $b['percent'] <=> $a['percent']);
            if (!$options) continue;
            $questions[] = [
                'title' => (string) $question[$lang]['title'],
                'multi' => str_starts_with((string) $question['type'], 'multi') || $question['type'] === 'exact5',
                'conditional' => $index === 0 && !empty($question['adaptive']),
                'options' => $options,
            ];
        }
        if ($questions) $groups[] = [
            'kind' => $index === 0 ? 'core' : 'optional',
            'title' => (string) $group[$lang],
            'questions' => $questions,
        ];
    }
    return $groups;
}
