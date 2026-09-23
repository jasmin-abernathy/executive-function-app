# RésoSoin — étude médecins / patients

## Emplacement public

- Présentation : `https://app.lepotager.org/resosoin/`
- Questionnaire : `https://app.lepotager.org/resosoin/questionnaire/`
- Confidentialité : `https://app.lepotager.org/resosoin/questionnaire/confidentialite.php`
- Résultats agrégés : `https://app.lepotager.org/resosoin/questionnaire/resultats.php`

## Positionnement testé

RésoSoin est présenté comme un projet de solution française et indépendante pour cabinets de santé :

- CMS maison léger ;
- site et domaine du cabinet indépendants de l’agenda ;
- agenda et automatisations courantes ;
- absence d’IA imposée ;
- distinction explicite entre absence d’IA et absence d’automatisation ;
- hébergement européen visé, avec préférence pour un prestataire soumis au droit européen pour les données sensibles ;
- prix cible nettement inférieur aux grandes plateformes, sans figer un tarif avant l’étude.

Le site ne présente pas RésoSoin comme « plus sécurisé que Doctolib » tant que l’infrastructure n’est pas finalisée et auditée.

## Méthode reprise du survey TDAH

- session pseudonyme ;
- jeton de reprise stocké côté navigateur, empreinte seulement côté serveur ;
- progression question par question ;
- sauvegarde au fil du parcours ;
- reprise après interruption ;
- questions fermées et échelles, sans texte libre médical ;
- présentation du concept seulement après les questions d’usage ;
- suppression de la session par le participant ;
- page de confidentialité ;
- résultats publics agrégés uniquement après un seuil de 10 réponses envoyées par audience ;
- contact volontaire pour un pilote séparé techniquement des réponses.

## Données volontairement exclues

Le questionnaire est réservé aux personnes de 18 ans ou plus et ne demande pas :

- diagnostic ;
- pathologie ;
- traitement ;
- motif de consultation ;
- identité ;
- numéro de sécurité sociale ;
- texte libre médical.

## Base de données

Le questionnaire utilise la configuration MariaDB existante de l’application `executive-function-app`, mais dans des tables dédiées :

- `resosoin_survey_sessions` ;
- `resosoin_survey_answers`.

Installation côté serveur :

```bash
php web/app.lepotager.org/resosoin/questionnaire/install.php
```

Nettoyage recommandé par cron :

```bash
php web/app.lepotager.org/resosoin/questionnaire/cleanup.php
```

Politique prévue :

- sessions actives supprimées après 30 jours d’inactivité ;
- réponses envoyées supprimées après 12 mois.

## Déploiement

Le GitHub SHA et le live doivent rester distingués. Après fusion/déploiement :

1. vérifier le DocumentRoot de `app.lepotager.org` ;
2. vérifier que `/resosoin/` sert bien cette version ;
3. initialiser les tables via CLI si nécessaire ;
4. démarrer un questionnaire test ;
5. sauvegarder, recharger, reprendre ;
6. envoyer ;
7. supprimer une session test ;
8. vérifier le seuil des résultats agrégés ;
9. confirmer que `schema.sql`, `_common.php`, `install.php` et `cleanup.php` sont inaccessibles par HTTP.
