# Resource Removal and History Contract

**Status:** Proposed shared contract for owner review; not yet implemented or accepted by module owners.
**Scope:** U03 user-facing errors and R01 removal/archive semantics. This contract does not authorize a schema migration or production-data operation.

**R01-B owner attestation, 9 October 2026:** Pavarit accepts Table/Package/Soup semantics in this merged contract and records [exact design/FK delta](../architecture/pavarit-r01-b-design-delta.md). His implementation uses V17; Methus owns V16 and Sirapat V18 per the team's confirmed reservation. This attestation does not accept other owners' implementation, authorize shared migration, or certify public runtime. [Schema/DTO delta](../database/pavarit-r01-b-schema-delta.md) adds only archived flags to these three resources; operational details reject archived resources until restore, while existing session history remains readable.

**Scoped HTTP status migration in R01-B:** the legacy occupied-table DELETE rejection changes from `400` to the shared R01 target `409`. ACTIVE-session rejection is already `409`. The DTO/error JSON shape stays unchanged. This change is submitted for API review in this PR; the category-child compatibility exception remains owned by R01-A. Existing tests are updated for the table rule rather than implying that every resource's status migration is complete.

## Rules

1. A Manager may hard-delete a resource only when it has never been used and no foreign key references it. Never cascade-delete business history to make deletion succeed.
2. If a resource has business history, remove it from operational lists by archiving it. Keep its orders, bills, payments, dining-session snapshots, stock transactions, and actor references readable through their existing history views.
3. Temporary deactivation and archival are separate states. Main lists exclude archived resources; a dedicated Manager-only archived list shows them. Existing `active=false` must not itself mean archived.
4. Archive is not a mutation of past snapshots. Disabling or archiving a buffet package or soup must not change an open dining session or its bill.
5. Do not add deletion endpoints for orders, bills, payments, or stock transactions. Do not add database-wipe functionality.
6. An archive request repeated for an already archived resource succeeds without further change. A repeated hard-delete for an absent resource returns `404`. Restore changes state only on the `archived → restored` transition, setting the agreed safe initial state. If the resource is already restored, repeat restore is a no-op that returns its current DTO and preserves its current active/available/table status, including any later activation or active session.
7. Every error response retains the existing `ErrorResponse` JSON shape. Authentication failures are `401`, role failures `403`, missing resources `404`, and R01 business safety conflicts are `409`. The explicitly listed U03 legacy cases keep their current status until a separately reviewed status migration; this PR does not change runtime status codes.

## Resource behavior

| Resource | Existing/main-list behavior | Removal decision | Archive list and restore | Safety constraints |
|---|---|---|---|---|
| Restaurant table | `GET /api/v1/tables` | `DELETE /api/v1/tables/{id}` hard-deletes only with no dining-session reference; otherwise archives | Manager-only `GET /api/v1/tables/archived`; `POST /api/v1/tables/{id}/restore` sets `AVAILABLE` only when transitioning from archived to restored | Reject archive/delete while an `ACTIVE` dining session exists. Restore retry on an already restored table returns current status without mutation, even if now `OCCUPIED` or referenced by an `ACTIVE` session. Preserve all closed-session history. |
| Menu item | Paginated `GET /api/v1/menu-items` | `DELETE /api/v1/menu-items/{id}` hard-deletes only with no order/history reference; otherwise archives | Manager-only `GET /api/v1/menu-items/archived` with the normal page shape; `POST /api/v1/menu-items/{id}/restore` sets unavailable only on the archived → restored transition | Archived items cannot be ordered. Repeat restore when already restored preserves current availability. Do not remove `OrderItem` references or snapshots. |
| Menu category | `GET /api/v1/menu-categories` | U03 currently rejects `DELETE /api/v1/menu-categories/{id}` with `400` whenever any menu item references the category, including unavailable items. Under R01, hard-delete only when there are no child/reference rows; reject archiving while any non-archived menu item remains, and allow archiving once all child menu items are archived. | Manager-only `GET /api/v1/menu-categories/archived`; `POST /api/v1/menu-categories/{id}/restore` restores the category but does not restore its children. | Preserve the current U03 guard/status until a separately reviewed runtime change. R01 category archive must retain all archived child rows, FK relationships, and their history; never cascade-delete or detach children. R01 non-archived-child conflict target is `409`. |
| Buffet package | `GET /api/v1/buffet-packages` with optional `active` filter | `DELETE /api/v1/buffet-packages/{id}` hard-deletes only with no session/history reference; otherwise archives | Manager-only `GET /api/v1/buffet-packages/archived`; `POST /api/v1/buffet-packages/{id}/restore` sets inactive only on the archived → restored transition | Keep temporary `PATCH /{id}/active` separate. Repeat restore preserves current activation. Preserve package ID and price snapshots on existing sessions/bills. |
| Soup | `GET /api/v1/soups` with optional `active` filter | `DELETE /api/v1/soups/{id}` hard-deletes only with no session/history reference; otherwise archives | Manager-only `GET /api/v1/soups/archived`; `POST /api/v1/soups/{id}/restore` sets inactive only on the archived → restored transition | Keep temporary `PATCH /{id}/active` separate. Repeat restore preserves current activation. Existing session/billing snapshots must not change. |
| Stock item | `GET /api/v1/stock` | `DELETE /api/v1/stock/items/{id}` hard-deletes only with no transaction/reference; otherwise archives | Manager-only `GET /api/v1/stock/items/archived`; `POST /api/v1/stock/items/{id}/restore` sets inactive only on the archived → restored transition | Repeat restore preserves current activation. Preserve current balance and all `StockTransaction` rows/actor references. Existing active toggle remains temporary deactivation. |
| Staff account | `GET /api/v1/admin/users` | `DELETE /api/v1/admin/users/{id}` hard-deletes only if never used/referenced; otherwise closes and archives, revoking access and credentials | Manager-only `GET /api/v1/admin/users/archived`; `POST /api/v1/admin/users/{id}/restore` | Repeat restore is a no-op and preserves account status. Closed account cannot log in; revoke existing sessions immediately. Reject self-close and any action that would leave zero active Managers. |
| Orders, bills, payments, stock transactions | Existing history/read endpoints | No delete/archive endpoint | Remain readable through existing history surfaces | No history clearing or FK/RLS/permission weakening. |

