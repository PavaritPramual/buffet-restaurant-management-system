# System Design

เอกสารนี้ย้ายแบบออกแบบระบบร้านบุฟเฟต์จาก [System Design ใน Notion](https://app.notion.com/p/3cfcb2e9d47a811ba912e01c6bcb449e) มาเก็บเป็นไฟล์ที่ version control ได้ใน repository (ตรวจต้นทาง 28 กันยายน 2026) ไฟล์ PlantUML อยู่ใน [รายการแผนภาพ](../diagrams/README.md) และแก้ไขได้โดยตรง

## ขอบเขตและผู้ใช้งาน

| Actor | งานหลัก |
| --- | --- |
| Customer | สแกน QR, ดูเมนู, สั่งอาหาร, ดูสถานะออเดอร์, ขอเช็กบิล |
| Service Staff | เปิดโต๊ะและรอบกิน, เลือกแพ็กเกจ/น้ำซุป, เสิร์ฟ, รับชำระ, ปิดรอบ |
| Supervisor | ดูสต็อก, บันทึกรับเข้าและปรับยอด |
| Kitchen Staff | รับออเดอร์และอัปเดต `RECEIVED → PREPARING → READY` |
| Manager | จัดการข้อมูลหลัก เช่น เมนู แพ็กเกจ โต๊ะ และสต็อก |

## เอกสารแบบออกแบบ

- [Use cases และคำอธิบาย flow](use-cases.md)
- [Domain model](domain-model.md)
- [Class diagrams](class-diagrams.md)
- [Sequence diagrams](sequence-diagrams.md)
- [Activity diagrams](activity-diagrams.md)
- [ER diagram](../diagrams/er-diagram.puml) และ [Data Dictionary](../database/data-dictionary-design.md)
- [Shared API/DTO contracts](../contracts/shared-contracts.md)

## แนวทางที่ตกลงใน Notion

- ระบบใช้ Layered Architecture: Controller → Service → Repository → Entity
- Frontend เป็น React แยกจาก Spring Boot และเรียก REST API; Supabase ใช้ PostgreSQL
- Staff login ตาม Tool Stack: Spring Security, BCrypt และ JWT; Customer เข้า flow สั่งอาหารด้วย token ของ Dining Session
- ราคา Package สำหรับ Billing ล็อกใน Dining Session ตอนเปิดรอบ การเปลี่ยนราคาใน Catalog ภายหลังไม่เปลี่ยนยอดของรอบที่เปิดไปแล้ว
- QR ปัจจุบันเป็นรหัสใช้แลกครั้งเดียวใน URL fragment; backend หมุน QR หลังแลกและออก cookie `HttpOnly` แยกต่อเครื่อง คำสั่งอาหารและการปิดรอบล็อกแถว Dining Session เดียวกันเพื่อกำหนดลำดับแน่นอน ดู [shared contract](../contracts/shared-contracts.md)
- Module ใช้ shared contract; การเปลี่ยน Entity, enum หรือ API ที่ข้าม module ต้องแจ้ง owner
- แบบออกแบบระบุ State สำหรับ Order, Strategy สำหรับการคำนวณบิล และ Template Method สำหรับ Stock เป็น pattern ที่ตั้งใจใช้ ตรวจ implementation จริงก่อนอ้างว่าเสร็จ
- UI ของ Customer เน้นมือถือ, Staff เป็น POS, Kitchen เป็น KDS และ Admin/Stock ใช้ sidebar

รายละเอียดแนวทางอยู่ใน [Tool Stack](https://app.notion.com/p/3ddcb2e9d47a8035a1b7ced6608331fa), [Implementation Guide](https://app.notion.com/p/3ddcb2e9d47a81b3a69efedb2d438cf2) และ [UI & UX Guide](https://app.notion.com/p/3ddcb2e9d47a81d28d17f0a23ccdf288)

## สถานะเทียบกับโค้ด

เอกสารในโฟลเดอร์นี้เป็น **design baseline** ที่คัดจาก Notion ไม่ใช่คำยืนยันว่า feature ทั้งหมดทำงานแล้ว ณ วันที่ย้ายเอกสาร `develop` มี Flyway V1–V5; ตาราง Dining Session, Payment, Stock และ Auth ที่แสดงในแผนภาพบางส่วนยังไม่อยู่ใน `develop` ตรวจ schema จริงจาก [Flyway migrations](https://github.com/PavaritPramual/buffet-restaurant-management-system/tree/develop/code/backend/src/main/resources/db/migration) และดู [schema delta ของ Menu/Ordering](../database/menu-ordering-schema-delta.md)

จุดที่ต้อง reconcile ก่อนอ้างว่า design ตรงกับ implementation:

1. Domain UML เดิมใช้ `DiningSession.customerCount` และ `MenuItem.price` ขณะที่ ER/Data Dictionary แยก `adult_count`/`child_count` และไม่มี `menu_items.price`
2. Use case เขียน Open และ Setup เป็นสองขั้น แต่ API ที่พัฒนาบน branch ปวริศช์เปิดรอบพร้อมโต๊ะ แพ็กเกจ น้ำซุป และจำนวนคนในคำขอเดียว
3. Class/sequence diagrams แสดง pattern และ service ที่เป็นแผนออกแบบ บางส่วนยังรอ implementation ของแต่ละ owner
4. SQL DDL ใน Notion เป็นตัวอย่างรวม 14 ตาราง ห้ามนำไปรันแทน Flyway หรือแก้ migration ที่ apply แล้ว

การเชื่อม Supabase ปัจจุบันใช้ `postgres` ซึ่งมี `BYPASSRLS` (ตรวจ 29 กันยายน 2026) ดังนั้น API ต้องตรวจสิทธิ์ Staff/Customer เอง V6 เปิด RLS, ระบุ policy ของ role นี้ และปิด grant ของ `PUBLIC`/client roles สำหรับตารางใหม่ ส่วน application role ที่ไม่มี `BYPASSRLS` เป็นงานออกแบบต่อไป ฐานส่วนกลางมี Flyway V1–V5 และยังไม่มี `dining_sessions` ณ วันที่ตรวจ; ต้องตรวจซ้ำก่อนนำ V6–V8 ขึ้นจริง

เมื่อ schema/flow เปลี่ยน ให้แก้ไฟล์ออกแบบใน commit เดียวกับการเปลี่ยน contract หรือ migration และแจ้ง owner ที่เกี่ยวข้อง
