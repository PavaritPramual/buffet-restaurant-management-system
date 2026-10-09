# R01-B design delta — Table / Package / Soup

Owner ปวริศช์ · baseline `69fb7afae06cf4230c814c0e58c4e8e13700ac3e` · 9 October 2026

รับรองเฉพาะ semantics ของ Table/Package/Soup ตาม [contract จาก PR #41](../contracts/deletion-contract.md). การ merge contract ไม่ใช่หลักฐานว่า archive runtime ทำแล้ว และไม่ได้รับรองโมดูลอื่นแทน owner

## Persistence / migration reservation

ตรวจ Supabase แบบ read-only 9 October 2026: Flyway V1–V15 success. ตามข้อตกลงที่ปวริศช์ยืนยัน **เมธัสใช้ V16, ปวริศช์ใช้ V17, ศิระพัทธ์ใช้ V18**. R01-B จึงใช้ **V17__archive_table_package_soup.sql**; ยกเลิกการจอง V16 ของ R01-B ที่บันทึกก่อนทราบข้อตกลง และไม่แก้งาน V16 ของเมธัส. ตรวจฐานกลางซ้ำก่อน apply. ไม่ reuse โค้ดหรือ commit จาก PR #39 ที่ปิด ไม่แก้ V1–V15 และไม่ apply ฐานกลางในรอบนี้

ระหว่าง V16/V18 ยังไม่เข้า develop ทดสอบไฟล์ที่มีจริง V1–V15 + V17 บนฐานใหม่ ไม่สร้าง placeholder V16/V18. หลังรวมงานของทีมต้องทวนลำดับ V1–V18 อีกครั้งก่อนรับรองฐานกลาง

เพิ่ม `archived BOOLEAN NOT NULL DEFAULT FALSE` ใน restaurant_tables / buffet_packages / soups. DTO ของสาม resource เพิ่ม boolean archived ต่อท้าย. ค่าเริ่มต้นของข้อมูลเก่าคือไม่ archived. archived แยกจาก active; package/soup archived ต้อง inactive. โต๊ะ archived ต้อง AVAILABLE และไม่มี ACTIVE session

## FK inventory ที่ตรวจจากฐานกลาง

| FK | Action | การรับรอง |
|---|---|---|
| dining_sessions.table_id → restaurant_tables | RESTRICT | รักษาทุกรอบเดิม |
| dining_sessions.package_id → buffet_packages | RESTRICT | รักษา ID / ราคา snapshot |
| dining_sessions.soup_id → soups | RESTRICT | รักษาชื่อที่ Staff อ่านผ่าน relationship |
| package_menu_items.package_id → buffet_packages | CASCADE เดิม | เป็น membership ไม่ใช่ Order history; R01-B จะไม่ hard-delete package ที่ยังมี membership และไม่ลบ membership เงียบ ๆ จึงใช้ archive เมื่อมี reference นี้ |

ไม่เปลี่ยน FK actions หรือลด RLS/grants. SQL constraint เป็น authority สำหรับ hard-delete race; flush ภายใน transaction และ ErrorResponse409 เดิมรองรับ rollback. เก็บ unique table_number ของรายการ archived ไว้ ไม่ใช้ชื่อใหม่ทับประวัติ

## Runtime / API

- DELETE204: ไม่มี reference ลบจริง; มี session history หรือ package membership เก็บออก. Repeat archive204; repeat hard-delete404
- Manager-only ทั้ง controller boundary และ service guard สำหรับ DELETE, GET /archived และ POST /{id}/restore
- Main lists ไม่คืน archived; แยก archive list. Direct master-data detail/update/active/status ของ archived ปฏิเสธ เพื่อไม่แก้สถานะผ่านทางลัด; Staff DiningSession/Billing ยังอ่าน relationship ของรอบเก่าได้
- Restore200: archived table → AVAILABLE; archived package/soup → inactive. Retry เมื่อไม่ archived คืน DTO ปัจจุบัน ไม่เปลี่ยน active/status แม้มีรอบ ACTIVE อยู่
- Open ใช้ lock โต๊ะ → package → soup และตรวจ archived/active หลัง lock. การลบใช้ lock resource เดียวกัน; ไม่ archive/delete โต๊ะที่มี ACTIVE หรือ OCCUPIED. Catalog archive ไม่เปลี่ยน DiningSession หรือราคา snapshot
- UI แยกปุ่มปิด/เปิดชั่วคราวจากลบ และมีรายการเก็บออก/คืนรายการ พร้อม confirmation และกันกดซ้ำ. ไม่เพิ่ม action นี้ให้ Stock/User/Menu ใน PR ของปวริศช์

## Gates

**แก้รีวิวธีรเมธ PR #46:** CustomerSessionPackageController เดิมเรียก operational CatalogService.getPackage จึงได้409หลัง archive และทำให้หน้า Customer โหลดไม่ครบ. แยก CustomerSessionPackageService ที่ตรวจ CustomerSessionVerifier แล้วอ่านเฉพาะ packageId ของ ACTIVE session นั้นผ่าน repository/mapper ใน read-only transaction. ไม่เปิด Catalogทั่วไปให้ archived และไม่ให้เปิดรอบใหม่ด้วยแพ็กเกจนี้. ไม่เปลี่ยน schema/API/DTO/สถานะ/locks; Billing ยังใช้ราคา snapshot. Regression ใช้ HTTP open → archive → QR exchange → package/menu/order/bill → payment → close พร้อม401/404และการปฏิเสธแพ็กเกจสำหรับรอบใหม่

DB tests บน H2/PostgreSQL ทิ้งได้; รอ reviewer ศรัณย์(contract/API), ศิระพัทธ์(UI), ธีรเมธ(Billing/history), เมธัส(migration/FK). ยังไม่ถือว่าฐานกลางหรือ public deployment ผ่าน
