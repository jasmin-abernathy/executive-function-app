# ADR 0004 — Adaptive time and optional floating timer

## Status

Accepted for the local Android prototype.

## Decision

- The first three completed occurrences of a task use a count-up stopwatch.
- Repeated tasks are provisionally matched by a normalized exact title; an explicit task-template model may replace this key later.
- Only completed sessions with positive active time contribute. Interrupted time and postponed sessions do not contribute.
- From the fourth occurrence onward, the suggested countdown uses the longest completed active duration, rounded up to a whole minute, plus one additional minute of deliberate margin.
- A later longer completion raises the suggestion. Finishing early may show supportive feedback; exceeding the suggestion is neutral and becomes extra time rather than failure.
- Learning and history stay entirely on device and use deterministic rules.
- During a running session, the user may explicitly enable a movable floating timer. Android's overlay permission is requested only at that moment. Closing or completing the session removes it.

## Rationale

The longest observed duration avoids turning the feature into a speed target. The added minute creates a realistic chance of finishing with time left, while the neutral overtime state preserves the non-punitive product principle.

The floating timer reduces the need to reopen the app, but remains optional because drawing over other apps is a sensitive Android permission.
