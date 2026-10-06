# สไลด์นำเสนอของทีม — Canva

## แนวทางปัจจุบัน

ผู้ใช้ยืนยันวันที่ 7 ตุลาคม 2026 ให้ทำสไลด์ **ทั้งทีม** ใน **Canva** เป็นต้นฉบับและเครื่องมือพรีเซนต์ เมื่อรุ่นหนึ่งตรวจแล้วให้ export PDF **จาก Canva** เก็บในโฟลเดอร์นี้พร้อมเลขเวอร์ชัน วันที่ และ changelog

**ไม่ใช้ PPTX เป็นชุดนำเสนอหรือชุดส่ง Reviewer ไม่ต้องตรวจ PPTX รวมถึง PDF/notes ที่สร้างจาก PPTX ร่างเดิม** ไฟล์รายคนเดิมคงไว้เป็นประวัติ ไม่ใช่หลักฐานว่าสไลด์ทีม Final ผ่านแล้ว

พื้นที่ Canva ที่ปวริศช์ให้: [Posd](https://canva.link/hwrws57kjwgvafx) — ตรวจพบหนึ่งหน้าว่างก่อนเริ่มงาน วันที่ 7 ตุลาคม 2026

## ชุด v01 ที่เตรียม

[เนื้อหาชุดทีม 40 หน้า](team-final-canva-content.md): **32 หน้าหลัก + 8 หน้าภาคผนวก** พร้อมข้อความบนสไลด์ speaker notes แหล่งอ้างอิง และผู้รับช่วงทั้ง5คน

[ผลตรวจเนื้อหา](../../test/evidence/pavarit-step3-docs-2026-10-07/canva-content-verification.json): 40notes/5owners/107references ตรวจsource60ไฟล์ที่commitอ้างอิงและlocalMarkdown89ลิงก์ผ่าน ผลนี้ไม่ใช่การตรวจภาพCanvaหรือPDFexport

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

ร่างเนื้อหาตรวจจาก code baseline `472fba4` และเอกสาร PR#24 แล้ว แต่การนำไฟล์เข้า Canva ถูก **automatic approval review ปฏิเสธ** เนื่องจากต้องอนุมัติการส่งไฟล์โครงการออกไปภายนอกโดยเฉพาะ จึงยังไม่อ้างว่าสไลด์40หน้าอยู่ใน Canva ไม่มี PDF export รุ่นทีมที่ตรวจแล้ว และยังไม่ได้ตรวจภาพ Canva

ไฟล์ที่ขออนุมัติส่งมีเนื้อหาเดียวกับร่าง Markdown40หน้า มีรายชื่อ/รหัสสมาชิกและผลตรวจที่อยู่ใน GitHub public ไม่มี credentialsจริงหรือข้อมูลลูกค้า การส่งต้องผ่าน approval ก่อน ไม่เปลี่ยนเป็นวิธีอื่นเพื่อข้ามการปฏิเสธ

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
| v01-content | 2026-10-07 | [เนื้อหา40หน้า](team-final-canva-content.md) / พื้นที่Canvaตามลิงก์ด้านบน | ร่างครบ รออนุมัติส่งเข้าCanva/ตรวจภาพ/ownerreview/PDFexport |

## ร่างเดิม — ไม่ขอ review และไม่ใช้ส่ง

`pavarit-step3-architecture.*`, `sirapat-step3-quality.*`, `sirapat-step3-quality-premerge.*` เป็นร่างPPTX/PDF/notesที่เก็บไว้ก่อนเปลี่ยนแนวทาง ให้ดู [ชุดทีมปัจจุบัน](team-final-canva-content.md) และCanva/PDFที่รับรองใหม่แทน ผลตรวจPPTXเดิมเป็นประวัติ ไม่ใช่ผลตรวจCanva
