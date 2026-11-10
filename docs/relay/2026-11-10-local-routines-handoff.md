# Relais — routines locales TDAH (10 novembre 2026)

## Base, portée et invariants

- Base de travail : `6fcd1db4de4ca8d9b556ba90476ef533fc1ef3ff` (PR #58 — reprise et rappels), toujours non fusionnée dans `main`.
- Développement isolé sur `feature/adhd-v2-local-routines-20261110`; la PR qui suit doit viser **la branche de la PR #58**, pas `main`, pour éviter de perdre son travail et ne pas la contourner.
- Sources de décision : 11 réponses obligatoires au questionnaire TDAH V2 du 10/11/2026, dont 6/11 ayant sélectionné les routines parmi leurs cinq fonctions et 6/8 préférant une séquence avec horaires facultatifs.
- Aucune dépendance réseau, permission, SDK analytics, abonnement, streak, pénalité d'absence ou notification de routine automatique.
- Ne jamais transformer `routine_steps` en tâches de premier niveau `tasks`; pas de modifications aux sessions de focus, aux étapes `task_steps`, ni aux récurrences existantes.

## Réalisé dans la branche

- `RoutineStore.kt` : modèles locaux, étapes ordonnées, repères horaires facultatifs `HH:mm` (simple affichage), modifications de modèle, déplacement, lancement/reprise, pause, validation et saut d'étape.
- Les exécutions ont **un instantané des étapes**, indépendant des modifications ultérieures du modèle ; une seule exécution inachevée possible par routine (index SQLite partiel).
- `RoutineActivity.kt` : écran autonome accessible depuis l'accueil, navigation revenir, création, ajout/modification/retrait (confirmé)/réordonnancement des étapes, reprise, pause, passage d'étape, état vide et erreurs.
- Migration additive v6→v7 ; `LocalBackup` format 7 avec toutes les tables ; anciennes sauvegardes 4/5/6 toujours importables avec listes de routines vides. Import transactionnel et test d'échec FK.
- Tests Robolectric `RoutineStoreTest` ; validation de l'accès depuis l'accueil `HomeModesTest`.

## Limites explicites

- Le repère horaire est **informatif uniquement**, pas une alarme ni un horaire qui réorganise automatiquement la routine. Ne pas promettre de notifications.
- Pas de synchronisation d'agenda, pas de création d'une tâche indépendante pour une étape, pas de routine automatique quotidienne. Ce serait un nouveau lot facultatif, soumis aux retours d'usage.
- Une routine peut être sauvegardée sans étapes et complétée plus tard ; démarrage impossible tant qu'aucune étape n'existe.
- Pas de suppression complète des routines dans la première interface (préservation des données) ; retrait d'une étape du modèle avec confirmation explicite et conservation de l'exécution courante.
- Les exécutions achevées restent dans la base et les sauvegardes, mais l'interface n'affiche pas encore un historique détaillé.

## Recette indispensable sur appareil Android

1. Installer une version de test compatible sans perte de données existantes ; créer une routine en trois étapes, dont une à heure fixe ; vérifier l'affichage sur petits écrans.
2. Lancer, terminer la première étape, mettre en pause, forcer la fermeture Android, reprendre un jour plus tard. Vérifier même exécution et progression conservée.
3. Modifier le modèle pendant une exécution : ancienne étape inchangée dans l'exécution en cours ; nouvelles étapes visibles au prochain lancement.
4. Exporter une sauvegarde format 7, effacer volontairement les données du **téléphone de test**, importer puis vérifier les routines et tâches restaurées. Vérifier également une ancienne sauvegarde v4, v5 ou v6.
5. FR/EN, thème clair/sombre, grande police, TalkBack, écran étroit (≤ 360 dp), boutons de montée/descente et confirmation de retrait ; aucune action hors écran ou contrôle masqué.
6. Vérifier la PR #58 en amont (rappels bloqués/rétablis, reprise >24h), car cette branche en dépend.
7. Ne pas fusionner la PR ni publier d'APK sans la recette ci-dessus et les tests GitHub Actions Android et secrets verts sur le SHA exact.

## Relais technique

- Si compilation en échec : consulter le job Android sur la tête exacte de la PR ; corriger les imports Compose et les tests avant toute fusion.
- Garde-fou `repo-factory` : tests + lint, pas de build APK automatique, documenter tout incident dans `repo-factory/errors`, état réel de la branche et SHA testés.
