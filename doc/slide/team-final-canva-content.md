# สไลด์รวมทีม — Canva content v01

[เปิดต้นฉบับ Canva ทีม 40 หน้า](https://www.canva.com/d/D0dhcGCjuTTS7pV) · design `DAHXQy_ZUvQ` · นำเข้าและอ่านกลับข้อความ/notesครบแล้ว ยังรอตรวจภาพทุกหน้า เจ้าของยืนยัน PDF export และผล release

วันที่ 7 ตุลาคม 2026 Code baseline develop472fba4 และหลักฐาน PR22 ไม่ใช่ public/release acceptance

32 หน้าหลัก + 8 หน้าภาคผนวก มีข้อความบนสไลด์ คำพูดประกอบ แหล่งอ้างอิงและเจ้าของ ครอบคลุมสมาชิกทั้ง5คน ร่างนี้ยังต้องเจ้าของยืนยันและเติมผล Finalจริง

Canva เป็นต้นฉบับและเครื่องมือพรีเซนต์ PDFที่exportจากCanvaของรุ่นตรวจแล้วเก็บในrepo ไม่ใช้PPTXเป็นชุดส่งหรือชุดreview เก็บPPTX/PDFเดิมเป็นประวัติเท่านั้น

เรื่อง tests: ใบงานกำหนดให้ทำ tests ผ่านและมีTestReport แต่ไม่ได้กำหนดจำนวนหน้าสไลด์ tests ทีมเลือกอธิบายระดับการทดสอบ กรณีสำคัญ concurrency ผลและขอบเขตอย่างละเอียด หน้าหลัก27–30และภาคผนวก38

## ลำดับและผู้รับช่วงอธิบาย

- 01. ระบบจัดการร้านอาหารบุฟเฟต์ — ปวริศช์
- 02. ปัญหาและขอบเขตของระบบ — ปวริศช์
- 03. ผู้ใช้และงานที่รับผิดชอบ — ปวริศช์
- 04. สมาชิกและการแบ่งโมดูล — ปวริศช์
- 05. Core Flow ที่เชื่อมทุกโมดูล — ปวริศช์
- 06. Tool Stack และเหตุผลที่เลือก — ปวริศช์
- 07. Layered Architecture และจุดเชื่อม — ปวริศช์
- 08. ฐานข้อมูลและความสัมพันธ์หลัก — เมธัส
- 09. JPA, Fetch และการรักษาประวัติ — เมธัส
- 10. เปิดรอบกินใน transaction เดียว — ปวริศช์
- 11. QR แลกสิทธิ์และรองรับหลายเครื่อง — ศิระพัทธ์
- 12. Menu และ Ordering ของลูกค้า — ศิระพัทธ์
- 13. Kitchen และ Serving แยก transition — ศรัณย์
- 14. State Pattern ใน Order Fulfillment — ศรัณย์
- 15. DTO, Validation และ ErrorResponse — ศรัณย์
- 16. Customer ขอคิดบิลและหยุดออเดอร์ — ธีรเมธ
- 17. Strategy Pattern สำหรับการคิดบิล — ธีรเมธ
- 18. Payment และ Close เป็นคนละขั้น — ธีรเมธ
- 19. Auth และการบังคับสิทธิ์ — เมธัส
- 20. Template Method สำหรับ Stock — เมธัส
- 21. Stock และ User รักษาข้อมูลเดิม — เมธัส
- 22. UI กลางและการจัดการสถานะหน้า — ศิระพัทธ์
- 23. Enterprise Patterns ใน implementation — ปวริศช์
- 24. SOLID: Single Responsibility และ Open/Closed — ปวริศช์
- 25. SOLID: Liskov และ Interface Segregation — ปวริศช์
- 26. SOLID: Dependency Inversion — ปวริศช์
- 27. แผนการทดสอบหลายระดับ — ศิระพัทธ์
- 28. Test Cases สำคัญและผลที่คาดหวัง — ศิระพัทธ์
- 29. Concurrency Tests บน PostgreSQL — ปวริศช์ / ศิระพัทธ์
- 30. ผลตรวจที่มีหลักฐานแล้ว — ศิระพัทธ์
- 31. Runtime และแผน Public Deployment — ธีรเมธ
- 32. ลำดับ Demo และสิ่งส่งมอบ — ปวริศช์
- 33. API ตัวอย่างที่ใช้ใน Core Flow — ศรัณย์ (ภาคผนวก)
- 34. ข้อขาด SOLID ที่ต้องปิดก่อนรับรอง — ปวริศช์ / ศรัณย์ (ภาคผนวก)
- 35. ตัวอย่างการคำนวณราคา Snapshot — ธีรเมธ (ภาคผนวก)
- 36. ตัวอย่าง Stock-in และ Adjustment — เมธัส (ภาคผนวก)
- 37. Schema และข้อมูลที่รักษาเป็นประวัติ — เมธัส (ภาคผนวก)
- 38. Frontend Regression และ Race Conditions — ศิระพัทธ์ (ภาคผนวก)
- 39. Release และ Checklist ก่อนนำเสนอ — ปวริศช์ (ภาคผนวก)
- 40. แหล่งอ้างอิงและหลักฐานรายวิชา — ปวริศช์ (ภาคผนวก)

## หน้า 01 — ระบบจัดการร้านอาหารบุฟเฟต์

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

Buffet Restaurant Management System

CP353002 Principles of Software Design and Development

Spring Boot · React · PostgreSQL

เปิดโต๊ะ → สั่งอาหาร → ครัว → เสิร์ฟ → ชำระ → ปิดโต๊ะ

### คำพูดและรายละเอียดประกอบ

ระบบสำหรับร้านบุฟเฟต์แบบ Walk-in เชื่อมการทำงานของลูกค้าและพนักงานตั้งแต่เปิดโต๊ะจนจบรอบกิน เราจะแสดงปัญหา โครงสร้างระบบ เหตุผลที่เลือกใช้ Patterns และหลักฐานการทดสอบ พร้อมเดโมของแต่ละบทบาท ชุดนี้เป็นร่างจากโค้ด baseline ปัจจุบัน ส่วน public URL และผล release ต้องเติมจากการตรวจจริงก่อนนำเสนอ

### แหล่งอ้างอิง

- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)

## หน้า 02 — ปัญหาและขอบเขตของระบบ

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

| ปัญหาในงานร้าน | แนวทางในระบบ |
| --- | --- |
| ข้อมูลโต๊ะกับออเดอร์ไม่ต่อกัน | ใช้ DiningSession เป็นรอบอ้างอิงร่วม |
| ครัวและคนเสิร์ฟไม่เห็นสถานะเดียวกัน | Order มีลำดับสถานะและสิทธิ์แยกตามหน้าที่ |
| ราคาเปลี่ยนระหว่างลูกค้ากิน | เก็บราคาแพ็กเกจตอนเปิดรอบ |
| ยอดสต็อกเปลี่ยนโดยหาที่มาไม่ได้ | บันทึก actor เหตุผล และยอดหลังทำรายการ |

### คำพูดและรายละเอียดประกอบ

ตัวอย่างปัญหาเป็นเหตุผลในการออกแบบ ไม่ใช่ผลสำรวจร้านจริงที่เราไม่ได้ทำ ขอบเขตคือร้าน Walk-in และการรับชำระที่พนักงานบันทึก ระบบนี้ยังไม่เชื่อม Payment Gateway ไม่มีระบบจองโต๊ะ WebSocket หรือสูตรหักวัตถุดิบอัตโนมัติ สต็อกมีการรับเข้าและปรับยอดที่มีประวัติ

### แหล่งอ้างอิง

- [doc/system-design/README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/system-design/README.md)
- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)

## หน้า 03 — ผู้ใช้และงานที่รับผิดชอบ

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

