# ADR 0013 — Accompagnement facultatif en cas de surcharge

Statut : implémenté dans la branche `feat/overload-support-cards-20261117` (validation téléphone encore requise).

## Problème

L'application propose déjà la reprise d'un focus interrompu et la réduction d'une tâche à un petit pas. Une personne en surcharge ne doit pourtant pas avoir à trouver la bonne rubrique ou à remplir un nouveau questionnaire.

L'analyse du projet libre [TEA Autonomia](https://github.com/alunoadsnoite/tea-mvp) a notamment mis en évidence ses cartes de régulation, son accès direct à une aide et la notion d'une action principale à la fois. Les parcours TDAH et autisme ne sont pas assimilés ; l'objectif reste une assistance d'organisation, non une prise en charge thérapeutique.

## Décision

- Ajouter un accès « Je sature » depuis l'accueil, le focus actif et l'écran de reprise. Un widget Android minimal est proposé séparément du widget d'outils existant.
- Une pause volontaire utilise **exactement** l'interruption habituelle du focus : la durée active et le contexte sont préservés, puis l'écran de reprise permet de reprendre, réduire ou reporter.
- Deux fiches simples sont proposées hors connexion, sans compteur, alerte ni notification.
- L'utilisateur peut créer, modifier et supprimer jusqu'à 12 fiches personnelles de 1 à 3 étapes, sans compte, permission ni IA.
- Fiches personnelles en SQLite avec migration de schéma v6 → v7 ; export local v7, import rétrocompatible v4-v6.
- L'ouverture du mode et la lecture des fiches ne créent pas de check-in et ne modifient pas la priorité des tâches.
- Les fiches personnelles et les paramètres liés à la santé sont des données locales sensibles : aucun diagnostic ni score, aucune télémétrie et rien de sensible sur le widget.
- FR/EN, cibles tactiles >= 48 dp, texte agrandissable et lecture par TalkBack.

## Périmètre exclu

- Pas de SOS médical ou d'alerte automatique de contacts.
- Pas de module séparé de routines, déjà couvert par les étapes de tâche.
- Pas de conseil imposé, de suivi obligatoire, de respiration forcée ou de « traitement » du TDAH.
- Pas de copie de code ou de visuels de TEA Autonomia.

## Validation

Vérifier les tests Kotlin/Robolectric et le lint. Sur un appareil : ouverture depuis chacun des trois écrans et du widget, restauration du focus après une pause, rotation, grandes polices, TalkBack, suppression explicite d'une fiche et conservation de ses données après export/import. Une sauvegarde v6 doit restaurer les autres données sans fiche.

Tant que ces contrôles de terrain ne sont pas terminés, le changement ne doit pas être présenté comme validé sur appareil.
