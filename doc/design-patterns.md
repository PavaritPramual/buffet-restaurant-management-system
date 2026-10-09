# Design Patterns — implemented patterns and review status

แกน pattern examples เดิมอ้าง source หลัง Architecture refactor `de7b5d546a05ad3ccef8c641ee5c53d638a8e039`. Fulfillment State source/tests ตรวจซ้ำกับ merged `develop d84f071` วันที่ 9 ตุลาคม; PR #25/#26 และ Stock/Profile PR #28 merge แล้ว. การตรวจนี้ไม่ใช่ approval ของเอกสาร delta รอบใหม่หรือ Final release.

## Enterprise / Architectural Patterns

| Pattern | ปัญหาที่แก้ | หลักฐาน | Diagram |
|---|---|---|---|
| Layered Architecture | แยก HTTP/business/data access ไม่ให้ Service ย้อนขึ้น Controller | [SessionUserContextProvider.java:16](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/SessionUserContextProvider.java#L16) และ shared UserSessionKeys | [Component](diagrams/component.puml) |
| MVC | Controller รับ HTTP, DTO เป็นข้อมูล, React เป็น View | [AuthController.java:22](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/controller/AuthController.java#L22) | [Component](diagrams/previews/component.svg) |
| Repository | queries/row locks อยู่หลัง Spring Data interfaces | [DiningSessionRepository.java:12](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/repository/DiningSessionRepository.java#L12) | [Session Class](diagrams/class-table-session.puml) |
| Service Layer | transaction/use case อยู่ใน Service | [CustomerBillingService.java:30](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java#L30) | [Session Class](diagrams/previews/class-table-session.svg) |
| DTO + Mapper | ไม่ส่ง Entity/credential/snapshot ให้ลูกค้า | [DiningSessionMapper.java:10](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/mapper/DiningSessionMapper.java#L10) | [Session Class](diagrams/class-table-session.puml) |
| Dependency Injection | ใช้ constructor/interface ให้ policy ทดแทนได้ | [StockService.java:28](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/StockService.java#L28) และ BillCalculator/UserContextProvider | [Stock Class](diagrams/class-stock-template.puml) |

## Behavioral Patterns — สามแบบในกลุ่มเดียวกัน

| Pattern | ปัญหาที่แก้ | ผู้ร่วมงานจริง | Class diagram |
|---|---|---|---|
| State | ห้ามข้าม/ย้อน transition ของ Order | OrderFulfillmentServiceImpl, OrderStateResolver, RegistryOrderStateResolver, Received/Preparing/Ready/Served | [Source](diagrams/class-order-state.puml) / [SVG](diagrams/previews/class-order-state.svg) |
| Strategy | แยก pricing/discount จากการรับชำระ | BillCalculator, BillingEngine, BillCalculationStrategy, DiscountCalculationStrategy, Standard/ChildRate/Promotion | [Source](diagrams/class-billing-strategy.puml) / [SVG](diagrams/previews/class-billing-strategy.svg) |
| Template Method | workflow ยอดและ audit เหมือนกัน แต่ delta/type ต่างกัน | StockTransactionProcessor, StockTransactionTemplate, StockInProcessor, StockAdjustmentProcessor | [Source](diagrams/class-stock-template.puml) / [SVG](diagrams/previews/class-stock-template.svg) |

### State

[RegistryOrderStateResolver.java](../code/backend/src/main/java/com/buffetrestaurant/service/state/RegistryOrderStateResolver.java) รับ `List<OrderState>`, ตรวจซ้ำ/ขาด/null แล้วสร้าง immutable registry. [OrderStateConfig.java](../code/backend/src/main/java/com/buffetrestaurant/config/OrderStateConfig.java) ประกาศ singleton beans สี่ตัว. [OrderFulfillmentServiceImpl](../code/backend/src/main/java/com/buffetrestaurant/service/impl/OrderFulfillmentServiceImpl.java) รับ resolver interface และใช้ `state.next()` เพื่อเทียบกับ transition ที่ร้องขอก่อน persist. SERVED ส่ง BusinessRuleException ตาม contract. Registry ไม่ยกเลิกการตรวจ permission หรือสร้างสถานะใหม่.

[OrderStateResolverTest](../code/backend/src/test/java/com/buffetrestaurant/service/state/OrderStateResolverTest.java) ตรวจ registration, startup failure และ replacement policy; [OrderStateTest](../code/backend/src/test/java/com/buffetrestaurant/service/state/OrderStateTest.java) กับ [OrderFulfillmentServiceTest](../code/backend/src/test/java/com/buffetrestaurant/service/OrderFulfillmentServiceTest.java) ตรวจ transition; [OrderFulfillmentIntegrationTest](../code/backend/src/test/java/com/buffetrestaurant/integration/OrderFulfillmentIntegrationTest.java) ตรวจ persisted lifecycle, denied roles และ invalid transitions. เพิ่ม workflow ต้อง review enum/adjacent transitions/API/UI ไม่อ้าง OCP ครบทุกมิติจาก registry อย่างเดียว.

### Strategy

รายละเอียดที่เจ้าของ Billing/Payment ตรวจจาก source revision `bdd3bd7` อยู่ใน [Billing/Payment Strategy, SOLID และ JPA notes](architecture/teeramet-billing-payment-solid-jpa.md). เอกสารนี้ไม่ใช้ผล CI แทนการตรวจ public payment flow.

[BillingEngine.java:21](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java#L21) implements BillCalculator และ compose pricing/discount interfaces. invalid pricing ถูกปฏิเสธก่อน discount. คำนวณ precise BigDecimal แล้ว HALF_UP payable totalสองตำแหน่ง; DTO ใช้ BillSummaryและPayment ใช้ยอดบันทึกจริง. BillingContext มาจาก session snapshot ผ่าน DiningSessionBillingReader; null discountContext หมายถึงไม่มี promotion

[BillingEngineTest.java:12](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/billing/BillingEngineTest.java#L12) ตรวจ invalid/replacement strategies และ rounding; PaymentIntegrationTest/PostgresPaymentIntegrationTest ตรวจ snapshot, duplicate และ Payment/close concurrency. Strategy ไม่รับผิดชอบ authorization หรือ DB locks

### Template Method

[StockTransactionTemplate.java:27](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java#L27) implements StockTransactionProcessor. final process ทำ actor → delta → nonnegative balance → save item → save audit. subclasses เปลี่ยน calculateDelta/transactionType เท่านั้น. [StockService.java:28](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/StockService.java#L28) เลือก processors ผ่าน qualifiers; @Transactional/row lock ยังอยู่ใน Service ยอดกับ history commit/rollbackพร้อมกัน. Validation quantities/reason อยู่ request/service; ไม่เดาว่า Adjustment คือ set quantity

[StockTransactionTemplateTest.java:28](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/StockTransactionTemplateTest.java#L28) และ AuthStock/PostgresStockSecurityIntegrationTest ตรวจยอด ประวัติ และ rollback. opening target/active ยังเป็นงาน Final ของเมธัส ไม่ใช่ฟีเจอร์ที่เพิ่มใน PR นี้

## หลักฐานและการรับรอง

[รายงาน tests](testing/architecture-refactor-report.md) · [SOLID](solid-analysis.md) · [Requirement Matrix](planning/step3-requirement-matrix.md). รอศรัณย์/ธีรเมธ/เมธัส/ศิระพัทธ์ตรวจตามพื้นที่ ยังไม่ติ๊ก public/release หรือรับรองสไลด์

รายละเอียด Template Method ของ Stock (ขั้นร่วม/steps ที่ override/transaction/audit/inactive guard) และ SOLID ดู [architecture/template-method-auth-stock.md](architecture/template-method-auth-stock.md)
