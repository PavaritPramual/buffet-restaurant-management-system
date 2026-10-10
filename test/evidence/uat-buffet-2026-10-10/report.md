# UAT หลัง deploy PR #49 — Buffet Restaurant Management

ผลสรุป: **กรณี Stock/Ordering ที่ได้รันจริงผ่านตามขอบเขตด้านล่าง พบข้อผิดพลาดเอกสาร Swagger 2 กลุ่ม; กรณี Stock โหลดล้มเหลวไม่เกิดจึงยังไม่ได้ตรวจ recovery** ไม่รับรอง UAT ทั้งระบบหรือฐานข้อมูลทั้งหมด

- Public URL: https://buffet-restaurant-management.onrender.com
- Swagger: https://buffet-restaurant-management.onrender.com/swagger-ui/index.html
- วันที่ 10 ตุลาคม 2026; เวลา Asia/Bangkok (UTC+07:00); เริ่ม preflight 11:59, สร้าง/ทดสอบข้อมูล 12:13–12:36 โดยประมาณ
- Run ID: `UAT261010TEERAMET`; ใช้ข้อมูลใหม่ทั้งหมดสำหรับ UAT ไม่มีการแก้/ลบข้อมูลเดิมของทีม
- ทดสอบหน้าเว็บจริงและปุ่ม Execute ของ Swagger ตาม schema ที่เห็นจริง ไม่มี SQL, migration, cookie extraction หรือการนำ cookie ไปใส่ Authorize
- ผู้ใช้อนุญาตให้ใช้ browser เดียวแบบ logout/login สลับ Manager, SERVICE_STAFF และ KITCHEN_STAFF แล้ว จึง **ไม่ได้ตรวจหลาย Staff session พร้อมกันใน profile แยก**
- Customer รับสิทธิ์จากลิงก์ QR สดที่ Staff UI ออกให้ session15 จริง; **ไม่ได้ตรวจการสแกนด้วยกล้องมือถือ** และไม่บันทึก QR/token/cookie/password ลง artifacts

## รุ่นและ migration

| จุดตรวจ | สถานะ | หลักฐาน / ขอบเขตที่ยืนยันได้ |
|---|---|---|
| Render deployed commit | ผ่านจากหลักฐานเจ้าของระบบ | ภาพ Render ที่ผู้ใช้ส่งก่อนเริ่ม UAT แสดง `Deploy succeeded / Live`, Source `af2b45b`, ชื่อ Merge PR #49, Deployed 10 ต.ค. 2026 เวลา 11:23:55 GMT+7; commit เป้าหมายเต็ม `af2b45ba5d73cfcb271d312f978374b394dd4b97`. ระหว่างรอบนี้ Render browser ของผู้ทดสอบอยู่หน้า login จึงยังไม่ได้อ่าน Source ล่าสุดด้วย session ของตนเอง |
| V19 | ผ่านจาก startup log ที่ผู้ใช้ส่ง | เวลา 11:47:12: Successfully validated 19 migrations; 11:47:13: Current version public = 19, schema up to date, no migration necessary. ไม่ได้ query ฐานกลาง และไม่อ้างว่าได้ตรวจทุกตาราง/สิทธิ์ฐานข้อมูล |
| JPA startup | ผ่านจาก startup log ที่ผู้ใช้ส่ง | 11:47:51 Initialized JPA EntityManagerFactory; 11:48:35 Started BuffetRestaurantApplication |
| Readiness | ผู้ใช้ตรวจผ่าน; รอบ browser ของผู้ทดสอบตรวจไม่ได้ | ผู้ใช้รายงาน `UP` ก่อนเริ่ม UAT; browser ของผู้ทดสอบเปิด `/actuator/health/readiness` แล้วเกิด `net::ERR_BLOCKED_BY_CLIENT`. จัดเป็นข้อจำกัดเครื่องมือ browser ไม่ใช่หลักฐานว่า API ล้มเหลว |
| System health ผ่าน Swagger | ผ่าน | เวลา 11:59:42, GET `/api/v1/system/health` → HTTP 200, `{"status":"UP","service":"buffet-restaurant-backend"}`; [ภาพ](swagger-health-200.jpg) |
| Swagger endpoint ที่ต้องใช้ | ผ่าน | โหลดรายการจริงและพบ stock create/in/history, menu create/update/stock-usage, customer menu/orders, incoming และ PATCH order status ครบ |

