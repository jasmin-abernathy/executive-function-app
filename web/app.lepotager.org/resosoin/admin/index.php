<?php

declare(strict_types=1);

require dirname(__DIR__, 2)
    . '/adhd-app/includes/bootstrap.php';
require dirname(__DIR__)
    . '/questionnaire/_professional_verification.php';

header('Cache-Control: no-store');
header('X-Robots-Tag: noindex, nofollow');

session_set_cookie_params([
    'httponly' => true,
    'secure' => !empty($_SERVER['HTTPS'])
        && $_SERVER['HTTPS'] !== 'off',
    'samesite' => 'Strict',
]);
session_start();

if (empty($_SESSION['admin_csrf'])) {
    $_SESSION['admin_csrf'] = random_token(24);
}

function resosoin_admin_h(mixed $value): string
{
    return htmlspecialchars(
        (string) $value,
        ENT_QUOTES | ENT_SUBSTITUTE,
        'UTF-8'
    );
}

$error = '';
$notice = '';

if (
    $_SERVER['REQUEST_METHOD'] === 'POST'
    && isset($_POST['login'])
) {
    $username = trim(
        (string) ($_POST['username'] ?? '')
    );
    $password = (string) (
        $_POST['password'] ?? ''
    );

    if (
        hash_equals(
            (string) config('admin.username'),
            $username
        )
        && password_verify(
            $password,
            (string) config('admin.password_hash')
        )
    ) {
        session_regenerate_id(true);
        $_SESSION['admin_authenticated'] = true;
        header('Location: ./');
        exit;
    }

    usleep(500000);
    $error = 'Identifiant ou mot de passe incorrect.';
}

$authenticated =
    !empty($_SESSION['admin_authenticated']);

if (
    $authenticated
    && $_SERVER['REQUEST_METHOD'] === 'POST'
    && isset($_POST['logout'])
) {
    if (
        !hash_equals(
            (string) $_SESSION['admin_csrf'],
            (string) ($_POST['csrf'] ?? '')
        )
    ) {
        $error = 'La session a expiré.';
    } else {
        $_SESSION = [];
        session_destroy();
        header('Location: ./');
        exit;
    }
}

if (
    $authenticated
    && $_SERVER['REQUEST_METHOD'] === 'POST'
    && !isset($_POST['logout'])
) {
    if (
        !hash_equals(
            (string) $_SESSION['admin_csrf'],
            (string) ($_POST['csrf'] ?? '')
        )
    ) {
        $error = 'La session a expiré.';
    } elseif (isset($_POST['save_api_key'])) {
        $apiKey = trim(
            (string) ($_POST['api_key'] ?? '')
        );

        $test =
            resosoin_test_annuaire_sante_api_key(
                $apiKey
            );

        if (!$test['ok']) {
            $error = (string) $test['message'];
        } else {
            try {
                resosoin_store_annuaire_sante_api_key(
                    $apiKey
                );
                $notice =
                    'Clé API testée auprès de l’Annuaire Santé puis enregistrée hors webroot.';
            } catch (Throwable $exception) {
                error_log(
                    '[RésoSoin admin] '
                    . $exception->getMessage()
                );
                $error =
                    'La clé a été validée mais son stockage privé a échoué.';
            }
        }
    } elseif (isset($_POST['test_current'])) {
        $apiKey =
            resosoin_annuaire_sante_api_key();

        if ($apiKey === '') {
            $error = 'Aucune clé API n’est configurée.';
        } else {
            $test =
                resosoin_test_annuaire_sante_api_key(
                    $apiKey
                );

            if ($test['ok']) {
                $notice = (string) $test['message'];
            } else {
                $error = (string) $test['message'];
            }
        }
    } elseif (isset($_POST['delete_api_key'])) {
        try {
            resosoin_delete_annuaire_sante_api_key();
            $notice = 'Clé API supprimée du stockage privé.';
        } catch (Throwable $exception) {
            error_log(
                '[RésoSoin admin] '
                . $exception->getMessage()
            );
            $error =
                'Impossible de supprimer la clé API.';
        }
    }
}

$record = $authenticated
    ? resosoin_annuaire_sante_secret_record()
    : [];
$configured = $authenticated
    && resosoin_annuaire_sante_api_key() !== '';
