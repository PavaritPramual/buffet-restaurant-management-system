# API Conventions

เอกสารนี้กำหนดมาตรฐานกลางสำหรับ API, DTO และ JSON ที่ทุก Module ต้องปฏิบัติตาม เพื่อให้ Backend และ Frontend สื่อสารกันได้อย่างสอดคล้อง และลดความขัดแย้งระหว่าง Module

---

## 1. JSON Field Naming — camelCase

Field ทุกตัวใน JSON request/response ต้องใช้ `camelCase` เสมอ (ห้ามใช้ `snake_case` หรือ `PascalCase`)

**Base API path:** `/api/v1` (ตามที่กำหนดใน `shared-contracts.md`) — ทุก endpoint ต้อง prefix ด้วยเส้นทางนี้
**ID:** ใช้ JSON `number` และ Java `Long` เสมอ (ไม่ใช่ string code)
**จำนวนเงิน:** ใช้ JSON `number` และ Java `BigDecimal` เสมอ

**ตัวอย่างที่ถูกต้อง:**

```json
{
  "orderId": 1001,
  "customerName": "Somchai",
  "tableNumber": "12",
  "totalAmount": 1590.00,
  "createdAt": "2026-10-07T07:30:00Z"
}
```

**ตัวอย่างที่ผิด:**

```json
{
  "order_id": "ORD-2026-0001",
  "CustomerName": "Somchai"
}
```

---

## 2. Enum Convention

### 2.1 การส่งค่า Enum จาก Backend (Java)

Java enum ต้องถูก serialize เป็น **UPPERCASE string** เสมอ

```json
{
  "status": "RECEIVED",
  "paymentMethod": "CASH"
}
```

ค่า enum ทั้งหมดต้องอ้างอิงจาก Shared Enum ใน `shared-contracts.md` เท่านั้น (`TableStatus`, `DiningSessionStatus`, `OrderStatus`, `PaymentMethod`, `PaymentStatus`, `UserRole`) — ดูค่าที่กำหนดไว้ทั้งหมดในหัวข้อ 2.4

### 2.2 การรับค่า Enum จาก Client

* Backend รับเฉพาะค่าที่เป็น **UPPERCASE** และตรงกับ enum ที่กำหนดไว้เท่านั้น
* หากค่าที่ส่งมาเป็น **lowercase** หรือเป็นค่าที่ **ไม่รู้จัก (ไม่ตรงกับ enum ใดๆ)** ระบบต้องตอบกลับ `400 Bad Request` พร้อม `ErrorResponse` (ดูหัวข้อ 6)

**ตัวอย่างที่ Reject:**

```json
{ "status": "received" }   // lowercase → 400
{ "status": "COOKING" }    // ไม่มีใน enum → 400
```

ยืนยันแล้วโดย `SharedEnumSerializationTest` (Jackson `ObjectMapper` reject ทั้ง unknown และ lowercase enum ด้วย exception) และ `EnumErrorResponseTest` (controller คืน `400` พร้อม `ErrorResponse` ตามรูปแบบหัวข้อ 6)

### 2.3 ห้ามสร้าง Enum ซ้ำกับ Shared Enum

* ก่อนสร้าง enum ใหม่ ทุก Module ต้องตรวจสอบ `shared-contracts.md` ก่อนเสมอ
* Enum ที่มีความหมายร่วมกันข้าม Module (เช่น `OrderStatus`, `PaymentMethod`, `UserRole`) ต้องอ้างอิงจาก Shared Enum เดียวกัน ห้าม Module ใด Module หนึ่งประกาศ enum ซ้ำหรือคล้ายกันขึ้นมาเอง
* หากจำเป็นต้องเพิ่มค่าใหม่ใน enum ที่เป็น shared ต้องแก้ที่ต้นทาง (`shared-contracts.md`) และแจ้งทุก Module ที่เกี่ยวข้อง

### 2.4 ค่า Shared Enum ปัจจุบัน

อ้างอิงตรงจาก `doc/contracts/shared-contracts.md`, backend `domain/enums/*.java` และ frontend `contracts/shared.ts` (สามแหล่งตรงกัน):

| Enum | ค่าที่อนุญาต |
|---|---|
| `TableStatus` | `AVAILABLE`, `OCCUPIED` |
| `DiningSessionStatus` | `ACTIVE`, `COMPLETED`, `CANCELLED` |
| `OrderStatus` | `RECEIVED`, `PREPARING`, `READY`, `SERVED` |
| `PaymentMethod` | `CASH`, `QR`, `CARD` |
| `PaymentStatus` | `PENDING`, `PAID`, `FAILED` |
| `UserRole` | `SERVICE_STAFF`, `KITCHEN_STAFF`, `SUPERVISOR`, `MANAGER` |

