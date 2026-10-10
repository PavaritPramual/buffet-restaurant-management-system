# Billing browser demo on an isolated H2 database

This is a module/pairwise demo, not proof of Docker/Supabase or the full Customer/Kitchen flow. No central credentials are needed. All data is transient and disappears when backend stops.

## Start backend (terminal 1)

Explicit datasource/Flyway arguments override configuration; the `.env` import is disabled. Demo provides admin/admin123 solely as a local sample account.

```bash
cd /home/koji/CS3-1/Prinsible_software/buffet-restaurant-management-system/code/backend
./mvnw spring-boot:run '-Dspring-boot.run.arguments=--spring.profiles.active=demo --spring.config.import= --spring.datasource.url=jdbc:h2:mem:billing_demo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1 --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/h2 --server.port=18080'
```

## Start frontend (terminal 2)

```bash
cd /home/koji/CS3-1/Prinsible_software/buffet-restaurant-management-system/code/frontend
VITE_API_BASE_URL=http://localhost:18080/api/v1 npm run dev -- --port 5173 --strictPort
```

If port 5173 is already occupied, stop your existing frontend before starting this one; do not stop unrelated processes. CORS currently allows localhost:5173 by default.

## Prepare sample data through API/UI

1. Open http://localhost:5173/admin and log in as the local demo manager admin/admin123.
2. Open `/admin/users` and create a SERVICE_STAFF account using your own local demo username/password, then log out.
3. Open http://localhost:18080/swagger-ui.html. Use Try it out for these POST requests; record the IDs from responses (do not assume ID 1):
   - `/api/v1/tables`: `{"tableNumber":"BILL-DEMO","capacity":4}`
   - `/api/v1/buffet-packages`: `{"name":"Billing demo","price":399.00,"description":"Local H2 only"}`
   - `/api/v1/soups`: `{"name":"Demo soup"}`
4. Log in on the frontend as the SERVICE_STAFF account, open `/staff/tables` and open the demo table with that package/soup, adults 2 and children 1. Expected bill is 997.50 baht with no discount.

## Acceptance checks (record actual outcomes)

### User-reported browser results — 4 October 2026

Evidence source: the developer running the local H2 demo reported these results in the chat; the agent did not independently observe the browser/network panel. No screenshots or HAR files have been attached to this report.

- SERVICE_STAFF account created through the user UI.
- Billing preview displayed total 997.50 and the matching session ID.
- Payment/close flow returned the table to AVAILABLE.
- The developer reported QR/CARD and unpaid-close checks behaved correctly after following the instructions; individual response bodies were not supplied.
- POST payment for a completed session returned 400 with `Only ACTIVE status can be paid` (response body supplied).
- Duplicate POST while the session remained active returned 409 (status supplied).
- GET payment status: logged out returned 401, demo admin/MANAGER returned 403, SERVICE_STAFF returned 200 as expected (developer confirmed the three-step check).

Follow-up agent verification on 5 October supplies PostgreSQL browser refresh/same-payment-ID and close screenshots, plus native HTTP checks for KITCHEN_STAFF/SUPERVISOR. See pr19-review-verification.md; browser network fault injection remains unverified. This local H2 report does not prove PostgreSQL/Supabase or Docker runtime.

- [ ] Session detail → bill link selects the correct session ID.
- [ ] Preview shows subtotal 997.50, discount 0, total 997.50 without writing a payment.
- [ ] Choose CASH, confirm receipt, and observe POST payments HTTP 201 with matching sessionId and PAID.
- [ ] Refresh the billing page, click preview, and see the existing payment confirmation from GET payments/sessions/{id}, without another POST.
- [ ] Return to session detail; session is still ACTIVE until explicitly closed.
- [ ] Close session, observe COMPLETED and table AVAILABLE.
- [ ] Repeat with new sessions for QR and CARD; these record staff-confirmed results and do not call a gateway.
- [ ] Duplicate POST for the same session returns 409 and does not replace the original record.
- [ ] Close an unpaid active session: rejected; session/table stay ACTIVE/OCCUPIED.
- [ ] No login returns 401; KITCHEN_STAFF, SUPERVISOR and MANAGER are rejected with 403 by Billing/Payment and DiningSession staff operations.
- [ ] Network/status read failures display an error and do not permit an automatic payment retry; the status-check button only issues GET.
- [ ] Capture screenshots or a short clip of bill breakdown, confirmation, error, and returned table. Do not capture cookies, passwords or central secrets.

No boxes above are checked merely because automated tests passed. Record browser evidence and any defects in Tasks/PR.

## Central runtime gate

Before Docker/Supabase testing, coordinate with Methus/Pavarit and inspect flyway_schema_history. V9 must precede V10/V11 or the team must agree a forward migration plan. Main runtime must configure BILLING_CONTEXT_PROVIDER=database, PAYMENT_STATUS_PROVIDER=database, DINING_SESSION_STAFF_ACCESS_PROVIDER=session. Do not use fixture authorization on the integrated production path.
