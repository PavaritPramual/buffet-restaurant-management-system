// @vitest-environment jsdom
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom'
import { beforeEach, afterEach, expect, it, vi } from 'vitest'
import CustomerOrderingPage from './CustomerOrderingPage'
import * as api from './api'
import { customerApiClient } from '../../api/client'

vi.mock('./api', async () => ({
  ...await vi.importActual<typeof import('./api')>('./api'),
  getCustomerContext: vi.fn(), redeemQr: vi.fn(), getCustomerPackage: vi.fn(),
  getBillStatus: vi.fn(), requestBill: vi.fn(), getMenu: vi.fn(), getOrders: vi.fn(), placeOrder: vi.fn(),
}))
beforeEach(() => { vi.mocked(api.getBillStatus).mockImplementation(async id => ({ sessionId: id, status: "NOT_REQUESTED", requestedAt: null, dueAmount: 299, paidAmount: 0, bill: { sessionId: id, subtotalAmount: 299, discountAmount: 0, totalAmount: 299 } })) })
afterEach(() => { cleanup(); vi.restoreAllMocks(); vi.resetAllMocks() })
const sessionA: api.SessionContext = { sessionId: 1, packageId: 7, tableNumber: 'T01', sessionStatus: 'ACTIVE' }
const sessionB: api.SessionContext = { sessionId: 2, packageId: 8, tableNumber: 'T02', sessionStatus: 'ACTIVE' }
const item: api.MenuItem = { id: 10, categoryId: 3, categoryName: 'ของทอด', name: 'ไก่ทอด', description: null, available: true, packageIds: [7], imageUrl: null }
const order: api.Order = { orderId: 99, sessionId: 1, tableNumber: 'T01', status: 'RECEIVED', createdAt: '', items: [{ menuItemId: 10, name: 'ไก่ทอด', quantity: 1 }] }
function deferred<T>() {
  let resolve!: (v: T) => void
  let reject!: (v: unknown) => void
  const promise = new Promise<T>((done, fail) => { resolve = done; reject = fail })
  return { promise, resolve, reject }
}
function details() {
  vi.mocked(api.getCustomerContext).mockResolvedValue(sessionA)
  vi.mocked(api.getCustomerPackage).mockImplementation(async (id) => ({ id, name: id === 1 ? 'Standard' : 'Premium', price: 299, description: null, active: true }))
  vi.mocked(api.getMenu).mockResolvedValue([item])
}
async function confirm() {
  fireEvent.click(await screen.findByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
}

it.each(['RECEIVED', 'PREPARING'] as const)('keeps one order with its latest %s status when refresh sees it before acknowledgement', async (status) => {
  details()
  const snapshot = deferred<api.Order[]>()
  vi.mocked(api.getOrders).mockResolvedValueOnce([]).mockReturnValueOnce(snapshot.promise)
  const acknowledgement = deferred<api.Order>()
  vi.mocked(api.placeOrder).mockReturnValue(acknowledgement.promise)
  render(<MemoryRouter initialEntries={['/customer/qr']}><CustomerOrderingPage /></MemoryRouter>)
  fireEvent.click(await screen.findByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }))
  await confirm()
  await act(async () => snapshot.resolve([{ ...order, status }]))
  await screen.findByText('คำสั่งซื้อ #99')
  await act(async () => acknowledgement.resolve(order))
  expect(api.placeOrder).toHaveBeenCalledTimes(1)
  expect(screen.getAllByText('คำสั่งซื้อ #99')).toHaveLength(1)
  expect(screen.getByText(status === 'PREPARING' ? 'กำลังเตรียม' : 'รับออเดอร์แล้ว')).toBeTruthy()
  if (status === 'PREPARING') expect(screen.queryByText('รับออเดอร์แล้ว')).toBeNull()
})

it('preserves an order submission error when an unrelated refresh completes later', async () => {
  details()
  const refresh = deferred<api.Order[]>()
  vi.mocked(api.getOrders).mockResolvedValueOnce([]).mockReturnValueOnce(refresh.promise)
  vi.mocked(api.placeOrder).mockRejectedValue({ response: { data: { message: 'ส่งคำสั่งซื้อไม่สำเร็จ' } } })
  render(<MemoryRouter initialEntries={['/customer/qr']}><CustomerOrderingPage /></MemoryRouter>)
  fireEvent.click(await screen.findByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }))
  await confirm()
  await screen.findByRole('alert')
  await act(async () => refresh.resolve([]))
  expect(screen.getByText('1 รายการ · ไก่ทอด × 1')).toBeTruthy()
  expect(screen.queryByRole('alert')?.textContent).toBe('ส่งคำสั่งซื้อไม่สำเร็จ')
})

function Navigation() {
  const navigate = useNavigate()
  return <><button onClick={() => navigate('/other')}>Leave</button><button onClick={() => navigate('/customer/qr')}>Return</button></>
}

