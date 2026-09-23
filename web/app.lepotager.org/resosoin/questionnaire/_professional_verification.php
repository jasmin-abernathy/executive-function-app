<?php

declare(strict_types=1);

const RESOSOIN_ANNUAIRE_SANTE_URL =
    'https://gateway.api.esante.gouv.fr/fhir/v2/Practitioner';
const RESOSOIN_PRO_VERIFICATION_TTL = 600;
const RESOSOIN_PRACTITIONER_ROLE_NAME_EXTENSION =
    'https://interop.esante.gouv.fr/ig/fhir/annuaire/StructureDefinition/as-ext-practitionerrole-name';

/**
 * Active codes from TRE_G15-ProfessionSante used by RésoSoin.
 * Deprecated TRE_G15 codes are intentionally excluded.
 */
function resosoin_allowed_health_professions(): array
{
    return [
        '10' => 'Médecin',
        '21' => 'Pharmacien',
        '26' => 'Audioprothésiste',
        '28' => 'Opticien-Lunetier',
        '31' => 'Assistant dentaire',
        '32' => 'Physicien médical',
        '40' => 'Chirurgien-Dentiste',
        '50' => 'Sage-Femme',
        '60' => 'Infirmier',
        '69' => 'Infirmier psychiatrique',
        '70' => 'Masseur-Kinésithérapeute',
        '80' => 'Pédicure-Podologue',
        '81' => 'Orthoprothésiste',
        '82' => 'Podo-Orthésiste',
        '83' => 'Orthopédiste-Orthésiste',
        '84' => 'Oculariste',
        '85' => 'Epithésiste',
        '86' => 'Technicien de laboratoire médical',
        '91' => 'Orthophoniste',
        '92' => 'Orthoptiste',
        '94' => 'Ergothérapeute',
        '95' => 'Diététicien',
        '96' => 'Psychomotricien',
        '98' => 'Manipulateur ERM',
    ];
}

function resosoin_normalize_professional_name(string $value): string
{
    $value = trim($value);
    if ($value === '') {
        return '';
    }

    $ascii = iconv(
        'UTF-8',
        'ASCII//TRANSLIT//IGNORE',
        $value
    );
    if ($ascii !== false) {
        $value = $ascii;
    }

    $value = strtoupper($value);

    return (string) preg_replace(
        '/[^A-Z0-9]/',
        '',
        $value
    );
}

function resosoin_resource_has_rpps(
    array $resource,
    string $rpps
): bool {
    foreach (($resource['identifier'] ?? []) as $identifier) {
        if (
            is_array($identifier)
            && (string) ($identifier['value'] ?? '') === $rpps
        ) {
            return true;
        }
    }

    return false;
}

function resosoin_human_name_matches(
    array $name,
    string $familyName
): bool {
    $expected =
        resosoin_normalize_professional_name(
            $familyName
        );
    if ($expected === '') {
        return false;
    }

    $candidate =
        resosoin_normalize_professional_name(
            (string) ($name['family'] ?? '')
        );

    return $candidate !== ''
        && hash_equals($expected, $candidate);
}

function resosoin_practitioner_name_matches(
    array $practitioner,
    string $familyName
): bool {
    foreach (($practitioner['name'] ?? []) as $name) {
        if (
            is_array($name)
            && resosoin_human_name_matches(
                $name,
                $familyName
            )
        ) {
            return true;
        }
    }

    return false;
}

function resosoin_role_name_matches(
    array $role,
    string $familyName
): bool {
    foreach (($role['extension'] ?? []) as $extension) {
        if (!is_array($extension)) {
            continue;
        }

        if (
            (string) ($extension['url'] ?? '')
            !== RESOSOIN_PRACTITIONER_ROLE_NAME_EXTENSION
        ) {
            continue;
        }

        $name = $extension['valueHumanName'] ?? null;
        if (
            is_array($name)
            && resosoin_human_name_matches(
                $name,
                $familyName
            )
        ) {
            return true;
        }
    }

    return false;
}

function resosoin_role_health_profession(
    array $role
): ?array {
    if (($role['active'] ?? true) === false) {
        return null;
    }

    $allowed = resosoin_allowed_health_professions();

    foreach (($role['code'] ?? []) as $codeableConcept) {
        if (!is_array($codeableConcept)) {
            continue;
        }

        foreach (($codeableConcept['coding'] ?? []) as $coding) {
            if (!is_array($coding)) {
                continue;
            }

            $code = (string) ($coding['code'] ?? '');
            $system = strtoupper(
                (string) ($coding['system'] ?? '')
            );

            $isProfessionSystem =
                str_contains(
                    $system,
                    'TRE_G15-PROFESSIONSANTE'
                )
                || str_contains(
                    $system,
                    'TRE-G15-PROFESSIONSANTE'
                );

            if (
                $isProfessionSystem
                && array_key_exists(
                    $code,
                    $allowed
                )
            ) {
                return [
                    'code' => $code,
                    'label' => $allowed[$code],
                ];
            }
        }
    }

    return null;
}

