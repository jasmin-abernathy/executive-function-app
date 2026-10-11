# Catalogue public du Verger — publication

Le catalogue est composé de deux accueils HTML, 38 fiches détaillées FR/EN et de ressources CSS/JS. Les cartes VeVak, RésoSoin, Librairie universelle et Atelier EPUB pointent vers leurs sites officiels. Dendrila Privacy renvoie vers sa fiche FR/EN enrichie de trois cartes : gratuit, Studio (prototype multi-sites avancé, prix envisagé 99 €/an pour 10 sites), Agence (fonctions encore à développer, prix envisagé 199 €/an pour 50 sites). Aucune offre Solo, aucune souscription active. Les pages indiquent les liens de demandes GitHub et de support WordPress.org.

## Diagnostic et synchronisation o2switch

**Fusionner GitHub n'est pas déployer sur o2switch.** Ce dépôt ne possède pas de workflow GitHub Actions publiant le site ; les pages sont servies depuis le DocumentRoot et non depuis le clone Git serveur.

Depuis une session SSH o2switch :

```bash
cd "$HOME/repositories/executive-function-app"
git fetch origin main
git show origin/main:scripts/deploy-app-catalogue.sh | bash -s -- --check
```

Ce diagnostic compare **44 fichiers** du catalogue entre `origin/main` et le dossier public, contrôle la présence du HTML attendu, puis teste les réponses HTTP du site sans écrire aucun fichier. Si le chemin cPanel diffère de `$HOME/public_html/app/app.lepotager.org`, définir `APP_LIVE` avec le vrai DocumentRoot avant la commande.

Après lecture du diagnostic, pour effectuer la synchronisation ciblée :

```bash
cd "$HOME/repositories/executive-function-app"
git fetch origin main
git show origin/main:scripts/deploy-app-catalogue.sh | bash -s -- --apply
```

La commande en mode `--apply` sauvegarde les fichiers qui vont changer sous `~/private/app-catalogue-backups/`, puis les copie avec vérification octet par octet. Elle ne supprime **aucun fichier** et ne modifie pas les dossiers PHP `adhd-app/`, `resosoin/`, `research/` et `soutenir/` ni les données, secrets, `.htaccess` ou paiements.

Elle ne compare **pas seulement** le SHA du clone : si `origin/main` est déjà à jour mais que le DocumentRoot est obsolète, elle recopie les fichiers manquants/différents. Après copie, elle contrôle le HTML public avec cache-busting. Un cache intermédiaire, un autre DocumentRoot ou une erreur HTTP peuvent encore empêcher le nouveau rendu ; un succès Git ou une copie locale ne suffisent pas à déclarer le site corrigé.

## Validation de code

```bash
node --test tests/catalogue-structure.test.mjs
bash -n scripts/deploy-app-catalogue.sh
```

Le test garantit que les accueils FR/EN comportent chacun 19 cartes et destinations, que les 38 fiches existent, que les routes, CSS et JS sont liés, et que le sitemap répertorie des fiches. Ce test n'est **pas** une preuve de déploiement.

## À vérifier après publication

Tester depuis le navigateur le retour catalogue, les filtres, les modes mobile et accessible, le basculement FR/EN, les 15 cartes ouvrant des fiches locales et les 4 cartes ouvrant des services dédiés. Comparer aussi le HTML effectivement servi et ne pas se limiter à la présence des fonctionnalités dans GitHub.

## Correctif mobile Dendrila Privacy du 10 octobre 2026

Le catalogue utilise volontairement des cartes compactes en **grille à deux colonnes sur mobile**. Réutiliser la classe `project-card` sur la fiche Dendrila forçait les listes et prix dans une colonne d'environ 46 px : texte vertical illisible.

La feuille `assets/project-detail.css` rétablit **le flux flex vertical uniquement pour les cartes `.privacy-offers .project-card`**, garde une carte par ligne sur téléphone et réorganise l'en-tête du projet sur petit écran. Les deux fiches FR/EN utilisent une version CSS distincte pour éviter un cache ancien. Contrôler la largeur du contenu à 320, 375, 390, 430, 768 et 1024 px, et ne pas confondre déploiement Git avec HTML/CSS réellement servis par o2switch.

## Statuts et validation Dendrila Privacy

- Gratuit : version publiée 0.0.5 (dépôt dendrila-privacy).
- Studio : prototype plus avancé (dendrila-monitor, branche feature/monitor-remote-link), réception HTTPS signée, tableau de bord, regroupements et CSV. Tests de bout en bout sur plusieurs WordPress indépendants requis.
- Agence : le socle Studio existe, mais les rapports, droits délégués, comparaison des environnements et licences restent à développer.
- Prix de travail seulement : Studio 99 €/an pour 10 sites, Agence 199 €/an pour 50 sites, selon docs/OFFRES-AGENCES.md. La surveillance locale et les alertes locales restent prévues gratuitement.
- Le déploiement depuis GitHub vers le DocumentRoot o2switch doit être vérifié indépendamment avec le script de diagnostic, puis une comparaison du HTML public.

