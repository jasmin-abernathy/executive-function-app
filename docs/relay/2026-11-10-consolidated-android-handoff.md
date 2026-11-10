# Relais consolidé — application TDAH, lots questionnaire V2 (10 novembre 2026)

## Branches et périmètre
- Base stable de cette intégration : `6fcd1db4de4ca8d9b556ba90476ef533fc1ef3ff`, PR #58 encore en attente de recette physique.
- Source 1 : PR #62 (routines SQLite v7, sauvegardes, reprise et interface).
- Source 2 : PR #63 (rappels 10/30/60, date d'échéance, garde « Fait » des parents).
- Source 3 : PR #64 (session longue ponctuelle, toucher pour pause, note pendant l'interruption, garde des petits pas pour les notifications de focus).
- Ce lot réunit les fichiers effectifs de ces trois sources dans **un commit de consolidation**. Aucun APK automatique, aucune modification de `main`.

## Invariants bloquants
- Aucun compte, permission Internet, analytics, streak, rappel automatique imposé, culpabilisation ni paiement.
- Routines séparées des tâches de premier niveau ; instantanés des étapes commencées.
- Migration additive SQLite 6→7 ; sauvegardes 4/5/6 importables ; format exporté 7 transactionnel.
- Arrêter une session de focus ou appuyer sur une notification ne termine pas silencieusement un parent ayant des petits pas.
- Le repère d'heure dans une routine est visuel ; le mode long dure 120 min sans changer le réglage par défaut.
- Ne pas fusionner cette branche ni les PR #62/63/64 directement dans `main` avant que la recette complète soit satisfaite.

## Vérifications requises
1. GitHub Actions Android sur le SHA exact de l'intégration, tests Robolectric et lint ; scan des secrets.
2. Essai sur téléphone Android 8+ (au moins Android 13 pour les permissions) : nouvelle routine, pause/reprise après fermeture forcée, déplacer/modifier étape en cours, sauvegarder/restaurer, import v6.
3. Rappels bloqués puis autorisés, attente/reboot, report 10/30/60 et tâche avec petits pas cochés/non cochés.
4. Timer tactile, double tap, note pendant pause, retour après plusieurs jours, mode 2 h une fois, mini-fenêtre/PiP, arrêt via notification avec petits pas.
5. FR/EN, grand texte, TalkBack, mode sombre, écran étroit et contraste ; aucune capture validée sur appareil au stade du code.
6. Contrôler les futures versions installables via un build explicite seulement après l'accord humain. La PR #48 de publication Google Play reste bloquée.

## Décision de fusion
- Si la recette humaine de #58 et de cette branche est positive, intégrer une seule fois le code consolidé (squash) après revue du diff. Ne pas fusionner ensuite séparément #62, #63 et #64 : elles sont des étapes de traçabilité, déjà incluses.
- Si un problème survient, corriger uniquement la branche d'intégration et relancer la CI du SHA final.
