<?php

declare(strict_types=1);

return array (
  'app_env' => 'production',
  'site_url' => 'https://app.lepotager.org/adhd-app',
  'site_name' => 'Le Potager Lab — ADHD App Research',
  'app_key' => 'PYGGiK7PyxuiOg0O3MTJWWqUw42sCaUumNjBPdfCP6A=',
  'cron_key' => 'PqP3Az07oeHxN1gQAwBGjHxsc4EH8aC0VqjeHdhaSAU',
  'db' => 
  array (
    'host' => 'localhost',
    'port' => 3306,
    'name' => 'sc1leja3715_adhd_app',
    'user' => 'sc1leja3715_jasmin_adhd',
    'charset' => 'utf8mb4',
    'password' => '[PvMh}J4)fVrUx`7m(c3W>~@D(vNnC',
  ),
  'mail' => 
  array (
    'mode' => 'mail',
    'from_email' => 'contact@lepotager.org',
    'from_name' => 'Le Potager Lab',
    'reply_to' => 'contact@lepotager.org',
    'smtp' => 
    array (
      'host' => 'mail.lepotager.org',
      'port' => 587,
      'encryption' => 'tls',
      'username' => '',
      'password' => '',
    ),
  ),
  'operator' => 
  array (
    'name' => 'Le Potager du Web',
    'legal_name' => 'Jasmin Lévêque — Entrepreneur individuel',
    'postal_address' => '3 rue Brunehaut
57000 Metz',
    'contact_email' => 'contact@lepotager.org',
  ),
  'admin' => 
  array (
    'username' => 'Potager-Lab',
    'password_hash' => '$2y$12$PMBwo/5fjLyqosqcaZK8Y.NlQdDBZuJ9H9MbPQZjg1PtooCIQabIS',
  ),
  'retention' => 
  array (
    'unfinished_days' => 90,
    'completed_months' => 24,
    'reminder_days_after_send' => 30,
  ),
);
