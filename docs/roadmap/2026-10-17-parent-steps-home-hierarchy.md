# 2026-10-17 — Tâches principales, petits pas et hiérarchie de l'accueil

## Déclencheur et retours terrain

- Au premier lancement, « À ne pas oublier » ressemblait à une note, pas une tâche : remplacer le libellé par « Nom de la tâche » et conserver un CTA explicite.
- Une première tâche a disparu sur appareil : aucune cause certaine établie sans la base de données du téléphone. Renforcer la persistance et empêcher la disparition automatique des parents comportant des petits pas à la fin d'une session de focus.
- « test de tâche » et « petit pas » se trouvaient au même niveau : l'ancien bouton de décomposition fabriquait une seconde vraie tâche. Ne plus proposer ce parcours par défaut, masquer dans la liste d'accueil les tâches historiquement générées par cette fonction sans les effacer de la base.
- Les nouveaux petits pas se saisissent un par ligne, sont sauvegardés dans `task_steps` sous leur parent, et sont affichés en cases à cocher imbriquées dans la carte parent. La première étape non cochée est mise en valeur.
- Le dé sélectionne une tâche principale parmi `openTasks()` ; son résultat souligne également le premier petit pas non coché. Aucune sous-tâche ne participe indépendamment au tirage.

## Règles de complétion

- Cocher un petit pas modifie uniquement `task_steps.completed`, pas le statut du parent.
- Cocher le dernier petit pas fait apparaître « La tâche principale est-elle maintenant terminée ? » avec les réponses « Oui, elle est terminée » ou « Pas encore ».
- « Pas encore » ferme le dialogue sans changer la tâche principale. On peut revenir à la question depuis la fiche des étapes.
- « Oui » termine explicitement la tâche principale, sans supprimer ni effacer ses petits pas.
- Terminer un timer sur une tâche avec petits pas termine la session et conserve la tâche READY, pour éviter toute clôture accidentelle. Le comportement des tâches sans petits pas reste inchangé.

## Ordre final sur l'accueil

1. Message d'accueil et point d'état.
2. Dans un même bloc, deux actions côte à côte : **Ajouter une tâche** / **Ajouter une note**.
3. Liste/présentation des tâches principales avec cases de petits pas.
4. Dé de sélection parmi les tâches principales.
5. Autres réglages/outils, si nécessaires.
6. **Dernier bloc : thèmes et détail des notes, repliables**.

## Compatibilité et recette Android à exécuter

- Aucun changement de schéma ou de version DB, conservation des tables et sauvegardes existantes.
- Vérifier avec tâches déjà créées et tâches fractionnées par l'ancien comportement, check-in récent, timer actif, thème filtré et mode basse énergie.
- Vérifier premier démarrage, création de 1 ou 3 petits pas, cocher tous puis « Pas encore », et revenir plus tard à la confirmation.
- Vérifier le tirage du dé, son animation, l'absence de tirage d'une sous-tâche et le retour au parent.
- Vérifier les échelles de police, les petites largeurs d'écran et TalkBack pour les deux boutons côte à côte.
- Vérifier l'absence de disparition de tâche après enregistrement sans démarrage et après une session de focus clôturée.

Ne pas générer d'APK sans demande. La PR 47 reste en brouillon jusqu'à la validation sur appareil.