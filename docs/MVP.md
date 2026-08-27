# P1 functional MVP

## Goal

Prove one narrow loop on a real Android device:

**Capture → choose → start → timer → interrupt → resume → return without penalty.**

## Must have

### Capture

- Create an inbox item with minimal interaction.
- Do not require project, category, due date, priority or energy level at capture time.

### Choose

- Show a small, understandable set of possible next actions.
- Manual choice must always remain possible.

### Start / focus

- Full-screen timer or focus state.
- Lightweight, local alerting.
- No account or network dependency.

### Interrupt / resume

- The user can explicitly interrupt a session.
- Current context is saved locally.
- Returning later offers a simple resume path.
- No lost progress, broken streak or guilt message.

### Local persistence

- Core items and session state survive app/process restarts.
- Basic delete/export/backup design is defined from the start even if UI is minimal.

### Accessibility minimum

- Dynamic/scalable text.
- Screen-reader semantics for core controls.
- Reduced-motion behaviour.
- No information conveyed by colour alone.
- Large touch targets for primary actions.

## Explicitly not required for P1

- cloud account;
- social features;
- AI assistant;
- companion/house;
- advanced statistics;
- complex routines;
- cross-device sync;
- calendar integration;
- premium billing.

These may be explored only after the core loop is usable.
