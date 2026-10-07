# PR #24 — แก้รีวิวเอกสารและ diagram

ตรวจวันที่ 7 ตุลาคม 2026 จาก `develop 472fba4` และ PR head ก่อนแก้ `bf32e11` ไม่มีการแก้ runtime/API/DTO/Entity/migration

## รายการแก้

| ข้อทัก | ข้อเท็จจริงและการแก้ | จุดตรวจ |
|---|---|---|
| ลิงก์ login | หน้า login อยู่ใน AdminShell ที่ `/admin` ไม่ใช่ `/login` แก้ README ทั้งลิงก์ Compose และ flow Manager | [routes](../../code/frontend/src/FoundationApp.tsx), [AdminShell](../../code/frontend/src/features/admin/AdminShell.tsx) |
| Use Case alias ซ้ำ | แยก CustomerMenu กับ AdminMenu และ actor edges ทำให้การดู package menu ของ Customer ไม่ถูกรวมกับ Manager menu | [source](../diagrams/use-case.puml), [SVG](../diagrams/previews/use-case.svg) |
| User/Profile อ้างเกินโค้ด | ระบุ list/create user กับ basic `displayName`/`email` ไม่มี edit/delete user หรือ full profile management; แก้ Use Case เต็ม/Manager/ภาพย่อและ Description | [AdminUserController](../../code/backend/src/main/java/com/buffetrestaurant/controller/AdminUserController.java), [AuthService](../../code/backend/src/main/java/com/buffetrestaurant/service/AuthService.java) |
| Auth cardinality | UserAccount มี UserProfile ได้ 0..1 จาก DB shared PK/FK; createUser สร้างทั้งคู่ และ authenticate ต้องมี profile เป็นกฎเฉพาะ use case ไม่ใช่ constraint บังคับทุก user | [class source](../diagrams/class-auth.puml), [SVG](../diagrams/previews/class-auth.svg), [V10](../../code/backend/src/main/resources/db/migration/common/V10__create_user_and_stock_tables.sql) |
| Supabase V13/V14 | เพิ่ม SELECT readback สดและ checksum/schema/privileges แทนการอ้างรายงานเก่าที่จบก่อน deployment | [หลักฐาน JSON](../../test/evidence/pavarit-step3-docs-2026-10-07/pr24-central-readback.json) |

## หลักฐาน Supabase V13/V14

อ่าน `flyway_schema_history` ผ่าน Supabase connector ของ project `zbflfljthmzqhshahemv` แบบ SELECT-only ไม่อ่านบัญชี/ข้อมูลธุรกรรม และไม่รัน migrate/repair/DDL

- V13 `restrict remaining client database access` success=true, checksum `419855576`, installed_on `2026-10-06 09:48:03.694447` ตามค่าที่ DB คืนมา
- V14 `add session bill request` success=true, checksum `-1840534327`, installed_on `2026-10-06 09:48:04.664729` ตามค่าที่ DB คืนมา
- V1–V14 ครบ; V9 มี installed_rank=11 หลัง V10/V11 ไม่ใช้ installed_rank แทน version
- เปรียบเทียบ checksum ทั้ง 14 ไฟล์แบบ Flyway SQL CRC32 บน UTF-8 ทีละบรรทัด โดยไม่รวม newline/BOM ตรงทั้งหมด เก็บ file/database checksum รายเวอร์ชันใน JSON
- `dining_sessions.bill_requested_at` เป็น nullable timestamp with time zone
- `restaurant_tables` เปิด RLS; client/PUBLIC table grants ใน public schema ไม่มี และ `anon`/`authenticated` ไม่มี SELECT ของ tables/sessions/Flyway history หรือ USAGE ของ table sequence

หลักฐานนี้ยืนยัน metadata ของฐานกลาง ณ เวลาที่เก็บ ไม่ใช่การรับรอง app JDBC role, JPA startup validation หรือ public Core Flow รอบใหม่ งาน Final ยังต้องตรวจ release runtime อีกครั้ง ผล validation ของฐานทดสอบ/CI และผลประวัติไม่ถูกนำมาอ้างเป็น live deployment ใหม่

## Cleanup และสถานะสไลด์

- Git audit refresh หลัง fetch แบบ snapshot **ก่อน commit แก้รีวิว** head `bf32e11` ของปวริศช์ แยก `8d4ca99` ในส่วนประวัติไว้ชัด Counts=53/22/3/8/5 เป็น candidates ไม่รับรอง meaningful ด้วยจำนวนล้วน
- Canva เป็นต้นฉบับ เก็บ PPTX/PDF ที่ export จาก Canva ตามเวอร์ชัน ข้อความแนวทางเก่าคงอยู่เฉพาะส่วนประวัติ
- สไลด์เป็นฉบับล่วงหน้า ยังไม่รับรอง รอปรับเนื้อหา/ลำดับ/ช่วงผู้พูดหลังโค้ดทีมเสร็จ รอบนี้แก้เฉพาะข้อเท็จจริง ไม่เปลี่ยนลำดับและผู้พูด
- ผล render/readback รอบนี้บันทึกเพิ่มแยกจาก evidence v02 เดิม ไม่แก้ evidence เก่าให้เสมือนตรวจ design ใหม่แล้ว

## ผลตรวจ

- Render PlantUML 4 ชุดที่เปลี่ยนผ่าน ตรวจ XML ของ SVG ทั้ง28ภาพ ไม่พบ syntax error หรือ alias ซ้ำ
- Local layout ทั้ง76หน้าผ่าน ฟอนต์ Sarabun/JetBrains Mono โหลดได้ เปิดตรวจภาพที่เปลี่ยนหน้า2/24/68/70 แล้ว — [2](../../test/evidence/pavarit-step3-docs-2026-10-07/pr24-draft-page-2.png), [24](../../test/evidence/pavarit-step3-docs-2026-10-07/pr24-draft-page-24.png), [68](../../test/evidence/pavarit-step3-docs-2026-10-07/pr24-draft-page-68.png), [70](../../test/evidence/pavarit-step3-docs-2026-10-07/pr24-draft-page-70.png)
- นำเข้า [Canva v02b ฉบับล่วงหน้า](https://www.canva.com/d/yWw6P3disBOSfHS) แยกจาก v02 เดิม อ่านกลับ76หน้า/76notes/704text elements/28images ชื่อหน้า ลำดับ ผู้พูด และโค้ด35หน้าตรงเดิม Notes4หน้าที่แก้ตรง source ไม่พบ elements ออกนอก canvas ปิด inspection transaction โดยไม่ save changes
- Code snippets35ชุดตรง Git baseline `472fba4`; local Markdown links ที่เกี่ยวข้องผ่าน ดูจำนวนและผลรายหน้าใน [verification JSON](../../test/evidence/pavarit-step3-docs-2026-10-07/pr24-design-verification.json)
- `git diff --check` ผ่าน งานเอกสารไม่มี runtime/migration change ไม่รัน backend/frontend suites ในเครื่องซ้ำ แต่ CI ของ commit ที่ push ต้องตรวจแยก ไม่อ้าง baseline CI แทนผล commit นี้
- Native Canva visual QA/owner approval/PPTX/PDF export/rehearsal/public/release ยังไม่รับรอง ไม่ติ๊ก Final หรือ merge PR จากผลเอกสาร
