<?php
declare(strict_types=1);

require dirname(__DIR__) . '/includes/bootstrap.php';

session_set_cookie_params([
    'httponly' => true,
    'secure' => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'samesite' => 'Strict',
]);
session_start();

if (empty($_SESSION['contacts_followup_csrf'])) {
    $_SESSION['contacts_followup_csrf'] = random_token(24);
}

function cf_h(mixed $value): string
{
    return htmlspecialchars((string) $value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function cf_panel_labels(): array
{
    return [
        'research' => 'User research panel',
        'codesign' => 'Co-design group',
        'beta' => 'Beta-testing group',
        'accessibility' => 'Accessibility testing',
        'opensource' => 'Open-source contributions',
        'translation' => 'Translation & localisation',
        'professional' => 'Professional advisory group',
        'crowdfunding' => 'Crowdfunding updates',
        'updates' => 'Occasional project updates',
        'future_youth' => 'Future age-appropriate research',
    ];
}

function cf_research_activity_labels(): array
{
    return [
        '0' => 'Short questionnaires',
        '1' => 'Individual interviews',
        '2' => 'Group discussions',
        '3' => 'Prototype testing',
        '4' => 'Diary studies',
        '5' => 'I am not sure yet',
    ];
}

function cf_decode_array(?string $json): array
{
    if ($json === null || $json === '') {
        return [];
    }
    $data = json_decode($json, true);
    return is_array($data) ? $data : [];
}

function cf_interest_recommendation(string $interest): array
{
    return match ($interest) {
        'research' => ['now', 'Useful now: invite to the V2 questionnaire, short surveys, prototype tests or interviews.'],
        'codesign' => ['now', 'Useful now: invite to concrete product-choice discussions, naming or interface tests.'],
        'translation' => ['now', 'Useful now: ask for FR/EN wording or localisation review.'],
        'updates' => ['now', 'Useful now: occasional low-frequency project updates only.'],
        'future_youth' => ['now', 'Useful now if an age-appropriate route is actually open for their country/age.'],
        'beta' => ['later', 'Wait until there is a functional build/APK to test.'],
        'accessibility' => ['later', 'Best when there is a screen/prototype or accessibility test task ready.'],
        'opensource' => ['later', 'Best when the repository/contribution path is ready to share.'],
        'professional' => ['later', 'Contact for a specific professional question, not a generic newsletter.'],
        'crowdfunding' => ['later', 'Contact only when crowdfunding/pre-orders genuinely open.'],
        default => ['later', 'Use only for the exact purpose originally selected.'],
    };
}

function cf_default_message(string $interest, string $lang, string $name): array
{
    $fr = $lang === 'fr';
    $hello = $name !== ''
        ? ($fr ? 'Bonjour ' . $name . ',' : 'Hi ' . $name . ',')
        : ($fr ? 'Bonjour,' : 'Hi,');

    $survey = 'https://app.lepotager.org/adhd-app/v2/';

    if ($fr) {
        return match ($interest) {
            'research' => [
                'Nouveau questionnaire de recherche produit — application TDAH',
                $hello . "\n\n"
                . "Vous aviez choisi de participer au panel de recherche utilisateur du projet.\n\n"
                . "Une nouvelle phase de recherche produit est maintenant disponible. Les réponses du premier questionnaire ont bien été conservées : cette nouvelle version sert surtout à trancher des choix concrets d’interface et de fonctionnalités.\n\n"
                . "La partie obligatoire comporte 11–12 questions (environ 5–8 minutes), puis plusieurs thèmes sont entièrement facultatifs. Il est possible de quitter et reprendre plus tard.\n\n"
                . "Questionnaire : {$survey}\n\n"
                . "Merci encore pour votre aide 🌱"
            ],
            'codesign' => [
                'Co-design — nouveaux choix concrets à tester pour l’application TDAH',
                $hello . "\n\n"
                . "Vous aviez choisi de rester impliqué·e dans le groupe de co-design.\n\n"
                . "Le projet est maintenant entré dans une phase plus concrète : écran Aujourd’hui, capture rapide, timer, reprise après interruption, routines, niveau de gamification et priorités du MVP.\n\n"
                . "Le nouveau questionnaire permet déjà d’arbitrer plusieurs de ces choix, avec une partie courte obligatoire puis des thèmes facultatifs :\n{$survey}\n\n"
                . "Je pourrai aussi vous recontacter ponctuellement pour des tests de maquettes ou de naming, uniquement dans le cadre du co-design que vous avez choisi.\n\n"
                . "Merci 🌱"
            ],
            'translation' => [
                'Relecture FR/EN — projet d’application TDAH',
                $hello . "\n\n"
                . "Vous aviez indiqué être intéressé·e par la traduction/localisation du projet.\n\n"
                . "Le questionnaire est désormais bilingue français/anglais et l’application est prévue bilingue dès sa première version. Si vous le souhaitez, vos retours sur des formulations peu naturelles, ambiguës ou difficiles à comprendre seront particulièrement utiles.\n\n"
                . "Version actuelle : {$survey}\n\n"
                . "Merci 🌱"
            ],
            'updates' => [
                'Quelques nouvelles du projet d’application TDAH',
                $hello . "\n\n"
                . "Vous aviez choisi de recevoir des nouvelles occasionnelles du projet.\n\n"
                . "Le questionnaire exploratoire initial est terminé et ses réponses sont conservées. Une deuxième phase, plus concrète, est maintenant en ligne pour départager les choix de conception du MVP.\n\n"
                . "Vous pouvez la consulter ici si vous en avez envie : {$survey}\n\n"
                . "Je garderai ces nouvelles peu fréquentes, comme prévu.\n\n"
                . "Merci 🌱"
            ],
            'future_youth' => [
                'Une nouvelle phase du questionnaire TDAH est disponible',
                $hello . "\n\n"
                . "Vous aviez demandé à être prévenu·e si une future phase de recherche adaptée à votre âge devenait disponible.\n\n"
                . "Une nouvelle version du questionnaire est désormais ouverte à certains participants mineurs selon le pays de résidence. Le questionnaire vérifie l’éligibilité avant de créer une réponse et explique les règles applicables.\n\n"
                . "Vous pouvez vérifier si cette phase vous concerne ici : {$survey}\n\n"
                . "Si votre pays ou votre âge n’est pas encore pris en charge, aucune réponse n’est créée.\n\n"
                . "Merci 🌱"
            ],
            'beta' => [
                'Bêta-test — projet d’application TDAH',
                $hello . "\n\n"
                . "Vous aviez choisi de participer aux bêta-tests. Il n’y a rien à tester immédiatement dans ce message : je conserve simplement votre préférence et je vous recontacterai lorsqu’une version fonctionnelle sera réellement disponible sur une plateforme que vous pouvez tester.\n\n"
                . "Merci d’avoir proposé votre aide 🌱"
            ],
            'accessibility' => [
                'Tests d’accessibilité — projet d’application TDAH',
                $hello . "\n\n"
                . "Vous aviez choisi de participer aux tests d’accessibilité. Je vous recontacterai lorsqu’un écran, une maquette ou une version fonctionnelle aura un test précis à effectuer (lisibilité, charge cognitive, navigation, contraste ou technologie d’assistance).\n\n"
                . "Merci d’avoir proposé votre aide 🌱"
            ],
            'opensource' => [
                'Contribution open source — projet d’application TDAH',
                $hello . "\n\n"
                . "Vous aviez indiqué être intéressé·e par une contribution open source au projet. Je conserve cette préférence et je vous recontacterai lorsqu’un dépôt ou une tâche de contribution suffisamment claire sera prêt à être partagé.\n\n"
                . "Merci 🌱"
            ],
            'professional' => [
                'Groupe consultatif professionnel — projet d’application TDAH',
                $hello . "\n\n"
                . "Vous aviez proposé de contribuer avec une expertise professionnelle ou de recherche. Je conserverai ce contact pour des questions ciblées correspondant à cette expertise, plutôt que pour des sollicitations générales.\n\n"
                . "Merci pour votre disponibilité 🌱"
            ],
            'crowdfunding' => [
                'Crowdfunding — projet d’application TDAH',
                $hello . "\n\n"
                . "Vous aviez demandé à être prévenu·e lors de l’ouverture d’un crowdfunding ou de précommandes. Cette campagne n’est pas encore ouverte ; je conserve simplement votre préférence et ne vous contacterai sur ce sujet que lorsqu’il y aura réellement quelque chose à annoncer.\n\n"
                . "Merci 🌱"
            ],
            default => [
                'Projet d’application TDAH',
                $hello . "\n\nMerci d’avoir choisi de rester impliqué·e dans ce projet. 🌱"
            ],
        };
    }

    return match ($interest) {
        'research' => [
            'New product-research questionnaire — ADHD app',
            $hello . "\n\n"
            . "You previously chose to join the user-research panel for the project.\n\n"
            . "A new product-research phase is now available. Your answers from the first questionnaire have been kept; this version focuses more concretely on interface and feature decisions.\n\n"
            . "The required part is 11–12 questions (about 5–8 minutes), followed by completely optional topics. You can leave and come back later.\n\n"
            . "Questionnaire: {$survey}\n\n"
            . "Thanks again for helping shape the project 🌱"
        ],
        'codesign' => [
            'Co-design — new concrete decisions for the ADHD app',
            $hello . "\n\n"
            . "You previously chose to stay involved in the co-design group.\n\n"
            . "The project is now testing concrete decisions: the Today screen, quick capture, timer behaviour, returning after interruptions, routines, gamification and MVP priorities.\n\n"
            . "The new questionnaire already explores several of those choices, with a short required section and optional topics:\n{$survey}\n\n"
            . "I may also contact you occasionally for mock-up or naming tests, only within the co-design category you selected.\n\n"
            . "Thank you 🌱"
        ],
        'translation' => [
            'English/French wording review — ADHD app project',
            $hello . "\n\n"
            . "You previously said you were interested in translation/localisation.\n\n"
            . "The questionnaire is now bilingual in English and French, and the app itself is planned to support both languages from its first version. Feedback on wording that feels unnatural, ambiguous or hard to understand would be especially useful.\n\n"
            . "Current questionnaire: {$survey}\n\n"
            . "Thank you 🌱"
        ],
        'updates' => [
            'A small update on the ADHD app project',
            $hello . "\n\n"
            . "You previously chose to receive occasional project updates.\n\n"
            . "The initial exploratory questionnaire is complete and its responses have been kept. A second, more concrete phase is now online to help choose the MVP design.\n\n"
            . "You can take a look here if you want: {$survey}\n\n"
            . "These updates will remain low-frequency, as requested.\n\n"
            . "Thank you 🌱"
        ],
        'future_youth' => [
            'A new age-appropriate ADHD app research round is available',
            $hello . "\n\n"
            . "You previously asked to be notified if a future age-appropriate research round became available.\n\n"
            . "A new questionnaire version is now open to some younger participants depending on country of residence. The questionnaire checks eligibility before creating a response and explains the applicable privacy route.\n\n"
            . "You can check whether this round applies to you here: {$survey}\n\n"
            . "If your country or age is not currently supported, no questionnaire response is created.\n\n"
            . "Thank you 🌱"
        ],
        'beta' => [
            'Beta testing — ADHD app project',
            $hello . "\n\n"
            . "You previously chose to join the beta-testing group. There is nothing you need to test in this message; I am keeping your preference and will contact you when a functional build is genuinely available for a platform you can test.\n\n"
            . "Thank you for volunteering 🌱"
        ],
        'accessibility' => [
            'Accessibility testing — ADHD app project',
            $hello . "\n\n"
            . "You previously chose to help with accessibility testing. I will contact you when there is a specific screen, mock-up or functional build to evaluate for readability, cognitive load, navigation, contrast or assistive technology.\n\n"
            . "Thank you for volunteering 🌱"
        ],
        'opensource' => [
            'Open-source contribution — ADHD app project',
            $hello . "\n\n"
            . "You previously said you were interested in contributing to the open-source project. I am keeping that preference and will contact you when a repository or contribution task is ready to share clearly.\n\n"
            . "Thank you 🌱"
        ],
        'professional' => [
            'Professional advisory group — ADHD app project',
            $hello . "\n\n"
            . "You previously offered relevant professional or research expertise. I will use this contact for specific questions that fit that expertise rather than for general project messages.\n\n"
            . "Thank you for your availability 🌱"
        ],
        'crowdfunding' => [
            'Crowdfunding — ADHD app project',
            $hello . "\n\n"
            . "You previously asked to be notified when crowdfunding or pre-orders open. There is no campaign open yet; I am simply keeping your preference and will only contact you about this when there is genuinely something to announce.\n\n"
            . "Thank you 🌱"
        ],
        default => [
            'ADHD app project',
            $hello . "\n\nThank you for choosing to stay involved in the project. 🌱"
        ],
    };
}

function cf_required_columns(): array
{
    return [
        'id',
        'session_id',
        'email_encrypted',
        'email_hash',
        'preferred_name_encrypted',
        'preferred_language',
        'interests_json',
        'details_json',
        'link_to_response',
        'status',
        'verification_token_hash',
        'unsubscribe_token_hash',
        'unsubscribe_token_encrypted',
        'consent_at',
        'verified_at',
        'unsubscribed_at',
        'created_at',
        'updated_at',
    ];
}

function cf_schema_check(PDO $pdo): array
{
    try {
        $rows = $pdo->query('SHOW COLUMNS FROM participation_contacts')->fetchAll();
    } catch (Throwable $e) {
        return ['ok' => false, 'missing' => cf_required_columns(), 'error' => $e->getMessage()];
    }

    $present = [];
    foreach ($rows as $row) {
        if (isset($row['Field'])) {
            $present[] = (string) $row['Field'];
        } elseif (isset($row[0])) {
            $present[] = (string) $row[0];
        }
    }

    $missing = array_values(array_diff(cf_required_columns(), $present));
    return ['ok' => $missing === [], 'missing' => $missing, 'error' => ''];
}

function cf_contact(PDO $pdo, string $id): ?array
{
    $stmt = $pdo->prepare(
        "SELECT id, email_encrypted, preferred_name_encrypted, preferred_language,
                interests_json, details_json, status, unsubscribe_token_encrypted,
                consent_at, verified_at, created_at, updated_at
         FROM participation_contacts
         WHERE id = ?
         LIMIT 1"
    );
    $stmt->execute([$id]);
    $row = $stmt->fetch();
    return $row ?: null;
}

function cf_unsubscribe_url(array $contact): string
{
    $token = decrypt_string($contact['unsubscribe_token_encrypted'] ?? null);
    if (!is_string($token) || $token === '') {
        return '';
    }
    return format_site_url('unsubscribe.php?token=' . rawurlencode($token));
}

function cf_message_html(string $plain, string $unsubscribeUrl): string
{
    $body = '<div style="font-size:16px;line-height:1.55">'
        . nl2br(cf_h($plain))
        . '</div>';

    if ($unsubscribeUrl !== '') {
        $body .= '<hr style="border:0;border-top:1px solid #dcded7;margin:26px 0">'
            . '<p style="font-size:13px;color:#5f6f63">'
            . 'You are receiving this message because you confirmed this project participation category. '
            . '<a href="' . cf_h($unsubscribeUrl) . '">Unsubscribe from all project participation messages</a>.'
            . '</p>';
    }

    return $body;
}

function cf_csrf_ok(): bool
{
    return hash_equals(
        (string) ($_SESSION['contacts_followup_csrf'] ?? ''),
        (string) ($_POST['csrf'] ?? '')
    );
}

$error = '';
$notice = '';
$preview = null;

if (isset($_GET['logout'])) {
    $_SESSION = [];
    session_destroy();
    header('Location: ./contacts-followup.php');
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['login'])) {
    $username = trim((string) ($_POST['username'] ?? ''));
    $password = (string) ($_POST['password'] ?? '');

    if (
        hash_equals((string) config('admin.username'), $username)
        && password_verify($password, (string) config('admin.password_hash'))
    ) {
        session_regenerate_id(true);
        $_SESSION['admin_authenticated'] = true;
        header('Location: ./contacts-followup.php');
        exit;
    }

    usleep(500000);
    $error = 'Invalid username or password.';
}

$authenticated = !empty($_SESSION['admin_authenticated']);
$schema = ['ok' => false, 'missing' => [], 'error' => ''];
$contacts = [];

if ($authenticated) {
    try {
        $pdo = db();
        $schema = cf_schema_check($pdo);

        if ($schema['ok']) {
            // Keep the same privacy invariant as the main V1 dashboard:
            // contact records must not remain directly linked to questionnaire responses.
            $unlink = $pdo->prepare(
                'UPDATE participation_contacts
                 SET session_id = NULL, link_to_response = 0
                 WHERE session_id IS NOT NULL OR link_to_response <> 0'
            );
            $unlink->execute();

            if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action'])) {
                if (!cf_csrf_ok()) {
                    throw new RuntimeException('The admin session expired. Reload the page.');
                }

                $action = (string) $_POST['action'];
                $contactId = clean_text($_POST['contact_id'] ?? '', 50);
                $contact = cf_contact($pdo, $contactId);

                if (!$contact) {
                    throw new RuntimeException('Contact not found.');
                }

                if ($action === 'resend_confirmation') {
                    if (($contact['status'] ?? '') !== 'pending') {
                        throw new RuntimeException('Only pending contacts can receive a new confirmation link.');
                    }

                    $email = decrypt_string($contact['email_encrypted'] ?? null);
                    if (!is_string($email) || !valid_email($email)) {
                        throw new RuntimeException('The pending email address could not be decrypted.');
                    }

                    $interests = cf_decode_array((string) ($contact['interests_json'] ?? '[]'));
                    $futureYouthOnly = count($interests) === 1 && in_array('future_youth', $interests, true);

                    $newToken = random_token();
                    $confirmUrl = format_site_url('confirm.php?token=' . rawurlencode($newToken));

                    if ($futureYouthOnly) {
                        $subject = 'Reminder — confirm future research notification';
                        $body = '<p>You recently asked to be notified if a future age-appropriate research round opens, but that preference has not been confirmed yet.</p>'
                            . button_html('Confirm this notification', $confirmUrl)
                            . '<p style="font-size:13px;color:#5f6f63">If you changed your mind or did not request this, simply ignore this message. No project messages will be sent unless you confirm.</p>';
                        $plain = "You recently asked to be notified if a future age-appropriate research round opens, but the preference has not been confirmed yet.\n\nConfirm:\n{$confirmUrl}\n\nIf you changed your mind or did not request this, simply ignore this email.";
                    } else {
                        $subject = 'Quick reminder — confirm your ADHD app research preferences';
                        $body = '<p>You recently asked to stay involved in the ADHD app project, but your email preferences have not been confirmed yet.</p>'
                            . button_html('Confirm my participation preferences', $confirmUrl)
                            . '<p style="font-size:13px;color:#5f6f63">If you changed your mind or did not request this, simply ignore this message. No further project messages will be sent unless you confirm.</p>';
                        $plain = "You recently asked to stay involved in the ADHD app project, but your email preferences have not been confirmed yet.\n\nConfirm:\n{$confirmUrl}\n\nIf you changed your mind or did not request this, simply ignore this email.";
                    }

                    $pdo->beginTransaction();
                    $stmt = $pdo->prepare(
                        'UPDATE participation_contacts
                         SET verification_token_hash = ?, updated_at = NOW()
                         WHERE id = ? AND status = \'pending\''
                    );
                    $stmt->execute([token_hash($newToken), $contact['id']]);

                    if ($stmt->rowCount() !== 1) {
                        $pdo->rollBack();
                        throw new RuntimeException('The pending contact changed before the reminder could be sent.');
                    }

                    try {
                        send_app_email(
                            $email,
                            $subject,
                            email_layout($subject, $body),
                            $plain
                        );
                        $pdo->commit();
                    } catch (Throwable $mailError) {
                        if ($pdo->inTransaction()) {
                            $pdo->rollBack();
                        }
                        throw $mailError;
                    }

                    $notice = 'A new confirmation link was generated and the reminder email was sent. The previous confirmation link is now invalid.';
                }

                if ($action === 'preview_message') {
                    if (($contact['status'] ?? '') !== 'active') {
                        throw new RuntimeException('Only confirmed contacts can receive project messages.');
                    }

                    $interest = clean_text($_POST['interest'] ?? '', 40);
                    $interests = array_map('strval', cf_decode_array((string) ($contact['interests_json'] ?? '[]')));

                    if (!in_array($interest, $interests, true)) {
                        throw new RuntimeException('That contact did not opt into the selected category.');
                    }

                    $name = decrypt_string($contact['preferred_name_encrypted'] ?? null);
                    $name = is_string($name) ? trim($name) : '';
                    $lang = (string) ($contact['preferred_language'] ?? 'en');
                    [$subject, $body] = cf_default_message($interest, $lang, $name);

                    $preview = [
                        'contact_id' => $contact['id'],
                        'interest' => $interest,
                        'email' => (string) decrypt_string($contact['email_encrypted'] ?? null),
                        'subject' => $subject,
                        'body' => $body,
                        'unsubscribe_url' => cf_unsubscribe_url($contact),
                    ];
                }

                if ($action === 'send_message') {
                    if (($contact['status'] ?? '') !== 'active') {
                        throw new RuntimeException('Only confirmed contacts can receive project messages.');
                    }

                    $interest = clean_text($_POST['interest'] ?? '', 40);
                    $interests = array_map('strval', cf_decode_array((string) ($contact['interests_json'] ?? '[]')));
                    if (!in_array($interest, $interests, true)) {
                        throw new RuntimeException('That contact did not opt into the selected category.');
                    }

                    if (($_POST['reviewed'] ?? '') !== 'yes') {
                        throw new RuntimeException('Confirm that you reviewed the message before sending.');
                    }

                    $email = decrypt_string($contact['email_encrypted'] ?? null);
                    if (!is_string($email) || !valid_email($email)) {
                        throw new RuntimeException('The contact email could not be decrypted.');
                    }

                    $unsubscribeUrl = cf_unsubscribe_url($contact);
                    if ($unsubscribeUrl === '' || !is_file(dirname(__DIR__) . '/unsubscribe.php')) {
                        throw new RuntimeException('No usable unsubscribe link is available. Message not sent.');
                    }

                    $subject = clean_text($_POST['subject'] ?? '', 180);
                    $plainBody = trim((string) ($_POST['body'] ?? ''));
                    if ($subject === '' || $plainBody === '' || mb_strlen($plainBody) > 8000) {
                        throw new RuntimeException('Subject and message are required; the message must stay under 8,000 characters.');
                    }

                    $plain = $plainBody
                        . "\n\n---\n"
                        . "Unsubscribe from all project participation messages:\n"
                        . $unsubscribeUrl;

                    $html = cf_message_html($plainBody, $unsubscribeUrl);

                    send_app_email(
                        $email,
                        $subject,
                        email_layout($subject, $html),
                        $plain
                    );

                    $notice = 'Project message sent to one confirmed contact for the selected opt-in category.';
                }
            }

            $rows = $pdo->query(
                "SELECT id, email_encrypted, preferred_name_encrypted, preferred_language,
                        interests_json, details_json, status, unsubscribe_token_encrypted,
                        consent_at, verified_at, created_at, updated_at
                 FROM participation_contacts
                 ORDER BY
                    CASE status WHEN 'pending' THEN 0 WHEN 'active' THEN 1 ELSE 2 END,
                    created_at ASC"
            )->fetchAll();

            foreach ($rows as $row) {
                $email = decrypt_string($row['email_encrypted'] ?? null);
                $name = decrypt_string($row['preferred_name_encrypted'] ?? null);
                $interests = array_map('strval', cf_decode_array((string) ($row['interests_json'] ?? '[]')));
                $details = cf_decode_array((string) ($row['details_json'] ?? '{}'));

                $researchActivities = [];
                foreach (($details['research_activities'] ?? []) as $value) {
                    $key = (string) $value;
                    $researchActivities[] = cf_research_activity_labels()[$key] ?? $key;
                }

                $contacts[] = [
                    'id' => (string) $row['id'],
                    'email' => is_string($email) && $email !== '' ? $email : '[unable to decrypt email]',
                    'name' => is_string($name) ? $name : '',
                    'language' => (string) ($row['preferred_language'] ?? 'en'),
                    'interests' => $interests,
                    'research_activities' => $researchActivities,
                    'platforms' => array_values(array_map('strval', is_array($details['platforms'] ?? null) ? $details['platforms'] : [])),
                    'contributions' => array_values(array_map('strval', is_array($details['contributions'] ?? null) ? $details['contributions'] : [])),
                    'note' => trim((string) ($details['note'] ?? '')),
                    'status' => (string) ($row['status'] ?? ''),
                    'consent_at' => (string) ($row['consent_at'] ?? ''),
                    'verified_at' => (string) ($row['verified_at'] ?? ''),
                    'created_at' => (string) ($row['created_at'] ?? ''),
                    'updated_at' => (string) ($row['updated_at'] ?? ''),
                    'unsubscribe_ok' => cf_unsubscribe_url($row) !== '',
                ];
            }
        }
    } catch (Throwable $e) {
        if (isset($pdo) && $pdo instanceof PDO && $pdo->inTransaction()) {
            $pdo->rollBack();
        }
        error_log('[ADHD V1 contacts follow-up] ' . $e->getMessage());
        $error = $e->getMessage();
    }
}

