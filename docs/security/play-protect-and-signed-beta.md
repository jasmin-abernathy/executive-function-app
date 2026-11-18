# Préparer une bêta Android signée et comprendre Play Protect

## Constat vérifié au 17 octobre 2026

- **Application TDAH** (`executive-function-app`) : workflow Android existant = `assembleDebug`, `app-debug.apk`; aucune signature de publication persistante n'était prévue.
- **VeVak** : keystore de signature conservé comme secret, job `bundlePlayRelease`. Le circuit de bêta FOSS peut toutefois générer des APK debug : **la signature ne suffit pas à expliquer toutes les différences d'avertissement**.
- **Mon Manager Web** (`lepotager-site-manager`) : son propre circuit fournit aussi des APK debug ; manifeste plus simple.
- L'app TDAH utilise `SYSTEM_ALERT_WINDOW` pour sa fenêtre flottante **optionnelle**, `FOREGROUND_SERVICE_SPECIAL_USE` pour son timer flottant, `POST_NOTIFICATIONS` et `RECEIVE_BOOT_COMPLETED` pour les notifications/rappels. Ces fonctionnalités sont réellement utilisées. Ne pas supprimer ces permissions à l'aveugle.
- L'app ne demande ni lecture de SMS, ni service d'accessibilité, ni accès aux notifications des autres apps : ne pas attribuer sans preuve l'alerte au blocage Play Protect propre à ces permissions.

## Comprendre l'alerte

Google documente plusieurs messages distincts : « Analyse d'appli recommandée », « Appli dangereuse bloquée », autorisations sensibles et avertissement de compatibilité. Il faut **la capture complète de l'alerte et son texte exact** pour savoir si l'on a affaire à une vraie classification Play Protect ou à un écran Android de provenance inconnue.

- https://developers.google.com/android/play-protect/warning-dev-guidance?hl=fr
- https://support.google.com/googleplay/answer/2812853?hl=fr

**Important :** aucun fichier de configuration Android ne permet d'imposer le bouton « Analyser l'appli ». Une bêta signée ne garantit pas l'absence d'alerte. Si la classification Play Protect semble erronée après revue du comportement de l'app, utiliser la procédure d'appel officielle.

## Publication bêta signée, préparée mais non activée

La compilation via `.github/workflows/signed-beta.yml` est uniquement manuelle, sur **`main` revue**, et ne peut fonctionner que lorsque des secrets de signature persistante ont été installés. Elle produit un `app-release.apk` (non debuggable) et un SHA256 après tests et vérification cryptographique.

Créer une **clé TDAH distincte des autres projets** ; la conserver dans un gestionnaire de mots de passe et effectuer une sauvegarde chiffrée indépendante. Exemple sur un ordinateur sécurisé :

```bash
keytool -genkeypair -keystore tdah-beta-signing.jks -storetype PKCS12 -alias tdah-beta -keyalg RSA -keysize 3072 -validity 10000
# Produire une version Base64 SANS afficher ni publier le fichier original dans Git
base64 -w 0 tdah-beta-signing.jks > tdah-beta-signing.base64
```

Dans GitHub, sous `Settings > Environments > android-signing`, ajouter les secrets suivants (ne pas les envoyer dans une conversation et ne pas les committer) :

- `TDAH_BETA_KEYSTORE_BASE64` — contenu du fichier Base64.
- `TDAH_BETA_STORE_PASSWORD` — mot de passe de la clé de stockage.
- `TDAH_BETA_KEY_ALIAS` — `tdah-beta` ou alias choisi.
- `TDAH_BETA_KEY_PASSWORD` — mot de passe de la clé.

Si l'interface GitHub indique un environnement inexistant, créer `android-signing` et le réserver à la branche `main`. Les outils d'assistance ne peuvent ni lire ni reconstruire ces secrets.

Une fois la PR de code examinée, puis **fusionnée volontairement dans `main`**, lancer Actions → « Signed Android beta (manual only) » → `confirm_signed_beta=true`. Le workflow vérifie l'APK avec `apksigner` et publie l'artefact 7 jours.

## Précaution indispensable pour les données

