# Swagger UI API Coverage — 10 October 2026

**Environment:** [Render deployment](https://buffet-restaurant-management.onrender.com/swagger-ui/index.html)
**Repository baseline:** `c778150c79657b80930ceca6a4f10c136c1fc285`
**Coverage:** 78/78 OpenAPI operations invoked at least once: 33 GET and 45 state-changing operations.

This report records HTTP status codes observed during the Swagger UI pass. A route can have more than one status because it was checked under different roles or before and after the UAT session changed state. For CRUD create/update/delete/restore operations, empty payloads or nonexistent IDs were used to check validation and resource handling without changing catalog or staff data. Those calls do not prove successful CRUD behavior. The valid CASH payment and session close were intentional UAT data changes; no payment gateway was called.

| Method | API path | Observed status | Context |
|---|---|---:|---|
| GET | `/api/v1/tables/{id}` | 200, 404 | Existing C01 returned 200; nonexistent ID returned 404 |
| PUT | `/api/v1/tables/{id}` | 400 | Empty update payload |
| DELETE | `/api/v1/tables/{id}` | 404 | Nonexistent ID |
| GET | `/api/v1/tables` | 200 | Manager read |
| POST | `/api/v1/tables` | 400 | Empty create payload |
| POST | `/api/v1/tables/{id}/restore` | 404 | Nonexistent ID |
| PATCH | `/api/v1/tables/{id}/status` | 400 | Invalid/empty request |
| GET | `/api/v1/tables/archived` | 200 | Manager read |
| POST | `/api/v1/dining-sessions` | 400 | Empty open-session payload |
| POST | `/api/v1/dining-sessions/{id}/close` | 400, 200 | Before payment rejected; after PAID closed successfully |
| GET | `/api/v1/dining-sessions/{id}` | 403, 200 | Manager denied; SERVICE_STAFF allowed |
| GET | `/api/v1/dining-sessions/active` | 403, 200 | Manager denied; SERVICE_STAFF allowed |
| PATCH | `/api/v1/orders/{id}/status` | 403 | Role/state rejection in Swagger; Kitchen/Service flow was also exercised through the UAT UI |
| GET | `/api/v1/orders/ready` | 403, 200 | Manager denied; SERVICE_STAFF allowed |
| GET | `/api/v1/orders/incoming` | 403 | SERVICE_STAFF denied as expected; Kitchen flow was exercised through the UAT UI |
| PUT | `/api/v1/stock/items/{id}` | 400 | Empty update payload |
| DELETE | `/api/v1/stock/items/{id}` | 404 | Nonexistent ID |
| PUT | `/api/v1/stock/items/{id}/active` | 400 | Empty request |
| POST | `/api/v1/stock/{itemId}/in` | 400 | Empty stock-in payload |
| POST | `/api/v1/stock/{itemId}/adjustments` | 400 | Empty adjustment payload |
| POST | `/api/v1/stock/items` | 400 | Empty create payload |
| POST | `/api/v1/stock/items/{id}/restore` | 404 | Nonexistent ID |
| GET | `/api/v1/stock` | 200 | Authorized read |
| GET | `/api/v1/stock/transactions` | 200 | Authorized read |
| GET | `/api/v1/stock/items/archived` | 200 | Manager read |
| GET | `/api/v1/soups/{id}` | 200 | Existing record |
| PUT | `/api/v1/soups/{id}` | 400 | Empty update payload |
| DELETE | `/api/v1/soups/{id}` | 404 | Nonexistent ID |
| GET | `/api/v1/soups` | 200 | Authorized read |
| POST | `/api/v1/soups` | 400 | Empty create payload |
| POST | `/api/v1/soups/{id}/restore` | 404 | Nonexistent ID |
| PATCH | `/api/v1/soups/{id}/active` | 400 | Empty request |
| GET | `/api/v1/soups/archived` | 403, 200 | SERVICE_STAFF denied; Manager allowed |
| GET | `/api/v1/menu-items/{id}` | 200 | Existing record |
| PUT | `/api/v1/menu-items/{id}` | 400 | Empty update payload |
| DELETE | `/api/v1/menu-items/{id}` | 404 | Nonexistent ID |
| GET | `/api/v1/menu-items` | 200 | Authorized read |
| POST | `/api/v1/menu-items` | 400 | Empty create payload |
| POST | `/api/v1/menu-items/{id}/restore` | 404 | Nonexistent ID |
| GET | `/api/v1/menu-items/archived` | 403, 200 | SERVICE_STAFF denied; Manager allowed |
| GET | `/api/v1/menu-categories/{id}` | 200 | Existing record |
| PUT | `/api/v1/menu-categories/{id}` | 400 | Empty update payload |
| DELETE | `/api/v1/menu-categories/{id}` | 404 | Nonexistent ID |
| GET | `/api/v1/menu-categories` | 200 | Authorized read |
| POST | `/api/v1/menu-categories` | 400 | Empty create payload |
| POST | `/api/v1/menu-categories/{id}/restore` | 404 | Nonexistent ID |
| GET | `/api/v1/menu-categories/archived` | 403, 200 | SERVICE_STAFF denied; Manager allowed |
| GET | `/api/v1/buffet-packages/{id}` | 200 | Existing record |
| PUT | `/api/v1/buffet-packages/{id}` | 400 | Empty update payload |
| DELETE | `/api/v1/buffet-packages/{id}` | 404 | Nonexistent ID |
| GET | `/api/v1/buffet-packages` | 200 | Authorized read |
| POST | `/api/v1/buffet-packages` | 400 | Empty create payload |
| POST | `/api/v1/buffet-packages/{id}/restore` | 404 | Nonexistent ID |
| PATCH | `/api/v1/buffet-packages/{id}/active` | 400 | Empty request |
| GET | `/api/v1/buffet-packages/archived` | 403, 200 | SERVICE_STAFF denied; Manager allowed |
| PUT | `/api/v1/admin/users/{id}/profile` | 400 | Empty profile payload |
| PUT | `/api/v1/admin/users/{id}/active` | 400 | Empty request |
| GET | `/api/v1/admin/users` | 403, 200 | SERVICE_STAFF denied; Manager allowed |
| POST | `/api/v1/admin/users` | 400 | Empty create payload |
| POST | `/api/v1/admin/users/{id}/restore` | 404 | Nonexistent ID |
| GET | `/api/v1/admin/users/archived` | 403, 200 | SERVICE_STAFF denied; Manager allowed |
| DELETE | `/api/v1/admin/users/{id}` | 404 | Nonexistent ID |
| POST | `/api/v1/payments` | 201 | UAT CASH payment recorded for session 13 |
| GET | `/api/v1/payments/sessions/{sessionId}` | 404, 200 | No record before UAT payment; record read after payment |
| GET | `/api/v1/dining-sessions/{sessionId}/orders` | 200, 401 | Active customer allowed; old credential denied after close |
| POST | `/api/v1/dining-sessions/{sessionId}/orders` | 409 | New order rejected after bill request |
| GET | `/api/v1/dining-sessions/{sessionId}/orders/{orderId}` | 200 | Active customer read |
| GET | `/api/v1/dining-sessions/{sessionId}/menu` | 200 | Active customer read |
| POST | `/api/v1/dining-sessions/{sessionId}/bill-request` | 200 | Repeated request returned current request state |
| GET | `/api/v1/dining-sessions/{sessionId}/bill-status` | 200, 401 | Active customer read; old credential denied after close |
| POST | `/api/v1/dining-sessions/qr-exchange` | 404 | Invalid QR token rejected |
| GET | `/api/v1/dining-sessions/customer-context` | 200, 401 | Active customer allowed; denied after close |
| POST | `/api/v1/billing/preview` | 200 | Backend-calculated bill total ฿747.50 |
| POST | `/api/v1/auth/logout` | 204 | Manager and UAT staff sessions logged out |
| POST | `/api/v1/auth/login` | 200 | Manager and SERVICE_STAFF login |
| GET | `/api/v1/auth/me` | 200, 401 | Authenticated identity; rejected after logout |
| GET | `/api/v1/system/health` | 200 | Service reports UP |
| GET | `/api/v1/dining-sessions/{sessionId}/package` | 200 | Active customer read |

## Final UAT state

- Session 13: `COMPLETED`.
- Payment: `PAID`, method `CASH`, amount ฿747.50. This is an application record for UAT, not a real cash collection or gateway payment.
- Table C01: `AVAILABLE`.
- Customer context and order reads using the old customer credential: HTTP 401 after close.
