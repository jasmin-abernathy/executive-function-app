import {readFileSync, readdirSync} from 'node:fs';
import {spawnSync} from 'node:child_process';
import assert from 'node:assert/strict';
const root=new URL('../',import.meta.url);
const read=p=>readFileSync(new URL(p,root),'utf8');
const source=read('app/src/main/java/org/lepotager/executivefunction/data/AppDatabase.kt');
const create=[...source.matchAll(/"""\s*(CREATE TABLE[\s\S]*?)"""/g)].map(m=>m[1]);
const extras=[...source.matchAll(/db\.execSQL\("(CREATE TABLE[^"\n]+)"\)/g)].map(m=>m[1]);
assert.equal(create.length,2);assert.equal(extras.length,11);
const selection=source.match(/val sql = """([\s\S]*?)"""/)[1];
const freeze=read('app/src/main/java/org/lepotager/executivefunction/data/LocalBackup.kt').match(/db\.execSQL\("(UPDATE focus_sessions SET elapsed_before_segment_ms=[^"\n]+)"/)[1];
const python=`
import sys,json,sqlite3
p=json.load(sys.stdin)
d=sqlite3.connect(':memory:')
d.execute('PRAGMA foreign_keys=ON')
for sql in p['schema']: d.execute(sql)
d.execute("INSERT INTO tasks VALUES ('a','Douche','douche',NULL,'NEUTRAL',0,'READY',1,1)")
for i,ms in enumerate([600000,779000,720000]):
 d.execute("INSERT INTO focus_sessions VALUES (?, 'a','COMPLETED',0,?,NULL,NULL,NULL,1,1)",(str(i),ms))
assert d.execute(p['selection'],('COMPLETED','a')).fetchone()==(3,779000)
d.execute("INSERT INTO learning_exclusions VALUES ('1')")
assert d.execute(p['selection'],('COMPLETED','a')).fetchone()==(2,720000)
d.execute("DELETE FROM learning_exclusions WHERE session_id='1'")
assert d.execute(p['selection'],('COMPLETED','a')).fetchone()==(3,779000)
d.execute("INSERT INTO learning_overrides VALUES ('douche',840000)")
assert d.execute("SELECT duration_ms FROM learning_overrides WHERE learning_key=(SELECT learning_key FROM tasks WHERE id='a')").fetchone()==(840000,)
d.execute("INSERT INTO check_ins VALUES ('c',1,1,1,1)")
d.execute("INSERT INTO session_context VALUES ('0','c')")
d.execute("DELETE FROM check_ins WHERE id='c'")
assert d.execute("SELECT check_in_id FROM session_context").fetchone()==(None,)
d.commit()
try:
 d.execute('BEGIN')
 d.execute("DELETE FROM tasks")
 d.execute("INSERT INTO task_steps VALUES ('bad','missing','step',0,0)")
except sqlite3.IntegrityError: d.rollback()
assert d.execute('SELECT COUNT(*) FROM tasks').fetchone()==(1,)
assert d.execute('SELECT COUNT(*) FROM focus_sessions').fetchone()==(3,)
d.execute("INSERT INTO learning_resets VALUES ('douche',1)")
assert d.execute(p['selection'],('COMPLETED','a')).fetchone()==(0,None)
d.execute("DELETE FROM learning_resets")
assert d.execute(p['selection'],('COMPLETED','a')).fetchone()==(3,779000)
d.execute("INSERT INTO task_steps VALUES ('step','a','Original step',0,1)")
d.execute("INSERT INTO session_steps SELECT '0',position,title FROM task_steps WHERE task_id='a'")
d.execute("UPDATE task_steps SET title='Changed later'")
assert d.execute("SELECT title FROM session_steps WHERE session_id='0'").fetchone()==('Original step',)
assert not d.execute('PRAGMA foreign_key_check').fetchall()
d.execute("INSERT INTO focus_sessions VALUES ('running','a','RUNNING',1,60000,1000,NULL,NULL,1,1)")
d.execute(p['freeze'],(5000,5000))
assert d.execute("SELECT status,elapsed_before_segment_ms,segment_started_at FROM focus_sessions WHERE id='running'").fetchone()==('INTERRUPTED',64000,None)
for value,expected in [(779000,840000),(780000,840000),(780001,900000)]:
 assert ((value+59999)//60000+1)*60000==expected
print('PASS: schema, exclusion/undo, reset/undo, immutable step snapshots, manual reference, check-in deletion, rollback, restore clock freeze, rounding')
`;
const result=spawnSync('python',['-c',python],{input:JSON.stringify({schema:[...create,...extras],selection,freeze}),encoding:'utf8'});
process.stdout.write(result.stdout);process.stderr.write(result.stderr);assert.equal(result.status,0);
const fr=read('app/src/main/res/values/strings.xml'),en=read('app/src/main/res/values-en/strings.xml');
const names=s=>[...s.matchAll(/<string name="([^"]+)"/g)].map(m=>m[1]).sort();
assert.deepEqual(names(fr),names(en));assert.equal(new Set(names(fr)).size,names(fr).length);
const xmlFiles=readdirSync(new URL('app/src/main/res/',root),{recursive:true}).filter(p=>p.endsWith('.xml')).map(p=>'app/src/main/res/'+p);
for(const xml of [...xmlFiles,'app/src/main/AndroidManifest.xml'].map(read)) {
 const check=spawnSync('python',['-c','import sys,xml.etree.ElementTree as E; E.fromstring(sys.stdin.read())'],{input:xml,encoding:'utf8'});
 assert.equal(check.status,0,check.stderr);
}
console.log('PASS: FR/EN resource parity, unique names, XML parsing');
