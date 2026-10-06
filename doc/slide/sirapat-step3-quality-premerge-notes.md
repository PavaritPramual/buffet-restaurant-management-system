# ศิระพัทธ์ — สไลด์ Step 3 และ speaker notes

สถานะ: ร่างปรับก่อน merge PR #22, 6 ตุลาคม 2026; public release ยัง pending

หมายเหตุสำหรับการรวมชุด Final: ใช้ PowerPoint/PDF ชื่อ sirapat-step3-quality-premerge เป็นร่างล่าสุด ตัวเลข 303/303 และ116/116 เป็น CI ที่ตรวจยืนยันตามแหล่งใน slide4 ต้องตรวจ CI ของ head ใหม่ใน PR#22 ก่อน merge ร่างเดิม sirapat-step3-quality เป็น snapshot local รอบเริ่มงานและเก็บไว้เทียบ ยังต้องเติม release/publicURLs/ผลจริงในชุดรวม

## 1. ศิระพัทธ์ — การทดสอบและคุณภาพ UI

- Menu / Ordering · Customer QR · Shared UI
- Buffet Restaurant Management System
- Step 3 · 6 ตุลาคม 2026
- Local regression และ CI ผ่านแล้ว, public ยัง pending

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

## 4. Session และผล CI ก่อน merge

- ทั้ง 4 roles กลับหน้าเข้าสู่ระบบเมื่อ API ตอบ 401
- คำตอบ auth ที่มาช้าไม่เปิดข้อมูลเดิมกลับขึ้นมา
- CI: Backend 303/303, Frontend 116/116 ผ่าน
- Lint 0 errors / 4 warnings เดิม, Build ผ่าน
- Public same-origin HTTPS และ release regression ยัง pending

ผล CI ของ PR #22 ที่ยืนยันก่อนแก้ draft: head a351fbaf5878f3ad1d68ae3906bd63eb337ffe20, run37478993077, 6 ตุลาคม 2026 21:27-21:28 Asia/Bangkok Backend/PostgreSQL303/303 ไม่มี failures/errors/skipped Frontend116/116 ใน14files Lint0errors/4warningsเดิมและbuildผ่าน ตัวเลขนี้เป็น CI ไม่ใช่ผล local Maven274/27skippedของรอบแรก
Local browser รอบก่อน mergeผ่าน14กลุ่มด้วย real HTTP/cookies และ H2 แยก รวม Menu/category CRUDผ่านUIและ4rolesรับ protected API401 ขณะเปิดหน้าอยู่หลังserver-side invalidation แยก Staff/Kitchen UIlogoutอีกกลุ่ม ไม่มีHTTPmocks และไม่ได้จำลองเวลาหมดอายุตามTTL publicยังpending พร้อม6URLguardtests และCIตรวจguardก่อนbrowser/test-data startup
Source CI: https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37478993077
Sources: doc/testing/sirapat-step3-premerge-report.md; test/browser/step3-core-flow.cjs; step3-runtime-config.test.cjs; AdminShell.test.tsx
หลังแก้ต้องตรวจCIของPRheadใหม่อีกครั้งก่อนmerge โดยใช้linkล่าสุดในPR/report ไม่ใช้ผลCIเก่ารับรองheadใหม่

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