$labels = cf_panel_labels();
?><!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="robots" content="noindex,nofollow">
<title>Contact follow-up — Le Potager Lab</title>
<style>
:root{--bg:#f4f2eb;--card:#fff;--text:#243129;--muted:#657068;--line:#d9ddd7;--green:#315f45;--soft:#e7f0e9;--warn:#fff3d8;--red:#7a3131}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif;padding:18px}
main{max-width:1100px;margin:0 auto;background:var(--card);border:1px solid var(--line);border-radius:22px;padding:clamp(20px,4vw,36px)}
h1,h2,h3{line-height:1.2}.eyebrow{text-transform:uppercase;letter-spacing:.08em;color:var(--green);font-size:.75rem;font-weight:800}
.lead{font-size:1.05rem;line-height:1.55}.small{color:var(--muted);font-size:.86rem;line-height:1.5}
.notice{padding:14px 16px;border:1px solid var(--line);border-radius:14px;margin:14px 0;background:#fafaf7}.ok{background:var(--soft)}.warn{background:var(--warn)}.err{background:#f8e4e4;color:var(--red)}
.actions{display:flex;gap:10px;flex-wrap:wrap;margin:16px 0}button,.btn{border:1px solid var(--line);border-radius:12px;padding:10px 14px;background:white;color:var(--text);font:inherit;font-weight:750;cursor:pointer;text-decoration:none}.primary{background:var(--green);color:#fff;border-color:var(--green)}.danger{color:var(--red)}
input,select,textarea{width:100%;border:1px solid var(--line);border-radius:11px;padding:10px;font:inherit;background:white}textarea{min-height:230px;resize:vertical}
label{display:block;font-weight:700;margin:10px 0 6px}
.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px}.contact{border:1px solid var(--line);border-radius:17px;padding:16px;background:#fff}.pending{border-left:5px solid #d79d33}.active{border-left:5px solid var(--green)}.unsubscribed{opacity:.65}
.chips{display:flex;gap:7px;flex-wrap:wrap;margin:8px 0}.chip{font-size:.78rem;padding:5px 8px;border-radius:999px;background:#edf1ed}.chip-now{background:#dcecdf}.chip-later{background:#f2ead6}
dl{display:grid;grid-template-columns:170px 1fr;gap:6px 12px}dt{font-weight:800}dd{margin:0}.divider{border:0;border-top:1px solid var(--line);margin:22px 0}
.preview{border:2px solid var(--green);border-radius:18px;padding:18px;margin:20px 0;background:#fbfdfb}
.checkline{display:flex;gap:9px;align-items:flex-start;font-weight:500}.checkline input{width:auto;margin-top:4px}
code{background:#f0f1ed;padding:2px 5px;border-radius:5px}
@media(max-width:760px){.grid{grid-template-columns:1fr}dl{grid-template-columns:1fr;gap:2px}.actions>*{flex:1 1 100%}}
</style>
</head>
<body>
<main>
<p class="eyebrow">Le Potager Lab · V1</p>
<h1>Contact follow-up</h1>
<p class="lead">Standalone contact-management page. It does not modify questionnaire answers, questionnaire JavaScript, the public form or the existing admin dashboard.</p>

<?php if (!$authenticated): ?>
    <?php if ($error !== ''): ?><div class="notice err"><?= cf_h($error) ?></div><?php endif; ?>
    <form method="post" style="max-width:460px">
        <input type="hidden" name="login" value="1">
        <label for="username">Admin username</label>
        <input id="username" name="username" autocomplete="username" required>
        <label for="password">Admin password</label>
        <input id="password" name="password" type="password" autocomplete="current-password" required>
        <div class="actions"><button class="primary" type="submit">Sign in</button></div>
    </form>
<?php else: ?>

<div class="actions">
    <a class="btn" href="./">Existing V1 dashboard</a>
    <a class="btn" href="?logout=1">Sign out</a>
</div>

<?php if ($error !== ''): ?><div class="notice err"><strong>Error:</strong> <?= cf_h($error) ?></div><?php endif; ?>
<?php if ($notice !== ''): ?><div class="notice ok"><?= cf_h($notice) ?></div><?php endif; ?>

<h2>Compatibility check</h2>
<?php if ($schema['ok']): ?>
    <div class="notice ok"><strong>Compatible.</strong> The current <code>participation_contacts</code> table exposes every field this module expects. No SQL migration is needed.</div>
<?php else: ?>
    <div class="notice err">
        <strong>Do not use the actions below.</strong>
        The current database structure does not match the expected V1 contact table.
        <?php if ($schema['missing']): ?><br>Missing columns: <?= cf_h(implode(', ', $schema['missing'])) ?><?php endif; ?>
        <?php if ($schema['error'] !== ''): ?><br><?= cf_h($schema['error']) ?><?php endif; ?>
    </div>
<?php endif; ?>

<?php if ($preview !== null): ?>
<section class="preview">
    <p class="eyebrow">Preview — one contact only</p>
    <h2>Review before sending</h2>
    <p><strong>To:</strong> <?= cf_h($preview['email']) ?></p>
    <p><strong>Opt-in category:</strong> <?= cf_h($labels[$preview['interest']] ?? $preview['interest']) ?></p>

    <form method="post">
        <input type="hidden" name="csrf" value="<?= cf_h((string) $_SESSION['contacts_followup_csrf']) ?>">
        <input type="hidden" name="action" value="send_message">
        <input type="hidden" name="contact_id" value="<?= cf_h($preview['contact_id']) ?>">
        <input type="hidden" name="interest" value="<?= cf_h($preview['interest']) ?>">

        <label for="subject">Subject</label>
        <input id="subject" name="subject" maxlength="180" value="<?= cf_h($preview['subject']) ?>" required>

        <label for="body">Plain-text message</label>
        <textarea id="body" name="body" maxlength="8000" required><?= cf_h($preview['body']) ?></textarea>

        <?php if ($preview['unsubscribe_url'] !== ''): ?>
            <div class="notice ok small">The personal unsubscribe URL will automatically be appended to both the text and HTML email.</div>
        <?php else: ?>
            <div class="notice err small">The personal unsubscribe token could not be decrypted. Sending will be blocked.</div>
        <?php endif; ?>

        <label class="checkline">
            <input type="checkbox" name="reviewed" value="yes" required>
            <span>I reviewed this message and confirm that it concerns only the category this person selected.</span>
        </label>

        <div class="actions">
            <button class="primary" type="submit">Send this one email</button>
            <a class="btn" href="./contacts-followup.php">Cancel</a>
        </div>
    </form>
</section>
<?php endif; ?>

<?php if ($schema['ok']): ?>
<h2>Contacts</h2>
<div class="notice">
    <strong>Privacy rule kept intact.</strong>
    This page removes any legacy direct link between a contact record and a questionnaire response before displaying contacts.
</div>

<?php if (!$contacts): ?>
    <p>No contact records found.</p>
<?php else: ?>
<div class="grid">
<?php foreach ($contacts as $c): ?>
<article class="contact <?= cf_h($c['status']) ?>">
    <h3><?= cf_h($c['email']) ?></h3>
    <?php if ($c['name'] !== ''): ?><p><strong>Preferred name:</strong> <?= cf_h($c['name']) ?></p><?php endif; ?>
    <p><strong>Status:</strong> <?= cf_h($c['status']) ?> · <strong>Language:</strong> <?= cf_h($c['language']) ?></p>

    <div class="chips">
    <?php foreach ($c['interests'] as $interest): ?>
        <?php [$when, $why] = cf_interest_recommendation($interest); ?>
        <span class="chip <?= $when === 'now' ? 'chip-now' : 'chip-later' ?>"><?= cf_h($labels[$interest] ?? $interest) ?></span>
    <?php endforeach; ?>
    </div>

    <?php if ($c['research_activities']): ?>
        <p class="small"><strong>Research activities:</strong> <?= cf_h(implode(' · ', $c['research_activities'])) ?></p>
    <?php endif; ?>
    <?php if ($c['platforms']): ?>
        <p class="small"><strong>Platforms:</strong> <?= cf_h(implode(' · ', $c['platforms'])) ?></p>
    <?php endif; ?>
    <?php if ($c['contributions']): ?>
        <p class="small"><strong>Possible contributions:</strong> <?= cf_h(implode(' · ', $c['contributions'])) ?></p>
    <?php endif; ?>
    <?php if ($c['note'] !== ''): ?>
        <div class="notice small"><strong>Participant note:</strong><br><?= nl2br(cf_h($c['note'])) ?></div>
    <?php endif; ?>

    <details>
        <summary class="small">Dates & contact-only technical details</summary>
        <dl class="small">
            <dt>Created</dt><dd><?= cf_h($c['created_at']) ?></dd>
            <dt>Confirmed</dt><dd><?= cf_h($c['verified_at'] ?: '—') ?></dd>
            <dt>Last contact record update</dt><dd><?= cf_h($c['updated_at']) ?></dd>
            <dt>Unsubscribe token</dt><dd><?= $c['unsubscribe_ok'] ? 'Available' : 'Unavailable' ?></dd>
        </dl>
    </details>

    <?php if ($c['status'] === 'pending'): ?>
        <div class="notice warn small">
            <strong>Pending only.</strong> Do not send project/news content yet.
            This action sends one transactional reminder and rotates the confirmation token.
        </div>
        <form method="post">
            <input type="hidden" name="csrf" value="<?= cf_h((string) $_SESSION['contacts_followup_csrf']) ?>">
            <input type="hidden" name="action" value="resend_confirmation">
            <input type="hidden" name="contact_id" value="<?= cf_h($c['id']) ?>">
            <button type="submit">Resend confirmation reminder</button>
        </form>
    <?php elseif ($c['status'] === 'active'): ?>
        <hr class="divider">
        <p class="small"><strong>What can you contact them about?</strong></p>
        <?php foreach ($c['interests'] as $interest): ?>
            <?php [$when, $why] = cf_interest_recommendation($interest); ?>
            <p class="small"><strong><?= cf_h($labels[$interest] ?? $interest) ?>:</strong> <?= cf_h($why) ?></p>
        <?php endforeach; ?>

        <form method="post">
            <input type="hidden" name="csrf" value="<?= cf_h((string) $_SESSION['contacts_followup_csrf']) ?>">
            <input type="hidden" name="action" value="preview_message">
            <input type="hidden" name="contact_id" value="<?= cf_h($c['id']) ?>">
            <label>Prepare a message for exactly one selected opt-in category</label>
            <select name="interest" required>
                <?php foreach ($c['interests'] as $interest): ?>
                    <option value="<?= cf_h($interest) ?>"><?= cf_h($labels[$interest] ?? $interest) ?></option>
                <?php endforeach; ?>
            </select>
            <div class="actions"><button type="submit">Prepare email preview</button></div>
        </form>
    <?php else: ?>
        <div class="notice small">Unsubscribed contacts cannot be mailed from this page.</div>
    <?php endif; ?>
</article>
<?php endforeach; ?>
</div>
<?php endif; ?>
<?php endif; ?>

<?php endif; ?>
</main>
</body>
</html>
