<?php
declare(strict_types=1);

define('CONTRIBUTION_SESSION_NAME','verger_contribution');
define('CONTRIBUTION_SITE_ID','VergerNumerique');
define('CONTRIBUTION_SITE_HOST','app.lepotager.org');
define('CONTRIBUTION_RETURN_URL','https://app.lepotager.org/soutenir/retour.php');
require __DIR__.'/stancer-contribution.php';

contribution_start_session();
contribution_headers();

if($_SERVER['REQUEST_METHOD']!=='POST'){http_response_code(405);header('Allow: POST');exit('Méthode non autorisée.');}

try{
    if(!contribution_same_origin())throw new InvalidArgumentException('Origine du formulaire invalide.');
    if(trim((string)($_POST['website']??''))!==''){header('Location: /soutenir/',true,303);exit;}
    $last=(int)($_SESSION['contribution_last_attempt']??0);
    if($last>0&&time()-$last<8)throw new InvalidArgumentException('Patiente quelques secondes avant de réessayer.');
    $_SESSION['contribution_last_attempt']=time();
    $raw=isset($_POST['amount'])&&$_POST['amount']!==''?$_POST['amount']:($_POST['custom_amount']??'');
    $cents=contribution_amount_to_cents($raw);
    $state=bin2hex(random_bytes(24));
    $returnUrl=CONTRIBUTION_RETURN_URL.'?state='.rawurlencode($state);
    session_write_close();

    $intent=contribution_request('POST','/payment_intents/',[
        'amount'=>$cents,
        'currency'=>'eur',
        'description'=>'Contribution libre Verger du Numérique — Le Potager du Web',
        'order_id'=>'VERGER-'.gmdate('YmdHis').'-'.strtoupper(bin2hex(random_bytes(3))),
        'methods_allowed'=>['card'],
        'threeds'=>'required',
        'capture'=>true,
        'return_url'=>$returnUrl,
    ]);
    $id=(string)($intent['id']??'');$url=(string)($intent['url']??'');
    if(!preg_match('/^pi_[A-Za-z0-9]{24}$/',$id)||!filter_var($url,FILTER_VALIDATE_URL)||parse_url($url,PHP_URL_SCHEME)!=='https'||parse_url($url,PHP_URL_HOST)!=='payment.stancer.com'){
        throw new RuntimeException('Réponse de paiement invalide.');
    }

    contribution_start_session();
    foreach(($_SESSION['contribution_records']??[]) as $key=>$record){
        if((int)($record['created_at']??0)<time()-86400)unset($_SESSION['contribution_records'][$key]);
    }
    $_SESSION['contribution_records'][$state]=['payment_intent_id'=>$id,'amount_cents'=>$cents,'created_at'=>time()];
    session_write_close();
    header('Location: '.$url,true,303);
    exit;
}catch(Throwable $e){
    error_log('[Verger contribution] '.$e->getMessage());
    contribution_start_session();
    $_SESSION['contribution_error']='La contribution n’a pas pu être préparée. Aucun paiement n’a été demandé.';
    session_write_close();
    header('Location: /soutenir/retour.php?error=1',true,303);
    exit;
}
