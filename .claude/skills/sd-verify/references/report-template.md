# Verification report template

`sd-verify` writes `docs/architecture/verify/<change-id>.md`. It reports. It never fixes.

```markdown
# Verify: <change-id>

- **Date:** YYYY-MM-DD
- **Result:** PASS | FAIL
- **OpenSpec structure:** `openspec validate <change-id> --strict` → pass | fail | not run (reason)
- **Code against the change:** run `/opsx:verify <change-id>` in a new session. This report does not repeat it.

## Checks

| # | Check | Result | Evidence |
|---|---|---|---|
| 1 | Every decision stop has an ADR linked from design.md | | |
| 2 | Each ADR has 2+ options, a decision and why, Android, iOS, edge cases, Rules | | |
| 3 | No artifact or code contradicts a given fact | | |
| 4 | No unresolved conflict (Q) is built over | | |
| 5 | 2 to 6 scenarios, one capability, at most one stop | | |
| 6 | Task groups in order: Domain, Data, Presentation | | |
| 7 | Each PR under 400 changed lines | | |
| 8 | Each PR stays inside its group's paths | | |
| 9 | Each ADR rule tagged Konsist, detekt or test has a matching check in the repo | | |
| 10 | Every R id of the change is covered by a scenario | | |
| 11 | Generated files are current (`graph.py --check`) | | |
| 12 | Independent review found no mismatch | | |

## Findings

| # | Severity | Finding | Where | Rule or source |
|---|---|---|---|---|

Severity: BLOCKER (do not archive), WARNING (fix or explain), NOTE.

## Next

PASS: run `/opsx:archive <change-id>`, then `python3 docs/architecture/tools/graph.py --ready`.
FAIL: fix the blockers, then run `sd-verify <change-id>` again.
```
