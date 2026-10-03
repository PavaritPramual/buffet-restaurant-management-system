# Sirapat Step 2 verification — 4 October 2026

Scope: Menu Catalog, Customer Ordering and their SQA/frontend acceptance criteria. Based on develop `727413e` (PR #17). This report describes the tested change in this PR; it does not approve the PR or declare the full team's Core Flow complete.

## Current delivery and verification

- Category/Menu CRUD is exercised through the real authenticated MANAGER session provider, with persisted update/delete and package-link assertions. All six mutation endpoints reject anonymous users (401) and SERVICE_STAFF/KITCHEN_STAFF/SUPERVISOR (403).
- Catalog pagination covers ascending/descending order, subsequent/empty pages, invalid bounds/fields/directions and stable ID tie-breaking. The Admin page exposes sorting and package names, with retry after a load error.
- Customer retries a failed menu load using the customer cookie after a successful single-use QR exchange. It does not redeem the consumed QR again. Cart edits are disabled while submitting, duplicate submissions are guarded immediately, and a previous session's late response cannot enter a newly scanned session.
- Shared small buttons meet the 44px minimum. Customer controls at 360px measured 44px, with the main confirmation button at 48px; page scroll width equals viewport width (360px).
- Existing applied migrations were not changed. Schema review against the current Notion ERD/Data Dictionary and historical PR #11 is recorded in [schema delta](../database/menu-ordering-schema-delta.md).

| Check | Actual result |
|---|---|
| Complete backend suite | 190 discovered; 188 passed, 2 skipped, 0 failures/errors |
| New authenticated catalog API tests | 16 passed |
| H2 migration from empty / V1 / V2 / V3 | 4 passed; final versions V1,V2,V3,V4,V6,V7,V8,V10 |
| PostgreSQL 18 Menu/Order upgrade from V2 | 1 passed; final versions V1–V8,V10,V11; FK/nullability/default/delete rules, positive quantity and client-role restrictions verified |
| Existing PostgreSQL Dining Session / order-close concurrency | 3 passed on a second isolated local database |
| Skipped tests | 2 PostgresStockSecurityIntegrationTest cases require Docker/Testcontainers; Docker daemon was unavailable |
| Frontend tests | 46 passed in 9 files |
| Frontend lint | 0 errors; 4 existing set-state-in-effect warnings in StockPage, UsersPage, KitchenBoardPage, StaffServingPage |
| Production build | Passed |
| Browser checks | 10 smoke scenarios passed, plus a final Admin checkbox/touch-target check |
| Swagger | Live `/v3/api-docs` includes catalog CRUD and customer endpoints; Swagger UI HTTP 200 |

## Browser evidence and reproducibility

[Browser script](../../test/browser/sirapat-step2-smoke.cjs) uses a fresh isolated H2 demo backend with `app.ordering.session-provider=database` and `app.menu.admin-access-provider=session`. Manager uses a real login/session; Customer uses a real single-use QR exchange and HttpOnly cookie. The local demo Staff provider remains a fixture for creating the session. No shared Supabase database was used.

Captured and visually inspected all seven images in [evidence](../../test/evidence/sirapat-step2-2026-10-04/): Customer 360px menu, confirm, RECEIVED order, loading, error, empty; Manager 1280px catalog. [Browser results](../../test/evidence/sirapat-step2-2026-10-04/browser-results.json) records viewport geometry and results. Loading delays/error aborts and the empty-menu response are explicit visual fault fixtures; the successful QR/order/catalog flow uses real API/database persistence.

Backend from `code/backend`:

```powershell
# Point these only at fresh disposable local databases; create anon/authenticated test roles.
$env:MENU_TEST_PG_URL='jdbc:postgresql://127.0.0.1:55432/sirapat_step2_test'
$env:MENU_TEST_PG_PASSWORD='step2-test'
$env:DINING_TEST_PG_URL='jdbc:postgresql://127.0.0.1:55432/sirapat_dining_test'
$env:DINING_TEST_PG_PASSWORD='step2-test'
mvn '-Dmaven.repo.local=.m2-cache' test
```

For a repeated PostgreSQL Menu upgrade run, use a new empty disposable database; this test deliberately checks the upgrade starting at V2. With no PostgreSQL environment variables, the optional PostgreSQL cases are skipped. V9 Payment is absent from this branch; test history jumps from V8 to V10 and does not prove deployment ordering on shared Supabase.

Browser backend from `code/backend`:

```powershell
mvn '-Dmaven.repo.local=.m2-cache' spring-boot:run '-Dspring-boot.run.profiles=demo' '-Dspring-boot.run.arguments=--spring.config.import= --app.ordering.session-provider=database --app.menu.admin-access-provider=session'
```

Frontend from `code/frontend`: `npm ci`, `npm test`, `npm run lint`, `npm run build`, `npm run dev`. With Playwright and Chrome installed, run `node test/browser/sirapat-step2-smoke.cjs` from repository root. `PLAYWRIGHT_MODULE` can point to an existing Playwright installation; `STEP2_EVIDENCE_DIR` changes the output folder. Restart the demo backend before repeating the browser seed.

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
