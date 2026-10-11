import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { dashboardApi, getErrorMessage } from './api'
import type { DashboardSummary } from './api'
import './dashboard.css'

const money = (value: number) => value.toLocaleString('th-TH', { style: 'currency', currency: 'THB' })
const number = (value: number) => value.toLocaleString('th-TH', { maximumFractionDigits: 3 })
const dateLabel = (date: string) => new Date(`${date}T00:00:00+07:00`).toLocaleDateString('th-TH', { timeZone: 'Asia/Bangkok', day: 'numeric', month: 'short', year: 'numeric' })
const todayInBangkok = () => new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Bangkok', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date())
function daysBefore(date: string, days: number) {
  const day = new Date(`${date}T00:00:00Z`)
  day.setUTCDate(day.getUTCDate() - days)
  return day.toISOString().slice(0, 10)
}

function Metric({ title, value, detail, warning = false }: { title: string; value: string | number; detail: string; warning?: boolean }) {
  return <div className={warning ? 'metric-warning' : undefined}><span>{title}</span><strong>{typeof value === 'number' ? number(value) : value}</strong><small>{detail}</small></div>
}

export default function DashboardPage() {
  const [today] = useState(todayInBangkok)
  const [startDate, setStartDate] = useState(() => daysBefore(today, 6))
  const [endDate, setEndDate] = useState(today)
  const [range, setRange] = useState({ startDate, endDate, revision: 0 })
  const [result, setResult] = useState<{ range: typeof range; data: DashboardSummary | null; error: string } | null>(null)
  const loading = result?.range !== range
  const data = loading ? null : result?.data
  const error = loading ? '' : result?.error
  const [rangeError, setRangeError] = useState('')

  useEffect(() => {
    let active = true
    dashboardApi.summary(range.startDate, range.endDate)
      .then((summary) => { if (active) setResult({ range, data: summary, error: '' }) })
      .catch((requestError: unknown) => { if (active) setResult({ range, data: null, error: getErrorMessage(requestError) }) })
    return () => { active = false }
  }, [range])

  function applyRange(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const days = (Date.parse(endDate) - Date.parse(startDate)) / 86_400_000
    if (!startDate || !endDate || !Number.isFinite(days) || days < 0 || days >= 366 || endDate > today) {
      setRangeError('เลือกช่วงวันที่ตามลำดับ ไม่เกิน 366 วัน และไม่เกินวันนี้')
      return
    }
    setRangeError('')
    setRange((previous) => ({ startDate, endDate, revision: previous.revision + 1 }))
  }

  function quickRange(days: number) {
    const start = daysBefore(today, days - 1)
    setStartDate(start); setEndDate(today); setRangeError('')
    setRange((previous) => ({ startDate: start, endDate: today, revision: previous.revision + 1 }))
  }

  const peak = data ? Math.max(1, ...data.sales.daily.map((day) => day.revenue)) : 1
  return <section className="admin-page dashboard-page">
    <div className="admin-page-heading"><div><p className="admin-eyebrow">ภาพรวมสำหรับผู้จัดการและหัวหน้างาน</p><h1>ภาพรวมร้าน</h1><p className="admin-subtitle">ติดตามรายได้ ลูกค้า งานบริการ และสต็อกของร้าน</p></div>
      <button className="admin-secondary-button" disabled={loading} onClick={() => setRange((previous) => ({ ...previous, revision: previous.revision + 1 }))}>รีเฟรชข้อมูล</button>
    </div>
    <form className="dashboard-filters admin-form" onSubmit={applyRange}>
      <label>ตั้งแต่วันที่<input type="date" required max={today} value={startDate} onChange={(event) => setStartDate(event.target.value)} /></label>
      <label>ถึงวันที่<input type="date" required max={today} value={endDate} onChange={(event) => setEndDate(event.target.value)} /></label>
      <button className="admin-primary-button" type="submit">ดูสรุป</button>
      <div className="dashboard-presets" aria-label="ช่วงวันที่ลัด">{[[1, 'วันนี้'], [7, '7 วัน'], [30, '30 วัน']].map(([days, label]) => <button key={days} className="admin-secondary-button" type="button" onClick={() => quickRange(Number(days))}>{label}</button>)}</div>
    </form>
    {rangeError && <p className="admin-error" role="alert">{rangeError}</p>}
    {loading && <p className="admin-empty" role="status">กำลังโหลดภาพรวมร้าน...</p>}
    {error && <div className="admin-error" role="alert">{error} <button className="admin-secondary-button" onClick={() => setRange((previous) => ({ ...previous, revision: previous.revision + 1 }))}>ลองอีกครั้ง</button></div>}
    {data && <>
      <h2 className="dashboard-heading">ยอดตามช่วงวันที่ {dateLabel(data.startDate)} – {dateLabel(data.endDate)}</h2>
      <p className="admin-subtitle dashboard-note">เวลาไทย • รายได้รวมยอดที่ชำระแล้วตามวันรับชำระ รวมบิลที่ยังรอปิดรอบ • รอบและลูกค้านับตามวันเปิดโต๊ะ</p>
      <div className="admin-metrics dashboard-metrics">
        <Metric title="รายได้ที่รับชำระแล้ว" value={money(data.sales.revenue)} detail="ยอดสุทธิจากบิล PAID" />
        <Metric title="บิลที่ชำระแล้ว" value={data.sales.paidBills} detail="1 บิลต่อรอบการใช้โต๊ะ" />
        <Metric title="รายได้เฉลี่ยต่อบิล" value={money(data.sales.averageBill)} detail="รายได้ ÷ จำนวนบิลที่ชำระ" />
        <Metric title="ลูกค้าที่เปิดรอบ" value={data.visits.adults + data.visits.children} detail={`ผู้ใหญ่ ${number(data.visits.adults)} • เด็ก ${number(data.visits.children)}`} />
        <Metric title="รอบที่เปิด" value={data.visits.openedSessions} detail="รวมรอบที่ปิดไปแล้ว" />
        <Metric title="ออเดอร์ที่สั่ง" value={data.visits.orders} detail="นับตามวันสั่งอาหาร" />
      </div>
      {data.sales.paidBills === 0 && <p className="admin-notice">ยังไม่มีบิลที่ชำระแล้วในช่วงวันที่นี้</p>}
      <div className="dashboard-columns">
        <section className="admin-section"><div className="admin-section-heading"><h2>รายได้รายวัน</h2><span>วันรับชำระล่าสุดก่อน</span></div>
          <div className="admin-table-wrap dashboard-scroll"><table className="admin-table"><thead><tr><th>วันที่</th><th>บิล</th><th>รายได้</th></tr></thead><tbody>
            {data.sales.daily.slice().reverse().map((day) => <tr key={day.date}><td>{dateLabel(day.date)}</td><td>{number(day.bills)}</td><td><strong>{money(day.revenue)}</strong><div className="dashboard-bar" aria-hidden="true"><span style={{ width: `${day.revenue / peak * 100}%` }} /></div></td></tr>)}
          </tbody></table></div>
        </section>
        <section className="admin-section"><div className="admin-section-heading"><h2>ช่องทางรับชำระ</h2></div><div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>ช่องทาง</th><th>บิล</th><th>รายได้</th></tr></thead><tbody>
          {data.sales.byMethod.map((method) => <tr key={method.method}><td>{{ CASH: 'เงินสด', QR: 'QR', CARD: 'บัตร' }[method.method]}</td><td>{number(method.bills)}</td><td><strong>{money(method.revenue)}</strong></td></tr>)}
        </tbody></table></div><p className="dashboard-help">ยอดจากแพ็กเกจและนโยบายคำนวณบิลเดิม ระบบบุฟเฟต์ไม่มีราคาขายแยกต่อเมนู</p></section>
      </div>
      <h2 className="dashboard-heading">สถานะร้านปัจจุบัน</h2><p className="admin-subtitle dashboard-note">อัปเดต {new Date(data.generatedAt).toLocaleString('th-TH', { timeZone: 'Asia/Bangkok' })} • ส่วนนี้แสดงสถานะล่าสุดของร้านทุกช่วงวันที่</p>
      <div className="admin-metrics dashboard-metrics">
        <Metric title="โต๊ะว่าง" value={data.operations.availableTables} detail={`โต๊ะใช้งานทั้งหมด ${number(data.operations.availableTables + data.operations.occupiedTables)}`} />
        <Metric title="โต๊ะกำลังใช้งาน" value={data.operations.occupiedTables} detail={`${number(data.operations.activeSessions)} รอบ • ${number(data.operations.activeGuests)} คน`} />
        <Metric title="เรียกบิล รอชำระ" value={data.operations.unpaidBillRequests} detail="ยังไม่มีรายการชำระสำเร็จ" warning={data.operations.unpaidBillRequests > 0} />
        <Metric title="ชำระแล้ว รอปิดรอบ" value={data.operations.paidAwaitingClose} detail="พนักงานบริการต้องปิดรอบเพื่อคืนโต๊ะ" />
        <Metric title="คิวครัว" value={data.operations.receivedOrders + data.operations.preparingOrders} detail={`รับแล้ว ${number(data.operations.receivedOrders)} • กำลังทำ ${number(data.operations.preparingOrders)}`} />
        <Metric title="พร้อมเสิร์ฟ" value={data.operations.readyOrders} detail="เฉพาะรอบที่กำลังใช้งาน" />
      </div>
      <div className="dashboard-columns">
        <section className="admin-section"><div className="admin-section-heading"><h2>เมนูยอดนิยมในช่วงวันที่</h2><span>สูงสุด 5 รายการ</span></div><div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>เมนู</th><th>จำนวนที่สั่ง (เสิร์ฟ)</th></tr></thead><tbody>
          {data.popularItems.map((item) => <tr key={`${item.menuItemId}:${item.name}`}><td><strong>{item.name}</strong></td><td>{number(item.servings)}</td></tr>)}
          {data.popularItems.length === 0 && <tr><td colSpan={2} className="admin-empty">ยังไม่มีรายการสั่งอาหารในช่วงวันที่นี้</td></tr>}
        </tbody></table></div><p className="dashboard-help">ใช้ชื่อเมนูที่บันทึกขณะสั่ง รวมทุกสถานะออเดอร์ เมนูที่เปลี่ยนชื่อจะแยกตามชื่อในประวัติ</p></section>
        <section className="admin-section"><div className="admin-section-heading"><h2>สต็อกที่ต้องดูแล</h2><Link to="/admin/stock">ดูสต็อก</Link></div>
          <p className="dashboard-help">ใช้งาน {number(data.stock.activeItems)} รายการ • หมด {number(data.stock.outOfStockItems)} • ต่ำกว่าหรือเท่าจุดเตือน {number(data.stock.lowStockItems)} • ไม่ถึงเป้าหมาย {number(data.stock.belowTargetItems)}</p>
          <div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>วัตถุดิบ</th><th>คงเหลือ / จุดเตือน</th><th>ขาดจากเป้าหมาย</th></tr></thead><tbody>
            {data.stock.lowItems.map((item) => <tr key={item.id}><td><strong>{item.name}</strong><span className="table-secondary">{item.unit}</span></td><td className="stock-low">{number(item.quantity)} / {number(item.threshold)}</td><td>{number(item.shortfall)}</td></tr>)}
            {data.stock.lowItems.length === 0 && <tr><td colSpan={3} className="admin-empty">ไม่มีสต็อกที่ถึงจุดเตือน</td></tr>}
          </tbody></table></div><p className="dashboard-help">แสดงสูงสุด 10 รายการที่ถึงจุดเตือน เรียงคงเหลือน้อยก่อน เฉพาะรายการที่เปิดใช้งาน</p>
        </section>
      </div>
    </>}
  </section>
}
