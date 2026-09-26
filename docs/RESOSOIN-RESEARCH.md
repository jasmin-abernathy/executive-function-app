# RésoSoin — étude médecins / patients

## Emplacement public

- Présentation : `https://app.lepotager.org/resosoin/`
- Questionnaire : `https://app.lepotager.org/resosoin/questionnaire/`\n- Invitation neutre : `https://app.lepotager.org/resosoin/questionnaire/invitation.php`
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

- confirmation 18+ et consentement explicite avant création de session ;
- version du questionnaire et date de consentement enregistrées ;
- session pseudonyme ;
- plusieurs brouillons peuvent coexister sur un même navigateur ; chaque jeton de reprise reste côté navigateur et seule son empreinte est stockée côté serveur ;
- progression question par question ;
- sauvegarde après chaque étape validée, y compris avant un retour arrière ; avertissement navigateur si une sélection visible n’est pas encore enregistrée ;
- reprise après interruption ;
- questions fermées et échelles, sans texte libre médical ;
- présentation du concept seulement après les questions d’usage ;
- suppression de la session par le participant, y compris après envoi tant que le jeton est présent dans le navigateur ;
- page de confidentialité ;
- résultats publics exploratoires seulement à partir de 20 réponses envoyées par audience, avec dénominateur par question et regroupement des cellules rares ;
- contact volontaire pour un pilote séparé techniquement des réponses ;\n- canal de recrutement limité à quelques catégories non identifiantes (`home`, `resosoin_page`, `neutral_invite`, `direct`) ;\n- page d’invitation neutre disponible pour recruter sans exposer d’abord le positionnement du produit.

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
9. confirmer que `schema.sql`, `_common.php`, `install.php`, `cleanup.php`, `validate.php` et les fichiers de documentation sont inaccessibles par HTTP ;
10. avec le budget GitHub Actions indisponible, valider localement PHP, le catalogue JSON, la syntaxe JavaScript et les tests RésoSoin avant tout push ;\n11. tester deux brouillons simultanés sur un même navigateur, retour arrière avec modification, branche sans Doctolib, refus de l’app, panne réseau au démarrage et suppression après envoi.


## Parcours des professionnels de santé

La version `2026-09-26-v3` n’exige aucune habilitation Pro Santé Connect, Datapass, FINESS ou statut d’établissement de santé.

Parcours principal :

1. le répondant déclare exercer une profession de santé réglementée ;
2. il choisit sa profession dans une liste fermée ;
3. le questionnaire enregistre uniquement cette déclaration et les réponses d’usage ;
4. la session utilise `professional_verification_method = self_declared` ;
5. `professional_verified` reste à `0` : une déclaration n’est jamais transformée en identité « vérifiée ».

La page indique explicitement que ce mécanisme n’empêche pas une fausse déclaration et que les résultats professionnels doivent être lus comme déclaratifs.

### Consultation facultative de l’Annuaire Santé

Si une clé `ESANTE-API-KEY` est configurée, le répondant peut facultativement rechercher sa fiche publique RPPS avant de commencer.

- le numéro RPPS est envoyé ponctuellement à l’API Annuaire Santé ;
- le nom d’exercice est comparé localement ;
- aucune concordance n’est nécessaire pour répondre ;
- une concordance signifie seulement qu’une fiche publique active correspond aux informations saisies ;
- elle ne prouve pas que le répondant est titulaire de la fiche ;
- le nom et le RPPS ne sont pas enregistrés dans les tables du questionnaire ni intégrés au jeton remis au navigateur ;
- la session utilise alors `professional_verification_method = directory_record_matched`, tout en conservant `professional_verified = 0`.

Le panneau privé reste disponible à :

`https://app.lepotager.org/resosoin/admin/`

Il permet de tester et stocker la clé hors webroot. L’absence de clé ne bloque jamais l’étude.

## Versions et anciens brouillons

Les nouvelles réponses utilisent `2026-09-26-v3`. Les brouillons actifs d’une ancienne version ne sont pas réécrits silencieusement : l’API demande de les supprimer puis de recommencer. Les réponses déjà envoyées restent intactes en base.

La page de résultats agrège uniquement la version courante afin de ne pas mélanger des questions dont le sens ou les options ont changé. Pour les professionnels, elle distingue la déclaration simple d’une éventuelle concordance de fiche Annuaire Santé.

## Ordre des questionnaires v3

### Professionnels

- profession réglementée déclarée et contexte d’exercice ;
- taille de la structure et canaux de rendez-vous ;
- irritants spontanés avant les questions sur Doctolib ;
- coût facultatif et satisfaction ;
- automatisations utiles avant l’avis sur l’IA ;
- critère de choix prioritaire ;
- concept RésoSoin ;
- fonctions prioritaires, frein principal, ordre de grandeur de prix et pilote facultatif.

### Patients

- canaux de rendez-vous ;
- difficulté à trouver un professionnel disponible et étape la plus pénible ;
- fréquence Doctolib seulement si Doctolib est utilisé ;
- préférence de compte et moyens d’accès (téléphone, proche, accessibilité) ;
- automatisations puis critère prioritaire et avis sur l’IA ;
- utilité potentielle d’une recherche commune, explicitement présentée comme un projet ;
- concept, avantages, frein principal et application facultative ;
- tranche d’âge déplacée en fin de parcours et rendue facultative.
