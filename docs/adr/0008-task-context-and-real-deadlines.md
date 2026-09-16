# 0008 — Thèmes, repères temporels et réglages de tâche explicites

Décision du 12 septembre 2026. Ce lot prolonge l'organisation locale sans transformer l'application en agenda rigide ni inventer de pression artificielle.

## Carte et réglages

Toucher une carte de tâche ouvre ses réglages. Le bouton « Commencer » reste distinct et visible ; la poignée `⋮` reste réservée au réordonnancement. Le résultat du D10 conserve son exception actuelle : toucher directement la tâche proposée peut la démarrer, car le lancer puis le toucher constituent deux actions volontaires.

L'écran de réglages d'une tâche regroupe progressivement : titre, premier pas, étapes, thème, type/programme réutilisable, importance, énergie, contexte, couleur, répétition, démarrage immédiat, « Prévoir de commencer à… », « À terminer pour… », personne/engagement externe, durée apprise ou référence manuelle, notifications, puis les actions secondaires de renommage, fin et suppression.

## Thèmes

Les thèmes intégrés sont : Travail, Perso, Maison, Administratif, Santé, Relations et Créatif. Un thème personnalisé est possible. Le thème est choisi par l'utilisateur ; aucun thème n'est automatiquement interprété comme plus facile, plus urgent, plus sain ou plus approprié à un état donné.

L'application pourra apprendre progressivement quels thèmes ont effectivement été accessibles dans des contextes et check-ins comparables, uniquement à partir d'observations locales. Cet apprentissage devra rester visible, corrigeable, ignorable et réinitialisable dans Suivi. Il constitue un indice de suggestion, jamais un diagnostic ni une règle psychologique prédéfinie.

## Deux repères temporels distincts

« Prévoir de commencer à… » est un rendez-vous de départ modifiable. Il ne devient jamais une fausse échéance et peut être déplacé ou supprimé sans modifier « À terminer pour… ».

« À terminer pour… » est une vraie deadline fournie par l'utilisateur. Une deadline doit rester visible dans les écrans où la tâche est présentée. L'application ne crée jamais de deadline fictive.

Lorsqu'une durée apprise ou manuelle existe et qu'une vraie deadline est définie, l'application peut calculer un « dernier démarrage confortable » : `deadline - durée estimée`. Cette valeur est informative et ne déplace ni le rendez-vous de départ ni la deadline.

Sans deadline, une suggestion « dans 5 minutes » peut être proposée uniquement comme option explicite. Elle ne doit pas être enregistrée ou appliquée silencieusement.

## Personne ou engagement externe

Une tâche peut indiquer une personne attendue, une personne à qui répondre ou un engagement externe. Ce champ sert à rendre le contexte visible et à expliquer une suggestion ; il ne sert pas à produire une pression artificielle.

## Suggestions

Les futures suggestions peuvent combiner des règles explicables : contexte compatible, durée connue par rapport au temps disponible, importance choisie, vraie deadline et, lorsqu'il existe suffisamment d'observations, accessibilité constatée d'un thème dans des états comparables.

Aucune de ces règles ne réordonne silencieusement la liste. Un ordre proposé doit rester volontaire, comme les mécanismes existants. Le D10 doit expliquer son pool de tâches admissibles lorsque des filtres réduisent les choix.

## Données prévues

Le prochain schéma local ajoute au minimum à la planification de tâche : thème, type/programme, rendez-vous de départ, vraie deadline et engagement/personne externe. Le thème utilisé au début d'une session doit être figé avec le contexte de session afin qu'un renommage ou changement de thème ultérieur ne réécrive pas l'observation historique.

La migration prévue est additive (SQLite 4 → 5). Les sauvegardes version 5 contiennent ces nouveaux champs. La restauration d'une sauvegarde version 4 reste acceptée avec des valeurs neutres pour les nouveaux champs ; aucune ancienne sauvegarde valide ne doit être rejetée uniquement à cause de ce lot.

## Accessibilité et garde-fous

- la deadline n'est jamais communiquée uniquement par une couleur ;
- le libellé TalkBack d'une carte indique clairement l'ouverture des réglages, distincte de « Commencer » et de la poignée de déplacement ;
- les thèmes utilisent des libellés textuels ;
- aucune suggestion ne dépend d'une animation ou d'un geste précis ;
- les apprentissages par thème restent consultables et réversibles ;
- aucune psychologie préprogrammée, deadline fictive ou réorganisation silencieuse.

## Hors de cette décision

Cette décision ne transforme pas les rendez-vous de départ en calendrier externe, ne crée pas de synchronisation cloud et ne définit pas encore de moteur de programme/routine complet. Les phases avancées du timer et l'hyperfocus restent des lots distincts.
