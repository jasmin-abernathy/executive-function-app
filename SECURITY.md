# Sécurité

Ne publiez pas de secrets, de sauvegardes personnelles ou de données de questionnaire dans les issues, commits ou journaux. Un rapport reproductible doit utiliser des tâches et états fictifs.

Pour signaler une vulnérabilité, utilisez « Report a vulnerability » dans l'onglet Security du dépôt si le signalement privé est activé. Sinon, demandez au mainteneur un canal privé avant de communiquer des détails exploitables. N'ouvrez pas d'issue publique avec des données personnelles.

Le prototype Android n'a pas de permission réseau. Les composants auxiliaires sont privés, les PendingIntent sont immuables et l'import local vérifie son format dans une transaction. Ces protections ne constituent pas un audit de sécurité complet. La version actuelle est un prototype ; aucune garantie de support de versions anciennes n'est publiée.

Avant livraison : vérifier les fichiers envoyés, les permissions ajoutées, les dépendances, le manifeste, les ressources traduites et les tests. Ne pas commiter de clés de signature, fichiers de configuration locale, caches de compilation ou APK. Les builds installables sont déclenchés uniquement sur demande explicite.
