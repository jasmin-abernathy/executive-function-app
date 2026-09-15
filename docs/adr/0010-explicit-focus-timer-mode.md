# 0010 — Choix explicite chronomètre / minuteur

Décision préparée le 15 septembre 2026. Elle ne change ni la règle d’apprentissage ni le stockage des mesures brutes.

## Intention

Le repère de durée appris ne doit pas imposer la manière d’afficher le temps. Avant une nouvelle session, l’utilisateur peut choisir explicitement :

- **Chronomètre** : le temps actif part de zéro ; la session est stockée avec `targetDurationMs = null`.
- **Minuteur** : une durée positive est choisie ; la référence apprise est proposée lorsqu’elle existe, mais une saisie manuelle reste possible. La cible choisie est stockée dans `targetDurationMs`.

Le dernier choix d’affichage peut être mémorisé comme préférence pour la prochaine session. Cette préférence n’est jamais la source de vérité d’une session déjà créée : une session en cours ou interrompue conserve sa cible persistée.

## Apprentissage et dépassement

Chronomètre et minuteur continuent tous deux à mesurer le temps actif réel avec les mêmes transitions de pause, reprise, report et fin. Choisir le chronomètre n’efface ni ne neutralise une référence apprise ; elle reste disponible lors d’un futur choix de minuteur.

L’arrivée à zéro ne termine pas une tâche automatiquement. Si le travail continue, le dépassement est affiché comme temps supplémentaire et reste mesuré. Il n’existe ni score négatif ni état d’échec associé au dépassement.

## Compatibilité

Le modèle courant possède déjà `FocusSession.targetDurationMs`. Aucune migration de base ou de sauvegarde n’est nécessaire pour distinguer ces deux modes : `null` signifie chronomètre et une valeur positive signifie minuteur. Les surfaces principales, PiP, notification et fenêtre flottante doivent continuer à dériver leur affichage de cette cible persistée, et non d’une préférence globale susceptible de changer en cours de session.

Une reprise d’une session interrompue conserve sa cible existante. Démarrer une autre tâche peut reporter la session active selon le comportement déjà établi ; ce changement doit rester annoncé avant confirmation.

## Validation attendue

Les tests doivent couvrir au minimum le chronomètre malgré une référence apprise, le minuteur manuel sans historique, la conservation de la cible après pause/reprise, le dépassement sans fin automatique et la compatibilité des affichages secondaires. La compilation ne remplace pas un essai sur appareil pour la lisibilité et l’accessibilité.

Les tests de persistance passent par le dépôt et une vraie réouverture SQLite sous Robolectric. Ils vérifient aussi qu’un nouveau paramètre de durée ne remplace pas la cible d’une session interrompue reprise et qu’un dépassement laisse la session en cours. Les actions PiP sont testées pour les états en cours et interrompu, avec une action de note distincte.

Le dialogue de choix est défilable et ses sélecteurs ont une cible de 48 dp minimum. Les boutons de l’introduction reprennent le vert pâle de la palette existante. Le résultat du dé est amené à l’écran à partir de sa position dans les sections identifiées de la liste ; la demande de visibilité attend sa composition, sans temporisation arbitraire.
