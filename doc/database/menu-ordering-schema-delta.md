# Menu and Ordering schema delta

Date: 26 September 2026

Scope: merged Menu/Ordering V4/V5 schema, Dining Session V6, and this branch's V7 FK integration

Design baseline: Notion ER Diagram and Data Dictionary & Migration

Package/Soup V3 merged through PR #12; the personal branch is now synced with the develop branch that includes PR #13. V4/V5 provide Menu/Ordering, V6 creates Dining Sessions, and this branch adds the forward-only V7 FK from orders to sessions. Verify the shared database migration history before any shared deployment; this work has only been applied to isolated test databases.

## Schema decisions

| Area | Previous implementation | Baseline / updated implementation | Reason |
|---|---|---|---|
| Package-menu table | `menu_item_packages` | `package_menu_items` | Match canonical table name and composite key order |
| Order table | `customer_orders` | `orders` | Match canonical table name |
| Session column | `dining_session_id` | `session_id` | Match Data Dictionary |
| Menu name | `VARCHAR(120) UNIQUE` | `VARCHAR(100)` | Match type and constraints in Data Dictionary; application duplicate validation remains |
| Menu description | Missing | Nullable `description TEXT` | Restore canonical field and expose it through Menu API/Admin/Customer UI |
| Order item note | Missing | Nullable `note VARCHAR(255)` in schema/JPA | Restore canonical persistence field; API exposure waits for fulfillment contract review |
| Internal foreign keys | Implicit category FK; no menu-item FK | Named category and menu-item FKs with specified delete behavior | Match ERD relationships and make constraints reviewable |
| Defaults | Status/time set only by Java | Database defaults for `orders.status` and `orders.created_at` | Match Data Dictionary and keep direct inserts valid |

## Proposed design extensions requiring approval

| Extension | Reason | Impact |
|---|---|---|
| `menu_items.image_url VARCHAR(500)` | Admin stores an external or site-relative menu image URL required by the delivered UI | Add field to ERD/Data Dictionary; no binary data stored |
| `orders.table_number VARCHAR(20)` | Preserve the displayed table number in `OrderFulfillmentContext` without a later session lookup | Denormalized snapshot; confirm with Architecture and Dining Session owner |
| `order_items.item_name VARCHAR(100)` | Preserve the ordered name if a menu is renamed later | Denormalized snapshot; confirm with Kitchen owner |
| `TIMESTAMP WITH TIME ZONE` for `orders.created_at` | Shared contract requires ISO-8601 with timezone | Update Data Dictionary from `TIMESTAMP` if approved |
| Positive quantity check and supporting indexes | Enforce an existing business rule and query paths at database level | Add constraints/indexes to canonical design |

## External foreign-key staging

V4 adds the package FK because the V3 Package/Soup migration is present in its migration chain:

- `package_menu_items.package_id` → `buffet_packages.id` with `ON DELETE CASCADE`

The Dining Session table did not exist when V4 ran, so `orders.session_id` could not receive its FK there. V6 creates `dining_sessions`; V7 adds `fk_orders_dining_session` from `orders.session_id` to `dining_sessions.id` with `ON DELETE CASCADE`, as specified in the Data Dictionary migration design. V4 and V6 remain unchanged. V7 is tested on isolated databases in this branch; this work has not migrated shared Supabase.

## Verification and reviewers

- Empty H2 test database: Flyway applies V1, V2, V3, V4, V6 and V7; Hibernate validates JPA.
- Empty local PostgreSQL 16 test database: Flyway applies V1–V7; Hibernate validates JPA.
- Upgrade test database: `MenuOrderingMigrationTest` migrates through V3 first, then applies all later available migrations through V7.
- Methus: review migration ordering, constraints and Supabase execution.
- Pavarit: approve schema extensions and external FK dependency.
- Sarun: approve Order item/name/note impact on `OrderFulfillmentContext`.
