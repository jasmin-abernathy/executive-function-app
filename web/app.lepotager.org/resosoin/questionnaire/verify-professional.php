<?php

declare(strict_types=1);

require __DIR__ . '/_common.php';
require __DIR__ . '/_professional_verification.php';

header('Cache-Control: no-store');
resosoin_require_same_origin();

$contentLength = (int) ($_SERVER['CONTENT_LENGTH'] ?? 0);
if ($contentLength > 4096) {
    json_response(
        ['ok' => false, 'error' => 'Requête trop volumineuse.'],
        413
    );
}

$input = read_json_body();

if (($input['certification'] ?? false) !== true) {
    json_response(
        [
            'ok' => false,
            'error' => 'Vous devez certifier être le professionnel de santé correspondant au numéro RPPS saisi.',
        ],
        422
    );
}

$rpps = preg_replace(
    '/\D+/',
    '',
    (string) ($input['rpps'] ?? '')
);
$familyName = clean_text(
    $input['family_name'] ?? '',
    120
);

if (
    !is_string($rpps)
    || !preg_match('/^\d{11}$/', $rpps)
) {
    json_response(
        [
            'ok' => false,
            'error' => 'Le numéro RPPS doit contenir 11 chiffres.',
        ],
        422
    );
}

if (mb_strlen($familyName) < 2) {
    json_response(
        [
            'ok' => false,
            'error' => 'Renseignez votre nom d’exercice.',
        ],
        422
    );
}

$apiKey = resosoin_annuaire_sante_api_key();
if ($apiKey === '') {
    json_response(
        [
            'ok' => false,
            'error' => 'La vérification RPPS n’est pas encore configurée sur le serveur.',
        ],
        503
    );
}

$url = RESOSOIN_ANNUAIRE_SANTE_URL
    . '?_revinclude=PractitionerRole%3Apractitioner'
    . '&identifier='
    . rawurlencode($rpps)
    . '&_format=json';

$curl = curl_init($url);
if ($curl === false) {
    json_response(
        [
            'ok' => false,
            'error' => 'Vérification RPPS indisponible.',
        ],
        503
    );
}

curl_setopt_array($curl, [
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_HTTPHEADER => [
        'Accept: application/fhir+json, application/json',
        'ESANTE-API-KEY: ' . $apiKey,
    ],
    CURLOPT_CONNECTTIMEOUT => 4,
    CURLOPT_TIMEOUT => 7,
    CURLOPT_FOLLOWLOCATION => false,
    CURLOPT_MAXREDIRS => 0,
]);

$body = curl_exec($curl);
$status = (int) curl_getinfo(
    $curl,
    CURLINFO_RESPONSE_CODE
);
$curlError = curl_error($curl);
curl_close($curl);

if ($body === false || $curlError !== '') {
    error_log(
        '[RésoSoin RPPS] Annuaire Santé network error: '
        . $curlError
    );
    json_response(
        [
            'ok' => false,
            'error' => 'L’Annuaire Santé ne répond pas pour le moment. Réessayez plus tard.',
        ],
        503
    );
}

if ($status === 429) {
    json_response(
        [
            'ok' => false,
            'error' => 'Le service de vérification est momentanément trop sollicité. Réessayez dans quelques instants.',
        ],
        503
    );
}

if ($status < 200 || $status >= 300) {
    error_log(
        '[RésoSoin RPPS] Annuaire Santé HTTP '
        . $status
    );
    json_response(
        [
            'ok' => false,
            'error' => 'La vérification RPPS est temporairement indisponible.',
        ],
        503
    );
}

$bundle = json_decode((string) $body, true);
if (!is_array($bundle)) {
    json_response(
        [
            'ok' => false,
            'error' => 'Réponse Annuaire Santé invalide.',
        ],
        503
    );
}

$profession =
    resosoin_bundle_matches_health_professional(
        $bundle,
        $rpps,
        $familyName
    );

if (!is_array($profession)) {
    json_response(
        [
            'ok' => false,
            'error' => 'Le nom, le numéro RPPS ou la profession ne correspondent pas à une profession de santé active autorisée dans l’Annuaire Santé.',
        ],
        422
    );
}

$verificationToken =
    resosoin_issue_professional_verification_token(
        $rpps,
        (string) $profession['code'],
        app_key_bytes()
    );

json_response([
    'ok' => true,
    'verification_token' => $verificationToken,
    'profession' => $profession['label'],
    'expires_in' => RESOSOIN_PRO_VERIFICATION_TTL,
    'message' => 'Professionnel de santé vérifié : '
        . $profession['label']
        . '.',
]);
