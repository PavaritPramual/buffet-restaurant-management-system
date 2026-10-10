# ศิระพัทธ์ — เนื้อหาสไลด์คุณภาพและผลทดสอบ 8 ตุลาคม 2026

ร่างส่วนบุคคลสำหรับรวมชุดทีม มี speaker notes และแหล่งข้อมูลครบ. ยังไม่ใช่ PPTX/PDF ที่ export จาก Canva หรือชุด Final ที่รับรอง. [รายงาน](../testing/sirapat-step3-followup-2026-10-08.md) เป็นแหล่งผลล่าสุด; draft303/116และผล7ต.ค.เก็บเป็นประวัติ.

## หน้า 1 — การตรวจระบบหลังรวมโค้ด

- develop `6d83eac` รวม API/State และคำแนะนำปิดรอบก่อนชำระแล้ว
- CI backend339/339 และ frontend118/118 ผ่าน
- Browser Core Flowจริง16/16 รวม State denials12กรณี
- Public HTTPS และ timed session expiry ยังรอตรวจรับ

Speaker notes: ผลCIมาจากrun37719753927ของdevelopที่รวม #26/#27/#29แล้ว. Browserใช้H2ใหม่และcookiesจริงที่12:36วันที่8ต.ค.; ผลนี้ไม่รับรองruntimepublic. localbackend310ผ่าน/27skipแยกจากCIไม่มีskip. URLguards6กรณีตรวจconfigurationเท่านั้น.

Sources: [CI develop](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37719753927); [Core Flow](../../test/evidence/sirapat-step3-followup-2026-10-08/develop/core-flow/results.json).

## หน้า 2 — ตรวจ Stock/Profile ได้ก่อน merge

- Candidate PR #28 `cc72fa0`: CI backend348/348, frontend121/121
- Browserจริง Stock/Profile8/8 และ Core Flow16/16
- ปิดใช้งานแล้วเคลื่อนไหวไม่ได้ ประวัติยังอยู่ เปิดกลับได้
- Profileตรวจชื่อ/นามสกุล ขนาดข้อมูล และสิทธิ์ผู้จัดการ

Speaker notes: ตัวเลข348/121เป็นbranchของเมธัส ไม่ใช่developหรือpublicrelease. UIสร้าง/แก้ข้อมูลจริงและอ่านpersistenceซ้ำ; targetไม่เปลี่ยนquantity. Phonepolicyปัจจุบันจำกัด20ตัวอักษรและไม่บังคับregex; legacy migrationใช้automatedH2tests ส่วนภาพlegacyfallbackเป็นcontrolledfixture. ตรวจ360/768/1280โดยตารางใช้horizontal scrollภายในหน้า. ยังต้องผ่านreviewและตรวจซ้ำหลังmerge.

Sources: [CI candidate](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37730612713); [Stock/Profileจริง](../../test/evidence/sirapat-step3-followup-2026-10-08/stock-profile-candidate/real-stock-profile/results.json).

## หน้า 3 — หลักฐานและการรับรองรุ่นส่ง

- ภาพ70ภาพ แยกflowจริงจากloading/empty/errorและconcurrencyfixtures
- ผูกผลกับcommit เวลา สภาพแวดล้อม และcanonical Git blob hashes
- ทวนรุ่นรวม แล้วตรวจpubliccookie/TTL/roles/flowบนdeployed SHA
- Reviewerรับรอง และทีมซ้อมจากชุดสไลด์รุ่นส่งจริง

Speaker notes: HTTPfixturesพิสูจน์การแสดงUIและracehandling ไม่พิสูจน์APIoutageจริงหรือPostgreSQLlocks. การlogoutฝั่งserverได้401ขณะshellเปิดพิสูจน์recoveryแต่ไม่ได้รอTTLจริง. SHA256จากGitblobตรวจย้อนกลับข้ามเครื่องได้ ส่วนworkingbytesเก่าที่ไม่ตรงเก็บเป็นประวัติพร้อมข้อจำกัด. Finalต้องให้main/release/deployedSHAและหลักฐานตรงกัน; ผลPRย่อยหรือCIไม่ปิดgateทีมโดยอัตโนมัติ.

Sources: [Evidence index](../../test/evidence/sirapat-step3-followup-2026-10-08/README.md); [Test plan](../testing/test-plan.md); [Notion Step3](https://app.notion.com/p/a9e90b8ff9648363a6ab81b48fd70816).
