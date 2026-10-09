import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { join, dirname } from 'node:path';

const base = fileURLToPath(new URL('../web/app.lepotager.org/', import.meta.url));
const read = path => readFileSync(join(base,path),'utf8');
const external = new Map([
  ['vevak','https://vevak.lepotager.org/'],
  ['resosoin','/resosoin/'],
  ['librairie-universelle','https://librairie.lepotager.org/'],
  ['dendrila-privacy','https://wordpress.org/plugins/dendrila-privacy/']
]);
const slugs = ['vevak','fonctions-executives','resosoin','resilience-vault','dendrila-cms','mon-manager-web','communication-libre','verger-associations','atelier-epub','librairie-universelle','dendrila-privacy','sans-effort'];
for(const lang of ['fr','en']){
  const home = read(lang==='fr'?'index.html':'en/index.html');
  const prefix=lang==='fr'?'/projets/':'/en/projects/';
  const cards=[...home.matchAll(/<article class="project-card[^"]*"[^>]*>([\s\S]*?)<\/article>/g)];
  const destinations=[...home.matchAll(/<a class="project-card-link" href="([^"]+)"/g)].map(m=>m[1]);
  assert.equal(cards.length,12,lang+' : douze cartes requises');
  assert.equal(destinations.length,12,lang+' : douze destinations requises');
  for (let i=0;i<slugs.length;i++){
    const slug=slugs[i];
    const wanted=external.get(slug)||prefix+slug+'/';
    assert.equal(destinations[i],wanted,lang+' : mauvaise destination pour '+slug);
    const detail=(lang==='fr'?'projets/':'en/projects/')+slug+'/index.html';
    assert.ok(existsSync(join(base,detail)),lang+' : fiche manquante : '+slug);
    const html=read(detail);
    assert.match(html, /class="detail-section"/,lang+' : contenu absent : '+slug);
    assert.ok(html.includes(lang==='fr'?'/#projets':'/en/#projets'),lang+' : lien retour absent : '+slug);
  }
  assert.ok(home.includes('/assets/style.css?v='),lang+' : CSS non référencé');
  assert.ok(home.includes('/assets/app.js?v='),lang+' : JavaScript non référencé');
}
const js=read('assets/app.js');
const css=read('assets/style.css');
assert.ok(!js.includes('project-expand'),'ancien accordéon non supprimé');
assert.ok(css.includes('.project-card-link'),'CSS des cartes absent');
assert.ok(read('sitemap.xml').includes('/projets/fonctions-executives/'),'Fiches absentes du sitemap');
console.log('OK : accueil FR/EN, 24 fiches, liens, CSS/JS et sitemap cohérents.');
