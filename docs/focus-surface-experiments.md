# Optional focus surfaces

This document records prepared Android focus surfaces that may or may not ship.

## Baseline

The authoritative focus state remains the local database. No system surface owns or advances the timer.

The running timer is timestamp-based. Android/SystemUI can display the notification chronometer without a one-second background worker.

## Prepared surfaces

### Ongoing notification

Low-priority and silent. Running sessions are ongoing; interrupted sessions become dismissible resumption cues.

Optional notification actions are prepared for:

- interrupt;
- resume;
- quick capture;
- Picture-in-Picture;
- complete.

Direct state actions use a short-lived `BroadcastReceiver` and reload the local repository. A stale notification action is ignored safely.

### Quick capture entry point

`ExternalLaunchAction.CAPTURE` opens a reusable capture dialog over the current application state. This can be invoked by notifications, launcher shortcuts and later by other Android surfaces.

### Picture-in-Picture

The main activity declares PiP support. PiP renders a deliberately minimal surface containing only the current task title and elapsed timer.

Manual PiP can be opened from the notification or app shortcut. Automatic PiP exists as a preference but is disabled by default.

### Keep screen on

`FLAG_KEEP_SCREEN_ON` support is prepared and disabled by default. It applies only while a focus session is actively running.

### Launcher shortcuts

Static long-press shortcuts are prepared for:

- quick capture;
- mini timer / PiP;
- focus-surface experimental settings.

## Experimental settings

A small standalone settings activity allows real-device testing before a final settings architecture is designed.

Conservative defaults:

- keep screen on: **off**;
- automatic PiP: **off**;
- notification actions: **on**.

## Deliberately not added

Even in the experiments branch, the following remain excluded until a concrete requirement exists:

- full-screen intents;
- exact alarms;
- a foreground service merely to keep elapsed time ticking;
- periodic background workers for timer display;
- companion artwork or animation.

## Reference boundary

Goodtime was reviewed for behaviour and architecture only. No Goodtime code or assets are copied. The implementation uses the app's existing state model and Android APIs directly.
