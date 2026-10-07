# Authentication and Stock Schema Delta

สถานะปัจจุบัน 6 ตุลาคม 2026 (ประวัติ Step 2): ปวริศช์รับรอง schema Auth/Stock ที่มีอยู่เป็นขอบเขต Step 2 ตาม [canonical Data Dictionary](step2-schema-approved.md) โดยเลื่อน opening_target_stock, active/inactive ของ Stock และชื่อ/นามสกุล/โทรศัพท์แยกไป Final; ต่อมา implement ใน V15 แล้ว (ดูหัวข้อ V15 ด้านล่าง) การรับรอง Step 2 ไม่ใช้แทนการอนุมัติ V15

The design baseline remains in [data-dictionary-design.md](data-dictionary-design.md). The current Auth/Stock implementation uses the following deliberate differences:

| Design baseline | Implemented schema | Notes |
| --- | --- | --- |
| `users` with `password VARCHAR(255)` | `app_users` with `password_hash VARCHAR(100)` | Names the table as application-owned and makes the BCrypt hash explicit. Username is capped at 80 to match the API/entity contract. |
| Profile `id`, `first_name`, `last_name`, `phone_number`, `email` | `user_profiles.user_id` shared primary key, `display_name`, `email`, plus V15 `first_name`, `last_name`, `phone_number` | `display_name` and `email` are preserved. V15 names are required for new accounts but nullable for legacy rows; phone is optional and limited to 20 characters. |
| Stock `current_stock`, `minimum_stock`, `opening_target_stock`, `active` | `stock_items.quantity`, `low_stock_threshold`, `opening_target_stock`, `active`, `sku`, `updated_at` | Quantity and target use `DECIMAL(12,3)` to support measured ingredients. V15 implements the target and active lifecycle; see the V15 schema table below. |
| Transaction `user_id`, `type`, `quantity`, nullable reason | `actor_user_id`, `transaction_type`, `quantity_delta`, required reason, `balance_after` | `IN` and `ADJUSTMENT` are the implemented transaction types. `balance_after` is kept as an audit snapshot; deleted users become a null actor, though user deletion is not currently exposed. |

V10 creates the common Auth/Stock tables. V11 applies PostgreSQL RLS and revokes access from `PUBLIC`, `anon`, and `authenticated`. JDBC ของแอปใช้ postgres และ BYPASSRLS จึงต้องตรวจสิทธิ์ผ่าน API ไม่อ้างว่า FORCE RLS จำกัด backend ได้ V1–V12 อยู่ใน develop และ Supabase แล้ว รวม Payment V9/V12

ประวัติ Supabase ณ 6 ตุลาคม: V9 installed_rank 11 หลัง V10/V11 และ V12 rank 12 ไม่ได้ตรวจยืนยันวิธีหรือผู้รัน Flyway validate/checksum ผ่านทั้ง 12 และ runtime เริ่มได้โดย outOfOrder=false/pending=0 ห้ามแก้ migration ที่ apply แล้วหรือใช้ repair เพื่อทำให้ผ่านโดยไม่สืบสาเหตุ

Stock-in/adjustment/history และ User/login ทดสอบผ่าน UI จริงบน PostgreSQL แยกแล้ว รายละเอียดและขอบเขตอยู่ใน [รายงานปิด Step 2](../testing/pavarit-step2-close-report.md) งานที่เลื่อนไป Final ต้องมี task แยก และ Step 2 ยังรอ PR รวมผ่านรีวิว/merge กับ smoke จาก develop

## Final delta: V15 (implemented ใน repo; สถานะฐานกลางดูหัวข้อ "สถานะ V15 บนฐานกลาง")

ไฟล์: `code/backend/src/main/resources/db/migration/common/V15__add_stock_target_active_and_profile_names.sql` (forward-only; V1–V14 ไม่ถูกแก้ การอนุมัติ V13/V14 ไม่ครอบคลุม V15)

