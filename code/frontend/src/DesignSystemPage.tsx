import { Button, Card, EmptyState, ErrorAlert, PageHeader, StatusBadge, TextField } from './components/common'
import './design-system.css'

export default function DesignSystemPage() {
  return <main className="design-page"><PageHeader eyebrow="Shared Frontend Baseline" title="Buffet Restaurant Management" description="รูปแบบกลางสำหรับ Customer, Staff, Kitchen และ Admin" action={<Button onClick={() => location.assign('/customer/sessions/1')}>ดูหน้าลูกค้า</Button>} />
    <section className="design-grid"><Card><h2>Customer Mobile</h2><p>เมนูแบบ mobile-first ปุ่มหลักสูง 48px และตะกร้าชัดเจน</p><StatusBadge status="RECEIVED" /></Card><Card><h2>Staff POS</h2><p>Card และ action หลักสำหรับเปิดโต๊ะหรือดู Session</p><Button>เปิดโต๊ะ</Button></Card><Card><h2>Kitchen KDS</h2><p className="kitchen-sample">โต๊ะ T01 · 4 รายการ</p><StatusBadge status="PREPARING" /></Card><Card><h2>Admin / Stock</h2><TextField label="ค้นหาข้อมูล" placeholder="ชื่อหรือรหัส" /><Button variant="secondary">ค้นหา</Button></Card></section>
    <section className="design-states"><ErrorAlert message="โหลดข้อมูลไม่สำเร็จ กรุณาลองใหม่" /><EmptyState title="ยังไม่มีข้อมูล" description="เพิ่มรายการแรกด้วยปุ่มหลักของหน้านี้" /></section>
  </main>
}
