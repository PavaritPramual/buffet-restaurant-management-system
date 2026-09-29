# Data Dictionary (design baseline)

ต้นทาง: [Data Dictionary & Migration ใน Notion](https://app.notion.com/p/3d8cb2e9d47a81d9aa4cdcec37b26c9d) · ย้ายเมื่อ 28 กันยายน 2026

เอกสารนี้อธิบาย **แบบออกแบบ** เดิมของ 14 ตาราง และส่วนต่างที่ implement ใน PR #16 ส่วน SQL DDL รวมก้อนใน Notion เป็น reference เท่านั้น การเปลี่ยนฐานข้อมูลจริงต้องใช้ Flyway migration ตามลำดับใน [backend](../../code/backend/src/main/resources/db/migration) ตรวจ [schema delta](menu-ordering-schema-delta.md) เมื่อเทียบ Menu/Ordering กับแบบออกแบบ

การตัดสินใจเพิ่มเติมสำหรับ Billing: `package_price_at_open` เก็บราคาแพ็กเกจ ณ เวลาเปิด Dining Session เพื่อไม่ให้การแก้ราคา Package ภายหลังเปลี่ยนยอดของลูกค้าที่กำลังกินอยู่ การเพิ่มคอลัมน์นี้ใช้ Flyway V8 หลัง V6/V7; แถวเดิมที่มีอยู่ก่อน V8 จะ backfill ด้วยราคา Package ณ เวลาย้ายข้อมูล ซึ่งไม่สามารถย้อนหาราคาตอนเปิดรอบจริงได้

ส่วนต่างจากแบบออกแบบ ณ PR #16: V6 เพิ่ม `customer_session_grants` เพื่อเก็บ hash ของ credential ลูกค้า, เวลาออกและหมดอายุ และ FK ไปยังรอบกินที่ลบ grant ตามรอบ; QR token ใน `dining_sessions` ใช้แลกได้ครั้งเดียวแล้วหมุนค่าใหม่ V6 บังคับ `adult_count + child_count >= 1` และเพิ่ม index บน FK ทั้งสามของรอบกิน V7 ใช้ `ON DELETE RESTRICT` สำหรับ `orders.session_id` เพื่อรักษาประวัติ Order แทน `CASCADE` ใน SQL แบบเดิม ส่วน `orders.session_id` มี index จาก V4 อยู่แล้ว

`customer_session_grants` ใน V6 มี `id` BIGINT PK, `session_id` BIGINT FK, `token_hash` VARCHAR(64) UNIQUE, `created_at` และ `expires_at` แบบ TIMESTAMP WITH TIME ZONE; hash นี้เป็น credential ใน cookie ไม่ใช่ QR token และ grant หลายรายการผูกกับรอบกินเดียวได้ ส่วน V8 เพิ่ม `dining_sessions.package_price_at_open` เป็น DECIMAL(10,2) NOT NULL เพื่อใช้คิดบิลตามราคา ณ เวลาเปิดรอบ

Backend ต่อ Supabase ด้วย role `postgres` ซึ่งมี `BYPASSRLS`: policy ใน V6 ระบุ role นี้และปิดสิทธิ์ `PUBLIC`, `anon`, `authenticated` บนตารางและ sequence ใหม่ แต่ `FORCE RLS` ไม่จำกัด `postgres` ได้ในสถาปัตยกรรมนี้ API จึงต้องตรวจสิทธิ์เอง งานสร้าง application role ที่ไม่มี `BYPASSRLS` ต้องออกแบบแยกต่างหาก วันที่ 29 กันยายน 2026 พบว่า V6–V8 ถูก apply บน Supabase ส่วนกลางแล้วโดยไม่ตั้งใจ; ขณะตรวจไม่พบแถวใน `dining_sessions` ห้ามแก้ migration ที่ apply แล้วย้อนหลัง และต้องตรวจ `flyway_schema_history` ก่อน migration ถัดไป

เอกสารพจนานุกรมข้อมูล (Data Dictionary) และโค้ดสำหรับสร้างฐานข้อมูล (SQL DDL Migration Script) ครบทั้ง 14 ตาราง
---
## 1. Data Dictionary (พจนานุกรมข้อมูลรายตาราง)
### 1.1 `users` (ตารางบัญชีผู้ใช้งาน)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสประจำตัวผู้ใช้ |
| `username` | VARCHAR(50) | UNIQUE, INDEX | NO | - | ชื่อผู้ใช้งานสำหรับ Login |
| `password` | VARCHAR(255) | - | NO | - | รหัสผ่านเข้ารหัส BCrypt |
| `role` | VARCHAR(30) | - | NO | - | บทบาทผู้ใช้ (`SERVICE_STAFF`, `KITCHEN_STAFF`, `SUPERVISOR`, `MANAGER`) |
| `active` | BOOLEAN | - | NO | TRUE | สถานะเปิดใช้งาน |
| `created_at` | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | วันเวลาที่สร้างบัญชี |
| `updated_at` | TIMESTAMP | - | YES | NULL | วันเวลาที่แก้ไขข้อมูลล่าสุด |

### 1.2 `user_profiles` (ตารางข้อมูลส่วนตัวพนักงาน — 1:1 with `users`)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสโปรไฟล์ |
| `user_id` | BIGINT | FK -> users(id), UNIQUE | NO | - | ผูกกับบัญชีผู้ใช้ (ห้ามซ้ำ) |
| `first_name` | VARCHAR(100) | - | NO | - | ชื่อจริง |
| `last_name` | VARCHAR(100) | - | NO | - | นามสกุล |
| `phone_number` | VARCHAR(20) | - | YES | NULL | เบอร์โทรศัพท์ติดต่อ |
| `email` | VARCHAR(100) | - | YES | NULL | อีเมลติดต่อ |

### 1.3 `restaurant_tables` (ตารางโต๊ะอาหาร)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสโต๊ะ |
| `table_number` | VARCHAR(20) | UNIQUE, INDEX | NO | - | หมายเลขโต๊ะ (เช่น T01, T02) |
| `capacity` | INT | - | NO | - | จำนวนที่นั่งสูงสุด |
| `status` | VARCHAR(20) | INDEX | NO | 'AVAILABLE' | สถานะโต๊ะ (`AVAILABLE`, `OCCUPIED`) |

### 1.4 `buffet_packages` (ตารางแพ็กเกจราคาบุฟเฟต์)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสแพ็กเกจ |
| `name` | VARCHAR(100) | - | NO | - | ชื่อแพ็กเกจ (เช่น Standard, Premium) |
| `price` | DECIMAL(10,2) | - | NO | - | ราคาต่อท่าน (บาท) |
| `description` | TEXT | - | YES | NULL | รายละเอียดแพ็กเกจ |
| `active` | BOOLEAN | - | NO | TRUE | สถานะเปิดให้บริการ |

### 1.5 `soups` (ตารางประเภทน้ำซุป)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสน้ำซุป |
| `name` | VARCHAR(100) | - | NO | - | ชือน้ำซุป (เช่น ซุปน้ำใส, ซุปต้มยำ, ซุปหม่าล่า) |
| `active` | BOOLEAN | - | NO | TRUE | สถานะพร้อมให้บริการ |

### 1.6 `menu_categories` (ตารางหมวดหมู่อาหาร)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสหมวดหมู่ |
| `name` | VARCHAR(100) | - | NO | - | ชื่อหมวดหมู่ (เช่น เนื้อสัตว์, ผัก, เครื่องดื่ม) |

### 1.7 `menu_items` (ตารางรายการอาหาร)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสรายการอาหาร |
| `category_id` | BIGINT | FK -> menu_categories(id) | NO | - | หมวดหมู่อาหาร |
| `name` | VARCHAR(100) | - | NO | - | ชื่ออาหาร |
| `description` | TEXT | - | YES | NULL | คำอธิบายอาหาร |
| `available` | BOOLEAN | - | NO | TRUE | สถานะพร้อมสั่ง (เปิด/ปิดเมื่อของหมด) |

### 1.8 `package_menu_items` (ตารางเชื่อม Many-to-Many)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `package_id` | BIGINT | PK, FK -> buffet_packages(id) | NO | - | รหัสแพ็กเกจ |
| `menu_item_id` | BIGINT | PK, FK -> menu_items(id) | NO | - | รหัสเมนูอาหาร |

### 1.9 `dining_sessions` (ตารางรอบการรับประทานอาหารประจำโต๊ะ)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสรอบการรับประทาน |
| `table_id` | BIGINT | FK -> restaurant_tables(id) | NO | - | โต๊ะที่ใช้บริการ |
| `package_id` | BIGINT | FK -> buffet_packages(id) | NO | - | แพ็กเกจที่เลือก |
| `soup_id` | BIGINT | FK -> soups(id) | NO | - | น้ำซุปที่เลือก |
| `adult_count` | INT | - | NO | - | จำนวนลูกค้าผู้ใหญ่ |
| `child_count` | INT | - | NO | 0 | จำนวนลูกค้าเด็ก |
| `package_price_at_open` | DECIMAL(10,2) | CHECK >= 0 | NO | - | ราคาแพ็กเกจต่อผู้ใหญ่หนึ่งคนที่ล็อกตอนเปิดรอบ |
| `session_token` | VARCHAR(100) | UNIQUE, INDEX | NO | - | Secure Token สำหรับ QR Code ประจำรอบ |
| `start_time` | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | เวลาเปิดโต๊ะ |
| `end_time` | TIMESTAMP | - | YES | NULL | เวลาปิดรอบ |
| `status` | VARCHAR(20) | INDEX | NO | 'ACTIVE' | สถานะรอบ (`ACTIVE`, `COMPLETED`, `CANCELLED`) |

### 1.10 `orders` (ตารางคำสั่งซื้ออาหาร)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสคำสั่งซื้อ |
| `session_id` | BIGINT | FK -> dining_sessions(id) | NO | - | รหัสรอบที่สั่ง |
| `status` | VARCHAR(20) | INDEX | NO | 'RECEIVED' | สถานะ (`RECEIVED`, `PREPARING`, `READY`, `SERVED`) |
| `created_at` | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | เวลาที่สั่ง |

### 1.11 `order_items` (ตารางรายการอาหารในคำสั่งซื้อ)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสรายการในคำสั่งซื้อ |
| `order_id` | BIGINT | FK -> orders(id) | NO | - | รหัสคำสั่งซื้อ |
| `menu_item_id` | BIGINT | FK -> menu_items(id) | NO | - | รหัสอาหาร |
| `quantity` | INT | - | NO | - | จำนวน |
| `note` | VARCHAR(255) | - | YES | NULL | หมายเหตุเพิ่มเติม |

### 1.12 `payments` (ตารางการชำระเงิน — 1:1 with `dining_sessions`)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสการชำระเงิน |
| `session_id` | BIGINT | FK -> dining_sessions(id), UNIQUE | NO | - | รหัสรอบที่ชำระ (1 Session ชำระได้ 1 ครั้ง) |
| `amount` | DECIMAL(10,2) | - | NO | - | ยอดเงินสุทธิ |
| `payment_method` | VARCHAR(20) | - | NO | - | ช่องทางชำระ (`CASH`, `QR`, `CARD`) |
| `payment_status` | VARCHAR(20) | - | NO | 'PENDING' | สถานะการชำระ (`PENDING`, `PAID`, `FAILED`) |
| `paid_at` | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | เวลาชำระเงิน |

### 1.13 `stock_items` (ตารางสต็อกสินค้าคงคลัง)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสสินค้าสต็อก |
| `name` | VARCHAR(100) | - | NO | - | ชื่อวัตถุดิบ / สินค้า |
| `unit` | VARCHAR(30) | - | NO | - | หน่วยนับ (kg, pack, bottle) |
| `current_stock` | DECIMAL(10,2) | - | NO | 0.00 | ยอดคงเหลือปัจจุบัน |
| `minimum_stock` | DECIMAL(10,2) | - | NO | 0.00 | ยอดเตือนขั้นต่ำ |
| `opening_target_stock` | DECIMAL(10,2) | - | NO | 0.00 | ยอดที่ควรมีก่อนเปิดร้าน |
| `active` | BOOLEAN | - | NO | TRUE | สถานะการใช้งาน |

### 1.14 `stock_transactions` (ตารางประวัติธุรกรรมสต็อก)

| Column | Data Type | Constraint | Nullable | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | PK | NO | Auto Inc | รหัสธุรกรรมสต็อก |
| `stock_item_id` | BIGINT | FK -> stock_items(id) | NO | - | วัตถุดิบที่ทำรายการ |
| `user_id` | BIGINT | FK -> users(id) | NO | - | ผู้บันทึกรายการ |
| `type` | VARCHAR(20) | INDEX | NO | - | ประเภทธุรกรรม (`STOCK_IN`, `ADJUSTMENT`) |
| `quantity` | DECIMAL(10,2) | - | NO | - | จำนวนที่รับเข้าหรือผลต่าง |
| `balance_after` | DECIMAL(10,2) | - | NO | - | ยอดคงเหลือสุทธิหลังทำรายการ |
| `reason` | VARCHAR(255) | - | YES | NULL | เหตุผลการปรับยอด |
| `created_at` | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | เวลาที่ทำรายการ |

---
