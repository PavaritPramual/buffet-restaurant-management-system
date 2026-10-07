# Buffet Restaurant Management System

ระบบจัดการร้านบุฟเฟต์แบบ Walk-in ครอบคลุมเปิดโต๊ะ เลือกแพ็กเกจ/น้ำซุป และให้ลูกค้าสแกน QR เพื่อสั่งอาหาร
ครัวรับและเตรียมออเดอร์ พนักงานเสิร์ฟ รับชำระ แล้วกดปิดรอบแยกเพื่อคืนโต๊ะ
Manager จัดการข้อมูลร้าน ผู้ใช้ และสต็อก โดย backend ตรวจสิทธิ์และคำนวณยอดจากข้อมูลจริง
พัฒนาด้วย Spring Boot, React และ PostgreSQL สำหรับวิชา CP353002 Principles of Software Design and Development

## สถานะปัจจุบัน

**แนวทางสไลด์ล่าสุด:** [Canva ทีม v02](https://www.canva.com/d/wORhypcIcwvGuFK) ใช้20หน้าหลักสำหรับ12นาที พร้อมภาคผนวกdiagram/codeครบตามโจทย์ ดู [guide/version](doc/slide/README.md), [coverage](doc/slide/course-diagram-coverage.md) และ [notes](doc/slide/team-final-canva-v02-content.md) อ่านกลับ76หน้าผ่านแล้ว ยังรอowner/CanvavisualQA/PDFexport/ซ้อม/ผลrelease ไม่ใช้PPTXเป็นชุดส่งหรือขอreview

Code baseline `develop 472fba4` ณ 7 ตุลาคม2026: Step2ปิดแล้วและPR#22รวมlocalregression/AdminShell/testpreparation
เอกสารและสไลด์ปวริศช์ชุดนี้รอreview **ยังไม่ใช่ Final ที่พร้อมส่ง** Public deployment, Stock target/active, Profileละเอียด, SOLID gaps, Gitเกณฑ์รายคน และreleaseยังต้องปิด
ดู [Requirement Matrix](doc/planning/step3-requirement-matrix.md), [Step3 plan](doc/planning/step3-final-plan.md) และ [Notionสด](https://app.notion.com/p/3f1cb2e9d47a81e28aa2dc642cd6ead6)

**เกณฑ์Gitที่อาจารย์ปรับ:** ขั้นต่ำ5meaningfulcommitsต่อคนตามการแจ้งวันที่7ตุลาคม2026 ดู [criteria update](doc/planning/course-criteria-updates.md) และ [Git audit](doc/planning/step3-git-audit.md)

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

[ERD source](doc/diagrams/er-diagram.puml) · [canonical Data Dictionary V1–V14](doc/database/step2-schema-approved.md) · [Diagram index](doc/diagrams/README.md)
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
V1baselineเปล่า V2tables V3packages/soups V4menu/order V5fulfillmentsnapshot V6sessions/grants V7ordersFK V8pricesnapshot V9payments V10auth V11stock V12paymentprivileges V13tableprivileges V14billrequest
ตรวจคำอธิบายไฟล์จริงจาก [migrations](code/backend/src/main/resources/db/migration) ไม่เดาจากinstalled_rank

SupabaseกลางV1–V14ได้รับรองและapplyตาม [Step2 completion report](doc/testing/pavarit-step2-completion-report.md) กับหน้าStep2 วันที่6ต.ค. V9เคยลงหลังV10/V11แต่validateปกติผ่านโดยไม่เปิดoutOfOrderค้างไว้
นี่เป็นผลประวัติ ไม่ได้ตรวจฐานกลางสดจากPRdocsนี้ FinalStock/Profileต้องforwardmigrationเลขว่างจริง reviewและขออนุมัติฐานกลางใหม่
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
frontendยังเป็นVitedevserver ไม่ใช่productiondeployment เปิด frontend/loginที่ [localhost:5173/login](http://localhost:5173/login)
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

Demoใช้H2memory+common/H2migrations+seed; restartแล้วข้อมูลหาย Managerตัวอย่าง `admin/admin123` เป็นข้อมูลdemoที่ประกาศในDemoDataSeeder
สร้างบัญชีSERVICE_STAFF/KITCHEN_STAFFผ่านManagerก่อนทดลองครบทุกrole คำสั่งนี้overridefixturedefaultsของdemoprofileให้ใช้login/customergrantจริง; ถ้ารันdemoprofileเปล่า Menu/Ordering/Fulfillmentเป็นfixtures จึงไม่ใช่หลักฐานintegration

### Flowแต่ละrole

- MANAGER: `/login` → Admin shell ข้อมูลร้าน/User/Stock เพิ่มmasterdataไม่ได้เพิ่มยอดStockทันที ใช้stock-in/adjustmentsเพื่อaudit
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

[CIของdevelop472fba4](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105): backend+PostgreSQL303/303 frontend116/116 URLguards6/6 lint0errors/4existingwarnings buildผ่าน
[PR#22 report](doc/testing/sirapat-step3-premerge-report.md) แยกlocalH2 realHTTP/cookies14PASS ก่อนmerge/hashตรงhead617d742 ออกจากhistoricalUIfixtures และpublicที่ยังไม่ตรวจ
งานdocsนี้ใช้CIbaseline ไม่อ้างว่ารันbackend/frontendใหม่ ดู [docs verification](doc/testing/pavarit-step3-docs-report.md)

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
  slide/       Canva content, presenter notes, version guides and approved PDF exports
img/           project media
.github/       CI configuration
docker-compose.yml
```

## Git workflow และชุดนำเสนอ

Personalbranch →reviewedPR→develop →reviewedreleasePR→main ทุกคนใช้บัญชีตน Commitเป็นงานที่มีความหมาย ไม่เติมจำนวน
[Git auditล่าสุด](doc/planning/step3-git-audit.md) แสดงทั้งห้าบัญชี branches/PRreviews/candidatecommitcounts และข้อขาดจริง
[สไลด์ทีมปัจจุบัน](doc/slide/README.md) / [Diagram coverage](doc/slide/course-diagram-coverage.md) / [runbook12นาที](doc/slide/team-final-12-minute-runbook.md)
ร่างPPTX/PDFรายคนเดิมเป็นประวัติ รุ่นส่งใช้Canva/PDFที่ownerรับรองตามreleaseจริง
