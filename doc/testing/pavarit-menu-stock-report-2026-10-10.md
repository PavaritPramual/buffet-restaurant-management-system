# รายงานสูตรเมนูและหักสต๊อก — 10 ตุลาคม 2026

เจ้าของ ปวริศช์ · ฐาน develop `c778150c79657b80930ceca6a4f10c136c1fc285` · backend `0210403` · frontend `e41a2755cea7b38d63e8d8dade8fabe7d0b71d65`

**สถานะ:** implement และตรวจบนฐานแยกผ่าน รอ PR review/merge และอนุมัติ V19 กลาง ไม่ใช่ public acceptance ของฟีเจอร์ใหม่

## Environment และวิธีตรวจ

- Windows / Java 26.0.1 ในเครื่อง (CI ใช้ Java 17), Maven ของโครงการ และ frontend toolchain เดิม
- H2 ใหม่ แยกชื่อฐาน; PostgreSQL 18.6 container แยกที่ loopback 15432 และ Testcontainers PostgreSQL 16.15 ตาม tests เดิม
- ปิด `.env` import ด้วย `-Dspring.config.import=`; ชุด PostgreSQL ใช้ฐานใหม่ที่มี marker ตาม helper พร้อม `ALLOW_DESTRUCTIVE_DB_TESTS=true` ไม่มี URL/รหัส Supabase ใน automated tests
- รัน `mvn --batch-mode --no-transfer-progress -Dspring.config.import= verify` โดยกำหนด MENU_TEST_PG_URL, DINING_TEST_PG_URL, PAYMENT_TEST_PG_URL และผู้ใช้/รหัสของฐานทิ้งได้
- frontend รัน tests, lint, build ตาม scripts โครงการ และ URL guards ตาม CI
- Browser รัน `test/browser/step3-local-runtime.cjs` โดยใช้ `FINAL_JAVA` ชี้ Java จริง, `FINAL_LOCAL_SCRIPTS` ระบุ menu-stock-consumption, step3-core-flow, step3-stock-profile และ output แยกของแต่ละ runner
- Launcher ใช้ fresh H2, seed fixtures ปิด, session/database providers จริง บัญชีสร้างใหม่และ login ผ่าน UI; HTTP ไม่ mock ใช้ Customer contexts แยกจาก Kitchen/Staff/Manager

## ผลตรวจจริง

| ชุด | ผล |
|---|---|
| Backend full suite | 429 ผ่าน; failures 0, errors 0, skipped 0 |
| สูตร/Consumption H2 | 7 scenarios ผ่าน |
| สูตร/Consumption PostgreSQL | 8 scenarios ผ่าน รวม FK/index/RLS/grants และ concurrency |
| Flyway/JPA | V1–V19 บนฐานแยกใหม่และ upgrade tests ผ่าน; migration เดิมไม่แก้ |
| Frontend | 185 tests ใน 20 files ผ่าน |
| URL guards | 6 ผ่าน |
| Lint / production build | ผ่าน; lint warnings เดิม 5 รายการ |
| Browser feature | 5 scenarios ผ่าน |
| Browser Core Flow | 16 scenarios ผ่าน |
| Browser Stock/Profile | 8 scenarios ผ่าน |
| Swagger | UI เปิดได้; OpenAPI มี GET สูตร, request stockUsage และ response 409 ของ start; เรียก endpoints ที่เปลี่ยนจริงด้วย cookie ผ่าน HTTP |

Warnings เดิมเกี่ยวกับ hook dependencies ใน StockPage (2), KitchenPage, StaffServingPage และ UsersPage ไม่ได้ยกเลิก rule เพื่อให้ผ่าน Backend มี Mockito dynamic-agent warning, Springdoc enabled warning และ Flyway แจ้ง PostgreSQL 18 ใหม่กว่ารุ่นที่ทดสอบรองรับ ไม่เกิด test failure

รัน full suite ครั้งแรกมีหนึ่ง failure เนื่องจากใช้ฐาน Menu ที่ focused tests apply V19 ไปแล้ว ขณะที่ legacy upgrade test ต้องเริ่มฐานใหม่ แก้การตั้งค่าด้วยฐานใหม่แยกชื่อ แล้ว full suite ผ่านทั้งหมด ไม่มีแก้ migration history/repair หรือแก้ assertion เพื่อหลบ failure

Browser รอบแรกติด selector ของ SelectField และรอบถัดมาติด process Java เก่าที่ค้างจาก alias จึงแก้ runner ให้เลือก combobox ตาม accessible name และใช้ Java executable จริง ทวนใหม่ครบสาม runners ผ่าน ผลและ hash ของ runner รอบสำเร็จอยู่ใน evidence ไม่ใช้ผลรอบเสียเป็น acceptance

