# ศิระพัทธ์ — รายงานเริ่ม Step 3 วันที่ 6 ตุลาคม 2026

ผลล่าสุด: [ตรวจเพิ่มก่อน merge PR #22](sirapat-step3-premerge-report.md). รายงานนี้คงตัวเลข/ภาพของรอบเริ่มงานไว้ตามวันที่ทดสอบ.

สถานะ: ทำงานส่วนที่เริ่มได้และมีผลตรวจ local แล้ว **ยังไม่ผ่าน Final/public release**

รายงานนี้เก็บ snapshot รอบเริ่มงาน 20:37–20:58 เวลาไทย ตัวเลข 112/274 และภาพ 23 ภาพเป็นผลรอบนั้น ดู [รายงานตรวจซ้ำ](sirapat-step3-recheck-report.md) สำหรับผล 116 frontend, Core Flow 12 กลุ่ม และ concurrency fixtures 3 กลุ่มที่ตรวจเพิ่ม ไม่ใช้สไลด์ร่าง/ผล local แทน release acceptance

## ขอบเขตและฐานที่ใช้

อ่าน [Step 3](https://app.notion.com/p/37e90b8ff964834fad3701e2d8115de2), [Regression task](https://app.notion.com/p/1cc90b8ff96482af882481112591accd), UI Guide และ Stock/Profile tasks แล้ว ยืนยัน PR #21 merge อยู่จริง และ fast-forward สาขา `sirapat_673380293-3_01` จาก `9490a8c` ถึง develop `54e35383597f10ef1afb2e7e46ce8bbc1c0d5306` โดย working tree เริ่มต้นสะอาด

ผล Step 2 backend303/frontend109 เป็น historical baseline รอบนี้ทดสอบ base54e3538 พร้อม diff AdminShell ด้านล่างขณะยังไม่ commit จึงแนบ [tested frontend patch](../../test/evidence/sirapat-step3-2026-10-06/tested-frontend.patch) และ [source hashes/summary](../../test/evidence/sirapat-step3-2026-10-06/verification-summary.json) ไม่เรียกว่าผล release commit

## งานที่ทำ

- ปรับ [test plan](test-plan.md) และ [traceability](requirement-test-traceability.md) ให้ตรงระบบรวมจริงและเพิ่ม Stock target/active, Profile, public deployment, permission/error cases พร้อม owner/dependencies ไม่อ้างว่า feature ที่ยังไม่ implement ผ่านแล้ว
- แก้ AdminShell ของ Manager/Supervisor: เมื่อ protected API ตอบ401 ให้ซ่อนข้อมูลและกลับ login; ยกเลิกผล auth เก่าที่มาช้าด้วย revision guard; ป้องกัน login/logout ซ้ำระหว่าง pending และถ้า logout ล้มเหลวยังแสดง session/error ตามเดิม
- เพิ่ม regression 3 cases ที่ **ล้มเหลวก่อนแก้และผ่านหลังแก้**: Manager401, Supervisor401, restore response หลัง session expiry; ใช้ Axios interceptor จริงในสองเคสแรก
- เพิ่ม [Core Flow browser runner](../../test/browser/step3-core-flow.cjs) ใช้ real HTTP/cookie, contexts แยก, QR/IDs จาก runtime, explicit test accounts/env และ output ที่ไม่เก็บ credentials; public ต้อง HTTPS/test scope/deployed commit จากเจ้าของ runtime
- เพิ่ม [UI state fixture runner](../../test/browser/step3-ui-states.cjs) restricted loopback; แยก fixtures จาก flowจริง พร้อม [คู่มือ](../../test/README.md)
- ส่ง [Menu SOLID/JPA notes](../architecture/sirapat-menu-ordering-solid-jpa.md) อ้างโค้ดจริง: PackageMenuItem เป็น join-table ผ่าน ElementCollection, category LAZY/no cascade, SQL membership cascade แยกจาก JPA cascade, EntityGraph เฉพาะ Customer query; ไม่อ้าง N+1 benchmark ที่ยังไม่ได้วัด
- ส่ง [PowerPoint 6 สไลด์](../slide/sirapat-step3-quality.pptx), [PDF](../slide/sirapat-step3-quality.pdf) และ [speaker notes](../slide/sirapat-step3-quality-notes.md) สำหรับส่วนศิระพัทธ์ ให้ปวริศช์รวมชุดของทีม

## ผลตรวจ local รอบนี้

เวลาตาม Asia/Bangkok วันที่6ตุลาคม2026; source base+patch ตามข้างต้น ไม่ใช้ Supabase กลางและไม่ apply migrations ส่วนกลาง

| Check | Result | Environment / time / limitation |
|---|---|---|
| Backend Maven verify | **301 discovered, 274 passed, 27 skipped**, 0 failures/errors; buildผ่าน | Java21.0.6, H2/PostgreSQL-mode; รอบ20:37–20:38 ไม่มี configured disposable PostgreSQL/Docker daemon จึงข้าม PG/Testcontainers; ไม่แทนผล CI303เดิม |
| Frontend Vitest | **112/112**, 14 files | Node/Vitest5; รอบ20:45; baseline109 + regressionsใหม่3 |
| Focused AdminShell | **17/17** หลังแก้; ก่อนแก้เคสใหม่ fail3/pass14 | jsdom + actual Axios401 interceptor; ไม่ใช่ browser/server expiry |
| Lint | **0 errors / 4 warnings เดิม** | StockPage, UsersPage, KitchenBoardPage, StaffServingPage; ไม่เพิ่ม warningใหม่ |
| TypeScript/Vite build | ผ่าน | ตรวจหลังแก้โค้ดครบ; local build ไม่ใช่ deployed assets |
| Real browser Core Flow | **11 groupsผ่าน / 0failed** | Chrome145.0.7632.117; local H2; **20:46:41–20:47:14**; ไม่มี HTTP mocks; real session/database providers |
| Controlled UI states | **4 page groups × loading/empty/error = 12 statesผ่าน** | Customer360, Staff/Kitchen768, Manager1280; รอบ20:50; targeted HTTP fixtures; Customer grant/contextเป็นfixture |
| Responsive visual review | ตรวจภาพ Core Flow11 + state12 รวม **23ภาพ** | ทุกภาพที่สร้างตรวจแล้ว ไม่มี horizontal body overflow; Staff QR cardถูกmask; ไม่ครอบคลุม Stock/Profileใหม่ที่ยังไม่มี |
| Slides/PDF | 6หน้าผ่าน package/layout/import และตรวจภาพทุกหน้า | PPTX editable text; PDFเป็นstatic rendered-slide export; ไม่อ้างว่าเปิดตรวจใน native PowerPoint ซึ่ง COM session ใช้ไม่ได้ |

หลักฐาน browser: [Core Flow results](../../test/evidence/sirapat-step3-2026-10-06/local-core-flow/results.json) · [State fixture results](../../test/evidence/sirapat-step3-2026-10-06/ui-state-fixtures/results.json)

### Core Flow ที่ตรวจจริง

1. Login ทั้ง4role ผ่าน UI/cookieคนละcontext ตรวจ incoming/ready/stock permission, anonymous และ spoofed X-User-Role
2. Supervisor/Staff/Kitchen เปิด `/ADMIN/MENU/` ไม่ได้; `/KITCHEN/` ให้ Kitchenเข้าและ Staffกลับโต๊ะ
3. Managerสร้าง Package/Soup/Tableผ่านUI; setup Menuใช้existing domain APIซึ่งไม่ได้ใช้เป็นหลักฐานMenu CRUD UI
4. Staffเปิดรอบจากโต๊ะที่สร้างจริง; ปิดก่อนชำระถูกปฏิเสธและรอบยังACTIVE
5. Phone AแลกQRครั้งเดียว; QRเดิมใช้ซ้ำ404; Phone BแลกQRที่refreshแล้วได้รอบเดียวกัน; anonymousใช้sessionIDอย่างเดียว401
6. Phone AยืนยันOrderหนึ่งPOST → Kitchenเตรียม/พร้อม → Staffเสิร์ฟ ตรวจ persisted SERVED
7. Phone Aขอคิดบิล → ทั้งสองมือถือเห็นREQUESTEDและปุ่มOrderdisabled; POSTOrderของทั้งสองcontextได้409
8. Staffชำระ997.50บาท; ทั้งสองมือถือเห็นPAID โดยtotal997.50/due0/paid997.50 และDiningSessionยังACTIVE
9. Staffยืนยันcloseแยก → โต๊ะAVAILABLE; credentialsทั้งสองมือถือใช้Ordersต่อไม่ได้
10. Logout Managerจากserverแล้วกดStockทำให้UIรับ401และกลับlogin; Kitchenlogoutแล้วprotectedAPI401
11. Swagger200 และมี bill-request/bill-status routes

QR StrictMode/same-tab rescan/remount/stale-response covered by current automated tests; ไม่ได้รัน controlled-delay browser concurrency ใหม่รอบนี้ และไม่อ้าง multi-tab synchronization หรือ physical phone testing

## งานที่ยังรอและเกณฑ์ตรวจรับ

- **เมธัส:** Stock target/active และ Profile first/last/phone พร้อม DTO/migrations/UI; ศิระพัทธ์ตรวจ UI/tests หลัง owner ส่ง code ตามcontractที่reviewแล้ว
- **ธีรเมธ:** publicHTTPS URL, test accounts/test scope, deployed commit และยืนยัน real providers/schema; เมื่อพร้อมให้รันpublicCoreFlowและStock/Profileร่วมกับทีม
- **PostgreSQL/CI:** [CI ของ head e1c78f8](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37475748916) ผ่าน backend/PostgreSQL 303/303, 0 skipped และ frontend112/112 พร้อม lint/build แล้ว; 27skippedข้างบนเป็นผล local เท่านั้น ต้องตรวจ CI ของ PR head ใหม่และ release/migrations รุ่นสุดท้ายอีกครั้ง
- **ปวริศช์/ศรัณย์:** reviewer E2E/traceability/API, requirement matrix/docsรวม; main/release/deployed revisionต้องตรงกัน
- **Slides:** รวมทั้ง5คนและPDFชุดส่ง ซ้อมด้วยบัญชี/QRจริง ตัวเลขpublic/URLsเติมเมื่อมีผลจริง

ไม่มีการติ๊กFinalผ่านจากผลlocal ไม่มีการmerge/release/deployเอง และไม่ส่งข้อความแทนผู้ใช้หาสมาชิกทีม
