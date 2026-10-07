# Diagram index — ครบตามใบงานข้อ 9.1

โค้ดปัจจุบันหลัง architecture refactor `de7b5d546a05ad3ccef8c641ee5c53d638a8e039` วันที่7ตุลาคม2026; diagramที่ไม่เปลี่ยนยังอ้าง baseline `472fba4` จำนวน **23 diagram รายละเอียด และ5ภาพย่อสำหรับนำเสนอ** ทุกภาพมี PlantUML source/SVG ภาพ public deployment เป็น design เท่านั้น รอ peer review ไม่ใช้จำนวนไฟล์รับรอง public หรือ Final

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
