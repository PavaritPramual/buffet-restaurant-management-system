import { useCallback, useEffect, useRef, useState } from 'react'
import { Button, Card, EmptyState, ErrorAlert, LoadingState, PageHeader, StatusBadge } from '../../components/common'
import type { StatusBadgeTone } from '../../components/common'
import { getApiError, getIncomingOrders, updateOrderStatus } from './api'
import type { FulfillmentOrder } from './api'
import type { OrderStatus } from '../../contracts/shared'
import './fulfillment.css'
import '../../design-system.css'

const POLL_INTERVAL_MS = 5000

const statusPresentation: Record<OrderStatus, { label: string; tone: StatusBadgeTone }> = {
  RECEIVED: { label: 'รับออเดอร์แล้ว', tone: 'info' },
  PREPARING: { label: 'กำลังเตรียม', tone: 'warning' },
  READY: { label: 'พร้อมเสิร์ฟ', tone: 'success' },
  SERVED: { label: 'เสิร์ฟแล้ว', tone: 'success' },
}

const nextAction: Partial<Record<OrderStatus, { targetStatus: OrderStatus; label: string }>> = {
  RECEIVED: { targetStatus: 'PREPARING', label: 'เริ่มเตรียมอาหาร' },
  PREPARING: { targetStatus: 'READY', label: 'พร้อมเสิร์ฟแล้ว' },
}

export default function KitchenBoardPage() {
  const [orders, setOrders] = useState<FulfillmentOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [updatingId, setUpdatingId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [mutationError, setMutationError] = useState('')
  const mutationInFlight = useRef(false)

  // silent = true for background polling, so it never flashes the full-page loading state.
  const load = useCallback(async (options?: { silent?: boolean }) => {
    if (!options?.silent) setLoading(true)
    try {
      setOrders(await getIncomingOrders())
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

  async function advance(order: FulfillmentOrder, targetStatus: OrderStatus) {
    if (mutationInFlight.current) return
    mutationInFlight.current = true
    setUpdatingId(order.orderId)
    setMutationError('')
    try {
      const updated = await updateOrderStatus(order.orderId, targetStatus)
      setOrders((current) => updated.status === 'READY' || updated.status === 'SERVED'
        ? current.filter((entry) => entry.orderId !== order.orderId)
        : current.map((entry) => (entry.orderId === order.orderId ? updated : entry)))
    } catch (cause) {
      setMutationError(getApiError(cause))
    } finally {
      mutationInFlight.current = false
      setUpdatingId(null)
    }
  }

  return <main className="fulfillment-page kitchen-page">
    <PageHeader eyebrow="ครัว" title="ออเดอร์ที่รอดำเนินการ" description="เริ่มเตรียมอาหารและกดพร้อมเสิร์ฟเมื่อทำเสร็จ · รายการใหม่จะขึ้นให้อัตโนมัติ"
      action={<Button variant="secondary" onClick={() => void load()}>อัปเดต</Button>} />
    {error && <ErrorAlert message={error} />}
    {mutationError && <ErrorAlert message={mutationError} />}
    {loading ? <LoadingState label="กำลังโหลดออเดอร์…" /> : orders.length === 0
      ? <EmptyState title="ยังไม่มีออเดอร์เข้าครัว" description="ออเดอร์ใหม่จะปรากฏที่นี่โดยอัตโนมัติ" />
      : <section className="order-board" aria-label="ออเดอร์เข้าครัว">
        {orders.map((order) => {
          const action = nextAction[order.status]
          return <Card key={order.orderId} className="order-board-card">
            <div className="order-board-header">
              <p className="kitchen-sample">โต๊ะ {order.tableNumber} · {order.items.length} รายการ</p>
              <StatusBadge tone={statusPresentation[order.status].tone}>{statusPresentation[order.status].label}</StatusBadge>
            </div>
            <span className="order-id">ออเดอร์ #{order.orderId}</span>
            <p className="order-items">{order.items.map((item) => `${item.name} × ${item.quantity}`).join(', ')}</p>
            <p className="order-time">รับออเดอร์เมื่อ {new Date(order.createdAt).toLocaleTimeString('th-TH', { hour: '2-digit', minute: '2-digit' })}</p>
            {action && <Button loading={updatingId === order.orderId} onClick={() => void advance(order, action.targetStatus)}>{action.label}</Button>}
          </Card>
        })}
      </section>}
  </main>
}
