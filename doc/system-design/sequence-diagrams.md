# Sequence diagrams

ต้นทาง: [Sequence diagrams ใน Notion](https://app.notion.com/p/3d7cb2e9d47a803fbb26e1b7e5dbdf65) · ย้ายเมื่อ 28 กันยายน 2026 · เป็น design baseline; ดู [สถานะเทียบกับโค้ด](README.md#สถานะเทียบกับโค้ด)

# Sequence Diagrams — Buffet Restaurant Management System
แสดง Interaction ระหว่าง Actors, Boundary (Controllers), Control (Services), Entity (Repositories/Domains) ตามสถาปัตยกรรม **Layered Architecture** และแสดงการทำงานของ **Design Patterns** ที่เกี่ยวข้องอย่างสมบูรณ์
---
## Scenario 1: Open Dining Session & QR Code Generation
พนักงานบริการ (Service Staff) ตรวจสอบโต๊ะว่าง เปิดรอบการรับประทาน (Dining Session) ระบุจำนวนคน เลือก Buffet Package และน้ำซุป ระบบเปลี่ยนสถานะโต๊ะเป็น `OCCUPIED` และสร้าง `sessionToken` สำหรับ QR Code ประจำโต๊ะ
[PlantUML: sequence-open-session](../diagrams/sequence-open-session.puml)
### คำอธิบาย Flow Scenario 1:
1. **ตรวจสอบโต๊ะว่าง**: พนักงานบริการเรียกดูโต๊ะที่มีสถานะ `AVAILABLE`
2. **เปิด Session**: พนักงานส่งข้อมูลเปิดโต๊ะ (โต๊ะ, จำนวนผู้ใหญ่, จำนวนเด็ก, รหัสแพ็กเกจ, รหัสน้ำซุป)
3. **ตรวจสอบและล็อกโต๊ะ**: Service ตรวจสอบความถูกต้องและปรับสถานะโต๊ะเป็น `OCCUPIED` ทันทีเพื่อป้องกัน Race Condition
4. **สร้าง Dining Session & QR**: ระบบสร้าง `DiningSession` และรหัส QR แบบใช้แลกครั้งเดียว Staff UI สร้างลิงก์ `/customer/qr#token=...` จาก response แล้วแสดง QR ให้ลูกค้า
---
## Scenario 2: Customer Ordering & Kitchen Order Flow (GoF State Pattern)
ลูกค้าสแกน QR สั่งอาหารรอบต่อรอบ (เมนูถูกกรองตาม Package) $`\rightarrow`$ ครัวรับออเดอร์และปรุงอาหารตาม **State Pattern** (`RECEIVED` $`\rightarrow`$ `PREPARING` $`\rightarrow`$ `READY`) โดยครัวไม่สามารถกดยืนยัน SERVED ได้ $`\rightarrow`$ พนักงานบริการนำส่งและกดยืนยัน `SERVED`
[PlantUML: sequence-ordering-kitchen](../diagrams/sequence-ordering-kitchen.puml)
### คำอธิบาย Flow Scenario 2:
1. **แลก QR และดูเมนูตามสิทธิ์**: หน้า Customer ล้าง fragment ทันที แล้วส่งรหัสใน body ของ `POST /api/v1/dining-sessions/qr-exchange` Backend หมุนรหัส QR และออก cookie `HttpOnly` แยกต่อเครื่อง คำขอดูเมนูและสั่งอาหารใช้ cookie เพื่อตรวจรอบที่ยัง `ACTIVE` ก่อนคืนเฉพาะเมนูของ Package
2. **สร้าง Order**: คำสั่งอาหารถูกสร้างและกำหนด State เริ่มต้นเป็น `ReceivedState` (`RECEIVED`)
3. **การเปลี่ยน State โดยห้องครัว**:
	- เมื่อกดรับออเดอร์ `Order.nextState()` จะเปลี่ยนสถานะเป็น `PreparingState` (`PREPARING`)
	- เมื่อปรุงเสร็จ `Order.nextState()` จะเปลี่ยนสถานะเป็น `ReadyState` (`READY`)
	- *ข้อกำหนด*: สิทธิ์ในครัวสิ้นสุดที่ `READY` เท่านั้น
4. **การเปลี่ยน State โดยพนักงานบริการ**: พนักงานบริการนำอาหารไปส่งที่โต๊ะ และกดยืนยันเสิร์ฟ ส่งผลให้ `Order.nextState()` ขยับเข้าสู่ `ServedState` (`SERVED`)
---
## Scenario 3: Billing, Calculation (Strategy Pattern) & Payment Processing
ลูกค้าขอเช็กบิล $`\rightarrow`$ พนักงานบริการเรียกดูยอดบิล $`\rightarrow`$ ระบบคำนวณราคาด้วย **Strategy Pattern** (Standard, Child Rate, Promotion) $`\rightarrow`$ ลูกค้าเลือกวิธีชำระ (CASH / QR / CARD) $`\rightarrow`$ พนักงานบันทึกการชำระเงิน $`\rightarrow`$ พนักงานกดปิดรอบแยกหลังตรวจ `PAID` ของรอบเดียวกัน แล้วระบบปิด `DiningSession` (`COMPLETED`) และคืนโต๊ะเป็น `AVAILABLE`
[PlantUML: sequence-billing-payment](../diagrams/sequence-billing-payment.puml)
### คำอธิบาย Flow Scenario 3:
1. **คำนวณบิลด้วย Strategy Pattern**: `BillCalculator` รับ Session แล้วประมวลผลคำนวณค่าบริการตาม Strategy ต่าง ๆ อย่างยืดหยุ่น:
	- `StandardCalculationStrategy`: คำนวณผู้ใหญ่ตามราคาเต็มของ Package
	- `ChildRateCalculationStrategy`: คำนวณส่วนลดตามเรตราคาเด็ก
	- `PromotionDiscountStrategy`: คำนวณส่วนลดพิเศษตามโปรโมชันของร้าน
2. **การบันทึกการชำระเงิน**: ลูกค้าชำระเงินผ่านช่องทางใดช่องทางหนึ่ง (CASH, QR, CARD) และพนักงานบริการเป็นผู้ยืนยันการรับชำระ
3. **ขอคิดบิล**: Customer cookie ของรอบ ACTIVE เรียก POST bill-request บันทึก bill_requested_at ครั้งเดียวและหยุดรับออเดอร์ใหม่ทุกเครื่อง ใช้ lock แถวรอบเดียวกับ Order/close หน้า Customer อ่าน GET bill-status และยอดจาก BillingEngine/ราคา snapshot; Staff เห็น badge แล้วรับชำระบนหน้าเดิม
4. **Transaction & Resource Release**: เมื่อบันทึก `Payment` แล้วรอบยัง ACTIVE พนักงานต้องกด close แยก และระบบตรวจ PAID ของรอบเดียวกันก่อน:
	- เปลี่ยนสถานะ `DiningSession` เป็น `COMPLETED` พร้อมประทับเวลา `endTime`
	- ยกเลิกสิทธิ์ cookie ลูกค้าทุกเครื่องและไม่ให้แลก QR ของรอบที่ปิดแล้ว
	- คืนสถานะของ `RestaurantTable` ให้เป็น `AVAILABLE` เพื่อเตรียมพร้อมรับลูกค้ากลุ่มถัดไป
