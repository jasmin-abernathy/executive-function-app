# Executive Function App

**🇫🇷 Français** · [🇬🇧 English](README.md)

> **Nom de travail.** Le nom définitif de l’application est encore en discussion.

**Une application Android local-first et non punitive, pensée pour réduire la friction liée aux fonctions exécutives.**

**Capturer ce qui compte. Choisir une prochaine action réaliste. Commencer avec moins de friction. Revenir sans transformer l’absence en échec.**

---

## Pourquoi ce projet existe

Pour beaucoup de personnes avec TDAH ou difficultés de fonctions exécutives, le problème n’est pas le manque d’outils de productivité.

C’est l’écart entre **savoir quoi faire** et réussir réellement à :

- choisir par où commencer ;
- démarrer sans paramétrage excessif ;
- percevoir le temps pendant l’action ;
- rester sur une tâche ;
- gérer une interruption ;
- reprendre plus tard sans reconstruire tout son système.

Gestionnaires de tâches, agendas, rappels, minuteurs, routines et applications gamifiées répondent déjà chacun à une partie utile du problème.

Dans un corpus communautaire exploratoire utilisé pendant la recherche produit, **14 personnes sur 21 utilisant déjà une app d’organisation utilisaient également au moins un outil d’une autre catégorie**. Il s’agit d’un signal de conception, pas d’une statistique représentative de l’ensemble des personnes TDAH.

Le projet ne cherche donc **pas à créer une nouvelle super-app de productivité**.

Il se concentre sur la continuité entre :

**capture → prochaine action → temps visible → interruption → reprise**

> Le système d’organisation doit être plus facile à utiliser que le problème qu’il essaie de résoudre.

---

## Promesse centrale

> **Aider à commencer, continuer et reprendre — sans transformer l’oubli en faute.**

La première boucle produit est volontairement courte.

### 1. Capturer

Déposer une pensée, une tâche ou une note dans une Inbox ultra-rapide, sans catégorie obligatoire.

### 2. Choisir

Faire émerger une prochaine action réaliste, avec éventuellement des informations comme le temps disponible, l’énergie ou le contexte.

### 3. Commencer

Démarrer avec un timer de concentration plein écran et le moins de décisions possible.

### 4. Reprendre

Lorsqu’une session est interrompue, conserver le contexte utile et proposer quelques choix simples :

**Reprendre · Réduire · Reporter · Changer**

L’interruption, le report et l’absence sont considérés comme des **situations normales**, pas comme des erreurs utilisateur.

---

## Principes produit

### Non punitif par conception

L’application ne doit pas ajouter une nouvelle couche de culpabilité autour de l’organisation.

Cela implique notamment :

- pas de streak cassée comme punition ;
- pas de progression perdue après une absence ;
- pas de compagnon triste ou déçu ;
- pas de check-in quotidien obligatoire ;
- pas d’« échec » simplement parce qu’une tâche est reportée ;
- le temps déjà consacré reste reconnu même si la tâche n’est pas terminée.

Revenir après un jour ou un mois doit ressembler à une **reprise**, pas à un recommencement.

### Local-first

Le cœur utile ne doit pas dépendre d’un compte cloud.

Objectifs actuels :

- données locales par défaut ;
- fonctionnement hors ligne autant que possible ;
- aucun compte obligatoire pour le cœur ;
- export, sauvegarde, restauration et suppression explicites ;
- aucune remontée silencieuse du contenu des tâches dans les diagnostics ;
- intégrations externes facultatives et explicites.

### Respect de la vie privée

Le projet ne repose ni sur la publicité comportementale ni sur la revente des données personnelles de productivité.

L’accès à l’agenda, aux wearables, à un ordinateur ou à d’autres outils ne doit exister que lorsqu’il est explicitement activé.

### L’accessibilité fait partie du cœur

L’accessibilité n’est pas une fonction premium.

Le projet prévoit notamment :

- une hiérarchie visuelle claire ;
- du texte agrandissable ;
- des contrastes suffisants ;
- un mode calme / à stimulation réduite ;
- la réduction des animations ;
- des contrôles visibles en alternative aux gestes ;
- aucune information reposant uniquement sur la couleur ;
- une complexité progressive.

