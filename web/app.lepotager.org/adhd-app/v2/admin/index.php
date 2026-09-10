<?php
declare(strict_types=1);require dirname(__DIR__).'/_common.php';
session_set_cookie_params(['httponly'=>true,'secure'=>!empty($_SERVER['HTTPS'])&&$_SERVER['HTTPS']!=='off','samesite'=>'Strict']);session_start();
$error='';if(isset($_GET['logout'])){session_destroy();header('Location: ./');exit;}
if($_SERVER['REQUEST_METHOD']==='POST'&&isset($_POST['login'])){$u=trim((string)($_POST['username']??''));$p=(string)($_POST['password']??'');if(hash_equals((string)config('admin.username'),$u)&&password_verify($p,(string)config('admin.password_hash'))){$_SESSION['v2_admin']=true;header('Location: ./');exit;}$error='Invalid username or password.';}
$auth=!empty($_SESSION['v2_admin']);$data=[];$rows=[];$qcounts=[];$guard=[];
if($auth){$pdo=db();if(v2_tables_ready($pdo)){
$data['started']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions")->fetchColumn();
$data['core']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE status IN ('core_submitted','completed')")->fetchColumn();
$data['done']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE status='completed'")->fetchColumn();
$data['minor']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE cohort='minor' AND status IN ('core_submitted','completed')")->fetchColumn();
$data['adult']=(int)$pdo->query("SELECT COUNT(*) FROM survey_v2_sessions WHERE cohort='adult' AND status IN ('core_submitted','completed')")->fetchColumn();
$rows=$pdo->query("SELECT current_step,status,COUNT(*) n FROM survey_v2_sessions GROUP BY current_step,status ORDER BY n DESC")->fetchAll();
$guard=$pdo->query("SELECT status,COUNT(*) n FROM survey_v2_guardian_consents GROUP BY status")->fetchAll();
$answers=$pdo->query("SELECT a.question_id,a.answer_json FROM survey_v2_answers a JOIN survey_v2_sessions s ON s.id=a.session_id WHERE s.status IN ('core_submitted','completed')")->fetchAll();
foreach($answers as $r){$a=json_decode((string)$r['answer_json'],true);foreach(is_array($a)?$a:[$a] as $v){$qcounts[$r['question_id']][(string)$v]=($qcounts[$r['question_id']][(string)$v]??0)+1;}}
}}
?><!doctype html><html lang="en"><head>
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
</style></head><body><div class="shell"><main class="card">
<?php if(!$auth):?><p class="eyebrow">V2 admin</p><h1>Research dashboard</h1><?php if($error):?><div class="notice error"><?=v2_h($error)?></div><?php endif;?><form method="post"><input type="hidden" name="login" value="1"><label>Username<input class="field" name="username"></label><br><br><label>Password<input class="field" type="password" name="password"></label><div class="actions"><button class="primary">Sign in</button></div></form>
<?php else:?><header class="header"><div><p class="eyebrow">V2 admin</p><h1>Product-research dashboard</h1></div><div class="actions"><a class="btn secondary" href="v1-comparison.php">V1 comparison</a><a class="btn quiet" href="?logout=1">Sign out</a></div></header>
<?php if(!$data):?><div class="notice error">V2 tables are not installed. <a href="../install.php">Run installer</a>.</div>
<?php else:?><div class="kpis"><div class="kpi"><span>Started</span><strong><?=$data['started']?></strong></div><div class="kpi"><span>Required submitted</span><strong><?=$data['core']?></strong></div><div class="kpi"><span>Fully finished</span><strong><?=$data['done']?></strong></div><div class="kpi"><span>Minor submitted</span><strong><?=$data['minor']?></strong></div></div>
<h2>Drop-off / current step</h2><div class="tablewrap"><table><thead><tr><th>Step</th><th>Status</th><th>Sessions</th></tr></thead><tbody><?php foreach($rows as $r):?><tr><td><?=v2_h($r['current_step'])?></td><td><?=v2_h($r['status'])?></td><td><?=$r['n']?></td></tr><?php endforeach;?></tbody></table></div>
<h2>Legacy guardian permission workflow</h2><p class="small">Historical only — no new guardian request is created by the current questionnaire.</p><div class="options"><?php foreach($guard as $g):?><div class="option"><strong><?=v2_h($g['status'])?></strong><span style="margin-left:auto"><?=$g['n']?></span></div><?php endforeach;?></div>
<h2>Core choices</h2><?php foreach(['recent_difficulty','visual_home_density','today_model','visual_quick_capture','inbox_review','visual_return_screen','progress_style','abandon_risks','mvp_top5','mvp_one','survey_ease'] as $qid):?>
<?php $countsForQuestion = $qcounts[$qid] ?? []; arsort($countsForQuestion); ?>
<h3><?=v2_h($qid)?></h3><div class="options"><?php foreach($countsForQuestion as $v=>$c):?><div class="option"><span><?=v2_h($v)?></span><strong style="margin-left:auto"><?=$c?></strong></div><?php endforeach;?></div><?php endforeach;?>
<div class="notice small"><strong>Privacy design:</strong> this admin does not display parent/guardian email addresses, participant names, exact ages, IP addresses or individual timestamps.</div>
<?php endif;?><?php endif;?>
</main></div></body></html>