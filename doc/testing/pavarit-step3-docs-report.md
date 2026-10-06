# ปวริศช์ — Step3 documentation verification

## เปลี่ยนแนวทางสไลด์ — อัปเดต7ตุลาคม2026

ใช้Canvaเป็นต้นฉบับและเครื่องมือพรีเซนต์ของทั้งทีม PDFต้องexportจากCanvaและเก็บแต่ละเวอร์ชัน Reviewerไม่ต้องตรวจPPTX/PDF/notesร่างเดิม ดู [สถานะสไลด์ปัจจุบัน](../slide/README.md) และ [ร่างชุดทีม40หน้า](../slide/team-final-canva-content.md)

เนื้อหาทีม32หน้าหลัก+8ภาคผนวกพร้อมnotes/owner/107referencesแล้ว การส่งไฟล์เข้าCanvaถูกautomaticapprovalreviewปฏิเสธ จึงรออนุมัติเฉพาะไฟล์ ไม่อ้างว่าCanva/PDFexport/visualQAเสร็จ ผลPPTX/PDF6หน้าด้านล่างเป็น **ผลประวัติก่อนเปลี่ยนแนวทาง** ไม่ใช่เกณฑ์รับรองสไลด์รุ่นทีม

ตรวจ 7 ตุลาคม2026 Code baseline `472fba4f25a27fa2e3cd1e1213151ce971646f3a` หลัง PR#22 merge งานรอบนี้เป็นเอกสารและสไลด์ ไม่เปลี่ยน runtime/API/DTO/Entity/migration/CI และไม่เชื่อม Supabase เพื่อรัน migration

## สิ่งส่งมอบ

- [Requirement Matrix](../planning/step3-requirement-matrix.md) 26รายการ ครอบคลุมเกณฑ์จากใบงาน/Requirements/CourseAudit พร้อมเจ้าของและช่องว่าง
- [Git audit](../planning/step3-git-audit.md) และ [commit CSV](../../test/evidence/pavarit-step3-docs-2026-10-07/git-commits.csv) ตรวจทั้ง5personalbranches+develop และPR/reviewerhistory
- [Componentและdiagram index](../diagrams/README.md) 9source/9SVG ส่วนTable/Session/QR/billrequest/payment/closeและpatternตรงcode
- [System Design](../system-design/README.md), [SOLID](../solid-analysis.md), [Patterns](../design-patterns.md), [README](../../README.md)
- สไลด์ปวริศช์6หน้า [editablePPTX](../slide/pavarit-step3-architecture.pptx), [PDF preview](../slide/pavarit-step3-architecture.pdf), [speaker notes](../slide/pavarit-step3-architecture-notes.md) ไม่แทนสไลด์ทีม5คน

## การตรวจรอบนี้

| ส่วน | วิธีตรวจ / ผล |
|---|---|
| Source references | ตรวจลิงก์ไฟล์/บรรทัดพร้อมneedle63จุดเทียบcodeตรึงSHAและworkingtree ผลเหมือนbaseline ไม่อ้างfuture-release |
| Markdown | ตรวจlocal pathของMarkdownที่เพิ่ม/แก้ รวมplan/report/notes ไม่มีmissing path; จำนวนสุดท้ายอยู่ verification.json ExternalURLsไม่ได้crawlทั้งหมด |
| UML | PlantUML1.2025.0 กับJava21.0.11 สร้าง9SVG exit0 ไม่มีSyntaxError Smetanaสำหรับclass/component ไม่ต้องGraphvizเพิ่ม Sequenceใช้rendererปกติ |
| UML visual | ตรวจPNGของ9diagrams ปรับComponentที่กว้างเกินจนอ่านยากให้เป็นlayers/modules/providers และแก้ชื่อmethod/QRHTTPstatusให้ตรงsource |
| PPTX integrity/layout | first-partyArtifactTool export/import ตรวจ6slides+6notes ขนาด1280×720 equivalent16:9 NotoSansThai fontpolicyผ่าน package/layoutไม่มีfindings/warnings |
| PPTX visual | RenderจากfinalizedPPTXแล้วตรวจทุกหน้า แก้arrowheadให้ชี้ทิศflowและข้อความSOLIDก่อนออกรุ่นสุดท้าย Text/shapes/connectorsแก้ได้ ไม่ใช้rasterdiagramในPPTX |
| PDF | 6pagesจากPPTXrender Renderซ้ำด้วยPoppler144dpi ตรวจทุกหน้าและเทียบpixelsกับPPTXrender ความต่างเฉลี่ยเล็กจากresampling รายค่าดูverification.json |
| Diff | runtime changes=0 ไม่มี.env/secrets/temp-generator/runtimepackages/JAR/fontdownloadในtracked diff git diff --checkผ่าน |

