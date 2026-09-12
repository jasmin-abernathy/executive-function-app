<?php
declare(strict_types=1);
header('Content-Type: text/html; charset=utf-8');
header('Cache-Control: no-store, max-age=0');
?><!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>CSP runtime check</title>
<style>
body{font-family:system-ui,sans-serif;max-width:760px;margin:40px auto;padding:20px}
#status{padding:18px;border:1px solid #bbb;border-radius:14px;line-height:1.5}
</style>
</head>
<body>
<h1>CSP runtime check</h1>
<div id="status">PHP/HTML OK. Waiting for same-origin external JavaScript…</div>
<script src="runtime-js/csp-check.php?v=049" defer></script>
</body>
</html>
