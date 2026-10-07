# Use cases และ Use Case Descriptions

อัปเดตจากโค้ด develop `472fba4` วันที่ 7 ตุลาคม 2026 โดยรักษาต้นทาง [Notion Use cases](https://app.notion.com/p/3d3cb2e9d47a80819cf9e6b549adba17) ไว้เป็นประวัติ ภาพและคำอธิบายนี้อ้าง implementation ปัจจุบัน รอ peer review และไม่รับรอง public deployment

[ภาพรวม](../diagrams/previews/use-case.svg) · [Customer/Service/Kitchen](../diagrams/previews/use-case-service-customer.svg) · [Manager/Supervisor](../diagrams/previews/use-case-management.svg) · [source](../diagrams/use-case.puml)

## UC-01 เปิดรอบและเลือกแพ็กเกจ/น้ำซุป

- **ผู้ใช้หลัก** SERVICE_STAFF
- **ก่อนเริ่ม** login แล้ว โต๊ะ AVAILABLE มี package/soup ที่ active
- **งานหลัก** เลือกโต๊ะ ระบุผู้ใหญ่/เด็ก แพ็กเกจและน้ำซุป ยืนยันคำขอเดียว ระบบล็อกโต๊ะ ตรวจ capacity และจำนวนคนอย่างน้อยหนึ่ง เก็บราคา snapshot สร้าง ACTIVE session พร้อมตั้งโต๊ะ OCCUPIED ใน transaction เดียว
- **ทางเลือก** โต๊ะไม่ว่าง/คำขอเปิดซ้ำ คนผิดหรือเกิน capacity หรือ master inactive ถูกปฏิเสธโดยไม่สร้าง session ครึ่งทาง
- **หลังสำเร็จ** ได้รอบจริง พร้อม QR รุ่นปัจจุบัน และราคาเดิมที่ใช้คิดบิล

การ setup ที่เคยแยกเป็น UC-02 ในแบบเดิมรวมอยู่ใน UC-01 นี้แล้ว ไม่อ้างว่าระบบเปิดรอบเปล่าก่อนเลือกแพ็กเกจ

## UC-02 แลก QR และอ่านเมนู

- **ผู้ใช้หลัก** Customer ไม่ต้องมีบัญชีพนักงาน
- **ก่อนเริ่ม** มีรอบ ACTIVE และ QR token รุ่นที่ยังไม่ถูกแลก
- **งานหลัก** สแกน fragment link หน้าเว็บอ่านแล้วล้าง fragment ส่ง POST body Backend ล็อก session หมุน QR และสร้าง grant ต่อเครื่อง เก็บเฉพาะ credential hash และ expiry ออก HttpOnly cookie จากนั้นอ่านเมนูที่ available และอยู่ในแพ็กเกจของรอบ
- **ทางเลือก** QR ผิด/ใช้ซ้ำ/รอบไม่ active ค้นไม่พบ cookie หายหรือหมดอายุไม่ได้สิทธิ์ ทุกเครื่องใช้ QR รุ่นถัดไป ไม่แลก token เดิมซ้ำ
- **หลังสำเร็จ** อ่านข้อมูลรอบเดียวกันได้ ไม่มี QR token/ราคา snapshot ใน Customer DTO

## UC-03 สั่งอาหารและดูออเดอร์

- **ผู้ใช้หลัก** Customer
- **ก่อนเริ่ม** cookie grant ถูกต้องตรงรอบ ACTIVE และยังไม่ขอคิดบิล
- **งานหลัก** เลือกเมนูและจำนวน ยืนยัน Backend ล็อก session/recheck สิทธิ์ ตรวจ package/available/quantity แล้วสร้าง Order RECEIVED กับ items และ item-name/table snapshots
- **ทางเลือก** menu ไม่อยู่ใน package หรือไม่ available ถูกปฏิเสธ Order ที่เข้า lock หลัง bill request/close ไม่ถูกบันทึก
- **หลังสำเร็จ** ออเดอร์ปรากฏฝั่ง Customer และ incoming ของครัว

## UC-04 เตรียมอาหาร

- **ผู้ใช้หลัก** KITCHEN_STAFF
- **ก่อนเริ่ม** login ตาม role และมี RECEIVED/PREPARING order
- **งานหลัก** อ่าน incoming เริ่ม PREPARING แล้ว READY เมื่อเตรียมเสร็จ State Pattern ตรวจ single next transition ก่อน persist enum
- **ทางเลือก** ข้ามขั้น ย้อนกลับ role ผิด หรือไปต่อหลัง SERVED ถูกปฏิเสธ
- **หลังสำเร็จ** READY order ปรากฏในหน้าพนักงานเสิร์ฟ

## UC-05 เสิร์ฟอาหาร

- **ผู้ใช้หลัก** SERVICE_STAFF
- **ก่อนเริ่ม** login และ Order READY
- **งานหลัก** อ่าน ready board เสิร์ฟแล้วกดยืนยัน ระบบตรวจ State และ role แล้วบันทึก SERVED
- **ทางเลือก** ครัว/Manager/Supervisor ไม่ได้รับสิทธิ์เสิร์ฟ ไม่มีการข้ามจาก RECEIVED มา SERVED
- **หลังสำเร็จ** order terminal ไม่เปลี่ยนต่อได้

## UC-06 ขอคิดบิลและดูสถานะ

- **ผู้ใช้หลัก** Customer
- **ก่อนเริ่ม** cookie ตรงรอบ ACTIVE
- **งานหลัก** ยืนยันขอคิดบิล ระบบล็อก session ตั้ง billRequestedAt ครั้งแรก คำนวณยอดจาก BillingEngine หน้าเว็บอ่านสถานะ NOT_REQUESTED/REQUESTED/PAID พร้อม backend BillSummary
- **ทางเลือก** กดซ้ำคืนผลเดิม ทุกมือถือหยุดสั่งเพิ่ม แต่ยังอ่านเมนู/ออเดอร์/บิลได้
- **หลังสำเร็จ** Staff เห็น badge ขอคิดบิล Session ยัง ACTIVE

## UC-07 รับชำระ

- **ผู้ใช้หลัก** SERVICE_STAFF
- **ก่อนเริ่ม** login รอบ ACTIVE ขอคิดบิลแล้ว และยังไม่มี Payment
- **งานหลัก** Staff อ่าน bill เลือกวิธี CASH/QR/CARD แล้วรับรองชำระ Backend อ่าน opening-price snapshot/counts คำนวณยอดเอง ล็อก session และบันทึก PAID/paidAt พร้อม unique session_id
- **ทางเลือก** ไม่ขอคิดบิล จ่ายซ้ำ หรือ role ผิดถูกปฏิเสธ คำตอบ POST สูญหายให้ GET ตรวจ Payment เดิมก่อน retry
- **หลังสำเร็จ** มี Payment เดียวของรอบ ไม่ปิด session อัตโนมัติ ไม่มี Payment Gateway หรือระบบพิมพ์ใบเสร็จในขอบเขตนี้

## UC-08 ปิดรอบ

- **ผู้ใช้หลัก** SERVICE_STAFF
- **ก่อนเริ่ม** login รอบ ACTIVE และ PaymentStatusLookup ยืนยัน PAID ของ session เดียวกัน
- **งานหลัก** กดยืนยัน close ระบบล็อก session/โต๊ะ ตรวจ PAID อีกครั้ง ลบ grants ตั้ง session COMPLETED/endTime และโต๊ะ AVAILABLE ใน transaction เดียว
- **ทางเลือก** ยังไม่จ่าย ผลเป็นของอีกรอบ หรือ provider ไม่มี ใช้ปิดสำเร็จไม่ได้
- **หลังสำเร็จ** โต๊ะพร้อมรอบใหม่ customer credentials เดิมใช้งานไม่ได้

## UC-09 รับเข้าและปรับยอดสต็อก

- **ผู้ใช้หลัก** MANAGER หรือ SUPERVISOR
- **ก่อนเริ่ม** login และมี stock item
- **งานหลัก** อ่านยอด/ประวัติ รับเข้าด้วยจำนวนบวก หรือ adjustment ด้วย signed delta และเหตุผล StockService ใช้ Template Method ล็อกแถว ตรวจยอดใหม่ไม่ติดลบ บันทึกยอดและ IN/ADJUSTMENT history พร้อม actor/reason/balance_after
- **ทางเลือก** จำนวนไม่ถูกต้อง ขาดเหตุผล หรือทำให้ยอดติดลบไม่บันทึกทั้งยอดและ history
- **หลังสำเร็จ** มีประวัติตรวจย้อนหลัง ไม่ใช้จำนวนตรวจนับเป็นยอดใหม่โดยตรง และไม่ตัด stock จากสูตรอาหารอัตโนมัติ

## UC-10 จัดการข้อมูลร้าน เมนู ผู้ใช้ และรายการสต็อก

- **ผู้ใช้หลัก** MANAGER
- **ก่อนเริ่ม** login ตาม role
- **งานหลัก** CRUD โต๊ะ เมนู/categories/package membership แพ็กเกจ น้ำซุป ผู้ใช้ และเพิ่ม/แก้ stock master ผ่าน UI/API ตาม contract
- **ทางเลือก** โต๊ะที่มี session history ลบไม่ได้ โต๊ะ active ไม่แก้ข้อมูลขัดรอบ package/soup ลบเป็น inactive เพื่อคงประวัติ Stock ใหม่เริ่มศูนย์และห้ามเปลี่ยน SKU/unit หลังมี transaction ราคาใหม่ไม่เปลี่ยน snapshot รอบเดิม
- **หลังสำเร็จ** ข้อมูลร้านพร้อมใช้ โดย Supervisor ไม่ได้สิทธิ์ CRUD master data เหล่านี้

## ขอบเขต Final ที่ยังไม่อยู่ baseline นี้

Stock opening target/lifecycle และ Profile firstName/lastName/phoneNumber ยังต้องเพิ่มตาม Final plan แผน public Deployment เป็นแบบออกแบบ ไม่ใช่ผลตรวจ runtime
