# Sirapat Step 2 verification — 4 October 2026

Scope: Menu Catalog, Customer Ordering and their SQA/frontend acceptance criteria. Based on develop `727413e` (PR #17), with review fixes after `dee08a8` on `sirapat_673380293-3_01`, submitted in [PR #18](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/18). This report does not approve a PR or declare the full team's Core Flow complete.

## Follow-up review: database safety, Admin errors and CI

The review of `36eee85` identified missing destructive-test guards and a shared Admin error state. The environment-driven Menu migration, Dining migration and order/close concurrency tests now require explicit `ALLOW_DESTRUCTIVE_DB_TESTS=true`, a strict loopback JDBC URL with a `buffet_test_` database name and no URL options, and the database comment `buffet-disposable-test-only`. These checks happen before any Flyway migration or Spring datasource startup; concurrency cleanup verifies the actual connected database again before DELETE. [Guard regression tests](../../code/backend/src/test/java/com/buffetrestaurant/integration/DisposablePostgresDatabaseTest.java) cover missing opt-in, unsafe URL forms, unmarked databases and database mismatch. See [setup instructions](../../test/README.md).

Menu Admin separates catalog-load errors from create/update/delete/validation errors. Only load failures offer “โหลดข้อมูลใหม่”; a successful catalog refresh preserves mutation errors and drafts. If a mutation succeeds but its reload fails, the form is reset and save success is retained; retry fetches catalog data without repeating the mutation.

Customer multi-tab ordering is outside the current requirement scope: coordination is per tab and the cookie is shared, so an older tab can show an obsolete session after another tab scans a QR. Backend session mismatch rejection remains enforced. The [test plan](test-plan.md#customer-browser-tab-limitation) records this limitation and recovery without adding synchronization.

[CI](../../.github/workflows/ci.yml) runs backend verification with fresh marked PostgreSQL databases and frontend tests/lint/build. Reports are workflow artifacts rather than additional evidence folders in the PR. GitHub execution results are available in the PR checks and must be verified on its current revision; CI configuration alone is not evidence of a passing run.

Follow-up local verification on 4 October: 218 backend cases discovered, 216 passed, 2 Docker-dependent Stock cases skipped, with no failures/errors. This includes 27 guard regression cases and all 5 environment-driven PostgreSQL cases on new marked databases. A separate negative integration run confirmed that missing opt-in skips all 5 PostgreSQL cases and an unmarked database is rejected before migration (0 public tables afterward), including Spring startup paths. Frontend: 83 passed across 11 files, build passed, lint had 0 errors and the same 4 existing warnings. `actionlint` 1.7.12 accepted the workflow. Raw logs are ignored `code/backend/step2-safety-review-{backend,disabled,unmarked}.log` and `code/frontend/step2-safety-review-{frontend,lint,build}.log`.

The older 189 backend / 78 frontend / 13 browser results and screenshots below are historical local evidence from the preceding revision. Browser scripts were not rerun in this follow-up; no new screenshot folder is added. The previously inspected screenshots remain evidence for that recorded execution.

## Final local recheck before PR submission (prior revision)

The final 4 October review found no new actionable defect in the reviewed changes. The full backend suite was rerun after all corrections: 191 discovered, 189 passed, 2 Docker-dependent Stock security cases skipped, and no failures/errors. PostgreSQL migrations and all three order/close concurrency cases passed on fresh disposable local databases. The permanent frontend suite passed 78 cases in 11 files; 6 additional temporary review cases also passed separately. Build passed; lint reported no errors and the same 4 existing warnings.

Both browser scripts were rerun sequentially against a fresh isolated demo: 10 smoke scenarios and 3 concurrency scenarios passed. The committed evidence contains [final smoke results](../../test/evidence/sirapat-step2-2026-10-04/final-review/browser-results.json), [final concurrency results](../../test/evidence/sirapat-step2-2026-10-04/final-review/concurrency/browser-concurrency-results.json), and all 10 inspected screenshots from those executions. Only this final evidence set is retained in the PR. Task test servers were stopped after verification. Remote develop was rechecked and remained at `727413e`; the peer/schema/Billing gates below remain pending.

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

[CustomerOrderingConcurrency.test.tsx](../../code/frontend/src/features/ordering/CustomerOrderingConcurrency.test.tsx) adds 6 component cases. The API suite adds 5 context-coordination cases, covering pending exchange, newer scan during context success/failure, explicit retry and obsolete exchange failure. The complete frontend suite now has 78 passing tests across 11 files. The backend production and test sources were unchanged in this second correction stage; the final full-suite verification is summarized below.

## Prior revision delivery and verification

- Category/Menu CRUD is exercised through the real authenticated MANAGER session provider, with persisted update/delete and package-link assertions. All six mutation endpoints reject anonymous users (401) and SERVICE_STAFF/KITCHEN_STAFF/SUPERVISOR (403).
- Catalog pagination covers ascending/descending order, subsequent/empty pages, invalid bounds/fields/directions and stable ID tie-breaking. The Admin page exposes sorting and package names, with retry after a load error.
- Customer retries a failed menu load using the customer cookie after a successful single-use QR exchange. It does not redeem the consumed QR again. Cart edits are disabled while submitting, duplicate submissions are guarded immediately, and a previous session's late response cannot enter a newly scanned session.
- Shared small buttons meet the 44px minimum. Customer controls at 360px measured 44px, with the main confirmation button at 48px; page scroll width equals viewport width (360px).
- Existing applied migrations were not changed. Schema review against the current Notion ERD/Data Dictionary and historical PR #11 is recorded in [schema delta](../database/menu-ordering-schema-delta.md).

| Check | Actual result |
|---|---|
| Complete backend suite after all corrections | 191 discovered; 189 passed, 2 skipped, 0 failures/errors; includes the catalog, ordering, Auth/Stock and PostgreSQL checks below |
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

The [final evidence](../../test/evidence/sirapat-step2-2026-10-04/final-review/) contains 10 captured and visually inspected images: the 7 smoke screenshots plus single-order history, retained submission error and restored table B with a successful order. [Smoke results](../../test/evidence/sirapat-step2-2026-10-04/final-review/browser-results.json) records geometry and 10 passing scenarios; [concurrency results](../../test/evidence/sirapat-step2-2026-10-04/final-review/concurrency/browser-concurrency-results.json) records the 3 additional passing scenarios. Images and JSON come from the same final executions, at `2026-10-04T00:58:53.371Z` and `2026-10-04T00:59:20.932Z`. Earlier evidence was archived locally under ignored `test/reports/` before being removed from the PR.

[Concurrency browser script](../../test/browser/sirapat-step2-concurrency.cjs) starts history refresh before order confirmation, delays its server read until the actual POST commits, then holds acknowledgement until history observes the order. The final result is 1 POST, 1 persisted order and 1 card. The submission-error case explicitly injects a POST 503 and delays a real earlier history response; its error remains visible after refresh. The QR case delays B exchange before server processing, leaves the Customer route and uses browser Back: the remounted page waits, restores B matching its cookie and places one real B order. A direct request to session A with B's cookie still returns 404. Other loading/error/empty visual states use explicit fault fixtures; successful QR/order/catalog flows use real API/database persistence.

Final local raw logs are `code/backend/step2-final-review-backend.log`, `code/frontend/step2-final-review-frontend.log`, `code/frontend/step2-final-review-lint.log`, `code/frontend/step2-final-review-build.log`, `test/reports/final-review-browser.log`, and `test/reports/final-review-concurrency.log` (ignored execution output). PostgreSQL tests used fresh disposable databases `sirapat_menu_finalreview_20261004` and `sirapat_dining_finalreview_20261004` on loopback port 55432. Database/demo/Vite servers were stopped after verification. Peer/schema/Billing gates below remain pending; no shared migration or team approval was performed.

Backend from `code/backend`:

```powershell
# Create fresh marked disposable local databases and anon/authenticated roles (test/README.md).
$env:ALLOW_DESTRUCTIVE_DB_TESTS='true'
$env:MENU_TEST_PG_URL='jdbc:postgresql://127.0.0.1:55432/buffet_test_menu_local'
$env:MENU_TEST_PG_PASSWORD='step2-test'
$env:DINING_TEST_PG_URL='jdbc:postgresql://127.0.0.1:55432/buffet_test_dining_local'
$env:DINING_TEST_PG_PASSWORD='step2-test'
mvn '-Dmaven.repo.local=.m2-cache' test
```

For a repeated PostgreSQL Menu upgrade run, use a new empty marked disposable database; this test deliberately checks the upgrade starting at V2. With no PostgreSQL URL or no explicit destructive-test opt-in, the optional PostgreSQL cases are skipped. V9 Payment is absent from this branch; test history jumps from V8 to V10 and does not prove deployment ordering on shared Supabase.

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
