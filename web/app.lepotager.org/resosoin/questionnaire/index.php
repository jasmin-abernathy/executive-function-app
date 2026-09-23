<?php
declare(strict_types=1);
require __DIR__ . '/_common.php';
$pdo = null;
$ready = false;
try {
    $pdo = db();
    $ready = resosoin_tables_ready($pdo);
} catch (Throwable) {
    $ready = false;
}
$audience = ($_GET['audience'] ?? '') === 'doctor' ? 'doctor' : (($_GET['audience'] ?? '') === 'patient' ? 'patient' : '');
?><!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="description" content="Questionnaire RésoSoin pour professionnels de santé et patients.">
  <meta name="theme-color" content="#4f7a61">
  <title>Étude RésoSoin — questionnaire</title>
  <link rel="canonical" href="https://app.lepotager.org/resosoin/questionnaire/">
  <link rel="icon" href="/favicon.svg" type="image/svg+xml">
  <link rel="stylesheet" href="/resosoin/questionnaire/assets/style.css">
  <script>window.RESOSOIN_PREFILL_AUDIENCE = <?= json_encode($audience, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) ?>;</script>
  <script defer src="/resosoin/questionnaire/assets/survey.js"></script>
</head>
<body>
  <div class="shell">
    <header class="header">
      <a class="brand" href="/resosoin/">RésoSoin · étude</a>
      <div class="header-links">
        <a href="/resosoin/">Présentation</a>
        <a href="confidentialite.php">Confidentialité</a>
        <a href="resultats.php">Résultats agrégés</a>
      </div>
    </header>

    <main class="card" id="survey-app">
      <?php if (!$ready): ?>
        <p class="eyebrow">Étude RésoSoin</p>
        <h1>Le questionnaire est en préparation.</h1>
        <div class="notice warning">La page de présentation est disponible, mais la base de réponses n’est pas encore initialisée.</div>
        <div class="actions"><a class="btn primary" href="/resosoin/">Retour à RésoSoin</a></div>
      <?php else: ?>
        <section id="landing">
          <p class="eyebrow">Étude produit · 18 ans et plus</p>
          <h1>Avant de construire davantage, on veut vérifier le besoin.</h1>
          <p class="lead">Environ 5 minutes. Les premières questions portent sur vos habitudes actuelles. RésoSoin n’est présenté qu’ensuite pour éviter de biaiser artificiellement les réponses.</p>
          <div class="soft">
            <strong>Vie privée :</strong> pas de nom, pas de diagnostic, pas de traitement, pas de texte libre médical. Votre navigateur conserve seulement un jeton pseudonyme pour pouvoir reprendre plus tard. Vous pouvez supprimer vos réponses depuis cette page.
          </div>
          <div class="audiences">
            <article class="audience">
              <p class="eyebrow">Professionnels</p>
              <h2>Je suis professionnel·le de santé</h2>
              <p>Organisation du cabinet, outils actuels, coûts, automatisation, IA, souveraineté et MVP.</p>
              <button class="btn primary" type="button" data-start="doctor">Commencer</button>
            </article>
            <article class="audience">
              <p class="eyebrow">Patients</p>
              <h2>Je suis patient·e</h2>
              <p>Prise de rendez-vous, comptes, automatisations, IA, hébergement européen et app facultative.</p>
              <button class="btn primary" type="button" data-start="patient">Commencer</button>
            </article>
          </div>
          <div id="resume-box" class="notice resume" hidden>
            <div><strong>Une réponse en cours a été trouvée sur ce navigateur.</strong><span id="resume-label"></span></div>
            <button type="button" class="btn secondary" id="resume-button">Reprendre</button>
          </div>
        </section>

        <section id="questionnaire" hidden>
          <div class="progress-row"><span class="eyebrow" id="audience-label"></span><span class="question-count" id="question-count"></span></div>
          <div class="progress" aria-hidden="true"><span id="progress-bar" style="width:0"></span></div>
          <div id="concept-card" class="concept" hidden>
            <strong>Voici le concept testé.</strong>
            <span>RésoSoin vise un site indépendant pour chaque cabinet, un agenda simple, des automatisations explicites, aucune IA imposée, un hébergement européen privilégiant la souveraineté juridique et un prix nettement inférieur aux grandes plateformes. Sans IA ne veut pas dire sans automatisation&nbsp;: rappels, confirmations, listes d’attente, créneaux et synchronisations peuvent fonctionner avec des règles déterministes.</span>
          </div>
          <form id="question-form">
            <h1 id="question-title"></h1>
            <p class="lead" id="question-help" hidden></p>
            <div id="question-options"></div>
            <div class="status" id="save-status" role="status" aria-live="polite"></div>
            <div class="actions">
              <button type="button" class="btn secondary" id="back-button">Précédent</button>
              <button type="submit" class="btn primary" id="next-button">Suivant</button>
            </div>
          </form>
          <div class="actions"><button type="button" class="btn quiet" id="delete-button">Supprimer mes réponses</button></div>
        </section>

        <section id="done" hidden>
          <p class="eyebrow">Merci</p>
          <h1>Réponse enregistrée.</h1>
          <p class="lead">Ces réponses serviront à décider du périmètre de RésoSoin avant de poursuivre le développement.</p>
          <div class="soft"><strong>Votre questionnaire n’est relié à aucune adresse e-mail.</strong> Si vous voulez participer à un pilote, contactez-nous séparément : ce contact ne sera pas rattaché techniquement à vos réponses.</div>
          <div class="pilot">
            <h2>Tester RésoSoin plus tard</h2>
            <div class="actions">
              <a class="btn primary" id="pilot-link" href="mailto:contact@lepotager.org?subject=Pilote%20RésoSoin">Proposer ma participation</a>
              <a class="btn secondary" href="resultats.php">Voir les résultats agrégés</a>
              <a class="btn secondary" href="/resosoin/">Retour à la présentation</a>
            </div>
          </div>
        </section>
      <?php endif; ?>
    </main>

    <footer class="footer">
      <span>RésoSoin · Le Potager du Web · Metz</span>
      <span><a href="confidentialite.php">Confidentialité</a> · <a href="mailto:contact@lepotager.org">Contact</a></span>
    </footer>
  </div>
</body>
</html>
