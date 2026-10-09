# Activity diagrams

ต้นทาง: [Activity diagrams ใน Notion](https://app.notion.com/p/3d7cb2e9d47a8033945bcc7c5d4801eb) · ย้ายเมื่อ 28 กันยายน 2026 · ปรับ source/preview ให้ตรง develop472fba4 วันที่7ตุลาคม2026; ดู [สถานะเทียบกับโค้ด](README.md#สถานะเทียบกับโค้ด)

## 1. Activity Diagram — Customer Dining & Ordering
ครอบคลุมตั้งแต่ลูกค้าเข้าร้าน เปิดรอบบุฟเฟต์ สั่งอาหารผ่าน QR/cookie และสั่งเพิ่มได้จนขอคิดบิล ทุกมือถือหยุดสั่งเพิ่มหลัง bill request
[PlantUML: activity-customer-ordering](../diagrams/activity-customer-ordering.puml)
## 2. Activity Diagram — Kitchen Order Processing
แสดง **Workflow และ State Transition ของ Order** ตาม GoF State Pattern (ครัวรับผิดชอบถึงสถานะ `READY` เท่านั้น)
[PlantUML: activity-kitchen](../diagrams/activity-kitchen.puml)
## 3. Activity Diagram — Payment & Close Dining Session
รวมหน้าที่ Cashier เข้าเป็น **Service Staff** ตามโครงสร้างทีม และแสดงขั้นตอนการคำนวณบิล การชำระเงิน และการปิด Session คืนโต๊ะ
[PlantUML: activity-payment-close](../diagrams/activity-payment-close.puml)
## 4. Activity Diagram — Stock Management
แสดงการจัดการวัตถุดิบและของใช้คงคลัง (Manager สร้าง/แก้/เปิด-ปิดใช้งาน item; Manager/Supervisor Stock In และ Stock Adjustment) item ที่ inactive ปฏิเสธ stock-in/adjust ด้วย 409 ตาม V15
[PlantUML: activity-stock](../diagrams/activity-stock.puml)
