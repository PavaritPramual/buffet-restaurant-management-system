# สูตรเมนูและการหักสต๊อกเมื่อครัวเริ่มทำ

เจ้าของ ปวริศช์ · 10 ตุลาคม 2026 · เพิ่มจาก develop `c778150` · โค้ด backend `0210403` และ UI `e41a275` ใน PR นี้

## พฤติกรรมที่เพิ่ม

Manager เลือกได้ว่าเมนูไม่หักสต๊อก หรือใช้วัตถุดิบใดบ้างต่อหนึ่งเสิร์ฟ เช่น หมู 0.100 กิโลกรัมและซอส 0.020 ลิตร เมนูเดิมเริ่มแบบไม่หัก เมนูของกินเล่นจึงไม่ถูกบังคับให้มีวัตถุดิบ

ตอนลูกค้าสั่ง ระบบคัดลอกสูตรภายใต้ menu row lock ไปเก็บกับ OrderItem การเปลี่ยนสูตร หน่วยที่แสดง หรือเปิด/ปิดการหักหลังสั่ง ไม่เปลี่ยนปริมาณของ Order ที่บันทึกแล้ว ไม่มีการประมาณสูตรให้ Order เก่า

เมื่อ Kitchen Staff กดเริ่มเตรียมอาหาร ระบบตรวจ State เดิมแล้วรวมวัตถุดิบทั้งหมดตามสูตรตอนสั่ง ล็อก Order ก่อน Stock โดย Stock เรียง ID หากเพียงพอจะหักยอด บันทึก CONSUMPTION พร้อมผู้ทำรายการและ Order ID และเปลี่ยนเป็น PREPARING ใน transaction เดียว หากขาด/ปิดใช้งาน/เก็บออกจะคืน 409 พร้อมรายการที่มีปัญหา โดย Order ยัง RECEIVED และไม่มีการหักบางส่วน เติมหรือเปิดวัตถุดิบกลับแล้วลองใหม่ได้

ไม่มีการจองสต๊อกตอนสั่ง ลูกค้าจึงอาจสั่งได้ก่อนครัวพบวัตถุดิบไม่พอ ไม่เพิ่มการแปลงหน่วย การคืนวัตถุดิบ การยกเลิกหรือแบ่งทำบางรายการ หากต้องการพฤติกรรมเหล่านี้ต้องกำหนดกฎธุรกิจเพิ่มก่อน

## การแบ่งชั้นและหลักฐานวิชา

| เกณฑ์ | ก่อนเพิ่ม | หลังเพิ่มและข้อจำกัด |
|---|---|---|
| Layered / MVC / Repository / Service Layer | Controller เรียก Service; Spring Data JPA | Controller ใหม่ยังเรียก MenuStockUsageService; ไม่มี Controller เรียก Repository |
| DTO และ Mapper | Customer/Staff แยก DTO; OrderingMapper | เพิ่ม request สูตรและ Manager-only response; Customer MenuItemResponse/OrderResponse ไม่เผยสูตรหรือยอด Stock |
| Constructor DI / DIP / ISP | Services ใช้ providers/interfaces | Catalog/Ordering ใช้ MenuStockUsageService; Fulfillment ใช้ OrderStockConsumptionService; consumption ใช้ contract ของ processor และ UserContextProvider |
| SRP | Catalog/Ordering/Fulfillment แยก use case | สูตรอยู่ใน MenuStockUsageServiceImpl; ยอดรวมและข้อขาดใน OrderStockConsumptionServiceImpl; final audit workflow ใน Template |
| OCP / State | RegistryOrderStateResolver และ State สี่ตัว | ยังใช้ resolver และ next() เดิม; เพิ่ม collaborator ก่อนบันทึก PREPARING ไม่มีการแทน State ด้วย switch |
| Template Method | Stock-in/Adjustment ใช้ final algorithm | เพิ่ม StockConsumptionProcessor และ processWithOrder; audit/active/nonnegative algorithm ใช้ร่วมกัน การเรียก consume ต้องมี positive quantity และ Order ID |
| LSP / contract | processor มี preconditions ของ workflow | consumption callers ใช้ StockConsumptionTransactionProcessor โดยเฉพาะ; shared final algorithm ไม่เปลี่ยนผล stock-in/adjustment; ไม่อ้างว่าเพิ่ม subclass แล้วพิสูจน์ LSP ทั้งระบบ |
| Strategy | Billing discount strategies / BillCalculator | คงโค้ดเดิมและตรวจ Customer/Staff/Payment regression; ยอดบิลไม่มาจากสูตร Stock |
| JPA relationships | One-to-One Profile และ One-to-Many OrderItem | คงของเดิม; เพิ่ม LAZY ElementCollection สองตาราง FK/index/ON DELETE ระบุใน V19 delta |
| Diagram ทุกชนิด | Use Case, Domain, Class, Sequence, Activity, ERD, Component, Deployment, State | เพิ่มรายละเอียดในภาพเดิมแปดภาพ; ไม่ลบ State/Strategy/Deployment หรือ diagram เพื่อให้โค้ดผ่าน |
| REST / validation / Swagger | ErrorResponse และ role-based session cookie | GET สูตรเฉพาะ Manager; request validation และ stock conflict 409; header role ปลอมไม่เพิ่มสิทธิ์ครัว |

ไม่ได้รับรองว่า SOLID ทุกไฟล์ทั้งระบบผ่านจากตารางนี้ รายงานรอบเก่าและเกณฑ์ส่งงานที่ยังรอ review/public/release ยังคงแยกจากผลของฟีเจอร์นี้

## จุดเชื่อมโค้ด

- [Recipe contract/implementation](../../code/backend/src/main/java/com/buffetrestaurant/service/impl/MenuStockUsageServiceImpl.java) บันทึกใน catalog transaction และคัดลอกสูตรเมื่อสั่ง
- [Fulfillment](../../code/backend/src/main/java/com/buffetrestaurant/service/impl/OrderFulfillmentServiceImpl.java) ล็อก Order ตรวจ State และเรียก interface ก่อนเปลี่ยนสถานะ
- [Consumption service](../../code/backend/src/main/java/com/buffetrestaurant/service/impl/OrderStockConsumptionServiceImpl.java) ใช้ MANDATORY transaction รวมปริมาณ ล็อก Stock เรียง ID และแจ้งข้อขาดก่อนเขียน
- [Processor](../../code/backend/src/main/java/com/buffetrestaurant/service/StockConsumptionProcessor.java) ใช้ final workflow จาก Template Method บันทึก negative delta และ Order ID
- [Scenarios](../../code/backend/src/test/java/com/buffetrestaurant/integration/MenuStockConsumptionScenarios.java) ใช้จริงทั้ง H2 และ PostgreSQL; session identity fixtures ไม่ใช่หลักฐาน login ผ่าน browser

## เอกสารร่วม

[Schema/JPA V19](../database/menu-stock-consumption-v19.md) · [Shared contracts](../contracts/shared-contracts.md#menu-stock-consumption--v19) · [Diagram index](../diagrams/README.md) · [รายงาน UAT รุ่นก่อน](../testing/pavarit-public-uat-2026-10-10.md)

V19 ยังไม่ apply บน Supabase กลาง ผล local/CI ไม่ใช้รับรอง Render ต้อง review/merge รับรอง migration และอนุมัติ apply แยกก่อน public UAT รุ่นใหม่
