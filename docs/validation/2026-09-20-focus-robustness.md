# Focus robustness audit — 20 September 2026

Baseline: `1b891902b0bd546269d00ef08f75f36d13eb6433`, descendant of the expected
Android consolidation `a751f567781312f2cc93d6c4f15f4c0334efa2f8`.
Read repo-factory's compatibility playbook and repository AGENTS.md before edits.

## Corrections

- Serialize external capture, quick notes, pending timer setup and check-in dialogs.
  Block focus pause prompts while another modal is open.
- Save pending external actions and dialog input across activity recreation.
  An unrelated new intent no longer discards a pending task request.
- Refresh preferences on resume and before reopening setup.
- Connect the previously unused PipFocusSurface to MainActivity, update its actions
  when the persisted session changes, and allow both running and paused sessions.
  Quick notes return to PiP only after a successful save or explicit cancellation.
- Exclude overlay permission requests and open setup/note/capture flows from auto-PiP.
  Disable the overlay on successful PiP entry; tolerate system refusal of PiP.
- Guard duplicate session confirmations and preserve a newer pending request when
  an earlier confirmation finishes.
- Scroll timer setup and preserve timer/note/capture draft input.

## Added regression coverage

- Real SQLite close/reopen for both null stopwatch and exact 300000 ms countdown
  targets after pause/resume, with changed preferences and duration references.
- Both resume entry points, elapsed time excluding pauses, overtime without closure,
  v4 backup round-trip and foreign-key integrity.
- Android PiP action labels, resume session identity and quick-note intent.
- Compose saved-state restoration of timer and note input.

Existing onboarding tests cover skip callback without apply, conditional branches,
minimal-intervention defaults, custom pauses, and explicit timer selection. Static
review confirms only the final Apply writes settings; Skip marks setup seen. A user
can deliberately override minimal defaults on the subsequent settings/review pages.

## Planning: prepared, not shipped

AppDatabase and LocalBackup still use version 4. PlanningSchemaV5 and
TaskPlanningSupport have no production callers. Themes, real deadlines, planned
starts and the session-theme snapshot are not wired to persistence/UI. The existing
planning UI uses v4 importance/energy/context/today fields. No v5 migration or backup
change is made in this robustness pass. Do not advertise v5 features as delivered.
Existing foreign-key cascades cover the live planning/session tables.

## Validation and remaining limits

Local passes: check-local-data (SQLite checks, FR/EN parity, XML), Kotlin grammar,
repo_hygiene, git diff whitespace. D10 rendering/animation and task ordering code
are unchanged. No APK/AAB or release generated.

Local Gradle --version and testDebugUnitTest/lintDebug cannot download Gradle 9.5.0
(network unreachable). The PR's Android workflow must provide the compile/test/lint
result for this exact commit; local static checks do not substitute for it.
No Android SDK/device/emulator available here: TalkBack, large-font device rendering,
PiP expansion into note entry and return, overlay permission revocation, notification
chronometer behavior across zero, and physical D10/drag rendering remain unverified.
The notification uses Android's system chronometer: its countdown/overtime display
needs device validation, unlike the explicit overtime text in focus/PiP/overlay.

Android lifecycle reference consulted:
https://developer.android.com/develop/ui/views/picture-in-picture
