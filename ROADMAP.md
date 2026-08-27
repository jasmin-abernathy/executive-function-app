# Roadmap

This roadmap describes validation stages, not release-date promises.

## P0 — Framing and safeguards

- [ ] Freeze the first user-visible value loop.
- [ ] Keep the non-punitive rules explicit and testable.
- [ ] Define the local-first data map.
- [ ] Define what never belongs in telemetry or bug reports.
- [ ] Define accessibility minimums before visual polish.
- [ ] Keep raw research data outside the repository.
- [ ] Document free-core vs optional premium boundary.
- [ ] Create the first Android architecture decision record.

**Exit criterion:** a contributor can explain what the MVP is, what it is not, what data it stores, and which product rules must not be broken.

## P1 — Functional MVP

Target loop:

**Capture → choose → start → timer → interrupt → resume → return.**

- [ ] Ultra-fast local capture / inbox.
- [ ] Minimal action view with low decision load.
- [ ] Full-screen focus timer with lightweight alerts.
- [ ] Explicit interruption state.
- [ ] Resume flow that does not punish elapsed time or absence.
- [ ] Local persistence of tasks and current session state.
- [ ] Basic local notifications.
- [ ] Essential readability/accessibility settings.
- [ ] Delete/export/backup foundations.
- [ ] No account requirement.

**Exit criterion:** the core loop works on a real Android device without network access and survives an interruption/relaunch.

## P2 — Advanced prototype

Only after P1 is usable:

- [ ] Time/energy-aware action suggestions without opaque scoring.
- [ ] Hyperfocus mode with one-gesture note/task capture.
- [ ] Routines/habits designed without debt or punishment.
- [ ] Configurable statistics and optional gamification.
- [ ] Optional companion / visual world prototype.
- [ ] More palettes and sensory/readability options.
- [ ] Optional external calendar integration.

**Exit criterion:** advanced features demonstrably reduce friction rather than making the core harder to understand.

## P3 — Documented beta

- [ ] Multi-device / multi-version Android testing.
- [ ] Accessibility review with TalkBack and large text.
- [ ] Privacy and security review.
- [ ] Reproducible release process.
- [ ] Documented backup/restore/delete behaviour.
- [ ] Public limitations and known failure modes.
- [ ] Beta feedback protocol separated from product analytics.
- [ ] Translation workflow for French and English.

## P4 — Post-validation only

Candidates, not commitments:

- [ ] Cross-device / PC relay integrations.
- [ ] Costly third-party integrations.
- [ ] Optional cosmetic packs.
- [ ] Convenience automation beyond the free core.

Any P4 item must preserve a complete, accessible, useful free core.
