<?php
declare(strict_types=1);
header('Content-Type: application/javascript; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');
header('X-Content-Type-Options: nosniff');
?>
/* Recovery watchdog — CSP-safe */
window.setTimeout(function () {
  var body = document.getElementById('survey-body');
  if (!body) return;

  var meaningful = body.querySelector(
    '.question-wrap, .welcome-wrap, .completion-wrap, form, [data-question-id]'
  );

  if (!meaningful && body.textContent.trim().length < 40) {
    body.innerHTML =
      '<div class="question-wrap">' +
      '<p class="eyebrow">Temporary loading problem</p>' +
      '<h1>The questionnaire did not finish starting</h1>' +
      '<p class="lead">A saved browser session may be waiting on the server. ' +
      'Starting a fresh local session does not delete already submitted responses.</p>' +
      '<div class="action-row">' +
      '<a class="btn btn-primary" href="?fresh=1">Start a fresh local session</a>' +
      '<a class="btn btn-secondary" href="privacy.php">Privacy information</a>' +
      '</div></div>';
  }
}, 9000);
