# Sequence diagrams

อ้าง develop472fba4 / 7 ตุลาคม2026 จาก [System Design](README.md) sourceและpreviewที่ปรับดู [diagram index](../diagrams/README.md) ประวัติbaseline28ก.ย.ยังอยู่Git history ไม่มี Order.nextState()/BillCalculator ในruntimeปัจจุบัน

## 1. เปิดรอบกิน

[Source](../diagrams/sequence-open-session.puml) · [SVG](../diagrams/previews/sequence-open-session.svg)

DiningSessionController → DiningSessionServiceImpl ตรวจ SERVICE_STAFF จาก login ล็อกโต๊ะ AVAILABLE ตรวจจำนวนคน/package/soup active สร้าง snapshotราคาและQR32bytes จากSecureRandom โต๊ะOCCUPIEDกับรอบACTIVE saveในtransactionเดียว Staff DTOส่งQRtokenให้ Staffสร้างfragmentlink ไม่ส่งcookieลูกค้าในขั้นนี้

## 2. แลก QR และเข้าถึงรอบกิน

[Source](../diagrams/sequence-qr-exchange.puml) · [SVG](../diagrams/previews/sequence-qr-exchange.svg)

Fragmentไม่เป็นHTTPrequest path หน้าCustomerล้างfragmentทันที POST tokenในbodyใช้ครั้งเดียว BackendหมุนQRและออกHttpOnlycookie เก็บhash+expiry8h ต่อเครื่องในgrant การreloadอ่านcustomer-contextด้วยcookie QRใหม่ในแท็บเดิมต้องclearcart/orders/sessionและignoreคำตอบเก่า InvalidQRแสดงerrorไม่fallbackรอบเก่า Same-token in-flightPromiseช่วยStrictMode ไม่cachetokenที่แลกแล้ว

## 3. Ordering / Kitchen / Serving

[Baseline source](../diagrams/sequence-ordering-kitchen.puml) · [State implementation](../design-patterns.md#state--order-fulfillment)

CustomerOrderingServiceImpl ใช้ SessionContextProviderตรวจรอบACTIVE เมนูตามpackageและquantity ก่อนwriteผ่านrequireSessionForOrderล็อกsessionเดียวกับclose/billrequest

OrderFulfillmentServiceImpl resolve OrderStatusผ่านOrderStateFactoryและตรวจcurrent.next()ก่อนเปลี่ยน persisted enum ไม่ใช่ให้CustomerOrderมีnextState() ครัว RECEIVED→PREPARING→READY และServiceStaff READY→SERVED baselineภาพOrderingยังต้องศรัณย์/ศิระพัทธ์ตรวจFinalว่าตรงcookie/lockนี้

## 4. ขอคิดบิล ชำระ และ close

[Source](../diagrams/sequence-billing-payment.puml) · [SVG](../diagrams/previews/sequence-billing-payment.svg)

Customerขอคิดบิลด้วยcookieและOrigin ล็อกsession refresh/recheckสิทธิ์ ตั้งbillRequestedAtครั้งแรก SessionยังACTIVEแต่Ordersใหม่409 ทุกมือถือยังอ่านบิลได้ หน้าสถานะpoll5sขณะvisible

BillingContextProvider→DiningSessionBillingReaderอ่านsnapshotตอนเปิด/counts จากDB BillingEngineประกอบStandardBillCalculation/ChildRateCalculationStrategy/PromotionDiscountStrategy ไม่รับยอดจากbrowser

PaymentServiceImplล็อกsessionเดียวกัน ต้องACTIVEและขอคิดบิลแล้ว ไม่มีpaymentเดิม คำนวณtrustedamount บันทึกPAID/paidAtและunique sessionId LostPOSTresponseให้GETexistingpaymentเพื่อตรวจ ไม่auto-retryPOST

Paymentไม่closeเอง Staffกดcloseแยก DiningSessionServiceImplล็อกsession ตรวจPaymentStatusLookupที่sessionIdตรงและPAID แล้วล็อกtable/deletegrants/COMPLETED/AVAILABLEในtransactionเดียว Afterclosecookieเดิมถูกปฏิเสธ

## หลักฐานและสิ่งที่รอ

CIของbaselineตรวจorder/bill/payment/closeconcurrencyบนPostgreSQLทิ้งได้ [รายงาน](../testing/sirapat-step3-premerge-report.md) Publicdeployment/release/browserใหม่ยังต้องตรวจเพิ่ม ไม่อ้างว่าภาพนี้เป็นE2Eที่deployแล้ว
