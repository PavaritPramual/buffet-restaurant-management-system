# ศิระพัทธ์ — ตรวจซ้ำ Step 3 วันที่ 6 ตุลาคม 2026

รายงานนี้เป็น snapshot รอบ21:12–21:25. ดู [ผลก่อน merge](sirapat-step3-premerge-report.md) สำหรับ public URL guard, Menu CRUD ผ่าน UI, protected401 ทั้ง4roles, ร่างสไลด์ปรับตัวเลข และ UX issue #23.

สถานะ: **งานเตรียมและ local regression ที่ทำได้โดยอิสระพร้อมส่งตรวจแล้ว แต่ส่วนศิระพัทธ์ยังไม่จบ Final** ยังต้องตรวจ Stock/Profile ของเมธัส, deployment ของธีรเมธ, docs/slides รวมของปวริศช์ และ release revision หลังทีมส่งงาน

## ฐานและสิ่งที่ตรวจสด

- Fetch origin ทุกสาขาแล้ว; develop ยังเป็น `54e35383597f10ef1afb2e7e46ce8bbc1c0d5306`; สาขาศิระพัทธ์/PR#22 เป็น `e1c78f8` ก่อนการตรวจเพิ่มและไม่มีงานค้างอื่น
- อ่าน Step3 และ Tasks ใหม่: Stock/Profile/production deployment ยังระบุไม่ได้เริ่ม ไม่มี field opening target/active หรือ first/last/phone ใน code ปัจจุบัน; remote branch เมธัส/ธีรเมธไม่มี commit ที่ใหม่กว่า integrated develop จึงไม่มี implementation ใหม่ให้ตรวจรับ
- PR#22 ยัง open/draft, mergeable และไม่มี reviewer submission ณเริ่มตรวจ; [CI e1c78f8](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37475748916) completed/success ทั้งสอง jobs
- Git author เป็น Sirapat Wongwiwatseree; ไม่มีการ merge/release/apply migration กลางหรือส่งข้อความหาสมาชิกแทนผู้ใช้

## แยกงานศิระพัทธ์ตามเกณฑ์

| งาน | สถานะที่พิสูจน์แล้ว | งานที่ศิระพัทธ์ต้องกลับมาทำ / สิ่งที่รอ |
|---|---|---|
| Sync/อ่านฐาน/แผนและ cookie contract | พร้อม: develop54e3538 และ requirement/test plan ตรง integrated baseline | Sync/recheck หลัง owner PR เข้า develop |
| Test plan/traceability Stock/Profile/public และ happy/error/permission | ร่างครบ มี test IDs/owner/ข้อจำกัด ไม่อ้าง feature ที่ยังไม่มีว่าผ่าน | เติมชื่อ tests/ผล/commit ของ feature ใหม่ แล้วให้ปวริศช์/ศรัณย์ตรวจ |
| Customer QR/Order/Bill/Payment/Close | Local real HTTP 12 กลุ่มผ่าน; controlled delay/remount/error 3 กลุ่มผ่านแยกกัน | รันซ้ำบน public release HTTPS และ accounts/schema/providers ที่ธีรเมธยืนยัน |
| Four-role route/expiry/logout | ผ่านใน browser จริง: false login401, role/spoof-header403, case/slash, Manager/Supervisor401, Staff/Kitchenlogout | ตรวจ public Secure cookies/Origin และ release environment |
| Responsive/loading/empty/error/confirmation/duplicate | ตรวจภาพรอบใหม่15ภาพ360/768/1280px; fixtures12statesและ23ภาพรอบแรกคงเป็นหลักฐานรอบเดิม; automated pending/race116ผ่าน | ตรวจ Stock/Profile UI ใหม่และ public responsive หลังมีโค้ด/URL |
| Stock/Profile UI tokens/components | **รอ implementation เมธัส**; ศิระพัทธ์ยังไม่ได้ตรวจรับ | ตรวจ DTO/UI validation, target/shortage/lifecycle/legacy names และ error/roles บน integrated commit |
| Reports/SOLID/JPA/slides ของตน | พร้อมส่งใน PR#22; notesตรง source, local vs CI vs fixtures ชัดเจน; slidesร่าง6หน้า | ตรวจ docs/diagram/deckรวมของปวริศช์และของเพื่อน เติม final results/URLs แล้วซ้อม5คน |
| Release gate | **ยังไม่ผ่าน** | ปวริศช์/ศรัณย์ review, migration/runtime owner ยืนยัน, main/deployed revision ตรงกัน แล้วศิระพัทธ์ตรวจ smoke/regressionก่อนส่ง |

