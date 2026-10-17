# UX TDAH — accueil recentré sur la création (17 octobre 2026)

## Choix ergonomique

- Deux actions en premier sur l’accueil : **Ajouter une tâche** et **Ajouter une note**, espacées et lisibles sur mobile.
- Le point d’état, le journal et l’aide quittent le premier niveau ; on les retrouve sous un seul volet **Point d’état, journal et aide**.
- Le grand bloc d’organisation devient une ligne discrète **Thèmes** / **Mes notes**. Le filtre et les opérations de gestion s’ouvrent seulement à la demande.
- « Dossier » devient **thème** dans les textes visibles ; aucune table SQL, aucune sauvegarde, aucun identifiant interne n’est renommé. Les contenus existants gardent leurs associations.
- Lors de la création d’une tâche ou note : proposer facultativement un thème existant ou la création immédiate d’un thème neuf, sans avoir à le définir à l’avance.
- Un utilisateur qui n’utilise jamais les thèmes peut ajouter immédiatement des tâches et des notes. Premier geste, timer et dé sont conservés.

## Vérifications de recette

1. Vérifier sur écran étroit et grosses polices que les deux CTA principaux restent lisibles et indépendants.
2. Créer une première tâche sans thème ; le choix de minuterie existant doit rester proposé. Créer une seconde tâche avec un thème neuf.
3. Sans aucun thème préexistant, créer une note avec nouveau thème. Vérifier que tâche et note peuvent être placées dans le même thème.
4. S’assurer que la suppression d’un thème ne supprime jamais ses tâches et notes.
5. Ouvrir un thème existant, ajouter une tâche et une note ; le thème sélectionné doit être proposé, avec possibilité de le retirer.
6. Afficher/réduire la liste des thèmes et la liste des notes, éditer les tâches préexistantes et vérifier que le timer avec tiroir de notes fonctionne toujours.
7. Vérifier FR/EN, TalkBack, grands caractères, mode clair/sombre et rotation avec une saisie en cours.

## Conservé hors périmètre

Le nom `folder` dans la base locale et le code reste inchangé pour ne pas introduire de migration de données cosmétique. Le partage Android par la feuille de partage remplit toujours une note rapide non classée ; ce parcours séparé devra être évalué avec les utilisateurs.
