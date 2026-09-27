# RésoSoin — stratégie d’interopérabilité (pré-développement)

## Décision de périmètre

RésoSoin ne doit pas devenir un logiciel de gestion de cabinet (LGC) bis.

Le produit doit rester une couche légère autour du parcours de rendez-vous, du site public du cabinet et d’automatisations choisies, en laissant le dossier patient, la prescription, la facturation métier et la télétransmission aux logiciels déjà utilisés par les professionnels.

Aucun connecteur patient réel n’est activé dans ce lot.

## Architecture cible

```text
Interfaces RésoSoin
        |
        v
Domaine métier RésoSoin
        |
        v
Couche d’adaptateurs / ports
   |       |       |
 export  import   connecteurs autorisés
        |
        v
LGC / services de santé
```

Le domaine ne dépend jamais directement d’un éditeur particulier. Chaque intégration propriétaire doit rester remplaçable.

## Paliers

### Palier 0 — maintenant

Sans donnée patient réelle :

- export PDF imprimable ;
- export CSV administratif lorsque pertinent ;
- prototype d’export structuré JSON ;
- prototypes UX d’import et d’export ;
- journal d’import/export fictif ;
- mapping documenté ;
- aucune donnée réelle envoyée à un tiers.

### Palier 1 — après validation terrain

Étudier les possibilités officielles, contractuelles et techniques pour les principaux logiciels rencontrés par les cabinets pilotes. Un connecteur n’est développé que si l’éditeur fournit un moyen d’intégration autorisé et suffisamment stable.

### Palier 2 — interopérabilité santé

Lorsque le périmètre le justifie, s’aligner sur le CI-SIS et les standards nationaux plutôt que multiplier les formats maison. La trajectoire ANS 2026 va vers FHIR pour les catégories prioritaires, tout en conservant des usages CDA/HL7v2 dans l’écosystème existant.

## Règles de sécurité

- données de démonstration synthétiques uniquement dans les prototypes ;
- aucun scraping d’interface métier ;
- aucune automatisation par identifiants utilisateur stockés ;
- aucun reverse engineering d’API privée ;
- aucun secret d’éditeur dans le dépôt ;
- validation stricte des imports ;
- prévisualisation et confirmation explicite avant import ;
- journalisation sans recopier inutilement le contenu clinique ;
- refus fail-closed d’un format/version inconnus ;
- séparation nette données publiques du site / données administratives / données de santé.

## Contrat interne proposé

Les adaptateurs futurs doivent converger vers un petit contrat interne versionné plutôt que propager les schémas propriétaires dans l’application.

Exemple conceptuel :

```json
{
  "schema": "resosoin.exchange.v1",
  "kind": "appointment-summary",
  "source": "synthetic-demo",
  "subject": {
    "external_reference": "DEMO-001"
  },
  "appointment": {
    "starts_at": "2030-01-01T10:00:00+01:00",
    "duration_minutes": 30,
    "status": "planned"
  }
}
```

Ce format n’est pas un standard médical et ne doit pas être présenté comme tel. Il sert uniquement de frontière interne/prototype avant choix d’un profil CI-SIS/FHIR pertinent.

## Ce qu’il faut mesurer auprès des cabinets pilotes

Pour chaque professionnel volontaire :

1. logiciel métier/LGC réellement utilisé ;
2. agenda utilisé ;
3. MSSanté utilisée ou non ;
4. imports/exports déjà disponibles ;
5. double saisie la plus coûteuse ;
6. documents réellement échangés ;
7. besoin d’un échange automatique ou simple export/import suffisant ;
8. interlocuteur éditeur/intégrateur disponible.

Le choix des premiers connecteurs doit découler de ces usages réels, pas d’un classement théorique des éditeurs.

## Sources techniques à surveiller

- ANS — CI-SIS et doctrine d’interopérabilité ;
- espace de publication CI-SIS ;
- espace de tests d’interopérabilité ANS ;
- documentation officielle des éditeurs de LGC concernés ;
- documentation officielle MSSanté / services socles lorsque le périmètre y arrive.

## Critère de passage à une intégration réelle

Ne brancher un connecteur à de vraies données que lorsque sont connus :

- API/protocole officiel ;
- droit d’usage ;
- modèle d’authentification ;
- données minimales nécessaires ;
- hébergement et responsabilités ;
- journalisation ;
- procédure de révocation ;
- tests d’interopérabilité ;
- comportement en panne ;
- procédure de sortie/réversibilité.


## État des intégrations éditeurs vérifié au 27 septembre 2026

### Doctolib

La documentation publique Doctolib confirme :
- l'existence de connecteurs entre Doctolib et des systèmes tiers ;
- des flux transmis via API lorsqu'un connecteur est effectivement mis en place ;
- un mécanisme de clé secrète pour l'authentification du système tiers ;
- un programme de partenaires logiciels/SaaS ;
- la possibilité, selon le contrat, d'un connecteur avec un logiciel de gestion de cabinet partenaire.

Conclusion RésoSoin : **intégration potentiellement réaliste, mais pas à coder comme une API publique libre-service**. La prochaine étape est une prise de contact partenariat/interopérabilité et l'obtention d'une documentation autorisée avant tout connecteur.

Références publiques :
- https://info.doctolib.fr/partenariats-doctolib/
- https://info.doctolib.fr/dpa/
- https://info.doctolib.fr/blog/definitions/

### Weda, Médistory, HelloDoc/AxiSanté, Cegedim/Maiia, Stellair

La recherche publique effectuée pour ce lot ne suffit pas à établir un contrat d'API publique stable et librement intégrable pour RésoSoin.

