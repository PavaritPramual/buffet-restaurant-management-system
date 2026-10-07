# สไลด์ทีมปัจจุบัน — Canva v02

[เปิด Canva v02](https://www.canva.com/d/wORhypcIcwvGuFK) · design `DAHXSWQA-Vs` · [เนื้อหาและ speaker notes](team-final-canva-v02-content.md)

## ช่วงนำเสนอ 12 นาที

ใช้ **หน้า1–20** พูดเนื้อหา10นาที50วินาที เผื่อส่งต่อคนและเวลาคลาดเคลื่อน1นาที10วินาที ส่วน **หน้า21–76 เป็นภาคผนวก** สำหรับตอบคำถาม ไม่อ่าน76หน้าบนเวที ดู [runbookพร้อมผู้พูด/เวลารายหน้า](team-final-12-minute-runbook.md)

เรียงตามโจทย์ UseCase/Architecture/Data, SOLIDทั้ง5, State/Strategy/TemplateMethod, QR/Billing integration, Tests และDemo ทุกหน้ามีผู้บรรยายหนึ่งคนระบุชื่อbranchจริง ส่วนสถานะที่ยังไม่เสร็จแยกไปภาคผนวก66

## ภาพและโค้ด

- มี23diagramรายละเอียดตามใบงานข้อ9.1ครบ พร้อม5ภาพย่อสำหรับอ่านบนจอ ภาพเต็มเก็บท้ายชุดและมีsource/SVG
- UseCaseรวมทั้ง5actorsพร้อมDescription Domain/Classพร้อมตำแหน่งPatterns Sequence4scenarios Activity ERD Component Deployment และState ดู [coverageพร้อมเลขหน้า](course-diagram-coverage.md)
- มี35หน้าที่แสดงcodeจากdevelop `472fba4` พร้อมไฟล์/บรรทัดในnotes ครอบคลุมSOLID Enterprise/BehavioralPatterns API/transaction/DTO/security และtestcases
- ใช้SarabunมีหัวกับJetBrainsMonoสำหรับcode พื้นครีม/navy/terracotta/gold มีsyntaxhighlightและภาพแทนข้อความยาว ลดcolon/ลูกศรในproseโดยคงsyntaxของcode/UML
- Gitขั้นต่ำปัจจุบันคือ5meaningfulcommitsต่อคนตามที่ปวริศช์แจ้งว่าอาจารย์ปรับ ดู [บันทึกเกณฑ์](../planning/course-criteria-updates.md) ไม่แก้ใบงานต้นฉบับที่ยังเขียน15

## ผลตรวจและข้อที่ยังรอ

[หลักฐานv02](../../test/evidence/pavarit-step3-docs-2026-10-07/canva-v02-verification.json) นำเข้า76หน้า อ่านกลับtitle/points/branch/code/notesตรงร่างทุกหน้า พบข้อความแก้ได้704องค์ประกอบและภาพdiagram28องค์ประกอบ ไม่มีองค์ประกอบออกนอกcanvas1920×1080 ภาพdiagramเป็นSVGที่นำเข้า ไม่อ้างว่าnodeภายในแต่ละdiagramเป็นnativeCanvashapeที่แก้แยกกันได้ ให้แก้จากPlantUMLsourceแล้วเปลี่ยนภาพเมื่อdesignเปลี่ยน

ตรวจภาพlocalทั้ง76หน้าและgeometryแล้ว ฟอนต์localโหลดครบ ยังต้องownerตรวจภาพในCanvaและซ้อมทั้ง5คน ไม่อ้างว่าCanvavisualQAทุกหน้าหรือpublic/releaseผ่าน งานนี้ไม่แก้runtimeและไม่รันmigration/testsบนSupabase

Canvaเป็นต้นฉบับและเครื่องมือพรีเซนต์ **ไม่ใช้PPTXเป็นชุดส่งหรือขอreview** เครื่องมือที่เชื่อมไม่มีexportPDF จึงยังไม่มีPDFv02จากCanva เมื่อownerตรวจแล้วให้แชร์/ดาวน์โหลด/PDFมาตรฐาน/ทุกหน้า บันทึก `doc/slide/team-final-v02.pdf` พร้อมวันที่/commit/changelog ไม่เรียกPDFที่สร้างจากlocalHTMLว่าCanvaexport

| รุ่น | วันที่ | ต้นฉบับ | สถานะ |
|---|---|---|---|
| v02 | 2026-10-07 | [Canva](https://www.canva.com/d/wORhypcIcwvGuFK) / [content](team-final-canva-v02-content.md) | 20หลัก+56ภาคผนวก; source/localQA/Canvareadbackผ่าน รอowner/CanvavisualQA/PDF/rehearsal/release |
| v01 | 2026-10-07 | [Canva](https://www.canva.com/d/D0dhcGCjuTTS7pV) | 40หน้ารุ่นเก่า เก็บเป็นประวัติ ไม่ใช้เป็นรุ่นปัจจุบัน |

---

# ประวัติรุ่น v01 — Canva

## แนวทางก่อนปรับ v02 (ประวัติ)

ผู้ใช้ยืนยันวันที่ 7 ตุลาคม 2026 ให้ทำสไลด์ **ทั้งทีม** ใน **Canva** เป็นต้นฉบับและเครื่องมือพรีเซนต์ เมื่อรุ่นหนึ่งตรวจแล้วให้ export PDF **จาก Canva** เก็บในโฟลเดอร์นี้พร้อมเลขเวอร์ชัน วันที่ และ changelog

**ไม่ใช้ PPTX เป็นชุดนำเสนอหรือชุดส่ง Reviewer ไม่ต้องตรวจ PPTX รวมถึง PDF/notes ที่สร้างจาก PPTX ร่างเดิม** ไฟล์รายคนเดิมคงไว้เป็นประวัติ ไม่ใช่หลักฐานว่าสไลด์ทีม Final ผ่านแล้ว

**ต้นฉบับรุ่นก่อน v01:** [เปิด Canva — Team Final v01](https://www.canva.com/d/D0dhcGCjuTTS7pV) · Design ID `DAHXQy_ZUvQ` · 40 หน้า ขนาด 1920×1080

พื้นที่ Canva ที่ปวริศช์ให้เดิม: [Posd](https://canva.link/hwrws57kjwgvafx) — ตรวจพบหนึ่งหน้าว่างก่อนเริ่มงาน จึงสร้างชุดทีมเป็น design ใหม่ ไม่แก้หน้าว่างเดิม

## ชุด v01 ที่เตรียม

[เนื้อหาชุดทีม 40 หน้า](team-final-canva-content.md): **32 หน้าหลัก + 8 หน้าภาคผนวก** พร้อมข้อความบนสไลด์ speaker notes แหล่งอ้างอิง และผู้รับช่วงทั้ง5คน

[ผลตรวจเนื้อหาและการนำเข้า](../../test/evidence/pavarit-step3-docs-2026-10-07/canva-content-verification.json): 40 notes / 5 owners / 107 references ตรวจ source 60 ไฟล์ที่ commit อ้างอิง พร้อมจำนวน local Markdown links ที่ตรวจใน JSON อ่านกลับจาก Canva พบข้อความทุกหน้าครบ คำพูดประกอบตรงร่างทั้ง 40 หน้า และมีองค์ประกอบข้อความแก้ไขได้ 535 รายการ

| ช่วง | เรื่อง / ผู้รับช่วงหลัก |
|---|---|
| 1–7 | ปัญหา scope บทบาท สมาชิก Core Flow Stack และ Architecture — ปวริศช์ |
| 8–10 | ERD/JPA/history — เมธัส; เปิดSession — ปวริศช์ |
| 11–15 | QR/Menu/Ordering — ศิระพัทธ์; Kitchen/Serving/State/API — ศรัณย์ |
| 16–18 | Bill request/Strategy/Payment/close — ธีรเมธ |
| 19–22 | Auth/Stock/Template Method — เมธัส; UI — ศิระพัทธ์ |
| 23–26 | Enterprise Patterns และ SOLIDทั้ง5 — ปวริศช์ร่วมowner |
| 27–30 | Test strategy/cases/PG concurrency/ผลและข้อจำกัด — ศิระพัทธ์ร่วมปวริศช์ |
| 31–32 | Runtime/deployment — ธีรเมธ; demo/ชุดส่ง — ปวริศช์ |
| 33–40 | ภาคผนวก API/codegaps/priceexample/stock/schema/races/release/sources — เจ้าของแต่ละส่วน |

หน้าที่ในร่างเป็นผู้รับช่วงอธิบาย **ไม่ใช่การยืนยันว่าเพื่อนตรวจหรือส่งงานแล้ว** แต่ละคนต้องตรวจและอธิบายโค้ดส่วนตนก่อนส่งจริง

## สถานะการส่งเข้า Canva

ผู้ใช้อนุมัติให้ส่งร่างเข้า Canva แล้ว นำเข้าสำเร็จเป็นชุดทีม v01 จำนวน 40 หน้า อ่านกลับยืนยันข้อความและ notes ตรงร่างทุกหน้า องค์ประกอบข้อความทั้งหมดอยู่ภายในขนาดสไลด์ และข้อความแก้ไขได้จริง ไม่ใช่ภาพแบนทั้งหน้า

ยังต้องตรวจภาพทุกหน้าใน Canva และให้เจ้าของยืนยันเนื้อหา ภาพ preview ถูกส่งให้ผู้ใช้ แต่เครื่องมือตรวจภาพไม่สามารถเปิด URL ภาพที่ Canva ส่งคืนได้ จึงไม่อ้างว่าผ่าน visual QA ทั้งชุด เครื่องมือ Canva ที่เชื่อมอยู่ไม่มีคำสั่ง export PDF จึงยังไม่มี PDF รุ่นทีมที่ export จาก Canva

ก่อนเก็บ PDF ให้เจ้าของตรวจ Canva แล้วเลือก **แชร์ → ดาวน์โหลด → PDF มาตรฐาน → ทุกหน้า** บันทึกเป็น `doc/slide/team-final-v01.pdf` พร้อมวันที่/commit ในตารางรุ่น การได้ PDF ร่างไม่ใช่การรับรอง Final; ต้องเติมผล public/release และซ้อมจริงก่อนส่ง

## รายละเอียดเรื่อง tests

ใบงานวิชาที่ให้ใน workspace ข้อ2ระบุเทคโนโลยี Testing ข้อ10กำหนด README How to Run Tests และข้อ14กำหนด testsผ่านพร้อมTest Report และเก็บslidesในdoc/slide/ แต่ไม่ได้กำหนดจำนวนหน้าสไลด์tests

ทีมเลือกอธิบายละเอียดในหน้าหลัก27–30และภาคผนวก38 เพื่อแสดงระดับการตรวจ ตัวอย่างกรณี ผลที่คาดหวัง PostgreSQL concurrency และขอบเขตหลักฐาน ไม่อ่านlogsทุกบรรทัดบนเวที

- CI baseline develop472fba4: backend/PostgreSQL303/303 frontend116/116 URLguards6/6 lint0errors/4warningsเดิมและbuildผ่าน
- BrowserPR22: localisolatedH2/realHTTP/cookies สองbrowsercontexts ก่อนmerge14PASS/0FAILและ17ภาพ ไม่ใช่มือถือจริงหรือpublicrerun
- Finalrelease/public/timedTTL/StockProfileใหม่ต้องเติมผลหลังตรวจจริง ห้ามยกผลbaselineมาเป็นผลรุ่นส่ง

## เก็บรุ่นที่ผ่าน review

1. แก้และพรีเซนต์จากCanvaต้นฉบับ สมาชิกตรวจส่วนตนและซ้อม
2. ระบุcode/deployedcommit/environmentในspeaker notes/รายงาน โดยไม่ใส่password/token/cookie
3. Export PDFจากCanvaเป็น `team-final-v01.pdf`, `team-final-v02.pdf` ตามรุ่นที่ตรวจ ห้ามนำPDFที่สร้างเองมาเรียกว่าexportจากCanva
4. เพิ่มแถวในversiontableและ [changelog](CHANGELOG.md) ระบุวันที่ linkCanva codeSHA และเรื่องที่เปลี่ยน
5. FinalPDFต้องตรงCanvaและreleaseที่ใช้ส่ง แก้publicURLsกับผลtestsจริงก่อนรับรอง

| รุ่น | วันที่ | Canva/PDF | สถานะ |
|---|---|---|---|
| v01-canva-draft | 2026-10-07 | [Canva 40 หน้า](https://www.canva.com/d/D0dhcGCjuTTS7pV) / [เนื้อหาและ notes](team-final-canva-content.md) | นำเข้าและอ่านกลับผ่าน; รอตรวจภาพทุกหน้า/owner review/PDF export/ผล release/ซ้อม |

## ร่างเดิม — ไม่ขอ review และไม่ใช้ส่ง

`pavarit-step3-architecture.*`, `sirapat-step3-quality.*`, `sirapat-step3-quality-premerge.*` เป็นร่างPPTX/PDF/notesที่เก็บไว้ก่อนเปลี่ยนแนวทาง ให้ดู [ชุดทีมปัจจุบัน](team-final-canva-content.md) และCanva/PDFที่รับรองใหม่แทน ผลตรวจPPTXเดิมเป็นประวัติ ไม่ใช่ผลตรวจCanva
