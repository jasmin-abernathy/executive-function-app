# Relay — post-V2 friction reduction — 2026-10-06

Branch: `work/post-v2-defaults-onboarding-20261006`

## Goal

Use the V2 aggregated feedback to stop asking for the same focus/task choices on every start. Configure defaults once, then keep one-off overrides.

## Product decisions

- Home modes: `ONE_NEXT`, `NOW_NEXT`, `LIST`.
- Recommended default: `NOW_NEXT`.
- Focus presets: `SHORT=10`, `NORMAL=25`, `LONG=50`, `OPEN=no target`.
- Primary Start uses stored defaults directly.
- Existing `FocusStartDialog` becomes the explicit “adjust this session” exception.
- A learned local duration may replace a fixed preset only when the user keeps that preference enabled.
- Compact home shows one current task and at most two next tasks.
- “Today is difficult” temporarily hides alternatives without deleting/rescheduling anything.
- Onboarding grows from 6 to 7 pages by adding home-view choice and replacing stopwatch/countdown setup with reusable focus presets.

## Existing code path causing friction

`HomeScreen.onStart` -> `AppViewModel.requestStart()` -> `PendingStart` -> `FocusStartDialog`.

The new normal path should call `AppViewModel.requestStartWithDefaults(...)` and skip `PendingStart`.

## Required source changes

- `AppViewModel.kt`: add `requestStartWithDefaults(taskId, preset, preferLearnedDuration)`.
- `MainActivity.kt`: persist/load home mode, focus preset, quick-start and learned-duration preference; normal Home start uses defaults; custom start still uses `requestStart`.
- `OnboardingFlow.kt`: extend config and add home/preset pages; total 7 pages.
- `Screens.kt`: compact `Now + Next` surface, difficult-day toggle, collapsed full list, one-off custom-session action.
- Update `OnboardingAndTimerChoiceTest.kt`; add domain-default and home-mode tests.
- Add FR/EN strings for these surfaces.

Full implementation detail is in the conversation handoff artifact `RELAIS-APP-TDAH-POST-V2-2026-10-06.md`.

## Connector blocker

This session can create branches and new Markdown files, but the safety layer blocked all existing-file mutations (`update_file`, `delete_file`) and raw Git-data writes (`create_blob`, `create_tree`). A dry-run of the planned transformations succeeded against the exact current source before any write attempt.

Do not interpret the branch as runtime-complete yet: only ADR/roadmap/error/handoff documentation is committed.

## Validation after integration

Run:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug --stacktrace
```

Do not generate an APK unless explicitly requested. Re-read the exact tested SHA, then open a draft PR to `main`. Because this work branch contains several connector-generated documentation commits, squash merge is required so `main` receives one clean commit.
