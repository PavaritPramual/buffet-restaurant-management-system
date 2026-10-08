# รับรอง JPA โมดูลปวริศช์และทวนระบบหลัง merge

เจ้าของ ปวริศช์ · branch `pavarit_673380278-9_01` · 8 ตุลาคม 2026 (Asia/Bangkok)

## Revision และขอบเขต

- Code/runner ที่ทดสอบจริงคือ [develop adc5798](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/adc5798b05279840dc6178f4291278467c929791) หลังรวม PR #28/#31/#32 ไม่ใช่ candidate ก่อน merge
- คำรับรอง JPA และ Component source/SVG อยู่ที่ [commit 8efe5b5](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/8efe5b5ba78b7d676caab09f68242bcc2835078d). ระหว่างรัน browser มี documentation diff เท่านั้น; diff ของ runtime/runner เทียบ adc5798 ว่าง จึงใช้ executable จาก revision นี้ได้ตรงตาม canonical manifest
- Runtime Java 21.0.11, Node 24.19.0, Spring Boot 3.5.16, Chromium 154.0.8037.98. CI ใช้ Java17/Node24 จึงแยก environment ของผล local จาก CI
- ปิด `.env` import/demo seed. Automated tests ใช้ H2 กับ PostgreSQL18 container ใหม่บน loopback15435 พร้อม marked disposable databases และ roles จาก setup SQL ของ CI; ไม่เชื่อม Supabase หรือ apply migration กลาง
- Browser เริ่ม fresh H2 กับ session/database providers จริงและบัญชีสุ่มเฉพาะ memory แยก Customer สอง contexts, Staff, Kitchen, Manager และ Supervisor. Runtime ports5283/8183 ปิดหลังจบ; PostgreSQL container หยุดแล้ว

## คำรับรอง JPA และ diagram

[ตาราง JPA กลาง](../architecture/jpa-entity-rationale.md) รับรองเฉพาะ RestaurantTable, BuffetPackage, Soup, DiningSession และ CustomerSessionGrant พร้อม code/migration/tests แบบ SHA/บรรทัด

- Table/Package/Soup ไม่มี inverse session collections; Session ถือ FK table/package/soup ทั้งสามแบบ LAZY/optional=false ไม่มี JPA cascade
- FK ที่ถือโดย session ใช้ SQL RESTRICT เพื่อรักษาประวัติ; Package/Soup ปิด active แทนลบ การแก้ราคาไม่เปลี่ยน package_price_at_open
- Grant ถือ session_id แบบ LAZY ไม่มี JPA cascade; SQL ON DELETE CASCADE ของ FK ต่างจาก close ซึ่งเรียก repository เพื่อลบ grants ภายใน transaction และตรวจ expiry/ACTIVE ทุกคำขอ
- Domain relations ไป Orders/Payment ไม่ได้แปลว่ามี associations ใน Session Entity; แถว Order/Payment และโมดูลอื่นยังให้เจ้าของรับรอง
- [Diagram index](../diagrams/README.md) ตรวจ7รายการ: Component แก้ FlywayV14เป็นV15และ baselineadc5798 แล้ว render SVGใหม่; Class/Sequence/Domain/UseCaseอีก6ชุดตรงเดิมในscopeตน ไม่เปลี่ยน source/SVG. Maven validate ผ่านก่อน render PlantUML1.2025.0 และตรวจภาพ Component ใหม่แล้ว
- PR #31 review/merge ผ่านแล้ว ข้อความรอรีวิวเดิมในเอกสารโมดูลปรับเป็นประวัติ; รอบใหม่นี้ยังต้อง review/merge ของตน

## ผล automated tests ที่รันใหม่

| ชุด | ผล | ขอบเขต |
|---|---|---|
| Backend Maven verify | 348/348, failures0/errors0/skipped0 | H2 และ PostgreSQLแยก; ดูทุกsuiteในbackend-summary |
| PostgreSQL migration/JPA/security/concurrency | ทุกPostgres suiteทำงาน ไม่มีskip | fresh/upgradeถึงV15, marker/privileges และ Order–bill request–close/Payment–close; ไม่แทนฐานกลาง |
| Frontend Vitest | 121/121, 14files | component/API fixtures ตามtestเดิม |
| URL guard tests | 6/6 | public-runner configuration validation ไม่ใช่public browser |
| Lint / build | 0errors, 4warningsเดิม; buildผ่าน | warnings set-state-in-effect ของKitchenBoard/StaffServing/Users/Stock |
| Maven validate / PlantUML | ผ่าน | syntax/render/ภาพ Component; ไม่renderภาพที่ไม่เปลี่ยน |

DiningSessionIntegrationTest, SessionContextProviderContractTest, CustomerBillingContractTest และ Step2CompletionIntegrationTest อยู่ในbackend suiteนี้. DiningSessionIntegrationTest จำลอง PaymentStatusLookup ขณะที่ Step2Completion และ browser ใช้database providers; ไม่รวมผล fixture เป็น Payment runtime proof

## ผล browser หลังรวมจริง

รัน 19:19:09–19:20:06 น. Asia/Bangkok วันที่8ตุลาคม2026. [Runtime summary](../../test/evidence/pavarit-integrated-2026-10-08/browser/runtime-summary.json) ระบุ revision/providers/runner hashes และ exit0 ทั้ง5runners

