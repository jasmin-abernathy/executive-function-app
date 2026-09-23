# RésoSoin — vérification des répondants médecins

État de départ : `jasmin-abernathy/executive-function-app`, PR #21, branche `work/resosoin-research-site`, SHA `3dfcdb4812f31f7cef59de0b26c882f2692570d8` observé le 23/09/2026. Ce document prépare l'intégration ; aucun accès Pro Santé Connect (PSC), aucun identifiant de production et aucun résultat « médecin vérifié » ne sont actuellement en place.

## Décision de produit

Réserver le parcours actuellement nommé `doctor` aux **médecins authentifiés par PSC**. Le parcours patient reste accessible sans PSC. Les autres professionnels de santé ne doivent pas être enregistrés dans l'audience « médecins vérifiés » ; leur éventuel parcours séparé est une décision ultérieure. Un simple RPPS saisi dans un formulaire et trouvé dans l'Annuaire Santé ne prouve pas que son titulaire répond.

Prévoir un état de disponibilité explicite : tant que PSC n'est pas raccordé et validé, le parcours médecin indique « vérification en préparation » et ne collecte aucune réponse sous une étiquette « vérifiée ». Les tests peuvent utiliser des identités de bac à sable isolées, jamais prises pour des médecins réels. Ne pas faire de bascule silencieuse vers l'auto-déclaration.

## Parcours cible

