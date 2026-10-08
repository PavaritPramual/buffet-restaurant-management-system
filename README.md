# Buffet Restaurant Management System

ระบบจัดการร้านบุฟเฟต์แบบ Walk-in ครอบคลุมเปิดโต๊ะ เลือกแพ็กเกจ/น้ำซุป และให้ลูกค้าสแกน QR เพื่อสั่งอาหาร
ครัวรับและเตรียมออเดอร์ พนักงานเสิร์ฟ รับชำระ แล้วกดปิดรอบแยกเพื่อคืนโต๊ะ
Manager จัดการข้อมูลร้านและสต็อก พร้อมดูรายการ/สร้างผู้ใช้และ basic profile โดย backend ตรวจสิทธิ์และคำนวณยอดจากข้อมูลจริง
พัฒนาด้วย Spring Boot, React และ PostgreSQL สำหรับวิชา CP353002 Principles of Software Design and Development

## สถานะปัจจุบัน

**สไลด์ฉบับล่วงหน้า ยังไม่รับรอง:** [Canva ทีม v02b](https://www.canva.com/d/yWw6P3disBOSfHS) แก้ข้อเท็จจริงตามรีวิว PR #24 เฉพาะ4หน้า คง76หน้า ลำดับและผู้พูดเดิมไว้ก่อน เป้าพรีเซนต์12นาทีและ runbook ยังเป็นร่าง รอให้โค้ดทุกส่วนเสร็จแล้วทบทวนเนื้อหา/การเรียง/ช่วงพูด ดู [guide/version](doc/slide/README.md), [coverage](doc/slide/course-diagram-coverage.md) และ [notes](doc/slide/team-final-canva-v02-content.md) ใช้Canvaเป็นต้นฉบับและเก็บทั้งPPTXกับPDFที่exportจากCanvaตามเวอร์ชัน ยังไม่มีไฟล์exportของร่างนี้

Code baseline: merged [`develop adc5798`](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/adc5798b05279840dc6178f4291278467c929791) ณ 8 ตุลาคม 2026. CI [run 37738400052](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37738400052) ผ่าน Backend/PostgreSQL 348/348, Frontend 121/121, URL guards 6/6, lint 0 errors/4 existing warnings และ build ผ่าน. Stock target/active และ Profile fields อยู่ใน V15 ที่ merge ผ่าน [PR #28](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/28) พร้อม tests และ reviewer approvals; หลักฐาน Stock/Profile candidate browser ระบุแยกใน [follow-up report](doc/testing/sirapat-step3-followup-2026-10-08.md)

เอกสาร/สไลด์และ CI **ยังไม่ใช่การรับรอง Final**. PR #33 (integrated JPA/evidence) merge แล้วเข้า `develop e6172b2`; PR #34 (Final Matrix/Git audit/README) ยัง open, CI ผ่านบน `345cf44` แต่ formal re-review/approval ยังรอ. Git audit ล่าสุดนับ 123 non-merge commits ใน `develop`; meaningfulness/account/ownership/การกระจายเวลายังต้องให้สมาชิกยืนยันเอง และแบบฟอร์มยืนยันนับเฉพาะ commits ที่ merge เข้า `develop`. Public deployment/URL, V15 central-schema approval/validate, integrated public regression, reviewed release บน main/deployed SHA และสไลด์ export/ซ้อมทีมยังต้องปิดตามหลักฐานจริง ดู [Requirement Matrix](doc/planning/step3-requirement-matrix.md), [Git audit](doc/planning/step3-git-audit.md), [dated Git evidence](test/evidence/sarun-git-audit-2026-10-08/README.md), [Step3 plan](doc/planning/step3-final-plan.md) และ [Notion](https://app.notion.com/p/3f1cb2e9d47a81e28aa2dc642cd6ead6)

**เกณฑ์ Git ปัจจุบัน:** ขั้นต่ำ 15 meaningful commits ต่อคน; การแจ้ง 5 commits วันที่ 7 ต.ค. ถูกยกเลิกวันที่ 8 ต.ค. ดู [criteria update](doc/planning/course-criteria-updates.md) และ [Git audit](doc/planning/step3-git-audit.md)

## สมาชิกและหน้าที่

| ลำดับ | สมาชิก | รหัสนักศึกษา | Section | Personal branch | Feature / Final responsibility |
|---:|---|---|---:|---|---|
| 1 | ปวริศช์ ประมวล | 673380278-9 | 01 | `pavarit_673380278-9_01` | Table/Package/Soup/DiningSession, integration, architecture/docs/Git/release |
| 2 | ศิระพัทธ์ วงศ์วิวัฒน์เสรี | 673380293-3 | 01 | `sirapat_673380293-3_01` | Menu/Ordering, Customer/sharedUI, regression/traceability |
| 3 | ศรัณย์ พาพรชัย | 673380515-1 | 02 | `sarun_673380515-1_02` | Kitchen/Serving/State, API/DTO/serialization audit |
| 4 | ธีรเมธ สายคำ | 673380273-9 | 02 | `teeramet_673380273-9_02` | Billing/Payment/Strategy, productiondeployment/runbook |
| 5 | เมธัส มณีวิจิตร | 673380300-2 | 01 | `methus_673380300-2_01` | Auth/User/Stock/TemplateMethod/AdminShell, DBtoolingและFinalextensions |

## Tech Stack

- Java17+, Spring Boot **3.5.16**, Maven wrapper, SpringDataJPA/Hibernate, Flyway, springdocOpenAPI
- PostgreSQLผ่าน Supabase **Session Pooler5432+SSL**; H2ใช้เฉพาะdemo/isolatedtests
- React19, TypeScript6, Vite8, ReactRouter7, Axios, Tailwind4, qrcode.react4
- JUnit5/Mockito/SpringBootTest, Vitest/TestingLibrary, Oxlint และGitHubActions
- SharedUI: Noto Sans Thai, primary `#9A3412`, background `#FFFDF9`, components/shellเดิม
- StaffAuthใช้ Spring **HTTPsession cookie** ไม่ใช้SupabaseAuth JWT ลูกค้าใช้QRแลกHttpOnlycustomergrant ไม่สร้างCustomeraccount

## Architecture

Controller → Service → Repository → Domain/DB ViewคือReactและDTO/Mapperเป็นขอบเขตJSON Servicesเป็นเจ้าของtransactions
Orderingอ่านสิทธิ์ผ่านSessionContextProvider Billingอ่านopeningprice/countsผ่านDiningSessionBillingReader closeอ่านPaymentStatusLookup
ไม่รับราคา/ยอดชำระจากbrowser และไม่ใช้X-User-Roleเพิ่มสิทธิ์

[Component source](doc/diagrams/component.puml) · [SVG preview](doc/diagrams/previews/component.svg) · [System Design](doc/system-design/README.md)
[SOLIDพร้อมไฟล์/บรรทัดและgaps](doc/solid-analysis.md) · [Enterprise/Behavioral Patterns](doc/design-patterns.md)

## ERD และ Data Dictionary

[ERD source](doc/diagrams/er-diagram.puml) · [Data Dictionary V1–V14 (Step 2 baseline)](doc/database/step2-schema-approved.md) · [V15 delta](doc/database/auth-stock-schema-delta.md) · [Diagram index](doc/diagrams/README.md)
มีTables/Packages/Soups/Sessions/Grants/Menu/Orders/Payments/Users/Profile/Stockและtransactions
Session snapshotราคาเมื่อเปิด billrequestหยุดOrderใหม่ PAIDยังต้องStaffcloseแยก ดู [Bill/Payment sequence](doc/diagrams/sequence-billing-payment.puml)

## Installation / Setup

ต้องมี Git, Java17+, Node24/npm และDockerComposeถ้าจะใช้containers Cloneและเลือกpersonalbranchของตน
ไฟล์ตัวอย่างอยู่ `code/backend/.env.example` กับ `code/frontend/.env.example`
คัดลอกเป็น `.env` ในโฟลเดอร์เดียวกันและใส่ค่าของสภาพแวดล้อมตน backend import optional `.env` เมื่อรันจาก `code/backend`
DB host/user/passwordใช้ค่าจากSupabaseSessionPooler ไม่ใช่REST service-role APIkey ห้ามcommit.env/secrets และVITE_*เป็นค่าที่browserอ่านได้

### Providers ของระบบรวม

ตั้งค่าให้ตรง `.env.example`/Compose:

```dotenv
DINING_SESSION_STAFF_ACCESS_PROVIDER=session
FULFILLMENT_ACCESS_PROVIDER=session
MASTER_DATA_ACCESS_PROVIDER=session
MENU_ADMIN_ACCESS_PROVIDER=session
ORDERING_SESSION_PROVIDER=database
BILLING_CONTEXT_PROVIDER=database
PAYMENT_STATUS_PROVIDER=database
```

Standaloneบางprovider defaultdisabledและตอบ503เพื่อfailclosed จึงต้องตั้งค่าข้างต้นให้ครบ ไม่ใช้fixtureprovidersในระบบรวม
Frontendค่าlocal `VITE_API_BASE_URL=http://localhost:8080/api/v1` และ `CORS_ALLOWED_ORIGINS=http://localhost:5173` ตามbackendoriginconfig Axiosส่งcredentials

### Database migrations

Flywayเป็นผู้จัดการschema JPAใช้ `ddl-auto: validate` และ `open-in-view: false`
Locations `db/migration/common` + `h2` หรือ `postgresql` ตามenvironment **ห้ามแก้migrationที่applyแล้ว**
V1baselineเปล่า V2tables V3packages/soups V4menu/order V5fulfillmentsnapshot V6sessions/grants V7ordersFK V8pricesnapshot V9payments V10auth V11stock V12paymentprivileges V13tableprivileges V14billrequest V15stocktarget/activeและprofile first/last/phone
ตรวจคำอธิบายไฟล์จริงจาก [migrations](code/backend/src/main/resources/db/migration) ไม่เดาจากinstalled_rank

เอกสาร migration V13–V15 และ read-only central schema evidence อยู่ใน [schema delta](doc/database/auth-stock-schema-delta.md) และ [canonical dictionary](doc/database/step2-schema-approved.md). PR #28 เพิ่ม forward migration V15 สำหรับ stock target/active และ profile fields พร้อม migration/integration tests
การ readback เป็น metadata/schema evidence ไม่ใช่การยืนยัน public app runtime หรือการอนุมัติย้อนหลัง; formal V15 approval, Flyway validate/checksum comparison และ release database verification ยังเป็น gates แยก ห้าม apply ซ้ำหรือ repair history เพื่อให้ผ่านเฉยๆ
Startup defaultFlywayenabledอาจapplypendingmigrations อย่าเริ่มimageใหม่ชี้ฐานกลางก่อนรับรอง pendingfiles ไม่repairhistoryให้ผ่านเฉยๆ

ProvisionManagerแรกบนระบบจริงใช้ `BOOTSTRAP_ADMIN_ENABLED`, `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_PASSWORD` และoptionaldisplay/email ตามตัวอย่าง
Runnerทำเฉพาะapp_usersว่าง BCryptผ่านAuthService **ปิดbootstrapหลังเตรียมบัญชีแล้ว** ไม่ใช้บัญชีdemoในproduction

## How to Run

### แบบlocal Compose (ฐานตาม .env)

จากroot:

```bash
docker compose up --build
```

Composeมีbackend/frontendเท่านั้น **ไม่มีPostgreSQLcontainer** Databaseใช้ที่backend.envระบุ ซึ่งปกติคือSupabaseกลาง
frontendยังเป็นVitedevserver ไม่ใช่productiondeployment เปิด frontend/loginที่ [localhost:5173/admin](http://localhost:5173/admin)
Health [localhost:8080/api/v1/system/health](http://localhost:8080/api/v1/system/health) และ [Swagger](http://localhost:8080/swagger-ui.html)
Stop `docker compose down` ดูlogsด้วย `docker compose logs backend` / `frontend`

### แยก backend / frontend

```bash
cd code/backend
./mvnw spring-boot:run
```

Windowsใช้ `mvnw.cmd` จากcode/backend และenv/providersข้างต้น Frontendอีกterminal:

```bash
cd code/frontend
npm ci
npm run dev
```

### DemoบนH2แยก ไม่ใช้ฐานกลาง

PowerShellจากcode/backend:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo" "-Dspring-boot.run.arguments=--app.menu.admin-access-provider=session --app.ordering.session-provider=database --app.fulfillment.access-provider=session"
```

Demoใช้H2memory+common/H2migrations+seed; restartแล้วข้อมูลหาย Managerตัวอย่าง `admin/admin123` เป็นข้อมูลdemo(`[TEST DATA]`)ที่ประกาศในDemoDataSeeder ห้ามใช้กับฐานกลาง ดูขั้นตอนรันซ้ำที่ [doc/setup-demo-data.md](doc/setup-demo-data.md)
สร้างบัญชีSERVICE_STAFF/KITCHEN_STAFFผ่านManagerก่อนทดลองครบทุกrole คำสั่งนี้overridefixturedefaultsของdemoprofileให้ใช้login/customergrantจริง; ถ้ารันdemoprofileเปล่า Menu/Ordering/Fulfillmentเป็นfixtures จึงไม่ใช่หลักฐานintegration

### Flowแต่ละrole

- MANAGER: `/admin` → Admin shell ข้อมูลร้าน/User/Stock เพิ่มmasterdataไม่ได้เพิ่มยอดStockทันที ใช้stock-in/adjustmentsเพื่อaudit
- SERVICE_STAFF: เปิดโต๊ะ →sessiondetails/QR →หน้าเสิร์ฟ →Billing/Payment→close
- KITCHEN_STAFF: incomingOrders→PREPARING→READY ส่วนSERVEเป็นหน้าที่SERVICE_STAFF
- SUPERVISOR: Stockoverview/history/stock-in/adjustment ไม่เปิดโต๊ะ/ชำระ/ครัว
- Customer: StaffQR `/customer/qr#token=...` →POSTexchange→cookie →สั่งก่อนbillrequest →ขอคิดบิล/ดูสถานะ
- เวลาManagerเพิ่มข้อมูลในCompose ข้อมูลเก็บในDBของ.env ไม่เก็บไว้เฉพาะbrowser DemoH2เป็นmemoryคนละฐาน

## API Documentation

- Local [Swagger UI](http://localhost:8080/swagger-ui.html) / [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [Shared Contracts](doc/contracts/shared-contracts.md), [Ordering contract](doc/contracts/ordering-contract.md), [API conventions](doc/contracts/api-conventions.md)
- [QR exchange sequence](doc/diagrams/sequence-qr-exchange.puml), [Billing/Payment integration](doc/diagrams/sequence-billing-payment.puml)
- Staffใช้login session Customerใช้HttpOnlygrant cookie QRtokenอยู่fragmentแล้วส่งในPOSTbody ไม่มีGET tokenในURL
- CustomerSessionResponseไม่มีQRtoken/ราคาsnapshot ErrorResponse/enumsใช้formatกลาง URLpublicSwaggerยังpending

## How to Run Tests

BackendH2 / unit / integration:

```bash
cd code/backend
./mvnw test
```

PostgreSQLmigration/security/concurrencyมีguardและต้องใช้ **DBทิ้งได้เท่านั้น** รันH2เฉยๆอาจskipPostgreSQL suites จึงห้ามอ้างว่า303ผ่านจากคำสั่งนี้เพียงอย่างเดียว
ดู [CI workflow](.github/workflows/ci.yml) ที่เตรียมmarked databases/user/roleด้วย `disposable-postgres-ci.sql` และ [PostgreSQL verification](doc/billing/pr19-review-verification.md)
กำหนดMENU_TEST_PG_URL/DINING_TEST_PG_URL/PAYMENT_TEST_PG_URLและALLOW_DESTRUCTIVE_DB_TESTSเฉพาะenvironmentทิ้งได้ที่มีmarker ไม่ชี้Supabaseกลาง

Frontend:

```bash
cd code/frontend
npm ci
npm test
node --test ../../test/browser/step3-runtime-config.test.cjs
npm run lint
npm run build
```

[CI ล่าสุดของ `develop adc5798`](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37738400052): Backend/PostgreSQL348/348, frontend121/121, URLguards6/6, lint0errors/4warningsเดิม, buildผ่าน จาก job logs
[PR#32 report](doc/testing/sirapat-step3-followup-2026-10-08.md) แยก develop regression, Stock/Profile candidate 8/8, controlled fixtures และ public gates ตาม revision; candidate/browser evidence ไม่ใช่ public acceptance
การแก้เอกสารครั้งนี้ใช้ CI ดังกล่าวเป็นหลักฐาน baseline ไม่อ้างว่ารัน backend/frontend ใหม่ รายละเอียดของ PR ที่เกี่ยวข้องอยู่ใน [PR #32 report](doc/testing/sirapat-step3-followup-2026-10-08.md) และ [Pavarit module docs report](doc/testing/pavarit-module-docs-report-2026-10-08.md)

## Deployment URL

**ยังไม่มีpublic URLที่รับรองในbaselineนี้** แผนธีรเมธคือRenderFreeWebService URLเดียว HTTPS SpringBootเสิร์ฟReactproductionและAPI `/api/v1`
ต้องรองรับSPArefreshโดยไม่เปลี่ยนAPI404เป็นเว็บ Securecustomer/staffcookies allowedOriginจริง portจากRenderและprovidersจริง
SupabaseSessionPooler+SSL ข้อมูลคงอยู่หลังredeploy StaffHTTPsessionในmemoryต้องloginใหม่หลังrestart CustomergrantpersistในDBแต่อายุ/สถานะยังต้องตรวจ
PublicCoreFlow/TTL/coldstart/redeploy/rollback/deployedSHAต้องผ่านก่อนใส่URLจริง ไม่มีการdeployจากPRเอกสารนี้

## Project Structure

```text
code/
  backend/     Spring Boot Controllers, Services, Repositories, Domain, DTOs, tests, Flyway
  frontend/    React views, shared UI, services, tests, Vite config
test/
  fixtures/    controlled shared contract samples
  browser/     real HTTP browser scripts and runtime URL guards
  evidence/    reports, sanitized inventories and screenshots
  reports/     ignored temporary outputs
doc/
  contracts/ database/ diagrams/ system-design/ testing/ planning/
  solid-analysis.md
  design-patterns.md
  slide/       Canva content, presenter notes, version guides and approved Canva PPTX/PDF exports
img/           project media
.github/       CI configuration
docker-compose.yml
```

## Git workflow และชุดนำเสนอ

Personalbranch →reviewedPR→develop →reviewedreleasePR→main ทุกคนใช้บัญชีตน Commitเป็นงานที่มีความหมาย ไม่เติมจำนวน
[Git audit ล่าสุด](doc/planning/step3-git-audit.md) แสดง develop snapshot หลัง PR #32, author candidates, PR/reviewer/merge history และสิ่งที่สมาชิกยังต้องยืนยัน
[สไลด์ทีมปัจจุบัน](doc/slide/README.md) / [Diagram coverage](doc/slide/course-diagram-coverage.md) / [runbook12นาที](doc/slide/team-final-12-minute-runbook.md)
ร่างPPTX/PDFรายคนเดิมเป็นประวัติ รุ่นส่งใช้Canvaพร้อมไฟล์PPTX/PDFที่exportจากCanvaและownerรับรองตามreleaseจริง
