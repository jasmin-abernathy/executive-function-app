<?php
declare(strict_types=1);

const CONTRIBUTION_SHARED_STANCER_CONFIG = '/home/sc1leja3715/stancer-private/private/stancer-config.php';

function contribution_start_session(): void
{
    if (session_status() === PHP_SESSION_ACTIVE) return;
    $https = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off')
        || (($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? '') === 'https');
    ini_set('session.use_strict_mode', '1');
    ini_set('session.use_only_cookies', '1');
    session_name((string)CONTRIBUTION_SESSION_NAME);
    session_set_cookie_params([
        'lifetime' => 0,
        'path' => '/soutenir/',
        'domain' => '',
        'secure' => $https,
        'httponly' => true,
        'samesite' => 'Lax',
    ]);
    session_start();
}

function contribution_headers(): void
{
    header('X-Content-Type-Options: nosniff');
    header('Referrer-Policy: strict-origin-when-cross-origin');
    header('Cache-Control: private, no-store, no-cache, must-revalidate, max-age=0');
    header('Pragma: no-cache');
    header("Content-Security-Policy: default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; form-action 'self'; base-uri 'self'; frame-ancestors 'self'");
}

function contribution_escape(string $value): string
{
    return htmlspecialchars($value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function contribution_config(): array
{
    static $config = null;
    if (is_array($config)) return $config;
    if (!is_readable(CONTRIBUTION_SHARED_STANCER_CONFIG)) throw new RuntimeException('Configuration de paiement indisponible.');
    $raw = require CONTRIBUTION_SHARED_STANCER_CONFIG;
    if (!is_array($raw)) throw new RuntimeException('Configuration de paiement invalide.');
    $key = (string)($raw['stancer_secret_key'] ?? $raw['api_key'] ?? '');
    $api = (string)($raw['stancer_api_base'] ?? $raw['api_base'] ?? 'https://api.stancer.com/v2');
    if (!preg_match('/^s(?:test|prod)_[A-Za-z0-9]+$/', $key)) throw new RuntimeException('Configuration Stancer invalide.');
    if (parse_url($api, PHP_URL_SCHEME) !== 'https' || parse_url($api, PHP_URL_HOST) !== 'api.stancer.com') throw new RuntimeException('Adresse API Stancer invalide.');
    return $config = ['key'=>$key,'api'=>rtrim($api,'/')];
}

function contribution_request(string $method, string $path, ?array $payload = null): array
{
    $config = contribution_config();
    $ch = curl_init($config['api'].'/'.ltrim($path,'/'));
    if ($ch === false) throw new RuntimeException('Connexion Stancer impossible.');
    $headers = ['Accept: application/json','User-Agent: '.CONTRIBUTION_SITE_ID.'-Contribution/1.0','Expect:'];
    $opts = [
        CURLOPT_RETURNTRANSFER=>true,CURLOPT_HEADER=>false,CURLOPT_CONNECTTIMEOUT=>5,CURLOPT_TIMEOUT=>15,
        CURLOPT_FOLLOWLOCATION=>false,CURLOPT_USERPWD=>$config['key'].':',CURLOPT_HTTPAUTH=>CURLAUTH_BASIC,
        CURLOPT_SSL_VERIFYPEER=>true,CURLOPT_SSL_VERIFYHOST=>2,CURLOPT_PROTOCOLS=>CURLPROTO_HTTPS,CURLOPT_HTTP_VERSION=>CURL_HTTP_VERSION_1_1,
    ];
    if (strtoupper($method) !== 'GET') $opts[CURLOPT_CUSTOMREQUEST] = strtoupper($method);
    if ($payload !== null) {
        $body = json_encode($payload, JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES|JSON_THROW_ON_ERROR);
        $opts[CURLOPT_POSTFIELDS]=$body;$headers[]='Content-Type: application/json';$headers[]='Content-Length: '.strlen($body);
    }
    $opts[CURLOPT_HTTPHEADER]=$headers;curl_setopt_array($ch,$opts);
    $body=curl_exec($ch);$errno=curl_errno($ch);$status=(int)curl_getinfo($ch,CURLINFO_RESPONSE_CODE);curl_close($ch);
    if($body===false||$errno!==0)throw new RuntimeException('Stancer est momentanément indisponible.');
    $data=$body!==''?json_decode($body,true,64,JSON_THROW_ON_ERROR):[];
    if($status<200||$status>=300||!is_array($data))throw new RuntimeException('Stancer a refusé la demande.');
    return $data;
}

function contribution_amount_to_cents(mixed $value): int
{
    $raw=str_replace([' ','€',','],['','','.'],trim((string)$value));
    if(!preg_match('/^\d{1,4}(?:\.\d{1,2})?$/',$raw))throw new InvalidArgumentException('Montant invalide.');
    [$euros,$dec]=array_pad(explode('.',$raw,2),2,'');
    $cents=((int)$euros*100)+(int)substr(str_pad($dec,2,'0'),0,2);
    if($cents<100||$cents>50000)throw new InvalidArgumentException('Le montant doit être compris entre 1 € et 500 €.');
    return $cents;
}

function contribution_same_origin(): bool
{
    foreach(['HTTP_ORIGIN','HTTP_REFERER'] as $key){
        $value=(string)($_SERVER[$key]??'');if($value==='')continue;
        $host=(string)(parse_url($value,PHP_URL_HOST)??'');
        if($host!==''&&strcasecmp($host,(string)CONTRIBUTION_SITE_HOST)!==0)return false;
    }
    return true;
}

function contribution_status(array $intent, ?array $payment): array
{
    $status=strtolower((string)($payment['status']??$intent['status']??'unknown'));
    if(in_array($status,['captured','paid','succeeded'],true))return ['kind'=>'success','label'=>'Confirmé'];
    if(in_array($status,['to_capture','capture_sent','authorized','processing','pending','requires_capture','require_payment_method'],true))return ['kind'=>'pending','label'=>'En cours'];
    if(in_array($status,['failed','refused','expired','cancelled','canceled','disputed'],true))return ['kind'=>'error','label'=>'Non abouti'];
    return ['kind'=>'pending','label'=>'Vérification en cours'];
}
