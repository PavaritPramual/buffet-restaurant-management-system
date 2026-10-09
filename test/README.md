# Testing guide

Current owner result: [9 October public regression](../doc/testing/sirapat-public-regression-2026-10-09.md), with local/CI/API/browser/release boundaries.

Each feature owner writes and maintains tests for their own backend, frontend, and business rules. The shared fixtures in `test/fixtures/` are contract examples, not a shared live database.

- Backend: from `code/backend`, run `./mvnw test` (`.\mvnw.cmd test` on Windows).
- Frontend: from `code/frontend`, run `npm ci`, `npm run test`, `npm run lint`, and `npm run build`.
- Automated tests must use isolated in-memory stores, mocks, or a dedicated disposable test database. Never point tests at the team's shared Supabase project.
- When an integration test needs PostgreSQL, give it its own schema/database and clean only that isolated data.

The environment-driven Menu/Dining PostgreSQL tests are disabled unless both their URL and `ALLOW_DESTRUCTIVE_DB_TESTS=true` are set. Before Flyway or Spring datasource startup they require an explicit loopback port, a database name beginning with `buffet_test_` (at most 63 characters), no JDBC URL options, and the database comment `buffet-disposable-test-only`. The concurrency cleanup checks the connected database again before its DELETE statements. Remote databases and unmarked local databases fail closed. This guard does not make a shared database disposable: create a fresh local PostgreSQL instance and never mark a team database for testing.

Create `anon` and `authenticated` NOLOGIN roles in that isolated instance, then create separate empty Menu and Dining databases, for example:

```sql
CREATE DATABASE buffet_test_menu_local;
COMMENT ON DATABASE buffet_test_menu_local IS 'buffet-disposable-test-only';
CREATE DATABASE buffet_test_dining_local;
COMMENT ON DATABASE buffet_test_dining_local IS 'buffet-disposable-test-only';
```

Set `MENU_TEST_PG_URL` / `DINING_TEST_PG_URL` to `jdbc:postgresql://127.0.0.1:<local-port>/<database-name>` and their `_PG_USER` / `_PG_PASSWORD` credentials. Use a new empty Menu database for each upgrade test run.

[GitHub CI](../.github/workflows/ci.yml) runs frontend tests/lint/build and backend verification on pull requests into `develop` and pushes to `develop`. Its PostgreSQL databases and roles are created in a fresh job service; Stock security tests use their own Testcontainers database. Surefire reports are retained as workflow artifacts, outside the source diff. Browser smoke/concurrency scripts and screenshot inspection remain local evidence; workflow success is a separate result and must be checked on the current PR revision.

See [test conventions](../doc/testing/test-conventions.md) and the [acceptance criteria template](../doc/testing/acceptance-criteria-template.md).

## Public HTTPS API regression

`node test/api/step3-public-regression.cjs` uses native HTTP requests, not browser automation. It creates uniquely labelled QA accounts/master data and performs order/payment/stock/profile operations through the application's API. Use only an approved test-data scope; it does not delete records or run shared database migrations.

Required environment: `FINAL_WEB_URL`, `FINAL_API_URL` (HTTPS, same origin, API ending `/api/v1`), `FINAL_ALLOW_TEST_DATA=true`, `FINAL_MANAGER_USERNAME`, `FINAL_MANAGER_PASSWORD`. Use a new `FINAL_RUN_LABEL` per approved run; set `FINAL_EVIDENCE_DIR` for sanitized output and `FINAL_LOCAL_ACCESS_FILE` under ignored `test/reports/` for generated role credentials. Never commit the access file or log environment credentials. `FINAL_DEPLOYED_COMMIT` is an owner-supplied label, not live attestation.

API cookie jars are separate from browser contexts. Save real manual browser evidence separately. This runner does not test elapsed TTL, mobile devices or all UI states; see the [current execution report](../doc/testing/sirapat-public-regression-2026-10-09.md).

## Step 3 browser regression

Current results: [9 October public regression](../doc/testing/sirapat-public-regression-2026-10-09.md), including integrated Stock/Profile, real HTTPS four-role Core Flow and the supplementary30-menu customer QR order. The [8 October follow-up](../doc/testing/sirapat-step3-followup-2026-10-08.md) preserves historical integrated/candidate results and reproduction instructions; its unmerged-candidate status is not current. `step3-local-runtime.cjs` starts disposable H2/Vite with real session/database providers, no `.env` import/demo seed, random private accounts and bootstrap readiness. Set `FINAL_JAVA`/`PLAYWRIGHT_MODULE` if needed. `FINAL_RUN_LABEL`, `FINAL_LOCAL_OUTPUT` and `FINAL_LOCAL_SCRIPTS` choose output and runners; see the [historical evidence reproduction guide](evidence/sirapat-step3-followup-2026-10-08/README.md).

