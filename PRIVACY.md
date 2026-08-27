# Privacy

## Principle

The application is designed **local-first**. The core should work without a mandatory account, remote profile or advertising/analytics SDK.

## Repository rule

This repository must never contain:

- raw questionnaire exports;
- participant contact details;
- interview transcripts containing identifying data;
- real task lists or notes from testers;
- access tokens, passwords, signing keys or keystores;
- production database dumps.

Only anonymised, aggregated or already-public research findings belong under `research/`.

## App data target

For the initial app, likely local data categories are:

- tasks / inbox items;
- optional notes attached to those items;
- focus-session state;
- interruption/resume state;
- user preferences and accessibility settings;
- optional local notification schedules.

Networked integrations are outside the core and must be opt-in, granular and documented before implementation.

## Diagnostics

Diagnostics must be safe by design. A bug report should not silently include task text, note contents or other user-created content. Prefer technical state, version information and redacted identifiers.

See [`docs/DATA_MAP.md`](docs/DATA_MAP.md).
