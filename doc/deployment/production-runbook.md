# Render production deploy, redeploy, and rollback

Owner: Teeramet · source branch `teeramet_673380273-9_02` · reviewed source revision `bdd3bd7` (8 October 2026). Public URL: <https://buffet-restaurant-management-system.onrender.com/>. This document is a procedure and an evidence ledger; it does not certify an untested release.

## What runs where

[`Dockerfile.production`](../../Dockerfile.production) builds the Vite frontend with `VITE_API_BASE_URL=/api/v1`, copies `dist` into Spring Boot's static resources, then packages one Java image. Render terminates public HTTPS and sends traffic to the app's `PORT`; Spring uses forwarded headers. [`docker-compose.yml`](../../docker-compose.yml) remains the separate local frontend/backend setup. The [deployment diagram](../diagrams/deployment-production-runtime.md) distinguishes observed HTTP behavior from configured database wiring.

The production profile sets Staff `JSESSIONID` to `HttpOnly; Secure; SameSite=Lax`, makes the customer cookie `Secure`, and selects session-based Staff access plus database-backed Ordering, Billing context, and Payment status. The customer cookie is created by application code as `HttpOnly`, `SameSite=Lax` by default, and is scoped to `/api/v1/dining-sessions`. Public cookie attributes still require a real login and QR exchange check after the new commit is deployed.

## Release preparation

1. Record the exact Git commit selected for deployment and the matching PR/CI run. Use the repository-root `Dockerfile.production` as Render's Dockerfile; build context must be the repository root. Check that backend/PostgreSQL and frontend CI jobs pass.
2. Check the approved migration list against the target database's `flyway_schema_history`. Do not deploy code requiring a new migration until the database owner approves that forward migration and its production application. Flyway validates checksums on startup; JPA uses `ddl-auto=validate`. Never modify an already-applied migration or delete a history row to make startup pass.
3. Keep credentials only in Render Environment. Required values are `SPRING_PROFILES_ACTIVE=production`, `SUPABASE_DB_HOST` (Session Pooler hostname), `SUPABASE_DB_PORT=5432`, `SUPABASE_DB_NAME=postgres`, `SUPABASE_DB_USERNAME`, and `SUPABASE_DB_PASSWORD`. Render supplies `PORT`; the app reads it before `SERVER_PORT`. Set `CORS_ALLOWED_ORIGINS` to the exact public HTTPS origin (without a trailing slash). Keep `BOOTSTRAP_ADMIN_ENABLED=false` after provisioning. Do not place passwords, QR tokens, or cookie values in Git, tasks, screenshots, or the evidence report.
4. If the first Manager account does not exist, follow [`setup-demo-data.md`](../setup-demo-data.md): enable bootstrap only temporarily with a new private credential, deploy once, verify the Manager can log in, then disable bootstrap and redeploy immediately. The bootstrap runner creates a user only while `app_users` is empty. Create a `[TEST DATA]` SERVICE_STAFF account through the Users UI for public flow tests. Do not use the H2 demo `admin/admin123` account in production.

## Deploy and verify

1. In the existing Render Web Service, select the intended branch and commit, then use **Manual Deploy → Deploy latest commit**. Do not create a second service for this release. Record the commit shown on the successful deploy page and the deploy time. Wait until status is **Live**.
2. Inspect startup logs for the Supabase pooler host/port, a real Hikari connection, Flyway validated/current version, JPA EntityManager initialization, and the server listening on Render's `PORT`. Capture only sanitized lines; never capture environment values, credentials, cookie values, or full request bodies.
3. Check public HTTP without credentials:

   ```bash
   BASE=https://buffet-restaurant-management-system.onrender.com
   curl -sS -o /dev/null -w '%{http_code} %{content_type}\n' "$BASE/"
   curl -sS -o /dev/null -w '%{http_code} %{content_type}\n' "$BASE/swagger-ui/index.html"
   curl -sS -o /dev/null -w '%{http_code} %{content_type}\n' "$BASE/v3/api-docs"
   curl -sS -o /dev/null -w '%{http_code} %{content_type}\n' "$BASE/actuator/health/readiness"
   curl -sS -o /dev/null -w '%{http_code} %{content_type}\n' "$BASE/kitchen/"
   curl -sS -o /dev/null -w '%{http_code} %{content_type}\n' "$BASE/Customer/QR/"
   curl -sS -o /dev/null -w '%{http_code} %{content_type}\n' "$BASE/api/v1/nonexistent"
   ```

   The first six should return 200 and HTML except API docs/health, which return JSON. Unknown API must return 404 JSON. The two route variants need a **new deploy of PR #35**, because the earlier public observation returned 404 for both.
