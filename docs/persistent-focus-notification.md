# Persistent focus presence — Android architecture note

Status: implementation plan, intentionally separate from the static companion widget.

## Product goal

Keep the current focus session visible outside the app without making the companion move, without polling every second, and without turning the launcher widget into a live timer.

The persistent surface is a system notification. The companion remains a separate, static, optional launcher widget.

## Reference reviewed

Goodtime (`adrcotfas/goodtime`) was reviewed as a reference for the Android behavior, not as a source to copy.

Useful ideas observed in Goodtime:

- an ongoing notification while a timer is active;
- the Android/SystemUI chronometer displays elapsed/countdown time without app-side one-second redraws;
- notification actions can return to the timer;
- active and finished notifications are treated as different lifecycles;
- Goodtime uses a foreground service and `specialUse` because its timer also owns background timer lifecycle/recovery work.

Goodtime is GPL-3.0. No code or artwork is copied into this project.

## Decision for our first implementation

Do **not** introduce a foreground service yet.

Our current focus clock is already timestamp-based (`segmentStartedAt` + `elapsedBeforeSegmentMs`). It does not need a process running every second to remain correct. Android can render an ongoing notification chronometer from a base timestamp itself.

For the first iteration:

1. create a low-noise notification channel dedicated to an active focus session;
2. while `FocusStatus.RUNNING`, show an ongoing notification with the task title and system chronometer;
3. while `FocusStatus.INTERRUPTED`, show the same session as resumable with frozen elapsed time;
4. cancel it on postpone or completion;
5. rebuild it from persisted local state when the app process returns;
6. tapping it opens the app at the existing focus/resume flow;
7. no companion image, no animation, no custom RemoteViews;
8. no one-second background polling.

## Why no foreground service yet

A foreground service adds Android/Play policy surface (`FOREGROUND_SERVICE`, a declared service type, and for an uncategorised timer potentially `specialUse`). The current app does not yet need continuous background computation. Adding one before a concrete need would violate the project rule to avoid changing more layers than necessary.

Re-evaluate a foreground service only if device tests show that a plain ongoing notification cannot meet one of these requirements:

- reliable persistence across ordinary process death;
- direct notification actions that must execute while the app is not running;
- future countdown alarms/session-finished work that truly requires an active foreground lifecycle.

## Android 13+ permission

`POST_NOTIFICATIONS` is a runtime permission on Android 13+. Do not request it on app launch.

The future UX should ask contextually when the user explicitly enables the persistent timer / first chooses to display focus outside the app, with a clear “Not now” path. A denial must not block focus sessions and must not be asked again on every session.

## Android 16/17 live-update possibility

Recent Android versions support promoted ongoing/live-update notifications. This could improve visibility on supported devices, but it is an enhancement, not an MVP requirement. Keep the base notification correct first, then add promotion behind capability/version checks if useful.

## Non-punitive constraints

- no red badge showing overdue work;
- no notification when there is no active/resumable focus session;
- no sound/vibration for the ongoing timer itself;
- no repeated reminders simply because a session is paused;
- no companion sadness/urgency copy;
- notification can be disabled without disabling the timer.

## Validation before merge

- unit-test notification state mapping separately from Android UI where possible;
- build + lint on the repository's actual AGP/Kotlin/SDK matrix;
- test Android 8/11, 13+, and current target SDK behavior;
- verify the latest branch SHA is green before calling the feature validated;
- keep any future GitHub Actions migration in a separate change.
