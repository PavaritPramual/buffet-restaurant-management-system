# Manager force actions — 9 ตุลาคม 2026

เพิ่มตามคำขอให้ Manager บังคับปิดโต๊ะค้างและบังคับลบเมนูที่มี history ได้ เริ่มจาก `origin/develop` ที่ `a6da906` หลัง PR #36 merge แล้ว ไม่แก้ migration เก่าและไม่เปลี่ยนข้อมูลร้านบน Render ระหว่างทดสอบคำสั่งนี้

## วิธีใช้งานหลัง merge และ deploy

1. เข้าบัญชี Manager แล้วเลือก **จัดการรอบกิน → บังคับปิดโต๊ะ** ใส่เหตุผลและยืนยัน รอบกินจะเป็น CANCELLED โต๊ะว่าง QR เดิมใช้ไม่ได้ งานจากรอบนั้นหยุดแสดงในครัว/เสิร์ฟ ประวัติยังอยู่ และไม่สร้างรายการชำระเงินขึ้นมาเอง
2. เลือก **เมนูอาหาร → บังคับลบ** ที่แถวเมนู ใส่เหตุผลและยืนยัน เมนูหายจากรายการจัดการและหน้าลูกค้า แม้เคยถูกสั่งแล้ว ชื่อและจำนวนในออเดอร์เก่ายังคงอยู่
3. ตรวจชื่อผู้ดำเนินการ เวลา และเหตุผลได้ที่ประวัติการบังคับจัดการในหน้าจัดการรอบกิน

คำสั่งนี้จำกัด Manager เท่านั้น ปุ่มลบปกติและ Service Staff close ปกติยังใช้กฎเดิม ไม่มีการลบประวัติออเดอร์หรือบันทึกว่าได้รับเงินทั้งที่ยังไม่จ่าย

## Automated evidence ในเครื่อง

| การตรวจ | ผล |
| --- | --- |
| Backend `mvn -B verify` | 362 discovered: 330 passed, 32 skipped, 0 failures/errors; build success |
| Manager integration หลังเพิ่ม unused-QR assertion | 8/8 passed, 0 skips |
| Frontend full suite | 130/130 passed, 15 files |
| Affected UI suites after final behavior adjustments | 15/15 passed, 2 files; temporary sandbox cache error resolved by rerun with one worker |
| Frontend lint / TypeScript / production build | Passed; 0 lint errors, 4 existing warnings |
| Public-runtime URL guard tests | 6/6 passed |

32 backend skips เป็น PostgreSQL/environment-gated และ Docker/Testcontainers tests ซึ่งเครื่องนี้ไม่มี runtime ที่พร้อม ไม่ใช่หลักฐานว่า PostgreSQL ผ่าน เพิ่ม PostgreSQL race tests อีก 4 กรณีให้ CI ตรวจ Order↔Force Close และ Order↔Force Delete ทั้งสองลำดับ พร้อมตรวจ V16/V17 และ RLS/sequence privileges ใน migration suite ผล CI ของ PR ต้องอ่านแยกจากผล local นี้

Integration tests ตรวจ Manager/anonymous/อีก 3 roles, role-header spoofing, required/blank/oversize reasons, duplicate attempts, unpaid/paid history, old cookie + unused QR revocation, table reopen, Kitchen/serving exclusion, rejected fulfillment, hidden menu IDs/lists, stale cart, reactivation rejection และการเพิ่มเมนูชื่อเดิมด้วย ID ใหม่

## Real browser evidence ในเครื่อง

ใช้ CUA browser กับ backend ที่ build จากงานนี้และ Vite โดยไม่ mock HTTP ฐานข้อมูล H2 ใหม่ในหน่วยความจำ ปิด `.env` import และ seed shared data เตรียมบัญชี 4 roles, package 499, 1 table และ 6 foods ผ่าน native API ด้วยรหัสสุ่มที่อยู่เฉพาะ ignored local setup file

ลูกค้าเปิด QR และส่งออเดอร์จริง #1: กุ้ง ×1 + หมูสไลซ์ ×2 จากหน้าจอ จากนั้น Manager ลบหมูตามปกติไม่ได้เพราะ history แต่บังคับลบพร้อมเหตุผลได้ รายการลดจาก 6 เหลือ 5 ออเดอร์เก่ายังมีหมู ×2 หลัง reload ตะกร้าที่เปิดไว้ก่อนลบถูกปฏิเสธและไม่เกิดออเดอร์ใหม่

Manager บังคับปิดรอบ #1 ที่ยังค้าง 499 บาทผ่านหน้าจอพร้อมเหตุผล ลูกค้า reload แล้วเห็นว่าหมดสิทธิ์ ตรวจ API แยกได้ CANCELLED, AVAILABLE, payment 404, unused QR 404, incoming orders ว่าง และ advance order 400 จากนั้น Service Staff เปิดโต๊ะเดิมเป็นรอบ #2 ACTIVE ได้ ประวัติบังคับลบ/ปิดยังอยู่หลัง reload

- [Manager: reopened table and retained operation history](../../test/evidence/manager-force-2026-10-09/manager-history-after-reload.png)
- [Menu removed from catalog](../../test/evidence/manager-force-2026-10-09/menu-removed.png)
- [Customer menu excludes deleted food; historical order retains it](../../test/evidence/manager-force-2026-10-09/customer-history-retained.png)
- [Force-close confirmation](../../test/evidence/manager-force-2026-10-09/force-close-confirmation.png)
- [Required-reason dialog](../../test/evidence/manager-force-2026-10-09/force-close-required-reason.png)
- [Customer credential revoked](../../test/evidence/manager-force-2026-10-09/customer-session-revoked.png)
- [Native API assertions, distinct from browser actions](../../test/evidence/manager-force-2026-10-09/local-api-assertions.json)

ภาพที่ยืนยันได้เป็น desktop viewport 1265px ไม่มี document overflow การสั่ง viewport 360px ใน browser tool ยังรายงานและ capture ขนาดเดิม จึง **ไม่รับรอง mobile visual QA** จากครั้งนี้ และได้ reset override แล้ว

## Release gates

ผล local และ CI ไม่ใช่ public acceptance ของฟีเจอร์ใหม่ รอ reviewer ของ shared entities/API และ migration/RLS, merge เข้า develop, deploy backend/schema แล้ว frontend และทดสอบปุ่มทั้งสองบน Render ด้วยข้อมูล QA ที่ระบุชัด ก่อนนับว่าเว็บสาธารณะใช้ได้

อ่าน [API/behavior/migration contract](../contracts/manager-force-actions.md) สำหรับขอบเขตผลกระทบ การเก็บ history และ rollback
