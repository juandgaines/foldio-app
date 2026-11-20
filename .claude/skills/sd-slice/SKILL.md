---
name: sd-slice
description: Step 4 of the system design framework. Split one chunk into OpenSpec changes that are small enough to review, sliced vertically between changes (each change is one behavior that works end to end) and horizontally inside each change (Domain, then Data, then Presentation, one PR per task group). Finds the decision stops and blocking conflicts of each change and computes which changes can run in parallel. Use after sd-architecture, once per chunk, or when someone says slice a chunk, plan the changes, or break this into PRs.
---

# sd-slice: split a chunk into changes

Step 4 of 10. Run it once per chunk. You turn one chunk into an ordered set of OpenSpec changes, each one reviewable, and each split into task groups that become PRs.

The sequence: sd-problem → sd-api → sd-architecture → **sd-slice** → sd-deep-dive → /opsx:propose → /opsx:apply → /opsx:verify → sd-verify → /opsx:archive.

## Inputs

The chunk id, for example `sd-slice c10-upload`. If the user gives none, run `python3 docs/architecture/tools/graph.py` and offer the chunks of the earliest wave that have no changes yet.

Read `docs/architecture/CONVENTIONS.md` first. Then `graph.json`, `architecture.json`, `REQUIREMENTS.md`, `API.md`, `DECISIONS.md`, `QUESTIONS.md` and `LOG.md`.

Stop if:
- `graph.json` or `docs/architecture/tools/graph.py` is missing: "Run `sd-architecture` first."
- The chunk id is not in `graph.json`: show the valid ids.
- The chunk already has changes: ask whether to revise them. Never renumber a change that exists in `openspec/changes/`.

## OpenSpec setup, once per repo

If `openspec/` does not exist, tell the user to run `openspec init` and continue; the plan does not need it yet.

If `openspec/config.yaml` exists and does not mention `docs/architecture`, show `assets/openspec-config.yaml` and ask whether to merge it. Merge only the `context` and `rules` keys and keep everything else in the file.

## Phase 1: Understand the chunk

Collect, for this chunk only:
- its requirements (R ids) and their sources
- the containers it touches and their status
- the given facts that apply (from `SOURCES.md`, `API.md`, `DECISIONS.md`)
- open decisions (D) and conflicts (Q) that touch it
- the chunks it depends on, and which of their changes it really needs

## Phase 2: Slice vertically into changes

Each change delivers **one behavior** that works end to end and can be tested through its scenarios. A good order:

1. **Walking skeleton:** the happy path, in the foreground, smallest useful version.
2. **Resilience:** process death, reboot, offline, reconnect.
3. **Scale:** batches, pagination, large inputs.
4. **Edge flows:** errors, limits, conflicts, collisions.

For a foundation chunk, changes are horizontal on purpose (a library, a format, a store). Each one must name in `usedBy` the change that uses it next, even if that change is not planned yet.

Give each change an id `cNN-MM-<name>` (MM counts from 01 inside the chunk) and a title that says what a user or a test can see.

For each change, write 2 to 6 scenarios ("given, when, then"), each citing the R id it covers.

## Phase 3: Find the stops and blockers

A **decision stop** is a choice that has more than one reasonable answer, is hard to change later, and is not a given fact. Check each change against: where data lives on disk, a byte format on disk or on the wire, key handling, background execution, concurrency, the source of truth for lists, pagination, caching and eviction, plaintext exposure, a library that shapes later code, a different answer per platform.

- Give each stop a D id in `DECISIONS.md` (status Open, with the change id) unless one already exists.
- A change may hit **at most one** stop. If it hits two, split it.
- Never answer a stop here. `sd-deep-dive` does that with the user.

A change that touches an unresolved conflict (a Q with status Clarify or Open) gets that Q in `blockedBy`.

## Phase 4: Slice horizontally into task groups

Each task group becomes one PR under 400 changed lines.

1. **Domain:** models, use cases, interfaces, fakes, and their tests, in shared code. Always first in a vertical change.
2. **Data:** one group per data source (database, API, crypto, file store). Waits for Domain.
3. **Presentation:** one group per screen. Waits for Domain. Runs on fakes, so it can run in parallel with Data.

When Android and iOS need different code that is large on both sides (background work, a bridge to other apps, biometrics), split that group into an Android group and an iOS group with their own paths. Otherwise keep platform code in the group that needs it.

For each group: a name, the paths it may touch, the groups it waits for (`after`), and an estimate in lines. A group over 400 lines gets split. A change with more than 6 groups gets split.

## Phase 5: Check the plan with the user

Show the plan as a table: change, behavior, kind, depends on, stop, blocked by, size, number of groups. Then ask (`AskUserQuestion`): accept, split a change, merge two changes, or reorder. Repeat until accepted.

Sizing rules every change must meet (tell the user which one fails, if any):
- one behavior, one capability, 2 to 6 scenarios
- at most one decision stop
- 3 to 6 task groups, each under 400 lines (a foundation change may have 2)
- testable without later changes
- dependencies named

## Phase 6: Write

1. Write `docs/architecture/chunks/<chunk-id>.md` from `references/change-plan-template.md`.
2. Add each change to `graph.json` with status `planned`. The fields are in `references/change-plan-template.md`.
3. Add each change's dependencies on changes of other chunks. If a dependency chunk has no changes yet, depend on nothing and add a note; `graph.py` warns until it is sliced.
4. Run `python3 docs/architecture/tools/graph.py`. Fix errors, review warnings with the user.

## Done when

- Every requirement of the chunk is covered by a scenario of some change.
- Every change meets the sizing rules, or the user accepted the exception and `LOG.md` says why.
- Every stop has a D id; every blocker has a Q id.
- `graph.py` runs with no errors.

## Finish

Append a summary to `LOG.md`. Show the user the change waves for this chunk and what `python3 docs/architecture/tools/graph.py --ready` lists. Then name the first ready change: "Next: run `sd-deep-dive <change-id>`." If every change is blocked, name the Q ids to resolve and suggest slicing another chunk of the same wave meanwhile.
