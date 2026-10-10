# รายงาน integration ปิด Step 2 — ปวริศช์

> รายงานนี้เป็นหลักฐานรอบ PR #20 ที่ปิดโดยไม่ merge งาน Manager/Customer Billing ที่เคยเลื่อนไป Final ในรอบนี้ถูกย้ายกลับ Step 2 และทำแล้ว ดู [รายงานล่าสุด](pavarit-step2-completion-report.md) สำหรับ V14, 296 backend tests, 108 frontend tests และ 14 browser scenarios รวมใหม่

ตรวจวันที่ 6 ตุลาคม 2026 (Asia/Bangkok) จาก develop `888b4ea` บน branch `pavarit_673380278-9_01` โค้ดที่ทดสอบตรงกับ commits `15024d4` (Auth/routes/config) และ `83b9bdf` (integration/security/tests) เอกสารและภาพแนบใน commit ถัดมา

## ผลตรวจ

| Gate | ผล | Environment / ข้อจำกัด |
| --- | --- | --- |
| Backend `mvnw.cmd verify` | 289/289 ผ่าน ไม่มี failure/error/skipped | H2 และ PostgreSQL 18 ทิ้งได้ รวม migration, concurrency, Auth และ client-role security |
| Frontend `npm test` | 103/103 ใน 13 files ผ่าน | Vitest; fixtures ระบุใน tests |
| Frontend lint/build | ผ่าน | lint 0 errors, 4 warnings เดิม set-state-in-effect; production build ผ่าน |
| Docker build | backend/frontend ผ่าน | Java 17 runtime, ไม่มี fixtures authorization ใน integration override |
| Core Flow browser | 11 scenarios ผ่าน | Chrome, Docker Compose → Spring Boot → PostgreSQL 18 แยก; contexts Customer/Kitchen/Staff/Manager/Supervisor แยกจริง ไม่มี mock response ใน flow |
| Visual loading/empty/error | 3 role groups × 3 states ผ่าน | HTTP response fixtures สำหรับภาพเท่านั้น ใช้ shell/login จริง; ไม่อ้างว่าเป็น backend outage จริง |
| Supabase V1–V12 readiness ก่อน V13 | checksum validate และ startup/JPA validate ผ่าน | actual JDBC postgres ผ่าน Session Pooler 5432 SSL, outOfOrder=false, pending=0; ไม่รัน migration ใหม่หรือ repair |
| Supabase หลังเพิ่ม V13 ใน branch | ยังไม่ผ่าน deployment gate | V13 pending=1 ตั้งใจรอ review; restaurant_tables ยังมี client grants บนฐานกลาง ห้าม deploy image นี้ก่อนรับรอง migration |

H2 ใช้ 9 migrations common/H2 (V1–V4,V6–V10); V5/V11/V12/V13 เป็น PostgreSQL-only PostgreSQL ทดสอบทั้งลำดับว่าง/upgrade จาก V2 → V13 และ upgrade V12 → V13 ที่จำลอง grants ของ client/PUBLICไว้ก่อน ไม่มี automated test เชื่อม Supabase

## สิ่งที่แก้

- Fulfillment ใช้ `SessionOrderFulfillmentAccessProvider` → `SessionUserContextProvider` จาก login cookie KITCHEN_STAFF ทำ RECEIVED → PREPARING → READY; SERVICE_STAFF ทำ READY → SERVED; MANAGER/SUPERVISOR ไม่ได้รับสิทธิ์สอง flow นี้
- Frontend เลิกส่ง X-User-Role; header ปลอมเพิ่มสิทธิ์ไม่ได้ ไม่มี login = 401, role ผิด = 403 มี tests ครบสี่ role กับ anonymous/logout
- Route guard ใช้ React Router matcher รวม case และ trailing slash; Staff navigation เชื่อมโต๊ะ/รอบกินกับงานเสิร์ฟและกลับจากบิลได้
- Table/Package/Soup APIs ใช้ session read guard สำหรับ SERVICE_STAFF/MANAGER/SUPERVISOR และ Manager-only mutations Customer อ่าน package ผ่าน `/dining-sessions/{sessionId}/package` โดยใช้ customer cookie ที่ scope ถูกต้องและต้อง ACTIVE/match session
- API package ของลูกค้าคืน catalog DTO เดิม ไม่คืน QR token หรือ package_price_at_open; UI ใช้ชื่อแพ็กเกจ Billing ยังคำนวณจาก snapshot ฝั่ง backend สัญญา Order/Payment ไม่เปลี่ยน
- Stock adjustment ใช้ ConfirmDialog กลางและ in-flight guard กัน submit ซ้ำ ส่วน stock-in/history และ create-user/login ผ่าน UI จริง
- V13 forward migration เปิด RLS ของ restaurant_tables และ revoke client/PUBLIC ของ application tables/sequences โดยไม่แก้ V1–V12 ไม่เพิ่ม Payment migration ซ้ำ