## Familles de produits et Dendrila Forms — 10 octobre 2026

Les anciens tags (autonomie, sécurité, collectifs, services, culture) ont été remplacés par quatre **familles exclusives** : WordPress, gestion de sites, applications et autres. Les pages FR/EN affichent les sections même sans JavaScript, et les filtres ne cachent plus arbitrairement les dernières cartes sur mobile. Toutes les fiches locales reprennent une famille unique.

**Dendrila Forms** apparaît dans « Applis web » avec ses fiches française et anglaise et ses liens vers le dépôt et ses issues GitHub. État : **0.1.0-alpha, démonstration technique**, exclusivement avec des données fictives. Ne pas le présenter comme prêt à recueillir des données personnelles/sensibles ou à gérer des entretiens privés.

La synchronisation attend **19 cartes par langue, 38 fiches FR/EN et 44 fichiers statiques**. Appliquer le diagnostic et la copie ciblée décrits plus haut ; contrôler ensuite la présence de « Dendrila Forms » et `data-project-group="sites"` dans le HTML réellement servi par le domaine.

## Plugins WordPress en développement — 11 octobre 2026

La famille WordPress regroupe **7 projets** : Dendrila Privacy (0.0.5 publié sur WordPress.org) et six chantiers présentés en français et en anglais :

- Dendrila Simple Edit : privé, PRs de développement, accès éditorial délégué, essais sur WordPress réels à faire.
- Dendrila Forms pour WordPress : public, 0.1.0-alpha, **pas de collecte réelle de données personnelles**.
- Dendrila Form Sentinel : privé, 0.1.0-alpha, références de formulaires uniquement, **pas de test de soumission/réception**.
- Dendrila Accessibility Assistant : public, 0.1.0-alpha, **pas de certification RGAA/WCAG**, couverture partielle explicitée.
- Dendrila Age Assurance : privé, fondation en PR, **médias directement exposés non protégés**.
- Dendrila Monitor : privé, prototypes en PR ; **aucune offre Solo payante**, Studio/Agence non commercialisés.

Le moteur PHP « Dendrila Forms » indépendant reste classé dans **Applis web** et possède sa fiche distincte de l'extension WordPress. Les fiches ne créent des liens GitHub publics que lorsque les dépôts le sont. Les dépôts privés renvoient vers le contact du Potager.

**Invariants :** 19 cartes par langue, 7 WordPress, 3 gestion de sites, 4 applications, 5 applis web, 38 fiches et 44 fichiers statiques synchronisés. Contrôler en production le HTML servant « Dendrila Accessibility Assistant » et une fiche FR/EN. GitHub et le site public peuvent être désynchronisés.

## Reclassement « Applis web » et Atelier EPUB en ligne — 11 octobre 2026

La rubrique **« Autres » a été supprimée** du catalogue visible et des filtres. Les familles publiques sont désormais : **WordPress** (7), **Gestion de sites** (3), **Applications** (4) et **Applis web** (5). Un projet a une famille unique, quelle que soit sa maturité.

- **Applications** : VeVak, Fonctions exécutives / TDAH, Resilience Vault et **Verger Associations** (PWA v0.3, v0.4 en préparation, adaptation Android Capacitor). Le prototype Webxdc 0.2 de ce dernier n'est plus présenté comme son architecture principale.
- **Applis web** : Communication libre (laboratoire, non déployé), Atelier EPUB (web en ligne, développement actif), Librairie universelle (alpha web publique), Sans Effort (projet de comparateur web non lancé) et Dendrila Forms (moteur PHP expérimental auto-hébergeable, données fictives uniquement).
- **Atelier EPUB** : la carte ouvre directement `https://epub-gen.lepotager.org/`, comme VeVak et la Librairie pour leurs sites dédiés. Les fiches locales FR/EN sont conservées pour documenter le développement et contiennent aussi le lien public, le dépôt communautaire et les limites connues. Le déploiement public a été documenté dans `jasmin-abernathy/atelier-epub`, sans que l'existence d'un site signifie que toutes ses fonctions ont été validées.

**Tests à préserver** : aucun filtre/groupe/carte `autres`, 19 cartes par langue (7/3/4/5), 38 fiches FR/EN, 44 fichiers statiques sans suppression, liens vers les services dédiés. Le rendu public Git→o2switch doit être revérifié ; un commit GitHub réussi n'est pas une preuve de synchronisation du DocumentRoot.
