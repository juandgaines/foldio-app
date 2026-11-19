# Catalogs for the deep dive

## The nine questions

Ask them about the change's behavior. Every answer no source gives is an open decision (D) or an open question (Q).

| # | Question | What it usually finds |
|---|---|---|
| 1 | What do the API and the design already do for this? | Given facts. Nothing to decide |
| 2 | Where is the data, and is it plaintext, before, during and after? | Storage areas, exposure, key scope |
| 3 | What if the process dies halfway? | What to persist, how to resume |
| 4 | What if the device is offline, or comes back online? | Disable, queue or retry |
| 5 | What if another device changes the same item meanwhile? | Concurrency and conflict handling |
| 6 | What must change in the local database and files? | Updates, cleanup, dedupe |
| 7 | How can it fail, and what does the user see? | Error mapping |
| 8 | Is the answer the same on every platform? | Platform splits, spikes |
| 9 | Build it, or use a library? What does the library assume? | Library choices that must fit the stack |

## Decision stop catalog

A stop is a choice with more than one reasonable answer, that is hard to change later, and that no source fixes. Check each row against the change.

| Stop | The question behind it | Example ways |
|---|---|---|
| Storage location | Which directory or store holds this data, and does the OS clear it? | Cache dir, files dir, no-backup dir, database |
| Format on disk or on the wire | Which exact bytes are written? Must Android and iOS agree? | Field order, nonce scheme, AAD contents |
| Key handling | Where a key lives, who can use it, when it is wiped | Keystore-wrapped, derived on demand, memory only |
| Background execution | What runs when the app is closed, and under which OS limits | WorkManager, user-initiated jobs, background URLSession |
| Concurrency | What happens when two callers or two devices act at once | One lock, single flight, optimistic version |
| Source of truth | Where screens read from | Database first, network first |
| Pagination | Who loads pages and where they are merged | Paging library, own paginator |
| Caching and eviction | How much is kept and what is removed first | Size cap with LRU, keep until unpinned |
| Plaintext exposure | Whether unencrypted data ever leaves memory | Stream, temp file with cleanup |
| Library that shapes the code | A dependency that every later change builds on | Database, networking, crypto, DI |
| Platform split | The same behavior needs a different answer per platform | Shared logic with platform actuals |

Not stops: names, UI layout, retry counts, timeouts, folder structure. Write those as notes.

## Edge case catalog

For every stop, list the edge cases whose answer would change the decision.

- **Lifecycle:** killed mid-operation, device reboot, swipe-away versus a system kill, low memory, background time limits
- **Connectivity:** offline start, reconnect, a network drop mid-transfer, metered-network rules
- **Concurrency:** two devices change the same item, two workers in one app race (token refresh), the echo of your own change
- **Security:** weak parameters from the server, truncated or reordered encrypted data, key reuse, secrets in logs or crash reports, stale credentials after a change on another device
- **Storage:** disk full, cache cleared by the user or the OS, files too large to hold in memory
- **Session:** expiry mid-operation, a revoked session, account switch, a biometric change
- **Platform:** behavior that differs between Android and iOS, OS version limits, a revoked permission
- **Limits:** quotas, rate limits, size caps, expiry windows
