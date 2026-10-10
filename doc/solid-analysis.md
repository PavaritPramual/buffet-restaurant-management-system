# SOLID analysis — Architecture refactor

ตัวอย่าง refactor ด้านล่างอ้างโค้ด `de7b5d546a05ad3ccef8c641ee5c53d638a8e039` วันที่ 7 ตุลาคม 2026 ต่อจาก PR #24; เก็บ commit นี้เพื่อให้ลิงก์ไฟล์/บรรทัดเดิมตรวจย้อนกลับได้ [เอกสารก่อน refactor](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/0dbbe1b7deae5db4189aa803f04a5246ccee8746/doc/solid-analysis.md) เป็นประวัติ G01–G05 ก่อนแก้

**สถานะตรวจ 9 ตุลาคม 2026:** [PR #25](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/25) ผ่านรีวิวและ merge แล้วที่ `cb612d9`; ไม่ได้รอ review ของ refactor เดิมอีก. Fulfillment delta ด้านล่างตรวจ source/tests ที่ merged `develop d84f071`; การทบทวนเอกสารรอบนี้ยังรอ peer review และไม่ใช่รับรอง Final/public deployment ทั้งระบบ.

## เอกสารตามเจ้าของโมดูล

- [ปวริศช์ — Table/Package/Soup, DiningSession/QR และ shared providers](architecture/pavarit-table-session-solid-jpa.md): SOLID, JPA/SQL lifecycle, snapshot/เวลา, transaction/lock และข้อจำกัดพร้อม source/tests ของ baseline `6d83eac`
- [ศิระพัทธ์ — Menu/Ordering](architecture/sirapat-menu-ordering-solid-jpa.md): หลักฐานตาม scope ของเจ้าของ; การยืนยันรุ่น Final และโมดูลที่เหลือยังเป็น gate แยก
- [ธีรเมธ — Billing/Payment](architecture/teeramet-billing-payment-solid-jpa.md): Strategy, price snapshot, rounding, payment transaction/JPA และ tests ที่ revision `bdd3bd7`; public release gate ยังแยก
- [ศรัณย์ — Order Fulfillment](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/d84f071ee03b738d6a6dd2a899c5f6e6cc230d3f/code/backend/src/main/java/com/buffetrestaurant/service/impl/OrderFulfillmentServiceImpl.java): role checks stay in the access provider; the service asks the current State for its only legal next state before persisting.

### Fulfillment delta — checked against merged `develop d84f071`

- **SRP:** `SessionOrderFulfillmentAccessProvider` owns kitchen/service-role checks; `RegistryOrderStateResolver` owns complete/unique State registration and resolution; `OrderFulfillmentServiceImpl` coordinates board reads and the requested transition; `CustomerOrder` persists the selected status but does not reimplement transition policy.
- **OCP / State:** the service depends on `OrderStateResolver`, obtains `current.next()`, and compares that one next status with the requested status. The registry is immutable after construction and fails on missing/duplicate states. This limits resolution branching, but a new business status still requires updating the enum, adjacent transitions, API/UI, and authorization contract.
- **DIP / ISP:** the fulfillment service receives the narrow `OrderFulfillmentAccessProvider` and `OrderStateResolver` interfaces through its constructor rather than depending on authentication internals or concrete state classes.
- **Evidence:** `OrderStateResolverTest` covers registry completeness, duplicate/missing registration and replacement policy; `OrderStateTest` covers terminal behavior; `OrderFulfillmentServiceTest` covers service rules; `OrderFulfillmentIntegrationTest` exercises the persisted full lifecycle, skipped/reversed transitions, wrong-role denials and unchanged state. The tests do not substitute for the separate public report or deployed-SHA attestation.
- **Review boundary:** source and focused tests are in merged PR #26 and were checked again against `d84f071`; this delta has not yet received the requested new Architecture/consistency review.

## S — Single Responsibility

| หน้าที่ | หลักฐานและเหตุผล |
|---|---|
| HTTP กับ use case | [AuthController.java:22](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/controller/AuthController.java#L22) รับ login/me/logout และ session lifecycle; authentication กับ user administration แยก contracts ให้ผู้เรียกใช้เฉพาะงาน |
| Authorization | [SessionUserContextProvider.java:18](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/SessionUserContextProvider.java#L18) อ่าน identity จริงและตรวจ roles; ไม่ authenticate password หรือคำนวณบิล |
| Calculation | [BillingEngine.java:21](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java#L21) ประสาน pricing/discount/rounding; Payment Service ดู lock/สิทธิ์/persistence แยก |
| State resolution | [RegistryOrderStateResolver.java:16](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/state/RegistryOrderStateResolver.java#L16) ประกอบและตรวจ registry; แต่ละ State ดู transition ของตน ส่วน Service บันทึก Order |

การมีหลายขั้นใน transaction ของหนึ่ง use case ไม่ได้แปลว่าต้องแยกทุกบรรทัดเป็น class. Mapper แยกจากการเปิด/ปิดรอบและ Ordering อยู่แล้ว

## O — Open/Closed

| จุดขยาย | หลักฐาน |
|---|---|
| State registration | [OrderStateResolver.java:7](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/state/OrderStateResolver.java#L7) กับ [RegistryOrderStateResolver.java:34](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/state/RegistryOrderStateResolver.java#L34) ไม่มี switch ของชนิด State; Config ลงทะเบียน singleton เดิมสี่ตัว |
| Pricing/discount | [BillCalculator.java:7](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillCalculator.java#L7) และ BillingEngine รับ pricing/discount interfaces เปลี่ยน policy ใน composition root ได้ |
| Stock workflow | [StockTransactionTemplate.java:27](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java#L27) คง workflow final; subclasses เปลี่ยน delta/type ผ่าน hooks |
| Fulfillment access / State | [OrderFulfillmentAccessProvider](../code/backend/src/main/java/com/buffetrestaurant/service/OrderFulfillmentAccessProvider.java), [OrderStateResolver](../code/backend/src/main/java/com/buffetrestaurant/service/state/OrderStateResolver.java) isolate role checks and state lookup from the orchestration service |

[OrderStateResolverTest.java:12](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/state/OrderStateResolverTest.java#L12) ตรวจ policy ทดแทนโดยไม่แก้ resolver. การเพิ่มสถานะธุรกิจใหม่ยังต้องแก้ enum, transitions ที่เกี่ยวข้อง และตรวจ API/UI/สิทธิ์ร่วมกัน; ไม่อ้างว่าเพิ่ม workflow ใดก็ได้โดยไม่มีผลต่อ contract

## L — Liskov Substitution

- UserContextProvider สำเร็จด้วย userId บวก username ไม่ว่าง และ role ไม่เป็น null; displayName เป็น presentation data และอาจไม่มีค่า. identity ขาด/ผิดได้ 401; identity ครบแต่ role ไม่ได้รับอนุญาตได้ 403
- BillCalculator คืน BillCalculation ไม่เป็น nullจาก trusted context. BillingEngine ตรวจ subtotal ก่อนเรียก discount และตรวจ reduction ไม่เป็น null/ติดลบ/เกิน subtotal — [BillingEngineTest.java:12](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/billing/BillingEngineTest.java#L12)
- BillingContextProvider ต้องคืน session เดียวกันพร้อม snapshot/counts/status; discountContext เป็น nullได้และหมายถึงไม่มี promotion — [DatabaseBillingContextContractTest.java:15](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/DatabaseBillingContextContractTest.java#L15) และ [CustomerBillingContractTest.java:18](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/CustomerBillingContractTest.java#L18)
- OrderState.next() ระบุไว้ว่า terminal state ส่ง BusinessRuleException; ไม่คืน nullหรือ throw UnsupportedOperationException ที่ผิดสัญญา — [OrderStateTest.java:10](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/state/OrderStateTest.java#L10)
- StockTransactionProcessor เป็น internal workflow ภายใต้ validation/transaction/row lock ของ StockService; Stock-in ใช้จำนวนบวก Adjustment ใช้ signed delta — [StockTransactionTemplateTest.java:28](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/StockTransactionTemplateTest.java#L28)
- Fixture ใช้ข้อมูล synthetic และไม่ได้พิสูจน์ runtime authentication/locking. Fulfillment fixture ใช้ role matrix เดียวกับ runtime; manager/supervisor ไม่ได้สิทธิ์สอง board — [FixtureOrderFulfillmentAccessProviderTest.java:18](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/test/java/com/buffetrestaurant/service/fixture/FixtureOrderFulfillmentAccessProviderTest.java#L18)

## I — Interface Segregation

| Contract | ผู้ใช้และขอบเขต |
|---|---|
| [AuthenticationService.java:6](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/AuthenticationService.java#L6) | AuthController ใช้ authenticate เท่านั้น |
| [UserAdministrationService.java:8](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/UserAdministrationService.java#L8) | AdminUserController/seed/bootstrap ใช้ list/create ไม่เพิ่ม edit/delete ที่ยังไม่ทำ |
| [UserContextProvider.java:14](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/UserContextProvider.java#L14) | Controllers/access providers ขอ identity/role ไม่รู้วิธีตรวจ password |
| [CustomerSessionVerifier.java:7](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/CustomerSessionVerifier.java#L7) | Billing/Ordering ขอ context หรือสิทธิ์อ่าน/สั่ง ไม่แลก QR หรือเปลี่ยนข้อมูลโต๊ะ |
| [BillCalculator.java:6](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillCalculator.java#L6) | Billing Preview/Customer Billing/Payment ขอผลคำนวณ ไม่อ้าง concrete engine |
| [StockTransactionProcessor.java:9](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionProcessor.java#L9) | StockService ขอ workflow ผ่าน qualifier ไม่เรียก hook ของ subclass |

SessionContextProvider, DiningSessionBillingReader, PaymentStatusLookup และ access providers เฉพาะแต่ละโมดูลคงแยกตาม use case

## D — Dependency Inversion และ constructor injection

- [CustomerBillingService.java:30](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java#L30) รับ EntityManager, verifier, calculator และ repositories ผ่าน constructor; field เป็น final และคง refresh/row lock เดิม
- [StockService.java:28](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/StockService.java#L28) รับ StockTransactionProcessor สองตัวผ่าน qualifier `stockInProcessor`/`stockAdjustmentProcessor`; template ยังเป็น implementation ร่วม
- [PaymentServiceImpl.java:53](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/impl/PaymentServiceImpl.java#L53) กับ BillingPreviewService รับ BillCalculator ไม่ผูก BillingEngine
- [SessionMenuAdminAccessProvider.java:12](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/SessionMenuAdminAccessProvider.java#L12) และ Staff/Fulfillment/Payment providers รับ UserContextProvider
- [UserSessionKeys.java:5](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/common/UserSessionKeys.java#L5) รักษาค่า userContext เดิม; Service ไม่ import Controller
- Cookie/CORS/origin settings รับผ่าน constructor พร้อมค่า default เดิม. @Autowired ที่ constructor สำหรับเลือก production constructor ไม่ใช่ field injection

## ผล review ของ refactor เดิม และ gate Final ที่ยังเหลือ

| ข้อเดิม | การแก้รอบนี้ | สถานะ |
|---|---|---|
| G01 / C06 field EntityManager | constructor final รวม cookie/CORS config | PR #25 review/merge ผ่านแล้ว |
| G02 / D05 concrete processors/Auth | focused auth contracts และ StockTransactionProcessor | PR #25 review/merge ผ่านแล้ว |
| G03 / C03 Service import Controller | shared UserSessionKeys และ UserContextProvider | PR #25 review/merge ผ่านแล้ว |
| G04 business/access collaborators | BillCalculator, CustomerSessionVerifier, UserContextProvider | PR #25 review/merge ผ่านแล้ว |
| G05 / O02 state-specific factory | validated registry + resolver contract แทน switch | PR #25 review/merge ผ่านแล้ว |
| L04 nullability/roles | contracts + invalid-result guards + fixture roles ตรง runtime | PR #25 review/merge ผ่านแล้ว |

ยังต้อง owner confirmation ของแต่ละโมดูล รวม Stock/Profile/deploy ใหม่ ตรวจ release commit และ public flow อีกครั้ง. การเพิ่ม interfaces และผล tests นี้ไม่ใช่ใบรับรอง SOLID ทุกคลาสโดยอัตโนมัติ

## หลักฐานทดสอบ

[รายงานรอบนี้](testing/architecture-refactor-report.md) · [ผลตรวจ source/API](../test/evidence/architecture-refactor-2026-10-07/source-contracts.json) · [Class/Component diagrams](diagrams/README.md)

## V19 menu-stock extension — 10 October 2026

[Scoped SOLID and course before/after matrix](architecture/menu-stock-consumption.md) links the new recipe service, consumption interface/implementation and Template subclass. State resolver and Billing Strategy remain in place; constructor injection and service boundaries remain. Integration scenarios run against real H2/PostgreSQL transactions; browser uses actual login. This is evidence for the touched scope, not a blanket certification of SOLID or Final.
