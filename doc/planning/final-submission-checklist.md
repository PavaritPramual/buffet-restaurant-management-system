# Checklist ชุดส่ง CP353002 — 10 ตุลาคม 2026

ผู้จัดชุดส่ง ปวริศช์ ประมวล ใช้ merged `develop 5ff807625d3c0a78692068a644ad2a528e988cff` เป็นฐานโค้ด งานรอบนี้แก้เอกสาร/source diagram และเก็บ Canva รุ่นที่เจ้าของเลือก ไม่มี runtime หรือ migration เปลี่ยน

ใบงานที่ผู้ใช้ให้ใน workspace เป็นเกณฑ์หลัก โดยเฉพาะข้อ 4–6, 8–11 และ 14 รายงานรอบก่อนยังเก็บไว้เป็นประวัติ ตารางนี้เป็นสารบัญและสถานะล่าสุดของชุดส่ง ไม่ใช่คะแนนที่อาจารย์รับรอง

## ชุดเอกสารและโค้ด

| ข้อในใบงาน | ผลส่งมอบ | หลักฐานปัจจุบัน | สถานะ |
|---|---|---|---|
| 2–3 Stack / Layers | Spring Boot 3.5.16, Java17+, Maven, PostgreSQL/JPA, React; Controller → Service → Repository + DTO/Mapper | [README](../../README.md), [Component](../diagrams/previews/component.svg) | มีโค้ด/เอกสารและ CI |
| 4 SOLID ทุกข้อ | S/O/L/I/D พร้อมไฟล์/บรรทัดแบบ commit-pinned และเหตุผล/ข้อจำกัด | [SOLID](../solid-analysis.md), [Architecture](../architecture/pavarit-table-session-solid-jpa.md) | มีหลักฐานครบชนิด ไม่ใช้ interface อย่างเดียวรับรองทั้งระบบ |
| 5.1 Enterprise Patterns | Layered, MVC, Repository, Service, DTO/Mapper, Constructor DI | [Pattern table](../design-patterns.md) | ครบตามตาราง |
| 5.2 Behavioral 3 แบบ | State, Strategy, Template Method พร้อม Class diagrams | [Pattern table](../design-patterns.md), [State](../diagrams/previews/class-order-state.svg), [Strategy](../diagrams/previews/class-billing-strategy.svg), [Template](../diagrams/previews/class-stock-template.svg) | ครบกลุ่มเดียวกัน |
| 6 Database | >6 ตาราง, One-to-One/One-to-Many, FK/index/fetch/cascade, migrations V1–V19 | [ERD](../diagrams/previews/er-diagram.svg), [Dictionary + deltas](../database/step2-schema-approved.md), [JPA rationale](../architecture/jpa-entity-rationale.md) | มี source/หลักฐาน; startup V19 ดูรายงาน public |
| 7 API | CRUD ของ master resources, Validation/ErrorResponse, HTTP statuses, pagination/sort ของ menu-items, Swagger | [API conventions](../contracts/api-conventions.md), [Public Swagger check](../testing/readme-final-public-check-2026-10-10.md) | โค้ดและหลักฐานตรวจมี |
| 8 Git รายคน | branch จริงทั้ง5, non-merge author commits ใน develop พร้อม changed paths/date/coauthors | [Audit](step3-git-audit.md), [inventory](../../test/evidence/final-submission-2026-10-10/develop-commits.csv) | ทุกคนมีจำนวน ≥15; owner/account/explanation ไม่รับรองแทนเจ้าตัว |
| 9 โครงสร้าง | code/, test/, doc/, img/ | [Project Structure](../../README.md#project-structure) | ครบ |
| 10 README | สมาชิก5คน ชื่อ/รหัส/section/branch/งาน, Stack, Architecture, DB, Setup/Run, API/Tests, Deploy URL, Structure | [README](../../README.md) | ครบหัวข้อ |
| 11 Deployment | Docker production, Compose, Render HTTPS URL เดียว, Supabase pooler, CI | [Runtime diagram](../diagrams/deployment-production-runtime.md), [public UAT](../../test/evidence/uat-buffet-2026-10-10/report.md) | มีผล public จริงตาม revision; การ merge ไม่เปลี่ยน Render source branch เอง |
| 12 สมาชิกและหน้าที่ | ทุกคนมี code contributions, README แยกเจ้าของโมดูลและงาน integration | [README สมาชิก](../../README.md#สมาชิกกลุ่ม) | มีหลักฐาน Git/code |
| 14 Tests + reports | CI H2/PostgreSQL/concurrency + frontend/guards/lint/build; local/public UAT แยกตามรุ่น | [Stock regression](../testing/pavarit-menu-stock-report-2026-10-10.md), [public UAT](../../test/evidence/uat-buffet-2026-10-10/report.md), [Swagger fix](../../test/evidence/pr52-swagger-fix-2026-10-10/report.md) | มีผลตรวจ; ไม่อ้าง CI เป็น public UAT |
| 14 Slides ใน doc/slide | Canva DAHXmHjoiFU 24 หน้า ตามลิงก์ที่ปวริศช์เลือก + PDF/PPTX export จริง | [Canva](https://canva.link/43kx8nvyrmulyam), [PDF](../slide/team-final-canva-v4-2026-10-10.pdf), [PPTX](../slide/team-final-canva-v4-2026-10-10.pptx), [ข้อความต้นฉบับ](../slide/team-final-canva-v4-2026-10-10.txt) | เก็บรุ่นปัจจุบันแล้ว ไม่แก้เนื้อหาในรอบนี้ |

## Diagram ตามข้อ 9.1

PlantUML 28 sources พร้อม SVG 28 ภาพ รวมภาพย่อ5ภาพ ทุกไฟล์ตรวจ syntax และ manifest ระบุ hash แยก source/preview ชุดนี้ไม่มี source ขาด preview

| ชนิดบังคับ | Source และภาพหลัก | การตรวจล่าสุด |
|---|---|---|
| Use Case + Description | [รวมทุก actor](../diagrams/use-case.puml), [Manager](../diagrams/use-case-management.puml), [Service/Customer](../diagrams/use-case-service-customer.puml), [Descriptions](../system-design/use-cases.md) | แก้ account lifecycle/recipe ให้ตรง V16–V19; actor rights คงเดิม |
| Domain / Conceptual Class | [Domain](../diagrams/domain-model.puml) | เติม archive fields และแยกสูตรปัจจุบัน/สูตรตอนสั่ง |
| Class + Pattern locations | [Table/Session](../diagrams/class-table-session.puml), [Auth](../diagrams/class-auth.puml), [Menu/Order](../diagrams/class-menu-order.puml), [State](../diagrams/class-order-state.puml), [Strategy](../diagrams/class-billing-strategy.puml), [Template](../diagrams/class-stock-template.puml) | เติม lifecycle ตาม Entity/service จริง; Class Pattern ทั้ง3มี source+SVG |
| Sequence ≥3 | [Open session](../diagrams/sequence-open-session.puml), [QR exchange](../diagrams/sequence-qr-exchange.puml), [Order/Kitchen](../diagrams/sequence-ordering-kitchen.puml), [Billing/Payment/close](../diagrams/sequence-billing-payment.puml) | มี4 scenarios; transaction/lock, single-use QR, snapshot และ separate close |
| Activity | [Customer](../diagrams/activity-customer-ordering.puml), [Kitchen](../diagrams/activity-kitchen.puml), [Payment/close](../diagrams/activity-payment-close.puml), [Stock](../diagrams/activity-stock.puml) | มี4 flows; kitchen shortage rollback และ role/state guard |
| ER / Schema | [ERD](../diagrams/er-diagram.puml) | ครอบคลุม V1–V19; ลบคำว่า V19 ยังรอ approval ใน title |
| Component + Deployment | [Component](../diagrams/component.puml), [Local](../diagrams/deployment-local.puml), [Production design](../diagrams/deployment-production-design.puml), [Observed runtime](../diagrams/deployment-production-runtime.md) | แยก design กับผล runtime จริง;ไม่ใช้ภาพ design รับรอง deployed SHA |
| State | [Order State](../diagrams/state-order.puml) | RECEIVED → PREPARING → READY → SERVED; ไม่เพิ่มสถานะธุรกิจ |

## เกณฑ์ก่อนส่งที่ต้องใช้หลักฐานจริง

- [x] ตรวจเอกสาร/diagram/โฟลเดอร์/README ตามรายการใบงานและเก็บ Git inventory
- [x] เก็บ Canva รุ่นที่ผู้ใช้เลือกใน repo เป็น PDF/PPTX จาก export จริง
- [x] มี CI และ public UAT/Swagger reports ตาม revision พร้อมระบุข้อจำกัด
- [ ] PR เอกสารชุดสุดท้ายผ่าน reviewer ในทีมและ merge เข้า develop
- [ ] Release PR develop → main ผ่าน reviewer และ merge แล้ว
- [ ] ระบุ final main SHA เทียบกับ source ที่ Render ใช้จริงและตรวจ public URL วันส่ง
- [ ] สมาชิกอธิบายส่วนของตนและซ้อมนำเสนอจริง (agent ไม่ติ๊กแทนการซ้อม)

### การใช้หลักฐานหลาย revision

Public recipe UAT ทดสอบ code `af2b45b`; หลังจากนั้นถึง baseline `5ff8076` มี runtime source เปลี่ยนเฉพาะ Swagger response annotations และ ErrorResponse examples พร้อม regression tests ใน PR #52 ไม่มี Service/Entity/recipe/payment/lock เปลี่ยน รายงาน Swagger สาธารณะบน code baseline `855a954` ตรวจการแก้นี้แล้ว เอกสารชุดนี้ไม่ยกจำนวน tests ของ revision เก่ามาเป็นผลที่รันใหม่

V19 ไม่ถูก apply ซ้ำในรอบนี้ Startup log เจ้าของระบบใน PR #52 แสดง validate19/schema19/JPA; ไม่มีการใช้ Flyway repair หรือแก้ไฟล์ migration ที่ apply แล้ว

### สิ่งที่ยังไม่รับรองจากการมีเอกสาร

ไม่ใช้จำนวน commits แทนความหมาย/บัญชีผู้เขียน ไม่รับรอง timed8h expiry บน public จาก isolated tests ไม่อ้างมือถือสองเครื่องหรือกล้องจริงเมื่อใช้ browser/cookie contexts และไม่อ้างว่า professor ตรวจหรือให้คะแนนแล้ว การ merge เอกสารไม่แทนการส่งลิงก์ในระบบรายวิชา

## ผลตรวจฐานกลางแบบอ่านอย่างเดียว 10 ตุลาคม 2026

ตรวจ flyway_schema_history พบ V1–V19 success และคำนวณ checksum จาก common + PostgreSQL migrations ตรงครบ19 ไม่มี repair/apply; V9 มี installed_rank11 ตามประวัติที่เก็บไว้ [History](../../test/evidence/final-submission-2026-10-10/flyway-history.json) · [Checksum comparison](../../test/evidence/final-submission-2026-10-10/flyway-checksums.json) การอ่านผ่าน connector ไม่รับรอง JDBC runtime role
