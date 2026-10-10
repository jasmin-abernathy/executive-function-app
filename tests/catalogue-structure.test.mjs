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
]);
const slugs = ['vevak','fonctions-executives','resosoin','resilience-vault','dendrila-cms','mon-manager-web','communication-libre','verger-associations','atelier-epub','librairie-universelle','dendrila-privacy','sans-effort','dendrila-forms'];
const types = {
  'dendrila-privacy':'wordpress',
  'dendrila-cms':'sites','mon-manager-web':'sites','resosoin':'sites',
  'vevak':'applications','fonctions-executives':'applications','resilience-vault':'applications',
  'communication-libre':'autres','verger-associations':'autres','atelier-epub':'autres','librairie-universelle':'autres','sans-effort':'autres','dendrila-forms':'autres'
};
const categories = ['wordpress','sites','applications','autres'];
for(const lang of ['fr','en']){
  const home = read(lang==='fr'?'index.html':'en/index.html');
  const prefix = lang==='fr'?'/projets/':'/en/projects/';
  const sections=[...home.matchAll(/<section class="project-group" data-project-group="([^"]+)"/g)].map(m=>m[1]);
  assert.deepEqual(sections,categories,lang+' : quatre sections attendues');
  const cards=[...home.matchAll(/<article class="project-card[^\"]*" data-project-category="([^"]+)">([\s\S]*?)<\/article>/g)];
  assert.equal(cards.length,13,lang+' : treize cartes requises');
  assert.ok(!home.includes('data-project-categories='),lang+' : anciens tags thématiques');
  assert.ok(!home.includes('class="project-tags"'),lang+' : anciennes étiquettes');
  for(const type of ['all',...categories]){
    assert.ok(home.includes('data-project-filter="'+type+'"'),lang+' : filtre absent '+type);
  }
  const destinations=cards.map(m=>{
    const href=m[2].match(/<a class="project-card-link" href="([^"]+)"/);
    assert.ok(href,lang+' : carte sans lien');
    return {category:m[1],href:href[1]};
  });
  assert.equal(new Set(destinations.map(x=>x.href)).size,13,lang+' : destinations dupliquées');
  for(const slug of slugs){
    const wanted=external.get(slug)||prefix+slug+'/';
    assert.ok(destinations.some(x=>x.href===wanted&&x.category===types[slug]),lang+' : mauvaise famille ou destination pour '+slug);
    const detail=(lang==='fr'?'projets/':'en/projects/')+slug+'/index.html';
    assert.ok(existsSync(join(base,detail)),lang+' : fiche absente '+slug);
    const html=read(detail);
    assert.ok(html.includes('class="detail-section"') || html.includes('class="detail-section '),lang+' : contenu absent '+slug);
    assert.ok(html.includes(lang==='fr'?'/#projets':'/en/#projets'),lang+' : retour manquant '+slug);
    assert.ok(html.includes('class="detail-category"'),lang+' : type absent '+slug);
  }
  assert.ok(home.includes('/assets/style.css?v=20261010-families1'),lang+' : CSS pas actualisé');
  assert.ok(home.includes('/assets/app.js?v=20261010-families1'),lang+' : JS pas actualisé');
}
const js=read('assets/app.js');
const css=read('assets/style.css');
assert.ok(!js.includes('project-expand'),'ancien accordéon non supprimé');
assert.ok(css.includes('.project-card-link'),'CSS des cartes absent');
assert.ok(read('sitemap.xml').includes('/projets/fonctions-executives/'),'Fiches absentes du sitemap');
for (const p of ['projets/dendrila-privacy/index.html','en/projects/dendrila-privacy/index.html']) {
 const html=read(p);
 assert.ok(html.includes('privacy-offers')&&html.includes('privacy-studio')&&html.includes('privacy-agency'),p+' : cartes manquantes');
 assert.ok(html.includes('https://github.com/jasmin-abernathy/dendrila-privacy/issues'),p+' : demandes GitHub manquantes');
 assert.ok(html.includes('https://wordpress.org/support/plugin/dendrila-privacy/'),p+' : forum WordPress manquant');
 assert.ok(html.includes('https://wordpress.org/plugins/dendrila-privacy/'),p+' : installation gratuite manquante');
 assert.ok(!/solo/i.test(html),p+' : offre Solo supprimée');
 assert.ok(html.includes(p.startsWith('en/')?'€99':'99 €'),p+' : tarif Studio manquant');
 assert.ok(html.includes(p.startsWith('en/')?'€199':'199 €'),p+' : tarif Agence manquant');
 assert.ok(html.includes(p.startsWith('en/')?'Advanced prototype':'Prototype avancé'),p+' : Studio doit être présenté comme avancé');
 assert.ok(html.includes(p.startsWith('en/')?'Planning · partial features':'En conception · fonctions partielles'),p+' : Agence doit être présentée comme moins avancée');
 assert.ok(html.includes('class="privacy-detail"'),p+' : la page manque son sélecteur CSS dédié');
 assert.ok(html.includes('/assets/project-detail.css?v=20261010-mobilefix1'),p+' : cache du CSS non invalidé');
}
assert.ok(read('sitemap.xml').includes('/projets/dendrila-privacy/'),'Dendrila Privacy absente du sitemap');
const detailCss=read('assets/project-detail.css');
assert.match(detailCss,/\.privacy-offers \.project-card\s*\{\s*display:\s*flex\s*;/,'Les cartes Dendrila reprennent la grille compacte mobile du catalogue');
assert.match(detailCss,/\.privacy-detail \.site-header \.header-inner\s*\{/,'Le bandeau Dendrila doit se réorganiser sur mobile');
for(const p of ['projets/dendrila-forms/index.html','en/projects/dendrila-forms/index.html']){
  const html=read(p);
  assert.ok(html.includes('0.1.0-alpha'),p+' : état alpha absent');
  assert.ok(html.includes('https://github.com/jasmin-abernathy/dendrila-forms/issues'),p+' : issues manquantes');
}
assert.ok(read('sitemap.xml').includes('/projets/dendrila-forms/'),'Dendrila Forms absent du sitemap');
assert.ok(js.includes('data-project-category'),'Filtrage par familles non implémenté');
console.log('OK : 13 projets par langue, 26 fiches, quatre catégories et liens Dendrila Forms.');
