# First push with Gitling (Android)

Gitling is used here as the Git client. It can clone the private GitHub repository, show status/diffs, stage files, commit and push. Editing is done with another Android editor/file app.

## Recommended first push

1. **Clone** `jasmin-abernathy/executive-function-app` in Gitling if it is not already cloned.
2. Extract this bootstrap kit somewhere temporary.
3. In Android's Files app (or another file manager/editor), copy the kit contents **into the root of the cloned repository**.
4. If prompted about `README.md`, replace the current one with the kit version only after checking the diff. It preserves the same project direction while adding links to the new files.
5. Open the repository in Gitling.
6. Open **Status** and review every changed/added file.
7. Stage the files.
8. Commit, for example:

   `chore: bootstrap repository structure`

9. Push `main`.

## Important

Do not copy the outer folder `executive-function-app-bootstrap-v0.1.0/` into the repository as one nested directory. Copy **its contents** so `README.md`, `docs/`, `.github/`, etc. sit at repository root.

## Editing from Android

Gitling does not need to be the text editor. Use an Android editor that can open files from the system file picker/Gitling storage, then return to Gitling to inspect and stage the diff.

## After the push

On GitHub, optionally create the labels/milestones described under `github/`. They are documentation/seed files; nothing in this first push silently modifies repository settings.
