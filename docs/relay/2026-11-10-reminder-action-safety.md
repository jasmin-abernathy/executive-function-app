# Relais — rappels locaux, intégrité des tâches et reports (10/11/2026)

- Base : PR #58 `6fcd1db4de4ca8d9b556ba90476ef533fc1ef3ff` (reprise douce et conservation des rappels lorsque les notifications Android sont bloquées). Développement sur une branche distincte, cible provisoire branche de PR #58.
- L'action de notification « Fait » n'est plus proposée lorsque la tâche possède des petits pas `task_steps`. Une ancienne notification encore affichée est protégée côté `TaskReminderReceiver` : elle ouvre les détails au lieu de terminer la tâche principale sans confirmation.
- Les trois raccourcis de report dans `JournalActivity` sont désormais 10 / 30 / 60 minutes (auparavant 5 / 15 / 60). La prochaine échéance enregistrée est affichée, dans le fuseau local.
- `TaskReminder.pendingAt` lit uniquement l'échéance locale enregistrée, et une nouvelle programmation remplace la précédente pour la même tâche (clé primaire `task_id`).
- Les notifications Android conservent un unique bouton « Pas maintenant » qui repousse de 10 minutes : ne pas prétendre qu'il s'agit d'un sélecteur 10/30/60 dans la notification. Les trois durées sont choisies depuis la fiche de tâche.
- Aucun changement de schéma de base, permissions ou réseau.

## Validation

- CI Android et tests Robolectric sur les chemins de notification, persistance d'une seule échéance, garde des petits pas.
- Recette téléphone : Android 13+ permission accordée/refusée, canal désactivé, report après reboot, notification ancienne « Fait », tâche dont les petits pas sont tous cochés mais parent non confirmé, FR/EN, petits écrans et TalkBack.
- Ne pas fusionner avant les validations physiques de PR #58 et de ce lot. Aucun APK automatique.
