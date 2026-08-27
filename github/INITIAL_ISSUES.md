# Initial issues to create

These are issue seeds, not a mandatory sprint.

## P0

### Freeze P1 state machine
Document the minimal states for capture, ready, focus, interrupted, resumed and completed/returned. Define transitions without performance scoring.

### Confirm local data model
List the minimum fields needed for task/inbox items and focus/resume state. Avoid speculative metadata.

### Accessibility acceptance criteria
Turn `docs/ACCESSIBILITY.md` into testable acceptance criteria for P1 screens.

### Create demo-data policy
Define safe synthetic task/note content for screenshots, bug reports and demos.

## P1

### Bootstrap Android project
Create the first compilable Kotlin/Compose app under `app/` using current stable tooling at implementation time.

### Implement ultra-fast capture
A new inbox item can be saved locally without requiring metadata.

### Implement focus timer state
Create a full-screen local focus/timer state with lightweight alerts.

### Implement interruption/resume
Persist interruption context and offer a one-action resume path after relaunch.

### Implement local persistence tests
Verify task/session state survives process death and app relaunch.

### Implement reduced-motion and text scaling baseline
Core screens remain usable with large text and reduced motion.

### Define export/delete prototype
Create the smallest transparent path for exporting and deleting local data.

## Research

### Convert current survey findings into product hypotheses
Only aggregated/anonymised findings; no raw response export in Git.
