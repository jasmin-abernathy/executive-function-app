# First push checklist

- [ ] Clone/open the private repo in Gitling.
- [ ] Copy the **contents** of this kit into the repository root.
- [ ] Review the `README.md` diff before replacing the existing file.
- [ ] Run `python scripts/repo_hygiene.py` if Python is available; otherwise rely on the GitHub Action after push.
- [ ] In Gitling, inspect **Status** and the diff.
- [ ] Confirm there is no raw survey export, email list, token, key or keystore.
- [ ] Stage the intended files.
- [ ] Commit: `chore: bootstrap repository structure`.
- [ ] Push `main`.
- [ ] On GitHub, create labels/milestones from `github/` when useful.
- [ ] Keep the repository private until the public-facing name/licence/contacts are ready.
