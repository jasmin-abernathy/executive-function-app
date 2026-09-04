# Persistent focus presence outside the app

## Product intent

A focus session should remain easy to find after the user leaves the app, locks the phone, or gets pulled into another application. The persistent surface is meant to preserve context, not create pressure.

The default Android implementation is therefore an **ongoing foreground notification** rather than a floating overlay.

## Current behaviour

When a focus starts:

- a foreground service starts;
- an ongoing, silent notification appears;
- Android's system chronometer renders elapsed time, so the app does not need a one-second background update loop;
- tapping the notification returns to the app;
- the notification is public on the lock screen, subject to the user's Android privacy settings;
- supported Android versions are asked to treat it as a promoted ongoing / Live Update candidate.

When the user interrupts a session:

- the notification remains present;
- the chronometer freezes;
- the task title and elapsed duration remain available as a calm resumption cue.

When the session is completed or postponed, the foreground service and its notification stop.

## Recovery

The focus session itself stays authoritative in the local database. The service uses `START_STICKY` and, if Android recreates it after process death, immediately adopts a temporary foreground notification before reconciling with the saved local focus state.

Opening the app also re-syncs the service from the local database. A notification/service failure must never roll back or invalidate a successful task transition.

## Notification permission

On Android 13+, the app requests notification permission when a user starts a focus session. Refusing the permission does **not** block focus: the in-app timer and all task/session data remain usable.

## Inspiration and implementation boundary

Goodtime was reviewed as a behavioural and Android-architecture reference because it solves the same persistence problem with an ongoing notification, a foreground timer service, process-death reconciliation, optional Picture-in-Picture and an optional keep-screen-on mode.

Goodtime is GPL-licensed. No Goodtime source code is copied into this implementation; the app uses its own data model, service code and wording.

## Deliberately deferred / optional surfaces

Two stronger forms of persistence may be added later, but should remain opt-in:

- **Picture-in-Picture timer**: a small floating system window while using another app;
- **Keep screen on**: prevents the display from sleeping during a focus session.

They should not be enabled by default because they are more visually intrusive and/or use more battery than the ongoing notification.

## Relationship with the companion widget

The companion home-screen widget and the focus notification have different jobs:

- the **companion widget** is a calm ambient presence on the launcher, useful before and between sessions;
- the **focus notification** is a temporary persistent execution surface while there is an active or resumable session.

Neither should use streak loss, overdue counts, guilt language, or punitive companion states.
