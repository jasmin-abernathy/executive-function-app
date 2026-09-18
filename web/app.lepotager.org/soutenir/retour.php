<?php
declare(strict_types=1);

define('CONTRIBUTION_SESSION_NAME','verger_contribution');
define('CONTRIBUTION_SITE_ID','VergerNumerique');
define('CONTRIBUTION_SITE_HOST','app.lepotager.org');
define('CONTRIBUTION_RETURN_URL','https://app.lepotager.org/soutenir/retour.php');
require __DIR__.'/stancer-contribution.php';

contribution_start_session();
contribution_headers();

$state=preg_replace('/[^a-f0-9]/','',strtolower((string)($_GET['state']??'')))??'';
$record=$state!==''?($_SESSION['contribution_records'][$state]??null):null;
$error=(string)($_SESSION['contribution_error']??'');
unset($_SESSION['contribution_error']);
$details=['kind'=>'pending','label'=>'Vérification en cours'];

if(is_array($record)&&!empty($record['payment_intent_id'])){
    try{
        $intent=contribution_request('GET','/payment_intents/'.rawurlencode((string)$record['payment_intent_id']));
        $payment=null;$paymentId=(string)($intent['payment']??'');
        if(preg_match('/^paym_[A-Za-z0-9]{24}$/',$paymentId))$payment=contribution_request('GET','/payments/'.rawurlencode($paymentId));
        $details=contribution_status($intent,$payment);
    }catch(Throwable $e){error_log('[Verger contribution return] '.$e->getMessage());}
}
if($error!=='')$details=['kind'=>'error','label'=>'Non démarrée'];

$title=$details['kind']==='success'?'Merci pour ta contribution':($details['kind']==='error'?'La contribution n’a pas démarré':'Contribution en cours de vérification');
$message=$details['kind']==='success'?'Stancer confirme que le paiement a abouti. Merci d’aider les projets du Verger à avancer.':($details['kind']==='error'?'Aucun paiement n’est indiqué comme confirmé pour cette tentative.':'Stancer ou ta banque peut encore finaliser l’opération. Évite de relancer immédiatement un second paiement.');
?>
<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="robots" content="noindex,nofollow">
  <title><?=contribution_escape($title)?> — Le Verger du Numérique</title>
  <link rel="icon" href="/favicon.svg" type="image/svg+xml">
  <link rel="stylesheet" href="/assets/style.css">
  <link rel="stylesheet" href="/accessibility.css">
</head>
<body>
<main id="contenu">
  <section class="legal-hero"><div class="shell legal-hero-inner"><p class="eyebrow">Retour Stancer</p><h1><?=contribution_escape($title)?>.</h1><p><?=contribution_escape($message)?></p></div></section>
  <section class="legal-content"><div class="shell legal-sections" style="max-width:900px"><article class="legal-card"><p class="legal-number">ÉTAT DU PAIEMENT</p><h2><?=contribution_escape((string)$details['label'])?></h2>
  <?php if(is_array($record)):?><p>Montant : <strong><?=contribution_escape(number_format(((int)$record['amount_cents'])/100,2,',',' '))?> €</strong></p><?php endif;?>
  <p style="margin-top:26px"><a class="button button-primary" href="/soutenir/">Retour à la page de soutien</a></p></article></div></section>
</main>
</body>
</html>
