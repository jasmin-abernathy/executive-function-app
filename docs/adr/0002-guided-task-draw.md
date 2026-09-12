# ADR 0002 — Optional guided task draw

- Status: Accepted; ordering behavior extended by [ADR 0007](0007-d10-and-manual-order.md)
- Date: 2026-09-10

## Context

When several tasks are visible, choosing can itself become the blocking task. A playful die or random draw can hand off that single decision without turning the app into a game, a coach or a source of pressure.

## Decision

Add an optional local task draw to the home screen.

- The draw considers only eligible, ready tasks.
- The explicit home-screen shuffle orders all eligible READY tasks and proposes the first. Other tasks retain their relative order after that group.
- The widget and its rerolls remain proposal-only. Neither route starts a task automatically.
- The user can roll again without penalty, justification, streak loss or hidden scoring.
- A redraw avoids the immediately previous result when at least one alternative exists.
- Manual selection remains fully available.
- No interaction data leaves the device.

Optional energy, time and context filters now narrow the ready pool before selection or shuffling. The selector remains deterministic under an injected random source so it can be tested without analytics or a backend.

## Consequences

This preserves user control and local operation. Ordering is persisted only after an explicit request, using the existing schema; a check-in or a refresh never silently shuffles the list.