Entity ทุกตัวต้องใช้ `@Enumerated(EnumType.STRING)` เท่านั้น ห้ามใช้ `EnumType.ORDINAL` (ตามที่ระบุไว้ท้าย `shared-contracts.md`)

---

## 3. รูปแบบวันเวลา — ISO-8601 + Timezone

ทุก field วันเวลาที่ส่งออกต้องอยู่ในรูปแบบ **ISO-8601 พร้อม timezone** เสมอ `OffsetDateTime` ใช้เมื่อมี offset และ `Instant` ใช้กับ timestamp ที่เป็น UTC

```
YYYY-MM-DDTHH:mm:ss[.fraction]Z
```

**ตัวอย่าง (ตรงกับที่กำหนดไว้ใน `shared-contracts.md`):**

```json
{
  "createdAt": "2026-10-07T08:09:10Z"
}
```

* API ปัจจุบัน serialize timestamps เป็น UTC (`Z`) แม้ UI จะแสดงตาม timezone ของเครื่องผู้ใช้
* ห้ามส่งเวลาแบบไม่มี timezone (เช่น `2026-10-07T14:30:00`)
* ห้ามส่งเป็น timestamp (epoch millis) ยกเว้นมีการตกลงเฉพาะกรณีและระบุไว้ใน field นั้นๆ อย่างชัดเจน

---

## 4. Required และ Nullable

| กติกา | คำอธิบาย |
|---|---|
| `required: true` | Field ต้องมีค่าเสมอในทุก request/response ถ้าขาดหายไป → `400 Bad Request` |
| `required: false` | Field เป็น optional สามารถไม่ส่งมาได้ |
| `nullable: true` | Field สามารถมีค่าเป็น `null` ได้อย่างมีความหมาย (เช่น ยังไม่ถูกกำหนดค่า) |
| `nullable: false` | Field ต้องไม่เป็น `null` หากส่งมาต้องมีค่าเสมอ |

* DTO ทุกตัวต้องระบุ `required` และ `nullable` ของแต่ละ field ไว้ใน schema/documentation อย่างชัดเจน
* Field ที่เป็น `required: true` และ `nullable: false` แต่ client ส่ง `null` มา → ต้องตอบ `400 Bad Request`

---

## 5. HTTP Status Codes

| Operation | Success | หมายเหตุ |
|---|---|---|
| Create | `201 Created` | คืนค่า resource ที่สร้างพร้อม `Location` header (ถ้ามี) |
| Read (single) | `200 OK` | ถ้าไม่พบ resource → `404 Not Found` |
| Read (list) | `200 OK` | คืน array ว่างถ้าไม่มีข้อมูล ไม่ใช่ 404 |
| Update | `200 OK` พร้อมคืน resource ที่อัปเดตแล้ว | มาตรฐานเดียวของทีม — ทุก Module ต้องคืน resource ฉบับล่าสุดใน body เสมอ ห้ามคืน `204 No Content` สำหรับ Update |
| Delete / archive | `204 No Content` | แนวทาง hard-delete, archive, archived-list, restore, repeat request และ safety conflicts อยู่ใน [shared deletion contract](deletion-contract.md) (approved baseline; implementation ต้องผ่าน resource-owner/schema/migration gates). ถ้าไม่พบ resource → `404 Not Found` |
| Validation Error | `400 Bad Request` | ใช้ `ErrorResponse` ตามรูปแบบด้านล่าง |

**Status code อื่นที่ใช้ร่วม:**

* `401 Unauthorized` — ไม่ได้ authenticate
* `403 Forbidden` — authenticate แล้วแต่ไม่มีสิทธิ์
* `404 Not Found` — ไม่พบ resource
* `409 Conflict` — ข้อมูลขัดแย้ง (เช่น ซ้ำ unique key)
* `500 Internal Server Error` — ข้อผิดพลาดฝั่ง server (ไม่ควรเกิดขึ้นจาก validation)

`GlobalExceptionHandler.java` มี handler สำหรับ `400`, `401`, `403`, `404`, `409`, `503` และ `500`; exception ที่ผ่าน handler ใช้ `ErrorResponse` รูปแบบเดียวกัน Endpoint ที่ตั้งใจคืน body ว่าง เช่น `GET /api/v1/auth/me` เมื่อไม่มี session จะระบุเป็นกรณียกเว้นใน contract ของ endpoint

