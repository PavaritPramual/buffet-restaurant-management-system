# เกณฑ์รายวิชาที่ปรับจากใบงานต้นฉบับ

## อัปเดต 7 ตุลาคม 2026

ปวริศช์แจ้งในแชทว่าอาจารย์ปรับขั้นต่ำเป็น **5 commits ต่อคน** ใช้จำนวนใหม่นี้ใน Requirement Matrix, Git audit, Step 3, PR และ Notion

เงื่อนไข meaningful commits, บัญชีของเจ้าของ, การกระจายเวลา, branch ที่ถูกชื่อ, PR/reviewer และการอธิบายโค้ดของตนยังต้องตรวจตามเดิม ไม่ใช้จำนวนล้วนเป็นการรับรองผลงาน

ใบงาน Markdown ต้นฉบับใน workspace ยังระบุ 15 จึงเก็บต้นฉบับไว้โดยไม่แก้ข้อความหลักฐาน ข้อมูล 15 ในรายงาน/commit เก่าเป็นประวัติที่ถูกแทนด้วยการแจ้งครั้งนี้

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
