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
10. vérifier que la CI valide PHP, le catalogue JSON, la syntaxe JavaScript et les tests du cœur du survey ;\n11. tester deux brouillons simultanés sur un même navigateur, retour arrière avec modification, branche sans Doctolib, refus de l’app, panne réseau au démarrage et suppression après envoi.


## Vérification des professionnels de santé

Le questionnaire professionnel utilise l’API officielle **Annuaire Santé FHIR v2** de l’ANS.

Parcours :

1. le professionnel renseigne son **nom d’exercice** et son **numéro RPPS** ;
2. il certifie être la personne correspondante ;
3. le serveur interroge l’Annuaire Santé par RPPS ;
4. le nom est comparé localement avec l’identité renvoyée ;
5. la profession doit appartenir à la nomenclature officielle **TRE_G15-ProfessionSante** et la fiche doit être active ;
6. un jeton de vérification signé, valable 10 minutes, autorise la création de la session professionnelle.

Le filtre accepte les professions de santé de TRE_G15 : médecins, sages-femmes et professions paramédicales/réglementées présentes dans cette nomenclature. Les simples usages de titre relevant d’autres nomenclatures ne passent pas ce filtre.

### Minimisation

Le nom et le RPPS **ne sont pas enregistrés dans les tables du questionnaire**, ni en clair ni sous forme de hash.

La session conserve seulement :

- `professional_verified = 1` ;
- `professional_verification_method = annuaire_sante_tre_g15`.

Le numéro RPPS est envoyé ponctuellement à l’API ANS ; le nom est comparé localement.

## Clé API Annuaire Santé

Après déploiement, la clé peut être ajoutée sans SSH via :

`https://app.lepotager.org/resosoin/admin/`

Le formulaire privé :

- réutilise le compte administrateur existant ;
- teste la clé auprès de l’ANS avant sauvegarde ;
- n’affiche jamais la clé enregistrée ;
- l’écrit hors webroot dans `~/private/executive-function-app/resosoin-annuaire-sante.json` ;
- applique des permissions restrictives ;
- permet de retester ou supprimer la clé.

La clé ne doit jamais être ajoutée à Git.
