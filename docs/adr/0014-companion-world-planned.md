# ADR 0014 — « Mon univers » et compagnon timide : architecture prévue, illustrations en attente

**Décision produit acceptée le 17 novembre 2026 ; implémentation non commencée.**
Épic de suivi : [#68](https://github.com/jasmin-abernathy/executive-function-app/issues/68). Détail opérationnel : [feuille de route](../roadmap/2026-11-17-companion-world.md).

## Source graphique unique — à ne pas élargir

Seul document autorisé pour **l'identité visuelle du compagnon** : `compagnon_timide_silhouettes2.pdf`, fourni par le commanditaire et réalisé par Charly.

- **Page 1** : trois états (basse énergie, neutre, actif) ; même silhouette reconnaissable d'un état à l'autre et lisible en miniature ; activité/occupation à terme.
- **Page 2** : corps arrondi et pétales ; réduction du nombre de pétales pour la lisibilité.
- **Page 3** : personnage quasi fermé en basse énergie, mi-ouvert au repos/neutre, en mouvement lorsqu'il est actif ; projections en miniatures tracées ou remplies.
- **Page 4** : pétales servant de membres pour se lever, saisir, manipuler et pousser des objets.

Ces pages **ne sont pas** des décors complets, des inventaires d'objets, des poses d'animation finalisées ou une demande d'inventer d'autres formes. **Aucun autre PDF/visuel ne doit guider la création.** Le logo neutre déjà intégré à Android demeure tel quel. Le PDF original n'est pas ajouté au dépôt public ; il appartient à l'auteur, et l'autorisation de l'icône n'est pas une licence générale des illustrations futures.

## Choix produit

1. **Deux destinations de l'espace d'accueil** : `Aujourd'hui` (défaut et chemin le plus court vers capture et focus), et `Mon univers` (optionnel). Pas de nouvel écran imposé au premier lancement ; ne jamais remplacer la vue des tâches par le monde.
2. **Un univers persistant mais facultatif**, avec un espace initial limité, des objets que l'utilisateur pourra disposer et un compagnon qui réagit à un état de session. Les décors et les objets devront être dessinés et validés ultérieurement.
3. **Un petit aperçu facultatif** sur `Aujourd'hui` après les actions essentielles, sans repousser le premier geste, les notes ou la tâche en cours en dessous du premier écran.
4. **Un compagnon flottant distinct de l'univers complet** : silhouette seule, transparente, mobile, masquable. Lorsqu'une session est en cours, il paraît actif ; en cas d'interruption, il paraît au repos ; lorsqu'il n'y a pas de session, l'overlay disparaît.
5. **Les modes existants restent disponibles** : écran de focus, notifications, PiP (sans permission de superposition) et minuteur flottant classique. Le personnage flottant est **opt-in**, jamais un remplacement forcé ni une nouvelle permission automatique.
6. **Aucune sanction de l'absence** : pas de série, points de rendement, niveau obligatoire, objet retiré, personnage attristé ou progrès perdu. L'aménagement libre ne dépend pas exclusivement des tâches achevées ; toute animation, suggestion ou création d'objet est optionnelle.

## Contrat de présentation de l'état

L'état du compagnon est **dérivé** d'une session existante, jamais stocké comme un nouveau minuteur :

| État de focus réel | Présentation de principe | Portée |
| --- | --- | --- |
| `RUNNING` | `ACTIF` (p. 1 / p. 3) | Monde, aperçu si souhaité, overlay optionnel |
| `INTERRUPTED` | `REPOS`, quasi fermé (p. 3) | Monde, aperçu, overlay optionnel ; temps de travail arrêté |
| Aucun focus actif / focus terminé ou reporté | `NEUTRE`, mi-ouvert (p. 3) | Monde et aperçu ; overlay absent |

Le mot « basse énergie » nomme une **pose du dessin** ; la posture au repos ne révèle ni ne diagnostique l'état de santé ou l'humeur de l'utilisateur. Aucun check-in obligatoire ni inference psychologique.

## Impact Android anticipé (pas encore codé)

- `MainActivity.kt` / `HomeScreen` / `WorkspaceHome` : navigation d'accueil limitée à deux destinations, monde accessible volontairement, écran de focus et PiP inchangés par défaut.
- Un composant Compose `CompanionWorldScreen`, puis `CompanionPreview` et éventuellement `CompanionSprite` après réception des dessins. Aucun faux avatar en attendant.
- Un **résolveur d'état pur** depuis `AppSnapshot.activeFocus` et `FocusStatus`, sans temporisation ni écriture par seconde.
- Données d'aménagement local en SQLite : configuration réversible, identifiants d'objets, positions relatives et inventaire cosmétique (schéma à figer lors de l'implémentation). Intégrer à l'export/restauration JSON **après synchronisation avec la version courante de la base**, notamment la PR #66 si elle est fusionnée avant. Migration testée et restauration des anciennes sauvegardes.
- `FocusOverlayService.kt` : actuellement le ticker éteint la superposition dès que l'état n'est plus `RUNNING`, et `MainActivity` désactive la préférence de superposition lors de l'interruption. Adapter **les deux** seulement au moment de la mise en œuvre du compagnon flottant, en gardant le mode classique inchangé par défaut. Une session `INTERRUPTED` n'accumule pas de temps actif.
- Conserver **PiP ≠ overlay** (ADR 0009). Autorisation `SYSTEM_ALERT_WINDOW` sur action explicite ; fenêtre transparente avec emprise tactile limitée, position sûre et action « Masquer » accessible, sans blocage des autres applications.
- Aucun compte, réseau, SDK de télémétrie, service de jeu, synchronisation ni moteur d'animation supplémentaire nécessaires au socle.

## Contrats de sobriété et d'accessibilité

Le compagnon ne masque pas les commandes essentielles. Fonctionnement normal avec l'univers ou la superposition désactivés, permissions refusées, économie d'énergie, animations réduites, police agrandie et lecteur d'écran. L'état reste compréhensible sans animation ni couleur. Les paramètres de préférence et le monde restent locaux, supprimables, exportables. Le personnage ne tient aucun discours thérapeutique.

## Séquencement et limite financière

**Dès maintenant :** documentation, tickets, conventions et spécifications. **Techniquement possible plus tard sans visuel :** résolveur pur, tests, modèle local et sauvegarde. **Bloqué jusqu'au financement et à la livraison du second devis :** décors, objets, animations, écran `Mon univers` visible, compagnon animé/flottant. Ne pas déployer un onglet vide ni générer des visuels de substitution.

Aucune date, commande ou paiement n'est présumé. Les devis et le travail artistique restent sous contrôle humain. Réévaluer les besoins graphiques en fonction des ressources réellement livrées.
