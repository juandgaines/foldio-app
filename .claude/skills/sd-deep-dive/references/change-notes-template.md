# Change notes template

`sd-deep-dive` writes one file per change: `docs/architecture/changes/<change-id>.md`. It is the input for `/opsx:propose`, so write it for a reader who has not seen the conversation.

```markdown
# <change-id>: <title>

- **Chunk:** <chunk-id>
- **Behavior:** <one sentence>
- **Requirements:** R<n>, R<n>
- **Depends on:** <change ids>
- **Decision stop:** D<n> <title> → docs/adr/NNNN-<slug>.md, or none
- **Blocked by:** Q<n>, or none

## Given

| Fact | Source |
|---|---|

## Components (Level 3)

| Component | Container | Owns | Status |
|---|---|---|---|

## The nine questions

| # | Question | Answer | Source or decision |
|---|---|---|---|
| 1 | What do the API and the design already do for this? | | |
| 2 | Where is the data, and is it plaintext, before, during and after? | | |
| 3 | What if the process dies halfway? | | |
| 4 | What if the device is offline, or comes back online? | | |
| 5 | What if another device changes the same item meanwhile? | | |
| 6 | What must change in the local database and files? | | |
| 7 | How can it fail, and what does the user see? | | |
| 8 | Is the answer the same on every platform? | | |
| 9 | Build it, or use a library? What does the library assume? | | |

## Decisions

| D | Decision | Status | ADR |
|---|---|---|---|

## Edge cases

| Edge case | Category | Handled by |
|---|---|---|

## Implementation notes

- **Data model:** entities and the fields that matter for this change.
- **State:** what is persisted, what lives only in memory.
- **Sync:** how it reacts to server events, if at all.
- **Errors:** API error → domain error → what the user sees.
- **Tests:** one test per scenario, plus the edge cases above.

## Scenarios for the spec

1. <given, when, then> (R<n>)

## Task groups

| # | Group | Paths | After | Parallel | Estimate (lines) |
|---|---|---|---|---|---|

## Open questions

| Q | Question | Owner |
|---|---|---|
```