| บทบาท | งานหลัก |
| --- | --- |
| Customer | QR / เมนู / ออเดอร์ / ขอคิดบิล / ดูสถานะ |
| SERVICE_STAFF | เปิดรอบ / เสิร์ฟ / รับชำระ / ปิดรอบ |
| KITCHEN_STAFF | รับออเดอร์ / เตรียม / พร้อมเสิร์ฟ |
| SUPERVISOR | อ่านและทำรายการสต็อกตามสิทธิ์ |
| MANAGER | จัดการข้อมูลร้าน ผู้ใช้ เมนู และสต็อก |

### คำพูดและรายละเอียดประกอบ

Customer ใช้สิทธิ์จากรอบกิน ไม่ต้องสมัครบัญชี ส่วนพนักงาน login ด้วยบัญชีและ role ที่ backend ตรวจจริง MANAGER และ SUPERVISOR ไม่ได้รับสิทธิ์ทำ Kitchen/Serving จากการเป็นตำแหน่งสูงกว่า บทบาทในตารางเป็น runtime ของรุ่นนี้ ไม่ใช้ role จาก header ที่ผู้ใช้ปลอมเอง

### แหล่งอ้างอิง

- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)
- [code/backend/src/main/java/com/buffetrestaurant/service/SessionOrderFulfillmentAccessProvider.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/SessionOrderFulfillmentAccessProvider.java)

## หน้า 04 — สมาชิกและการแบ่งโมดูล

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

| สมาชิก | โมดูลและหน้าที่ |
| --- | --- |
| ปวริศช์ 673380278-9 | Table / Package / Soup / Session / Integration |
| ศิระพัทธ์ 673380293-3 | Menu / Ordering / Customer UI / Regression |
| ศรัณย์ 673380515-1 | Kitchen / Serving / Order State / API |
| ธีรเมธ 673380273-9 | Billing / Payment / Strategy / Deployment |
| เมธัส 673380300-2 | Auth / User / Stock / Template Method / DB |

### คำพูดและรายละเอียดประกอบ

แต่ละคนมี personal branch ของตน งานรวมผ่าน develop และ PR review หน้าที่ในร่างสไลด์เป็นผู้รับช่วงอธิบาย ไม่ใช่หลักฐานว่าทุกคนส่งหรือยืนยันเนื้อหาแล้ว เจ้าของต้องตรวจส่วนของตนก่อนรวมรุ่นส่ง และสมาชิกต้องอธิบายโค้ดจริง ไม่อ่านสไลด์อย่างเดียว

### แหล่งอ้างอิง

- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)
- [doc/planning/step3-git-audit.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/planning/step3-git-audit.md)

## หน้า 05 — Core Flow ที่เชื่อมทุกโมดูล

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

Staff เปิดโต๊ะ → Customer แลก QR → สั่งอาหาร

Kitchen: RECEIVED → PREPARING → READY

Staff: SERVED → รับคำขอคิดบิล → รับชำระ

PAID → Staff กด close → โต๊ะ AVAILABLE

### คำพูดและรายละเอียดประกอบ

รอบกินเป็นแกนของ flow โดยมีโต๊ะ แพ็กเกจ จำนวนผู้ใหญ่และเด็ก ออเดอร์ และ Payment อ้าง session เดียวกัน การขอคิดบิลทำให้หยุดรับออเดอร์ใหม่ ส่วนการชำระกับการปิดรอบเป็นคนละขั้น เรายังคงให้พนักงานยืนยันปิดรอบเพื่อควบคุมการคืนโต๊ะ

### แหล่งอ้างอิง

- [doc/system-design/sequence-diagrams.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/system-design/sequence-diagrams.md)
- [doc/contracts/customer-bill-request.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/contracts/customer-bill-request.md)

## หน้า 06 — Tool Stack และเหตุผลที่เลือก

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

| ส่วน | เทคโนโลยี / เหตุผล |
| --- | --- |
| Backend | Java 17+ / Spring Boot 3.5.16 / Maven |
| Persistence | Spring Data JPA / PostgreSQL / Flyway |
| Frontend | React / TypeScript / Vite / shared UI |
| คุณภาพ | JUnit / Mockito / Vitest / CI / Browser regression |
| Auth และ API | HTTP session cookie / customer grant / OpenAPI |

### คำพูดและรายละเอียดประกอบ

Stack นี้ตรงข้อกำหนดรายวิชา Spring ช่วย wiring และ transaction JPA ใช้ Repository interfaces ส่วน Flyway กำหนด schema ตามเวอร์ชัน PostgreSQL ใช้ตรวจ constraint และ row lock จริง React เป็นหน้าเว็บและสื่อสารด้วย DTO ระบบไม่ได้ใช้ Supabase Auth JWT ใน runtime รุ่นนี้

### แหล่งอ้างอิง

- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)
- [code/backend/pom.xml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/pom.xml)
- [.github/workflows/ci.yml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/.github/workflows/ci.yml)

## หน้า 07 — Layered Architecture และจุดเชื่อม

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

React → Controller → Service → Repository → Database

DTO / Mapper เป็นขอบเขตข้อมูลที่ส่งออก API

SessionContextProvider → Ordering

DiningSessionBillingReader → Billing

PaymentStatusLookup → กฎปิดรอบ

### คำพูดและรายละเอียดประกอบ

Controller รับ HTTP และ payload Service เป็นเจ้าของ business rule กับ transaction Repository ทำ data access โมดูลเชื่อมกันผ่านข้อมูลและ interface ที่ตกลงไว้ ไม่ส่ง Entity ของ Payment ไปให้ Session ใช้แทน contract ทั้งระบบยังมีข้อขาด layer/DI ที่บันทึกใน SOLID report ต้องแก้ก่อนรับรอง Final สไลด์นี้แสดงโครงที่ใช้ ไม่อ้างว่าการแยกโฟลเดอร์ทำให้ถูกหลักทุกจุด

### แหล่งอ้างอิง

- [doc/diagrams/component.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/component.puml)
- [doc/solid-analysis.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/solid-analysis.md)

## หน้า 08 — ฐานข้อมูลและความสัมพันธ์หลัก

ผู้รับช่วง: **เมธัส**

### ข้อความบนสไลด์

app_users 1 ↔ 1 user_profiles ด้วย shared PK

restaurant_tables 1 → N dining_sessions

dining_sessions 1 → N orders → N order_items

dining_sessions 1 → 0..1 payments

buffet_packages N ↔ N menu_items ผ่าน package_menu_items

stock_items 1 → N stock_transactions

### คำพูดและรายละเอียดประกอบ

ความสัมพันธ์ One-to-One และ One-to-Many ตรงเกณฑ์วิชา และมี Many-to-Many สำหรับเมนูในแพ็กเกจ orders และ payments เก็บ sessionId เป็น scalar ใน JPA แต่ฐานข้อมูลมี FK จริง จึงต้องแยกสิ่งที่เป็น database relationship ออกจาก object association ใน baseline รอบ Step 2 Profile ใช้ชื่อตาราง `user_profiles` และ fields `display_name`/`email`; ต่อมา V15 เพิ่ม firstName/lastName/phoneNumber (nullable สำหรับข้อมูลเดิม) ตาม [Auth/Stock schema delta](../database/auth-stock-schema-delta.md)

### แหล่งอ้างอิง

- [doc/diagrams/er-diagram.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/er-diagram.puml)
- [doc/database/step2-schema-approved.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/database/step2-schema-approved.md)
- [code/backend/src/main/java/com/buffetrestaurant/domain/UserProfile.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/domain/UserProfile.java)

## หน้า 09 — JPA, Fetch และการรักษาประวัติ

ผู้รับช่วง: **เมธัส**

### ข้อความบนสไลด์

| ความสัมพันธ์ | เหตุผลของ mapping / constraint |
| --- | --- |
| Session → Table / Package / Soup | ManyToOne LAZY ไม่ cascade ลบ master |
| Order → OrderItem | OneToMany LAZY, ALL, orphanRemoval |
| Orders → Session ใน DB | FK RESTRICT รักษาประวัติรอบ |
| Payment → Session ใน DB | FK และ UNIQUE ไม่ชำระซ้ำ |
| DTO mapping | ทำใน transaction; open-in-view=false |

