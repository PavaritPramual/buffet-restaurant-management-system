# Step 3 Final — plan snapshot

**อัปเดตแนวทางสไลด์7ตุลาคม2026:** ใช้ [Canva ทีม 40 หน้า](https://www.canva.com/d/D0dhcGCjuTTS7pV) เป็นต้นฉบับและเครื่องมือพรีเซนต์ของทั้ง5คน ผู้ใช้อนุมัตินำเข้าแล้ว อ่านกลับข้อความและnotesครบ รอตรวจภาพทุกหน้า/owner review/PDF export/ผลrelease/ซ้อม เก็บPDFที่exportจากCanvaเป็นเวอร์ชัน พร้อมlink/changelogใน [doc/slide](../slide/README.md) ไม่ใช้PPTXเป็นชุดส่งหรือขอreview ร่างรายคนและข้อความPPTXในsnapshotด้านล่างเป็นประวัติ ไม่ติ๊กเกณฑ์สไลด์Finalจากร่างเนื้อหา

Snapshot Notion 7 ตุลาคม 2026 ก่อน PRเอกสารปวริศช์ สถานะสดดู [Step3](https://app.notion.com/p/3f1cb2e9d47a81e28aa2dc642cd6ead6)

ผลจัดทำเอกสารรอบนี้ดู [matrix](step3-requirement-matrix.md) และ [Git audit](step3-git-audit.md) งานที่รอรีวิว/สมาชิกอื่น/public/releaseยังไม่ปิด


	**สถานะ: เริ่ม Final แล้ว — PR #22 merge งาน local regression/test preparation/AdminShell; ยังไม่ผ่าน public deployment หรือปิด Final** สร้างจาก Step 2 ที่ปิดแล้วและคำสั่งปวริศช์ ณ 2026-10-06 เริ่มทันทีและเร่งให้เสร็จเร็วที่สุด เป้าภายใน 2 วันจากนี้ถึง 2026-10-08 ไม่ใช้วันในแผนเป็นเหตุให้รอ และไม่ลดเกณฑ์ตรวจรับ
	ทุกคนรับงานขนานกันตามหน้าที่เดิม ปวริศช์ประสานการรวมและตรวจรับ หน้าใหม่นี้ไม่ใช้การติ๊กแทนการ implement/review/deploy

| เจ้าของ | งานหลัก | สิ่งส่งให้ทีม | Reviewer |
| --- | --- | --- | --- |
| ปวริศช์ | Architecture, Integration และชุดส่งงาน | Component Diagram, requirement matrix, SOLID/pattern docs, README, Git audit, slides และ release record | ศรัณย์: architecture/contracts; ศิระพัทธ์: หลักฐานและความครบถ้วน |
| ศิระพัทธ์ | Regression, Customer และ UI Quality | Final test plan/traceability, browser report/ภาพ responsive, Menu SOLID/JPA notes และ slides | ปวริศช์: E2E/traceability; ศรัณย์: API ที่เกี่ยวข้อง |
| ศรัณย์ | API, Serialization และ Order State | API audit/OpenAPI examples, State docs/diagrams, Order JPA/SOLID notes และ slides | ปวริศช์: integration; ศิระพัทธ์: tests/UI |
| ธีรเมธ | Production Deployment และ Billing/Payment | Production Docker/config, public URLs, Deployment Diagram, deploy/rollback runbook, Billing evidence และ slides | ปวริศช์: runtime/integration; ศิระพัทธ์: browser; ศรัณย์: API |
| เมธัส | Stock/Profile และ Database Tooling | Stock/Profile code+tests, forward migrations, JPA rationale, setup instructions, schema/docs และ slides | ศรัณย์: DTO/API; ศิระพัทธ์: UI/tests; ปวริศช์: migration/integration |
## 1. ปวริศช์ — Architecture, Integration และชุดส่งงาน
### ก่อนเริ่ม
- [ ] sync branch pavarit_673380278-9_01 จาก develop และตรวจ working tree ก่อนแก้
- [ ] อ่านใบงานวิชา Requirements/Course Audit และหลักฐานปิด Step 2; ใช้ Tasks เป็นรายการรับงาน
**อ้างอิง:** [Notion page](https://app.notion.com/p/3cfcb2e9d47a81ed9ad9d2abb8a174fd) · [Notion page](https://app.notion.com/p/3ddcb2e9d47a816bae8ccc995ca92d6a) · [Notion page](https://app.notion.com/p/3cfcb2e9d47a811ba912e01c6bcb449e) · [Notion page](https://app.notion.com/p/3e4cb2e9d47a81a0b8ced1cddb237ae7) · [Notion page](https://app.notion.com/p/822603f5de7247e68cfe7f50b378fe4f) · [รายงานล่าสุด](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/testing/pavarit-step2-completion-report.md)
### งานที่ต้องทำ
- [ ] ทำตารางข้อกำหนด → หลักฐาน → เจ้าของ → สถานะ จากใบงานรายวิชา ให้ครอบคลุมคะแนนและไฟล์ส่ง
- [ ] ทำ Component Diagram ของ modules/providers จริง โดยแสดง Controller → Service → Repository และขอบเขต Table/Session, Ordering, Fulfillment, Billing/Payment, Auth/Stock
- [ ] รวม [solid-analysis.md](../solid-analysis.md) ครบ S/O/L/I/D พร้อมไฟล์ บรรทัด และเหตุผล อ้าง commit รุ่นส่ง
- [ ] รวม [design-patterns.md](../design-patterns.md) ให้มี Pattern \| ปัญหาที่แก้ \| ไฟล์/คลาส \| Class Diagram ครบ Enterprise Patterns และ State/Strategy/Template Method
- [ ] ปรับ README ครบชื่อ/คำอธิบาย สมาชิกและหน้าที่ Stack Architecture ERD Setup/Run API Tests Deployment URL และ Project Structure
- [ ] ตรวจ Git ทั้ง 5 คน: branch ถูกชื่อ บัญชีผู้เขียน meaningful commits ≥5 ต่อคน การกระจายเวลา PR/reviewer และสิทธิ์ให้อาจารย์เข้าถึง; บันทึกข้อขาดจริง ไม่สร้าง commits เติมยอดหรือ push แทนกัน
- [ ] รวม slides ต้นฉบับและ PDF ใน doc/slide/ พร้อมลิงก์ภาพ/diagram ที่ใช้
- [ ] ประสาน release PR develop → main หลัง Final gates ผ่าน review แล้ว บันทึก merge commit/tag และ deployed commit ให้ตรงรุ่นส่ง
### เกณฑ์ผ่าน
- [ ] เอกสารและ requirement matrix ครบ พร้อมหลักฐานที่เปิดอ่านได้ ไม่มีรายการผ่านที่อ้าง design อย่างเดียว
- [ ] สมาชิกทั้ง 5 ยืนยันรายละเอียดส่วนของตนและอธิบายโค้ดได้
**ผลส่งมอบ:** Component Diagram, requirement matrix, SOLID/pattern docs, README, Git audit, slides และ release record
**Reviewer:** ศรัณย์: architecture/contracts; ศิระพัทธ์: หลักฐานและความครบถ้วน
**เป้าส่งต่อ:** เริ่มทันที ส่ง PR/ร่าง docs เมื่อพร้อมภายใน 24 ชั่วโมงแรก; review/deploy/evidence ทำคู่ขนาน; ยืนยัน release/ซ้อม/ชุดส่งภายใน 48 ชั่วโมง ไม่รอวัน Due
**หลักฐาน:** ยังไม่มีผล Final — แนบ PR / commit / tests / report / URL เมื่อทำจริง
## 2. ศิระพัทธ์ — Regression, Customer และ UI Quality
### ก่อนเริ่ม
- [x] sync branch sirapat_673380293-3_01; อ่าน PR #21 และรายงานจริงเพื่อแยก baseline ที่ผ่านจาก Final ที่ต้องตรวจใหม่
- [x] อ่าน test plan/traceability, UI Guide และ cookie Ordering Contract
**อ้างอิง:** [Test Plan](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/testing/test-plan.md) · [Traceability](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/testing/requirement-test-traceability.md) · [Notion page](https://app.notion.com/p/3ddcb2e9d47a81d28d17f0a23ccdf288) · [Ordering Contract](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/contracts/ordering-contract.md)
### งานที่ต้องทำ
- [x] ปรับ test plan/requirement traceability ให้รวม Stock/Profile และ public deployment พร้อม happy/error/permission cases
- [x] ตรวจ Customer ด้วย real HTTP/cookies บน H2 แยก: QR ใช้ครั้งเดียว สอง browser contexts แทนสองมือถือ เปลี่ยน QR ในแท็บเดิม StrictMode สั่งอาหาร ขอคิดบิล และดูสถานะชำระ — 14กลุ่ม Core Flow ผ่าน; ยังไม่ใช่มือถือจริงหรือ public acceptance
- [x] ตรวจ role ทั้ง 4 บน local runtime: เปิด URL ตรง ตัวพิมพ์/trailing slash และ protected API401 หลัง server-side invalidation ขณะ shell เปิดอยู่แล้วกลับ login; ตรวจ Staff/Kitchen UI logout แยก และ AdminShell logout ผ่าน component tests
- [ ] ตรวจ session หมดอายุตามเวลา (timed TTL) และ Secure-cookie/expiry บน public HTTPS จาก release ที่ deploy จริง
- [x] ตรวจ responsive ใน local baseline: Customer360px, Staff/Kitchen768px, Manager1280px พร้อม confirmation/กันกดซ้ำ; ภาพ Core Flow ล่าสุด17ภาพ ส่วน loading/empty/error12states เป็น controlled HTTP fixtures รอบก่อนที่ระบุว่า historical — public และ Stock/Profile ใหม่ต้องตรวจอีกครั้ง
- [ ] ตรวจ Stock/Profile UI ใหม่ใช้ tokens/components กลาง; แก้ defect ใน Customer/shared UI ที่พบและส่งเจ้าของ feature แก้ส่วนของตน
- [x] รวม automated/browser regression report ระบุ commit environment เวลา ผล จำนวน warnings/skipped และแยก fixtures ออกจาก Core Flow จริง
- [x] ส่งตัวอย่าง SOLID ของ Menu/Ordering, cascade/fetch ของ Menu/PackageMenuItem และสไลด์เรื่อง test strategy/QR/frontend quality
### เกณฑ์ผ่าน
- [ ] ไม่มี defect ที่ขัด flow หลักหรือสิทธิ์; tests รอบ release ล่าสุดมีหลักฐาน
- [ ] Browser regression บน public URL ผ่าน โดยแยก contexts/roles ไม่อาศัย hardcoded session IDs
**ผลส่งมอบ:** Final test plan/traceability, browser report/ภาพ responsive, Menu SOLID/JPA notes และ slides
**Reviewer:** ปวริศช์: E2E/traceability; ศรัณย์: API ที่เกี่ยวข้อง
**เป้าส่งต่อ:** เริ่มทันที ส่ง PR/ร่าง docs เมื่อพร้อมภายใน 24 ชั่วโมงแรก; review/deploy/evidence ทำคู่ขนาน; ยืนยัน release/ซ้อม/ชุดส่งภายใน 48 ชั่วโมง ไม่รอวัน Due
**หลักฐานอัปเดต:** 2026-10-07 — [PR #22](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/22) · [merge 472fba4](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/472fba4f25a27fa2e3cd1e1213151ce971646f3a) · [CI หลัง merge](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105); PavaritPramual และ sarunph-ctrl APPROVED head617d742 ก่อน merge เวลา00:27:27 Asia/Bangkok. CI ของ merge472fba4 ผ่าน backend/PostgreSQL303/303 ไม่มี skipped, frontend116/116, URLguards6/6, lint0errors/4warningsเดิมและbuildผ่าน. [รายงาน PR #22](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/doc/testing/sirapat-step3-premerge-report.md) · [Browser evidence/source hashes](https://github.com/PavaritPramual/buffet-restaurant-management-system/tree/472fba4/test/evidence/sirapat-step3-premerge-2026-10-06) — browser14PASS/H2แยกเป็นผลก่อนmergeที่มีpatch/hashesตรงhead617d742 ไม่ใช่ browser rerun หลังmerge. [Menu SOLID/JPA](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/doc/architecture/sirapat-menu-ordering-solid-jpa.md) · [PPTX](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/doc/slide/sirapat-step3-quality-premerge.pptx) · [PDF](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/doc/slide/sirapat-step3-quality-premerge.pdf). Stock/Profile/public/TTL/release ยังไม่ผ่าน Final; ข้อแนะนำลิงก์NotionและUX issue#23ยังต้องติดตาม
## 3. ศรัณย์ — API, Serialization และ Order State
### ก่อนเริ่ม
- [ ] sync branch sarun_673380515-1_02; อ่าน shared contract/API conventions และ State implementation ที่ merge แล้ว
- [ ] ดู Stock/Profile DTO ที่เสนอและ public environment ของธีรเมธก่อนตรวจร่วม
**อ้างอิง:** [API Conventions](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/contracts/api-conventions.md) · [Shared Contracts](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/contracts/shared-contracts.md) · [Notion page](https://app.notion.com/p/3ddcb2e9d47a81b3a69efedb2d438cf2) · [Notion page](https://app.notion.com/p/3d7cb2e9d47a803fbb26e1b7e5dbdf65) · [Order State](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/diagrams/order-fulfillment-state-diagram.md)
### งานที่ต้องทำ
- [ ] ตรวจ Stock/Profile DTO/API: field/type/validation/HTTP status/ErrorResponse ตรง frontend และ backend
- [ ] ปรับ Swagger schema/examples ตาม cookie flow, bill request, dueAmount/paidAmount และ Payment/close; ไม่เผย QR credential หรือข้อมูลภายใน
- [ ] ตรวจ timezone/date, enum และราคา JSON ให้ตรง contract
- [ ] อัปเดต State/Class/Sequence diagrams ของ Kitchen/Serving จากโค้ดและ role จริง; Order State ที่มีแล้วเป็น baseline ต้องตรวจ Final อีกครั้ง
- [ ] เขียน State Pattern: ปัญหาที่แก้ state/context/transition classes และ tests ของลำดับอนุญาต/ปฏิเสธ
- [ ] ตรวจ Kitchen/Serving บน public deployment RECEIVED → PREPARING → READY → SERVED และ role ผิดทำ transition ไม่ได้
- [ ] ส่ง cascade/fetch rationale ของ Order/OrderItem ตัวอย่าง SOLID และ slides API/serialization/State
### เกณฑ์ผ่าน
- [ ] Swagger/frontend/backend ใช้ contract เดียวกัน และ API ไม่คืน Entity หรือข้อมูลลับโดยไม่จำเป็น
- [ ] State transitions และสิทธิ์ผ่านพร้อม commit/public evidence
**ผลส่งมอบ:** API audit/OpenAPI examples, State docs/diagrams, Order JPA/SOLID notes และ slides
**Reviewer:** ปวริศช์: integration; ศิระพัทธ์: tests/UI
**เป้าส่งต่อ:** เริ่มทันที ส่ง PR/ร่าง docs เมื่อพร้อมภายใน 24 ชั่วโมงแรก; review/deploy/evidence ทำคู่ขนาน; ยืนยัน release/ซ้อม/ชุดส่งภายใน 48 ชั่วโมง ไม่รอวัน Due
**หลักฐาน:** ยังไม่มีผล Final — แนบ PR / commit / tests / report / URL เมื่อทำจริง
## 4. ธีรเมธ — Production Deployment และ Billing/Payment
### ก่อนเริ่ม
- [ ] sync branch teeramet_673380273-9_02; ตรวจ Dockerfile/Compose/provider settings ปัจจุบัน
- [ ] อ่าน deployment decision URL เดียวด้านล่าง; frontend Dockerfile เดิมเป็น Vite dev server ยังไม่ใช่ production
- [ ] เตรียม Render access และ env ส่วนตัว; ไม่ใส่ passwords/cookies/token ใน Tasks, Git หรือภาพหลักฐาน
**อ้างอิง:** [Notion page](https://app.notion.com/p/3ddcb2e9d47a8035a1b7ced6608331fa) · [Compose](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/docker-compose.yml) · [Billing/Payment evidence](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/billing/pr19-review-verification.md) · [Shared Contract](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/contracts/shared-contracts.md) · [Render Web Services](https://render.com/docs/web-services) · [Free limitations](https://render.com/docs/free) · [Supabase connection](https://supabase.com/docs/guides/database/connecting-to-postgres)
### งานที่ต้องทำ
- [ ] ทำ production multi-stage Docker build: build React แล้ว package static assets กับ Spring Boot ให้เว็บ/API/Swagger อยู่ Render Web Service URL เดียว; local Compose ยังแยกได้
- [ ] ใช้ VITE_API_BASE_URL=/api/v1 ใน production; React deep links refresh ได้ โดย API/Swagger/assets ไม่ถูก fallback เป็น HTML
- [ ] ผูก SERVER_PORT กับ PORT ของ Render; ตั้ง HTTPS/forwarded headers, Staff session cookie และ Customer HttpOnly cookie เป็น Secure, SameSite=Lax พร้อม allowed Origin ตรง public URL
- [ ] ตั้ง production providers: DiningSession/Fulfillment/MasterData/MenuAdmin=session, Ordering/BillingContext/PaymentStatus=database; runtime ไม่ใช้ fixture
- [ ] เก็บ secrets ใน Render environment ไม่ bake ลง image; ปิด bootstrap admin หลังเตรียมบัญชีและตรวจ logs/screenshots ไม่มี credentials
- [ ] เชื่อม Supabase Session Pooler 5432 SSL และตรวจ Flyway/JPA startup; deploy code ที่ต้องมี migration ใหม่หลังฐานกลางได้รับอนุมัติและ apply แล้วเท่านั้น
- [ ] ทำ Deployment Diagram และคู่มือ deploy/redeploy/rollback; rollback code ต้องเข้ากับ schema ใหม่ ห้ามย้อน migration ที่ apply แล้ว
- [ ] ตรวจ session หลัง restart: Staff login session ในหน่วยความจำอาจต้อง login ใหม่; ยืนยันพฤติกรรม Customer grant/ข้อมูลถาวรจริง ไม่อ้างว่าทุก session คงอยู่
- [ ] ตรวจ public Billing/Payment: snapshot price/backend amount ปิดก่อนจ่ายไม่ได้ จ่ายซ้ำไม่ได้ refresh พบ payment เดิม PAID ไม่ close อัตโนมัติ
- [ ] เขียน Strategy Pattern, Payment cascade/fetch/SOLID notes และ slides networking/runtime
- [ ] ทดลอง cold start ของ Render Free หลัง idle; บันทึกระยะรอและเปิดตรวจระบบก่อน demo มี local Compose/คลิปสำรองแต่ public deployment gate ยังต้องผ่าน
### เกณฑ์ผ่าน
- [ ] Public HTTPS ของเว็บ/API/Swagger เปิดได้ พร้อม cookies/login/QR/Core Flow จริง
- [ ] ข้อมูลถาวรยังอยู่หลัง redeploy; logs/startup/Flyway evidence ระบุ release commit ชัด
**ผลส่งมอบ:** Production Docker/config, public URLs, Deployment Diagram, deploy/rollback runbook, Billing evidence และ slides
**Reviewer:** ปวริศช์: runtime/integration; ศิระพัทธ์: browser; ศรัณย์: API
**เป้าส่งต่อ:** เริ่มทันที ส่ง PR/ร่าง docs เมื่อพร้อมภายใน 24 ชั่วโมงแรก; review/deploy/evidence ทำคู่ขนาน; ยืนยัน release/ซ้อม/ชุดส่งภายใน 48 ชั่วโมง ไม่รอวัน Due
**หลักฐาน:** ยังไม่มีผล Final — แนบ PR / commit / tests / report / URL เมื่อทำจริง
## 5. เมธัส — Stock/Profile และ Database Tooling
### ก่อนเริ่ม
- [x] sync branch methus_673380300-2_01; อ่านสอง Tasks Final และ canonical schema/ERD/UI Guide
- [x] ตรวจ history ล่าสุดและจองเลข forward migrations สองชุดจากเลขว่างจริงใน Tasks; ยังไม่ประกาศเลข V15/V16 โดยไม่ได้ตรวจ
- [x] ใช้ DB แยกสำหรับ tests; V1–V14 ที่ apply กลางแล้วห้ามแก้ และการอนุมัติ V13/V14 ไม่ครอบคลุม Final migrations ใหม่
**อ้างอิง:** [Notion page](https://app.notion.com/p/3f1cb2e9d47a815c9098ffc0e5ededd2) · [Notion page](https://app.notion.com/p/3f1cb2e9d47a8103ac1ef67c72c7b02e) · [Notion page](https://app.notion.com/p/3d8cb2e9d47a80488aa4dacb4ef5a008) · [Notion page](https://app.notion.com/p/3d8cb2e9d47a81d9aa4cdcec37b26c9d) · [Schema ที่รับรอง](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/database/step2-schema-approved.md) · [Auth/Stock delta](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/database/auth-stock-schema-delta.md) · [Notion page](https://app.notion.com/p/3ddcb2e9d47a81d28d17f0a23ccdf288)
### งานที่ต้องทำ
- [x] เพิ่ม Stock opening_target_stock และ active ผ่าน Flyway/JPA/DTO/API/UI; ใช้ DECIMAL(12,3) ตาม quantity ปัจจุบัน target ≥0/default0 และ active/defaultTRUE ทั้งรายการใหม่/เดิม
- [x] แสดงยอดเป้าหมายก่อนเปิดร้านและจำนวนขาด max(target − quantity,0); ไม่เปลี่ยน quantity/lowStockThreshold หรือสร้าง stock-in อัตโนมัติ
- [x] Manager ปิด/เปิดรายการได้; inactive ยังอ่านประวัติได้ แต่ stock-in/adjustment ต้องถูกปฏิเสธจน activate กลับ ทั้ง backend และ UI
- [x] เพิ่ม Profile firstName/lastName/phoneNumber ผ่าน User API/UI; first/last length100 phone20; phone จำกัดความยาวเท่านั้นเพราะยังไม่มีรูปแบบที่ยืนยัน
- [x] เก็บ display_name/email/sharedPK เดิม; first/last/phone ของข้อมูลเก่า nullable ไม่เดาแยกชื่อ; UI ใช้ชื่อเดิมพร้อมให้ Manager เติม ส่วนบัญชีใหม่ต้องมีชื่อ/นามสกุล
- [x] ทดสอบสิทธิ์ duplicate/validation inactive lifecycle ประวัติ Stock และข้อมูล Profile เก่า รวม Flyway/JPA บน H2/PostgreSQL แยก
- [ ] รวม JPA rationale ทุก Entity จากเจ้าของ: cardinality, FK/index, owner side, cascade/fetch และผลกับ query/ประวัติ; ไม่เปลี่ยน mapping เพียงเพื่อให้เอกสารดูดี
- [ ] ทำ setup/demo-data instructions ที่รันซ้ำได้ มีป้ายข้อมูลทดสอบ และไม่ลบข้อมูลทีม/เปิด bootstrap admin ค้าง
- [ ] เขียน Template Method: algorithm ขั้นร่วม/steps ที่ override และการรักษา transaction/audit พร้อม Auth/Stock SOLID/slides
- [ ] อัปเดต ERD/Data Dictionary/Auth-Stock delta กับ Notion จาก migration/contract ที่ review แล้ว โดยแยก planned กับ implemented
### เกณฑ์ผ่าน
- [ ] Stock/Profile ผ่าน UI/API จริงและมี tests/error cases; migration รักษาข้อมูลเดิม
- [ ] ไฟล์ migration ผ่าน review; apply ฐานกลางเมื่ออนุมัติ แล้ว readback/history/JPA ผ่านก่อนรับรอง
**ผลส่งมอบ:** Stock/Profile code+tests, forward migrations, JPA rationale, setup instructions, schema/docs และ slides
**Reviewer:** ศรัณย์: DTO/API; ศิระพัทธ์: UI/tests; ปวริศช์: migration/integration
**เป้าส่งต่อ:** เริ่มทันที ส่ง PR/ร่าง docs เมื่อพร้อมภายใน 24 ชั่วโมงแรก; review/deploy/evidence ทำคู่ขนาน; ยืนยัน release/ซ้อม/ชุดส่งภายใน 48 ชั่วโมง ไม่รอวัน Due
**หลักฐาน:** ยังไม่มีผล Final — แนบ PR / commit / tests / report / URL เมื่อทำจริง
## UI Design Lock — ใช้ baseline เดียวกัน
อ้างอิง [Notion page](https://app.notion.com/p/3ddcb2e9d47a81d28d17f0a23ccdf288) และ [tokens.css](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/code/frontend/src/styles/tokens.css)
- Noto Sans Thai น้ำหนัก400/500/600/700; primary #9A3412, hover #7C2D12, background #FFFDF9, surface #FFFFFF, text #1F2937
- Admin rail ใช้ tokens เดิม #173B34/#295449/#B3D982 ไม่สร้างชุด style ใหม่
- ใช้ Button/TextField/SelectField/StatusBadge/Card/PageHeader/LoadingState/EmptyState/ErrorAlert/ConfirmDialog/DataTable กลาง มี label/focus/disabled/loading/validation
- สถานะใช้ข้อความไทยร่วมกับสี ปุ่มสำคัญกดได้สะดวกและกัน submit ซ้ำ action destructive ยืนยันก่อน
- [ ] แต่ละ UI PR แนบภาพปกติ/loading/empty/error ตามความเหมาะสมที่360/768/1280pxและให้ศิระพัทธ์ review
## Deployment decision — ตกลงแล้ว แต่ยังไม่ implement
- Render Free Web Service เดียวผ่าน HTTPS; React production assets เสิร์ฟโดย Spring Boot เว็บ/API/Swagger ใช้ origin เดียว; API base /api/v1
- Local development คง frontend/backend แยกตาม Compose; production ไม่เปิด Vite dev server
- Staff Auth ใช้ server-side HTTP session cookie + BCrypt ตามโค้ดจริง; Customer ใช้ QR fragment → POST token body ครั้งเดียว → HttpOnly cookie/hash grants ไม่ใช้ JWT/Supabase Auth ในรอบนี้
- ตั้ง Secure cookie ทั้ง Staff/Customer, SameSite=Lax และตรวจ Origin ของ customer mutations ตาม contract; production providers ใช้ session/database จริง
- Spring Boot รองรับ static resources ([เอกสาร](https://docs.spring.io/spring-boot/3.5/reference/web/servlet.html#web.servlet.spring-mvc.static-content)); refresh React routes ต้องไม่ทับ API404/Swagger/assets
- Supabase เป็นฐานเดิมผ่าน Session Pooler5432 SSL ไม่มีฐาน Render ใหม่ ใช้ Flyway ของโปรเจกต์ ไม่ใช้ registry Supabase migrations แทน flyway_schema_history
- V1–V14 apply แล้ว ห้ามแก้ย้อนหลัง/repair ให้ผ่านเฉยๆ; Final migrations ต้องตรวจ DB แยก, review, อนุมัติฐานกลางแยก, apply/readback แล้วจึง deploy code ที่ต้องใช้ schema ใหม่
- JDBC ปัจจุบันใช้ postgres/BYPASSRLS; API ต้องตรวจ role/session ไม่อ้าง FORCE RLS จำกัด backend เก็บ client grants ที่ปิดแล้ว
- Render Free idle15นาทีแล้ว sleep และ cold start อาจรอประมาณ1นาที ([ข้อจำกัด](https://render.com/docs/free)); ตรวจ memory/startupจริง ไม่รับรอง resource ก่อนทดลอง
- ธีรเมธเสนอทางเลือกภายในวันแรกได้พร้อมเวลา/ค่าใช้จ่าย/ผลต่อcookieให้ปวริศช์ตัดสินก่อนเปลี่ยน ค่าเริ่มต้นยังเป็นRender Freeและไม่สมัครบริการเสียเงินเอง
## ลำดับเร่งงาน — เริ่มทันที เป้าภายใน 48 ชั่วโมง
ปวริศช์ต้องการทำให้เสร็จเร็วที่สุด เป้าหมายภายใน 2 วันนับจาก 2026-10-06 ถึง 2026-10-08 วันที่ใน Tasks เป็นเป้าติดตามงาน ไม่ใช่เหตุให้รอเริ่มหรือรอส่งรีวิว งานพร้อมก่อนให้ตรวจและส่งต่อทันที
| ช่วงจากตอนนี้ | ทำพร้อมกัน | ผลที่ต้องส่งต่อ |
| --- | --- | --- |
| ทันที–24 ชั่วโมงแรก | เมธัส Stock/Profile; ธีรเมธ production Docker/deploy setup; ปวริศช์ matrix/Component/SOLID/Git; ศิระพัทธ์ tests/UI; ศรัณย์ API/State; ทุกคนเขียน slides/JPA ของตน | โค้ดพร้อมแล้วเปิด PR ทันที reviewers ตรวจระหว่างงานอื่นดำเนินต่อ ร่างเอกสาร/สไลด์ส่งให้ผู้รวม ไม่รอ feature ทั้งหมดเสร็จ |
| 24–36 ชั่วโมง | แก้ review/รวม feature และ deploy setup; รับรอง/อนุมัติ/apply forward migrations ตาม gate; public Core Flow; เก็บ evidence และรวม docs/slides | ระบบ public ผ่าน flow/roles/Stock/Profile; ปิด defect ที่ขัด demo และตรวจ diagrams/contracts จาก commit รวม |
| 36–48 ชั่วโมง | Freeze features; regression release commit; ซ้อมทั้ง5คน; แก้เฉพาะ blocker; release PR develop→main ผ่าน reviewer และ public smoke | ชุดส่งครบ main/deployed commitตรงกัน URL/Swagger เปิดได้ ทีมอธิบายส่วนตนได้ และปิด Final เมื่อครบจริง |
**หลักการเร่ง:** งานเตรียม เอกสาร tests และ review ทำคู่ขนาน ทุกงานส่งต่อเมื่อพร้อม ใช้ dependency จริงเป็นจุดรอ รักษาเกณฑ์ tests/review/migration approval/public acceptance ทั้งหมด หากทำเสร็จก่อน 48 ชั่วโมงให้ตรวจรับและจบได้ทันที
## ใคร merge อะไรก่อน และต้องรออะไร
1. Stock/Profile กับ production setup ทำขนานและเปิด PR เข้า develop ได้; production setup ทดลองบน DB แยก/schemaเดิมได้ก่อน ไม่ต้องรอเขียน docs เสร็จ
2. Reviewer ตรวจตามพื้นที่ก่อน merge เจ้าของแก้ RC/test failures จนผ่าน; ถ้าแก้ API/schemaให้ส่ง owner reviewตรงส่วน
3. Public runtime ที่ต้องใช้ Stock/Profile ใหม่รอ PR featureรวมและ migrationกลางที่รับรอง/อนุมัติ/applyแล้ว; ไม่ให้ startup run migrationใหม่เงียบๆ
4. เอกสารเขียนขนานได้ แต่ final line references/ERD/Deployment/Swagger/report ต้องตรึงกับ commitจริงหลังรวม
5. Regression release + public Core Flowผ่าน แล้วเปิด develop → main PRพร้อม reviewerก่อนส่ง
- [ ] PR ภาษาไทยทุกชุดแนบปัญหา/behavior/tests/dependencies/breaking changesและ reviewerตามพื้นที่
- [ ] PR ทุกชุดผ่าน reviewก่อน merge ไม่มีการรวมเพราะตอบช้า และไม่ pushfeatureตรงmain
- [ ] หาก blockerกระทบกำหนดให้รายงานเจ้าของ/ผลกระทบ/สิ่งที่ต้องตัดสิน ไม่ติ๊กผ่านเพื่อให้ครบ
## Checklist ตรวจรับ Final — ภาพรวม
- [x] Automated regression ของ develop หลัง PR#22 merge472fba4 ผ่าน: backend/PostgreSQL303/303 รวมmigration/concurrency/security ไม่มี skipped; frontend116/116; URLguards6/6แยก; lint0errors/4warningsเดิมและbuildผ่าน — [PR #22](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/22) · [merge 472fba4](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/472fba4f25a27fa2e3cd1e1213151ce971646f3a) · [CI หลัง merge](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105)
- [ ] รัน Backend/PG/frontend/lint/build ซ้ำบน Final release ที่รวม Stock/Profile และ production setup แล้ว พร้อมบันทึก warnings/skipped; ผล472fba4เป็น baseline ระหว่าง Final
- [ ] Flyway checksum/JPA validate ผ่าน DBทดสอบใหม่และฐานกลางหลัง migrations ที่อนุมัติ พร้อม role/grants/readback ไม่มี secrets
- [ ] Public browser contexts Customer/Kitchen/Staff แยกกัน เปิดโต๊ะ→QR→Order→Kitchen→Serving→ขอคิดบิล→Payment→Closeสำเร็จ โต๊ะกลับAVAILABLE
- [ ] roleผิด cookieผิดรอบ QRซ้ำ จ่ายซ้ำ ปิดก่อนจ่าย และ Orderหลังbill-request/closeถูกปฏิเสธ
- [ ] Stock/Profile UI/APIจริง ข้อมูลเดิม/ประวัติยังอยู่ ไม่มีเดาแยกชื่อเก่าหรือแก้quantityตรงจากฟอร์มmaster data
- [ ] Diagramครบ UseCase+Description,Domain,Class+patterns,Sequenceอย่างน้อย3,Activity,ERD+Dictionary,Component,Deployment,State มีsourceและpreviewอ่านได้
- [ ] SOLIDทั้ง5, EnterprisePatternsและ Behavioral State/Strategy/TemplateMethodมีcode/file/line/เหตุผล/classdiagram
- [ ] READMEครบหัวข้อวิชา TestReportและcode/test/doc/img/doc-slideพร้อม ทุกลิงก์สำคัญเปิดได้
- [ ] Git5คน branch/account/meaningfulcommits≥5/เวลา/PR/reviewerตรวจจริง ไม่ยืนยันcountล่วงหน้า
- [ ] ทั้ง5คนซ้อมอธิบายโค้ดส่วนตนและdemoด้วยบัญชีจริง ไม่แก้DBด้วยมือระหว่างflow
- [ ] Release PRผ่านreviewและmerge main deployedcommitตรงรุ่นส่ง มีrelease record/tag
- [ ] PublicURL/Swaggerตรวจอีกครั้งก่อนส่ง พร้อมdemoscript/coldstartinstructions/คลิปหรือComposeสำรอง
## บันทึกหลักฐานและการส่งต่อ
| เจ้าของ | PR/commit | Tests/report/URL | Reviewer | สถานะFinal |
| --- | --- | --- | --- | --- |
| ปวริศช์ | ยังไม่มี — เติมเมื่อส่ง PR | ยังไม่มี — เติมหลักฐานจริง | ศรัณย์: architecture/contracts; ศิระพัทธ์: หลักฐานและความครบถ้วน | ยังไม่ได้ตรวจรับ |
| ศิระพัทธ์ | [PR #22](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/22) · [merge 472fba4](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/472fba4f25a27fa2e3cd1e1213151ce971646f3a) · [CI หลัง merge](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105) | [รายงาน PR #22](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/doc/testing/sirapat-step3-premerge-report.md); CI303/116และguards6ผ่าน; browser14กลุ่ม/H2แยกก่อนmerge,17ภาพ; SOLID/JPAและร่างPPTX/PDF6หน้าอยู่ในrepo | ปวริศช์และศรัณย์ APPROVED PR#22 ที่617d742 | local regression/test preparation/AdminShell ผ่านและmergeแล้ว; public/Stock/Profile/TTL/releaseยังค้าง |
| ศรัณย์ | ยังไม่มี — เติมเมื่อส่ง PR | ยังไม่มี — เติมหลักฐานจริง | ปวริศช์: integration; ศิระพัทธ์: tests/UI | ยังไม่ได้ตรวจรับ |
| ธีรเมธ | ยังไม่มี — เติมเมื่อส่ง PR | ยังไม่มี — เติมหลักฐานจริง | ปวริศช์: runtime/integration; ศิระพัทธ์: browser; ศรัณย์: API | ยังไม่ได้ตรวจรับ |
| เมธัส | ยังไม่มี — เติมเมื่อส่ง PR | ยังไม่มี — เติมหลักฐานจริง | ศรัณย์: DTO/API; ศิระพัทธ์: UI/tests; ปวริศช์: migration/integration | ยังไม่ได้ตรวจรับ |
ไฟล์หลักฐานต้องระบุ commit/environment/time/testdata และปิดบังpassword/token/cookie ใช้ข้อมูลทดสอบชื่อเฉพาะผ่านUI/APIไม่ลบข้อมูลทีม Gate releaseติ๊กเมื่อmainและpublicตรงกัน
## แหล่งอ้างอิง — ใช้ประกอบแต่ละการตัดสิน
### แผน ทีม และเกณฑ์ส่ง
- [Notion page](https://app.notion.com/p/3e4cb2e9d47a81a0b8ced1cddb237ae7) — หลักฐานเริ่มต้น/ขอบเขตที่เลื่อนจากStep2
- [Notion page](https://app.notion.com/p/822603f5de7247e68cfe7f50b378fe4f) — เจ้าของ สถานะ dependencies และdueของงานจริง
- [Notion page](https://app.notion.com/p/3cfcb2e9d47a81ed9ad9d2abb8a174fd) และ [Notion page](https://app.notion.com/p/3ddcb2e9d47a816bae8ccc995ca92d6a) — rubric/ไฟล์ส่ง/เกณฑ์ตรวจโค้ดกับGit ต้องปรับ auditจากหลักฐานจริงไม่ใช้สีเก่าเป็นสถานะปัจจุบัน
- [Notion page](https://app.notion.com/p/3cfcb2e9d47a813795c9fc40d62b9868) และ [Notion page](https://app.notion.com/p/3ddcb2e9d47a81be8ea8d3ffd65c9e7c) — branches หน้าที่และreviewers
- [Notion page](https://app.notion.com/p/3d3cb2e9d47a8137a529cbe248c73a3b) — แผนเดิมใช้เป็นประวัติ วันส่งเดิม3ต.ค.เป็นประวัติ งานปัจจุบันเร่งตามเป้า48ชั่วโมงและเกณฑ์ตรวจรับ
### Design และ Implementation
- [Notion page](https://app.notion.com/p/3cfcb2e9d47a8184ba3ce8a9fef721e1) — scopeและสิ่งที่ไม่ทำ
- [Notion page](https://app.notion.com/p/3cfcb2e9d47a811ba912e01c6bcb449e) และ [Notion page](https://app.notion.com/p/3d7cb2e9d47a807fbddfdc5810ac154a) — actors/domain/boundaries
- [Notion page](https://app.notion.com/p/3d8cb2e9d47a80488aa4dacb4ef5a008) และ [Notion page](https://app.notion.com/p/3d8cb2e9d47a81d9aa4cdcec37b26c9d) — fields/FKs/history/migration baseline
- [Notion page](https://app.notion.com/p/3d7cb2e9d47a803fbb26e1b7e5dbdf65) — QR/order/billing/payment/close sequences
- [Notion page](https://app.notion.com/p/3ddcb2e9d47a81b3a69efedb2d438cf2) — endpoints/screens/integration
- [Notion page](https://app.notion.com/p/3ddcb2e9d47a81d28d17f0a23ccdf288) — font/color/components/roleUIและresponsive
- [Notion page](https://app.notion.com/p/3ddcb2e9d47a8035a1b7ced6608331fa) — currentAuthและแผนdeploymentใหม่
### Repository contracts/schema/tests
- [Repository](https://github.com/PavaritPramual/buffet-restaurant-management-system) · [PR #21](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/21)
- [Shared Contracts](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/contracts/shared-contracts.md) · [API Conventions](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/contracts/api-conventions.md) · [Ordering Contract](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/contracts/ordering-contract.md) · [Customer Bill Request](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/contracts/customer-bill-request.md)
- [Canonical Dictionary](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/database/step2-schema-approved.md) · [Auth/Stock Delta](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/database/auth-stock-schema-delta.md) · [Menu/Ordering Delta](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/database/menu-ordering-schema-delta.md)
- [Diagram Sources](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/diagrams/README.md) · [System Design](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/system-design/README.md) · [Migration Files](https://github.com/PavaritPramual/buffet-restaurant-management-system/tree/develop/code/backend/src/main/resources/db/migration)
- [Test Plan](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/testing/test-plan.md) · [Traceability](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/testing/requirement-test-traceability.md) · [Regression Checklist](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/testing/regression-checklist.md)
- [Step2 Completion Evidence](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/testing/pavarit-step2-completion-report.md) · [Billing Review Evidence](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/develop/doc/billing/pr19-review-verification.md) · [CI](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions)
### Runtime/Deployment references
- [Render Web Services](https://render.com/docs/web-services) — PORT/Docker/publicURL
- [Render Free](https://render.com/docs/free) — sleep/restart/resource limitations
- [Spring Boot Static Content](https://docs.spring.io/spring-boot/3.5/reference/web/servlet.html#web.servlet.spring-mvc.static-content) — ReactassetsในSpringBoot
- [Supabase PostgreSQL Connections](https://supabase.com/docs/guides/database/connecting-to-postgres) — SessionPooler/SSL
- [Supabase Project Schemas](https://supabase.com/dashboard/project/zbflfljthmzqhshahemv/database/schemas) — ตรวจschemaจริง ห้ามถือว่าconnectorroleคือJDBCrole
- [Flyway Validate](https://documentation.red-gate.com/flyway/reference/commands/validate) — checksum/history gate
## Tasks ของ Step 3
งานเดิม8รายการเชื่อมหน้านี้ และเพิ่มรายการ productionsetup/regression/APIaudit/slides/release/migrationacceptance ที่แยกตรวจได้ โดยคงงานfeatureที่เสร็จStep2ไว้เสร็จ
## บอร์ดติดตาม Step 3 — 14 รายการ
| Task | เจ้าของ | กำหนดแผน | ตรวจรับ |
| --- | --- | --- | --- |
| [Notion page](https://app.notion.com/p/3ddcb2e9d47a812b9da3e3ae30c40580) | ปวริศช์ (Component) + ธีรเมธ (Deployment) + ศรัณย์ (State) | 2026-10-08 | ศรัณย์ (Component/API), ปวริศช์ (integration), ศิระพัทธ์ (ความครบ/หลักฐาน) |
| [Notion page](https://app.notion.com/p/3ddcb2e9d47a817f8721f9ec4a925d60) | ทุกคนส่งส่วนตน; ปวริศช์รวม SOLID/Patterns; ศิระพัทธ์รวม tests | 2026-10-08 | ศิระพัทธ์ (tests/traceability), ศรัณย์ (API), ปวริศช์ (SOLID/integration) |
| [Notion page](https://app.notion.com/p/3ddcb2e9d47a81669dd9f3667d8aacac) | เมธัสรวบรวม + Entity Owner ทั้ง5คนส่งส่วนตน | 2026-10-08 | ปวริศช์ (architecture/history), ศรัณย์ (serialization/query contracts) |
| [Notion page](https://app.notion.com/p/3ddcb2e9d47a81b885c7e3b50726e3a0) | ธีรเมธ (Deployment) + ปวริศช์ (integration) + ทุก Feature Owner | 2026-10-08 | ปวริศช์ (runtime), ศิระพัทธ์ (public browser), ศรัณย์ (API), เมธัส (migration) |
| [Notion page](https://app.notion.com/p/3ddcb2e9d47a8172854bd94f4047e7e1) | ปวริศช์ (audit) + ทุกคนยืนยันบัญชีและhistoryตัวเอง | 2026-10-07 | ศิระพัทธ์ (quality checklist), สมาชิกแต่ละคนยืนยันบัญชีตน |
| [Notion page](https://app.notion.com/p/3ddcb2e9d47a81c89956ed8d2664bb40) | ปวริศช์รวม + ทุกคนส่งdocs/slidesของตน | 2026-10-08 | ศิระพัทธ์ (หลักฐาน), ศรัณย์ (API/diagrams), ธีรเมธ (deployment), เมธัส (DB/setup) |
| [Notion page](https://app.notion.com/p/3f1cb2e9d47a8103ac1ef67c72c7b02e) | เมธัส | 2026-10-07 | ศรัณย์ (DTO/API), ศิระพัทธ์ (UI/tests), ปวริศช์ (migration/integration) |
| [Notion page](https://app.notion.com/p/3f1cb2e9d47a815c9098ffc0e5ededd2) | เมธัส | 2026-10-07 | ศรัณย์ (DTO/API), ศิระพัทธ์ (UI/tests), ปวริศช์ (migration/integration) |
| [Notion page](https://app.notion.com/p/3f1cb2e9d47a810ca825ca259b193dba) | ธีรเมธ | 2026-10-07 | ปวริศช์ (runtime/integration), ศิระพัทธ์ (browser), ศรัณย์ (API) |
| [Notion page](https://app.notion.com/p/3f1cb2e9d47a8118923ce11532cd3936) | ศิระพัทธ์คุม tests + ปวริศช์คุม E2E + ทุก Feature Owner | 2026-10-08 | ปวริศช์ (E2E/traceability), ศรัณย์ (API/State) |
| [Notion page](https://app.notion.com/p/3f1cb2e9d47a810781dfc9e7e0d15cd1) | ศรัณย์ | 2026-10-08 | ปวริศช์ (integration), ศิระพัทธ์ (tests/frontend) |
| [Notion page](https://app.notion.com/p/3f1cb2e9d47a812a8f01ff18ca3d3053) | ทุกคนเขียนส่วนตัวเอง; ปวริศช์รวม; ศิระพัทธ์ตรวจความครบ | 2026-10-08 | ศิระพัทธ์ (คุณภาพ/หลักฐาน), เจ้าของ Feature ยืนยันส่วนตน |
| [Notion page](https://app.notion.com/p/3f1cb2e9d47a81328a07c9d2031fad66) | ปวริศช์ (release coordinator) + ธีรเมธ (deployed commit) + ศิระพัทธ์ (gate) | 2026-10-08 | ศิระพัทธ์ (release quality), ศรัณย์ (contracts), ธีรเมธ (runtime) |
| [Notion page](https://app.notion.com/p/3f1cb2e9d47a8129898ad4544a9b31f0) | เมธัส (เลข/ไฟล์/validation) + ปวริศช์ (รับรอง/อนุมัติ) + ธีรเมธ (runtime) | 2026-10-08 | ปวริศช์ (schema/history), ศรัณย์ (contract), ศิระพัทธ์ (tests) |
สถานะตั้งต้นเมื่อสร้างแผน: งานเดิม8รายการมี2กำลังดำเนินการ/6ยังไม่ได้เริ่ม; งานใหม่6รายการยังไม่ได้เริ่ม. อัปเดต 2026-10-07 หลัง PR#22: Regression/Public Core Flow, JPA rationale และ Slides รายคน เริ่มมีผลส่งมอบส่วนศิระพัทธ์และเปลี่ยนเป็นกำลังดำเนินการ; Complete tests/SOLID/pattern documentation คงกำลังดำเนินการ. ยังไม่ปิด Tasks รวมจาก PR ย่อย เพราะรอเจ้าของอื่น/public/release. เริ่มทันที เป้าติดตาม7–8ต.ค.และปิดภายใน48ชั่วโมง ทำเสร็จก่อนให้ส่งตรวจทันที