## ผลทดสอบหน้าเว็บ

| ID | ข้อทดสอบ | ผลที่คาดหวัง | ผลจริง | สถานะ | เวลา ICT | หลักฐาน |
|---|---|---|---|---|---|---|
| UI-01 | สร้างวัตถุดิบ/รับเข้า | เริ่มศูนย์; รับเข้าและ IN history ตรงกัน | A ID6 เริ่ม 0; รับเข้า 3 kg; IN11 +3/balance3; UI/API ตรงกัน | ผ่าน | 12:13–12:13:54 | [ui-stock-A-initial-zero.jpg](ui-stock-A-initial-zero.jpg) · [ui-stock-A-in-3kg.jpg](ui-stock-A-in-3kg.jpg) |
| UI-02 | เมนูมีสูตร/ไม่มีสูตร | บันทึก 0.100 kg ต่อเสิร์ฟและเมนูสูตรว่างได้ | menu44 automatic=true/A0.1; menu45 false/[]; อ่าน UI/API กลับตรง | ผ่าน | 12:14–12:21 | [ui-menu-recipe-readback.jpg](ui-menu-recipe-readback.jpg) · [swagger-menu45-no-stock-usage-200.jpg](swagger-menu45-no-stock-usage-200.jpg) |
| UI-03 | QR สด → สั่ง 2 เสิร์ฟ | RECEIVED; ไม่หักก่อน PREPARING | ลิงก์จาก QR สดของ session15 เข้า Customer ได้; UI order7/menu44×2 RECEIVED; A3/B0.05 หลังสั่งทุกใบ | ผ่านเฉพาะ QR link/grant และเว็บ; ไม่ได้ตรวจกล้อง | 12:26:04–12:29 | [ui-customer-order7-received.jpg](ui-customer-order7-received.jpg) · [stock-after-orders-before-kitchen.json](stock-after-orders-before-kitchen.json) |
| UI-04 | ครัวเริ่มทำ | หักสูตร×จำนวนเสิร์ฟ; CONSUMPTION ผูก orderId | UI order7 PREPARING; A3→2.8; transaction13 CONSUMPTION -0.2/orderId7; UI/API ตรง | ผ่าน | 12:31:19–12:32:04 | [ui-kitchen-order7-preparing.jpg](ui-kitchen-order7-preparing.jpg) · [ui-stock-after-order7.jpg](ui-stock-after-order7.jpg) · [swagger-consumption-order7.jpg](swagger-consumption-order7.jpg) |
| UI-05 | เริ่มทำซ้ำ | ไม่หัก stock/history ซ้ำ | UI เปลี่ยนปุ่มเป็นพร้อมเสิร์ฟ จึงส่งคำขอซ้ำผ่าน Swagger; 400 PREPARING→PREPARING; history มีของ order7 เพียงรายการเดียว | ผ่าน | 12:32:37; ยืนยัน history 12:35–12:36 | [swagger-order7-duplicate-400.jpg](swagger-order7-duplicate-400.jpg) · [stock-A-history-final.json](stock-A-history-final.json) |
| UI-06 | stock ไม่พอแบบ 2 วัตถุดิบ | ปฏิเสธทั้ง order; ไม่หัก A ที่มีพอ; B/history คงเดิม; RECEIVED | UI order10 ต้องใช้ A/B อย่างละ0.2 แต่ B0.05; ข้อความปฏิเสธ; order10 RECEIVED; ไม่มี CONSUMPTION ของ order8/10 ใน A/B | ผ่าน | 12:33; ยืนยัน history 12:35–12:36 | [ui-kitchen-order10-shortage.jpg](ui-kitchen-order10-shortage.jpg) · [stock-A-history-final.json](stock-A-history-final.json) · [stock-B-history-final.json](stock-B-history-final.json) |
| UI-07 | เปลี่ยนสูตรหลังสั่ง | ออเดอร์เก่า snapshot; ออเดอร์ใหม่สูตรล่าสุด | สร้าง order7 เมื่อ A0.1; Manager UI เปลี่ยนเป็น0.25 และ PUT ยืนยัน; order7×2 หัก0.2; order11×2 หัก0.5 | ผ่าน | 12:26:34 → แก้12:30:20 → เริ่ม12:31:19/12:33:37 | [menu44-updated-recipe.json](menu44-updated-recipe.json) · [stock-A-history-final.json](stock-A-history-final.json) |
| UI-08 | เมนูไม่มีสูตร | สั่ง/เริ่ม/เสิร์ฟได้; ไม่หัก stock | order9/menu45×1 เดิน RECEIVED→PREPARING→READY→SERVED ผ่าน UI; ไม่มี CONSUMPTION ของ order9 | ผ่าน | 12:27:48–12:34:50 โดยประมาณ | [ui-kitchen-order9-no-recipe-preparing.jpg](ui-kitchen-order9-no-recipe-preparing.jpg) · [ui-service-order9-ready.jpg](ui-service-order9-ready.jpg) · [customer-orders-after-kitchen.json](customer-orders-after-kitchen.json) |
| UI-09 | stock load failure/retry (เงื่อนไขถ้าเกิด) | เมนูอื่นยังใช้ได้; retry หลังระบบคืน | Stock/menu โหลดสำเร็จตลอดรอบ; ไม่พบ failure จึงไม่สามารถรับรอง recovery และไม่แก้ระบบกลางเพื่อบังคับให้ล้มเหลว | ทดสอบไม่ได้ — ไม่เกิดเงื่อนไข | 12:13–12:36 | [ui-stock-final.jpg](ui-stock-final.jpg) |

