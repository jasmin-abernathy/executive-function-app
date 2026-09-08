# Companion home-screen widget

## Product intent

The companion widget is an optional, calm home-screen presence. It is deliberately static: no animation, no floating overlay, no movement across the launcher.

The widget supports two responsive layouts:

- **compact (~1 cell)**: mainly the future official companion illustration;
- **medium**: illustration area + lightweight context + `Open/Resume` and `Note` actions.

No streak, urgency counter, overdue count, guilt message, affection decay or companion need is shown.

## Current implementation status

The code reserves the visual areas but intentionally ships **no temporary companion drawing**. Neutral text placeholders are used until the official illustrator assets exist.

The widget reads local app state only. It can represent:

- active focus;
- interrupted/resumable focus;
- an available task;
- a quiet state with nothing pressing.

The future visual scene selection is intentionally independent from task completion. The current model includes:

- `RESTING`
- `CRAFTING`
- `SEWING`
- `OBSERVING`

Scenes rotate deterministically by day for now. They must never imply a reward/punishment relationship with productivity.

## Official artwork integration contract

When approved artwork exists, add static assets under Android resources using stable semantic names, for example:

```text
res/drawable-nodpi/
  companion_resting.webp
  companion_crafting.webp
  companion_sewing.webp
  companion_observing.webp
```

If later progression stages need separate art, keep the activity semantic first and stage second, for example:

```text
companion_resting_stage_1.webp
companion_resting_stage_2.webp
companion_crafting_stage_1.webp
```

Do not add provisional animal/ghost art to fill these slots.

### Artwork requirements

- transparent background where appropriate;
- readable at launcher-widget scale;
- static frame only;
- no baked-in text;
- no essential information encoded only in the illustration;
- safe margins so launchers can crop/pad without cutting the character;
- artwork should remain optional to understanding the widget actions.

## Interactions

- tapping the compact widget opens the app;
- `Resume` opens the current interrupted context when one exists;
- `Open` opens the app in its normal state;
- `Note` opens a global quick-capture dialog regardless of which app screen would otherwise be visible.

Widget refreshes are best-effort after task/focus state changes. A failed launcher refresh must never make a successful task action fail.

## Inspiration boundary

Older projects such as aNeko demonstrate the value of a persistent companion presence and skin/resource separation, but this implementation deliberately does **not** use aNeko's animated overlay/service model. The product requirement here is a normal Android AppWidget with static official art only.
