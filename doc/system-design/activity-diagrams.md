# Activity diagrams

ต้นทาง: [Activity diagrams ใน Notion](https://app.notion.com/p/3d7cb2e9d47a8033945bcc7c5d4801eb) · ย้ายเมื่อ 28 กันยายน 2026 · เป็น design baseline; ดู [สถานะเทียบกับโค้ด](README.md#สถานะเทียบกับโค้ด)

## 1. Activity Diagram — Customer Dining & Ordering
ครอบคลุมตั้งแต่ลูกค้าเข้าร้าน เปิดรอบบุฟเฟต์ สั่งอาหารผ่าน QR และสามารถสั่งอาหารเพิ่มได้ตลอดรอบ
[PlantUML: activity-customer-ordering](../diagrams/activity-customer-ordering.puml)
## 2. Activity Diagram — Kitchen Order Processing
แสดง **Workflow และ State Transition ของ Order** ตาม GoF State Pattern (ครัวรับผิดชอบถึงสถานะ `READY` เท่านั้น)
[PlantUML: activity-kitchen](../diagrams/activity-kitchen.puml)
## 3. Activity Diagram — Payment & Close Dining Session
รวมหน้าที่ Cashier เข้าเป็น **Service Staff** ตามโครงสร้างทีม และแสดงขั้นตอนการคำนวณบิล การชำระเงิน และการปิด Session คืนโต๊ะ
[PlantUML: activity-payment-close](../diagrams/activity-payment-close.puml)
## 4. Activity Diagram — Stock Management
แสดงการจัดการวัตถุดิบและของใช้คงคลังแบบ Simple Stock (Stock In และ Stock Adjustment)
[PlantUML: activity-stock](../diagrams/activity-stock.puml)
