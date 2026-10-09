# Diagram index — ครบตามใบงานข้อ 9.1

สำหรับ production topology ที่แยกผล public HTTP จริงออกจาก configuration และระบุข้อจำกัดของ deployed SHA ดู [Deployment runtime diagram (Mermaid)](deployment-production-runtime.md). ภาพ PlantUML `deployment-production-design.puml` ด้านล่างยังเป็น design เดิม ไม่ใช่ใบรับรอง release.

ชุดเดิมหลัง architecture refactor `de7b5d546a05ad3ccef8c641ee5c53d638a8e039` มี **23 diagram รายละเอียด และ5ภาพย่อสำหรับนำเสนอ**; PR #24/#25 ผ่าน review/merge แล้ว และ PR #26 ปรับ State resolver sources/previews เพิ่ม ทุกภาพมี PlantUML source/SVG ภาพ public deployment เป็น design เท่านั้น ไม่ใช้จำนวนไฟล์รับรอง public หรือ Final

## Delta ตรวจล่าสุด — develop `d84f071`, 9 ตุลาคม 2026

ตรวจ source กับ State service/role provider, V15 Profile/Stock entities, integration tests และ use-case descriptions. อัปเดต Activity Kitchen ให้เห็น explicit role/next-state guards; Domain Model ให้สะท้อน UserProfile และ V15 stock fields; Manager/combined Use Case ให้แสดง contact-profile update. SVG preview ถูก render ใหม่จาก PlantUML sources.

| ภาพที่ตรวจ | Delta ที่ตรงกับ code | Source / preview |
|---|---|---|
| Activity Kitchen/Serving | role-specific board access; only `RECEIVED → PREPARING → READY → SERVED`; each denied board read or transition ends that request, and denied transitions leave persisted status unchanged | [source](activity-kitchen.puml) / [SVG](previews/activity-kitchen.svg) |
| Domain Model | add `UserProfile` one-to-one conceptual relation; include V15 `openingTargetStock` and `active`, derived shortfall note | [source](domain-model.puml) / [SVG](previews/domain-model.svg) |
| Manager / Supervisor Use Case | reflect Manager contact-profile update fields and limited account-edit scope | [source](use-case-management.puml) / [SVG](previews/use-case-management.svg) |
| All actors Use Case | update baseline revision and include Manager contact-profile update; clarify account edit/delete is unavailable while contact updates are limited to name and phone | [source](use-case.puml) / [SVG](previews/use-case.svg) |

