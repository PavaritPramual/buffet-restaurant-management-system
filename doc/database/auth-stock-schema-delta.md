# Authentication and Stock Schema Delta

สถานะปัจจุบัน 6 ตุลาคม 2026: ปวริศช์รับรอง schema Auth/Stock ที่มีอยู่เป็นขอบเขต Step 2 ตาม [canonical Data Dictionary](step2-schema-approved.md) โดยเลื่อน opening_target_stock, active/inactive ของ Stock และชื่อ/นามสกุล/โทรศัพท์แยกไป Final งานเหล่านี้ยังไม่ได้ implement ไม่ใช้การรับรองแทนผลพัฒนา

The design baseline remains in [data-dictionary-design.md](data-dictionary-design.md). The current Auth/Stock implementation uses the following deliberate differences:

| Design baseline | Implemented schema | Notes |
| --- | --- | --- |
| `users` with `password VARCHAR(255)` | `app_users` with `password_hash VARCHAR(100)` | Names the table as application-owned and makes the BCrypt hash explicit. Username is capped at 80 to match the API/entity contract. |
| Profile `id`, `first_name`, `last_name`, `phone_number`, `email` | `user_profiles.user_id` shared primary key, `display_name`, `email` | Current Admin UI accepts one display name; separate names and phone are deferred. |
| Stock `current_stock`, `minimum_stock`, `opening_target_stock`, `active` | `stock_items.quantity`, `low_stock_threshold`, `sku`, `updated_at` | Quantity uses `DECIMAL(12,3)` to support measured ingredients. `opening_target_stock` and the active/inactive lifecycle remain unimplemented; the current schema is not complete against the baseline. |
| Transaction `user_id`, `type`, `quantity`, nullable reason | `actor_user_id`, `transaction_type`, `quantity_delta`, required reason, `balance_after` | `IN` and `ADJUSTMENT` are the implemented transaction types. `balance_after` is kept as an audit snapshot; deleted users become a null actor, though user deletion is not currently exposed. |

V10 creates the common Auth/Stock tables. V11 applies PostgreSQL RLS and revokes access from `PUBLIC`, `anon`, and `authenticated`. JDBC ของแอปใช้ postgres และ BYPASSRLS จึงต้องตรวจสิทธิ์ผ่าน API ไม่อ้างว่า FORCE RLS จำกัด backend ได้ V1–V12 อยู่ใน develop และ Supabase แล้ว รวม Payment V9/V12

ประวัติ Supabase ณ 6 ตุลาคม: V9 installed_rank 11 หลัง V10/V11 และ V12 rank 12 ไม่ได้ตรวจยืนยันวิธีหรือผู้รัน Flyway validate/checksum ผ่านทั้ง 12 และ runtime เริ่มได้โดย outOfOrder=false/pending=0 ห้ามแก้ migration ที่ apply แล้วหรือใช้ repair เพื่อทำให้ผ่านโดยไม่สืบสาเหตุ

Stock-in/adjustment/history และ User/login ทดสอบผ่าน UI จริงบน PostgreSQL แยกแล้ว รายละเอียดและขอบเขตอยู่ใน [รายงานปิด Step 2](../testing/pavarit-step2-close-report.md) งานที่เลื่อนไป Final ต้องมี task แยก และ Step 2 ยังรอ PR รวมผ่านรีวิว/merge กับ smoke จาก develop
