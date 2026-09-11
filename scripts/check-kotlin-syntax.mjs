import {readFileSync,readdirSync} from 'node:fs';
import {spawnSync} from 'node:child_process';
import assert from 'node:assert/strict';
const root=new URL('../app/src/',import.meta.url);
function files(url) {return readdirSync(url,{withFileTypes:true}).flatMap(e=>{const child=new URL(e.name+(e.isDirectory()?'/':''),url);return e.isDirectory()?files(child):e.name.endsWith('.kt')?[child]:[]})}
const sources=files(root).map(url=>({file:url.pathname,source:readFileSync(url,'utf8')}));
const script=`
import json,sys,tree_sitter,tree_sitter_kotlin
p=tree_sitter.Parser(tree_sitter.Language(tree_sitter_kotlin.language()))
errors=[]
for item in json.load(sys.stdin):
 t=p.parse(item['source'].encode())
 def visit(n):
  if n.type=='ERROR' or n.is_missing: errors.append((item['file'],n.start_point,n.type))
  for c in n.children: visit(c)
 visit(t.root_node)
for e in errors: print(e)
sys.exit(1 if errors else 0)
`;
const r=spawnSync('python',['-c',script],{input:JSON.stringify(sources),encoding:'utf8',env:process.env});
process.stdout.write(r.stdout);process.stderr.write(r.stderr);assert.equal(r.status,0);
console.log('PASS: Kotlin grammar ('+sources.length+' files). This does not replace compilation or Android lint.');
