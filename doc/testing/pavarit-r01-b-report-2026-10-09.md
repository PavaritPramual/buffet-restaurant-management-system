# R01-B — รายงานทดสอบ Table / Package / Soup

เจ้าของ ปวริศช์ · ตรวจ 9 ตุลาคม 2026 (ICT)

ฐาน develop `69fb7afae06cf4230c814c0e58c4e8e13700ac3e` รวม PR #41; implementation/tests [46b4ef9](https://github.com/PavaritPramual/buffet-restaurant-management-system/tree/46b4ef9fe952eabeca4f71d83136b194cadc29fd). Backend commit `9f5c9a5`; Frontend commit `46b4ef9`. เอกสารรายงานนี้เพิ่มภายหลัง โดยไม่เปลี่ยน implementation ที่ทดสอบ

## ขอบเขตและ design

- [Owner design / lock / FK](../architecture/pavarit-r01-b-design-delta.md)
- [V17 schema delta](../database/pavarit-r01-b-schema-delta.md)
- [Contract และ HTTP status migration](../contracts/deletion-contract.md)
- [ERD และ sequence delta แบบ Mermaid source/preview](../diagrams/r01-b-removal.md)

ยึดการจอง **เมธัส V16 / ปวริศช์ V17 / ศิระพัทธ์ V18**. V17 เพิ่ม archived แค่สามตาราง ไม่เพิ่ม Entity หรือลบ FK/grants ไม่แก้ V1–V15 ไม่สร้าง placeholder V16/V18 และไม่ได้ apply/repair Supabase กลาง

ข้อมูลไม่มี reference ลบจริง; มีประวัติหรือ package membership เก็บออก. รายการหลักไม่แสดง archived; Manager มีรายการเก็บออกและคืนรายการ. คืนครั้งแรกเป็น table AVAILABLE หรือ package/soup inactive; retry ไม่เปลี่ยนสถานะปัจจุบัน. ปิด active ชั่วคราวยังแยกจาก archive

## Environment

Windows, Java 21.0.11, Maven 3.9.16. Automated tests ปิด `spring.config.import` ไม่อ่าน `.env` ใช้ H2 แยก และ PostgreSQL 18 container ผูกเฉพาะ loopback พร้อมฐาน marker ตาม `disposable-postgres-ci.sql`. Runtime ใช้ fresh H2 ใน local-regression, session/database providers จริงและไม่ seed fixture. บัญชี/credentials เป็นของฐานทิ้งได้และไม่เก็บในหลักฐาน

## Automated results

| ตรวจ | ผลจริง |
|---|---|
| Backend `mvn --batch-mode --no-transfer-progress -Dspring.config.import= clean verify` | **367 passed, 0 failures/errors/skipped** รวม PostgreSQL และ Testcontainers suites |
| `MasterDataArchiveMigrationTest` ที่เพิ่มหลัง full suite: `mvn --batch-mode --no-transfer-progress -Dspring.config.import= -Dtest=MasterDataArchiveMigrationTest test` | **1 passed, 0 failures/errors/skipped**; upgrade H2 V15 → V17 รักษา legacy rows/default false และปฏิเสธ unsafe archive state |
| `MasterDataRemovalIntegrationTest` ใน full suite | **5 passed** ใช้ session auth/database Billing/Payment จริง; hard-delete, history, package membership, auth และ retry restore |
| `PostgresMasterDataRemovalConcurrencyTest` ใน full suite | **7 passed** ใช้ PostgreSQL row locks จริงและ controlled auth mock; open/delete/archive แข่งกัน และ FK failure rollback |
| Frontend `npm test` หลังแก้ครบ | **157 passed / 17 files** |
| Frontend `npm run lint` | exit 0; **4 warnings เดิม** set-state-in-effect ใน StockPage, UsersPage, KitchenBoardPage, StaffServingPage; ไม่มี warning ใหม่ใน MasterDataPage |
| Frontend `npm run build` | exit 0 |
| `mvn -Dspring.config.import= validate` | ผ่านก่อนจัดทำ diagram delta |