L'ancienne `app-debug.apk` et la future `app-release.apk` ont la **même identité de package mais une signature différente**. Android refusera généralement une mise à jour directe ; **ne pas désinstaller l'ancienne app sans un export/sauvegarde effectivement testé**. Le code de signature ne migre pas les données automatiquement.

Pour les versions futures, conserver la même clé de release. Le futur basculement vers Google Play et Play App Signing demandera de décider de la clé dès le départ pour éviter une migration de signature irréversible.

## Tests de sécurité avant diffusion

- Installations réelles sur plusieurs téléphones, captures des messages, versions Android et provenance du téléchargement ;
- Vérification APK SHA256, signature et chaîne de provenance ;
- Refus des permissions optionnelles, vérification que le timer normal fonctionne ;
- Analyse Play Protect volontaire et, en cas de détection erronée persistante, appel officiel avec l'APK exact ;
- Aucune instruction aux testeurs de désactiver Play Protect.


## Diagnostic confirmé par la capture — 17 octobre 2026

Le téléphone affiche exactement « **Appli bloquée pour protéger votre appareil** » puis « **Play Protect n'a jamais vu d'appli de ce développeur auparavant. Elle n'est peut-être pas sûre.** ». Il s'agit d'un avertissement de réputation/origine inconnue de cet APK, **pas d'une preuve positive de malware**. Cela ne permet pas de garantir que Google approuvera n'importe quel canal de distribution. Ne jamais demander aux testeurs de désactiver Play Protect.

### Plus tard, après le compagnon officiel : partage interne d'application

L'option **Partage interne d'application** de Play Console permet d'importer *l'APK debug existant*, sans le reconstruire, et Google le signe à nouveau avec sa clé de partage interne. Les liens expirent après 60 jours et chaque lien peut être téléchargé par 100 utilisateurs (documentation officielle : https://support.google.com/googleplay/android-developer/answer/9844679?hl=fr).

1. Depuis le compte développeur autorisé, ouvrir https://play.google.com/console/ puis l'application « Fonctions exécutives » si elle existe déjà.
2. Dans « Tester et publier > Tests internes > Partage interne d'application », vérifier les testeurs autorisés ; utiliser le lien « Page d'importation » pour importer l'APK GitHub Actions du commit `5e5d497` (ancienne PR 47, désormais fusionnée).
3. Sur le téléphone du testeur, activer **Partage interne d'application** dans les paramètres Google Play (7 appuis sur la version du Play Store, puis activer l'option). Installer via le lien de partage créé par Google.
4. **Ne pas désinstaller** une version avec données avant d'avoir testé son export. L'APK installé via ce lien est *resigné par Google*, et sa signature diffère de la version debug existante.

Cette option donne un lien pratique et utilise le Play Store comme vecteur de distribution, mais reste distincte d'une vraie version de test sur un canal Play Store. Les testeurs peuvent devoir activer le partage interne, ce qui n'est pas idéal pour un recrutement élargi.

### Après intégration du compagnon officiel : canal « Tests internes » Google Play

Le workflow `.github/workflows/signed-beta.yml` a été complété : lancement manuel uniquement sur `main` après configuration des secrets, tests/lint, `assembleRelease` **et `bundleRelease`**, vérification des signatures, puis publication de `app-release.aab`, `app-release.apk`, empreintes SHA256 et mapping de minification.

Le fichier à envoyer dans **Play Console > Tester et publier > Tests > Tests internes > Créer une version** est **`app-release.aab`**, pas `app-debug.apk`. Configurer **Play App Signing** lors du premier import ; laisser Google créer et gérer la clé de signature de distribution et conserver la clé GitHub exclusivement comme **clé d'importation (upload key)**. Google re-signe les APK livrés aux testeurs. Documentation officielle : https://developer.android.com/studio/publish/app-signing et https://support.google.com/googleplay/android-developer/answer/9845334?hl=fr.

