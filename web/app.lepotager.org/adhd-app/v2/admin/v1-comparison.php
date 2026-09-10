<?php
declare(strict_types=1);require dirname(__DIR__).'/_common.php';session_set_cookie_params(['httponly'=>true,'secure'=>!empty($_SERVER['HTTPS'])&&$_SERVER['HTTPS']!=='off','samesite'=>'Strict']);session_start();if(empty($_SESSION['v2_admin'])){header('Location: ./');exit;}$pdo=db();function ch(mixed $v):string{return htmlspecialchars((string)$v,ENT_QUOTES|ENT_SUBSTITUTE,'UTF-8');}function pct(int $c,int $n):string{return $n>0?number_format(($c/$n)*100,1).'%':'—';}function ex1(string $a='s'):string{return "NOT EXISTS (SELECT 1 FROM survey_answers t WHERE t.session_id={$a}.id AND t.question_id='survey_comment' AND LOWER(TRIM(REPLACE(REPLACE(REPLACE(CAST(t.answer_json AS CHAR),'\\\"',''),CHAR(13),' '),CHAR(10),' ')))='test de jasmin')";}function lc(PDO $pdo,string $src,array $qids,?string $cohort=null):array{$out=[];$n=[];$ph=implode(',',array_fill(0,count($qids),'?'));if($src==='v1'){$sql="SELECT a.question_id,a.answer_json FROM survey_answers a JOIN survey_sessions s ON s.id=a.session_id WHERE s.status='completed' AND ".ex1('s')." AND a.question_id IN ($ph)";$st=$pdo->prepare($sql);$st->execute($qids);}else{$sql="SELECT a.question_id,a.answer_json FROM survey_v2_answers a JOIN survey_v2_sessions s ON s.id=a.session_id WHERE s.status IN ('core_submitted','completed') AND a.question_id IN ($ph)";$p=$qids;if($cohort!==null){$sql.=" AND s.cohort=?";$p[]=$cohort;}$st=$pdo->prepare($sql);$st->execute($p);}foreach($st->fetchAll() as $r){$a=json_decode((string)$r['answer_json'],true);if(is_array($a))continue;$q=(string)$r['question_id'];$k=(string)$a;$out[$q][$k]=($out[$q][$k]??0)+1;$n[$q]=($n[$q]??0)+1;}return['counts'=>$out,'n'=>$n];}$qids=['recent_difficulty','visual_home_density','visual_quick_capture','visual_return_screen','progress_style','survey_ease'];$labels=['recent_difficulty'=>'Main everyday difficulty','visual_home_density'=>'Home-screen density','visual_quick_capture'=>'Quick capture during focus','visual_return_screen'=>'Return after a break / days away','progress_style'=>'Visible progress style','survey_ease'=>'Questionnaire ease'];$v1=lc($pdo,'v1',$qids);$v2a=lc($pdo,'v2',$qids,'adult');$v2m=lc($pdo,'v2',$qids,'minor');?><!doctype html><html lang="en"><head>
<link rel="icon" href="../favicon.ico?v=123" sizes="any">
<link rel="icon" type="image/png" sizes="32x32" href="../favicon-32x32.png?v=123">
<link rel="apple-touch-icon" sizes="180x180" href="../apple-touch-icon.png?v=123">
<link rel="manifest" href="../site.webmanifest?v=123">
<meta name="theme-color" content="#f5f3ed">
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>V1 ↔ V2 comparison</title><style>
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
</style></head><body><div class="shell"><main class="card"><header class="header"><div><p class="eyebrow">Historical evidence</p><h1>V1 ↔ V2 direct comparison</h1></div><a class="btn secondary" href="./">Back to admin</a></header><div class="notice"><strong>Method:</strong> these six questions retain the same question IDs and answer codes as V1. V1 adults and V2 adults can therefore be compared directly. V2 ages 13–17 are shown separately.</div><?php foreach($qids as $qid):$keys=array_values(array_unique(array_merge(array_keys($v1['counts'][$qid]??[]),array_keys($v2a['counts'][$qid]??[]),array_keys($v2m['counts'][$qid]??[]))));$n1=(int)($v1['n'][$qid]??0);$na=(int)($v2a['n'][$qid]??0);$nm=(int)($v2m['n'][$qid]??0);$adultN=$n1+$na;?><h2><?=ch($labels[$qid])?></h2><p class="small"><code><?=ch($qid)?></code> · V1 n=<?=$n1?> · V2 adults n=<?=$na?> · V2 13–17 n=<?=$nm?></p><div class="tablewrap"><table><thead><tr><th>Answer code</th><th>V1 adults</th><th>V2 adults</th><th>V2 13–17</th><th>Combined adults</th></tr></thead><tbody><?php foreach($keys as $key):$c1=(int)($v1['counts'][$qid][$key]??0);$ca=(int)($v2a['counts'][$qid][$key]??0);$cm=(int)($v2m['counts'][$qid][$key]??0);$cc=$c1+$ca;?><tr><td><?=ch($key)?></td><td><?=$c1?> · <?=ch(pct($c1,$n1))?></td><td><?=$ca?> · <?=ch(pct($ca,$na))?></td><td><?=$cm?> · <?=ch(pct($cm,$nm))?></td><td><strong><?=$cc?> · <?=ch(pct($cc,$adultN))?></strong></td></tr><?php endforeach;?></tbody></table></div><?php endforeach;?></main></div></body></html>