# Relay — post-V2 friction reduction — 2026-10-07

Branch: `work/post-v2-defaults-onboarding-20261006`
Pull request: #34 (open, draft, not merged)
Latest verified head: `73d34beb53447850fc73804808c83712aef05474`

## Goal

Use V2 feedback to configure recurring choices once and preserve one-off overrides. Keep the die available for randomly choosing a task throughout the home flow.

## Implemented

- Home modes `ONE_NEXT`, `NOW_NEXT`, and `LIST`; recommended default `NOW_NEXT`.
- Focus presets `SHORT=10`, `NORMAL=25`, `LONG=50`, and `OPEN`.
- Primary start uses stored defaults when quick start is enabled; one-off session editing remains available.
- Learned duration stays local and only overrides a fixed preset when enabled.
- Compact home shows one current task and up to two alternatives; full list remains available.
- Difficult-day mode hides alternatives. Activating it now also closes an already expanded full list.
- Seven-page first-run setup, with French and English strings.
- Added regression coverage for focus-preset migration and the expanded-list/difficult-day interaction.
- Random task selection via the die remains intact.

## Review fixes on this branch

- Materialized the legacy timer-mode migration into `focus_session_preset`, so a later one-off dialog timer choice cannot become the global preset.
- Closed the full task list when difficult-day mode is activated.
- Replaced the stale pre-integration handoff text with current implementation and validation status.

## Validation status

- Prior tested head `0d60ccd031109180a08c91e71253f23b0bc5809c`: Android CI and secret scans passed.
- Latest head `73d34beb53447850fc73804808c83712aef05474`: secret scan passed.
- Android unit tests/lint have not been run on the latest head.
- No APK was generated.

## Required before review/merge

- Current `main` is `5a871270546b0999081ab2274ec5b7474f508a1f`; this branch remains two commits behind, with merge base `4a05d6aa426a88db77e1f768fc9bcd0e7e498c05`.
- Integrate the two new `main` commits in a local checkout. They affect web index pages; preserve their content and inspect conflicts.
- Run `./gradlew :app:testDebugUnitTest :app:lintDebug --stacktrace` on the resulting head and verify secret scans.
- Keep the PR draft until checks pass. Obtain human review before merging; squash merge is preferred.

## Tooling limitation at relay

The GitHub connector reports `allow_update_branch=false`; this session has no local checkout and no workflow-dispatch action. It could not rebase/update the branch or run Android CI for the latest head. The last verified branch state is still available remotely.

## Guardrails

- Do not merge without Jasmin's explicit instruction.
- Do not generate an APK unless explicitly requested.
- Do not remove or hide the random-task die from the home flow.