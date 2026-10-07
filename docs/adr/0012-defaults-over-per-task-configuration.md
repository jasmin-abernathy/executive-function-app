# ADR 0012 — Prefer user defaults over per-task configuration

Date: 2026-10-06

## Context

The V2 aggregated research results point to a recurring tension: people accept a useful initial setup, but repeated configuration and maintenance can become a reason to stop using an organisation app. The strongest early needs also favour starting, routines, a flexible Today view and optional reminders rather than a dense per-task form.

The existing Android flow asks for timer mode and duration whenever a task is started. A useful option has therefore become a repeated decision cost.

## Decision

- Ask once for a default home view and a default focus-session preset.
- Let the primary Start action reuse those defaults without opening a configuration dialog.
- Keep an explicit one-off “adjust this session” path.
- Keep learned duration local and optional; it may override a fixed preset only when the user enabled that preference.
- Keep Open focus mode as a stopwatch with no target duration.
- Store these choices in existing local preferences; no account or network dependency is introduced.

## Home views

- `ONE_NEXT`: show one current action.
- `NOW_NEXT`: show the current action and at most two next actions.
- `LIST`: keep the existing full list.

A temporary difficult-day control can reduce `NOW_NEXT` to the current action without deleting or rescheduling anything.

## Focus presets

- `SHORT`: 10 minutes.
- `NORMAL`: 25 minutes.
- `LONG`: 50 minutes.
- `OPEN`: no countdown target.

The values are starting defaults, not productivity goals. A one-off override remains available.

## Consequences

The common path becomes faster while the existing detailed timer dialog remains available for exceptions, reminder entry points and people who explicitly disable quick start.
