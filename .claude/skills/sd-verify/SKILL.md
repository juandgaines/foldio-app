---
name: sd-verify
description: Step 9 of the system design framework. Check that a finished change follows the design, after /opsx:verify has checked the code against the change. Verifies ADR links and content, given facts, unresolved conflicts, sizing and slicing rules, PR sizes and paths, enforcement of ADR rules, requirement coverage and fresh generated files, with an independent reviewer. Reports and never fixes. Also runs a full project check. Use before /opsx:archive, or when someone says verify the change, check the design, or is this ready to archive.
---

# sd-verify: check the change against the design

Step 9 of 10. Run it once per change, after `/opsx:verify` and before `/opsx:archive`. OpenSpec checks that the code matches the change. You check that the change matches the design: its decisions, its given facts, its size and its slices. You report. You never fix.

The sequence: sd-problem → sd-api → sd-architecture → sd-slice → sd-deep-dive → /opsx:propose → /opsx:apply → /opsx:verify → **sd-verify** → /opsx:archive.

## Inputs

The change id, for example `sd-verify c10-01-upload-foreground`, or `sd-verify --full` for the whole project.

Read `docs/architecture/CONVENTIONS.md` first. Then `graph.json`, `changes/<change-id>.md`, the ADRs it links, `DECISIONS.md`, `QUESTIONS.md`, `REQUIREMENTS.md`, `API.md`, and `openspec/changes/<change-id>/` (proposal, specs, design, tasks).

Stop if:
- `changes/<change-id>.md` is missing: "Run `sd-deep-dive <change-id>` first."
- `openspec/changes/<change-id>/` is missing and the change is not archived: "Run `/opsx:propose <change-id>` first."

Ask the user whether `/opsx:verify <change-id>` already ran in a fresh session, and whether it found CRITICAL issues. Record the answer. Do not repeat its checks: task completion and code-to-scenario coverage are its job.

## Change checks

Run each check and record PASS, FAIL or SKIPPED with evidence (file and line, command output, PR number).

1. **OpenSpec structure.** Run `openspec validate <change-id> --strict` if the CLI is installed. Record the result. If it is not installed, SKIPPED.
2. **ADRs linked.** Every D id in the change's `stops` has an ADR in `docs/adr/`, `DECISIONS.md` marks it Decided, and `design.md` links it.
3. **ADR content.** Each linked ADR has: quoted context, at least two real options, the decision and why, Android, iOS, every edge case from the deep dive answered, and a Rules section with each rule tagged Konsist, detekt, test or AI review.
4. **Given facts.** No artifact and no code contradicts a given fact from `SOURCES.md`, `API.md` or `DECISIONS.md`.
5. **Conflicts.** `blockedBy` is empty, and no artifact or code builds on a Q that is still Clarify or Open.
6. **Change size.** One capability, 2 to 6 scenarios, at most one stop.
7. **Slice order.** `tasks.md` groups go Domain, then Data, then Presentation; platform groups only where the plan has them.
8. **PR size.** Each PR of the change is under 400 changed lines. With the GitHub CLI: `gh pr list --state all --search "<change-id>" --json number,title,additions,deletions,files`. Without it, ask the user for the PR links.
9. **PR paths.** Each PR only touches the paths of its task group in `graph.json`.
10. **Rules enforced.** For each ADR rule tagged Konsist, detekt or test, a matching test or config exists in the repo. Search for the rule id or its subject.
11. **Requirements covered.** Every R id of the change appears in at least one scenario of its spec.
12. **Generated files current.** `python3 docs/architecture/tools/graph.py --check` passes.
13. **Independent review.** Start a subagent with no context from this session. Give it the change folder, the ADRs, `changes/<change-id>.md` and the PR diffs. Ask it to report only mismatches between the design and the code, with file and line, and no style comments. Record what it finds.

Severity for each failed check: BLOCKER (do not archive), WARNING (fix or explain in the PR), NOTE.

## Full mode

`sd-verify --full` checks the whole project instead of one change:

- every R id is covered by a chunk, and every chunk's R ids by its changes' scenarios
- every chunk has changes, or is listed as not sliced yet
- every element in `architecture.json` is used by a chunk
- every D is Decided, Dropped, or Open with a change id; every Q has an owner
- every open or conflict element, crossing and responsibility has a D or a Q
- ADR statuses: Proposed ones the reviewer has not reviewed yet
- `graph.py --check` passes, and `graph.json` statuses match `openspec/changes/`

Write `docs/architecture/verify/full-YYYY-MM-DD.md`.

## Write

1. Write `docs/architecture/verify/<change-id>.md` from `references/report-template.md`.
2. If the result is PASS, set the change's status to `verified` in `graph.json` and run `python3 docs/architecture/tools/graph.py`.
3. Append a summary to `LOG.md`.

## Done when

Every check has PASS, FAIL or SKIPPED with evidence, and the report is written.

## Finish

PASS: "Next: run `/opsx:archive <change-id>`, then `python3 docs/architecture/tools/graph.py --ready` to pick the next change." Name the changes it unlocks.

FAIL: list the blockers, each with its file and the rule or source it breaks. "Fix them, then run `sd-verify <change-id>` again."
