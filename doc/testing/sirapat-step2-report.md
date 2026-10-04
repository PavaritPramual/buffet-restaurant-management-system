# Sirapat Step 2 verification — 4 October 2026

Scope: Menu Catalog, Customer Ordering and their SQA/frontend acceptance criteria. Based on develop `727413e` (PR #17), with review fixes after `dee08a8` on `sirapat_673380293-3_01`. The changes are being submitted for peer review. This report does not approve a PR or declare the full team's Core Flow complete.

## Final recheck before PR submission

The final 4 October review found no new actionable defect in the reviewed changes. The full backend suite was rerun after all corrections: 191 discovered, 189 passed, 2 Docker-dependent Stock security cases skipped, and no failures/errors. PostgreSQL migrations and all three order/close concurrency cases passed on fresh disposable local databases. The permanent frontend suite passed 78 cases in 11 files; 6 additional temporary review cases also passed separately. Build passed; lint reported no errors and the same 4 existing warnings.

Both browser scripts were rerun sequentially against a fresh isolated demo: 10 smoke scenarios and 3 concurrency scenarios passed. The latest JSON evidence is in [final smoke results](../../test/evidence/sirapat-step2-2026-10-04/final-review/browser-results.json) and [final concurrency results](../../test/evidence/sirapat-step2-2026-10-04/final-review/concurrency/browser-concurrency-results.json). All 10 newly captured screenshots were inspected locally; the earlier committed screenshots below illustrate the same scenarios. Task test servers were stopped after verification. Remote develop was rechecked and remained at `727413e`; the peer/schema/Billing gates below remain pending.

## Corrections after two review rounds

The first 4 October run passed its original tests, but two further review rounds reproduced five gaps. Those earlier results did not cover the failing interleavings. This verification includes the corrected behavior and permanent regression tests:

- Admin catalog accepts only the latest page/sort request. Older success, error and completion responses cannot overwrite data, show a stale error or dismiss the current loading indicator. Changing page/sort also invalidates the previous request immediately; mutation reloads and error retry use the same guard.
- A customer order acknowledged during a pending history refresh increments the history revision. An older snapshot or failure is ignored, so it cannot erase that order or report an obsolete error. A subsequent refresh still updates its status normally.
- Menu and Users editors are behind a matched MANAGER route. SUPERVISOR is redirected for canonical, trailing-slash and uppercase URLs; MANAGER retains access for all six variants. Backend authorization remains enforced by the existing MANAGER-only provider and API integration tests.
- QR exchanges coalesce duplicate subscriptions only for the latest scan and execute in scan order. A-B-A queues a new final A exchange instead of reusing the initial A response; a consumed token returns its 404, without displaying an obsolete session. StrictMode duplicates and recovery after a failed exchange remain covered.
- The PostgreSQL concurrency test waits until the competing transaction actually reaches the session row lock before releasing it. Separate cases verify order-before-close, close-before-order, and a request after committed close/grant revocation. The production backend was already rejecting orders safely; its authorization behavior was not relaxed.

Permanent coverage: [OrderingConcurrency.test.tsx](../../code/frontend/src/features/ordering/OrderingConcurrency.test.tsx) (19 cases), [api.test.ts](../../code/frontend/src/features/ordering/api.test.ts) (initially 5 cases; now 10 after the next corrections), and [PostgresOrderCloseConcurrencyTest.java](../../code/backend/src/test/java/com/buffetrestaurant/integration/PostgresOrderCloseConcurrencyTest.java) (3 cases). This first correction stage added 21 frontend tests and one backend test.

## Further Customer corrections after the next review

The next review reproduced three additional Customer interleavings despite the original 67 frontend cases passing. All three are corrected in the current local changes:

- Context restoration waits for the latest QR exchange across route remounts. If a newer scan starts during a context request, its obsolete response or error is ignored and the latest cookie is read again. A failed latest exchange stays visible rather than restoring the older table; explicit retry exchanges that QR again. Successful QR/menu retry still uses the cookie without consuming its QR twice.
- POST acknowledgement adds an order only when its ID is absent from history. If refresh already saw it, that record and its newer kitchen status are retained. This covers both RECEIVED and PREPARING snapshots before acknowledgement, while the previous history-revision guard continues to protect acknowledgements from older snapshots.
- Load/QR, order submission and history refresh errors have separate state. A history success clears only its own error; order failure remains visible and the cart remains available for retry.

[CustomerOrderingConcurrency.test.tsx](../../code/frontend/src/features/ordering/CustomerOrderingConcurrency.test.tsx) adds 6 component cases. The API suite adds 5 context-coordination cases, covering pending exchange, newer scan during context success/failure, explicit retry and obsolete exchange failure. The complete frontend suite now has 78 passing tests across 11 files. The backend production and test sources were unchanged in this second correction stage; its earlier full-suite and subsequent focused verification are identified separately below.

## Current delivery and verification

- Category/Menu CRUD is exercised through the real authenticated MANAGER session provider, with persisted update/delete and package-link assertions. All six mutation endpoints reject anonymous users (401) and SERVICE_STAFF/KITCHEN_STAFF/SUPERVISOR (403).
- Catalog pagination covers ascending/descending order, subsequent/empty pages, invalid bounds/fields/directions and stable ID tie-breaking. The Admin page exposes sorting and package names, with retry after a load error.
- Customer retries a failed menu load using the customer cookie after a successful single-use QR exchange. It does not redeem the consumed QR again. Cart edits are disabled while submitting, duplicate submissions are guarded immediately, and a previous session's late response cannot enter a newly scanned session.
- Shared small buttons meet the 44px minimum. Customer controls at 360px measured 44px, with the main confirmation button at 48px; page scroll width equals viewport width (360px).
- Existing applied migrations were not changed. Schema review against the current Notion ERD/Data Dictionary and historical PR #11 is recorded in [schema delta](../database/menu-ordering-schema-delta.md).

| Check | Actual result |
|---|---|
| Complete backend suite after first fixes (earlier run) | 191 discovered; 189 passed, 2 skipped, 0 failures/errors; not rerun for the second Customer-only corrections |
| Backend focused recheck before second Customer corrections | 38 passed: catalog 16, ordering 15, Auth/Stock 4, PostgreSQL order/close 3; backend unchanged since this run |
| New authenticated catalog API tests | 16 passed |
| H2 migration from empty / V1 / V2 / V3 | 4 passed; final versions V1,V2,V3,V4,V6,V7,V8,V10 |
| PostgreSQL 18 Menu/Order upgrade from V2 | 1 passed; final versions V1–V8,V10,V11; FK/nullability/default/delete rules, positive quantity and client-role restrictions verified |
| PostgreSQL Dining Session / order-close concurrency | 4 passed on a second isolated local database: 1 migration case and 3 concurrency/revocation cases |
| Skipped tests | 2 PostgresStockSecurityIntegrationTest cases require Docker/Testcontainers; Docker daemon was unavailable |
| Frontend tests after all corrections | 78 passed in 11 files |
| Frontend lint | 0 errors; 4 existing set-state-in-effect warnings in StockPage, UsersPage, KitchenBoardPage, StaffServingPage |
| Production build | Passed |
| Browser checks after all corrections | 13 passed: 10 smoke scenarios + 3 concurrency regressions; 10 latest screenshots visually inspected |
| Swagger | Live `/v3/api-docs` includes catalog CRUD and customer endpoints; Swagger UI HTTP 200 |

## Browser evidence and reproducibility

[Browser script](../../test/browser/sirapat-step2-smoke.cjs) uses a fresh isolated H2 demo backend with `app.ordering.session-provider=database` and `app.menu.admin-access-provider=session`. Manager uses a real login/session; Customer uses a real single-use QR exchange and HttpOnly cookie. The local demo Staff provider remains a fixture for creating the session. No shared Supabase database was used.

After all corrections, captured and visually inspected 10 images in [latest evidence](../../test/evidence/sirapat-step2-2026-10-04/second-fixes/): the 7 smoke screenshots plus single-order history, retained submission error and restored table B with a successful order. [Smoke results](../../test/evidence/sirapat-step2-2026-10-04/second-fixes/browser-results.json) records geometry and 10 passing scenarios; [concurrency results](../../test/evidence/sirapat-step2-2026-10-04/second-fixes/concurrency/browser-concurrency-results.json) records the 3 additional passing scenarios. Earlier screenshots and results remain in the parent folder and first-fix subfolder.

[Concurrency browser script](../../test/browser/sirapat-step2-concurrency.cjs) starts history refresh before order confirmation, delays its server read until the actual POST commits, then holds acknowledgement until history observes the order. The final result is 1 POST, 1 persisted order and 1 card. The submission-error case explicitly injects a POST 503 and delays a real earlier history response; its error remains visible after refresh. The QR case delays B exchange before server processing, leaves the Customer route and uses browser Back: the remounted page waits, restores B matching its cookie and places one real B order. A direct request to session A with B's cookie still returns 404. Other loading/error/empty visual states use explicit fault fixtures; successful QR/order/catalog flows use real API/database persistence.

Local raw execution logs are `code/backend/step2-fixed-backend.log`, `code/frontend/step2-fixed-frontend.log`, `code/frontend/step2-fixed-lint.log`, `code/frontend/step2-fixed-build.log`, and `test/reports/step2-fixed-browser.log` (ignored execution output). Optional PostgreSQL tests used fresh disposable databases `sirapat_menu_fixed_20261004` and `sirapat_dining_fixed_20261004` on loopback port 55432. Database/test servers were stopped after verification. These changes remain local for review; no push, PR, shared migration or approval was performed in this correction run.

Latest Customer-only correction logs: `code/frontend/step2-second-fix-frontend.log`, `step2-second-fix-lint.log`, `step2-second-fix-build.log`, `test/reports/step2-second-fix-browser.log`, and `step2-second-fix-concurrency-browser.log`. All current frontend tests passed before the two browser scripts ran sequentially. No backend code changed or full backend suite was rerun in this stage; the previous 38-case recheck is in `code/backend/step2-recheck-backend.log`. Demo/Vite servers were stopped after the latest browser checks. Peer/schema/Billing gates below remain pending.

Backend from `code/backend`:

```powershell
# Point these only at fresh disposable local databases; create anon/authenticated test roles.
$env:MENU_TEST_PG_URL='jdbc:postgresql://127.0.0.1:55432/sirapat_menu_fixed_20261004'
$env:MENU_TEST_PG_PASSWORD='step2-test'
$env:DINING_TEST_PG_URL='jdbc:postgresql://127.0.0.1:55432/sirapat_dining_fixed_20261004'
$env:DINING_TEST_PG_PASSWORD='step2-test'
mvn '-Dmaven.repo.local=.m2-cache' test
```

For a repeated PostgreSQL Menu upgrade run, use a new empty disposable database; this test deliberately checks the upgrade starting at V2. With no PostgreSQL environment variables, the optional PostgreSQL cases are skipped. V9 Payment is absent from this branch; test history jumps from V8 to V10 and does not prove deployment ordering on shared Supabase.

Browser backend from `code/backend`:

```powershell
mvn '-Dmaven.repo.local=.m2-cache' spring-boot:run '-Dspring-boot.run.profiles=demo' '-Dspring-boot.run.arguments=--spring.config.import= --app.ordering.session-provider=database --app.menu.admin-access-provider=session'
```

Frontend from `code/frontend`: `npm ci`, `npm test`, `npm run lint`, `npm run build`, `npm run dev`. With Playwright and Chrome installed, run `node test/browser/sirapat-step2-smoke.cjs` from repository root. `PLAYWRIGHT_MODULE` can point to an existing Playwright installation; `STEP2_EVIDENCE_DIR` changes the output folder. Restart the demo backend before repeating the browser seed.

Then run `node test/browser/sirapat-step2-concurrency.cjs` against the same isolated demo. Its seed names are unique per run. `STEP2_CONCURRENCY_EVIDENCE_DIR` sets its output folder; optional `STEP2_FRONTEND_URL` / `STEP2_BACKEND_URL` must point to localhost or 127.0.0.1. It asserts acceptance behavior and exits nonzero if any regression returns.

## Remaining acceptance gates

- New PR peer review: Sarun for API/DTO and Pavarit for integration/shared UI; Methus for database evidence.
- Explicit decisions for existing schema extensions and updates to the canonical ERD/Data Dictionary are still pending. Approval of PR #13 by Sarun/Pavarit is recorded, but it is not evidence that every extension was added to canonical design or reviewed by Methus.
- Billing request/Bill Status depends on Teeramet's Billing/Payment contract and implementation; full Payment → Close Session and production Staff/Fulfillment authorization remain team integration work.
- Never modify migrations already applied to a target database. Coordinate V9/V10/V11 order before any shared deployment. No shared migration was applied in this run.

---

## Historical module report — 26 September 2026

The following results/dependencies were recorded on 26 September. They are retained as historical evidence and are superseded by the current verification above.

Date: 26 September 2026

## Delivered

- Persistent MenuCategory, MenuItem, Order and OrderItem with Flyway V4 after Package/Soup V3.
- Menu/category CRUD, pagination and sorting.
- Session/package menu filtering and validated order creation with initial `RECEIVED` status.
- Customer mobile ordering, cart confirmation and order status UI.
- Admin menu management with image URLs and package access.
- Shared design tokens and common components following the UI & UX Guide.

## Verification

| Check | Result |
|---|---|
| Backend test suite | 75 passed, 0 failed |
| Menu/Ordering integration tests | 17 passed, including production-safe ordering disablement, fail-closed menu mutations, session-scoped order access, persistence, invalid-order rollback, protected order history, package-reference validation and pagination |
| Flyway V1–V3 → V4 upgrade test | Passed |
| PostgreSQL 16 V1–V5 migration | Passed after syncing PR #12; five backend-role policies present, backend insert/select works, and `anon`/`authenticated` table and sequence access is revoked |
| Frontend component tests | 7 passed, 0 failed, including delete/form reset, final-page deletion, package selection and duplicate-submit protection |
| Frontend lint | Passed |
| Frontend production build | Passed |

## Integration dependencies

- Replace the production-disabled `SessionContextProvider` with the DiningSession implementation. The fixture is restricted to `local` and `test` profiles.
- Let Sarun review and consume `OrderFulfillmentContext` for Kitchen status transitions.
- Replace the fail-closed `MenuAdminAccessProvider` with the Authentication implementation that verifies an authorized staff role; the permissive provider is restricted to `local` and `test`.
- Connect the Billing request flow after its contract is ready.
- Have Methus review migrations V4 and V5 before they are applied to the shared Supabase project. V5 enables RLS and revokes direct access from `anon` and `authenticated`.
