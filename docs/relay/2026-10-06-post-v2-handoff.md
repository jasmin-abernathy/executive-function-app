# Relay — post-V2 friction reduction — 2026-10-07

Branch: `work/post-v2-defaults-onboarding-20261006`
Pull request: #34 (open, draft, not merged)
Validated code head: `539a7457c136cf1aced9b5b7e93c820947e912df`

## Goal

Use V2 feedback to configure recurring choices once and preserve one-off overrides. Keep the die available for randomly choosing a task throughout the home flow.

## Implemented

- Home modes `ONE_NEXT`, `NOW_NEXT`, and `LIST`; recommended default `NOW_NEXT`.
- Focus presets `SHORT=10`, `NORMAL=25`, `LONG=50`, and `OPEN`.
- Primary start uses stored defaults when quick start is enabled; one-off session editing remains available.
- Learned duration stays local and only overrides a fixed preset when enabled.
- Compact home shows one current task and up to two alternatives; full list remains available.
- Difficult-day mode hides alternatives and closes an already expanded full list.
- Seven-page first-run setup, with French and English strings.
- Regression coverage for focus-preset migration and the expanded-list/difficult-day interaction.
- Random task selection via the die remains intact.

## Main integration

- `main` at integration time: `5a871270546b0999081ab2274ec5b7474f508a1f`.
- Merged into the PR branch with merge commit `53786be7d5cd7f99e8692e6afd894e669d3968e7`.
- The two incoming changes affected only:
  - `web/app.lepotager.org/index.html`
  - `web/app.lepotager.org/en/index.html`
- Their `main` content was preserved.
- After integration the PR branch was 0 commits behind `main`.

## Validation fixes found while finishing the PR

Two validation issues were found and corrected without changing the intended product behaviour:

1. `FocusPresetPreferencesTest` used the obsolete generic Robolectric call `RuntimeEnvironment.getApplication<Application>()`. It was replaced by `RuntimeEnvironment.getApplication()`.
2. `HomeModesTest.difficultDayClosesAnAlreadyExpandedTaskList` expected a lazy-list item to exist before scrolling it into composition. The test now scrolls `home-task-list` to the item before asserting it, then scrolls back to the difficult-day control.

These were test/validation defects, not changes to the die, focus defaults, difficult-day behaviour, or other product logic.

## Exact validation status

Validated code head: `539a7457c136cf1aced9b5b7e93c820947e912df`.

- GitHub Actions Android run #157: **passed**.
- Command executed by CI: `./gradlew :app:testDebugUnitTest :app:lintDebug --stacktrace`.
- Unit tests: **67 completed, 0 failed**.
- Android lint: **passed** as part of the same successful workflow.
- Secret scan run #86: **passed** on the same code head.
- No APK was generated.
- The PR remains a draft and was not merged.

A documentation-only commit updating this handoff may sit after the validated code head; it does not change Android, Gradle, web, or product code.

## Review fixes retained

- Legacy timer-mode migration is materialized into `focus_session_preset`, so a later one-off dialog timer choice cannot replace the global preset.
- Difficult-day mode closes an already expanded task list.
- The random-task die remains available and unchanged.

## Remaining before merge

- Human review of PR #34.
- Keep the PR in draft until Jasmin explicitly decides it is ready.
- Do not merge without Jasmin's explicit instruction.
- Do not generate an APK unless explicitly requested.
