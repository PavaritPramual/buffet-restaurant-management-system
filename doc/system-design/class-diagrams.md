# Class diagrams

ต้นทาง: [Class diagrams ใน Notion](https://app.notion.com/p/3d7cb2e9d47a804ea688c7367a0a0190) · ย้ายเมื่อ 28 กันยายน 2026 · เป็น design baseline; ดู [สถานะเทียบกับโค้ด](README.md#สถานะเทียบกับโค้ด)

# Class Diagrams — Buffet Restaurant Management System
แบ่งตาม Subsystem และแสดงสถาปัตยกรรม Layered Architecture พร้อมการประยุกต์ใช้ **GoF Behavioral Patterns ครบ 3 รูปแบบ** ตามเกณฑ์วิชา:
---
## 1. Table & Dining Session Management
ดูแลการจัดการโต๊ะ เปิดรอบการรับประทานอาหาร (Dining Session) และผูก `sessionToken` สำหรับ QR Code ประจำโต๊ะ
[PlantUML: class-table-session](../diagrams/class-table-session.puml)
---
## 2. Menu & Order Domain
แสดงความสัมพันธ์ระหว่างเมนูอาหาร, หมวดหมู่, แพ็กเกจบุฟเฟต์ (Many-to-Many), คำสั่งซื้อ และรายการในคำสั่งซื้อ
[PlantUML: class-menu-order](../diagrams/class-menu-order.puml)
---
## 3. Kitchen & Order State Pattern (GoF Behavioral Pattern #1)
ประยุกต์ใช้ **State Pattern** จัดการสถานะของ `Order` โดยให้คลาส `Order` ทำหน้าที่เป็น Context คอยกระจายความรับผิดชอบในการเปลี่ยนสถานะไปยัง Concrete States (ครัวเลื่อนสถานะได้ถึง READY และพนักงานเสิร์ฟเลื่อนไป SERVED)
[PlantUML: class-order-state](../diagrams/class-order-state.puml)
---
## 4. Payment & Billing Strategy Pattern (GoF Behavioral Pattern #2)
ประยุกต์ใช้ **Strategy Pattern** สำหรับระบบคำนวณราคาและส่วนลด (ผู้ใหญ่ปกติ, อัตราพิเศษของเด็ก, และโปรโมชันลดราคา) โดย `BillCalculator` เป็น Context
[PlantUML: class-billing-strategy](../diagrams/class-billing-strategy.puml)
---
## 5. Stock Management Template Method Pattern (GoF Behavioral Pattern #3)
ประยุกต์ใช้ **Template Method Pattern** กำหนดโครงสร้าง Template ในการทำธุรกรรมสต็อก (ตรวจสอบความถูกต้อง $`\rightarrow`$ คำนวณยอดใหม่ $`\rightarrow`$ อัปเดตสต็อก $`\rightarrow`$ บันทึกประวัติ $`\rightarrow`$ เขียนบันทึก Audit Log) โดยแยกการทำงานระหว่างการรับเข้า (`StockIn`) และการปรับยอดนับจริง (`StockAdjustment`)
[PlantUML: class-stock-template](../diagrams/class-stock-template.puml)
---
## 6. User & Authentication
จัดการข้อมูลผู้ใช้งานและยืนยันตัวตนสำหรับ Staff และ Manager
[PlantUML: class-auth](../diagrams/class-auth.puml)
