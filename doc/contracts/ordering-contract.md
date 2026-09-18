# Menu Catalog and Customer Ordering

Owner: ศิระพัทธ์. This module consumes `SessionContext` from the Table/Dining Session owner and publishes the order shape in `shared-contracts.md` for Kitchen and Billing. It does not change the shared enums.

## API

| Method | Path | Result |
|---|---|---|
| GET | `/api/v1/menu-categories` | Categories |
| POST / PUT / DELETE | `/api/v1/menu-categories[/{id}]` | Category CRUD; deleting a category with items returns 409 |
| GET | `/api/v1/menu-items?page=0&size=20&sort=name,asc` | `{content,page,size,totalElements}`; max size 100 |
| GET / POST / PUT / DELETE | `/api/v1/menu-items[/{id}]` | Menu item CRUD |
| GET | `/api/v1/dining-sessions/{id}/menu` | Available items in the active session's package |
| POST | `/api/v1/dining-sessions/{id}/orders` | 201 with Order, initial status `RECEIVED` |
| GET | `/api/v1/dining-sessions/{id}/orders` | Orders for a session |
| GET | `/api/v1/orders/{id}` | Order summary and current status |

Order request: `{"items":[{"menuItemId":1,"quantity":2}]}`. Quantity must be positive; duplicate item IDs, unavailable items, items outside the package, and inactive sessions are rejected. The saved order snapshots the item's name and table number. Errors use the shared `ErrorResponse` fields. The Order response matches `OrderFulfillmentContext`: `orderId`, `sessionId`, `tableNumber`, `items`, `status`, and `createdAt`.

Menu item input includes `categoryId`, `name`, `available`, `packageIds`, and optional `imageUrl`. The URL may use HTTP(S) or an absolute site path. The frontend displays an image only when `imageUrl` is set. Image files themselves are hosted elsewhere; this module stores only the URL. The development-only admin page is `/admin/menu` and supports category/item CRUD and ten-item pagination.

## Integration seams

`OrderingPorts.Catalog` and `OrderingPorts.Orders` are replaced by persistent adapters after the Supabase/JPA/Flyway baseline lands. `OrderingPorts.Sessions` must read the real Dining Session owner contract. The current `DemoOrderingStore` provides process-local sample data (session 1, package 1, table A01) for independent development and is disabled by the `persistence` Spring profile. Its data resets on restart and is not production storage. The persistence adapter should implement all three ports before activating that profile.

The customer page is `/customer/sessions/{id}`. In development, this feature uses the Vite `/api/v1` proxy to reach the local backend without depending on the shared CORS setup. Production uses `VITE_API_BASE_URL`. The admin route is development-only until the Authentication owner supplies staff authorization. QR token validation, customer access control, staff authorization for menu CRUD, fulfillment status updates, billing, and real Session lookup belong to the respective owners at integration. These endpoints must be secured before deployment. API/JSON changes require ศรัณย์'s review, shared entity changes require ปวริศช์'s review, and migrations require เมธัส's review.
