# Template Method, SOLID และสไลด์ — Auth/Stock

สถานะ: implemented ใน `StockTransactionTemplate` (โค้ดจริง) ยกเว้นหัวข้อที่ระบุว่า planned

## 1. Template Method ใน Stock

คลาสฐาน [StockTransactionTemplate](../../code/backend/src/main/java/com/buffetrestaurant/service/StockTransactionTemplate.java) กำหนดอัลกอริทึมใน `process(...)` ที่เป็น `final` ไม่ให้ subclass เปลี่ยนลำดับได้

| ลำดับ | ขั้นตอน | ผู้รับผิดชอบ |
| --- | --- | --- |
| 1 | ตรวจ actor (ต้องล็อกอิน มิฉะนั้น 401) | ขั้นร่วมในฐาน |
| 2 | ตรวจ `item.isActive()` ถ้า inactive → `InactiveStockItemException` (HTTP 409) | ขั้นร่วมในฐาน (เพิ่มโดย V15) |
| 3 | `calculateDelta(quantity)` | **override** — `StockInProcessor` ใช้ +quantity, `StockAdjustmentProcessor` ใช้ signed delta |
| 4 | ตรวจยอดใหม่ไม่ติดลบ | ขั้นร่วมในฐาน |
| 5 | `item.applyDelta` + `items.save` | ขั้นร่วมในฐาน |
| 6 | บันทึก `StockTransaction` (type, delta, balanceAfter, reason, actor) | ขั้นร่วมในฐาน; `transactionType()` **override** |

Steps ที่ override มีเพียง `calculateDelta` และ `transactionType` จึงเพิ่มประเภทธุรกรรมใหม่ได้โดยไม่แก้ลำดับ/การตรวจกฎ

### การรักษา transaction และ audit
- Transaction boundary และการล็อกแถว (`findByIdForUpdate`, pessimistic lock) อยู่ที่ `StockService` ไม่ใช่ใน template จึงทำให้ขั้น 2–6 อยู่ใน transaction เดียวกัน ถ้าขั้นใดโยน exception ทั้งยอดและประวัติ rollback
- Active guard อยู่ใน template หลัง lock แถว ดังนั้น toggle active พร้อมกับ stock-in จะไม่ลอดกฎ (ทั้ง stock-in และ adjustment ใช้ทางเดียวกัน)
- Audit: ทุกธุรกรรมเก็บ actor, reason, `quantity_delta`, `balance_after`, เวลา; ประวัติอ่านได้แม้ item เป็น inactive เพราะการอ่านไม่ผ่าน template
- การเปลี่ยน target/active ผ่าน `StockService.updateItem/setActive` **ไม่สร้าง** `StockTransaction` และไม่แตะ quantity/lowStockThreshold (ตามขอบเขตงาน)

## 2. SOLID (Auth/Stock)

| หลักการ | หลักฐานในโค้ด |
| --- | --- |
| SRP | `StockService` จัดการ transaction/lock, template จัดการกฎธุรกรรมสต็อก, `AuthService` จัดการบัญชี/โปรไฟล์, controller แปลง HTTP เท่านั้น |
| OCP | เพิ่ม processor ใหม่ด้วย subclass; `final process` กันการแก้ลำดับ |
| LSP | `StockInProcessor`/`StockAdjustmentProcessor` ใช้แทน `StockTransactionProcessor` ได้ ไม่ผ่อนกฎของฐาน (inactive/nonnegative) |
| ISP | controller พึ่ง `UserAdministrationService` (create/list/updateProfile) ไม่พึ่ง `AuthService` ทั้งก้อน |
| DIP | `StockService` พึ่ง `StockTransactionProcessor` ผ่าน qualifier IN/ADJUSTMENT |

ข้อจำกัดตรง ๆ: `AuthService` ยังรวมทั้ง login และ user administration; แยกเป็นสองคลาสเป็น refactor ที่ **planned** ยังไม่ทำ

## 3. โครงสไลด์ (2 สไลด์)
1. **Template Method ใน Stock** — ตารางขั้นตอนข้างบน, diagram [class-stock-template](../diagrams/class-stock-template.puml), จุดที่ override
2. **Transaction/Audit และ Inactive** — lock → process → audit; ตัวอย่าง 409 เมื่อ inactive; ยืนยันโดย `StockProfileFinalIntegrationTest` และ `StockTransactionTemplateTest`
