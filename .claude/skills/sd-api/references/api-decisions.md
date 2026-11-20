# API decision areas (design mode)

Walk these in order. Each area lists the usual options and what matters for a mobile client. Present them; the user chooses.

## 1. API style

| Option | Fits when | Watch out for |
|---|---|---|
| REST with JSON | Resource-shaped data, many clients, HTTP caching | Several round trips for one screen |
| GraphQL | Screens need data from many resources at once | Caching and offline storage are harder; query cost limits |
| gRPC | Typed contracts, streaming, internal or high-volume traffic | Browser and proxy support, debugging, binary payloads |

## 2. Real-time updates

| Option | Fits when | Watch out for |
|---|---|---|
| Polling | Updates are rare, or latency of minutes is fine | Battery and data cost |
| Server-sent events (SSE) | One-way server to app, while the app is open | Needs replay on reconnect; not delivered when the app is closed |
| WebSocket | Two-way, low-latency traffic | Connection management, reconnect state, battery |
| Push notifications plus fetch | Updates must reach a closed app | Delivery is not guaranteed; payload limits; platform services |

Decide also: how a client resumes after a disconnect (event ids, cursors), and what happens after a long one (full resync).

## 3. Authentication

| Option | Fits when | Watch out for |
|---|---|---|
| Access and refresh tokens (JWT or opaque) | Native apps, offline sessions | Refresh races, rotation, storage on the device |
| OAuth 2 / OpenID Connect with an identity provider | Social sign-in, enterprise accounts | Redirect flows on mobile, provider lock-in |
| Session cookies | Web-first products | Awkward for native apps |

Decide also: token lifetimes, rotation, what a reused refresh token does, how logout and password change affect other devices.

## 4. Pagination

| Option | Fits when | Watch out for |
|---|---|---|
| Cursor | Lists that change while the user scrolls | No jumping to page N |
| Keyset | Stable sort on indexed fields | Sort must be unique |
| Offset or page | Small, rarely changing lists | Duplicates and gaps when items change |

## 5. Sync model

| Option | Fits when | Watch out for |
|---|---|---|
| Fetch on demand | Online-only screens | Nothing works offline |
| Delta sync with a cursor or a change feed | Offline-first lists | Tombstones for deletes, cursor expiry |
| Event stream with replay | Live updates across devices | Replay window, ordering, duplicate events |

## 6. Concurrency

| Option | Fits when | Watch out for |
|---|---|---|
| Last write wins | Low-value data, single editor | Silent data loss |
| Optimistic concurrency (version or ETag on writes) | Several devices edit the same item | Clients must handle conflicts |
| Server merge | Structured data the server can merge | Not possible for encrypted content |

## 7. Large data

| Option | Fits when | Watch out for |
|---|---|---|
| Single request | Small files | No resume |
| Chunked, resumable sessions | Large files on mobile networks | Session expiry, integrity checks, quota reservation |
| Presigned URLs to object storage | Downloads and uploads at scale | URL expiry, what the URL reveals |

## 8. Errors

Decide the error format (for example a problem-details JSON body) and a catalog of codes that change client behavior: retry, re-authenticate, resolve a conflict, free space, show a message. Include retry hints for rate limits.

## 9. Retries and idempotency

Decide which writes are safe to retry and how: client-generated ids, idempotency keys, idempotent completion of multi-step operations.

## 10. Versioning

Decide how the contract evolves: a version in the path, a header, or additive changes only. Decide what an old app does when the server no longer supports it.

## 11. Privacy

For each payload, decide what the server may see. Record anything that travels in plaintext even when content is encrypted: names, sizes, timestamps, device names.
