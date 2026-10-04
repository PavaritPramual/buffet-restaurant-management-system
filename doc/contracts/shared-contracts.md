# Shared Contracts

เอกสารนี้เป็น canonical contract ระหว่าง Module จนกว่าจะถึง Integration Checkpoint การเปลี่ยน field, type หรือ enum ต้องแจ้ง Entity Owner และ Module ที่ใช้งานก่อน merge

## Conventions

- Base API: `/api/v1`
- ID ใช้ JSON number และ Java `Long`
- จำนวนเงินใช้ JSON number และ Java `BigDecimal`
- วันเวลาใช้ ISO-8601 พร้อม timezone เช่น `2026-09-18T10:00:00+07:00`
- Enum ส่งผ่าน JSON เป็น uppercase string และไม่รับ lowercase
- Field ที่ไม่ระบุว่า nullable ต้องมีค่า

## SessionContext

Owner: ปวริศช์ — Table & Dining Session

| Field | JSON type | Nullable | Notes |
|---|---|---|---|
| `sessionId` | number | No | Dining Session identifier |
| `sessionToken` | string | No | QR token แบบใช้ครั้งเดียวใน response พนักงานเท่านั้น; ต้องไม่อยู่ใน response ลูกค้า |
| `packageId` | number | No | Buffet Package identifier |
| `tableId` | number | No | Restaurant Table identifier |
| `tableNumber` | string | No | หมายเลขโต๊ะที่แสดงต่อผู้ใช้ |
| `sessionStatus` | string | No | `DiningSessionStatus` |
| `adultCount` | number | No | Integer ตั้งแต่ 0 ขึ้นไป |
| `childCount` | number | No | Integer ตั้งแต่ 0 ขึ้นไป |

ลูกค้าเปิด `/customer/qr#token={sessionToken}`; หน้าเว็บล้าง fragment แล้วส่ง token ใน body ของ `POST /api/v1/dining-sessions/qr-exchange` พร้อม `Origin` ที่อนุญาต Backend หมุน QR token และออก `customer_session` แบบสุ่มใน `HttpOnly` cookie อายุสูงสุด 8 ชั่วโมง เก็บเฉพาะ SHA-256 hash ของ credential ใน `customer_session_grants` ลูกค้าหลายเครื่องแลก QR รุ่นถัดไปได้คนละ credential

Customer response มีเพียง `sessionId`, `packageId`, `tableNumber`, `sessionStatus`; ไม่ส่ง `sessionToken`, ราคา snapshot หรือข้อมูล Billing คำขอเมนู/ออเดอร์ต้องมี cookie ของ session เดียวกันที่ยัง `ACTIVE` และคำขอเขียนต้องผ่าน Origin check การปิดรอบเพิกถอน credentials ทั้งหมด

## OrderFulfillmentContext

Owner: ศิระพัทธ์ (Order data) / ศรัณย์ (fulfillment contract review)

| Field | JSON type | Nullable | Notes |
|---|---|---|---|
| `orderId` | number | No | Order identifier |
| `sessionId` | number | No | Dining Session identifier |
| `tableNumber` | string | No | หมายเลขโต๊ะ |
| `items` | array | No | `{ menuItemId, name, quantity }` |
| `status` | string | No | `OrderStatus` |
| `createdAt` | string | No | ISO-8601 with timezone |

## BillingContext

Owner: ธีรเมธ — Billing & Payment

| Field | JSON type | Nullable | Notes |
|---|---|---|---|
| `sessionId` | number | No | Dining Session identifier |
| `packagePrice` | number | No | ราคาแพ็กเกจ ณ ตอนเปิด Dining Session จาก `package_price_at_open`; backend อ่านเอง ไม่รับจาก browser |
| `adultCount` | number | No | จำนวนผู้ใหญ่ |
| `childCount` | number | No | จำนวนเด็ก |
| `discountContext` | object | Yes | Billing owner กำหนดรายละเอียดภายใน |
| `sessionStatus` | string | No | `DiningSessionStatus` |

