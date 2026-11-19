# Change plan template

`sd-slice` writes one file per chunk: `docs/architecture/chunks/<chunk-id>.md`.

```markdown
# <chunk-id>: <chunk title>

- **Area:** <area>
- **Kind:** capability | foundation
- **Depends on:** <chunk ids>
- **Requirements:** R<n>, R<n>
- **Containers:** <element ids>

## Given

What the sources already fix for this chunk, with sources. Never reopened.

## Changes

| # | Change | Behavior | Kind | Depends on | Used by | Stops | Blocked by | Size |
|---|---|---|---|---|---|---|---|---|
| 1 | cNN-01-<name> | <one behavior, end to end> | vertical | | | D<n> | | S |

## cNN-01-<name>

**Behavior:** <one sentence>

**Scenarios** (2 to 6):
1. <given, when, then> (R<n>)

**Decision stop:** D<n> <title>, or none.

**Blocked by:** Q<n> <title>, or none.

**Task groups:**

| # | Group | Paths | After | Parallel | Estimate (lines) |
|---|---|---|---|---|---|
| 1 | Domain | shared/src/commonMain/.../<feature>, shared/src/commonTest/... | | no | 250 |
| 2 | Data: <source> | shared/src/commonMain/.../data/<source> | Domain | yes | 300 |
| 3 | Presentation: <screen> | composeApp/.../<screen> | Domain | yes | 350 |

(Repeat the section for each change.)

## Not in this chunk

What a reader might expect here, and which chunk or change has it.
```

## Rules

- A vertical change delivers one behavior a user or a test can see.
- A foundation change names the change that uses it next in **Used by**.
- The Domain group comes first. Data and Presentation groups wait for it, then can run in parallel because screens run on fakes.
- A platform group (Android or iOS) only touches its own source set and app.
- If a change needs a second stop, a second capability, or more than 6 groups, split it.

## The change in graph.json

Add one entry per change to the `changes` list of `docs/architecture/graph.json`:

```json
{
  "id": "c10-01-upload-foreground",
  "chunk": "c10-upload",
  "title": "Upload one file while the app is open",
  "kind": "vertical",
  "size": "M",
  "dependsOn": ["c03-03-chunk-cipher", "c07-02-login"],
  "usedBy": [],
  "stops": ["D8"],
  "blockedBy": [],
  "status": "planned",
  "scenarios": 4,
  "requirements": ["R20"],
  "taskGroups": [
    { "name": "Domain", "paths": ["shared/src/commonMain/kotlin/upload/domain/"], "after": [], "estimateLines": 250 },
    { "name": "Data: staging store", "paths": ["shared/src/commonMain/kotlin/upload/data/staging/"], "after": ["Domain"], "estimateLines": 300 },
    { "name": "Data: upload API", "paths": ["shared/src/commonMain/kotlin/upload/data/remote/"], "after": ["Domain"], "estimateLines": 300 },
    { "name": "Presentation: picker and snackbar", "paths": ["composeApp/src/commonMain/kotlin/files/upload/"], "after": ["Domain"], "estimateLines": 250 }
  ]
}
```

| Field | Values |
|---|---|
| `kind` | `vertical` or `foundation` |
| `size` | `S`, `M`, `L` |
| `dependsOn` | Change ids that must be done (verified or archived) first |
| `usedBy` | Required on foundation changes: the change that uses it next. It may name a change not planned yet |
| `stops` | D ids. At most one |
| `blockedBy` | Q ids of unresolved conflicts. Empty it when the conflict is answered |
| `status` | `planned` here. Later: `designed` (sd-deep-dive), `proposed`, `verified` (sd-verify), `archived` |
| `taskGroups[].after` | Names of groups in the same change that must merge first |

Then run `python3 docs/architecture/tools/graph.py`.
