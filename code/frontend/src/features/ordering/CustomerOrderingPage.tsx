import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getApiError, getCategories, getMenu, getOrders, placeOrder } from './api'
import type { Category, MenuItem, Order } from './api'
import './ordering.css'

export default function CustomerOrderingPage() {
  const { sessionId: rawId } = useParams()
  const sessionId = Number(rawId)
  const invalidSession = !Number.isSafeInteger(sessionId) || sessionId < 1
  const [menu, setMenu] = useState<MenuItem[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [cart, setCart] = useState<Record<number, number>>({})
  const [category, setCategory] = useState<number | 'all'>('all')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    if (invalidSession) return
    let active = true
    Promise.all([getMenu(sessionId), getCategories(), getOrders(sessionId)])
      .then(([nextMenu, nextCategories, nextOrders]) => {
        if (!active) return
        setMenu(nextMenu)
        setCategories(nextCategories)
        setOrders(nextOrders)
        setError('')
      })
      .catch((cause) => { if (active) setError(getApiError(cause)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [sessionId, invalidSession])

  const visibleMenu = category === 'all' ? menu : menu.filter((item) => item.categoryId === category)
  const count = Object.values(cart).reduce((sum, quantity) => sum + quantity, 0)

  function changeQuantity(id: number, amount: number) {
    setCart((current) => {
      const next = { ...current }
      const quantity = Math.max(0, (next[id] ?? 0) + amount)
      if (quantity) next[id] = quantity
      else delete next[id]
      return next
    })
  }

  async function submit() {
    if (submitting || count === 0) return
    setSubmitting(true)
    setError('')
    setNotice('')
    try {
      const items = Object.entries(cart).map(([id, quantity]) => ({ menuItemId: Number(id), quantity }))
      const created = await placeOrder(sessionId, items)
      setOrders((current) => [...current, created])
      setCart({})
      setNotice(`ส่งคำสั่งซื้อ #${created.orderId} แล้ว`)
    } catch (cause) {
      setError(getApiError(cause))
    } finally {
      setSubmitting(false)
    }
  }

  async function refreshOrders() {
    try {
      setOrders(await getOrders(sessionId))
      setError('')
    } catch (cause) { setError(getApiError(cause)) }
  }

  return (
    <main className="ordering-page">
      <header className="ordering-header">
        <span className="ordering-eyebrow">BUFFET RESTAURANT</span>
        <h1>เลือกเมนูที่ชอบ</h1>
        <p>รอบการรับประทาน #{rawId}</p>
      </header>
      {(invalidSession || error) && <div role="alert" className="ordering-alert">{invalidSession ? 'รหัสรอบการรับประทานไม่ถูกต้อง' : error}</div>}
      {notice && <div role="status" className="ordering-notice">{notice}</div>}
      {invalidSession ? null : loading ? <p className="ordering-state">กำลังโหลดเมนู...</p> : <>
        <nav className="ordering-categories" aria-label="หมวดหมู่เมนู">
          <button className={category === 'all' ? 'active' : ''} onClick={() => setCategory('all')}>ทั้งหมด</button>
          {categories.map((entry) => <button key={entry.id} className={category === entry.id ? 'active' : ''}
            onClick={() => setCategory(entry.id)}>{entry.name}</button>)}
        </nav>
        {visibleMenu.length === 0 ? <p className="ordering-state">ยังไม่มีเมนูในหมวดนี้</p> :
          <section className="ordering-grid" aria-label="เมนูอาหาร">
            {visibleMenu.map((item) => <article key={item.id} className="ordering-card">
              <div>
                {item.imageUrl && <img className="ordering-item-image" src={item.imageUrl} alt={item.name} loading="lazy" />}
                <span className="ordering-item-label">เมนู #{item.id}</span><h2>{item.name}</h2>
              </div>
              <div className="ordering-stepper">
                <button aria-label={`ลด ${item.name}`} onClick={() => changeQuantity(item.id, -1)} disabled={!cart[item.id]}>−</button>
                <span aria-live="polite">{cart[item.id] ?? 0}</span>
                <button aria-label={`เพิ่ม ${item.name}`} onClick={() => changeQuantity(item.id, 1)}>+</button>
              </div>
            </article>)}
          </section>}
        <section className="ordering-cart" aria-label="ตะกร้าอาหาร">
          <div><h2>ตะกร้าอาหาร</h2><p>เลือกแล้ว {count} รายการ</p></div>
          <button className="ordering-submit" disabled={!count || submitting} onClick={submit}>
            {submitting ? 'กำลังส่ง...' : 'ยืนยันคำสั่งซื้อ'}
          </button>
        </section>
        <section className="ordering-history" aria-label="สถานะคำสั่งซื้อ">
          <div className="ordering-history-title"><h2>คำสั่งซื้อของโต๊ะ</h2><button onClick={refreshOrders}>อัปเดตสถานะ</button></div>
          {orders.length === 0 ? <p>ยังไม่มีคำสั่งซื้อ</p> : orders.map((order) =>
            <article key={order.orderId} className="ordering-order">
              <strong>คำสั่งซื้อ #{order.orderId}</strong><span>{order.status}</span>
              <p>{order.items.map((item) => `${item.name} × ${item.quantity}`).join(', ')}</p>
            </article>)}
        </section>
      </>}
    </main>
  )
}
