# R01-A — Menu archive delta for owner review

วันที่ 9 ตุลาคม 2026 · เจ้าของ Entity/UI: ศิระพัทธ์ · ฐาน `develop 69fb7af` (merge PR41)

**Owner design/schema delta สำหรับ review ก่อน merge/apply ฐานกลาง**
ผู้ใช้ยืนยันให้ทำ R01-A ตาม Notion ต่อ แยกจาก PR39 force close/menu removal ที่ปิดโดยไม่ merge
อ้างอิง [งาน R01-A](https://app.notion.com/p/FIX-R01-A-Manager-3f4cb2e9d47a81a8a0aee08a0f60f68d),
[Step FIX](https://app.notion.com/p/Step-FIX-UAT-Defects-Manager-Data-Management-3f4cb2e9d47a81d1ae9fd42e77d189c9),
และ [PR41 ที่ merge แล้ว](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/41)

## สิ่งที่ตรวจแล้วและสิ่งที่ต้องรับรอง

PR41 แก้ข้อสังเกต UI เดิมแล้ว: `CATEGORY_HAS_ITEMS` สอดคล้องกับการตรวจทุกเมนู รวม unavailable,
ข้อความบอกให้ย้ายหมวดตามพฤติกรรมที่ทำได้จริง และเพิ่ม regression ของ unavailable-only category
PR41 head 634bfa1 merge เป็น 69fb7af แล้ว รวมกฎ category ที่มี archived children และ restore retry
ผู้ใช้ให้ทำ implementation ต่อระหว่างรอเลข แล้วพบ [PR46/schema delta ของปวริศช์](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/46)
ระบุเลขทีม **เมธัส V16 / ปวริศช์ V17 / ศิระพัทธ์ V18** จึงใช้ V18 ตามประกาศนี้
ไม่มี placeholder V16/V17 และไม่มีการ apply/repair ฐานกลาง; ก่อน deploy ต้องรวมและทวน chain V1–V18 ตามลำดับ
การอ่าน FK ฐานกลางรอบนี้ยังทำไม่ได้เพราะไม่มีไฟล์ connection configuration ใน checkout จึงส่ง inventory จาก immutable migrations ให้เมธัส/ปวริศช์เทียบฐานจริงก่อนอนุมัติ shared apply

## Menu-specific decisions implemented for review

| หัวข้อ | พฤติกรรมที่ขอให้ศรัณย์/ปวริศช์/เมธัสรับรอง |
|---|---|
| State | V18 เพิ่ม nullable `archived_at` ใน `menu_items` และ `menu_categories`; `NULL` คืออยู่ในรายการใช้งาน แยกจาก `available=false` |
| Main lists | Menu main page และ categories ไม่คืน archived rows; `GET /{id}` ปกติไม่ใช้แก้/สั่งรายการเก็บออก |
| Menu removal | DELETE 204: ไม่มี Order/history และไม่มี package membership ให้ hard-delete; มี reference อย่างใดอย่างหนึ่งให้ archive + unavailable โดยเก็บ ID, package links และ OrderItem snapshots ไม่ปล่อย CASCADE ลบ link เงียบ ๆ |
| Category removal | มีเมนู **ที่ไม่ archived** แม้ unavailable ก็ conflict; ไม่มี child ใดเลยให้ hard-delete; มีแต่ archived children ให้ archive หมวดโดยเก็บ FK ไม่ cascade |
| Category conflict | R01-A เปลี่ยน runtime เฉพาะ category child guard จาก U03 400 เป็น 409 ตาม R01 target พร้อมข้อความไทยและ regression tests; shape ErrorResponse เดิม |
| Restore menu | ต้องคืนหมวดก่อนถ้าหมวด archived; ครั้งแรกเคลียร์ archived_at และตั้ง unavailable; restore ซ้ำคืน DTO ปัจจุบันโดยไม่ปิดขายรายการที่เปิดกลับแล้ว |
| Restore category | คืนเฉพาะหมวด ไม่คืน children อัตโนมัติ; retry ไม่เปลี่ยนสถานะเมนู |
| Names | ชื่อ archived rows ยังถูก service duplicate validation จองไว้เหมือนปัจจุบัน ไม่เสนอ partial unique/name reuse โดยเงียบ; schema V4 ไม่มี unique name constraint ต้องไม่อ้างว่ามี |
| DTO | Main/archived list และ restore ใช้ fields MenuItemResponse/MenuCategoryResponse เดิม; route ระบุ archive scope ไม่เพิ่ม archivedAt ใน JSON โดยไม่จำเป็น |
| Access | DELETE, /archived, /restore บังคับ Manager ที่ controller/provider และ service; ไม่ใช้ UI เป็น security boundary |
| Orders | Customer menu query exclude ทั้ง item/category archived; POST stale-cart ตรวจอีกครั้งภายใต้ lock ก่อนสร้าง order; ไม่ยอมรับออเดอร์ใหม่หลัง archive สำเร็จ |

DELETE ข้อความยืนยันระบุทั้งลบถาวรสำหรับรายการไม่เคยใช้ และเก็บรายการที่มีประวัติ
ข้อความสำเร็จต้องไม่อ้างว่าลบ Order history; restore ไม่เรียก activation ให้อัตโนมัติ

## Repository FK inventory (ยังไม่ใช่การรับรองฐานกลาง)

อ่านจาก immutable V4 และ JPA ปัจจุบัน:

| Reference | Repository rule | การรักษา |
|---|---|---|
| menu_items.category_id → menu_categories.id | RESTRICT | category ที่เหลือ archived children ต้อง archive แทนลบ |
| order_items.menu_item_id → menu_items.id | RESTRICT | ไม่แก้ ไม่ SET NULL ไม่ cascade; item_name snapshot เดิมคงอยู่ |
| package_menu_items.menu_item_id → menu_items.id | CASCADE บน configuration link | เก็บออกเมื่อมี membership แม้ไม่มี Order history เพื่อรักษา link; ลบจริงเมื่อไม่มี link และไม่มี history เท่านั้น |
| package_menu_items.package_id → buffet_packages.id | CASCADE บน owned configuration link | ไม่เปลี่ยน package behavior ใน R01-A |
| order_items.order_id → orders.id | CASCADE | R01-A ไม่เพิ่ม endpoint ลบ orders และไม่เปลี่ยน constraint นี้ |

เมธัส/ปวริศช์ต้องเทียบ FK จริง/Flyway history กับ inventory นี้ก่อนอนุมัติ shared apply
ไม่ใช้ source inventory หรือ disposable CI เป็นหลักฐานว่าฐานกลางตรง

## Allocated forward-only schema delta — V18

| Table | Column/type/default | Query index |
|---|---|---|
| menu_items | archived_at TIMESTAMP WITH TIME ZONE NULL, default NULL | PG partial indexes สำหรับ main/archive; H2 supporting index ที่ archived_at |
| menu_categories | archived_at TIMESTAMP WITH TIME ZONE NULL, default NULL | PG partial indexes สำหรับ main/archive; H2 supporting index ที่ archived_at |

ไม่มี backfill/ล้างข้อมูลเดิม ไม่มี rename/drop history/FK/RLS; ไม่ใช้ boolean available เป็น archive state
ใช้ `V18__archive_menu_catalog.sql` แยก H2/PostgreSQL ตาม vendor location โดยมี version เดียวในแต่ละ environment
ไม่เพิ่มไฟล์ `V16`/`V17` เอง ไม่คัดลอก migration จาก PR39 และลบ schema fixture ชั่วคราวออกแล้ว
H2 ทดสอบ upgrade จาก V15 ที่มี Order history พร้อม Flyway validate; PostgreSQL migration/metadata/RLS และ row-lock races ให้ CI ตรวจบน disposable DB

## Transaction/race implementation

- Order path ล็อก MenuItem ร่วมกับ archive ก่อนตรวจ availability/archive/package; คืน response items ตามลำดับคำขอเดิม
- DELETE/restore/update item ใช้ pessimistic item lock เดียวกับ Order ที่ล็อก item ID เรียงจากน้อยไปมากก่อนตรวจ availability/archive/package เพื่อกัน deadlock จาก order หลายเมนู
- Category create/move/archive serialize parent change กับ child insertion; lock order category IDs เรียง → item; Order ไม่ล็อกย้อน item → category ถ้ามีการย้ายหมวดแข่งหลังอ่าน ID ให้ 409 และ retry โดยไม่ถือ lock ผิดลำดับ
- เมื่อตรวจ FK แล้วมี reference แข่งแทรก ให้ DB constraint เป็น authority และ rollback 409 ทั้ง transaction ไม่มี partial write; ไม่จับ integrity exception แล้วทำ archive ใน transaction ที่ถูก rollback-only แล้ว
- Archive → restore → explicit available=true → repeated restore ต้องรักษา available=true; category restore retry ต้องไม่คืน children

## Verification coverage and release gates

1. Manager/anonymous/ทุก role ที่ไม่ใช่ Manager รวม service-level enforcement
2. unused hard-delete/404 retry, referenced archive/204 retry, main/archive pagination filters
3. unavailable-only category blocked, all-archived-child category archived, item restore blocked while parent archived
4. ประวัติ Order/Bill/Payment และ package links คงเดิมหลัง archive/restore
5. Customer stale cart ถูกปฏิเสธโดยไม่มี order ใหม่; ordering/archive/create/move races บน disposable PostgreSQL
6. restore → activate → repeat restore คงสถานะ, loading/error/confirmation/double-click และ refresh failure ไม่ส่ง write ซ้ำ

Manager route เชื่อม gateway กับ API จริงแล้ว; UI tests ใช้ gateway จำลองเฉพาะทดสอบ states ไม่ถือเป็นหลักฐาน backend
ผลจริงและภาพ browser อยู่ในรายงาน R01-A แยก local / CI / public อย่างชัดเจน
ยังต้อง review design/schema/status delta และ merge ก่อน apply/deploy; ต้องทวน full chain V1–V18 เมื่อรวม PR45/46 และ public SHA ที่รวมจริงก่อนปิด Step FIX
