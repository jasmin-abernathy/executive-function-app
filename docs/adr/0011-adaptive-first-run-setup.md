# ADR 0011 — Adaptive first-run setup

Status: proposed / prepared on `prep/tdah-first-run-setup-20260916`.

## Context

The application has several optional supports, but presenting them as a long settings page at first launch would add decision load before the user has done anything useful.

Two existing Potager flows provide a better interaction model:

- Le Jardinier starts with one broad situation, then asks only the questions relevant to that branch;
- the ADHD product-validation survey first asks where the difficulty occurs (starting, choosing, focus, time, switching, returning, remembering), then presents branch-specific support choices.

The survey also explicitly tests product-abandonment risks such as too many settings, too many notifications and mandatory daily check-ins. The app should not recreate those risks during onboarding.

## Decision

The first launch uses a local adaptive setup wizard rather than a flat settings form.

Flow:

1. explain that setup is local, optional and editable later;
2. ask which part most often gets difficult;
3. ask one branch-specific support question, with at most two selections;
4. configure the actual focus defaults and automatic supports that exist in the app;
5. show a review before persisting anything.

The broad difficulty answer is not treated as a diagnosis and is not sent anywhere.

The wizard writes only existing local preferences. It does not create an account, request new permissions, contact a server, or silently enable a feature before the final Apply action.

`Skip` marks the first-run flow as seen without changing the current settings. The same flow remains re-openable from the home help entry.

## Settings covered

The prepared flow can configure:

- stopwatch or countdown as the default focus mode;
- task die availability;
- pause suggestions and their interval, using the proposed presets or a custom value from 5 to 120 minutes;
- optional mood / motivation / energy check-ins;
- explicit state/context-based adaptation;
- calm mode;
- automatic Picture-in-Picture mini window.

Manual tools remain available even when they are not selected during setup. The first-run choices primarily decide defaults and automatic behaviour rather than permanently removing features.

## Branching rule

Only the follow-up support choices relevant to the selected difficulty are shown. For example, choosing difficulty deciding what to do first surfaces the die and limited-choice supports rather than pause controls.

The `minimal intervention` choice is conceptually exclusive: the final implementation/review must ensure it cannot produce contradictory defaults alongside a support that explicitly asks the app to intervene.

## Accessibility and cognitive load

- one main question per step;
- scrolling supported for large text / small screens;
- touch targets at least 48 dp;
- text labels remain visible; icons are supplemental;
- custom pause input is validated before moving to the next step;
- no timed interaction;
- Back and Skip remain available;
- no guilt, streak, urgency or diagnostic language.

## Validation before merge

Before merging this ADR's implementation:

- run Android unit tests and lint;
- verify FR/EN resource parity;
- test Skip leaves preferences unchanged;
- test branch-specific follow-up content;
- test preset and custom pause intervals;
- test Apply persists exactly the reviewed settings;
- test reopening the flow starts from the current settings;
- test the first-run flow does not overlap external capture, active focus, pending start or check-in dialogs;
- verify large text / TalkBack on an emulator or device when available.
