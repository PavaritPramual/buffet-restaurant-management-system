# Step 2 integration evidence — 6 October 2026

Code under test: `15024d4` + `83b9bdf`, based on develop `888b4ea`.

- `core-flow/`: Chrome with real API/database/auth contexts on Docker Compose + disposable PostgreSQL 18. Screens: Customer 360px, Staff/Kitchen 768px, Admin 1280px. QR cards masked; all names/data are disposable samples.
- `ui-states/`: authenticated browser shells with controlled HTTP loading/empty/error responses, explicitly fixtures. Not evidence of a real database outage.
- `shared-jdbc-metadata.json`: read-only metadata using actual app JDBC role. V1–V12 applied; residual restaurant_tables grants require proposed V13. No credentials.
- `step2-shared-http.json`: Compose health/Swagger/CORS on shared Supabase **before adding V13**. Current PR migration has not been applied centrally.

See [full Thai report](../../../doc/testing/pavarit-step2-close-report.md) for commands, results and remaining review/deployment gates. Evidence does not mark Step 2 merged or deployed.