### คำพูดและรายละเอียดประกอบ

LAZY ช่วยเลื่อนการโหลดข้อมูลที่ไม่จำเป็น และ mapper ต้องทำภายในขอบเขต transaction OrderItem เป็นส่วนหนึ่งของ Order จึงใช้ cascade แต่ห้ามใช้ cascade ลบข้อมูล menu หรือ package ที่แชร์หลายรอบ SQL ON DELETE กับ JPA Cascade เป็นคนละกลไก Owner ต้องตรวจ query/N+1 และ fetch rationale ของโมดูลตนเพิ่มเติม Payment ไม่มี JPA association กับ Session จึงไม่แต่ง cascade/fetch ที่ไม่มีในโค้ด

### แหล่งอ้างอิง

- [doc/system-design/README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/system-design/README.md)
- [code/backend/src/main/java/com/buffetrestaurant/domain/CustomerOrder.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/domain/CustomerOrder.java)
- [code/backend/src/main/java/com/buffetrestaurant/domain/Payment.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/domain/Payment.java)

## หน้า 10 — เปิดรอบกินใน transaction เดียว

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

ล็อกโต๊ะ → ตรวจ AVAILABLE และจำนวนคน

ตรวจ package / soup ที่ active

เก็บ package_price_at_open และสร้าง QR

Session ACTIVE + Table OCCUPIED → commit

### คำพูดและรายละเอียดประกอบ

คำขอเปิดรอบจะผ่านทั้งกฎ capacity และจำนวนลูกค้ารวมอย่างน้อยหนึ่งคน Row lock โต๊ะทำให้คำขอพร้อมกันมีลำดับ เมื่ออีกคำขอได้ lock ต่อจะพบโต๊ะไม่ว่าง ราคา snapshot เก็บ precision/scale เดียวกับแพ็กเกจ หากขั้นใดล้ม transaction rollback จึงไม่เหลือโต๊ะ occupied โดยไม่มีรอบ

### แหล่งอ้างอิง

- [code/backend/src/main/java/com/buffetrestaurant/service/impl/DiningSessionServiceImpl.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/DiningSessionServiceImpl.java)
- [doc/diagrams/sequence-open-session.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/sequence-open-session.puml)

## หน้า 11 — QR แลกสิทธิ์และรองรับหลายเครื่อง

ผู้รับช่วง: **ศิระพัทธ์**

### ข้อความบนสไลด์

QR fragment → หน้าเว็บล้าง URL → POST body

ล็อก session → ใช้รหัสครั้งเดียว → หมุน QR ใหม่

ออก HttpOnly customer cookie ต่อเครื่อง

เก็บ hash + expiry และตรวจ ACTIVE ทุกคำขอ

สแกนใหม่ในแท็บเดิม: ล้างข้อมูลรอบเก่าและกัน response เก่าทับ

### คำพูดและรายละเอียดประกอบ

Fragment ไม่ถูกส่งเป็น request path แต่รหัสยังเป็นความลับที่ต้องระวังการแชร์ Backend แลกรหัสเพียงครั้งเดียวและหมุน QR เพื่อเครื่องถัดไป Frontend แชร์ Promise เฉพาะ token ที่กำลังแลกเพื่อรองรับ StrictMode และเรียงคำขอคนละ token เพื่อไม่ให้ cookie สุดท้ายผิดรอบ ไม่ cache ผลสำเร็จจนเปิดรหัสใช้แล้วซ้ำได้ HttpOnly ช่วยไม่ให้ JavaScript อ่าน cookie แต่ไม่ได้ป้องกัน XSS ทั้งหมด Public ต้องตรวจ Secure/HTTPS จริง

### แหล่งอ้างอิง

- [doc/contracts/ordering-contract.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/contracts/ordering-contract.md)
- [code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerSessionAccessService.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerSessionAccessService.java)
- [doc/diagrams/sequence-qr-exchange.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/sequence-qr-exchange.puml)

## หน้า 12 — Menu และ Ordering ของลูกค้า

ผู้รับช่วง: **ศิระพัทธ์**

### ข้อความบนสไลด์

| การทำงาน | เงื่อนไขสำคัญ |
| --- | --- |
| ดูเมนู | เฉพาะ available และอยู่ในแพ็กเกจของรอบ |
| สร้างออเดอร์ | cookie ถูกต้อง / ACTIVE / ยังไม่ขอคิดบิล |
| รายละเอียดออเดอร์ | menuItemId + quantity; ไม่รับราคา |
| ข้อมูลประวัติ | table_number และ item_name เป็น snapshot |
| หลังส่งสำเร็จ | เริ่ม RECEIVED; แสดงประวัติไม่ซ้ำ |

### คำพูดและรายละเอียดประกอบ

Backend ตรวจรายการอาหาร จำนวนบวก รายการซ้ำและเมนูที่ไม่อยู่ในแพ็กเกจ ไม่เชื่อว่าการซ่อนเมนูใน UI เป็นการป้องกันเพียงพอ Menu admin มี pagination/sorting และ CRUD ฝั่ง Manager เมื่อมีประวัติออเดอร์ห้ามลบ menu item แล้วให้ทำ unavailable Frontend จัดการ delayed response ของเมนูและ history เพื่อไม่ให้ผลเก่าทับรายการที่เพิ่งสั่ง

### แหล่งอ้างอิง

- [code/backend/src/main/java/com/buffetrestaurant/controller/CustomerOrderingController.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/CustomerOrderingController.java)
- [doc/architecture/sirapat-menu-ordering-solid-jpa.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/architecture/sirapat-menu-ordering-solid-jpa.md)
- [doc/testing/requirement-test-traceability.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/requirement-test-traceability.md)

## หน้า 13 — Kitchen และ Serving แยก transition

ผู้รับช่วง: **ศรัณย์**

### ข้อความบนสไลด์

RECEIVED → PREPARING → READY → SERVED

KITCHEN_STAFF: PREPARING และ READY

SERVICE_STAFF: READY → SERVED

ห้ามข้าม ย้อน หรือเปลี่ยน SERVED ต่อ

Backend อ่านสิทธิ์จาก login cookie จริง

### คำพูดและรายละเอียดประกอบ

ครัวอ่าน incoming orders ที่ยัง RECEIVED/PREPARING พนักงานเสิร์ฟอ่าน ready orders การเปลี่ยนสถานะต้องผ่านทั้ง role และลำดับ next state MANAGER/SUPERVISOR ไม่สามารถทำ transition เหล่านี้ได้ใน flow ปัจจุบัน Header X-User-Role ไม่เพิ่มสิทธิ์ ต้องยึด production session provider ส่วน fixture ใช้เฉพาะ test ที่ระบุ

### แหล่งอ้างอิง

- [code/backend/src/main/java/com/buffetrestaurant/controller/OrderFulfillmentController.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/OrderFulfillmentController.java)
- [code/backend/src/main/java/com/buffetrestaurant/service/SessionOrderFulfillmentAccessProvider.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/SessionOrderFulfillmentAccessProvider.java)
- [doc/diagrams/order-fulfillment-state-diagram.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/order-fulfillment-state-diagram.md)

## หน้า 14 — State Pattern ใน Order Fulfillment

ผู้รับช่วง: **ศรัณย์**

### ข้อความบนสไลด์

| หน้าที่ | คลาสจริง |
| --- | --- |
| Context | OrderFulfillmentServiceImpl |
| State contract | OrderState: status() / next() |
| Concrete states | Received / Preparing / Ready / Served |
| Resolve state | OrderStateFactory.forStatus(enum) |
| Persistence | CustomerOrder.status เป็น enum string |

### คำพูดและรายละเอียดประกอบ

