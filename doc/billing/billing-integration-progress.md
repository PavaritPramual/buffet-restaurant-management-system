# สถานะ Billing integration — 28 September 2026

Branch: `teeramet_673380273-9_02`

## สิ่งที่เปลี่ยนและการเชื่อมต่อ

1. `BillingPreviewPage.tsx` รับรหัสรอบจากช่องกรอกหรือ URL ของพนักงาน แล้วเรียก API
   เมื่อเปลี่ยนรหัสจะล้างบิลเดิม ป้องกันการกดซ้ำขณะโหลด และไม่แสดง response ที่เป็นของรอบอื่น
2. `api/billing.ts` ส่งเพียง `{ "sessionId": ... }` ไปยัง POST `/api/v1/billing/preview`
   ไม่ส่งราคา จำนวนคน ส่วนลด หรือสถานะจาก browser ไปคำนวณอีกต่อไป
3. `BillingPreviewRequest` เป็น record ที่เก็บ sessionId; Controller ใช้ `@Valid`
   เพื่อเรียกกฎ `@NotNull` และ `@Positive` ก่อนเรียก service
4. `BillingController` ดูแล HTTP และ Swagger แล้วส่งรหัสให้ `BillingPreviewService`
5. `BillingPreviewService` เรียก `BillingContextProvider` ตรวจรหัสตรงกันและสถานะ ACTIVE
   แล้วส่ง context ให้ BillingEngine เดิม ไม่มีการบันทึก Payment หรือปิดรอบ
6. BillingEngine และ Strategy ไม่ได้เปลี่ยนสูตร เก็บค่าคำนวณเต็มความละเอียดและปัดยอดสุดท้าย
   ด้วย HALF_UP ตามเดิม Service เก็บ dependency ใน field ส่วนข้อมูลบิลเก็บในตัวแปรของแต่ละคำขอ
7. `DisabledBillingContextProvider` ยังเป็นค่าเริ่มต้น: ตอบ 503 จนกว่าจะมี adapter ฐานข้อมูล
   ไม่สร้างข้อมูลปลอมใน runtime เพราะ PaymentService ก็ใช้ Provider เดียวกัน

PaymentServiceImpl เดิมจะอ่าน context และคำนวณยอดใหม่ก่อนบันทึก Payment;
การดูบิลไม่ใช่ใบเสร็จ และไม่ได้ยืนยันว่ามีการจ่ายเงินแล้ว

## เปิดหน้าไหน

- `/billing/preview`: กรอกรหัสรอบ แล้วกดดูบิล
- `/staff/sessions/12/billing`: รหัสมาจาก URL แล้วกดดูบิล
- เมื่อ backend รันได้แต่ Provider ยัง disabled จะได้ 503 พร้อมข้อความระบบยังไม่พร้อม
- ยังไม่ติดตั้ง PaymentPanel ในหน้านี้จนกว่าจะเชื่อมฐานข้อมูลและสิทธิ์พนักงาน
- ใช้ common UI components กับ CSS เฉพาะหน้า เพราะ entry ปัจจุบันโหลด shared CSS
  แต่ไม่ได้โหลด Tailwind stylesheet ที่ utility classes เดิมต้องใช้

## งานที่รอเชื่อมต่อ

1. รวม DiningSession ที่ผ่าน review จาก PR16 ตามขั้นตอน Git ของทีม
2. ทำ BillingContextProvider โดยเรียก DiningSessionBillingReader.requireBySessionId
   ใช้ packagePriceAtOpen เป็น packagePrice และกำหนด discountContext ฝั่ง backend
3. ทำ PaymentStatusLookup อ่าน PaymentRepository คืน sessionId และสถานะจากฐานข้อมูล
   การปิดรอบยังเป็นอีก action ของพนักงานหลังจ่าย
4. จองเลข migration Payment กับเมธัส: PR16 ใช้ V6–V8 แล้ว ยังไม่ได้จอง V9
   ทำ FK/unique constraint และ paid_at nullable ตามชนิดเวลาที่ตกลงกัน
5. เชื่อม authentication และ role ของพนักงานก่อนเปิดใช้งานจริง
6. เชื่อม UI ชำระเงิน พร้อมอ่านสถานะเพื่อจัดการกรณีส่งคำขอแล้วไม่ทราบผล
7. ทดสอบเปิดรอบ → ดูบิล → จ่าย → ปิดรอบ ในฐานข้อมูลทดสอบแยกก่อนตรวจ runtime ร่วม

ตอนนี้มี Payment entity เดิมแต่ยังไม่มี migration ของตาราง payments ดังนั้น backend ทั้งระบบ
อาจติด JPA schema validation จนกว่างาน schema จะพร้อม เทสต์เฉพาะ web layer ไม่ได้ยืนยัน startup ทั้งระบบ

## Contract และ fixture

HTTP body เปลี่ยนจาก BillingContext เป็น BillingPreviewRequest แล้ว
แก้ shared-contracts.md, Swagger และ frontend call พร้อมเพิ่ม billing-preview-request.json
fixtures billing-context เดิมยังเป็นข้อมูลคำนวณภายใน ไม่ใช่ HTTP request อีกต่อไป
ต้องให้ปวริศช์และศรัณย์รีวิวการเปลี่ยน contract ก่อน merge; ยังไม่ได้ส่งข้อความแทนผู้ใช้

## หลักฐานทดสอบ

- Frontend: `npm test -- --environment jsdom` ผ่าน 13 tests (6 tests ใหม่ของ Billing)
- Frontend build และ lint ผ่านหลังปรับ CSS; tests 13/13 ผ่านหลังแก้ครั้งสุดท้าย
- Backend ผ่าน 13/13 tests, Failures 0, Errors 0, BUILD SUCCESS (11 web + 2 calculation)
- Backend เลือก BillingControllerTest และ StandardBillCalculationTest ใช้ service/engine จริง
  แต่ mock provider ไม่เชื่อมฐานข้อมูล ไม่แตะ Supabase
- ตรวจ browser แทรกราคา, ส่วนลดจาก backend, ขอบเขต HALF_UP, รหัสผิด,
  รอบไม่ ACTIVE, context คนละรอบ, 404 และ 503
- sandbox/JDK นี้ไม่อนุญาต Mockito self-attach จึงใช้ javaagent ตอนเริ่ม test process
  ไม่ได้เปลี่ยน pom ของโปรเจกต์เพื่อแก้ข้อจำกัดเฉพาะเครื่อง

```sh
mvn -o -Dtest=BillingControllerTest,StandardBillCalculationTest \
  -DargLine=-javaagent:/home/koji/.m2/repository/org/mockito/mockito-core/5.17.0/mockito-core-5.17.0.jar test
```

ยังไม่ได้ merge PR16, สร้าง migration, เปิดระบบชำระจริง, commit หรือ push
