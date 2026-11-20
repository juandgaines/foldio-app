---
name: sd-problem
description: Step 1 of the system design framework. Understand what an app must do before designing it, from whatever exists (requirements, a design file, an API spec, code) or from an interview when nothing exists. Writes sources, requirements, interview questions and conflicts to docs/architecture/. Use when starting a new app or a major version, or when someone says understand the problem, scope the app, gather requirements, or start the system design.
---

# sd-problem: understand the problem

Step 1 of 10. You find out what the app must do and what is still unclear. You find facts and ask questions; the user decides. Never fill a gap with an assumption, and never answer an open question for the user.

The sequence: **sd-problem** → sd-api → sd-architecture → sd-slice → sd-deep-dive → /opsx:propose → /opsx:apply → /opsx:verify → sd-verify → /opsx:archive.

## Before you start

1. If `docs/architecture/LOG.md` exists, read it and `QUESTIONS.md`. Ask whether to resume, revise or start over. Never overwrite silently.
2. Otherwise set up the folders. Copy from this skill's `assets/` folder, skipping any file that already exists:
   - `assets/CONVENTIONS.md` → `docs/architecture/CONVENTIONS.md`
   - `assets/LOG.md` → `docs/architecture/LOG.md`
   - `assets/adr-template.md` → `docs/adr/TEMPLATE.md`
3. Read `docs/architecture/CONVENTIONS.md`. Every rule in it applies here.
4. Create a task list with the four phases below.

## Phase 1: Find and read the sources

Ask which sources exist (`AskUserQuestion`, multiSelect): written requirements, a design file, an API spec, backend code, existing app code, none of these. Ask for links or paths.

Read each source completely. If one is too large for a single read, read it in parts and write in `LOG.md` what you covered. Use an Explore subagent for a large repo.

| Source | Read it for |
|---|---|
| Requirements | Features, platforms, promises (offline, security, reliability, limits), out of scope |
| Design file | Flows, screens, states (empty, error, offline, locked, conflict), platform differences |
| API spec | Capabilities, auth, limits, error codes, what each payload reveals to the server. Step 2 reads it in depth; here, read it to find features and conflicts |
| Backend code | What the server stores and sees, background jobs, what it never does |
| App code | Modules and features that already exist. Existing code is given, not open |

Reading tips:
- A Swagger UI page is a JavaScript shell. Fetch the spec itself (`/v3/api-docs` for springdoc, `/swagger.json`, `/openapi.json`) or ask for the file.
- For Figma, list the pages, then the top-level frames, and look at screenshots only where a state matters.
- For Notion or Google Docs, use the connector if one is available; otherwise ask for an export into the repo, for example `docs/requirements.md`.

Write `docs/architecture/SOURCES.md`: one section per source with its link, what you read, and the given facts it provides, each with its location.

## Phase 2: Interview

With sources, ask only what they do not answer. Without sources, interview for the vision first: everything is open.

| Area | Ask about |
|---|---|
| Product | What the app does, for whom, the three to five core flows, what the first version must include, what is out of scope |
| Platforms | Android, iOS, desktop, web; shared code; minimum OS versions |
| Data | What the app stores, how big, how sensitive, who may see it, the server included |
| Offline | What must work with no connection: reading, writing, both |
| Devices and sync | One device or several; live updates; what happens when two devices change the same thing |
| Security | Sign-in, encryption, what the server may never see, what happens on a lost device |
| Background work | What must continue when the app is closed or the phone restarts |
| Limits | Sizes, counts, quotas, time windows, battery and data budgets |
| Other apps and the OS | Opening, sharing or editing files in other apps; notifications; widgets; extensions |
| Server | Does it exist? Is its API fixed, or designed together with the app? |

Rules:
- Ask only when the answer changes a requirement, a limit, a platform or the scope. Write everything else as a note.
- Batch up to 4 questions per `AskUserQuestion` call, 2 to 4 options each. The user can always type their own answer.
- Do not recommend answers about what the product should do. For non-functional targets you may offer common values, labeled as suggestions.
- Write each answer to `LOG.md` the moment you get it.

## Phase 3: Cross-check

Compare the sources with each other and with what the platforms allow. Look for:

- a feature with no endpoint, or an endpoint no feature uses
- a field or rule one source has and another lacks
- a screen or state in the design that the requirements do not describe, and the reverse
- one source contradicting itself, such as "plaintext never touches disk" next to "save a decrypted copy"
- numbers that disagree: limits, timeouts, quotas, cooldowns
- promises a platform cannot keep, such as "uploads survive the app being killed" when iOS cancels background transfers after the user swipes the app away, or long background work on Android 15+, which limits dataSync foreground services

Each finding becomes a conflict in `QUESTIONS.md`, with both quotes and their sources. Ask the user, for each one: Clarify (someone asks the source's owner) or Open (the team decides later).

## Phase 4: Write the requirements

Write `docs/architecture/REQUIREMENTS.md`:

```markdown
# Requirements: <App>

## Functional
| R | Area | Requirement | Source |
|---|---|---|---|

## Non-functional
| R | Quality | Target | Source |
|---|---|---|---|
(offline, security, reliability, performance, limits, accessibility)

## Platforms and constraints
| R | Constraint | Source |
|---|---|---|

## Out of scope
- <item> (<source>)
```

Write `docs/architecture/QUESTIONS.md`:

```markdown
# Questions: <App>

## Conflicts
| Q | Source A says | Source B says | Status: Clarify or Open | Owner |
|---|---|---|---|---|

## Open questions
| Q | Question | Why it matters | Owner |
|---|---|---|---|

## Answered
| Q | Question | Answer | Answered by, date |
|---|---|---|---|
```

Every requirement cites a source or a user answer. Number R and Q once and never renumber.

**Approval gate.** Show the counts (requirements, conflicts, open questions), every conflict, and the open questions. Continue only after the user confirms.

## Signs you went too deep

Stop and write a note when you are asking about endpoint fields, screen layouts, class names, libraries or retry numbers. Those belong to later steps.

## Done when

- `SOURCES.md` lists every source with its given facts.
- `REQUIREMENTS.md` covers every feature area, each requirement with a source.
- `QUESTIONS.md` marks every conflict Clarify or Open, with an owner.
- The user confirmed.

## Finish

Append a summary to `LOG.md`. Tell the user how many requirements, conflicts and open questions there are, and which Clarify items to send to the source owners now. Then: "Next: run `sd-api`."
