# README Final และ public Swagger smoke — 10 ตุลาคม 2026

ตรวจโดยปวริศช์ ช่วงประมาณ 14:36–14:43 น. Asia/Bangkok โค้ดอ้างอิง develop `855a954a29eb0354c07851fb8da1e44f7adc2f9a` หลัง PR #49/#52 merge ผู้ใช้แจ้ง deploy develop ล่าสุดก่อนตรวจ รอบนี้ตรวจ public URL โดยตรง ไม่ได้อ่าน Render dashboard หรือ startup log ชุดใหม่ จึงไม่ใช้ผล HTTP เป็นหลักฐานยืนยัน SHA ทุก byte

## ผลตรวจ

| จุดตรวจ | ผลจริง | หลักฐาน |
|---|---|---|
| Swagger UI สาธารณะ | โหลดรายการ API และ operation details สำเร็จหลัง cold start | Browser ที่ `/swagger-ui/index.html` |
| POST menu-items เอกสาร | 201, MenuItemResponse, Location uri-reference; ไม่มี default 200 | [OpenAPI excerpts](../../test/evidence/readme-final-public-2026-10-10/openapi-check.json) และตรวจ response table ใน Swagger UI |
| POST session orders เอกสาร | 201, OrderResponse, Location uri-reference; ไม่มี default 200 | OpenAPI excerpts และ Swagger UI |
| ErrorResponse schema | status/error/message/path ไม่มี example เฉพาะ Billing; fields/types เดิมครบ | OpenAPI excerpts; Stock 401/403 example เป็น placeholder กลาง |
| Swagger Execute GET stock ไม่ login | 401 Unauthorized, A signed-in user is required, path /api/v1/stock | Browser response เวลา 14:41:58; [HTTP smoke ตรวจซ้ำ](../../test/evidence/readme-final-public-2026-10-10/http-smoke.json) |
| System health | 200, status UP | HTTP smoke เวลา 14:42:26 |
| README ตามโจทย์ | ชื่อ/รหัส/Section/branch ครบ 5 คน; แยกงานเขียนโค้ดกับ SOLID/Patterns/JPA/diagrams/tests และ integration เพิ่ม | README และใบงานข้อ 12 |
| เอกสารปัจจุบัน | PR #49/#52 merge แล้ว, ERD canonical ถึง V19, เชื่อม UAT public และผล Swagger หลังแก้ | README |

ตอนเริ่ม OpenAPI timeout 60 วินาทีสองครั้งและ health timeout 45 วินาที Browser แสดง Render Application loading ก่อนเปิด Swagger สำเร็จ บันทึกเป็นพฤติกรรม cold start ในรอบนี้ ไม่รับรองระยะ cold start ที่แน่นอนหรือ SLA

## ขอบเขตหลักฐาน

รอบนี้ปิดข้อเอกสาร DOC-01/DOC-02 ของ PR #52 บน Swagger สาธารณะ และตรวจ read-only HTTP smoke ไม่สร้างเมนู/ออเดอร์หรือทำ Billing/Payment/close ซ้ำ ผล create runtime/Location และ 58 tests ใช้รายงาน local PR #52 ที่ระบุ environment ชัดเจน ส่วน public Stock UAT และ Core Flow ใช้รายงานเดิมแยกตาม revision

ไม่ query/apply migration หรือแก้ข้อมูลกลาง; V19/JPA อ้าง startup evidence ใน PR #52 ไม่ใช้ smoke รอบนี้แทน checksum/DB privilege audit การรับรอง main/release, owner Git audit และชุด Canva export/ซ้อมยังต้องมีหลักฐานของงานนั้น