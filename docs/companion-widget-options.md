# Optional companion widget controls

This branch prepares configuration around the static companion widget without providing any companion illustration.

## Per-widget settings

Each widget instance can keep its own local preferences:

- compact-widget tap action: open / resume / capture;
- show or hide task context on the medium widget;
- show or hide medium-widget action buttons;
- future scene selection.

Preferences are removed when that widget instance is deleted.

## Future scene selection

The scene preference is semantic only. It does not draw or generate anything.

Prepared values:

- automatic;
- resting;
- crafting;
- sewing;
- observing.

`AUTO` keeps the existing deterministic time-based scene selection. Fixed choices simply override that semantic value. No choice depends on task completion, streaks or productivity.

When official illustrations arrive, these values can resolve to static asset names described in `docs/companion-widget.md`.

## Android configuration

The widget is declared `reconfigurable` and `configuration_optional`.

Default behaviour remains usable without opening configuration:

- compact tap opens the app;
- task context is visible on medium widgets;
- medium action buttons are visible;
- scene mode is automatic.

## Artwork boundary

No provisional ghost, cat, dog, hybrid creature, furniture, clothing or other substitute artwork belongs in this branch. Empty visual slots stay empty until approved illustrator assets are available.
