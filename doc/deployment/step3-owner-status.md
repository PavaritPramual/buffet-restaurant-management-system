# ธีรเมธ Step 3 — สถานะที่ตรวจได้ ณ 9 ตุลาคม 2026

ตรวจ branch `teeramet_673380273-9_02` ที่ `2f8bc4b` ซึ่งรวม `develop` ถึง `e6172b2` แล้ว. [CI ของ commit นี้](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37811005800) ผ่านทั้ง backend/PostgreSQL และ frontend. ภาพ Render Deploys ที่เจ้าของบริการส่งวันที่ 9 ตุลาคม 2026 แสดง `2f8bc4b` เป็น **Deploy succeeded | Live**. Public HTTP checks ยืนยันเฉพาะเส้นทางที่ระบุด้านล่าง; ไม่ใช้ CI หรือหน้าเว็บ 200 แทนผล end-to-end ที่ต้องล็อกอิน.

| รายการจากแผน | สถานะ | หลักฐาน / งานถัดไป |
| --- | --- | --- |
| Sync branch, ตรวจ Dockerfile/Compose/providers | **เสร็จ** | Merge commit `2ea71ed`; ตรวจ [`Dockerfile.production`](../../Dockerfile.production), [`docker-compose.yml`](../../docker-compose.yml), [`application-production.yml`](../../code/backend/src/main/resources/application-production.yml) |
| Production multi-stage build, frontend/backend URL เดียว | **ผ่าน public smoke** | Render Live `2f8bc4b`; [`Dockerfile.production`](../../Dockerfile.production) build React → Spring Boot; `/`, Swagger, API และ health เปิดจาก URL เดียว. เครื่องนี้เข้า Docker daemon ไม่ได้ แต่ Render build/deploy รุ่นนี้สำเร็จ |
| `/api/v1` และ SPA refresh; API/asset ไม่ fallback | **ผ่าน public HTTP checks** | บน Live `2f8bc4b`: `/kitchen/`, `/Customer/QR/`, `/ADMIN/users`, `/staff/sessions/1/billing` = 200 HTML; unknown API/asset = 404 JSON; public JS bundle มี `/api/v1` และไม่มี `localhost:8080`. ยังต้องตรวจการทำงานจริงหลัง browser refresh แบบล็อกอิน |
| `PORT`, forwarded headers, Staff/Customer cookie, allowed origin | **บางส่วน** | Public HTTPS/health ผ่าน; source production config และ cookie tests ผ่าน; public QR exchange ด้วย token ทดสอบที่ไม่ถูกต้องผ่าน origin guard แล้วตอบ 404; ยังต้องดู cookie attributes หลัง login/QR จริง |
| Production providers ใช้ session/database ตามแผน | **โค้ดพร้อม; รอตรวจ runtime** | ตั้งค่าใน `application-production.yml`; ต้องยืนยัน active profile และ flow บน release ที่ deploy |
| Secrets ใน Render และ bootstrap ปิดหลังเตรียมบัญชี | **รอยืนยัน runtime** | `.dockerignore` กัน `.env`; ไม่บันทึก secret ในเอกสาร; ต้องดู Render env แบบไม่เผยค่าและเตรียม Manager/SERVICE_STAFF ทดสอบ |
| Supabase Session Pooler SSL, Flyway/JPA | **ผ่านใน log รุ่นก่อน; รอ log ของ release ใหม่** | owner-supplied Render startup excerpt เห็น pooler `:5432`, 15 migrations validated, schema version 15, JPA initialized; ต้องตรวจ startup log ของ deploy `2f8bc4b` โดยไม่เผย secret |
| Deployment Diagram และ deploy/redeploy/rollback | **เอกสารเสร็จ** | [diagram](../diagrams/deployment-production-runtime.md) และ [runbook](production-runbook.md); รอ reviewer ตรวจ |
| Staff/Customer session หลัง restart | **ยังไม่ตรวจ public** | runbook ระบุวิธีและพฤติกรรมที่ต้องยืนยัน; ต้องทดสอบหลัง redeploy จริง |
| Public snapshot/Billing/Payment/duplicate/close | **ยังไม่ตรวจ public** | ต้องมี `[TEST DATA]` Manager/SERVICE_STAFF และ session จริง; local/integration tests ไม่แทน public gate |
| Strategy, Payment JPA/SOLID และสไลด์ runtime | **owner notes เสร็จ; Canva ยังรอรวม** | [code-linked notes](../architecture/teeramet-billing-payment-solid-jpa.md) และ [slide content](../slide/teeramet-runtime-networking-notes.md) |
| Render Free cold start | **ยังไม่วัด** | ต้องปล่อย idle แล้วจับเวลารอบแรกบน public URL; รายงานตัวเลขจริง |
| Public HTTPS + cookies/login/QR/Core Flow และข้อมูลหลัง redeploy | **ยังไม่ผ่านเกณฑ์รับรอง** | HTTPS หน้า/API/Swagger/health และ deployed SHA ผ่าน; ยังต้องครบ authenticated flow, cookie attributes และ persistence |

ข้อที่ติ๊กได้โดยไม่กล่าวเกินหลักฐาน: **ตรวจ source/sync**, **production multi-stage image และ URL เดียว**, **public SPA/API/asset routing**, **Deployment Diagram + runbook**, และ **owner notes ของ Strategy/Payment JPA/SOLID**. เนื้อหาสไลด์เป็นร่างพร้อมให้รวม ไม่ใช่ Canva/PDF ที่ผ่าน review. รายการที่ระบุ “บางส่วน” ไม่ควรติ๊กเป็นผ่านทั้งข้อใน Notion จนกว่าจะมี authenticated public evidence.