เป็น source/documentation review ของ merged baseline ไม่ใช่ peer approval หรือ Final release sign-off. Public flow/denial evidence แยกอยู่ใน [PR #36 report](https://github.com/PavaritPramual/buffet-restaurant-management-system/blob/b2928f564b796b0e7a5e7260e0f3f567ca40a386/doc/testing/sirapat-public-regression-2026-10-09.md); deployed revision ยัง owner-reported.

**ประวัติตรวจโมดูลปวริศช์ใน PR #31:** code baseline `6d83eace6bbd4d20f4d3cb3a25eb2d5ddb81fcc6`; scope ตารางต่อไปนี้ผ่านศรัณย์/ศิระพัทธ์ review ที่ head5939ecd และ merge แล้ว ภาพอื่นคง revision และการตรวจของเจ้าของเดิม

## ตรวจหลังรวม Stock/Profile — develop adc5798

ปวริศช์ตรวจ 8 ตุลาคม 2026 เทียบ code `adc5798b05279840dc6178f4291278467c929791` พร้อม Entity/providers/roles/transaction/response ของส่วนตน; ชุดนี้รอ review ของ PR รอบใหม่ ไม่ใช่รับรอง public หรือ Final. Class/Sequence ที่ระบุ baseline6d83eac ยังคงเป็นภาพรอบเดิมที่ตรวจว่าพฤติกรรมส่วนตนไม่เปลี่ยน ไม่ได้เปลี่ยนชื่อ baseline โดยไม่มีเหตุผล

| ภาพ | ผลตรวจหลัง merge | Source / preview |
|---|---|---|
| Component | แก้ baseline เป็น adc5798 และ Flyway V1–V15 ตาม schema ที่รวมแล้ว; layers/providers ของส่วนตนคงเดิม; validate/render และตรวจภาพใหม่ | [source](component.puml) / [SVG](previews/component.svg) |
| Class Table/Session/QR | associations, FK owner, LAZY/no JPA cascade, snapshot และชนิดเวลาตรงเดิม; ไม่เปลี่ยน source/SVG | [source](class-table-session.puml) / [SVG](previews/class-table-session.svg) |
| Sequence เปิดรอบ | transaction/row lock, Staff-only QR, HTTP400/404 และ snapshot reader ตรงเดิม | [source](sequence-open-session.puml) / [SVG](previews/sequence-open-session.svg) |
| Sequence แลก QR | fragment/body/cookie, single-use/rotation, expiry และ revoke ตรงเดิม | [source](sequence-qr-exchange.puml) / [SVG](previews/sequence-qr-exchange.svg) |
| Domain ส่วน Table/Session/Grant | cardinality ตรงเดิม แยก Domain จาก JPA associations; ไม่รับรองส่วน Stock/Profile แทนเจ้าของ | [source](domain-model.puml) / [SVG](previews/domain-model.svg) |
| Use Case Customer/Staff | เปิดรอบ/แลก QR/ขอคิดบิล/close และ actor rights ของส่วนตนตรงเดิม | [source](use-case-service-customer.puml) / [SVG](previews/use-case-service-customer.svg) |
| Use Case รวม เฉพาะโมดูลตน | Table/Package/Soup/Session/QR ตรงเดิม; ไม่รับรอง Auth/Stock/Payment ทั้งภาพแทนเจ้าของ | [source](use-case.puml) / [SVG](previews/use-case.svg) |

คำรับรอง JPA อยู่ใน [ตารางกลาง](../architecture/jpa-entity-rationale.md) และผลรอบใหม่อยู่ใน [รายงานระบบรวม](../testing/pavarit-integrated-validation-2026-10-08.md). ตารางด้านล่างเป็นประวัติ delta ของ PR #31

### ตรวจ Auth / Stock / Activity / ERD เทียบ V15 (9 ตุลาคม 2026)

render ใหม่เฉพาะภาพที่ source เปลี่ยนจริง ภาพอื่นไม่แตะ

| ภาพ | สิ่งที่แก้ | Source / preview |
|---|---|---|
| ERD | title เป็น Flyway V1–V15 (เดิมเขียน V1–V14 แต่มีคอลัมน์ V15 แล้ว) | [source](er-diagram.puml) / [SVG](previews/er-diagram.svg) |
| Class Auth | เพิ่ม id/username/createdAt, `listUsers`, `/me`, methods ของ AdminUserController และ note Manager-only/ชื่อบังคับที่ API แต่ DB nullable | [source](class-auth.puml) / [SVG](previews/class-auth.svg) |
| Class Stock Template | เพิ่ม `createItem/updateItem/setActive/overview/history`, `updateDetails/updateOpeningTarget/changeActive`, แก้ `StockTransaction` เป็น `quantityDelta/transactionType/createdAt` | [source](class-stock-template.puml) / [SVG](previews/class-stock-template.svg) |
| Activity Stock | เพิ่มวงจร item (create/edit/active), inactive → 409 ก่อนคำนวณ และ balance ติดลบ → 400 | [source](activity-stock.puml) / [SVG](previews/activity-stock.svg) |
| Domain Model | `StockItem` เพิ่ม openingTargetStock/active; `StockTransaction` ใช้ชื่อฟิลด์ตรง entity; ลบ note ว่า "planned for Final" | [source](domain-model.puml) / [SVG](previews/domain-model.svg) |

| ภาพที่ตรวจ | ผลเทียบโค้ด | Source / preview |
|---|---|---|
| Component | แก้ baseline และจำกัดข้ออ้าง constructor/interfaces ตามขอบเขต services ที่ตรวจ | [source](component.puml) / [SVG](previews/component.svg) |
| Class Table/Session/QR | แก้ทิศทาง JPA owner, ไม่มี reverse collections/cascade, เพิ่ม adapters และแยกเวลา/ราคา internal | [source](class-table-session.puml) / [SVG](previews/class-table-session.svg) |
| Sequence เปิดรอบ | แก้ Staff response ที่เดิมอ้างว่ามีราคา snapshot; เพิ่ม UTC mapping/Billing reader; แก้รีวิว PR #31 ให้ occupied/capacity/missing or inactive package-soup เป็น HTTP 400 และ missing table เป็น 404 ตาม runtime | [source](sequence-open-session.puml) / [SVG](previews/sequence-open-session.svg) |
| Sequence แลก QR | แก้ reply ผ่าน Controller ตามโค้ด และอายุ grant เป็น 8 ชั่วโมงแน่นอน | [source](sequence-qr-exchange.puml) / [SVG](previews/sequence-qr-exchange.svg) |
| Domain ส่วน Table/Session/Grant | ความสัมพันธ์เดิมตรง; เพิ่ม note แยก Domain cardinality จาก JPA mapping และ snapshot/lifecycle | [source](domain-model.puml) / [SVG](previews/domain-model.svg) |
| Use Case Customer/Staff | ตรงเดิมใน scope เปิดรอบ/แลก QR/ขอคิดบิล/close; ไม่แก้ source/preview | [source](use-case-service-customer.puml) / [SVG](previews/use-case-service-customer.svg) |
| Use Case รวม เฉพาะโมดูลปวริศช์ | ตรงเดิมใน scope Table/Package/Soup/Session/QR; ไม่รับรองรายละเอียด Auth/Stock/Payment ของเจ้าของอื่น | [source](use-case.puml) / [SVG](previews/use-case.svg) |

เหตุผล/source/tests อยู่ใน [เอกสารโมดูลปวริศช์](../architecture/pavarit-table-session-solid-jpa.md) และ [รายงานตรวจรอบนี้](../testing/pavarit-module-docs-report-2026-10-08.md)

[ตารางเทียบโจทย์และหน้าสไลด์](../slide/course-diagram-coverage.md) · [Use Case Descriptions](../system-design/use-cases.md) · [Canonical Data Dictionary](../database/step2-schema-approved.md)

| Diagram | Source | Preview | หน้า v02 |
|---|---|---|---:|
| ผู้ใช้งานทั้งห้ากลุ่มมีหน้าที่ต่างกัน | [presentation-use-case.puml](presentation-use-case.puml) | [SVG](previews/presentation-use-case.svg) | 2 |
| Controller Service และ Repository แยกหน้าที่ | [presentation-component.puml](presentation-component.puml) | [SVG](previews/presentation-component.svg) | 4 |
| Session เป็นแกนของความสัมพันธ์ข้อมูล | [presentation-erd.puml](presentation-erd.puml) | [SVG](previews/presentation-erd.svg) | 5 |
| QR แลกสิทธิ์ก่อนเรียก Ordering | [presentation-sequence-qr.puml](presentation-sequence-qr.puml) | [SVG](previews/presentation-sequence-qr.svg) | 14 |
| คิดบิล ชำระ และปิดรอบเป็นคนละขั้น | [presentation-sequence-payment.puml](presentation-sequence-payment.puml) | [SVG](previews/presentation-sequence-payment.svg) | 15 |
| Deployment ของ runtime ที่มีอยู่ปัจจุบัน | [deployment-local.puml](deployment-local.puml) | [SVG](previews/deployment-local.svg) | 16 |
| ลูกค้า พนักงานบริการ และครัวทำอะไรกับระบบ | [use-case-service-customer.puml](use-case-service-customer.puml) | [SVG](previews/use-case-service-customer.svg) | 22 |
| Manager และ Supervisor มีสิทธิ์ต่างกัน | [use-case-management.puml](use-case-management.puml) | [SVG](previews/use-case-management.svg) | 24 |
| Component แสดงขอบเขตและจุดเชื่อมของโมดูล | [component.puml](component.puml) | [SVG](previews/component.svg) | 26 |
| Domain Model แสดงแนวคิดก่อนรายละเอียดคลาส | [domain-model.puml](domain-model.puml) | [SVG](previews/domain-model.svg) | 29 |
| ERD แสดงตารางและความสัมพันธ์ของฐานข้อมูล | [er-diagram.puml](er-diagram.puml) | [SVG](previews/er-diagram.svg) | 32 |
| Class ของ Session แสดง Entity และ provider | [class-table-session.puml](class-table-session.puml) | [SVG](previews/class-table-session.svg) | 34 |
| Class Diagram แสดงตำแหน่ง State Pattern | [class-order-state.puml](class-order-state.puml) | [SVG](previews/class-order-state.svg) | 38 |
| State Diagram แสดงลำดับ Order และสิทธิ์ | [state-order.puml](state-order.puml) | [SVG](previews/state-order.svg) | 40 |
| Class Diagram แสดง composition ของ Strategy | [class-billing-strategy.puml](class-billing-strategy.puml) | [SVG](previews/class-billing-strategy.svg) | 41 |
| Class Diagram แสดง Template และ hooks | [class-stock-template.puml](class-stock-template.puml) | [SVG](previews/class-stock-template.svg) | 43 |
| Sequence แสดงการแลก QR และออกสิทธิ์ลูกค้า | [sequence-qr-exchange.puml](sequence-qr-exchange.puml) | [SVG](previews/sequence-qr-exchange.svg) | 46 |
| Sequence แสดงการเปิดรอบใน transaction เดียว | [sequence-open-session.puml](sequence-open-session.puml) | [SVG](previews/sequence-open-session.svg) | 49 |
| Activity แสดงทางเลือกและเงื่อนไขก่อนสั่ง | [activity-customer-ordering.puml](activity-customer-ordering.puml) | [SVG](previews/activity-customer-ordering.svg) | 51 |
| Sequence แยกคิดบิล ชำระ และปิดรอบ | [sequence-billing-payment.puml](sequence-billing-payment.puml) | [SVG](previews/sequence-billing-payment.svg) | 55 |
| Use Case ภาพรวมทุก actor ของระบบ | [use-case.puml](use-case.puml) | [SVG](previews/use-case.svg) | 68 |
| Class ของ Menu และ Ordering ตรง source | [class-menu-order.puml](class-menu-order.puml) | [SVG](previews/class-menu-order.svg) | 69 |
| Class ของ Auth และ UserProfile แบบ shared PK | [class-auth.puml](class-auth.puml) | [SVG](previews/class-auth.svg) | 70 |
| Sequence ของ Ordering ครัว และงานเสิร์ฟ | [sequence-ordering-kitchen.puml](sequence-ordering-kitchen.puml) | [SVG](previews/sequence-ordering-kitchen.svg) | 71 |
| Activity ของครัวและการยืนยันเสิร์ฟ | [activity-kitchen.puml](activity-kitchen.puml) | [SVG](previews/activity-kitchen.svg) | 72 |
| Activity ของ Payment และ close แยกกัน | [activity-payment-close.puml](activity-payment-close.puml) | [SVG](previews/activity-payment-close.svg) | 73 |
| Activity ของการรับเข้าและปรับสต็อก | [activity-stock.puml](activity-stock.puml) | [SVG](previews/activity-stock.svg) | 74 |
| Deployment design สำหรับระบบสาธารณะ | [deployment-production-design.puml](deployment-production-design.puml) | [SVG](previews/deployment-production-design.svg) | 75 |

## Render ซ้ำ

Java17+ / PlantUML1.2025.0 กับ Smetana สำหรับ class/ER/component ไม่ต้อง Graphviz

```text
java -Djava.awt.headless=true -jar <plantuml.jar> -nbthread 1 -tsvg -charset UTF-8 -o previews "doc/diagrams/*.puml"
```

Order Entity เก็บ enum ไม่เก็บ State object; Context อยู่ FulfillmentService ภาพ Auth ใช้ UserAccount/UserProfile shared PK และ HTTP session ไม่ใช่ Supabase Auth ส่วน Stock adjustment ใช้ signed delta และ audit history

อัปเดต Component และ Class Auth/Session/Stock/Billing/State ตาม contracts/registry จริง. Canva v02b คงเป็นฉบับล่วงหน้าก่อน refactor ไม่แก้สไลด์หรือรับรอง owner/rehearsal จาก PR นี้