### Moins de friction avant plus de fonctions

Une action utile doit demander très peu de gestes et très peu de décisions.

L’application doit rester utilisable un jour difficile, lorsque **planifier est déjà une tâche en soi**.

### Facultatif veut réellement dire facultatif

Routines, statistiques, notifications, agenda, gamification et compagnon pourront enrichir l’expérience.

Aucune de ces couches ne doit être nécessaire pour utiliser le noyau organisation + concentration.

### Pas de dépendance à l’IA générative dans le cœur

L’expérience essentielle repose sur les données locales et des règles déterministes.

L’application ne doit pas avoir besoin d’une API d’IA générative pour décider ce que la personne devrait faire ensuite.

---

## État actuel du projet

**Stade : prototype Android P1 exécutable.**

Le dépôt contient désormais une application Android reproductible couvrant toute la première boucle :

```text
Capturer → choisir → commencer → se concentrer → interrompre → reprendre → terminer ou reporter
```

Déjà implémenté :

- capture locale ultra-rapide avec premier petit pas facultatif ;
- choix manuel et minuteur plein écran en temps écoulé ;
- capture rapide sans quitter le focus ;
- contexte d’interruption durable et écran de retour non punitif ;
- reprise, réduction, report/changement et fin ;
- première présence interactive du compagnon sur l’accueil et la reprise ;
- persistance SQLite résistante au redémarrage du processus ;
- ressources françaises et anglaises ;
- grandes cibles tactiles, texte agrandissable et sémantique TalkBack ;
- tests unitaires, lint Android et assemblage de l’APK dans la CI.

Il n’y a ni compte, ni permission réseau, ni SDK analytics, ni backend cloud, ni sauvegarde cloud automatique. Il s’agit d’un prototype de validation, pas d’une revendication d’efficacité clinique.

## Ce que le premier prototype doit permettre de valider

Les premiers tests doivent porter sur les comportements réels plutôt que sur le nombre de fonctionnalités.

Par exemple :

- Combien de temps faut-il entre l’ouverture de l’app et le vrai démarrage ?
- La prochaine action est-elle immédiatement compréhensible ?
- Peut-on capturer quelque chose sans casser une session de concentration ?
- Une interruption conserve-t-elle suffisamment de contexte ?
- Le retour après plusieurs jours est-il facile ?
- L’interface génère-t-elle de la culpabilité, de la surcharge ou du temps d’écran indésirable ?
- Les fonctions avancées peuvent-elles rester cachées sans rendre l’app incompréhensible ?

Le projet ne revendique **aucune efficacité clinique**.

Les résultats de recherche sont considérés comme des signaux de conception et des hypothèses à tester.

---

## Direction fonctionnelle

### Cœur / priorité maximale

Le cœur prévu comprend notamment :

- capture ultra-rapide dans une Inbox ;
- vue Aujourd’hui / prochaine action souple ;
- premier geste physique facultatif pour une tâche ;
- réduction facultative d’une tâche en petit pas concret ;
- timer de concentration plein écran ;
- transitions de phase manuelles et prévisibles ;
- note ou tâche rapide pendant une session ;
- état d’interruption et contexte de reprise ;
- rappels locaux avec actions utiles comme **Commencer / Fait / Plus tard** ;
- export, sauvegarde et récupération des données locales ;
- modes accessibilité et calme.

### Très proche du cœur

Éléments à valider autour de cette boucle :

- routines séquentielles ;
- tâches souples autour des vrais rendez-vous fixes ;
- mode basse énergie ;
- sessions longues / hyperfocus intentionnel ;
- check-ins contrôlés par l’utilisateur ;
- export ou affichage léger d’un agenda ;
- widgets et contrôles simples sur wearable ;
- friction douce anti-distraction.

La direction générale privilégie la souplesse plutôt qu’un planning minute par minute qu’un imprévu suffit à casser.

---

## Compagnon et progression visuelle

Un compagnon et une petite couche maison / artisanat sont développés comme **couche de motivation facultative**, pas comme cœur de l’application. Une première silhouette vectorielle provisoire est intégrée à l’accueil : elle réagit au toucher et après une capture réussie, puis reste volontairement absente du mode focus.

