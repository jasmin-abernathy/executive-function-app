# Post-V2 product direction — 2026-10-06

## Evidence used

This lot uses only aggregated V2 results. The current sample is small, so these are product hypotheses to test, not universal conclusions.

- 11 people submitted the mandatory section; 7 completed the full questionnaire.
- Starting the activity was the most common main difficulty (5/11).
- “Now + Next” and “fixed points + flexible tasks” each received 5/11 for a difficult-day structure.
- A dedicated initial setup was acceptable to 4/7 respondents, and another 2/7 accepted a few minutes when the benefit is clear.
- Clear focus modes were preferred to choosing a duration every time (6/9 versus 2/9).
- Repeated configuration/maintenance appeared among app-abandonment reasons.

## Product response

1. Expand first-run setup instead of pushing decisions into every task.
2. Add reusable home-view and focus-session defaults.
3. Make the normal Start action reuse those defaults directly.
4. Keep a one-off custom-session action for exceptions.
5. Add a compact `Now + Next` home surface and a temporary difficult-day reduction.
6. Preserve the full task list and existing D10/manual organisation tools behind deliberate actions.

## Proposed defaults

### Home views

- `ONE_NEXT`: one current action.
- `NOW_NEXT`: current action + at most two next actions.
- `LIST`: existing full list.

### Focus presets

- `SHORT`: 10 minutes.
- `NORMAL`: 25 minutes.
- `LONG`: 50 minutes.
- `OPEN`: no countdown target.

A learned local duration may take priority over a fixed preset only if the user explicitly keeps that preference enabled.

## Next UX layer

A temporary “Today is difficult” control should reduce the visible surface to the current action without deleting, rescheduling or marking anything as failed.

## Deferred

- Final illustration/companion integration waits for the incoming graphic package.
- Questionnaire V3 should be implemented after those visuals arrive so the round can compare real visual cards instead of another text-heavy survey.
- Full routine redesign and calendar integration come after this friction-reduction baseline is stable.

## Validation target

A tester should be able to configure the app once, create a task quickly, tap Start, and begin without choosing timer settings again. A one-off override must remain available without changing global defaults.
