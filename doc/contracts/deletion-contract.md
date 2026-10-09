# Resource Removal and History Contract

**Status:** Proposed shared contract for owner review; not yet implemented or accepted by module owners.
**Scope:** U03 user-facing errors and R01 removal/archive semantics. This contract does not authorize a schema migration or production-data operation.

## Rules

1. A Manager may hard-delete a resource only when it has never been used and no foreign key references it. Never cascade-delete business history to make deletion succeed.
2. If a resource has business history, remove it from operational lists by archiving it. Keep its orders, bills, payments, dining-session snapshots, stock transactions, and actor references readable through their existing history views.
3. Temporary deactivation and archival are separate states. Main lists exclude archived resources; a dedicated Manager-only archived list shows them. Existing `active=false` must not itself mean archived.
4. Archive is not a mutation of past snapshots. Disabling or archiving a buffet package or soup must not change an open dining session or its bill.
5. Do not add deletion endpoints for orders, bills, payments, or stock transactions. Do not add database-wipe functionality.
6. An archive request repeated for an already archived resource succeeds without further change. A repeated hard-delete for an absent resource returns `404`. Restore is idempotent and returns the current resource; it does not reactivate it. A separate explicit activation action is required.
7. Every error response retains the existing `ErrorResponse` JSON shape and the endpoint's established HTTP status. Authentication failures remain `401`, role failures `403`, missing resources `404`, and business conflicts `409` unless an existing endpoint's contract explicitly uses another status.

## Resource behavior

| Resource | Existing/main-list behavior | Removal decision | Archive list and restore | Safety constraints |
|---|---|---|---|---|
| Restaurant table | `GET /api/v1/tables` | `DELETE /api/v1/tables/{id}` hard-deletes only with no dining-session reference; otherwise archives | Manager-only `GET /api/v1/tables/archived`; `POST /api/v1/tables/{id}/restore` restores as `AVAILABLE` | Reject archive/delete while an `ACTIVE` dining session exists. Do not change `OCCUPIED` to available as a side effect. Preserve all closed-session history. |
| Menu item | Paginated `GET /api/v1/menu-items` | `DELETE /api/v1/menu-items/{id}` hard-deletes only with no order/history reference; otherwise archives | Manager-only `GET /api/v1/menu-items/archived` with the normal page shape; `POST /api/v1/menu-items/{id}/restore` restores as unavailable | Archived items cannot be ordered. Do not remove `OrderItem` references or snapshots. |
| Menu category | `GET /api/v1/menu-categories` | `DELETE /api/v1/menu-categories/{id}` hard-deletes only with no child/reference; otherwise archives when safe | Manager-only `GET /api/v1/menu-categories/archived`; `POST /api/v1/menu-categories/{id}/restore` restores the category but not its children | If any available menu item remains, preserve the existing `400` and explain how to move/archive items first. Never silently delete children. |
| Buffet package | `GET /api/v1/buffet-packages` with optional `active` filter | `DELETE /api/v1/buffet-packages/{id}` hard-deletes only with no session/history reference; otherwise archives | Manager-only `GET /api/v1/buffet-packages/archived`; `POST /api/v1/buffet-packages/{id}/restore` restores inactive | Keep temporary `PATCH /{id}/active` separate. Preserve package ID and price snapshots on existing sessions/bills. |
| Soup | `GET /api/v1/soups` with optional `active` filter | `DELETE /api/v1/soups/{id}` hard-deletes only with no session/history reference; otherwise archives | Manager-only `GET /api/v1/soups/archived`; `POST /api/v1/soups/{id}/restore` restores inactive | Keep temporary `PATCH /{id}/active` separate. Existing session/billing snapshots must not change. |
| Stock item | `GET /api/v1/stock` | `DELETE /api/v1/stock/items/{id}` hard-deletes only with no transaction/reference; otherwise archives | Manager-only `GET /api/v1/stock/items/archived`; `POST /api/v1/stock/items/{id}/restore` | Preserve current balance and all `StockTransaction` rows/actor references. Existing active toggle remains temporary deactivation. |
| Staff account | `GET /api/v1/admin/users` | `DELETE /api/v1/admin/users/{id}` hard-deletes only if never used/referenced; otherwise closes and archives, revoking access and credentials | Manager-only `GET /api/v1/admin/users/archived`; `POST /api/v1/admin/users/{id}/restore` | Closed account cannot log in; revoke its existing sessions/permissions. Reject self-close and any action that would leave zero active Managers. Restore remains inactive until explicitly enabled. |
| Orders, bills, payments, stock transactions | Existing history/read endpoints | No delete/archive endpoint | Remain readable through existing history surfaces | No history clearing or FK/RLS/permission weakening. |

The `DELETE` action's user-visible success must say that the item was removed from active use; it must not imply that historical records were erased. If removal is blocked, keep data unchanged and return the documented `ErrorResponse`.

## HTTP and response contract

- Successful hard-delete or archive: `204 No Content` (preserve the current delete response shape).
- Successful restore: `200 OK` with the resource DTO, still inactive/unavailable.
- Archive-list read: `200 OK`, empty array/page when there are no archived rows.
- Not authenticated: `401 ErrorResponse`; authenticated but not Manager: `403 ErrorResponse`.
- Unknown ID or repeated hard-delete after physical deletion: `404 ErrorResponse`.
- Active-session, category-child, last-Manager, or other business safety violation: `409 ErrorResponse`; no partial mutation.
- Repeat archive: `204`, no further mutation. Repeat restore: `200`, return current inactive resource.

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
| Category still has menu items | `หมวดหมู่นี้ยังมีเมนูอยู่ กรุณาย้ายหรือเก็บเมนูออกก่อน` | Preserve current `400 ErrorResponse` |
| A historical resource is successfully archived | UI success copy: `นำรายการออกจากรายการใช้งานแล้ว ประวัติยังคงอยู่ในรายการเก็บออก` | `204 No Content`; do not alter API JSON shape. This replaces the current history-based delete error for resources that can be archived. |

Validation errors continue to use the shared `ErrorResponse`; translate only the specified U03 business cases in this change. Do not change status codes or add fields to localize the messages.

## Persistence and ownership gate

Before implementation, each Entity owner must confirm whether history/FK references exist, the archive DTO fields, main/archive filtering, and restore preconditions. If an archive field is needed:

1. Entity owner records the exact field/behavior delta.
2. Methus inspects actual history and allocates a new forward-only migration version.
3. Do not edit an applied migration or apply the change to the shared database before owner review and Pavarit approval.
4. Add tests for hard-delete-with-no-history, archive-with-history, main/archive filtering, repeat archive/restore, authorization, conflict/no-partial-write, and history preservation.

Current code has no general archive state/list contract. Existing `active` flags and `DELETE` endpoints are not proof that these requirements are met. This document must be reviewed by Pavarit (business rules), Sirapat (UI/copy), and Methus (Auth/DB) before owner implementation.
