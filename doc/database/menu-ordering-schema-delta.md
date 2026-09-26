# Menu and Ordering schema delta

Date: 26 September 2026

Scope: replacement Menu/Ordering PR, Flyway V4/V5 and matching JPA mappings

Design baseline: Notion ER Diagram and Data Dictionary & Migration

The shared Supabase project is at Flyway V1/V2. Package/Soup owns V3 in PR #12. This branch is stacked on that PR so Menu/Ordering can be developed and tested as V4/V5 now. Do not deploy V4/V5 to shared Supabase until V3 is merged and applied.

## Decisions carried into the replacement PR

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

The Dining Session table is reserved for V6 and does not exist when V4 runs, so `orders.session_id` cannot receive its canonical FK in V4. Pavarit and Methus must add `orders.session_id` → `dining_sessions.id` with the agreed delete behavior in V6 or a later reserved forward migration. This deferred constraint must be confirmed before the replacement PR merges.

## Verification and reviewers

- Empty test database: Spring integration suite migrates V1 → V4 and validates JPA.
- Upgrade test database: `MenuOrderingMigrationTest` migrates V1–V3 first, then applies V4.
- Methus: review migration ordering, constraints and Supabase execution.
- Pavarit: approve schema extensions and external FK dependency.
- Sarun: approve Order item/name/note impact on `OrderFulfillmentContext`.
