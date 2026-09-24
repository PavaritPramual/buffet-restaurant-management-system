# Menu Catalog and Customer Ordering

Owner: ศิระพัทธ์. This module consumes `SessionContext` from the Table/Dining Session owner and publishes the order shape in `shared-contracts.md` for Kitchen and Billing. It does not change the shared enums.

## API

| Method | Path | Result |
|---|---|---|
| GET | `/api/v1/menu-categories` | Categories |
| POST / PUT / DELETE | `/api/v1/menu-categories[/{id}]` | Category CRUD; deleting a category with items returns 400 |
| GET | `/api/v1/menu-items?page=0&size=20&sort=name,asc` | `{content,page,size,totalElements,totalPages}`; max size 100 |
| GET / POST / PUT / DELETE | `/api/v1/menu-items[/{id}]` | Menu item CRUD |
| GET | `/api/v1/dining-sessions/{id}/menu` | Available items in the active session's package |
| POST | `/api/v1/dining-sessions/{id}/orders` | 201 with Order, initial status `RECEIVED` |
| GET | `/api/v1/dining-sessions/{id}/orders` | Orders for a session |
| GET | `/api/v1/dining-sessions/{sessionId}/orders/{orderId}` | Order summary after session validation; cross-session access returns 404 |

Order request: `{"items":[{"menuItemId":1,"quantity":2}]}`. Quantity must be positive; duplicate item IDs, unavailable items, items outside the package, and inactive sessions are rejected. The saved order snapshots the item's name and table number. Errors use the shared `ErrorResponse` fields. The Order response matches `OrderFulfillmentContext`: `orderId`, `sessionId`, `tableNumber`, `items`, `status`, and `createdAt`.

Menu item input includes `categoryId`, `name`, optional `description`, `available`, `packageIds`, and optional `imageUrl`. The URL may use HTTP(S) or an absolute site path. The frontend displays an image only when `imageUrl` is set. Image files themselves are hosted elsewhere; this module stores only the URL. The admin page is `/admin/menu` and supports category/item CRUD and ten-item pagination; Auth must protect it during integration.

## Integration seams

Menu categories, menu items, package access, orders and order items use JPA persistence and Flyway `V3__create_menu_and_order_tables.sql`. `SessionContextProvider` isolates the remaining Session integration. Customer ordering returns `503 Service Unavailable` by default until the real Dining Session adapter is configured. The fixture adapter exposes active session 1 and completed session 2 only under the `local` or `test` profile.

Run locally with `--spring.profiles.active=local`. Never enable the fixture profile in a deployed environment.

PostgreSQL migration `V4__restrict_menu_and_order_access.sql` enables RLS and removes direct table and sequence privileges from Supabase `anon` and `authenticated` roles. Application access continues through the backend database role; Methus must still review the migration before shared deployment.

Schema reconciliation decisions, extensions and blocked external foreign keys are recorded in `doc/database/menu-ordering-schema-delta.md`.

The customer page is `/customer/sessions/{id}` and calls the shared Axios client through `VITE_API_BASE_URL`. QR token validation, customer access control, staff authorization for menu CRUD, fulfillment status updates, billing, and real Session lookup belong to the respective owners at integration. These endpoints must be secured before deployment. API/JSON changes require ศรัณย์'s review, shared entity changes require ปวริศช์'s review, and migrations require เมธัส's review.
