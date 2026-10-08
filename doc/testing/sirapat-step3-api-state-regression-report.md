# ศิระพัทธ์ — ผลตรวจ API/State และ regression รอบส่งตรวจ

**Historical snapshot — 7 October:** PR #29/#26/#27 merge แล้ววันที่ 8 ต.ค.; สถานะ unmerged/dependencies ด้านล่างเป็นข้อมูลขณะรันเดิม. ผลล่าสุดแยก develop กับ PR #28 candidate อยู่ใน [รายงาน 8 ตุลาคม](sirapat-step3-followup-2026-10-08.md). canonical Git hashes เพิ่มใน evidence index ตาม reviewer feedback โดยไม่แก้ผลรันเก่า.

วันที่ 7 ตุลาคม 2026 (Asia/Bangkok). เจ้าของ: ศิระพัทธ์. Reviewer: ปวริศช์ (E2E/traceability), ศรัณย์ (API/State).

**ผล:** integrated develop ผ่าน local regression รอบใหม่: real HTTP/browser Core Flow **16 PASS / 0 FAIL** รวมคำขอ State ที่ต้องถูกปฏิเสธ **12 กรณี** พร้อมตรวจ `ErrorResponse` และ persisted status หลังทุกคำขอ. ส่งเป็น PR ของ tests/docs/evidence เข้า develop ได้; **ยังไม่รับรอง Final/public release**.

งาน API/State audit หลักเป็นของศรัณย์; รายงานนี้ส่งผลตรวจ tests/frontend และ integrated regression ตามหน้าที่ศิระพัทธ์ ไม่แทน approval ของ reviewer หรือผลรัน public.

## 1. รุ่นและสภาพแวดล้อมที่ตรวจ

| รายการ | ค่าที่ตรวจจริง |
|---|---|
| Integrated develop | `cb612d9bd396f7ec5b4ca6c70925acf087e305a6` หลัง merge PR #25 |
| Automated backend/frontend source | `cb612d9`; ไม่มีการแก้ production source ใน PR นี้ |
| Browser/test source | `f97ef42872b5d0fd585d475815166b42b021bc62` เพิ่ม negative State assertions ใน `step3-core-flow.cjs` |
| Working tree ระหว่าง browser | dirty เฉพาะ test-plan/traceability docs; แนบ [tested diff](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/tested-docs-diff.json) และ [source hashes](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/source-hashes.json) |
| Backend runtime | jar ที่ build รอบนี้, Java 21.0.6, Maven 3.9.16; profile `local-regression`; H2 `jdbc:h2:mem:sirapat_submit` |
| Frontend/browser | Node 24.13.1; Chrome 145.0.7632.117; Vite local runtime |
| Web / API | `http://127.0.0.1:5177` / `http://127.0.0.1:8087/api/v1` |
| Providers | Auth/master/menu/dining/fulfillment ใช้ session; ordering/billing/payment ใช้ database |
| แยกข้อมูล | H2 ใหม่เฉพาะรอบนี้; ปิด `.env` import; ไม่มี demo seed/fixture providers; credentials สุ่มใน memory; runtime ปิดแล้วหลังรัน |
| Core Flow เวลา | 18:49:06–18:49:29 Asia/Bangkok, 7 ต.ค. 2026 |
| Deployed commit | ไม่มี: รอบนี้เป็น local H2 ไม่ใช่ public runtime |

ดู [runtime summary](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/runtime-summary.json), [startup proof](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/runtime-startup.txt) และ [verification summary](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/verification-summary.json). ชื่อ environment ใน runner เป็นคำประกาศของผู้รัน; หลักฐาน runtime แยกข้างต้นใช้ยืนยัน providers/database รอบนี้.

## 2. Automated รอบใหม่

