import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import CustomerOrderingPage from './CustomerOrderingPage'
import { getCategories, getMenu, getOrders, placeOrder } from './api'

vi.mock('./api', () => ({
  getCategories: vi.fn(), getMenu: vi.fn(), getOrders: vi.fn(), placeOrder: vi.fn(),
  getApiError: () => 'API error',
}))

beforeEach(() => {
  vi.mocked(getCategories).mockResolvedValue([{ id: 1, name: 'อาหารหลัก' }])
  vi.mocked(getMenu).mockResolvedValue([{ id: 2, categoryId: 1, name: 'ผักรวม', available: true,
    packageIds: [1], imageUrl: 'https://example.com/vegetables.jpg' }])
  vi.mocked(getOrders).mockResolvedValue([])
  vi.mocked(placeOrder).mockResolvedValue({ orderId: 8, sessionId: 1, tableNumber: 'A01',
    items: [{ menuItemId: 2, name: 'ผักรวม', quantity: 1 }], status: 'RECEIVED',
    createdAt: '2026-09-18T17:00:00+07:00' })
})
afterEach(() => { cleanup(); vi.clearAllMocks() })

describe('CustomerOrderingPage', () => {
  it('shows menu image and submits one selected item', async () => {
    const user = userEvent.setup()
    render(<MemoryRouter initialEntries={['/customer/sessions/1']}><Routes>
      <Route path="/customer/sessions/:sessionId" element={<CustomerOrderingPage />} />
    </Routes></MemoryRouter>)
    expect((await screen.findByRole('img', { name: 'ผักรวม' })).getAttribute('src'))
      .toBe('https://example.com/vegetables.jpg')
    await user.click(screen.getByRole('button', { name: 'เพิ่ม ผักรวม' }))
    await user.click(screen.getByRole('button', { name: 'ยืนยันคำสั่งซื้อ' }))
    await waitFor(() => expect(placeOrder).toHaveBeenCalledWith(1, [{ menuItemId: 2, quantity: 1 }]))
    expect(screen.getAllByText(/คำสั่งซื้อ #8/).length).toBe(2)
  })

  it('does not fetch for an invalid session ID', async () => {
    render(<MemoryRouter initialEntries={['/customer/sessions/nope']}><Routes>
      <Route path="/customer/sessions/:sessionId" element={<CustomerOrderingPage />} />
    </Routes></MemoryRouter>)
    expect(screen.getByRole('alert').textContent).toContain('รหัสรอบการรับประทานไม่ถูกต้อง')
    expect(getMenu).not.toHaveBeenCalled()
  })
})