| ส่วน | ผล | วิธีตรวจ |
|---|---|---|
| Core Flow | 16/16 | real HTTP/browser ไม่มีmocks; รวม12State denialsและpersisted status |
| Stock/Profile | 8/8 | UI/HTTPจริงหลังmerge; defaults/target/shortfall/IN/ADJUST/inactive/history/reactivation/Profile/role/validation |
| Customer/Staff/Kitchen/Manager states | 12/12 | controlled loading/empty/error แยกจากCoreFlowจริง |
| Delayed responses/concurrency UI | 3/3 | ควบคุมHTTP responses เพื่อทวนduplicate/stale response/remount; ไม่แทนPostgreSQL locking |
| Stock/Profile states | 10/10 | controlled loading/empty/error และlegacyfallback; legacy migrationจริงอยู่ในautomated suite |

CoreFlowผ่านเปิดโต๊ะ/QR single-use/สองcontexts/เปลี่ยนQR, Order→PREPARING→READY→SERVED, ปิดก่อนจ่ายถูกปฏิเสธ, ทุกgrantหยุดสั่งหลังbill-request, Bill/Paymentยอด997.50ตรงกัน, PAIDยังACTIVEจนกดclose, โต๊ะกลับAVAILABLEและเพิกถอนcookieสองเครื่อง รวมrole/header spoof/401/logout/Swagger

Server-side invalidationในrunnerไม่ใช่timedTTL expiry และสองbrowsercontextsไม่ใช่มือถือจริง. ชื่อภาพ `*-expired-*` จึงอ้างเพียงinvalidationที่รัน ไม่รับรองexpiryตามเวลา

Core runnerมีข้อความpendingของStock/Profileจากtemplateรอบเก่าคงอยู่ในraw JSON; ผลStock/Profileใหม่8/8และสถานะmergeอ้างจากรายงานนี้กับrunnerเฉพาะ ไม่แก้rawผลให้ดูผ่าน. sourceTree dirtyในrawหมายถึงdocumentation diffระหว่างรัน; canonical manifestกับruntime diffว่างเป็นหลักฐานประกอบ

## ภาพและ provenance

- เก็บ53ภาพใน [screenshot index](../../test/evidence/pavarit-integrated-2026-10-08/screenshots.json) ครอบคลุมCustomer360px, Staff/Kitchen768px และManager1280px; ตรวจcontact sheetsทั้ง5ชุดและComponentภาพใหม่ ไม่มีpassword/cookie/QR credentialในภาพ QRถูกmaskโดยrunner
- Canonical manifest348filesใช้Git blobs/modes/SHA256ของadc5798 ไม่เทียบCRLF checkout-byte hashesกับGit blob hashes. runner hashesในruntime-summaryเป็นcheckout bytes ณ เวลารันและแยกจากcanonical manifest
- ใหม่มี54source anchors และ7diagram source/SVG hash pairsที่ผูกกับdocs commit8efe5b5; รายงาน/manifestของPR#31/#32คงเป็นประวัติไม่แก้ย้อนหลัง
- ใช้ [verify.py](../../test/evidence/pavarit-integrated-2026-10-08/verify.py) ตรวจ SHA/anchors/links/images/counts/fixtures/runtime diff; ผลอยู่ใน [verification-result.json](../../test/evidence/pavarit-integrated-2026-10-08/verification-result.json)
- ไม่พบบั๊กruntimeที่ขัดflowรอบนี้ ไม่มีการแก้runtime/API/DTO/cookie/state/runner/migration; พบและแก้เอกสารJPA owner confirmationกับComponentV15เท่านั้น
- doc/learning21ไฟล์คงhashเดิม ไม่มีการstage/แก้ไฟล์ผู้ใช้

## ทำซ้ำ

1. Checkout codeadc5798 และใช้Java21/Node24. เปิดPostgreSQL18ทิ้งได้ ตั้งroles/ฐานที่มีmarkerด้วยdisposable-postgres-ci.sql และenvชุดMENU_TEST/DINING_TEST/PAYMENT_TESTตามCIให้ชี้loopbackฐานทิ้งได้เท่านั้น
2. รันbackend `mvnw --batch-mode --no-transfer-progress -Dspring.config.import= verify`; frontend `npm ci`, `npm test`, URLguard tests, `npm run lint`, `npm run build`
3. รันstep3-local-runtime.cjs โดยกำหนดPLAYWRIGHT_MODULE/FINAL_JAVA, portsว่าง และFINAL_LOCAL_SCRIPTSเป็นCoreFlow,StockProfile,UIStates,Concurrency,StockProfileStatesทั้งห้าชุด; บัญชีbootstrapชั่วคราวเฉพาะH2ของlauncher
4. รัน `python test/evidence/pavarit-integrated-2026-10-08/verify.py` จากcheckoutPRนี้ การตรวจsourceชุดรอบนี้ไม่เรียกverifyของPR31ซึ่งassertbaselineก่อนStock/Profile

## Gate ที่ยังไม่ผ่านจากรายงานนี้

- [ ] Review/merge PR JPAและintegrated evidenceรอบนี้
- [ ] ทวนreleaseหลังproductionsetupรวม และรับรองapplication JDBC/Flyway/JPA/checksum/schema/approvalฐานกลาง
- [ ] Public HTTPS/Swagger/Secure-cookie/timedTTL/coldstart/deployedSHA และCoreFlow/StockProfileบนpublicจริง
- [ ] เจ้าของOrder/Payment/โมดูลอื่นรับรองJPA/diagramของตน รวมREADME/Matrix/Git auditรุ่นส่ง
- [ ] Canva/export/ซ้อม12นาที/releaseเข้าmainและรับรองFinal
