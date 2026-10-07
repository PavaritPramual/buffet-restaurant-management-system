# รายงาน Architecture/SOLID refactor หลัง PR #24

ตรวจวันที่ 7 ตุลาคม 2026 จากโค้ด `de7b5d546a05ad3ccef8c641ee5c53d638a8e039` บน branch `pavarit_673380278-9_01` เทียบฐาน develop `0dbbe1b7deae5db4189aa803f04a5246ccee8746` เอกสาร/evidence commit ที่ตามมาไม่เปลี่ยน runtime source

## สภาพแวดล้อมและผลตรวจ

Windows, Java 21.0.11 (Maven compiler release 17), Spring Boot 3.5.16, Maven 3.9.16, H2 test configuration และ PostgreSQL 18 ใน container ทิ้งได้เฉพาะงานนี้บน loopback port 15432 ไม่มี automated test ชี้ Supabase ไม่มี migration ใหม่ และไม่แก้ V1–V14

| การตรวจ | ผล |
|---|---|
| `mvn --batch-mode --no-transfer-progress verify` รวม H2 และ opt-in PostgreSQL | 336 tests, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS |
| Frontend `npm test` | 116/116, 14 test files |
| Runtime URL guards `node --test test/browser/step3-runtime-config.test.cjs` | 6/6 |
| Frontend lint | 0 errors; 4 warnings เดิมใน StockPage, KitchenBoardPage, StaffServingPage, UsersPage เรื่อง setState ใน effect |
| Frontend build | ผ่าน |
| Runtime jar + real session/database providers | Flyway V1–V14, JPA validate, health และ OpenAPI เริ่มสำเร็จบน PostgreSQL แยก |
| HTTP smoke ของ jar ล่าสุด | 34/34 คำขอ พร้อมตรวจยอด Stock/history และยอด Customer/Staff/Payment ตรงกัน |
| Source/API comparison | 192 production Java files; ไม่มี Service import Controller หรือ field injection; mapping/cookie annotations และ public DTO/enum/ErrorResponse/migration/frontend ไม่เปลี่ยน |
| PlantUML | source ทั้ง 6 ชุด render SVG/PNG สำเร็จและตรวจภาพครบ Component, Auth, Stock, Billing, State, Table/Session |

ผลและรายละเอียดราย suite อยู่ใน [test summary](../../test/evidence/architecture-refactor-2026-10-07/test-summary.json), [source/contracts](../../test/evidence/architecture-refactor-2026-10-07/source-contracts.json) และ [HTTP smoke](../../test/evidence/architecture-refactor-2026-10-07/runtime-smoke.json) ไม่มี password, cookie หรือ QR credential ใน evidence

## ตารางตอบข้อทักศรัณย์

| ข้อ | สิ่งที่แก้ | หลักฐานตรวจ |
|---|---|---|
| C03 layer/session contract | ย้าย userContext key ไป common; UserContextProvider แยกจาก Controller; runtime session reader ไม่ import Controller | SessionUserContextProviderTest, AuthStockIntegrationTest และ source check |
| D05 concrete dependencies / constructor injection | AuthenticationService กับ UserAdministrationService; StockTransactionProcessor สอง qualifier; BillCalculator; CustomerSessionVerifier; EntityManager และ cookie/CORS config ผ่าน constructor final | BootstrapAdminConfigTest, Auth/Stock/Payment integration, runtime jar startup; ไม่มี bean กำกวม |
| L04 null/identity/roles | identity ข้อมูลจำเป็นไม่ครบเป็น 401; authenticated role ผิดเป็น 403; billing snapshot ต้องตรง session; ตรวจ subtotal ก่อน discount และผล discount ก่อนยอดสุทธิ; fixture ใช้ role matrix เดียวกับ runtime | SessionUserContextProviderTest, DatabaseBillingContextContractTest, CustomerBillingContractTest, BillingEngineTest, FixtureOrderFulfillmentAccessProviderTest |
| O02 State extensibility | OrderStateResolver + List-based registry; ลงทะเบียน singleton เดิมผ่าน configuration; ไม่ใช้ static switch factory; ตรวจ enum ครบ/ซ้ำ/null ตอน startup | OrderStateResolverTest, OrderStateTest, OrderFulfillmentServiceTest และ OrderFulfillmentIntegrationTest |

