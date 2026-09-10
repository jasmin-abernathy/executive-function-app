<?php

declare(strict_types=1);

require dirname(__DIR__) . '/adhd-app/includes/bootstrap.php';

function research_redirect(string $lang, string $status): never
{
    $base = $lang === 'en' ? '/en/' : '/';

    header(
        'Location: '
        . $base
        . '?research='
        . rawurlencode($status)
        . '#participer-recherche-tdah',
        true,
        303
    );
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Allow: POST');
    http_response_code(405);
    header('Content-Type: text/plain; charset=UTF-8');
    echo "Method not allowed.\n";
    exit;
}

$contentLength = (int) ($_SERVER['CONTENT_LENGTH'] ?? 0);
if ($contentLength > 8192) {
    http_response_code(413);
    exit;
}

$lang = ($_POST['lang'] ?? 'fr') === 'en' ? 'en' : 'fr';

/*
 * Protection navigateur contre les soumissions cross-site.
 * On ne bloque pas les clients qui n'envoient pas ces en-têtes,
 * mais lorsqu'ils sont présents ils doivent indiquer une requête locale.
 */
$fetchSite = strtolower((string) ($_SERVER['HTTP_SEC_FETCH_SITE'] ?? ''));
if ($fetchSite !== '' && !in_array($fetchSite, ['same-origin', 'same-site', 'none'], true)) {
    research_redirect($lang, 'error');
}

if (
    isset($_SERVER['HTTP_ORIGIN'])
    && function_exists('same_origin_request')
    && !same_origin_request()
) {
    research_redirect($lang, 'error');
}

/*
 * Honeypot : pour un bot, on simule une réussite mais
 * aucune donnée n'est stockée et aucun mail n'est envoyé.
 */
if (trim((string) ($_POST['company'] ?? '')) !== '') {
    research_redirect($lang, 'check-email');
}

if (
    ($_POST['adult'] ?? '') !== '1'
    || ($_POST['consent'] ?? '') !== '1'
) {
    research_redirect($lang, 'invalid');
}

$email = mb_strtolower(trim((string) ($_POST['email'] ?? '')));

if (
    strlen($email) > 254
    || !valid_email($email)
) {
    research_redirect($lang, 'invalid-email');
}