La direction actuelle est celle d’un compagnon :

- discret ;
- doux ;
- légèrement étrange ;
- observateur plutôt que coach ;
- facultatif ;
- silencieux lorsque l’utilisateur le souhaite.

Le compagnon ne doit jamais :

- perdre de l’affection parce que l’app n’a pas été ouverte ;
- avoir faim ou devenir triste pendant une absence ;
- culpabiliser l’utilisateur pour le faire revenir ;
- bloquer la capture ou le démarrage d’un timer ;
- devoir être « entretenu » avant que la personne puisse s’occuper de sa vraie vie.

L’effort réalisé dans le monde réel pourra éventuellement produire une progression visuelle durable à travers des objets, de l’artisanat ou l’évolution de son espace.

L’application doit rester totalement utile lorsque cette couche est désactivée.

L’identité graphique et le design définitif du compagnon sont encore en développement. La logique actuelle est conçue pour recevoir les illustrations finales sans modifier le parcours fonctionnel.

La [charte graphique du prototype](docs/VISUAL_DIRECTION.md) fixe la palette d’interface claire, ses dérivés accessibles et la séparation entre interface fonctionnelle et futures illustrations. Un [aperçu HTML interactif de l’accueil](docs/home-preview.html) permet de contrôler cette direction sans compiler l’application.

---

## Possibilités ultérieures

Plusieurs éléments restent volontairement hors du premier chemin critique :

- synchronisation agenda avancée ;
- règles de récurrence plus complexes ;
- relais local avec un ordinateur ;
- automatisations avancées ;
- personnalisation plus riche du compagnon ;
- contenus maison / artisanat supplémentaires ;
- intégrations wearables ;
- liens vers des services externes de body doubling ;
- services hébergés facultatifs lorsqu’ils génèrent réellement un coût d’infrastructure récurrent.

Ce sont des pistes de roadmap, pas des fonctionnalités présentées comme déjà disponibles.

---

## Direction du modèle de données

L’architecture sépare **ce qui est planifié** de **ce qui s’est réellement passé**.

Modèle conceptuel de travail :

```text
Activity
  └─ Schedule
       └─ Occurrence
            └─ Execution
                 └─ FocusSession
```

Modifier une récurrence future ne doit ainsi pas réécrire l’historique réel des exécutions ou des sessions de concentration.

---

## Direction technique

Cible actuelle :

- **Android**
- **Kotlin**
- **Jetpack Compose**
- architecture local-first
- comportements déterministes pour le cœur
- intégrations externes facultatives

Le prototype doit privilégier :

- un état local transparent ;
- peu de permissions ;
- un fonctionnement hors ligne propre ;
- peu d’activité inutile en arrière-plan ;
- des intégrations qui ne deviennent jamais des dépendances cachées.

Compiler localement avec JDK 17 et Android SDK 37 :

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

L’APK de développement est généré dans `app/build/outputs/apk/debug/app-debug.apk`.

---

## Structure du dépôt

```text
.github/   Templates GitHub et métadonnées du dépôt
app/       Application / prototype Android
assets/    Ressources visuelles et éléments du projet
docs/      Documentation produit, architecture et décisions
github/    Roadmap, issues et éléments de pilotage
research/  Recherche et benchmarks pouvant être publiés
scripts/   Outils de maintenance et d’hygiène du dépôt
```

Les réponses brutes du questionnaire, données personnelles, identifiants, secrets et tokens ne doivent **jamais** être commités dans ce dépôt.

---

## Démarche de recherche

Le projet combine :

- recherche utilisateur directe ;
- questionnaires de validation produit ;
- observations publiques de communautés ;
- analyse des applications existantes ;
- recommandations d’accessibilité et de santé numérique utilisées comme garde-fous.

La recherche sert notamment à répondre à des questions concrètes :

