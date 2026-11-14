# ADHD V2 — feuille de route fondée sur les 11 réponses agrégées du 10 novembre 2026

## Périmètre et prudence

Export d'origine : questionnaire V2, 14 démarrages, 11 parties obligatoires envoyées, 7 réponses intégrales ; 0 mineur parmi les réponses envoyées. Les thèmes facultatifs comptent 7 à 9 réponses selon la question. Il s'agit de signaux de conception **exploratoires**, pas de statistiques représentatives ou de preuves d'efficacité clinique. Aucun contenu individuel n'est nécessaire.

## Audit code → réponses → arbitrages

| Rang | Signal du questionnaire | État de `main` au démarrage | Livraison proposée | Validation |
| --- | --- | --- | --- | --- |
| P0 Reprise | 9/11 craignent la perte de progression ; 6/11 préfèrent « de quoi avez-vous besoin aujourd'hui ? » au retour | `FocusTransitions` conserve la durée ; `ResumeScreen` impose déjà de choisir Reprendre/Réduire/Reporter | **Lot A :** question d'accueil après ≥24 h d'interruption, sans toucher à la session ; tests de cumul avant/après report. Ensuite recette physique après 2 jours d'absence, restauration et décès de processus | Temps et notes conservés ; aucune tâche effacée ni message de retard |
| P0 Rappels | 8/11 craignent les notifications nombreuses ; 5/11 les retiennent au MVP ; 3/11 les choisissent comme fonction unique | `TaskReminder` stocke l'échéance ; actions Commencer / Fait / Pas maintenant (+10 min). La ligne SQL était supprimée **avant** le contrôle des autorisations | **Lot A :** ne pas consommer un rappel si la notification est bloquée ; réarmer les rappels locaux autorisés au retour dans l'app ; vérifier les tâches déjà terminées avant « plus tard ». **Lot B :** tester un report paramétrable sans multiplier les notifications | Refus/réactivation des notifications, canal désactivé, redémarrage, tâche terminée, absence de duplication |
| P1 Accueil et saisie | 8/11 retiennent Aujourd'hui ; 0/11 choisissent un formulaire complet pour capturer pendant le focus | Modes ONE_NEXT/NOW_NEXT/LIST et capture instantanée ; check-in affiché avant les actions principales | **Lot A :** afficher « Ajouter une tâche / Ajouter une note » avant le check-in, sans le supprimer. **Lot B :** évaluer temps jusqu'au premier geste et nombre de décisions répétées | Une tâche et une note créées sans catégorie imposée ; check-in toujours trouvable et accessible |
| P1 Routines | 6/11 sélectionnent routines ; 3/11 comme fonction unique ; 6/8 combinent séquence et horaires, 2/8 séquence seule | `recurrences` permet des répétitions simples mais pas de séquence de routine ; `task_steps` reste imbriqué à une tâche | **Lot B distinct** : modèle local `routine_template` et `routine_step` avec ordre, horaire *optionnel* seulement pour les vraies échéances ; lancement manuel, suspendre/sauter/reprendre sans punition ; lien facultatif avec Aujourd'hui. Migration additive 6→7 et backup versionné validés **avant** intégration UI | Idempotence, réouverture, changement d'ordre, passage minuit, horaires fixes, restauration v4–v6, aucune suppression d'anciennes tâches |
| P2 Focus/hyperfocus | 6/9 préfèrent les modes de durée ; 7/9 trouvent naturel un tap pour mettre en pause ; 7/9 veulent des rappels pour boire/bouger | Presets SHORT/NORMAL/LONG/OPEN, pause explicite, capture et notifications existantes, mais le tap sur l'horloge ne suspend pas immédiatement la session | **Lot C** : prototype de pause tactile accessible avec confirmation visuelle et tiroir minimal ; mode long volontaire + rappels corporels facultatifs, configurables et discrets | Pause/reprise sans double comptage, TalkBack, PiP, rotation, sans check-in forcé, hors-ligne |

## Garde-fous d'implémentation

- Ne pas déplacer les données vers un cloud ni ajouter des permissions sans besoin.
- Aucun streak punitif, récompense perdue, notification d'inactivité imposée, abonnement intrusif ou compagnon déçu.
- Ne pas fusionner l'ébauche Google Play (#48) ni construire d'APK à chaque commit ; vérifier d'abord CI Android (tests + lint) et une recette téléphone.
- Conserver le flux `main` existant et les changements non destructifs indépendants des futures migrations.
- Les routines exigent un lot et une validation de persistance séparés : ne pas annoncer la fonctionnalité comme intégrée après ce premier lot.
- Bilan sur appareil : démarrage simple, retour après >24 h, état interrompu, report 10 min, permission notifications refusée/accordée, affichage FR/EN, grandes polices, TalkBack, focus avec écran verrouillé.

## Définition de « terminé » pour le lot A

Tests CI Android réussis ; aucune migration SQL ; affichage des actions principales avant le check-in ; copie du temps de session et de ses notes intacte ; rappel bloqué non supprimé, reprise du calendrier local lorsqu'Android réautorise l'affichage. Une PR distincte reste réversible.
