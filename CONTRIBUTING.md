# Contribuer

Lire `AGENTS.md`, les principes dans `docs/PRODUCT_PRINCIPLES.md` et les décisions dans `docs/adr/` avant de modifier le parcours. Le choix manuel, la reprise non punitive, le fonctionnement local et le caractère facultatif des aides sont des exigences du cœur.

Travailler par lot cohérent. Vérifier la base de branche avant l'envoi et ne jamais écraser les changements d'une autre personne. Décrire le problème, le comportement résultant et les limites de validation. Dans le présent lot, un seul commit final est demandé et aucun APK n'est autorisé.

Avec JDK 17 et le SDK Android correspondant au projet :

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug
node scripts/check-local-data.mjs
```

Le contrôle local des données utilise Node et Python 3. Le contrôle de syntaxe facultatif `node scripts/check-kotlin-syntax.mjs` utilise les paquets Python `tree-sitter` et `tree-sitter-kotlin` ; il ne remplace pas la compilation. Les essais sur appareil doivent être signalés séparément des tests unitaires.

Conserver la parité FR/EN, les alternatives visibles aux gestes, les couleurs non porteuses de sens obligatoire et la réduction des mouvements. Aucun compagnon, logo ou substitut illustré ne doit être ajouté sans les éléments et décisions artistiques correspondants.

Le choix de licence et les droits des assets sont décrits dans `docs/LICENSING.md`. Ne pas importer de code ou de graphismes tiers sans en vérifier les droits et les attributions.
