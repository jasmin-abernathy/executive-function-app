# ADHD / Executive Function App — working repository

> **Working title only.** The final product name has not been selected yet.

[Français](README.fr.md)

A local-first, non-punitive planning and focus app designed to reduce the cost of
**starting, continuing and returning** when executive function is low.

The project is not trying to become a feature-heavy productivity suite. Its first
value loop is intentionally narrow:

**Capture → choose → start → interrupt/resume → return without penalty.**

## Current status

The product principles and user research are already advanced, but the main app is
still at the **prototype / MVP preparation** stage. The visual world, companion,
house and advanced integrations are not prerequisites for proving the core loop.

This repository therefore separates:

- **P0 — framing and safeguards**
- **P1 — functional MVP**
- **P2 — advanced prototype**
- **P3 — documented beta**
- **P4 — only after validation**
- **premium candidates**, which are economic/product candidates, not promises

See [ROADMAP.md](ROADMAP.md) and [docs/MVP.md](docs/MVP.md).

## Product promise

The app should remain usable when planning itself feels difficult.

Core rules:

- one useful action should require very few gestures and decisions;
- interruptions and absences never erase progress;
- no broken streaks, guilt loops, leaderboards or punishment mechanics;
- advanced complexity stays hidden until requested;
- local use works without a mandatory account or cloud dependency;
- accessibility and essential organization features are not paywalled;
- the game layer is optional;
- the companion is a game character, **not an AI therapist or synthetic friend**;
- no advertising;
- no generative AI inside the app.

More detail: [docs/PRODUCT_PRINCIPLES.md](docs/PRODUCT_PRINCIPLES.md).

## What is expected to stay free

The free/core product includes the functions that make the app genuinely usable for
executive-function difficulties, including:

- essential timer and focus controls;
- quick capture;
- interruption/resume support;
- Hyperfocus support when implemented;
- non-punitive return;
- local-first data ownership;
- accessibility settings and readability palettes;
- essential task organization;
- basic local notifications;
- export/backup/restore/delete controls.

Premium candidates are limited to additional power, costly integrations or optional
cosmetics. See [docs/PREMIUM_BOUNDARY.md](docs/PREMIUM_BOUNDARY.md).

## Privacy model

The target architecture is local-first:

- no account required for the core app;
- no tracking by default;
- task content stays on-device unless the user explicitly exports or sends it;
- external calendar access is optional and granular;
- raw questionnaire responses and participant information are **never stored in this
  repository**;
- bug reports must never silently attach task content.

See [PRIVACY.md](PRIVACY.md) and [docs/DATA_MAP.md](docs/DATA_MAP.md).

## Repository layout

```text
.github/              GitHub forms, PR template and repository hygiene workflow
app/                  Application source code when implementation begins
assets/               Licensed visual assets only
docs/                 Product, safety, privacy, roadmap and development decisions
github/               Labels, Project setup and initial issue seed data
research/             Anonymised/public findings only — never raw participant data
scripts/              Local setup and GitHub bootstrap helpers
```

## GitHub bootstrap

The kit includes a safe bootstrap tool.

```bash
python scripts/bootstrap-github.py --repo OWNER/REPO
```

This is a **dry run**. Nothing is created.

After reviewing the output:

```bash
python scripts/bootstrap-github.py --repo OWNER/REPO --apply
```

It can create the label set, milestones, the repository setup issue with persistent
GitHub checkboxes, and the initial P0/P1 issues.

See [docs/GITHUB_SETUP.md](docs/GITHUB_SETUP.md).

## License

The repository decision is **GNU AGPL v3, version 3 only** for the project code and
backend.

The included `LICENSE` file is deliberately a placeholder rather than a hand-copied
legal text. **Before making the repository public**, replace it with the official,
unmodified GNU AGPL v3 text:

```bash
./scripts/fetch-agpl-license.sh
```

or on Windows PowerShell:

```powershell
.\scripts\fetch-agpl-license.ps1
```

The software licence does **not** grant rights to use the final project name, logo or
identity as though a fork were the original project. See [TRADEMARKS.md](TRADEMARKS.md)
and [docs/LICENSING.md](docs/LICENSING.md).

## Research and evidence

This is a product informed by co-design, user research and current recommendations.
It must **not** be described as clinically validated unless future evidence actually
supports that claim.

Preferred wording: **“co-designed and informed by user research and recommendations.”**

See [docs/RESEARCH_POLICY.md](docs/RESEARCH_POLICY.md).

## Contributing

Before proposing a feature, read:

1. [docs/PRODUCT_PRINCIPLES.md](docs/PRODUCT_PRINCIPLES.md)
2. [docs/MVP.md](docs/MVP.md)
3. [ROADMAP.md](ROADMAP.md)
4. [CONTRIBUTING.md](CONTRIBUTING.md)

A feature that adds pressure, data collection or configuration burden needs a much
stronger justification than a feature that reduces friction.
# executive-function-app
