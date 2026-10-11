# Retours du test Android — 11 octobre 2026

- Création et sélection d’un dossier depuis une note sans perdre le brouillon. Rangement de tâches et notes existantes depuis le dossier, avec déplacement explicite et retrait sans suppression.
- Espacements augmentés. Huit couleurs pastel supplémentaires ; même palette dans les deux thèmes et texte foncé sur les cartes claires. Choix aléatoire à la création, choix explicite conservé.
- Confirmation du comptage à la création et au lancement. Alternatives dépliées sur demande ; choix propre à la tâche, sans modification du réglage général.
- Retour à l’accueil pendant un timer, bandeau de retour au suivi et tiroir des notes du dossier sur le timer plein écran.
- Mode « Une chose à la fois » après une humeur ou une énergie de niveau 0 ou 1 : une proposition sur l’accueil, dé limité aux tâches d’effort 0 ou 1. Toutes les tâches restent accessibles manuellement. Le timer est inchangé. Un nouveau point d’état remplace le précédent ; expiration après quatre heures.
- Date repère facultative dans la fiche : contour violet progressif, sans clignotement. Fenêtre de 7 jours si la date est fixée dans les 14 prochains jours, sinon 14 jours. Les libellés restent factuels et la date peut être déplacée ou retirée.

## Données

Migration additive SQLite 5 → 6 : table task_options pour dates et comptage individuel. Les sauvegardes 4 et 5 restent importables ; les exports 6 conservent ces nouvelles options. Aucun transfert réseau ajouté.

## Validation

Contrôles locaux : données SQLite et parité des ressources FR/EN. Gradle ne peut pas être téléchargé dans cet environnement ; la CI Android du SHA final fait foi. Tests ajoutés : seuils et expiration du mode allégé, timer conservé lors d’un point d’état, fenêtres de mise en évidence, migration 5 → 6, options indépendantes et restauration des sauvegardes.

À vérifier sur téléphone : clavier et annulation des dialogues, tiroir de notes, retour accueil/timer, calendrier, grande police et lisibilité des couleurs.
