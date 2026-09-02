# Android application

This directory contains the runnable P1 Android prototype.

## Implemented loop

`capture → choose → start → focus → interrupt → resume → complete/postpone`

- Kotlin + Jetpack Compose UI;
- direct, transactional SQLite persistence;
- no account, backend, network permission or analytics SDK;
- process-restart-safe elapsed-time focus sessions;
- quick capture while a focus session remains active;
- French and English resources;
- accessibility semantics and visible alternatives to every action.

## Build

Requirements: JDK 17 and Android SDK 37.

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Boundaries

- `model/` contains durable task/session state.
- `domain/` owns pure clock and transition rules.
- `data/` owns SQLite and transactional orchestration.
- `ui/` renders the three P1 surfaces: Today, Focus and Resume.
- `AppViewModel` is the small boundary between UI and repository.

The project deliberately has no dependency-injection framework, navigation framework, remote service abstraction or background service in P1. See [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md).
