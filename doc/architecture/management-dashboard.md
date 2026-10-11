# Management Dashboard

`GET /api/v1/dashboard?startDate=2026-10-01&endDate=2026-10-07` เป็น read-only summary สำหรับ MANAGER / SUPERVISOR เท่านั้น ตรวจ HTTP session ด้วย `UserContextProvider` ที่ service boundary รวมการตรวจบัญชี active/archive ทุกครั้ง ไม่อ่าน role header และไม่ขยายสิทธิ์ Payment, DiningSession หรือ Fulfillment เดิม

โครงสร้าง Controller → `DashboardService` → `DashboardRepository` → Response DTO แยกจาก transactional commands ใช้ aggregate queries และ projection ในฐานข้อมูล ไม่โหลดประวัติทุกแถวเข้า memory ใช้ read-only REPEATABLE_READ เพื่อให้ aggregate ในคำขอเดียวกันใช้ snapshot เดียวกันบน PostgreSQL และตอบ Cache-Control: no-store ไม่เพิ่ม schema/migration และไม่แก้ Strategy (Billing), State (Order) หรือ Template Method (Stock)

ทดสอบด้วย `DashboardIntegrationTest` บน H2 และ `PostgresDashboardIntegrationTest` บนฐาน PostgreSQL แยกที่ผ่าน `DisposablePostgresDatabase` guard ตั้ง `DASHBOARD_TEST_PG_URL` เป็น loopback URL ชื่อ `buffet_test_...`, `DASHBOARD_TEST_PG_USER`, `DASHBOARD_TEST_PG_PASSWORD`, `ALLOW_DESTRUCTIVE_DB_TESTS=true` และ comment ฐาน `buffet-disposable-test-only` พร้อม roles `anon/authenticated` ห้ามใช้ฐานกลาง

CI สร้าง `buffet_test_dashboard_ci` ใน PostgreSQL service ชั่วคราวพร้อม marker และส่ง opt-in ให้ชุด Dashboard โดยเฉพาะ เพื่อให้ native aggregate query และขอบเขตวันเวลาไทยถูกตรวจบน PostgreSQL ทุก PR

รายวันใช้ EXTRACT ปี/เดือน/วันหลัง AT TIME ZONE เป็นเวลาไทยโดยตรง เนื่องจาก H2 แปลง TIMESTAMP WITH TIME ZONE เป็น DATE ตาม session timezone ต่างจาก PostgreSQL ชุด H2 จึงตั้ง database session เป็น UTC เพื่อยืนยันว่าบิลตรงเที่ยงคืนไทยไม่หาย แม้เครื่องทดสอบใช้เวลาไทย

| ข้อมูล | นิยาม |
|---|---|
| ช่วงวันที่ | รวมทั้งวันเริ่มและวันสิ้นสุดใน Asia/Bangkok; ไม่ส่งวันที่ใช้วันนี้; ไม่เกิน 366 วันและไม่เกินวันนี้ ค้นด้วย `>= start` และ `< วันถัดจาก end` |
| รายได้/ยอดขายสุทธิ | SUM Payment.amount เฉพาะ PAID ตาม paid_at รวมบิลที่ชำระแล้วแต่ยังรอปิดรอบ; ไม่รวม PENDING/FAILED และไม่คำนวณราคาขายใหม่ |
| จำนวนบิล / ค่าเฉลี่ย | COUNT Payment.PAID / รายได้หารจำนวนบิล ปัด HALF_UP 2 ตำแหน่ง; ไม่มีบิลได้ 0 |
| รายวัน/ช่องทางชำระ | รายได้ตามวันรับชำระเวลาไทยและ CASH/QR/CARD; เติมวัน/ช่องทางที่ไม่มีข้อมูลเป็น 0 |
| รอบ/ลูกค้า | รอบที่ start_time อยู่ในช่วง รวมรอบที่ปิดแล้ว; ผู้ใหญ่และเด็กนับตามข้อมูลเปิดรอบเดิม ซึ่งเก็บ LocalDateTime เป็น UTC |
| ออเดอร์/เมนูยอดนิยม | ตาม created_at เวลาไทย; จำนวนเสิร์ฟจาก OrderItem snapshot รวมทุกสถานะ ชื่อที่เปลี่ยนแยกตามชื่อประวัติ; สูงสุด 5 รายการ เรียงจำนวนแล้ว id/ชื่อ |
| สถานะโต๊ะ/รอบ | สถานะปัจจุบัน ไม่กรองช่วงวันที่; โต๊ะไม่เก็บออก, รอบ ACTIVE, ลูกค้าในรอบ ACTIVE, เรียกบิลยังไม่ PAID และ PAID แต่ยัง ACTIVE |
| คิวอาหาร | ออเดอร์ RECEIVED/PREPARING/READY ที่รอบยัง ACTIVE; เป็นจำนวนออเดอร์ ไม่ใช่เสิร์ฟ |
| สต๊อก | ปัจจุบันเฉพาะ active และไม่เก็บออก; ถึงจุดเตือนเมื่อ quantity <= threshold ตาม StockPage เดิม; หมดเมื่อ quantity = 0; ขาดเป้าหมายเมื่อ quantity < target |
| รายการเตือนสต๊อก | สูงสุด 10 รายการที่ถึงจุดเตือน เรียง quantity แล้ว id; shortfall = max(target - quantity, 0) หน่วยของแต่ละรายการไม่ถูกรวมกัน |

ระบบไม่มีต้นทุนวัตถุดิบหรือค่าใช้จ่าย จึงไม่แสดงกำไรหรือสร้างค่าประมาณต้นทุน เมนูบุฟเฟต์ไม่มีราคาขายต่อจาน รายได้ใช้รายการรับชำระจริงเสมอ

Frontend `/admin` เปิด Dashboard เป็นหน้าแรกสำหรับสอง role ใช้ AdminShell, session expiry, Axios credentials, tokens และส่วนประกอบเดิม รองรับ loading/error/retry/empty, รีเฟรชและเปลี่ยนช่วงวันที่ พร้อมละทิ้ง response จากช่วงเก่า สถานะปัจจุบันมีเวลาอัปเดตแสดงแยกจากยอดตามช่วงวันที่

บัญชี H2 สร้างผ่าน Manager และ API เดิมด้วย `scripts/setup-demo-users.ps1` ซึ่งอนุญาตเฉพาะ loopback ส่วนบัญชี `Demo...` ของเว็บสาธิตออนไลน์สร้างผ่านหน้า Manager → พนักงาน หลังตรวจบัญชีเดิม รายละเอียดอยู่ใน README และ [ผลตรวจออนไลน์](../testing/online-demo-accounts-2026-10-11.md) ไม่ทำ provisioning/reset บน shared database ใน startup และไม่เพิ่ม role enum
