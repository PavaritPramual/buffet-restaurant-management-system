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
