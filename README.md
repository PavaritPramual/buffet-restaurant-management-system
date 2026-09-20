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

Backend foundation ปัจจุบันยังไม่มี datasource configuration
เมื่อรวม persistence baseline แล้ว ต้องกำหนดค่า Supabase ตามที่ baseline ต้องการ

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
