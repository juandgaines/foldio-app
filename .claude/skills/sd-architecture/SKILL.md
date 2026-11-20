---
name: sd-architecture
description: Step 3 of the system design framework. Map the app top-down with the C4 model (context, containers), its data and trust boundaries, and who is responsible for what between client, server and OS. Then break the app into chunks with dependencies and waves, so the team sees the order of work and what can run in parallel. Writes architecture.json and graph.json and generates Mermaid views plus an HTML viewer. Use after sd-api, or when someone says C4, high-level architecture, containers, boundaries, or break the app into chunks.
---

# sd-architecture: high-level client architecture

Step 3 of 10. You map the app from the outside in: who and what surrounds it (Level 1), what it is made of (Level 2), where data lives and what may cross between zones, and who owns each concern. Then you cut it into chunks and order them.

The sequence: sd-problem → sd-api → **sd-architecture** → sd-slice → sd-deep-dive → /opsx:propose → /opsx:apply → /opsx:verify → sd-verify → /opsx:archive.

## Inputs

Read `docs/architecture/CONVENTIONS.md` first. Then `REQUIREMENTS.md`, `QUESTIONS.md`, `API.md`, `DECISIONS.md` (if present) and `LOG.md`.

If `REQUIREMENTS.md` is missing: "Run `sd-problem` first." If `API.md` is missing: "Run `sd-api` first."

If `architecture.json` exists, ask whether to resume, revise or start over.

## Setup

1. Copy `assets/graph.py` and `assets/viewer.html` from this skill into `docs/architecture/tools/`. If they already exist and differ, ask before replacing them.
2. Read `references/json-format.md`. It defines both JSON files.
3. Look at `examples/foldio/` for a complete example: the two JSON files and what the script generates from them.
4. Create a task list with the phases below.

From here on, you never draw a diagram by hand. You edit `architecture.json` and `graph.json`, and run:

```
python3 docs/architecture/tools/graph.py
```

It validates both files and writes `ARCHITECTURE.md`, `GRAPH.md` and `architecture.html`. Fix every error it prints and review every warning.

## Phase 1: Level 1, context

Add the people and the systems around the app as elements: the backend, storage it hands out links to, email or push providers, identity or payment providers, OS services the app relies on (secure key storage, biometrics, the document picker), and other apps that open or share the app's files.

Every element needs a source (R id, endpoint, frame) or a user answer in `LOG.md`. Add a relation for every interaction, with a short label.

## Phase 2: Level 2, containers

A container is anything that runs code or stores data on its own. Walk this checklist for every platform, and ask about each candidate the sources do not settle:

| Candidate | It is a container when |
|---|---|
| App process, one per platform | Always |
| App extension (share, file provider, widget, watch) | It runs in its own process. On iOS every extension does |
| Background runner | Work continues while the app is closed (WorkManager, background URLSession, scheduled tasks). Draw the OS runner even though you do not own it |
| Local database | Data must be read offline or survive a restart |
| File store | Files are cached, pinned, staged for upload or edited. List its areas in the description |
| Secure key storage | Keys or tokens are kept (Keystore, Keychain) |
| Bridge to other apps | Files are opened in, shared with or edited by other apps |
| Push receiver | The backend pushes messages |
| Backend and its stores | Only when the backend is built in this project |

Rules:
- Shared KMP (or any cross-platform) code is not a container. It is a component inside each app container.
- Set `platform` on containers that exist on one platform only (`android`, `ios`). The script then draws one Level 2 view per platform, with shared stores in each.
- When a requirement promises behavior that needs a container nobody drew, add it with status `open`. Uploads that survive a reboot need a background runner; opening files in other apps needs a bridge.
- An element two sources disagree on gets status `conflict` and a note with its Q id.

Run the script. **Approval gate 1:** show the containers per platform, each marked given, open or conflict. Continue only after the user confirms.

## Phase 3: Data and trust zones

List every kind of data the app handles: credentials, keys, tokens, user content, metadata, caches, queues. For each, answer from the sources or ask:

- where it is created, stored and sent
- whether it is plaintext in each place
- whether it survives process death, and whether a background runner can use it while the app is locked or closed

Add zones: app memory, app storage, secure key storage, other apps on the device, user-visible storage, network or server, OS backup. Add a crossing for every movement between zones, with `allowed: true` and what may cross, or `allowed: false` for what must never cross.

Every crossing of plaintext or an unwrapped key that no source requires becomes an open decision in `DECISIONS.md`.

## Phase 4: Responsibilities

For each concern, record who owns it: `client`, `server`, `os`, `third-party` or `shared`, and whether a source fixes it (`given`) or not (`open`). Cover at least: validation, encryption, conflict detection, conflict resolution, name collisions, quota, retries, caching and eviction, search, notifications, background resume, and every limit in the requirements.

Each `open` responsibility becomes an open decision in `DECISIONS.md`.

## Phase 5: Chunks

Cut the app into chunks and write them in `graph.json`.

- A **capability chunk** is one thing a user can do or see: "Upload files", "Browse files". It is vertical.
- A **foundation chunk** is a building block several capabilities share: scaffold and CI, design system, crypto core, local database, network client, storage layout, background runner setup, fake API. It is horizontal on purpose. Walk this list and confirm each one is needed.

For every chunk: id `cNN-<name>`, title, area, kind, size (S: 1 to 2 changes, M: 3 to 4, L: 5 to 6; split anything bigger), `dependsOn`, the containers it touches, the R ids it covers, and a one-line summary.

Rules:
- Every functional requirement is covered by at least one chunk.
- Every foundation is used by at least one capability chunk.
- A dependency means "cannot start before the other is done". Do not add dependencies that are only nice to have; they remove parallel work.
- Leave `changes` empty. `sd-slice` fills it.

Run the script. It computes the waves (chunks in the same wave can be built in parallel) and the critical path.

**Approval gate 2:** show the wave table, the critical path, the open and conflict elements, the riskiest crossings, and every warning. Ask the user about anything you are unsure of. Adjust and rerun until they confirm.

## Signs you went too deep

Stop and write a note when you are naming classes, picking libraries, designing endpoints, or splitting a chunk into tasks. Components (Level 3) and changes come later, one chunk at a time.

## Done when

- `architecture.json` has Level 1, Level 2 (per platform where needed), data, zones, crossings and responsibilities.
- `graph.json` has every chunk with dependencies, and `graph.py` runs with no errors.
- Every open element, crossing and responsibility has a D or Q id.
- The user confirmed both gates.

## Finish

Append a summary to `LOG.md`. Tell the user: number of containers (given, open, conflict), number of chunks and waves, the critical path, and the path to `docs/architecture/architecture.html` (open it in a browser; diagrams need an internet connection to load Mermaid, the chunk board works offline). Then name the first chunk to slice, usually one in wave 1: "Next: run `sd-slice <chunk-id>`."
