<?php

declare(strict_types=1);

$root = dirname(__DIR__);

$homeCandidates = [];

$envHome = getenv('HOME');
if (is_string($envHome) && $envHome !== '') {
    $homeCandidates[] = $envHome;
}

$serverHome = $_SERVER['HOME'] ?? '';
if (is_string($serverHome) && $serverHome !== '') {
    $homeCandidates[] = $serverHome;
}

// Fallback o2switch : déduire /home/<compte> depuis un chemin situé sous public_html.
$publicHtmlMarker = '/public_html/';
$markerPosition = strpos(__DIR__, $publicHtmlMarker);
if ($markerPosition !== false) {
    $homeCandidates[] = substr(__DIR__, 0, $markerPosition);
}

$configCandidates = [];
foreach (array_unique($homeCandidates) as $home) {
    $configCandidates[] =
        rtrim($home, '/') . '/private/executive-function-app/config.php';
}

// Compatibilité temporaire avec l'ancienne installation.
$configCandidates[] = $root . '/config.php';

$configFile = null;
foreach ($configCandidates as $candidate) {
    if (is_file($candidate)) {
        $configFile = $candidate;
        break;
    }
}

if ($configFile === null) {
    if (PHP_SAPI === 'cli') {
        fwrite(STDERR, "Missing application configuration.\n");
        exit(1);
    }

    $request = basename((string) ($_SERVER['SCRIPT_NAME'] ?? ''));
    if (
        $request !== 'index.php'
        || !str_contains((string) ($_SERVER['SCRIPT_NAME'] ?? ''), '/setup/')
    ) {
        header('Location: setup/');
        exit;
    }
}

$GLOBALS['APP_CONFIG'] = $configFile !== null ? require $configFile : [];

function config(?string $key = null, mixed $default = null): mixed
{
    $config = $GLOBALS['APP_CONFIG'] ?? [];
    if ($key === null) {
        return $config;
    }
    $segments = explode('.', $key);
    $value = $config;
    foreach ($segments as $segment) {
        if (!is_array($value) || !array_key_exists($segment, $value)) {
            return $default;
        }
        $value = $value[$segment];
    }
    return $value;
}

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/security.php';
require_once __DIR__ . '/mailer.php';
require_once __DIR__ . '/templates.php';

date_default_timezone_set('Europe/Paris');

if (PHP_SAPI !== 'cli') {
    header('X-Content-Type-Options: nosniff');
    header('Referrer-Policy: strict-origin-when-cross-origin');
    header('Permissions-Policy: camera=(), microphone=(), geolocation=()');
}
