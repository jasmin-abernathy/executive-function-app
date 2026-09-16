# 0009 — Mini-fenêtre de focus sans permission de superposition

Décision du 12 septembre 2026.

## Décision

Le bouton « Mini-fenêtre Android » correspond au mode Picture-in-Picture (PiP) de l'application. Il doit entrer directement dans ce mode lorsqu'une session est en cours ou interrompue et que l'appareil prend PiP en charge.

Il ne doit jamais ouvrir `ACTION_MANAGE_OVERLAY_PERMISSION` et ne dépend pas de `SYSTEM_ALERT_WINDOW`.

La permission de superposition reste réservée au timer flottant optionnel géré par `FocusOverlayService`. Refuser cette permission ne bloque ni le timer principal ni la mini-fenêtre PiP.

## Actions visibles dans la mini-fenêtre

La mini-fenêtre propose deux actions maximum :

- session en cours : **Pause** + **Ajouter une note** ;
- session interrompue : **Reprendre** + **Ajouter une note**.

Pause/Reprendre réutilisent les transitions de focus existantes. Les icônes doivent décrire clairement l'action : symbole pause pour Pause, lecture pour Reprendre, note avec `+` pour Ajouter une note.

Après une transition Pause/Reprendre, les actions PiP sont recalculées à partir du nouvel état de session. Il ne doit pas rester une action « Pause » sur une session déjà interrompue.

## Note rapide

« Ajouter une note » ouvre une saisie courte locale. Enregistrer crée une `quick_note` via le dépôt existant. Annuler ne crée rien.

Lorsque la note a été ouverte depuis PiP, enregistrer ou annuler ramène au focus réduit si la session est toujours en cours ou interrompue. Une erreur d'enregistrement reste visible dans l'application et ne doit pas être masquée par un retour automatique en PiP.

Cette note est distincte de la capture d'une nouvelle tâche et distincte de la note d'interruption.

## Accessibilité et sécurité

- chaque `RemoteAction` a un libellé accessible explicite ;
- aucun geste précis n'est nécessaire ;
- aucune nouvelle permission sensible n'est ajoutée ;
- aucun contenu de tâche n'est placé dans le libellé des actions PiP ;
- le comportement reste fonctionnel si les notifications ou la permission de superposition sont refusées.

## Validation requise avant fusion

La compilation et les tests unitaires ne suffisent pas pour ce lot. Tester sur appareil ou émulateur compatible PiP : entrée directe en mini-fenêtre, Pause → Reprendre, Reprendre → Pause, ouverture d'une note, sauvegarde, annulation, retour en PiP, et comportement lorsque la permission de superposition est refusée.
