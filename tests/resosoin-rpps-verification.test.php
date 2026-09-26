<?php

declare(strict_types=1);

require dirname(__DIR__)
    . '/web/app.lepotager.org/resosoin/questionnaire/_professional_verification.php';

function expect_true(bool $value, string $message): void
{
    if (!$value) {
        throw new RuntimeException($message);
    }
}

function practitioner(): array
{
    return [
        'resourceType' => 'Practitioner',
        'id' => 'practitioner-1',
        'active' => true,
        'identifier' => [
            [
                'system' => 'http://rpps.fr',
                'value' => '10003461033',
            ],
        ],
        'name' => [
            [
                'family' => 'Lévêque-Dupont',
                'given' => ['Camille'],
            ],
        ],
    ];
}

function role(
    string $code,
    string $display,
    string $system =
        'https://mos.esante.gouv.fr/NOS/TRE_G15-ProfessionSante/FHIR/TRE-G15-ProfessionSante'
): array {
    return [
        'resourceType' => 'PractitionerRole',
        'id' => 'role-' . $code,
        'active' => true,
        'practitioner' => [
            'reference' => 'Practitioner/practitioner-1',
        ],
        'code' => [
            [
                'coding' => [
                    [
                        'system' => $system,
                        'code' => $code,
                        'display' => $display,
                    ],
                ],
            ],
        ],
        'extension' => [
            [
                'url' => RESOSOIN_PRACTITIONER_ROLE_NAME_EXTENSION,
                'valueHumanName' => [
                    'family' => 'Lévêque-Dupont',
                    'given' => ['Camille'],
                ],
            ],
        ],
    ];
}

function bundle_with_role(array $role): array
{
    return [
        'resourceType' => 'Bundle',
        'entry' => [
            ['resource' => practitioner()],
            ['resource' => $role],
        ],
    ];
}

$doctorMatch =
    resosoin_bundle_matches_health_professional(
        bundle_with_role(role('10', 'Médecin')),
        '10003461033',
        'leveque dupont'
    );
expect_true(
    is_array($doctorMatch)
    && $doctorMatch['code'] === '10',
    'Doctor must pass TRE_G15 verification.'
);

$midwifeMatch =
    resosoin_bundle_matches_health_professional(
        bundle_with_role(role('50', 'Sage-Femme')),
        '10003461033',
        'Lévêque-Dupont'
    );
expect_true(
    is_array($midwifeMatch)
    && $midwifeMatch['code'] === '50',
    'Midwife must pass TRE_G15 verification.'
);

$nurseMatch =
    resosoin_bundle_matches_health_professional(
        bundle_with_role(role('60', 'Infirmier')),
        '10003461033',
        'Lévêque-Dupont'
    );
expect_true(
    is_array($nurseMatch)
    && $nurseMatch['code'] === '60',
    'Nurse must pass TRE_G15 verification.'
);

$titleRole = role(
    '71',
    'Ostéopathe',
    'https://mos.esante.gouv.fr/NOS/TRE_R95-UsagerTitre/FHIR/TRE-R95-UsagerTitre'
);
expect_true(
    resosoin_bundle_matches_health_professional(
        bundle_with_role($titleRole),
        '10003461033',
        'Lévêque-Dupont'
    ) === null,
    'A title outside TRE_G15 must not pass.'
);

expect_true(
    resosoin_bundle_matches_health_professional(
        bundle_with_role(role('50', 'Sage-Femme')),
        '10003461033',
        'Autre Nom'
    ) === null,
    'Mismatched family name must fail.'
);

$inactiveRole = role('60', 'Infirmier');
$inactiveRole['active'] = false;
expect_true(
    resosoin_bundle_matches_health_professional(
        bundle_with_role($inactiveRole),
        '10003461033',
        'Lévêque-Dupont'
    ) === null,
    'Inactive role must fail.'
);

$wrongReference = role('60', 'Infirmier');
$wrongReference['practitioner']['reference'] =
    'Practitioner/other';
expect_true(
    resosoin_bundle_matches_health_professional(
        bundle_with_role($wrongReference),
        '10003461033',
        'Lévêque-Dupont'
    ) === null,
    'Role must belong to the RPPS practitioner.'
);

$appKey = random_bytes(32);
$token =
    resosoin_issue_professional_verification_token(
        '10003461033',
        '50',
        $appKey,
        1_700_000_000
    );

$payload =
    resosoin_validate_professional_verification_token(
        $token,
        $appKey,
        1_700_000_100
    );
expect_true(
    is_array($payload)
    && $payload['profession_code'] === '50',
    'Signed token must preserve allowed profession code.'
);

expect_true(
    !array_key_exists('rpps_digest', $payload),
    'Optional directory token must not retain an RPPS-derived digest.'
);

expect_true(
    resosoin_validate_professional_verification_token(
        $token,
        $appKey,
        1_700_000_700
    ) === null,
    'Expired verification token must fail.'
);

echo "RésoSoin RPPS verification tests OK.\n";
