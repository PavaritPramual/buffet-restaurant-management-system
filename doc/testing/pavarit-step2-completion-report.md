# รายงานเติม Step 2 แทน PR #20

## งานที่เพิ่ม

ปิด PR #20 โดยไม่ merge และเก็บสาม commits เดิมบน `pavarit_673380278-9_01` ต่อยอดจาก develop `888b4ea` รอบนี้ย้าย Manager master-data UI และ Customer Request Bill/Bill Status กลับ Step 2 ส่วน Stock target/active และ Profile รายละเอียดยังคง Final

- Manager ใช้ Admin shell/ManagerRoute/components เดิม จัดการโต๊ะ แพ็กเกจ น้ำซุป และ stock item ผ่านเว็บ; โต๊ะ ACTIVE ห้ามแก้และมีประวัติห้ามลบ แพ็กเกจ/น้ำซุปปิด/เปิดได้โดยคงประวัติและ opening-price snapshot
- Stock catalog API เฉพาะ Manager เพิ่มรายการยอดศูนย์ แก้ name/threshold ได้ ไม่แก้ยอดตรง และไม่เปลี่ยน SKU/หน่วยหลังมี transaction
- Customer ขอคิดบิลแบบ idempotent ตรวจ cookie/Origin/session ACTIVE เพิ่ม bill_requested_at ผ่าน V14 และหยุดรับ Order ใหม่ทุกเครื่องด้วย lock ร่วมกับ Order/close
- Customer GET bill-status ใช้ BillingEngine/snapshot และ recorded amount หลัง PAID ไม่มี Payment ID หรือ QR credential; Staff เห็น badge แล้วใช้ Billing/Payment/close เดิม ชำระแล้วรอบยัง ACTIVE
- Customer polling ทุก 5 วินาทีเมื่อแสดงหน้า ยกเลิกเมื่อ unmount และป้องกันคำตอบ QR เก่า/คำตอบก่อนขอคิดบิลเขียนทับสถานะใหม่

Contract: [Customer bill request](../contracts/customer-bill-request.md) · [Canonical schema](../database/step2-schema-approved.md) · [ERD](../diagrams/er-diagram.puml)

## ผลตรวจ

### แก้รีวิว DTO/API ของศรัณย์ — 6 ตุลาคม 2026

- Customer bill `status` เปลี่ยนจาก String เป็น enum CustomerBillStatus โดย JSON ยังใช้ NOT_REQUESTED/REQUESTED/PAID เดิม อธิบายชัดว่า PAID ไม่ปิด session
- เพิ่ม dueAmount/paidAmount แยกยอดค้างกับยอดที่จ่ายจริง และกำหนด bill.totalAmount เป็นยอดสุทธิของบิลเสมอ ไม่ใช้เป็นยอดค้าง หลัง PAID ยอดรวมยังคงเป็นยอดบิลสุดท้าย แต่ dueAmount เป็นศูนย์
- หน้า Customer แสดงยอดรวม ยอดค้างชำระ และยอดชำระแล้วแยกกัน ปรับ frontend type/fixtures/contract พร้อม JSON ตัวอย่าง ไม่มีการเปลี่ยน PaymentResult, BillSummary หรือ migration
- API integration assertions ครอบคลุมยอดทั้ง NOT_REQUESTED/REQUESTED/PAID และ OpenAPI enum/amount fields; frontend regression ตรวจข้อความยอดก่อน/หลังจ่าย
- Backend verify 303/303 ไม่มี failures/errors/skipped บน H2/PostgreSQL แยก (port15433 container buffet-pr21-sarun-pg), frontend109/109, lint0errors/4warningsเดิม และ build ผ่าน ไม่แตะ Supabase หรือฐานเว็บของผู้ใช้ ภาพ browser ด้านล่างยังเป็นหลักฐานรอบเดิม

### แก้รีวิว PR #21 ของศิระพัทธ์ — 6 ตุลาคม 2026

- บังคับ `Order → Request Bill → Payment → Close` ฝั่ง server: ก่อนขอคิดบิล Payment ตอบ 409 และไม่มีแถว Payment เพิ่ม ดูยอดล่วงหน้าได้
- Payment ใช้ PESSIMISTIC_WRITE แถว DiningSession เดียวกับ Order/Request Bill/Close แล้ว refresh และตรวจ ACTIVE + bill_requested_at หลังได้ lock ภายใน transaction เดียวกับบันทึก PAID
- หน้า Billing อ่าน Staff session จาก API เดิมและปิดปุ่มรับชำระก่อนขอคิดบิล ให้กดดูบิลอีกครั้งหลังลูกค้าขอ ไม่มีการเปลี่ยน Payment DTO หรือ migration
- เพิ่ม H2/API tests ก่อนขอ→409, ขอแล้ว→201 PAID, หลัง PAID→Order 409/รอบยัง ACTIVE และ PostgreSQL tests สามคำขอพร้อมกัน, Payment รอ Request Bill commit, Payment รอ Close แล้ว re-check COMPLETED
- ผลรอบแก้: backend 302/302 ไม่มี failures/errors/skipped; PostgreSQL concurrency 8/8, frontend 109/109, Billing UI regression 7/7, lint 0 errors/4 warnings เดิม และ build ผ่าน
- ฐานรอบนี้เป็น PostgreSQL 18.6 container แยก `buffet-pr21-review-pg` ที่ port 15433 มี marker buffet-disposable-test-only; Payment role ไม่ใช่ superuser ไม่แตะฐานเว็บทดลองของผู้ใช้หรือ Supabase
- ภาพ/browser evidence ด้านล่างเป็นรอบ implementation เดิม ไม่ได้อ้างว่ารัน browser flow ใหม่ในรอบแก้รีวิวนี้

