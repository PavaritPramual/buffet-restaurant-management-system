# Buffet Restaurant Management System

ระบบจัดการร้านอาหารบุฟเฟต์สำหรับการเปิดโต๊ะ สั่งอาหาร ติดตามงานครัว ชำระเงิน และจัดการสต็อก พัฒนาด้วย Spring Boot, React และ Supabase PostgreSQL ตาม Layered Architecture

## สมาชิกและ Branch

| สมาชิก | รหัสนักศึกษา | Section | Branch | Feature Owner |
|---|---:|---:|---|---|
| ปวริศช์ ประมวล | 673380278-9 | 01 | `pavarit_673380278-9_01` | Table, Dining Session, Buffet Package/Soup |
| ศิระพัทธ์ วงศ์วิวัฒน์เสรี | 673380293-3 | 01 | `sirapat_673380293-3_01` | Menu Catalog, Customer Ordering |
| ศรัณย์ พาพรชัย | 673380515-1 | 02 | `sarun_673380515-1_02` | Kitchen, Serving, Order State |
| ธีรเมธ สายคำ | 673380273-9 | 02 | `teeramet_673380273-9_02` | Billing, Payment, Deployment |
| เมธัส มณีวิจิตร | 673380300-2 | 01 | `methus_673380300-2_01` | Authentication, Stock, Admin Shell |

## Repository Structure

```text
code/   source code and configuration
test/   shared fixtures, evidence, and test reports
doc/    contracts, diagrams, documentation, and slides
img/    project media
```

## Git Workflow

```text
personal branch -> Pull Request -> develop -> release Pull Request -> main
```

- สมาชิกแต่ละคนต้องสร้างและ push personal branch ด้วยบัญชี GitHub ของตนเอง
- ห้าม push feature ตรงเข้า `main` หรือ `develop`
- ทุก Pull Request ต้องมี reviewer อย่างน้อยหนึ่งคน
- Commit message ใช้รูปแบบ `<type>: <description>`

## Tech Stack

- Java 17, Spring Boot 3.x, Maven
- Supabase PostgreSQL, Spring Data JPA, Flyway
- React, Vite, TypeScript, Tailwind CSS
- Axios, React Router
- JUnit 5, Mockito, Spring Boot Test

## Run Backend

```bash
cd code/backend
./mvnw test
./mvnw spring-boot:run
```

บน Windows ใช้ `mvnw.cmd` แทน `./mvnw` ได้ จากนั้นเปิด:

- Health API: `http://localhost:8080/api/v1/system/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Run Frontend

```bash
cd code/frontend
cp .env.example .env
npm ci
npm run dev
```

Frontend เปิดที่ `http://localhost:5173` และใช้ `VITE_API_BASE_URL` เป็น API endpoint

## Supabase Convention

ใช้ Session pooler พอร์ต `5432` และบังคับ SSL ห้าม commit ไฟล์ `.env` หรือข้อมูลลับลง repository ตัวอย่างชื่อ environment variables อยู่ใน `code/backend/.env.example`

```dotenv
SUPABASE_DB_HOST=your-session-pooler-host
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USERNAME=postgres.your-project-ref
SUPABASE_DB_PASSWORD=change-me
```

Backend โหลดค่าฐานข้อมูลจาก `code/backend/.env` ผ่าน Spring config import แบบ optional
และบังคับ SSL ด้วย `sslmode=require` จึงไม่ต้องใส่ secret ใน `application.yml`

### Database migration convention

- ไฟล์ migration อยู่ที่ `code/backend/src/main/resources/db/migration/`
- ใช้ชื่อ `V<ลำดับ>__<คำอธิบายสั้นแบบ snake_case>.sql` เช่น `V1__baseline.sql`
- migration ที่ apply แล้วห้ามแก้ไข ให้เพิ่ม version ใหม่แทน
- ใช้ schema `public` และให้ Flyway เป็นผู้จัดการ schema; JPA ใช้ `ddl-auto: validate`
- `V1__baseline.sql` เป็น migration เปล่าสำหรับยืนยันการทำงานของ Flyway เท่านั้น
	ไม่สร้างตารางของ Table, Order, Billing หรือโมดูลอื่น