Service resolve current state จาก enum แล้วเทียบ requested target กับ next().status ก่อนบันทึก แต่ละ state จึงรับผิดชอบลำดับถัดไปของตน Served.next ปฏิเสธด้วย BusinessRuleException ตาม contract ไม่ใช่ throw UnsupportedOperationException แบบไม่มีสัญญา Factory ยังมี switch จึงต้องแก้เมื่อเพิ่ม enum และต้องเปลี่ยน API/UI ร่วม ไม่อ้างว่า OCP สมบูรณ์ทุกจุด

### แหล่งอ้างอิง

- [doc/diagrams/class-order-state.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/class-order-state.puml)
- [code/backend/src/main/java/com/buffetrestaurant/service/state/OrderState.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/state/OrderState.java)
- [code/backend/src/main/java/com/buffetrestaurant/service/state/OrderStateFactory.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/state/OrderStateFactory.java)

## หน้า 15 — DTO, Validation และ ErrorResponse

ผู้รับช่วง: **ศรัณย์**

### ข้อความบนสไลด์

| ขอบเขต | รูปแบบปัจจุบัน |
| --- | --- |
| Input | Request DTO + @Valid / Bean Validation |
| Output | Response DTO / Mapper ไม่คืน Entity ทั้งก้อน |
| Customer / Staff | แยก response; Customer ไม่เห็น QR token |
| ErrorResponse | timestamp, status, error, message, path |
| HTTP | 400 validation / 401 login / 403 role / 404 / 409 rule |

### คำพูดและรายละเอียดประกอบ

DTO ทำให้ API ไม่ผูกกับโครงสร้าง Entity และควบคุมข้อมูลที่เปิดเผย ResponseEntity กับการคืน DTO ตรงยังใช้ JSON body รูปแบบเดียวกัน API ต้องมี OpenAPI examples ที่ตรง runtime ฝั่ง request ใหม่ต้องตรวจชื่อ fields/enums และราคาเป็น BigDecimal เวลาแต่ละ field ต้องยึดชนิดจริง ไม่อ้างทุก timestamp เป็น OffsetDateTime เพราะ Session start/end ใช้ LocalDateTime

### แหล่งอ้างอิง

- [doc/contracts/api-conventions.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/contracts/api-conventions.md)
- [code/backend/src/main/java/com/buffetrestaurant/dto/response/ErrorResponse.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/dto/response/ErrorResponse.java)
- [code/backend/src/main/java/com/buffetrestaurant/exception/GlobalExceptionHandler.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/exception/GlobalExceptionHandler.java)

## หน้า 16 — Customer ขอคิดบิลและหยุดออเดอร์

ผู้รับช่วง: **ธีรเมธ**

### ข้อความบนสไลด์

POST bill-request → บันทึกเวลาครั้งแรก

กดซ้ำ → คืนสถานะเดิม ไม่สร้างคำขอซ้ำ

ทุกเครื่องของรอบนั้นสั่งเพิ่มไม่ได้

GET bill-status → NOT_REQUESTED / REQUESTED / PAID

Session ยัง ACTIVE จนพนักงานกด close

### คำพูดและรายละเอียดประกอบ

Bill request มี nullable bill_requested_at จาก V14 และไม่เพิ่ม DiningSessionStatus ใหม่ การอ่านยอดใช้ BillSummary ที่ backend คำนวณ ลูกค้าไม่ส่งยอดและไม่ยืนยัน PAID เอง Request และ order ใช้ lock เดียวกัน หาก order ได้ lock ก่อนบันทึกได้ ถ้า bill request ก่อน order หลังจากนั้นได้409 UI ของ staff แสดง badge ส่วน customer poll สถานะขณะหน้าเปิด

### แหล่งอ้างอิง

- [doc/contracts/customer-bill-request.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/contracts/customer-bill-request.md)
- [code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java)
- [code/backend/src/main/java/com/buffetrestaurant/controller/CustomerBillingController.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/CustomerBillingController.java)

## หน้า 17 — Strategy Pattern สำหรับการคิดบิล

ผู้รับช่วง: **ธีรเมธ**

### ข้อความบนสไลด์

BillingEngine → BillCalculationStrategy + DiscountCalculationStrategy

StandardBillCalculation → ChildRateCalculationStrategy

ราคาผู้ใหญ่เต็มราคา · เด็ก 50% ใน config ปัจจุบัน

BigDecimal → หักส่วนลด → HALF_UP 2 ตำแหน่ง

Context จาก backend ใช้ราคา snapshot ของรอบ

### คำพูดและรายละเอียดประกอบ

BillingEngine เป็นผู้ประสาน pricing และ discount ผ่าน interface Config ประกอบ Standard กับ child rate0.5 เป็น composition ไม่ใช่ ChildRate extends Standard ตัว Discount Strategy มี implementation แต่ database provider baseline ส่ง promotion context เป็น null จึงไม่อ้างว่ามี UI จัดการโปรโมชั่นแล้ว Strategy ไม่จัดการสิทธิ์หรือ transaction ซึ่งยังอยู่ PaymentService

### แหล่งอ้างอิง

- [doc/diagrams/class-billing-strategy.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/class-billing-strategy.puml)
- [code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java)
- [code/backend/src/main/java/com/buffetrestaurant/config/BillingConfig.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/config/BillingConfig.java)

## หน้า 18 — Payment และ Close เป็นคนละขั้น

ผู้รับช่วง: **ธีรเมธ**

### ข้อความบนสไลด์

| ขั้น | กฎสำคัญ |
| --- | --- |
| อ่านบิล | อ่าน session จริงและคำนวณยอด backend |
| รับชำระ | ACTIVE + ขอคิดบิลแล้ว + ไม่จ่ายซ้ำ |
| บันทึก | PAID / amount / payment method / paid_at |
| Close | ตรวจ PAID ของ session เดียวกันผ่าน lookup |
| ผลหลัง close | COMPLETED / AVAILABLE / ถอน customer grants |

### คำพูดและรายละเอียดประกอบ

Payment รองรับวิธีบันทึก CASH/QR/CARD แต่ไม่ได้เรียก payment gateway การเลือก QR ในวิธีชำระต่างจาก QR ที่ใช้เปิดสิทธิ์สั่งอาหาร DB UNIQUE session_id เป็นด่านกันชำระซ้ำ Close ต้องอ่านผลจริงอีกครั้งและคืนโต๊ะใน transaction เดียว ถ้าจุดเชื่อม Payment ไม่พร้อมตอบ503 จึงไม่แสดง success หรือคืนโต๊ะโดยไม่มีผลชำระ

### แหล่งอ้างอิง

- [code/backend/src/main/java/com/buffetrestaurant/service/impl/PaymentServiceImpl.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/PaymentServiceImpl.java)
- [code/backend/src/main/java/com/buffetrestaurant/domain/Payment.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/domain/Payment.java)
- [doc/diagrams/sequence-billing-payment.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/sequence-billing-payment.puml)

## หน้า 19 — Auth และการบังคับสิทธิ์

ผู้รับช่วง: **เมธัส**

### ข้อความบนสไลด์

| ชั้นตรวจ | แนวทาง |
| --- | --- |
| Staff login | BCrypt → HTTP session cookie → UserContext |
| Protected API | ไม่มี login 401 / role ผิด 403 |
| Customer | grant cookie + expiry + session ID + ACTIVE |
| Frontend guards | direct URL / case / trailing slash / logout |
| Public release | HTTPS / Secure cookie / allowed Origin ต้องตรวจจริง |

### คำพูดและรายละเอียดประกอบ

ซ่อนเมนูเป็นเพียง UX backend ต้องตรวจ permission ทุก endpoint Customer กับ Staff ใช้ cookie คนละหน้าที่ Header ปลอมไม่ทำให้เป็น staff Public environment จะใช้ origin เดียวตามแผนและต้องทดสอบ timed TTL จริง แยกจาก invalidation ผ่าน logout ซึ่งมีหลักฐาน local แล้ว Supabase ในระบบนี้ใช้ database ไม่ได้เป็นผู้ login Staff

### แหล่งอ้างอิง

- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)
- [doc/testing/requirement-test-traceability.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/requirement-test-traceability.md)
- [code/backend/src/main/java/com/buffetrestaurant/service/SessionUserContextProvider.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/SessionUserContextProvider.java)

