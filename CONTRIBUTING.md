# Contributing

Thanks for helping. This project has an unusual constraint: **reducing cognitive friction is more important than adding features**.

Before proposing a feature, read:

1. [`docs/PRODUCT_PRINCIPLES.md`](docs/PRODUCT_PRINCIPLES.md)
2. [`docs/MVP.md`](docs/MVP.md)
3. [`ROADMAP.md`](ROADMAP.md)
4. [`PRIVACY.md`](PRIVACY.md)

## Before opening an issue

Ask:

- Does this reduce the effort required to start, continue or resume?
- Can the same result be achieved with less configuration?
- Does it create pressure, guilt, a streak or a failure state?
- Does it require new personal data?
- Does it belong in the MVP or is it a later experiment?

A feature that adds pressure, data collection or configuration burden needs a much stronger justification than one that removes friction.

## Pull requests

Keep PRs narrow. Include:

- what problem is being solved;
- the user-visible behaviour before/after;
- privacy/accessibility impact;
- tests performed;
- screenshots only when they contain no sensitive data.

Do not include real task content, phone numbers, participant responses, email addresses, tokens, API keys or other personal data in commits, screenshots or logs.

## Commit style

Simple Conventional Commit-style prefixes are encouraged, for example:

- `feat: add interruption resume state`
- `fix: preserve timer state after process restart`
- `docs: clarify free core boundary`
- `chore: bootstrap repository structure`

No rigid commit policy is required during early prototyping; clarity matters more than ceremony.
