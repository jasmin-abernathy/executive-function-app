# Persistent focus presence outside the app

## Product intent

A focus session should remain easy to find after the user leaves the app, locks the phone, or gets pulled into another application. The persistent surface is meant to preserve context, not create pressure.

The first Android implementation is therefore a **single system notification** rather than a floating overlay or a permanently running background component.

## Current behaviour

When a focus starts:

- a silent, ongoing notification appears if notification permission is available;
- Android's system chronometer renders elapsed time, so the app does not need a one-second background update loop;
- tapping the notification returns to the app;
- the task title is marked private for lock-screen presentation;
- the local database remains the source of truth for the session.

When the user interrupts a session:

- the notification becomes a calm, dismissible resumption cue;
- the chronometer freezes;
- the task title and elapsed duration remain available if the user keeps the notification.

When the session is completed or postponed, the notification is removed.

## Notification permission

On Android 13+, the app asks for notification permission when a user starts a focus session. Refusing the permission does **not** block focus: the in-app timer and all task/session data remain usable.

If permission is granted after the session has already started, the activity immediately asks the notifier to surface the current state.

## Why there is no foreground service yet

Goodtime was reviewed as a behavioural and Android-architecture reference because it solves a broader persistence problem with an ongoing notification, a foreground timer service, process-death reconciliation, optional Picture-in-Picture and an optional keep-screen-on mode.

This app's current MVP timer does not yet need a process to tick every second, play audio, fire an exact end-of-session event, or execute notification actions while the UI is gone. Android SystemUI can render the chronometer from a timestamp by itself. Keeping a foreground service alive at this stage would therefore add permissions, lifecycle complexity, battery/process overhead and Play-policy surface without providing a required MVP capability.

The implementation deliberately keeps the useful Goodtime pattern — **ongoing notification + system chronometer + local authoritative state** — while deferring the service layer until a real requirement justifies it.

Goodtime is GPL-licensed. No Goodtime source code is copied into this implementation; the app uses its own state model, notification code and wording.

## Recovery and current limit

The notification is posted to Android's system notification manager, while the focus state itself is persisted locally. Reopening the app re-syncs the notification from the database.

A device reboot currently clears the notification; the saved focus/task state still survives and is restored when the app is opened. Boot-time restoration can be added later if user testing shows it is useful. It is intentionally not part of this first slice.

A notification failure must never roll back or invalidate a successful task transition.

## Deliberately deferred / optional surfaces

Stronger forms of persistence may be added later, but should remain opt-in and be introduced one layer at a time:

- **foreground service**: only when background actions, reliable completion handling or other active work require one;
- **Picture-in-Picture timer**: a small floating system window while using another app;
- **Keep screen on**: prevents the display from sleeping during a focus session.

They should not be enabled by default because they are more intrusive and/or add battery and lifecycle costs.

## Relationship with the companion widget

The companion home-screen widget and the focus notification have different jobs:

- the **companion widget** is a calm ambient presence on the launcher, useful before and between sessions;
- the **focus notification** is a temporary execution surface while there is an active or resumable session.

Neither should use streak loss, overdue counts, guilt language, or punitive companion states.