function resosoin_role_references_practitioner(
    array $role,
    string $practitionerId
): bool {
    $reference = (string) (
        $role['practitioner']['reference'] ?? ''
    );

    if ($reference === '' || $practitionerId === '') {
        return false;
    }

    return $reference === 'Practitioner/' . $practitionerId
        || str_ends_with(
            $reference,
            '/Practitioner/' . $practitionerId
        );
}

function resosoin_bundle_matches_health_professional(
    array $bundle,
    string $rpps,
    string $familyName
): ?array {
    $practitioners = [];
    $roles = [];

    foreach (($bundle['entry'] ?? []) as $entry) {
        if (!is_array($entry)) {
            continue;
        }

        $resource = $entry['resource'] ?? null;
        if (!is_array($resource)) {
            continue;
        }

        if (
            ($resource['resourceType'] ?? '')
            === 'Practitioner'
        ) {
            $practitioners[] = $resource;
        } elseif (
            ($resource['resourceType'] ?? '')
            === 'PractitionerRole'
        ) {
            $roles[] = $resource;
        }
    }

    foreach ($practitioners as $practitioner) {
        if (
            ($practitioner['active'] ?? true) === false
            || !resosoin_resource_has_rpps(
                $practitioner,
                $rpps
            )
        ) {
            continue;
        }

        $practitionerId = (string) (
            $practitioner['id'] ?? ''
        );

        foreach ($roles as $role) {
            if (
                !resosoin_role_references_practitioner(
                    $role,
                    $practitionerId
                )
            ) {
                continue;
            }

            $profession =
                resosoin_role_health_profession($role);
            if ($profession === null) {
                continue;
            }

            $nameMatches =
                resosoin_role_name_matches(
                    $role,
                    $familyName
                )
                || resosoin_practitioner_name_matches(
                    $practitioner,
                    $familyName
                );

            if ($nameMatches) {
                return $profession;
            }
        }
    }

    return null;
}

function resosoin_base64url_decode(
    string $value
): string|false {
    $padding = strlen($value) % 4;
    if ($padding !== 0) {
        $value .= str_repeat('=', 4 - $padding);
    }

    return base64_decode(
        strtr($value, '-_', '+/'),
        true
    );
}

function resosoin_issue_professional_verification_token(
    string $rpps,
    string $professionCode,
    string $appKey,
    ?int $now = null
): string {
    $now ??= time();

    $payload = [
        'v' => 1,
        'kind' => 'health_professional',
        'method' => 'annuaire_sante_tre_g15',
        'profession_code' => $professionCode,
        'iat' => $now,
        'exp' => $now + RESOSOIN_PRO_VERIFICATION_TTL,
        'rpps_digest' => hash_hmac(
            'sha256',
            $rpps,
            $appKey
        ),
    ];

    $json = json_encode(
        $payload,
        JSON_UNESCAPED_UNICODE
        | JSON_UNESCAPED_SLASHES
    );
    if ($json === false) {
        throw new RuntimeException(
            'Impossible de créer la preuve de vérification.'
        );
    }

    $encoded = rtrim(
        strtr(
            base64_encode($json),
            '+/',
            '-_'
        ),
        '='
    );

    $signature = hash_hmac(
        'sha256',
        $encoded,
        $appKey
    );

    return $encoded . '.' . $signature;
}

function resosoin_validate_professional_verification_token(
    string $token,
    string $appKey,
    ?int $now = null
): ?array {
    $now ??= time();

    $parts = explode('.', $token, 2);
    if (count($parts) !== 2) {
        return null;
    }

    [$encoded, $signature] = $parts;
    $expected = hash_hmac(
        'sha256',
        $encoded,
        $appKey
    );

    if (!hash_equals($expected, $signature)) {
        return null;
    }

    $raw = resosoin_base64url_decode($encoded);
    if ($raw === false) {
        return null;
    }

    $payload = json_decode($raw, true);
    $allowed = resosoin_allowed_health_professions();
    $professionCode =
        (string) ($payload['profession_code'] ?? '');

    if (
        !is_array($payload)
        || ($payload['kind'] ?? '')
            !== 'health_professional'
        || ($payload['method'] ?? '')
            !== 'annuaire_sante_tre_g15'
        || !array_key_exists(
            $professionCode,
            $allowed
        )
        || (int) ($payload['exp'] ?? 0) < $now
        || (int) ($payload['iat'] ?? 0) > $now + 60
    ) {
        return null;
    }

    return $payload;
}

function resosoin_private_runtime_dir(): string
{
    $candidates = [];

    $envHome = getenv('HOME');
    if (
        is_string($envHome)
        && $envHome !== ''
    ) {
        $candidates[] = $envHome;
    }

    $serverHome = $_SERVER['HOME'] ?? '';
    if (
        is_string($serverHome)
        && $serverHome !== ''
    ) {
        $candidates[] = $serverHome;
    }

    $marker = '/public_html/';
    $position = strpos(__DIR__, $marker);
    if ($position !== false) {
        $candidates[] = substr(
            __DIR__,
            0,
            $position
        );
    }

    foreach (array_unique($candidates) as $home) {
        if ($home !== '') {
            return rtrim($home, '/')
                . '/private/executive-function-app';
        }
    }

    throw new RuntimeException(
        'Répertoire privé introuvable.'
    );
}