หลังใส่ค่าจริงใน `.env` ให้รันจาก `code/backend`:

```bash
mvnw.cmd spring-boot:run
```

จากนั้นตรวจใน Supabase SQL Editor:

```sql
select installed_rank, version, description, success
from public.flyway_schema_history
order by installed_rank;
```

ควรพบแถว `1 | baseline | true` ซึ่งยืนยันว่าเกิดตาราง `flyway_schema_history`
โดยไม่ต้องมีตาราง business ใด ๆ ใน migration นี้

### Authentication and stock demo

สำหรับลองหน้า Admin โดยไม่ใช้ Supabase ให้รัน backend ด้วย H2 demo profile จาก `code/backend`:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo 
```

Profile นี้ใช้ Flyway migrations ชุด common/H2 และ seed ข้อมูลซ้ำได้เมื่อเริ่มระบบใหม่
(account และ stock item ที่มีแล้วจะไม่ถูกสร้างซ้ำ; initial stock ถูกบันทึกเป็น transaction)
บัญชี demo คือ `admin` / `admin123` ใช้กับ demo เท่านั้น ห้ามนำรหัสนี้ไปใช้กับระบบจริง
จากนั้นเปิด frontend และเข้า `http://localhost:5173/admin/stock` หรือ `/admin/users`.

API login ใช้ HTTP session cookie: `POST /api/v1/auth/login`, `GET /api/v1/auth/me`
และ `POST /api/v1/auth/logout` ส่วน stock ใช้ `GET /api/v1/stock`,
`POST /api/v1/stock/{id}/in`, `POST /api/v1/stock/{id}/adjustments` และ
`GET /api/v1/stock/transactions`. ทุก movement บันทึก actor, reason, delta และ balance หลังทำรายการ
ใน `stock_transactions`.

Migration review: `V10__create_user_and_stock_tables.sql` เป็น schema กลางที่ทดสอบบน H2;
`V11__restrict_flyway_and_stock_access.sql` ใช้เฉพาะ PostgreSQL เปิด RLS ให้ backend datasource role
และ revoke privileges ของ `PUBLIC`, `anon`, `authenticated` จาก `flyway_schema_history`
และตาราง auth/stock. V6–V8 มีอยู่ใน migration history ปัจจุบัน ส่วน Payment V9 ยังไม่มีใน checkout นี้
และห้ามระบุว่าอยู่ใน develop แล้ว. เนื่องจาก Flyway ใช้ `outOfOrder=false` ต้องตกลงกับ Payment owner
ให้นำ V9 ขึ้นก่อน V10/V11 หรือกำหนดเลขใหม่ก่อน apply migration ใด ๆ ลงฐานกลาง; หากใช้ V10/V11 ก่อน
V9 ที่เพิ่มภายหลังจะไม่ถูกรันตามลำดับปกติ. H2 integration run ยืนยัน migration V1–V8 และ V10;
V11 และ concurrent stock มี PostgreSQL Testcontainers tests ซึ่งต้องใช้ Docker. ก่อนใช้ shared Supabase
ให้ปวริศช์ review V11 และ schema delta ใน [auth-stock schema delta](doc/database/auth-stock-schema-delta.md)
รวมถึงอนุมัติการเลื่อน `opening_target_stock` และ active/inactive lifecycle หรือกำหนดงาน follow-up
ให้ตรงกับแบบ; จนกว่าจะตกลงกัน ห้ามถือว่า Stock schema ครบตาม design baseline.