- Avant l'import, vérifier que `org.lepotager.executivefunction` est bien l'identifiant du projet Play Console, et que le `versionCode` (actuellement 1) est strictement supérieur à toute version déjà importée.
- Ne jamais perdre la clé d'importation locale ni enregistrer ses mots de passe dans Git. La clé Google Play de **signature de distribution** n'est pas identique à la clé d'importation si Google l'a générée.
- Préparer la déclaration des fonctionnalités **`FOREGROUND_SERVICE_SPECIAL_USE`** : le service persistant existe pour afficher le timer flottant lorsque l'utilisateur l'active ; le cas doit être expliqué dans Play Console (https://developer.android.com/develop/background-work/services/fgs/service-types).
- Conserver la fonction de fenêtre flottante optionnelle, ne jamais attribuer sans preuve la classification Play Protect à une permission isolée.
- Les tests internes accueillent jusqu'à 100 testeurs avec leurs comptes Google et ne rendent pas l'application publique ; la première disponibilité peut prendre plusieurs heures.
- Les nouveaux comptes personnels ayant besoin d'un accès en production ont des exigences de **tests fermés**, distinctes des **tests internes** ; ne pas confondre les deux.
- Ne pas annoncer que le partage interne ou le canal de test permettra une mise à jour automatique d'une installation debug préexistante : signatures différentes = sauvegarde et transition contrôlée.

**Le workflow est préparé, mais sans accès à la Play Console ni au keystore GitHub, aucune version Google Play n'a été publiée.**

## Décision produit : aucun échange avec Google avant le compagnon

Le 17 octobre 2026, décision explicite : **attendre que l'illustration officielle et le comportement du compagnon soient intégrés et validés avant d'envoyer quoi que ce soit à Google**. La branche préparatoire n'a pas vocation à être fusionnée ni lancée à cette étape. Il n'est pas autorisé de produire des illustrations provisoires du compagnon.

- Le workflow de signature et AAB ne contient aucune étape d'envoi à Google ni aucun jeton Play Console.
- Il est limité à `main` et nécessite les deux drapeaux manuels `confirm_signed_beta=true` et `confirm_companion_integrated=true` ; valeurs par défaut : `false`.
- Le compagnon doit être livré à partir des créations graphiques officielles, jamais d'un animal ni d'une illustration temporaire générée par l'assistant.
- L'accueil peut accueillir une illustration **statique** du compagnon ; les widgets facultatifs petit/moyen pourront utiliser ses différentes poses plus tard. Préserver les parcours utilisateur rapides et non culpabilisants.
- La version actuelle est `versionCode = 1`, `versionName = "0.1.0-dev"` ; choisir numéro, nom et identité définitive *après* validation du compagnon et de la première publication Play (aucun numéro Play Console connu).
- Avant transmission de l'AAB, revérifier la sauvegarde/restauration des données locales : les APK debug ne peuvent pas être remplacés directement si la signature de distribution est différente.
- Vérifier à l'avenir que les éventuelles bibliothèques natives embarquées dans le bundle sont compatibles avec les pages mémoire de 16 Ko (https://developer.android.com/guide/practices/page-sizes?hl=fr) ; aucune conclusion avant inspection d'un AAB réellement compilé.
- API Android : `targetSdk=37` est conforme à l'exigence API 36+ pour les nouvelles publications depuis août 2026 (https://support.google.com/googleplay/android-developer/answer/11926878?hl=fr).
- Politique données : **tests internes uniquement** exemptés du formulaire Play « Sécurité des données » ; dès qu'un canal fermé/ouvert/production est utilisé, la déclaration et la politique de confidentialité deviennent requises, même sans collecte (https://support.google.com/googleplay/android-developer/answer/10787469?hl=fr).
- Le manifeste actuel ne demande pas `INTERNET` et n'utilise pas de compte distant ; toutefois l'affirmation finale « aucune donnée collectée » demande une revue de toutes les dépendances/SDK et des futures fonctions, compagnon compris.
- Le service de premier plan `specialUse` correspond au timer flottant facultatif ; préparer une justification compréhensible et vérifier si une démonstration vidéo est demandée lors de la soumission (https://support.google.com/googleplay/android-developer/answer/13392821?hl=fr).

**État** : préparation GitHub uniquement, sans publication, sans envoi d'APK/AAB à Google, sans création d'application dans la Play Console et sans création de clé de production.