Dining Session owner ให้ข้อมูลผ่าน `DiningSessionBillingReader.requireBySessionId(sessionId)` ซึ่งคืนราคา snapshot, จำนวนคน และสถานะ; Billing owner เป็นผู้เติม `discountContext` และคำนวณยอด

## Billing preview API

Owner: ธีรเมธ — Billing & Payment (pending cross-module review)

`POST /api/v1/billing/preview` accepts `{ "sessionId": 1 }` only as business input.
`sessionId` is a required positive integer (Java `Long`). Browser-supplied prices,
counts, status or discounts are not used. `BillingContext` above is internal backend
calculation input; its existing fixtures remain calculation examples, not HTTP request bodies.
The backend provider must resolve the price snapshot at session opening, guest counts,
status and backend-owned discount rules. Only ACTIVE sessions can be previewed for payment.

Response 200: `sessionId`, `subtotalNoneDiscount`, `discountAmount`, `totalBeforeRounding`,
`roundingAdjustment`, `totalAmount` (all JSON numbers). The engine rounds the final total
once to 2 decimals with HALF_UP; `roundingAdjustment = totalAmount - totalBeforeRounding`.
Preview performs no payment write and does not close a session.

Errors use ErrorResponse: 400 invalid input/inactive session, 404 session not found,
503 provider unavailable. The default provider currently returns 503; database adapter
and staff authorization remain pending integration. Do not expose as a production staff API yet.
Request fixture: `test/fixtures/billing-preview-request.json`.
Frontend: `/billing/preview` for entering an ID, `/staff/sessions/:sessionId/billing` for a selected session.


## PaymentResult

### Payment HTTP API

- `POST /api/v1/payments`: รับ `{sessionId, paymentMethod}`; backend อ่านราคา snapshot และคำนวณยอดเอง คืน PaymentResult และ HTTP 201 เมื่อบันทึก PAID
- `GET /api/v1/payments/sessions/{sessionId}`: อ่าน PaymentResult เดิมโดยไม่สร้างหรือส่งชำระซ้ำ; ไม่มีรายการคืน 404
- ทั้งสอง endpoint ต้อง login เป็น SERVICE_STAFF; ไม่ login คืน 401, role อื่นคืน 403
- หลังบันทึก PAID ยังต้องกดปิดรอบแยก; close rule อ่านสถานะจาก PaymentStatusLookup
- การอ่านสถานะล้มเหลวไม่เท่ากับยังไม่ชำระ; frontend ต้องตรวจผลอีกครั้งก่อนทำรายการต่อ
- Endpoint อ่านสถานะเป็น contract เพิ่มเติมที่ต้องให้ศรัณย์/ปวริศช์ review ใน PR

Owner: ธีรเมธ — Billing & Payment

| Field | JSON type | Nullable | Notes |
|---|---|---|---|
| `paymentId` | number | No | Payment identifier |
| `sessionId` | number | No | Dining Session identifier |
| `paymentMethod` | string | No | `PaymentMethod` |
| `paymentStatus` | string | No | `PaymentStatus` |
| `paidAt` | string | No | ISO-8601 with timezone |

## UserContext

Owner: เมธัส — Authentication

| Field | JSON type | Nullable | Notes |
|---|---|---|---|
| `userId` | number | No | User identifier |
| `username` | string | No | ชื่อบัญชีพนักงาน |
| `role` | string | No | `UserRole` |
| `active` | boolean | No | สถานะบัญชี |

## Shared Enums

| Enum | Values |
|---|---|
| `TableStatus` | `AVAILABLE`, `OCCUPIED` |
| `DiningSessionStatus` | `ACTIVE`, `COMPLETED`, `CANCELLED` |
| `OrderStatus` | `RECEIVED`, `PREPARING`, `READY`, `SERVED` |
| `PaymentMethod` | `CASH`, `QR`, `CARD` |
| `PaymentStatus` | `PENDING`, `PAID`, `FAILED` |
| `UserRole` | `SERVICE_STAFF`, `KITCHEN_STAFF`, `SUPERVISOR`, `MANAGER` |

Entity ต้องใช้ `@Enumerated(EnumType.STRING)` เท่านั้น ห้ามใช้ `EnumType.ORDINAL`
