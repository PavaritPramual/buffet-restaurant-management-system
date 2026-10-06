import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Button, Card, ConfirmDialog, EmptyState, ErrorAlert, LoadingState, PageHeader, RefreshIcon, StatusBadge } from '../../components/common'
import { getApiError, getCustomerPackage, getCustomerContext, getMenu, getOrders, placeOrder, redeemQr } from './api'
import type { MenuItem, Order, SessionContext } from './api'
import type { OrderStatus } from '../../contracts/shared'
import type { StatusBadgeTone } from '../../components/common'
import './ordering.css'

const orderStatusPresentation: Record<OrderStatus, { label: string; tone: StatusBadgeTone }> = {
  RECEIVED: { label: 'รับออเดอร์แล้ว', tone: 'info' },
  PREPARING: { label: 'กำลังเตรียม', tone: 'warning' },
  READY: { label: 'พร้อมเสิร์ฟ', tone: 'success' },
  SERVED: { label: 'เสิร์ฟแล้ว', tone: 'success' },
}

function tokenFromHash(hash: string) { return new URLSearchParams(hash.slice(1)).get('token') ?? '' }

export default function CustomerOrderingPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const [scan, setScan] = useState<{ id: number; token: string } | null>(() =>
    location.hash ? { id: 0, token: tokenFromHash(location.hash) } : null)
  const scanEpoch = useRef(0)
  const processedHash = useRef(location.hash)
  const acceptedContext = useRef<SessionContext | null>(null)
  const submittingEpoch = useRef<number | null>(null)
  const refreshingEpoch = useRef<number | null>(null)
  const ordersRevision = useRef(0)
  const historyRef = useRef<HTMLElement | null>(null)
  const [reloadAttempt, setReloadAttempt] = useState(0)
  const [session, setSession] = useState<SessionContext | null>(null)
  const [packageName, setPackageName] = useState('')
  const [menu, setMenu] = useState<MenuItem[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [cart, setCart] = useState<Record<number, number>>({})
  const [category, setCategory] = useState<number | 'all'>('all')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [refreshing, setRefreshing] = useState(false)
  const [confirming, setConfirming] = useState(false)
  const [error, setError] = useState('')
  const [orderError, setOrderError] = useState('')
  const [historyError, setHistoryError] = useState('')
  const [notice, setNotice] = useState('')

  useLayoutEffect(() => {
    if (!location.hash) { processedHash.current = ''; return }
    if (processedHash.current !== location.hash) {
      processedHash.current = location.hash
      setScan({ id: ++scanEpoch.current, token: tokenFromHash(location.hash) })
      acceptedContext.current = null; submittingEpoch.current = null; refreshingEpoch.current = null
      setReloadAttempt(0); setRefreshing(false)
      setSession(null); setPackageName(''); setMenu([]); setOrders([]); setCart({})
      setCategory('all'); setError(''); setOrderError(''); setHistoryError(''); setNotice(''); setConfirming(false)
      setSubmitting(false); setLoading(true)
    }
  }, [location.hash])

  useEffect(() => {
    if (location.hash) navigate('/customer/qr', { replace: true })
  }, [location.hash, navigate])

  useEffect(() => {
    let active = true
    const epoch = scanEpoch.current
    const isCurrent = () => active && scanEpoch.current === epoch
    // A successful one-use QR exchange must not be repeated if loading its menu fails.
    const lookup = reloadAttempt > 0 && acceptedContext.current
      ? getCustomerContext()
      : scan ? redeemQr(scan.token) : getCustomerContext({ retryFailedQr: reloadAttempt > 0 })
    lookup
      .then(async (context) => {
        if (!isCurrent()) return
        acceptedContext.current = context
        const [nextMenu, nextOrders, buffetPackage] = await Promise.all([
          getMenu(context.sessionId), getOrders(context.sessionId), getCustomerPackage(context.sessionId),
        ])
        if (isCurrent()) {
          setSession(context); setPackageName(buffetPackage.name); setMenu(nextMenu); setOrders(nextOrders); setError('')
        }
      })
      .catch((cause) => { if (isCurrent()) setError(getApiError(cause)) })
      .finally(() => { if (isCurrent()) setLoading(false) })
    return () => { active = false }
  }, [scan, reloadAttempt])

  const visibleMenu = category === 'all' ? menu : menu.filter((item) => item.categoryId === category)
  const categories = useMemo(() => Array.from(new Map(menu.map((item) => [item.categoryId, { id: item.categoryId, name: item.categoryName }])).values()), [menu])
  const cartItems = useMemo(() => menu.filter((item) => cart[item.id]).map((item) => ({ ...item, quantity: cart[item.id] })), [menu, cart])
  const count = cartItems.reduce((sum, item) => sum + item.quantity, 0)
  const changeQuantity = (id: number, amount: number) => setCart((current) => {
    const next = { ...current }; const quantity = Math.max(0, (next[id] ?? 0) + amount)
    if (quantity) next[id] = quantity; else delete next[id]
    return next
  })

  async function submit() {
    const epoch = scanEpoch.current
    if (!session || submittingEpoch.current === epoch || count === 0) return
    submittingEpoch.current = epoch
    setSubmitting(true); setOrderError(''); setNotice('')
    try {
      const created = await placeOrder(session.sessionId, cartItems.map((item) => ({ menuItemId: item.id, quantity: item.quantity })))
      if (scanEpoch.current === epoch) {
        // Earlier order-history snapshots must not erase this acknowledged order.
        ++ordersRevision.current
        // A refresh may already have observed this order with a newer kitchen status.
        setOrders((current) => current.some((order) => order.orderId === created.orderId) ? current : [created, ...current])
        setCart({}); setConfirming(false); setNotice(`ส่งคำสั่งซื้อ #${created.orderId} แล้ว`)
        historyRef.current?.focus()
      }
    } catch (cause) {
      if (scanEpoch.current === epoch) { setOrderError(getApiError(cause)); setConfirming(false) }
    } finally { if (scanEpoch.current === epoch) { submittingEpoch.current = null; setSubmitting(false) } }
  }

  async function refreshOrders() {
    if (!session) return
    const epoch = scanEpoch.current
    if (refreshingEpoch.current === epoch) return
    const revision = ordersRevision.current
    refreshingEpoch.current = epoch; setRefreshing(true)
    try {
      const refreshed = await getOrders(session.sessionId)
      if (scanEpoch.current === epoch && ordersRevision.current === revision) { setOrders(refreshed); setHistoryError('') }
    } catch (cause) { if (scanEpoch.current === epoch && ordersRevision.current === revision) setHistoryError(getApiError(cause)) }
    finally { if (scanEpoch.current === epoch) { refreshingEpoch.current = null; setRefreshing(false) } }
  }

  function retryLoad() { setError(''); setLoading(true); setReloadAttempt((attempt) => attempt + 1) }

  return <main className="ordering-page customer-page">
    <PageHeader eyebrow="สั่งอาหารผ่าน QR" title="เลือกเมนูที่ชอบ" description={session ? `โต๊ะ ${session.tableNumber} · ${packageName}` : 'ตรวจสอบ QR ของรอบการรับประทาน'} />
    {error && <ErrorAlert message={error} />}
    {orderError && <ErrorAlert message={orderError} />}
    {historyError && <ErrorAlert message={historyError} />}
    {notice && <div className="ordering-notice" role="status">{notice}</div>}
    {!loading && !session && error && <Card className="customer-retry"><p>ลองโหลดอีกครั้ง หาก QR หมดสิทธิ์ให้ขอ QR ใหม่จากพนักงานค่ะ</p><Button onClick={retryLoad}>ลองอีกครั้ง</Button></Card>}
    {loading ? <LoadingState label="กำลังตรวจสอบรอบการรับประทานและโหลดเมนู…" /> : session && <>
      <nav className="category-chips" aria-label="หมวดหมู่เมนู">
        <button className={category === 'all' ? 'active' : ''} onClick={() => setCategory('all')}>ทั้งหมด</button>
        {categories.filter((entry) => menu.some((item) => item.categoryId === entry.id)).map((entry) => <button key={entry.id} className={category === entry.id ? 'active' : ''} onClick={() => setCategory(entry.id)}>{entry.name}</button>)}
      </nav>
      {visibleMenu.length === 0 ? <EmptyState title="ยังไม่มีเมนูในหมวดนี้" description="ลองเลือกหมวดอื่นหรือสอบถามพนักงานได้ค่ะ" /> : <section className="menu-grid" aria-label="เมนูอาหาร">
        {visibleMenu.map((item) => <Card key={item.id} className="menu-card">
          {item.imageUrl ? <img src={item.imageUrl} alt={item.name} loading="lazy" /> : <div className="menu-image-placeholder" aria-hidden="true">🍽️</div>}
          <div className="menu-card-body"><small>{item.categoryName}</small><h2>{item.name}</h2>{item.description && <p className="menu-description">{item.description}</p>}
            <div className="quantity-stepper"><Button variant="secondary" aria-label={`ลด ${item.name}`} onClick={() => changeQuantity(item.id, -1)} disabled={submitting || !cart[item.id]}>−</Button><span aria-live="polite">{cart[item.id] ?? 0}</span><Button aria-label={`เพิ่ม ${item.name}`} disabled={submitting} onClick={() => changeQuantity(item.id, 1)}>+</Button></div>
          </div>
        </Card>)}
      </section>}
      <Card className="cart-card"><div><h2>ตะกร้าอาหาร</h2><p>{count ? `${count} รายการ · ${cartItems.map((item) => `${item.name} × ${item.quantity}`).join(', ')}` : 'ยังไม่ได้เลือกเมนู'}</p></div><Button size="lg" disabled={!count || submitting} onClick={() => setConfirming(true)}>ยืนยันการสั่ง</Button></Card>
      <section className="order-history" ref={historyRef} tabIndex={-1} aria-label="สถานะคำสั่งซื้อ"><div className="section-title"><div><h2>สถานะคำสั่งซื้อ</h2><p>ติดตามรายการที่ส่งเข้าครัวแล้ว</p></div><Button variant="secondary" disabled={refreshing} aria-busy={refreshing} className="ui-icon-button" aria-label="อัปเดตสถานะคำสั่งซื้อ" title="อัปเดตสถานะคำสั่งซื้อ" onClick={refreshOrders}><RefreshIcon /></Button></div>
        {orders.length === 0 ? <EmptyState title="ยังไม่มีคำสั่งซื้อ" /> : <div className="order-list">{orders.map((order) => <Card key={order.orderId} className="order-card"><div><strong>คำสั่งซื้อ #{order.orderId}</strong><p>{order.items.map((item) => `${item.name} × ${item.quantity}`).join(', ')}</p></div><StatusBadge tone={orderStatusPresentation[order.status].tone}>{orderStatusPresentation[order.status].label}</StatusBadge></Card>)}</div>}
      </section>
    </>}
    <ConfirmDialog open={confirming} title="ยืนยันการสั่งอาหาร" description={`ส่ง ${count} รายการเข้าครัว เมื่อยืนยันแล้วจะติดตามสถานะได้ด้านล่าง`} busy={submitting} onCancel={() => setConfirming(false)} onConfirm={() => void submit()} />
  </main>
}
