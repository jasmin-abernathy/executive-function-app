# Initial data map

This is a design target, not yet an implementation inventory.

| Data | Default location | Network required? | Notes |
| --- | --- | --- | --- |
| Inbox/task text | On device | No | User-created content |
| Optional task notes | On device | No | Sensitive by default |
| Focus/session state | On device | No | Keep only what is needed to resume |
| Interruption/resume state | On device | No | Functional state, not a performance score |
| Preferences | On device | No | Includes readability/motion choices |
| Local notification schedule | On device / OS | No | No remote push required for core |
| External calendar data | Not in core | Optional | Future opt-in integration only |
| Research responses | Outside app repository | N/A | Never commit raw participant data |

## Questions to answer before implementing any network feature

1. What exact user-visible feature requires the network?
2. What data leaves the device?
3. Can the feature work with less data?
4. Is consent/permission granular and reversible?
5. How can the user export or delete related data?
6. What happens offline?
