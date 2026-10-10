# Catalogue public du Verger — publication

Le catalogue est composé de deux accueils HTML, 24 fiches détaillées FR/EN et de ressources CSS/JS. Les cartes VeVak, RésoSoin et Librairie universelle pointent vers leurs sites officiels. Dendrila Privacy renvoie vers sa fiche FR/EN enrichie de trois cartes : gratuit, Studio (prototype multi-sites avancé, prix envisagé 99 €/an pour 10 sites), Agence (fonctions encore à développer, prix envisagé 199 €/an pour 50 sites). Aucune offre Solo, aucune souscription active. Les pages indiquent les liens de demandes GitHub et de support WordPress.org.

## Diagnostic et synchronisation o2switch

**Fusionner GitHub n'est pas déployer sur o2switch.** Ce dépôt ne possède pas de workflow GitHub Actions publiant le site ; les pages sont servies depuis le DocumentRoot et non depuis le clone Git serveur.

Depuis une session SSH o2switch :

```bash
cd "$HOME/repositories/executive-function-app"
git fetch origin main
git show origin/main:scripts/deploy-app-catalogue.sh | bash -s -- --check
```

Ce diagnostic compare **30 fichiers** du catalogue entre `origin/main` et le dossier public, contrôle la présence du HTML attendu, puis teste les réponses HTTP du site sans écrire aucun fichier. Si le chemin cPanel diffère de `$HOME/public_html/app/app.lepotager.org`, définir `APP_LIVE` avec le vrai DocumentRoot avant la commande.

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

Le test garantit que les accueils FR/EN comportent chacun 12 cartes et destinations, que les 24 fiches existent, que les routes, CSS et JS sont liés, et que le sitemap répertorie des fiches. Ce test n'est **pas** une preuve de déploiement.

## À vérifier après publication

Tester depuis le navigateur le retour catalogue, les filtres, les modes mobile et accessible, le basculement FR/EN, les 9 cartes avec fiches locales et les 3 cartes ouvrant des services dédiés. Comparer aussi le HTML effectivement servi et ne pas se limiter à la présence des fonctionnalités dans GitHub.

## Correctif mobile Dendrila Privacy du 10 octobre 2026

Le catalogue utilise volontairement des cartes compactes en **grille à deux colonnes sur mobile**. Réutiliser la classe `project-card` sur la fiche Dendrila forçait les listes et prix dans une colonne d'environ 46 px : texte vertical illisible.

La feuille `assets/project-detail.css` rétablit **le flux flex vertical uniquement pour les cartes `.privacy-offers .project-card`**, garde une carte par ligne sur téléphone et réorganise l'en-tête du projet sur petit écran. Les deux fiches FR/EN utilisent une version CSS distincte pour éviter un cache ancien. Contrôler la largeur du contenu à 320, 375, 390, 430, 768 et 1024 px, et ne pas confondre déploiement Git avec HTML/CSS réellement servis par o2switch.

## Statuts et validation Dendrila Privacy

- Gratuit : version publiée 0.0.5 (dépôt dendrila-privacy).
- Studio : prototype plus avancé (dendrila-monitor, branche feature/monitor-remote-link), réception HTTPS signée, tableau de bord, regroupements et CSV. Tests de bout en bout sur plusieurs WordPress indépendants requis.
- Agence : le socle Studio existe, mais les rapports, droits délégués, comparaison des environnements et licences restent à développer.
- Prix de travail seulement : Studio 99 €/an pour 10 sites, Agence 199 €/an pour 50 sites, selon docs/OFFRES-AGENCES.md. La surveillance locale et les alertes locales restent prévues gratuitement.
- Le déploiement depuis GitHub vers le DocumentRoot o2switch doit être vérifié indépendamment avec le script de diagnostic, puis une comparaison du HTML public.
