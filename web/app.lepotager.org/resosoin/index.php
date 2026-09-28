<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="description" content="RésoSoin étudie un outil de rendez-vous et d’organisation pour des cabinets indépendants : site à leur nom, agenda adapté et automatisations utiles.">
  <meta name="theme-color" content="#315c3a">
  <title>RésoSoin — le cabinet garde son site, les rendez-vous deviennent plus simples</title>
  <link rel="canonical" href="https://app.lepotager.org/resosoin/">
  <link rel="icon" href="/favicon.svg" type="image/svg+xml">
  <style>
:root{--paper:#f7f8f4;--white:#fff;--mist:#eaf1e9;--ink:#1e2922;--muted:#53645a;--line:#cbd9cd;--forest:#244b34;--deep:#173424;--lime:#d9eacb;--warm:#f2f0e8;font-family:Inter,ui-sans-serif,system-ui,-apple-system,"Segoe UI",sans-serif}
*{box-sizing:border-box}html{scroll-behavior:smooth;color:var(--ink)}body{margin:0;background:var(--paper);color:var(--ink);line-height:1.55}a{color:var(--forest);text-underline-offset:.2em}a:hover{color:#143621}a:focus-visible,button:focus-visible,summary:focus-visible{outline:3px solid #005fcc;outline-offset:3px}.skip-link{position:absolute;top:-6rem;left:1rem;z-index:40;padding:.7rem 1rem;background:#fff}.skip-link:focus{top:1rem}.shell{width:min(1160px,calc(100% - 3rem));margin-inline:auto}.section{padding:clamp(4.5rem,8vw,7.5rem) 0;scroll-margin-top:90px}.eyebrow{margin:0 0 1rem;color:var(--forest);font-size:.75rem;font-weight:850;letter-spacing:.12em;text-transform:uppercase}h1,h2,h3,p{margin-top:0}h1,h2,h3{line-height:1.08;letter-spacing:-.035em}h2{font-size:clamp(2.3rem,4.4vw,4.1rem);max-width:16ch}h3{font-size:clamp(1.45rem,2.4vw,2rem)}p{max-width:70ch}.section-intro,.section-heading>p:last-child{color:var(--muted);font-size:1.05rem}.section-heading{margin-bottom:2.6rem}.button{display:inline-flex;align-items:center;justify-content:center;min-height:48px;padding:.8rem 1.2rem;border:1px solid var(--forest);border-radius:999px;color:var(--forest);background:var(--white);font-weight:770;line-height:1.2;text-align:center;text-decoration:none;transition:transform .18s ease,background .18s ease,box-shadow .18s ease}.button:hover{transform:translateY(-3px);box-shadow:0 7px 16px #1734241c}.button-primary{color:#fff;background:var(--forest)}.button-primary:hover{color:#fff;background:var(--deep)}.button-small{min-height:42px;padding:.65rem 1.1rem}
.site-header{position:sticky;top:0;z-index:20;background:#ffffffed;border-bottom:1px solid var(--line);backdrop-filter:blur(16px)}.header-inner{min-height:76px;display:flex;align-items:center;justify-content:space-between;gap:2rem}.brand{display:flex;align-items:center;gap:.7rem;color:var(--ink);text-decoration:none;font-weight:850;white-space:nowrap}.brand-mark{width:2.6rem;height:2.6rem;display:grid;place-items:center;border:1px solid var(--line);border-radius:50%;background:var(--mist)}.brand-mark svg{width:1.8rem;height:1.8rem;fill:var(--forest);stroke:var(--mist);stroke-width:2}.brand-copy{display:grid;line-height:1.1}.brand-copy small{margin-top:.22rem;color:var(--muted);font-size:.65rem;letter-spacing:.05em;text-transform:uppercase}.nav{display:flex;align-items:center;gap:clamp(.6rem,1.3vw,1.4rem)}.nav-link{color:var(--ink);font-size:.9rem;font-weight:700;text-decoration:none}.nav-link:hover{text-decoration:underline}.accessibility-toggle{display:inline-flex;align-items:center;justify-content:center;gap:.35rem;min-width:44px;min-height:44px;padding:.45rem .7rem;border:1px solid var(--forest);border-radius:999px;background:#fff;color:var(--forest);cursor:pointer;font:700 .8rem/1.2 inherit}.accessibility-toggle svg{width:22px;height:22px;flex:none}.accessibility-toggle::before{display:none!important}.accessibility-label{font-size:.8rem}.accessibility-toggle[aria-pressed="true"]{background:var(--deep);color:#fff}
.hero{position:relative;padding:clamp(4rem,7vw,7rem) 0 clamp(4rem,7vw,7rem);background:radial-gradient(circle at 78% 21%,#e2efde 0,transparent 29%),linear-gradient(140deg,#fbfcf9,var(--paper))}.hero-grid{display:grid;grid-template-columns:minmax(0,1.1fr) minmax(340px,.8fr);gap:clamp(2.5rem,6vw,6rem);align-items:center}.hero h1{max-width:14ch;margin:0 0 1.65rem;font-size:clamp(3.1rem,5.5vw,5.2rem);line-height:1.02;letter-spacing:-.055em}.hero h1 span{color:#55805d}.lede{max-width:58ch;font-size:clamp(1.1rem,1.55vw,1.27rem);color:var(--muted)}.lede strong{color:var(--ink)}.hero-actions{display:flex;flex-wrap:wrap;gap:.8rem;margin:2rem 0 1.6rem}.trust-row{display:flex;flex-wrap:wrap;gap:.5rem}.trust-row span{padding:.3rem .65rem;border:1px solid var(--line);border-radius:999px;color:var(--forest);font-size:.78rem;font-weight:760;background:#ffffffa8}.journey-card{position:relative;overflow:hidden;padding:clamp(1.5rem,3vw,2.4rem);border:1px solid var(--line);border-radius:30px;background:#fff;box-shadow:0 30px 60px #17342413}.journey-card::before{content:"";position:absolute;width:180px;height:180px;right:-78px;top:-82px;border:34px solid var(--mist);border-radius:50%;pointer-events:none}.journey-head{position:relative;border-bottom:1px solid var(--line);padding-bottom:1.2rem}.journey-head p:last-child{max-width:29ch;margin-bottom:0;font-size:1.18rem;font-weight:770;line-height:1.35}.journey{list-style:none;margin:1.3rem 0 0;padding:0;counter-reset:journey}.journey li{display:grid;grid-template-columns:2.75rem minmax(0,1fr);gap:1rem;position:relative}.journey li+li{margin-top:1.7rem}.journey li:not(:last-child)::after{content:"";position:absolute;left:1.36rem;top:2.8rem;bottom:-1.65rem;border-left:2px dotted #b5cbb7}.journey-number{width:2.75rem;height:2.75rem;display:grid;place-items:center;border-radius:50%;background:var(--forest);color:#fff;font-weight:850}.journey strong{display:block;font-size:1.08rem}.journey p{margin:.45rem 0 0;color:var(--muted);font-size:.88rem}.status{display:inline-block;width:max-content;margin-top:.25rem;padding:.2rem .55rem;border-radius:999px;font-size:.71rem;font-weight:770;line-height:1.3;white-space:nowrap}.status-existing{background:#e4f1e3;color:#254d30}.status-building{background:#f0eee5;color:#555242}.status-testing{background:#edf1ee;color:#455950}
.audience-section{background:var(--mist)}.audience-section .section-heading h2{max-width:none}.audience-panels{display:grid;grid-template-columns:1fr 1fr;gap:1.3rem}.audience-panel{position:relative;overflow:hidden;padding:clamp(1.6rem,3vw,2.8rem);border:1px solid var(--line);border-radius:24px;background:#fff}.audience-panel-patient{background:#f8fbf7}.audience-panel::before{content:"";position:absolute;top:0;left:0;width:100%;height:6px;background:var(--forest)}.audience-panel-patient::before{background:#88b091}.audience-label{margin-bottom:1.2rem;color:var(--forest);font-size:.76rem;font-weight:840;letter-spacing:.1em;text-transform:uppercase}.audience-panel h3{max-width:21ch;min-height:2.4em;margin-bottom:1.5rem}.feature-list{list-style:none;margin:0;padding:0;counter-reset:features}.feature-list li{display:grid;grid-template-columns:2rem minmax(0,1fr);column-gap:.8rem;align-items:start;padding:1rem 0;border-top:1px solid var(--line);counter-increment:features}.feature-list li::before{content:counter(features,decimal-leading-zero);color:#74947b;font-size:.82rem;font-weight:900;font-variant-numeric:tabular-nums}.feature-list strong{font-size:.96rem;line-height:1.35}.feature-list .status{grid-column:2;margin-top:.38rem}
.mechanics-section{background:var(--paper)}.mechanics-grid{display:grid;grid-template-columns:minmax(0,1.25fr) minmax(290px,.75fr);gap:clamp(2.5rem,7vw,6rem);align-items:start}.mechanics-grid h2{max-width:14ch}.connection-flow{display:grid;gap:0;margin-top:2.7rem;border-top:1px solid var(--line)}.flow-step{display:grid;grid-template-columns:8.5rem minmax(0,1fr);gap:1.3rem;padding:1.45rem 0;border-bottom:1px solid var(--line)}.flow-kicker{color:var(--forest);font-size:.73rem;font-weight:870;letter-spacing:.08em;text-transform:uppercase}.flow-step strong{font-size:1.08rem}.flow-step p{grid-column:2;margin:-.5rem 0 0;color:var(--muted);font-size:.94rem}.flow-arrow{display:none}.validation-card{padding:clamp(1.7rem,3vw,2.6rem);border-radius:26px;background:var(--deep);color:#fff}.validation-card .eyebrow{color:#c5dbc8}.validation-card h3{max-width:17ch;margin-bottom:1.6rem}.question-list{list-style:none;margin:0 0 2rem;padding:0;counter-reset:questions}.question-list li{position:relative;min-height:3.3rem;padding:.1rem 0 1rem 3.2rem;border-bottom:1px solid #ffffff3c;counter-increment:questions}.question-list li+li{padding-top:1rem}.question-list li::before{content:counter(questions,decimal-leading-zero);position:absolute;top:.05rem;left:0;color:#b7d6bb;font-size:1.55rem;font-weight:850;line-height:1}.question-list li+li::before{top:1rem}.automation-note{padding:1.15rem;border-radius:14px;background:#edf5ec;color:var(--ink);font-size:.94rem}.automation-note strong{display:block;margin-bottom:.25rem;color:var(--forest)}.text-link{color:#d2eed6;font-weight:760}.text-link:hover{color:#fff}
.price-study-section{background:var(--warm)}.price-study-grid{display:grid;grid-template-columns:minmax(0,.96fr) minmax(0,1.04fr);gap:clamp(3rem,7vw,7rem)}.price-block h2,.study-block h2{max-width:13ch}.price-block>p,.study-block>p{color:var(--muted)}.price-block>.eyebrow,.study-block>.eyebrow{color:var(--forest)}.price-ranges{list-style:none;display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1px;margin:1.8rem 0 1.2rem;padding:1px;border:1px solid var(--line);border-radius:20px;overflow:hidden;background:var(--line);counter-reset:range}.price-ranges li{position:relative;min-height:100px;display:flex;flex-direction:column;justify-content:center;padding:1.15rem;background:#fff;counter-increment:range;transition:background .18s ease}.price-ranges li:hover{background:#ecf5ea}.price-ranges li::before{content:counter(range,decimal-leading-zero);position:absolute;top:.7rem;right:.75rem;color:#8ba88e;font-size:.67rem;font-weight:850}.range-amount{font-size:clamp(1.1rem,1.65vw,1.5rem);font-weight:850;letter-spacing:-.03em;white-space:nowrap}.range-caption{margin-top:.2rem;color:var(--muted);font-size:.72rem}.price-footnote{font-size:.86rem}.value-note{margin-top:1.6rem;padding:1.5rem 1.6rem;border-radius:18px;background:#e1eddc}.value-note strong{display:block;color:var(--deep);font-size:1.2rem}.value-note p{margin:.55rem 0 0;color:#3d5343;font-size:.92rem}.study-cards{display:grid;gap:1rem;margin-top:1.7rem}.study-card{display:grid;grid-template-columns:3.5rem minmax(0,1fr);gap:1rem;padding:1.4rem;border:1px solid var(--line);border-radius:20px;background:#fff}.study-icon{display:grid;place-items:center;width:3rem;height:3rem;border-radius:50%;background:var(--mist);color:var(--forest);font-weight:880;font-size:1.1rem}.study-card h3{margin:.1rem 0 .4rem;font-size:1.35rem}.study-card p{margin-bottom:1rem;color:var(--muted);font-size:.94rem}.privacy-note{display:grid;gap:.2rem;margin-top:1.5rem;padding:1rem 0;border-top:1px solid var(--line);color:var(--muted);font-size:.89rem}.privacy-note strong{color:var(--ink)}.neutral-invite{font-size:.85rem}
.transparency-section{background:#fff}.transparency-grid{display:grid;grid-template-columns:minmax(0,.85fr) minmax(0,1.15fr);gap:clamp(3rem,7vw,7rem)}.details-stack{display:grid;gap:.75rem}.details-stack details{padding:0 1.2rem;border:1px solid var(--line);border-radius:14px;background:var(--paper)}.details-stack summary{position:relative;padding:1.1rem 2rem 1.1rem 0;list-style:none;cursor:pointer;font-weight:770}.details-stack summary::-webkit-details-marker{display:none}.details-stack summary::after{content:"+";position:absolute;right:.1rem;top:.75rem;color:var(--forest);font-size:1.4rem}.details-stack details[open] summary::after{content:"−"}.details-content{padding:1.1rem 0;border-top:1px solid var(--line)}.details-content p{color:var(--muted);font-size:.94rem}.details-content p:last-child{margin-bottom:0}.sources{display:grid;gap:.5rem}.sources a{display:block;padding:.8rem;border:1px solid var(--line);border-radius:10px;background:#fff;text-decoration:none}.sources a:hover{border-color:var(--forest)}.sources strong{display:block;color:var(--forest)}.sources span{color:var(--muted);font-size:.85rem}.footer{padding:2rem 0;border-top:1px solid var(--line);background:#fff}.footer-inner{display:flex;justify-content:space-between;gap:1rem;flex-wrap:wrap;color:var(--muted);font-size:.85rem}
@media(max-width:980px){.hero-grid,.mechanics-grid,.price-study-grid,.transparency-grid{grid-template-columns:1fr}.hero-grid{gap:3rem}.journey-card{max-width:760px}.mechanics-grid{gap:3.5rem}.price-study-grid{gap:4rem}.price-block h2,.study-block h2{max-width:none}}
@media(max-width:700px){.shell{width:min(100% - 2rem,1160px)}.site-header .header-inner{min-height:64px;gap:.4rem}.brand-mark{width:2.25rem;height:2.25rem}.brand-copy small,.nav-link,.accessibility-label{display:none}.nav{gap:.4rem}.accessibility-toggle{width:44px;padding:0}.nav .button-small{min-height:42px;padding:.55rem .8rem;font-size:.82rem}.hero{padding:3.7rem 0 4rem}.hero h1{font-size:clamp(2.85rem,9.3vw,4.3rem)}.hero-actions{display:grid}.hero-actions .button{width:100%}.section{padding:4rem 0}.audience-panels{grid-template-columns:1fr}.audience-panel h3{min-height:0}.price-ranges{grid-template-columns:repeat(2,minmax(0,1fr))}.transparency-grid{gap:1.6rem}}
@media(max-width:400px){.shell{width:min(100% - 1.4rem,1160px)}.brand-copy{font-size:.9rem}.hero h1{font-size:clamp(2.5rem,9.2vw,3rem)}h2{font-size:2.3rem}.journey-card,.audience-panel,.validation-card{padding:1.35rem}.flow-step{grid-template-columns:1fr;gap:.35rem}.flow-step p{grid-column:1;margin:0}.study-card{grid-template-columns:1fr}.price-ranges li{padding:.8rem;min-height:88px}.range-amount{font-size:1.08rem}}
@media(prefers-reduced-motion:reduce){html{scroll-behavior:auto}*,*::before,*::after{transition-duration:.01ms!important;animation-duration:.01ms!important}}

  </style>
  <link rel="stylesheet" href="/accessibility.css">
  <script defer>
    document.addEventListener('DOMContentLoaded', () => {
      const button = document.querySelector('[data-app-accessibility-toggle]');
      const label = button.querySelector('.accessibility-label');
      const storageKey = 'app-lepotager-accessible-mode';
      const apply = enabled => {
        document.documentElement.dataset.appAccessible = String(enabled);
        button.setAttribute('aria-pressed', String(enabled));
        const text = enabled ? 'Version standard' : 'Version accessible';
        button.setAttribute('aria-label', text);
        button.title = text;
        label.textContent = text;
      };
      let saved = false;
      try { saved = localStorage.getItem(storageKey) === 'true'; } catch (_) {}
      apply(saved);
      button.addEventListener('click', () => {
        const enabled = document.documentElement.dataset.appAccessible !== 'true';
        apply(enabled);
        try { localStorage.setItem(storageKey, String(enabled)); } catch (_) {}
      });
    });
  </script>
</head>
<body>
  <a class="skip-link" href="#contenu">Aller au contenu</a>

  <header class="site-header">
    <div class="shell header-inner">
      <a class="brand" href="/resosoin/" aria-label="RésoSoin, revenir en haut de la page">
        <span class="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 40 40" role="img">
            <circle cx="12" cy="20" r="5"></circle>
            <circle cx="28" cy="12" r="5"></circle>
            <circle cx="28" cy="28" r="5"></circle>
            <path d="M16 18l7-4M16 22l7 4"></path>
          </svg>
        </span>
        <span class="brand-copy">RésoSoin <small>projet en étude</small></span>
      </a>

      <nav class="nav" aria-label="Navigation RésoSoin">
        <a class="nav-link" href="#projet">Le projet</a>
        <a class="nav-link" href="#pour-qui">Pour qui</a>
        <a class="nav-link" href="#etude">L’étude</a>
        <button class="accessibility-toggle" type="button" data-app-accessibility-toggle aria-pressed="false" aria-label="Activer la version accessible" title="Activer la version accessible"><svg viewBox="0 0 24 24" fill="none" aria-hidden="true" focusable="false"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="6.5" r="1.6" fill="currentColor"/><path d="M6.7 9.4h10.6M12 9.8v4.5m0 0-3 4.5m3-4.5 3 4.5" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg><span class="accessibility-label">Version accessible</span></button>
        <a class="button button-small button-primary" href="#etude">Participer</a>
      </nav>
    </div>
  </header>

  <main id="contenu">
    <section class="hero" id="projet">
      <div class="shell hero-grid">
        <div class="hero-copy">
          <p class="eyebrow">RésoSoin · projet en étude</p>
          <h1>Votre cabinet.<br><span>Vos outils.</span><br>Des rendez-vous plus simples.</h1>
          <p class="lede">Un site au nom du cabinet, un agenda pensé pour l’équipe et des démarches claires pour les patients : voilà ce que RésoSoin veut construire avec vous. <strong>Le service est encore en étude.</strong></p>

          <div class="hero-actions" aria-label="Participer à l'étude RésoSoin">
            <a class="button button-primary" href="/resosoin/questionnaire/?audience=doctor&amp;source=resosoin_page">Je travaille dans un cabinet</a>
            <a class="button button-secondary" href="/resosoin/questionnaire/?audience=patient&amp;source=resosoin_page">Je prends des rendez-vous</a>
          </div>

          <div class="trust-row" aria-label="Principes du projet">
            <span>Site indépendant</span>
            <span>Automatisations explicites</span>
            <span>Sans IA imposée</span>
          </div>
        </div>

        <aside class="journey-card" aria-label="Schéma du parcours RésoSoin envisagé">
          <div class="journey-head">
            <p class="eyebrow">Le fil conducteur</p>
            <p>Relier les outils sans retirer au cabinet son identité ni ses choix.</p>
          </div>
          <ol class="journey">
            <li>
              <span class="journey-number">1</span>
              <div>
                <strong>Le site du cabinet</strong>
                <small class="status status-existing">Socle existant</small>
                <p>Nom, domaine et informations publiques restent propres au cabinet.</p>
              </div>
            </li>
            <li>
              <span class="journey-number">2</span>
              <div>
                <strong>Agenda et outils de l’équipe</strong>
                <small class="status status-building">En conception</small>
                <p>Plusieurs praticiens, secrétariat et intégrations doivent encore être construits et testés.</p>
              </div>
            </li>
            <li>
              <span class="journey-number">3</span>
              <div>
                <strong>Un accès simple pour les patients</strong>
                <small class="status status-testing">À tester</small>
                <p>Prendre, déplacer ou annuler un rendez-vous sans imposer un portail unique.</p>
              </div>
            </li>
          </ol>
        </aside>
      </div>
    </section>

    <section class="section audience-section" id="pour-qui">
      <div class="shell">
        <div class="section-heading">
          <p class="eyebrow">Deux usages, un même objectif</p>
          <h2>Deux parcours. Un lien direct.</h2>
          <p>RésoSoin part des pratiques réelles. Les fonctions qui ne sont pas encore construites sont indiquées comme telles.</p>
        </div>

        <div class="audience-panels">
          <article class="audience-panel audience-panel-pro">
            <div class="audience-label">Côté cabinet</div>
            <h3>Garder la relation directe avec les patients.</h3>
            <ul class="feature-list">
              <li><strong>Site et domaine propres</strong><span class="status status-existing">Socle existant</span></li>
              <li><strong>Agenda multi-praticiens et secrétariat</strong><span class="status status-building">En conception</span></li>
              <li><strong>Rappels, confirmations et créneaux libérés</strong><span class="status status-building">En conception</span></li>
              <li><strong>Export, migration et raccord aux outils utilisés</strong><span class="status status-testing">À valider</span></li>
            </ul>
          </article>

          <article class="audience-panel audience-panel-patient">
            <div class="audience-label">Côté patient</div>
            <h3>Faire les démarches essentielles sans apprendre un nouvel écosystème.</h3>
            <ul class="feature-list">
              <li><strong>Trouver le site du cabinet</strong><span class="status status-existing">Principe actuel</span></li>
              <li><strong>Réserver, déplacer ou annuler simplement</strong><span class="status status-building">En conception</span></li>
              <li><strong>Choisir les rappels utiles</strong><span class="status status-building">En conception</span></li>
              <li><strong>Téléphone et aide d’un proche toujours possibles</strong><span class="status status-testing">Exigence à tester</span></li>
            </ul>
          </article>
        </div>
      </div>
    </section>

    <section class="section mechanics-section" aria-labelledby="fonctionnement-title">
      <div class="shell mechanics-grid">
        <div>
          <p class="eyebrow">Comment cela doit se relier</p>
          <h2 id="fonctionnement-title">Un service qui s’adapte au cabinet.</h2>
          <p class="section-intro">Le site public conserve l’identité du cabinet. L’agenda et les outils de l’équipe doivent se raccorder sans mélanger les contenus publics et les données sensibles. Voici le parcours envisagé.</p>

          <div class="connection-flow" aria-label="Fonctionnement envisagé">
            <div class="flow-step">
              <span class="flow-kicker">1 · Présence</span>
              <strong>Chaque cabinet garde son site</strong>
              <p>Informations publiques, identité et domaine restent séparables du service de rendez-vous.</p>
            </div>
            <span class="flow-arrow" aria-hidden="true">→</span>
            <div class="flow-step">
              <span class="flow-kicker">2 · Organisation</span>
              <strong>L’agenda se raccorde aux pratiques de l’équipe</strong>
              <p>Multi-praticiens, secrétariat, calendriers et autres outils doivent être prototypés avec les cabinets.</p>
            </div>
            <span class="flow-arrow" aria-hidden="true">→</span>
            <div class="flow-step">
              <span class="flow-kicker">3 · Accès</span>
              <strong>Le patient utilise le chemin le plus simple</strong>
              <p>Le site du cabinet reste la porte d’entrée ; une recherche commune entre cabinets est seulement une hypothèse à tester.</p>
            </div>
          </div>
        </div>

        <aside class="validation-card">
          <p class="eyebrow">Ce que l’étude doit décider</p>
          <h3>Quatre décisions à prendre avec vous.</h3>
          <ol class="question-list">
            <li>Quels irritants font vraiment perdre du temps aux équipes ?</li>
            <li>Quelles intégrations sont indispensables au quotidien ?</li>
            <li>Qu’est-ce qui bloque ou complique la prise de rendez-vous côté patient ?</li>
            <li>Quel coût permet un service durable sans faire payer des fonctions inutiles ?</li>
          </ol>
          <p class="automation-note"><strong>Automatiser ne veut pas dire imposer de l’IA.</strong> Confirmations, rappels, horaires récurrents ou créneaux libérés peuvent reposer sur des règles explicites et prévisibles.</p>
          <a class="text-link" href="#etude">Voir comment participer à l’étude →</a>
        </aside>
      </div>
    </section>

    <section class="section price-study-section" id="etude">
      <div class="shell">
        <div class="price-study-grid">
          <div class="price-block">
            <p class="eyebrow">Prix étudiés</p>
            <h2>Quel prix serait juste pour ce socle&nbsp;?</h2>
            <p>Nous demandons aux professionnels quel prix mensuel <strong>par praticien</strong> leur semblerait raisonnable pour un socle comprenant site, agenda, gestion de plusieurs praticiens et automatisations courantes.</p>

            <ol class="price-ranges" aria-label="Tranches proposées dans le questionnaire professionnel">
              <li><span class="range-amount">&lt; 30 €</span><span class="range-caption">Moins de 30 €</span></li>
              <li><span class="range-amount">30–49 €</span><span class="range-caption">par mois</span></li>
              <li><span class="range-amount">50–69 €</span><span class="range-caption">par mois</span></li>
              <li><span class="range-amount">70–99 €</span><span class="range-caption">par mois</span></li>
              <li><span class="range-amount">100–149 €</span><span class="range-caption">par mois</span></li>
              <li><span class="range-amount">150 € +</span><span class="range-caption">150 € ou plus</span></li>
            </ol>
            <p class="price-footnote">Une réponse « Impossible à estimer à ce stade » est également proposée. Il s’agit d’ordres de grandeur, pas d’un tarif annoncé. Les SMS et la mise en service pourraient être facturés à part.</p>

            <div class="value-note">
              <strong>Un coût plus juste, sans service au rabais.</strong>
              <p>Le modèle étudié cherche à concentrer l’investissement sur ce dont un cabinet a réellement besoin, sans faire payer une suite entière lorsqu’elle n’est pas utilisée. Fiabilité, confidentialité, accessibilité, intégration aux outils existants, migration et accompagnement restent des exigences à financer correctement.</p>
            </div>
          </div>

          <div class="study-block">
            <p class="eyebrow">Participer à l’étude</p>
            <h2>Vos usages d’abord, le concept ensuite.</h2>
            <p>Les premières questions portent sur les pratiques réelles afin de limiter l’effet de présentation. Les résultats resteront exploratoires et auto-sélectionnés.</p>

            <div class="study-cards">
              <article class="study-card">
                <span class="study-icon" aria-hidden="true">01</span>
                <div>
                  <h3>Professionnels de santé</h3>
                  <p>Organisation actuelle, irritants, automatisations, intégrations, priorités et coût acceptable.</p>
                  <a class="button button-primary" href="/resosoin/questionnaire/?audience=doctor&amp;source=resosoin_page">Répondre côté professionnel</a>
                </div>
              </article>

              <article class="study-card">
                <span class="study-icon" aria-hidden="true">02</span>
                <div>
                  <h3>Patients</h3>
                  <p>Habitudes de rendez-vous, difficultés, accessibilité, comptes, rappels et recherche de cabinets.</p>
                  <a class="button button-secondary" href="/resosoin/questionnaire/?audience=patient&amp;source=resosoin_page">Répondre côté patient</a>
                </div>
              </article>
            </div>

            <div class="privacy-note">
              <strong>18 ans et plus.</strong>
              <span>Pas de nom, diagnostic ou traitement demandé. Les réponses peuvent être reprises sur le même navigateur et supprimées depuis le questionnaire.</span>
            </div>
            <p class="neutral-invite"><a href="/resosoin/questionnaire/invitation.php">Partager le lien d’invitation neutre, sans présenter RésoSoin auparavant →</a></p>
          </div>
        </div>
      </div>
    </section>

    <section class="section transparency-section" aria-labelledby="transparence-title">
      <div class="shell transparency-grid">
        <div>
          <p class="eyebrow">Transparence</p>
          <h2 id="transparence-title">Ce qui est visé n’est pas encore acquis.</h2>
          <p class="section-intro">RésoSoin ne se présente ni comme un produit fini ni comme « plus sûr » qu’une solution existante. Les choix d’hébergement, l’agenda final, les intégrations, la recherche entre cabinets et une éventuelle application patient restent à prototyper ou à valider.</p>
        </div>

        <div class="details-stack">
          <details>
            <summary>Hébergement, HDS et juridiction</summary>
            <div class="details-content">
              <p>L’objectif est d’héberger les données en Europe et, lorsqu’il s’agit de données de santé, de recourir à un prestataire adapté aux exigences HDS. Ce choix n’est pas encore finalisé.</p>
              <p>Doctolib indique aujourd’hui recourir notamment à AWS ainsi qu’à S3NS/GCP pour l’hébergement de données de santé dans l’EEE. Pour les données particulièrement sensibles, la CNIL recommande de prendre aussi en compte la juridiction applicable au fournisseur, pas seulement la localisation physique des serveurs.</p>
            </div>
          </details>

          <details>
            <summary>Prix et comparaison avec les solutions existantes</summary>
            <div class="details-content">
              <p>RésoSoin n’affiche pas de prix commercial à ce stade. Les six tranches ci-dessus sont les réponses proposées dans le questionnaire de recherche. Elles ne préjugent ni du périmètre final ni de sa viabilité économique.</p>
              <p>Les offres et tarifs publics des solutions existantes évoluent et ne couvrent pas toujours le même périmètre. Ils servent donc de contexte, pas de preuve qu’un service futur offrirait automatiquement les mêmes prestations pour moins cher.</p>
            </div>
          </details>

          <details>
            <summary>Sources utilisées pour cadrer ces affirmations</summary>
            <div class="details-content sources">
              <a href="https://info.doctolib.fr/solution/gestionnaire-de-taches/page/2/">
                <strong>Doctolib Pro — offres et tarifs publics</strong>
                <span>Repère public sur les offres ; les montants et périmètres peuvent évoluer.</span>
              </a>
              <a href="https://info.doctolib.fr/securite/">
                <strong>Doctolib — sécurité et hébergement</strong>
                <span>Présentation publique de l’hébergement et des mesures de sécurité.</span>
              </a>
              <a href="https://community.doctolib.fr/t/on-repond-a-vos-questions-sur-la-securite-des-donnees/107903">
                <strong>Doctolib Communauté — AWS et Cloud Act</strong>
                <span>Échanges et réponse publique de Doctolib sur le risque d’accès par des autorités américaines.</span>
              </a>
              <a href="https://www.cnil.fr/fr/cloud-les-risques-dune-certification-europeenne-permettant-lacces-des-autorites-etrangeres">
                <strong>CNIL — cloud et autorités étrangères</strong>
                <span>Pourquoi la juridiction du prestataire compte pour les traitements les plus sensibles.</span>
              </a>
            </div>
          </details>
        </div>
      </div>
    </section>
  </main>

  <footer class="footer">
    <div class="shell footer-inner">
      <span>RésoSoin · Le Potager du Web · projet du Verger du Numérique</span>
      <span><a href="/resosoin/questionnaire/confidentialite.php">Confidentialité de l’étude</a> · <a href="mailto:contact@lepotager.org">Contact</a></span>
    </div>
  </footer>
</body>
</html>
