#!/usr/bin/env bash
# Mise à jour ciblée et non destructive des résultats publics du questionnaire V2.
set -euo pipefail
umask 077

REPO="${HOME}/repositories/executive-function-app"
LIVE="${HOME}/public_html/app/app.lepotager.org/adhd-app/v2"
SOURCE="web/app.lepotager.org/adhd-app/v2"
FILES=(public-analytics.php runtime-js/questionnaire-v131.php results.php)

fail() { printf 'ERREUR : %s\n' "$*" >&2; exit 1; }
command -v git >/dev/null || fail 'Git est introuvable.'
command -v php >/dev/null || fail 'PHP est introuvable.'
[[ -d "$REPO" ]] || fail "Clone introuvable : $REPO"
[[ -f "$LIVE/results.php" && -f "$LIVE/_common.php" && -f "$LIVE/index.php" ]] ||
    fail "Site V2 introuvable à l'emplacement attendu : $LIVE. Vérifier le DocumentRoot cPanel."
[[ ! -L "$LIVE/results.php" ]] || fail "Le fichier public est un lien symbolique inattendu."
REMOTE="$(git -C "$REPO" remote get-url origin)"
[[ "$REMOTE" == *'jasmin-abernathy/executive-function-app'* ]] ||
    fail "Le clone ne pointe pas vers le bon dépôt : $REMOTE"

printf '1/4 — Récupération des fichiers corrigés\n'
git -C "$REPO" fetch origin main
HEAD="$(git -C "$REPO" rev-parse origin/main)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
for f in "${FILES[@]}"; do
    mkdir -p "$TMP/$(dirname "$f")"
    git -C "$REPO" show "origin/main:$SOURCE/$f" > "$TMP/$f" ||
        fail "Fichier absent dans origin/main : $f"
    php -l "$TMP/$f" >/dev/null || fail "Syntaxe PHP incorrecte : $f"
done

# Double garde-fou : ni valeur brute, ni ancien rendu de comptes dans la page publique.
grep -Fq 'class="chart-percent"' "$TMP/results.php" ||
    fail "Le fichier distant ne contient pas la version en pourcentages."
grep -Fq 'V2_PUBLIC_MIN_RESPONSES' "$TMP/public-analytics.php" ||
    fail "La protection des petits échantillons est absente."
if grep -Fq '<?=$value?>' "$TMP/results.php" ||
   grep -Fq '<?=$count?>' "$TMP/results.php" ||
   grep -Fq 'Il y a actuellement {$n}' "$TMP/results.php"; then
    fail "Un ancien affichage de nombres bruts est encore présent."
fi

printf '2/4 — Sauvegarde ciblée\n'
STAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP="$HOME/private/app-v2-results-backups/$STAMP"
mkdir -p "$BACKUP"
for f in "${FILES[@]}"; do
    if [[ -f "$LIVE/$f" ]]; then
        mkdir -p "$BACKUP/$(dirname "$f")"
        cp -p "$LIVE/$f" "$BACKUP/$f"
    fi
done

printf '3/4 — Installation limitée aux trois fichiers concernés\n'
for f in "${FILES[@]}"; do
    dest="$LIVE/$f"
    [[ -d "$(dirname "$dest")" ]] || fail "Dossier live absent : $(dirname "$dest")"
    staging="${dest}.deploy-$$"
    install -m 0644 "$TMP/$f" "$staging"
    mv -f "$staging" "$dest"
    cmp -s "$TMP/$f" "$dest" || fail "La copie est différente de la source : $f"
done
php -l "$LIVE/results.php" >/dev/null || fail 'Le fichier public n’est pas valide après installation.'
printf 'Code publié depuis origin/main : %s\n' "$HEAD"
printf 'Sauvegarde : %s\n' "$BACKUP"

printf '4/4 — Vérification HTTP du vrai site\n'
URL="https://app.lepotager.org/adhd-app/v2/results.php?lang=fr&verification=$STAMP"
if command -v curl >/dev/null && curl -fsSL --max-time 20 -H 'Cache-Control: no-cache' "$URL" > "$TMP/live.html"; then
    if grep -Fq 'Résultats publics du questionnaire V2' "$TMP/live.html" &&
       ! grep -Fq 'Il y a actuellement' "$TMP/live.html"; then
        printf 'OK : le serveur renvoie la nouvelle page publique.\n'
    else
        fail "Le site renvoie encore une ancienne page : vérifier le DocumentRoot, le script Git→live et le cache PHP (OPcache)."
    fi
else
    printf 'ATTENTION : contrôle HTTP impossible depuis le serveur ; vérifier manuellement : %s\n' "$URL" >&2
fi
