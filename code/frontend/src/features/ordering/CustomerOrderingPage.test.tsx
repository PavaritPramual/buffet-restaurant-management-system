// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import CustomerOrderingPage from './CustomerOrderingPage'
import * as api from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return { ...actual, getMenu: vi.fn(), getCategories: vi.fn(), getOrders: vi.fn(), placeOrder: vi.fn() }
})

afterEach(() => { cleanup(); vi.clearAllMocks() })

function renderPage(path = '/customer/sessions/1') {
  return render(<MemoryRouter initialEntries={[path]}><Routes><Route path="/customer/sessions/:sessionId" element={<CustomerOrderingPage />} /></Routes></MemoryRouter>)
}

describe('CustomerOrderingPage', () => {
  it('rejects an invalid session id before calling the API', () => {
    renderPage('/customer/sessions/not-a-number')
    expect(screen.getByRole('alert').textContent).toContain('รหัสรอบการรับประทานไม่ถูกต้อง')
    expect(api.getMenu).not.toHaveBeenCalled()
  })

  it('shows menu images and creates an order with duplicate-submit protection', async () => {
    vi.mocked(api.getMenu).mockResolvedValue([{ id: 10, categoryId: 1, categoryName: 'ของทอด', name: 'ไก่ทอด', description: 'ทอดใหม่ทุกจาน', available: true, packageIds: [1], imageUrl: '/images/chicken.jpg' }])
    vi.mocked(api.getCategories).mockResolvedValue([{ id: 1, name: 'ของทอด' }])
    vi.mocked(api.getOrders).mockResolvedValue([])
    vi.mocked(api.placeOrder).mockResolvedValue({ orderId: 7, sessionId: 1, tableNumber: 'T01', status: 'RECEIVED', createdAt: '2026-09-24T10:00:00+07:00', items: [{ menuItemId: 10, name: 'ไก่ทอด', quantity: 1 }] })
    renderPage()
    const image = await screen.findByRole('img', { name: 'ไก่ทอด' })
    expect(image.getAttribute('src')).toBe('/images/chicken.jpg')
    expect(screen.getByText('ทอดใหม่ทุกจาน')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
    const confirmButton = screen.getByRole('button', { name: 'ยืนยัน' })
    fireEvent.click(confirmButton)
    fireEvent.click(confirmButton)
    await waitFor(() => expect(api.placeOrder).toHaveBeenCalledWith(1, [{ menuItemId: 10, quantity: 1 }]))
    expect(api.placeOrder).toHaveBeenCalledTimes(1)
    expect(await screen.findByText('รับออเดอร์แล้ว')).toBeTruthy()
  })

  it('shows an actionable empty state when no menu is available', async () => {
    vi.mocked(api.getMenu).mockResolvedValue([]); vi.mocked(api.getCategories).mockResolvedValue([]); vi.mocked(api.getOrders).mockResolvedValue([])
    renderPage()
    expect(await screen.findByText('ยังไม่มีเมนูในหมวดนี้')).toBeTruthy()
    expect(screen.getByText('ลองเลือกหมวดอื่นหรือสอบถามพนักงานได้ค่ะ')).toBeTruthy()
  })
})