try {
    $pdo = db();
    $hash = email_hash($email);

    /*
     * On réutilise un éventuel contact existant :
     * pas de doublon inutile dans participation_contacts.
     */
    $stmt = $pdo->prepare(
        "SELECT id,
                status,
                CASE
                    WHEN updated_at >= DATE_SUB(NOW(), INTERVAL 5 MINUTE)
                    THEN 1 ELSE 0
                END AS recent
         FROM participation_contacts
         WHERE email_hash = ?
         ORDER BY updated_at DESC
         LIMIT 1"
    );
    $stmt->execute([$hash]);
    $existing = $stmt->fetch();

    /*
     * Déjà inscrit et confirmé : rien à recréer.
     */
    if ($existing && $existing['status'] === 'active') {
        research_redirect($lang, 'already');
    }

    /*
     * Anti-spam / double clic :
     * ne pas renvoyer plusieurs mails dans les 5 minutes.
     */
    if (
        $existing
        && $existing['status'] === 'pending'
        && (int) $existing['recent'] === 1
    ) {
        research_redirect($lang, 'check-email');
    }

    $contactId = $existing
        ? (string) $existing['id']
        : uuid_v4();

    $verifyToken = random_token();
    $unsubscribeToken = random_token();

    $interestsJson = json_encode(
        ['research'],
        JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES
    );

    $detailsJson = json_encode(
        ['source' => 'homepage_research_form'],
        JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES
    );

    if ($interestsJson === false || $detailsJson === false) {
        throw new RuntimeException('Unable to encode participation data.');
    }

    $pdo->beginTransaction();

    if ($existing) {
        $stmt = $pdo->prepare(
            "UPDATE participation_contacts
             SET session_id = NULL,
                 email_encrypted = ?,
                 email_hash = ?,
                 preferred_name_encrypted = NULL,
                 preferred_language = ?,
                 interests_json = ?,
                 details_json = ?,
                 link_to_response = 0,
                 status = 'pending',
                 verification_token_hash = ?,
                 unsubscribe_token_hash = ?,
                 unsubscribe_token_encrypted = ?,
                 consent_at = NOW(),
                 verified_at = NULL,
                 unsubscribed_at = NULL,
                 updated_at = NOW()
             WHERE id = ?"
        );

        $stmt->execute([
            encrypt_string($email),
            $hash,
            $lang,
            $interestsJson,
            $detailsJson,
            token_hash($verifyToken),
            token_hash($unsubscribeToken),
            encrypt_string($unsubscribeToken),
            $contactId,
        ]);
    } else {
        $stmt = $pdo->prepare(
            "INSERT INTO participation_contacts
            (
                id,
                session_id,
                email_encrypted,
                email_hash,
                preferred_name_encrypted,
                preferred_language,
                interests_json,
                details_json,
                link_to_response,
                status,
                verification_token_hash,
                unsubscribe_token_hash,
                unsubscribe_token_encrypted,
                consent_at
            )
            VALUES
            (
                ?,
                NULL,
                ?,
                ?,
                NULL,
                ?,
                ?,
                ?,
                0,
                'pending',
                ?,
                ?,
                ?,
                NOW()
            )"
        );

        $stmt->execute([
            $contactId,
            encrypt_string($email),
            $hash,
            $lang,
            $interestsJson,
            $detailsJson,
            token_hash($verifyToken),
            token_hash($unsubscribeToken),
            encrypt_string($unsubscribeToken),
        ]);
    }

    $confirmUrl = format_site_url(
        'confirm.php?token=' . rawurlencode($verifyToken)
    );

    if ($lang === 'fr') {
        $subject = 'Confirme ton inscription aux prochaines recherches TDAH';

        $body = '<p>Tu as demandé à être recontacté·e pour de futures études '
            . 'ou tests utilisateurs autour de l’application fonctions exécutives / TDAH.</p>'
            . button_html('Confirmer mon inscription', $confirmUrl)
            . '<p style="font-size:13px;color:#5f6f63">'
            . 'Si tu n’es pas à l’origine de cette demande, ignore simplement ce message.'
            . '</p>';

        $text = "Confirme ton inscription aux prochaines recherches TDAH :\n"
            . $confirmUrl
            . "\n\nSi tu n'es pas à l'origine de cette demande, ignore ce message.";
    } else {
        $subject = 'Confirm your signup for future ADHD research';

        $body = '<p>You asked to be contacted about future studies or user tests '
            . 'related to the executive functions / ADHD app.</p>'
            . button_html('Confirm my signup', $confirmUrl)
            . '<p style="font-size:13px;color:#5f6f63">'
            . 'If you did not request this, simply ignore this message.'
            . '</p>';

        $text = "Confirm your signup for future ADHD research:\n"
            . $confirmUrl
            . "\n\nIf you did not request this, ignore this message.";
    }

    $sent = send_app_email(
        $email,
        $subject,
        email_layout($subject, $body),
        $text
    );

    if ($sent === false) {
        throw new RuntimeException('Confirmation email could not be sent.');
    }

    $pdo->commit();

    /*
     * Notification interne demandée.
     * Son échec ne bloque pas l'inscription de la personne.
     */
    try {
        $safeEmail = htmlspecialchars(
            $email,
            ENT_QUOTES | ENT_SUBSTITUTE,
            'UTF-8'
        );

        $adminSubject = 'Nouvelle inscription au panel TDAH';

        $adminBody = '<p>Une nouvelle inscription a été enregistrée '
            . 'depuis le formulaire de app.lepotager.org.</p>'
            . '<p><strong>Adresse :</strong> '
            . $safeEmail
            . '</p>'
            . '<p><strong>Langue :</strong> '
            . ($lang === 'en' ? 'EN' : 'FR')
            . '</p>'
            . '<p>Statut : <strong>en attente de confirmation</strong>.</p>';

        send_app_email(
            'contact@lepotager.org',
            $adminSubject,
            email_layout($adminSubject, $adminBody),
            "Nouvelle inscription au panel TDAH\n"
            . "Adresse : {$email}\n"
            . "Langue : "
            . ($lang === 'en' ? 'EN' : 'FR')
            . "\nStatut : en attente de confirmation.\n"
        );
    } catch (Throwable $notificationError) {
        error_log(
            '[Research signup admin notification] '
            . $notificationError->getMessage()
        );
    }

    research_redirect($lang, 'check-email');

} catch (Throwable $error) {
    if (isset($pdo) && $pdo instanceof PDO && $pdo->inTransaction()) {
        $pdo->rollBack();
    }

    error_log('[Research signup] ' . $error->getMessage());

    research_redirect($lang, 'error');
}
