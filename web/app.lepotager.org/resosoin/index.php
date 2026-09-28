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
  <link rel="stylesheet" href="/resosoin/assets/style.css">
  <link rel="stylesheet" href="/accessibility.css">
  <script defer src="/accessibility.js"></script>
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
        <a class="button button-small button-primary" href="#etude">Participer</a>
      </nav>
    </div>
  </header>

  <main id="contenu">
    <section class="hero" id="projet">
      <div class="shell hero-grid">
        <div class="hero-copy">
          <p class="eyebrow">RésoSoin · projet en étude</p>
          <h1>Le cabinet garde son site. Les rendez-vous deviennent plus simples.</h1>
          <p class="lede">RésoSoin étudie un outil de rendez-vous et d’organisation pour des cabinets indépendants : une présence web à leur nom, un agenda adapté à leur équipe et des automatismes utiles. Le projet n’est pas encore un service de rendez-vous ouvert au public.</p>

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
          <h2>Moins de friction pour l’équipe comme pour les patients.</h2>
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
          <h2 id="fonctionnement-title">Le site public reste simple. Les données sensibles restent ailleurs.</h2>
          <p class="section-intro">Le socle web du cabinet peut rester léger. Les rendez-vous, documents et futures données de santé doivent vivre dans une couche séparée, adaptée à leur sensibilité. L’hébergement final et les intégrations ne sont pas encore arrêtés.</p>

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
          <h3>Avant de construire davantage, on veut vérifier quatre choses.</h3>
          <ul class="question-list">
            <li>Quels irritants font vraiment perdre du temps aux équipes ?</li>
            <li>Quelles intégrations sont indispensables au quotidien ?</li>
            <li>Qu’est-ce qui bloque ou complique la prise de rendez-vous côté patient ?</li>
            <li>Quel coût permet un service durable sans faire payer des fonctions inutiles ?</li>
          </ul>
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

            <div class="price-ranges" aria-label="Tranches proposées dans le questionnaire professionnel">
              <span>Moins de 30 €</span>
              <span>30 à 49 €</span>
              <span>50 à 69 €</span>
              <span>70 à 99 €</span>
              <span>100 à 149 €</span>
              <span>150 € ou plus</span>
            </div>
            <p class="price-footnote">Une réponse « Impossible à estimer à ce stade » est également proposée. Il s’agit d’ordres de grandeur, pas d’un tarif annoncé. Les SMS et la mise en service pourraient être facturés à part.</p>

            <div class="value-note">
              <strong>Un coût plus accessible ne doit pas vouloir dire un service au rabais.</strong>
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
