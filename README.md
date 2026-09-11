# Executive Function App

[🇫🇷 Français](README.fr.md) · **🇬🇧 English**

> **Working title.** The final product name is still under discussion.

**A local-first, non-punitive Android app designed to reduce executive-function friction.**

**Capture what matters. Choose a realistic next action. Start with less friction. Come back without turning absence into failure.**

---

## Why this project exists

For many people with ADHD or executive-function difficulties, the problem is not a lack of productivity tools.

It is the gap between **knowing what to do** and actually being able to:

- choose where to start;
- begin without excessive setup;
- perceive time while working;
- stay with an action;
- handle interruptions;
- resume later without rebuilding the whole system.

Task managers, calendars, reminders, focus timers and routine apps already solve useful parts of this problem.

In an exploratory community corpus used during product research, **14 of 21 people who already used an organisation app also relied on at least one tool from another category**. This is a design signal, not a population-level ADHD statistic.

The goal is therefore **not to build another giant all-in-one productivity app**.

The project focuses on the connective layer between:

**capture → next action → visible time → interruption → return**

> The organisation system should be easier to use than the problem it is trying to solve.

---

## Core promise

> **Help people start, continue and resume — without turning forgetting into a fault.**

The first product loop is deliberately short.

### 1. Capture

Put a thought, task or note into an ultra-fast Inbox without mandatory categorisation.

### 2. Choose

Surface one realistic next action, optionally using information such as available time, energy or context.

### 3. Start

Begin with a low-friction, full-screen focus timer and as few decisions as possible.

### 4. Resume

When something is interrupted, preserve useful context and offer a small number of choices:

**Resume · Reduce · Postpone · Switch**

Interruption, delay and absence are treated as **normal states**, not user errors.

---

## Product principles

### Non-punitive by design

The app must not create another layer of guilt around organisation.

That means:

- no broken streak as punishment;
- no lost progress after an absence;
- no sad or disappointed companion;
- no mandatory daily check-in;
- no “failure” simply because something was postponed;
- time already spent remains meaningful even when a task is unfinished.

Returning after one day or one month should feel like **resuming**, not starting over.

### Local-first

The useful core should not depend on a cloud account.

Current design goals include:

- local data by default;
- offline operation wherever practical;
- no mandatory account for core use;
- explicit export, backup, restore and deletion;
- no silent upload of task contents in diagnostics;
- optional and explicit external integrations.

### Privacy-respecting

The project does not rely on behavioural advertising or the sale of personal productivity data.

Calendar access, wearables, desktop communication or other integrations should only exist when explicitly enabled.

### Accessibility is core functionality

Accessibility is not an upgrade.

The project is designed around:

- clear visual hierarchy;
- scalable text;
- sufficient contrast;
- a calm / reduced-stimulation mode;
- reduced animations;
- visible alternatives to gestures;
- no information conveyed by colour alone;
- progressive disclosure of advanced options.

### Low friction before feature count

A useful action should require very few taps and very few decisions.

The app should remain usable on a difficult day, when **planning itself has become work**.

### Optional means optional

Routines, statistics, notifications, calendar integration, gamification and the visual companion may all enrich the experience.

None of them should be required to use the core organisation and focus loop.

### No generative-AI dependency in the core

The essential experience is designed around local data and deterministic rules.

The app should not require a generative-AI API to decide what the user ought to do next.

---

## Current status

**Stage: runnable P1 Android prototype.**

The repository now contains a reproducible Android application for the complete first loop:

```text
Capture → choose → start → focus → interrupt → resume → finish or postpone
```

Implemented today:

- ultra-fast local capture with an optional first small step;
- manual choice or optional local task draw, followed by full-screen elapsed-time focus;
- quick capture without leaving focus;
- durable interruption context and a non-punitive return screen;
- resume, reduce, postpone/switch and complete transitions;
- process-restart-safe SQLite persistence;
- French and English resources;
- large touch targets, scalable text and TalkBack semantics;
- unit tests and Android lint; APK packaging only on explicit request.

