# ธีรเมธ Step 3 — สถานะที่ตรวจได้ ณ 8 ตุลาคม 2026

ตรวจ branch `teeramet_673380273-9_02` ที่ `bdd3bd7` ซึ่งรวม `develop` ถึง `e6172b2` แล้วและไม่มีงานค้างก่อนเพิ่มเอกสารชุดนี้. [CI ของ source SHA](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37807568449) ผ่านทั้ง backend/PostgreSQL และ frontend. **ยังไม่มีหลักฐานว่า public Render deploy SHA นี้**; ห้ามใช้ CI หรือหน้าเว็บ 200 แทนผล end-to-end.

| รายการจากแผน | สถานะ | หลักฐาน / งานถัดไป |
| --- | --- | --- |
| Sync branch, ตรวจ Dockerfile/Compose/providers | **เสร็จ** | Merge commit `2ea71ed`; ตรวจ [`Dockerfile.production`](../../Dockerfile.production), [`docker-compose.yml`](../../docker-compose.yml), [`application-production.yml`](../../code/backend/src/main/resources/application-production.yml) |
| Production multi-stage build, frontend/backend URL เดียว | **โค้ดพร้อม; รอตรวจ image รุ่นนี้** | Dockerfile build React → Spring Boot; public `/` และ Swagger เปิดได้ แต่เครื่องนี้เข้า Docker daemon ไม่ได้ จึงไม่ได้ build image ซ้ำ |
| `/api/v1` และ SPA refresh; API/asset ไม่ fallback | **บางส่วน** | Source/CI ของ PR #35 มี route tests; public `/kitchen/` และ `/Customer/QR/` ยัง 404; ต้อง deploy `bdd3bd7` แล้วทดสอบใหม่ |
| `PORT`, forwarded headers, Staff/Customer cookie, allowed origin | **บางส่วน** | Source production config และ cookie tests ผ่าน; public QR exchange ด้วย token ทดสอบที่ไม่ถูกต้องผ่าน origin guard แล้วตอบ 404; ยังต้องดู cookie attributes หลัง login/QR จริง |
| Production providers ใช้ session/database ตามแผน | **โค้ดพร้อม; รอตรวจ runtime** | ตั้งค่าใน `application-production.yml`; ต้องยืนยัน active profile และ flow บน release ที่ deploy |
| Secrets ใน Render และ bootstrap ปิดหลังเตรียมบัญชี | **รอยืนยัน runtime** | `.dockerignore` กัน `.env`; ไม่บันทึก secret ในเอกสาร; ต้องดู Render env แบบไม่เผยค่าและเตรียม Manager/SERVICE_STAFF ทดสอบ |
| Supabase Session Pooler SSL, Flyway/JPA | **ผ่านใน log รุ่นก่อน; รอ release ใหม่** | owner-supplied Render startup excerpt เห็น pooler `:5432`, 15 migrations validated, schema version 15, JPA initialized; ต้องผูกกับ deployed SHA ใหม่ |
| Deployment Diagram และ deploy/redeploy/rollback | **เอกสารเสร็จ** | [diagram](../diagrams/deployment-production-runtime.md) และ [runbook](production-runbook.md); รอ reviewer ตรวจ |
| Staff/Customer session หลัง restart | **ยังไม่ตรวจ public** | runbook ระบุวิธีและพฤติกรรมที่ต้องยืนยัน; ต้องทดสอบหลัง redeploy จริง |
| Public snapshot/Billing/Payment/duplicate/close | **ยังไม่ตรวจ public** | ต้องมี `[TEST DATA]` Manager/SERVICE_STAFF และ session จริง; local/integration tests ไม่แทน public gate |
| Strategy, Payment JPA/SOLID และสไลด์ runtime | **owner notes เสร็จ; Canva ยังรอรวม** | [code-linked notes](../architecture/teeramet-billing-payment-solid-jpa.md) และ [slide content](../slide/teeramet-runtime-networking-notes.md) |
| Render Free cold start | **ยังไม่วัด** | ต้องปล่อย idle แล้วจับเวลารอบแรกบน public URL; รายงานตัวเลขจริง |
| Public HTTPS + cookies/login/QR/Core Flow และข้อมูลหลัง redeploy | **ยังไม่ผ่านเกณฑ์รับรอง** | HTTPS หน้า/API/Swagger/health บางส่วนผ่าน; ต้องครบ authenticated flow, release SHA และ persistence |

ข้อที่ติ๊กได้จากงานรอบนี้โดยไม่กล่าวเกินหลักฐาน: **ตรวจ source/sync**, **Deployment Diagram + runbook**, และ **owner notes ของ Strategy/Payment JPA/SOLID**. เนื้อหาสไลด์เป็นร่างพร้อมให้รวม ไม่ใช่ Canva/PDF ที่ผ่าน review. รายการที่ระบุ “บางส่วน” ไม่ควรติ๊กเป็นผ่านทั้งข้อใน Notion จนกว่าจะมี public evidence.
