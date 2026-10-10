# PR #52 — Swagger correction verification

ตรวจวันที่ 10 ตุลาคม 2026 ประมาณ 13:14–13:19 น. (Asia/Bangkok)

- Code commit: `1823c7f53a98e9992f7f93c28471ec1f863eba06`
- Base ที่รวมแล้ว: `develop` / PR #49 merge `af2b45ba5d73cfcb271d312f978374b394dd4b97`
- Environment: local Spring Boot 3.5.16 / Java 21.0.12.1 / Maven 3.9.12, compile release 17; in-memory H2 ใหม่ชื่อ `swagger_pr52`, profile `demo`, seed และ bootstrap ปิด
- Swagger UI: `http://localhost:18888/swagger-ui/index.html`; OpenAPI: `http://localhost:18888/v3/api-docs`
- ใช้ local H2 common+h2 migrations เท่านั้น ไม่เชื่อม Supabase ไม่รัน SQL/migration บนฐานกลาง ไม่เปลี่ยน Render และไม่ได้ทดสอบ public release ใหม่

## การแก้และผลตรวจ

| ข้อ | ผลที่คาดหวัง | ผลจริงหลังแก้ในเครื่อง | หลักฐาน |
|---|---|---|---|
| DOC-01 | Shared ErrorResponse ไม่ฝัง status/error/path ของ Billing ใน error ของ endpoint อื่น; คง fields เดิม | **ผ่าน local**: เอา examples เฉพาะ operation ออกจาก status/error/message/path; timestamp/status/error/message/path และ types เดิมครบ; Stock 401/403 และ Fulfillment 409 แสดง schema sample กลาง | [Stock 401/403](swagger-stock-neutral-errors.png), [Fulfillment 409](swagger-fulfillment-neutral-409.png), [OpenAPI excerpt](api-docs-excerpts.json) |
| DOC-02 / Menu create | POST /menu-items ประกาศ 201, MenuItemResponse และ Location ตรง runtime | **ผ่าน local**: มี 201 ไม่มี default 200, application/json MenuItemResponse; Location เป็น relative URI `/api/v1/menu-items/{createdId}` | [Swagger menu 201](swagger-menu-create-201.png), [tests](test-results.json) |
| DOC-02 / Order create | POST /dining-sessions/{sessionId}/orders ประกาศ 201, OrderResponse และ Location ตรง runtime | **ผ่าน local**: มี 201 ไม่มี default 200, application/json OrderResponse; Location เป็น `/api/v1/dining-sessions/{sessionId}/orders/{createdOrderId}` | [Swagger order 201](swagger-order-create-201.png), [tests](test-results.json) |
| Runtime error | GET /stock โดยไม่ login ยังตอบ 401/ErrorResponse ของ operation จริง | **ผ่าน local Swagger Execute** เวลา 13:19:14: 401 Unauthorized, message A signed-in user is required, path /api/v1/stock | [Execute 401](swagger-stock-execute-401.png), [ข้อความจากหน้าจอ](swagger-stock-rendered.txt) |
| Regression | OpenAPI และ runtime Location ยังตรงกัน; Stock consumption/rollback ไม่เปลี่ยน | **ผ่าน local** 58 tests, failures/errors/skipped=0; package BUILD SUCCESS | [test-results.json](test-results.json), [local startup](local-startup.txt) |

`status: 0` และ `string` ใน Example Value เป็น placeholder ที่ Swagger สร้างจากชนิดข้อมูลหลังเอา examples กลางออก **ไม่ใช่ HTTP response จริง**. วิธีนี้ตรงทางเลือกใน review ให้เอาตัวอย่างกลางที่ชวนเข้าใจผิดออก; HTTP code ของแต่ละ response และ response ที่ Execute ได้เป็นตัวอ้างอิงสถานะจริง. ไม่ได้เปลี่ยน ExceptionHandler, service, authorization, business rules หรือ migration ให้ตรงตัวอย่างเดิม

Swagger UI รอบนี้ตรวจการแสดงเอกสารและ Execute GET แบบไม่ login เท่านั้น. การสร้างเมนู/ออเดอร์จริงและ Location ตรวจด้วย MockMvc integration tests บน H2; ไม่อ้างว่าได้สร้างข้อมูลใหม่บน public release รอบนี้

## การทดสอบและตรวจซ้ำ

เปิด terminal ใน project backend:

```bash
cd /home/koji/CS3-1/Prinsible_software/buffet-restaurant-management-system/code/backend
mvn --batch-mode --no-transfer-progress -Dtest=OpenApiContractIntegrationTest,MenuCatalogIntegrationTest,OrderingIntegrationTest,OrderFulfillmentControllerTest,MenuStockConsumptionIntegrationTest test
mvn --batch-mode --no-transfer-progress -DskipTests package
```

ชุด `/v3/api-docs` เพิ่ม assertions: create 201/no200, response DTO, Location schema/example, ErrorResponse fields/types และไม่มี examples เฉพาะ operation; เพิ่ม error schema ของ Fulfillment 409. MenuCatalog/Ordering integration tests ตรวจ Location ของ response จริงเทียบ ID ที่สร้าง. Consumption integration tests ตรวจ snapshot, duplicate และ rollback บนฐานทดสอบ

ผลแยกตาม environment:

- **Local:** ผ่านตามตารางและภาพข้างต้น; ปิด process และแท็บ Swagger หลังเก็บหลักฐาน
- **CI:** ตรวจ checks ของ commit ใหม่บน [PR #52](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/52/checks); การผ่าน CI ไม่ยืนยันว่า public deploy ได้รับโค้ดนี้แล้ว
- **Public retest:** ยังไม่ได้ deploy/retest การแก้ Swagger นี้; DOC-01/DOC-02 เดิมของ `af2b45b` ยังคงเป็นผลไม่ผ่านทางเอกสารตาม [รายงาน UAT เดิม](../uat-buffet-2026-10-10/report.md)

หลัง review/merge และ deploy commit ที่มีการแก้ จึงตรวจสอง create operations และ error examples บน Swagger สาธารณะอีกครั้ง ก่อนรับรองว่าข้อเอกสารนี้ปิดบน public runtime