## Core Flow ที่ตรวจจริง

1. Manager เพิ่ม SERVICE_STAFF/KITCHEN_STAFF/SUPERVISOR ผ่าน User UI และ login แต่ละ context ไป landing ตาม role
2. เตรียม package/soup/table/menu ผ่าน domain APIs ด้วย Manager login เฉพาะ disposable data ไม่แก้ DB ด้วยมือใน flow
3. Staff เปิดรอบ 2 ผู้ใหญ่ 1 เด็ก เลือก package/soup จริง ลอง close ก่อนจ่ายต้องถูกปฏิเสธและรอบยัง ACTIVE
4. Customer เปิด QR จาก Staff UI, fragment ถูกล้าง, แลก token ได้ครั้งเดียวและส่ง order ผ่าน cookie เพียงหนึ่ง POST
5. Kitchen กด PREPARING → READY แล้ว Staff เข้างานเสิร์ฟกด SERVED อ่านสถานะจาก DB/API จริง
6. Staff ชำระ CASH ยอด 997.50 ที่ backend คำนวณ รอบยัง ACTIVE; reload เห็น paymentId เดิม แล้วกด close แยก โต๊ะ AVAILABLE และ credential ลูกค้าใช้ต่อไม่ได้
7. Supervisor รับ stock +2 และปรับ -1 พร้อมเหตุผล/confirmation ตรวจ balance และ audit history ผ่าน UI/API; Kitchen logout แล้ว API 401

QR expired/mismatched/closed credential, duplicate payment, package snapshot, Order-vs-close PostgreSQL locking และ concurrent stock ถูกตรวจโดย automated integration tests เดิมและใหม่ ไม่ใช้ภาพ browser happy path แทน concurrency/security tests

## ฐานกลางที่ตรวจจาก JDBC ของแอป

V1–V12 success ทั้งหมด installed_rank 9 คือ V10, rank 10 คือ V11, rank 11 คือ V9 และ rank 12 คือ V12 ไม่ได้ยืนยันวิธีหรือผู้ที่รัน V9 ภายหลัง V10/V11

current_user/session_user = postgres และ BYPASSRLS = true ดังนั้น RLS ไม่จำกัด backend role นี้ API เป็นจุดตรวจสิทธิ์ Payment table/sequence ใช้งานผ่าน backend ได้ anon/authenticated อ่าน payments และ flyway_schema_history ไม่ได้ แต่ restaurant_tables ยังมี 14 grants (7 ต่อ role) และ restaurant_tables_id_seq ใช้งานได้ทั้งสอง role หลักฐานนี้เป็นเหตุให้เพิ่ม V13

ก่อนเพิ่ม V13 เริ่ม Compose กับฐานกลางแล้วได้ health 200, Swagger 200, CORS allowed origin localhost:5173 และ credentials=true, schema up-to-date ไม่มี migration necessary หลังเพิ่ม V13 ไม่เริ่ม image ใหม่กับ Supabase และไม่เขียนข้อมูลทดสอบลงฐานกลาง

เครื่องมือตรวจ `test/tools/SharedDatabaseReadiness.java` เป็น read-only validate/info/JDBC metadata ไม่ migrate/repair และไม่พิมพ์ password/cookie/token หากมี pending migration จะ exit fail หลังรายงาน metadata ห้าม ignore pending แล้วอ้างว่าพร้อม deploy

## หลักฐานภาพและ JSON

- [Core Flow results](../../test/evidence/step2-close-2026-10-06/core-flow/results.json)
- [Visual states results](../../test/evidence/step2-close-2026-10-06/ui-states/results.json)
- [JDBC metadata จริง](../../test/evidence/step2-close-2026-10-06/shared-jdbc-metadata.json)
- [Shared runtime health/Swagger/CORS](../../test/evidence/step2-close-2026-10-06/step2-shared-http.json)