## หน้า 20 — Template Method สำหรับ Stock

ผู้รับช่วง: **เมธัส**

### ข้อความบนสไลด์

StockService: transaction + row lock

StockTransactionTemplate.process() เป็น final

ตรวจ actor → คำนวณ delta → ตรวจยอดไม่ติดลบ → save + audit

StockInProcessor: IN / delta บวก

StockAdjustmentProcessor: ADJUSTMENT / signed delta

### คำพูดและรายละเอียดประกอบ

เราต้องการให้รับเข้าและปรับยอดผ่านขั้นร่วมกันเสมอ โดย subclass เปลี่ยนเฉพาะ calculateDelta และ transactionType Template ไม่เปิด transaction เอง StockService เป็นเจ้าของขอบเขต transaction และ lock เพื่อให้ quantity กับ history commit/rollbackพร้อมกัน การตรวจ request/เหตุผลอยู่ DTO และ Service ไม่วาด hook validate ที่ไม่มีจริงในคลาส

### แหล่งอ้างอิง

- [doc/diagrams/class-stock-template.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/class-stock-template.puml)
- [code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java)
- [code/backend/src/main/java/com/buffetrestaurant/service/StockService.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/StockService.java)

## หน้า 21 — Stock และ User รักษาข้อมูลเดิม

ผู้รับช่วง: **เมธัส**

### ข้อความบนสไลด์

| Baseline ใช้งานได้ | งาน Final ที่ยังต้องรวม |
| --- | --- |
| sku / name / unit / quantity / threshold | opening_target_stock และ active |
| stock-in / adjustment พร้อมเหตุผล | inactive ห้ามทำรายการแต่ยังอ่าน history |
| รายการใหม่เริ่มยอด0 ไม่แก้ยอดจาก master form | จำนวนที่ขาดจาก target ไม่เพิ่ม stock อัตโนมัติ |
| User Profile: display_name / email | firstName / lastName / phoneNumber |
| Flyway V1–V14 เป็น baseline | forward migration หลังreview/อนุมัติเลขใหม่ |

### คำพูดและรายละเอียดประกอบ

สไลด์นี้แยก implementation ปัจจุบันกับ scope Final เพื่อไม่เล่าเหมือนทำครบแล้ว SKU/หน่วยเปลี่ยนไม่ได้หลังมี history Adjustment เป็นผลต่างบวกหรือลบ ไม่ใช่ตั้ง counted stock ตรงทุกกรณี ข้อมูลชื่อเก่าห้ามเดาแยกชื่อเป็น first/last เจ้าของต้อง merge code/test/schema/docsใหม่แล้วปรับสไลด์รุ่นส่ง และห้ามแก้ migration ที่ applyแล้ว

### แหล่งอ้างอิง

- [doc/planning/step3-requirement-matrix.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/planning/step3-requirement-matrix.md)
- [doc/database/auth-stock-schema-delta.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/database/auth-stock-schema-delta.md)
- [doc/system-design/README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/system-design/README.md)

## หน้า 22 — UI กลางและการจัดการสถานะหน้า

ผู้รับช่วง: **ศิระพัทธ์**

### ข้อความบนสไลด์

Noto Sans Thai · primary #9A3412 · background #FFFDF9

Customer 360px · Staff/Kitchen 768px · Manager 1280px

Shared components / Admin rail / role navigation

Loading · Empty · Error · Confirmation · กันกดซ้ำ

QR เปลี่ยนรอบ → ล้างตะกร้าและข้อมูลเดิม

### คำพูดและรายละเอียดประกอบ

รูปแบบ UI ใช้ tokens/componentsกลางเพื่อให้หน้าจอจากหลายคนสอดคล้องกัน ส่วน state ต้องแสดงข้อความที่ทำให้ผู้ใช้ทำต่อได้ เช่น errorในการปิดก่อนจ่ายต้องคงโต๊ะ occupied Responsive มีภาพlocal17ภาพในรอบตรวจจริง ส่วน loading/empty/errorบางภาพเป็น controlled fixtures จึงใช้พูดเรื่อง UI behavior แต่ไม่เอาไปอ้าง public acceptance

### แหล่งอ้างอิง

- [doc/testing/sirapat-step3-premerge-report.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/sirapat-step3-premerge-report.md)
- [doc/testing/requirement-test-traceability.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/requirement-test-traceability.md)
- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)

## หน้า 23 — Enterprise Patterns ใน implementation

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

| Pattern | จุดใช้และประโยชน์ |
| --- | --- |
| Layered / MVC | Controller–Service–Repository; React เป็น View |
| Repository | Spring Data JPA queries และ row locks |
| Service Layer | business rules กับ transaction ใน use case |
| DTO + Mapper | แยก API contract และข้อมูลตาม audience |
| Dependency Injection | constructor และ config wiring/providers |

### คำพูดและรายละเอียดประกอบ

วิชากำหนด Enterprise Patternsทั้ง6แบบในรายการ จึงอธิบายจากชื่อคลาสจริง LayeredกับMVCมีคนละมุมมอง React View แยก API ไม่ใช่ Thymeleaf Repositoryไม่ใช่แค่ชื่อโฟลเดอร์เพราะมี interfacequery/lockจริง DI ยังมีข้อขาด constructorและconcretecollaboratorsบางจุดต้องแก้ก่อนFinal การมีSpringไม่ได้พิสูจน์SOLIDครบอัตโนมัติ

### แหล่งอ้างอิง

- [doc/design-patterns.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/design-patterns.md)
- [doc/diagrams/component.puml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/diagrams/component.puml)

## หน้า 24 — SOLID: Single Responsibility และ Open/Closed

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

| หลักการ | ตัวอย่างจากโค้ด |
| --- | --- |
| S: Controller | รับ HTTP แล้วเรียก Service |
| S: Mapper | แปลง Entity เป็น response แยกจากกฎเปิดรอบ |
| O: BillingEngine | เปลี่ยน pricing/discount ผ่าน strategies |
| O: Stock template | เพิ่ม processor ผ่าน hooks ของขั้นร่วม |
| ข้อจำกัด | State factory ยังต้องแก้ switch เมื่อเพิ่ม enum |

### คำพูดและรายละเอียดประกอบ

Single Responsibility ดูเหตุผลที่จะทำให้คลาสเปลี่ยน การเปิดรอบมีหลายขั้นแต่เป็น use case เดียว ไม่แยกคลาสทุกบรรทัดโดยไม่มีเหตุผล Open/Closedแสดงจากจุดที่รับstrategyผ่านinterfaceแล้วเปลี่ยนimplementationได้ ส่วนStatefactoryยังเป็นworkflow4ค่าที่ต้องแก้เมื่อเพิ่มstatus ต้องบอกtradeoffตรงไปตรงมา ไม่อ้างทุกclassเปิดขยายได้โดยไม่แก้อะไร

### แหล่งอ้างอิง

- [doc/solid-analysis.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/solid-analysis.md)
- [code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java)
- [code/backend/src/main/java/com/buffetrestaurant/service/state/OrderStateFactory.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/state/OrderStateFactory.java)

## หน้า 25 — SOLID: Liskov และ Interface Segregation

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

| หลักการ | สัญญาที่รักษา |
| --- | --- |
| L: OrderState | next() มีสัญญาปฏิเสธ terminal state |
| L: Pricing strategy | คืนยอดไม่ติดลบ; ไม่ทำลาย context |
| I: SessionContextProvider | Ordering รับเฉพาะ context ที่ต้องใช้ |
| I: PaymentStatusLookup | Close อ่านเฉพาะผลชำระของรอบ |
| ตรวจด้วย tests | transition / invalid strategy / wrong session |

### คำพูดและรายละเอียดประกอบ

