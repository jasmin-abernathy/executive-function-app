# 17 octobre 2026 — Couleurs des tâches mieux différenciées

## Retour utilisateur
Les six premières pastilles étaient quasiment blanches ou très faiblement teintées, surtout en mode sombre. Il était difficile de distinguer les teintes sans regarder de près.

## Modification
Le sélecteur commence désormais par **six couleurs plus gaies, sans être fluorescentes** : framboise, rouge terre cuite, corail, orange abricot, jaune soleil et jaune citron.

La palette reste légère, mais les fonds et les contours sont plus contrastés. Les nuances froides (vert sauge, bleu, menthe, turquoise, etc.) restent plus bas, et le blanc neutre est toujours disponible. Les pastilles conservent leurs libellés d'accessibilité et le coche de sélection.

## Compatibilité
- Conserver les identifiants historiques de `TaskColor` : aucune réécriture des données des tâches ni migration SQLite.
- La couleur `LEMON` est ajoutée en fin d'enum pour préserver les ordres historiques.
- La sauvegarde des couleurs continue par nom.
- Les tâches existantes utilisant les anciennes couleurs framboise/corail/terre cuite/abricot/sable s'afficheront avec les **nouvelles nuances**, sans perdre leur association.
- Pas d'image provisoire, pas de compagnon généré, aucun lien avec la PR 48 Google Play.

## Vérifications
Tests unitaires pour l'ordre des six couleurs, la présence de toutes les couleurs, la conservation du neutre et la différenciation minimale entre pastilles. Recette visuelle sur téléphone en modes clair et sombre, avec une grande taille de police et TalkBack.

Pas d'APK automatique.
