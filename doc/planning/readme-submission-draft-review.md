> รายงานส่วนเดิมด้านล่างเป็นประวัติการตรวจ Draft ก่อน PR #49 merge สถานะปัจจุบันอัปเดต 10 ตุลาคม 2026 ตามหัวข้อนี้

## ผลตรวจล่าสุดสำหรับ README Final

- [x] หน้าที่ทั้ง 5 คนระบุโมดูลที่เขียนโค้ดและหลักฐาน Layered Architecture/SOLID/Patterns/JPA/diagrams/tests ตามใบงานข้อ 12
- [x] เปลี่ยน baseline เป็น develop 855a954 หลัง PR #49/#52 merge และเชื่อมรายงาน UAT/Swagger ทั้งสองรุ่น
- [x] ใช้ ERD canonical ที่รวม V16–V19 และแก้ข้อความ V19 ที่ยังระบุไม่ apply ตาม startup evidence ของ PR #52
- [x] ตรวจ Swagger public หลัง deploy: เมนู/ออเดอร์ประกาศ 201 พร้อม Location, ErrorResponse กลางถูกแก้, Execute Stock ไม่ login 401 และ health UP
- [x] ลิงก์ใน README และ git diff --check ผ่าน
- [ ] README review/merge เข้า develop
- [ ] Release PR/main, owner Git audit และ Canva export/ซ้อมตามชุดส่งจริง

[ผลตรวจ public รอบนี้และขอบเขต](../testing/readme-final-public-check-2026-10-10.md) ส่วน checklist เดิมด้านล่างคงเป็นประวัติ ห้ามอ่านเป็นสถานะปัจจุบัน

---
# README ฉบับส่งงาน — draft review

ตรวจและจัดทำ 10 ตุลาคม 2026 โดยปวริศช์ เป็นฉบับร่างในเครื่อง ยังไม่ commit/push และไม่เปลี่ยนโค้ด/schema/deployment

## แหล่งตรวจ

- ใบงาน `ใบงานโปรเจค_ CP353002 Principles of Software Design and Development (Spring Boot).md` ใน workspace โดยอ่านข้อ 3–14 และยึดชื่อหัวข้อข้อ 10 ตามตัวอักษร
- Handoff README ที่ผู้ใช้แนบ รวมแนวทางรูปภาพ/หลักฐาน/9 นาที + Q&A 3 นาที
- Team ใน Notion ตรวจชื่อจริง รหัส Section และชื่อ branch ทั้งห้าคน พร้อมตรวจ remote branches
- develop ล่าสุด `c778150c79657b80930ceca6a4f10c136c1fc285`; main `ac72620d6451d075ea215172b538fb884fa780d6` ยังเป็น bootstrap
- โค้ด/config ของ branch `pavarit_673380278-9_01` head `c92a282` ซึ่งเพิ่มงาน PR #49 ต่อจาก develop; README แยกงาน pending นี้อย่างชัดเจน
- pom/package/lockfile, Compose และ Dockerfile.production, env examples และ application profiles
- SOLID/Pattern/JPA/Contracts, diagram index, test plan/traceability, Requirement Matrix/Git audit, production runbook, slide index และรายงาน UAT/PR #49
- Step FIX และ Step 3 ใน Notionมีข้อความประวัติหลาย revision ไม่ใช้ช่องติ๊กเก่ารับรอง release ล่าสุด

ไฟล์ `เกณฑ์การให้คะแนน.html` และ `CLO3-CP353002_มคอ3.docx` ใน handoff ไม่พบใน workspace ที่ตรวจ จึงไม่อ้างว่าได้เทียบเกณฑ์ในสองไฟล์นี้ครบ ใบงาน Markdown ที่มีเป็นเกณฑ์หลัก

## หัวข้อบังคับที่รักษาชื่อ

