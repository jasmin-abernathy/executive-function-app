#!/usr/bin/env bash
# Contrôle et publication non destructifs du catalogue app.lepotager.org.
set -Eeuo pipefail
umask 077

MODE="--check"
if (( $# > 0 )); then MODE="$1"; fi
LIVE="$HOME/public_html/app/app.lepotager.org"
if (( $# > 1 )); then LIVE="$2"; fi
REPO="$HOME/repositories/executive-function-app"
SRC="web/app.lepotager.org"

fail() { printf 'ERREUR : %s\n' "$*" >&2; exit 1; }
warn() { printf 'ATTENTION : %s\n' "$*" >&2; }
case "$MODE" in --check|--apply) ;; *) fail "Usage: deploy-catalogue-live.sh [--check|--apply] [document-root]" ;; esac
for executable in git cmp realpath; do
  command -v "$executable" >/dev/null || fail "Commande absente : $executable"
done

[[ -d "$REPO/.git" ]] || fail "Clone Git absent : $REPO"
ORIGIN="$(git -C "$REPO" remote get-url origin)"
[[ "$ORIGIN" == *"jasmin-abernathy/executive-function-app"* ]] || fail "Le clone pointe vers un autre dépôt"
[[ -d "$LIVE" && -f "$LIVE/index.html" ]] || fail "DocumentRoot ou index.html absent : $LIVE (vérifier dans cPanel)"
[[ ! -L "$LIVE" ]] || fail "DocumentRoot est un lien symbolique inattendu"
LIVE="$(realpath "$LIVE")"
[[ "$(basename "$LIVE")" == "app.lepotager.org" ]] || fail "Nom de dossier live inattendu : $LIVE"
[[ "$LIVE" == "$HOME/public_html/"* ]] || fail "Destination hors du webroot attendu : $LIVE"
[[ -d "$LIVE/adhd-app" && -d "$LIVE/resosoin" ]] || fail "Sous-applications attendues absentes : mauvais DocumentRoot possible"

printf 'Clone : %s\n' "$REPO"
printf 'Destination : %s\n' "$LIVE"
printf 'SHA local : %s\n' "$(git -C "$REPO" rev-parse --short HEAD)"
git -C "$REPO" fetch --quiet origin main || fail "Impossible de récupérer origin/main"
SHA="$(git -C "$REPO" rev-parse refs/remotes/origin/main)"
printf 'SHA source GitHub : %s\n' "$SHA"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

MANIFEST="$TMP/manifest"
printf '%s\n' index.html en/index.html assets/app.js assets/style.css assets/project-detail.css sitemap.xml > "$MANIFEST"
git -C "$REPO" ls-tree -r --name-only "$SHA" -- "$SRC/projets" "$SRC/en/projects" |
  sed -n 's#^web/app.lepotager.org/\(projets/[^/]*/index.html\|en/projects/[^/]*/index.html\)$#\1#p' >> "$MANIFEST"

COUNT="$(wc -l < "$MANIFEST")"
[[ "$COUNT" -eq 30 ]] || fail "30 fichiers attendus (accueils, ressources, sitemap, 24 fiches), trouvé : $COUNT"
while IFS= read -r relative; do
  case "$relative" in
    index.html|en/index.html|assets/app.js|assets/style.css|assets/project-detail.css|sitemap.xml|projets/*/index.html|en/projects/*/index.html) ;;
    *) fail "Fichier hors catalogue : $relative" ;;
  esac
  mkdir -p "$TMP/source/$(dirname "$relative")"
  git -C "$REPO" show "$SHA:$SRC/$relative" > "$TMP/source/$relative" || fail "Fichier introuvable dans Git : $relative"
done < "$MANIFEST"

[[ "$(grep -c 'class="project-card-link"' "$TMP/source/index.html")" -eq 12 ]] || fail "Accueil français incorrect dans Git"
[[ "$(grep -c 'class="project-card-link"' "$TMP/source/en/index.html")" -eq 12 ]] || fail "Accueil anglais incorrect dans Git"
grep -Fq 'https://vevak.lepotager.org/' "$TMP/source/index.html" || fail "Lien officiel VeVak manquant"
grep -Fq '.project-card-link' "$TMP/source/assets/style.css" || fail "Styles des cartes manquants"
if command -v node >/dev/null; then node --check "$TMP/source/assets/app.js" || fail "JavaScript invalide"; fi

