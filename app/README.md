# Android application source

This directory is intentionally **not** filled with a fake Android project yet.

When implementation starts, create the Android project here (Kotlin + Jetpack Compose is the current target) and keep the first architecture deliberately small.

## Intended first modules / responsibilities

The exact package names can wait, but P1 should keep clear boundaries between:

- **capture** — ultra-fast creation of an inbox item;
- **tasks** — local task/inbox model and simple organization;
- **focus** — timer/session state;
- **resume** — interruption and return state;
- **settings** — readability, motion and local preferences;
- **data** — local persistence and export/delete boundaries.

Avoid creating service abstractions, cloud modules or a complex navigation graph before the P1 loop needs them.

See [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md).