There is no account, network permission, analytics SDK, cloud backend or automatic cloud backup. This is a validation prototype, not a claim of clinical effectiveness.


The September batch adds correctable duration learning, ordered steps, Today, simple repetitions, optional check-ins, energy/time/context filters, notes, local reminders, a widget, notification/PiP timers and local backup/restore. Suggested ordering requires an explicit action. Exact rules and limits are documented in [decision 0006](docs/adr/0006-transparent-learning-and-user-led-planning.md).

## What the first prototype should validate

Early testing should focus on behaviour rather than feature count.

Questions include:

- How long does it take to go from opening the app to actually starting?
- Is the next action immediately understandable?
- Can something be captured without derailing the current focus session?
- Does interruption preserve enough context?
- Is returning after several days easy?
- Does the interface create guilt, overload or unwanted screen time?
- Can advanced functionality remain hidden without making the app confusing?

The project does **not** claim clinical effectiveness.

Research findings are treated as design signals and hypotheses to test.

---

## Functional direction

### Core / highest priority

Planned core functionality includes:

- ultra-fast Inbox capture;
- flexible Today / next-action view;
- optional first physical step for a task;
- optional reduction of a task into a smaller concrete step;
- full-screen focus timer;
- predictable manual focus-state transitions;
- quick note or task capture during focus;
- interruption state and resume context;
- useful local reminder actions such as **Start / Done / Later**;
- local export, backup and recovery;
- accessibility and calm modes.

### Close to the core

Features to validate around the central loop include:

- sequential routines;
- flexible tasks around fixed appointments;
- low-energy mode;
- intentional longer / hyperfocus sessions;
- user-controlled check-ins;
- lightweight calendar export or display;
- widgets and simple wearable controls;
- gentle anti-distraction “speed bumps”.

The preferred direction is flexibility rather than making an imperfect day destroy a minute-by-minute schedule.

---

## Companion and visual progression

A companion and small home/crafting layer are being considered as an **optional motivation layer**, not as the heart of the app. No character or graphical substitute is included in the prototype until the official illustrations are ready.

The current direction is a companion that is:

- discreet;
- warm;
- slightly strange;
- observant rather than coaching;
- optional;
- quiet when the user wants it to be.

The companion must never:

- lose affection because the app was ignored;
- become hungry or sad during an absence;
- guilt the user into returning;
- block task capture or focus;
- require care before the user can deal with real life.

Real-world effort may eventually create visible and persistent progress through objects, crafting or changes to the companion's space.

The app must remain fully useful when this layer is disabled.

The visual identity and final companion design are still being developed with the illustrator. Integration will start from the official assets, without a generated temporary silhouette and without changing the functional flow.

The [prototype visual-direction guide](docs/VISUAL_DIRECTION.md) defines the light interface palette, its accessible implementation tones, and the boundary between functional UI and future illustration work. An [interactive HTML home preview](docs/home-preview.html) makes that direction reviewable without compiling the application.

---

## Later possibilities

Features intentionally outside the first critical path may include:

- advanced calendar synchronisation;
- deeper recurrence rules;
- local desktop relay;
- advanced automations;
- richer companion customisation;
- additional home/crafting content;
- wearable integrations;
- external body-doubling links;
- optional hosted services where real recurring infrastructure costs exist.

These are roadmap items, not promises of current functionality.

---

## Data model direction

The architecture separates **planning** from **what actually happened**.

A working conceptual model is:

```text
Activity
  └─ Schedule
       └─ Occurrence
            └─ Execution
                 └─ FocusSession
```

This makes it possible to modify a future recurrence without rewriting historical executions or focus sessions.

---

## Technical direction

Current target:

- **Android**
- **Kotlin**
- **Jetpack Compose**
- local-first architecture
- deterministic behaviour for core prioritisation and reminders
- optional external integrations