## ผล API ผ่าน Swagger UI

ทุก path ในตารางต่อจาก `/api/v1`; ใช้ dataset เดียวกับหน้าเว็บ

| ID | ข้อทดสอบ | ผลที่คาดหวัง | ผลจริง | สถานะ | เวลา ICT | หลักฐาน |
|---|---|---|---|---|---|---|
| API-01 | POST /stock/items | 201; quantity=0 | Manager สร้าง stock B ID7 ตาม schema →201/quantity0 | ผ่าน | 12:18:45 | [swagger-stock-B-create-201-zero.jpg](swagger-stock-B-create-201-zero.jpg) |
| API-02 | POST /stock/{itemId}/in | ยอด/history/UI ตรง | B+0.05 →200/transaction12/balance0.05; UI ตรง | ผ่าน | 12:19:09 | [swagger-stock-B-in-0050-200.jpg](swagger-stock-B-in-0050-200.jpg) · [ui-stock-A-B-baseline.jpg](ui-stock-A-B-baseline.jpg) |
| API-03 | GET /stock/transactions?itemId=6/7 | IN/CONSUMPTION ตาม order; เทียบ UI | A มีIN11, CONSUMPTION13(order7,-0.2),14(order11,-0.5); B มีIN12เท่านั้น; UI ตรง | ผ่าน | 12:19:36; 12:32:04; 12:35–12:36 | [stock-A-history-final.json](stock-A-history-final.json) · [stock-B-history-final.json](stock-B-history-final.json) · [ui-stock-final.jpg](ui-stock-final.jpg) |
| API-04 | POST/PUT /menu-items | บันทึกสูตรได้; snapshot ไม่เปลี่ยนย้อนหลัง | POSTmenu46→201 สูตรA/B0.1; PUTmenu44→200 สูตรA0.25 หลังorder7; history ยืนยันsnapshot | ผ่าน API; documentation 201 ไม่ตรง | 12:20:17; 12:30:20 | [swagger-menu-rollback-create-201.jpg](swagger-menu-rollback-create-201.jpg) · [menu44-updated-recipe.json](menu44-updated-recipe.json) |
| API-05 | GET /menu-items/{id}/stock-usage | สูตรตรง Manager UI | 44=0.1ก่อน/0.25หลัง;45=false/[];46=A/B0.1 →200 | ผ่าน | 12:18:03–12:30:33 | [swagger-menu44-stock-usage-200.jpg](swagger-menu44-stock-usage-200.jpg) · [swagger-menu44-updated-0250.jpg](swagger-menu44-updated-0250.jpg) · [menu46-stock-usage.json](menu46-stock-usage.json) |
| API-06 | GET /dining-sessions/15/menu | cookie QR อ่านเฉพาะเมนูpackageได้ | 200: menu44/45/46 ผูกpackage7 ตรง Customer UI; cookieส่งอัตโนมัติ ไม่กรอกช่องcookie | ผ่าน | 12:27:03 | [swagger-customer-menu15-200.jpg](swagger-customer-menu15-200.jpg) |
| API-07 | POST /dining-sessions/15/orders | สร้าง RECEIVED; ไม่หักก่อนครัว | bodyตามSwagger: menu46×2 →201/order8/RECEIVED; stockก่อนครัวยังA3/B0.05; GETorders200ตรงUI | ผ่าน API; documentation 201 ไม่ตรง | 12:27:29; GET12:28:42 | [swagger-customer-order8-201.jpg](swagger-customer-order8-201.jpg) · [customer-orders-before-kitchen.json](customer-orders-before-kitchen.json) · [stock-after-orders-before-kitchen.json](stock-after-orders-before-kitchen.json) |
| API-08 | GET /orders/incoming (Kitchen) | พบออเดอร์UAT/สถานะตรงUI | 200: order7/8/9/10/11 ก่อนเริ่ม; หลังเริ่มพบ7/11PREPARING,8/10RECEIVED;9READYไม่อยู่incoming | ผ่าน | 12:31; 12:33:50โดยประมาณ | [incoming-before-kitchen.json](incoming-before-kitchen.json) · [incoming-after-shortage.json](incoming-after-shortage.json) · [swagger-incoming-kitchen-200.jpg](swagger-incoming-kitchen-200.jpg) |
| API-09 | PATCH /orders/{id}/status | PREPARING200; หักถูก; ส่งซ้ำไม่หัก | order11 PREPARING200 หัก0.5; order7ซ้ำ400; historyของ order7 มี transaction ID13 เพียงหนึ่งรายการ ไม่มีduplicate | ผ่าน | 12:32:37; 12:33:37 | [swagger-order11-preparing-200.jpg](swagger-order11-preparing-200.jpg) · [swagger-order7-duplicate-400.jpg](swagger-order7-duplicate-400.jpg) · [stock-A-history-final.json](stock-A-history-final.json) |
| API-10 | PATCH order8 stock ไม่พอ | 409/ErrorResponse; rollbackทั้งorder/stock | 409 Conflict ต้องใช้0.200มี0.050; order8/10RECEIVED; Aหักเฉพาะ7/11 รวม0.7 ไม่มี partial หรือhistoryของshortage; B0.05ไม่เปลี่ยน | ผ่าน | 12:32:53; ยืนยัน12:35–12:36 | [order8-shortage-error409.json](order8-shortage-error409.json) · [incoming-after-shortage.json](incoming-after-shortage.json) · [stock-final.json](stock-final.json) · [stock-B-history-final.json](stock-B-history-final.json) |
| API-11 | ไม่มี Staff login | 401/ErrorResponse | GET incoming401 A signed-in user is required; logoutแล้ว GETstock401 แม้Customer grantยังอยู่ | ผ่าน | 12:00:25; 12:34:24 | [swagger-incoming-no-login-401.jpg](swagger-incoming-no-login-401.jpg) · [swagger-stock-no-login-401.jpg](swagger-stock-no-login-401.jpg) |
| API-12 | roleผิด | 403; ไม่อนุญาตเข้าถึงต่างหน้าที่ | Manager GETincoming403; Kitchen GETstock403 Your role is not allowed to perform this operation | ผ่าน | 12:20:43; 12:33:56 | [swagger-incoming-manager-403.jpg](swagger-incoming-manager-403.jpg) · [swagger-stock-kitchen-403.jpg](swagger-stock-kitchen-403.jpg) |

