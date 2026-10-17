# UX TDAH — démarrage, partage et reprise sans configuration imposée

## Périmètre intégré

La branche commence à `eae1b0d`, tête de la PR #46 : **toutes** ses modifications sont incluses (dossiers, notes, étapes, dates repères, timer, mode « Une chose à la fois », dé et tests). La nouvelle PR de consolidation doit cibler `main`, mais ne doit être fusionnée qu'après la validation du SHA final et des essais sur téléphone.

## Ce que ce lot ajoute

- Premier lancement : bouton prioritaire « Commencer sans configurer » ; la personnalisation reste un choix explicite.
- Premier usage : les check-ins automatiques sont inactifs par défaut, sans désactiver la fonction volontaire ni modifier les préférences déjà enregistrées.
- Android ACTION_SEND text/plain : note préremplie à confirmer avant enregistrement local, sans autorisation ni service distant.
- Rappels de tâche : toucher le corps ouvre les détails existants (dossier, étapes, notes associées). Le bouton « Commencer » ouvre l'écran de choix du timer, distinct.
- Tâche reportée : raccourci discret de reprise depuis l'accueil, sans streak, classement supplémentaire ou notification liée à l'inactivité.

## Recette humaine sur téléphone

1. Installation vierge → « Commencer sans configurer » → nouvelle tâche → minuterie ; aucun formulaire préalable.
2. Partager texte ou URL depuis un navigateur → brouillon prérempli → annuler, puis recommencer et sauvegarder.
3. Partager pendant qu'un timer est actif ; vérifier sa continuité et le retour à l'accueil.
4. Programmer un rappel, toucher le corps puis le bouton « Commencer » ; vérifier deux parcours distincts.
5. Tâche dans un dossier avec notes : vérifier la consultation depuis la fiche puis le tiroir plein écran pendant le timer.
6. Reporter une tâche, fermer l'application, revenir plusieurs jours plus tard ; reprendre sans compteur ni message de retard.
7. Mode sombre, thème clair, anglais, grande police, TalkBack, rotation, Android 8+ ; vérifier les sauvegardes v4/v5/v6 sans migration additionnelle.

## Après validation / pas dans ce lot

Ne pas ajouter de récapitulatif quotidien ni de nouveaux réglages de capture ou rappels tant que les retours utilisateurs ne prouvent pas leur utilité. Aucun partage image, réseau, compte, IA ou SMS. Aucun APK automatique.

## Erreur d'orchestration récupérée

Deux tentatives initiales de génération par remplacements de texte se sont arrêtées avant tout push parce que la même condition d'interface avait plusieurs occurrences. Le correctif a été de construire l'arbre Git par petits groupes, puis de ne publier qu'un seul commit final. Aucun état distant n'a été écrasé.
