# R01-B schema delta — V17 (not applied to Supabase)

Owner ปวริศช์ · 9 October 2026 · [design/FK inventory](../architecture/pavarit-r01-b-design-delta.md)

Team reservation confirmed by Pavarit: **Methus V16 / Pavarit V17 / Sirapat V18**. Only V17 is authored here. Shared Flyway history read back V1–V15 success; it was read only. Changes must be reviewed before separate shared-database approval.

| Table / field | SQL / Java / JSON | Default/null | Meaning |
|---|---|---|---|
| restaurant_tables.archived | BOOLEAN / boolean / boolean | FALSE / NOT NULL | Removed from operational lists; retain old sessions and unique table_number |
| buffet_packages.archived | BOOLEAN / boolean / boolean | FALSE / NOT NULL | Removed from selection; distinct from temporary active=false |
| soups.archived | BOOLEAN / boolean / boolean | FALSE / NOT NULL | Removed from selection; distinct from temporary active=false |

Check constraints require archived table status AVAILABLE and archived package/soup inactive. No old row is deleted; no columns, associations, timestamp/price types, FK actions, indexes, grants, RLS policies or sequences are changed. No new application table is exposed.

Forward file: `code/backend/src/main/resources/db/migration/common/V17__archive_table_package_soup.sql` is portable H2/PostgreSQL SQL. Main lists exclude archived even without active/status filters. Manager-only archive endpoints return the same resource DTO with an additive `archived` flag. Direct master-data changes to archived rows require restoring first; old Staff session details keep names and Billing keeps opening-price snapshot.

`package_menu_items` has a pre-existing CASCADE FK to package. To avoid silent membership removal, the service archives a package with any membership even when it has no session history. Session FKs are RESTRICT in the shared DB; flush and existing 409 ErrorResponse protect unexpected hard-delete FK conflicts.

See [API contract](../contracts/deletion-contract.md), [diagram delta](../diagrams/r01-b-removal.md), and final report. After V16/V18 merge, rerun the full ordered V1–V18 chain on a fresh DB and validate the approved shared history before public acceptance. Do not invent placeholder migrations or leave outOfOrder enabled as a workaround.

## ยืนยันหลังรีวิวเมธัส

ลำดับรวมงานและฐานกลางคือ **V16 ก่อน V17**; V18 ตามเลขที่ทีมจอง การทดสอบ branch นี้บนฐานทิ้งได้ซึ่งยังไม่มีไฟล์ V16 ไม่ใช่การอนุมัติให้ apply V17 ก่อน V16 บน Supabase หาก V16/V18 ยังไม่รวม ต้องเว้น gate ทวน full V1–V18 และการ apply ฐานกลางไว้ ไม่มีการใช้ Flyway repair/outOfOrder เพื่อข้ามขั้น

MenuOrdering migration tests ตรวจเวอร์ชันที่จำเป็นเป็น ordered subsequence พร้อม Flyway validate และ pending ว่าง จึงรับไฟล์ V16/V18 ที่เพิ่มได้โดยยังตรวจว่า baseline ไม่หายหรือเรียงผิด H2 archive upgrade test ตรวจว่า V17 ถูก apply แทนบังคับให้เป็นเวอร์ชันสุดท้าย

PostgresMasterDataArchiveMigrationTest ใช้ PostgreSQL container ใหม่ ตรวจ legacy rows ก่อน V17 มี archived=false หลัง upgrade, CHECK ทั้งสามปฏิเสธ unsafe archive และยอมรับ state ที่ถูกต้อง ตรวจ exception จาก CHECK/FK จริงผ่าน test-only HTTP probe กับ GlobalExceptionHandler เดิมได้ ErrorResponse 409 ไม่ใช่ 500 ไม่เพิ่ม endpoint production และไม่ใช่หลักฐาน auth/public acceptance

ข้อสังเกต: table_number ของโต๊ะที่เก็บออกยังถูกจองตาม UNIQUE เดิม ต้องคืนรายการเดิมก่อนใช้เลขเดิม ไม่ทำลายประวัติเพื่อให้สร้างเลขซ้ำได้ รอบนี้ไม่เปลี่ยน unique constraint หรือสัญญาการลบ