| ข้อ 10 ของใบงาน | หัวข้อใน README |
|---|---|
| ชื่อโปรเจค | Buffet Restaurant Management System ตรงชื่อระบบ/metadata ที่ตรวจ |
| สมาชิกกลุ่ม | สมาชิกกลุ่ม พร้อมคอลัมน์ ลำดับ / ชื่อ-นามสกุล / รหัสนักศึกษา / Section / Branch / หน้าที่รับผิดชอบ |
| Tech Stack | Tech Stack |
| System Architecture | System Architecture |
| Database Design (ER Diagram) | Database Design (ER Diagram) |
| Installation & Setup | Installation & Setup |
| How to Run | How to Run |
| API Documentation | API Documentation |
| How to Run Tests | How to Run Tests |
| Deployment URL | Deployment URL |
| Project Structure | Project Structure |

คงชื่อ `doc/` ตามโจทย์ ไม่เปลี่ยนเป็น `docs/` คง personal branch ของทั้งห้าคนตามรูปแบบ หัวข้อเสริม Overview/Features/Submission Index/Limitations ไม่แทนที่หัวข้อบังคับ และมี links diagrams ครบทุกชนิด

## การตรวจและขอบเขต

- เปลี่ยน URL หลักเป็น buffet-restaurant-management.onrender.com จาก URL เก่าใน README ไม่แก้ runbook รอบเก่าให้ดูเป็นหลักฐานปัจจุบัน
- แยก develop baseline, PR #49, applied-history ที่ตรวจไว้ และ public/deployed/release gates ไม่อ้างว่าการ merge/deploy/apply เกิดแล้ว
- คัดลอกภาพจริง PNG สี่ภาพและ Component/ERD SVG จาก Git blob ของ develop พร้อม provenance/hash ไม่มีภาพ UI ที่สร้างขึ้น พบ source ภาพ develop ยังมี metadata/schema V15 จึงระบุ limitation ใต้ภาพและให้ link ภาพที่แก้สำหรับ PR #49 ไม่อ้างว่า diagram เก่านี้ครอบคลุม V16–V18 ครบ
- Demo command เทียบ application-demo.yml/DemoDataSeeder และเพิ่ม empty config import; แยก fixture defaults จาก provider overrides และไม่ใช้ bootstrap ของ production ใน demo
- ทวนข้อกำหนด commands กับ scripts/config ที่มี คำสั่ง production/Compose ไม่ได้รันซ้ำเพื่อเขียน README เพราะ startup อาจ migrate DB ที่ตั้งไว้ Backend/frontend results อ้างรายงานและ CI ที่ระบุ ไม่อ้างว่า runtime suite รันใหม่รอบนี้
- Git commit ขั้นต่ำยัง 15 ต่อคน; ไม่คัดลอกตัวเลข audit เดือนก่อนเป็นยอดปัจจุบัน ไม่รับรอง meaningfulness/คะแนนจากจำนวน
- Slide 9+3 นาทีเป็นทิศทางใน handoff ล่าสุด ยังต้องปรับ Canva/export/ซ้อม; runbook 12 นาทีเดิมเก็บเป็นประวัติ

ผลตรวจไฟล์/หัวข้อ/ลิงก์/ภาพและ preview ดู `test/reports/readme-draft/` ในเครื่อง พร้อม [manifest ภาพ](../../img/readme/source-manifest.json) Preview เป็น local Markdown renderer ที่จัดรูปแบบใกล้ GitHub ไม่ใช่การรับรองว่า README ถูก publish แล้ว

## จุดที่ต้องรับรองก่อนรุ่นส่ง

- [ ] เจ้าของตรวจ README draft และรายการชื่อ/หน้าที่
- [ ] Refresh baseline หลัง PR #49 review/merge และ V19 ที่ได้รับอนุมัติ
- [ ] บันทึก deployed backend SHA และ public acceptance ของรุ่นส่ง
- [ ] Refresh Requirement Matrix/Git audit รายบุคคลและ review/merge gates
- [ ] ชุด Canva/PPTX/PDF รุ่นส่งและการซ้อมตามเวลา 9+3 นาที
- [ ] Release PR เข้า main และ commit/tag ตรงรุ่นที่ deploy

ไม่รวม README draft เข้า PR #49 โดยอัตโนมัติ เพราะ PR นั้นส่งรีวิวฟีเจอร์อยู่แล้ว ให้เจ้าของเลือกจังหวะส่ง README หลังตรวจฉบับร่าง
