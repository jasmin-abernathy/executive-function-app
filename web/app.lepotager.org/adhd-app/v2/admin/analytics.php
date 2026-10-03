<?php
declare(strict_types=1);

/** Read the active survey's JSON data, without executing its PHP or JavaScript. */
function v2_admin_catalog(string $root): array
{
    $index = file_get_contents($root . '/index.php');
    if ($index === false || !preg_match('~src="(runtime-js/questionnaire-[a-zA-Z0-9-]+\.php)(?:\?[^"<>]*)?"~', $index, $script)) {
        throw new RuntimeException('Questionnaire source unavailable.');
    }
    $source = file_get_contents($root . '/' . $script[1]);
    if ($source === false || !preg_match('/^const DATA=(\{[^\r\n]+\});\r?$/m', $source, $match)) {
        throw new RuntimeException('Questionnaire catalogue unavailable.');
    }
    $data = json_decode($match[1], true, 512, JSON_THROW_ON_ERROR);
    $groups = [['fr' => 'Questions principales', 'en' => 'Core questions', 'questions' => $data['core']]];
    foreach ($data['modules'] as $module) {
        $groups[] = ['fr' => $module['fr_title'], 'en' => $module['en_title'], 'questions' => $module['questions']];
    }
    $questions = [];
    foreach ($groups as $group) {
        foreach ($group['questions'] as $q) $questions[$q['id']] = $q;
    }
    foreach ($questions as &$q) {
        if ($q['type'] === 'dynamic_one') {
            foreach (['fr', 'en'] as $lang) $q[$lang]['options'] = $questions[$q['from']][$lang]['options'];
        }
    }
    unset($q);
    return ['groups' => $groups, 'questions' => $questions];
}

/** One stored answer per session/question; denominator counts people, not ticks. */
function v2_admin_aggregate(array $rows): array
{
    $result = [];
    foreach ($rows as $row) {
        $answer = json_decode((string)$row['answer_json'], true);
        if (json_last_error() !== JSON_ERROR_NONE) continue;
        $values = is_array($answer) ? $answer : [$answer];
        $values = array_values(array_unique(array_filter($values, static fn($v) => is_string($v) && $v !== '')));
        if (!$values) continue;
        $id = (string)$row['question_id'];
        $result[$id] ??= ['n' => 0, 'counts' => []];
        ++$result[$id]['n'];
        foreach ($values as $value) $result[$id]['counts'][$value] = ($result[$id]['counts'][$value] ?? 0) + 1;
    }
    return $result;
}

function v2_admin_chart(array $q, array $stats, string $lang): void
{
    $fr = $lang === 'fr';
    $text = $q[$lang];
    $n = $stats['n'] ?? 0;
    $counts = $stats['counts'] ?? [];
    $options = [];
    foreach ($text['options'] ?? [] as $option) $options[$option[0]] = $option;
    // Keep historical/unknown choices visible, but never invent their wording.
    foreach ($counts as $code => $count) {
        if (!isset($options[$code])) $options[$code] = [$code, ($fr ? 'Ancien choix — libellé indisponible : ' : 'Historical choice — label unavailable: ') . $code];
    }
    uasort($options, static fn($a, $b) => ($counts[$b[0]] ?? 0) <=> ($counts[$a[0]] ?? 0));
    $multi = str_starts_with($q['type'], 'multi') || $q['type'] === 'exact5';
    ?>
    <section class="question" aria-labelledby="q-<?=v2_h($q['id'])?>">
      <h3 id="q-<?=v2_h($q['id'])?>"><?=v2_h($text['title'])?></h3>
      <?php if (!empty($text['help'])): ?><p class="small"><?=v2_h($text['help'])?></p><?php endif; ?>
      <p class="chart-base"><strong><?=$n?> <?=$fr ? 'répondant(s)' : 'respondent(s)'?></strong> · <?=$fr ? 'à cette question' : 'to this question'?>
      <?php if ($multi): ?> — <?=$fr ? 'Choix multiples : le total peut dépasser 100 %.' : 'Multiple choices: the total may exceed 100%.'?><?php endif; ?></p>
      <?php if (!$n): ?><p class="notice"><?=$fr ? 'Aucune réponse pour le moment.' : 'No answers yet.'?></p><?php else: ?>
      <ol class="chart">
      <?php foreach ($options as $code => $option): $count = $counts[$code] ?? 0; $percent = $n ? 100 * $count / $n : 0; ?>
        <li>
          <div class="chart-label"><div><span><?=v2_h($option[1])?></span>
          <?php if (!empty($option[2])): ?><span class="small chart-description"><?=v2_h($option[2])?></span><?php endif; ?></div>
          <strong class="chart-value"><?=$count?> / <?=$n?> · <?=number_format($percent, 1, $fr ? ',' : '.', '')?> %</strong></div>
          <div class="chart-track" aria-hidden="true"><span style="width:<?=number_format(min(100, $percent), 3, '.', '')?>%"></span></div>
        </li>
      <?php endforeach; ?>
      </ol>
      <?php endif; ?>
    </section>
    <?php
}