4. With the `[TEST DATA]` Staff account in a private browser window, log in at the public URL. In browser DevTools, inspect the **attributes only** of `JSESSIONID` (`HttpOnly`, `Secure`, `SameSite=Lax`); do not copy its value. Open a table and scan its QR in a separate customer browser context, then check `customer_session` attributes in the same way. Confirm an unauthorized Staff request gives 401 and a wrong-role request gives 403. If there is no test account yet, record **awaiting Auth/Manager test access**, not failed.
5. Use one clearly named `[TEST DATA]` table/package/session. Verify the bill uses the price snapshot from opening the session, the amount is calculated by the backend, closing before payment is rejected, CASH/QR/CARD recording returns `PAID`, duplicate payment is rejected, refresh shows the existing payment, and the table is released only after Staff explicitly closes the paid session. Never repeat the same POST after a lost response; GET `/api/v1/payments/sessions/{sessionId}` first. Record only test IDs and displayed amounts, never QR/cookie/password values.
6. Redeploy the same schema-compatible commit, then verify payment/session records remain in PostgreSQL. Staff login is an in-memory HTTP session and may require login again after restart. Customer grants are stored in PostgreSQL, but the customer cookie's actual post-restart behavior must be checked; do not infer persistence merely from the database record.
7. After an idle period on Render Free, measure time from the first public request to a usable health/API response. Record the observed seconds and date. Start the service before a demo, and keep local Compose plus a recorded demo as a backup; neither substitutes for the public acceptance test.

## Redeploy and rollback

For a routine redeploy, confirm the chosen SHA and database compatibility, run the same CI/startup/public checks, and append a new evidence row. To roll back a bad application release, choose a previously working **application** commit only if it can read and write the database schema now present. Applied Flyway migrations are forward-only: do not roll back SQL files or edit their checksums. If the old app cannot operate on the current schema, prepare and review a forward compatibility fix instead. Keep the last known good Render deploy ID, Git SHA, schema version, and rollback decision in the release record.

## Evidence ledger, 8 October 2026

| Evidence | Result | Limit |
| --- | --- | --- |
| [PR #35 source and CI](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/35), [CI run](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37807568449) | Source head `bdd3bd7`; backend/PostgreSQL and frontend jobs passed | CI does not prove which commit Render runs |
| Public HTTPS checks at about 23:36 ICT | `/`, Swagger, `/v3/api-docs`, readiness: 200; unknown API: 404 JSON; public-origin QR exchange with deliberately invalid token: 404 rather than origin rejection | No credentials or real QR used; no Billing/Payment flow tested |
| Public deep-link variants at the same check | `/kitchen/` and `/Customer/QR/`: 404 JSON | PR #35 route fix was not yet visible on public URL |
| Earlier owner-supplied Render startup excerpt, about 20:57 ICT | Pooler `:5432` over `sslmode=require`, 15 Flyway migrations validated, schema version 15, JPA initialized, Tomcat on 10000 | Historical deployment log; current deployed SHA and data persistence after redeploy remain unverified |

Release gate still open: deployed SHA, refreshed deep links, real cookie/login/QR/Billing/Payment checks, persistence after redeploy, and measured Render Free cold start. Update this ledger only from the release actually tested.