Liskovไม่ใช่แค่มีextendsหรือimplements แต่ผู้เรียกต้องใช้implementationแทนกันได้ตามcontract Served.nextที่rejectตามcontractจึงต่างจากmethodที่throwUnsupportedOperationExceptionจนผู้เรียกคาดไม่ถึง InterfaceSegregationให้โมดูลใช้contractเล็ก เช่นcloseไม่ต้องรับPaymentEntityทั้งตัว testsช่วยยืนยันพฤติกรรมที่contractกำหนด

### แหล่งอ้างอิง

- [doc/solid-analysis.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/solid-analysis.md)
- [code/backend/src/main/java/com/buffetrestaurant/service/state/OrderState.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/state/OrderState.java)
- [code/backend/src/main/java/com/buffetrestaurant/integration/payment/PaymentStatusLookup.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/integration/payment/PaymentStatusLookup.java)

## หน้า 26 — SOLID: Dependency Inversion

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

CustomerOrderingServiceImpl → SessionContextProvider

DatabaseBillingContextProvider → DiningSessionBillingReader

BillingEngine → pricing / discount interfaces

Constructor รับ dependencies ชัดเจนและทดแทนใน tests

ก่อน Final: ปิดข้อขาด G01–G04 ตาม audit

### คำพูดและรายละเอียดประกอบ

ส่วนที่แสดงเป็นตัวอย่างการพึ่งabstractionจริง ส่วนเกณฑ์วิชาระบุServiceขึ้นกับinterfaceและconstructorinjectionเท่านั้น เรายังพบCustomerBillingServicefieldEntityManager, contextproviderอ้างControllerconstantและconcretecollaborators จึงบันทึกข้อขาดและownerไว้ ห้ามพูดว่าDผ่านทั้งระบบก่อนแก้และมีreview หากแก้แล้วสไลด์ต้องชี้releasecommitใหม่

### แหล่งอ้างอิง

- [doc/solid-analysis.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/solid-analysis.md)
- [code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerOrderingServiceImpl.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerOrderingServiceImpl.java)
- [code/backend/src/main/java/com/buffetrestaurant/service/impl/DatabaseBillingContextProvider.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/DatabaseBillingContextProvider.java)

## หน้า 27 — แผนการทดสอบหลายระดับ

ผู้รับช่วง: **ศิระพัทธ์**

### ข้อความบนสไลด์

| ระดับ | ตรวจอะไร / เครื่องมือ |
| --- | --- |
| Unit | State / Strategy / Template rules: JUnit + Mockito |
| API integration | HTTP / validation / cookies / persistence: Spring Boot Test |
| PostgreSQL integration | Flyway / constraints / privileges / row-lock concurrency |
| Frontend component | states / races / guards: Vitest + Testing Library |
| Browser E2E | หลาย contexts และ UI→HTTP→backend จริง |

### คำพูดและรายละเอียดประกอบ

Testsมีไว้ตรวจbusinessrulesและการเชื่อมจริง Unitไม่แทนDBlocking H2ใช้ตรวจAPIและflowที่แยกได้แต่lock/grantsต้องPostgreSQL ภาพที่จำลองHTTPresponsesใช้ตรวจUIstateไม่ใช่หลักฐานbackend Browserของรุ่นส่งต้องรันบนpublicHTTPSและrolesจริง แยกรายงานแต่ละenvironmentเพื่ออธิบายผลได้

### แหล่งอ้างอิง

- [doc/testing/test-plan.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/test-plan.md)
- [.github/workflows/ci.yml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/.github/workflows/ci.yml)
- [doc/testing/requirement-test-traceability.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/requirement-test-traceability.md)

## หน้า 28 — Test Cases สำคัญและผลที่คาดหวัง

ผู้รับช่วง: **ศิระพัทธ์**

### ข้อความบนสไลด์

| กรณี | Expected result |
| --- | --- |
| เปิดโต๊ะ occupied / จำนวนเกินcapacity | ปฏิเสธ; ไม่เกิดsessionใหม่ |
| QRใช้แล้ว / cookieผิดรอบ | เข้าไม่ได้; ไม่สร้างorder |
| roleผิด / ปลอมX-User-Role | 401/403ตามกรณี; สิทธิ์ไม่เพิ่ม |
| ขอคิดบิลแล้วสั่งเพิ่ม | 409; ordersไม่เพิ่มทุกเครื่อง |
| จ่ายซ้ำ / closeก่อนPAID | ปฏิเสธ; ไม่สร้างpaymentซ้ำหรือคืนโต๊ะ |

### คำพูดและรายละเอียดประกอบ

Testcaseต้องมีsetup action expectedresultและตรวจทั้งresponseกับDB ไม่ใช่ดูแค่statuscode Cookieผิดแบบcredentialinvalidต่างจากgrantถูกแต่sessionidผิด จึงอธิบายcodeจากcontractของแต่ละกรณี การcloseที่ถูกปฏิเสธต้องตรวจว่าsessionยังACTIVE/tableOCCUPIED การใช้ผลPaymentจากsessionอื่นต้องไม่ปิดรอบใหม่ให้สำเร็จ

### แหล่งอ้างอิง

- [doc/testing/requirement-test-traceability.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/requirement-test-traceability.md)
- [code/backend/src/test/java/com/buffetrestaurant/integration/DiningSessionIntegrationTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/integration/DiningSessionIntegrationTest.java)
- [code/backend/src/test/java/com/buffetrestaurant/integration/PaymentIntegrationTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/integration/PaymentIntegrationTest.java)

## หน้า 29 — Concurrency Tests บน PostgreSQL

ผู้รับช่วง: **ปวริศช์ / ศิระพัทธ์**

### ข้อความบนสไลด์

| การแข่งขัน | ผลตามผู้ได้ lock ก่อน |
| --- | --- |
| เปิดโต๊ะพร้อมกัน | มี active sessionได้หนึ่งรอบ |
| Order ก่อน bill request | Orderบันทึกเสร็จ แล้วหยุดOrderใหม่ |
| Bill request ก่อน Order | Orderถูกปฏิเสธ409 |
| Order ก่อน close | Order commit ก่อนปิดและถอนgrants |
| Close ก่อน Order | Orderเข้าไม่ได้ ไม่บันทึกหลังปิด |

### คำพูดและรายละเอียดประกอบ

SuitePostgresOrderCloseConcurrencyTestรอactualrow-lockcontention ไม่อาศัยsleepเดาว่าสองคำขอชนกัน Orderและbillrequest/Payment/closeใช้lockDiningSessionเดียวกัน TestsตรวจDBหลังcommitและทดสอบcredentialที่ถูกถอนแล้วอีกขั้น Basesเป็นDBทิ้งได้ที่มีmarkerและALLOW_DESTRUCTIVE_DB_TESTS ไม่ชี้Supabaseกลาง Automatedtestไม่ได้ลบข้อมูลทีม

### แหล่งอ้างอิง

- [code/backend/src/test/java/com/buffetrestaurant/integration/PostgresOrderCloseConcurrencyTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/integration/PostgresOrderCloseConcurrencyTest.java)
- [.github/workflows/ci.yml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/.github/workflows/ci.yml)

## หน้า 30 — ผลตรวจที่มีหลักฐานแล้ว

ผู้รับช่วง: **ศิระพัทธ์**

### ข้อความบนสไลด์

| หลักฐาน / รุ่น | ผลและขอบเขต |
| --- | --- |
| CI develop472fba4 | Backend/PostgreSQL 303/303; ไม่มีskipped |
| CI frontend | 116/116 + URLguards6/6; buildผ่าน |
| Lint | 0errors / 4warningsเดิม |
| Browserก่อนmergePR22 | 14PASS/0FAIL,17ภาพ; H2แยก/realHTTP |
| ยังต้องตรวจ Final | publicHTTPS / timedTTL / StockProfileใหม่ / release |

### คำพูดและรายละเอียดประกอบ

