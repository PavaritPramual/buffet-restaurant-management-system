// @vitest-environment jsdom
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, useNavigate } from 'react-router-dom'
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import CustomerOrderingPage from './CustomerOrderingPage'
import * as api from './api'

vi.mock('./api', async () => ({
  ...await vi.importActual<typeof import('./api')>('./api'),
  getCustomerContext: vi.fn(), redeemQr: vi.fn(), getCustomerPackage: vi.fn(),
  getBillStatus: vi.fn(), requestBill: vi.fn(), getMenu: vi.fn(), getOrders: vi.fn(), placeOrder: vi.fn(),
}))
const context: api.SessionContext = { sessionId: 1, packageId: 7, tableNumber: 'T01', sessionStatus: 'ACTIVE' }
const item: api.MenuItem = { id: 10, categoryId: 3, categoryName: 'ของทอด', name: 'ไก่ทอด', description: null, available: true, packageIds: [7], imageUrl: null }
const bill: api.CustomerBillStatus = { sessionId: 1, status: 'NOT_REQUESTED', requestedAt: null, dueAmount: 299, paidAmount: 0, bill: { sessionId: 1, totalAmount: 299, subtotalAmount: 299, discountAmount: 0 } }
let poll: () => void
beforeEach(() => {
  vi.mocked(api.redeemQr).mockResolvedValue(context)
  vi.mocked(api.getCustomerContext).mockResolvedValue(context)
  vi.mocked(api.getCustomerPackage).mockResolvedValue({ id: 7, name: 'Standard', price: 299, active: true, description: null })
  vi.mocked(api.getMenu).mockResolvedValue([item]); vi.mocked(api.getOrders).mockResolvedValue([])
  vi.mocked(api.getBillStatus).mockResolvedValue(bill)
  const originalInterval = window.setInterval.bind(window)
  vi.spyOn(window, 'setInterval').mockImplementation((callback, delay, ...args) => {
    if (delay === 5000) { poll = callback as () => void; return 123 }
    return originalInterval(callback, delay, ...args)
  })
  vi.spyOn(window, 'clearInterval')
})
afterEach(() => { cleanup(); vi.restoreAllMocks(); vi.resetAllMocks() })
function Navigation() {
  const navigate = useNavigate()
  return <button onClick={() => navigate('/customer/qr#token=new')}>QR ใหม่</button>
}
function show() { render(<MemoryRouter initialEntries={['/customer/qr#token=first']}><Navigation /><CustomerOrderingPage /></MemoryRouter>) }
async function ready() { show(); await screen.findByRole('button', { name: 'เพิ่ม ไก่ทอด' }) }
function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (error: unknown) => void
  const promise = new Promise<T>((done, fail) => { resolve = done; reject = fail })
  return { promise, resolve, reject }
}
function assertExpired() {
  expect(screen.getByRole('heading', { name: 'สิทธิ์สั่งอาหารสิ้นสุดแล้ว' })).toBeTruthy()
  expect(screen.queryByRole('button', { name: 'ยืนยันการสั่ง' })).toBeNull()
  expect(screen.queryByRole('button', { name: 'ขอคิดบิล' })).toBeNull()
  expect(screen.queryByText('ไก่ทอด')).toBeNull()
  expect(screen.queryByText('กำลังตรวจสอบสถานะบิล ก่อนรับคำสั่งซื้อใหม่')).toBeNull()
  expect(screen.queryByRole('dialog')).toBeNull()
}
it.each([401, 404])('shows terminal access expiry on initial lookup %s without a retry loop', async status => {
  vi.mocked(api.redeemQr).mockRejectedValue({ response: { status } })
  show(); await screen.findByRole('heading', { name: 'สิทธิ์สั่งอาหารสิ้นสุดแล้ว' })
  assertExpired()
  expect(api.getBillStatus).not.toHaveBeenCalled()
  expect(screen.queryByRole('button', { name: 'ลองอีกครั้ง' })).toBeNull()
})
it.each([401, 404])('clears success, menu, cart and dialogs and stops polling when close returns %s', async status => {
  vi.mocked(api.placeOrder).mockResolvedValue({ orderId: 99, sessionId: 1, tableNumber: 'T01', status: 'RECEIVED', createdAt: '', items: [{ menuItemId: 10, name: 'ไก่ทอด', quantity: 1 }] })
  await ready()
  fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
  await screen.findByText('ส่งคำสั่งซื้อ #99 แล้ว')
  fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  fireEvent.click(screen.getByRole('button', { name: 'ขอคิดบิล' }))
  vi.mocked(api.getBillStatus).mockRejectedValue({ response: { status } })
  await act(async () => poll())
  assertExpired()
  expect(screen.queryByText('ส่งคำสั่งซื้อ #99 แล้ว')).toBeNull()
  expect(screen.queryByText('คำสั่งซื้อ #99')).toBeNull()
  expect(window.clearInterval).toHaveBeenCalledWith(123)
  const reads = vi.mocked(api.getBillStatus).mock.calls.length
  await act(async () => poll())
  expect(api.getBillStatus).toHaveBeenCalledTimes(reads)
})
it.each([undefined, 503])('keeps a transient %s failure recoverable and pauses writes until bill recovery', async status => {
  const backgroundBill = deferred<api.CustomerBillStatus>()
  // Initial load and the immediate background poll are separate requests.
  // Reject the captured poll instead of racing an interval against its in-flight guard.
  vi.mocked(api.getBillStatus).mockResolvedValueOnce(bill).mockReturnValueOnce(backgroundBill.promise)
  await ready()
  fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  await act(async () => backgroundBill.reject(status ? { response: { status } } : new Error('offline')))
  expect(screen.queryByText('สิทธิ์สั่งอาหารสิ้นสุดแล้ว')).toBeNull()
  expect(screen.getByText('1 รายการ · ไก่ทอด × 1')).toBeTruthy()
  expect((screen.getByRole('button', { name: 'ยืนยันการสั่ง' }) as HTMLButtonElement).disabled).toBe(true)
  expect(screen.getByText('ยอดรวม ฿299.00')).toBeTruthy()
  fireEvent.click(screen.getByRole('button', { name: 'ตรวจสอบบิลอีกครั้ง' }))
  await act(async () => {})
  expect(screen.queryByRole('alert')).toBeNull()
  expect((screen.getByRole('button', { name: 'ยืนยันการสั่ง' }) as HTMLButtonElement).disabled).toBe(false)
})
it('accepts a new QR in the same tab and ignores an older order acknowledgement after expiry', async () => {
  const oldOrder = deferred<api.Order>()
  const expiryPoll = deferred<api.CustomerBillStatus>()
  // Control the first background poll; a captured interval can skip while it is still running.
  vi.mocked(api.getBillStatus).mockResolvedValueOnce(bill).mockReturnValueOnce(expiryPoll.promise)
  vi.mocked(api.placeOrder).mockReturnValue(oldOrder.promise)
  await ready()
  fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
  expect(api.placeOrder).toHaveBeenCalledTimes(1)
  await act(async () => expiryPoll.reject({ response: { status: 401 } })); assertExpired()
  vi.mocked(api.redeemQr).mockResolvedValue({ ...context, sessionId: 2, tableNumber: 'T02' })
  fireEvent.click(screen.getByRole('button', { name: 'QR ใหม่' }))
  await screen.findByText('โต๊ะ T02 · Standard')
  await act(async () => oldOrder.resolve({ orderId: 99, sessionId: 1, tableNumber: 'T01', status: 'RECEIVED', createdAt: '', items: [] }))
  expect(screen.queryByText('ส่งคำสั่งซื้อ #99 แล้ว')).toBeNull()
  expect(screen.queryByText('สิทธิ์สั่งอาหารสิ้นสุดแล้ว')).toBeNull()
  expect(screen.getByText('ยังไม่ได้เลือกเมนู')).toBeTruthy()
})
it.each([true, false])('checks an order 404 separately (valid grant: %s)', async valid => {
  await ready()
  vi.mocked(api.placeOrder).mockRejectedValue({ response: { status: 404, data: { message: 'เมนูนี้ถูกเก็บออกแล้ว' } } })
  if (!valid) vi.mocked(api.getCustomerContext).mockRejectedValue({ response: { status: 401 } })
  fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
  if (valid) {
    await screen.findByText('เมนูนี้ถูกเก็บออกแล้ว')
    expect(screen.queryByText('สิทธิ์สั่งอาหารสิ้นสุดแล้ว')).toBeNull()
    expect(screen.getByText('1 รายการ · ไก่ทอด × 1')).toBeTruthy()
  } else { await screen.findByRole('heading', { name: 'สิทธิ์สั่งอาหารสิ้นสุดแล้ว' }); assertExpired() }
})
it('expires from an order-history 401 while an older bill response is pending', async () => {
  await ready()
  const oldBill = deferred<api.CustomerBillStatus>()
  vi.mocked(api.getBillStatus).mockReturnValue(oldBill.promise)
  await act(async () => poll())
  vi.mocked(api.getOrders).mockRejectedValue({ response: { status: 401 } })
  fireEvent.click(screen.getByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }))
  await screen.findByRole('heading', { name: 'สิทธิ์สั่งอาหารสิ้นสุดแล้ว' })
  await act(async () => oldBill.resolve(bill)); assertExpired()
})
it('expires on bill-request 404', async () => {
  await ready()
  vi.mocked(api.requestBill).mockRejectedValue({ response: { status: 404 } })
  fireEvent.click(screen.getByRole('button', { name: 'ขอคิดบิล' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
  await screen.findByRole('heading', { name: 'สิทธิ์สั่งอาหารสิ้นสุดแล้ว' }); assertExpired()
})
it('ignores an old bill 401 after a new QR has already restored access', async () => {
  await ready()
  const oldBill = deferred<api.CustomerBillStatus>()
  vi.mocked(api.getBillStatus).mockReturnValueOnce(oldBill.promise)
  await act(async () => poll())
  vi.mocked(api.redeemQr).mockResolvedValue({ ...context, sessionId: 2, tableNumber: 'T02' })
  fireEvent.click(screen.getByRole('button', { name: 'QR ใหม่' }))
  await screen.findByText('โต๊ะ T02 · Standard')
  await act(async () => oldBill.reject({ response: { status: 401 } }))
  expect(screen.getByText('โต๊ะ T02 · Standard')).toBeTruthy()
  expect(screen.queryByText('สิทธิ์สั่งอาหารสิ้นสุดแล้ว')).toBeNull()
  expect((screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }) as HTMLButtonElement).disabled).toBe(false)
})
