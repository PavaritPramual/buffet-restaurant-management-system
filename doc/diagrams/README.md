# PlantUML diagrams

ERD ปรับเมื่อ 6 ตุลาคม 2026 ให้ตรงกับ Flyway V1–V12 และ schema Supabase ที่ตรวจจริง ดู [canonical Data Dictionary และการรับรอง extensions](../database/step2-schema-approved.md) Diagram อื่นที่คัดจาก baseline ต้องอ่านข้อจำกัดใน System Design ประกอบ

ไฟล์ `.puml` เหล่านี้คัดจากหน้า [System Design ใน Notion](https://app.notion.com/p/3cfcb2e9d47a811ba912e01c6bcb449e) เมื่อ 28 กันยายน 2026 เป็น source ที่แก้ไขได้ GitHub จะแสดง source; ใช้ PlantUML renderer หรือ IDE extension เพื่อสร้างภาพ SVG/PNG

| ประเภท | ไฟล์ |
| --- | --- |
| Use case | [use-case.puml](use-case.puml) |
| Domain | [domain-model.puml](domain-model.puml) |
| ERD | [er-diagram.puml](er-diagram.puml) |
| Class | [Table/Session](class-table-session.puml), [Menu/Order](class-menu-order.puml), [Order State](class-order-state.puml), [Billing Strategy](class-billing-strategy.puml), [Stock Template](class-stock-template.puml), [Auth](class-auth.puml) |
| Sequence | [Open Session](sequence-open-session.puml), [Ordering/Kitchen](sequence-ordering-kitchen.puml), [Billing/Payment](sequence-billing-payment.puml) |
| Activity | [Customer/Ordering](activity-customer-ordering.puml), [Kitchen](activity-kitchen.puml), [Payment/Close](activity-payment-close.puml), [Stock](activity-stock.puml) |

ดูคำอธิบายและข้อแตกต่างจาก implementation ที่ [System Design](../system-design/README.md)
