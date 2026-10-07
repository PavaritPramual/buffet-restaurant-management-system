# Slide version history

## v02b-draft — แก้รีวิว PR #24 วันที่ 7 ตุลาคม 2026

- แก้ User/Profile ให้เป็น list/create + basic displayName/email, แยก CustomerMenu/AdminMenu และ cardinality UserAccount–UserProfile เป็น 1 ต่อ 0..1
- Regenerate SVG 4 ชุด และนำเข้าเป็น [Canva v02b](https://www.canva.com/d/yWw6P3disBOSfHS) หน้า 2, 24, 68, 70 เปลี่ยนเฉพาะภาพ/notes คงลำดับ ผู้พูด และโค้ดเดิมทั้ง76หน้า เก็บ v02 เดิมเป็นประวัติ
- ยังเป็นฉบับล่วงหน้า ไม่รับรองเนื้อหา/เวลา/การส่งออก/Final รอให้โค้ดทุกส่วนเสร็จแล้วทบทวนตาม comment ของปวริศช์
- เพิ่มหลักฐาน Supabase SELECT สด V1–V14/checksums/grants และ refresh Git audit snapshot ก่อน commit แก้รีวิว ดู [รายงาน](../testing/pr24-review-fixes-report.md)

## สถานะฉบับล่วงหน้า — 7 ตุลาคม 2026

- ปวริศช์ยังไม่รับรองสไลด์ เก็บ comment ว่าเนื้อหาเยอะ สลับผู้พูดบ่อย และเนื้อหา/ลำดับต้องทบทวนหลังโค้ดทุกส่วนเสร็จ
- คงสไลด์เดิมไว้ ยังไม่แก้เนื้อหา ลำดับ หรือผู้พูด เวลา 12 นาทีและ runbook เป็นร่างที่ยังไม่รับรอง
- ชื่อไฟล์สำรองที่ต้อง export จาก Canva คือ `team-final-v02-draft.pptx` ยังไม่ได้ดาวน์โหลดเนื่องจากเครื่องมือ export/browser ใช้ไม่ได้ ดู [สถานะร่าง](team-final-v02-draft-status.md)

## ปรับแนวทางเก็บไฟล์ — 7 ตุลาคม 2026

- ตามคำยืนยันล่าสุด เก็บ PPTX และ PDF ที่ export จาก Canva ไว้ใน `doc/slide/` ตามเลขเวอร์ชัน โดย Canva ยังคงเป็นต้นฉบับและเครื่องมือพรีเซนต์
- v02 ใช้ชื่อ `team-final-v02.pptx` และ `team-final-v02.pdf` ตรวจภาพและฟอนต์หลัง export ก่อนรับรอง ไม่มีไฟล์ export ทั้งสองใน repo ณ การปรับแนวทางนี้
- ข้อความไม่ใช้ PPTX ในบันทึกรุ่นก่อนเป็นประวัติของแนวทางเดิม ร่าง PPTX รายคนเดิมไม่ใช่ไฟล์ export ชุดทีมจาก Canva

## v02 — 7 ตุลาคม 2026

- ปรับตามผู้ใช้ให้มีcodeจริง ภาพdiagramตามข้อ9.1ครบ ผู้พูดหนึ่งbranchต่อหน้า ฟอนต์ไทยมีหัวและลำดับตามหลักการออกแบบซอฟต์แวร์
- ระบุช่วงพูด12นาทีเป็น20หน้าหลัก อีก56หน้าเป็นภาคผนวกอ้างอิง ไม่เพิ่มเวลาพูดเป็น76หน้า
- 23ภาพรายละเอียดและ5ภาพย่อพร้อมPlantUML/SVG 35หน้าcode 76notes SARABUN/JetBrainsMono navy/cream/terracotta/gold
- อัปเดตUseCaseDescriptions Auth/MenuOrderClasses Orderingsequence Stock/CustomerActivities StatepermissionsและDeploymentdesignที่เคยขาด/ล้าสมัย
- ปรับเกณฑ์Gitเป็น5meaningfulcommitsต่อคนตามการแจ้งของปวริศช์ เก็บworksheet15ไว้เป็นต้นฉบับ
- [Canva v02](https://www.canva.com/d/wORhypcIcwvGuFK) `DAHXSWQA-Vs` นำเข้า76หน้า อ่านกลับครบ 704ข้อความแก้ได้/28diagramimages ไม่มีelementออกนอกcanvas ตรวจภาพlocalครบ ไม่อ้างCanvaallpagevisualQA/PDF/owner/releaseว่าผ่านแล้ว
- v01คงไว้เป็นประวัติ ไม่แก้/ลบdesignเดิม ไม่สร้างPPTX ไม่แก้runtime ไม่mergePR24

## v01-content — 7 ตุลาคม 2026

- เปลี่ยนต้นฉบับ/เครื่องมือพรีเซนต์เป็นCanvaรวมทั้ง5คน ไม่ใช้PPTXเป็นชุดส่งหรือขอreview
- เตรียม32หน้าหลัก+8หน้าภาคผนวกพร้อมspeaker notes เจ้าของและ107source references
- เพิ่มรายละเอียดtestsเป็น4หน้าหลักและภาคผนวก race cases แยกunit/API/PostgreSQL/frontend/browser และผลlocal/fixture/public
- อ้างcodebaseline472fba4 กับdocsPR24; ไม่อ้างStock/Profileใหม่ publicdeployment SOLIDgapsหรือreleaseว่าผ่านแล้ว
- ผู้ใช้อนุมัติการส่งไฟล์แล้ว นำเข้าเป็น [Canva Team Final v01](https://www.canva.com/d/D0dhcGCjuTTS7pV) design `DAHXQy_ZUvQ` จำนวน 40 หน้า 1920×1080
- อ่านกลับข้อความทุกหน้าครบ notes ตรงร่าง 40 หน้า และพบข้อความแก้ไขได้ 535 รายการ ไม่มีองค์ประกอบข้อความออกนอก canvas; ยังไม่ได้รับรองภาพทุกหน้า/owner review หรือ export PDF จาก Canva
- ร่างPPTX/PDFรายคนเดิมเก็บเป็นประวัติ Reviewerข้ามส่วนนี้ได้