The `DELETE` action's user-visible success must say that the item was removed from active use; it must not imply that historical records were erased. If removal is blocked, keep data unchanged and return the documented `ErrorResponse`.

## HTTP and response contract

- Successful hard-delete or archive: `204 No Content` (preserve the current delete response shape).
- Successful restore: `200 OK` with the resource DTO. Only the first archived → restored transition applies the resource-specific safe initial state; retries preserve current state.
- Archive-list read: `200 OK`, empty array/page when there are no archived rows.
- Not authenticated: `401 ErrorResponse`; authenticated but not Manager: `403 ErrorResponse`.
- Unknown ID or repeated hard-delete after physical deletion: `404 ErrorResponse`.
- R01 active-session, non-archived-category-child, last-Manager, or other business safety violation: `409 ErrorResponse`; no partial mutation. Until a separately reviewed status migration, category-child and occupied-table U03 errors retain current `400`; this is a legacy compatibility exception, not the R01 target.
- Repeat archive: `204`, no further mutation. Restore changes to the safe initial state only while transitioning from archived to restored. A repeat restore of an already restored resource returns `200` with its current DTO and makes no state change; it must not deactivate an active item or make an occupied table available.

### Restore retry acceptance scenarios

1. Archive a table, restore it, open an `ACTIVE` session, then repeat restore. The retry returns the current table DTO and does not alter `OCCUPIED` or the active session.
2. Archive a package/soup/stock item/staff account, restore it to the agreed inactive state, explicitly activate it, then repeat restore. The retry returns the current active DTO and does not deactivate it.
3. Restore an already restored menu item after it has been made available again. The retry preserves `available=true`.
4. A first restore from archived sets only the resource's agreed initial state; all later retries are no-ops.

### Menu category archive acceptance scenarios

1. Under current U03 behavior, request category deletion while any menu item references it, including when every child item is unavailable. Return the existing `400 ErrorResponse` and leave the category and all child records unchanged.
2. Under the R01 contract, request category archive while at least one child menu item is not archived. Return `409 ErrorResponse`; do not archive the category or mutate any child.
3. Archive every child menu item, then archive the category. The category is removed from operational lists and appears in the Manager-only archived list; retain every child row, its category FK, order references, and history. Do not cascade-delete children or require moving them to another category.
4. Restore an archived category while its children remain archived. Restore only the category; children remain archived and unavailable until explicitly restored under their own contract.
5. Hard-delete a category only when no child or other FK reference remains. If archived children still reference it, archive the category instead and preserve those relationships.

## Authorization, account revocation, and referential integrity gates

