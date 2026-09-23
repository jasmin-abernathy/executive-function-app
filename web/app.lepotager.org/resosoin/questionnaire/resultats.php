<?php

declare(strict_types=1);

require __DIR__ . '/_common.php';

const RESOSOIN_RESULTS_THRESHOLD = 20;
const RESOSOIN_CELL_THRESHOLD = 3;

$pdo = db();
$ready = resosoin_tables_ready($pdo);
$audience = ($_GET['audience'] ?? '') === 'patient'
    ? 'patient'
    : 'doctor';
$questions = resosoin_question_map($audience);

$displayIds = $audience === 'doctor'
    ? [
        'doctor_pain_points',
        'doctor_eu_jurisdiction',
        'doctor_ai_preference',
        'doctor_automation',
        'doctor_concept_interest',
        'doctor_concept_reasons',
        'doctor_price',
    ]
    : [
        'patient_direct_site',
        'patient_eu_jurisdiction',
        'patient_ai_preference',
        'patient_automation',
        'patient_structure_preference',
        'patient_concept_interest',
        'patient_app_preference',
    ];

$total = 0;
$results = [];

if ($ready) {
    $stmt = $pdo->prepare(
        "SELECT COUNT(*)
         FROM resosoin_survey_sessions
         WHERE audience = ?
           AND status = 'submitted'"
    );
    $stmt->execute([$audience]);
    $total = (int) $stmt->fetchColumn();

    if ($total >= RESOSOIN_RESULTS_THRESHOLD) {
        $stmt = $pdo->prepare(
            "SELECT a.answer_json
             FROM resosoin_survey_answers a
             JOIN resosoin_survey_sessions s
               ON s.id = a.session_id
             WHERE s.audience = ?
               AND s.status = 'submitted'
               AND a.question_id = ?"
        );

        foreach ($displayIds as $questionId) {
            $question = $questions[$questionId] ?? null;
            if (!is_array($question)) {
                continue;
            }

            $stmt->execute([$audience, $questionId]);
            $rows = $stmt->fetchAll(PDO::FETCH_COLUMN);
            $respondents = count($rows);
            $counts = [];

            foreach ($rows as $raw) {
                $answer = json_decode((string) $raw, true);
                $values = is_array($answer) ? $answer : [$answer];

                foreach ($values as $value) {
                    $key = (string) $value;
                    $counts[$key] = ($counts[$key] ?? 0) + 1;
                }
            }

            arsort($counts);
            $results[$questionId] = [
                'respondents' => $respondents,
                'counts' => $counts,
                'multi' => ($question['type'] ?? '') === 'multi',
            ];
        }
    }
}
?><!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="theme-color" content="#4f7a61">
  <title>Résultats agrégés — RésoSoin</title>
  <link rel="icon" href="/favicon.svg" type="image/svg+xml">
  <link rel="stylesheet" href="/resosoin/questionnaire/assets/style.css">
</head>
<body>
<div class="shell">
<header class="header">
  <a class="brand" href="/resosoin/">RésoSoin · étude</a>
  <div class="header-links">
    <a href="./">Questionnaire</a>
    <a href="confidentialite.php">Confidentialité</a>
  </div>
</header>

<main class="card">
  <p class="eyebrow">Résultats agrégés</p>
  <h1><?= $audience === 'doctor'
      ? 'Professionnels de santé'
      : 'Patients' ?></h1>

  <div class="actions">
    <a class="btn <?= $audience === 'doctor'
        ? 'primary'
        : 'secondary' ?>" href="?audience=doctor">Professionnels</a>
    <a class="btn <?= $audience === 'patient'
        ? 'primary'
        : 'secondary' ?>" href="?audience=patient">Patients</a>
  </div>

  <div class="notice">
    <strong>Étude exploratoire et auto-sélectionnée.</strong>
    <span>Ces répartitions décrivent les répondants à cette étude ; elles ne sont pas représentatives de l’ensemble des médecins ou patients.</span>
  </div>

  <p class="lead">
    Les répartitions détaillées ne sont publiées qu’à partir de
    <?= RESOSOIN_RESULTS_THRESHOLD ?> questionnaires envoyés par profil.
    Les cellules de moins de <?= RESOSOIN_CELL_THRESHOLD ?> sélections sont
    regroupées afin de limiter l’exposition de catégories rares.
  </p>

  <?php if (!$ready): ?>
    <div class="notice warning">La base de l’étude n’est pas encore initialisée.</div>
  <?php elseif ($total < RESOSOIN_RESULTS_THRESHOLD): ?>
    <div class="notice">
      <?= $total ?> réponse<?= $total > 1 ? 's' : '' ?> envoyée<?= $total > 1 ? 's' : '' ?>.
      Les détails apparaîtront à partir de <?= RESOSOIN_RESULTS_THRESHOLD ?>.
    </div>
  <?php else: ?>
    <?php foreach ($displayIds as $questionId): ?>
      <?php
        $question = $questions[$questionId] ?? null;
        $result = $results[$questionId] ?? null;
        if (!is_array($question) || !is_array($result)) {
            continue;
        }
        $respondents = (int) $result['respondents'];
        $counts = $result['counts'];
        $hiddenSmall = 0;
      ?>
      <section class="result-section">
        <h2><?= resosoin_h((string) $question['title']) ?></h2>
        <p class="small">
          n = <?= $respondents ?> répondant<?= $respondents > 1 ? 's' : '' ?> à cette question.
          <?php if ($result['multi']): ?>
            Plusieurs choix étaient possibles : les pourcentages peuvent dépasser 100 % au total.
          <?php endif; ?>
        </p>

        <div class="options">
          <?php foreach ($counts as $key => $value): ?>
            <?php
              $value = (int) $value;
              if ($value < RESOSOIN_CELL_THRESHOLD) {
                  $hiddenSmall += $value;
                  continue;
              }
              $label = $question['options'][$key]
                  ?? ($key === 'not_applicable' ? 'Non concerné' : $key);
              $percent = $respondents > 0
                  ? round(($value / $respondents) * 100)
                  : 0;
            ?>
            <div class="option result-option">
              <span><?= resosoin_h((string) $label) ?></span>
              <strong><?= $value ?> · <?= $percent ?> %</strong>
            </div>
          <?php endforeach; ?>

          <?php if ($hiddenSmall > 0): ?>
            <div class="option result-option">
              <span>Réponses rares regroupées</span>
              <strong>&lt; <?= RESOSOIN_CELL_THRESHOLD ?> par catégorie</strong>
            </div>
          <?php endif; ?>
        </div>
      </section>
    <?php endforeach; ?>
  <?php endif; ?>

  <div class="actions">
    <a class="btn primary" href="./?audience=<?= resosoin_h($audience) ?>&amp;source=neutral_invite">
      Répondre au questionnaire
    </a>
  </div>
</main>

<footer class="footer">
  <span>RésoSoin · Le Potager du Web · Metz</span>
</footer>
</div>
</body>
</html>
