# ศิระพัทธ์ — Regression หลังรวมโค้ด และ Stock/Profile ก่อน merge

วันที่ 8 ตุลาคม 2026 (Asia/Bangkok). เจ้าของ: ศิระพัทธ์. Reviewer: ปวริศช์ (E2E/traceability), ศรัณย์ (API/State); เมธัสรับ defect ของ Stock/Profile.

**ผลส่งตรวจ:** develop หลัง #26/#27/#29 ผ่าน real Core Flow **16/16**; Stock/Profile ของ PR #28 ตรวจได้ก่อน merge และผ่าน Core Flow **16/16** กับ Stock/Profile จริง **8/8**. ไม่พบ flow/permission blocker ในขอบเขตที่ตรวจ. ส่ง tests/docs/evidence ได้ทันที; ยังไม่ปิด Final/public acceptance.

## รุ่นโค้ดและสภาพแวดล้อม

| ชุด | Source / runner | เวลา browser Asia/Bangkok |
|---|---|---|
| Integrated develop | `6d83eace6bbd4d20f4d3cb3a25eb2d5ddb81fcc6`; runner `4f8bc5a6fe072476772ef8c3ffcd610c9b35dc5c` มีเฉพาะ tests/evidence เพิ่ม ไม่มี production diff | Core Flow 12:36:08–12:36:32 |
| Stock/Profile candidate | PR #28 `cc72fa0c9125c50de185e02940b3359faee776f8`; มี develop `6d83eac` เป็น ancestor; ไม่ merge feature เข้า PR ของเรา | Core Flow 12:39:41–12:40:05 |
| Stock/Profile real UI | Runtime source `cc72fa0`; runner `4e5b30986916742be5edcd02944b7dc9249a1df4` | 12:41:31–12:41:39 |
| Stock/Profile UI fixtures | Runtime source `cc72fa0`; runner `b8084aa5a35aa798b3fd5fef5842c50c62e80f0a` | เสร็จ 12:43:10 |

ทุกรอบใช้ **fresh isolated in-memory H2**, ปิด `.env` import/demo seed, สร้างบัญชีสุ่มใน memory และปิด runtime หลังรัน. Providers เป็น session/database จริง; Core Flow และ Stock/Profile จริงไม่มี HTTP mocks. Web `http://127.0.0.1:5177`, API `http://127.0.0.1:8087/api/v1`. Java 21.0.6, Maven 3.9.16, Node 24.13.1, Chrome 145.0.7632.117. แต่ละ launcher run ใช้ H2 คนละชุด; controlled fixtures แยกจากผลจริง. ไม่เชื่อม/แก้ Supabase กลาง และไม่มี deployed commit ที่รับรองในรอบนี้.

[Evidence index](../../test/evidence/sirapat-step3-followup-2026-10-08/README.md) · [ผลรวมและเวลา UTC](../../test/evidence/sirapat-step3-followup-2026-10-08/verification-summary.json). Production backend/frontend/migrations ไม่มี diff ใน PR tests นี้.

**Recheck ระหว่างจัดส่ง:** PR #28 เปลี่ยนเป็น `5a75d42d280f0688edbd1ccd3d231d5353b3878e`. Diff จากtestedcc72fa0มีเฉพาะ `doc/database/auth-stock-schema-delta.md` และ `step2-schema-approved.md`; runtime/test blobsทั้ง344ไฟล์ไม่เปลี่ยน จึงคงผลรันจริงที่cc72fa0 ไม่เปลี่ยนป้ายผลเป็นรอบใหม่. Ownerแก้คำอธิบายให้แยก V13–V15 success=true ที่reviewerอ่านพบออกจาก V15 approval/validate/checksum evidenceที่ยังรอ; reviewersยังต้องยืนยันheadใหม่.

## Automated และ CI แยกตาม revision

