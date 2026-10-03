<?php
declare(strict_types=1);require dirname(__DIR__).'/_common.php';
require __DIR__.'/analytics.php';
$lang = ($_GET['lang'] ?? 'fr') === 'en' ? 'en' : 'fr';
function tr(string $fr, string $en): string { global $lang; return $lang === 'fr' ? $fr : $en; }
$catalog = ['groups' => [], 'questions' => []]; $catalogError = false;

session_set_cookie_params(['httponly'=>true,'secure'=>!empty($_SERVER['HTTPS'])&&$_SERVER['HTTPS']!=='off','samesite'=>'Strict']);session_start();
$error='';if(isset($_GET['logout'])){session_destroy();header('Location: ./');exit;}
if($_SERVER['REQUEST_METHOD']==='POST'&&isset($_POST['login'])){$u=trim((string)($_POST['username']??''));$p=(string)($_POST['password']??'');if(hash_equals((string)config('admin.username'),$u)&&password_verify($p,(string)config('admin.password_hash'))){$_SESSION['v2_admin']=true;header('Location: ./');exit;}$error=tr('Identifiant ou mot de passe incorrect.', 'Invalid username or password.');}
$auth=!empty($_SESSION['v2_admin']);$data=[];$rows=[];$qcounts=[];$guard=[];
if($auth){
try { $catalog = v2_admin_catalog(dirname(__DIR__)); } catch (Throwable $e) { $catalogError = true; }
$pdo=db();if(v2_tables_ready($pdo)){
$data['started']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions")->fetchColumn();
$data['core']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE status IN ('core_submitted','completed')")->fetchColumn();
$data['done']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE status='completed'")->fetchColumn();
$data['minor']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE cohort='minor' AND status IN ('core_submitted','completed')")->fetchColumn();
$data['adult']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE cohort='adult' AND status IN ('core_submitted','completed')")->fetchColumn();
$rows=$pdo->query("SELECT current_step,status,COUNT(*) n FROM survey_v2_sessions GROUP BY current_step,status ORDER BY n DESC")->fetchAll();
$answers=$pdo->query("SELECT a.question_id,a.answer_json FROM survey_v2_answers a JOIN survey_v2_sessions s ON s.id=a.session_id WHERE s.status IN ('core_submitted','completed')")->fetchAll();
$qcounts = v2_admin_aggregate($answers);
}}
?><!doctype html><html lang="<?=v2_h($lang)?>"><head>
<link rel="icon" href="../favicon.ico?v=123" sizes="any">
<link rel="icon" type="image/png" sizes="32x32" href="../favicon-32x32.png?v=123">
<link rel="apple-touch-icon" sizes="180x180" href="../apple-touch-icon.png?v=123">
<link rel="manifest" href="../site.webmanifest?v=123">
<meta name="theme-color" content="#f5f3ed">
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>V2 admin</title><style>
:root{
  --bg:#f5f3ed;--card:#fffdfa;--text:#203028;--muted:#65736b;
  --line:#d9dfd8;--green:#4f7a61;--green2:#dfece3;--peach:#f3e4dc;
  --yellow:#f2edcf;--lav:#e8e4f2;--blue:#e0eaf0;--pink:#f1e0e7;
  --danger:#7b3c3c;--shadow:0 14px 40px rgba(33,48,40,.08)
}
*{box-sizing:border-box}
html{background:var(--bg);color:var(--text);font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif}
body{margin:0;padding:18px}
a{color:#315f45}
.shell{max-width:880px;margin:0 auto}
.header{display:flex;justify-content:space-between;gap:14px;align-items:center;margin-bottom:16px}
.brand{text-decoration:none;color:var(--text);font-weight:800}
.card{background:var(--card);border:1px solid var(--line);border-radius:26px;padding:clamp(20px,4vw,36px);box-shadow:var(--shadow)}
.eyebrow{text-transform:uppercase;letter-spacing:.08em;font-size:.78rem;font-weight:850;color:var(--green)}
h1,h2,h3{line-height:1.12;margin-top:.2em}
h1{font-size:clamp(2rem,6vw,3.7rem)} h2{font-size:clamp(1.6rem,4vw,2.55rem)}
.lead{font-size:1.08rem;line-height:1.55;color:#45564d}
.notice,.soft{border:1px solid var(--line);border-radius:18px;padding:15px 16px;margin:16px 0;background:#fafaf7;line-height:1.5}
.soft{background:var(--green2)}
.warning{background:var(--peach)}
.actions{display:flex;flex-wrap:wrap;gap:10px;margin-top:22px}
button,.btn{appearance:none;border:0;border-radius:15px;padding:13px 17px;font:inherit;font-weight:800;cursor:pointer;text-decoration:none;display:inline-flex;align-items:center;justify-content:center}
.primary{background:var(--green);color:white}
.secondary{background:white;color:var(--text);border:1px solid var(--line)}
.quiet{background:transparent;color:var(--green);border:1px solid transparent}
button:disabled{opacity:.55;cursor:not-allowed}
.options{display:grid;gap:10px;margin:20px 0}
.option{display:flex;gap:12px;align-items:flex-start;border:1px solid var(--line);border-radius:17px;padding:14px;background:white;cursor:pointer}
.option:has(input:checked){outline:3px solid rgba(79,122,97,.18);border-color:var(--green);background:#f7fbf8}
.option input{margin-top:3px;accent-color:var(--green)}
.small{font-size:.86rem;color:var(--muted);line-height:1.45}
.progress{height:8px;border-radius:999px;background:#e7ebe6;overflow:hidden;margin:8px 0 20px}
.progress>span{display:block;height:100%;background:var(--green)}
.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}
.module{border:1px solid var(--line);border-radius:19px;padding:17px;background:white;display:flex;flex-direction:column;justify-content:space-between;gap:12px}
.module.done{background:var(--green2)}
.badge{display:inline-flex;width:max-content;padding:5px 8px;border-radius:999px;background:#eef2ed;color:var(--muted);font-size:.75rem;font-weight:800}
.field{width:100%;border:1px solid var(--line);border-radius:14px;padding:13px;font:inherit;background:white}
.consent{display:flex;gap:10px;align-items:flex-start;margin:12px 0;line-height:1.45}
.consent input{margin-top:4px;accent-color:var(--green)}
.footer{display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap;padding:18px 4px;color:var(--muted);font-size:.82rem}
.success{background:var(--green2)}
.error{background:#f7e5e5;border-color:#e4c6c6;color:#6e3030}
.kpis{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:10px}
.kpi{border:1px solid var(--line);border-radius:16px;padding:14px;background:white}
.kpi strong{display:block;font-size:1.8rem}
table{width:100%;border-collapse:collapse;min-width:620px}
th,td{padding:10px;border-bottom:1px solid var(--line);text-align:left;vertical-align:top}
.tablewrap{overflow:auto;border:1px solid var(--line);border-radius:16px}
@media(max-width:680px){body{padding:10px}.grid,.kpis{grid-template-columns:1fr}.actions>*{flex:1 1 100%}.card{border-radius:20px}}
.question{border-top:1px solid var(--line);padding:26px 0;scroll-margin-top:16px}
.question h3{font-size:1.2rem;line-height:1.45;overflow-wrap:anywhere}
.chart{list-style:none;padding:0;display:grid;gap:18px;margin:20px 0 0}
.chart-label{display:flex;justify-content:space-between;align-items:baseline;gap:12px;line-height:1.5}
.chart-label>div{min-width:0;overflow-wrap:anywhere}.chart-description{display:block}
.chart-value{white-space:nowrap;font-size:.9rem;font-variant-numeric:tabular-nums}
.chart-track{height:12px;background:var(--green2);border-radius:8px;overflow:hidden;margin-top:7px}
.chart-track span{display:block;height:100%;background:var(--green);border-radius:8px}
.chart-base{font-size:.86rem;line-height:1.5;color:#45564d}
.section-nav{display:flex;flex-wrap:wrap;gap:10px;margin:22px 0}.section-nav a{padding:9px 12px;border:1px solid var(--line);border-radius:12px}
.header{flex-wrap:wrap}.header h1{font-size:clamp(1.8rem,5vw,2.8rem)}
details{margin:24px 0}summary{cursor:pointer;font-weight:700;padding:12px 0}
@media(max-width:480px){.chart-label{display:block}.chart-value{display:block;margin-top:4px}.card{padding:18px}.kpis{grid-template-columns:repeat(2,minmax(0,1fr))}}
</style></head><body><div class="shell"><main class="card">
<?php if(!$auth):?><p class="eyebrow">V2 admin</p><h1><?=tr('Résultats du questionnaire', 'Research dashboard')?></h1><?php if($error):?><div class="notice error"><?=v2_h($error)?></div><?php endif;?><form method="post"><input type="hidden" name="login" value="1"><label><?=tr('Identifiant', 'Username')?><input class="field" name="username"></label><br><br><label><?=tr('Mot de passe', 'Password')?><input class="field" type="password" name="password"></label><div class="actions"><button class="primary"><?=tr('Se connecter', 'Sign in')?></button></div></form>
<?php else:?><header class="header"><div><p class="eyebrow">V2 admin</p><h1><?=tr('Résultats du questionnaire V2', 'Product-research dashboard')?></h1></div><div class="actions"><a class="btn secondary" href="?lang=fr" lang="fr">Français</a><a class="btn secondary" href="?lang=en" lang="en">English</a><a class="btn secondary" href="v1-comparison.php"><?=tr('Comparaison avec la V1', 'V1 comparison')?></a><a class="btn quiet" href="?logout=1"><?=tr('Se déconnecter', 'Sign out')?></a></div></header>
<?php if(!$data):?><div class="notice error"><?=tr('Les tables du questionnaire V2 ne sont pas installées.', 'V2 tables are not installed.')?> <a href="../install.php"><?=tr('Ouvrir l’installation', 'Run installer')?></a>.</div>
<?php else:?><div class="kpis"><div class="kpi"><span><?=tr('Commencés', 'Started')?></span><strong><?=$data['started']?></strong></div><div class="kpi"><span><?=tr('Partie obligatoire envoyée', 'Required submitted')?></span><strong><?=$data['core']?></strong></div><div class="kpi"><span><?=tr('Terminés intégralement', 'Fully finished')?></span><strong><?=$data['done']?></strong></div><div class="kpi"><span><?=tr('Réponses de mineurs', 'Minor submitted')?></span><strong><?=$data['minor']?></strong></div></div>
<details><summary><?=tr('Parcours et abandons', 'Progress and drop-off')?></summary><div class="tablewrap"><table><thead><tr><th><?=tr('Étape', 'Step')?></th><th><?=tr('Statut', 'Status')?></th><th>Sessions</th></tr></thead><tbody><?php foreach($rows as $r):
$stepLabels = ['completed'=>tr('Questionnaire terminé','Questionnaire completed'),'optional_hub'=>tr('Modules facultatifs','Optional modules'),'core_submitted'=>tr('Partie obligatoire envoyée','Core submitted')];
$statusLabels = ['active'=>tr('En cours','In progress'),'core_submitted'=>tr('Partie obligatoire envoyée','Core submitted'),'completed'=>tr('Terminé','Completed'),'deleted'=>tr('Supprimé','Deleted')];
?><tr><td><?=v2_h($catalog['questions'][$r['current_step']][$lang]['title'] ?? $stepLabels[$r['current_step']] ?? $r['current_step'])?></td><td><?=v2_h($statusLabels[$r['status']] ?? $r['status'])?></td><td><?=$r['n']?></td></tr><?php endforeach;?></tbody></table></div></details>
<?php if ($catalogError): ?><div class="notice error"><?=tr('Les libellés du questionnaire sont temporairement indisponibles.', 'Questionnaire labels are temporarily unavailable.')?></div><?php else: ?>
<p class="notice"><?=tr('Les graphiques regroupent les questionnaires dont la partie obligatoire a été envoyée. Chaque pourcentage utilise le nombre de personnes ayant répondu à la question, toutes langues confondues. Les questions facultatives ou adaptatives peuvent avoir moins de réponses.', 'Charts include questionnaires with submitted core answers. Each percentage uses the number of people who answered that question, across all languages. Optional or adaptive questions may have fewer answers.')?></p>
<nav class="section-nav" aria-label="<?=tr('Sections des résultats', 'Results sections')?>"><?php foreach ($catalog['groups'] as $i => $group): ?><a href="#group-<?=$i?>"><?=v2_h($group[$lang])?></a><?php endforeach; ?></nav>
<?php foreach ($catalog['groups'] as $i => $group): ?>
<h2 id="group-<?=$i?>"><?=v2_h($group[$lang])?></h2>
<?php foreach ($group['questions'] as $question): $qid = $question['id']; v2_admin_chart($catalog['questions'][$qid], $qcounts[$qid] ?? [], $lang); endforeach; ?>
<?php endforeach; ?>
<?php endif; ?>
<div class="notice small"><?=tr('Cette page affiche uniquement des résultats agrégés, sans noms, adresses e-mail, âges exacts, adresses IP ni horodatages individuels.', 'This page displays aggregate results only, without names, email addresses, exact ages, IP addresses or individual timestamps.')?></div>
<?php endif;?><?php endif;?>
</main></div></body></html>
