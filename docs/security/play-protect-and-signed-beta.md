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
