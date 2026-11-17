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

Le futur compagnon pourra devenir une source de chaleur et de personnalité de l’interface. Il restera facultatif, discret et jamais culpabilisant. Sa présence ne devra ni masquer une action ni ajouter une étape obligatoire.

Exception autorisée le 17 novembre 2026 : la silhouette **neutre remplie en noir et gris**, tirée uniquement de la page 3 de `compagnon_timide_silhouettes2.pdf` (vignette miniature à droite), est utilisée **provisoirement comme icône de lancement Android**. Les tracés et les couleurs de Charly sont conservés ; seul un cadrage uniforme et un fond blanc Android sont ajoutés. Le compagnon n’est pas pour autant intégré dans l’interface ni décliné en nouvelles poses. Les illustrations complètes attendront leur validation.

États envisagés après réception des illustrations :

- repos discret sur l’accueil ;
- réaction curieuse au toucher ;
- confirmation douce après une capture réussie ;
- présence rassurante sur l’écran de reprise ;
- absence volontaire sur **l'écran principal de focus** afin de limiter la distraction, mais **compagnon actif dans la fenêtre flottante si l'utilisateur l'active explicitement** ; à l'interruption, il adoptera une pose de repos.

Le ton est doux, un peu étrange et socialement maladroit. Il peut employer un humour sec ou naïf, mais jamais un vocabulaire de coach, une félicitation excessive, une série quotidienne, une mine triste après une absence ou une formulation laissant entendre que l’utilisateur a échoué.

Les illustrations officielles devront fonctionner sur fond transparent, rester lisibles sur le blanc et sur le mode sombre, et respecter les couleurs de l’interface. Elles ne devront jamais être indispensables à la compréhension d’une action.

## Projet « Mon univers » (en attente des illustrations du second devis)

**Ne pas anticiper la commande artistique.** Le seul document de référence est `compagnon_timide_silhouettes2.pdf` : trois états et formes réduites (pp. 1–3), pétales qui saisissent ou poussent (p. 4). Il ne fournit **aucun** décor de pièce, inventaire d'objets ou animation finalisée. Aucune illustration d'une autre proposition ni d'une IA ne doit servir de substitution.

À terme, l'écran « Aujourd'hui » restera prioritaire avec les tâches et notes. L'onglet facultatif **« Mon univers »** présentera un premier espace à aménager. Un petit aperçu facultatif pourra figurer sur l'accueil **après** les commandes essentielles. Le compagnon flottant, à la place du rectangle uniquement sur choix explicite, sera actif lors d'un focus, au repos lors d'une pause et absent sans session. Aucune réaction à la baisse d'activité ne doit être culpabilisante.

L'ensemble est défini dans [ADR 0014](adr/0014-companion-world-planned.md) et [sa feuille de route](roadmap/2026-11-17-companion-world.md). **Actuellement, seul le logo neutre est intégré ; aucun écran, décor ni compagnon flottant n'est disponible.**

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

## Graphismes des outils fonctionnels

Les fonctions annexes utilisent une famille de pictogrammes tracés directement en Compose :

- même zone de dessin de `22 dp` et même épaisseur de trait ;
- badges de `44 dp`, bordure vert clair et fond d’action très pâle ;
- signes simples : capture, dé, démarrage, temps, pause, validation, réduction et report ;
- pictogramme toujours accompagné d’un libellé pour les actions importantes ;
- aucune dépendance à une bibliothèque graphique lourde ;
- aucune reprise du style, du logo ou des illustrations d’une application tierce.

Ces graphismes sont strictement fonctionnels. Ils ne préfigurent ni le compagnon, ni ses objets, ni le logo, ni l’univers illustré à produire avec l’illustrateurice.
