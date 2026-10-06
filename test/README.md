# Testing guide

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

## Step 3 browser regression

`node test/browser/step3-core-flow.cjs` uses actual cookies and independent Manager/Supervisor/Staff/Kitchen/two-phone contexts. Set `PLAYWRIGHT_MODULE` to an available Playwright installation if it is not on the module path. Chrome must be available. The runner creates uniquely named table/package/soup/menu test data through UI/domain APIs and completes its session; it retains that run's historical data for evidence. Use an isolated local database or an owner-approved public test tenant.

- `FINAL_ENVIRONMENT`: `local-h2`, `local-postgres` or `public`; this is an operator declaration, not database/provider attestation.
- `FINAL_WEB_URL` and `FINAL_API_URL` (including `/api/v1`): loopback for local; HTTPS for public. Avoid trailing slashes. Public deployment is designed for a single origin.
- `FINAL_ALLOW_TEST_DATA=true`: confirms the approved test-data scope. No default demo password is accepted by the runner.
- `FINAL_MANAGER_USERNAME/PASSWORD`, `FINAL_SUPERVISOR_USERNAME/PASSWORD`, `FINAL_STAFF_USERNAME/PASSWORD`, `FINAL_KITCHEN_USERNAME/PASSWORD`: four existing test accounts. Read them privately from the runtime owner; never commit actual values.
- `FINAL_DEPLOYED_COMMIT`: required for public; record the runtime owner's confirmed revision, then verify it against the release record. Browser output labels this as owner-supplied.
- `FINAL_EVIDENCE_DIR`: output directory, default `test/reports/step3-core-flow`. Results include source revision/dirty-tree marker, URLs, timestamps, browser, counts and pending release gates. Attach the tested diff/source hashes when dirty.

`node test/browser/step3-ui-states.cjs` uses the same loopback URLs and Manager/Staff/Kitchen credentials. `FINAL_STATE_EVIDENCE_DIR` sets its output directory. It injects HTTP responses for Customer/Staff/Kitchen/Manager loading/empty/error at 360/768/1280px; Customer context is a fixture, employee shells use real login. This runner is deliberately restricted to loopback and is never public Core Flow evidence.

The Core Flow runner covers the integrated baseline. Stock target/active and Profile checks remain pending until their owner supplies reviewed code/contracts; the runner does not claim full Step 3 acceptance. See [Final plan](../doc/testing/test-plan.md) and [current report](../doc/testing/sirapat-step3-report.md).

`node test/browser/step3-concurrency.cjs` runs only on declared `local-h2`/`local-postgres` loopback environments with `FINAL_ALLOW_TEST_DATA=true` and explicit Manager/Staff test accounts. Set `FINAL_CONCURRENCY_EVIDENCE_DIR` for its output. It delays real order/history/QR requests and injects an order 503 to check acknowledgement races, error retention and changing from table A to B across unmount/Back. These are controlled fixtures, labelled `httpMocks:true`, and must never be counted as unmodified real Core Flow or public acceptance. It leaves its own test sessions/history in the isolated runtime, which can be discarded after the run.
