# Relais — concentration et hyperfocus, lot sans migration (10 novembre 2026)

## Base et règles
- Branche empilée sur la PR #58, tête de base `6fcd1db4de4ca8d9b556ba90476ef533fc1ef3ff` ; cette dernière attend une recette physique.
- Sondage V2 : 6/9 préfèrent quelques modes de session clairs, 7/9 trouvent naturel un toucher pour faire pause, 7/9 envisagent un rappel corporel doux et facultatif en session longue.
- Ne pas fusionner ni produire d'APK sans CI Android/lint/scan secrets et essais sur téléphone.

## Changements
- Dans « Utiliser une autre méthode », option **Session longue de 2 h (cette fois seulement)**, sans modification du réglage par défaut ou de l'override propre à la tâche.
- Le compteur de `FocusScreen` est tactile et accessible (rôle bouton, description et cible d'au moins 64 dp). Un toucher demande l'interruption persistée déjà prise en charge par `FocusRepository`; les contrôles de pause existants restent disponibles.
- `ResumeScreen` permet d'ouvrir une note rapide pendant la pause, sans annuler ni terminer la session.
- Protection homogène de `FocusRepository` et `FocusActionReceiver` via `FocusCompletionPolicy` : terminer une session n'équivaut pas à terminer un parent contenant des petits pas. Ce garde-fou évite la clôture accidentelle depuis une notification ancienne.
- Aucun changement SQL, nouvelle permission, serveur ou SDK.

## Limites
- Le toucher ouvre l'écran de reprise existant : ce n'est **pas encore** un tiroir flottant sur le chronomètre ; l'ergonomie sur appareil décidera si un tiroir plus compact est souhaitable.
- Les propositions de pause déjà facultatives continuent de fonctionner ; aucun nouveau rappel corporel automatique n'est ajouté à ce lot. Ne pas confondre avec un vrai mode hyperfocus intégral.
- « Session longue » est un compteur 120 min ; pas un système de sessions adaptatives.

## Recette mobile avant fusion
1. Toucher le temps deux fois rapidement, vérifier l'absence de double interruption / d'erreur.
2. Reprendre après pause puis tuer le processus et revenir, vérifier le temps et la note d'interruption.
3. Ouvrir une note rapide depuis la pause, l'enregistrer et revenir sans perte de session.
4. Choisir 2 h une seule fois, puis démarrer une nouvelle tâche : les préférences ne sont pas modifiées.
5. Terminer une session dont la tâche parent possède des petits pas, depuis le bouton puis depuis une notification : le parent reste actif jusqu'à confirmation explicite.
6. TalkBack, clavier, texte agrandi, FR/EN, rotation, verrouillage, mini fenêtre/PiP, Android 8+ ; afficher les états d'erreur sans masquer les actions.
