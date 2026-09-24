import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Button, Card, ConfirmDialog, EmptyState, ErrorAlert, LoadingState, PageHeader, StatusBadge } from '../../components/common'
import { getApiError, getCategories, getMenu, getOrders, placeOrder } from './api'
import type { Category, MenuItem, Order } from './api'
import type { OrderStatus } from '../../contracts/shared'
import type { StatusBadgeTone } from '../../components/common'
import './ordering.css'

const orderStatusPresentation: Record<OrderStatus, { label: string; tone: StatusBadgeTone }> = {
  RECEIVED: { label: 'รับออเดอร์แล้ว', tone: 'info' },
  PREPARING: { label: 'กำลังเตรียม', tone: 'warning' },
  READY: { label: 'พร้อมเสิร์ฟ', tone: 'success' },
  SERVED: { label: 'เสิร์ฟแล้ว', tone: 'success' },
}

export default function CustomerOrderingPage() {
  const { sessionId: rawId } = useParams()
  const sessionId = Number(rawId)
  const invalidSession = !Number.isSafeInteger(sessionId) || sessionId < 1
  const [menu, setMenu] = useState<MenuItem[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [cart, setCart] = useState<Record<number, number>>({})
  const [category, setCategory] = useState<number | 'all'>('all')
  const [loading, setLoading] = useState(!invalidSession)
  const [submitting, setSubmitting] = useState(false)
  const [confirming, setConfirming] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    if (invalidSession) return
    let active = true
    Promise.all([getMenu(sessionId), getCategories(), getOrders(sessionId)])
      .then(([nextMenu, nextCategories, nextOrders]) => { if (active) { setMenu(nextMenu); setCategories(nextCategories); setOrders(nextOrders); setError('') } })
      .catch((cause) => { if (active) setError(getApiError(cause)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [sessionId, invalidSession])

  const visibleMenu = category === 'all' ? menu : menu.filter((item) => item.categoryId === category)
  const cartItems = useMemo(() => menu.filter((item) => cart[item.id]).map((item) => ({ ...item, quantity: cart[item.id] })), [menu, cart])
  const count = cartItems.reduce((sum, item) => sum + item.quantity, 0)
  const changeQuantity = (id: number, amount: number) => setCart((current) => {
    const next = { ...current }; const quantity = Math.max(0, (next[id] ?? 0) + amount)
    if (quantity) next[id] = quantity; else delete next[id]
    return next
  })

  async function submit() {
    if (submitting || count === 0) return
    setSubmitting(true); setError(''); setNotice('')
    try {
      const created = await placeOrder(sessionId, cartItems.map((item) => ({ menuItemId: item.id, quantity: item.quantity })))
      setOrders((current) => [created, ...current]); setCart({}); setConfirming(false); setNotice(`ส่งคำสั่งซื้อ #${created.orderId} แล้ว`)
    } catch (cause) { setError(getApiError(cause)); setConfirming(false) } finally { setSubmitting(false) }
  }

  async function refreshOrders() {
    try { setOrders(await getOrders(sessionId)); setError('') } catch (cause) { setError(getApiError(cause)) }
  }

  return <main className="ordering-page customer-page">
    <PageHeader eyebrow="สั่งอาหารผ่าน QR" title="เลือกเมนูที่ชอบ" description={`โต๊ะจากรอบการรับประทาน #${rawId ?? '-'}`} />
    {(invalidSession || error) && <ErrorAlert message={invalidSession ? 'รหัสรอบการรับประทานไม่ถูกต้อง กรุณาสแกน QR ใหม่' : error} />}
    {notice && <div className="ordering-notice" role="status">{notice}</div>}
    {!invalidSession && (loading ? <LoadingState label="กำลังตรวจสอบรอบการรับประทานและโหลดเมนู…" /> : <>
      <nav className="category-chips" aria-label="หมวดหมู่เมนู">
        <button className={category === 'all' ? 'active' : ''} onClick={() => setCategory('all')}>ทั้งหมด</button>
        {categories.filter((entry) => menu.some((item) => item.categoryId === entry.id)).map((entry) => <button key={entry.id} className={category === entry.id ? 'active' : ''} onClick={() => setCategory(entry.id)}>{entry.name}</button>)}
      </nav>
      {visibleMenu.length === 0 ? <EmptyState title="ยังไม่มีเมนูในหมวดนี้" description="ลองเลือกหมวดอื่นหรือสอบถามพนักงานได้ค่ะ" /> : <section className="menu-grid" aria-label="เมนูอาหาร">
        {visibleMenu.map((item) => <Card key={item.id} className="menu-card">
          {item.imageUrl ? <img src={item.imageUrl} alt={item.name} loading="lazy" /> : <div className="menu-image-placeholder" aria-hidden="true">🍽️</div>}
          <div className="menu-card-body"><small>{item.categoryName}</small><h2>{item.name}</h2>{item.description && <p className="menu-description">{item.description}</p>}
            <div className="quantity-stepper"><Button variant="secondary" aria-label={`ลด ${item.name}`} onClick={() => changeQuantity(item.id, -1)} disabled={!cart[item.id]}>−</Button><span aria-live="polite">{cart[item.id] ?? 0}</span><Button aria-label={`เพิ่ม ${item.name}`} onClick={() => changeQuantity(item.id, 1)}>+</Button></div>
          </div>
        </Card>)}
      </section>}
      <Card className="cart-card"><div><h2>ตะกร้าอาหาร</h2><p>{count ? `${count} รายการ · ${cartItems.map((item) => `${item.name} × ${item.quantity}`).join(', ')}` : 'ยังไม่ได้เลือกเมนู'}</p></div><Button size="lg" disabled={!count} onClick={() => setConfirming(true)}>ยืนยันการสั่ง</Button></Card>
      <section className="order-history"><div className="section-title"><div><h2>สถานะคำสั่งซื้อ</h2><p>ติดตามรายการที่ส่งเข้าครัวแล้ว</p></div><Button variant="secondary" onClick={refreshOrders}>อัปเดต</Button></div>
        {orders.length === 0 ? <EmptyState title="ยังไม่มีคำสั่งซื้อ" /> : <div className="order-list">{orders.map((order) => <Card key={order.orderId} className="order-card"><div><strong>คำสั่งซื้อ #{order.orderId}</strong><p>{order.items.map((item) => `${item.name} × ${item.quantity}`).join(', ')}</p></div><StatusBadge tone={orderStatusPresentation[order.status].tone}>{orderStatusPresentation[order.status].label}</StatusBadge></Card>)}</div>}
      </section>
    </>)}
    <ConfirmDialog open={confirming} title="ยืนยันการสั่งอาหาร" description={`ส่ง ${count} รายการเข้าครัว เมื่อยืนยันแล้วจะติดตามสถานะได้ด้านล่าง`} busy={submitting} onCancel={() => setConfirming(false)} onConfirm={() => void submit()} />
  </main>
}
