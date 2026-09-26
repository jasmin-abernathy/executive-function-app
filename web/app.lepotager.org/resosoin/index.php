<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="description" content="RésoSoin : étude d’un réseau de cabinets indépendants avec site propre, rendez-vous interopérables, automatisations explicites et contrôle des données.">
  <meta name="theme-color" content="#315c3a">
  <title>RésoSoin — une autre façon de gérer le site et les rendez-vous d’un cabinet</title>
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
      <a class="brand" href="/">
        <span class="brand-mark" aria-hidden="true">R</span>
        <span>RésoSoin <small>· Le Verger du Numérique</small></span>
      </a>
      <nav class="nav" aria-label="Navigation RésoSoin">
        <a href="#principe">Principe</a>
        <a href="#parcours">Parcours</a>
        <a href="#automatisation">Automatisation</a>
        <a href="#donnees">Données</a>
        <a href="#etude">Questionnaires</a>
        <a href="#sources">Sources</a>
      </nav>
    </div>
  </header>

  <main id="contenu">
    <section class="hero">
      <div class="shell hero-grid">
        <div>
          <p class="eyebrow">Projet en étude · santé numérique</p>
          <h1>Et si le cabinet gardait vraiment la main&nbsp;?</h1>
          <p class="lede">RésoSoin, développé à Metz, étudie une organisation différente des plateformes centralisées&nbsp;: chaque cabinet garde son site et son domaine, tandis que les services de rendez-vous et de recherche pourraient être reliés entre eux sans retirer cette autonomie. L’étude sert précisément à déterminer quelles fonctions valent la peine d’être construites.</p>
          <div class="hero-actions">
            <a class="button button-primary" href="/resosoin/questionnaire/?audience=doctor&amp;source=resosoin_page">Je suis professionnel·le de santé</a>
            <a class="button button-secondary" href="/resosoin/questionnaire/?audience=patient&amp;source=resosoin_page">Je suis patient·e</a>
          </div>
          <div class="tag-row" aria-label="Principes de RésoSoin">
            <span class="tag">CMS maison</span>
            <span class="tag">Site indépendant</span>
            <span class="tag">Sans IA imposée</span>
            <span class="tag">Automatisations explicites</span>
            <span class="tag">Hébergement européen visé</span>
            <span class="tag">Open source visé</span>
          </div>
        </div>
        <aside class="hero-card" aria-label="Pourquoi cette étude">
          <strong>On ne veut pas coder la plateforme avant de savoir ce qui compte vraiment.</strong>
          <p class="big">Les questionnaires servent à décider ce que RésoSoin doit devenir — ou ne pas devenir.</p>
          <ul>
            <li>Quels irritants sont réellement importants pour les médecins&nbsp;?</li>
            <li>Quel prix paraît acceptable&nbsp;?</li>
            <li>«&nbsp;Sans IA&nbsp;» est-il un vrai critère de choix&nbsp;?</li>
            <li>Les patients veulent-ils une app, ou seulement un site simple&nbsp;?</li>
          </ul>
        </aside>
      </div>
    </section>

    <section id="principe" class="section section-soft">
      <div class="shell">
        <div class="section-heading">
          <p class="eyebrow">Le principe testé</p>
          <h2>Un réseau de cabinets indépendants, pas une plateforme qui possède toute la relation.</h2>
          <p>Chaque cabinet conserve son propre site et son propre domaine. «&nbsp;Décentralisé mais interopérable&nbsp;» signifie ici qu’un patient pourrait accéder à plusieurs cabinets depuis un point d’entrée commun, tandis que chaque cabinet garderait sa présence web, ses choix d’outils et la possibilité de partir avec ses données. Cette interopérabilité reste à prototyper et à tester.</p>
        </div>
        <div class="grid-3">
          <article class="card">
            <div class="card-icon" aria-hidden="true">1</div>
            <h3>Un CMS maison léger</h3>
            <p>Le socle actuel utilise un CMS maison minimal, conçu spécifiquement pour les sites de cabinets, les équipes multi-praticiens et la réversibilité.</p>
          </article>
          <article class="card">
            <div class="card-icon" aria-hidden="true">2</div>
            <h3>Le site reste celui du cabinet</h3>
            <p>Nom de domaine, informations publiques et identité du cabinet restent séparables de l’agenda. Quitter un service ne devrait pas signifier perdre sa présence web.</p>
          </article>
          <article class="card">
            <div class="card-icon" aria-hidden="true">3</div>
            <h3>Le sensible reste séparé</h3>
            <p>Le site public et son CMS ne doivent pas contenir de dossier patient. Les rendez-vous, documents et futures données de santé utilisent une couche distincte adaptée à leur niveau de sensibilité.</p>
          </article>
        </div>
      </div>
    </section>

    <section id="parcours" class="section">
      <div class="shell">
        <div class="section-heading">
          <p class="eyebrow">Parcours visé</p>
          <h2>La valeur ne dépend pas d’une copie miniature de Doctolib.</h2>
          <p>RésoSoin est étudié comme un socle pour relier des cabinets autonomes. Les éléments ci-dessous distinguent ce qui existe déjà de ce qui doit encore être construit et validé.</p>
        </div>
        <div class="grid">
          <article class="card">
            <h3>Côté patient</h3>
            <p><span class="tag">Disponible aujourd’hui</span> découvrir le projet et répondre à l’étude.</p>
            <p><span class="tag">À prototyper</span> trouver le site du cabinet, prendre ou modifier un rendez-vous simplement, recevoir un rappel facultatif.</p>
            <p><span class="tag">Projet à tester</span> rechercher plusieurs cabinets indépendants depuis un point d’entrée commun sans imposer un compte central.</p>
          </article>
          <article class="card">
            <h3>Côté équipe</h3>
            <p><span class="tag">Socle existant</span> CMS léger avec site et domaine propres au cabinet.</p>
            <p><span class="tag">À prototyper</span> gérer plusieurs agendas et automatiser confirmations, rappels ou créneaux libérés avec des règles explicites.</p>
            <p><span class="tag">Exigence de conception</span> pouvoir exporter les données utiles et changer de solution sans perdre la présence web du cabinet.</p>
          </article>
        </div>
        <div class="notice" style="margin-top:1rem;color:#202622">
          <strong>Les objections font partie de l’étude.</strong>
          <span>Visibilité des cabinets, migration, formation, support, continuité de service, compatibilité avec le secrétariat et coût total sont mesurés au lieu d’être présentés comme déjà résolus.</span>
        </div>
      </div>
    </section>

    <section id="automatisation" class="section">
      <div class="shell">
        <div class="automation-callout">
          <div class="automation-symbol" aria-hidden="true">≠</div>
          <div>
            <p class="eyebrow">Point important</p>
            <h2>Absence d’IA ≠ absence d’automatisation.</h2>
            <p class="lede">On peut automatiser énormément de tâches avec des règles simples, explicites et prévisibles, sans envoyer le contenu d’une consultation à un modèle d’IA.</p>
            <div class="automation-list" aria-label="Exemples d’automatisation sans IA">
              <span>Confirmations</span>
              <span>Rappels</span>
              <span>Liste d’attente</span>
              <span>Créneaux libérés</span>
              <span>Horaires récurrents</span>
              <span>Synchronisation calendrier</span>
              <span>Notifications</span>
              <span>Statistiques simples</span>
            </div>
          </div>
        </div>
        <div class="grid" style="margin-top:1rem">
          <article class="card">
            <h3>Ce que « sans IA imposée » veut dire</h3>
            <p>Le fonctionnement essentiel du produit ne dépend pas d’un modèle génératif. Une éventuelle fonction d’IA future devrait être identifiable, facultative et désactivable, plutôt qu’invisible dans le parcours.</p>
          </article>
          <article class="card">
            <h3>Ce que cela ne veut pas dire</h3>
            <p>RésoSoin n’a pas vocation à être un agenda manuel des années 2000. Les tâches répétitives qui peuvent être décrites par des règles peuvent être automatisées sans IA.</p>
          </article>
        </div>
      </div>
    </section>

    <section id="donnees" class="section section-warm">
      <div class="shell">
        <div class="section-heading">
          <p class="eyebrow">Données & souveraineté</p>
          <h2>Le lieu du serveur ne suffit pas&nbsp;: la juridiction du prestataire compte aussi.</h2>
          <p>Doctolib indique stocker les données en France et en Allemagne, recourir à Amazon Web Services et disposer d’un hébergement certifié HDS en France. La question étudiée par RésoSoin est différente&nbsp;: pour les données les plus sensibles, privilégier si possible un prestataire soumis exclusivement au droit européen afin de réduire l’exposition à des demandes d’accès fondées sur un droit extra-européen.</p>
        </div>
        <div class="compare">
          <article class="card">
            <h3>Objectif RésoSoin</h3>
            <ul class="checklist">
              <li>Hébergement des données en Europe.</li>
              <li>Pour les données de santé&nbsp;: prestataire adapté aux exigences HDS.</li>
              <li>Préférence pour une entité soumise au droit européen pour les données sensibles.</li>
              <li>Minimisation&nbsp;: ne pas collecter ce qui n’est pas nécessaire.</li>
              <li>Séparation technique entre site public et données privées.</li>
            </ul>
          </article>
          <article class="card">
            <h3>Ce qu’on ne prétend pas encore</h3>
            <p>RésoSoin n’est pas présenté comme « plus sécurisé que Doctolib » tant que l’infrastructure n’a pas été finalisée et auditée. Doctolib indique chiffrer les données, conserver ses clés en France et n’avoir reçu, à sa connaissance, aucune demande Cloud Act concernant les données hébergées sur AWS. Le différenciateur testé ici est donc la réduction de l’exposition juridique extra-européenne, pas l’affirmation d’un transfert connu vers les autorités américaines.</p>
          </article>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="shell">
        <div class="section-heading">
          <p class="eyebrow">Prix</p>
          <h2>Tester un modèle nettement moins cher avant de fixer un tarif.</h2>
          <p>À titre de repère, Doctolib affiche actuellement 149&nbsp;€ TTC par mois et par soignant pour «&nbsp;Agenda & prise de RDV&nbsp;». RésoSoin veut tester si un socle beaucoup plus ciblé peut être viable à un tarif nettement inférieur, sans empiler des fonctions inutiles.</p>
        </div>
        <div class="grid">
          <article class="fact-card">
            <strong>149 € / mois / soignant</strong>
            <p class="muted">Tarif public affiché par Doctolib pour Agenda & prise de RDV au moment de cette étude.</p>
          </article>
          <article class="fact-card">
            <strong>Le bon prix reste à trouver</strong>
            <p class="muted">Le questionnaire médecins teste plusieurs fourchettes au lieu d’imposer dès maintenant un prix décidé en interne.</p>
          </article>
        </div>
      </div>
    </section>

    <section id="etude" class="section section-dark">
      <div class="shell">
        <div class="section-heading">
          <p class="eyebrow">Étude RésoSoin</p>
          <h2>Deux questionnaires courts, avec une durée réelle encore en cours de mesure.</h2>
          <p>Les premières questions portent sur vos usages réels. Le concept RésoSoin n’est présenté qu’ensuite afin de réduire l’effet de présentation. Les résultats resteront exploratoires et auto-sélectionnés.</p>
        </div>
        <div class="audience-grid">
          <article class="audience-card">
            <p class="eyebrow">Professionnels</p>
            <h3>Médecins et autres professionnels de santé</h3>
            <ul>
              <li>Profession réglementée déclarée, sans identité demandée.</li>
              <li>Solutions actuelles, irritants et taille de la structure.</li>
              <li>Automatisations, critères de choix et place de l’IA.</li>
              <li>Fonctions prioritaires, frein principal et prix indicatif.</li>
            </ul>
            <a class="button button-primary" href="/resosoin/questionnaire/?audience=doctor&amp;source=resosoin_page">Répondre côté professionnel</a>
          </article>
          <article class="audience-card">
            <p class="eyebrow">Patients</p>
            <h3>Personnes qui prennent des rendez-vous de santé</h3>
            <ul>
              <li>Habitudes de prise de rendez-vous et disponibilité.</li>
              <li>Difficultés concrètes, compte et accessibilité.</li>
              <li>Automatisations, critères de choix et place de l’IA.</li>
              <li>Recherche commune, freins et application facultative.</li>
            </ul>
            <a class="button button-primary" href="/resosoin/questionnaire/?audience=patient&amp;source=resosoin_page">Répondre côté patient</a>
          </article>
        </div>
        <div class="notice" style="margin-top:1rem;color:#202622">
          <strong>Vie privée de l’étude</strong>
          <span>18 ans et plus. Pas de nom, pas de diagnostic, pas de traitement, pas de texte libre médical. Les réponses sont pseudonymes, peuvent être reprises sur le même navigateur et supprimées depuis le questionnaire.</span>
        </div>
        <p style="margin-top:1rem"><a href="/resosoin/questionnaire/invitation.php">Lien d’invitation neutre pour partager l’étude sans présenter RésoSoin auparavant →</a></p>
      </div>
    </section>

    <section id="sources" class="section">
      <div class="shell">
        <div class="section-heading">
          <p class="eyebrow">Sources & nuances</p>
          <h2>Comparer sans raconter n’importe quoi.</h2>
          <p>Les arguments du projet doivent rester vérifiables. Les liens ci-dessous expliquent les chiffres et le point juridique sur l’hébergement.</p>
        </div>
        <div class="source-list">
          <article class="source-card">
            <a href="https://info.doctolib.fr/solution/gestionnaire-de-taches/page/2/">Tarifs publics Doctolib Pro</a>
            <small>Agenda & prise de RDV affiché à 149 €/mois TTC par soignant ; fonctions IA proposées séparément dans la suite.</small>
          </article>
          <article class="source-card">
            <a href="https://info.doctolib.fr/securite/">Doctolib — sécurité et hébergement</a>
            <small>Doctolib indique un stockage en France et en Allemagne et explique son recours à Amazon Web Services.</small>
          </article>
          <article class="source-card">
            <a href="https://community.doctolib.fr/t/on-repond-a-vos-questions-sur-la-securite-des-donnees/107903">Doctolib Communauté — AWS et Cloud Act</a>
            <small>Doctolib indique qu’aucune demande de ce type n’avait été reçue et précise que les clés de chiffrement sont conservées en France.</small>
          </article>
          <article class="source-card">
            <a href="https://www.cnil.fr/fr/cloud-les-risques-dune-certification-europeenne-permettant-lacces-des-autorites-etrangeres">CNIL — cloud et accès d’autorités étrangères</a>
            <small>La CNIL rappelle que, pour les données les plus sensibles, la juridiction du prestataire est un critère distinct de la localisation physique des serveurs.</small>
          </article>
        </div>
      </div>
    </section>
  </main>

  <footer class="footer">
    <div class="shell footer-inner">
      <span>RésoSoin · projet du Verger du Numérique / Le Potager du Web · Metz</span>
      <span><a href="/resosoin/questionnaire/confidentialite.php">Confidentialité de l’étude</a> · <a href="mailto:contact@lepotager.org">Contact</a></span>
    </div>
  </footer>
</body>
</html>
