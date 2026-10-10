# รายงานตรวจชุดส่ง Final — 10 ตุลาคม 2026

ผู้ตรวจ ปวริศช์ ประมวล ฐาน merged develop `097ad127d30f5a0923cffca00c89c5a2b8df1561` งานนี้แก้เฉพาะเอกสารและภาพ ไม่เปลี่ยน runtime/API/migration และไม่ apply/repair ฐานกลาง

## ผลตรวจ

| รายการ | ผลและหลักฐาน |
|---|---|
| โจทย์รายวิชาและชุดส่ง | [Checklist](../planning/final-submission-checklist.md) รวม README, code/test/doc/img, diagrams, SOLID/JPA/Patterns, Git และสไลด์; review/release/rehearsal แยกเป็น gate |
| Diagram | 28 PlantUML sources + 28 SVG; Maven validate ผ่าน Java21 ก่อน render; PlantUML1.2025.0 syntax/render ผ่าน ตรวจ SVG XML และภาพครบ รวม source/preview hashes ใน [manifest](../../test/evidence/final-submission-2026-10-10/diagram-manifest.json) |
| Design ล่าสุด | เติม V16–V18 archive/restore และ V19 recipe/snapshot ใน Use Case, Domain/Class/ERD/Stock Activity; Auth lifecycle เป็น service จริง ไม่สร้าง implementation สมมุติ; State/Strategy/Template Method และ layered dependencies คงเดิม |
| Canva | ชุดที่ปวริศช์เลือก [Canva](https://canva.link/43kx8nvyrmulyam) export จาก browser โดยตรง ไม่แก้เนื้อหา; PDF24หน้า/PPTX24slides/text snapshot พร้อม [manifest](../../test/evidence/final-submission-2026-10-10/slide-manifest.json) และ contact sheets3ชุด ตรวจ PDFทุกหน้า; ไม่อ้างว่า PPTX ผ่าน PowerPoint render แยก |
| Flyway กลาง | อ่าน history ของ project zbflfljthmzqhshahemv; V1–V19 success และ checksum เทียบ common/PostgreSQL files ตรง19/19 [ผล](../../test/evidence/final-submission-2026-10-10/flyway-checksums.json); V9 installed_rank11 เป็นประวัติจริง; connector role ไม่พิสูจน์ JDBC role |
| HTTP สาธารณะ | health, OpenAPI และ Swagger HTTP200 วันที่10ต.ค. [smoke](../../test/evidence/final-submission-2026-10-10/public-smoke.json); เป็น read-only smoke ไม่ใช่ UATใหม่ครบทุก operation |
| Git บน develop เท่านั้น | non-merge authors: ปวริศช์90 ศิระพัทธ์40 ศรัณย์30 ธีรเมธ22 เมธัส17 ไม่มี empty change; [CSV](../../test/evidence/final-submission-2026-10-10/develop-commits.csv) เก็บ paths/messages/coauthors แยก ชื่อและจำนวนไม่ใช้แทนความเข้าใจหรือคำรับรองของสมาชิก |
| ลิงก์และ diff | relative Markdown links ของไฟล์ที่เปลี่ยนไม่มีปลายทางหาย, git diff --check ผ่าน; code/ และ migration diff ว่าง |

## CI และหลักฐานหลาย revision

[CI run38045798712](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/38045798712) ของ head `e80bf860cf02023365501b0ac99c89e6aec1a878`: Backend431 failures0/errors0/skipped0, Frontend187, URL guards6, lint0errors/5warnings และ buildผ่าน ทั้งสอง jobs success ตัวเลขนี้มาจาก CI ที่ตรวจแล้ว ไม่ใช่ suite ที่รันใหม่ในงานเอกสารนี้

Public recipe UAT บน `af2b45b` อยู่ใน [รายงานเดิม](../../test/evidence/uat-buffet-2026-10-10/report.md) ส่วน [Swagger รอบ PR52](readme-final-public-check-2026-10-10.md) แยกตาม revision. จาก af2b45b ถึง develop097ad12 มี code เปลี่ยน6ไฟล์เฉพาะ Swagger response annotations/examples และ regression tests ไม่มี Service/Entity/transaction/lock/recipe logic เปลี่ยน จึงเก็บผล UATเดิมเป็นหลักฐานตามรุ่น ไม่เรียกว่าได้ทวนระบบทั้งหมดอีกครั้ง

## การรันซ้ำ

จาก repo root ใช้ Python ที่มี pypdf รัน `python test/evidence/final-submission-2026-10-10/verify.py` ตรวจ slide counts/checksums/diagram hashes/relative links โดยอ่าน history snapshot ไม่เชื่อม DB; ออกไฟล์ผลใน evidence folder. Maven `validate` และ PlantUML syntax/render เป็นการตรวจ local แยกจาก CI. ติดต่อฐานกลางในรอบนี้มี SELECT history อย่างเดียว

## Gate ที่ยังไม่ติ๊กแทนหลักฐาน

- [ ] PR เอกสารนี้ได้รับ review และ merge
- [ ] Release develop → main ผ่าน review และ merge
- [ ] บันทึก SHA รุ่นส่ง เทียบ runtime source กับ deployment และตรวจ URL ก่อนส่ง
- [ ] ทีมซ้อมอธิบายส่วนของตนและส่งลิงก์ตามช่องทางรายวิชา

เอกสาร/สไลด์เก่าเก็บไว้เป็นประวัติ ไม่ใช้ข้อความค้างจากรายงานเก่าปิด gate ใหม่ และไม่เปลี่ยนความหมายว่า public timed expiry หรือกล้องมือถือจริงผ่านจาก fixture/H2/CI