| Table | Column | Type / constraint | หมายเหตุ |
| --- | --- | --- | --- |
| `stock_items` | `opening_target_stock` | `DECIMAL(12,3) NOT NULL DEFAULT 0`, CHECK `>= 0` (`chk_stock_opening_target_nonnegative`) | **ต่างจาก design baseline** (`DECIMAL(10,2)`) เพราะตาม quantity ปัจจุบัน (12,3); แถวเดิม = 0 |
| `stock_items` | `active` | `BOOLEAN NOT NULL DEFAULT TRUE` | แถวเดิม = TRUE; inactive ปฏิเสธ stock-in/adjust (409) แต่อ่านประวัติได้ |
| `user_profiles` | `first_name` | `VARCHAR(100)` NULL | **ต่างจาก baseline** (NOT NULL) เพราะข้อมูลเก่าไม่มีชื่อ; API บังคับสำหรับบัญชีใหม่ |
| `user_profiles` | `last_name` | `VARCHAR(100)` NULL | API บังคับสำหรับบัญชีใหม่ |
| `user_profiles` | `phone_number` | `VARCHAR(20)` NULL | optional; backend จำกัดเพียงความยาวสูงสุด 20 ไม่มี regex เพราะรูปแบบเบอร์ยังไม่มีผู้ถือ Data Dictionary ยืนยัน |

คงเดิม: `display_name`, `email`, shared PK `user_id`, `quantity`, `low_stock_threshold` ส่วน shortfall = `max(target − quantity, 0)` คำนวณตอนอ่าน ไม่เก็บ ไม่สร้าง stock-in อัตโนมัติ

## สถานะ V15 บนฐานกลาง (ปัจจุบัน)

- **Repo/tests:** implemented; H2 และ PostgreSQL Testcontainers (`PostgresStockProfileMigrationTest`, `PostgresStockSecurityIntegrationTest`) ผ่าน การทดสอบบน DB ทิ้งได้ **ไม่ใช่หลักฐานรับรองการ apply ฐานกลาง**
- **ฐานกลาง: ยังไม่ยืนยัน** — มีบันทึก readback ของทีม dev (ด้านล่าง) ว่า apply แล้ว แต่ **ยังไม่ได้แนบ** หลักฐานอนุมัติ V15 แยกจาก V13/V14, sanitized Flyway history (version/script/checksum/success) และ validate ที่ตรงกับไฟล์ใน commit นี้ จึงยังยืนยันการ apply ไม่ได้ และห้าม apply ซ้ำเพื่อแก้เอกสาร
- **Canonical:** [step2-schema-approved.md](step2-schema-approved.md) ครอบคลุม V1–V12 เท่านั้น ไม่มีคอลัมน์ V15 และ V13/V14 ยังไม่ apply Supabase
- Notion ยังไม่ได้ sync จากเอกสารนี้

### ประวัติรอบก่อน (7 ต.ค. 2026 ก่อน readback) — superseded

เคยระบุว่า central apply/readback = planned (รออนุมัติ) ข้อความนี้ถูกแทนที่ด้วยรายการสถานะข้างต้น

**Central apply readback (7 ต.ค. 2026 18:32 +07, ทีม dev รายงาน; หลักฐานต้นทางยังไม่แนบ จึงยังยืนยันไม่ได้):** มีรายงานว่า Flyway validate 15 migrations ผ่าน, migrate v14→v15 สำเร็จ; `flyway_schema_history` rank 15 version 15 success=true; columns/ชนิด/nullable/default ตรง V15; CHECK `chk_stock_opening_target_nonnegative` มีอยู่; แอปเริ่มได้ด้วย `ddl-auto: validate` (JPA ผ่าน); stock_items และ user_profiles บนฐานกลางว่าง (0 แถว) จึงไม่มีข้อมูลเดิมให้ตรวจ backfill ซึ่งครอบคลุมโดย H2/PostgreSQL migration tests แทน