The prototype should favour:

- transparent local state;
- few permissions;
- graceful offline use;
- low background activity;
- integrations that never become hidden dependencies.

Build locally with JDK 17 and Android SDK 37:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug
```

On explicit request, run `./gradlew :app:assembleDebug`. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Repository structure

```text
.github/   GitHub templates and repository metadata
app/       Android application / prototype
assets/    Visual and project assets
docs/      Product, architecture and decision documentation
github/    Roadmap, issues and repository-planning material
research/  Publishable research and benchmark material
scripts/   Repository and maintenance utilities
```

Raw survey responses, personal information, credentials, secrets and tokens must **never** be committed to this repository.

---

## Research approach

The project combines:

- direct user research;
- product-validation surveys;
- public community observations;
- competitor analysis;
- accessibility and digital-health recommendations used as design guardrails.

Research is used to answer practical product questions such as:

- What helps immediately before task initiation?
- How much information should the Today screen display?
- Can quick capture avoid becoming another forgotten Inbox?
- How should a focus timer behave during interruption?
- How should the app welcome someone back after several weeks?
- Which rewards remain motivating without becoming punitive?
- Which integrations are worth their cognitive and technical cost?

Neither community observations nor survey results are presented as representative of every person with ADHD.

---

## Business model principles

The project aims for a **genuinely useful free and open-source core**.

Anything directly supporting the central promise should remain accessible:

- essential organisation;
- task initiation;
- focus;
- interruption and resume;
- accessibility;
- local data ownership.

Paid layers may eventually fund:

- advanced automation;
- integrations with real maintenance costs;
- hosted services with real recurring infrastructure costs;
- optional support or professional services;
- non-essential cosmetic or content packs.

When a feature creates no recurring infrastructure cost, a one-time purchase is preferred over a forced subscription where practical.

The project rejects:

- behavioural advertising;
- loot boxes;
- artificial scarcity;
- countdown-driven FOMO;
- accessibility paywalls;
- deliberately frustrating free functionality.

Paid functionality should add **power**, not pressure.

---

## Open source and licence

The software core is intended to use the **GNU AGPL v3** licence.

See [`docs/LICENSING.md`](docs/LICENSING.md).

The software licence does not automatically grant rights to the project name, logo, visual identity or third-party artwork unless explicitly stated.

Open source is part of the product philosophy:

- auditability;
- durability;
- contribution;
- interoperability;
- user control.

---

## Contributing

The project is still being shaped through research and prototype testing.

Not every idea in the backlog is a committed feature.

Before proposing a large feature:

1. check the roadmap and existing issues;
2. explain which real friction it removes;
3. describe the smallest useful version;
4. consider privacy and accessibility;
5. consider additional cognitive load;
6. avoid expanding the product simply because another app has the feature.

A useful rule for contributions is:

> **Does this help the user find the thread again, or does it ask them to manage one more thing?**

See the repository issue templates for the current contribution workflow.

---

## What this project is not

This project is not:

- a medical device;
- a diagnostic tool;
- a substitute for professional care;
- a promise to “fix” ADHD;
- a mandatory cloud service;
- an AI therapist;
- a social network;
- a productivity game that punishes absence;
- an attempt to replace every specialised tool.

It is an attempt to make an important part of everyday organisation **easier to enter, easier to continue and easier to return to**.

---

## Project stewardship

The project is currently led by **Jasmin Lévêque / Le Potager du Web**.

A longer-term cooperative structure, **Le Verger du Numérique**, is being explored as a future SCOP project that could mutualise development, design, maintenance and governance across several digital projects.

It is **not currently presented as an already constituted cooperative**.

---

## Feedback

Useful feedback includes disagreement.

If the app would create more pressure, more setup, more screen time or more guilt for you, that is exactly the kind of information this project needs.

The goal is not to teach people how to maintain another productivity system.

**The goal is to make part of that maintenance disappear.**
