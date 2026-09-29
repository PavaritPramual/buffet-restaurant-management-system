# Menu Catalog and Customer Ordering

Owner: ศิระพัทธ์. This module consumes `SessionContext` from the Table/Dining Session owner and publishes the order shape in `shared-contracts.md` for Kitchen and Billing. It does not change the shared enums.

## API

| Method | Path | Result |
|---|---|---|
| GET | `/api/v1/menu-categories` | Categories |
| POST / PUT / DELETE | `/api/v1/menu-categories[/{id}]` | Category CRUD; deleting a category with items returns 400 |
| GET | `/api/v1/menu-items?page=0&size=20&sort=name,asc` | `{content,page,size,totalElements,totalPages}`; max size 100 |
| GET / POST / PUT / DELETE | `/api/v1/menu-items[/{id}]` | Menu item CRUD |
| POST | `/api/v1/dining-sessions/qr-exchange` | Redeem one-time QR token from JSON body; returns customer context and `HttpOnly` cookie |
| GET | `/api/v1/dining-sessions/customer-context` | Active customer context from cookie; excludes QR token and price |
| GET | `/api/v1/dining-sessions/{id}/menu` | Available items in the active session's package; requires matching customer cookie |
| POST | `/api/v1/dining-sessions/{id}/orders` | 201 with Order, initial status `RECEIVED`; requires matching customer cookie and allowed Origin |
| GET | `/api/v1/dining-sessions/{id}/orders` | Orders for an active session; requires matching customer cookie |
| GET | `/api/v1/dining-sessions/{sessionId}/orders/{orderId}` | Order summary; requires matching customer cookie; cross-session access returns 404 |

## QR entry-point design decision

The initial sequence design and an earlier revision of PR #16 used `GET /api/v1/dining-sessions/token/{token}`. That placed a bearer QR token in the request path, where access logs, proxies, monitoring, and browser history could retain it. The route was removed from both the backend and frontend before PR #16 enters `develop`. The backend on `develop` never exposed this route, so there is no deployed endpoint to deprecate; the older sequence diagram on `develop` is replaced by the updated diagram in this PR. The same decision is recorded in [Notion System Design](https://app.notion.com/p/3cfcb2e9d47a811ba912e01c6bcb449e) and its [Sequence diagrams](https://app.notion.com/p/3d7cb2e9d47a803fbb26e1b7e5dbdf65).

The current public QR entry point is `POST /api/v1/dining-sessions/qr-exchange` with the one-time token in the JSON body. The customer URL carries it in a fragment, which is not sent in the HTTP request, and the page replaces that URL after reading it. The backend rotates the QR token and returns a separate `HttpOnly` cookie for later requests. `customer_session_grants` stores only the hash of that customer credential. No compatibility GET route accepts a QR token in a URL path.

Customer QR route: `/customer/qr#token={oneTimeToken}`. The browser removes the fragment immediately and sends the token only in the JSON body of `POST /api/v1/dining-sessions/qr-exchange`. On success the backend rotates the displayed QR and issues a random `customer_session` credential in an `HttpOnly` cookie, storing only its hash. Each phone may redeem the newly displayed QR for its own credential. Menu and order requests use the cookie, require an `ACTIVE` session, and must match the path session ID. Missing/invalid cookie returns 401, mismatched or closed session returns 404. A numeric session ID alone does not authorize customer access. State-changing customer requests require an allowed `Origin`; missing/disallowed Origin returns 403.

Order request: `{"items":[{"menuItemId":1,"quantity":2}]}`. Quantity must be positive; duplicate item IDs, unavailable items, items outside the package, and inactive sessions are rejected. The saved order snapshots the item's name and table number. Errors use the shared `ErrorResponse` fields. The Order response matches `OrderFulfillmentContext`: `orderId`, `sessionId`, `tableNumber`, `items`, `status`, and `createdAt`.

Menu item input includes `categoryId`, `name`, optional `description`, `available`, `packageIds`, and optional `imageUrl`. The URL may use HTTP(S) or an absolute site path. The frontend displays an image only when `imageUrl` is set. Image files themselves are hosted elsewhere; this module stores only the URL. The admin page is `/admin/menu` and supports category/item CRUD and ten-item pagination; Auth must protect it during integration.

A menu item without order history may be deleted. A menu item referenced by `order_items` keeps its history and deletion returns `400 Bad Request` with `Cannot delete a menu item with order history; mark it unavailable instead`; staff should set `available=false` to close sales for that item.

## Integration seams

Menu categories, menu items, package access, orders and order items use JPA persistence and Flyway V4 after Package/Soup V3. The runtime `SessionContextProvider` verifies the active session ID and token from the database. The fixed-token fixture is limited to tests. Staff session operations fail closed until a staff authorization provider is configured; local/test may use a fixture provider.

Run locally with `--spring.profiles.active=local`. Never enable a fixture provider in a deployed environment.

PostgreSQL migration `V5__restrict_menu_and_order_access.sql` enables RLS, defines policies for the backend datasource role, and removes direct table and sequence privileges from Supabase `anon` and `authenticated` roles. Methus must still review the migration before shared deployment.

Schema reconciliation decisions, extensions and blocked external foreign keys are recorded in `doc/database/menu-ordering-schema-delta.md`.

The customer page uses `/customer/qr#token={oneTimeToken}` and calls the credential-enabled Axios client through `VITE_API_BASE_URL`. Local same-site deployment uses `SameSite=Lax`; a public cross-site deployment requires an explicitly designed cookie/CORS setup or a same-site reverse proxy. Menu mutations and staff session operations are fail-closed by default; Authentication must provide the deployed role-checking implementations. API/JSON changes require ศรัณย์'s review, shared entity changes require ปวริศช์'s review, and migrations require เมธัส's review.