### ผลรอบ implementation เดิม

Implementation commits: `1133186` Manager, `d56cc60` Bill request backend, `1472f4e` Customer/Staff UI; เอกสารและ evidence ใน commit ถัดมา รักษา commits เดิมของ PR #20 ทั้งสามชุดไว้

| ชุดตรวจ | ผล | สภาพแวดล้อม |
| --- | --- | --- |
| Backend verify | 296/296 ไม่มี failures/errors/skipped | H2 + PostgreSQL 18.6 loopback ทิ้งได้; Payment DB ใช้ role buffet_backend_test ที่ไม่ใช่ superuser |
| V14 metadata | H2/PostgreSQL ผ่าน timezone/nullable และ Hibernate validate | migration common V14; V1–V12 ไม่เปลี่ยน |
| PostgreSQL concurrency | Order→close / close→Order และ Order→bill-request / bill-request→Order ผ่าน | actual row lock; threads/transactions แยก; ตรวจ pg_stat_activity ว่า worker รอ lock |
| Frontend | 108/108, build ผ่าน, lint 0 errors/4 warnings เดิม | Vitest/TypeScript/Vite; warnings เดิมใน Admin Stock/User และ Kitchen/Serving |
| Core Flow browser | 14 scenarios ผ่าน | Chrome contexts Customer360/Staff768/Kitchen768/Manager1280/Supervisor1280, Docker Compose → Spring Boot Java17 → PostgreSQL ทิ้งได้; ไม่มี HTTP mocks |
| UI states | 7 page groups × loading/empty/error ผ่าน | HTTP response fixtures กับ shell/login จริง แยกจาก Core Flow; font Noto Sans Thai, primary #9a3412, no body overflow |

หลักฐาน: [directory](../../test/evidence/step2-complete-2026-10-06/) · [backend summary](../../test/evidence/step2-complete-2026-10-06/backend-test-summary.json) · [Core Flow results](../../test/evidence/step2-complete-2026-10-06/core-flow/results.json) · [UI state results](../../test/evidence/step2-complete-2026-10-06/ui-states/results.json)

ภาพปิดบังการ์ด QR ของ Staff ทุกภาพ ไม่มี cookie/password/token ใน evidence JSON ภาพที่ใช้บัญชี demo/data มีเฉพาะข้อมูลจากฐานทิ้งได้

## Browser flow จริง

1. Manager เพิ่มผู้ใช้ผ่าน UI แล้ว login ตาม role ตรวจ Fulfillment/master-data permission และ spoofed header
2. Manager สร้าง Table/Package/Soup/Stock item ผ่าน UI ใหม่ หน้า stock ใหม่แสดงยอดศูนย์
3. Staff เปิดรอบ ผู้ใหญ่สอง/เด็กหนึ่ง ราคา package399; close ก่อนจ่ายถูกปฏิเสธ
4. Customer แลก QR ผ่าน fragment/cookie และสั่งหนึ่ง order; ใช้ QR เดิมซ้ำได้404
5. Kitchen เตรียม→พร้อมเสิร์ฟ; Staff เสิร์ฟ; Customer refresh เห็น SERVED
6. Customer ยืนยันขอคิดบิล เห็น REQUESTED ปุ่มสั่งถูกปิด และ POST Order จริงได้409; Staff table overview เห็น badge
7. Staff รับ CASH997.50; Customer polling เห็น PAID และยอดตรง recorded Payment; refresh Staff เห็น paymentId เดิม
8. Staff กด close แยก โต๊ะกลับ AVAILABLE และ Customer credential ใช้ต่อไม่ได้
9. Supervisor รับเข้ารายการ stock ที่ Manager สร้างและปรับยอดพร้อมเหตุผล/confirmation ตรวจ historyและbalance; logout ทำให้ API401

Menu category/item setup ยังคงใช้ existing Manager domain APIs ของโมดูลที่ผ่านรีวิวแล้ว; ไม่ใช้ API setup เป็นหลักฐานหน้าจอใหม่ Table/Package/Soup/Stock item

## ฐานกลางและเงื่อนไขปิดขั้น

อ่าน Supabase history วันที่6ตุลาคม2026ยังพบ V1–V12 success โดย V9 rank11 ตามข้อมูลเดิม ไม่แตะข้อมูลทีม ไม่ apply V13/V14 และไม่เริ่ม image ใหม่นี้กับ Supabase

V13 ปิด residual client grants; V14 เพิ่มคำขอคิดบิล ยังต้อง DB review/merge/deployment แล้วตรวจ Flyway validate/grants และ smoke จาก develop ที่รวมจริง ไม่มีการ repair หรือแก้ checksum ของ applied migrations

PR ใหม่ภาษาไทยรวมทั้งงานเดิมและงานรอบนี้ ขอศรัณย์ตรวจ API/transition, ศิระพัทธ์ UI/tests, เมธัส DB/Auth/V13/V14 และธีรเมธ Billing/Payment/runtime หลังเปิด PR หยุดรอรีวิว ไม่ merge เอง ไม่ติ๊กปิด Step 2 ก่อนผ่าน review และระบบกลาง

Stock opening_target_stock/active lifecycle, Profile first/last/phone, public deployment, slides, SOLID/pattern report และ Git-history audit ยังคง Final