$updatedAt = (string) (
    $record['updated_at'] ?? ''
);
?><!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="robots" content="noindex,nofollow">
  <meta name="theme-color" content="#315c3a">
  <title>RésoSoin · configuration privée</title>
  <link rel="icon" href="/favicon.svg" type="image/svg+xml">
  <link rel="stylesheet" href="/resosoin/questionnaire/assets/style.css">
  <style>
    .admin-wrap{max-width:720px;margin:5vh auto}
    .admin-grid{display:grid;gap:14px}
    .secret-field{display:grid;gap:6px}
    .secret-field input{width:100%;min-height:48px;border:1px solid var(--line);border-radius:14px;padding:10px 12px;background:#fff;color:var(--text)}
    .status-card{border:1px solid var(--line);border-radius:18px;padding:16px;background:#fff}
    .status-dot{display:inline-block;width:10px;height:10px;border-radius:999px;background:#8b3434;margin-right:7px}
    .status-dot.ok{background:var(--green)}
  </style>
</head>
<body>
<div class="shell admin-wrap">
  <main class="card">
    <p class="eyebrow">RésoSoin · administration privée</p>
    <h1>Clé API Annuaire Santé</h1>
    <p class="lead">Cette clé active uniquement la recherche facultative d’une fiche RPPS publique. RésoSoin reste utilisable sans clé : le questionnaire professionnel repose d’abord sur une déclaration, et une fiche concordante n’authentifie pas l’identité du répondant.</p>

    <?php if (!$authenticated): ?>
      <p class="lead">Utilisez le même compte administrateur que pour le tableau de bord de recherche.</p>

      <?php if ($error !== ''): ?>
        <div class="notice error"><?= resosoin_admin_h($error) ?></div>
      <?php endif; ?>

      <form method="post" class="admin-grid">
        <input type="hidden" name="login" value="1">

        <label class="secret-field">
          <span>Identifiant</span>
          <input name="username" autocomplete="username" required>
        </label>

        <label class="secret-field">
          <span>Mot de passe</span>
          <input name="password" type="password" autocomplete="current-password" required>
        </label>

        <button class="btn primary" type="submit">Se connecter</button>
      </form>
    <?php else: ?>

      <?php if ($error !== ''): ?>
        <div class="notice error"><?= resosoin_admin_h($error) ?></div>
      <?php endif; ?>

      <?php if ($notice !== ''): ?>
        <div class="notice"><?= resosoin_admin_h($notice) ?></div>
      <?php endif; ?>

      <div class="status-card">
        <strong>
          <span class="status-dot <?= $configured ? 'ok' : '' ?>" aria-hidden="true"></span>
          <?= $configured ? 'Clé configurée' : 'Aucune clé configurée' ?>
        </strong>
        <p class="small">
          La valeur de la clé n’est jamais réaffichée.
          <?php if ($updatedAt !== ''): ?>
            Dernière mise à jour : <?= resosoin_admin_h($updatedAt) ?>.
          <?php endif; ?>
        </p>
      </div>

      <div class="notice">
        <strong>Stockage privé</strong>
        <span>La clé est enregistrée dans le répertoire privé du compte d’hébergement, hors de <code>public_html</code> et hors Git, avec permissions restrictives. Elle sert uniquement aux requêtes serveur vers l’Annuaire Santé.</span>
      </div>

      <form method="post" class="admin-grid">
        <input type="hidden" name="csrf" value="<?= resosoin_admin_h((string) $_SESSION['admin_csrf']) ?>">

        <label class="secret-field">
          <span>Nouvelle clé API Annuaire Santé</span>
          <input
            name="api_key"
            type="password"
            autocomplete="new-password"
            spellcheck="false"
            placeholder="Coller la clé ESANTE-API-KEY"
            required
          >
        </label>

        <button class="btn primary" type="submit" name="save_api_key" value="1">
          Tester auprès de l’ANS et enregistrer
        </button>
      </form>

      <?php if ($configured): ?>
        <div class="actions">
          <form method="post">
            <input type="hidden" name="csrf" value="<?= resosoin_admin_h((string) $_SESSION['admin_csrf']) ?>">
            <button class="btn secondary" type="submit" name="test_current" value="1">
              Tester la clé actuelle
            </button>
          </form>

          <form method="post" onsubmit="return confirm('Supprimer la clé API Annuaire Santé ?');">
            <input type="hidden" name="csrf" value="<?= resosoin_admin_h((string) $_SESSION['admin_csrf']) ?>">
            <button class="btn quiet" type="submit" name="delete_api_key" value="1">
              Supprimer la clé
            </button>
          </form>
        </div>
      <?php endif; ?>

      <form method="post" style="margin-top:24px">
        <input type="hidden" name="csrf" value="<?= resosoin_admin_h((string) $_SESSION['admin_csrf']) ?>">
        <button class="btn quiet" type="submit" name="logout" value="1">
          Se déconnecter
        </button>
      </form>
    <?php endif; ?>
  </main>
</div>
</body>
</html>
