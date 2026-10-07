# ปวริศช์ — speaker notes และแหล่งอ้างอิง

สไลด์6หน้า ร่างส่วนปวริศช์สำหรับรวมชุดทีม ไม่ใช่สไลด์Finalทั้งทีม Code baseline develop472fba4 / 7ต.ค.2026
ต้นฉบับ [PPTX](pavarit-step3-architecture.pptx) มี editable text/shapes/connectors และspeaker notesในทุกหน้า
[PDF](pavarit-step3-architecture.pdf) เป็นภาพrenderของPPTX6หน้า สำหรับดูด้วยfontเดิม ไม่ใช่PDFข้อความที่ค้นหา/แก้ไขได้
ฟอนต์ Noto Sans Thai primary#9A3412 background#FFFDF9 ตามUIทีม ถ้าเปิดPPTXในเครื่องที่ไม่มีfont ให้ติดตั้ง [Noto Sans Thai จากGoogleFonts](https://fonts.google.com/noto/specimen/Noto+Sans+Thai) ก่อนเพื่อคงlayout ฟอนต์ไม่ได้embedในPPTX

ใช้notesประกอบการอธิบาย อย่าอ่านทุกข้อความบนslide ตัวเลขtestsเป็นCIของbaseline ไม่ใช่ผลtestsใหม่/public/release
Sourceของภาพโครงสร้างสร้างเป็นnativeeditable shapesตาม [Component](../diagrams/component.puml) และ [Sequences](../system-design/sequence-diagrams.md) ไม่มีภาพถ่ายระบบproduction

## Slide 1

ผมรับ Table, Package, Soup และ DiningSession รวมจุดเชื่อม Auth/Ordering/Billing/Payment งานทีมแบ่งตามโมดูล ไม่ได้เขียน Payment Entity หรือ State แทนเจ้าของ ใน Step2 ผมรวม Manager master data และ Customer bill request เพื่อให้ flow ต่อกันได้ สไลด์นี้แยก ownership กับหน้าที่ integration

Sources: https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/doc/testing/pavarit-step2-completion-report.md



Code baseline: develop472fba4 (7 Oct2026). This is not Final public/release acceptance.
## Slide 2

React เป็น View ส่ง JSON/cookie ไป Controller ซึ่งตรวจ payloadและเรียกservice กฎธุรกิจและtransactionอยู่service repositoryจัดการJPA DomainแมปDB Providerแยกจุดเชื่อม SessionContextProvider สำหรับOrdering DiningSessionBillingReader สำหรับBilling และPaymentStatusLookupสำหรับclose สิทธิ์staffใช้sessionproviders ส่วนCustomerใช้grant ต้องกล่าวข้อยกเว้นcurrentLayeringว่าSessionUserContextProviderยังimportAuthControllerconstantและกำลังติดตามก่อนFinal

Sources: https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/code/backend/src/main/java/com/buffetrestaurant/service/impl/DiningSessionServiceImpl.java



Code baseline: develop472fba4 (7 Oct2026). This is not Final public/release acceptance.
## Slide 3

เปิดรอบล็อกโต๊ะและsnapshotprice ขอคิดบิลเซ็ตbillRequestedAtครั้งเดียวและคงACTIVEแต่หยุดOrderใหม่ทุกเครื่อง Paymentต้องACTIVEและrequestedแล้ว backendคำนวณยอดจากsnapshot ไม่รับราคาจากbrowser ปิดรอบตรวจPAIDของsessionเดียวกันแล้วdeletegrants/sessionCOMPLETED/tableAVAILABLEในtransactionเดียว Order/billrequest/Payment/closeใช้locksessionเดียวกันเพื่อserialize; closeก่อนจ่าย409 providerไม่มี503

Sources: https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/code/backend/src/main/java/com/buffetrestaurant/service/impl/PaymentServiceImpl.java



Code baseline: develop472fba4 (7 Oct2026). This is not Final public/release acceptance.
## Slide 4

แบบเดิมGETtokenในpathถูกถอดก่อนmerge backend ไม่ต้องdeprecateendpointที่ไม่เคยpublish แบบใหม่fragmentไม่เป็นHTTPrequestpath Reactจับและล้างhash POSTbody แลกครั้งเดียวและหมุนQR grantเก็บhashcredential+expiry8h cookieHttpOnlyต่อเครื่อง ลูกค้าหลายมือถือใช้QRรุ่นถัดไป Same-tokenin-flightPromiseรองรับStrictMode คนละtokenserializeเพื่อไม่ให้ cookie เก่าเขียนทับ invalidnewQRไม่fallbackรอบเดิม OriginGuard/CORSเฉพาะoriginและpublicต้องHTTPS/Secure ไม่กล่าวว่าHttpOnlyคือการป้องกันXSSทั้งหมด

Sources: https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerSessionAccessService.java



Code baseline: develop472fba4 (7 Oct2026). This is not Final public/release acceptance.
## Slide 5

S ControllerรับHTTP/serviceดูusecase/mapperจัดresponse O BillingEngineเปลี่ยนpricing/discountผ่านstrategy; factoryStateยังswitchจึงไม่อ้างว่าOCPทั้งระบบ L State.nextมีdocumentedterminalBusinessRuleException Strategyต้องคืนnonnegativeไม่mutatecontext I interfaceSessionContextและPaymentLookupเล็กไม่รวมEntityข้ามโมดูล D InjectinterfacesในSession/Ordering แต่ยังมีconcretedependenciesและfieldinjection ต้องrecordgapไม่อ้างผ่านวิชาเต็ม Behavioral3แบบState/Strategy/TemplateMethodมีtestsจริง

Sources: https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/code/backend/src/main/java/com/buffetrestaurant/service/SessionContextProvider.java



Code baseline: develop472fba4 (7 Oct2026). This is not Final public/release acceptance.
## Slide 6

ตัวเลขมาจากCIหลังmergePR22 ไม่ใช่รันใหม่ในPRเอกสารนี้303backendรวมPostgreSQL tests116frontend6URLguards lint0errors4existingwarnings buildpass Browser14groups17screenshotsเป็นlocalH2ก่อนmerge14PASS0FAIL patchedhashตรงhead617 ไม่ใช่publicrerunหรือมือถือจริง สิ่งรอStock/Profileใหม่ RenderHTTPS/Securecookies timedTTL latestrelease fullregression Gitmeaningfulcommitเกณฑ์รายบุคคลให้สมาชิกยืนยัน publicdeploymentและreleasePRreview/main/deployedcommit ทีมต้องซ้อมด้วยบัญชีจริง

Sources: https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37503690105

https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/472fba4/doc/testing/sirapat-step3-premerge-report.md



Code baseline: develop472fba4 (7 Oct2026). This is not Final public/release acceptance.