- Every mutation and archived-list endpoint (`DELETE`, archive, `/archived`, `/restore`) must enforce Manager access both at the controller boundary and in the service/application layer. Existing `RestaurantTableServiceImpl.deleteTable` currently has no service-level access provider; add and test that guard before exposing or expanding table removal behavior.
- Closing a staff account must set it inactive and immediately invalidate its server-side sessions. Use the application's session registry to invalidate every `HttpSession` for the account; the request authorization path must also reject inactive accounts so stale sessions cannot remain usable if invalidation races or fails. Password hashes are retained (not treated as revocable tokens) but inactive accounts cannot authenticate. Archived usernames/emails remain reserved; do not silently reassign historical actor identity.
- Reject self-close. Serialize last-Manager validation and account closure in one transaction by locking active Manager rows in deterministic ID order before counting/updating; concurrent closures must not leave zero active Managers. Restore does not bypass the explicit activation flow.
- Foreign keys from orders, order items, bills, payments, dining sessions, stock transactions, and actor/user audit references must use `ON DELETE RESTRICT`/`NO ACTION`, never `CASCADE` or `SET NULL`. Service-level existence checks are an early user-facing guard only; database constraints are the race-safe authority.
- Map FK constraint violations during hard-delete to `409 ErrorResponse` and roll back the whole transaction with no partial mutation. Keep unique/other integrity conflicts distinguishable where needed.
- Entity owners and Methus must inventory and attest the actual FK actions before implementation. Pavarit will review the FK inventory and allocate the migration version; do not edit applied migrations or apply to the shared database before explicit approval.

Existing `active`/`available` filters continue to describe temporary service availability, not archive status. Archive reads use a distinct route so archived records cannot leak into customer/staff operational lists by accidentally omitting a filter.

## U03 message requirements

Messages are selected from stable domain error identifiers/types, not by matching or translating arbitrary English exception strings. Keep the existing response fields (`timestamp`, `status`, `error`, `message`, `path`) and established HTTP status.

| Situation | Thai `message` copy | Status/body |
|---|---|---|
| Guest count exceeds table capacity | `จำนวนผู้ใช้บริการเกินความจุของโต๊ะ (สูงสุด {capacity} คน)` | Preserve current `400 ErrorResponse` |
| Table is occupied/unavailable for opening a session | `โต๊ะไม่ว่าง กรุณาเลือกโต๊ะที่พร้อมใช้งาน` | Preserve current `400 ErrorResponse` |
| Occupied table cannot be hard-deleted | `ไม่สามารถลบโต๊ะได้ เนื่องจากโต๊ะยังไม่ว่าง` | Preserve current `400 ErrorResponse` |
| Table has an active dining session and cannot be edited/removed | `ไม่สามารถแก้ไขหรือลบโต๊ะได้ ขณะยังมีรอบใช้งานอยู่` | `409 ErrorResponse` |
| Table has closed dining history (current behavior until archive is implemented) | `โต๊ะนี้มีประวัติการใช้งาน จึงลบถาวรไม่ได้` | Preserve current `409 ErrorResponse`; successful archive will replace this rejection |
| Menu item has order history (current behavior until archive is implemented) | `เมนูนี้มีประวัติการสั่งซื้อ จึงลบถาวรไม่ได้` | Preserve current `400 ErrorResponse`; successful archive will replace this rejection |
| Category has any menu items (current U03 behavior; including unavailable items) | `หมวดหมู่นี้ยังมีเมนูอยู่ กรุณาย้ายเมนูไปหมวดอื่นก่อนลบหมวดหมู่นี้` | Current behavior is `400 ErrorResponse`; under R01, only a remaining non-archived child blocks category archive (`409 ErrorResponse`) after a reviewed status migration |
| A historical resource is successfully archived | UI success copy: `นำรายการออกจากรายการใช้งานแล้ว ประวัติยังคงอยู่ในรายการเก็บออก` | `204 No Content`; do not alter API JSON shape. This replaces the current history-based delete error for resources that can be archived. |

Validation errors continue to use the shared `ErrorResponse`; translate only the specified U03 business cases in this change. Do not change status codes or add fields to localize the messages.

## Persistence and ownership gate

Before implementation, each Entity owner must confirm whether history/FK references exist, the archive DTO fields, main/archive filtering, and restore preconditions. If an archive field is needed:

1. Entity owner records the exact field/behavior delta.
2. Methus inspects actual history and allocates a new forward-only migration version.
3. Do not edit an applied migration or apply the change to the shared database before owner review and Pavarit approval.
4. Add tests for hard-delete-with-no-history, archive-with-history, main/archive filtering, restore → activate/open session → repeat restore, authorization at controller and service layers, FK conflict/no-partial-write, and history preservation.

Current code has no general archive state/list contract. Existing `active` flags and `DELETE` endpoints are not proof that these requirements are met. The current category delete guard checks all menu items regardless of availability. The current U03 error statuses remain compatibility behavior; R01 target conflicts are separately stated above. This document must be reviewed by Pavarit (business rules), Sirapat (UI/copy), and Methus (Auth/DB) before owner implementation.