## การหัก Stock และ rollback ที่ตรวจได้

| จุดตรวจ | A (ID6) kg | B (ID7) kg | ประวัติใหม่ |
|---|---:|---:|---|
| หลังรับเข้า ก่อนสั่ง | 3.000 | 0.050 | IN11(A), IN12(B) |
| หลังสั่งก่อน Kitchen | 3.000 | 0.050 | ยังไม่มี CONSUMPTION |
| หลังเริ่ม order7 | 2.800 | 0.050 | CONSUMPTION13: -0.200, orderId7 |
| หลังเริ่มซ้ำ/shortage และเริ่ม order11 | 2.300 | 0.050 | เพิ่มเฉพาะ CONSUMPTION14: -0.500, orderId11 |

ยอด A: `3.000 − (0.100×2) − (0.250×2) = 2.300 kg` ตรงทั้ง UI/API. มี CONSUMPTION ของ order7 เพียงครั้งเดียว และไม่มีของ order8/10/9 ในประวัติ A/B จึงไม่พบ duplicate deduction, partial consumption หรือการหักของเมนูไม่มีสูตร. ตรวจจากยอดและประวัติที่ระบบเปิดให้อ่าน พร้อมสถานะออเดอร์; ไม่ได้ตรวจ transaction internals หรือ query ฐานกลาง

