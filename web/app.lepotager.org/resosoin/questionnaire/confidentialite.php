<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="theme-color" content="#4f7a61">
  <title>Confidentialité — étude RésoSoin</title>
  <link rel="icon" href="/favicon.svg" type="image/svg+xml">
  <link rel="stylesheet" href="/resosoin/questionnaire/assets/style.css">
</head>
<body><div class="shell">
<header class="header"><a class="brand" href="/resosoin/">RésoSoin · étude</a><div class="header-links"><a href="./">Questionnaire</a><a href="resultats.php">Résultats</a></div></header>
<main class="card">
<p class="eyebrow">Confidentialité</p><h1>Une étude produit, pas un questionnaire médical.</h1>
<p class="lead">L’objectif est de comprendre les usages et attentes autour des rendez-vous et services numériques de cabinet. Nous évitons volontairement de collecter des données de santé dans cette étude.</p>
<h2>Ce qui est demandé</h2>
<ul><li>Votre profil de réponse : professionnel·le de santé ou patient·e.</li><li>Des choix sur vos usages, préférences, attentes, prix et fonctionnalités.</li><li>Pour les professionnels, la profession réglementée déclarée et le contexte d’exercice, sans identité.</li><li>Pour les patients, une tranche d’âge large facultative à partir de 18 ans.</li></ul>
<h2>Parcours des professionnels de santé</h2>
<p>L’accès au questionnaire professionnel repose sur une déclaration explicite d’exercice d’une profession de santé réglementée. Cette déclaration n’authentifie pas l’identité du répondant et les résultats professionnels sont interprétés comme des réponses déclaratives.</p>
<p>Une consultation facultative de l’Annuaire Santé peut être proposée lorsque la clé API publique nécessaire est configurée :</p>
<ul>
<li>le numéro RPPS est transmis ponctuellement à l’API officielle Annuaire Santé de l’ANS ;</li>
<li>le nom d’exercice est comparé localement avec la fiche renvoyée ;</li>
<li>une concordance confirme seulement l’existence d’une fiche professionnelle publique active correspondant aux informations saisies ; elle ne prouve pas que le répondant en est titulaire ;</li>
<li>le nom et le numéro RPPS ne sont pas enregistrés dans les tables du questionnaire ni intégrés au jeton remis au navigateur ;</li>
<li>la session conserve seulement la méthode de participation : <code>self_declared</code> ou <code>directory_record_matched</code>.</li>
</ul>
<p>L’absence de clé API ou l’échec de cette consultation facultative ne bloque pas le questionnaire : le professionnel peut répondre sur déclaration.</p>
<h2>Ce qui n’est pas demandé</h2>
<ul><li>Nom, prénom, adresse, numéro de sécurité sociale.</li><li>Diagnostic, pathologie, traitement ou motif de consultation.</li><li>Texte libre médical.</li><li>Adresse e-mail dans le questionnaire lui-même.</li></ul>
<h2>Âge et consentement</h2><p>Cette étude est réservée aux personnes de 18 ans ou plus. Votre accord explicite est vérifié avant la création d’une session et la version du questionnaire est enregistrée avec la date de consentement.</p><h2>Sauvegarde et reprise</h2>
<p>Une session pseudonyme est créée avec un jeton aléatoire stocké dans votre navigateur. Le serveur ne stocke qu’une empreinte de ce jeton. Plusieurs brouillons peuvent coexister sur le même navigateur ; sur un appareil partagé, chaque personne doit vérifier le profil avant de reprendre ou supprimer une réponse.</p>
<h2>Durées</h2>
<ul><li>Session commencée mais non envoyée : suppression automatique après 30 jours d’inactivité.</li><li>Réponse envoyée : conservation maximale prévue de 12 mois pour ce cycle de recherche produit.</li></ul>
<h2>Suppression</h2>
<p>Tant que le jeton est présent dans votre navigateur, le bouton « Supprimer mes réponses » efface la session et ses réponses, y compris après l’envoi du questionnaire. Si le jeton est perdu, vous pouvez écrire à contact@lepotager.org ; comme les réponses sont pseudonymes et ne contiennent pas votre identité, il peut être impossible d’identifier avec certitude quelle réponse vous appartient.</p>
<h2>Résultats</h2><p>Les résultats publics sont exploratoires et auto-sélectionnés. Ils ne constituent pas un sondage représentatif. Les répartitions ne sont affichées qu’après un seuil de réponses et les petites cellules sont regroupées.</p><h2>Contact pour un pilote</h2>
<p>Le lien de contact proposé à la fin ouvre votre messagerie. Votre adresse e-mail n’est donc pas enregistrée dans les tables du questionnaire et n’est pas techniquement reliée à vos réponses.</p>
<h2>Hébergement</h2>
<p>Cette étude est prévue sur l’infrastructure française de Le Potager du Web chez o2switch. Elle ne collecte volontairement pas de données médicales. Pour les futures données de santé éventuellement traitées par RésoSoin, le projet prévoit une infrastructure distincte répondant aux exigences applicables, avec hébergement européen et recherche d’une souveraineté juridique renforcée.</p>
<div class="actions"><a class="btn primary" href="./">Retour au questionnaire</a><a class="btn secondary" href="mailto:contact@lepotager.org">contact@lepotager.org</a></div>
</main><footer class="footer"><span>RésoSoin · Le Potager du Web · Metz</span></footer></div></body></html>