| ชุด | ผลจริง | หลักฐาน / ขอบเขต |
|---|---|---|
| Backend `verify` บน H2 | **334 discovered: 307 passed, 27 skipped, 0 failures/errors**; BUILD SUCCESS | [ผล backend](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/backend-result.txt), [suite counts](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/backend-summary.json); จบ 18:41:16 |
| Frontend Vitest | **116/116**, 14 files | [log](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/frontend-test.txt) |
| Frontend lint / build | **0 errors / 4 existing warnings**, build PASS | [lint](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/frontend-lint.txt), [build](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/frontend-build.txt) |
| Public runtime URL guards | **6/6** | `node --test test/browser/step3-runtime-config.test.cjs`; ยืนยัน fail-fast configuration ไม่ใช่ HTTPS/cookie runtime acceptance |
| GitHub CI ของ PR ส่งตรวจนี้ | ตรวจ conclusion/head/logs ในหน้า PR และลิงก์ผลที่ส่งใน Notion | [Workflow](../../.github/workflows/ci.yml) รัน PostgreSQL ใน isolated service; ห้ามนำผล local H2 มารวมเป็น no-skips CI |

27 skipped คือ PostgresApplicationClientAccessTest 2, PostgresDiningSessionMigrationTest 1, PostgresMenuOrderingMigrationTest 1, PostgresOrderCloseConcurrencyTest 8, PostgresPaymentIntegrationTest 13 และ PostgresStockSecurityIntegrationTest 2. รอบ local ปิด destructive PG opt-in และไม่มี Docker daemon พร้อมใช้งาน จึง **ยังไม่ได้ตรวจ PostgreSQL migration/locking ในรอบ local นี้**. ไม่มีการเชื่อมต่อหรือแก้ shared Supabase.

ผล sandbox ครั้งแรกที่ Vitest หา temp directory ไม่พบ และการตั้ง profile `demo` ที่ไม่ bootstrap Manager เป็นปัญหาตั้งสภาพแวดล้อมก่อนรันสำเร็จ; logs ที่แนบเป็นรอบสำเร็จบน temp directory ภายใน workspace และ profile `local-regression`. ไม่นับครั้งตั้งค่าที่ล้มเหลวเป็นผล acceptance.

## 3. API/State: คำขอจริงกับ cookie session

เพิ่ม assertions ใน runner เดิม ใช้ `PATCH /api/v1/orders/{id}/status` ผ่าน backend จริง. ทุก denial ตรวจ HTTP status, `ErrorResponse.status/path/error/message/timestamp` และอ่าน customer history ใหม่เพื่อยืนยันสถานะที่เก็บไม่เปลี่ยน. ไม่มี HTTP mocks ใน Core Flow.

| Role | สถานะเดิม → คำขอ | Expected / actual | Stored state |
|---|---|---|---|
| anonymous | RECEIVED → PREPARING | 401 / 401 | RECEIVED |
| MANAGER | RECEIVED → PREPARING | 403 / 403 | RECEIVED |
| SUPERVISOR | RECEIVED → PREPARING | 403 / 403 | RECEIVED |
| SERVICE_STAFF | RECEIVED → PREPARING | 403 / 403 | RECEIVED |
| KITCHEN_STAFF | RECEIVED → READY (ข้ามขั้น) | 400 / 400 | RECEIVED |
| KITCHEN_STAFF | RECEIVED → `unknown` | 400 / 400 | RECEIVED |
| KITCHEN_STAFF | unknown order id → PREPARING | 404 / 404 | existing order ยังคง RECEIVED |
| KITCHEN_STAFF | PREPARING → RECEIVED | 400 / 400 | PREPARING |
| KITCHEN_STAFF | READY → SERVED (ผิด role) | 403 / 403 | READY |
| KITCHEN_STAFF | READY → PREPARING | 400 / 400 | READY |
| SERVICE_STAFF | SERVED → SERVED | 400 / 400 | SERVED |
| KITCHEN_STAFF | SERVED → READY | 400 / 400 | SERVED |

สิทธิ์ถูกตรวจก่อน State transition: wrong-role request อาจได้ 403 แม้ transition จะผิดด้วย. Happy path `RECEIVED → PREPARING → READY → SERVED` ทำผ่าน Kitchen/Staff UI จริงและ customer history เห็น `SERVED`.

