# Requirement Matrix — Step 3

เอกสารปรับ 7 ตุลาคม 2026: C03/C05/C06/C08 ใช้ refactor code `de7b5d546a05ad3ccef8c641ee5c53d638a8e039` และ [รายงานล่าสุด](../testing/architecture-refactor-report.md); ข้ออื่นคงหลักฐานเดิมตาม SHA ที่แต่ละแถวระบุ รอ reviewer/release รับรอง

ประวัติฐานเริ่มต้น ตรวจ 7 ตุลาคม 2026 จาก code baseline `472fba4f25a27fa2e3cd1e1213151ce971646f3a` และ CI [37503690105](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105) เอกสารชุดนี้รอ PR review ไม่ใช่การรับรอง release/public deployment

## Sources และกติกาสถานะ

เกณฑ์ปัจจุบันขั้นต่ำ **5 meaningful commits ต่อคน** ตาม [การปรับที่ปวริศช์แจ้ง](course-criteria-updates.md) ส่วนใบงานต้นฉบับ15และauditเก่าเก็บเป็นประวัติ

- ใบงานรายวิชาที่ปวริศช์ให้ใน workspace `ใบงานโปรเจค_ CP353002 Principles of Software Design and Development (Spring Boot).md` (SHA-256 `30ef76f8fbda34deca6f9349af34849eb6c1874b14bae2d826611a35d8e3ebb0`) ข้อ 3–11 เป็นแหล่งเกณฑ์หลัก ต้นฉบับไม่ได้อยู่ใน tracked repo จึงไม่สร้างลิงก์ที่เปิดไม่ได้
- [Requirements](https://app.notion.com/p/3cfcb2e9d47a81ed9ad9d2abb8a174fd) และ [Course Audit](https://app.notion.com/p/3ddcb2e9d47a816bae8ccc995ca92d6a) ตรวจ 7 ต.ค. หน้าหลังเป็น audit design เดือนกันยายน ต้องอ่าน matrix นี้เพื่อดู code ล่าสุด
- [Step 3](https://app.notion.com/p/3f1cb2e9d47a81e28aa2dc642cd6ead6), [Tasks](https://app.notion.com/p/822603f5de7247e68cfe7f50b378fe4f), [System Design](../system-design/README.md), [Contracts](../contracts/shared-contracts.md), [Step 2 report](../testing/pavarit-step2-completion-report.md)

`มี implementation/CI` คือหลักฐาน code/local/isolated DB ของ baseline `จัดทำใน PR นี้` ต้อง reviewer รับรอง `บางส่วน` ยังมีช่องว่าง `รอ` ยังไม่มีหลักฐานรับรองครบ ไม่ใช้ diagram หรือ fixture เป็นหลักฐาน public system

## เกณฑ์รายวิชา

| ID / ข้อกำหนด | หลักฐานจากโค้ดหรือไฟล์ | เจ้าของ / reviewer | สถานะและสิ่งที่ยังขาด |
|---|---|---|---|
| C01 Spring Boot3.x Java17+ Maven | [pom.xml:9](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/pom.xml#L9); Maven wrapper | ปวริศช์ / ศรัณย์ | มี implementation/CI Spring Boot3.5.16 Java17 |
| C02 SQL JPA ≥6tables 1:1/1:N | [UserProfile.java:19](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/domain/UserProfile.java#L19); [CustomerOrder.java:39](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/domain/CustomerOrder.java#L39); [V1–V14 dictionary](../database/step2-schema-approved.md) | เมธัส + Entity owners / ปวริศช์ | มี migration/JPA/CI; Stock/Profile extensions ใหม่ยังรอ |
| C03 Controller→Service→Repository | [UserSessionKeys.java:5](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/common/UserSessionKeys.java#L5) / UserContextProvider / [Component](../diagrams/component.puml) | ปวริศช์ / ศรัณย์ | แก้ service-to-controller import แล้ว; source check/tests ผ่าน รอรีวิว PR refactor ไม่รับรอง Final |
| C04 DTO/Mapper MVC API | [DiningSessionMapper.java:10](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/mapper/DiningSessionMapper.java#L10); [OrderingMapper.java:13](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/mapper/OrderingMapper.java#L13) | ทุก owner / ศรัณย์ | มี implementation; React View ใช้ API แยก ไม่ใช่ Thymeleaf MVC |
| C05 SOLID S/O/L/I/Dมีcode+file+line | [SOLID](../solid-analysis.md) และ [report](../testing/architecture-refactor-report.md) | ทุกคนยืนยัน / ศรัณย์ | G01–G05/L04 implement/tests ผ่านในขอบเขตรอบนี้ รอ review/owner confirmation และ release ใหม่ |
| C06 Constructor injection เท่านั้น | [CustomerBillingService.java:30](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java#L30) / UserContextProvider / BillCalculator / StockTransactionProcessor | ปวริศช์ / ศรัณย์ | EntityManager และ cookie/CORS settings ผ่าน constructor; ไม่มี production field injection ใน source check รอ review |
| C07 Enterprise patternsครบ6 | [Pattern matrix](../design-patterns.md); Controller/Service/JPA/DTO/config | ปวริศช์ / ศรัณย์ | มี implementationและเอกสาร PR นี้; ไม่อ้างว่า D ผ่านหมด |
| C08 Behavioral ≥3แบบกลุ่มเดียว | [RegistryOrderStateResolver.java:13](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/de7b5d546a05ad3ccef8c641ee5c53d638a8e039/code/backend/src/main/java/com/buffetrestaurant/service/state/RegistryOrderStateResolver.java#L13) / [Patterns](../design-patterns.md) | ศรัณย์/ธีรเมธ/เมธัส ปวริศช์รวม | State registry, Strategy และ Template Method มีโค้ด/tests/diagrams; รอ owners/release รับรอง |
| C09 FK/index/cascade/fetch rationale | [canonical dictionary](../database/step2-schema-approved.md); [Menu JPA](../architecture/sirapat-menu-ordering-solid-jpa.md); [Session rationale](../system-design/README.md#jpa-ของ-table-session) | ทุก Entity owner / เมธัส+ปวริศช์ | บางส่วน ยังรอเจ้าของ Payment/Auth/Stock ยืนยัน rationale ทั้งชุด |
| C10 CRUD≥2resources | [RestaurantTableController.java:55](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/RestaurantTableController.java#L55); [BuffetPackageController.java:46](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/BuffetPackageController.java#L46); Menu/Soup controllers | ปวริศช์+ศิระพัทธ์ / ศรัณย์ | มี API/UI/CI Package/Soup ลบเป็น inactive; โต๊ะมีประวัติลบไม่ได้ |
| C11 HTTP200/201/204/400/404/409/500 ErrorResponse | [GlobalExceptionHandler.java:19](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/exception/GlobalExceptionHandler.java#L19); [API convention](../contracts/api-conventions.md) | ศรัณย์ / ปวริศช์ | มี code/tests ต้อง Final Swagger/API audit รวม endpoint ใหม่ |
| C12 BeanValidation/@Valid | [DiningSessionController.java:32](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/DiningSessionController.java#L32); [OpenDiningSessionRequest.java:8](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/dto/request/OpenDiningSessionRequest.java#L8) | ทุก owner / ศรัณย์ | มี CI; Stock/Profile ใหม่ต้องตรวจเพิ่ม |
| C13 Pagination/sorting≥1 | [MenuItemController.java:31](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/code/backend/src/main/java/com/buffetrestaurant/controller/MenuItemController.java#L31); PageResponse/MenuCatalogIntegrationTest | ศิระพัทธ์ / ศรัณย์ | มี implementation/CI |
| C14 Swagger/REST resource URLs | [contracts](../contracts/api-conventions.md); OpenApiConfig; /swagger-ui.html | ศรัณย์ / ปวริศช์ | local Swagger มี; public URL ยังรอ |
| C15 tests+report | [CI](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105); [PR22 report](../testing/sirapat-step3-premerge-report.md) | ศิระพัทธ์+ทุก owner / ปวริศช์ | ล่าสุด refactor backend336 frontend116 guards6 ผ่าน ดู architecture report; Final release/public/TTL ยังรอ |
| C16 personalbranch/account/≥5meaningful commits | [Git audit](step3-git-audit.md) | ทุกคน ปวริศช์ audit / ศิระพัทธ์ | branches/account history พบจริง; ยังมีสมาชิกยังต้องประเมินความหมายตามขั้นต่ำ5 และดูจำนวนล่าสุดในGit audit |
| C17 PR/reviewer/code contribution | [PR inventory](step3-git-audit.md#pr-และ-reviewer-history) | ทุกคน / เพื่อน reviewer | มี code/PR history ไม่แทนการยืนยันทุกคนอธิบายงานได้ |
| C18 code/test/doc/img | [root tree](https://github.com/PavaritPramual/buffet-restaurant-management-system/tree/472fba4f25a27fa2e3cd1e1213151ce971646f3a) | ปวริศช์ / ศิระพัทธ์ | โครงสร้างมีครบ img เป็นที่เก็บสื่อไม่ใช่หลักฐาน diagram ครบ |
| C19 UseCase+Description | [diagrams](../diagrams/use-case.puml); [descriptions](../system-design/use-cases.md); [slide coverage](../slide/course-diagram-coverage.md) | ปวริศช์ / ศรัณย์ | ปรับ5actors/UC10งานตรงcode มีsource/SVG/Canva รอowner/releaseconfirmation |
| C20 Domain/Class | [index](../diagrams/README.md); [Patterns](../design-patterns.md) | ปวริศช์+owners / ศรัณย์ | Domain/Classทุกโมดูลพร้อมpatternpositions source/SVG/Canva รอpeer review |
| C21 Sequence≥3 Activity ER+Dictionary | [index](../diagrams/README.md); [dictionary](../database/step2-schema-approved.md) | ทุกowner / ปวริศช์ | 4scenarios/4Activities/ERDตรงbaseline มีsource/SVG/Canva รอreleaseconsistency |
| C22 Component/Deployment/State | [index](../diagrams/README.md) | ปวริศช์/ธีรเมธ/ศรัณย์ | ครบsource/SVG/Canva Localruntimeกับpublicdesignแยกกัน Publicdeploymentจริงยังรอ |
| C23 READMEทุกหัวข้อ | [README](../../README.md) | ปวริศช์ / ศิระพัทธ์ | จัดทำ PRนี้ publicURLยังระบุ pending ตามจริง |
| C24 Dockerfile/Compose/publicdeploy | [Compose](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4f25a27fa2e3cd1e1213151ce971646f3a/docker-compose.yml); backend/frontend Dockerfiles | ธีรเมธ / ปวริศช์ | local Composeมี ยังรอ production singleURL build+HTTPS+rollback |
| C25 Slide/doc/slide/ | [Canva guide](../slide/README.md); [v02content](../slide/team-final-canva-v02-content.md); [12minutes](../slide/team-final-12-minute-runbook.md) | ทุกคนปวริศช์รวม / ศิระพัทธ์ | 20หลัก+56ภาคผนวก 35codepages/28diagramimages อ่านกลับครบ76notes/704editabletexts; รอowner/CanvavisualQA/PDF/release/ซ้อม ร่างยังไม่รับรอง; เก็บPPTX/PDFที่exportจากCanvaตามเวอร์ชัน |
| C26 deployจริงทุกคนอธิบาย/release | [Final plan](step3-final-plan.md) | ทุกคน ปวริศช์ประสาน | รอ publicregression/release reviewed/main/deployedSHA/ซ้อม ส่งURLจริง |

## System scope ที่เลื่อนไป Final และ blocker

| สิ่งที่เหลือ | เจ้าของ | หลักฐานที่จะปิด |
|---|---|---|
| Stock target/active และ Profile firstName/lastName/phoneNumber | เมธัส | forward migration ที่รับรอง tests/API/UI/code+ประวัติเดิมอยู่ |
| Production Render URL เดียว HTTPS/SPA/API404/Securecookie/restart | ธีรเมธ | publicURLs runbook DeploymentDiagram deploySHA/real browser |
| Constructor-only DI, service dependencies และ Auth constant ใน C03/C06 | ปวริศช์ร่วมowners ศรัณย์review | แก้และ tests ผ่านใน PR architecture ถัดจาก PR24; รอ reviewer ก่อนปิด gate |
| State/API/date/enum/price audit | ศรัณย์ | report/diagram/Swaggerตรงrelease |
| Public regression/timedTTL/StockProfileใหม่ | ศิระพัทธ์ | report latest release contexts+roles+negativecases |
| Gitเกณฑ์รายคน สมาชิกยืนยัน สไลด์รวม release | ทุกคน ปวริศช์ประสาน | auditสุดท้าย +slides +reviewed release/main/deploySHA |

รายการไม่ได้ถ่วงน้ำหนักเป็นคะแนนหรือ percent เพราะไม่มีคะแนนรายข้อที่ยืนยันครบ ไม่ติ๊ก Final ทั้งระบบจาก PRเอกสารชุดเดียว
