<?php
declare(strict_types=1);
require __DIR__ . '/../web/app.lepotager.org/adhd-app/v2/admin/analytics.php';
function v2_h(mixed $v): string { return htmlspecialchars((string)$v, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8'); }
function check(bool $ok, string $message): void { if (!$ok) throw new RuntimeException($message); }
$catalog = v2_admin_catalog(__DIR__ . '/../web/app.lepotager.org/adhd-app/v2');
check(count($catalog['questions']) >= 30, 'Core and optional questions loaded from the active survey');
check($catalog['questions']['recent_difficulty']['fr']['options'][0][1] === 'Commencer l’activité.', 'Exact French survey wording');
check($catalog['questions']['mvp_one']['fr']['options'] === $catalog['questions']['mvp_top5']['fr']['options'], 'Dynamic choices reuse source labels');
foreach ($catalog['questions'] as $q) {
    foreach (['fr', 'en'] as $lang) check(!empty($q[$lang]['title']) && !empty($q[$lang]['options']), 'Every question has bilingual labels: ' . $q['id']);
}
$stats = v2_admin_aggregate([
    ['question_id'=>'abandon_risks', 'answer_json'=>'["streak","cloud","cloud"]'],
    ['question_id'=>'abandon_risks', 'answer_json'=>'["streak"]'],
    ['question_id'=>'abandon_risks', 'answer_json'=>'null'],
    ['question_id'=>'abandon_risks', 'answer_json'=>'{broken'],
    ['question_id'=>'abandon_risks', 'answer_json'=>'[]'],
    ['question_id'=>'survey_ease', 'answer_json'=>'"easy"'],
]);
check($stats['abandon_risks']['n'] === 2, 'Denominator counts respondents, excluding empty/invalid answers');
check($stats['abandon_risks']['counts'] === ['streak'=>2,'cloud'=>1], 'Repeated choice counted once per person');
ob_start();
v2_admin_chart($catalog['questions']['abandon_risks'], $stats['abandon_risks'], 'fr');
$html = ob_get_clean();
check(str_contains($html, '100,0 %') && str_contains($html, '50,0 %'), 'Multiple-choice percentages use people, not selections');
check(str_contains($html, 'le total peut dépasser 100 %'), 'Multiple-choice denominator explained');
check(str_contains($html, '0,0 %'), 'Unselected options remain visible');
check(str_contains($html, 'Perdre une série ou de la progression après une absence.'), 'Chart displays exact answer wording');
ob_start();
v2_admin_chart($catalog['questions']['survey_ease'], [], 'fr');
check(str_contains(ob_get_clean(), 'Aucune réponse'), 'Explicit empty state without division by zero');
ob_start();
v2_admin_chart($catalog['questions']['survey_ease'], ['n'=>1,'counts'=>['<script>alert(1)</script>'=>1]], 'en');
$html = ob_get_clean();
check(!str_contains($html, '<script>') && str_contains($html, '&lt;script&gt;'), 'Unknown historical answer escaped');
check(str_contains($html, 'Historical choice'), 'Unknown answer is not silently dropped or mistranslated');

require __DIR__ . '/../web/app.lepotager.org/adhd-app/v2/public-analytics.php';
// The public output deliberately has no n / counts keys; the private dashboard remains unchanged.
$publicQ = [
    'id'=>'sample', 'type'=>'multi2',
    'fr'=>['title'=>'Une question', 'options'=>[['a','Choix A'],['b','Choix B']]],
    'en'=>['title'=>'A question', 'options'=>[['a','Option A'],['b','Option B']]],
];
$publicCatalog = ['groups'=>[['fr'=>'Groupe', 'en'=>'Group', 'questions'=>[$publicQ]]], 'questions'=>['sample'=>$publicQ]];
check(v2_public_groups($publicCatalog, ['sample'=>['n'=>9,'counts'=>['a'=>9]]], 'fr') === [], 'Public hides questions below privacy threshold');
$public = v2_public_groups($publicCatalog, ['sample'=>['n'=>10,'counts'=>['a'=>9,'b'=>7,'private_legacy_value'=>1]]], 'fr');
check($public[0]['questions'][0]['options'] === [['label'=>'Choix A','percent'=>90],['label'=>'Choix B','percent'=>70]], 'Public percentages count respondents even for multiple choices');
check($public[0]['questions'][0]['multi'] === true, 'Public view indicates multiple choices');
check(!str_contains(json_encode($public), '"n"') && !str_contains(json_encode($public), 'private_legacy_value'), 'Public data contains no counts or unknown answer values');
check(v2_public_groups($publicCatalog, ['sample'=>['n'=>10,'counts'=>['a'=>9]]], 'en')[0]['questions'][0]['options'][0]['label'] === 'Option A', 'Public result labels localized');
$publicPage = file_get_contents(__DIR__ . '/../web/app.lepotager.org/adhd-app/v2/results.php');
check(str_contains($publicPage, 'class="chart-percent"') && str_contains($publicPage, '<?=$option') && !str_contains($publicPage, '<?=$n?>'), 'Public HTML renders percentages without respondent counts');
$survey = file_get_contents(__DIR__ . '/../web/app.lepotager.org/adhd-app/v2/runtime-js/questionnaire-v131.php');
check(str_contains($survey, 'View public results (%)') && str_contains($survey, 'Voir les résultats publics (%)') && str_contains($survey, 'href="results.php?lang='), 'Completion screen links to localized public results');

echo "V2 admin: catalogue, dynamic choices, denominators, empty states and escaping OK\n";
