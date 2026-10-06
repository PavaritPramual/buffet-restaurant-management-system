# Final test plan — Buffet Restaurant Management System

Owner: ศิระพัทธ์. Updated 6 October 2026 from [Notion Step 3](https://app.notion.com/p/37e90b8ff964834fad3701e2d8115de2). Each feature owner tests their module; ปวริศช์ reviews E2E/traceability and ศรัณย์ reviews API/State. Final acceptance requires the reviewed release commit to be deployed and checked.

## Scope and baseline

Start from integrated develop **54e3538** (PR #21). Table/Session, cookie authentication, Menu/Ordering, Kitchen/Serving, request bill, Payment, close and Manager master-data screens are implemented. [Step 2 closure](pavarit-step2-close-report.md) and [completion report](pavarit-step2-completion-report.md) are historical evidence. Their 303 backend / 109 frontend results do not certify a later release.

Final adds Stock opening target/active lifecycle and separate Profile names/phone (เมธัส), production deployment (ธีรเมธ), release integration (ปวริศช์) and API audit (ศรัณย์). ศิระพัทธ์ owns Customer/shared UI fixes, regression, responsive review and evidence. Feature UI defects go to that feature's owner with reproduction steps. No new gateway, WebSocket, analytics or automatic stock purchasing.

## Layers and evidence

| Layer | Purpose | Evidence and limits |
|---|---|---|
| Unit / frontend component | Rules, state, QR StrictMode/remount/races, confirmation, duplicate actions, role routes, expiry | Vitest/JUnit; mocks and fixture providers identified |
| Controller / H2 integration | Validation/status/ErrorResponse, persistence, cookie/Origin, payment/close | Surefire reports; H2 is not PostgreSQL lock/grant evidence |
| Disposable PostgreSQL | Fresh/upgrade migration, constraints/grants, actual transaction lock contention | Marked isolated loopback databases; passed/failed/skipped counts |
| Local browser Core Flow | Real HTTP/cookies, separate role/device contexts, dynamic IDs, responsive pages | Results + screenshots; no HTTP mocks; local is not public acceptance |
| Controlled browser state fixtures | Loading/empty/error and request delays | Separate report; injected responses are not a real API outage |
| Public release browser | HTTPS/Origin/cookie, roles, persisted full flow on deployed commit | URL + deployed commit + owner schema/runtime confirmation + timestamp |

Record source revision/tree or diff identity, commands, environment, start/end time, passed/failed/skipped counts, warnings and screenshot provenance. Keep historical, current local and public results separate. An authored test or workflow is not a passing execution.

## Environments and safety

- Backend tests use isolated H2 by default. Testcontainers cases skip without Docker; report skipped cases.
- [CI](../../.github/workflows/ci.yml) provisions disposable PostgreSQL and runs backend verify plus frontend tests/lint/build. Inspect the actual PR head.
- Environment-driven PostgreSQL tests require ALLOW_DESTRUCTIVE_DB_TESTS=true, explicit loopback JDBC port, buffet_test_ database name without options and database comment buffet-disposable-test-only. Payment uses a non-superuser backend role. See [testing guide](../../test/README.md).
- Never run destructive tests/demo seeding on team Supabase. Shared forward migrations require owner review; do not edit V1–V14, repair checksums or reserve another owner's migration number.
- Local browser uses isolated runtime and real session/database providers. H2 demo must override its Menu/Ordering/Fulfillment fixture defaults; disable .env import for that process.
- Public browser uses owner-supplied HTTPS URL, four test accounts and approved test tenant/data scope. No demo login or seed creation on public. Deployed revision is supplied by runtime owner; a page title cannot prove it.
- Exclude passwords, cookies, QR credentials and storage state. Mask the entire Staff QR card in images; never save QR URLs, request bodies or authorization headers.

## Regression cases

| ID | Happy case | Error / permission case |
|---|---|---|
| QR-01 | Fragment exchanged once and removed; StrictMode shares exchange | Used/malformed token rejected; no token in API paths or fallback to old session after failed rescan |
| QR-02 | Two independent phones redeem successive refreshed QR codes | Wrong session/forged credential rejected; old QR cannot be reused |
| QR-03 | Same-tab new QR clears old menu/cart/history; latest context wins | Late old menu/order/bill cannot overwrite new scan; retry consumed QR via cookie |
| ORDER-01 | Package menu, quantities/removal, confirmation, one order | Zero/negative/duplicate/unavailable/out-of-package rejected without saving; pending double clicks send one POST |
| ORDER-02 | RECEIVED → PREPARING → READY → SERVED through Kitchen/Staff | Wrong role/invalid transition denied; refresh before acknowledgement preserves one card/latest status |
| BILL-01 | Confirmation → REQUESTED; both phones stop orders; Staff badge | Cookie/Origin/session guards; idempotent request; Order after request=409 |
| BILL-02 | Payment → PAID, due=0, paid=recorded amount; still ACTIVE | Payment before request=409; duplicate does not add payment; closed session cannot pay/order |
| CLOSE-01 | Separate confirmed close → AVAILABLE and customer access revoked | Unpaid close denied; PostgreSQL Order/Request Bill/Payment/Close recheck after session lock |
| ROLE-01 | Manager admin; Supervisor stock; Staff table/serving/billing; Kitchen kitchen | Anonymous401/wrong role403; direct URL/case/trailing slash; spoofed X-User-Role denied |
| AUTH-01 | Login restore, logout then protected API401, all four roles | Expiry removes protected UI; late auth cannot restore it; failed logout keeps session/error |
| STOCK-01 | Target default0/active=true; Manager edits; shortage=max(target−quantity,0) | Negative/invalid/over-precision DECIMAL(12,3) and unauthorized writes denied; target never changes quantity/creates stock-in |
| STOCK-02 | Manager deactivate/reactivate; history readable | Inactive IN/ADJUST denied by UI/backend until active; balances/history preserved |
| PROFILE-01 | New first/last≤100, phone≤20; Manager completes legacy data | New names required; validation/roles; legacy nullable/displayName fallback without guessed name splitting |
| MIGRATION-01 | H2/PG fresh+upgrade preserve users/shared PK/display_name/email/history | Owner checks number/history/checksum/grants; no destructive shared DB run |
| DEPLOY-01 | Same-origin HTTPS web/API/Swagger; actual providers/final schema/deep links | Cold start/loading, failures, invalid cookie/Origin; no fixtures or leaked credentials |

Stock/Profile exact endpoints and phone validation policy await their owner's reviewed contract. Do not invent them. See [traceability](requirement-test-traceability.md).

## Browser and responsive procedure

Customer **360px**, Staff/Kitchen **768px**, Manager/Supervisor **1280px**. Separate context for each employee role and phone; actual login cookies; derive IDs from UI/responses. Capture normal/loading/empty/error/confirmation. Check width, labels, Thai wording, focus, shared Noto Sans Thai/tokens/components, ≥44px controls and ≥48px Customer primary action. Inspect each image before claiming visual QA.

Open table → QR → order → kitchen → serve → request bill → payment → close. Second phone must disable orders after polling, display recorded PAID/due amounts, and lose access after close. QR remount/StrictMode/rescan races use a separate controlled-delay report.

One ordering tab per browser profile is supported. Another tab exchanging a different session changes the shared HttpOnly cookie: old-session requests are rejected, but cross-tab notifications are not implemented. Multiple phone contexts do not prove multi-tab synchronization.

## Exit gates

1. Current PR backend/PG/frontend/lint/build/local browser recorded; no flow/permission defect; skipped tests resolved at release.
2. Stock/Profile code, DTO/migrations and responsive screenshots reviewed on integrated revision.
3. Public URL/deployed commit/schema/provider confirmation supplied; public regression rerun with separate role/device contexts and dynamic IDs.
4. ปวริศช์ E2E/traceability and ศรัณย์ API review recorded; main/release/deployed revision matches evidence. ศิระพัทธ์ cannot self-approve team gates.

Current runs/dependencies: [Step 3 report](sirapat-step3-report.md). Module contribution: [Menu SOLID/JPA notes](../architecture/sirapat-menu-ordering-solid-jpa.md).
