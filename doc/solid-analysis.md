# SOLID analysis

Code baseline `472fba4f25a27fa2e3cd1e1213151ce971646f3a` ตรวจ 7 ตุลาคม2026 แหล่งเกณฑ์ [Requirements](https://app.notion.com/p/3cfcb2e9d47a81ed9ad9d2abb8a174fd) และใบงานวิชาใน workspace ลิงก์ source ตรึง SHA/บรรทัดเพื่อให้ตรวจซ้ำได้ งานนี้จัดทำเอกสาร ไม่แก้ production source หรืออ้างว่า SOLID ผ่านครบทั้งระบบ

## S — Single Responsibility

| หลักฐาน | หน้าที่ / เหตุผล |
|---|---|
| [DiningSessionController.java:23](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/DiningSessionController.java#L23) | รับ HTTP/validate payload/กำหนด response status แล้วเรียก service ไม่รู้วิธีเก็บ session |
| [DiningSessionServiceImpl.java:89](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/DiningSessionServiceImpl.java#L89) | usecaseเปิดรอบ ตรวจโต๊ะ/guest/package/soup/snapshotในtransactionเดียว การอยู่หลายขั้นในหนึ่งusecaseไม่ใช่หลายเหตุผลที่จะเปลี่ยน |
| [DiningSessionMapper.java:10](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/mapper/DiningSessionMapper.java#L10) | เปลี่ยนEntityเป็นStaffDTO แยกจากกฎธุรกิจ ลูกค้ามีCustomerSessionResponseเล็กต่างหาก |
| [CustomerOrderingServiceImpl.java:26](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerOrderingServiceImpl.java#L26) + [OrderingMapper.java:13](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/mapper/OrderingMapper.java#L13) | Orderingดูpackage/quantity/session ส่วนMapperดูJSONfield/menu/category/orderitemชื่อsnapshot |
| [BillingEngine.java:21](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java#L21) | ประสานpricing+discount+rounding ส่วนการpersist/authorization/paymentอยู่คนละservice |

ข้อจำกัด: CustomerSessionAccessServiceรวมexchange/authorization/hash/rotation จึงอาจแยกเมื่อpolicyเปลี่ยนจริง ไม่สร้างabstractเพิ่มโดยไม่มีเหตุผล CustomerBillingServiceประกอบDTO/คำนวณและqueryไว้หลายขั้น ยังต้องownerreviewขอบเขตหลังFinal

## O — Open/Closed

| หลักฐาน | Extension point |
|---|---|
| [BillingEngine.java:10](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java#L10) + [BillingConfig.java:27](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/config/BillingConfig.java#L27) | เปลี่ยนpricing/discountผ่านimplementationsและwiring โดยไม่เปลี่ยนBillingEngine.calculate |
| [StandardBillCalculation.java:12](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/StandardBillCalculation.java#L12) | Childpricingเป็นcompositionผ่านinterface ไม่เป็นsubclassของStandardที่overrideผิดสัญญา |
| [StockTransactionTemplate.java:43](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java#L43) | Subclassเปลี่ยนdelta/typeผ่านhooks แต่ไม่เปลี่ยนขั้นตอนaudit/nonnegativebalanceในfinalprocess |
| [SessionContextProvider.java:5](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/SessionContextProvider.java#L5) | Orderingขึ้นกับcontext contract เปลี่ยนprovider database/explicittestได้โดยไม่ส่งEntityให้Ordering |

ข้อจำกัด: [OrderStateFactory.java:12](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/state/OrderStateFactory.java#L12) ต้องแก้factoryเมื่อเพิ่มenumใหม่ จึงไม่อ้างว่าStateFactoryOCPครบถ้วน การเพิ่มOrderstatusเป็นsharedcontract/migration/UIchange ต้องreviewร่วม

## L — Liskov Substitution

| หลักฐาน | สัญญาที่ต้องรักษา / ผลตรวจ |
|---|---|
| [OrderState.java:22](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/state/OrderState.java#L22) + [ServedState.java:4](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/state/ServedState.java#L4) | next() ของterminalstateปฏิเสธตามdocumentedcontract ไม่แกล้งคืนค่า/throw UnsupportedOperationExceptionจนclientเข้าใจผิด |
| [BillingEngine.java:26](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java#L26) | Strategyผลต้องไม่null/ติดลบ discount≤subtotal Engineตรวจขอบเขตและtests invalidstrategy ป้องกันimplementationแทนกันแล้วผิดยอด |
| [StockTransactionTemplate.java:27](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java#L27) | ทั้งStockInและAdjustmentใช้workflowfinalเดียวกัน คืนTransactionพร้อมbalance/actor/reason; StockInรับpositiveจากDTO/Service ขณะที่Adjustmentเป็นsigneddelta |
| [DatabaseSessionContextProvider.java:22](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/DatabaseSessionContextProvider.java#L22) | Databaseproviderตรวจสิทธิ์/ACTIVE/idและล็อกforOrder ใช้production contractจริง ส่วนfixtureเป็นข้อมูลcontrolledtests ไม่อ้างว่าสับเปลี่ยนfixtureเป็นproductionแล้วsecurityเท่ากัน |

หลักฐาน tests: [OrderStateTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/service/state/OrderStateTest.java) · [BillingEngineTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/service/billing/BillingEngineTest.java) · [StockTransactionTemplateTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/service/StockTransactionTemplateTest.java)

เงื่อนไขก่อนเรียกสำคัญ: Template.processเป็นinternalworkflow Service/DTOตรวจquantityและreasonก่อน Hookไม่รับผิดชอบBeanValidationแทนController การไม่มีUnsupportedOperationExceptionจากtextsearchเป็นหลักฐานเฉพาะสิ่งนั้น ไม่พิสูจน์LSPทุกคลาส

## I — Interface Segregation

| Interface | ผู้ใช้ / เหตุผล |
|---|---|
| [SessionContextProvider.java:5](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/SessionContextProvider.java#L5) | Orderingต้องการsessionId/packageId/table/statusและauthorization+lock ไม่ต้องCRUDโต๊ะหรืออ่านราคาPayment |
| [DiningSessionBillingReader.java:7](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/integration/billing/DiningSessionBillingReader.java#L7) | Billingอ่านsnapshotราคา/counts/statusผ่านrecordเล็ก ไม่เข้ามาเปลี่ยนDiningSessionEntity |
| [PaymentStatusLookup.java:6](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/integration/payment/PaymentStatusLookup.java#L6) | closeอ่านเพียงpaymentverificationของรอบเดียวกัน ไม่เรียกcharge APIหรือผูกPaymentEntity |
| [DiningSessionStaffAccessProvider.java:4](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/DiningSessionStaffAccessProvider.java#L4) + [OrderFulfillmentAccessProvider.java:10](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/OrderFulfillmentAccessProvider.java#L10) | usecasesร้องขอสิทธิ์เฉพาะงาน StaffกับKitchen ไม่เพิ่มทุกpermissionลงinterfaceเดียว |
| [BillCalculationStrategy.java:7](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillCalculationStrategy.java#L7) + [DiscountCalculationStrategy.java:6](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/DiscountCalculationStrategy.java#L6) | แยกpricingออกจากdiscount เพราะsignatureและreason-to-changeต่างกัน |

ข้อจำกัด: การมีinterfaceไม่แปลว่าเล็กพอทุกที่ User/Auth/Stockบางserviceยังconcrete ต้องเจ้าของทบทวนตามC06 ไม่สร้างinterfaceที่มีแต่ชื่อเพื่อให้อ้างผ่านวิชา

## D — Dependency Inversion และ constructor injection

| หลักฐาน | การขึ้นกับabstraction |
|---|---|
| [DiningSessionServiceImpl.java:51](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/DiningSessionServiceImpl.java#L51) | constructorรับRepository interfaces, DiningSessionStaffAccessProvider และObjectProvider<PaymentStatusLookup> Runtimeไม่มีlookupให้503 failclosed ไม่แกล้งPAIDในfixtureproduction |
| [CustomerOrderingServiceImpl.java:32](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerOrderingServiceImpl.java#L32) | constructorรับSessionContextProviderและRepositoryinterfaces เพื่อtestusecaseโดยไม่สร้างEntity/HTTPdependencyข้ามโมดูล |
| [BillingEngine.java:16](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/billing/BillingEngine.java#L16) | pricing/discountเป็นinterfaces composition配置โดยBillingConfig |
| [DatabaseBillingContextProvider.java:18](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/DatabaseBillingContextProvider.java#L18) | adapterรับDiningSessionBillingReader ไม่อ่านราคาแพ็กเกจปัจจุบันหรือใช้ข้อมูลหน้าเว็บ |

ไม่ได้รับรองว่าDครบทั้งระบบ มีรายการต้องแก้ก่อนFinalด้านล่าง Constructorในหลายคลาสถูกต้องแต่มีfield injectionหนึ่งจุดและconcrete dependenciesอยู่จริง

## ข้อจำกัดที่ต้องปิดก่อนรับรอง Final

| Gap | หลักฐาน | เจ้าของ / reviewer | เกณฑ์ปิด |
|---|---|---|---|
| G01 Constructor-onlyยังไม่ครบ | [CustomerBillingService.java:25](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java#L25) fieldEntityManager | ปวริศช์ / ศรัณย์+ธีรเมธ | constructorinjectEntityManager finalfield +bill/concurrencytestsผ่าน |
| G02 Serviceขึ้นกับconcrete collaborators | [StockService.java:24](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/StockService.java#L24) และ [AuthController.java:21](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/AuthController.java#L21) | เมธัสร่วมปวริศช์ / ศรัณย์ | ownerอธิบายและปรับusecase/providerinterfacesที่มีเหตุผลตามใบงาน พร้อมtests ไม่refactorในPRdocsนี้ |
| G03 Layerdependencyย้อนขึ้นController | [SessionUserContextProvider.java:3](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/SessionUserContextProvider.java#L3) และconstantในrequireAuthenticated | เมธัสร่วมปวริศช์ / ศรัณย์ | ย้ายsessionkey/sharedcontractให้serviceไม่importcontroller และAuth/sessiontestsผ่าน |
| G04 Interfaceการauthorizeไม่ครบทุกservice | [SessionMenuAdminAccessProvider.java:10](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/SessionMenuAdminAccessProvider.java#L10) ใช้concretecontextprovider; CustomerBilling/Payment/BillingPreviewบางตัวรับconcreteengine/access | ownerทุกโมดูล / ศรัณย์ | แยกruntimeconfigกับusecaseboundariesตามเกณฑ์Dโดยไม่เปลี่ยนbehavior/securitycontract |
| G05 OCPของStatefactoryจำกัด | [OrderStateFactory.java:12](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/state/OrderStateFactory.java#L12) | ศรัณย์ / ปวริศช์ | ระบุlimitationและตัดสินร่วมตามscope ไม่อ้างfactoryไม่มีจุดต้องแก้เมื่อเพิ่มstate |

G01–G04เป็นช่องว่างcode/เกณฑ์รายวิชา ไม่ใช่เพียงงานแต่งเอกสาร PRนี้บันทึกgapและowner ต้องมีงานแก้code/reviewแยกก่อนติ๊กC03/C06ผ่านทุกเงื่อนไข G05เป็นtradeoffที่อธิบายได้ ต้องเจ้าของยืนยัน ไม่ตีความว่าทุกclassต้องเปิดขยายไม่มีขอบเขต

## หลักฐานทดสอบและแหล่งที่มา

- [CI baseline](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105): backend/PostgreSQL303, frontend116, URLguards6 ผ่าน การเขียนreportนี้ไม่ได้รันtestsใหม่
- [DiningSessionIntegrationTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/integration/DiningSessionIntegrationTest.java) · [PostgresOrderCloseConcurrencyTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/integration/PostgresOrderCloseConcurrencyTest.java) · [SessionFulfillmentIntegrationTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/integration/SessionFulfillmentIntegrationTest.java) · [PaymentIntegrationTest.java](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/test/java/com/buffetrestaurant/integration/PaymentIntegrationTest.java)
- [Menu/Ordering SOLID+JPA ของศิระพัทธ์](architecture/sirapat-menu-ordering-solid-jpa.md) ใช้ประกอบS/I/D ไม่แทนการยืนยันownerอื่น
- [Requirement Matrix](planning/step3-requirement-matrix.md), [System Design](system-design/README.md), [Patterns](design-patterns.md)

สมาชิกทุกคนยังต้องยืนยันและอธิบายส่วนตน ใช้reportนี้สำหรับreview ไม่ใช่claimว่าFinalพร้อมส่ง
