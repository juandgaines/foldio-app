---
name: sd-api
description: Step 2 of the system design framework. Settle how the app talks to its server. When an API exists, extract its contract as given facts and list what it leaves undefined. When it does not, choose the communication technology (REST, GraphQL, gRPC, real-time channels, push), auth, pagination, sync and error model with the user, record ADRs and draft an OpenAPI contract. Use after sd-problem, or when someone says API design, server contract, or how the app talks to the backend.
---

# sd-api: server and API

Step 2 of 10. You settle the contract between the app and its server. If the server already exists, its API is a source: you extract it and find its gaps. If it does not, you design it with the user, one decision at a time.

The sequence: sd-problem → **sd-api** → sd-architecture → sd-slice → sd-deep-dive → /opsx:propose → /opsx:apply → /opsx:verify → sd-verify → /opsx:archive.

## Inputs

Read `docs/architecture/CONVENTIONS.md` first. Then read `REQUIREMENTS.md`, `QUESTIONS.md`, `SOURCES.md` and `LOG.md`.

If `REQUIREMENTS.md` is missing, stop: "Run `sd-problem` first."

If `API.md` exists, ask whether to resume, revise or start over.

## Choose the mode

Ask the user, unless `SOURCES.md` or `LOG.md` already answers it:

- **Extract:** the API exists and the app adapts to it.
- **Design:** there is no API yet, or the team designs it together with the app.
- **Both:** the API exists but the team can change parts of it. Extract first, then design only the parts the user marks as changeable.

## Extract mode

1. Read the whole spec. A Swagger UI page is a JavaScript shell: fetch `/v3/api-docs`, `/swagger.json` or `/openapi.json`, or ask for the file. Use a subagent if the spec is very large, and ask it for facts with their endpoint and field names.
2. Group the endpoints by capability: auth, users, items, transfers, sync, admin.
3. Map every functional requirement (R) to the endpoints it uses. A requirement with no endpoint is a conflict. An endpoint no requirement uses is a note.
4. Extract the given facts, each with its endpoint and field:

   | Area | What to record |
   |---|---|
   | Auth | Sign-up and sign-in flow, what the client sends and gets back, token lifetimes, refresh and rotation, what a reused or expired token does |
   | Pagination | Cursor, offset or page; defaults and limits; sort guarantees |
   | Real-time | Channel (SSE, WebSocket, push), event types, replay or resume, what happens after a long disconnect |
   | Concurrency | Version fields, conflict errors, overwrite flags |
   | Long operations | Async jobs, batches, their limits, how progress is reported |
   | Large data | Upload and download protocol, chunk sizes, resume, expiry, integrity checks |
   | Limits | Sizes, quotas, rate limits, retry hints |
   | Errors | The error format and the codes that change client behavior |
   | Privacy | What each payload reveals to the server |

5. List the **gaps**: things a client must know but the spec does not say. Examples: whether two concurrent token refreshes are safe, how long the event replay window is, what logout revokes.
6. Compare with the requirements. Every disagreement becomes a conflict in `QUESTIONS.md` (both quotes, both sources). Ask the user: Clarify or Open.

## Design mode

Walk the decision areas in `references/api-decisions.md`, in order. Skip an area the requirements make irrelevant (no real-time needs, no large files). For each area that applies:

1. Show the options and their trade-offs for a mobile client: flaky networks, background limits, battery, offline use, both platforms.
2. Ask the user to choose. Never choose for them. You may say which option fits a requirement better, citing the R id.
3. Write an ADR in `docs/adr/` from `docs/adr/TEMPLATE.md`. Status: Proposed. Number ADRs in order (`0001-...`).
4. Add a row to `DECISIONS.md` (create it if missing, see below).

Then draft `docs/architecture/openapi.yaml`: one path per capability the requirements need, with request and response schemas at the level of fields that matter, the error format, and the auth scheme. Mark anything not yet decided with `x-open: true` and a description.

## Mobile questions for both modes

Answer each from the spec or the decisions. Each one nobody can answer is a gap or an open question.

- Is every write safe to retry? (idempotency keys, client-generated ids, idempotent completion)
- Can the client resume a large transfer after a kill or a reboot?
- How does the client learn about changes made on another device?
- How does the client know its cached data is stale?
- What does the client do when it was offline longer than any replay window?
- What happens when the app and the server disagree on a version of the contract?

## Files

`docs/architecture/API.md`:

```markdown
# API: <App>

- **Mode:** extract | design | both
- **Spec:** <link or openapi.yaml>

## Capabilities
| Capability | Endpoints | Requirements |
|---|---|---|

## Given
| Area | Fact | Source (endpoint, field) |
|---|---|---|

## Gaps
| Gap | Why the client needs it | Q |
|---|---|---|

## Requirement coverage
| R | Endpoints | Status: covered, missing, partial |
|---|---|---|

## Mobile questions
| Question | Answer | Source or gap |
|---|---|---|
```

`docs/architecture/DECISIONS.md`, created here if it does not exist:

```markdown
# Decisions: <App>

## Given (not decisions)
| Fact | Source |
|---|---|

## Decisions
| D | Decision | Options seen | Status: Open, Decided, Dropped | Change | ADR |
|---|---|---|---|---|---|
```

Never put a given fact in the Decisions table. Never renumber D ids.

## Signs you went too deep

Stop at the contract. The networking library, the client's retry counts and the class that wraps the API belong to `sd-deep-dive`.

## Done when

- Every functional requirement maps to endpoints, or is marked missing and listed as a conflict.
- Every gap is listed, and those that block a feature have a Q id.
- In design mode: every applicable decision area has an ADR, and `openapi.yaml` parses as YAML.
- The user confirmed the summary.

## Finish

Append a summary to `LOG.md`. Tell the user the number of capabilities, given facts, gaps, new conflicts and ADRs. Then: "Next: run `sd-architecture`."
