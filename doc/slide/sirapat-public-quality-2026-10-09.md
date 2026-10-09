# QA slide content — ศิระพัทธ์, 9 October 2026

The user deferred Canva and requested separate files for manual insertion. Delivered an editable [three-slide PPTX](sirapat-qa-2026-10-09/sirapat-qa-v01.pptx) and [PDF](sirapat-qa-2026-10-09/sirapat-qa-v01.pdf), with complete speaker notes inside the PPTX and below. All three slides and PDF pages were visually inspected; [validation](../../test/evidence/sirapat-public-2026-10-09/slide-validation.json) records package/font/geometry/import checks. The canonical [Canva v02b draft](https://www.canva.com/d/yWw6P3disBOSfHS) was not edited. Use [current report](../testing/sirapat-public-regression-2026-10-09.md) for provenance; older deck counts are historical.

## Page A — ทดสอบหลายระดับ ก่อนยืนยันผล

**ข้อความบนสไลด์**
- Component/JUnit ตรวจ validation, role, QR retry และ race
- CI ใช้ PostgreSQL ที่สร้างแยกสำหรับทดสอบ
- เว็บ Render ใช้ cookie/API จริง ครบ 4 บทบาท
- เก็บผลตาม source และ environment ที่ตรวจจริง

**Speaker notes — ประมาณ 40 วินาที**

“เราแบ่งการตรวจตามสิ่งที่ต้องพิสูจน์ค่ะ Component และ JUnit ตรวจเงื่อนไข รวมถึงการตอบกลับช้า การสแกน QR ใหม่ และสิทธิ์ของแต่ละบทบาท ส่วน CI มี PostgreSQL แยกสำหรับ migration และ concurrency เว็บ Render ตรวจการทำงานจริงผ่าน HTTPS โดยสร้างบัญชีทดสอบตามที่ผู้ใช้อนุญาต และเดินครบตั้งแต่เปิดโต๊ะจนปิดโต๊ะ ผลแต่ละระดับมีหน้าที่ต่างกัน จึงบันทึก revision กับ environment แยกไว้เพื่อให้ตรวจย้อนกลับได้ค่ะ”

**Sources:** test-plan.md; requirement-test-traceability.md; baseline-ci.json; public api/results.json and browser/results.json. Owner ศิระพัทธ์; CI baseline d84f071; submitted production source 11edeb0.

## Page B — Core Flow และผลรอบปัจจุบัน

**ข้อความบนสไลด์**
- QR → RECEIVED → PREPARING → READY → SERVED
- ขอคิดบิล → ชำระ → ปิดโต๊ะ → ยกเลิกสิทธิ์ลูกค้า
- Public API 12 กลุ่ม / Browser 12 กลุ่ม ผ่าน
- CI PR #36: Backend352 / Frontend125 ผ่าน
- เพิ่ม30อาหาร/5หมวด; QR ส่งจริง6เมนูรวม8ชิ้น ครัวได้รับครบ

**Speaker notes — ประมาณ 50 วินาที**

“รอบวันที่ 9 ตุลาคม เราตรวจ API จริงผ่าน 12 กลุ่ม และใช้งานหน้าเว็บจริงอีก 12 กลุ่มค่ะ ลูกค้าเพิ่มจำนวนและยืนยันออเดอร์ ครัวเริ่มเตรียมจนพร้อมเสิร์ฟ พนักงานเสิร์ฟ และสถานะกลับมาที่ลูกค้าได้ หลังขอคิดบิล ระบบไม่รับออเดอร์เพิ่ม เราทดลองชำระและปิดโต๊ะ แล้วตรวจว่าสิทธิ์ลูกค้าถูกยกเลิกด้วย อีกส่วนตรวจ Stock และ Profile ว่าบันทึกแล้วโหลดกลับได้ ตามคำขอล่าสุดเราเพิ่มอาหารพร้อมสั่ง30รายการใน5หมวด แล้วใช้QRลูกค้าส่งจริง6เมนูรวม8ชิ้นเป็นออเดอร์3 ครัวได้รับครบและลูกค้าโหลดกลับมายังเห็นออเดอร์ หลังเสิร์ฟ ชำระ499บาทและปิดรอบ4แล้ว การแก้ข้อความ QR ภาษาไทยเพิ่ม 4 tests รวมเป็น 125 ที่ผ่านทั้งในเครื่องและ CI ของ PR #36 ส่วน backend ใน CI ผ่าน 352 tests ไม่มีการข้าม PostgreSQL ตัวเลข 12 เป็นกลุ่มสถานการณ์ ไม่ใช่จำนวน JUnit cases; รอบเมนูจำนวนมากเป็นหลักฐานเพิ่มเติม และ CI ยังไม่ใช่หลักฐานการ deploy ค่ะ”

**Sources:** current report executed-results table; API payment997.50; browser payment1247.50/session2; screenshots customer-paid-360 and staff-closed-available-768. Avoid adding these layer counts together.

**CI source:** [PR #36 run37876412024](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37876412024) ata43a558 passed352 backend/125 frontend. The PPTX notes additionally link this immutable run, the current report and volume observations. User will merge the separate files into the team deck.

## Page C — ข้อจำกัดและงานก่อน Final

**ข้อความบนสไลด์**
- Secure / HttpOnly / SameSite ตรวจจาก HTTPS จริง
- ลูกค้า 2 cookie contexts ตรวจที่ API; browser ใช้ 1 context
- QR ภาษาไทยแก้แล้วใน branch รอ review/redeploy
- ยังรอ timed TTL, live SHA/runtime/schema และทีมรับรอง

**Speaker notes — ประมาณ 45 วินาที**

“เราตรวจ attributes ของ cookie บน HTTPS จริงแล้วค่ะ และใช้ cookie jars สองชุดแยกกันสำหรับลูกค้าสองคนใน API แต่การตรวจหน้าเว็บใช้ browser context เดียวและจำลองขนาดจอ จึงยังไม่เรียกว่าใช้มือถือจริงสองเครื่อง ระหว่างตรวจพบข้อความ QR ที่แสดงภาษาอังกฤษ เราแก้ให้มีคำแนะนำภาษาไทยและทดสอบในเครื่องแล้ว แต่ยังต้อง review และ deploy การตั้งอายุ cookie 8 ชั่วโมงยังไม่ใช่ผลทดสอบหลังเวลาผ่านครบ 8 ชั่วโมง ส่วน live SHA, runtime และ schema ต้องมีเจ้าของยืนยัน ก่อนรับรอง Final และซ้อมนำเสนอทั้งทีมค่ะ”

**Sources:** cookieAttributes in API evidence; browser limits; local-fix/results.json; runtime owner's deployment record; current report remaining gates. Canva content/order/export still require ปวริศช์ and feature-owner review.

## Reviewer prompts

- Explain why bill/payment and explicit session close are separate operations.
- Show a rejected role/State transition and persisted unchanged state.
- Explain MenuItem category LAZY/no cascade, ElementCollection package membership, and aggregate-owned OrderItem lifecycle using [own SOLID/JPA note](../architecture/sirapat-menu-ordering-solid-jpa.md).
- Show the baseline/PR/deployed identity distinction. Do not call a passed CI run proof of a deployed release.
