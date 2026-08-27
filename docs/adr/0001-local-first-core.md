# ADR 0001 — Local-first core

**Status:** Accepted  
**Date:** 2026-08

## Context

The app is intended to reduce friction during moments of low executive function. Requiring authentication, connectivity or a cloud round-trip before capture/focus would add failure modes and privacy cost.

## Decision

The P1 core works locally without a mandatory account. Networked features must be optional and justified individually.

## Consequences

- capture and focus remain available offline;
- local persistence is a first-class requirement;
- sync is not part of P1;
- backup/export must not assume a proprietary cloud account;
- external integrations need explicit privacy/data-map updates.
