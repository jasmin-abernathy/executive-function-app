# ADHD / Executive Function App — working repository

> **Working title only.** The final product name has not been selected yet.

[Français](#français) · [English](#english)

## Français

Application Android **local-first**, non punitive, pensée pour réduire le coût de **commencer, continuer et reprendre** lorsqu'une fonction exécutive est difficile à mobiliser.

Le projet ne cherche pas à devenir une suite de productivité surchargée. Sa première boucle de valeur est volontairement étroite :

**Capturer → choisir → commencer → interrompre/reprendre → revenir sans pénalité.**

### État actuel

Les principes produit et la recherche utilisateur sont déjà avancés, mais l'application principale est encore au stade **cadrage / préparation du MVP**. L'univers visuel, le compagnon, la maison et les intégrations avancées ne sont pas des prérequis pour valider la boucle centrale.

Le dépôt sépare donc :

- **P0 — cadrage et garde-fous** ;
- **P1 — MVP fonctionnel** ;
- **P2 — prototype avancé** ;
- **P3 — bêta documentée** ;
- **P4 — après validation seulement**, notamment les candidats premium, qui restent des hypothèses économiques et produit, pas des promesses.

Voir [`ROADMAP.md`](ROADMAP.md) et [`docs/MVP.md`](docs/MVP.md).

### Promesse produit

L'application doit rester utilisable lorsque **planifier est déjà une tâche difficile**.

Règles de base :

- une action utile doit demander très peu de gestes et de décisions ;
- une interruption ou une absence ne doit jamais effacer la progression ;
- pas de streak cassé, boucle de culpabilité, classement ou mécanique punitive ;
- la complexité avancée reste cachée tant qu'elle n'est pas demandée ;
- le cœur de l'application fonctionne localement, sans compte ni cloud obligatoires ;
- l'accessibilité et l'organisation essentielle ne sont pas payantes ;
- la couche ludique reste facultative ;
- le compagnon éventuel est un personnage de jeu, **pas un thérapeute IA ni un faux ami synthétique** ;
- aucune publicité ;
- aucune IA générative intégrée au cœur de l'application.

Voir [`docs/PRODUCT_PRINCIPLES.md`](docs/PRODUCT_PRINCIPLES.md).

### Ce qui doit rester gratuit

Le cœur gratuit comprend ce qui rend réellement l'application utilisable en cas de difficultés exécutives :

- minuteur et commandes de focus essentielles ;
- capture rapide ;
- interruption / reprise ;
- retour non punitif ;
- données local-first ;
- réglages d'accessibilité et palettes de lisibilité ;
- organisation essentielle des tâches ;
- notifications locales de base ;
- export, sauvegarde, restauration et suppression des données.

Les candidats premium sont limités à de la puissance supplémentaire, des intégrations coûteuses à maintenir ou des cosmétiques facultatifs. Voir [`docs/PREMIUM_BOUNDARY.md`](docs/PREMIUM_BOUNDARY.md).

### Vie privée

Architecture cible :

- aucun compte requis pour le cœur de l'application ;
- aucun tracking par défaut ;
- le contenu des tâches reste sur l'appareil sauf action explicite de l'utilisateur ;
- accès aux calendriers externes facultatif et granulaire ;
- les réponses brutes aux questionnaires et les informations de participants ne sont **jamais stockées dans ce dépôt** ;
- un rapport de bug ne doit jamais joindre silencieusement le contenu des tâches.

Voir [`PRIVACY.md`](PRIVACY.md) et [`docs/DATA_MAP.md`](docs/DATA_MAP.md).

### Organisation du dépôt

```text
.github/              modèles d'issues/PR et contrôle d'hygiène du dépôt
app/                  emplacement du projet Android quand l'implémentation commence
assets/               ressources visuelles avec licences documentées uniquement
docs/                 produit, architecture, accessibilité, décisions et publication
github/               labels, milestones et premières issues à créer
research/             résultats publics/anonymisés uniquement — jamais de données brutes
scripts/              contrôles locaux simples
```

### Premier push avec Gitling

Voir [`docs/GITLING_WORKFLOW.md`](docs/GITLING_WORKFLOW.md). Le kit est conçu pour pouvoir être décompressé/copier dans le clone local, puis **Stage all → vérification du diff → commit → push**.

### Licence

Le code du projet est destiné à être distribué sous **GNU AGPL v3, version 3 uniquement (`AGPL-3.0-only`)**. Le fichier `LICENSE` est encore un placeholder dans ce bootstrap privé : avant toute publication, remplace-le par le texte GNU officiel via `scripts/fetch-agpl-license.*`. Les marques, noms et éléments graphiques peuvent avoir des règles distinctes : voir [`TRADEMARKS.md`](TRADEMARKS.md) et [`docs/LICENSING.md`](docs/LICENSING.md).

### Recherche et niveau de preuve

Le produit est informé par la co-conception, la recherche utilisateur et la littérature disponible. Il ne doit **pas** être présenté comme cliniquement validé tant qu'une évaluation future ne le permet pas.

Formulation préférée : **« co-conçu et informé par la recherche utilisateur et les recommandations disponibles »**.

Voir [`docs/RESEARCH_POLICY.md`](docs/RESEARCH_POLICY.md).

---

## English

A **local-first, non-punitive Android app** designed to reduce the cost of **starting, continuing and returning** when executive function is low.

The project is not trying to become a feature-heavy productivity suite. Its first value loop is intentionally narrow:

**Capture → choose → start → interrupt/resume → return without penalty.**

### Current status

Product principles and user research are already advanced, while the main app is still at the **framing / MVP preparation** stage. The visual world, companion, house and advanced integrations are not prerequisites for proving the core loop.

The repository separates:

- **P0 — framing and safeguards**;
- **P1 — functional MVP**;
- **P2 — advanced prototype**;
- **P3 — documented beta**;
- **P4 — only after validation**, including premium candidates, which are product/economic hypotheses rather than promises.

See [`ROADMAP.md`](ROADMAP.md) and [`docs/MVP.md`](docs/MVP.md).

### Product promise

The app should remain usable when **planning itself feels difficult**.

Core rules:

- one useful action should require very few gestures and decisions;
- interruptions and absences never erase progress;
- no broken streaks, guilt loops, leaderboards or punishment mechanics;
- advanced complexity stays hidden until requested;
- the core works locally without a mandatory account or cloud dependency;
- accessibility and essential organization features are not paywalled;
- the game layer is optional;
- a future companion is a game character, **not an AI therapist or synthetic friend**;
- no advertising;
- no generative AI in the app core.

See [`docs/PRODUCT_PRINCIPLES.md`](docs/PRODUCT_PRINCIPLES.md).

### Privacy model

The target architecture is local-first: no account required for the core app, no tracking by default, task content kept on-device unless the user explicitly exports or sends it, granular optional external-calendar access, and no raw participant data in this repository.

See [`PRIVACY.md`](PRIVACY.md) and [`docs/DATA_MAP.md`](docs/DATA_MAP.md).

### Gitling workflow

See [`docs/GITLING_WORKFLOW.md`](docs/GITLING_WORKFLOW.md). The repository bootstrap is designed to be copied into a local clone, reviewed in Gitling, staged, committed and pushed without requiring a desktop Git client.

### Licence

Project code is intended to use **GNU AGPL v3 only (`AGPL-3.0-only`)**. The bootstrap `LICENSE` file is still a private-repository placeholder; replace it with the official GNU text before publication using `scripts/fetch-agpl-license.*`. Names, logos and visual identity may have separate rules; see [`TRADEMARKS.md`](TRADEMARKS.md).