---

## 6. รูปแบบ ErrorResponse

ทุก exception response ที่ผ่าน `GlobalExceptionHandler` ใช้โครงสร้างเดียวกันนี้ ตรงกับ `ErrorResponse` (Java record) ที่มีอยู่จริงใน `dto/response/ErrorResponse.java`:

```java
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
```

```json
{
  "timestamp": "2026-10-07T08:09:10Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Malformed request or unsupported enum value",
  "path": "/api/v1/orders"
}
```

**คำอธิบาย field:**

| Field | ประเภท | คำอธิบาย |
|---|---|---|
| `timestamp` | string | ISO-8601 UTC พร้อม timezone (`Z`) |
| `status` | number | HTTP status code |
| `error` | string | HTTP reason phrase แบบ Title Case (เช่น `"Bad Request"`, `"Not Found"`) — **ไม่ใช่** `UPPER_SNAKE_CASE` เพราะ `GlobalExceptionHandler` ใช้ค่าจาก Spring `HttpStatus.getReasonPhrase()` โดยตรง (ยืนยันโดย `EnumErrorResponseTest`) |
| `message` | string | คำอธิบาย error โดยรวม (human-readable) |
| `path` | string | endpoint ที่เกิด error |

> **หมายเหตุ:** โครงสร้างปัจจุบันไม่มี field แยกรายฟิลด์ (เช่น `fieldErrors`) — ทุก validation error ทั้งหมดถูกรวมไว้ใน `message` เดียว ถ้า Module ใดต้องการ field-level detail เพิ่มเติม ต้องเสนอแก้ `ErrorResponse.java` และแจ้งทุก Module ก่อน ไม่ใช่เพิ่มเองใน DTO เฉพาะจุด

Controller จะคืน DTO โดยตรงหรือห่อด้วย `ResponseEntity<DTO>` ก็ได้เมื่อ HTTP status เป็น `200` และ JSON body มี shape เดียวกัน ใช้ `ResponseEntity` เมื่อต้องกำหนด status/header เช่น `201 Location`, `Set-Cookie` หรือ `Cache-Control` ข้อผิดพลาดทั้งหมดใช้ `ErrorResponse` จาก handler กลาง

ข้อยกเว้นที่ระบุชัด: `GET /api/v1/auth/me` ตอบ `401` แบบไม่มี body เพื่อให้ frontend ตรวจ session ต่อได้โดยไม่แสดง error message; การ login ที่ไม่ผ่านและ auth failures ที่ผ่าน handler ยังคงใช้ `ErrorResponse`

---

## 7. ตัวอย่าง Request/Response ฉบับสมบูรณ์

ตัวอย่างนี้อิง `OrderFulfillmentContext` ซึ่งเป็น contract จริงที่มี Owner ร่วมกันระหว่าง ศิระพัทธ์ (Order data) และ ศรัณย์ (fulfillment contract review) ตาม `shared-contracts.md`

**อ่าน Order (Read):**

Request `GET /api/v1/orders/1001`

Response `200 OK`

```json
{
  "orderId": 1001,
  "sessionId": 501,
  "tableNumber": "12",
  "items": [
    { "menuItemId": 30, "name": "Grilled Pork Skewer", "quantity": 2 },
    { "menuItemId": 45, "name": "Tom Yum Soup", "quantity": 1 }
  ],
  "status": "RECEIVED",
  "createdAt": "2026-10-07T08:09:10Z"
}
```

**อัปเดตสถานะ Order (Update):**

Request `PATCH /api/v1/orders/1001/status`

```json
{ "status": "PREPARING" }
```

Response `200 OK`

```json
{
  "orderId": 1001,
  "sessionId": 501,
  "tableNumber": "12",
  "items": [
    { "menuItemId": 30, "name": "Grilled Pork Skewer", "quantity": 2 },
    { "menuItemId": 45, "name": "Tom Yum Soup", "quantity": 1 }
  ],
  "status": "PREPARING",
  "createdAt": "2026-10-07T08:09:10Z"
}
```

**Validation Error ตัวอย่าง (lowercase enum):**

Request `PATCH /api/v1/orders/1001/status`

```json
{ "status": "preparing" }
```

Response `400 Bad Request`

```json
{
  "timestamp": "2026-10-07T08:09:11Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Malformed request or unsupported enum value",
  "path": "/api/v1/orders/1001/status"
}
```

