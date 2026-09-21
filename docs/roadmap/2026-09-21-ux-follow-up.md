# Suivi UX — lot du 21 septembre 2026

Ce fichier est la checklist persistante des demandes utilisateur pour le lot UX en cours.
Il doit être mis à jour avant fusion de la PR #20.

## Garde-fou absolu

- [x] Ne pas modifier le design, le rendu, les couleurs, la géométrie ou l'animation du D10.
- [x] Aucun fichier D10 dans le diff actuel.

## Durées et pauses

- [x] Afficher les durées >= 60 min en heures/minutes.
- [x] Permettre une durée personnalisée pour « proposer une pause après… ».
- [x] Permettre de choisir la durée réelle de la pause.
- [x] Pause réelle configurable de 1 à 60 min.
- [x] Rappel doux à la fin de la pause.
- [x] Jamais de reprise automatique forcée.
- [ ] Validation appareil des rappels de pause.

## Sous-tâches

- [x] Ajouter autant de sous-tâches que nécessaire dès la capture.
- [x] Conserver l'ordre.
- [x] Persister après réouverture de la base.
- [x] Réutiliser le modèle d'étapes existant sans migration SQLite.
- [ ] Validation appareil / petit écran avec 10+ sous-tâches.

## Check-in humeur / motivation / énergie

- [x] Enregistrer localement le check-in.
- [x] Après un nouveau check-in, revenir explicitement et systématiquement à l'accueil.
- [x] Refaire l'historique : une métrique à la fois, tendance, dernier niveau et repères.
- [x] Fournir une lecture graphique claire, sans trois lignes superposées.
- [x] Conserver l'accès aux détails, à l'édition et à la suppression.
- [ ] Validation TalkBack et grande police.

## Onboarding première ouverture

- [x] Logique conditionnelle inspirée du Jardinier / questionnaires ADHD.
- [x] Les réglages appliqués sont de vrais réglages locaux.
- [x] Remplacer le questionnaire multi-écrans par une conversation interactive.
- [x] Afficher le fil de discussion : question de l'app, réponse utilisateur, réponse suivante.
- [x] Auto-avancer sur le choix de difficulté et supprimer « Suivant » comme mécanique générale.
- [x] Garder un bouton « Passer », des confirmations seulement quand plusieurs réglages sont à choisir, et un récapitulatif final modifiable.
- [x] Ne pas réintroduire un formulaire plat ou une longue page de réglages.
- [ ] Validation appareil / petit écran / grande police.

## Fenêtre flottante maison

- [x] Bouton note rapide.
- [x] Bouton pause/reprise.
- [x] Afficher ⏯️ pendant RUNNING.
- [x] Afficher ▶️ pendant INTERRUPTED.
- [x] Ajouter une semi-transparence au repos côté fenêtre flottante maison (50 %).
- [x] Rendre la fenêtre flottante maison opaque pendant l'interaction, puis la rendre à nouveau semi-transparente après un court délai.
- [ ] Validation appareil du déplacement + clics + note + pause/reprise.

## Picture-in-Picture Android

- [x] Actions système pause/reprise/note.
- [ ] Le code alpha 0,5 ne produit pas la semi-transparence attendue dans l'APK testé : ne pas le considérer comme validé.
- [ ] Tester sur appareil si l'alpha de fenêtre est respecté sur certains Android.
- [x] Documenter que le bouton/roue « Paramètres » appartient à SystemUI et n'est pas supprimable proprement.
- [ ] Si la roue Paramètres est rédhibitoire, décider explicitement si la mini-fenêtre produit doit être la fenêtre flottante maison plutôt que le vrai PiP.
- [ ] Ne pas prétendre que « tap PiP -> opaque » est résolu sans test réel.

## Écran verrouillé / veille

- [x] Faire persister techniquement le minuteur/chronomètre via le foreground service et la notification de focus, à la manière de GoodTime.
- [x] Utiliser la session persistée comme source de vérité, pas un ticker fragile dans l'Activity.
- [x] Garantir une notification de focus persistante avec chronomètre/minuteur et actions pause/reprise.
- [ ] Vérifier le comportement après extinction écran, passage arrière-plan et recréation de l'Activity.
- [ ] Validation appareil requise sur écran verrouillé.

## Validation finale

- [ ] Tests Android + lint verts sur le dernier SHA exact.
- [ ] Secret scan vert sur le même SHA.
- [ ] FR/EN en parité.
- [ ] Validation appareil des points signalés ci-dessus.
- [ ] Re-squasher le lot final proprement avant fusion.
- [ ] Aucun APK/AAB généré sauf demande explicite.


## Cartes de tâches et reprise

- [x] Clic simple sur une carte : ouvrir la fiche d’édition existante.
- [x] Appui long puis glisser sur la carte : réordonner directement la liste.
- [x] Pendant le déplacement : contour de surbrillance renforcé + élévation.
- [x] Conserver le handle et Monter/Descendre comme alternatives accessibles.
- [x] Une tâche reportée affiche « Continuer » au lieu de « Commencer ».
- [x] « Continuer » réactive la même session POSTPONED : même sessionId, même temps accumulé, même cible chrono/minuteur.
- [ ] Validation appareil du conflit clic / appui long / drag.

## Couleurs des tâches

- [x] Éclaircir les couleurs existantes vers une palette plus pastel.
- [x] Ajouter rouge pastel, violet doux, bleu profond, menthe et pêche.
- [x] Aucun choix explicite de couleur : attribution aléatoire parmi les couleurs non neutres.
- [x] Un choix explicite « Neutre » reste neutre.
- [ ] Validation contraste clair/sombre sur appareil.

## Format des compteurs

- [x] Utiliser un format commun mm:ss sous une heure et h:mm:ss à partir d’une heure.
- [x] Appliquer ce format au PiP / mini-timer, à l’écran focus et à la fenêtre flottante.
- [x] Exemple vérifié par test attendu : 95 min 17 s → 1:35:17.
