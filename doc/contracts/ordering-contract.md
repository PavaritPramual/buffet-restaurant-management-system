# Menu Catalog and Customer Ordering

Owner: ศิระพัทธ์. This module consumes `SessionContext` from the Table/Dining Session owner and publishes the order shape in `shared-contracts.md` for Kitchen and Billing. It does not change the shared enums.

## API

| Method | Path | Result |
|---|---|---|
| GET | `/api/v1/menu-categories` | Categories |
| POST / PUT / DELETE | `/api/v1/menu-categories[/{id}]` | Category CRUD; deleting a category with items returns 400 |
| GET | `/api/v1/menu-items?page=0&size=20&sort=name,asc` | `{content,page,size,totalElements,totalPages}`; max size 100 |
| GET / POST / PUT / DELETE | `/api/v1/menu-items[/{id}]` | Menu item CRUD |
| GET | `/api/v1/dining-sessions/{id}/menu` | Available items in the active session's package; requires matching `X-Session-Token` |
| POST | `/api/v1/dining-sessions/{id}/orders` | 201 with Order, initial status `RECEIVED`; requires matching `X-Session-Token` |
| GET | `/api/v1/dining-sessions/{id}/orders` | Orders for an active session; requires matching `X-Session-Token` |
| GET | `/api/v1/dining-sessions/{sessionId}/orders/{orderId}` | Order summary; requires matching `X-Session-Token`; cross-session access returns 404 |

Customer QR route: `/customer/qr/{token}`. The page resolves the token through `GET /api/v1/dining-sessions/token/{token}`, then sends it in `X-Session-Token` on every menu and order request. The bearer token must match the path session ID and an `ACTIVE` row. Missing, mismatched, unknown, or closed-session tokens return 404. A numeric session ID alone does not authorize customer access.

Order request: `{"items":[{"menuItemId":1,"quantity":2}]}`. Quantity must be positive; duplicate item IDs, unavailable items, items outside the package, and inactive sessions are rejected. The saved order snapshots the item's name and table number. Errors use the shared `ErrorResponse` fields. The Order response matches `OrderFulfillmentContext`: `orderId`, `sessionId`, `tableNumber`, `items`, `status`, and `createdAt`.

Menu item input includes `categoryId`, `name`, optional `description`, `available`, `packageIds`, and optional `imageUrl`. The URL may use HTTP(S) or an absolute site path. The frontend displays an image only when `imageUrl` is set. Image files themselves are hosted elsewhere; this module stores only the URL. The admin page is `/admin/menu` and supports category/item CRUD and ten-item pagination; Auth must protect it during integration.

A menu item without order history may be deleted. A menu item referenced by `order_items` keeps its history and deletion returns `400 Bad Request` with `Cannot delete a menu item with order history; mark it unavailable instead`; staff should set `available=false` to close sales for that item.

## Integration seams

Menu categories, menu items, package access, orders and order items use JPA persistence and Flyway V4 after Package/Soup V3. The runtime `SessionContextProvider` verifies the active session ID and token from the database. The fixed-token fixture is limited to tests. Staff session operations fail closed until a staff authorization provider is configured; local/test may use a fixture provider.

Run locally with `--spring.profiles.active=local`. Never enable a fixture provider in a deployed environment.

PostgreSQL migration `V5__restrict_menu_and_order_access.sql` enables RLS, defines policies for the backend datasource role, and removes direct table and sequence privileges from Supabase `anon` and `authenticated` roles. Methus must still review the migration before shared deployment.

Schema reconciliation decisions, extensions and blocked external foreign keys are recorded in `doc/database/menu-ordering-schema-delta.md`.

The customer page uses `/customer/qr/{token}` and calls the shared Axios client through `VITE_API_BASE_URL`. Menu mutations and staff session operations are fail-closed by default; Authentication must provide the deployed role-checking implementations. API/JSON changes require ศรัณย์'s review, shared entity changes require ปวริศช์'s review, and migrations require เมธัส's review.
