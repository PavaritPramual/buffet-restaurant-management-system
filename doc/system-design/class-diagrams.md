# Class diagrams

อัปเดตจาก develop472fba4 / 7 ตุลาคม2026 source/previewดู [diagram index](../diagrams/README.md) ส่วน baselineที่ยังไม่auditระบุไว้ ไม่ใช้ชื่อคลาสในdesignเดิมแทนcodeจริง

| ส่วน | คลาสและขอบเขตจริง | Source / preview |
|---|---|---|
| Table/Session | DiningSession/CustomerSessionGrant/RestaurantTable, DiningSessionServiceImpl, CustomerSessionAccessService, CustomerBillingService และ narrow billing/payment interfaces | [source](../diagrams/class-table-session.puml) / [SVG](../diagrams/previews/class-table-session.svg) |
| Menu/Order | MenuItem categoryและpackageIds, CustomerOrder/OrderItem, MenuCatalog/OrderingบริการและDTO mapping | [baseline source](../diagrams/class-menu-order.puml) เจ้าของตรวจFinalอีกครั้ง |
| State | OrderFulfillmentServiceImplเป็นcontext resolvepersisted enumด้วยOrderStateFactory, OrderStateและReceived/Preparing/Ready/ServedState | [source](../diagrams/class-order-state.puml) / [SVG](../diagrams/previews/class-order-state.svg) |
| Strategy | BillingEngineขึ้นกับBillCalculationStrategy/DiscountCalculationStrategy; StandardBillCalculationมีchildStrategyผ่านBillCalculationStrategy; publicBillSummaryแยกจากinternalBillCalculation | [source](../diagrams/class-billing-strategy.puml) / [SVG](../diagrams/previews/class-billing-strategy.svg) |
| Template Method | StockTransactionTemplate.final process()และcalculateDelta()/transactionType() hooks; StockInProcessor/StockAdjustmentProcessor; transactionอยู่StockService | [source](../diagrams/class-stock-template.puml) / [SVG](../diagrams/previews/class-stock-template.svg) |
| Auth | UserAccount/UserProfile AuthServiceและSpringHTTPsession | [baseline source](../diagrams/class-auth.puml) รอเมธัสFinalProfileและconsistency |

DiningSessionStatusมีCANCELLEDในenum แต่ยังไม่มีcancelendpoint Billrequestไม่เพิ่มstatusใหม่ QRgrantกับsnapshotไม่เปิดเผยในCustomerSessionResponse Stocktarget/activeและProfileละเอียดเป็นงานFinal ไม่ใช่fieldsปัจจุบัน

เหตุผลpattern/code/testsและข้อจำกัดSOLIDอยู่ [Design Patterns](../design-patterns.md) และ [SOLID](../solid-analysis.md) ทุกEntityownerยังต้องยืนยันcascade/fetchของตนก่อนส่งFinal