จำนวน Backend ไม่ใช่ full suite 368 ในครั้งเดียว: เป็น full 367 และ focused upgrade 1 ตามเวลาที่รันจริง. Logs ใน `test/reports/r01-*.log` เป็น ignored local diagnostics; ไม่ส่ง raw runtime logs ที่อาจมีข้อมูลสิทธิ์. CI ของ PR ต้องรันอีกครั้งบน head ที่ push

PostgreSQL ใช้ env `ALLOW_DESTRUCTIVE_DB_TESTS=true`, MENU/DINING/PAYMENT_TEST_PG_URL ที่ loopback และฐาน `buffet_test_*_ci`. Payment ใช้ role `buffet_backend_test`; fixture authorization ใน concurrency suite ไม่ใช่หลักฐาน production login. HTTP/H2 integration tests แยกพิสูจน์ Manager-only, 401/403 และ spoofed header ไม่เพิ่มสิทธิ์

Frontend tests เพิ่ม confirmation/กันกดซ้ำ, archived list/restore, error/occupied guard รวมการรักษา `/stock/items` และ reset state เมื่อเปลี่ยนชนิดข้อมูล เพื่อไม่ให้ archive UI กระทบหน้าสต็อก

## Local browser / HTTP runtime

ตรวจผ่าน browser จริงบน Vite + API fresh H2 ก่อน commit; รูปไม่มี QR token/cookie/password

1. Manager 1280px เก็บแพ็กเกจของ ACTIVE session: หายจากรายการหลักและยังอยู่รายการเก็บออก
2. คืนแพ็กเกจ: กลับรายการหลักแบบ inactive ไม่เปิดใช้งานอัตโนมัติ
3. โต๊ะ OCCUPIED ปิดปุ่มแก้และลบ; backend guard ตรวจซ้ำ ไม่ใช้ UI เป็น authority
4. HTTP cookie jars แยก Manager/Staff/Customer: เก็บ package/soup เดิมออก → แลก QR → ขอคิดบิล **747.50** → Payment CASH **747.50** → close **COMPLETED**. ไม่ใช่ payment gateway จริง
5. Staff 768px เปิดรายละเอียดรอบ COMPLETED: ชื่อ **บุฟเฟต์มาตรฐาน / น้ำซุปต้มยำ** ยังอ่านได้; QR ใช้ไม่ได้หลังปิด

API/runtime flow ใช้โค้ด backend เดียวกับ commit `9f5c9a5`. ภาพ Manager/Staff เก็บก่อน frontend guard เพิ่มเติมใน `46b4ef9`; guard `/stock/items` และ reset เมื่อเปลี่ยนหน้าได้รับ regression tests ในชุด 157 แล้ว ไม่อ้างว่าเป็น public acceptance หรือทวนมือถือจริง

### ภาพ

![Manager archive 1280px](../../test/evidence/pavarit-r01-b-2026-10-09/manager-archive-1280.png)

![Restored inactive 1280px](../../test/evidence/pavarit-r01-b-2026-10-09/manager-restored-inactive-1280.png)

![Occupied table guard 1280px](../../test/evidence/pavarit-r01-b-2026-10-09/manager-occupied-table-1280.png)

![Closed Staff history 768px](../../test/evidence/pavarit-r01-b-2026-10-09/staff-closed-history-768.png)

## Gates ที่ยังรอ

- Review API/status delta, UI, Billing/history และ migration/FK; PR merge
- V16/V18 รวมแล้วทวน full V1–V18 ใหม่; history/checksum ฐานกลางและอนุมัติ V17 แยก
- Deploy approved SHA และ public regression/Final. ผลฐานทิ้งได้/fixture/CI ไม่แทน gate นี้
- Menu/Category/Stock/User removal เป็นของเจ้าของพื้นที่ ไม่รับรองแทนจาก PR นี้

Diagram delta เป็น Mermaid ที่มี source ฝังใน Markdown ไม่อ้างว่า render PlantUML/SVG baseline ใหม่. baseline diagrams เก็บเป็นประวัติ; การรับรองทั้งระบบยังรอทุก owner
