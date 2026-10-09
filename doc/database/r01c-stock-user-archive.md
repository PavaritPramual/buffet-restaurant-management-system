# R01-C: ลบ/เก็บออก Stock item และ User (Manager)

## เลข migration

| เลข | โฟลเดอร์ | เจ้าของ | สถานะ |
|---|---|---|---|
| V1–V15 | common | develop | merge แล้ว |
| **V16** | common | **R01-C (PR นี้)** | PR นี้ |
| V17+ | - | table/menu/package/soup archive | ให้เจ้าของตรวจ `flyway_schema_history` และจองเลขก่อนใช้ |

- ตรวจแล้ว: `origin/develop` ไม่มี V16 ขึ้นไป และ branch `codex/manager-force-actions` เลิกใช้แล้ว จึงใช้ V16 ต่อจาก V15
- ถ้ามี branch อื่นจองเลขเดียวกัน ให้ตรวจ `flyway_schema_history` ก่อน merge
- เมื่อเพิ่ม migration ต้องอัปเดตรายการเวอร์ชันใน `MenuOrderingMigrationTest` และ `PostgresMenuOrderingMigrationTest`
  (จุดที่ merge conflict ได้)

## Schema delta (V16__add_stock_item_and_user_archive.sql)

- `stock_items.archived_at TIMESTAMP NULL`
- `app_users.archived_at TIMESTAMP NULL`
- `fk_stock_transaction_actor` (`stock_transactions.actor_id -> app_users.id`) เปลี่ยน `ON DELETE SET NULL` เป็น `ON DELETE RESTRICT`
  เพื่อไม่ให้ชื่อผู้ทำรายการหายเมื่อลบ user จริง
- index `idx_stock_transactions_actor (actor_id)`
- ไม่แก้ `quantity`/`opening_target_stock` ของแถวเดิม; แถวเดิมมี `archived_at = NULL`
- ไม่ต้องมี migration เฉพาะ PostgreSQL (RLS ครอบตารางเดิมอยู่แล้ว)

## API (ทุกเส้นทาง Manager เท่านั้น: 401 ไม่ล็อกอิน, 403 ไม่ใช่ Manager, 404 ไม่พบ, 409 ขัดกติกา)

| Method | Path | พฤติกรรม |
|---|---|---|
| DELETE | `/api/v1/stock/items/{id}` | ไม่มี transaction และยอด = 0 → ลบจริง; อื่น ๆ → เก็บออก (active=false, archivedAt) ตอบ 204 |
| GET | `/api/v1/stock/items/archived` | รายการที่เก็บออก |
| POST | `/api/v1/stock/items/{id}/restore` | กู้คืน (ยังเป็น inactive) |
| DELETE | `/api/v1/admin/users/{id}` | ไม่เคยมี stock transaction → ลบจริง; มี → เก็บออก; ตอบ 204 |
| GET | `/api/v1/admin/users/archived` | บัญชีที่เก็บออก |
| POST | `/api/v1/admin/users/{id}/restore` | กู้คืน (ยังปิดใช้งาน) |
| PUT | `/api/v1/admin/users/{id}/active` | เปิด/ปิดบัญชี (เปิดบัญชีที่เก็บออกไม่ได้) |

กติกา:
- Stock ที่เก็บออกจะไม่อยู่ใน list ปกติ/ภาพรวม และ stock-in/adjust/แก้ไข/เปิดใช้งาน ถูกปฏิเสธ 409
- ห้ามลบ/ปิดตนเอง และห้ามทำให้ไม่เหลือ Manager ที่ active (409)
- บัญชีที่ลบ/เก็บออก/ปิด: login ไม่ได้, session เดิมถูก invalidate หลัง commit, ชื่อผู้ทำ transaction ย้อนหลังยังอยู่
- ฟิลด์ response เพิ่มแบบ additive: `archivedAt` (stock/user), `active` (user)

### ส่วนที่ต่างจาก contract (PR #41) ที่ต้องให้ reviewer ยืนยัน
1. ลบ stock จริงเมื่อไม่มี transaction **และ** ยอด = 0 (legacy ที่ยอด > 0 และไม่มี transaction จะเก็บออก เพื่อไม่แก้/ทิ้งยอด)
2. ไม่ล้าง password hash; กัน login ด้วย flag `active`/`archived_at`
3. เพิ่ม `PUT /admin/users/{id}/active` เพราะ restore แล้วบัญชียังปิดอยู่
4. การเพิกถอน session ใช้ `StaffSessionRegistry` ในหน่วยความจำ (รองรับ node เดียว) ถ้าขยายหลาย node ต้องย้ายไป Spring Session/ตรวจ DB ทุก request

## UI (1280px)
- หน้า สต็อก: ปุ่ม "ลบ/เก็บออก" (Manager) + ConfirmDialog, ส่วน "รายการที่เก็บออก" + กู้คืน
- หน้า พนักงาน: คอลัมน์สถานะ/จัดการ (เปิด-ปิด, ลบ/เก็บออก; ซ่อนสำหรับบัญชีตนเอง), ส่วน "บัญชีที่เก็บออก"
- ตรวจที่ viewport 1280px ไม่มี horizontal overflow (scrollWidth 1265 ≤ 1280 ที่เหลือจาก scrollbar)

## ผล validation

| ชุดทดสอบ | DB | ผล |
|---|---|---|
| Backend `./mvnw.cmd test` | H2 | 359 tests, 0 fail, 0 error, 25 skipped (ชุด Postgres ข้ามเพราะไม่ตั้ง env) |
| `StockUserArchiveIntegrationTest` | H2 | 9/9 ผ่าน (ลบจริง, เก็บออก, legacy qty>0, RBAC/forged header, revoke session/login, actor name คงอยู่, self guard, last-Manager) |
| Frontend lint/build/test | - | lint ผ่าน (warning เดิม), build ผ่าน, 130/130 tests |
| `PostgresStockUserArchiveIntegrationTest` (ชุดเดียวกับ H2 9 เคส) | PostgreSQL 16 (Testcontainers) | 9/9 ผ่าน (apply V16 บน PostgreSQL จริง) |
| `PostgresStockProfileMigrationTest`, `PostgresStockSecurityIntegrationTest` | PostgreSQL 16 (Testcontainers) | 1/1 และ 2/2 ผ่าน |
| `PostgresMenuOrderingMigrationTest` | PostgreSQL 16 (container ชั่วคราว `buffet_test_*`) | 1/1 ผ่าน (รายการเวอร์ชันมี 16) |
