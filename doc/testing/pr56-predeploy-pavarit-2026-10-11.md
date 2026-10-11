# ปวริศช์ตรวจ PR #56 ก่อน deploy

ตรวจ 11 ตุลาคม 2026 บน head `aabc88feeac32fc2a80befd7842e10331a7e5e27` โดยใช้ checkout แยก, Java21, backend port18056/H2 memory ใหม่ และ frontend localhost5173. ปิด spring.config.import; runtime ใช้ session/database providers จริง ทุก write อยู่ใน local H2 เท่านั้น ไม่ apply migration หรือเขียนข้อมูลร้านบน Supabase/Render

## ผลตรวจอัตโนมัติ

- [CI ของ head aabc88f](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/38128695551) Backend/PostgreSQL441 ผ่าน ไม่มี failures/errors/skipped; Frontend193, guards6, lint0errors/5warnings และ buildผ่าน
- รัน frontend ใน checkout นี้ซ้ำ: 193/193 ผ่าน, lint0errors/5warnings เดิม และ production buildผ่าน
- Backend package ผ่าน; DashboardIntegrationTest/DemoDataSeederTest ที่รันในรอบตรวจ source c8a2cc2 ผ่าน6/6 บน H2. หลัง c8a2cc2 ถึง aabc88f เปลี่ยนเฉพาะเอกสาร/หลักฐาน โค้ด application ไม่เปลี่ยน ไม่อ้างว่า local รอบนี้รัน PostgreSQL หรือ backend suite441ซ้ำ

## API จริงบน H2 ใหม่

สร้างบัญชีผ่าน setup-demo-users.ps1 กับ Manager API เดิม แล้วสร้าง package299/menu/soup/สามโต๊ะ เปิดสามรอบผู้ใหญ่2เด็ก1 สั่งรอบละ2เสิร์ฟ ผ่าน PREPARING → READY → SERVED ก่อนขอคิดบิล ใช้ HTTP sessions แยกตาม role/customer

- สองรอบ PAID: CASH747.50 และ QR747.50 รอบแรก closeแล้ว รอบที่สองยัง ACTIVE; รอบที่สามเรียกบิลและยังไม่จ่าย
- หลังขอคิดบิลสั่งเพิ่ม409, จ่ายซ้ำ409, ปิดก่อนจ่าย400ตาม contract เดิม, customerของรอบที่ปิดแล้วอ่านOrder401
- Dashboardรายได้1495.00, บิล2, เฉลี่ย747.50, รอบเปิด3, ออเดอร์3, เมนูยอดนิยม6เสิร์ฟ
- สถานะปัจจุบัน active2, PAIDรอclose1, เรียกบิลรอจ่าย1 ตรงกับ flow; Supervisorปรับstockผ่าน API เดิมเพื่อให้มีรายการเตือน
- Dashboard Manager/Supervisor200, ServiceStaff/KitchenStaff403, anonymous401

ดู [ผล API ที่ตัด token/cookie/password ออก](../../test/evidence/pr56-predeploy-2026-10-11/local-api-flow.json). การตรวจรอบแรกคาด closeก่อนจ่าย409แต่ระบบตอบ400; แก้ความคาดหวังของ runner ให้ตรงโค้ดเดิมและเริ่ม H2 ใหม่ก่อนรันทวน ไม่มีการแก้ application เพื่อให้ผ่าน

## Browser จริงด้วย Playwright CLI

- Manager login เข้า /admin/dashboard, หน้ารายได้แสดง1495.00และเฉลี่ย747.50ตรง API; เปลี่ยนช่วงเป็น1–3ต.ค.แสดงรายได้0/ไม่มีบิล โดยสถานะร้านยังเป็นปัจจุบัน
- logoutกลับหน้าlogin; restart backendทำให้ sessionเดิมใช้ต่อไม่ได้และกลับหน้าlogin
- Supervisor login เข้าDashboard มีเมนู Dashboard/Stockเท่านั้น; direct /admin/users ถูกพาไป /admin/stock
- Supervisor viewport360×800 มี document.scrollWidth360 ไม่ล้นแนวนอน ตรวจภาพจริงแล้ว
- [ภาพ Manager](../../test/evidence/pr56-predeploy-2026-10-11/manager-dashboard-local.png) · [ภาพ Supervisor](../../test/evidence/pr56-predeploy-2026-10-11/supervisor-dashboard-local-360.png)

Core Flow ข้างต้นทดสอบผ่าน HTTP API จริง ส่วน browser ตรวจ Dashboard/login/navigation/date range ไม่อ้างว่าได้กดทุกขั้นของ Core Flow ผ่าน UI อีกครั้ง 401จาก auth/me ก่อนloginและหลังrestartเป็นผลที่คาดไว้ ไม่มีข้อผิดพลาด Dashboard ที่พบจากการตรวจนี้

## ข้อสรุปและ gate หลัง deploy

ไม่พบ blocker จากขอบเขตที่ตรวจ รุ่นนี้พร้อมให้ reviewerพิจารณารวมและ deploy; หลังdeployยังต้องยืนยัน Live SHA และทวน Manager/Supervisor Dashboard200/หน้าแรก รวมสิทธิ์อีกครั้งบน Render โดยผลlocal/CIไม่แทนpublic acceptance. รายงานนี้ไม่เปลี่ยน Auth/State/Strategy/Template Method, migration หรือกฎธุรกิจเดิม ปิด browser/backend/frontend local หลังตรวจ
