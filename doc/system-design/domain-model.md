> อัปเดตส่วน Table/Session, Domain, State/Strategy/Stock diagrams ณ 7 ต.ค.2026 จาก develop472fba4 ส่วน Menu/Auth/Ordering baseline ต้อง owner ตรวจ Finalอีกครั้ง QR flow ปัจจุบันดู [QR exchange](../diagrams/sequence-qr-exchange.puml)

# Domain model

ต้นทาง: [Domain model ใน Notion](https://app.notion.com/p/3d7cb2e9d47a807fbddfdc5810ac154a) · ย้ายเมื่อ 28 กันยายน 2026 · เป็น design baseline; ดู [สถานะเทียบกับโค้ด](README.md#สถานะเทียบกับโค้ด)

[PlantUML: domain-model](../diagrams/domain-model.puml)
## Domain Model / Conceptual Class Diagram
Domain Model คือแบบจำลองโครงสร้างของระบบในระดับแนวคิด ใช้แสดง **Entity หรือสิ่งสำคัญที่มีอยู่ในระบบ, Attribute ของแต่ละ Entity และความสัมพันธ์ระหว่าง Entity** โดยยังไม่ลงรายละเอียดในระดับการเขียนโปรแกรม เช่น Method, Controller, Service หรือ Repository
สำหรับระบบจัดการร้านอาหารบุฟเฟต์ ได้ปรับปรุงตามข้อตกลง Scope ล่าสุด:
1. **ตัด ****`Customer`**** Entity ออก**: เนื่องจากเป็นร้านบุฟเฟต์แบบ Walk-in ไม่มีระบบสมาชิกลูกค้า ลูกค้าแลก QR token ของ `DiningSession` ครั้งเดียว แล้วใช้ credential ใน cookie ที่ผูกกับรอบกินผ่าน `CustomerSessionGrant`
2. **ความสัมพันธ์ Many-to-Many ระหว่าง ****`BuffetPackage`**** และ ****`MenuItem`**: แต่ละ Package บุฟเฟต์สามารถรวมรายการอาหารได้หลายเมนู และอาหารหนึ่งจานสามารถอยู่ในหลาย Package ได้
3. **ปรับ ****`Ingredient`**** เป็น ****`StockItem`**: จัดเก็บสต็อกวัตถุดิบและของใช้ในร้านแบบ Simple Stock ควบคู่กับ `StockTransaction`
### คำอธิบาย Entity

| Entity | คำอธิบาย |
| --- | --- |
| **UserAccount** | เก็บข้อมูลบัญชีผู้ใช้งานระบบและบทบาท เช่น พนักงานบริการ (SERVICE_STAFF), พนักงานครัว (KITCHEN_STAFF), หัวหน้าพนักงาน (SUPERVISOR), และผู้จัดการ (MANAGER) |
| **RestaurantTable** | แทนโต๊ะในร้าน ประกอบด้วยหมายเลขโต๊ะ ความจุ และสถานะของโต๊ะ (AVAILABLE / OCCUPIED) |
| **DiningSession** | แทนรอบการเข้าใช้บริการของลูกค้าในโต๊ะนั้น ๆ บันทึกจำนวนลูกค้า, ราคาแพ็กเกจ ณ เวลาเปิด, เวลาเริ่ม-สิ้นสุด, สถานะ (ACTIVE / COMPLETED), และ QR token ที่หมุนหลังแลก |
| **CustomerSessionGrant** | สิทธิ์ลูกค้าแต่ละเครื่อง เก็บเฉพาะ hash ของ credential ใน cookie, เวลาออกและหมดอายุ ผูกกับ DiningSession โดยไม่มีบัญชีลูกค้า |
| **BuffetPackage** | เก็บข้อมูลประเภทบุฟเฟต์และราคาต่อหัว (เช่น Standard, Premium) ผูกกับชุดรายการอาหารที่สั่งได้ |
| **Soup** | เก็บข้อมูลประเภทน้ำซุปที่ลูกค้าเลือกในรอบการรับประทาน |
| **MenuCategory** | จัดหมวดหมู่รายการอาหาร เช่น เนื้อ, ผัก, ของทานเล่น, เครื่องดื่ม, ของหวาน |
| **MenuItem** | เก็บข้อมูลรายการอาหาร ชื่อ ราคา และสถานะพร้อมให้บริการ (available) |
| **Order** | แทนคำสั่งอาหารในแต่ละรอบของโต๊ะ บันทึกเวลาที่สั่งและสถานะ (RECEIVED, PREPARING, READY, SERVED) |
| **OrderItem** | เก็บรายละเอียดแต่ละรายการอาหารภายใน Order เช่น เมนู จำนวน และหมายเหตุเพิ่มเติม |
| **Payment** | เก็บข้อมูลการชำระเงินของ Dining Session เช่น ยอดเงินสุทธิ วิธีชำระ (CASH / QR / CARD) และสถานะการชำระ |
| **StockItem** | มี sku/name/unit/quantity/lowStockThreshold และ V15 `openingTargetStock`/`active`; shortfall คำนวณจาก target − quantity ตอนอ่าน |
| **StockTransaction** | บันทึกประวัติ stock-in (`IN`) และ adjustment (`ADJUSTMENT`) ด้วย signed delta พร้อม balanceAfter เหตุผลและผู้บันทึก ไม่ใช่การตั้งยอดตรวจนับโดยตรง |
