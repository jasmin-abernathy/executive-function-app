<?php
declare(strict_types=1);
header('Content-Type: application/javascript; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');
header('X-Content-Type-Options: nosniff');
?>
(() => {
  "use strict";
  setTimeout(() => {
    if (window.__ADHD_V2_READY__) return;
    const root = document.getElementById("app");
    if (!root) return;
    root.innerHTML = `
      <section class="card">
        <p class="eyebrow">Loading recovery / Récupération</p>
        <h2>The questionnaire did not initialise / Le questionnaire ne s’est pas initialisé</h2>
        <p class="lead">Your saved server response has not been deleted.</p>
        <p class="lead">Votre réponse déjà enregistrée sur le serveur n’a pas été supprimée.</p>
        <div class="actions">
          <a class="btn primary" href="./?fresh=1">Start locally fresh / Repartir localement</a>
          <a class="btn secondary" href="./">Retry / Réessayer</a>
        </div>
      </section>`;
  }, 4500);
})();