| ชุด / source | Backend | Frontend / lint / build | หลักฐาน |
|---|---|---|---|
| Local develop `6d83eac` | **337 discovered / 310 passed / 27 skipped / 0 failures/errors**; verify PASS | **118/118**, 14 files; 0 errors / 4 existing warnings; build PASS | [backend](../../test/evidence/sirapat-step3-followup-2026-10-08/develop/backend-summary.json), [frontend](../../test/evidence/sirapat-step3-followup-2026-10-08/develop/frontend-test.txt) |
| CI develop `6d83eac` | **339/339**, 0 failures/errors/skipped | **118/118**, guards **6/6**, lint/build PASS | [run37719753927](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37719753927); อ่าน job logs สด |
| Local PR #28 `cc72fa0` | **346 discovered / 318 passed / 28 skipped / 0 failures/errors**; clean verify PASS | **121/121**, 14 files; 0 errors / 4 existing warnings; build PASS; npm ci ตาม candidate lockfile | [backend](../../test/evidence/sirapat-step3-followup-2026-10-08/stock-profile-candidate/backend-summary.json), [frontend](../../test/evidence/sirapat-step3-followup-2026-10-08/stock-profile-candidate/frontend-test.txt) |
| CI PR #28 `cc72fa0` | **348/348**, 0 failures/errors/skipped | **121/121**, guards **6/6**, lint/build PASS | [run37730612713](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37730612713); อ่าน job logs สด |

Local skips เป็น PostgreSQL/Testcontainers ที่ runtime เครื่องนี้ไม่พร้อม; candidate เพิ่ม PostgresStockProfileMigrationTest 1 skip จาก 27 ของ develop. ไม่ใช้ local H2 อ้างว่า PostgreSQL locking/grants ผ่าน และไม่ใช้ CI ของ candidate อ้างเป็นผล integrated develop. Public URL guards ตรวจ configuration ก่อนเปิด browser ไม่ใช่ HTTPS runtime acceptance.

## Regression ที่ทำเพิ่มได้ทันที

- #26 API/State: real cookies, Kitchen/Staff UI transitions และ State denials 12 กรณี ตรวจ HTTP/ErrorResponse/persisted history ไม่เปลี่ยนหลัง denial ผ่านทั้ง develop และ candidate.
- #27 unpaid close: Staff เห็น **กรุณาบันทึกการชำระเงินก่อนปิดรอบกิน**; session ยัง ACTIVE และ table ยัง OCCUPIED. เพิ่ม assertion ข้อความและ table state ลง runner; payment/close ภายหลังคืน AVAILABLE และ revoke ทั้งสอง customer contexts.
- Manager Menu CRUD ทำผ่าน UI; QR ใช้ครั้งเดียว/same-tab rescan/StrictMode/two contexts; bill/payment/close; protected API401 recovery ทั้งสี่ roles และ Staff/Kitchen UI logout ผ่าน. Server-side invalidation พิสูจน์ 401 recovery; ไม่ใช่ timed TTL.

## Stock/Profile บน PR #28 ก่อน merge — 8/8 จริง

| กลุ่ม | ตรวจจริง |
|---|---|
| Defaults / target | Omit target ผ่าน APIได้ default0/active=true/quantity0; Managerสร้างและแก้ decimal target ผ่าน UI, reload แล้ว persisted target5.125, quantity0, shortfall5.125 |
| Negative target | Browser min0 ปฏิเสธค่าลบ; API400 และรายการสต็อกไม่เปลี่ยน |
| Movements / confirmation | Supervisorรับเข้า2.125 และปรับ+1 ผ่าน UI; ก่อนยืนยันยังมีประวัติ1รายการ หลังยืนยันมี2; quantity3.125, shortfall2 |
| Inactive lifecycle | Managerปิดผ่าน confirmation; Supervisorปุ่มรับเข้า/ปรับยอด disabled; APIทั้งสอง409; balance/historyเดิมไม่เปลี่ยนและยังอ่านได้ |
| Reactivation / roles | Supervisor/Staff/Kitchen toggle403, anonymous401; Managerเปิดกลับผ่านUI แล้วรับเข้าได้; quantity6.125, shortfall0, history3 |
| Profile CRUD | Managerสร้าง/แก้ชื่อและนามสกุลผ่านUIและreload; phone/emailoptional; phoneใช้max length ไม่บังคับregex; username/displayName/roleเดิมคงอยู่ |
| Profile validation / roles | Required names, firstName101/phone21 ได้400; wrong roles403/anonymous401; saved profileไม่เปลี่ยน; browser required/maxLength100/20 |
| Responsive | Stock/Profileจริง360/768/1280px; documentไม่ล้น; ตารางกว้างอยู่ในพื้นที่เลื่อนแนวนอน |

