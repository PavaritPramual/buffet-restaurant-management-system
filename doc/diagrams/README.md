# PlantUML diagrams

Current code baseline develop472fba4 / 7 ตุลาคม2026 ส่วนที่ปรับรอบนี้มีsourceและSVGที่renderแล้ว Sourceเป็นไฟล์แก้ไขได้ ไม่ใช้ภาพแทนsource

| Diagram | Source | Preview / สถานะ |
|---|---|---|
| Component | [component.puml](component.puml) | [SVG](previews/component.svg) runtime collaborationจริง ไม่ใช่publicdeployment |
| Domain | [domain-model.puml](domain-model.puml) | [SVG](previews/domain-model.svg) ไม่มีMenuItem.price/Stocktargetที่ยังไม่ทำ Payment0..1 |
| Table/Session class | [class-table-session.puml](class-table-session.puml) | [SVG](previews/class-table-session.svg) grants/snapshot/billrequest |
| State class | [class-order-state.puml](class-order-state.puml) | [SVG](previews/class-order-state.svg) contextคือFulfillmentService |
| Strategy class | [class-billing-strategy.puml](class-billing-strategy.puml) | [SVG](previews/class-billing-strategy.svg) compositionจากactualinterfaces |
| Template class | [class-stock-template.puml](class-stock-template.puml) | [SVG](previews/class-stock-template.svg) finalprocessและsigneddelta |
| Open session sequence | [sequence-open-session.puml](sequence-open-session.puml) | [SVG](previews/sequence-open-session.svg) rowlock+transaction |
| QR exchange sequence | [sequence-qr-exchange.puml](sequence-qr-exchange.puml) | [SVG](previews/sequence-qr-exchange.svg) fragment/body/cookie |
| Bill/Payment/Close sequence | [sequence-billing-payment.puml](sequence-billing-payment.puml) | [SVG](previews/sequence-billing-payment.svg) billก่อนpay/closeแยก |
| ERD V1–V14 | [er-diagram.puml](er-diagram.puml) | schemaรับรองจาก[Dictionary](../database/step2-schema-approved.md); baselineไม่ได้renderใหม่รอบนี้ |
| UseCase | [use-case.puml](use-case.puml) | [Descriptions](../system-design/use-cases.md) baselineรอFinalconsistency |
| Menu/Order, Auth classes | [Menu/Order](class-menu-order.puml), [Auth](class-auth.puml) | baselineเจ้าของต้องFinalreview |
| Ordering/Kitchen sequence | [sequence-ordering-kitchen.puml](sequence-ordering-kitchen.puml) | baselineเจ้าของต้องตรวจcookie/lock/state |
| Activities | [Customer](activity-customer-ordering.puml), [Kitchen](activity-kitchen.puml), [Payment](activity-payment-close.puml), [Stock](activity-stock.puml) | baselineต้องFinalreviewตามcode |
| Order state | [state documentation](order-fulfillment-state-diagram.md) | มี source Mermaidในเอกสาร ศรัณย์รับFinalaudit |
| Deployment | ธีรเมธจัดทำหลังruntimeจริงพร้อม | ยังไม่มีpublicdiagram/URLที่รับรอง |

## สร้างภาพซ้ำ

ใช้ Java17+ กับ PlantUML1.2025.0 (renderรอบนี้) เช่น

```text
java -jar <path-to-plantuml.jar> -tsvg -charset UTF-8 -o previews doc/diagrams/component.puml
```

Class/componentใช้ SmetanaภายในJava (`!pragma layout smetana`) จึงไม่ต้องติดตั้งGraphvizเพิ่ม Sequenceใช้rendererเดิม previewเก็บsourcePlantUMLแบบcompressedในSVG ไม่ใส่ข้อมูลลับใดๆ

บันทึกผลrender/QAที่ [documentation verification](../testing/pavarit-step3-docs-report.md) และ [System Design](../system-design/README.md)
