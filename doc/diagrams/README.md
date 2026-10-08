# Diagram index — ครบตามใบงานข้อ 9.1

ชุดเดิมหลัง architecture refactor `de7b5d546a05ad3ccef8c641ee5c53d638a8e039` มี **23 diagram รายละเอียด และ5ภาพย่อสำหรับนำเสนอ**; PR #24/#25 ผ่าน review/merge แล้ว และ PR #26 ปรับ State resolver sources/previews เพิ่ม ทุกภาพมี PlantUML source/SVG ภาพ public deployment เป็น design เท่านั้น ไม่ใช้จำนวนไฟล์รับรอง public หรือ Final

**ตรวจโมดูลปวริศช์ 8 ตุลาคม 2026:** code baseline `6d83eace6bbd4d20f4d3cb3a25eb2d5ddb81fcc6`; ตารางต่อไปนี้เป็น scope ของ PR เอกสารรอบใหม่ ยังรอศรัณย์/ศิระพัทธ์ review ของรอบนี้ ภาพอื่นคง revision และการตรวจของเจ้าของเดิม

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