`step3-stock-profile.cjs` checks real Manager/Supervisor UI, persisted target/shortfall/active history/reactivation, Profile create/update/length/permissions and responsive360/768/1280 on isolated loopback runtime. `step3-stock-profile-states.cjs` checks controlled loading/empty/error and legacy presentation; it is fixture evidence. Feature code can be tested independently before merge, but final integrated/public reruns remain necessary. `evidence-source-hashes.cjs` writes/verifies SHA256 from exact Git blobs without checkout line-ending transformations.

`node test/browser/step3-core-flow.cjs` uses actual cookies and independent Manager/Supervisor/Staff/Kitchen/two-phone contexts. Set `PLAYWRIGHT_MODULE` to an available Playwright installation if it is not on the module path. Chrome must be available. The runner creates uniquely named table/package/soup/menu test data through UI/domain APIs and completes its session; it retains that run's historical data for evidence. Use an isolated local database or an owner-approved public test tenant.

- `FINAL_ENVIRONMENT`: `local-h2`, `local-postgres` or `public`; this is an operator declaration, not database/provider attestation.
- `FINAL_WEB_URL` and `FINAL_API_URL` (including `/api/v1`): loopback for local; **same-origin HTTPS required for public**, including effective port. URL guards run before credentials, browser or test-data requests. Avoid trailing slashes. `node --test test/browser/step3-runtime-config.test.cjs` verifies 6 guards without a browser/network and runs in CI separately from Vitest counts.
- `FINAL_ALLOW_TEST_DATA=true`: confirms the approved test-data scope. No default demo password is accepted by the runner.
- `FINAL_MANAGER_USERNAME/PASSWORD`, `FINAL_SUPERVISOR_USERNAME/PASSWORD`, `FINAL_STAFF_USERNAME/PASSWORD`, `FINAL_KITCHEN_USERNAME/PASSWORD`: four existing test accounts. Read them privately from the runtime owner; never commit actual values.
- `FINAL_DEPLOYED_COMMIT`: required for public; record the runtime owner's confirmed revision, then verify it against the release record. Browser output labels this as owner-supplied.
- `FINAL_EVIDENCE_DIR`: output directory, default `test/reports/step3-core-flow`. Results include source revision/dirty-tree marker, URLs, timestamps, browser, counts and pending release gates. Attach the tested diff/source hashes when dirty.

`node test/browser/step3-ui-states.cjs` uses the same loopback URLs and Manager/Staff/Kitchen credentials. `FINAL_STATE_EVIDENCE_DIR` sets its output directory. It injects HTTP responses for Customer/Staff/Kitchen/Manager loading/empty/error at 360/768/1280px; Customer context is a fixture, employee shells use real login. This runner is deliberately restricted to loopback and is never public Core Flow evidence.

The Core Flow runner covers the integrated baseline, including Manager Category/Menu create, update, reload/read and confirmed deletion through UI. Only its own unused test item/category are deleted; HTTP reads verify persistence. It sends 12 negative State requests through real cookie sessions (401/403/400/404), checks ErrorResponse fields and reads persisted history after every denial; happy transitions use Kitchen/Staff UI. All four roles receive actual protected API401 while their shell is mounted after server-side session invalidation (API logout, no UI logout/mocks); Staff/Kitchen UI logout is checked separately after a new login. This proves 401 recovery, not elapsed TTL expiry or public Secure-cookie behavior. Stock target/active and Profile are covered separately by the dedicated runners and the current 9 October public report; the older Core Flow runner alone does not certify those features. See [Final plan](../doc/testing/test-plan.md), the historical [API/State regression report](../doc/testing/sirapat-step3-api-state-regression-report.md) and the historical [pre-merge report](../doc/testing/sirapat-step3-premerge-report.md).

`node test/browser/step3-concurrency.cjs` runs only on declared `local-h2`/`local-postgres` loopback environments with `FINAL_ALLOW_TEST_DATA=true` and explicit Manager/Staff test accounts. Set `FINAL_CONCURRENCY_EVIDENCE_DIR` for its output. It delays real order/history/QR requests and injects an order 503 to check acknowledgement races, error retention and changing from table A to B across unmount/Back. These are controlled fixtures, labelled `httpMocks:true`, and must never be counted as unmodified real Core Flow or public acceptance. It leaves its own test sessions/history in the isolated runtime, which can be discarded after the run.
