<?php

declare(strict_types=1);

function base64url_encode(string $data): string
{
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function random_token(int $bytes = 32): string
{
    return base64url_encode(random_bytes($bytes));
}

function uuid_v4(): string
{
    $data = random_bytes(16);
    $data[6] = chr((ord($data[6]) & 0x0f) | 0x40);
    $data[8] = chr((ord($data[8]) & 0x3f) | 0x80);
    return vsprintf('%s%s-%s-%s-%s-%s%s%s', str_split(bin2hex($data), 4));
}

function app_key_bytes(): string
{
    $key = base64_decode((string) config('app_key'), true);
    if ($key === false || strlen($key) !== 32) {
        throw new RuntimeException('The configured app_key must be a base64-encoded 32-byte key.');
    }
    return $key;
}

function token_hash(string $token): string
{
    return hash_hmac('sha256', $token, app_key_bytes());
}

function email_hash(string $email): string
{
    return hash_hmac('sha256', mb_strtolower(trim($email)), app_key_bytes());
}

function encrypt_string(?string $value): ?string
{
    if ($value === null || $value === '') {
        return null;
    }

    $key = app_key_bytes();

    if (function_exists('sodium_crypto_secretbox')) {
        $nonce = random_bytes(SODIUM_CRYPTO_SECRETBOX_NONCEBYTES);
        $cipher = sodium_crypto_secretbox($value, $nonce, $key);
        return 'sodium:' . base64_encode($nonce . $cipher);
    }

    $iv = random_bytes(12);
    $tag = '';
    $cipher = openssl_encrypt($value, 'aes-256-gcm', $key, OPENSSL_RAW_DATA, $iv, $tag);
    if ($cipher === false) {
        throw new RuntimeException('Encryption failed.');
    }
    return 'openssl:' . base64_encode($iv . $tag . $cipher);
}

function decrypt_string(?string $payload): ?string
{
    if ($payload === null || $payload === '') {
        return null;
    }

    $key = app_key_bytes();

    if (str_starts_with($payload, 'sodium:')) {
        if (!function_exists('sodium_crypto_secretbox_open')) {
            return null;
        }
        $raw = base64_decode(substr($payload, 7), true);
        if ($raw === false || strlen($raw) <= SODIUM_CRYPTO_SECRETBOX_NONCEBYTES) {
            return null;
        }
        $nonce = substr($raw, 0, SODIUM_CRYPTO_SECRETBOX_NONCEBYTES);
        $cipher = substr($raw, SODIUM_CRYPTO_SECRETBOX_NONCEBYTES);
        $plain = sodium_crypto_secretbox_open($cipher, $nonce, $key);
        return $plain === false ? null : $plain;
    }

    if (str_starts_with($payload, 'openssl:')) {
        if (!function_exists('openssl_decrypt')) {
            return null;
        }
        $raw = base64_decode(substr($payload, 8), true);
        if ($raw === false || strlen($raw) <= 28) {
            return null;
        }
        $iv = substr($raw, 0, 12);
        $tag = substr($raw, 12, 16);
        $cipher = substr($raw, 28);
        $plain = openssl_decrypt($cipher, 'aes-256-gcm', $key, OPENSSL_RAW_DATA, $iv, $tag);
        return $plain === false ? null : $plain;
    }

    return null;
}

function json_response(array $payload, int $status = 200): never
{
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-store');
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function read_json_body(): array
{
    $raw = file_get_contents('php://input');
    if ($raw === false || trim($raw) === '') {
        return [];
    }
    $data = json_decode($raw, true);
    if (!is_array($data)) {
        json_response(['ok' => false, 'error' => 'Invalid JSON body.'], 400);
    }
    return $data;
}

function clean_text(mixed $value, int $maxLength = 500): string
{
    $text = trim((string) $value);
    return mb_substr($text, 0, $maxLength);
}

function valid_email(string $email): bool
{
    return filter_var($email, FILTER_VALIDATE_EMAIL) !== false && mb_strlen($email) <= 254;
}

function same_origin_request(): bool
{
    $origin = $_SERVER['HTTP_ORIGIN'] ?? '';
    if ($origin === '') {
        return true;
    }

    $site = parse_url((string) config('site_url'));
    $candidate = parse_url($origin);
    if (!is_array($site) || !is_array($candidate)) {
        return false;
    }

    $siteScheme = strtolower((string) ($site['scheme'] ?? ''));
    $originScheme = strtolower((string) ($candidate['scheme'] ?? ''));
    $siteHost = strtolower((string) ($site['host'] ?? ''));
    $originHost = strtolower((string) ($candidate['host'] ?? ''));
    $sitePort = (int) ($site['port'] ?? ($siteScheme === 'https' ? 443 : 80));
    $originPort = (int) ($candidate['port'] ?? ($originScheme === 'https' ? 443 : 80));

    return $siteScheme !== ''
        && hash_equals($siteScheme, $originScheme)
        && hash_equals($siteHost, $originHost)
        && $sitePort === $originPort;
}

function require_same_origin(): void
{
    if (!same_origin_request()) {
        json_response(['ok' => false, 'error' => 'Cross-origin request blocked.'], 403);
    }
}

function session_from_token(PDO $pdo, string $token): ?array
{
    if ($token === '') {
        return null;
    }
    $stmt = $pdo->prepare('SELECT * FROM survey_sessions WHERE resume_token_hash = ? AND status <> \'deleted\' LIMIT 1');
    $stmt->execute([token_hash($token)]);
    $row = $stmt->fetch();
    return $row ?: null;
}

function format_site_url(string $path = ''): string
{
    return rtrim((string) config('site_url'), '/') . '/' . ltrim($path, '/');
}
