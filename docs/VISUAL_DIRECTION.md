# Charte graphique / Visual direction

Statut : **direction d’interface validée — 2 septembre 2026**. Le brief de devis a été envoyé à l’illustrateurice.

Ce document est la source de vérité pour l’interface. Il remplace toutes les palettes précédemment envisagées, notamment beige, marron, terracotta, lavande, jaune et bleu.

## Intention

L’application doit être calme, claire et immédiatement lisible. L’interface s’efface derrière l’action à accomplir : elle n’utilise ni aplats saturés, ni décoration gratuite, ni code couleur complexe.

Le style artisanal et organique reste pertinent pour les futures illustrations. Il ne doit pas modifier la palette de l’interface, réduire le contraste ou transformer l’accueil en moodboard.

## Thème clair

Le thème clair utilise quatre rôles visuels principaux :

| Rôle | Valeur | Usage |
| --- | --- | --- |
| Fond | `#FFFFFF` | fond principal et grandes surfaces |
| Bouton | `#EAF6ED` | fond très pâle des actions |
| Bordure | `#B7D8BF` | contours, champs et décorations discrètes |
| Texte | `#2D2D2D` | textes, libellés et icônes |

Le ton `#F6FBF7` est uniquement un dérivé de surface très léger pour les états et conteneurs secondaires. Il ne constitue pas une couleur d’accent supplémentaire.

Règles :

- le fond d’un écran reste blanc ;
- les cartes restent blanches et sont délimitées par une bordure vert clair ;
- les boutons sont plus clairs que leurs bordures ;
- le texte sur les boutons reste anthracite ;
- aucun vert saturé ou foncé n’est utilisé comme aplat d’action.

## Mode sombre doux

Le mode sombre suit automatiquement le réglage Android. Il conserve une dominante neutre légèrement végétale, sans noir pur, marron ou accent saturé.

| Rôle | Valeur | Usage |
| --- | --- | --- |
| Fond | `#1D2420` | fond principal |
| Surface | `#252E29` | cartes et dialogues |
| Surface relevée | `#2A342E` | conteneurs secondaires |
| Bouton | `#314238` | actions douces |
| Bordure | `#789582` | contours et champs |
| Texte | `#EEF3EF` | texte principal |
| Texte secondaire | `#C8D2CB` | indications et descriptions |

Le mode sombre doit rester moins contrasté visuellement qu’un thème noir et blanc, tout en maintenant une lisibilité suffisante pour le texte courant.

## Composants

- cartes arrondies de `20 dp`, avec bordure de `1 dp` ;
- grands conteneurs de `28 dp` ;
- boutons et contrôles tactiles d’au moins `48 dp` ;
- une seule action principale évidente par zone ;
- champs blancs en clair, surfaces douces en sombre, toujours bordés ;
- ombres légères et neutres, jamais colorées ;
- aucun état n’est communiqué uniquement par la couleur.

## Accessibilité et sobriété

- texte principal anthracite sur blanc en clair ;
- texte blanc cassé sur fond anthracite-vert en sombre ;
- textes agrandissables et zones tactiles accessibles ;
- animations compatibles avec la réduction de mouvement ;
- couleurs dynamiques Android désactivées pour préserver la direction ;
- aucune série quotidienne, alerte agressive ou décoration culpabilisante.

## Compagnon et illustration

Le compagnon est la principale source de chaleur et de personnalité de l’interface. Il reste facultatif, discret et jamais culpabilisant. Sa présence ne doit ni masquer une action ni ajouter une étape obligatoire.

La première implémentation utilise une silhouette vectorielle provisoire, fantomatique et légèrement animale. Elle sert à valider les emplacements et les réactions avant l’intégration des fichiers de l’illustrateurice. Elle doit pouvoir être remplacée sans modifier la logique des écrans.

États actuellement prévus :

- repos discret sur l’accueil ;
- réaction curieuse au toucher ;
- confirmation douce après une capture réussie ;
- présence rassurante sur l’écran de reprise ;
- absence volontaire pendant le focus afin de limiter la distraction.

Le ton est doux, un peu étrange et socialement maladroit. Il peut employer un humour sec ou naïf, mais jamais un vocabulaire de coach, une félicitation excessive, une série quotidienne, une mine triste après une absence ou une formulation laissant entendre que l’utilisateur a échoué.

Les illustrations définitives devront fonctionner sur fond transparent, rester lisibles sur le blanc et sur le mode sombre, et respecter les couleurs de l’interface. Elles ne doivent jamais être indispensables à la compréhension d’une action.

## Interdits

- fond beige, crème, marron ou vert foncé en thème clair ;
- boutons vert saturé ;
- palette multicolore ;
- aplats lavande, jaune, orange ou bleu ;
- texte gris trop clair ;
- noir pur en mode sombre ;
- esthétique infantile, médicale ou futuriste ;
- gamification punitive ;
- remplacement automatique de la palette par les couleurs du téléphone.
