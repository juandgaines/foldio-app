# JSON format

Two files are the source of truth. `tools/graph.py` validates them and generates `ARCHITECTURE.md`, `GRAPH.md` and `architecture.html`. Never write waves, unlocks, critical paths or diagrams by hand: the script computes them.

## architecture.json

```json
{
  "project": { "name": "Foldio", "description": "Encrypted, offline-first file app" },
  "elements": [
    { "id": "user", "name": "User", "kind": "person", "status": "given",
      "description": "Stores and opens private files", "source": "REQUIREMENTS.md R1" },
    { "id": "foldio", "name": "Foldio", "kind": "system", "status": "given",
      "description": "Android and iOS apps, one shared KMP codebase" },
    { "id": "api", "name": "Foldio API", "kind": "external", "status": "given",
      "description": "Auth, metadata, upload sessions, events", "source": "API.md" },
    { "id": "android", "name": "Android app", "kind": "container", "parent": "foldio",
      "tech": "Kotlin, Compose, shared KMP", "status": "given", "description": "UI and shared logic" },
    { "id": "file-store", "name": "File store", "kind": "store", "parent": "foldio",
      "tech": "App-private storage", "status": "open", "description": "Areas: look-once cache, pinned, staging, edit copy" },
    { "id": "android-vault", "name": "Vault", "kind": "component", "parent": "android",
      "status": "open", "description": "Holds the master key in memory" }
  ],
  "relations": [
    { "from": "user", "to": "android", "label": "Uses" },
    { "from": "android", "to": "api", "label": "Calls", "tech": "HTTPS, JSON" }
  ],
  "zones": [
    { "id": "memory", "name": "App memory" },
    { "id": "storage", "name": "App storage" },
    { "id": "network", "name": "Network" }
  ],
  "data": [
    { "id": "master-key", "name": "Master key", "status": "given", "source": "REQUIREMENTS.md R40",
      "locations": [ { "zone": "memory", "plaintext": true, "survivesDeath": false, "backgroundAccess": false } ] }
  ],
  "crossings": [
    { "from": "memory", "to": "network", "data": ["auth-key"], "allowed": true,
      "rule": "Only SHA-256 of the Argon2id output", "status": "given", "source": "API.md /auth/login" },
    { "from": "memory", "to": "storage", "data": ["master-key"], "allowed": false,
      "rule": "Never written in plaintext", "status": "given" }
  ],
  "responsibilities": [
    { "concern": "Encrypt file content", "owner": "client", "status": "given", "source": "REQUIREMENTS.md R41" },
    { "concern": "Detect name collisions", "owner": "server", "status": "given", "source": "API.md 409 FILE_DUPLICATE_NAME" }
  ]
}
```

| Field | Values |
|---|---|
| `kind` | `person`, `system` (the app itself or any system you build), `external` (systems you do not build, including OS services and other apps), `container`, `store`, `component` |
| `parent` | Required for `container` and `store` (a `system`) and for `component` (a `container` or `store`) |
| `status` | `given`, `open`, `conflict` |
| `owner` | `client`, `server`, `os`, `third-party`, `shared` |
| `allowed` | `true`: this data may cross. `false`: it must never cross |

Optional on every element, relation, data item, crossing and responsibility: `source`, `notes`.

Level 1 shows people, systems and externals. Level 2 shows the containers and stores inside each system. Level 3 shows the components inside one container. Relations are drawn at the highest level that makes sense: a relation from a component to the API shows up as container to API in Level 2 and system to API in Level 1. You write each relation once, at the most specific level you know.

## graph.json

```json
{
  "project": { "name": "Foldio" },
  "areas": [
    { "id": "foundation", "name": "Foundations" },
    { "id": "files", "name": "Files" }
  ],
  "chunks": [
    { "id": "c03-crypto-core", "title": "Crypto core", "area": "foundation", "kind": "foundation",
      "size": "M", "dependsOn": ["c01-scaffold"], "containers": ["android", "ios"],
      "requirements": ["R40", "R41"], "summary": "Argon2id, AES-256-GCM chunks, key wrapping, shared test vectors" }
  ],
  "changes": [
    { "id": "c03-01-key-derivation", "chunk": "c03-crypto-core", "title": "authKey and KEK from a password",
      "kind": "foundation", "size": "S", "dependsOn": [], "usedBy": ["c07-02-login"],
      "stops": ["D1"], "blockedBy": [], "status": "planned", "scenarios": 3,
      "requirements": ["R40"],
      "taskGroups": [
        { "name": "Domain", "paths": ["shared/src/commonMain/kotlin/crypto", "shared/src/commonTest"], "after": [], "estimateLines": 250 },
        { "name": "Android", "paths": ["shared/src/androidMain"], "after": ["Domain"], "estimateLines": 150 },
        { "name": "iOS", "paths": ["shared/src/iosMain"], "after": ["Domain"], "estimateLines": 150 }
      ] }
  ]
}
```

| Field | Values |
|---|---|
| Chunk `kind` | `capability` or `foundation` |
| Change `kind` | `vertical` or `foundation` |
| `size` | `S`, `M`, `L` (weights 1, 2, 3 on the critical path) |
| Change `status` | `planned` (sd-slice), `designed` (sd-deep-dive), `proposed`, `verified` (sd-verify), `archived` |
| `dependsOn` | Chunk ids for chunks, change ids for changes |
| `usedBy` | Required on foundation changes: the change that uses it next. It may name a change that is not planned yet |
| `stops` | D ids of the decision stops this change hits |
| `blockedBy` | Q ids of unresolved conflicts. Empty it when the conflict is answered |
| `taskGroups[].after` | Names of groups in the same change that must merge first |

The script also reads `openspec/changes/` in the repo. A change folder that exists means at least `proposed`. A folder under `openspec/changes/archive/` whose name ends with the change id means `archived`. The higher of the two statuses wins.

## Commands

```
python3 docs/architecture/tools/graph.py           validate, then regenerate everything
python3 docs/architecture/tools/graph.py --check   validate, and fail if a generated file is stale
python3 docs/architecture/tools/graph.py --ready   list the changes that can start now
```

Errors stop generation: unknown ids, missing parents, cycles, bad values. Warnings are printed and also shown in `GRAPH.md` and the viewer: sizing rules, a chunk dependency with no matching change dependency, a foundation change without `usedBy`, a wave that is a single L chunk.
