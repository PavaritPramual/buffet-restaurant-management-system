# System Design

อ้าง code baseline develop `472fba4f25a27fa2e3cd1e1213151ce971646f3a` ณ 7 ตุลาคม 2026 ลิงก์ source ตรึง SHA เพื่อให้บรรทัดตรวจซ้ำได้ เอกสารนี้รอ peer review ไม่ใช่หลักฐาน release/public acceptance

Step 2 ปิดแล้ว PR#21 merge ที่54e3538 และ PR#22 merge ที่472fba4 เพิ่ม local regression/AdminShell/test preparation ดู [Step3 matrix](../planning/step3-requirement-matrix.md) เพื่อแยก implementation, documentation และ Final gates

## ขอบเขตและผู้ใช้งาน

ระบบ Walk-in บุฟเฟต์ Customer ไม่มีบัญชีร้าน ใช้ QR แลก customer cookie ต่อเครื่อง Staff ใช้ Spring HTTP login session คนละกลไกกับ Customer

| Role | งานที่อนุญาต |
|---|---|
| MANAGER | ข้อมูลร้าน Menu/โต๊ะ/แพ็กเกจ/น้ำซุป/รายการสต็อก และ stock movements; Users เฉพาะ list/create พร้อม basic profile |
| SUPERVISOR | อ่านสต็อก/ทำ stock-in และ adjustments ไม่รับชำระ/เปิดโต๊ะ/ครัว |
| SERVICE_STAFF | เปิดรอบ แสดง QR เสิร์ฟ READY→SERVED รับชำระและ close |
| KITCHEN_STAFF | incoming Orders RECEIVED→PREPARING→READY |
| Customer cookie | อ่านเมนูตามแพ็กเกจ สั่งก่อนขอคิดบิล อ่านออเดอร์/ขอและดูสถานะบิลรอบเดียวกัน |

ทุก protected API ตรวจจาก login/cookie จริง `X-User-Role` เพิ่มสิทธิ์ไม่ได้ Menu/Ordering ใช้ `SessionContextProvider` ไม่ข้ามไปสร้าง Entity ของโมดูลอื่น Staff/client roles กับ JDBC database role เป็นคนละเรื่อง

## เอกสารแบบออกแบบ

