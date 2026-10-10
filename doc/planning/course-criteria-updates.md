# เกณฑ์รายวิชาที่ปรับจากใบงานต้นฉบับ

## เกณฑ์ปัจจุบัน — อัปเดต 8 ตุลาคม 2026

ปวริศช์ยืนยันใน [ความเห็น PR #34](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/34#issuecomment-6062776592) ว่าอาจารย์ยังใช้ขั้นต่ำ **15 meaningful commits ต่อคน** ตามใบงานเดิม และยกเลิกการแจ้งลดเหลือ 5 เมื่อวันที่ 7 ตุลาคม ใช้ 15 ใน Requirement Matrix, Git audit, Step 3, README และเอกสารส่งงานที่ยังเป็นสถานะปัจจุบัน

จำนวน Git author candidates เป็นเพียงรายการให้ตรวจ ไม่รับรอง meaningfulness, account identity, ownership, การกระจายเวลา หรือ PR/reviewer history สมาชิกต้องยืนยันข้อมูลของตนเอง ห้ามสร้าง commits เพื่อเติมจำนวน

## ประวัติ — การแจ้งวันที่ 7 ตุลาคม 2026 (ถูกยกเลิก)

ปวริศช์แจ้งในแชทว่าอาจารย์ปรับขั้นต่ำเป็น 5 commits ต่อคน จึงมีการนำเกณฑ์ชั่วคราวนี้ไปใส่ในเอกสารบางฉบับ ก่อนมีการยืนยันใหม่วันที่ 8 ตุลาคมให้กลับมาใช้ 15 ตามใบงาน เก็บการแจ้งนี้ไว้เพื่ออธิบายประวัติการแก้เอกสารเท่านั้น ไม่ใช่เกณฑ์ปัจจุบัน

ใบงาน Markdown ต้นฉบับใน workspace ระบุ 15 และคงไว้โดยไม่แก้ข้อความหลักฐาน

## Diagram ตามใบงานข้อ 9.1

- Use Case Diagram และ Use Case Description
- Domain Model หรือ Conceptual Class Diagram
- Class Diagram พร้อมตำแหน่ง Design Pattern
- Sequence Diagram อย่างน้อย 3 Scenario หลัก
- Activity Diagram
- ER Diagram หรือ Database Schema พร้อม Data Dictionary ตามข้อ 6
- Component Diagram และ Deployment Diagram
- State Diagram เมื่อ Entity มีสถานะ

สไลด์ต้องใช้ภาพ diagram ที่ตรง implementation พร้อมไฟล์ source และตำแหน่งในชุดนำเสนอ ภาพ Deployment ที่เป็น design ต้องระบุว่าเป็นแบบออกแบบ ไม่ใช้ภาพแทนผล public deployment ที่ยังไม่ได้ตรวจ
