# Manager force actions

Requested on 2026-10-09. These are separate Manager-only operations; normal Service Staff close and normal menu delete retain their existing guards.

## API

All paths below start with `/api/v1`. Identity comes from the trusted staff login session. Manager is the only allowed role; anonymous callers receive 401, other roles 403. Role headers cannot grant access.

| Method / path | Result |
| --- | --- |
| GET `/manager/dining-sessions/active` | Active sessions with table ID/number, people counts, status and UTC timestamps. No QR token. |
| GET `/manager/operations` | Latest 50 successful force operations, newest ID first. Includes action, resource snapshot, reason, actor ID/username and UTC time. |
| POST `/manager/dining-sessions/{id}/force-close` | 200 session summary without QR. ACTIVE becomes CANCELLED, `endTime` is set, table becomes AVAILABLE and customer grants are revoked atomically. |
| POST `/manager/menu-items/{id}/force-delete` | 204. Menu is removed from both Manager and Customer catalogs, including when order history exists. |

Both POST bodies are `{"reason":"reason for the operation"}`. Reason is required, nonblank and at most 500 characters; surrounding whitespace is removed. Invalid input returns 400 without a write. Missing resources and already deleted menus return 404; an already closed session returns 400. Failures do not create success audit entries.

## History and concurrency

Force close cancels the dining session; it does **not** record a payment, mark unpaid money as paid or change an existing payment. Order rows, item snapshots and payment rows remain. Cancelled-session orders disappear from Kitchen/Service Staff work queues and cannot advance. Existing COMPLETED-session behavior is unchanged. An existing customer cookie and an unused QR token for the cancelled session cannot be used. Service Staff can open a new session on the available table.

Force delete sets `menu_items.deleted_at`, sets `available=false` and clears package links. The referenced menu row and historical order items remain, preserving the FK and historical item names/quantities. Catalog reads exclude deleted rows; editing the old ID cannot reactivate it. A new item may reuse the deleted item's name and receives a new ID. Categories referenced by retained menu rows remain protected by the category delete guard.

Force close shares the DiningSession row lock used by Order, Bill Request, Payment and normal Close; fulfillment rechecks CANCELLED under that lock. Order and menu mutations lock menu rows, acquired in ID order for multi-item orders. A stale cart is rejected after deletion; an order that commits first remains in history. Audit and the operation commit or roll back together.

## UI and migration handoff

Manager uses **จัดการรอบกิน → บังคับปิดโต๊ะ** or **เมนูอาหาร → บังคับลบ**. Each dialog explains the effect and requires a reason before confirmation; duplicate submits are blocked. The session page displays the latest 50 force actions.

Forward migrations only: common V16 adds nullable `menu_items.deleted_at` and `manager_operations`; PostgreSQL V17 enables audit-table RLS and revokes direct PUBLIC/anon/authenticated table and sequence access. Existing migrations and enum values are unchanged. Do not run these manually on the shared database from a QA script. เมธัส reviews migration/RLS, ศรัณย์ reviews the new routes, and ปวริศช์ reviews shared entities before release.

Deploy backend/schema before frontend. Rolling the frontend back only removes the buttons; retaining V16/V17 and using the new backend preserves deletion filtering. Rolling the backend back to a pre-V16 implementation can expose logically deleted menus in its unfiltered Manager catalog, so use a forward fix or a coordinated rollback that accounts for deleted rows. No purge or restore endpoint is introduced.