หลักฐานโค้ดพร้อมบรรทัดดู [SOLID analysis](../solid-analysis.md), [Pattern docs](../design-patterns.md), [shared contracts](../contracts/shared-contracts.md) และ [diagrams](../diagrams/README.md)

## ธุรกรรมและ concurrency ที่คงไว้

- PostgreSQL suites ตรวจ Order ชน bill request/close, Payment ชน close และการปฏิเสธหลังปิดรอบ โดยใช้ row lock ของ DiningSession และ transaction จริง
- CustomerBillingService ยังคง lock และ refresh หลังได้ lock แล้วตรวจ credential ซ้ำ ไม่ใช้ mock/fixture เป็นหลักฐาน concurrency
- Stock-in กับ Adjustment ยังมี validation, row lock, history และ rollback ตามกฎเดิม; HTTP smoke ได้ balance 8 และ history 2 จากรับเข้า 10/ปรับ -2
- Customer bill, staff preview และ Payment อ่านราคาที่ snapshot ตอนเปิดรอบ ผ่าน calculator contract เดียวกัน การชำระไม่ปิดรอบอัตโนมัติ

## Runtime smoke ที่ตรวจจริง

เปิด `target/app.jar` ด้วย `spring.config.import` ว่างและ datasource loopback ที่กำหนดชัดเจน ใช้ master-data/menu/dining-session/fulfillment แบบ session และ ordering/billing/payment แบบ database ไม่มี fixture provider

ใช้ cookie jars แยก Manager, Service Staff, Kitchen, Supervisor และ Customer ผ่าน HTTP จริง ตรวจ login/me/logout, 401 ก่อน login, 403 role ผิด/ปลอม X-User-Role, สร้างผู้ใช้, Stock-in/Adjustment, เปิดโต๊ะ, แลก QR, ขอคิดบิล, Staff preview, ชำระ และ close หลัง close credential ลูกค้าได้ 401 ตาม contract ข้อมูลทดสอบทั้งหมดอยู่ในฐานทิ้งได้

นี่เป็น local HTTP smoke ไม่ใช่ browser/UI acceptance หรือ public deployment การทดสอบ browser/public release ยังต้องรันกับรุ่นที่ทีมส่ง

## ข้อผิดพลาดของการจัดสภาพทดสอบและการแก้

การรัน backend ซ้ำรอบหนึ่งใช้ database เดิมที่มี V14 แล้ว ทำให้ PostgresMenuOrderingMigrationTest ซึ่งต้องเริ่มจากฐานใหม่และตรวจ foundation V2 ไม่ผ่าน (expected V2, actual V14) จึงสร้าง database ใหม่สามฐานที่มี marker `buffet-disposable-test-only` และรันชุดทั้งหมดอีกครั้ง ผลล่าสุด 336/336 ไม่เปลี่ยน assertion หรือใช้ Flyway repair

การรันซ้ำต้องสร้าง PostgreSQL databases ใหม่ตาม `code/backend/src/test/resources/disposable-postgres-ci.sql` และตั้ง ALLOW_DESTRUCTIVE_DB_TESTS พร้อม MENU/DINING/PAYMENT_TEST_PG_URL เป็น loopback databases ที่มี marker ห้าม reuse ฐานที่ผ่าน migration suite แล้ว และห้ามใช้ฐานร่วมของทีม

## สิ่งที่ยังรอรับรอง

- งานรอบนี้ implement/tests ผ่าน แต่รอ PR review และ merge; ไม่ merge เอง
- Reviewer: ศรัณย์ architecture/API/State, ธีรเมธ Billing/Payment, เมธัส Auth/Stock, ศิระพัทธ์ regression
- การเพิ่ม State ผ่าน registration ไม่ยกเว้นการตรวจ enum/transitions/API/UI เมื่อเพิ่มสถานะธุรกิจใหม่
- Stock/Profile extensions, public deployment, owner confirmation, สไลด์และ Final release ยังคงเป็น gates แยก สไลด์ยังเป็นฉบับล่วงหน้า
- ไม่อ้างว่า interfaces หรือผล tests ชุดนี้รับรอง SOLID ทุกคลาสทั้งระบบ
