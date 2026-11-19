---
name: sd-deep-dive
description: Step 5 of the system design framework. Design one change in detail before it is proposed in OpenSpec. Adds its Level 3 components, runs the nine questions, walks each decision stop with the user and records an ADR, lists the edge cases and open questions, and writes implementation notes that /opsx:propose turns into proposal, specs, design and tasks. Use after sd-slice, once per change, or when someone says deep dive, design this change, or how should this work in detail.
---

# sd-deep-dive: design one change

Step 5 of 10. Run it once per change, right before `/opsx:propose`. You work out how this change works: its components, its decisions, its edge cases and its tests. The user makes every decision. You write it down so OpenSpec can build on it.

The sequence: sd-problem → sd-api → sd-architecture → sd-slice → **sd-deep-dive** → /opsx:propose → /opsx:apply → /opsx:verify → sd-verify → /opsx:archive.

## Inputs

The change id, for example `sd-deep-dive c10-01-upload-foreground`. If the user gives none, run `python3 docs/architecture/tools/graph.py --ready` and offer the ready changes.

Read `docs/architecture/CONVENTIONS.md` first. Then `graph.json` (the change), `chunks/<chunk-id>.md` (its plan), `architecture.json`, `REQUIREMENTS.md`, `API.md`, `DECISIONS.md`, `QUESTIONS.md` and `LOG.md`. Read the ADRs of the changes it depends on.

Stop if:
- the change is not in `graph.json`, or `chunks/<chunk-id>.md` is missing: "Run `sd-slice <chunk-id>` first."
- a dependency is not verified or archived: name it, and ask whether to continue anyway (the design may change when it lands).
- `blockedBy` lists a Q that is still Clarify or Open: tell the user. They can answer it now (record the answer in `QUESTIONS.md`, empty `blockedBy`), or design the parts the conflict does not touch and leave the rest open, or pick another change.

If `changes/<change-id>.md` exists, ask whether to resume or revise.

Create a task list with the phases below.

## Phase 1: Given facts

List every fact the sources already fix for this change, each with its source. These are never reopened, and the ADR's Given section repeats them.

## Phase 2: Components (Level 3)

For each container the change touches, list the components it adds or changes: what each owns, what it reads and writes, which other components or containers it talks to. Keep it at the level of a box on a diagram, not a class.

Add them to `docs/architecture/architecture.json`:

```json
{ "id": "upload-engine", "name": "Upload engine", "kind": "component", "parent": "android",
  "status": "open", "description": "Stages ciphertext, runs the 5-step upload" }
```

and a relation for each interaction:

```json
{ "from": "upload-engine", "to": "api", "label": "Initiate, chunks, complete" }
```

`parent` is a container or store id. `status` is `given`, `open` or `conflict`; set it to `given` once the change's decisions are made. Shared KMP code is a component inside each app container, not a container.

## Phase 3: The nine questions

Ask them about this change's behavior. Answer each from a source or a decision. Every answer nobody has is a new open decision (D) or open question (Q). The catalog with what each question usually finds is in `references/catalogs.md`.

1. What do the API and the design already do for this?
2. Where is the data, and is it plaintext, before, during and after?
3. What if the process dies halfway?
4. What if the device is offline, or comes back online?
5. What if another device changes the same item meanwhile?
6. What must change in the local database and files?
7. How can it fail, and what does the user see?
8. Is the answer the same on every platform?
9. Build it, or use a library? What does the library assume?

If a new decision turns out to be a second stop for this change, stop and tell the user: the change should be split with `sd-slice` first.

## Phase 4: Decision stops

For the change's stop (at most one), and any decision Phase 3 found:

1. Name the decision and its D id.
2. Quote the requirement and API text that force it.
3. Present at least two real options. For each: how it works, what it costs, what can go wrong on Android, what can go wrong on iOS. Use the stop catalog in `references/catalogs.md`.
4. List the edge cases whose answer would change the choice, from the edge case catalog.
5. Ask the user to choose (`AskUserQuestion`). Never choose for them. If they want your view, give it with its reasons and the R ids it serves, and still let them pick.
6. If the answer depends on how a real device behaves, suggest a spike: a small throwaway test on a phone, and what to measure. Record the result before deciding.
7. Write the ADR: `docs/adr/NNNN-<slug>.md` from `docs/adr/TEMPLATE.md`, status Proposed, with the change id and D id. Fill every section. The Rules section lists short checkable rules, each tagged Konsist, detekt, test or AI review.
8. Update `DECISIONS.md`: status Decided, the ADR file, the change id.

The ADR does not wait for approval. The reviewer reviews it with the change's PRs.

## Phase 5: Edge cases

For the whole change, walk the edge case catalog (lifecycle, connectivity, concurrency, security, storage, session, platform, limits). Keep the ones that apply. For each, say where it is handled: a scenario, a component, a rule in an ADR, or "out of scope" with a reason.

## Phase 6: Implementation notes and scenarios

Write what OpenSpec needs to produce good artifacts:
- data model: entities and the fields that matter for this change
- state: what is persisted, what lives only in memory
- sync: how the change reacts to server events, if at all
- errors: API error, domain error, what the user sees
- tests: one test per scenario, plus the edge cases
- final scenarios (2 to 6, each with its R id) and task groups (from the plan, adjusted)

## Phase 7: Write

1. Write `docs/architecture/changes/<change-id>.md` from `references/change-notes-template.md`. Write it for a reader who has not seen this conversation.
2. Set the change's status to `designed` in `graph.json`. Update `stops`, `blockedBy`, `scenarios` and `taskGroups` if they changed.
3. Run `python3 docs/architecture/tools/graph.py`. Fix errors.

## Signs you went too deep

Stop and leave it to the code when you are writing class signatures, SQL, UI layouts or exact retry numbers. Name the component, the rule and the test instead.

## Done when

- Every stop of the change has an ADR, and `DECISIONS.md` links it.
- Every one of the nine questions has an answer, a D or a Q.
- Every edge case that applies says where it is handled.
- `changes/<change-id>.md` exists and the change is `designed` in `graph.json`.

## Finish

Append a summary to `LOG.md`. Then tell the user:

"Next: run `/opsx:propose <change-id>` and point it at `docs/architecture/changes/<change-id>.md`. Then run `openspec validate <change-id> --strict`."
