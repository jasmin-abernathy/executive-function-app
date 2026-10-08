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
.section-nav{display:flex;flex-wrap:wrap;gap:8px;margin:24px 0}.section-nav a{padding:9px 12px;border:1px solid var(--border);border-radius:12px;text-decoration:none;background:white}
.question{padding:22px 0;border-top:1px solid var(--border)}.chart{list-style:none;margin:0;padding:0;display:grid;gap:13px}
.chart li{min-width:0}.chart-head{display:flex;justify-content:space-between;align-items:baseline;gap:18px}.chart-label{overflow-wrap:anywhere}.chart-percent{font-variant-numeric:tabular-nums;font-weight:850;white-space:nowrap}
.track{height:11px;background:var(--pale);border-radius:8px;overflow:hidden;margin-top:5px}.track span{display:block;height:100%;background:#508066;border-radius:8px}
.small{color:var(--muted);font-size:.9rem}.empty{padding:20px;border:1px solid var(--border);border-radius:14px;background:#fafbf9}
.actions{margin-top:30px;display:flex;gap:12px;flex-wrap:wrap}.button{display:inline-block;background:var(--green);color:white;text-decoration:none;border-radius:12px;font-weight:800;padding:12px 17px}
footer{color:var(--muted);font-size:.9rem;text-align:center;padding:20px}
@media(max-width:520px){.chart-head{align-items:flex-start}.chart-label{flex:1}main{border-radius:18px}}
</style>
</head>
<body>
<div class="shell">
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
    <?php if ($unavailable): ?>
      <div class="empty" role="status"><?= $fr ? 'Les résultats sont temporairement indisponibles.' : 'Results are temporarily unavailable.' ?></div>
    <?php elseif (!$groups): ?>
      <div class="empty" role="status"><?= $fr ? 'Les résultats apparaîtront ici dès que suffisamment de réponses pourront être présentées sans compromettre la confidentialité.' : 'Results will appear here when enough responses can be shared without compromising privacy.' ?></div>
    <?php else: ?>
      <nav class="section-nav" aria-label="<?= $fr ? 'Sections des résultats' : 'Results sections' ?>">
      <?php foreach ($groups as $i => $group): ?>
        <a href="#group-<?=$i?>"><?=v2_h($group['title'])?></a>
      <?php endforeach; ?>
      </nav>
      <?php foreach ($groups as $i => $group): ?>
        <section aria-labelledby="group-<?=$i?>">
          <h2 id="group-<?=$i?>"><?=v2_h($group['title'])?></h2>
          <?php foreach ($group['questions'] as $question): ?>
            <section class="question">
              <h3><?=v2_h($question['title'])?></h3>
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
        </section>
      <?php endforeach; ?>
    <?php endif; ?>
    <div class="actions"><a class="button" href="./?lang=<?=v2_h($lang)?>"><?= $fr ? 'Revenir au questionnaire' : 'Return to the questionnaire' ?></a></div>
  </main>
  <footer><?= $fr ? 'Aucun chiffre brut ni aucune réponse individuelle ne sont publiés.' : 'No raw counts or individual responses are published.' ?></footer>
</div>
</body>
</html>
