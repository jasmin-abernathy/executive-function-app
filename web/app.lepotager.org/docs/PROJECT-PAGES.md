# Catalogue public du Verger — publication

Le catalogue est composé de deux accueils HTML, 24 fiches détaillées FR/EN et de ressources CSS/JS. Les fiches VeVak, RésoSoin, Librairie universelle et Dendrila Privacy restent disponibles à leur ancienne adresse, mais les cartes principales pointent directement vers leurs pages officielles.

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

Tester depuis le navigateur le retour catalogue, les filtres, les modes mobile et accessible, le basculement FR/EN, les 8 cartes avec fiches locales et les 4 cartes ouvrant des pages dédiées. Comparer aussi le HTML effectivement servi et ne pas se limiter à la présence des fonctionnalités dans GitHub.
