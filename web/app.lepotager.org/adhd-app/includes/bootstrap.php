<?php

declare(strict_types=1);

$root = dirname(__DIR__);
$configFile = $root . '/config.php';

if (!is_file($configFile)) {
    if (PHP_SAPI === 'cli') {
        fwrite(STDERR, "Missing config.php. Run the web installer first.\n");
        exit(1);
    }
    $request = basename((string) ($_SERVER['SCRIPT_NAME'] ?? ''));
    if ($request !== 'index.php' || !str_contains((string) ($_SERVER['SCRIPT_NAME'] ?? ''), '/setup/')) {
        header('Location: setup/');
        exit;
    }
}

$GLOBALS['APP_CONFIG'] = is_file($configFile) ? require $configFile : [];

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
