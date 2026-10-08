# Authentication and Stock Schema Delta

สถานะปัจจุบัน 6 ตุลาคม 2026 (ประวัติ Step 2): ปวริศช์รับรอง schema Auth/Stock ที่มีอยู่เป็นขอบเขต Step 2 ตาม [canonical Data Dictionary](step2-schema-approved.md) โดยเลื่อน opening_target_stock, active/inactive ของ Stock และชื่อ/นามสกุล/โทรศัพท์แยกไป Final; ต่อมา implement ใน V15 แล้ว (ดูหัวข้อ V15 ด้านล่าง) การรับรอง Step 2 ไม่ใช้แทนการอนุมัติ V15

The design baseline remains in [data-dictionary-design.md](data-dictionary-design.md). The current Auth/Stock implementation uses the following deliberate differences:

| Design baseline | Implemented schema | Notes |
| --- | --- | --- |
| `users` with `password VARCHAR(255)` | `app_users` with `password_hash VARCHAR(100)` | Names the table as application-owned and makes the BCrypt hash explicit. Username is capped at 80 to match the API/entity contract. |
| Profile `id`, `first_name`, `last_name`, `phone_number`, `email` | `user_profiles.user_id` shared primary key, `display_name`, `email`, plus V15 `first_name`, `last_name`, `phone_number` | `display_name` and `email` are preserved. V15 names are required for new accounts but nullable for legacy rows; phone is optional and limited to 20 characters. |
| Stock `current_stock`, `minimum_stock`, `opening_target_stock`, `active` | `stock_items.quantity`, `low_stock_threshold`, `opening_target_stock`, `active`, `sku`, `updated_at` | Quantity and target use `DECIMAL(12,3)` to support measured ingredients. V15 implements the target and active lifecycle; see the V15 schema table below. |
| Transaction `user_id`, `type`, `quantity`, nullable reason | `actor_user_id`, `transaction_type`, `quantity_delta`, required reason, `balance_after` | `IN` and `ADJUSTMENT` are the implemented transaction types. `balance_after` is kept as an audit snapshot; deleted users become a null actor, though user deletion is not currently exposed. |

V10 creates the common Auth/Stock tables. V11 applies PostgreSQL RLS and revokes access from `PUBLIC`, `anon`, and `authenticated`. JDBC ของแอปใช้ postgres และ BYPASSRLS จึงต้องตรวจสิทธิ์ผ่าน API ไม่อ้างว่า FORCE RLS จำกัด backend ได้ V1–V12 อยู่ใน develop และมีรายงาน apply ฐานกลาง รวม Payment V9/V12; สถานะ V13–V15 ที่ตรวจพบในฐานกลางแยกไว้ด้านล่าง

ประวัติ Supabase ณ 6 ตุลาคม (ข้อมูลก่อนหน้า): V9 installed_rank 11 หลัง V10/V11 และ V12 rank 12; ตอนนั้นไม่ได้ตรวจยืนยันวิธีหรือผู้รัน และบันทึกว่า Flyway validate/checksum ผ่านทั้ง 12 กับ runtime เริ่มได้โดย `outOfOrder=false/pending=0` ห้ามแก้ migration ที่ apply แล้วหรือใช้ repair เพื่อทำให้ผ่านโดยไม่สืบสาเหตุ

Stock-in/adjustment/history และ User/login ทดสอบผ่าน UI จริงบน PostgreSQL แยกแล้ว รายละเอียดและขอบเขตอยู่ใน [รายงานปิด Step 2](../testing/pavarit-step2-close-report.md) งานที่เลื่อนไป Final ต้องมี task แยก

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

- **Repo/tests:** implemented; H2 และ PostgreSQL Testcontainers (`PostgresStockProfileMigrationTest`, `PostgresStockSecurityIntegrationTest`) ผ่าน การทดสอบบน DB ทิ้งได้ไม่ใช่หลักฐานแทนการตรวจฐานกลางหรือ approval
- **ฐานกลาง — apply ที่ตรวจพบ:** จาก read-only inspection ของ reviewer วันที่ 8 ต.ค. 2026 พบ V13–V15 ใน Flyway history ด้วย `success=true` พร้อม checksum/script ตามตาราง sanitized ด้านล่าง และ schema readback ตรงกับคอลัมน์ V15
- **หลักฐานที่ยังขาด:** ยังไม่มีหลักฐานอนุมัติ V15 แยกจาก V13/V14 และไม่มีผล Flyway `validate`/checksum comparison ที่ตรวจ migration file ของ commit นี้ สถานะจึงเป็น **V13–V15 apply แล้วตาม history ที่ reviewer ตรวจพบ; V15 approval และการยืนยัน checksum/validate ยังรอหลักฐาน** การตรวจ history นี้ไม่ใช่การรัน validate และไม่ใช่การอนุมัติย้อนหลัง
- ห้าม apply ซ้ำ, แก้ history หรือใช้ `repair` เพื่อปรับเอกสาร สถานะ canonical Step 2/ข้อจำกัดการอนุมัติแยกใน [step2-schema-approved.md](step2-schema-approved.md)
- Notion ยังไม่ได้ sync จากเอกสารนี้

### Sanitized read-only history/schema readback (reviewer ตรวจ 8 ต.ค. 2026)

| Rank | Version | Script | Checksum | Success | Installed on |
| ---: | --- | --- | ---: | --- | --- |
| 13 | 13 | `V13__restrict_remaining_client_database_access.sql` | `419855576` | true | — |
| 14 | 14 | `V14__add_session_bill_request.sql` | `-1840534327` | true | — |
| 15 | 15 | `V15__add_stock_target_active_and_profile_names.sql` | `-1092732854` | true | `2026-10-07 11:32:31.146844` |

Schema readback ที่ reviewer รายงาน: `stock_items.opening_target_stock NUMERIC(12,3) NOT NULL DEFAULT 0`; `stock_items.active BOOLEAN NOT NULL DEFAULT true`; `user_profiles.first_name`, `last_name`, `phone_number` nullable; มี CHECK `chk_stock_opening_target_nonnegative`. รายการนี้เป็น readback แบบ sanitized ไม่ใช่ผล validate และไม่ได้ยืนยันว่า checksum ในฐานตรงกับ migration file ของ commit ปัจจุบัน

### ประวัติข้อความสถานะก่อนตรวจ 8 ต.ค. 2026 — superseded

ข้อความเดิมระบุว่า central apply/readback planned หรือยังไม่ยืนยันการ apply และกล่าวว่า V13/V14 ยังไม่ apply; ข้อมูลดังกล่าวเป็นสถานะ/ความรู้ก่อน read-only inspection ของ reviewer วันที่ 8 ต.ค. และถูกแทนที่ด้วยสถานะกับหลักฐานที่รายงานข้างต้น
