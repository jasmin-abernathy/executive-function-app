#!/usr/bin/env bash
# Synchronisation statique non destructive du catalogue app.lepotager.org.
# Usage: bash scripts/deploy-app-catalogue.sh --check|--apply
set -euo pipefail
umask 077

MODE="${1:---check}"
case "$MODE" in --check|--apply) ;; *) echo "Usage: $0 --check|--apply" >&2; exit 2;; esac

REPO="${APP_REPO:-$HOME/repositories/executive-function-app}"
LIVE="${APP_LIVE:-$HOME/public_html/app/app.lepotager.org}"
SRC="web/app.lepotager.org"
fail() { printf 'ERREUR: %s\n' "$*" >&2; exit 1; }
warn() { printf 'ATTENTION: %s\n' "$*" >&2; }

command -v git >/dev/null || fail "Git introuvable."
command -v cmp >/dev/null || fail "cmp introuvable."
command -v realpath >/dev/null || fail "realpath introuvable."
[[ -d "$REPO/.git" ]] || fail "Clone absent: $REPO (renseigner APP_REPO)."
[[ -d "$LIVE" && -f "$LIVE/index.html" && -f "$LIVE/en/index.html" && -f "$LIVE/assets/style.css" ]] || fail "Dossier public non identifié: $LIVE (vérifier le DocumentRoot cPanel et APP_LIVE)."
[[ ! -L "$LIVE" ]] || fail "Le dossier public est un lien symbolique imprévu."
LIVE="$(cd "$LIVE" && pwd -P)"
HOME_REAL="$(cd "$HOME" && pwd -P)"
[[ "$LIVE" == "$HOME_REAL"/public_html/* && "$(basename "$LIVE")" == "app.lepotager.org" ]] || fail "Destination refusée : $LIVE"
[[ "$LIVE" != "/" && "$LIVE" != "$HOME_REAL" ]] || fail "Destination dangereuse."
REMOTE="$(git -C "$REPO" remote get-url origin)"
[[ "$REMOTE" == *"jasmin-abernathy/executive-function-app"* ]] || fail "Dépôt inattendu : $REMOTE"

echo "Récupération de origin/main sans modifier le checkout serveur..."
git -C "$REPO" fetch --quiet origin main
SHA="$(git -C "$REPO" rev-parse origin/main)"
printf 'Révision GitHub : %s\nDossier cible : %s\n' "$SHA" "$LIVE"

FILES=(index.html en/index.html assets/style.css assets/app.js assets/project-detail.css sitemap.xml)
PAGES=()
while IFS= read -r path; do
  rel="${path#"$SRC/"}"
  case "$rel" in
    projets/*/index.html|en/projects/*/index.html) PAGES+=("$rel");;
  esac
done < <(git -C "$REPO" ls-tree -r --name-only origin/main -- "$SRC/projets" "$SRC/en/projects")
[[ "${#PAGES[@]}" -eq 24 ]] || fail "24 fiches attendues dans Git, ${#PAGES[@]} trouvées."
FILES+=("${PAGES[@]}")

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
mkdir -p "$TMP/staging"
for rel in "${FILES[@]}"; do
  target="$TMP/staging/$rel"
  mkdir -p "$(dirname "$target")"
  git -C "$REPO" show "origin/main:$SRC/$rel" > "$target" || fail "Source Git manquante: $rel"
  [[ -s "$target" ]] || fail "Source Git vide: $rel"
  live_target="$LIVE/$rel"
  safe_target="$(realpath -m "$live_target")"
  [[ "$safe_target" == "$LIVE/"* ]] || fail "Chemin ou lien symbolique dangereux: $rel"
  [[ ! -L "$live_target" ]] || fail "Fichier cible en lien symbolique: $rel"
done

grep -Fq 'class="project-card-link"' "$TMP/staging/index.html" || fail "L'accueil FR Git ne contient pas les cartes cliquables."
grep -Fq 'class="project-card-link"' "$TMP/staging/en/index.html" || fail "L'accueil EN Git ne contient pas les cartes cliquables."
grep -Fq 'href="https://vevak.lepotager.org/"' "$TMP/staging/index.html" || fail "La carte VeVak n'utilise pas son site dédié."
grep -Fq 'project-card-link' "$TMP/staging/assets/style.css" || fail "Le CSS des cartes cliquables manque."
if grep -Fq 'project-expand' "$TMP/staging/assets/app.js"; then fail "Ancien accordéon toujours présent dans le JS Git."; fi
grep -Fq 'href="/projets/dendrila-privacy/"' "$TMP/staging/index.html" || fail "La carte Dendrila Privacy FR ne renvoie pas à sa fiche."
grep -Fq 'href="/en/projects/dendrila-privacy/"' "$TMP/staging/en/index.html" || fail "La carte Dendrila Privacy EN ne renvoie pas à sa fiche."
grep -Fq 'privacy-offers' "$TMP/staging/projets/dendrila-privacy/index.html" || fail "Les offres Dendrila Privacy sont absentes."
grep -Fq '20261010-mobilefix1' "$TMP/staging/projets/dendrila-privacy/index.html" || fail "Le CSS de la fiche Dendrila n'est pas actualisé."
grep -Fq '.privacy-offers .project-card {' "$TMP/staging/assets/project-detail.css" || fail "Le correctif mobile des cartes Dendrila est absent."
for rel in "${PAGES[@]}"; do
  grep -Fq 'detail-section' "$TMP/staging/$rel" || fail "Fiche HTML incomplète: $rel"
done
if command -v node >/dev/null; then
  node --check "$TMP/staging/assets/app.js" >/dev/null || fail "JavaScript du catalogue invalide."
fi

CHANGED=()
for rel in "${FILES[@]}"; do
  if ! cmp -s "$TMP/staging/$rel" "$LIVE/$rel"; then
    CHANGED+=("$rel")
    printf 'DIFFÉRENT OU ABSENT : %s\n' "$rel"
  fi
done
printf 'Comparaison des %d fichiers statiques : %d différents, %d identiques.\n' "${#FILES[@]}" "${#CHANGED[@]}" "$((${#FILES[@]}-${#CHANGED[@]}))"

check_http() {
  if ! command -v curl >/dev/null; then
    warn "curl absent : impossible de vérifier le HTML réellement renvoyé."
    return
  fi
  local stamp http_root http_detail
  stamp="$(date +%s)"
  http_root="https://app.lepotager.org/?_catalogue_check=$stamp"
  http_detail="https://app.lepotager.org/projets/dendrila-privacy/?_catalogue_check=$stamp"
  if curl -fsSL --max-time 20 -H 'Cache-Control: no-cache' "$http_root" -o "$TMP/public-home.html"; then
    if grep -Fq 'class="project-card-link"' "$TMP/public-home.html" && grep -Fq 'href="https://vevak.lepotager.org/"' "$TMP/public-home.html" && grep -Fq 'href="/projets/dendrila-privacy/"' "$TMP/public-home.html"; then
      echo "HTTP accueil : nouveau catalogue détecté."
    else
      warn "HTTP accueil : ANCIEN HTML encore servi (dossier incorrect, cache ou synchronisation non appliquée)."
    fi
  else
    warn "Contrôle HTTP de l'accueil impossible depuis le serveur."
  fi
  if curl -fsSL --max-time 20 -H 'Cache-Control: no-cache' "$http_detail" -o "$TMP/public-detail.html"; then
    if grep -Fq 'privacy-offers' "$TMP/public-detail.html" && grep -Fq 'https://wordpress.org/support/plugin/dendrila-privacy/' "$TMP/public-detail.html"; then
      echo "HTTP fiche Dendrila Privacy : nouvelle page détectée."
    else
      warn "HTTP fiche Dendrila Privacy : contenu inattendu."
    fi
  else
    warn "HTTP fiche Dendrila Privacy : requête impossible ou page absente."
  fi
}

if [[ "$MODE" == "--check" ]]; then
  echo "Mode diagnostic : aucune modification effectuée."
  check_http
  exit 0
fi

if [[ "${#CHANGED[@]}" -eq 0 ]]; then
  echo "Les 30 fichiers du DocumentRoot sont déjà identiques à Git."
  check_http
  exit 0
fi

STAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP="$HOME/private/app-catalogue-backups/$STAMP"
mkdir -p "$BACKUP"
printf 'Sauvegarde avant modification : %s\n' "$BACKUP"
for rel in "${CHANGED[@]}"; do
  target="$LIVE/$rel"
  mkdir -p "$(dirname "$target")"
  if [[ -f "$target" ]]; then
    mkdir -p "$BACKUP/$(dirname "$rel")"
    cp -p "$target" "$BACKUP/$rel"
  fi
  tmp_target="${target}.catalogue-$$.tmp"
  install -m 0644 "$TMP/staging/$rel" "$tmp_target"
  mv -f "$tmp_target" "$target"
  cmp -s "$TMP/staging/$rel" "$target" || fail "Comparaison après copie échouée : $rel"
done
printf 'Copie terminée : %d fichiers corrigés depuis GitHub %s\n' "${#CHANGED[@]}" "$SHA"
echo "Aucun --delete : questionnaires, paiements, configurations privées et autres services non touchés."
check_http
echo "Important : vérifier dans un navigateur les cartes FR/EN, les filtres et les fiches."
