# ธีรเมธ — เนื้อหา Canva ส่วน Runtime/Network (รอตรวจรวม)

อ้าง source `bdd3bd7` และผล public check วันที่ 8 ตุลาคม 2026; ยังไม่ใช่ PDF/Canva ฉบับส่งหรือผลทดสอบ Billing/Payment บน release จริง. ใช้คู่กับ [Deployment Diagram](../diagrams/deployment-production-runtime.md) และ [runbook](../deployment/production-runbook.md). เมื่อทีมใส่ลง Canva ให้เปลี่ยน SHA/ผลตรวจตามรุ่นที่ deploy จริงก่อน export PDF.

## สไลด์: เว็บและ API อยู่ URL เดียว

**ข้อความบนสไลด์**

- Browser → HTTPS Render → Spring Boot
- Spring Boot เสิร์ฟ React, `/api/v1`, Swagger จาก image เดียว
- Spring Boot → Supabase Session Pooler `:5432` ผ่าน JDBC SSL
- Build React ด้วย `VITE_API_BASE_URL=/api/v1`; Render ส่ง `PORT`

**ภาพ:** [Deployment Diagram](../diagrams/deployment-production-runtime.md) หรือวางแผนภาพเดียวกันใน Canva. ระบุใต้ภาพว่าเส้นทาง DB มีหลักฐานจาก startup log ก่อนหน้า ส่วน deployed SHA ปัจจุบันต้องยืนยันจาก Render.

**คำพูดประมาณ 30 วินาที:** “production ใช้ URL เดียว หน้าเว็บกับ API จึงส่ง cookie ไป origin เดียวกัน React ถูก build แล้วฝังใน Spring Boot ไม่ได้เปิด Vite dev server บน Render. Render รับ HTTPS และส่งคำขอเข้า port ของ Java. Backend ต่อ PostgreSQL ผ่าน Supabase Session Pooler โดยใช้ SSL; Flyway ตรวจ migration และ JPA ตรวจ mapping ตอนเริ่มระบบ”

## สไลด์: สิ่งที่ต้องพิสูจน์ก่อนรับรอง release

**ข้อความบนสไลด์**

- Staff `JSESSIONID` และ Customer `customer_session`: `HttpOnly; Secure; SameSite=Lax`
- Login → QR → Billing preview → PAID → Staff ปิดโต๊ะ (แยกขั้น)
- ทดสอบ refresh/deep link, จ่ายซ้ำ, ข้อมูลหลัง redeploy
- Render Free หลับเมื่อไม่มีการใช้งาน: วัด cold start ก่อน demo

**คำพูดประมาณ 35 วินาที:** “CI ที่ผ่านยืนยันโค้ด แต่ไม่ยืนยัน release บน Render. เราต้องดู commit ที่ deploy จริง ตรวจ cookie โดยไม่คัดลอกค่า ตรวจบิลจากราคา snapshot และยอด backend หลังจ่ายซ้ำต้องถูกปฏิเสธ โต๊ะไม่ว่างจนพนักงานกดปิด. หลัง redeploy ต้องดูข้อมูลถาวรอีกครั้ง; Staff session ในหน่วยความจำอาจต้องล็อกอินใหม่”

**สถานะสำหรับผู้รวมสไลด์:** PR #35 head `bdd3bd7` และ CI ผ่าน; public root/Swagger/health เปิดได้ แต่ `/kitchen/` และ `/Customer/QR/` ยัง 404 ตอนตรวจ จึงต้อง deploy commit ใหม่และทดสอบซ้ำ. ยังไม่มีผล login/QR/Billing/Payment ด้วยบัญชีทดสอบบน public URL. อย่าใส่เครื่องหมายผ่านใน Canva/PDF ก่อนมีหลักฐานนั้น.
