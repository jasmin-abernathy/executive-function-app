# Charte graphique / Visual direction

Statut : **base d’interface validée, illustration encore provisoire — 2 septembre 2026**. Le brief de devis a été envoyé à l’illustrateurice.

Ce document est la source de vérité pour le prototype. Il remplace la précédente interprétation beige, sauge, terracotta et marron tirée des moodboards.

## Intention

L’application doit être :

- claire, lumineuse, joyeuse et immédiatement lisible ;
- chaleureuse et humaine, sans esthétique de « tech froide » ;
- adulte, sans devenir clinique ni enfantine ;
- simple et aérée, avec très peu de bruit visuel ;
- organique dans ses formes et futurement enrichie d’illustrations artisanales.

Le style artisanal guide les formes, les traits et les futures illustrations. **Il ne définit pas la couleur de fond de l’interface.** Les textures, objets et irrégularités du moodboard ne doivent jamais transformer l’écran principal en palette beige ou marron.

## Palette source retenue

| Rôle | Couleur | Valeur | Usage |
| --- | --- | --- | --- |
| Identité | Bleu lavande | `#8B8DEB` | aplats de marque, conteneurs, repères visuels |
| Énergie | Jaune lumineux | `#FFD93D` | actions positives, mise en avant ponctuelle |
| Fond | Crème très clair | `#FFF9F0` | fond principal, repos visuel |
| Texte | Anthracite | `#2D2D2D` | texte principal et texte sur couleurs claires |

Cette palette est la dernière palette chiffrée explicitement retenue pour l’application.

## Tons d’implémentation accessibles

Les valeurs suivantes sont des dérivés techniques, pas une nouvelle palette :

| Jeton | Valeur | Raison |
| --- | --- | --- |
| Lavande accessible | `#6567C7` | texte, icônes et boutons avec texte blanc ; contraste AA |
| Lavande clair | `#8B8DEB` | teinte source, utilisée avec texte anthracite |
| Conteneur lavande | `#E8E4EF` à `#F4F0FA` | surfaces secondaires discrètes |
| Conteneur jaune | `#FFF1A6` | accent énergétique sans éblouissement |
| Surface | `#FFFDF9` | cartes sur fond crème |

Contrastes vérifiés pour le thème clair :

- `#2D2D2D` sur `#FFF9F0` : environ **13.2:1** ;
- `#FFFFFF` sur `#6567C7` : environ **4.9:1** ;
- `#2D2D2D` sur `#8B8DEB` : environ **4.7:1** ;
- `#2D2D2D` sur `#FFD93D` : environ **10:1**.

## Accents secondaires

Les discussions plus récentes ajoutent un **bleu clair** pour le calme et un **orange doux** pour la chaleur. Leurs valeurs finales n’ont pas encore été arrêtées par l’illustrateurice.

Le prototype utilise provisoirement :

- bleu clair `#83C7E8` ;
- orange doux `#F28A62`.

Ils restent secondaires et ne doivent pas remplacer le duo lavande–jaune.

## Formes et composition

- cartes arrondies de `20 dp` ;
- grands conteneurs de `28 dp` ;
- boutons et contrôles tactiles d’au moins `48 dp` ;
- espacement généreux et hiérarchie courte ;
- une action principale évidente par zone ;
- illustrations simples et organiques, jamais indispensables à la compréhension ;
- aucune alerte rouge, série quotidienne ou décoration anxiogène sur l’accueil.

## Accessibilité et sobriété

- le texte courant vise WCAG AA ;
- aucune information n’est transmise uniquement par la couleur ;
- les textes restent agrandissables ;
- les animations respectent la réduction de mouvement ;
- le mode sombre conserve lavande, jaune et bleu, sans revenir au vert ou au marron ;
- les couleurs dynamiques Android sont désactivées afin que le système ne remplace pas l’identité de l’application.

## Compagnon et illustration

Le compagnon reste une couche facultative, discrète, douce, légèrement étrange et jamais culpabilisante. Son style final, les textures et les éléments d’atelier seront définis après le retour de l’illustrateurice. Ils ne bloquent pas le développement du cœur capture → action → focus → interruption → reprise.

## Interdits

- identité verte héritée du Potager du Web ;
- dominante marron, terracotta ou sauge ;
- fond beige sombre ;
- violet saturé sur de grandes surfaces ;
- esthétique infantile, médicale ou futuriste ;
- gamification punitive ;
- remplacement automatique de la palette par les couleurs du téléphone.
