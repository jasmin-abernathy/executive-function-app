<?php

declare(strict_types=1);

/*
 * Exemple de configuration uniquement.
 * Ne jamais mettre de secret réel dans ce fichier.
 *
 * Configuration de production attendue hors webroot :
 * ~/private/executive-function-app/config.php
 *
 * Générer une app_key :
 * php -r 'echo base64_encode(random_bytes(32)), PHP_EOL;'
 *
 * Générer une cron_key :
 * php -r 'echo bin2hex(random_bytes(32)), PHP_EOL;'
 */

return [
    'app_env' => 'production',

    'site_url' => 'https://app.lepotager.org/adhd-app',
    'site_name' => 'Le Potager Lab — ADHD App Research',

    'app_key' => 'A_REMPLACER_HORS_GIT',
    'cron_key' => 'A_REMPLACER_HORS_GIT',

    'db' => [
        'host' => 'localhost',
        'port' => 3306,
        'name' => 'A_REMPLACER_HORS_GIT',
        'user' => 'A_REMPLACER_HORS_GIT',
        'charset' => 'utf8mb4',
        'password' => 'A_REMPLACER_HORS_GIT',
    ],

    'mail' => [
        'mode' => 'mail',
        'from_email' => 'contact@example.org',
        'from_name' => 'Application Research',
        'reply_to' => 'contact@example.org',

        'smtp' => [
            'host' => 'smtp.example.org',
            'port' => 587,
            'encryption' => 'tls',
            'username' => 'A_REMPLACER_HORS_GIT',
            'password' => 'A_REMPLACER_HORS_GIT',
        ],
    ],

    'operator' => [
        'name' => 'Nom public',
        'legal_name' => 'Nom légal',
        'postal_address' => "Adresse\nCode postal Ville",
        'contact_email' => 'contact@example.org',
    ],

    'admin' => [
        'username' => 'A_REMPLACER_HORS_GIT',
        'password_hash' => 'A_REMPLACER_HORS_GIT',
    ],

    'retention' => [
        'unfinished_days' => 90,
        'completed_months' => 24,
        'reminder_days_after_send' => 30,
    ],
];