[ยอดก่อนครัว](stock-after-orders-before-kitchen.json), [ยอดหลัง order7](stock-after-order7.json), [ยอดสุดท้าย](stock-final.json), [history A](stock-A-history-final.json), [history B](stock-B-history-final.json)

## ข้อผิดพลาด Swagger แยกจาก API

### DOC-01 — ไม่ผ่าน: ErrorResponse examples ไม่ตรง status/operation

Example Value ใต้ 401/403/409 และบางสถานะอื่นแสดง `status:400`, `Bad Request`, path `/api/v1/billing/preview` แม้เป็น Stock หรือ Order Fulfillment. Actual Execute ตอบ 401/403/409 และ path ถูกต้อง. ต้องแก้ OpenAPI examples ให้ตรง code/operation หรือเอาตัวอย่างที่ผิดออก; **ไม่ใช่หลักฐานว่า API authorization/rollback ล้มเหลว**

[ตัวอย่างเอกสารผิด](swagger-example-mismatch.jpg), [401 จริง](swagger-stock-no-login-401.jpg), [403 จริง](swagger-stock-kitchen-403.jpg), [409 จริง](swagger-order8-shortage-409.jpg)

### DOC-02 — ไม่ผ่าน: Create response 201 ไม่ได้ประกาศ

POST `/api/v1/menu-items` สร้าง menu46 และ POST `/api/v1/dining-sessions/15/orders` สร้าง order8 จริงตอบ201พร้อม Location แต่ Swagger ประกาศเพียง200 จึงขึ้น `201 Undocumented`. API สร้างสำเร็จ; ควรประกาศ response201 ใน OpenAPI ให้ตรง controller

[menu create201](swagger-menu-rollback-create-201.jpg), [order create201](swagger-customer-order8-201.jpg)

ไม่พบข้อผิดพลาด UI/API ที่ทำให้กรณีหลักที่รันล้มเหลวในรอบนี้; พบข้อผิดพลาดเอกสารข้างต้น ไม่ได้แก้โค้ดระหว่าง UAT

## ทะเบียนข้อมูล UAT และสถานะหลังทดสอบ

| ประเภท | ชื่อ/รหัส | ID | สถานะที่บันทึกจริง |
|---|---|---:|---|
| Stock A | UAT261010TEERAMET-A | 6 | เริ่ม0, รับเข้า3, สุดท้าย2.300kg |
| Stock B | UAT261010TEERAMET-B | 7 | เริ่ม0, รับเข้า0.050, สุดท้าย0.050kg |
| Category | UAT261010TEERAMET | 13 | หมวดใหม่ |
| Package | UAT261010TEERAMET | 7 | ราคา100.00; packageเฉพาะUAT |
| Menu recipe | UAT261010TEERAMET สูตรสำเร็จ | 44 | สูตรA0.100ตอนorder7; เปลี่ยนเป็น0.250ก่อนorder11 |
| Menu no recipe | UAT261010TEERAMET ไม่หักสต๊อก | 45 | automatic=false, [] |
| Menu shortage | UAT261010TEERAMET สต๊อกไม่พอ | 46 | A/Bอย่างละ0.100ต่อเสิร์ฟ |
| Table | UAT261010TEERA | ไม่อ่านtableId | สร้างใหม่ capacity4; ยังกำลังใช้งาน |
| Soup | UAT261010TEERAMET | ไม่อ่านsoupId | สร้างใหม่สำหรับUAT |
| Dining session | โต๊ะUAT/ผู้ใหญ่1/เด็ก0/package7 | 15 | เปิด12:26:04; ยัง ACTIVE; ยังไม่ได้ขอบิล/จ่าย/ปิดรอบในชุดUATนี้ |
| Order old recipe/UI | menu44×2 สูตร0.100 | 7 | PREPARING; หักA0.200ผ่านUI |
| Order shortage/API | menu46×2 | 8 | RECEIVED; PATCH409 |
| Order no recipe/UI | menu45×1 | 9 | SERVED; ไม่หักStock |
| Order shortage/UI | menu46×2 | 10 | RECEIVED; UIปฏิเสธ |
| Order new recipe/UI→API | menu44×2 สูตร0.250 | 11 | PREPARING; หักA0.500ผ่านAPI |
| Transactions | IN A/B, CONSUMPTION order7/11 | 11/12/13/14 | รายละเอียดในJSON |