1. La personne choisit « Je suis médecin » sur la page de l'étude. Explication brève de PSC, des données nécessaires à la vérification, de la séparation avec les réponses, et du fait que PSC ne consent pas à l'étude à sa place.
2. Redirection vers PSC par OpenID Connect, flux authorization code, avec `state`, `nonce` et PKCE ; contexte temporaire lié à une session serveur, cookie `Secure`, `HttpOnly`, `SameSite=Lax`. Aucun jeton PSC dans l'URL de reprise, `localStorage`, journal applicatif ou analytics.
3. Retour de PSC : valider serveur à serveur code et jetons selon la documentation ANS (issuer, audience, signature via clés de l'émetteur, expiration, nonce/state et PKCE). Examiner les attributs effectivement accordés par l'habilitation ; contrôler **la profession médecin**, pas seulement « professionnel de santé » ou un RPPS existant. Prévoir refus, annulation, identité sans attribut requis, panne PSC et révocation sans créer de session médecin.
4. Montrer le consentement propre à l'étude et la condition 18+ après authentification. Créer ensuite la session pseudonyme existante. Lier la preuve vérifiée à la session côté serveur, jamais par une valeur `doctor: true` fournie par le navigateur.
5. Sur `start`, `resume`, `save`, `submit` et `delete` de l'audience `doctor`, appliquer l'autorisation côté serveur. Vérifier l'état après perte de session, changement d'appareil, nouvelle connexion et suppression. Les réponses patient n'ont pas cette contrainte.
6. À l'envoi, empêcher deux réponses soumises simultanées pour une même identité vérifiée pendant la campagne, dans une transaction et via une contrainte unique côté base. Le système de suppression reste utilisable ; si la suppression retire aussi le marqueur anti-doublon, une nouvelle réponse redevient possible. Ne pas promettre « une personne ne pourra jamais répondre deux fois » tout en autorisant l'effacement complet.

## Données et séparation

- Consulter le RPPS/identifiant professionnel et la profession uniquement pour l'authentification et la limitation des doublons. Utiliser une empreinte HMAC avec clé dédiée à cette campagne, stockée côté serveur hors dépôt ; une simple empreinte SHA-256 d'un RPPS public est ré-identifiable par dictionnaire. Ne conserver ni nom, ni RPPS brut, ni jeton PSC, ni courriel dans les tables de réponses.
- Table de vérification distincte, accès serveur restreint : identifiant opaque, HMAC d'identité, état vérifié, horodatage, référence de session nécessaire au traitement des droits et à l'anti-doublon. Les réponses restent dans `resosoin_survey_answers`. Tant qu'un lien technique subsiste entre tables, les réponses sont **pseudonymisées et potentiellement ré-identifiables**, jamais « anonymes ».
- Définir la rétention du lien et du marqueur, la procédure de suppression et la réponse aux demandes de droit avant collecte. Éviter le RPPS/IP/jeton dans les journaux. Le résultat public ne doit pas exposer une spécialité ou de petits sous-groupes identifiables ; revoir le seuil de dix réponses comme mesure de publication, pas comme garantie d'anonymat.
- Actualiser la notice de confidentialité : responsable, finalité de vérification, attributs PSC reçus, base juridique retenue avec examen RGPD, durée de conservation, destinataires, droits, contact et différence entre authentification PSC et consentement à l'étude. Une connexion PSC implique une collecte d'identité professionnelle ; retirer l'affirmation absolue « pas d'identité » pour le parcours médecin.

## Mes tâches GPT-6 (architecture et sécurité)

1. Vérifier les exigences actuelles de raccordement PSC et les attributs de profession réellement délivrés, puis formaliser un mapping de codes médecin vérifiable avec l'ANS. Créer le dossier de raccordement Datapass/bac à sable **après** identification des informations d'organisme et des droits nécessaires, sans inventer de client ID ni manipuler les identifiants de Jasmin.
2. Concevoir et implémenter sur branche isolée le flux OIDC et ses contrôles serveur, avec fermeture sûre en cas d'échec. Ajouter une frontière claire entre identité vérifiée, consentement et réponses ; revoir les requêtes `api.php` et schéma SQL avec migration réversible et contrainte de concurrence.
3. Tester les cas adverses : RPPS d'un tiers, autre profession PSC, falsification de callback/state/nonce, rejeu, session expirée, doublon parallèle, suppression et réinscription, réponse patient, erreurs réseau, absence d'attribut, CSRF et fuite dans journaux/URL. Faire une revue des flux de données avant toute activation.
4. Contrôler la compatibilité PHP/o2switch, la gestion des secrets hors Git, les droits des tables, les paramètres de cookie, le déploiement et la vérification en production. Ne pas brancher les identifiants de production ni déclarer le parcours vérifié avant raccordement officiel et test réel.

## Tâches GPT-5.6 (parcours, contenu et validation ordinaire)

1. Sur une branche issue du bon HEAD après vérification du worktree, remplacer partout « professionnel·le de santé » par « médecin » là où l'audience `doctor` reste visée ; garder un message clair pour les autres professions. Mettre une étape d'explication PSC et les états chargement, annulation, refus, erreur et reprise, sans faux bouton de connexion fonctionnel avant intégration.
2. Adapter `questionnaire/index.php`, `survey.js`, `questions.json`, `confidentialite.php`, la page `/resosoin/`, l'accueil FR/EN et la documentation au nouveau parcours. Ne pas faire passer l'auto-déclaration ou une recherche RPPS pour une vérification.
3. Corriger les problèmes de questionnaire déjà documentés dans `AUDIT-REPARATION-RESOSOIN-2026-09-23.md` : retour arrière, reprise de plusieurs sessions, branchements, neutralité, durée annoncée, raisons de refus, accessibilité. Garder le formulaire patient indépendant du flux PSC.
4. Préparer tests UI, clavier/mobile, libellés, traductions et messages de confidentialité. Valider PHP lint, JSON, JS, chemins et tests applicatifs en un lot. Aucun APK ; un commit cohérent après lecture de `repo-factory/AGENTS.md`.
5. Ne fusionner ni déployer de parcours prétendument vérifié tant que les critères GPT-6 et le raccordement PSC ne sont pas satisfaits. Relever les nouveaux HEAD avant toute reprise pour éviter d'écraser un travail parallèle.

## Dépendances et acceptation

- PSC : service gratuit selon l'ANS, mais raccordement avec demande Datapass, bac à sable et production ; délais et éligibilité à confirmer pour ce service. Les modalités exactes et les attributs dépendent de l'habilitation obtenue.
- Une identité médecin PSC valide peut ouvrir le questionnaire après consentement ; une autre profession, un RPPS copié ou un callback forgé ne le peuvent pas.
- Les réponses « médecins vérifiés » ne contiennent pas de RPPS brut ; une suppression fonctionnelle ne laisse pas un lien secret non documenté. Les résultats patient restent disponibles indépendamment de PSC.
- Les notices et résultats distinguent vérification, pseudonymisation et consentement. Un test complet en bac à sable précède toute activation réelle.

Sources officielles consultées :

- ANS, Pro Santé Connect : https://esante.gouv.fr/produits-services/pro-sante-connect
- ANS, parcours de raccordement : https://esante.gouv.fr/ens/offre/pro-sante-connect
- ANS, Annuaire Santé et API FHIR : https://esante.gouv.fr/produits-services/annuaire-sante
- CNIL, données personnelles : https://www.cnil.fr/fr/definition/donnee-personnelle
- CNIL, consentement : https://www.cnil.fr/fr/les-bases-legales/consentement
