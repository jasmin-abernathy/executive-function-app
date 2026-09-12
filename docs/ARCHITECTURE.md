# Architecture direction

## Current target

- Android
- Kotlin
- Jetpack Compose
- local-first storage
- no mandatory account for the core app

The first compilable project pins a mutually compatible toolchain and lets Dependabot propose controlled updates. The current baseline is documented in the Gradle files and verified in CI.

## Architectural priorities

1. **Fast local startup** — the capture path must not wait for a network request.
2. **Durable state** — tasks and interruption/resume state survive process death.
3. **Small dependency surface** — add libraries only for a concrete need.
4. **No hidden analytics layer** — product research is separate from behavioural tracking.
5. **Testable state transitions** — especially start → interrupt → resume → finish/return.
6. **Accessibility semantics close to UI code** — not added as a release afterthought.

## P1 Android building blocks

- Compose for UI;
- `SQLiteOpenHelper` for structured task/session data and explicit transactions;
- `StateFlow` for the observable in-process snapshot;
- pure functions for clock, focus-state transitions and optional task draws;
- Android local notifications / alarms where appropriate;
- WorkManager only when actual deferrable background work exists.

SQLite is used directly in P1 to avoid annotation processing, generated code and an additional persistence abstraction before the schema justifies it. A unique partial index enforces one active focus session. State changes are persisted atomically; the running timer is derived from timestamps instead of writing once per second.

The application requests no network permission and disables automatic Android cloud backup. Local export and restore are explicit user actions.

The choice-paralysis aid exposes a pure proposal selector and a pure full-order permutation with injectable Random. Optional energy/time/context filters narrow the READY pool. The explicit home action persists the full order and proposes its first task; the widget uses proposal-only selection. Both avoid the previous proposal when an alternative exists and never auto-start. Tasks outside the pool retain their relative order. Stable IDs keep the result across snapshot refreshes. Manual handle dragging keeps temporary UI order and saves once on release; buttons and TalkBack actions remain available. The 500 ms D10 overlay is inside Compose and uses no system overlay permission. See [ADR 0007](adr/0007-d10-and-manual-order.md).

## Avoid in P1

- backend service;
- authentication system;
- remote sync engine;
- analytics SDK;
- dependency injection framework solely for ceremony;
- multi-module architecture without a demonstrated need.