## 4. Real browser Core Flow: 16 PASS

[ผลรายกลุ่มและรายละเอียด](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/core-flow/results.json) (`httpMocks:false`):

1. Login จริง 4 roles; invalid login, anonymous และ spoofed role header ถูกปฏิเสธ.
2. Direct URL รวม uppercase/trailing slash ไม่ข้าม role guard.
3. Manager สร้าง package/soup/table ผ่าน UI.
4. Manager Category/Menu **create/update/read-after-reload/delete ผ่าน UI**; HTTP reads ตรวจ persistence และลบเฉพาะ unused data ของรอบนี้.
5. Staff เปิดรอบจริง; ปิดก่อนชำระไม่ได้และรอบยัง ACTIVE.
6. QR ใช้ครั้งเดียว; 2 independent customer browser contexts ร่วมโต๊ะเดียวกัน; reuse ได้ 404.
7. QR ใหม่ใน tab เดิม reset cart; reload ใช้ cookie โดยไม่ re-exchange.
8. Negative State requests ตอน RECEIVED ผ่าน 7 กรณี.
9. Confirm สั่งครั้งเดียว; Kitchen/Serving UI เปลี่ยน State จริง.
10. Negative State requests หลังเริ่มเตรียม/READY/SERVED ผ่าน 5 กรณี.
11. Bill request หยุดคำสั่งจากทั้งสอง contexts; backend ปฏิเสธเพิ่ม order ด้วย 409.
12. Payment บันทึกจริง; customer ทั้งสองแสดง total/paid 997.50, due 0.
13. Explicit close ทำให้โต๊ะ AVAILABLE และ revoke customer ทั้งสอง contexts.
14. Shell ของ Manager/Supervisor/Staff/Kitchen ได้ protected API **401 จริง** หลัง server-side invalidation และซ่อนหน้าที่ต้องมีสิทธิ์.
15. Staff/Kitchen login ใหม่แล้ว logout ผ่าน UI; protected APIs ได้ 401.
16. Swagger HTTP200 และมี customer bill contract.

2 contexts ใช้แทนการตรวจหลายมือถือ ไม่ใช่ physical phones. การ invalidate session ผ่าน server API logout พิสูจน์ UI recovery จาก 401 แต่ **ไม่ใช่ elapsed TTL expiry**. Public timed TTL และ Secure-cookie ยัง pending.

## 5. Controlled fixtures และภาพ

| ชุดแยก | ผล | หลักฐาน |
|---|---|---|
| Loading/empty/error Customer/Staff/Kitchen/Manager | 4 role groups / **12 states PASS**, 360/768/1280px; รัน 18:49:36 | [UI state results](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/ui-state-fixtures/results.json); Customer context เป็น fixture, employee shell login จริงและ inject เฉพาะ responses |
| Concurrency UI | **3 PASS / 0 FAIL**; รัน 18:49:36–18:49:38 | [results](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/concurrency-fixtures/browser-concurrency-results.json); delay order/history/QR + injected 503 |
| Screenshot inspection | **32 images**: 17 real Core Flow + 12 UI fixtures + 3 concurrency fixtures | [visual QA](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/visual-qa.json); ตรวจครบผ่าน contact sheets 8 ชุด; ไม่พบ layout blocker ในภาพที่ตรวจ |

Fixtures ทั้งสองชุดระบุ `httpMocks:true`; ไม่ใช้เป็นหลักฐาน unmodified runtime, PostgreSQL locking หรือ public acceptance. QR card ที่ปิดด้วยสีในบางภาพเป็นการ mask credential ตาม runner.

## 6. งานศรัณย์และ issue ที่ต้องติดตาม

