# Authentication and Stock Schema Delta

Status: implementation delta for review; feature-owner approval is still required before merge or applying V10/V11 to shared Supabase.

The design baseline remains in [data-dictionary-design.md](data-dictionary-design.md). The current Auth/Stock implementation uses the following deliberate differences:

| Design baseline | Implemented schema | Notes |
| --- | --- | --- |
| `users` with `password VARCHAR(255)` | `app_users` with `password_hash VARCHAR(100)` | Names the table as application-owned and makes the BCrypt hash explicit. Username is capped at 80 to match the API/entity contract. |
| Profile `id`, `first_name`, `last_name`, `phone_number`, `email` | `user_profiles.user_id` shared primary key, `display_name`, `email` | Current Admin UI accepts one display name; separate names and phone are deferred. |
| Stock `current_stock`, `minimum_stock`, `opening_target_stock`, `active` | `stock_items.quantity`, `low_stock_threshold`, `sku`, `updated_at` | Quantity uses `DECIMAL(12,3)` to support measured ingredients. Opening target and active/inactive lifecycle are not yet implemented. |
| Transaction `user_id`, `type`, `quantity`, nullable reason | `actor_user_id`, `transaction_type`, `quantity_delta`, required reason, `balance_after` | `IN` and `ADJUSTMENT` are the implemented transaction types. `balance_after` is kept as an audit snapshot; deleted users become a null actor, though user deletion is not currently exposed. |

V10 creates the common Auth/Stock tables. V11 applies PostgreSQL RLS and revokes access from `PUBLIC`, `anon`, and `authenticated`; the backend connects as the table owner. V6–V9 belong to the develop migration history and are intentionally not recreated or edited in this feature branch.

Before merge, owners should either approve this delta as the implementation contract or request a follow-up migration/API change to align with the design baseline. Do not edit an already-applied migration to make the schemas appear aligned.