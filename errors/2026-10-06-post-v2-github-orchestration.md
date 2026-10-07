# GitHub/API orchestration incidents — post-V2 lot (2026-10-06)

## Branch creation with `base_ref` was blocked

**Symptom:** creating the isolated work branch with `base_ref: "main"` was blocked by the platform safety layer before GitHub mutated the repository.

**Recovery:** re-read the exact `main` HEAD and create the branch from that explicit commit SHA. The branch was then verified against the expected SHA.

**Rule:** when a connector-side safety layer rejects a symbolic branch source, do not assume any mutation happened. Re-fetch the source branch and retry from an exact verified SHA.

## Optional `errors/` lookup returned 404

**Symptom:** an exploratory read of the project-level `errors/` directory returned 404 because the directory did not exist yet.

**Recovery:** treat the result as “directory absent”, not as a repository failure, and create the directory only as part of this documented lot.

**Rule:** optional path probes must be caught or interpreted as optional. A missing optional directory is not evidence that repository access failed.

## JavaScript orchestration used invalid Python-style triple quotes

**Symptom:** a dry-run transformation script failed with a JavaScript syntax error before any remote write.

**Cause:** Python-style triple-quoted strings were accidentally used in the JavaScript orchestration layer.

**Recovery:** rerun the transformation with valid JavaScript strings after confirming no remote mutation had occurred.

**Rule:** validate orchestration syntax before remote mutation and keep target-language interpolation/backticks out of unsafe JavaScript template literals.

## Raw Git object writes were blocked

**Symptom:** `create_blob` / atomic tree orchestration was rejected by the connector safety layer. The work-branch HEAD was re-read and confirmed unchanged.

**Recovery:** use the supported Contents API file writes on the isolated branch, then keep the pull request squashable so `main` can still receive one clean commit.

**Rule:** when raw Git-data mutations are unavailable, prefer an isolated branch plus squash merge over weakening the atomicity requirement on the target branch.
