# รายงานตรวจเอกสาร Table / Session / QR ของปวริศช์

ตรวจ 8 ตุลาคม 2026 (Asia/Bangkok) · เจ้าของ ปวริศช์ · branch `pavarit_673380278-9_01`

## Revision และขอบเขต

- Code baseline [6d83eac](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/6d83eace6bbd4d20f4d3cb3a25eb2d5ddb81fcc6) เป็น develop ที่ merge แล้ว ไม่รวม PR #28 ที่ยังเปิดตอนเริ่มตรวจ
- เอกสาร SOLID/JPA [d1ff4af](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/d1ff4afdfa2fb160c8aefa1efdc43992b1c7e6de) และ diagram [52d17fd](https://github.com/PavaritPramual/buffet-restaurant-management-system/commit/52d17fd409ed483e7c859840a05afc783aabb9be) คือสองชุดที่ตรวจ รายงาน/manifest เป็น commit ชุดถัดไป
- [เอกสารโมดูล](../architecture/pavarit-table-session-solid-jpa.md), [SOLID รวม](../solid-analysis.md), [Diagram index](../diagrams/README.md), [หลักฐาน structured](../../test/evidence/pavarit-module-docs-2026-10-08/validation.json)
- Windows; local Java 26.0.1, PlantUML 1.2025.0 ใช้ Java 21.0.11 แบบ headless; H2 memory แยก และปิด `.env` import ด้วย `-Dspring.config.import=`
- `git diff 6d83eace6bbd4d20f4d3cb3a25eb2d5ddb81fcc6 -- code` ว่าง โค้ด runtime/API/DTO/migration ไม่เปลี่ยน และไม่ได้ติดต่อหรือ apply migration บน Supabase

## ผลตรวจในเครื่องรอบนี้

รันจาก `code/backend` ก่อน render diagram

```powershell
.\mvnw.cmd validate
.\mvnw.cmd '-Dspring.config.import=' '-Dtest=DiningSessionIntegrationTest,SessionContextProviderContractTest,CustomerBillingContractTest,Step2CompletionIntegrationTest' test
```

`validate` ผ่าน และ targeted tests รวม **24/24** ผ่าน นับเฉพาะ XML สี่ suite นี้ ไม่รวมรายงานเก่าที่ค้างใน target

| Suite | Tests | Failures / errors / skipped |
|---|---:|---|
| DiningSessionIntegrationTest | 11 | 0 / 0 / 0 |
| SessionContextProviderContractTest | 3 | 0 / 0 / 0 |
| CustomerBillingContractTest | 3 | 0 / 0 / 0 |
| Step2CompletionIntegrationTest | 7 | 0 / 0 / 0 |

DiningSessionIntegrationTest ใช้ Mockito PaymentStatusLookup สำหรับ PAID/PENDING/ID ผิด จึงตรวจ close rule ไม่ใช่ Payment provider จริง ส่วน Step2CompletionIntegrationTest ใช้ database providers ตาม properties บน H2/MockMvc Contract tests ใช้ mocks/fixtures ตามชื่อ test ไม่ถือว่าเป็นหลักฐาน lock ของ PostgreSQL

## PostgreSQL concurrency — อ้าง CI ของ baseline

[CI run 37719753927](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37719753927) ผูกกับ code commit `6d83eace6bbd4d20f4d3cb3a25eb2d5ddb81fcc6`; Backend and PostgreSQL ผ่าน **339/339**, ไม่มี failures/errors/skipped โดย PostgresOrderCloseConcurrencyTest **8/8** และ PostgresPaymentIntegrationTest **15/15**

ตรวจชื่อ suite/จำนวนจาก job log จริง [Backend job](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37719753927/job/113124457269) ไม่ได้รัน PostgreSQL ใหม่ในงานเอกสารนี้ Concurrency suite ใช้ PostgreSQL จริงและ Mockito PaymentStatusLookup ตาม source ดังนั้นยืนยันการเรียง Order/bill-request/close ตาม lock ภายใต้เงื่อนไข tests ส่วน Payment provider มี integration suite แยก ไม่อ้างว่า isolated CI เป็น public runtime acceptance

## Source references และตัวอย่างโค้ด

ตรวจ source anchors **39 จุด** ด้วย `git show` จาก SHA ที่ลิงก์ระบุ ทุกไฟล์/บรรทัดตรงข้อความต้นฉบับ เก็บ path/line/needle ใน validation.json การตรวจนี้ยืนยันตำแหน่งอ้างอิง ส่วนคำอธิบาย contract/JPA ตรวจเทียบ source และ migration โดยตรง

ตัวอย่างจาก [CustomerBillingService บรรทัด 42](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/6d83eace6bbd4d20f4d3cb3a25eb2d5ddb81fcc6/code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java#L42) แสดงการตรวจ credential ก่อน lock และซ้ำหลัง refresh ไม่ใช่การเพิ่ม runtime code ใน PR นี้

```java
        access.requireSession(id, credential);
        var session = sessions.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Active dining session not found"));
        entityManager.refresh(session, LockModeType.PESSIMISTIC_WRITE);
        // Refresh the cookie check after locking: close may have committed while we waited.
        access.requireSession(id, credential);
```

`request()` มี `@Transactional` และใช้ session lock เดียวกับ close; ส่วนการสร้าง Order เรียก verifier ที่ refresh ภายใต้ PESSIMISTIC_WRITE ภายใน transaction ของ Ordering เมื่อขอคิดบิลแล้ว session ยัง ACTIVE แต่ไม่รับ Order ใหม่ อธิบาย scope และ tests ในเอกสารโมดูล

## Diagram ที่ตรวจ

ตรวจเจ็ดรายการเฉพาะโมดูลปวริศช์ เปลี่ยน source/SVG **ห้าภาพ** และ Use Case สองภาพตรงเดิมในขอบเขตนี้ รายละเอียด delta อยู่ใน Diagram index

| ภาพที่เปลี่ยน | PNG ที่ใช้ตรวจภาพ | ผล |
|---|---|---|
| [component](../diagrams/previews/component.svg) | 2339 × 746 | render/syntax และตรวจภาพผ่าน |
| [class-table-session](../diagrams/previews/class-table-session.svg) | 1606 × 1007 | render/syntax และตรวจภาพผ่าน |
| [sequence-open-session](../diagrams/previews/sequence-open-session.svg) | 1391 × 838 | render/syntax และตรวจภาพผ่าน |
| [sequence-qr-exchange](../diagrams/previews/sequence-qr-exchange.svg) | 1394 × 968 | render/syntax และตรวจภาพผ่าน |
| [domain-model](../diagrams/previews/domain-model.svg) | 1381 × 800 | render/syntax และตรวจภาพผ่าน |

ตรวจภาพ render ทุกภาพที่แก้แล้ว ข้อความอยู่ในกรอบและ cardinality/ลูกศร/transaction/response ตรง source ราคา snapshot เป็นข้อมูลภายใน Billing reader; Staff DTO มี QR token แต่ไม่มีราคา snapshot; Customer ใช้ credential cookie; start/end Entity ใช้ LocalDateTime แต่ API mapper ระบุ UTC

ตัวอย่างคำสั่งจาก root ใช้ PlantUML 1.2025.0 ในเครื่อง โดย `<java21>` และ `<plantuml.jar>` คือ absolute paths ของเครื่องผู้ตรวจ

```powershell
& '<java21>' '-Djava.awt.headless=true' -jar '<plantuml.jar>' -checkonly doc/diagrams/component.puml doc/diagrams/class-table-session.puml doc/diagrams/sequence-open-session.puml doc/diagrams/sequence-qr-exchange.puml doc/diagrams/domain-model.puml
& '<java21>' '-Djava.awt.headless=true' -jar '<plantuml.jar>' -nbthread 1 -tsvg -charset UTF-8 -o previews doc/diagrams/component.puml doc/diagrams/class-table-session.puml doc/diagrams/sequence-open-session.puml doc/diagrams/sequence-qr-exchange.puml doc/diagrams/domain-model.puml
```

Source/SVG hashes ใน validation.json ใช้ bytes จาก Git blob ของ diagram commit หลีกเลี่ยงความต่าง LF/CRLF ใน Windows ไม่ใช้ hash ของภาพ PNG ที่ไม่ได้ส่งเป็นหลักฐาน source

ตรวจ references ซ้ำได้จาก root ด้วย `python test/evidence/pavarit-module-docs-2026-10-08/verify.py` ผลรอบนี้ source anchors 39, source links 66, relative links 80, code snippet 1 และ diagram hash pairs 5 ผ่านทั้งหมด การตรวจ source links เก่าที่ยังคงเป็นประวัติยืนยันว่าไฟล์/บรรทัดมีอยู่ ไม่ใช้แทนการรับรองคำอธิบายของโมดูลอื่น

## ตรวจความครบและส่งต่อ

- [x] Source anchors/relative links/ตัวอย่างโค้ด/manifest ตรวจผ่านด้วย [verify.py](../../test/evidence/pavarit-module-docs-2026-10-08/verify.py)
- [x] `git diff --check` ผ่าน ตรวจ diff ไม่มี secrets/cookie/QR credential จริง ตัวอย่าง A ใน diagram เป็น placeholder
- [x] Commit จำกัดเอกสาร/diagram/evidence; `doc/learning/` 21 ไฟล์ของผู้ใช้คง untracked ไม่ stage หรือแก้ไข
- [x] C03/D05/L04/O02 ระบุ PR #25 ผ่าน review/merge แล้ว เก็บ baseline เก่าเป็นประวัติ; เอกสารรอบนี้มี gate review ของตน
- [ ] ศรัณย์ตรวจ architecture/contracts/JPA และศิระพัทธ์ตรวจ previews/references/tests ของ PR นี้
- [ ] ตรวจเอกสารอีกครั้งกับ Final release หลังรวม Stock/Profile/production setup และรับรองส่วนโมดูลอื่นโดยเจ้าของ
- [ ] Public deployment/HTTPS/Secure cookie/timed-TTL/รุ่นที่ deploy/สไลด์และซ้อม/release main ยังรอหลักฐานแยก

ไม่พบเหตุที่ต้องแก้ runtime ใน scope ที่ตรวจ งานเอกสารนี้ไม่ได้รับรองทั้งระบบหรือ public พร้อมส่ง และไม่มีการเปลี่ยน Payment/Stock/Profile ของเจ้าของอื่น หลังเปิด PR ให้รอ review ตามข้อตกลง
