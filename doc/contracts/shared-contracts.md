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
| `sessionToken` | string | No | Token สำหรับ QR/session lookup |
| `packageId` | number | No | Buffet Package identifier |
| `tableId` | number | No | Restaurant Table identifier |
| `tableNumber` | string | No | หมายเลขโต๊ะที่แสดงต่อผู้ใช้ |
| `sessionStatus` | string | No | `DiningSessionStatus` |
| `adultCount` | number | No | Integer ตั้งแต่ 0 ขึ้นไป |
| `childCount` | number | No | Integer ตั้งแต่ 0 ขึ้นไป |

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
| `packagePrice` | number | No | ราคาต่อคนของ Package |
| `adultCount` | number | No | จำนวนผู้ใหญ่ |
| `childCount` | number | No | จำนวนเด็ก |
| `discountContext` | object | Yes | Billing owner กำหนดรายละเอียดภายใน |
| `sessionStatus` | string | No | `DiningSessionStatus` |

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
