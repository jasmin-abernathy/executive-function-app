# 0007 — D10 et ordre manuel

Le bouton « Mélanger l’ordre des tâches au dé » applique une permutation complète des tâches READY admissibles. La première devient la proposition ; les autres tâches sont conservées ensuite dans leur ordre relatif. Les identifiants restent uniques. Un relancer évite la précédente proposition lorsque plusieurs choix sont disponibles. Random est injectable pour des tests déterministes.

Le mélange est persisté avant de présenter son résultat. Une erreur de sauvegarde ne présente pas un mélange comme réussi. Rien ne démarre automatiquement. Le bloc titre/premier pas du résultat est cliquable, séparément de « Relancer », et conserve un bouton « Commencer », une action nommée et une annonce TalkBack.

Le widget garde le sens annoncé de son raccourci : proposer une tâche, sans réorganiser la liste. Ses relancers conservent ce comportement. Le bouton principal de mélange reste explicite.

Le D10 est dessiné en perspective à partir de dix faces, avec ombrage et rotations rapides pendant 500 ms. Une Box racine Compose le place au-dessus de l’accueil, hors LazyColumn. Il ne dépend ni de SYSTEM_ALERT_WINDOW ni du service de fenêtre flottante. Le mode calme ou la désactivation des animations système saute immédiatement à la proposition, sans vibration.

Chaque tâche porte une poignée verticale de 48 dp minimum. Seul un appui long sur cette poignée capture le déplacement ; le reste de la carte permet le défilement normal. L’ordre provisoire reste dans l’UI et une seule écriture est demandée au relâchement. Annuler le geste ne sauvegarde rien. Les boutons ↑/↓ du mode Organiser et les actions TalkBack restent disponibles.

Les tests couvrent permutation, admissibilité, exclusions, cas vides/unitaires, reproductibilité et relancer, rechargement SQLite, écriture unique et annulation, résultat en mode calme, raccourci sans mutation et sémantique accessible. Les gestes réels, le grossissement du texte et la perception du rendu 3D demandent toujours un essai sur appareil.

Ce lot ne change ni le schéma SQLite ni les permissions Android. Il n’ajoute pas de description de tâche, programme réutilisable, drawer du timer, hyperfocus ou cycle complet de pauses. Aucun compagnon ni asset tiers n’est importé.
