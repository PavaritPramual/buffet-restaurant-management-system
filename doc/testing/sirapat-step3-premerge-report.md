# ศิระพัทธ์: ตรวจเพิ่มก่อน merge PR #22

6 ตุลาคม 2026, Asia/Bangkok. ขอบเขต PR: local regression, test preparation และ AdminShell session fix. พร้อมส่ง review แยกจากการปิด Final ซึ่งยังรอ Stock/Profile, public deployment และ release gates.

## สิ่งที่เก็บเพิ่ม

1. Public runner บังคับ Web/API เป็น same-origin HTTPS รวม effective port ก่อน credentials/browser/test data. มี6 Node guard cases และ CI step แยกจาก frontend Vitest.
2. ทั้ง4roles รับ protected HTTP401 จริงขณะ shell เปิดอยู่แล้วกลับ login. ใช้ API logout เพื่อ invalidate session ฝั่ง server ไม่มี UI logout/HTTP mocks ในกลุ่มนี้ แยก Staff/Kitchen login+UI logout อีกกลุ่ม. **ยังไม่ได้พิสูจน์ timed TTL expiry หรือ Secure cookies บน public**.
3. Category/Menu create/update/read หลัง reload/delete ผ่าน Manager UI จริง ตรวจ POST201/PUT200/DELETE204 และ persistence ผ่าน HTTP. ลบเฉพาะ unused item/category ของรอบนี้ ส่วนเมนู Core Flow เก็บเป็น history. ตรวจ description/packages แต่ไม่ได้อ้างว่า image URL ครบผ่าน UI.
4. ร่าง6สไลด์/PDF/notes ใหม่ `sirapat-step3-quality-premerge.*`: CI backend303/303, frontend116/116 และ public pending. เก็บร่างเดิมเป็น snapshot รอบเริ่มงาน.
5. เปิด [UX issue #23](https://github.com/PavaritPramual/buffet-restaurant-management-system/issues/23) มอบหมายธีรเมธ (`kojidesu01`) แก้ข้อความ unpaid-close ภาษาอังกฤษ. ไม่ block PR เพราะ guard ปฏิเสธและคง ACTIVE ถูกต้อง.

## ผลและหลักฐาน

| Check | ผล | ขอบเขต |
|---|---|---|
| Public URL guards | 6/6ผ่าน | Node tests ไม่มี browser/network; HTTPS default443 originเดียวกันผ่าน; HTTP/hostname/portต่างและcredentials/query/fragmentไม่ผ่าน |
| Local frontend | 116/116,14files | 22:06; lint0errors/4warningsเดิม; TypeScript/Vite buildผ่าน |
| Local Core Flow | **14PASS/0FAIL** | 22:08:02–22:08:41, Chrome145.0.7632.117, H2แยก/session+database providersจริง, `httpMocks:false` |
| Browser screenshots | 17ภาพตรวจทุกภาพ | Customer360px, Staff/Kitchen768px, Manager1280px; QRcardถูกmask |
| Latest draft | 6สไลด์/PDF6หน้า | Import/editต้นฉบับ, editable text/notes; package/layout/reimportผ่าน; render/ตรวจทุกหน้า ไม่อ้างnativePowerPointQA |
| Verified CI before this edit | backend303/303,frontend116/116 | [run37478993077](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37478993077), source a351fba; backend0failures/errors/skipped. **ตรวจcurrent CIของheadหลังpushในPR#22ก่อนmergeด้วย** |

Browser ทดสอบ `a351fbaf5878f3ad1d68ae3906bd63eb337ffe20` + runner/URL module working diff ก่อน commit. Frontend/backend implementationไม่เปลี่ยนในรอบนี้. [Results](../../test/evidence/sirapat-step3-premerge-2026-10-06/core-flow/results.json), [tested patch](../../test/evidence/sirapat-step3-premerge-2026-10-06/tested-premerge.patch), [source hashes/summary](../../test/evidence/sirapat-step3-premerge-2026-10-06/verification-summary.json).

รอบเริ่มเพิ่ม Menu case ล้มเพราะ runner อ่านตารางก่อน reload เสร็จ แก้การรอ table แล้วรัน Core Flow ใหม่ผ่าน14กลุ่ม. UI-state/concurrency fixtures ไม่ได้นับเป็น rerun รอบนี้และเก็บตามรายงานเดิม.

## ร่างล่าสุดสำหรับรวมสไลด์

[PowerPoint](../slide/sirapat-step3-quality-premerge.pptx), [PDF](../slide/sirapat-step3-quality-premerge.pdf), [speaker notes](../slide/sirapat-step3-quality-premerge-notes.md). Slide4เป็นCIที่ยืนยันพร้อมrun/commitในnotes แยกจากlocalMaven274/27skippedร่างเดิม. ใช้current PR checksตรวจheadล่าสุดก่อนmerge.

## งานที่ยังรอ

- เมธัส: Stock target/active และ Profile implementation/DTO/migrations/UI แล้วศิระพัทธ์ตรวจรับบน integrated revision.
- ธีรเมธ: public same-origin HTTPS, test accounts/scope และ deployed commit/schema/providers แล้ว rerun Final browser รวม timed expiry.
- ปวริศช์/ศรัณย์: review PR/API/E2E/traceability; final main/deployed revision; สไลด์รวมและซ้อม5คน.

CI/localผ่านไม่แทน reviewer approval. PRนี้mergeได้หลังreviewer/gatesของPRผ่าน แต่ยังไม่ถือว่า Final task ศิระพัทธ์เสร็จ.
