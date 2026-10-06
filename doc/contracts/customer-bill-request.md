# Customer ขอคิดบิล — Step 2 extension

## สิทธิ์และ lifecycle

ลูกค้าใช้ `customer_session` HttpOnly cookie ของรอบ ACTIVE และ session ID ที่ตรงกัน ทุก response ใช้ `Cache-Control: no-store` POST ต้องมี Origin ใน allowlist เดิม ไม่ใช้ QR token ใน URL และไม่รับราคา ยอดเงิน หรือ PAID จากลูกค้า

V14 เพิ่ม `dining_sessions.bill_requested_at TIMESTAMP WITH TIME ZONE NULL DEFAULT NULL` โดยคง enum ACTIVE/COMPLETED เดิม เมื่อขอแล้วทุกเครื่องดูเมนู/ออเดอร์/บิลได้ แต่สร้างออเดอร์เพิ่มไม่ได้ ไม่มีการยกเลิกคำขอใน Step 2

## API

### Customer response semantics

`status` ใช้ Java enum `CustomerBillStatus` และ frontend union ที่ตรงกัน เป็นสถานะของ bill flow แยกจาก `DiningSessionStatus` และ `PaymentStatus` ไม่เพิ่มสถานะ session หรือคอลัมน์ฐานข้อมูล

| Bill status | สั่งเพิ่ม | dueAmount | paidAmount | รอบกิน |
| --- | --- | --- | --- | --- |
| NOT_REQUESTED | ได้ | ยอดสุทธิที่ต้องจ่าย | 0 | ACTIVE |
| REQUESTED | ไม่ได้ | ยอดสุทธิที่ต้องจ่าย | 0 | ACTIVE |
| PAID | ไม่ได้ | 0 | ยอด PAID ที่บันทึกจริง | ยัง ACTIVE จน Staff กด close |

`bill.totalAmount` หมายถึงยอดสุทธิของบิลเสมอ ไม่ใช่ยอดค้างชำระ ก่อนจ่ายคำนวณจากราคา snapshot ผ่าน BillingEngine หลังจ่ายใช้ยอด Payment ที่บันทึกไว้เป็นยอดสุทธิสุดท้ายของบิล โดยแสดงยอดค้างจาก `dueAmount` และยอดที่จ่ายแล้วจาก `paidAmount` เท่านั้น ไม่เปลี่ยน DTO `BillSummary` หรือ `PaymentResult` ของโมดูล Billing/Payment

ตัวอย่างบิล 997.50 บาทหลังจ่าย:

```json
{
  "sessionId": 1,
  "status": "PAID",
  "requestedAt": "2026-10-06T07:00:00Z",
  "bill": {"sessionId": 1, "subtotalAmount": 997.50, "discountAmount": 0.00, "totalAmount": 997.50},
  "dueAmount": 0.00,
  "paidAmount": 997.50
}
```

### Payment หลังคำขอคิดบิล

`POST /api/v1/payments` รับชำระได้เฉพาะรอบ `ACTIVE` ที่มี `bill_requested_at` แล้ว หากยังไม่ขอคิดบิลจะคืน `409 ErrorResponse` โดยไม่สร้าง Payment พนักงานยังดูยอดล่วงหน้าได้ แต่หน้า Billing ปิดปุ่มรับชำระจนพบคำขอจริงจาก Staff session API ให้กดดูบิลใหม่หลังลูกค้าขอคิดบิล

Payment ล็อกแถว DiningSession ด้วย `PESSIMISTIC_WRITE` ใน transaction เดียวกับการบันทึก PAID แล้ว refresh/re-check ACTIVE และเวลาขอหลังได้ lock ใช้แถวเดียวกับ Order, Request Bill และ Close การชำระไม่เปิดรับ Order กลับและไม่ปิดโต๊ะอัตโนมัติ ไม่เพิ่ม migration หรือเปลี่ยน DTO ของ Payment

| Method / path | ผล |
| --- | --- |
| POST `/api/v1/dining-sessions/{sessionId}/bill-request` | ล็อกแถว session แบบเดียวกับ Order/close บันทึกเวลาครั้งแรก กดซ้ำคืนเวลาเดิม |
| GET `/api/v1/dining-sessions/{sessionId}/bill-status` | อ่านสถานะและยอดจาก BillingEngine/package snapshot; หาก PAID ใช้ amount ที่ Payment บันทึกจริง |

```json
{
  "sessionId": 12,
  "status": "REQUESTED",
  "requestedAt": "2026-10-06T06:00:00Z",
  "bill": { "sessionId": 12, "subtotalAmount": 997.50, "discountAmount": 0.00, "totalAmount": 997.50 }
}
```

`status`: NOT_REQUESTED / REQUESTED / PAID เป็นสถานะหน้าบิล ไม่ใช่สถานะ DiningSession ลูกค้าไม่เห็น credential, sessionToken, paymentId, วิธีรับชำระหรือผู้รับชำระใน DTO นี้

ErrorResponse: cookie ขาด/ผิด/หมดอายุ 401; Origin ผิด 403; cookie ผิดรอบหรือรอบปิด 404; Order หลังขอคิดบิล 409 การขอคิดบิลไม่ได้ชำระหรือปิดโต๊ะ

Order ที่ได้ lock ก่อนคำขอคิดบิลบันทึกเสร็จก่อน คำขอที่ได้ lock หลังคำขอคิดบิลต้องถูกปฏิเสธ รอบ PAID ยังต้องให้ SERVICE_STAFF กด close แยกและตรวจ Payment ของรอบเดิม

## Staff / UI

Staff DTO เพิ่ม `billRequestedAt` หน้าโต๊ะและรายละเอียดแสดง badge ขอคิดบิล แล้วเปิดหน้าบิลเดิม Customer ตรวจสถานะทุก 5 วินาทีเมื่อหน้าแสดงอยู่ หยุดเมื่อ unmount และกันผลจาก QR เก่าเขียนทับรอบใหม่ หากอ่านบิลไม่ได้หยุดปุ่มสั่งชั่วคราวและแสดงข้อผิดพลาด ไม่สมมติว่าจ่ายแล้ว

## Manager master data

Manager ใช้ `/admin/tables`, `/admin/packages`, `/admin/soups`, `/admin/stock-items` ภายใน shell และ ManagerRoute เดิม โต๊ะ ACTIVE ห้ามแก้/ลบ/เปลี่ยนสถานะ และโต๊ะมีประวัติห้ามลบ ใช้ lock โต๊ะร่วมกับเปิดรอบ ไม่ให้ PATCH status สร้าง OCCUPIED ที่ไม่มี session

Stock catalog POST `/api/v1/stock/items` และ PUT `/api/v1/stock/items/{id}` รับ sku/name/unit/lowStockThreshold เฉพาะ Manager ยอดใหม่ศูนย์ แก้ยอดผ่าน in/adjustments เดิมเท่านั้น SKU/หน่วยเปลี่ยนไม่ได้หลังมี audit transaction ไม่เพิ่ม DELETE, active lifecycle หรือ opening target ในรอบนี้

## Migration / rollout

V1–V12 ที่ apply บน Supabase คง checksum เดิม V13 security และ V14 bill request ยังรอ review/merge/deployment ทดสอบเฉพาะฐานทิ้งได้ ห้ามเริ่ม image ใหม่กับฐานกลางก่อนรับรอง migration แล้วตรวจ validate/grants/merged develop smoke หลัง deploy
