# ADR 0002 — Optional guided task draw

- Status: Accepted
- Date: 2026-09-10

## Context

When several tasks are visible, choosing can itself become the blocking task. A playful die or random draw can hand off that single decision without turning the app into a game, a coach or a source of pressure.

## Decision

Add an optional local task draw to the home screen.

- The draw considers only eligible, ready tasks.
- It proposes one task but never starts it automatically.
- The user can roll again without penalty, justification, streak loss or hidden scoring.
- A redraw avoids the immediately previous result when at least one alternative exists.
- Manual selection remains fully available.
- No interaction data leaves the device.

The current slice uses task readiness because duration, energy and context are not yet stored. When those fields exist, they will filter the eligible pool before the draw. The selector remains deterministic under an injected random source so it can be tested without analytics or a backend.

## Consequences

This addresses choice paralysis now without prematurely changing the database schema. It also creates a clean seam for future energy, time and context filters while preserving user control and local-first operation.
