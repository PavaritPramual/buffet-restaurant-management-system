// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import StaffServingPage from './StaffServingPage'
import * as api from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return { ...actual, getIncomingOrders: vi.fn(), getReadyOrders: vi.fn(), updateOrderStatus: vi.fn() }
})

afterEach(() => { cleanup(); vi.clearAllMocks(); vi.useRealTimers() })

describe('StaffServingPage', () => {
  it('shows an empty state when there are no ready orders', async () => {
    vi.mocked(api.getReadyOrders).mockResolvedValue([])
    render(<StaffServingPage />)
    expect(await screen.findByText('ยังไม่มีออเดอร์พร้อมเสิร์ฟ')).toBeTruthy()
  })

  it('shows the order created time for a READY order', async () => {
    vi.mocked(api.getReadyOrders).mockResolvedValue([
      { orderId: 5, sessionId: 1, tableNumber: 'A05', status: 'READY', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 2 }] },
    ])
    render(<StaffServingPage />)
    expect(await screen.findByText(/รับออเดอร์เมื่อ/)).toBeTruthy()
  })

  it('marks a READY order as SERVED and removes it from the board', async () => {
    vi.mocked(api.getReadyOrders).mockResolvedValue([
      { orderId: 5, sessionId: 1, tableNumber: 'A05', status: 'READY', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 2 }] },
    ])
    vi.mocked(api.updateOrderStatus).mockResolvedValue({ orderId: 5, sessionId: 1, tableNumber: 'A05', status: 'SERVED', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 2 }] })
    render(<StaffServingPage />)
    expect(await screen.findByText(/ออเดอร์ #5/)).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'เสิร์ฟแล้ว' }))
    await waitFor(() => expect(api.updateOrderStatus).toHaveBeenCalledWith(5, 'SERVED', 'SERVICE_STAFF'))
    expect(await screen.findByText('เสิร์ฟออเดอร์ #5 แล้ว')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'เสิร์ฟแล้ว' })).toBeNull()
  })

  it('shows an error message when loading orders fails', async () => {
    vi.mocked(api.getReadyOrders).mockRejectedValue(new Error('network error'))
    render(<StaffServingPage />)
    expect(await screen.findByRole('alert')).toBeTruthy()
  })

  it('polls automatically and shows a new order without any button click, matching the promised copy', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    vi.mocked(api.getReadyOrders)
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        { orderId: 7, sessionId: 1, tableNumber: 'C07', status: 'READY', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 1 }] },
      ])

    render(<StaffServingPage />)
    await waitFor(() => expect(api.getReadyOrders).toHaveBeenCalledTimes(1))
    expect(screen.queryByText(/ออเดอร์ #7/)).toBeNull()

    await vi.advanceTimersByTimeAsync(5000)

    await waitFor(() => expect(api.getReadyOrders).toHaveBeenCalledTimes(2))
    expect(await screen.findByText(/ออเดอร์ #7/)).toBeTruthy()
  })
})
