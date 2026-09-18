# Testing guide

Each feature owner writes and maintains tests for their own backend, frontend, and business rules. The shared fixtures in `test/fixtures/` are contract examples, not a shared live database.

- Backend: from `code/backend`, run `./mvnw test` (`.\mvnw.cmd test` on Windows).
- Frontend: from `code/frontend`, run `npm ci`, `npm run lint`, and `npm run build`.
- Automated tests must use isolated in-memory stores, mocks, or a dedicated disposable test database. Never point tests at the team's shared Supabase project.
- When an integration test needs PostgreSQL, give it its own schema/database and clean only that isolated data.

See [test conventions](../doc/testing/test-conventions.md) and the [acceptance criteria template](../doc/testing/acceptance-criteria-template.md).
