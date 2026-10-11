# Setup และ Demo Data (รันซ้ำได้)

## หลักการ
- ข้อมูลทดสอบทั้งหมดมีป้าย **`[TEST DATA]`** ในชื่อ/เหตุผล เพื่อแยกจากข้อมูลจริงของทีม
- ขั้นตอนไม่มีคำสั่งลบ/truncate/reset และไม่แตะข้อมูลที่ไม่มีป้าย; รันซ้ำแล้วไม่ซ้ำ (seed เช็คว่ามีอยู่แล้วก่อนสร้าง)
- Tests ใช้ DB แยก (H2 in-memory, PostgreSQL Testcontainers หรือ PostgreSQL ชั่วคราวที่มี disposable guard) ห้ามชี้ tests ไป Supabase กลาง
- บัญชี demo มีรหัสเผยแพร่ใน README ใช้เฉพาะ H2 ในเครื่อง ไม่ใช้กับฐานกลาง/production

## 1) Demo บน H2 (ไม่แตะฐานกลาง)
```powershell
cd code\backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo" "-Dspring-boot.run.arguments=--spring.config.import= --app.menu.admin-access-provider=session --app.ordering.session-provider=database --app.fulfillment.access-provider=session"
```
- profile `demo` ใช้ H2 memory + `app.seed.enabled=true`; `DemoDataSeeder` สร้างบัญชี `Manager/Manager123` ผ่าน `UserAdministrationService` และสต็อกตัวอย่างพร้อมเป้าหมาย (`[TEST DATA]`) เฉพาะรายการที่ยังไม่มี
- restart แล้วข้อมูลหาย จึงรันซ้ำได้โดยไม่มีผลข้างเคียง

หลัง backend พร้อม เปิด PowerShell อีกหน้าจาก repository root แล้วรัน `.\scripts\setup-demo-users.ps1` สคริปต์ใช้ Manager login และ API จัดการผู้ใช้เดิมสร้างบัญชี `Supervisor/Supervisor123`, `ServiceStaff/ServiceStaff123`, `KitchenStaff/KitchenStaff123` ดูตารางครบใน README หากใช้ port อื่นส่ง `-BaseUrl http://localhost:8081/api/v1` ได้ สคริปต์อนุญาตเฉพาะ localhost และตรวจบัญชีที่มีอยู่แทนการแก้หรือทับข้อมูล

สร้างด้วย UI ได้เช่นกัน: login Manager → พนักงาน → กรอก username/password ตามตาราง, ชื่อแสดง `[TEST DATA]` ตาม role, ชื่อ `Demo`, นามสกุลตาม role แล้วเลือก role ที่ตรงกัน การสร้างต้องเป็น Manager; ไม่เพิ่ม role enum ใหม่และรหัสเก็บด้วย BCrypt ตามเดิม

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
cd code\backend; mvn -o test          # PostgreSQL บางชุดต้อง Docker หรือ environment opt-in
cd code\frontend; npm test
```
PostgreSQL แบบ Testcontainers ต้องมี Docker daemon และ role จาก `postgres-test-roles.sql` (ดู [testing docs](testing/test-plan.md)) ส่วนชุดที่รับ environment opt-in ต้องใช้ฐานชั่วคราวที่ผ่าน `DisposablePostgresDatabase` guard เท่านั้น ดูตัวอย่าง Dashboard และตัวแปรที่ต้องตั้งใน [management-dashboard](architecture/management-dashboard.md) โดย CI เตรียมฐานแยกให้แต่ละชุดจาก `disposable-postgres-ci.sql`
