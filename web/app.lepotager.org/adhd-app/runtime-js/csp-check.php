<?php
declare(strict_types=1);
header('Content-Type: application/javascript; charset=utf-8');
header('Cache-Control: no-store, max-age=0');
header('X-Content-Type-Options: nosniff');
?>
document.addEventListener('DOMContentLoaded', function () {
  var el = document.getElementById('status');
  if (el) {
    el.textContent = 'CSP OK — same-origin PHP-served JavaScript executed successfully.';
  }
});