function resosoin_annuaire_sante_secret_path(): string
{
    return resosoin_private_runtime_dir()
        . '/resosoin-annuaire-sante.json';
}

function resosoin_annuaire_sante_secret_record(): array
{
    try {
        $path =
            resosoin_annuaire_sante_secret_path();
    } catch (Throwable) {
        return [];
    }

    if (!is_file($path)) {
        return [];
    }

    $decoded = json_decode(
        (string) file_get_contents($path),
        true
    );

    return is_array($decoded) ? $decoded : [];
}

function resosoin_annuaire_sante_api_key(): string
{
    $record =
        resosoin_annuaire_sante_secret_record();

    $runtimeKey = trim(
        (string) ($record['api_key'] ?? '')
    );
    if ($runtimeKey !== '') {
        return $runtimeKey;
    }

    if (function_exists('config')) {
        return trim(
            (string) config(
                'annuaire_sante.api_key',
                ''
            )
        );
    }

    return '';
}

function resosoin_test_annuaire_sante_api_key(
    string $apiKey
): array {
    $apiKey = trim($apiKey);
    if (
        strlen($apiKey) < 8
        || strlen($apiKey) > 512
    ) {
        return [
            'ok' => false,
            'message' =>
                'Format de clé API invalide.',
        ];
    }

    $url = RESOSOIN_ANNUAIRE_SANTE_URL
        . '?_count=1&_format=json';

    $curl = curl_init($url);
    if ($curl === false) {
        return [
            'ok' => false,
            'message' =>
                'Impossible de préparer le test API.',
        ];
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
    $error = curl_error($curl);
    curl_close($curl);

    if (
        $body === false
        || $error !== ''
    ) {
        return [
            'ok' => false,
            'message' =>
                'Annuaire Santé injoignable pour le moment.',
        ];
    }

    if (
        $status === 401
        || $status === 403
    ) {
        return [
            'ok' => false,
            'message' =>
                'La clé API a été refusée par l’Annuaire Santé.',
        ];
    }

    if (
        $status < 200
        || $status >= 300
    ) {
        return [
            'ok' => false,
            'message' =>
                'Test Annuaire Santé indisponible (HTTP '
                . $status
                . ').',
        ];
    }

    $decoded = json_decode(
        (string) $body,
        true
    );

    if (
        !is_array($decoded)
        || ($decoded['resourceType'] ?? '')
            !== 'Bundle'
    ) {
        return [
            'ok' => false,
            'message' =>
                'Réponse Annuaire Santé inattendue.',
        ];
    }

    return [
        'ok' => true,
        'message' =>
            'Clé API validée par l’Annuaire Santé.',
    ];
}

function resosoin_store_annuaire_sante_api_key(
    string $apiKey
): void {
    $apiKey = trim($apiKey);
    if (
        strlen($apiKey) < 8
        || strlen($apiKey) > 512
    ) {
        throw new DomainException(
            'Format de clé API invalide.'
        );
    }

    $dir = resosoin_private_runtime_dir();
    if (!is_dir($dir)) {
        if (
            !mkdir(
                $dir,
                0700,
                true
            )
            && !is_dir($dir)
        ) {
            throw new RuntimeException(
                'Impossible de créer le stockage privé.'
            );
        }
    }

    @chmod($dir, 0700);

    $path =
        resosoin_annuaire_sante_secret_path();
    $temp = $path
        . '.tmp-'
        . bin2hex(random_bytes(6));

    $payload = json_encode(
        [
            'api_key' => $apiKey,
            'updated_at' => gmdate('c'),
        ],
        JSON_PRETTY_PRINT
        | JSON_UNESCAPED_SLASHES
    );

    if ($payload === false) {
        throw new RuntimeException(
            'Impossible de préparer la clé API.'
        );
    }

    if (
        file_put_contents(
            $temp,
            $payload . PHP_EOL,
            LOCK_EX
        ) === false
    ) {
        throw new RuntimeException(
            'Impossible d’écrire la clé API.'
        );
    }

    @chmod($temp, 0600);

    if (!rename($temp, $path)) {
        @unlink($temp);
        throw new RuntimeException(
            'Impossible de finaliser la clé API.'
        );
    }

    @chmod($path, 0600);

    $stored =
        resosoin_annuaire_sante_secret_record();

    if (
        !hash_equals(
            $apiKey,
            (string) ($stored['api_key'] ?? '')
        )
    ) {
        throw new RuntimeException(
            'La clé API n’a pas été relue correctement.'
        );
    }
}

function resosoin_delete_annuaire_sante_api_key(): void
{
    $path =
        resosoin_annuaire_sante_secret_path();

    if (
        is_file($path)
        && !unlink($path)
    ) {
        throw new RuntimeException(
            'Impossible de supprimer la clé API.'
        );
    }
}
