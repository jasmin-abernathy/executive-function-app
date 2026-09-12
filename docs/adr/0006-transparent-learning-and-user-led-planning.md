# 0006 — Suivi transparent et organisation volontaire

Décision du 11 septembre 2026. Cette tranche complète le noyau Android ; elle n'intègre aucun compagnon ni illustration externe.

## Organisation

Les couleurs choisies restent conservées. L’ordre est modifiable manuellement ou par un mélange explicitement demandé au D10 facultatif ; les tâches non admissibles conservent leur ordre relatif. Voir [ADR 0007](0007-d10-and-manual-order.md). L'adaptation est désactivée par défaut : après activation, énergie disponible, durée connue et contexte filtrent les propositions. Une durée inconnue n'exclut pas une tâche. Un bouton distinct applique volontairement l’ordre suggéré par importance ; le mélange au dé reste aléatoire parmi les tâches admissibles.

La section « Tâches et suivi » donne accès aux étapes cochables et réordonnables, au premier pas, au repère Aujourd'hui, aux occurrences terminées et à la répétition. Aujourd'hui correspond à une date locale, sans report automatique. Une répétition simple, tous les N jours, matérialise au prochain chargement au plus une occurrence ouverte par famille. Aucun arriéré n'est créé après une absence. Ce mécanisme n'est pas un agenda avec rendez-vous fixes ni un moteur de récurrences complexes.

Les notes ont leur propre section et restent accessibles pendant le focus. Le widget Android propose capture, dé et notes ; il ne lance jamais automatiquement une tâche et ne sonde pas la base périodiquement.

## Apprentissage visible et corrigeable

Après trois occurrences terminées et mesurées, la référence est la plus longue durée active, arrondie à la minute supérieure, puis augmentée d'une minute : 12:59 donne 14 min ; 13:00 donne également 14 min. Les pauses sont exclues. Le temps accumulé avant un report est repris dans l'occurrence suivante de la même tâche, sans additionner plusieurs fois les cumuls intermédiaires.

Le suivi affiche les observations, leur statut, leur maximum et la référence. L'utilisateur peut exclure/réintégrer une observation, définir/retirer une référence manuelle, séparer/regrouper les familles et recommencer les observations avec annulation possible. Les mesures brutes restent inchangées. Une référence manuelle reste prioritaire après une remise à zéro des observations. Les corrections affectent les futures sessions ; une session déjà démarrée conserve sa cible.

Les check-ins locaux et facultatifs enregistrent humeur, motivation et énergie sur quatre niveaux. L'invitation apparaît uniquement dans l'application, au maximum toutes les quatre heures, et peut être désactivée. Les entrées sont modifiables et supprimables. L'énergie déclarée expire après quatre heures pour les propositions.

Le découpage adaptatif reste explicable : après trois réalisations terminées avec des niveaux comparables (bas/haut) sur les trois dimensions, l'application peut proposer le découpage le plus détaillé observé. Les étapes sont figées dans `session_steps` à la fin de chaque session ; modifier ensuite la tâche ne réécrit pas l'observation. Une ancienne session sans instantané n'invente pas de découpage. La proposition doit être acceptée, et ne remplace pas des étapes déjà saisies. C'est une règle locale fondée sur des observations, pas une conclusion causale ni un modèle médical.

## Temps visible et pauses

Le chronomètre de notification fonctionne indépendamment de la fenêtre flottante, sans rafraîchissement par seconde depuis l'application. Il propose pause/reprise et fin. La fenêtre flottante volontaire reste déplaçable ; le mode Android PiP offre une autre petite fenêtre, avec activation automatique facultative quand on quitte le focus. Le refus d'une permission ne bloque pas le timer principal.

Les propositions de pause ne suspendent jamais la session automatiquement. Leur échéance est conservée en temps actif et reconstruite après reprise/redémarrage. Les alarmes locales sont inexactes : Android peut retarder une notification. Les rappels de tâche proposent Commencer, Fait et Plus tard ; Commencer demande confirmation dans l'application. Aucune surveillance en continu des autres applications.

Le mode calme désactive animation et vibration du dé. Le maintien de l'écran allumé est facultatif et limité à la session active. Les couleurs du timer flottant suivent les variantes claires/sombres.

## Données et récupération

Migration SQLite 3 → 4 additive, sans supprimer les tâches et sessions existantes. Les nouvelles tables stockent uniquement des données locales. Aucune permission réseau n'est ajoutée.

L'export JSON explicite comprend tâches, sessions, étapes, apprentissage, check-ins, notes, répétitions et rappels. Il est en clair ; les préférences d'affichage ne sont pas incluses. La restauration demande confirmation et valide format, version, colonnes, types, relations et états dans une transaction. Une erreur conserve les données précédentes. Une session exportée en cours revient interrompue à l'instant de l'export, sans compter les jours passés dans le fichier de sauvegarde. La suppression d'une occurrence active est refusée ; l'effacement intégral demande confirmation.

## Validation et livraison

La vérification Android utilise `:app:testDebugUnitTest :app:lintDebug`, sans assemblage d'APK. Le workflow ne produit un APK qu'après déclenchement manuel avec `build_apk=true`. Un push normal ne déclenche aucun packaging.

Les scripts locaux vérifient les requêtes SQLite réelles, les ressources FR/EN, le XML et la syntaxe Kotlin. Les tests unitaires couvrent le temps, les transitions, le dé et les filtres. Les permissions, PiP, les interactions tactiles/TalkBack, la mise en veille et les différences entre fabricants nécessitent encore une validation sur appareil lors d'un essai d'APK expressément demandé.

Hors de cette tranche : compagnon, identité illustrée, calendrier externe, PC, wearables et règles de planification avancées.
