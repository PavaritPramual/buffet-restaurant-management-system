# Setup และ Demo Data (รันซ้ำได้)

## หลักการ
- ข้อมูลทดสอบทั้งหมดมีป้าย **`[TEST DATA]`** ในชื่อ/เหตุผล เพื่อแยกจากข้อมูลจริงของทีม
- ขั้นตอนไม่มีคำสั่งลบ/truncate/reset และไม่แตะข้อมูลที่ไม่มีป้าย; รันซ้ำแล้วไม่ซ้ำ (seed เช็คว่ามีอยู่แล้วก่อนสร้าง)
- Tests ใช้ DB แยก (H2 in-memory หรือ PostgreSQL Testcontainers) ห้ามชี้ tests ไป Supabase กลาง
- ห้ามใช้บัญชี demo (`admin/admin123`) กับฐานกลาง/production

## 1) Demo บน H2 (ไม่แตะฐานกลาง)
```powershell
cd code\backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```
- profile `demo` ใช้ H2 memory + `app.seed.enabled=true`; `DemoDataSeeder` สร้างบัญชี Manager ตัวอย่างและสต็อกตัวอย่างพร้อมเป้าหมาย (`[TEST DATA]`) เฉพาะรายการที่ยังไม่มี
- restart แล้วข้อมูลหาย จึงรันซ้ำได้โดยไม่มีผลข้างเคียง

## 2) Provision Manager คนแรกบนฐานจริง (bootstrap admin)
1. ตั้ง env ชั่วคราว: `BOOTSTRAP_ADMIN_ENABLED=true`, `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_PASSWORD` (ตั้งเอง ไม่ commit), optional `BOOTSTRAP_ADMIN_DISPLAY_NAME`, `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_FIRST_NAME`, `BOOTSTRAP_ADMIN_LAST_NAME`
2. Runner สร้างบัญชีเฉพาะเมื่อ `app_users` ว่าง จึงไม่ทับบัญชีทีม
3. ล็อกอินครั้งแรกแล้ว **ตั้ง `BOOTSTRAP_ADMIN_ENABLED=false` (หรือลบ env) และ restart ทันที** ห้ามปล่อยเปิดค้าง
4. สร้างบัญชีอื่นผ่านหน้า Users (ต้องกรอกชื่อ/นามสกุล)

## 3) ข้อมูลเดิมหลัง V15
- แถว `user_profiles` เดิมมี `first_name/last_name/phone_number = NULL` ระบบไม่เดาแยกชื่อ; UI แสดง display name เดิมและให้ Manager กด "เติมข้อมูล"
- `stock_items` เดิมได้ `opening_target_stock = 0`, `active = TRUE` อัตโนมัติ

## 4) การรัน tests
```powershell
cd code\backend; mvn -o test          # H2 ทั้งหมด; PostgreSQL tests ข้ามหากไม่มี Docker
cd code\frontend; npm test
```
PostgreSQL tests (`Postgres*Test`) ต้องมี Docker daemon และ role จาก `postgres-test-roles.sql` (ดู [testing docs](testing/test-plan.md))
