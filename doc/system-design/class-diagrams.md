# Class diagrams

อัปเดตจาก develop472fba4 / 7 ตุลาคม2026 source/previewดู [diagram index](../diagrams/README.md) ทุกภาพปรับตามsourceและrenderSVGแล้ว ยังต้องownerreviewFinal ไม่ใช้ชื่อคลาสในdesignเดิมแทนcodeจริง

| ส่วน | คลาสและขอบเขตจริง | Source / preview |
|---|---|---|
| Table/Session | DiningSession/CustomerSessionGrant/RestaurantTable, DiningSessionServiceImpl, CustomerSessionAccessService, CustomerBillingService และ narrow billing/payment interfaces | [source](../diagrams/class-table-session.puml) / [SVG](../diagrams/previews/class-table-session.svg) |
| Menu/Order | MenuItem categoryและpackageIds, CustomerOrder/OrderItem, MenuCatalog/OrderingบริการและDTO mapping | [source](../diagrams/class-menu-order.puml) / [SVG](../diagrams/previews/class-menu-order.svg) เจ้าของตรวจFinalอีกครั้ง |
| State | OrderFulfillmentServiceImplเป็นcontext resolvepersisted enumผ่านOrderStateResolver; runtime RegistryOrderStateResolverตรวจregistrationของOrderStateและReceived/Preparing/Ready/ServedState | [source](../diagrams/class-order-state.puml) / [SVG](../diagrams/previews/class-order-state.svg) |
| Strategy | BillingEngineขึ้นกับBillCalculationStrategy/DiscountCalculationStrategy; StandardBillCalculationมีchildStrategyผ่านBillCalculationStrategy; publicBillSummaryแยกจากinternalBillCalculation | [source](../diagrams/class-billing-strategy.puml) / [SVG](../diagrams/previews/class-billing-strategy.svg) |
| Template Method | StockTransactionTemplate.final process()และcalculateDelta()/transactionType() hooks; StockInProcessor/StockAdjustmentProcessor; transactionอยู่StockService | [source](../diagrams/class-stock-template.puml) / [SVG](../diagrams/previews/class-stock-template.svg) |
| Auth | UserAccount/UserProfile AuthServiceและSpringHTTPsession | [source](../diagrams/class-auth.puml) / [SVG](../diagrams/previews/class-auth.svg) UserAccount มี UserProfile ได้ 0..1 แบบ shared PK; createUser สร้างทั้งคู่ แต่ DB ไม่บังคับทุก user ต้องมี profile; Profile มี firstName/lastName/phoneNumber (V15, nullable สำหรับข้อมูลเดิม) และ updateProfile() |

DiningSessionStatusมีCANCELLEDในenum แต่ยังไม่มีcancelendpoint Billrequestไม่เพิ่มstatusใหม่ QRgrantกับsnapshotไม่เปิดเผยในCustomerSessionResponse Stock target/active และ Profile firstName/lastName/phoneNumber implement แล้วใน V15 (ดู class-stock-template/class-auth)

เหตุผลpattern/code/testsและข้อจำกัดSOLIDอยู่ [Design Patterns](../design-patterns.md) และ [SOLID](../solid-analysis.md) ทุกEntityownerยังต้องยืนยันcascade/fetchของตนก่อนส่งFinal