Role flow: ทุก stock read จำกัดเฉพาะ `MANAGER` และ `SUPERVISOR`; roles นี้ทำ stock-in/adjustment ได้ด้วย;
`MANAGER` เท่านั้นที่อ่าน/สร้าง users และแก้ menu catalog. Login เปลี่ยน session ID หลังยืนยันตัวตน.
หน้า Staff Tables, Dining Session, Kitchen และ Serving ใช้ authenticated shell ร่วมกันเพื่อตรวจ `/auth/me`,
แสดง logout และกลับหน้า login เมื่อ session หมดอายุ; การอนุญาตของ DiningSession/Ordering/Fulfillment API
ยังใช้ provider ของเจ้าของ feature และยังไม่ได้เชื่อมกับ Auth session ใน PR นี้. QR flow ไม่เปลี่ยน.

สำหรับฐาน production ที่ยังไม่มี account ให้ provision manager แรกโดยตั้ง
`BOOTSTRAP_ADMIN_ENABLED=true`, `BOOTSTRAP_ADMIN_USERNAME` และ `BOOTSTRAP_ADMIN_PASSWORD`
(อย่างน้อย 8 ตัวอักษร) พร้อม `BOOTSTRAP_ADMIN_DISPLAY_NAME`/`BOOTSTRAP_ADMIN_EMAIL` ตามต้องการ
runner จะทำงานเฉพาะเมื่อ `app_users` ยังว่าง ใช้ BCrypt ผ่าน `AuthService` และไม่สร้างซ้ำ
เมื่อมีผู้ใช้แล้ว ปิด `BOOTSTRAP_ADMIN_ENABLED` หลัง bootstrap.

## System Design

เอกสารแบบออกแบบที่ย้ายจาก Notion อยู่ที่ [doc/system-design/README.md](doc/system-design/README.md) พร้อม [PlantUML ที่แก้ไขได้](doc/diagrams/README.md) และ [Data Dictionary](doc/database/data-dictionary-design.md) โปรดดูสถานะเทียบกับโค้ดในหน้า System Design ก่อนใช้เป็นหลักฐาน implementation

## Shared Contracts

- Canonical contract: `doc/contracts/shared-contracts.md`
- Canonical fixtures: `test/fixtures/`
- API enums ใช้ uppercase JSON string เท่านั้น
- Entity ต้องใช้ `@Enumerated(EnumType.STRING)` ห้ามใช้ `EnumType.ORDINAL`

## Current Foundation Boundary

Foundation นี้ยังไม่รวม RestaurantTable CRUD, BuffetPackage, DiningSession, QR, Close Session หรือ Payment integration

## Run with Docker Compose

ต้องติดตั้ง Docker และ Docker Compose และเปิด Docker daemon ไว้

รันจาก root repository:

```bash
docker compose up --build
```

เปิดบริการ:

- Frontend: http://localhost:5173
- Backend health: http://localhost:8080/api/v1/system/health
- Swagger UI: http://localhost:8080/swagger-ui.html

Health endpoint ควรตอบ:

```json
{"status":"UP","service":"buffet-restaurant-backend"}
```

Compose มีเฉพาะ backend และ frontend ไม่มี PostgreSQL container
Frontend ใช้ Vite development server

### Environment

- `SERVER_PORT`: พอร์ต backend ภายใน container กำหนดเป็น `8080`
- `CORS_ALLOWED_ORIGINS`: origin ที่ backend อนุญาต เปลี่ยนได้ผ่าน `code/backend/.env` หากไม่กำหนดจะใช้ `http://localhost:5173` เป็นค่าเริ่มต้นจาก Java
- `VITE_API_BASE_URL`: URL ที่ browser ใช้เรียก API กำหนดเป็น `http://localhost:8080/api/v1`

หลังเปลี่ยนค่าใน `code/backend/.env` ให้รัน `docker compose up -d --force-recreate backend` เพื่อให้ backend รับค่าใหม่

