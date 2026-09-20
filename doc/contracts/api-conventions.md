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
  "createdAt": "2026-09-18T14:30:00+07:00"
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

ทุก field ที่เป็นวันเวลาต้องอยู่ในรูปแบบ **ISO-8601 พร้อม timezone offset** เสมอ (Java type: `OffsetDateTime`)

```
YYYY-MM-DDTHH:mm:ssXXX
```

**ตัวอย่าง (ตรงกับที่กำหนดไว้ใน `shared-contracts.md`):**

```json
{
  "createdAt": "2026-09-18T10:00:00+07:00"
}
```

* ห้ามส่งเวลาแบบไม่มี timezone (เช่น `2026-09-18T14:30:00`)
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
| Delete | `204 No Content` | ถ้า resource ไม่พบ → `404 Not Found` |
| Validation Error | `400 Bad Request` | ใช้ `ErrorResponse` ตามรูปแบบด้านล่าง |

**Status code อื่นที่ใช้ร่วม:**

* `401 Unauthorized` — ไม่ได้ authenticate
* `403 Forbidden` — authenticate แล้วแต่ไม่มีสิทธิ์
* `404 Not Found` — ไม่พบ resource
* `409 Conflict` — ข้อมูลขัดแย้ง (เช่น ซ้ำ unique key)
* `500 Internal Server Error` — ข้อผิดพลาดฝั่ง server (ไม่ควรเกิดขึ้นจาก validation)

> **สถานะการ implement ปัจจุบัน:** `GlobalExceptionHandler.java` ตอนนี้มี handler จริงเฉพาะ `400 Bad Request` (validation error และ malformed/unreadable body) กับ `500 Internal Server Error` (catch-all `Exception`) เท่านั้น **ยังไม่มี handler สำหรับ `401`, `403`, `404`, `409`** — Module ที่ต้องใช้ status เหล่านี้ (เช่น resource not found, สิทธิ์ไม่พอ, ข้อมูลซ้ำ) ต้องเพิ่ม exception + handler ของตัวเองใน `GlobalExceptionHandler` พร้อมเขียน test ยืนยันรูปแบบ `ErrorResponse` ก่อน จึงจะถือว่า contract นี้ใช้ได้ครบสำหรับ status code นั้นๆ

---

## 6. รูปแบบ ErrorResponse

ทุก error response ต้องใช้โครงสร้างเดียวกันนี้ ตรงกับ `ErrorResponse` (Java record) ที่มีอยู่จริงใน `dto/response/ErrorResponse.java`:

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
  "timestamp": "2026-09-18T14:30:00+07:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Malformed request or unsupported enum value",
  "path": "/api/v1/orders"
}
```

**คำอธิบาย field:**

| Field | ประเภท | คำอธิบาย |
|---|---|---|
| `timestamp` | string | ISO-8601 พร้อม timezone |
| `status` | number | HTTP status code |
| `error` | string | HTTP reason phrase แบบ Title Case (เช่น `"Bad Request"`, `"Not Found"`) — **ไม่ใช่** `UPPER_SNAKE_CASE` เพราะ `GlobalExceptionHandler` ใช้ค่าจาก Spring `HttpStatus.getReasonPhrase()` โดยตรง (ยืนยันโดย `EnumErrorResponseTest`) |
| `message` | string | คำอธิบาย error โดยรวม (human-readable) |
| `path` | string | endpoint ที่เกิด error |

> **หมายเหตุ:** โครงสร้างปัจจุบันไม่มี field แยกรายฟิลด์ (เช่น `fieldErrors`) — ทุก validation error ทั้งหมดถูกรวมไว้ใน `message` เดียว ถ้า Module ใดต้องการ field-level detail เพิ่มเติม ต้องเสนอแก้ `ErrorResponse.java` และแจ้งทุก Module ก่อน ไม่ใช่เพิ่มเองใน DTO เฉพาะจุด

> **หมายเหตุความครบถ้วน:** `ErrorResponse` shape ด้านบนคือ contract ที่ยืนยันแล้วจริงสำหรับ error ที่ `GlobalExceptionHandler` จัดการอยู่ปัจจุบัน (400, 500) เท่านั้น สำหรับ `401`, `403`, `404`, `409` ให้ implement handler ใหม่ใน `GlobalExceptionHandler` ที่คืน `ErrorResponse` shape เดียวกันนี้ พร้อม unit test ยืนยัน ก่อนถือว่า Module นั้นปฏิบัติตาม contract ฉบับนี้ครบถ้วน

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
  "createdAt": "2026-09-18T14:30:00+07:00"
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
  "createdAt": "2026-09-18T14:30:00+07:00"
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
  "timestamp": "2026-09-18T14:31:00+07:00",
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
- [x] ไม่มี contract ที่ขัดกับ `shared-contracts.md` — แก้ `ErrorResponse` ให้ตรงกับ `dto/response/ErrorResponse.java` จริง, แก้ base path เป็น `/api/v1`, แก้ type inconsistency (`orderId` เป็น `number` ทุกจุด, `tableNumber` เป็น `string` ทุกจุด), จัดลำดับหัวข้อ 2.3/2.4 ให้ถูก, ฟันธง Update = `200 OK` เป็นมาตรฐานเดียว, และระบุชัดว่า `401/403/404/409` handler ยังไม่ implement จริง ต้องเพิ่ม handler+test ก่อนถือว่าใช้ contract สมบูรณ์
- [ ] ปวริศช์และ Feature Owner อย่างน้อย 1 คน review — ยังไม่ผ่าน รอ PR review