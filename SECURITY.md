# Security policy

## Reporting a vulnerability

Until a dedicated security contact is published, do **not** open a public issue containing exploit details, personal data, secrets or screenshots with sensitive content.

If the repository is still private, report the problem directly to the repository owner through an existing private channel. Before a public release, this file must be updated with a dedicated private reporting route.

## Sensitive information

Never commit:

- `.env` files;
- `local.properties`;
- Android signing keys / keystores;
- API tokens or OAuth secrets;
- production credentials;
- raw participant data;
- real task/note content used for debugging.

The repository hygiene check in `scripts/repo_hygiene.py` rejects several common secret-bearing file types, but it is not a substitute for review.
