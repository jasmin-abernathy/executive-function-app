# État fonctionnel Android — septembre 2026

Cette liste distingue les comportements présents dans le code des pistes futures. Elle ne présente pas le prototype comme validé sur tous les appareils.

| Demande | État du code |
| --- | --- |
| Capture, focus, interruption et reprise | Présents, avec notes et conservation du temps actif après report |
| Ordre choisi par l'utilisateur | Présent ; suggestion de nouvel ordre uniquement sur action volontaire |
| Dé facultatif D10 en 3D | Surimpression Compose de 500 ms, mode calme ; mélange explicite des tâches admissibles, widget limité à une proposition |
| Couleurs des blocs | Palette choisie par tâche, variantes claires et sombres |
| Pauses proposées après un délai | Délai configurable, continuer ou reporter ; aucune pause forcée |
| Temps visible ailleurs | Notification, fenêtre flottante et PiP facultatifs |
| Apprentissage des durées | Après trois réalisations ; maximum actif arrondi vers le haut + une minute |
| Suivi corrigeable | Observations, exclusions annulables, référence manuelle, familles, remise à zéro annulable |
| Humeur, motivation et énergie | Check-ins locaux facultatifs, modifiables et supprimables |
| Adaptation des propositions | Filtres explicites temps/énergie/contexte et importance, activables |
| Adaptation du découpage | Proposition à accepter à partir d'étapes historiques et d'états comparables |
| Étapes et répétitions | Étapes ordonnées, occurrences manuelles ou tous les N jours au chargement, sans arriéré |
| Aujourd'hui, notes et widget | Présents |
| Sauvegarde, restauration, suppression | Export JSON explicite et restauration atomique ; pas de synchronisation |

La [décision 0006](docs/adr/0006-transparent-learning-and-user-led-planning.md) décrit les règles et limites exactes.

Prochaine validation : parcours sur téléphone, permissions refusées/accordées, verrouillage, reprise après arrêt du processus, notifications/PiP, grandes polices, TalkBack et appréciation des couleurs et de l'animation. Une compilation de contrôle ne valide pas ces usages. Un APK d'essai attend une demande explicite.

Les idées suivantes restent des évolutions distinctes : planification autour de rendez-vous fixes, agenda externe, récurrences complexes, relais PC, wearables, anti-distraction entre applications. Aucun de ces éléments n'est présenté comme déjà implémenté.

## Compagnon et environnement évolutif — prévu, **non implémenté**

Un second espace facultatif « Mon univers », son aperçu discret depuis « Aujourd'hui » et un **compagnon flottant actif/au repos** sont spécifiés dans [la feuille de route du 17 novembre 2026](docs/roadmap/2026-11-17-companion-world.md) et [l'ADR 0014](docs/adr/0014-companion-world-planned.md) (suivi : [épic #68](https://github.com/jasmin-abernathy/executive-function-app/issues/68)). **Source graphique unique : `compagnon_timide_silhouettes2.pdf`.** L'icône neutre existante reste provisoirement en place. Les nouveaux décors, objets et animations sont **en attente du financement et de la livraison du second devis de Charly** ; aucun onglet monde ou mode flottant personnage n'est activé dans le code actuel.