## ผลตรวจเพิ่ม

Source ของรอบนี้: `e1c78f8` พร้อม diff tests/runner ก่อน commit ถัดไป แนบ [tested patch](../../test/evidence/sirapat-step3-recheck-2026-10-06/tested-recheck.patch) และ [summary/source hashes](../../test/evidence/sirapat-step3-recheck-2026-10-06/verification-summary.json) เวลาตาม Asia/Bangkok

| Check | Result | ขอบเขตและเวลา |
|---|---|---|
| Frontend ก่อนเพิ่ม cases | 112/112, 14files | 21:12; ทดสอบ e1c78f8 สดอีกครั้ง |
| Frontend หลังเพิ่ม4cases | **116/116**, 14files | 21:15; pending login/logout duplicate, late loginหลังexpiry/newlogin และ late logout failure |
| Lint/build | **0errors/4warningsเดิม; buildผ่าน** | 21:15; ไม่มีแก้ implementation หน้าจอเพิ่ม |
| CoreFlow real HTTP | **12PASS/0FAIL** | 21:17:01–21:17:40; Chrome145.0.7632.117, H2แยก, session/database providersจริง, ไม่มี HTTP mocks |
| Controlled concurrency | **3PASS/0FAIL** | 21:20:44–21:20:53; delayed real responses + injected503 แยกไฟล์ `httpMocks:true` |
| Visual review | **15ภาพรอบใหม่ตรวจทุกภาพ** | CoreFlow12 + concurrency3; Customer360, Staff/Kitchen768, Manager1280; QRcardถูกmask |
| Docs/package consistency | ลิงก์ local ใน6docsครบ; PPTX hashตรง validation/6slides | ไม่อ้างเปิดตรวจใน native PowerPoint และไม่ได้ regenerateสไลด์เพราะตัวเลขในร่างเป็น snapshotเดิม |
| Backend/PostgreSQL | CI e1c78f8: **303/303,0skipped**, buildผ่าน | ไม่รัน local Mavenซ้ำเพราะ backendไม่เปลี่ยน; CI ของ PR head ถัดไปตรวจใน PR#22/Notionแยกจาก report localนี้ |

หลักฐาน: [CoreFlow results](../../test/evidence/sirapat-step3-recheck-2026-10-06/core-flow/results.json) · [Concurrency fixture results](../../test/evidence/sirapat-step3-recheck-2026-10-06/concurrency-fixtures/browser-concurrency-results.json)

QR ใหม่ในแท็บเดิมทดสอบ same-document navigation ด้วย performance.timeOrigin เดิม ตะกร้าและ confirmation ถูกล้าง แลก QRเพียง1POST และ reloadไม่แลกซ้ำ โทรศัพท์ที่สองยังใช้ contextเดิมได้ Controlled remount ทดสอบเปลี่ยนโต๊ะ A→B ระหว่างรอ exchange แล้วออกหน้า/Back: UIและcookieเป็นB, สั่งผิดAได้404 และบันทึกorderBเพียง1รายการ ส่วน acknowledgement/history interleaving แสดง1card/1persistedorder และ delayed GETไม่ลบข้อความ order503

## จุด UX ที่ส่งให้เจ้าของพิจารณา

เมื่อ Staffกดปิดรอบก่อนชำระ ระบบปฏิเสธถูกต้องและ sessionยังACTIVE แต่ข้อความในภาพ `staff-unpaid-close-768.png` เป็นอังกฤษว่า `No payment result is available for this dining session` ควรให้ธีรเมธ (Billing/Payment) พิจารณาข้อความไทยที่อธิบายว่าต้องชำระก่อนปิดรอบ เป็น UX follow-up ที่พบจริง ไม่ใช่ผลทดสอบ flow/permission ล้มเหลว และยังไม่ได้ส่งข้อความให้เจ้าของแทนผู้ใช้

ไม่สามารถติ๊กศิระพัทธ์เสร็จ Final โดยอาศัยการเตรียม tests/report เพียงอย่างเดียว เพราะยังมีหน้าที่ตรวจรับงานเพื่อนและ public releaseหลังส่งมอบ การมี PR/CI ผ่านไม่แทน reviewer approval หรือการซ้อม/ตรวจส่งงานร่วมกัน
