# Relay — post-V2 friction reduction — 2026-10-07

Branch: `work/post-v2-defaults-onboarding-20261006`
Pull request: #34 (open, draft, not merged)
Verified head: `0d60ccd031109180a08c91e71253f23b0bc5809c`

## Goal

Use the V2 aggregated feedback to stop asking for the same focus/task choices on every start. Configure defaults once, then keep one-off overrides.

## Product decisions implemented

- Home modes: `ONE_NEXT`, `NOW_NEXT`, `LIST`; recommended default `NOW_NEXT`.
- Focus presets: `SHORT=10`, `NORMAL=25`, `LONG=50`, `OPEN=no target`.
- The primary Start action uses saved defaults directly when quick start is enabled.
- The existing `FocusStartDialog` remains available as the one-off adjustment flow; disabling quick start also keeps the dialog on primary start.
- A learned local duration may replace a fixed preset only when its preference is enabled.
- Compact home shows one current task and at most two next tasks, with an option to show the full list.
- “Today is difficult” temporarily hides alternatives without deleting or rescheduling anything.
- First-run setup has seven pages, including home-view and reusable focus choices.
- Preferences and learned durations remain local.

## Implementation

- `AppViewModel.kt`: `requestStartWithDefaults(...)` resolves the optional learned duration and starts without `PendingStart`.
- `MainActivity.kt`: persists preferences, maps old stopwatch preference to `OPEN`, and routes quick/custom starts.
- `OnboardingFlow.kt`: captures home mode, preset, quick start, learned-duration preference, and existing support settings.
- `Screens.kt`: compact home modes, difficult-day simplification, full-list toggle, and custom-start action.
- FR/EN resources and domain/Compose tests are included.

## Validation at verified head

- Android workflow run `37581816092`: success; unit tests and lint passed.
- Secret scan run `37581813817`: success.
- FR/EN resource XML parsing and key parity checks passed.
- No APK was generated.
- No human review comments or review threads had been recorded as of 2026-10-07.

Before relying on these results after any new commit, check that the workflows pass on that exact new head.

## Remaining steps

1. Finish independent review of the complete PR diff, including preferences migration, notification permission behavior, accessibility, and FR/EN copy.
2. Fix any substantial issue on this work branch and verify CI on the resulting SHA.
3. Obtain human review. Keep the PR as a draft until the owner is ready to request review.
4. Do not merge without Jasmin's explicit instruction. Squash merge is preferred so `main` receives one clean commit.

## Guardrails

- Do not generate an APK unless explicitly requested.
- Preserve existing ADR/roadmap/error documentation.
- Do not claim local test execution when only GitHub workflow results were inspected.
- If GitHub writes are blocked, finish read-only review and report the blocked operation; do not bypass the safety layer.