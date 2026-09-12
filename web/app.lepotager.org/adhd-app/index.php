<?php
declare(strict_types=1);

/*
 * V1 public entry redirect.
 *
 * This file intentionally redirects only:
 *   /adhd-app/
 *   /adhd-app/index.php
 *
 * Other V1 endpoints remain available:
 *   /adhd-app/admin/
 *   /adhd-app/results.php
 *   /adhd-app/privacy.php
 *   /adhd-app/confirm.php
 *   /adhd-app/unsubscribe.php
 *   /adhd-app/admin/contacts-followup.php
 *
 * Temporary redirect (302) is deliberate while V2 is still being stabilised.
 */

$params = [];

// Preserve only harmless navigation parameters that also make sense in V2.
// Never forward arbitrary V1 tokens or contact parameters.
$lang = isset($_GET['lang']) ? strtolower(trim((string) $_GET['lang'])) : '';
if (in_array($lang, ['fr', 'en'], true)) {
    $params['lang'] = $lang;
}

if (isset($_GET['fresh']) && (string) $_GET['fresh'] === '1') {
    $params['fresh'] = '1';
}

$target = 'v2/';
if ($params !== []) {
    $target .= '?' . http_build_query($params, '', '&', PHP_QUERY_RFC3986);
}

// Avoid browsers/CDNs caching the redirect while V2 is still evolving.
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');
header('Expires: 0');

header('Location: ' . $target, true, 302);
exit;
