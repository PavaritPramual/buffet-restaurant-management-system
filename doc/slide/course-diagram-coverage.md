# Diagram coverage ตามใบงานข้อ 9.1

ใบงานกำหนด diagram หลายชนิด จึงตรวจทุกชนิด ไม่ใช้ Component/ERD เพียงสองภาพแทนทั้งหมด ชุดCanva v02ใช้หน้าหลัก1–20สำหรับ12นาที ภาคผนวก21–76มีรายละเอียดครบและเปิดตอบคำถาม ภาพทั้งหมดมีsource ไม่ใช้ลิงก์อย่างเดียวแทนภาพ

| เกณฑ์ | ภาพ / รายละเอียด | หน้า v02 |
|---|---|---|
| Use Case และ Description | [presentation-use-case](../diagrams/presentation-use-case.puml) · [use-case-service-customer](../diagrams/use-case-service-customer.puml) · [use-case-management](../diagrams/use-case-management.puml) · [use-case](../diagrams/use-case.puml) | 2, 22, 24, 68 และ Descriptionหน้า3 |
| Domain / Conceptual Class | [domain-model](../diagrams/domain-model.puml) | 29 |
| Class พร้อมตำแหน่ง Patterns | [class-table-session](../diagrams/class-table-session.puml) · [class-menu-order](../diagrams/class-menu-order.puml) · [class-auth](../diagrams/class-auth.puml) · [class-order-state](../diagrams/class-order-state.puml) · [class-billing-strategy](../diagrams/class-billing-strategy.puml) · [class-stock-template](../diagrams/class-stock-template.puml) | 34, 69, 70, 38, 41, 43 |
| Sequence ≥3 scenarios | [sequence-open-session](../diagrams/sequence-open-session.puml) · [sequence-qr-exchange](../diagrams/sequence-qr-exchange.puml) · [sequence-ordering-kitchen](../diagrams/sequence-ordering-kitchen.puml) · [sequence-billing-payment](../diagrams/sequence-billing-payment.puml) | 49, 46, 71, 55 |
| Activity | [activity-customer-ordering](../diagrams/activity-customer-ordering.puml) · [activity-kitchen](../diagrams/activity-kitchen.puml) · [activity-payment-close](../diagrams/activity-payment-close.puml) · [activity-stock](../diagrams/activity-stock.puml) | 51, 72, 73, 74 |
| ERD / Schema | [presentation-erd](../diagrams/presentation-erd.puml) · [er-diagram](../diagrams/er-diagram.puml) | 5, 32 |
| Component | [presentation-component](../diagrams/presentation-component.puml) · [component](../diagrams/component.puml) | 4, 26 |
| Deployment | [deployment-local](../diagrams/deployment-local.puml) · [deployment-production-design](../diagrams/deployment-production-design.puml) | 16, 75 |
| State | [state-order](../diagrams/state-order.puml) | 40 |

Use Case Description เต็มพร้อมactor/preconditions/main flow/alternative/postconditions อยู่ [use-cases.md](../system-design/use-cases.md) Public deployment diagramต้องเทียบรุ่นที่deployจริงอีกครั้ง ภาพlocalแสดงconfigurationที่มีอยู่ ไม่อ้างว่าฐานกลางถูกตรวจสดในงานสไลด์

[เนื้อหา/notes](team-final-canva-v02-content.md) · [คู่มือ12นาที](team-final-12-minute-runbook.md) · [เกณฑ์commitใหม่5ต่อคน](../planning/course-criteria-updates.md)