| หน้า | ขนาด | ภาพ |
| --- | --- | --- |
| Customer order | 360px | [customer-order](../../test/evidence/step2-close-2026-10-06/core-flow/customer-order-360.png) |
| Staff session/unpaid error | 768px | [session](../../test/evidence/step2-close-2026-10-06/core-flow/staff-session-768.png), [unpaid](../../test/evidence/step2-close-2026-10-06/core-flow/staff-unpaid-error-768.png) |
| Kitchen/Serving | 768px | [preparing](../../test/evidence/step2-close-2026-10-06/core-flow/kitchen-preparing-768.png), [serving](../../test/evidence/step2-close-2026-10-06/core-flow/staff-serving-768.png) |
| Payment/Close | 768px | [refresh](../../test/evidence/step2-close-2026-10-06/core-flow/payment-refresh-768.png), [available](../../test/evidence/step2-close-2026-10-06/core-flow/table-available-768.png) |
| User/Stock | 1280px | [users](../../test/evidence/step2-close-2026-10-06/core-flow/admin-users-1280.png), [confirm](../../test/evidence/step2-close-2026-10-06/core-flow/stock-confirm-1280.png), [history](../../test/evidence/step2-close-2026-10-06/core-flow/stock-history-1280.png) |
| Loading/Empty/Error fixtures | 768/1280px | [state images](../../test/evidence/step2-close-2026-10-06/ui-states) |

QR card ถูก mask สีทึบใน Staff screenshots จึงไม่มี token/QR credential ในภาพ ไม่มี cookie/password ใน results JSON ใช้ข้อมูล demo ที่ตั้งชื่อเฉพาะทั้งหมด ภาพตรวจผ่านด้าน layout/navigation/font/shared colors ไม่มี horizontal body overflow ที่ขนาดกำหนด Admin rail variants อยู่ใน tokens.css เดิมและบันทึกขอบเขตยอมรับใน UI Guide แล้ว

## วิธีทำซ้ำโดยไม่ใช้ฐานกลาง

Backend PostgreSQL tests ต้องสร้าง loopback databases ชื่อ buffet_test_* ที่มี comment buffet-disposable-test-only และตั้ง ALLOW_DESTRUCTIVE_DB_TESTS=true พร้อม MENU_TEST_PG_*, DINING_TEST_PG_*, PAYMENT_TEST_PG_* ดู [CI workflow](../../.github/workflows/ci.yml) และ [disposable SQL setup](../../code/backend/src/test/resources/disposable-postgres-ci.sql) Menu migration upgrade ต้องเป็น database ใหม่ทุก run

Browser demo: สร้าง PostgreSQL 18 container แบบทิ้งได้พร้อม database buffet_test_runtime ตั้ง STEP2_TEST_PG_PORT เป็น published localhost port แล้วใช้:

```powershell
docker compose -p step2-close -f docker-compose.yml -f test/runtime/step2-disposable.override.yml up -d --build
$env:STEP2_DISPOSABLE_DEMO = 'true'
node test/browser/step2-core-flow.cjs
node test/browser/step2-ui-states.cjs
```

ต้องมี Playwright/Chrome; ตั้ง PLAYWRIGHT_MODULE ได้เมื่อใช้ runtime bundled library Demo seeder ใช้เฉพาะ disposable profile และ override providers เป็น session/database ชัดเจน ห้ามใช้ demo account/seed บน Supabase output ดิบอยู่ test/reports ซึ่งถูก ignore; CI เก็บ surefire artifact แยก

## ขอบเขต Final และเงื่อนไขปิด Step 2

- **ประวัติ ณ รอบ Step 2 (ก่อน V15):** Stock opening_target_stock/active lifecycle และ Profile first_name/last_name/phone ยังไม่ implement ในรอบนั้น; ภายหลังเพิ่มด้วย V15 (ดู [Auth/Stock schema delta](../database/auth-stock-schema-delta.md))
- Manager CRUD screens ของ Table/Package/Soup/stock items และ Customer Request Bill/Bill Status ยังไม่ครบตาม UI Guide เดิม เป็นงานหน้าจอเพิ่มที่ไม่บล็อก Staff Core Flow รอบนี้ แยก Tasks Final และเว้น checklist ทุก-action ของ Role Flow ไม่ใช้ API seed/demo แทน UI ที่ยังไม่มี
- ไม่รวม public deployment/slides/SOLID-pattern report/Git audit Final
- PR รวมต้องผ่าน review จากศรัณย์ (API/transition), ศิระพัทธ์ (UI/tests), เมธัส (DB/Auth/V13), ธีรเมธ (Payment/runtime)
- หลัง merge ต้องรับรอง/นำ V13 ขึ้นฐานกลางอย่างมีขั้นตอน ตรวจ validate/grants และ smoke จาก develop จริงก่อนปิด Step 2 ไม่ merge เองและไม่ apply V13 ในรอบนี้
