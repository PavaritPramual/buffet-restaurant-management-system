# Authentication and Stock Schema Delta

Status: partial implementation delta; it is not yet approved as complete against the design baseline. Feature-owner approval is required before merge or applying V10/V11 to shared Supabase.

The design baseline remains in [data-dictionary-design.md](data-dictionary-design.md). The current Auth/Stock implementation uses the following deliberate differences:

| Design baseline | Implemented schema | Notes |
| --- | --- | --- |
| `users` with `password VARCHAR(255)` | `app_users` with `password_hash VARCHAR(100)` | Names the table as application-owned and makes the BCrypt hash explicit. Username is capped at 80 to match the API/entity contract. |
| Profile `id`, `first_name`, `last_name`, `phone_number`, `email` | `user_profiles.user_id` shared primary key, `display_name`, `email` | Current Admin UI accepts one display name; separate names and phone are deferred. |
| Stock `current_stock`, `minimum_stock`, `opening_target_stock`, `active` | `stock_items.quantity`, `low_stock_threshold`, `sku`, `updated_at` | Quantity uses `DECIMAL(12,3)` to support measured ingredients. `opening_target_stock` and the active/inactive lifecycle remain unimplemented; the current schema is not complete against the baseline. |
| Transaction `user_id`, `type`, `quantity`, nullable reason | `actor_user_id`, `transaction_type`, `quantity_delta`, required reason, `balance_after` | `IN` and `ADJUSTMENT` are the implemented transaction types. `balance_after` is kept as an audit snapshot; deleted users become a null actor, though user deletion is not currently exposed. |

V10 creates the common Auth/Stock tables. V11 applies PostgreSQL RLS and revokes access from `PUBLIC`, `anon`, and `authenticated`; the backend connects as the table owner. V6–V8 are present in the current migration history and are intentionally not recreated or edited. The current checkout has no Payment V9 migration; do not describe V9 as already present in develop.

Before applying migrations to a shared database, coordinate Payment V9 with its owner. With Flyway `outOfOrder=false`, apply Payment V9 before V10/V11, or agree on a replacement version before any of these migrations are applied; V9 added after V10/V11 will not be applied in normal version order. Do not edit an already-applied migration to make the schemas appear aligned.

Before calling Stock complete, owners must explicitly approve this schema delta, approve deferring `opening_target_stock` and active/inactive lifecycle, or request the follow-up migration/API/UI work that implements them. These items remain outstanding; V10/V11 do not implement them.