Compose อ่าน `code/backend/.env` ถ้ามี และส่งค่าเข้า backend ตอนรัน
ใช้ `.env.example` เป็นตัวอย่าง ห้าม commit `.env` หรือใส่ secrets ใน Dockerfile
ห้ามใส่ secrets ในตัวแปร `VITE_*` เพราะเป็นค่าฝั่ง frontend

Backend ปกติใช้ Supabase datasource จาก `.env`; `demo` profile ใช้ H2 ในเครื่องและไม่ต้องตั้งค่า Supabase
สำหรับ automated tests ใช้ H2 จาก `src/test/resources/application.yml`.

### Verify CORS

เปิด http://localhost:5173 แล้วเปิด Developer Tools (F12) > Console และรัน:

หากไม่สามารถ copy & paste ในช่อง console ให้ใช้คำสั่งนี้:

```
allow pasting
```

เพื่อให้ browser อนุญาตให้วางใน console ได้

```javascript
await fetch('http://localhost:8080/api/v1/system/health', {
  headers: { 'Content-Type': 'application/json' }
}).then(response => response.json())
```

ควรได้ `status: "UP"` โดยไม่มี CORS error

### Stop

กด Ctrl+C ใน terminal ที่รัน Compose แล้วลบ containers/network ด้วย:

```bash
docker compose down
```

### Troubleshooting

- ติดต่อ Docker ไม่ได้: ตรวจว่า Docker daemon ทำงานและผู้ใช้มีสิทธิ์เข้าถึง
- Port already allocated: ตรวจว่าพอร์ต 8080 หรือ 5173 ถูกโปรแกรมอื่นใช้อยู่หรือไม่
- CORS error: เปิด frontend ด้วย `http://localhost:5173` ให้ตรงกับ origin ที่อนุญาต
- Connection refused: รอ backend เริ่มทำงานเสร็จ เพราะ `depends_on` ไม่ได้รอให้ backend พร้อมรับคำขอ
- Build ดาวน์โหลด dependency ไม่สำเร็จ: ตรวจการเชื่อมต่ออินเทอร์เน็ตและข้อความ error แล้วลอง build ใหม่
- แก้ source หรือ configuration แล้ว: รัน `docker compose up --build` ใหม่

ดู log แยกบริการได้ด้วย:

```bash
docker compose logs backend
docker compose logs frontend
```

## Billing / Payment runtime setup

After Auth/session/schema are ready, copy the non-secret provider settings from
`code/backend/.env.example` into your local deployment environment:

```env
DINING_SESSION_STAFF_ACCESS_PROVIDER=session
BILLING_CONTEXT_PROVIDER=database
PAYMENT_STATUS_PROVIDER=database
```

Defaults remain disabled (fail closed). JDBC/Hibernate and Flyway use the same datasource
and DB role configured by SUPABASE_DB_USERNAME. This is a database login, not a Supabase
REST service_role key. If deployment separates migration and runtime roles, owners must
explicitly grant the actual runtime role and test it; do not add auth.uid() policies to
our Spring session authorization flow.

V9 creates payments. V12 is a **proposed forward migration** aligning policy/grants with
the migration/JDBC role and resolving the identity sequence through pg_get_serial_sequence.
Do not change applied V9. Methus must confirm V12 is available and inspect central
flyway_schema_history before merge/apply. If V10/V11 already ran without V9, V12 cannot
create the missing payments table by itself: coordinate a versioned forward creation plan.
Do not enable out-of-order migrations or run shared DB migrations to bypass that gate.

For an isolated H2 browser smoke run use `doc/billing/billing-demo-checklist.md`.
For PostgreSQL payment/duplicate/security verification use the guarded disposable DB setup
in `doc/billing/pr19-review-verification.md`. Never use central Supabase for automated tests.

Public BillSummary now contains sessionId/subtotalAmount/discountAmount/totalAmount only;
precise rounding intermediates are internal. PaymentResult adds the recorded amount.
These contract and strategy-composition changes require Sarun/Pavarit/Sirapat review.
