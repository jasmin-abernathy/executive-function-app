# Vérification SEO et UX des trois vitrines — 26 novembre 2026

Périmètre : `https://app.lepotager.org/`, `https://dendrila.lepotager.org/` et `https://retrocms.lepotager.org/`. Ce contrôle complète les travaux sur Simple Edit, les thèmes WordPress et Dendrila Retro. Il ne mesure pas les positions dans Google ni le trafic organique.

## Résultats HTTP et indexation

Les URL ont été reprises dans les trois sitemaps HTTPS actuels, puis interrogées directement. Les liens et ressources supplémentaires situés sur ces mêmes domaines ont été vérifiés par requêtes HEAD, sans télécharger les archives de distribution.

| Contrôle | Résultat observé |
| --- | --- |
| Pages du sitemap app | 46 sur 46 répondent HTTP 200 |
| Pages du sitemap dendrila | 18 sur 18 répondent HTTP 200 |
| Pages du sitemap retrocms | 6 sur 6 répondent HTTP 200 |
| Autres cibles internes liées depuis ces pages | 53 sur 53 répondent HTTP 200, sans redirection |
| robots.txt | Les trois fichiers autorisent l’exploration et indiquent leur sitemap HTTPS |
| Pages en `noindex` dans les sitemaps | Aucune |
| Titres et descriptions des 70 pages | Présents |
| Canonique différente de l’URL de sitemap | Aucune |
| Canonique absente avant correction | Une : le questionnaire ADHD V2 |
| Balises `hreflang` | Présentes sur 48 pages ; aucune relation non réciproque détectée parmi les pages contrôlées |
| JSON-LD | 50 scripts sur 47 pages ; aucun JSON invalide |
| Images sans attribut `alt` dans les 70 documents | Aucune |
| Ancres internes vérifiables entre les pages contrôlées | Aucune cible manquante détectée |

Le sitemap Retro référence bien les six pages FR/EN, dont `/en/legal/`. L’ancienne URL supposée `/en/legal-notice/` n’est pas utilisée par ce sitemap. Les pages de démonstration ne sont pas ajoutées à ce groupe de six pages éditoriales.

Ces résultats concernent les liens internes rencontrés, pas chaque URL historique du serveur ni les sites externes. La validité syntaxique du JSON-LD ne constitue pas une garantie d’affichage de résultats enrichis.

## Correction apportée dans ce lot

Dans `web/app.lepotager.org/adhd-app/v2/index.php`, ajout d’une unique canonique absolue :

```html
<link rel="canonical" href="https://app.lepotager.org/adhd-app/v2/">
```

Elle correspond à l’URL du sitemap et évite de traiter les paramètres de reprise ou de langue comme autant d’URL canoniques différentes.

Le contrôle initial du HTML ne trouvait pas de H1 sur ce questionnaire. Après exécution du JavaScript local, un H1 est bien rendu : « Help decide how the app should actually work ». Aucune modification du titre visible ou de la logique du questionnaire n’a donc été nécessaire.

Le questionnaire RésoSoin contient plusieurs titres dans ses différents états de formulaire. Leur présence dans le HTML brut n’a pas été interprétée comme plusieurs titres simultanément visibles.

Validation du correctif : syntaxe PHP valide avec PHP 8.4.27, une seule canonique trouvée, URL identique à celle du sitemap, et `git diff --check` sans erreur. Aucun test de réponse au questionnaire ni aucune soumission n’ont été effectués.

## Contrôle mobile et interface

Les sources locales ont été rendues dans Chromium à **390 × 844 pixels**, avec la réduction des animations activée. Les fichiers ont été servis directement depuis les copies de travail au navigateur ; aucune réponse de formulaire n’a été transmise.

| Page contrôlée | Largeur de document | Images cassées | Erreurs JavaScript |
| --- | ---: | ---: | ---: |
| Accueil app | 390 px | 0 | 0 |
| Fiche Simple Edit sur app | 390 px | 0 | 0 |
| Accueil questionnaire ADHD V2 | 390 px | 0 | 0 |
| Accueil dendrila | 390 px | 0 | 0 |
| Fiche Simple Edit sur dendrila | 390 px | 0 | 0 |
| Accueil retrocms | 390 px | 0 | 0 |
| Installation retrocms | 390 px | 0 | 0 |

Aucun champ visible sans libellé ni bouton visible sans nom n’a été détecté dans ces sept pages. Les trois accueils ont aussi fait l’objet d’une inspection visuelle : texte et boutons restent lisibles dans la largeur de l’écran. Les cartes horizontales de l’accueil app restent dans leurs zones défilantes et ne créent pas de débordement horizontal de la page.

Ce contrôle limité ne remplace pas un audit complet de conformité d’accessibilité, un test de lecteur d’écran ou une mesure des Core Web Vitals.

## Démonstrations Retro : copie locale à distinguer de la production

La copie locale initiale de `retrosite/retrocms/demo/` était incomplète au moment du rendu. Les trois CSS suivants y étaient absents :

- `demo/assets/oldweb-fixed-portal.css` ;
- `demo/borne-02/assets/borne-02/borne.css` ;
- `demo/signal-96/assets/signal-96/signal.css`.

Des images de Borne 02 et Signal 96 manquaient aussi dans cette copie. Le document Signal 96 occupait alors 768 pixels à une largeur de fenêtre de 390 pixels, sans sa feuille de style. Ces résultats portent sur cette copie locale incomplète, pas sur des erreurs HTTP observées dans les démonstrations en production. Les exports régénérés doivent être vérifiés dans le lot de livraison Retro.

Après les contrôles HTTPS, les navigations Chromium distantes ont reçu une page o2switch `HTTP 429 — Slow down`. Les requêtes distantes ont été arrêtées et les contrôles visuels ont continué localement. Cette réponse n’a pas été transformée en diagnostic de lien cassé ou en défaut du code des vitrines.

## Mise en ligne du correctif questionnaire

Le correctif du questionnaire est présent dans la source de ce lot. Le script `scripts/deploy-app-catalogue.sh` a été complété pour inclure **uniquement** `adhd-app/v2/index.php` dans sa liste explicite. Il exige que ce point d’entrée existe déjà en production, vérifie les chemins et les liens symboliques, prépare les fichiers avant toute copie, puis sauvegarde l’ancienne version avant son remplacement atomique. Les réponses, réglages, secrets et autres fichiers du questionnaire restent hors de cette liste. Le nombre de fichiers affiché est calculé à partir de la liste réelle.

`bash -n scripts/deploy-app-catalogue.sh` et le contrôle syntaxique du PHP passent. Le déploiement pourra utiliser le parcours existant `--check` puis `--apply` depuis le compte o2switch. Ce sous-lot n’a publié aucun fichier et n’a modifié aucune donnée de questionnaire.

## Données Search Console disponibles

Le connecteur GSC Wizard refuse les lectures parce que son essai est terminé. La connexion Windsor.ai possède la propriété `https://app.lepotager.org/` ; les propriétés `dendrila` et `retrocms` ne figurent pas dans sa liste. Sur la période demandée du 27 octobre au 23 novembre 2026, la lecture quotidienne renvoie 24 lignes du 31 octobre au 23 novembre, toutes avec zéro clic et zéro impression. La lecture par page ne renvoie aucune ligne. Il n’est donc pas possible de classer les pages par opportunité ni de revendiquer un gain mesuré ; une position égale à zéro sans impression n’est pas un classement Google. L’historique antérieur aux 30 derniers jours est indisponible avec l’essai actuel.