Cela ne signifie **pas** qu'aucune interopérabilité n'existe. Il faut distinguer :
- interfaces réservées aux partenaires ;
- exports/imports disponibles dans le logiciel ;
- connecteurs Ségur/CI-SIS ;
- API contractuelles non publiques ;
- absence réelle d'interface.

Aucun connecteur propriétaire ne doit donc être développé à partir d'hypothèses ou de reverse engineering.

## Décision d'architecture pour la prochaine étape

GPT-6 doit conserver trois frontières distinctes :

1. **ExchangeContract** : représentation interne minimale, versionnée, indépendante des éditeurs.
2. **ClinicalInteropAdapter** : future frontière CI-SIS/FHIR/CDA lorsque des données de santé doivent réellement être échangées.
3. **VendorAdapter** : adaptateur propriétaire uniquement lorsqu'une documentation et une autorisation d'intégration existent.

Le contrat prototype `resosoin.exchange.v1` ne doit jamais devenir par inertie un format clinique propriétaire. Dès qu'un cas d'usage clinique réel est retenu, choisir le volet CI-SIS correspondant et mapper le domaine RésoSoin vers ce standard.

## Points de décision à confier à GPT-6

Avant tout code clinique :
- identifier les ressources FHIR/volets CI-SIS correspondant exactement aux cas d'usage retenus ;
- déterminer si RésoSoin manipule réellement une donnée de santé ou seulement une donnée administrative de rendez-vous ;
- séparer identités, rendez-vous, documents et messages ;
- définir les frontières HDS/secret management/authentification ;
- prévoir idempotence, déduplication, reprise sur erreur et audit ;
- définir les règles de consentement/base légale au niveau produit avec validation juridique appropriée ;
- concevoir les tests contractuels des adaptateurs sans nécessiter de données patients réelles.

## Références nationales

- Doctrine d'interopérabilité ANS : https://esante.gouv.fr/doctrine/interoperabilite
- CI-SIS : https://esante.gouv.fr/produits-services/ci-sis
- Espace de publication CI-SIS : https://prod-convergence.esante.gouv.fr/offres-services/ci-sis/espace-publication

Au 27 septembre 2026, l'ANS indique une trajectoire vers FHIR pour les catégories prioritaires et, à terme, pour l'ensemble des volets CI-SIS. Les implémentations existantes CDA et autres profils ne doivent cependant pas être supposées disparues : le choix doit suivre le cas d'usage et le volet applicable.

## Matrice de cadrage avant intégration

Les standards ci-dessous sont des **candidats à vérifier pour chaque flux**, pas une promesse de compatibilité. La qualification juridique et HDS dépend du contenu réel et des rôles des opérateurs.

| Cas | Données minimales et sens | Classe pressentie | Interface candidate | Accès et prérequis | Panne / hors ligne |
|---|---|---|---|---|---|
| Disponibilités | Créneaux et identifiant d'agenda, éditeur → RésoSoin | Administratif, contexte de soin possible | API agenda officielle ou export autorisé | OAuth/clé par cabinet, accord éditeur, analyse HDS si hébergement de données de santé | Afficher l'horodatage et refuser de confirmer un créneau périmé |
| Créer, modifier, annuler un RDV | Référence, créneau, statut, RésoSoin ↔ agenda | Potentiellement donnée de santé identifiable | API officielle ; profil applicable à qualifier | Droits d'écriture, contrat, idempotence, audit, analyse HDS | File locale seulement si traitement et sécurité approuvés ; réconciliation avant nouvelle écriture |
| Identité ou référence patient | Identifiant externe minimal, LGC ↔ RésoSoin | Santé si reliée à la prise en charge | Profil identité CI-SIS/FHIR à sélectionner selon usage | Habilitation, rapprochement d'identités, contrat, HDS à qualifier | Aucun rapprochement automatique sur référence ambiguë |
| Document transmis | Référence de document et métadonnées, patient → professionnel | Santé probable | Volet documentaire CI-SIS applicable, MSSanté si pertinent | Authentification forte, destinataire, hébergement et traçabilité qualifiés | Ne pas promettre une remise tant qu'aucun accusé n'est obtenu |
| Messagerie | Destinataires, message et pièces jointes, bidirectionnel | Santé selon contenu | MSSanté ou solution agréée selon usage | Identités, consentement/base légale, droits et conservation à définir | État d'envoi explicite ; pas de répétition silencieuse |
| Import/export administratif | Coordonnées et statistiques minimisées, cabinet ↔ RésoSoin | Administratif ou santé selon lien au soin | CSV documenté ou API officielle | Contrôle des colonnes, autorisation et analyse HDS selon données | Prévisualisation, rejet des lignes invalides, export réversible |

## Premier port exécutable : simulation seulement

`prototypes/exchange-contract.js` fournit `validate` et `createMockAdapter` pour un résumé de rendez-vous synthétique. Le port `ExchangeContract` valide taille, champs, version et calendrier. Le mock émet un jeton de prévisualisation lié à la clé et au contenu ; une confirmation explicite avec ce jeton est requise avant l'écriture en mémoire. Il détecte une clé d'idempotence déjà utilisée ou réutilisée avec un autre contenu. Son registre est uniquement en mémoire, sans persistance ni connexion réseau. Le journal clinique est absent. Le fichier est un module Node de démonstration, pas un composant chargé par les pages publiques.

Un futur `ClinicalInteropAdapter` devra sélectionner un volet officiel adapté au cas réel. Un futur `VendorAdapter` exigera documentation et autorisation de l'éditeur. Aucun des deux n'est implémenté. Ce mock ne doit pas servir à traiter des données réelles ni être exposé comme une API publique.
