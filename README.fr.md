# Pixel Trackers Manager

[Read in English](README.md)

Pixel Trackers Manager (PTM) est un assistant WordPress **local-first** d’audit de confidentialité et de documentation, développé par **Le Potager du Web**.

Il aide l’administrateur à comprendre ce que fait réellement son site : repérer les traceurs et services tiers, contrôler les pages légales, documenter les pratiques liées aux données et, s’il le souhaite, gérer le consentement des visiteurs.

> **État actuel :** version de test `0.0.2-test4`. PTM n’est pas un outil de certification juridique et ne remplace pas une analyse adaptée à l’activité réelle de l’organisation.

## Principes du projet

- **Détecter d’abord, demander ensuite.** PTM réutilise les informations déjà présentes dans WordPress avant de les redemander.
- **Local par défaut.** Les résultats d’audit, réglages et réponses restent dans WordPress, sauf action explicite de l’administrateur vers un service externe.
- **Aucune publication juridique silencieuse.** PTM peut préparer des brouillons et proposer des mises à jour, mais les changements publics importants demandent une validation explicite.
- **Pas de dark patterns.** Si l’interface de consentement native est activée, Accepter et Refuser doivent garder le même poids visuel.
- **Moteur indépendant des builders.** Le consentement fonctionne au niveau WordPress ; Elementor, Divi et les autres constructeurs sont des couches de compatibilité, pas des dépendances.
- **Écriture prudente.** Si PTM ne comprend pas assez sûrement la structure interne d’un constructeur, il utilise un shortcode ou un parcours manuel plutôt que de réécrire des données inconnues.

## Ce que PTM couvre actuellement

### Analyse du site

- analyse progressive du site complet ;
- détection des traceurs et services tiers ;
- distinction entre activité réellement observée, intégration disponible et élément restant à vérifier ;
- une URL en erreur n’interrompt pas le reste du scan ;
- les brouillons/pages privées/en attente/programmées peuvent être distingués d’une vraie erreur 404 publique.

### Documentation légale

- pages de référence Mentions légales, Politique de confidentialité et Cookies / Consentement ;
- Assistant RGPD dans un onglet dédié ;
- éléments manquants de la couverture documentaire cliquables vers la bonne section de l’assistant ;
- séparation entre éditeur du site, responsable du traitement, contact public, contact pour l’exercice des droits et DPO réellement désigné ;
- shortcodes publics et création de brouillons ;
- mise à jour explicite plutôt qu’une publication silencieuse.

### Consentement

- barre native ou encart centré facultatif ;
- blocage par défaut des services facultatifs reconnus une fois la fonction activée ;
- même poids visuel pour Tout accepter et Tout refuser ;
- contrôle permanent « Gérer mes choix » ;
- mode de test administrateur et travail de compatibilité avec les modules dynamiques des builders.

### Compréhension de l’écosystème WordPress

PTM peut exploiter ou inspecter, de manière prudente, des informations provenant de WordPress/Gutenberg, Elementor, Divi, certains plugins de formulaires, de sauvegarde, de consentement ou d’analytics.

Le projet prévoit aussi de couvrir les pratiques qui ont lieu **en dehors de WordPress** lorsque le site ne peut pas les révéler : envois groupés depuis Gmail/Outlook, contacts WhatsApp, outils de réservation comme Calendly/Koalendar, formulaires externes, HelloAsso, paiements, tableurs ou stockage cloud.

## Installation de test

1. Placez le dépôt dans `wp-content/plugins/pixel-trackers-manager/`, ou construisez un ZIP du dossier du plugin.
2. Activez **Pixel Trackers Manager** dans WordPress.
3. Ouvrez PTM une première fois pour lancer l’assistant de configuration.

Prérequis de la version de test :

- WordPress 6.5+
- PHP 7.4+

## Documentation

- [`README.md`](README.md) — présentation du projet en anglais.
- [`docs/USER-GUIDE.fr.md`](docs/USER-GUIDE.fr.md) — guide utilisateur français.
- [`docs/USER-GUIDE.md`](docs/USER-GUIDE.md) — guide utilisateur anglais.
- `docs/TESTS-0.0.2-test4.md` — checklist de validation actuelle.
- `docs/CAHIER-DES-CHARGES.md` — cahier des charges et feuille de route interne.
- `changelog.txt` — historique détaillé des versions de test.
- [`CONTRIBUTING.fr.md`](CONTRIBUTING.fr.md) — guide de contribution en français.
- [`SECURITY.fr.md`](SECURITY.fr.md) — politique de signalement des vulnérabilités.

## Développement

Le dépôt sert actuellement au développement pré-publication. La branche de référence doit rester installable ; les changements non terminés devraient, lorsque possible, passer par une branche de développement ou de fonctionnalité.

Avant de retenir une version candidate à la publication, PTM doit au minimum passer :

- les contrôles de syntaxe PHP ;
- les contrôles de syntaxe JavaScript ;
- des tests d’activation/désactivation WordPress ;
- la checklist manuelle de la version courante ;
- les vérifications spécifiques WordPress.org lorsque nous sélectionnerons une build pour le catalogue.

## Signaler un problème

Utilisez les modèles d’issues GitHub pour les bugs reproductibles et propositions d’amélioration. Ne publiez jamais de mots de passe, clés API, données personnelles ou exports de sites clients dans une issue publique.

Pour une vulnérabilité pouvant exposer des données ou compromettre un site, suivez [`SECURITY.fr.md`](SECURITY.fr.md) plutôt que de publier les détails d’exploitation dans une issue.

## Licence

Voir [`LICENSE`](LICENSE). La licence et les métadonnées de publication seront revérifiées avant la première soumission au catalogue WordPress.org.
