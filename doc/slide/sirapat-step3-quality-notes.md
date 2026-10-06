# ศิระพัทธ์ — สไลด์ Step 3 และ speaker notes

สถานะ: contribution รอบเริ่มงาน 6 ตุลาคม 2026; public release ยังรอ

หมายเหตุสำหรับการรวมชุด Final: PPTX/PDF นี้เป็นร่างรอบเริ่มงานและยังคงผล local112/274/27skippedตามภาพเดิม ไม่ใช่ตัวเลขตรวจซ้ำล่าสุด หลังตรวจเพิ่ม frontendเป็น116 และ CoreFlow12+concurrencyfixtures3; CIของe1c78f8ผ่านPG303/303 ดู [รายงานตรวจซ้ำ](../testing/sirapat-step3-recheck-report.md) และ PR#22 สำหรับ CI ของ head ล่าสุด ให้เปลี่ยนตัวเลข/ภาพ/URLในชุดรวมหลัง release จริงและให้เจ้าของยืนยันก่อนซ้อม

## 1. ศิระพัทธ์ — การทดสอบและคุณภาพ UI

- Menu / Ordering · Customer QR · Shared UI
- Buffet Restaurant Management System
- Step 3 · 6 ตุลาคม 2026
- ผล local รอบเริ่มงาน — ยังรอ public release

หน้าที่ของศิระพัทธ์คือดูแล test strategy/traceability, Customer และ components กลาง ตรวจ UI เจ้าของ feature และส่งหลักฐานที่แยก baseline/local/public ชัดเจน สไลด์ชุดนี้เป็นส่วนส่งให้ปวริศช์รวม ยังไม่ได้รับรอง release หรือการซ้อมห้าคน
Source: https://app.notion.com/p/37e90b8ff964834fad3701e2d8115de2

## 2. หลักฐานการทดสอบแต่ละระดับ

- Unit / Component: กฎธุรกิจและ state ของหน้าเว็บ
- H2 Integration: HTTP, cookie, Origin และ persistence
- PostgreSQL: migration, grants และ lock contention
- Browser: flow จริงผ่านบัญชีแยกและ QR ที่ระบบสร้าง
- Public release: HTTPS และ environment ที่นำไปส่งจริง

ผล H2 ไม่พิสูจน์ PostgreSQL lock/grants ส่วน HTTP response fixtures ใช้ตรวจ loading/empty/error ต้องแยกจาก Core Flow ที่ไม่มี mocks CI ต้องเป็น run ของ revision ที่ตรวจรับ เอกสารอ้าง test name อย่างเดียวไม่ใช่ผลรัน
Sources: doc/testing/test-plan.md; .github/workflows/ci.yml; doc/testing/sirapat-step3-report.md

## 3. QR และสิทธิ์ของ Customer

- QR fragment → แลกครั้งเดียว → HttpOnly cookie
- StrictMode แชร์การแลก QR; สแกนใหม่ล้างข้อมูลรอบเก่า
- หลายมือถือใช้ context แยกและ QR ที่ refresh แล้ว
- ขอคิดบิล → ทุกมือถือหยุด Order → PAID → ปิดรอบ
- ID อย่างเดียวไม่ให้สิทธิ์; cookie / Origin ต้องผ่าน

QR token ถูกลบจาก fragment ทันที ส่งใน POST body เท่านั้น backend rotate QR และเก็บ hash ของ credential หน้าเว็บกัน stale response และ retry menu ผ่าน cookie แทนแลก token ใช้แล้วซ้ำ ใช้หนึ่ง ordering tab ต่อ browser profile; ไม่ได้อ้างว่ามี cross-tab synchronization
Sources: doc/contracts/ordering-contract.md; code/frontend/src/features/ordering/api.ts; CustomerOrderingPage.tsx; CustomerOrderingPage.test.tsx

## 4. Session หมดอายุและผลตรวจในเครื่อง

- แก้ Manager / Supervisor ให้กลับหน้าเข้าสู่ระบบเมื่อ API 401
- คำตอบ auth ที่มาช้าไม่เปิดข้อมูลเดิมกลับขึ้นมา
- Frontend 112 ผ่าน · Backend 274 ผ่าน / 27 skipped
- Lint 0 errors / 4 warnings เดิม · Build ผ่าน
- รอบนี้ใช้ H2 แยก; ยังไม่ได้รับรอง PostgreSQL / public

ก่อนแก้เพิ่มสาม regression cases แล้วล้มเหลวทั้งสาม: Manager/Supervisor ยังเห็น stock หลัง protected API401 และ late restore ยังคืนหน้าเดิมได้ หลังแก้ทั้งหมดผ่าน คำว่า backend274มาจาก301 discovered minus27 skipped ไม่ใช้303ของStep2แทนผลใหม่ สาเหตุ skipped คือไม่มี configured disposablePG และ Docker daemon ตัวเลขเป็น local รอบ6ตุลาคม ไม่ใช่ release
Sources: code/frontend/src/features/admin/AdminShell.tsx; AdminShell.test.tsx; doc/testing/sirapat-step3-report.md

## 5. Menu / Ordering: SOLID และ JPA

- Controller → Service → Repository / Mapper → DTO
- แยก Catalog กับ Ordering; inject access/session providers
- MenuCategory: LAZY และไม่ cascade ลบหมวดร่วม
- PackageMenuItem: join table ผ่าน ElementCollection
- EntityGraph โหลด category / packageIds เมื่อ Customer ต้องใช้

S แยก catalog/order/mapping; O เพิ่ม provider ได้; L allowed/denied contract แต่ fixtureใช้เฉพาะการตั้งค่าทดสอบ; I orderingไม่รับcatalog writes; D constructor injection PackageMenuItemไม่มีJavaentityแยก ต้องแยกSQLcascade membershipกับJPAentitycascade Orderhistoryใช้RESTRICT จึงmark unavailableแทนลบ ไม่มีquery-count benchmarkของAdmin pagination จึงไม่อ้างว่าไม่มีN+1ทุกquery
Sources: doc/architecture/sirapat-menu-ordering-solid-jpa.md; MenuItem.java; MenuItemRepository.java; V4__create_menu_and_order_tables.sql

## 6. เกณฑ์ก่อนส่ง Final

- Stock target / active และ Profile: รอเจ้าของ feature + review
- รัน regression ใหม่บน release commit และ public HTTPS
- ภาพ Customer 360px · Staff/Kitchen 768px · Manager 1280px
- Reviewer ยืนยัน; main และ deployed commit ตรงกับหลักฐาน
- รวมสไลด์กับทีมและซ้อมด้วยบัญชีจริง / QR จริง

เมธัสรับStock/Profile ธีรเมธรับpublicdeployment ปวริศช์รวมrelease/E2E ศรัณย์ตรวจAPI/State ศิระพัทธ์ตรวจUI/tests ไม่มีการติ๊กFinalจากผลlocalหรือreportเก่า ไม่มีpassword/token/cookieในสไลด์
Sources: Notion Step3 https://app.notion.com/p/37e90b8ff964834fad3701e2d8115de2; doc/testing/requirement-test-traceability.md
