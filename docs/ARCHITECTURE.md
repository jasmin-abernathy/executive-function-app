# Architecture direction

## Current target

- Android
- Kotlin
- Jetpack Compose
- local-first storage
- no mandatory account for the core app

Dependency versions are intentionally **not pinned in this bootstrap kit**. They should be selected when the first compilable Android project is created, so the repository does not start with stale build configuration.

## Architectural priorities

1. **Fast local startup** — the capture path must not wait for a network request.
2. **Durable state** — tasks and interruption/resume state survive process death.
3. **Small dependency surface** — add libraries only for a concrete need.
4. **No hidden analytics layer** — product research is separate from behavioural tracking.
5. **Testable state transitions** — especially start → interrupt → resume → finish/return.
6. **Accessibility semantics close to UI code** — not added as a release afterthought.

## Likely Android building blocks

These are directions, not locked dependencies:

- Compose for UI;
- a local database for structured task/session data;
- DataStore-style preferences for small settings;
- Android local notifications / alarms where appropriate;
- WorkManager only when actual deferrable background work exists.

## Avoid in P1

- backend service;
- authentication system;
- remote sync engine;
- analytics SDK;
- dependency injection framework solely for ceremony;
- multi-module architecture without a demonstrated need.