[ผลจริง 8 กลุ่ม](../../test/evidence/sirapat-step3-followup-2026-10-08/stock-profile-candidate/real-stock-profile/results.json). Legacy data preservation/nullable names ถูกตรวจโดย StockProfileMigrationTest และ StockProfileFinalIntegrationTest บน H2; ภาพ legacy fallback ด้านล่างเป็น fixture ไม่ใช่หลักฐาน migration จริง.

## Fixtures, ภาพ และ provenance

- Develop: loading/empty/error **12 states** + controlled concurrency **3/3**; `httpMocks:true`.
- Stock/Profile: overview/editor/Profile loading/empty/error **9 states** + legacy fallback **1 state**; `httpMocks:true`.
- ตรวจภาพครบ **70 ภาพ** ผ่าน contact sheets12ชุด: develop32 (จริง17/fixtures15), candidate38 (Core17/StockProfile11/fixtures10). ไม่พบ layout blocker ในภาพที่ตรวจ; การกดผ่านUIและdocument overflowตรวจด้วยrunner. ไม่ได้อ้าง full accessibility audit หรือมือถือจริง.
- PR #29 reviewerขอ canonical hashes: เพิ่ม [335 Git blob hashes ของ tested f97ef42](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/canonical-source-hashes.json) และวิธีverify; คง raw manifestเก่าเป็นประวัติ. `MenuAdminPage.tsx` historical working bytesไม่ได้เก็บ จึงไม่อ้างว่าอธิบาย mismatchย้อนหลังแล้ว. รอบใหม่แนบ canonical runtime340/candidate344 และ runner340 hashes.
- รอบตั้งค่า Javaในsandboxค้างที่ test agentจึงหยุดและรัน verifyสำเร็จ. Browserrunnerรอบแรกเลือกทั้งoverview/historyrow และ fixtureรอบแรกunrouteก่อนresponseเสร็จ; แก้เฉพาะtestsแล้วทวน Stock/Profile8/8 กับ fixtures10/10 สำเร็จ. ไม่ใช่ product defect; ไม่รวมรอบไม่สมบูรณ์เป็น acceptance. Core Flowของcandidateเสร็จ16/16ก่อนส่วนrunnerที่ผิดจึงเก็บผลCoreนั้นแยกตามเวลา.

## ส่งต่อและเงื่อนไขปิด Final

1. ส่ง PR tests/report/evidence/traceability และ [ร่างสไลด์ส่วนศิระพัทธ์](../slide/sirapat-step3-quality-2026-10-08-draft.md) ให้ reviewersตรวจได้ทันที. PR #29/#26/#27 mergeแล้ว; รายงาน7ต.ค.เป็นประวัติ.
2. PR #28 ยังต้องผ่าน owner/reviewer gate. Ownerแก้เอกสารที่5a75d42แล้วหลัง [รีวิวปวริศช์8ต.ค.](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/28#pullrequestreview-5451813407); ต้องยืนยันheadใหม่และหลักฐานรับรองที่ยังขาด. ผลUI/testsนี้ไม่อนุมัติ migrationกลางย้อนหลัง.
3. หลังรวมStock/Profileให้รัน integrated revision อีกครั้ง. ธีรเมธยืนยันsame-origin HTTPS/test accounts/data scope/providers/schema/deployed SHA จึงรันpublic Core Flow/StockProfile, Secure-cookie/timedTTL/deep links/Swaggerจริง.
4. ปวริศช์/ศรัณย์review, main/release/deployed SHAตรงกัน, ทีมreview/export/rehearse slidesก่อนปิดFinal. Notiontaskคงกำลังดำเนินการจน gateครบ.
