# ADR 0003 — P1 local state and timer semantics

Status: accepted — 2026-09-02

## Decision

P1 stores tasks and focus sessions in a local SQLite database through `SQLiteOpenHelper`.

The database records transitions, not timer ticks. A running session stores:

- time accumulated before the current segment;
- the wall-clock start of the current segment;
- its durable state (`RUNNING`, `INTERRUPTED`, `POSTPONED`, `COMPLETED`).

Displayed elapsed time is calculated as accumulated time plus the non-negative difference between now and the segment start. Interrupting, completing or postponing freezes that value inside the same transaction as the related task-state update.

## Why

- survives process death and ordinary device restarts;
- does not require a foreground service merely to count time;
- avoids a database write every second;
- keeps transitions deterministic and unit-testable;
- keeps P1’s dependency and build surface small.

## Guardrails

- SQLite enforces at most one active session;
- all task/session state pairs change in one transaction;
- backward clock movement never creates negative time;
- interruption context is optional and normalized before storage;
- starting another task safely postpones any current active session;
- no task content leaves the device;
- automatic cloud backup remains disabled until explicit export/restore is designed.

## Consequences

Wall-clock changes can make one live segment temporarily inaccurate. This trade-off is accepted for P1 because it avoids permanent background execution. A monotonic-clock checkpoint strategy can be evaluated later if field testing shows this is material.
