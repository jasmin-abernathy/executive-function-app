# À reporter dans repo-factory/errors — accueil Android

- Symptôme produit : seule une partie des changements demandés était visible. Cause vérifiée : PR 35 toujours ouverte en brouillon, absente de main. Correction préparée : reprise de ce diff dans le lot consolidé. Prévention : distinguer code de branche, fusion et version Android installée.
- Réécriture locale interrompue sur values-fr/strings.xml : le français utilise values, l’anglais values-en. Reprise ciblée après inventaire des ressources ; parité des clés contrôlée. Ne pas supposer un dossier de langue.
- Gradle ne peut télécharger sa distribution : Network is unreachable. Tests Android non exécutés localement. Validation prévue par GitHub Actions, sans APK ; ne pas annoncer de tests verts avant le résultat du commit final.
- Publication de branche refusée par le contrôle automatique : autorisation de modification jugée insuffisante pour l’envoi au dépôt distant. Aucun contournement. Vérification en lecture seule : branche distante absente. Demander l’accord explicite après préparation locale.
- Clone de repo-factory impossible sans identifiants dans le terminal, alors que les lectures par le connecteur réussissent. Ce document reste dans le lot local pour être reporté dans repo-factory après autorisation ; ne pas prétendre avoir synchronisé ce journal.
- Une recherche rg sans correspondance, liée à d’autres contrôles par &&, a interrompu les contrôles suivants. Reprendre les contrôles séparément ; ne pas confondre absence de correspondance et panne du dépôt.

- Envoi Git en terminal sans identifiants : reprise avec le connecteur après accord explicite. L’API de création de tree a d’abord reçu l’arbre local du nouveau commit au lieu de celui du parent ; refus 422 sans mutation de branche. Correction : lire HEAD~1^{tree}, puis créer le lot atomique via blobs. Branche et PR vérifiées après création.
- Lint Compose : deux LocalContextConfigurationRead dans HomeWorkspace. Utiliser LocalConfiguration.current pour une langue réactive aux changements de configuration, sans suppression du contrôle.
