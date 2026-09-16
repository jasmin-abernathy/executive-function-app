# Accessibility baseline

Accessibility is part of P1, not a final polish pass.

## Minimum checks

- Core flow usable with TalkBack.
- Text can scale without clipping essential controls.
- Primary actions have meaningful accessibility labels.
- Focus order follows the visual/task order.
- Touch targets are comfortably large.
- Colour is never the sole error/status cue.
- Reduced-motion preference is respected.
- No mandatory time-limited interaction for core tasks.
- Error text explains what to do next.
- Timer state is understandable without relying on animation.

## Cognitive accessibility

- Prefer one clear primary action.
- Avoid forcing categories/metadata during capture.
- Keep optional configuration behind progressive disclosure.
- Preserve context after interruption.
- Do not use guilt, urgency or scarcity copy to force engagement.
- The local-algorithm introduction is short, skippable and re-openable. Merely reading it never changes adaptation settings.
- Stopwatch/countdown selection is explicit before a new session. Reaching zero never completes a task automatically or creates a failure state.

## Pictogrammes fonctionnels

- Les pictogrammes décoratifs placés à côté d’un libellé ne reçoivent pas de description redondante.
- Les outils secondaires du focus peuvent être représentés par une icône seule uniquement lorsqu’ils disposent d’une description accessible localisée et d’une cible d’au moins 48 dp. Pause et Terminer conservent un libellé visible.
- Une action importante ou destructive ne dépend jamais de la compréhension du pictogramme seul.
- Le dé affiche un résultat graphique, mais la tâche proposée reste annoncée en texte. Son bloc titre/premier pas permet de démarrer avec une action TalkBack nommée ; « Commencer » reste visible et « Relancer » reste une action séparée.
- Chaque tâche conserve une poignée de 48 dp minimum et des actions TalkBack Monter/Descendre. Le mode Organiser conserve les boutons ↑/↓ ; le glissement n’est jamais obligatoire.
- Le D10 tourne pendant 500 ms dans une surimpression Compose interne. Le mode calme ou la désactivation des animations système affiche le résultat immédiatement, sans vibration.

## Temps

- Un chronomètre est identifié par l’absence de cible persistée ; un minuteur possède une cible positive. Les préférences globales ne doivent pas changer rétroactivement une session active.
- L’affichage principal, la notification, la fenêtre flottante et le PiP dérivent tous leur état de la session persistée.
- Après zéro, le temps supplémentaire reste lisible et mesuré ; aucune fin automatique n’est déclenchée.
