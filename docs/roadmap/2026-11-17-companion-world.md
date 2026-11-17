# Feuille de route — « Mon univers » du compagnon timide (17 novembre 2026)

**Statut : prévu, non implémenté.** Ne pas construire un univers factice en attendant le budget du second devis.
Décision : [ADR 0014](../adr/0014-companion-world-planned.md) · Épic [#68](https://github.com/jasmin-abernathy/executive-function-app/issues/68).

## Ce qui existe déjà dans le dépôt

- `HomeScreen` + `WorkspaceHome` : tâches, notes et thèmes, avec actions rapides en premier. **Pas d'onglet monde aujourd'hui.**
- `FocusSession` / `FocusStatus` / `AppSnapshot` : moteur local de focus et transitions `RUNNING` / `INTERRUPTED`, minutes calculées depuis les horodatages.
- `FocusOverlayService` : timer flottant **rectangulaire** activé à la demande et déplaçable ; actuellement arrêté quand le focus n'est pas `RUNNING`. PiP existe séparément.
- `assets/compagnon-timide-neutre.svg` : **logo provisoire uniquement**, extraction autorisée de la silhouette neutre de la page 3 du PDF. **Ce fichier n'est pas une bibliothèque de poses ni de décors.**
- `docs/PRODUCT_PRINCIPLES.md` § 7 : couche ludique facultative, jamais indispensable aux fonctionnalités de base.

## Parcours cible, sans changer le cœur TDAH

```text
Application
├─ Aujourd'hui [par défaut]
│  ├─ Ajouter une tâche / Ajouter une note [prioritaires]
│  ├─ Tâche en cours / prochain petit pas / minuteur
│  ├─ Notes et thèmes existants
│  └─ Aperçu réduit de l'univers [facultatif, pas avant les actions]
├─ Mon univers [nouvel onglet facultatif lorsque les visuels existent]
│  ├─ Un environnement initial aménageable
│  ├─ Compagnon (neutre / actif / repos)
│  ├─ Objets décoratifs et placement libre [pas de score]
│  └─ Options : affichage, mouvements, retour à Aujourd'hui
└─ Focus / pause / reprise [écrans existants conservés]
   └─ Option superposition : minuteur classique OU personnage flottant
```

Navigation « Aujourd'hui » ↔ « Mon univers » uniquement dans le périmètre accueil. Le bouton Retour d'Android revient à « Aujourd'hui », pas à une tâche cachée ; un focus actif continue normalement, quel que soit l'onglet consulté. Aucun assistant obligatoire et aucune création d'objet forcée au premier démarrage.

## Source graphique unique et périmètre exact

Le **seul** document admis pour le compagnon est `compagnon_timide_silhouettes2.pdf` de Charly :
- p. 1 : reconnaissable dans les trois états « basse énergie », « neutre », « actif » ;
- p. 2 : corps doux et pétales peu nombreux pour petite taille ;
- p. 3 : repos quasi fermé, neutre mi-ouvert, actif mobile et déclinaisons miniatures ;
- p. 4 : pétales capables d'attraper et pousser des objets.

**Hors source et donc non validés** : paysages/pièces, couleurs de décor, inventaire d'objets, métiers, gestes précis de bricolage, poses intermédiaires, animations, systèmes de progression graphiques. Tout cela dépend d'une création/validation artistique ultérieure. Aucun visuel d'un autre projet, aucun dessin IA, aucune retouche de Charly.

## Phases de mise en œuvre

### A — Maintenant : planification sans nouvelles dépenses [réalisée lors du dépôt de ce document]

- [x] Enregistrer la source visuelle unique, le périmètre du compagnon et le blocage financier.
- [x] Préciser la navigation cible, les états, la réutilisation du focus et de l'overlay.
- [x] Créer les tickets séparés sans date ni achat engagé : [#69](https://github.com/jasmin-abernathy/executive-function-app/issues/69), [#70](https://github.com/jasmin-abernathy/executive-function-app/issues/70), [#71](https://github.com/jasmin-abernathy/executive-function-app/issues/71), [#72](https://github.com/jasmin-abernathy/executive-function-app/issues/72).
- [ ] Réévaluer le lot technique uniquement lorsque le travail reprend, depuis la branche `main` réelle.

### B — Socle technique préparatoire [#69, faisable sans illustration ; non commencé]

1. Introduire un `CompanionPresentationState` pur, alimenté **uniquement** par le statut du focus (neutre/actif/repos). Jamais un deuxième chrono ou un sondage caché.
2. Définir les préférences distinctes : `universeEnabled`, `homePreviewEnabled`, `floatingStyle = CLASSIC|COMPANION`, `animationEnabled`, `timerLabelVisible`. Respecter le mode calme existant et l'accessibilité.
3. Prévoir un modèle `WorldState` local à **un seul** environnement initial : objets décoratifs identifiés, emplacement normalisé, z-index facultatif, réinitialisation et **création facultative en cours** (identifiant d'objet, état persistant). Durant un focus, le compagnon peut paraître s'en occuper ; durant une pause, il cesse visuellement et l'avancement reste intact. Les déclencheurs de progression restent réversibles et à valider avec les personnes testant l'application : **pas de rendement horaire, de quota, ni de déblocage par tâche obligatoire**.
4. Ajouter **après stabilisation des migrations en cours** les tables nécessaires à `AppDatabase` et `LocalBackup` ; export vN, restauration atomique compatible avec versions antérieures, suppression complète.
5. Tests du résolveur, des préférences et de la restauration ; ne pas activer d'onglet vide en production.

### C — Livraison artistique quand le second devis devient possible [#72, bloquée]

**Premier lot minimal à faire préciser à Charly, sans lui commander quoi que ce soit maintenant :**
- poses finales exploitables séparément : neutre, actif/occupé, repos/quasi fermé (y compris en petite taille, et statiques) ;
- éventuellement éléments des pétales/objets en calques séparés pour mouvement **ultérieur**, sans exiger de l'animation dès le départ ;
- **un seul** décor initial et quelques objets aménageables, tous non présents dans le PDF actuel ;
- fichiers transparents/optimisés, dimensions, points d'ancrage et ordre des calques, variantes adaptées aux fonds clair et sombre si nécessaires ;
- conditions de réutilisation et crédit pour **ces nouveaux fichiers** à confirmer indépendamment de l'autorisation du logo.

**Pas de placeholder dessiné par IA et pas d'assets non commandés.**

### D — Interface « Mon univers » + aperçu [#70, après C]

- Installer un sélecteur de deux destinations limité à l'accueil. « Aujourd'hui » reste valeur par défaut.
- Écran Compose utilisant **uniquement les assets livrés** ; environnement local persistant, déplacement d'objets facultatif, invitation non intrusive à l'aménagement.
- Si Charly livre une représentation de fabrication : une seule création facultative « en cours » à la fois ; l'utilisateur peut en reprendre l'aménagement plus tard, aucune disparition pendant une absence. **Ne pas inventer l'objet ou l'animation tant que les fichiers ne sont pas disponibles**.
- Aperçu discret après les commandes essentielles (ou masqué au choix) ; aucune notification « reviens voir ton monde ».
- Pas de pénalité après absence, pas de points, de scores, de série quotidienne, de mesure « meilleur travailleur ». Le décor n'est pas la contrepartie obligatoire d'un focus.
- Si les assets manquent, **ne pas montrer l'onglet**, conserver l'interface existante.

### E — Compagnon flottant [#71, après C, indépendant de D]

- Proposer `CLASSIC` par défaut, `COMPANION` en option, `OFF` à tout moment ; conserver PiP distinct, sans autorisation de superposition.
- Dessiner le compagnon avec fenêtre transparente mobile et surface tactile réduite ; prévoir un moyen évident de fermer/masquer et une option compteur court.
- Adapter `FocusOverlayService` **et** le chemin de désactivation dans `MainActivity` : running → occupé ; interrompu → repos ; aucune session/fermeture → disparu. Un focus en pause **n'accumule pas** de temps.
- Protéger la permission `SYSTEM_ALERT_WINDOW` (demande volontaire), la notification de premier plan et les règles Android des dernières versions prises en charge ; refuser la permission ne casse rien.
- Pas d'animation requise pour le fonctionnement ; réduction de mouvement toujours opérationnelle.

## Tests d'acceptation (avant toute sortie)

| Parcours | Critère |
| --- | --- |
| Utilisateur sans compagnon | Aujourd'hui, notes, tâches, focus, D10 et exports inchangés |
| Aucun asset nouveau | Aucune page vide ni silhouette artificielle affichée |
| Focus en cours | Compagnon actif sans masquer les actions ; temps de focus exact |
| Pause et reprise | Compagnon au repos puis actif ; aucun temps de pause comptabilisé |
| Fermeture du focus | Overlay absent et état neutre dans l'univers |
| Gestion de permission | Refus de superposition accepté ; PiP et minuteur principal fonctionnent |
| Données | Export/import, anciennes sauvegardes, effacement et reprise après mort du processus |
| Confort | TalkBack, agrandissement 200 %, petite largeur, mode sombre, réduction des mouvements, batterie |
| Progression | Utilisable sans achever de tâche ni revenir quotidiennement ; aucune punition |
| Droits d'auteur | Seuls assets officiels autorisés ; pas de modification ni transfert de droits présumé |

## Conditions de départ / non-objectifs

Pas de budget ni d'engagement de Charly supposés. Pas de date de livraison promise. Pas de multi-pièces, personnalités multiples, réseau, « companion AI », marketplace ou contenu premium à ce stade. Les fonctions de focus, d'accessibilité et le **socle gratuit** restent indépendants de l'univers ; d'éventuels cosmétiques supplémentaires sont une piste ultérieure, jamais un blocage des fonctionnalités de base.