## สิ่งที่พิสูจน์

- Manager สร้างสูตรหลายวัตถุดิบ/แก้ mode; validation จำนวน/ซ้ำ/ทศนิยม/สถานะและสิทธิ์รักษาความครบชุด
- Order เก็บสูตรตอนสั่ง; แก้หรือปิดสูตรภายหลังไม่เปลี่ยน snapshot เดิม
- รวมวัตถุดิบซ้ำทั้ง Order; สต๊อกไม่พอ/inactive rollback ยอด audit และสถานะทั้งชุด เติม/เปิดกลับแล้วลองใหม่ได้
- คำขอเริ่มทำพร้อมกัน Order เดียวผ่านเพียงครั้งเดียว; หลาย Order แย่ง Stock มีผู้ได้ 200 และผู้ถูกปฏิเสธ 409 ไม่ติดลบบน PostgreSQL จริง
- Browser สูตรหมู 0.100 กก. × 2 เสิร์ฟ: ยอด 0.150 ไม่พอได้ 409; เติม 0.350 แล้วแก้สูตรใหม่เป็น 0.200 ต่อเสิร์ฟ ก่อน retry ยังหักตามสูตรเก่า 0.200 รวม เหลือ 0.300 และ audit หนึ่งรายการ
- Core Flow หลังหักผ่านครัว/เสิร์ฟ/ขอคิดเงิน/CASH/close; bill 299 จากราคาแพ็กเกจ ไม่ใช้ราคาหรือจำนวน Stock คำนวณบิล
- สูตรอ่านเฉพาะ Manager; anonymous 401, Kitchen 403; Customer DTO ไม่เผยสูตร; หน่วย Stock ที่มีสูตร/snapshot อ้างอิงแก้ไม่ได้และรักษาประวัติเมื่อเก็บออก

Tests integration ที่ใช้ identity fixtures แยกจาก browser login จริง H2 ไม่ใช้แทน PostgreSQL locking และ browser local ไม่ใช้แทน Render/HTTPS/cold start/8h TTL

## หลักฐาน

[ผลและภาพชุดนี้](../../test/evidence/menu-stock-v19-2026-10-10/) มี backend suite summary, runtime/runner hashes, scenario results และภาพ Manager 1280px, Customer 360px, Kitchen/Staff 768px ภาพไม่เก็บ cookie/password/QR credential

Runner ระบุ sourceCommit เป็น `e41a275` และ working-tree เพราะมีการแก้ runner/เอกสารใน commit สุดท้ายของ PR runtime code ไม่เปลี่ยนหลัง commit นี้ ใช้ `source-sha256.json` และ runtime-summary ตรวจไฟล์ที่รัน ไม่อ้างว่า baseline เก่าเท่ากับ release สุดท้าย

[Public UAT รุ่นก่อน](pavarit-public-uat-2026-10-10.md) และ [Swagger coverage รุ่นก่อน](../../test/evidence/pavarit-public-uat-2026-10-10/swagger-ui-api-coverage.md) ถูกนำมาด้วยจาก `fc0edac` เป็นหลักฐานของ develop `c778150` เท่านั้น 78/78 operations หมายถึงเรียกครบ โดยหลาย CRUD เป็น validation/permission checks ไม่ใช่ happy-path ครบทุก operation

## Design / เกณฑ์ส่งและข้อค้าง

[ตารางเกณฑ์ก่อน–หลัง](../architecture/menu-stock-consumption.md) · [V19 Data Dictionary/JPA](../database/menu-stock-consumption-v19.md) · [Diagram index/source/preview](../diagrams/README.md)

คง State resolver, Strategy Billing และ Template Method ของ Stock; เพิ่ม consumption processor และ interfaces ตาม Layered Architecture ตรวจเกณฑ์วิชาและไม่ลบ diagram ชนิดใด Maven validate และ PlantUML render ผ่าน ตรวจภาพแปดภาพที่แก้แล้ว `git diff --check` ผ่าน

- [x] Implement/tests/docs บนฐานแยก
- [ ] PR review และ merge
- [ ] รับรอง/อนุมัติ/apply V19 กลาง แล้ว Flyway checksum/JPA validate
- [ ] Render deploy commit ที่ merge และ public recipe → Order → Kitchen → audit acceptance

Read-only Supabase พบ V1–V18 success และ V19 ยังว่างก่อนทำ ไม่ใช้ history แทน validation ไม่ apply หรือ repair ฐานกลางในงานนี้
