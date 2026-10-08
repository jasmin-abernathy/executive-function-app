<?php
declare(strict_types=1);
require __DIR__ . '/_common.php';
require __DIR__ . '/public-analytics.php';

header('Cache-Control: no-store, max-age=0');
header('X-Content-Type-Options: nosniff');
$lang = ($_GET['lang'] ?? 'fr') === 'en' ? 'en' : 'fr';
$fr = $lang === 'fr';
$groups = [];
$unavailable = false;

try {
    $pdo = db();
    if (!v2_tables_ready($pdo)) {
        $unavailable = true;
    } else {
        // Read only submitted questionnaires, like the private research dashboard.
        // No identity, session token, cohort, timestamps or free text are fetched.
        $rows = $pdo->query(
            "SELECT a.question_id, a.answer_json
             FROM survey_v2_answers a
             JOIN survey_v2_sessions s ON s.id = a.session_id
             WHERE s.status IN ('core_submitted','completed')"
        )->fetchAll(PDO::FETCH_ASSOC);
        $catalog = v2_admin_catalog(__DIR__);
        $groups = v2_public_groups($catalog, v2_admin_aggregate($rows), $lang);
    }
} catch (Throwable $error) {
    error_log('[ADHD V2 public results] ' . $error->getMessage());
    $unavailable = true;
}
?><!doctype html>
<html lang="<?=v2_h($lang)?>">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="robots" content="noindex, follow">
<meta name="theme-color" content="#f5f3ed">
<meta name="description" content="<?= $fr ? 'Résultats publics du questionnaire V2 en pourcentages anonymisés.' : 'Public results of the V2 questionnaire, presented as aggregate percentages.' ?>">
<link rel="icon" href="favicon.ico" sizes="any">
<link rel="icon" type="image/png" sizes="32x32" href="favicon-32x32.png">
<title><?= $fr ? 'Résultats publics — Questionnaire V2' : 'Public results — V2 questionnaire' ?> · Le Potager Lab</title>
<style>
:root{color-scheme:light;--bg:#f5f3ed;--paper:#fffdfa;--text:#203028;--muted:#56685c;--green:#315f45;--pale:#dfece3;--border:#d9dfd8}
*{box-sizing:border-box}html{font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif;background:var(--bg);color:var(--text)}body{margin:0;padding:clamp(12px,3vw,28px);line-height:1.5}
a{color:var(--green)}a:focus-visible{outline:3px solid var(--green);outline-offset:4px}
.shell{max-width:930px;margin:auto}.header{display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;margin-bottom:18px}
.brand{color:var(--text);text-decoration:none;font-weight:800}.languages{display:flex;gap:8px}.languages a{border:1px solid var(--border);border-radius:24px;padding:7px 12px;text-decoration:none;background:#fff}
.languages a[aria-current="page"]{background:var(--green);color:white;border-color:var(--green)}
main{background:var(--paper);border:1px solid var(--border);border-radius:24px;padding:clamp(20px,4vw,38px);box-shadow:0 14px 40px #21302812}
.eyebrow{font-weight:800;color:var(--green);text-transform:uppercase;letter-spacing:.09em;font-size:.8rem}
h1{font-size:clamp(2rem,5vw,3rem);line-height:1.14;margin:.3rem 0 1rem}h2{font-size:1.55rem;margin:2.5rem 0 1rem}h3{font-size:1.07rem;margin:0 0 1rem;line-height:1.4}
.lead{max-width:66ch;font-size:1.08rem;color:#45564d}.note{padding:14px 16px;background:#eef6ef;border:1px solid #cbded0;border-radius:14px;margin:20px 0;color:#355443}
.topics-hint{margin:18px 0 14px;color:var(--green);font-weight:700}
.question{padding:22px 0;border-top:1px solid var(--border)}.chart{list-style:none;margin:0;padding:0;display:grid;gap:13px}
.chart li{min-width:0}.chart-head{display:flex;justify-content:space-between;align-items:baseline;gap:18px}.chart-label{overflow-wrap:anywhere}.chart-percent{font-variant-numeric:tabular-nums;font-weight:850;white-space:nowrap}
.track{height:11px;background:var(--pale);border-radius:8px;overflow:hidden;margin-top:5px}.track span{display:block;height:100%;background:#508066;border-radius:8px}
.small{color:var(--muted);font-size:.9rem}.empty{padding:20px;border:1px solid var(--border);border-radius:14px;background:#fafbf9}
.actions{margin-top:30px;display:flex;gap:12px;flex-wrap:wrap}.button{display:inline-block;background:var(--green);color:white;text-decoration:none;border-radius:12px;font-weight:800;padding:12px 17px}
footer{color:var(--muted);font-size:.9rem;text-align:center;padding:20px}
@media(max-width:520px){.chart-head{align-items:flex-start}.chart-label{flex:1}main{border-radius:18px}}

/* Separate reading paths: required core vs voluntary deep-dive topics. */
.reading-guide{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px;margin:22px 0 26px}
.guide{display:flex;flex-direction:column;gap:6px;border-radius:15px;padding:16px 17px;border:1px solid var(--border)}
.guide strong{font-size:1.04rem}.guide p{margin:0;font-size:.92rem;line-height:1.5}
.guide--core{background:#edf5ee;border-color:#a9c9b2;color:#244c35}
.guide--optional{background:#f1f3f5;border-color:#c5cfd5;color:#364e5b}
.result-group{border:1px solid var(--border);border-radius:18px;padding:0;margin:18px 0 22px;scroll-margin-top:18px;overflow:hidden}
.result-group--core{background:#f0f7f1;border:2px solid #a9c9b2;border-top:6px solid #427355}
.result-group--optional{background:#f8fafb;border-color:#bfcbd1;border-left:6px solid #718995}
.group-summary{display:flex;align-items:center;justify-content:space-between;gap:18px;padding:clamp(17px,3vw,27px);min-height:112px;list-style:none;cursor:pointer}
.group-summary::-webkit-details-marker{display:none}
.group-summary:hover{background:#20302808}
.group-summary:focus-visible{outline:3px solid var(--green);outline-offset:-4px;border-radius:13px}
.group-summary-main{display:block;min-width:0}
.group-heading{display:block;font-size:clamp(1.18rem,3vw,1.55rem);font-weight:800;line-height:1.27;margin:4px 0 7px}
.group-kind{display:block;font-size:.79rem;font-weight:850;letter-spacing:.07em;text-transform:uppercase}
.result-group--core .group-kind{color:#245337}.result-group--optional .group-kind{color:#3a5968}
.group-teaser{display:block;font-size:.92rem;line-height:1.45;color:#42574b}
.result-group--optional .group-teaser{color:#4d6069}
.group-summary-action{display:flex;align-items:center;justify-content:flex-end;gap:8px;flex-shrink:0;color:var(--green);font-weight:800;font-size:.84rem}
.result-group--optional .group-summary-action{color:#3a5968}
.group-open{display:none}.result-group[open] .group-open{display:inline}
.result-group[open] .group-closed{display:none}
.group-chevron{display:inline-block;font-size:1.6rem;line-height:1;transition:transform .18s ease}
.result-group[open] .group-chevron{transform:rotate(180deg)}
.group-body{border-top:1px solid var(--border);padding:6px clamp(15px,3vw,26px) 15px}
.optional-heading{padding:24px 4px 0}.optional-heading h2{margin:0 0 6px}.optional-heading p{margin:0;color:var(--muted)}
.result-group .question{background:white;border:1px solid var(--border);border-radius:14px;padding:18px;margin:12px 0}
.question h3{margin:0 0 10px}.adaptive-note{font-size:.87rem;color:#355b43;margin:0 0 10px}
.back-to-top{position:fixed;z-index:10;right:clamp(14px,3vw,28px);bottom:calc(18px + env(safe-area-inset-bottom,0px));display:inline-flex;align-items:center;justify-content:center;gap:9px;min-height:48px;padding:10px 16px;background:var(--green);color:white;text-decoration:none;font-size:.95rem;font-weight:800;border-radius:100px;border:2px solid white;box-shadow:0 8px 26px #2030284d}
.back-to-top[hidden]{display:none}
.back-to-top:hover{background:#234832}
.back-to-top:focus-visible{outline:3px solid var(--text);outline-offset:3px}
html{scroll-behavior:smooth}
@media(max-width:650px){.reading-guide{grid-template-columns:1fr}.result-group{margin:14px 0 20px}.group-summary{align-items:flex-start;flex-direction:column;gap:12px}.group-summary-action{align-self:flex-end}.back-to-top{padding:9px 12px;min-height:48px}}
@media(prefers-reduced-motion:reduce){html{scroll-behavior:auto}.group-chevron{transition:none}}
</style>
</head>
<body>
<div class="shell" id="top">
  <header class="header">
    <a href="./" class="brand">🌱 Le Potager Lab · V2</a>
    <nav class="languages" aria-label="<?= $fr ? 'Langue' : 'Language' ?>">
      <a href="?lang=fr" lang="fr" <?= $fr ? 'aria-current="page"' : '' ?>>Français</a>
      <a href="?lang=en" lang="en" <?= !$fr ? 'aria-current="page"' : '' ?>>English</a>
    </nav>
  </header>
  <main>
    <p class="eyebrow"><?= $fr ? 'Étude participative' : 'Participatory research' ?></p>
    <h1><?= $fr ? 'Résultats publics du questionnaire V2' : 'Public results of the V2 questionnaire' ?></h1>
    <p class="lead"><?= $fr ? 'Découvrez les préférences exprimées pour concevoir une application d’organisation plus utile et moins contraignante.' : 'Explore the preferences shared to help design a more useful, less demanding organization app.' ?></p>
    <p class="note"><?= $fr
        ? 'Seuls des pourcentages agrégés sont publiés. Les questions ayant reçu trop peu de réponses sont masquées afin de préserver la confidentialité.'
        : 'Only aggregate percentages are published. Questions with too few answers are hidden to protect privacy.' ?></p>
    <div class="reading-guide" aria-label="<?= $fr ? 'Comprendre les deux parties' : 'Understanding the two sections' ?>">
      <div class="guide guide--core">
        <strong><?= $fr ? '01 · Partie obligatoire' : '01 · Required section' ?></strong>
        <p><?= $fr ? 'Les choix essentiels communs à l’étude, avec quelques questions adaptées au parcours.' : 'Core product decisions, with some questions tailored to each path.' ?></p>
      </div>
      <div class="guide guide--optional">
        <strong><?= $fr ? '02 · Modules facultatifs' : '02 · Optional modules' ?></strong>
        <p><?= $fr ? 'Des sujets complémentaires que chaque personne peut choisir de remplir ou non.' : 'Extra topics each person can choose to answer or skip.' ?></p>
      </div>
    </div>
    <?php if ($unavailable): ?>
      <div class="empty" role="status"><?= $fr ? 'Les résultats sont temporairement indisponibles.' : 'Results are temporarily unavailable.' ?></div>
    <?php elseif (!$groups): ?>
      <div class="empty" role="status"><?= $fr ? 'Les résultats apparaîtront ici dès que suffisamment de réponses pourront être présentées sans compromettre la confidentialité.' : 'Results will appear here when enough responses can be shared without compromising privacy.' ?></div>
    <?php else: ?>
      <p class="topics-hint"><?= $fr
          ? 'Choisissez un thème ci-dessous pour afficher ou masquer ses résultats.'
          : 'Select a topic below to show or hide its results.' ?></p>
      <?php $optionalHeadingShown = false; ?>
      <?php foreach ($groups as $i => $group): $core = $group['kind'] === 'core'; ?>
        <?php if (!$core && !$optionalHeadingShown): $optionalHeadingShown = true; ?>
          <div class="optional-heading">
            <h2><?= $fr ? 'Questions facultatives' : 'Optional questions' ?></h2>
            <p><?= $fr ? 'Ces thèmes étaient proposés après la partie obligatoire : tout le monde n’a pas répondu à chacun.' : 'These topics were offered after the required section: not everyone answered each one.' ?></p>
          </div>
        <?php endif; ?>
        <details class="result-group <?= $core ? 'result-group--core' : 'result-group--optional' ?>" id="group-<?=$i?>">
          <summary class="group-summary">
            <span class="group-summary-main">
              <span class="group-kind"><?= $core ? ($fr ? 'Partie obligatoire' : 'Required section') : ($fr ? 'Module facultatif' : 'Optional module') ?></span>
              <span class="group-heading" role="heading" aria-level="<?= $core ? '2' : '3' ?>"><?=v2_h($group['title'])?></span>
              <span class="group-teaser"><?= $core
                  ? ($fr ? 'Questions communes, avec quelques variantes selon le parcours.' : 'Core questions, with some differences depending on the path.')
                  : ($fr ? 'Réponses sur ce thème complémentaire, proposé au choix.' : 'Answers about this additional, voluntary topic.') ?></span>
            </span>
            <span class="group-summary-action" aria-hidden="true">
              <span class="group-closed"><?= $fr ? 'Voir les détails' : 'View details' ?></span>
              <span class="group-open"><?= $fr ? 'Masquer les détails' : 'Hide details' ?></span>
              <span class="group-chevron">⌄</span>
            </span>
          </summary>
          <div class="group-body">
          <?php foreach ($group['questions'] as $question): ?>
            <section class="question">
              <h3><?=v2_h($question['title'])?></h3>
              <?php if (!empty($question['conditional'])): ?>
                <p class="adaptive-note"><?= $fr ? 'Question adaptée selon le premier choix, non présentée à tout le monde.' : 'Adapted to the first answer; not shown to everyone.' ?></p>
              <?php endif; ?>
              <?php if ($question['multi']): ?>
                <p class="small"><?= $fr ? 'Plusieurs réponses étaient possibles : le total peut dépasser 100 %.' : 'Multiple answers were possible: the total may exceed 100%.' ?></p>
              <?php endif; ?>
              <ol class="chart">
                <?php foreach ($question['options'] as $option): ?>
                  <li>
                    <div class="chart-head"><span class="chart-label"><?=v2_h($option['label'])?></span><strong class="chart-percent"><?=$option['percent']?> %</strong></div>
                    <div class="track" aria-hidden="true"><span style="width:<?=$option['percent']?>%"></span></div>
                  </li>
                <?php endforeach; ?>
              </ol>
            </section>
          <?php endforeach; ?>
          </div>
        </details>
      <?php endforeach; ?>
    <?php endif; ?>
    <div class="actions"><a class="button" href="./?lang=<?=v2_h($lang)?>"><?= $fr ? 'Revenir au questionnaire' : 'Return to the questionnaire' ?></a></div>
  </main>
  <footer><?= $fr ? 'Aucun chiffre brut ni aucune réponse individuelle ne sont publiés.' : 'No raw counts or individual responses are published.' ?></footer>
</div>
<a href="#top" class="back-to-top" id="back-to-top"
   aria-label="<?= $fr ? 'Retour en haut de la page' : 'Back to top of page' ?>"
   title="<?= $fr ? 'Retour en haut' : 'Back to top' ?>">
  <span aria-hidden="true">↑</span>
  <span class="back-to-top-text" aria-hidden="true"><?= $fr ? 'En haut' : 'Top' ?></span>
</a>
<script>
// Progressive enhancement: the navigation remains usable if scripts are blocked.
(() => {
  const button = document.getElementById('back-to-top');
  if (!button) return;
  const syncVisibility = () => { button.hidden = window.scrollY < 280; };
  window.addEventListener('scroll', syncVisibility, { passive: true });
  syncVisibility();
})();
</script>
</body>
</html>
