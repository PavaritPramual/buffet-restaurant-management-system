# Data Dictionary / JPA delta — V19

ปวริศช์ · 10 ตุลาคม 2026 · Supabase project `zbflfljthmzqhshahemv` ตรวจ read-only พบ V1–V18 success=true ไม่มี V19 ก่อนลงมือ ข้อความนี้ยืนยัน history ไม่ใช่ checksum validation ของฐานกลาง V19 เตรียมใน PR ยังไม่ apply กลาง

| ตาราง/column | ชนิดและกฎ | ความสัมพันธ์ / JPA |
|---|---|---|
| menu_items.automatic_stock_deduction | BOOLEAN NOT NULL DEFAULT FALSE | เมนูเก่าไม่ถูกเปิดอัตโนมัติ; สูตรอย่างน้อยหนึ่งรายการตรวจใน Service |
| menu_stock_usage.menu_item_id | BIGINT PK คู่กับ stock_item_id; FK menu_items ON DELETE CASCADE | MenuItem เป็น owner ของ LAZY ElementCollection; parent menu ลบได้ตาม guard เดิม |
| menu_stock_usage.stock_item_id | BIGINT FK stock_items ON DELETE RESTRICT | scalar ID ไม่มี JPA cascade ไป StockItem; index idx_menu_usage_stock |
| menu_stock_usage.quantity_per_serving | DECIMAL(12,3) NOT NULL CHECK > 0 | หน่วยเดียวกับ Stock ไม่แปลงหน่วย; API ปฏิเสธเกินสามทศนิยม |
| menu_stock_usage.stock_unit | VARCHAR(24) NOT NULL | เก็บหน่วยตอนกำหนดสูตร; Service ห้ามเปลี่ยน Stock unit เมื่อมีสูตร/Order อ้างอิง |
| order_item_stock_usage.order_item_id | BIGINT PK คู่กับ stock_item_id; FK order_items ON DELETE CASCADE | OrderItem เป็น owner ของ LAZY ElementCollection แยกจากสูตรปัจจุบัน |
| order_item_stock_usage.stock_item_id | BIGINT FK stock_items ON DELETE RESTRICT | index idx_order_usage_stock; ไม่ cascade ลบ Stock |
| order_item_stock_usage.quantity_per_serving / stock_unit | DECIMAL(12,3) > 0 / VARCHAR(24), NOT NULL | คัดลอกสูตรตอนสั่ง; ปริมาณใช้จริงเท่ากับต่อเสิร์ฟ × OrderItem.quantity |
| stock_transactions.transaction_type | เพิ่ม CONSUMPTION ใน CHECK เดิม | Java enum string; IN/ADJUSTMENT เดิมยังใช้ได้ |
| stock_transactions.order_id | BIGINT nullable FK orders ON DELETE RESTRICT | scalar ID ไม่มี Order association/cascade; UNIQUE(order_id,stock_item_id) เป็น index สำหรับ audit และกันหักซ้ำ |
| chk_consumption_order | CONSUMPTION ต้องมี order_id และ negative delta; manual types ต้อง order_id NULL | ไม่เขียน audit ที่ไม่มี Order; transaction failure rollback status/balance |

ElementCollection เป็น value ownership และจัดการลบแถว collection โดย Hibernate ไม่มี `cascade=` option แบบ Entity association ส่วน SQL CASCADE เป็นคนละกลไกกับ JPA CascadeType และไม่มีการ cascade จากสูตรไป Stock/Order parents

StockService จะ archive รายการที่มีสูตรหรือ snapshot อ้างอิง แม้ยอดศูนย์และไม่มี movement รายการที่ไม่มีการอ้างอิงและไม่เคยมี movement ยังลบจริงได้ตาม contract เดิม Stock archived/inactive ใช้ทำอาหารไม่ได้จน restore/activate โดย Manager

V19 PostgreSQL เปิด RLS สองตารางใหม่ revoke PUBLIC/anon/authenticated และให้ backend role ที่รัน migration พร้อม policy postgres ที่ใช้จริง ตารางไม่มี generated sequence ใหม่ `postgres` มี BYPASSRLS จึงยังต้องตรวจสิทธิ์ที่ API ไม่อ้างว่า policy จำกัด postgres ได้

H2 มีโครงสร้าง FK/CHECK/unique เดียวกัน ไม่มี policy PostgreSQL-only Tests PostgreSQL ใช้ marked disposable loopback database ไม่ใช้ connector role หรือ fixture แทน runtime permission checks

แผนขึ้นกลางหลังผ่าน review: ตรวจ V19 ว่างและ V1–V18 checksum ด้วย runtime connection, สำรองตามกระบวนการทีม, ขออนุมัติ V19, apply ผ่าน Flyway ของแอป, validate/JPA, deploy commit ที่ merge และทวน recipe → kitchen → history บน public การ rollback ไม่ย้อนลบ V19 หรือประวัติ consumption ให้ปิด automatic deduction สำหรับเมนูใหม่และใช้ forward fix ตามเหตุจริง

[H2 migration](../../code/backend/src/main/resources/db/migration/h2/V19__menu_stock_consumption.sql) · [PostgreSQL migration](../../code/backend/src/main/resources/db/migration/postgresql/V19__menu_stock_consumption.sql) · [Design decision](../architecture/menu-stock-consumption.md)