---

## 8. ความสอดคล้องกับ Shared Contracts

* เอกสารนี้ต้องไม่มีข้อกำหนดใดขัดแย้งกับ `shared-contracts.md`
* หากพบความขัดแย้ง ให้ยึด `shared-contracts.md` เป็นหลัก และแก้ไขเอกสารนี้ให้สอดคล้องกัน
* การเปลี่ยนแปลง enum ค่าที่ใช้ร่วมกันหลาย Module ต้องแจ้งและได้รับการ review จาก Feature Owner ที่เกี่ยวข้องก่อน merge

---

## 9. Review Checklist

- [x] ตัวอย่าง request/response อ่านแล้วนำไปใช้ได้ทันที — อิงจาก `OrderFulfillmentContext` จริงใน `shared-contracts.md` และ `shared.ts` โดยตรง
- [x] ค่า enum ตรงกับ backend และ frontend — ตรวจสอบกับ `domain/enums/*.java` และ `contracts/shared.ts` แล้ว (ทั้งสองฝั่งตรงกัน)
- [x] ไม่มี contract ที่ขัดกับ `shared-contracts.md` — base path `/api/v1`, ID เป็น JSON number, `ErrorResponse` ตรงกับ DTO จริง และ handler ครอบคลุม status ที่ใช้อยู่
- [ ] ปวริศช์และ Feature Owner อย่างน้อย 1 คน review — ยังไม่ผ่าน รอ PR review

---

## 10. Contract audit — Stock/Profile, billing and serialization (7 October 2026)

The audited implementation is `origin/develop` at baseline `0dbbe1b`; these are current wire contracts, not claims about a deployed release.

| API | Request / response contract | Status and errors |
|---|---|---|
| `POST /api/v1/stock/items` | `sku`, `name`, `unit`, `lowStockThreshold`; threshold is a JSON number / Java `BigDecimal`, precision up to 3 decimals | `201` with `Location`; validation/duplicate errors use `ErrorResponse` (`400`/`409`) |
| `PUT /api/v1/stock/items/{id}` | Same metadata fields; quantity is never set here | `200`; invalid/not found/conflict use `ErrorResponse` |
| `POST /api/v1/stock/{itemId}/in` | Positive numeric `quantity` (up to 3 decimals) and non-blank `reason` | `200`; invalid amount/reason/balance is `400 ErrorResponse`; missing item is `404 ErrorResponse` |
| `POST /api/v1/stock/{itemId}/adjustments` | Non-zero signed numeric `quantityDelta` (up to 3 decimals) and non-blank `reason` | `200`; invalid amount/reason/balance is `400 ErrorResponse` |
| `POST /api/v1/admin/users` | `username`, `password`, `displayName`, nullable/optional `email`, and `role`; optional email is validated as an email address | `201`; invalid fields are `400 ErrorResponse`; duplicate username is `409 ErrorResponse`; password is never returned |
| `GET /api/v1/admin/users` | Returns `id`, `username`, `displayName`, nullable `email`, and `role` | `200`; errors use `ErrorResponse` |
| `GET /api/v1/auth/me`, `POST /api/v1/auth/login` | `UserContext` is `userId`, `username`, `displayName`, `role` | `/me` without a session intentionally returns empty `401`; login errors use `ErrorResponse` |

Stock and package prices are JSON numbers (`BigDecimal` on the backend, `number` in the frontend); frontend form strings are converted to numbers before sending. Stock response quantities and transaction deltas are also JSON numbers. Enum values are case-sensitive uppercase strings. `Instant` values and API-created `OffsetDateTime` timestamps use UTC `Z`; session start/end values are mapped from the UTC database clock.

Staff login issues the `JSESSIONID` cookie. Customer QR exchange accepts its one-time token only in the `POST /api/v1/dining-sessions/qr-exchange` body, then sets an HttpOnly `customer_session` cookie; customer context/bill calls use that cookie and do not return it. Examples use only synthetic IDs and contain no live QR, cookie, password, or session-token values. The public deployment URL is still unconfirmed, so the public Swagger endpoint has not been checked.

Billing preview accepts only `{"sessionId":12}`. Payment accepts only `sessionId` and `paymentMethod`; amount is calculated server-side. `BillSummary.totalAmount` is the net total, while customer bill status reports numeric `dueAmount` and `paidAmount` separately. Payment and session close are separate staff actions; closing requires a recorded PAID payment.