ตัวเลขCIมาจากrun37503690105ของdevelop472fba4 Browserทดสอบก่อนmergeด้วยtestedpatchและsourcehashตรงhead617d742 ไม่ได้รันใหม่หลังmergeและไม่ใช่มือถือจริง ใช้สองbrowsercontexts ภาพcontrolledfixturesรอบก่อนแยกจาก14CoreFlowกลุ่มจริง PR24CIhead0f1a05cผ่านสองjobsแต่เป็นdocsrevision ไม่ใช้กล่าวว่าPublicFinalผ่าน ในรุ่นส่งต้องแทนตัวเลขด้วยผลlatestreleaseที่ตรวจจริง

### แหล่งอ้างอิง

- [https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105)
- [doc/testing/sirapat-step3-premerge-report.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/sirapat-step3-premerge-report.md)
- [test/evidence/sirapat-step3-premerge-2026-10-06/verification-summary.json](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/test/evidence/sirapat-step3-premerge-2026-10-06/verification-summary.json)

## หน้า 31 — Runtime และแผน Public Deployment

ผู้รับช่วง: **ธีรเมธ**

### ข้อความบนสไลด์

| สถานะ | เส้นทาง runtime |
| --- | --- |
| Localปัจจุบัน | Vite frontend + SpringBoot + DBตาม.env |
| แผนFinal | ReactbuildในSpringBoot → RenderURLเดียว |
| Database | Supabase SessionPooler5432 + SSL |
| Publicเงื่อนไข | HTTPS / Securecookies / Origin / SPArefresh / API404 |
| ต้องตรวจหลังdeploy | restart / persistence / coldstart / commitตรงรุ่น |

### คำพูดและรายละเอียดประกอบ

นี่เป็นแผนdeploymentไม่ใช่ผลที่deployเสร็จแล้ว ปัจจุบันComposeไม่มีPostgreSQLcontainerและfrontendยังเป็นVitedevserver ธีรเมธต้องทำproductionDocker/PORT/realproviders/secrets env/rollbackrunbook SPAfallbackต้องไม่เปลี่ยนAPI404เป็นHTMLหลังrefresh RenderFreecoldstartต้องตรวจจริงและเตรียมระบบก่อนdemo PublicURL/Swaggerและdeployedcommitต้องเติมหลังมีหลักฐาน ไม่ใส่URLสมมติในสไลด์

### แหล่งอ้างอิง

- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)
- [doc/system-design/README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/system-design/README.md)
- [docker-compose.yml](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/docker-compose.yml)

## หน้า 32 — ลำดับ Demo และสิ่งส่งมอบ

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

Manager: ข้อมูลร้านและบัญชี → Staff: เปิดโต๊ะและQR

Customer: สั่ง → Kitchen: PREPARING/READY → Staff: SERVED

Customer: ขอคิดบิล → Staff: PAID → close → AVAILABLE

ส่ง: repo / diagrams / SOLID–Patterns / TestReport / CanvaPDF

ก่อนส่ง: release reviewed / mainตรงdeployedcommit / publicsmoke

### คำพูดและรายละเอียดประกอบ

เตรียมบัญชีทดสอบต่างroleและbrowsercontextsแยก ใช้ข้อมูลทดสอบชื่อเฉพาะ ไม่แก้DBด้วยมือระหว่างflow ไม่โชว์รหัสQRcookieหรือpasswordบนสไลด์ กรณีเดโมpublicล่มใช้หลักฐานสำรองที่บอกenvironmentชัดเจน แต่หลักฐานสำรองไม่ได้แทนข้อกำหนดpublicdeployment สมาชิกทุกคนต้องอธิบายส่วนตนและซ้อมการส่งช่วง

### แหล่งอ้างอิง

- [doc/planning/step3-requirement-matrix.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/planning/step3-requirement-matrix.md)
- [doc/planning/step3-final-plan.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/planning/step3-final-plan.md)

## หน้า 33 — API ตัวอย่างที่ใช้ใน Core Flow

ผู้รับช่วง: **ศรัณย์**

### ข้อความบนสไลด์

| Method / endpoint | สิทธิ์ / งาน |
| --- | --- |
| POST /dining-sessions | SERVICE_STAFF เปิดรอบ |
| POST /dining-sessions/qr-exchange | bodytoken → customer cookie |
| GET /dining-sessions/{id}/menu | customercookie / อ่านเมนูแพ็กเกจ |
| POST /dining-sessions/{id}/orders | customercookie / สร้างออเดอร์ |
| PATCH /orders/{id}/status | loginrole / เปลี่ยนสถานะถัดไป |
| POST /dining-sessions/{id}/bill-request | customercookie / หยุดสั่งเพิ่ม |
| POST /dining-sessions/{id}/close | SERVICE_STAFF / ตรวจPAID |

### คำพูดและรายละเอียดประกอบ

ทุกendpointมีprefix/api/v1 ตารางเป็นตัวอย่างที่ตรวจจากController ไม่ใช่Swaggerทั้งระบบ รายละเอียดrequest/response/statusดูOpenAPIของreleaseที่จะส่ง APIแลกQRไม่มีGETtokenในpath และCustomerไม่ได้ใช้sessionidล้วนเพื่ออ่านข้อมูล ก่อนรับรองFinalศรัณย์ต้องตรวจStock/ProfileDTOและpublicSwaggerเพิ่ม

### แหล่งอ้างอิง

- [code/backend/src/main/java/com/buffetrestaurant/controller/CustomerOrderingController.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/CustomerOrderingController.java)
- [code/backend/src/main/java/com/buffetrestaurant/controller/OrderFulfillmentController.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/OrderFulfillmentController.java)
- [code/backend/src/main/java/com/buffetrestaurant/controller/CustomerBillingController.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/CustomerBillingController.java)
- [code/backend/src/main/java/com/buffetrestaurant/controller/DiningSessionController.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/DiningSessionController.java)

## หน้า 34 — ข้อขาด SOLID ที่ต้องปิดก่อนรับรอง

ผู้รับช่วง: **ปวริศช์ / ศรัณย์**

### ข้อความบนสไลด์

| Gap | งานแก้ / หลักฐานรับรอง |
| --- | --- |
| G01 | EntityManager: constructor injection + bill/concurrencytests |
| G02 | Stock/Authconcretecollaborators: ตรวจusecaseinterfaces |
| G03 | ย้ายsessionkey ให้Serviceไม่importController |
| G04 | ทบทวนauthorization/context/engineboundaries |
| G05 | อธิบายStatefactoryswitchtradeoffโดยowner |

### คำพูดและรายละเอียดประกอบ

G01–G04เป็นช่องว่างโค้ดจริงไม่ใช่แค่เติมคำในreport เจ้าของแต่ละโมดูลต้องรับงานแก้และreviewแยกจากdocsPR เมื่อผ่านให้updatecodeSHAและสไลด์D ตัวอย่างconstructormethodเพียงหนึ่งจุดไม่รับรองทั้งระบบ และไม่สร้างinterfaceเปล่าเพื่อให้ครบชื่อpatternหรือSOLID

### แหล่งอ้างอิง

- [doc/solid-analysis.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/solid-analysis.md)

## หน้า 35 — ตัวอย่างการคำนวณราคา Snapshot

ผู้รับช่วง: **ธีรเมธ**

### ข้อความบนสไลด์

ตัวอย่างสมมติ: เปิดรอบราคา399บาท ผู้ใหญ่2 เด็ก1

ผู้ใหญ่: 399 × 2 = 798.00

เด็ก: 399 × 1 × 0.5 = 199.50

ไม่มีโปรโมชั่น: total = 997.50 บาท

แก้ราคาแพ็กเกจเป็น499ภายหลัง → รอบเดิมยังใช้399

### คำพูดและรายละเอียดประกอบ

ตัวเลขนี้เป็นworkedexampleตามconfig0.5และsnapshot ไม่ใช่ข้อมูลลูกค้าจริง ไม่เรียกCItestresult Discountcontextbaselineเป็นnullจึงไม่มีpromotionลดเพิ่ม Engineใช้BigDecimalและroundHALF_UP2decimal Paymentบันทึกยอดbackendเดียวกัน ถ้าทีมเปลี่ยนpolicyเด็กหรือส่วนลด ต้องตรวจconfig/testsแล้วปรับตัวอย่างใหม่

