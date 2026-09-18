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

Spring Data JPA, Flyway และ datasource configuration จะเพิ่มผ่าน PR ของ Database Owner

## Shared Contracts

- Canonical contract: `doc/contracts/shared-contracts.md`
- Canonical fixtures: `test/fixtures/`
- API enums ใช้ uppercase JSON string เท่านั้น
- Entity ต้องใช้ `@Enumerated(EnumType.STRING)` ห้ามใช้ `EnumType.ORDINAL`

## Current Foundation Boundary

Foundation นี้ยังไม่รวม RestaurantTable CRUD, BuffetPackage, DiningSession, QR, Close Session หรือ Payment integration
