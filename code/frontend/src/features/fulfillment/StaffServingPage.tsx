import { useCallback, useEffect, useState } from 'react'
import { Button, Card, EmptyState, ErrorAlert, LoadingState, PageHeader, StatusBadge } from '../../components/common'
import { getApiError, getReadyOrders, updateOrderStatus } from './api'
import type { FulfillmentOrder } from './api'
import './fulfillment.css'

const POLL_INTERVAL_MS = 5000

export default function StaffServingPage() {
  const [orders, setOrders] = useState<FulfillmentOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [servingId, setServingId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  // silent = true for background polling, so it never flashes the full-page loading state.
  const load = useCallback(async (options?: { silent?: boolean }) => {
    if (!options?.silent) setLoading(true)
    try {
      setOrders(await getReadyOrders())
      setError('')
    } catch (cause) {
      setError(getApiError(cause))
    } finally {
      if (!options?.silent) setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
    const intervalId = setInterval(() => { void load({ silent: true }) }, POLL_INTERVAL_MS)
    return () => clearInterval(intervalId)
  }, [load])

  async function markServed(order: FulfillmentOrder) {
    if (servingId !== null) return
    setServingId(order.orderId)
    setError(''); setNotice('')
    try {
      await updateOrderStatus(order.orderId, 'SERVED', 'SERVICE_STAFF')
      setOrders((current) => current.filter((entry) => entry.orderId !== order.orderId))
      setNotice(`เสิร์ฟออเดอร์ #${order.orderId} แล้ว`)
    } catch (cause) {
      setError(getApiError(cause))
    } finally {
      setServingId(null)
    }
  }

  return <main className="fulfillment-page staff-page">
    <PageHeader eyebrow="พนักงานเสิร์ฟ" title="ออเดอร์พร้อมเสิร์ฟ" description="ยืนยันเมื่อเสิร์ฟถึงโต๊ะแล้ว · รายการใหม่จะขึ้นให้อัตโนมัติ"
      action={<Button variant="secondary" onClick={() => void load()}>อัปเดต</Button>} />
    {error && <ErrorAlert message={error} />}
    {notice && <div className="ordering-notice" role="status">{notice}</div>}
    {loading ? <LoadingState label="กำลังโหลดออเดอร์…" /> : orders.length === 0
      ? <EmptyState title="ยังไม่มีออเดอร์พร้อมเสิร์ฟ" description="ออเดอร์จากครัวจะปรากฏที่นี่โดยอัตโนมัติเมื่อพร้อม" />
      : <section className="order-board" aria-label="ออเดอร์พร้อมเสิร์ฟ">
        {orders.map((order) => <Card key={order.orderId} className="order-board-card">
          <div className="order-board-header">
            <div><strong>โต๊ะ {order.tableNumber}</strong><span className="order-id"> · ออเดอร์ #{order.orderId}</span></div>
            <StatusBadge tone="success">พร้อมเสิร์ฟ</StatusBadge>
          </div>
          <p className="order-items">{order.items.map((item) => `${item.name} × ${item.quantity}`).join(', ')}</p>
          <p className="order-time">รับออเดอร์เมื่อ {new Date(order.createdAt).toLocaleTimeString('th-TH', { hour: '2-digit', minute: '2-digit' })}</p>
          <Button loading={servingId === order.orderId} onClick={() => void markServed(order)}>เสิร์ฟแล้ว</Button>
        </Card>)}
      </section>}
  </main>
}
