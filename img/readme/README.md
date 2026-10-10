# README assets และแหล่งภาพ

ภาพคัดลอกจากหลักฐานจริงโดยไม่แต่ง UI หรือเปลี่ยนข้อความ ไม่รวมรหัสผ่าน cookie หรือ QR credential

| Asset | แหล่งเดิม | Environment / revision |
|---|---|---|
| customer-bill-public.png | [ภาพ UAT](../../test/evidence/pavarit-public-uat-2026-10-10/customer-bill-requested-served-1280.png) | public Render 10 ต.ค.; frontend baseline c778150 ตรง byte ตามรายงาน; backend SHA ไม่ได้ attest |
| manager-menu-public.png | [ภาพ UAT](../../test/evidence/pavarit-public-uat-2026-10-10/menu-images-1280.png) | public Render baseline เดียวกัน ภาพ Manager จัดเมนู |
| staff-payment-local.png | [ภาพ regression](../../test/evidence/menu-stock-v19-2026-10-10/core-flow/staff-paid-768.png) | local H2 ของ PR #49; runtime code e41a275, runner hash ใน evidence; ไม่ใช่ Render |
| kitchen-preparing-local.png | [ภาพ regression](../../test/evidence/menu-stock-v19-2026-10-10/menu-stock/kitchen-preparing-768.png) | local H2 ของ PR #49; ไม่ใช่ public acceptance |
| component-develop.svg | Git blob `c778150:doc/diagrams/previews/component.svg` | คัดลอก byte เดิมของ develop ก่อน V19 ไม่ใช้ diagram บน branch PR #49 แทน baseline โดยเงียบ |
| er-diagram-develop.svg | Git blob `c778150:doc/diagrams/previews/er-diagram.svg` | เหมือนข้างต้น source อาจมีวันที่ตรวจเก่าใน title; revision การคัดลอกไม่ใช่วันที่ออกแบบใหม่ |

PNG ทั้งสี่และ SVG ทั้งสองคง bytes ตรงแหล่งเดิม ตรวจ SHA-256 ใน [manifest](source-manifest.json)

ภาพเป็น snapshot ของเหตุการณ์ที่ระบุ ไม่รับรองว่าเว็บปัจจุบัน deploy SHA นี้ หรือว่า Final ผ่านทุก gate ภาพ public บางภาพจับส่วนที่เลื่อนดูบิล/เมนู ไม่ใช่ screenshot หน้าเต็ม ส่วน diagram รายละเอียดเปิดอ่านเต็มได้จาก link และมีคำอธิบายสำคัญเป็น Markdown ใน README
