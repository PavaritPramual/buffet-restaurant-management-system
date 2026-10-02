// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import KitchenBoardPage from './KitchenBoardPage'
import * as api from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return { ...actual, getIncomingOrders: vi.fn(), getReadyOrders: vi.fn(), updateOrderStatus: vi.fn() }
})

afterEach(() => { cleanup(); vi.clearAllMocks(); vi.useRealTimers() })

describe('KitchenBoardPage', () => {
  it('shows an empty state when there are no incoming orders', async () => {
    vi.mocked(api.getIncomingOrders).mockResolvedValue([])
    render(<KitchenBoardPage />)
    expect(await screen.findByText('ยังไม่มีออเดอร์เข้าครัว')).toBeTruthy()
  })

  it('shows the table and item count in kitchen-ticket typography', async () => {
    vi.mocked(api.getIncomingOrders).mockResolvedValue([
      { orderId: 1, sessionId: 1, tableNumber: 'A01', status: 'RECEIVED', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 2 }] },
    ])
    render(<KitchenBoardPage />)
    const ticketLine = await screen.findByText('โต๊ะ A01 · 1 รายการ')
    expect(ticketLine.className).toContain('kitchen-sample')
  })

  it('lets the kitchen start preparing a RECEIVED order', async () => {
    vi.mocked(api.getIncomingOrders).mockResolvedValue([
      { orderId: 1, sessionId: 1, tableNumber: 'A01', status: 'RECEIVED', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 2 }] },
    ])
    vi.mocked(api.updateOrderStatus).mockResolvedValue({ orderId: 1, sessionId: 1, tableNumber: 'A01', status: 'PREPARING', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 2 }] })
    render(<KitchenBoardPage />)
    expect(await screen.findByText('รับออเดอร์แล้ว')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'เริ่มเตรียมอาหาร' }))
    await waitFor(() => expect(api.updateOrderStatus).toHaveBeenCalledWith(1, 'PREPARING', 'KITCHEN_STAFF'))
    expect(await screen.findByText('กำลังเตรียม')).toBeTruthy()
    expect(screen.getByRole('button', { name: 'พร้อมเสิร์ฟแล้ว' })).toBeTruthy()
  })

  it('removes an order from the board once marked READY', async () => {
    vi.mocked(api.getIncomingOrders).mockResolvedValue([
      { orderId: 2, sessionId: 1, tableNumber: 'A02', status: 'PREPARING', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 1 }] },
    ])
    vi.mocked(api.updateOrderStatus).mockResolvedValue({ orderId: 2, sessionId: 1, tableNumber: 'A02', status: 'READY', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 1 }] })
    render(<KitchenBoardPage />)
    fireEvent.click(await screen.findByRole('button', { name: 'พร้อมเสิร์ฟแล้ว' }))
    await waitFor(() => expect(api.updateOrderStatus).toHaveBeenCalledWith(2, 'READY', 'KITCHEN_STAFF'))
    await waitFor(() => expect(screen.queryByText(/ออเดอร์ #2/)).toBeNull())
  })

  it('shows an error message when loading orders fails', async () => {
    vi.mocked(api.getIncomingOrders).mockRejectedValue(new Error('network error'))
    render(<KitchenBoardPage />)
    expect(await screen.findByRole('alert')).toBeTruthy()
  })

  it('polls automatically and shows a new order without any button click, matching the promised copy', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    vi.mocked(api.getIncomingOrders)
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        { orderId: 9, sessionId: 1, tableNumber: 'B09', status: 'RECEIVED', createdAt: '2026-09-18T10:00:00+07:00', items: [{ menuItemId: 101, name: 'Sliced Pork', quantity: 1 }] },
      ])

    render(<KitchenBoardPage />)
    await waitFor(() => expect(api.getIncomingOrders).toHaveBeenCalledTimes(1))
    expect(screen.queryByText('โต๊ะ B09 · 1 รายการ')).toBeNull()

    await vi.advanceTimersByTimeAsync(5000)

    await waitFor(() => expect(api.getIncomingOrders).toHaveBeenCalledTimes(2))
    expect(await screen.findByText('โต๊ะ B09 · 1 รายการ')).toBeTruthy()
  })
})