- [PR #26](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/26) head `76eccee2ea932e2cae6132c96d0c80e5b572dfd4` ยัง **unmerged** ขณะตรวจวันที่ 7 ต.ค. 18:56; ไม่รวมใน baseline/browser รอบนี้.
- [ผล review ของศิระพัทธ์](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/26#pullrequestreview-5441001051) ยัง CHANGES_REQUESTED: `doc/slide/team-final-canva-v02-content.md` หน้า 71/บรรทัด 1433 ยังเป็น `StateFactorycurrent.next`; รอศรัณย์แก้แล้วตรวจ head ใหม่. ไม่แก้ source/approval ของเพื่อนใน PR ส่งผลตรวจนี้.
- [CI PR #26](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37605901014) success; reviewers บันทึก backend339/339 ไม่มี skips, frontend117/117 และ guards6. ตัวเลขนี้เป็น **ผลคนละ head/PR** ไม่ใช่ผล local รอบนี้หรือ integrated develop.
- [Issue #23](https://github.com/PavaritPramual/buffet-restaurant-management-system/issues/23) ธีรเมธรับผิดชอบ: ข้อความ `No payment result is available for this dining session` ยังพบใน [ภาพ Staff unpaid close](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/core-flow/staff-unpaid-close-768.png). Logic ไม่ยอม close ก่อนชำระถูกต้อง; ข้อความ UX เป็น follow-up และไม่ block scope local tests/docs นี้. หลัง owner แก้ต้องตรวจซ้ำ.

## 7. สิ่งที่ต้องทำต่อก่อนปิด Final

| Gate | เจ้าของส่งงาน / สิ่งที่ศิระพัทธ์ตรวจต่อ |
|---|---|
| Stock target/active | เมธัสส่ง reviewed implementation/migration; ศิระพัทธ์ตรวจ validation/default/shortage/inactive/history/reactivate/UI |
| Profile | เมธัสส่ง firstName/lastName/phone + legacy fallback; ศิระพัทธ์ตรวจ form/required/length/compatibility |
| API/State audit PR #26 | ศรัณย์แก้ review blocker; ศิระพัทธ์ตรวจ tests/frontend/document source ของ head ใหม่; ปวริศช์รับรวมเข้า develop |
| Public runtime | ธีรเมธส่ง same-origin HTTPS Web/API, approved test accounts/data scope, providers/schema และ deployed SHA |
| Public release regression | ศิระพัทธ์ + ปวริศช์รัน integrated release ทั้ง Core Flow, roles/TTL/Secure cookies/deep links/Swagger/Stock/Profile พร้อม commit/time/logs/images |
| Review/release | ปวริศช์และศรัณย์ review PR นี้; ปวริศช์ยืนยัน main/release, ธีรเมธยืนยัน deployed commit; CI/merge ไม่แทน public acceptance |
| Slides/ชุดส่ง | นำตัวเลขของ Final revision จริงใส่ team deck; review/export/rehearsal โดยเจ้าของทั้งห้าคน; ร่างเก่าและตัวเลข CI303/116 เป็นประวัติ |

## 8. วิธีส่งและตรวจกลับ

ส่ง report นี้, updated test plan/traceability/checklist, runner assertions และ evidence ใน PR เข้า develop; ใส่ PR/report/CI link ใน [Regression task](https://app.notion.com/p/85290b8ff96482c59ba201e5d9a767fd) และ [Step 3](https://app.notion.com/p/a9e90b8ff9648363a6ab81b48fd70816). คง task เป็น **กำลังดำเนินการ** จนกว่าจะผ่าน gates ข้างต้น. ดู [evidence index/reproduction](../../test/evidence/sirapat-step3-api-state-regression-2026-10-07/README.md) และ [testing guide](../../test/README.md).

รายงาน PR #22 และผลก่อนหน้าเก็บเป็นประวัติ; ผลใหม่ในรายงานนี้ผูกกับ source/time/environment ข้างต้น. หาก develop เปลี่ยนจาก baseline ต้องประเมินส่วนที่เปลี่ยนและรัน integration ที่เกี่ยวข้องใหม่ก่อนอ้างเป็นผลของ release.