ไฟล์ผลตรวจ sanitized และSHA256ของPPTX/PDF/SVGอยู่ [verification.json](../../test/evidence/pavarit-step3-docs-2026-10-07/verification.json) ไม่มีcookies/passwords/QRcredentialจริง บัญชีadmin/admin123ในREADMEเป็นdemoaccountที่ประกาศในsourceอยู่แล้ว ไม่ใช่secretproduction

PPTXไม่ได้embedfont ผู้เปิดต้องติดตั้งNotoSansThai PDFเป็นภาพpreviewจึงไม่ค้นหา/แก้ข้อความ ไม่มีการเปิดMicrosoftPowerPointจริงเพื่อnativeQA ผลpackage/layout/renderไม่ใช่การรับรองnativePowerPointทุกเครื่อง

## Tests ที่อ้างและขอบเขตหลักฐาน

[CI37503690105](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105) ของdevelop472fba4 ผ่าน backend/PostgreSQL303/303 ไม่มีskip frontend116/116 URLguards6/6 lint0errors/4existingwarnings และbuild

PRเอกสารนี้ **ไม่ได้รันbackend/frontend suiteใหม่** เพราะไม่มีsource/configที่เปลี่ยน CIจะตรวจPRตามworkflowเมื่อเปิด ใช้ผลbaselineเพื่ออ้างcodeimplementation ไม่เอาผลbaselineแทนFinalreleaseใหม่

Browser14PASS/0FAILและ17ภาพใน [รายงานศิระพัทธ์](sirapat-step3-premerge-report.md) เป็นlocalisolatedH2/realHTTPcookies ก่อนmerge testedpatch/sourcehashตรงhead617d742 ไม่ใช่publicdeploymentหรือbrowserrerunหลังmerge FixturesUIรอบก่อนมีlabelsแยก

## ข้อที่ยังไม่ผ่านก่อนFinal

- `CustomerBillingService` field-injectedEntityManager, `SessionUserContextProvider`อ้างAuthControllerconstant และconcreteService dependencies ดูSOLIDG01–G04 ต้องแก้code/reviewแยก เจ้าของปวริศช์ร่วมเมธัสและmoduleowners ศรัณย์review
- Gitcandidatecountsก่อนชุดdocs: ปวริศช์44 ศิระพัทธ์22 ศรัณย์3 ธีรเมธ8 เมธัส5 นับrootที่มีfilesด้วย ไม่ใช่meaningfulqualification ทุกคนต้องยืนยันบัญชี/ความหมาย/การกระจายเวลา สมาชิก3คนยังต่ำกว่า15 ไม่เพิ่มcommitsเพื่อเติมยอด
- Stocktarget/active/Profileละเอียด, ownerJPA/patternconfirmation, DeploymentDiagram/RenderHTTPS/Securecookies/SPA404/restart/coldstart/runbook
- Public/timedTTL/regressionของreleaseล่าสุด สมาชิกอีก3คนส่งslides รวมทีม/ซ้อม/releasePRเข้าmain/deployedSHA

เอกสารรอบนี้รอศรัณย์และศิระพัทธ์review ไม่mergeเอง Notionติ๊กเฉพาะผลจัดทำ/ตรวจที่ผ่าน พร้อมช่องreview/งานรวม/public/releaseที่ยังว่าง