STALE="$TMP/stale"
: > "$STALE"
while IFS= read -r relative; do
  file="$LIVE/$relative"
  [[ ! -L "$file" ]] || fail "Fichier public lié symboliquement : $file"
  if [[ ! -f "$file" ]] || ! cmp -s "$TMP/source/$relative" "$file"; then
    printf '%s\n' "$relative" >> "$STALE"
  fi
done < "$MANIFEST"
TOTAL_STALE="$(wc -l < "$STALE")"
printf 'Identiques à GitHub : %s / 30\n' "$((30 - TOTAL_STALE))"
printf 'Absents ou obsolètes : %s / 30\n' "$TOTAL_STALE"
if (( TOTAL_STALE > 0 )); then
  sed 's/^/  À synchroniser : /' "$STALE"
fi
if [[ -f "$LIVE/index.php" ]]; then
  warn "index.php existe aussi à la racine : vérifier DirectoryIndex dans cPanel/Apache."
fi
if [[ -f "$LIVE/.htaccess" ]]; then
  grep -Eq '^[[:space:]]*DirectoryIndex[[:space:]]+index.html([[:space:]]|$)' "$LIVE/.htaccess" ||
    warn "La priorité du index.html n'est pas confirmée dans .htaccess."
fi

if [[ "$MODE" == "--check" && "$TOTAL_STALE" -gt 0 ]]; then
  echo "CONCLUSION : la version GitHub et les fichiers publics sont différents."
  echo "Après vérification du DocumentRoot, relancer avec --apply."
  exit 2
fi

if [[ "$MODE" == "--apply" && "$TOTAL_STALE" -gt 0 ]]; then
  BACKUP="$HOME/private/catalogue-deploy-backups/$(date +%Y%m%d-%H%M%S)"
  mkdir -p "$BACKUP"
  # Sauvegarder tous les fichiers existants avant toute écriture.
  while IFS= read -r relative; do
    if [[ -f "$LIVE/$relative" ]]; then
      mkdir -p "$BACKUP/$(dirname "$relative")"
      cp -p "$LIVE/$relative" "$BACKUP/$relative" || fail "Sauvegarde impossible : $relative"
    fi
  done < "$STALE"
  while IFS= read -r relative; do
    destination="$LIVE/$relative"
    mkdir -p "$(dirname "$destination")"
    [[ ! -L "$(dirname "$destination")" ]] || fail "Dossier lié symboliquement : $destination"
    temp_file="$destination.new-$$"
    install -m 0644 "$TMP/source/$relative" "$temp_file" || fail "Copie impossible : $relative"
    mv -f "$temp_file" "$destination" || fail "Installation impossible : $relative"
    cmp -s "$TMP/source/$relative" "$destination" || fail "Comparaison après copie échouée : $relative"
  done < "$STALE"
  echo "Sauvegarde : $BACKUP"
  echo "Copie terminée. Aucun autre répertoire et aucun fichier dynamique modifié."
fi

if command -v curl >/dev/null; then
  HTTP="$TMP/public.html"
  if curl -fsSL --max-time 20 -H 'Cache-Control: no-cache' \
       "https://app.lepotager.org/?catalogue_test=$(date +%s)" > "$HTTP"; then
    if grep -Fq 'class="project-card-link"' "$HTTP" &&
       grep -Fq 'https://vevak.lepotager.org/' "$HTTP"; then
      echo "HTTP : le serveur sert le nouvel accueil."
    else
      warn "Le serveur HTTP sert encore une autre page ou une ancienne version du HTML."
      exit 3
    fi
  else
    warn "Contrôle HTTP impossible. Les fichiers locaux seuls ne prouvent pas la publication."
    exit 4
  fi
else
  warn "curl absent : impossible de contrôler la version HTTP."
  exit 4
fi