ข้อมูล UAT ยังคงอยู่เพื่อให้ reviewer ตรวจซ้ำ **ไม่ได้ลบ/เก็บออกหรือเพิ่ม Stock เพื่อทำให้กรณี shortage ผ่าน**. โต๊ะ UAT ยังใช้งานอยู่; ไม่ได้แตะออเดอร์เดิมของทีมที่เห็นในหน้าครัว. ออกจากบัญชี Staff หลังเก็บหลักฐานแล้ว

[ทะเบียน JSON](data-registry.json), [ออเดอร์ก่อนครัว](customer-orders-before-kitchen.json), [ออเดอร์สุดท้าย](customer-orders-after-kitchen.json)

## หลักฐานภาพสำคัญ

- [Stockใหม่ศูนย์](ui-stock-A-initial-zero.jpg), [รับเข้า3kg](ui-stock-A-in-3kg.jpg), [ก่อนครัวยังไม่หัก](ui-stock-after-orders-before-kitchen.jpg)
- [ลูกค้าสั่งorder7](ui-customer-order7-received.jpg), [Kitchen PREPARING](ui-kitchen-order7-preparing.jpg)
- [order7 CONSUMPTION](swagger-consumption-order7.jpg), [เริ่มซ้ำ400](swagger-order7-duplicate-400.jpg)
- [UI stockไม่พอ](ui-kitchen-order10-shortage.jpg), [API409](swagger-order8-shortage-409.jpg)
- [สูตรใหม่0.250](swagger-menu44-updated-0250.jpg), [order11เริ่ม200](swagger-order11-preparing-200.jpg)
- [no-recipe PREPARING](ui-kitchen-order9-no-recipe-preparing.jpg), [READY](ui-service-order9-ready.jpg), [Customerสถานะสุดท้าย](ui-customer-final-order-statuses.jpg)
- [Stock/historyสุดท้ายUI](ui-stock-final.jpg), [history A API](swagger-stock-A-history-final.jpg), [history B API](swagger-stock-B-history-final.jpg)

## ขอบเขตที่ยังไม่รับรอง

- Mobile camera QR scan, หลายStaffsessionพร้อมกัน/profileแยก และ recovery เมื่อ Stock API ล้มเหลว
- Deployed SHA/V19 อ้างอิงภาพและstartup logที่ผู้ใช้ส่ง; ไม่ได้เข้าRender/Supabaseที่ต้องมีสิทธิ์และไม่ได้queryฐานกลาง
- ไม่ได้ redeploy/restart/cold-start ระหว่างรอบนี้ จึงไม่รับรอง persistence หลัง redeploy จาก UATชุดนี้
- Billing/Payment/close ไม่ได้ทดสอบซ้ำในUATStockนี้; ผลรอบก่อนต้องอ้างหลักฐานรอบก่อนแยกต่างหาก

รายงานนี้พร้อมให้ reviewer ตรวจผลที่รันจริงและข้อจำกัด แต่ไม่ควรเปลี่ยนกรณีที่ไม่ได้รันเป็นผ่าน หรือรับรองว่าฐานข้อมูล/ระบบทั้งหมดผ่าน
