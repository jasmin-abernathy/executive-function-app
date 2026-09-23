<?php
declare(strict_types=1);
require __DIR__ . '/_common.php';
$pdo = db();
$ready = resosoin_tables_ready($pdo);
$audience = ($_GET['audience'] ?? '') === 'patient' ? 'patient' : 'doctor';
$catalog = resosoin_questions();
$questions = resosoin_question_map($audience);
$total = 0;
$counts = [];
if ($ready) {
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM resosoin_survey_sessions WHERE audience = ? AND status = 'submitted'");
    $stmt->execute([$audience]);
    $total = (int) $stmt->fetchColumn();
    if ($total >= 10) {
        $stmt = $pdo->prepare(
            "SELECT a.question_id, a.answer_json
             FROM resosoin_survey_answers a
             JOIN resosoin_survey_sessions s ON s.id = a.session_id
             WHERE s.audience = ? AND s.status = 'submitted'"
        );
        $stmt->execute([$audience]);
        foreach ($stmt->fetchAll() as $row) {
            $answer = json_decode((string) $row['answer_json'], true);
            foreach (is_array($answer) ? $answer : [$answer] as $value) {
                $key = (string) $value;
                $qid = (string) $row['question_id'];
                $counts[$qid][$key] = ($counts[$qid][$key] ?? 0) + 1;
            }
        }
    }
}
$displayIds = $audience === 'doctor'
    ? ['doctor_pain_points','doctor_eu_jurisdiction','doctor_ai_preference','doctor_automation','doctor_concept_interest','doctor_concept_reasons','doctor_price']
    : ['patient_direct_site','patient_eu_jurisdiction','patient_ai_preference','patient_automation','patient_structure_preference','patient_concept_interest','patient_app_preference'];
?><!doctype html><html lang="fr"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="theme-color" content="#4f7a61"><title>Résultats agrégés — RésoSoin</title><link rel="icon" href="/favicon.svg" type="image/svg+xml"><link rel="stylesheet" href="/resosoin/questionnaire/assets/style.css"></head><body><div class="shell"><header class="header"><a class="brand" href="/resosoin/">RésoSoin · étude</a><div class="header-links"><a href="./">Questionnaire</a><a href="confidentialite.php">Confidentialité</a></div></header><main class="card"><p class="eyebrow">Résultats agrégés</p><h1><?= $audience === 'doctor' ? 'Professionnels de santé' : 'Patients' ?></h1><div class="actions"><a class="btn <?= $audience === 'doctor' ? 'primary' : 'secondary' ?>" href="?audience=doctor">Professionnels</a><a class="btn <?= $audience === 'patient' ? 'primary' : 'secondary' ?>" href="?audience=patient">Patients</a></div><p class="lead">Les répartitions ne sont affichées qu’à partir de 10 questionnaires envoyés pour le profil concerné, afin d’éviter de sur-interpréter quelques réponses isolées.</p><?php if (!$ready): ?><div class="notice warning">La base de l’étude n’est pas encore initialisée.</div><?php elseif ($total < 10): ?><div class="notice"><?= $total ?> réponse<?= $total > 1 ? 's' : '' ?> envoyée<?= $total > 1 ? 's' : '' ?>. Les détails apparaîtront à partir de 10.</div><?php else: ?><?php foreach ($displayIds as $qid): $q = $questions[$qid] ?? null; if (!is_array($q)) continue; $c = $counts[$qid] ?? []; arsort($c); ?><section><h2><?= resosoin_h((string) $q['title']) ?></h2><div class="options"><?php foreach ($c as $key => $value): $label = $q['options'][$key] ?? $key; ?><div class="option"><span><?= resosoin_h((string) $label) ?></span><strong><?= (int) $value ?></strong></div><?php endforeach; ?></div></section><?php endforeach; ?><?php endif; ?><div class="actions"><a class="btn primary" href="./?audience=<?= resosoin_h($audience) ?>">Répondre au questionnaire</a></div></main><footer class="footer"><span>RésoSoin · Le Potager du Web · Metz</span></footer></div></body></html>
