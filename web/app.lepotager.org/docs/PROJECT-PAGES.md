# Catalogue public du Verger

Les douze cartes de l’accueil (`index.html` et `en/index.html`) renvoient vers leurs fiches statiques sous `projets/<slug>/` et `en/projects/<slug>/`.

La liste et les fiches restent accessibles sans JavaScript ; les filtres sont enrichis via `assets/app.js`. Les cartes sont un seul lien natif couvrant toute la surface : pas d’accordéon mobile et pas de liens imbriqués.

À chaque évolution d’un projet, mettre à jour les deux fiches FR/EN, les statuts des deux accueils, et la date de vérification. Vérifier la maturité dans le dépôt concerné et ne pas présenter les prototypes comme prêts pour la production.

Préserver les dossiers dynamiques (`adhd-app`, `resosoin`, `research`, `soutenir`) lors des déploiements. Vérifier les 24 chemins, l’accessibilité, les liens de retour et les filtres mobiles.
