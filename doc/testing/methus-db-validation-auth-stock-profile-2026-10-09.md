# รายงานตรวจ DB / Validation / Auth-Stock-Profile (9 ต.ค. 2026)

ผู้ตรวจ: เมธัส (branch `methus_673380300-2_01`) — ขอบเขตของรายงานนี้คือ **ตรวจในเครื่อง (H2) เท่านั้น** ไม่ได้ตรวจฐานข้อมูลกลาง

## 1. Code ที่ตรวจ

| รายการ | ค่า |
|---|---|
| HEAD SHA | `e6172b20096f7fb5487418ce278928a82b3f59ee` (Merge PR #33) |
| Branch | `methus_673380300-2_01` |
| `origin/develop` ตอนตรวจ (local ref, ไม่ได้ fetch ใหม่) | `a6da906bfb7d3c2d7a9bb3e8b5601a7886219bcc`; HEAD เป็น ancestor ของ ref นี้ |
| Working tree | **dirty**: แก้เฉพาะ `doc/**` 14 ไฟล์ (diagram .puml/.svg, `auth-stock-schema-delta.md`, `jpa-entity-rationale.md`, `activity-diagrams.md`); **ไม่มีไฟล์ใน `code/` ถูกแก้**. SHA-256 ของ `git diff` = `dec27edec1dcf9e672da9ed4ca33e1224e388e618c0d99fd73dd6ee75bd562a9`; มี untracked `Data Dictionary & Migration page from Notion.md` |
| Migration ที่ตรวจ | `common/V1,V2,V4,V7,V8,V10,V14,V15`, `postgresql/V3,V5,V6,V9,V11,V12,V13` (+ `h2/` สำหรับ test) — ตรงกับ HEAD ไม่มีการแก้ |

## 2. Environment

- Windows 11 Pro, Java 17.0.12 (`C:\java\jdk-17.0.12`), Maven 3.9.16, Node (ติดตั้งแล้ว), Python 3.11
- DB ที่ใช้: **H2 in-memory** ของ Spring test profile เท่านั้น
- Docker Desktop: engine **ไม่ทำงาน** (`docker ps` เชื่อม `dockerDesktopLinuxEngine` ไม่ได้) → ไม่มี PostgreSQL disposable ในเครื่อง; `psql` ไม่ได้ติดตั้ง
- ไม่มี env `MENU_TEST_PG_URL` / `DINING_TEST_PG_URL` / `PAYMENT_TEST_PG_URL` / `ALLOW_DESTRUCTIVE_DB_TESTS`
- ไฟล์ `code/backend/.env` มีอยู่ในเครื่อง แต่ **ไม่ได้อ่านหรือใช้** และไม่ได้รัน `test/tools/SharedDatabaseReadiness.java`

## 3. คำสั่งที่รัน

```powershell
cd code\backend
mvn --batch-mode --no-transfer-progress verify
```

(เวลาเริ่ม ≈ 14:20 +07:00; ไม่ได้รัน frontend / browser runner)

## 4. ผลจริง

`BUILD SUCCESS`, exit code 0 — **Tests run: 346, Failures: 0, Errors: 0, Skipped: 28**; สร้าง `target/app.jar` สำเร็จ

ชุดที่เกี่ยวกับ Auth / Stock / Profile / DB (จาก surefire reports):

| Test | ผล |
|---|---|
| `AuthStockIntegrationTest` | 6 ผ่าน |
| `StockProfileFinalIntegrationTest` (target/active/profile names, V15 บน H2) | 7 ผ่าน |
| `StockProfileMigrationTest` | 1 ผ่าน |
| `AuthServiceTest` | 3 ผ่าน |
| `StockTransactionTemplateTest` | 3 ผ่าน |
| `BootstrapAdminConfigTest` | 2 ผ่าน |
| `DisposablePostgresDatabaseTest` (guard: loopback, ชื่อ `buffet_test_*`, comment marker) | 27 ผ่าน |

**Skipped 28 = PostgreSQL tests ทั้งหมด** (ไม่มี disposable PG): `PostgresApplicationClientAccessTest` 2, `PostgresDiningSessionMigrationTest` 1, `PostgresMenuOrderingMigrationTest` 1, `PostgresOrderCloseConcurrencyTest` 8, `PostgresPaymentIntegrationTest` 13, `PostgresStockProfileMigrationTest` 1, `PostgresStockSecurityIntegrationTest` 2

หมายเหตุ log: มี `SQL Error 23505` (duplicate `app_users.username`) และ warning ตอน `registryOrderStateResolver` สร้าง context ปรากฏใน log แต่เป็นกรณีทดสอบเชิงลบที่คาดไว้ — test เหล่านั้นผ่านทั้งหมด

### สิ่งที่ผลนี้ **พิสูจน์ได้** / **พิสูจน์ไม่ได้**

| พิสูจน์ได้ | พิสูจน์ไม่ได้ |
|---|---|
| Logic/validation Auth, Stock (target/active), Profile (ชื่อ/นามสกุล/โทรศัพท์) บน H2 ที่ HEAD นี้ | Flyway V1–V15 บน PostgreSQL จริง (RLS, GRANT/REVOKE ใน V11–V13 เป็นไฟล์ `postgresql/` ซึ่ง H2 ไม่ได้รัน) |
| Build/package ผ่าน | JPA/Hibernate validate กับ schema PostgreSQL |
| Guard ของ disposable DB ทำงาน | สถานะฐานกลาง (history, checksum, role/privilege) |

## 5. สิ่งที่ยังรอ

1. **Flyway `validate` ของ migration ใน commit นี้ เทียบฐานกลาง** (read-only, `outOfOrder=false`, ไม่ repair/migrate) — ผลต้องมา `success=true, pending=0` และ checksum V13/V14/V15 ตรงกับตารางใน `auth-stock-schema-delta.md` (V13 `419855576`, V14 `-1840534327`, V15 `-1092732854`) ผมยังไม่ได้คำนวณ checksum จากไฟล์ใน repo
2. **การอนุมัติ V15 แยกจาก V13/V14** (ตาม `auth-stock-schema-delta.md` ยังขาดหลักฐาน)
3. **PostgreSQL integration tests 28 ตัวที่ถูก skip** — ต้องรันบน disposable PostgreSQL ที่ธีรเมธ/ทีมจัดให้ (หรือเปิด Docker ในเครื่อง) พร้อมเปิด `ALLOW_DESTRUCTIVE_DB_TESTS=true` ตามกติกา guard
4. **Runtime JDBC → Flyway → JPA บนฐานกลาง** ด้วย role/connection ที่ธีรเมธกำหนดให้ backend ใช้ (ดูข้อ 6)
5. Stock target/active + Profile ใน UI/Browser runner (ตาม `test/README.md` ยัง pending จนกว่าเจ้าของโค้ดส่ง contract) — ไม่ได้รันในรอบนี้
6. ไฟล์ doc/diagram ที่ยัง uncommitted 14 ไฟล์ ต้อง commit/review ก่อนอ้างอิงเป็น SHA

## 6. ประสานธีรเมธ (runtime JDBC / Flyway / JPA)

ข้อความที่จะส่ง:

> ธีรเมธ — ขอให้ยืนยันบนฐานข้อมูลกลาง สำหรับ SHA `e6172b2` (ไฟล์ migration V1–V15 ไม่ได้ถูกแก้ใน working tree):
> 1. รัน Flyway `validate` แบบ read-only (`outOfOrder=false`, ห้าม `repair`/`migrate` เพื่อให้ผ่าน) แล้วส่งผล success/pending และ checksum V13–V15
> 2. ยืนยัน runtime ของ backend: JDBC connect → Flyway validate → JPA (Hibernate) เริ่มได้ โดยแจ้ง **role ที่ backend ใช้จริง** และสถานะ BYPASSRLS/privilege ที่ตรวจ
> 3. ส่งหลักฐานแบบ sanitized (ไม่มี password/host เต็ม): วัน-เวลา, ผู้รัน, คำสั่ง, ผลดิบ
> 4. ช่วยจัดหรืออนุญาต disposable PostgreSQL (ชื่อ `buffet_test_*`, comment `buffet-disposable-test-only`, loopback) สำหรับรัน PostgreSQL tests 28 ตัว

### ข้อห้าม (บังคับ)

- **ห้ามใช้ connector role** (รวมถึง role/credential ของ Supabase connector หรือ role ที่ใช้เชื่อมเครื่องมือภายนอก) เป็นหลักฐานแทน runtime role ของ backend หรือแทนผลตรวจฐานกลาง
- **ห้ามใช้ผล CI** (PostgreSQL 18 service ชั่วคราวใน GitHub Actions, DB `buffet_test_*_ci`) เป็นหลักฐานแทนฐานข้อมูลกลาง — CI พิสูจน์ได้เฉพาะ migration บน PostgreSQL ว่างที่ทิ้งได้
- **ห้ามชี้ automated test ไปที่ Supabase/ฐานกลาง** (ตาม `test/README.md`) และห้าม mark ฐานทีมเป็น disposable
- ห้ามแก้ migration ที่ apply แล้ว หรือ `repair` เพื่อให้ validate ผ่านโดยไม่สืบสาเหตุ

## 7. สรุปสถานะ

| หัวข้อ | สถานะ |
|---|---|
| Validation/logic Auth-Stock-Profile (H2, SHA `e6172b2`) | ✅ ผ่าน 346 tests / 0 fail |
| PostgreSQL migration + concurrency tests | ⏸ Skipped 28 (ไม่มี disposable PG / Docker ปิด) |
| Flyway validate / checksum บนฐานกลาง | ⏳ รอธีรเมธ |
| Runtime JDBC/Flyway/JPA บนฐานกลาง | ⏳ รอธีรเมธ |
| V15 approval | ⏳ รอหลักฐาน |