### แหล่งอ้างอิง

- [code/backend/src/main/java/com/buffetrestaurant/config/BillingConfig.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/config/BillingConfig.java)
- [code/backend/src/main/java/com/buffetrestaurant/service/billing/StandardBillCalculation.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/StandardBillCalculation.java)
- [code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java)

## หน้า 36 — ตัวอย่าง Stock-in และ Adjustment

ผู้รับช่วง: **เมธัส**

### ข้อความบนสไลด์

| ตัวอย่างสมมติ | Delta / ยอดหลังรายการ |
| --- | --- |
| เริ่มต้น | quantity = 10.000 kg |
| รับเข้า3.500kg | IN +3.500 → 13.500kg |
| ปรับลด1.250kg | ADJUSTMENT −1.250 → 12.250kg |
| ปรับจนยอดติดลบ | ปฏิเสธและrollback ไม่เกิดauditปลอม |
| History | type / delta / balance_after / reason / user |

### คำพูดและรายละเอียดประกอบ

Quantityใช้precision12scale3 ตัวอย่างไม่ได้ทำรายการจริงบนฐานกลาง Masterformเพิ่มitemเริ่มquantity0และไม่ให้แก้quantityตรง การเปลี่ยนยอดต้องผ่านtransactionhistory เหตุผลและactorจำเป็น Adjustmentเป็นsigneddelta ผู้ใช้ต้องส่งผลต่างไม่ใช่ยอดนับใหม่ ส่วนtarget/activeเป็นFinalpendingก่อนรวม

### แหล่งอ้างอิง

- [code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java)
- [doc/database/auth-stock-schema-delta.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/database/auth-stock-schema-delta.md)

## หน้า 37 — Schema และข้อมูลที่รักษาเป็นประวัติ

ผู้รับช่วง: **เมธัส**

### ข้อความบนสไลด์

| ข้อมูล | กลไก |
| --- | --- |
| ราคาเมื่อเปิดรอบ | dining_sessions.package_price_at_open |
| ชื่อโต๊ะ / อาหาร | orders.table_number / order_items.item_name |
| เวลาขอคิดบิล | bill_requested_at nullable OffsetDateTime |
| เวลาชำระ | paid_at nullableก่อนPAID + OffsetDateTime |
| เริ่ม / จบรอบ | start_time / end_time ใช้LocalDateTimeจริง |

### คำพูดและรายละเอียดประกอบ

Snapshotทำให้การแก้masterไม่เปลี่ยนความหมายของhistory แต่ต้องทดสอบmigrationและข้อมูลเก่าจริง ค่าenumเก็บเป็นstring Quantityต้องบวก และFKมีindexรองรับqueryตามเหตุผล V1–V14ถูกapplyตามรายงานStep2 ไม่ได้เชื่อมฐานกลางตรวจสดในงานslidesนี้ เลขmigrationFinalต้องจองจากhistoryจริงก่อนสร้าง ไม่เดาจากinstalled_rank

### แหล่งอ้างอิง

- [doc/system-design/README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/system-design/README.md)
- [doc/database/step2-schema-approved.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/database/step2-schema-approved.md)
- [doc/testing/pavarit-step2-completion-report.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/pavarit-step2-completion-report.md)

## หน้า 38 — Frontend Regression และ Race Conditions

ผู้รับช่วง: **ศิระพัทธ์**

### ข้อความบนสไลด์

| กรณี | สิ่งที่ต้องตรวจ |
| --- | --- |
| StrictModeeffectซ้ำ | QRtokenเดียว in-flightมีPOSTเดียว |
| สแกนA→B→A | คำขอเรียงและหน้า/cookieตรงQRล่าสุด |
| QRใหม่แลกไม่สำเร็จ | ไม่กลับไปโชว์สิทธิ์/รอบเดิมอัตโนมัติ |
| historyมาก่อนPOSTack | Orderไม่ซ้ำและไม่ถูกลบจากresponseเก่า |
| sessionหมดสิทธิ์ | protected401พากลับlogin; timedTTLตรวจเพิ่ม |

### คำพูดและรายละเอียดประกอบ

Componenttestsสามารถควบคุมdelayเพื่อบังคับraceและตรวจผลช้าไม่ทับstateใหม่ แต่ต้องแยกfixturesออกจากrealHTTPE2E กรณีQRใช้แล้วเปิดใหม่ต้องยิงคำขอใหม่ให้backendปฏิเสธ ไม่cachepromiseสำเร็จตลอดเวลา Currentlocalreportพิสูจน์server-sideinvalidation ไม่เท่ากับรอsessionTTLหมดตามเวลาบนpublic

### แหล่งอ้างอิง

- [doc/testing/requirement-test-traceability.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/requirement-test-traceability.md)
- [doc/testing/sirapat-step3-premerge-report.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/sirapat-step3-premerge-report.md)
- [doc/contracts/ordering-contract.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/contracts/ordering-contract.md)

## หน้า 39 — Release และ Checklist ก่อนนำเสนอ

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

Stock/Profile + SOLIDgaps ผ่านcode/tests/review

PublicHTTPS + realproviders + migrationรับรอง

Finalbackend/PG/frontend + browserครบทุกrole

Canva/PDFตรงrelease · สมาชิกตรวจ/ซ้อมครบ5คน

releasePR→mainตรงdeployedSHA · URL/Swaggerเปิดจริง

### คำพูดและรายละเอียดประกอบ

Checklistทั้งหมดนี้ยังไม่ติ๊กจากการสร้างสไลด์ งานreviewGitmeaningfulcommitsและcontributionต้องตรวจตามจริง ก่อนนำเสนอเปิดsystemเตรียมcoldstartและทดสอบpublicURLsอีกครั้ง เก็บCanvaเป็นต้นฉบับและexportPDFจากCanvaของรุ่นที่ยืนยันแล้วไว้doc/slide ไม่อ้างPPTXร่างเก่าว่าเป็นชุดส่ง

### แหล่งอ้างอิง

- [doc/planning/step3-requirement-matrix.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/planning/step3-requirement-matrix.md)
- [doc/planning/step3-git-audit.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/planning/step3-git-audit.md)
- [doc/planning/step3-final-plan.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/planning/step3-final-plan.md)

## หน้า 40 — แหล่งอ้างอิงและหลักฐานรายวิชา

ผู้รับช่วง: **ปวริศช์**

### ข้อความบนสไลด์

| สิ่งที่ตรวจ | ไฟล์หลัก |
| --- | --- |
| Architecture / Diagram | doc/system-design + doc/diagrams |
| SOLID / Patterns | doc/solid-analysis.md + doc/design-patterns.md |
| API / Data | doc/contracts + doc/database |
| Tests / Environment | doc/testing + test/evidence + CIrun |
| Course / Git / Owners | requirementmatrix + Gitaudit + README |

### คำพูดและรายละเอียดประกอบ

ใบงานวิชาข้อ2กำหนดTesting ข้อ10กำหนดHowtoRunTestsในREADME และข้อ14กำหนดtestsผ่านพร้อมTestReportแต่ไม่ได้บังคับจำนวนหน้าสไลด์tests ทีมเลือกเล่าอย่างละเอียดเพื่อแสดงว่าออกแบบและเชื่อมระบบแล้วตรวจอย่างไร Sourcesของทุกหน้าอ้างbaseline472fba4ก่อนFinalownerconfirmation URLจริงและผลreleaseต้องupdateก่อนส่ง เก็บlinkCanvaกับPDFversionและchangelogในrepo

### แหล่งอ้างอิง

- [doc/planning/step3-requirement-matrix.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/planning/step3-requirement-matrix.md)
- [doc/testing/pavarit-step3-docs-report.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/doc/testing/pavarit-step3-docs-report.md)
- [README.md](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0f1a05c38bc6baa5436369d45a6545ea030a64d1/README.md)
