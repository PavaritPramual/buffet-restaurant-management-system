# ปวริศช์ — Step3 documentation verification

## รอบแก้รีวิว PR #24 — v02b ฉบับล่วงหน้า

ดู [รายงานแก้รีวิวและหลักฐาน Supabase สด](pr24-review-fixes-report.md) และ [Canva v02b](https://www.canva.com/d/yWw6P3disBOSfHS) แก้ข้อเท็จจริง4หน้าโดยคงลำดับ/ผู้พูด ยังไม่รับรองร่าง รายงาน v02/v01 ด้านล่างเป็นประวัติ ไม่ใช่ผลรับรอง design ใหม่

## ประวัติ v02 — diagram/code/12นาที/เกณฑ์commit5

[Canva v02](https://www.canva.com/d/wORhypcIcwvGuFK) มี20หน้าหลักสำหรับ12นาทีและ56ภาคผนวก 35หน้าcodeจากdevelop472fba4 ภาพ28ชุดรวม23diagramsรายละเอียด+5ภาพย่อ ครบUseCase+Description Domain Class+Patterns Sequence4scenarios Activity ERD Component Deployment State ดู [coverage](../slide/course-diagram-coverage.md) และ [runbook](../slide/team-final-12-minute-runbook.md)

PlantUMLrenderทั้งหมดไม่มีsyntaxerrors ตรวจภาพlocalทุก76หน้าและgeometryผ่าน ฟอนต์Sarabun/JetBrainsMonoโหลดครบ นำเข้าCanvaแล้วอ่านกลับtitle/points/code/branch/notesและdiagramครบ76หน้า พบ704editabletextsและ28imageelements ไม่มีelementออกนอกcanvas [ผลตรวจ](../../test/evidence/pavarit-step3-docs-2026-10-07/canva-v02-verification.json) ภาพdiagramเป็นSVGimage ไม่อ้างว่าnodesเป็นCanvashapesแก้แยกได้

รายงานฉบับนี้บันทึกการใช้ขั้นต่ำ 5 meaningful commits ตามการแจ้งวันที่ 7 ต.ค. ซึ่งถูกยกเลิกวันที่ 8 ต.ค.; เกณฑ์ปัจจุบันคือ 15 ตาม [บันทึกเกณฑ์](../planning/course-criteria-updates.md). เก็บ candidates ก่อน commit v02 (50/22/3/8/5) เป็น historical snapshot ไม่ใช้แทน audit ล่าสุด; ทุกคนยังต้องยืนยันความหมาย/บัญชี/เวลา

ยังไม่รับรองCanvaallpagevisualQA owner/PDFexport/rehearsal/public/release ไม่มีruntimechangeหรือbackend/frontendtestsใหม่ ไม่มีSupabasemigration ไม่merge PR24 ส่วนDeploymentpublicเป็นdesign ไม่ใช่deployedcertificate

[Source/link check v02](../../test/evidence/pavarit-step3-docs-2026-10-07/canva-v02-source-check.json) ตรวจcode35snippetsกับGit SHAจริง SVG/source28ชุด และlocalMarkdown285ลิงก์ผ่าน ไม่พบruntime/secret pathsในdiff

## ประวัติการตรวจv01และร่างPPTXก่อนเปลี่ยนแนวทาง


## เปลี่ยนแนวทางสไลด์ — อัปเดต7ตุลาคม2026

ใช้Canvaเป็นต้นฉบับและเครื่องมือพรีเซนต์ของทั้งทีม PDFต้องexportจากCanvaและเก็บแต่ละเวอร์ชัน Reviewerไม่ต้องตรวจPPTX/PDF/notesร่างเดิม ดู [สถานะสไลด์ปัจจุบัน](../slide/README.md) และ [ร่างชุดทีม40หน้า](../slide/team-final-canva-content.md)

ผู้ใช้อนุมัติการนำเข้าแล้ว [Canva ทีม v01](https://www.canva.com/d/D0dhcGCjuTTS7pV) มี32หน้าหลัก+8ภาคผนวก อ่านกลับยืนยันข้อความและnotesทั้ง40หน้าตรงร่าง พบข้อความแก้ไขได้535รายการและไม่มีกรอบข้อความออกนอกcanvas1920×1080 ยังไม่ได้ตรวจภาพทุกหน้า/owner review/PDF export เครื่องมือภาพเปิดURLpreviewไม่ได้และCanva connectorไม่มีexportPDF จึงคงgateเหล่านี้ไว้ ผลPPTX/PDF6หน้าด้านล่างเป็น **ผลประวัติก่อนเปลี่ยนแนวทาง** ไม่ใช่เกณฑ์รับรองสไลด์รุ่นทีม

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
- Gitcandidatecountsก่อนชุดdocs: ปวริศช์44 ศิระพัทธ์22 ศรัณย์3 ธีรเมธ8 เมธัส5 นับrootที่มีfilesด้วย ไม่ใช่meaningfulqualification ทุกคนต้องยืนยันบัญชี/ความหมาย/การกระจายเวลา จำนวนนี้เป็นประวัติก่อนอาจารย์ปรับเกณฑ์ขั้นต่ำเป็น5 ดู [ผลตรวจใหม่](../planning/step3-git-audit.md) ไม่เพิ่มcommitsเพื่อเติมยอด
- Stocktarget/active/Profileละเอียด, ownerJPA/patternconfirmation, DeploymentDiagram/RenderHTTPS/Securecookies/SPA404/restart/coldstart/runbook
- Public/timedTTL/regressionของreleaseล่าสุด สมาชิกอีก3คนส่งslides รวมทีม/ซ้อม/releasePRเข้าmain/deployedSHA

เอกสารรอบนี้รอศรัณย์และศิระพัทธ์review ไม่mergeเอง Notionติ๊กเฉพาะผลจัดทำ/ตรวจที่ผ่าน พร้อมช่องreview/งานรวม/public/releaseที่ยังว่าง
