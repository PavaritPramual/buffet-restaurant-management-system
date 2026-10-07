# JPA Entity Rationale

สถานะ: เอกสารอธิบาย mapping ที่ **implemented อยู่แล้ว** (ตรวจกับ `code/backend/.../domain/*.java` และ migration V1–V15) ไม่มีการเปลี่ยน mapping เพื่อให้เอกสารดูดีขึ้น
การยืนยันเหตุผลเชิงออกแบบจากเจ้าของแต่ละ Entity: Auth/Stock/Profile = เมธัส/ปวริศช์ (ในเอกสารนี้); Menu/Ordering = [sirapat-menu-ordering-solid-jpa.md](sirapat-menu-ordering-solid-jpa.md); Table/Session/Payment ยังรอเจ้าของลงชื่อยืนยัน (ระบุ "รอเจ้าของยืนยัน")
ทุก association ใช้ `LAZY` (ไม่มี EAGER); ทุก FK มี index ที่ใช้ใน query หลัก

| Entity → table | ความสัมพันธ์/cardinality | Owner side, FK, ON DELETE | Cascade / fetch | ผลต่อ query และประวัติ |
| --- | --- | --- | --- | --- |
| `UserAccount` → `app_users` | 1 : 0..1 กับ `UserProfile`; 1 : 0..* กับ `StockTransaction` (actor) | ไม่ถือ FK; username UNIQUE, role CHECK, `idx_app_users_role` | ไม่ cascade | login ค้นด้วย username (unique index); ลบ user ไม่ทำให้ประวัติสต็อกหาย (actor → NULL) |
| `UserProfile` → `user_profiles` | 1 : 1 กับ user (`@OneToOne @MapsId`, shared PK `user_id`) | Owner = profile; FK `user_id` → `app_users` ON DELETE CASCADE | ไม่ cascade จาก user; LAZY | join ด้วย PK ไม่ต้องมี index เพิ่ม; V15 เพิ่ม `first_name/last_name/phone_number` nullable เพื่อไม่เดาชื่อของข้อมูลเก่า |
| `StockItem` → `stock_items` | 1 : 0..* กับ `StockTransaction` | ไม่ถือ FK; sku UNIQUE; CHECK quantity/threshold/`opening_target_stock` ≥ 0 | ไม่ cascade | `active` เป็น flag แทนการลบ จึงรักษา FK/ประวัติ; shortfall คำนวณ (`max(target−quantity,0)`) ไม่เก็บใน DB |
| `StockTransaction` → `stock_transactions` | N : 1 กับ `StockItem` (optional=false), N : 0..1 กับ `UserAccount` | Owner = transaction; `stock_item_id` ON DELETE RESTRICT; `actor_user_id` ON DELETE SET NULL | ไม่ cascade; LAZY ทั้งคู่ | `idx_stock_transactions_item_created` รองรับ history รายสินค้าเรียงเวลา; RESTRICT บังคับให้ใช้ inactive แทนลบ; CHECK delta ≠ 0, balance ≥ 0 |
| `RestaurantTable` → `restaurant_tables` | 1 : 0..* กับ `DiningSession` | ไม่ถือ FK; table_number UNIQUE, `idx_tables_status` | ไม่ cascade | รอเจ้าของยืนยัน |
| `BuffetPackage` → `buffet_packages` | 1 : 0..* กับ `DiningSession`; M:N กับ `MenuItem` ผ่าน `package_menu_items` | ไม่ถือ FK; price > 0 | ไม่ cascade | session เก็บ `package_price_at_open` เป็น snapshot (V8) ราคาที่เปลี่ยนภายหลังไม่กระทบบิลเก่า; รอเจ้าของยืนยัน |
| `Soup` → `soups` | 1 : 0..* กับ `DiningSession` | ไม่ถือ FK | ไม่ cascade | รอเจ้าของยืนยัน |
| `MenuCategory` → `menu_categories` | 1 : 0..* กับ `MenuItem` | ไม่ถือ FK | ไม่ cascade | ดู Sirapat doc |
| `MenuItem` → `menu_items` | N : 1 กับ category (LAZY); M:N กับ package ผ่าน `@ElementCollection` (LAZY) | Owner = menu_item; FK category RESTRICT, `idx_menu_items_category`; `package_menu_items` FK ON DELETE CASCADE, `idx_package_menu_items_menu` | collection อยู่ใต้ lifecycle ของ item | order item เก็บ `item_name` snapshot จึงเปลี่ยนชื่อเมนูไม่กระทบออเดอร์เก่า |
| `DiningSession` → `dining_sessions` | N : 1 กับ table, package, soup (LAZY); 1 : 0..* grants; 1 : 0..* orders; 1 : 0..1 payment | Owner = session; FK table/package/soup ทั้งหมด RESTRICT + index; session_token UNIQUE; CHECK guest count; `idx_dining_sessions_status` | ไม่ cascade | RESTRICT กันลบข้อมูลหลักที่มี session อ้างอิง; รอเจ้าของยืนยัน |
| `CustomerSessionGrant` → `customer_session_grants` | N : 1 กับ session (LAZY) | Owner = grant; FK ON DELETE CASCADE; `token_hash` UNIQUE; `idx_customer_grants_session` | grant ตายตาม session | เก็บเฉพาะ hash ของ token; รอเจ้าของยืนยัน |
| `CustomerOrder` → `orders` | N : 1 กับ session โดย `session_id` เป็นคอลัมน์ `Long` (ไม่ใช่ `@ManyToOne`); 1 : N กับ `OrderItem` | FK ที่ DB (V7) RESTRICT, `idx_orders_session`, `idx_orders_status`; ฝั่ง JPA `@OneToMany(mappedBy="order")` | `cascade=ALL`, `orphanRemoval=true`, LAZY | บันทึกออเดอร์พร้อมรายการใน transaction เดียว; ไม่ผูก entity session จึงลด join ต่อ query แต่ต้อง join ด้วยมือ; รอเจ้าของยืนยัน |
| `OrderItem` → `order_items` | N : 1 กับ order (LAZY); `menu_item_id` เป็น `Long` | Owner = item; FK order ON DELETE CASCADE, `idx_order_items_order`; FK menu_item RESTRICT; quantity > 0 | ตาม order | snapshot `item_name` รักษาประวัติใบสั่ง |
| `Payment` → `payments` | 1 : 1 กับ session โดย `session_id` เป็น `Long` UNIQUE | FK RESTRICT; amount ≥ 0; method CHECK CASH/QR/CARD; status CHECK PENDING/PAID/FAILED | ไม่ cascade | UNIQUE บังคับ 1 session ต่อ 1 payment; ไม่ลบ session ที่มี payment; รอเจ้าของยืนยัน |

## หมายเหตุสำคัญ
- `orders.session_id`, `order_items.menu_item_id`, `payments.session_id` เป็น plain `Long` ใน JPA แต่มี FK จริงใน DB — เป็นการเลือกของเจ้าของ module ไม่ได้แก้ในงานนี้
- ความต่างระหว่าง H2 (ทดสอบ) กับ PostgreSQL (production) จัดการโดย migration แยกโฟลเดอร์ `db/migration/h2` และ `db/migration/postgresql` ส่วน `common` (V1,2,4,7,8,10,14,15) ใช้ร่วม
- V1–V14 ที่ apply กลางแล้วห้ามแก้; การเปลี่ยนใหม่ใช้ V15+ เท่านั้น
