# Use cases

ต้นทาง: [Use cases ใน Notion](https://app.notion.com/p/3d3cb2e9d47a80819cf9e6b549adba17) · ย้ายเมื่อ 28 กันยายน 2026 · เป็น design baseline; ดู [สถานะเทียบกับโค้ด](README.md#สถานะเทียบกับโค้ด)

[PlantUML: use-case](../diagrams/use-case.puml)

## Use Case Descriptions (ตัวหลัก)
### UC-01 Open Dining Session
- **Primary Actor:** Staff
- **Preconditions:** Staff เข้าสู่ระบบแล้ว และมีโต๊ะสถานะ Available
- **Main Flow:**
	1. Staff เปิดหน้าจัดการโต๊ะ
	2. ระบบแสดงสถานะโต๊ะทั้งหมด
	3. Staff เลือกโต๊ะที่ว่าง
	4. Staff ระบุจำนวนลูกค้า
	5. Staff เปิด Dining Session
	6. ระบบสร้าง Dining Session และผูกกับโต๊ะ
- **Alternative / Exception:** หากไม่มีโต๊ะว่าง ระบบแจ้งว่าไม่มีโต๊ะ Available และไม่สร้าง Dining Session
- **Postconditions:** มี Dining Session ที่ Active และผูกกับโต๊ะหนึ่งโต๊ะ
### UC-02 Setup Dining Session
- **Primary Actor:** Staff
- **Preconditions:** มี Dining Session ที่ Active
- **Main Flow:**
	1. ระบบเปลี่ยนสถานะโต๊ะเป็น Occupied
	2. Staff เลือก Buffet Package ที่ลูกค้าต้องการ โดยแต่ละ Package มีราคาและชุดเมนูที่แตกต่างกัน
	3. Staff เลือกน้ำซุป
	4. ระบบบันทึก Package และข้อมูลการรับประทานใน Dining Session
	5. ระบบเปิดใช้งาน QR ของโต๊ะสำหรับ Dining Session ปัจจุบัน
- **Alternative / Exception:** หาก Package ที่เลือกไม่ Available ระบบไม่ให้ยืนยันและให้เลือกใหม่
- **Postconditions:** Dining Session มี Buffet Package ที่กำหนดชัดเจน และ Customer จะเห็นเฉพาะเมนูที่อนุญาตสำหรับ Package นั้นผ่าน QR
### UC-03 Place Order
- **Primary Actor:** Customer
- **Preconditions:** โต๊ะมี Dining Session ที่ Active และ QR สามารถเข้าถึง Session ได้
- **Main Flow:**
	1. Customer สแกน QR ของโต๊ะ
	2. ระบบอ่าน Buffet Package ของ Dining Session และแสดงเฉพาะเมนูที่ Package นั้นสั่งได้
	3. Customer เลือกรายการอาหารและจำนวน
	4. Customer ยืนยันการสั่ง
	5. ระบบสร้าง Order สำหรับรอบการสั่งนั้น
	6. ระบบสร้าง Order Items จากรายการที่เลือก
	7. ระบบตั้งสถานะ Order เป็น RECEIVED
	8. Order ปรากฏในหน้าของ Kitchen Staff
- **Alternative / Exception:** หากรายการอาหารไม่ Available หรือไม่อยู่ใน Buffet Package ของ Session ระบบแจ้งและไม่เพิ่มรายการนั้นใน Order
- **Postconditions:** มี Order ใหม่ที่ผูกกับ Dining Session และพร้อมให้ครัวดำเนินการ
### UC-04 Update Order Status
- **Primary Actor:** Kitchen Staff
- **Preconditions:** มี Order ที่อยู่ในสถานะ RECEIVED หรือ PREPARING
- **Main Flow:**
	1. Kitchen Staff เปิดรายการ Incoming Orders
	2. Kitchen Staff เลือก Order
	3. เปลี่ยนสถานะจาก RECEIVED เป็น PREPARING เมื่อเริ่มเตรียม
	4. เมื่อเตรียมเสร็จ เปลี่ยนสถานะเป็น READY
	5. ระบบบันทึกสถานะล่าสุดของ Order
- **Alternative / Exception:** ระบบไม่อนุญาตการเปลี่ยนสถานะที่ข้ามลำดับหรือไม่ถูกต้อง
- **Postconditions:** Order มีสถานะล่าสุดที่สะท้อนขั้นตอนการทำอาหาร
### UC-05 Serve Order
- **Primary Actor:** Staff
- **Preconditions:** Order อยู่ในสถานะ READY
- **Main Flow:**
	1. Staff ดูรายการ Order ที่ READY
	2. Staff นำอาหารไปเสิร์ฟยังโต๊ะที่เกี่ยวข้อง
	3. Staff ยืนยันว่าเสิร์ฟแล้ว
	4. ระบบเปลี่ยนสถานะ Order เป็น SERVED
- **Alternative / Exception:** หาก Order ไม่ได้อยู่สถานะ READY ระบบไม่ให้ยืนยันการเสิร์ฟ
- **Postconditions:** Order อยู่ในสถานะ SERVED
### UC-06 Process Payment
- **Primary Actor:** Staff
- **Secondary Actor:** Customer
- **Preconditions:** Customer ขอเช็กบิล และ Dining Session ยัง Active
- **Main Flow:**
	1. Staff เปิดรายละเอียด Dining Session
	2. ระบบคำนวณยอดจาก Buffet Package ค่าเพิ่ม และส่วนลดถ้ามี
	3. Staff ตรวจสอบยอด
	4. Customer เลือกวิธีชำระ CASH / QR / CARD
	5. Staff ยืนยันการชำระเงิน
	6. ระบบบันทึก Payment
	7. ระบบแสดงหรือออกใบเสร็จ
- **Alternative / Exception:** หากการชำระยังไม่สำเร็จ ระบบไม่บันทึก Payment เป็น Completed และยังไม่อนุญาตให้ปิด Session
- **Postconditions:** มี Payment ที่ผูกกับ Dining Session และระบุวิธี/สถานะการชำระ
### UC-07 Close Dining Session
- **Primary Actor:** Staff
- **Preconditions:** Dining Session มีการชำระเงินสำเร็จแล้ว
- **Main Flow:**
	1. Staff เลือกปิด Dining Session
	2. ระบบตรวจสอบสถานะ Payment
	3. ระบบเปลี่ยน Dining Session เป็น Closed
	4. ระบบเปลี่ยนสถานะโต๊ะเป็น Available
	5. QR ของโต๊ะไม่สามารถใช้สั่งอาหารใน Session เดิมได้อีก
- **Alternative / Exception:** หากยังไม่มี Payment ที่สำเร็จ ระบบไม่อนุญาตให้ปิด Dining Session
- **Postconditions:** Dining Session ปิดสมบูรณ์และโต๊ะพร้อมรับลูกค้ารอบถัดไป
### UC-08 Record Stock Movement
- **Primary Actor:** Head Staff / Supervisor
- **Preconditions:** Head Staff เข้าสู่ระบบแล้ว
- **Main Flow:**
	1. Head Staff เปิดหน้าจัดการ Stock
	2. ระบบแสดง Stock Item ปริมาณคงเหลือ ปริมาณขั้นต่ำ และสถานะ Stock
	3. Head Staff บันทึก Stock In เมื่อรับวัตถุดิบเข้า
	4. ระบบสร้าง Stock Transaction และเพิ่มจำนวนคงเหลือ
	5. Head Staff บันทึก Stock Adjustment จากการตรวจนับจริง พร้อมเหตุผล
	6. ระบบสร้าง Stock Transaction และอัปเดตจำนวนคงเหลือ
- **Alternative / Exception:** หากจำนวนที่กรอกไม่ถูกต้อง ระบบไม่บันทึก Transaction
- **Postconditions:** ปริมาณ Stock ปัจจุบันและประวัติ Stock Transaction ถูกอัปเดต