- [Component source](../diagrams/component.puml) / [SVG](../diagrams/previews/component.svg)
- [Use cases](use-cases.md), [Domain](domain-model.md), [Classes](class-diagrams.md), [Sequences](sequence-diagrams.md), [Activities](activity-diagrams.md)
- [Diagram index](../diagrams/README.md), [ERD](../diagrams/er-diagram.puml), [canonical Data Dictionary](../database/step2-schema-approved.md)
- [SOLID](../solid-analysis.md), [Patterns](../design-patterns.md), [shared contract](../contracts/shared-contracts.md), [API convention](../contracts/api-conventions.md)
- [Notion System Design](https://app.notion.com/p/3cfcb2e9d47a811ba912e01c6bcb449e), [Requirements](https://app.notion.com/p/3cfcb2e9d47a81ed9ad9d2abb8a174fd), [Step2](https://app.notion.com/p/3e4cb2e9d47a81a0b8ced1cddb237ae7)

## Table / Session lifecycle ที่ใช้จริง

1. `POST /api/v1/dining-sessions` โดย SERVICE_STAFF ตรวจโต๊ะ AVAILABLE พร้อมล็อก row ตรวจจำนวนคนและ package/soup active สร้างรอบ ACTIVE และ table OCCUPIED ใน transaction เดียว Snapshot `packagePriceAtOpen` ตอนเปิด จึงไม่เปลี่ยนเมื่อ Manager แก้ราคา
2. Staff QR ชี้ `/customer/qr#token=...` หน้า Customer อ่านและล้าง fragment ทันที แล้ว `POST /api/v1/dining-sessions/qr-exchange` ส่ง token ใน body Backend ล็อก session หมุน QR token และออก `HttpOnly customer_session` เก็บเฉพาะ SHA256 credential hash พร้อม expiry8h ใน `customer_session_grants` ลูกค้าหลายเครื่องแลก QR รุ่นถัดไปได้
3. ใช้ cookie อ่าน `/customer-context` และทุก Customer API ตรวจ grant/expiry/ACTIVE/idตรงกัน ลูกค้าไม่รับ QR token หรือราคา snapshot ใน CustomerSessionResponse Staff DTO มี QR/ราคา snapshot/billRequestedAt แยกกัน
4. Ordering เรียก `requireSessionForOrder` และ refresh ภายใต้ lock แถว session ก่อน save Order ใช้ lockเดียวกับ bill request/Payment/close เพื่อกำหนดลำดับ ไม่รับ Order ใหม่หลัง bill request
5. Customer `POST /dining-sessions/{id}/bill-request` ตั้งเวลาเพียงครั้งแรก กดซ้ำ idempotent Sessionยัง ACTIVE แต่ทุกเครื่องสั่งเพิ่มไม่ได้ อ่าน menu/orders/bill status ได้ `GET /bill-status` คืน backend BillSummary และ NOT_REQUESTED/REQUESTED/PAID
6. Staff Payment อ่าน BillingContext ผ่าน `DiningSessionBillingReader` ใช้ snapshot/counts Backendคำนวณยอดเอง ต้องขอคิดบิลก่อน PAID, unique session_id กันชำระซ้ำ
7. Staffกด close แยก `PaymentStatusLookup` ต้องคืนผล PAID ของ session เดียวกัน แล้วลบ grantsทั้งหมด session COMPLETED และ table AVAILABLE ใน transactionเดียว providerไม่มีตอบ503 ยังไม่ปิดโต๊ะ

หลักฐาน implementation: [DiningSessionServiceImpl.java:89](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/DiningSessionServiceImpl.java#L89) · [CustomerSessionAccessService.java:55](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerSessionAccessService.java#L55) · [CustomerSessionAccessService.java:81](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerSessionAccessService.java#L81) · [CustomerBillingService.java:41](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java#L41) · [PaymentServiceImpl.java:71](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/PaymentServiceImpl.java#L71) · [DiningSessionServiceImpl.java:146](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/service/impl/DiningSessionServiceImpl.java#L146)

## QR design decision — แบบเดิมสู่ปัจจุบัน

แบบเดิมใน draft: GET token ใน path และส่ง token ทุกคำขอ ความเสี่ยงคือ tokenติด URL/serverlogs/history/referrer แบบปัจจุบัน: fragment→ล้างURL→POST bodyใช้ครั้งเดียว→หมุนQR→HttpOnly cookie/hash ในDB ไม่มี GET token-in-path ใน backendที่ merge เข้าdevelop จึงไม่มี endpointเก่าที่ต้อง deprecate ให้ลูกค้าเก่า ส่วน browser history/addressbarและการส่งต่อfragmentยังต้องระวัง ไม่อ้างว่า QR code ไม่มีความเสี่ยงทั้งหมด

Frontend แชร์ Promiseเฉพาะ tokenที่กำลังแลก ไม่cachetokenที่ใช้แล้ว คนละtokenในแท็บเดียวเรียงคำขอเพื่อให้cookieสุดท้ายตรงQRล่าสุด ล้างข้อมูลรอบก่อนเปลี่ยน QR และกันผลตอบเก่าทับหน้าใหม่ QRใหม่ใช้ไม่ได้แสดงerror ไม่fallbackcookieรอบเก่าอัตโนมัติ ดู [QR sequence](../diagrams/sequence-qr-exchange.puml) และ tests ของPR#16/#22

## JPA ของ Table/Session

| Mapping / constraint | เหตุผล |
|---|---|
| DiningSession→RestaurantTable/Package/Soup ManyToOne LAZY ไม่มี cascade | ข้อมูล master/shared history ไม่ควรถูกสร้างหรือลบตาม session Mapping DTO ภายใน service transaction, open-in-view=false |
| CustomerSessionGrant→DiningSession ManyToOne LAZY | grantแยกต่อเครื่องและมีexpiry closeลบgrantsอย่างชัดเจน ไม่กระจายcredentialให้โมดูลอื่น |
| orders.session_id FK RESTRICT V7 + indexจากV4 | รักษาประวัติออเดอร์ ห้ามลบรอบที่ถูกอ้าง; ไม่เพิ่มindexซ้ำ |
| payments.session_id unique + FK | หนึ่งรอบมีpaymentได้อย่างมากหนึ่งรายการ DBเป็นด่านท้ายการกันซ้ำ |
| session_token UK / token_hash UK และFKindexes V6 | QR lookup/grant lookup/indexFK และความเป็นเอกลักษณ์ ต่างจาก rowlockที่ใช้serializeกิจกรรม |
| packagePriceAtOpen DECIMAL10,2 V8 | precision/scaleตรงpackage price historicalsnapshot ไม่อ่านราคาปัจจุบันตอนจ่าย |
| bill_requested_at TIMESTAMP WITH TIME ZONE nullable V14 | OffsetDateTime UTCเมื่อขอ ไม่เพิ่ม DiningSessionStatus ใหม่ |

[DiningSession.java:25](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/domain/DiningSession.java#L25) · [CustomerSessionGrant.java:21](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/domain/CustomerSessionGrant.java#L21) · [Menu/Order rationaleของเจ้าของ](../architecture/sirapat-menu-ordering-solid-jpa.md) JPAcascadeกับSQL ON DELETEคนละกลไก อย่าสรุปแทนกัน Start/endTimeเป็นLocalDateTimeในcodeปัจจุบัน ไม่อ้างว่าทุกtimestampเป็นOffsetDateTime

## สถานะเทียบกับโค้ด

- Flyway common/H2/PostgreSQL มี V1–V14; central V13/V14 ตรวจ SELECT สดวันที่ 7 ตุลาคม พบ success=true/checksum ตรง repo พร้อม schema/grants [หลักฐาน](../testing/pr24-review-fixes-report.md#หลักฐาน-supabase-v13v14) ไม่ใช่ app JDBC/JPA validate ใหม่ และไม่ได้ apply/repair migration
- State/Strategy/Template Methodมีimplementationและtestsแล้ว ไม่ใช่เพียงintent; ดูparticipantsจริงใน pattern report
- Stockมี sku/name/unit/quantity/lowStockThreshold; openingTargetStock/active และ Profile first/last/phone ยังเป็นFinal ห้ามถือว่า ERD designเก่าคือcurrentJPA
- enum DiningSessionStatusประกาศCANCELLEDแต่ไม่มีcancelAPI Order state4ค่า UserRoles4ค่า Stringenum
- Layering/DI ยังมีช่องว่างที่พบจากsource: CustomerBillingService field-injectedEntityManager และ SessionUserContextProvider import AuthController constant รวมconcreteService dependencies ดู [SOLID gap log](../solid-analysis.md#ข้อจำกัดที่ต้องปิดก่อนรับรอง-final)

## Deployment decision สำหรับ Step3 — ยังไม่ implement

แผนหลัก Render Free WebService URLเดียว buildReactproductionแล้วSpringBootserveweb/API/Swagger APIbase `/api/v1` HTTPS Securecookies allowedOriginตรงURL SPArefreshห้ามทับAPI404/Swagger/assets ธีรเมธรับDocker/PORT/providers/runbook/deploymentdiagram ปวริศช์ตรวจintegration ศิระพัทธ์ตรวจpublicbrowser

SupabaseSessionPooler5432+SSL ฐานเดิม ห้ามแก้V1–V14 applied ต้องตรวจเลข/review/อนุมัติใหม่สำหรับStock/Profile forwardmigration ก่อนdeploycodeที่ต้องใช้schemaใหม่ [Step3 plan](../planning/step3-final-plan.md) publicURL/restart/coldstart/releaseSHAยังpending

## ประวัติ design ก่อนปิดStep2

ย้ายเอกสารbaselineจากNotion28ก.ย.2026 ตอนนั้นdevelopมีV1–V5, V6–V8ยังอยู่PR#16 วันที่29ก.ย.พบV6–V8ถูกapplyฐานกลางก่อนmergeโดยไม่ตั้งใจ พบ0แถวในsession/grant/orderขณะตรวจ และrolepostgresมีBYPASSRLS จึงAPIต้องตรวจสิทธิ์เอง FORCE RLSไม่จำกัดroleBYPASSRLS

ข้อมูลเดือนกันยายนเก็บเพื่ออธิบายที่มา ไม่ใช่สถานะปัจจุบัน PR#21รับรองV1–V14แล้ว ดู [Step2 completion](../testing/pavarit-step2-completion-report.md) ไม่repair historyเพื่อให้ผ่านหรือแก้appliedfilesย้อนหลัง

เมื่อcontract/schemaเปลี่ยน ให้อัปเดตsourceและdocsในงานเดียวกันและให้ownerตรวจร่วมกัน