- Qu’est-ce qui aide juste avant de commencer ?
- Combien d’informations l’écran Aujourd’hui doit-il afficher ?
- Une capture rapide peut-elle éviter de devenir une nouvelle Inbox oubliée ?
- Comment un timer doit-il réagir à une interruption ?
- Comment accueillir quelqu’un qui revient après plusieurs semaines ?
- Quelles récompenses restent motivantes sans devenir punitives ?
- Quelles intégrations valent réellement leur coût cognitif et technique ?

Ni les observations communautaires ni les questionnaires ne sont présentés comme représentatifs de toutes les personnes TDAH.

---

## Principes du modèle économique

Le projet vise un **cœur gratuit, réellement utile et open source**.

Tout ce qui soutient directement la promesse centrale doit rester accessible :

- organisation essentielle ;
- démarrage ;
- concentration ;
- interruption et reprise ;
- accessibilité ;
- contrôle des données locales.

Des couches payantes pourront éventuellement financer :

- des automatisations avancées ;
- des intégrations ayant un vrai coût de maintenance ;
- des services hébergés ayant un vrai coût récurrent ;
- du support ou des prestations facultatives ;
- des contenus ou cosmétiques non essentiels.

Lorsqu’une fonction ne génère aucun coût serveur récurrent, un achat unique est privilégié à un abonnement forcé lorsque c’est réaliste.

Le projet refuse :

- publicité comportementale ;
- loot boxes ;
- rareté artificielle ;
- compte à rebours FOMO ;
- accessibilité payante ;
- version gratuite volontairement frustrante.

Le payant doit ajouter de la **puissance**, pas de la pression.

---

## Open source et licence

Le cœur logiciel a vocation à utiliser la licence **GNU AGPL v3**.

Voir [`docs/LICENSING.md`](docs/LICENSING.md).

La licence du logiciel ne donne pas automatiquement de droits sur le nom du projet, le logo, l’identité visuelle ou les créations de tiers, sauf mention explicite.

L’open source fait partie de la philosophie du produit :

- auditabilité ;
- pérennité ;
- contribution ;
- interopérabilité ;
- contrôle par l’utilisateur.

---

## Contribuer

Le projet est encore façonné par la recherche et le prototypage.

Toutes les idées présentes dans le backlog ne sont donc pas des fonctionnalités décidées.

Avant de proposer une fonction importante :

1. vérifier la roadmap et les issues existantes ;
2. expliquer quelle friction réelle elle réduit ;
3. décrire la plus petite version utile ;
4. réfléchir à la vie privée et à l’accessibilité ;
5. mesurer la charge cognitive qu’elle ajoute ;
6. éviter d’élargir le produit uniquement parce qu’une autre app possède cette fonction.

Une règle simple peut guider les contributions :

> **Est-ce que ce comportement aide l’utilisateur à retrouver le fil, ou est-ce qu’il lui demande encore quelque chose à gérer ?**

Voir les templates d’issues du dépôt pour le fonctionnement actuel des contributions.

---

## Ce que ce projet n’est pas

Ce projet n’est pas :

- un dispositif médical ;
- un outil de diagnostic ;
- un remplacement d’un accompagnement professionnel ;
- une promesse de « réparer » le TDAH ;
- un service cloud obligatoire ;
- un thérapeute IA ;
- un réseau social ;
- un jeu de productivité qui punit l’absence ;
- une tentative de remplacer tous les outils spécialisés.

C’est une tentative pour rendre une partie importante de l’organisation quotidienne **plus facile à commencer, plus facile à poursuivre et plus facile à reprendre**.

---

## Portage du projet

Le projet est actuellement porté par **Jasmin Lévêque / Le Potager du Web**.

À plus long terme, une structuration coopérative, **Le Verger du Numérique**, est envisagée comme projet de future SCOP pouvant mutualiser développement, design, maintenance et gouvernance entre plusieurs projets numériques.

Il ne s’agit **pas actuellement d’une coopérative déjà constituée**.

---

## Retours

Les désaccords sont utiles.

Si l’application risque de créer pour vous davantage de pression, de paramétrage, de temps d’écran ou de culpabilité, c’est précisément le type de retour dont ce projet a besoin.

L’objectif n’est pas d’apprendre aux gens à entretenir un nouveau système de productivité.

**L’objectif est de faire disparaître une partie de cet entretien.**