it('restores the latest cookie context after navigating away and back during QR exchange', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  vi.mocked(api.redeemQr).mockImplementation(actual.redeemQr)
  details()
  let cookie = sessionA
  vi.mocked(api.getCustomerContext).mockImplementation(actual.getCustomerContext)
  const get = vi.spyOn(customerApiClient, 'get').mockImplementation(async () => ({ data: cookie }))
  vi.mocked(api.getOrders).mockResolvedValue([])
  const exchange = deferred<{ data: api.SessionContext }>()
  const post = vi.spyOn(customerApiClient, 'post').mockImplementation(async () => {
    const response = await exchange.promise; cookie = sessionB; return response
  })
  render(<MemoryRouter initialEntries={['/customer/qr#token=B']}><Navigation /><Routes>
    <Route path="/customer/qr" element={<CustomerOrderingPage />} />
    <Route path="/other" element={<p>Other page</p>} />
  </Routes></MemoryRouter>)
  await waitFor(() => expect(post).toHaveBeenCalledTimes(1))
  fireEvent.click(screen.getByRole('button', { name: 'Leave' }))
  await screen.findByText('Other page')
  fireEvent.click(screen.getByRole('button', { name: 'Return' }))
  expect(screen.queryByText('โต๊ะ T01 · Standard')).toBeNull()
  expect(get).not.toHaveBeenCalled()
  await act(async () => exchange.resolve({ data: sessionB }))
  await waitFor(() => expect(cookie.sessionId).toBe(2))
  expect(await screen.findByText('โต๊ะ T02 · Premium')).toBeTruthy()
  expect(screen.queryByText('โต๊ะ T01 · Standard')).toBeNull()
})

it('preserves a failed latest scan across remount and retries it explicitly without restoring A', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  details()
  vi.mocked(api.redeemQr).mockImplementation(actual.redeemQr)
  vi.mocked(api.getCustomerContext).mockImplementation(actual.getCustomerContext)
  vi.mocked(api.getOrders).mockResolvedValue([])
  let cookie = sessionA
  const get = vi.spyOn(customerApiClient, 'get').mockImplementation(async () => ({ data: cookie }))
  const exchange = deferred<{ data: api.SessionContext }>()
  const post = vi.spyOn(customerApiClient, 'post').mockImplementationOnce(() => exchange.promise)
    .mockImplementationOnce(async () => { cookie = sessionB; return { data: sessionB } })
  render(<MemoryRouter initialEntries={['/customer/qr#token=B']}><Navigation /><Routes>
    <Route path="/customer/qr" element={<CustomerOrderingPage />} />
    <Route path="/other" element={<p>Other page</p>} />
  </Routes></MemoryRouter>)
  await waitFor(() => expect(post).toHaveBeenCalledTimes(1))
  fireEvent.click(screen.getByRole('button', { name: 'Leave' }))
  fireEvent.click(screen.getByRole('button', { name: 'Return' }))
  await act(async () => exchange.reject({ response: { data: { message: 'แลก QR ไม่สำเร็จ' } } }))
  expect((await screen.findByRole('alert')).textContent).toBe('แลก QR ไม่สำเร็จ')
  expect(screen.queryByText('โต๊ะ T01 · Standard')).toBeNull()
  expect(get).not.toHaveBeenCalled()
  fireEvent.click(screen.getByRole('button', { name: 'ลองอีกครั้ง' }))
  expect(await screen.findByText('โต๊ะ T02 · Premium')).toBeTruthy()
  expect(post.mock.calls.map(([, body]) => (body as { token: string }).token)).toEqual(['B', 'B'])
  expect(get).toHaveBeenCalledTimes(1)
})

it('clears a history failure on refresh recovery while retaining an order failure', async () => {
  details()
  const refresh = deferred<api.Order[]>()
  vi.mocked(api.getOrders).mockResolvedValueOnce([]).mockReturnValueOnce(refresh.promise).mockResolvedValueOnce([])
  vi.mocked(api.placeOrder).mockRejectedValue({ response: { data: { message: 'ส่งคำสั่งซื้อไม่สำเร็จ' } } })
  render(<MemoryRouter initialEntries={['/customer/qr']}><CustomerOrderingPage /></MemoryRouter>)
  fireEvent.click(await screen.findByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }))
  await confirm()
  await screen.findByText('ส่งคำสั่งซื้อไม่สำเร็จ')
  await act(async () => refresh.reject({ response: { data: { message: 'อัปเดตประวัติไม่สำเร็จ' } } }))
  expect(screen.getByText('อัปเดตประวัติไม่สำเร็จ')).toBeTruthy()
  expect(screen.getByText('ส่งคำสั่งซื้อไม่สำเร็จ')).toBeTruthy()
  fireEvent.click(screen.getByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }))
  await waitFor(() => expect(screen.queryByText('อัปเดตประวัติไม่สำเร็จ')).toBeNull())
  expect(screen.getByText('ส่งคำสั่งซื้อไม่สำเร็จ')).toBeTruthy()
})
