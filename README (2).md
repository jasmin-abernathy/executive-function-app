# Pixel Trackers Manager

[Lire en français](README.fr.md)

Pixel Trackers Manager (PTM) is a local-first WordPress privacy audit and documentation assistant developed by **Le Potager du Web**.

It helps site administrators understand what their WordPress site actually does: detect trackers and third-party services, review legal pages, document relevant data practices, and optionally manage visitor consent.

> **Current status:** test build `0.0.2-test4`. PTM is not a legal certification tool and does not replace advice adapted to the organisation's real activities.

## Core principles

- **Observe first, ask second.** PTM reuses information already available in WordPress before asking the administrator to enter it again.
- **Local by default.** Audit results, settings and questionnaire answers stay in WordPress unless the administrator explicitly starts an external lookup.
- **No silent legal publishing.** PTM can prepare drafts and proposed updates, but important public changes require an explicit administrator action.
- **No dark patterns.** If the native consent interface is enabled, Accept and Reject must have equal visual weight.
- **Builder-independent privacy engine.** The consent layer works at WordPress level; Elementor, Divi and other builders are compatibility layers, not dependencies.
- **Conservative writes.** When PTM cannot safely understand a builder's internal structure, it falls back to a shortcode/manual workflow rather than rewriting unknown data.

## What PTM currently covers

### Site analysis

- progressive full-site scan;
- tracker and third-party service detection;
- distinction between active evidence, an available integration, and something that still needs human verification;
- failed URLs do not stop the rest of the scan;
- WordPress drafts/private/pending/scheduled pages can be distinguished from genuine public 404 errors.

### Legal documentation

- Legal Notice, Privacy Policy, and Cookies / Consent reference pages;
- guided GDPR assistant in a dedicated tab;
- clickable documentation gaps that open the relevant assistant section;
- separation between site publisher, data controller, public contact, privacy-rights contact, and an actually designated DPO;
- public legal shortcodes and draft creation;
- explicit update workflow instead of silent publication.

### Consent

- optional native consent bar or centred panel;
- deny-by-default behaviour for recognised optional services once enabled;
- equal visual treatment for Accept all and Reject all;
- persistent “Manage my choices” control;
- administrator testing mode and compatibility work for dynamic builder modules.

### WordPress ecosystem awareness

PTM can use or inspect information from supported WordPress components where it is safe to do so, including WordPress/Gutenberg, Elementor, Divi, selected form plugins, backup plugins, and known consent or analytics integrations.

The project also aims to cover practices that happen **outside WordPress** when the website alone cannot reveal them, for example mass emails sent from Gmail/Outlook, WhatsApp contacts, booking tools such as Calendly/Koalendar, external forms, HelloAsso, payment platforms, spreadsheets, or cloud storage.

## Test installation

1. Place the repository in `wp-content/plugins/pixel-trackers-manager/`, or build a ZIP from the plugin directory.
2. Activate **Pixel Trackers Manager** in WordPress.
3. Open PTM for the first time to launch the guided setup.

Current test requirements:

- WordPress 6.5+
- PHP 7.4+

## Documentation

- [`README.fr.md`](README.fr.md) — French project overview.
- [`docs/USER-GUIDE.md`](docs/USER-GUIDE.md) — English user guide.
- [`docs/USER-GUIDE.fr.md`](docs/USER-GUIDE.fr.md) — French user guide.
- `docs/TESTS-0.0.2-test4.md` — current validation checklist.
- `docs/CAHIER-DES-CHARGES.md` — internal product specification and roadmap (French).
- `changelog.txt` — detailed test-build history.
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — contribution guide.
- [`SECURITY.md`](SECURITY.md) — vulnerability reporting policy.

## Development workflow

The repository is currently used for active pre-release development. Keep the stable working branch installable and use a development or feature branch for unfinished changes when practical.

Before a release candidate, PTM should at minimum pass:

- PHP syntax checks;
- JavaScript syntax checks;
- WordPress activation/deactivation tests;
- the current manual validation checklist;
- WordPress.org-specific checks when a build is selected for directory submission.

## Reporting issues

Please use the GitHub issue templates for reproducible bugs and feature proposals. Do not include passwords, API keys, personal data, or client-site exports in public reports.

For a vulnerability that could expose data or compromise a site, follow [`SECURITY.md`](SECURITY.md) rather than opening a public issue with exploitation details.

## License

See [`LICENSE`](LICENSE). The repository licensing and WordPress.org release metadata will be checked again before the first directory submission.
