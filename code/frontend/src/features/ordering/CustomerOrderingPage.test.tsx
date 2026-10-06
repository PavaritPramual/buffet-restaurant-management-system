// @vitest-environment jsdom
import { StrictMode } from 'react'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import { customerApiClient } from '../../api/client'
import CustomerOrderingPage from './CustomerOrderingPage'
import * as api from './api'
import type { MenuItem, SessionContext } from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return { ...actual, redeemQr: vi.fn(), getCustomerContext: vi.fn(), getCustomerPackage: vi.fn(), getMenu: vi.fn(), getCategories: vi.fn(), getBillStatus: vi.fn(), requestBill: vi.fn(), getOrders: vi.fn(), placeOrder: vi.fn() }
})

beforeEach(() => { vi.mocked(api.getBillStatus).mockImplementation(async id => ({ sessionId: id, status: "NOT_REQUESTED", requestedAt: null, dueAmount: 299, paidAmount: 0, bill: { sessionId: id, subtotalAmount: 299, discountAmount: 0, totalAmount: 299 } })) })
afterEach(() => { cleanup(); vi.restoreAllMocks(); vi.resetAllMocks() })

const sessionA: SessionContext = { sessionId: 1, packageId: 1, tableNumber: 'T01', sessionStatus: 'ACTIVE' }
const sessionB: SessionContext = { sessionId: 2, packageId: 2, tableNumber: 'T02', sessionStatus: 'ACTIVE' }
const chicken: MenuItem = { id: 10, categoryId: 1, categoryName: 'ของทอด', name: 'ไก่ทอด', description: 'ทอดใหม่ทุกจาน', available: true, packageIds: [1], imageUrl: '/images/chicken.jpg' }

function Navigation() {
  const navigate = useNavigate()
  const location = useLocation()
  return <><button onClick={() => navigate('/customer/qr#token=B')}>สแกน B</button><button onClick={() => navigate('/customer/qr#token=A')}>สแกน A</button><output data-testid="fragment">{location.hash}</output></>
}

function renderPage(path = '/customer/qr#token=test-token', strict = false) {
  const page = <MemoryRouter initialEntries={[path]}><Navigation /><Routes><Route path="/customer/qr" element={<CustomerOrderingPage />} /></Routes></MemoryRouter>
  return render(strict ? <StrictMode>{page}</StrictMode> : page)
}

function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason: unknown) => void
  const promise = new Promise<T>((done, fail) => { resolve = done; reject = fail })
  return { promise, resolve, reject }
}

function mockSessionDetails() {
  vi.mocked(api.getBillStatus).mockImplementation(async id => ({ sessionId: id, status: "NOT_REQUESTED", requestedAt: null, dueAmount: 299, paidAmount: 0, bill: { sessionId: id, subtotalAmount: 299, discountAmount: 0, totalAmount: 299 } }))
  vi.mocked(api.getCustomerPackage).mockImplementation(async (id) => ({ id, name: id === 1 ? 'Standard' : 'Premium', price: 299, description: null, active: true }))
  vi.mocked(api.getMenu).mockImplementation(async (id) => id === 1 ? [chicken] : [])
  vi.mocked(api.getOrders).mockResolvedValue([])
}

describe('CustomerOrderingPage', () => {
  it('shows loading while session verification is pending', async () => {
    const lookup = deferred<SessionContext>()
    vi.mocked(api.redeemQr).mockReturnValue(lookup.promise)
    mockSessionDetails()
    renderPage()
    expect(screen.getByText('กำลังตรวจสอบรอบการรับประทานและโหลดเมนู…')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'ยืนยันการสั่ง' })).toBeNull()
    lookup.resolve(sessionA)
    expect(await screen.findByText('โต๊ะ T01 · Standard')).toBeTruthy()
  })

  it('retries a failed menu load using the cookie instead of consuming the QR again', async () => {
    vi.mocked(api.redeemQr).mockResolvedValue(sessionA)
    vi.mocked(api.getCustomerContext).mockResolvedValue(sessionA)
    mockSessionDetails()
    vi.mocked(api.getMenu).mockRejectedValueOnce({ response: { data: { message: 'โหลดเมนูไม่สำเร็จ' } } }).mockResolvedValue([chicken])
    renderPage()
    expect(await screen.findByRole('alert')).toHaveProperty('textContent', 'โหลดเมนูไม่สำเร็จ')
    fireEvent.click(screen.getByRole('button', { name: 'ลองอีกครั้ง' }))
    expect(await screen.findByText('โต๊ะ T01 · Standard')).toBeTruthy()
    expect(api.redeemQr).toHaveBeenCalledTimes(1)
    expect(api.getCustomerContext).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('keeps the cart after an order failure and prevents two pending submissions', async () => {
    vi.mocked(api.redeemQr).mockResolvedValue(sessionA)
    mockSessionDetails()
    const pending = deferred<api.Order>()
    vi.mocked(api.placeOrder).mockReturnValue(pending.promise)
    renderPage()
    fireEvent.click(await screen.findByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
    const confirm = screen.getByRole('button', { name: 'ยืนยัน' })
    fireEvent.click(confirm); fireEvent.click(confirm)
    expect(api.placeOrder).toHaveBeenCalledTimes(1)
    expect(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }).hasAttribute('disabled')).toBe(true)
    pending.reject({ response: { data: { message: 'ส่งออเดอร์ไม่สำเร็จ' } } })
    expect(await screen.findByRole('alert')).toHaveProperty('textContent', 'ส่งออเดอร์ไม่สำเร็จ')
    expect(screen.getByText('1 รายการ · ไก่ทอด × 1')).toBeTruthy()
    expect(screen.queryByRole('dialog')).toBeNull()
    expect(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }).hasAttribute('disabled')).toBe(false)
  })

  it('filters categories and removes an item when its quantity returns to zero', async () => {
    vi.mocked(api.redeemQr).mockResolvedValue(sessionA)
    mockSessionDetails()
    vi.mocked(api.getMenu).mockResolvedValue([chicken, { ...chicken, id: 11, name: 'ชาไทย', categoryId: 2, categoryName: 'เครื่องดื่ม' }])
    renderPage()
    fireEvent.click(await screen.findByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ลด ไก่ทอด' }))
    expect(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }).hasAttribute('disabled')).toBe(true)
    fireEvent.click(screen.getByRole('button', { name: 'เครื่องดื่ม' }))
    expect(screen.queryByRole('button', { name: 'เพิ่ม ไก่ทอด' })).toBeNull()
    expect(screen.getByRole('button', { name: 'เพิ่ม ชาไทย' })).toBeTruthy()
  })

  it('does not add the previous session order when a new QR is scanned during submission', async () => {
    vi.mocked(api.redeemQr).mockImplementation(async (token) => token === 'A' ? sessionA : sessionB)
    mockSessionDetails()
    const pending = deferred<api.Order>()
    vi.mocked(api.placeOrder).mockReturnValue(pending.promise)
    renderPage('/customer/qr#token=A')
    fireEvent.click(await screen.findByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    fireEvent.click(screen.getByRole('button', { name: 'สแกน B' }))
    expect(await screen.findByText('โต๊ะ T02 · Premium')).toBeTruthy()
    pending.resolve({ orderId: 99, sessionId: 1, tableNumber: 'T01', status: 'RECEIVED', createdAt: '', items: [] })
    await waitFor(() => expect(screen.queryByText('คำสั่งซื้อ #99')).toBeNull())
    expect(screen.getByText('ยังไม่ได้เลือกเมนู')).toBeTruthy()
  })

  it('redeems the QR fragment without placing the token in an API URL', async () => {
    vi.mocked(api.redeemQr).mockResolvedValue({ sessionId: 1, packageId: 1, tableNumber: 'T01', sessionStatus: 'ACTIVE' })
    vi.mocked(api.getCustomerPackage).mockResolvedValue({ id: 1, name: 'Standard', price: 299, description: null, active: true })
    vi.mocked(api.getMenu).mockResolvedValue([])
    vi.mocked(api.getOrders).mockResolvedValue([])
    renderPage()
    expect(await screen.findByText('โต๊ะ T01 · Standard')).toBeTruthy()
    expect(api.redeemQr).toHaveBeenCalledWith('test-token')
  })

  it('shares one QR POST under StrictMode and still displays the session', async () => {
    const actual = await vi.importActual<typeof import('./api')>('./api')
    const post = vi.spyOn(customerApiClient, 'post').mockResolvedValue({ data: sessionA })
    vi.mocked(api.redeemQr).mockImplementation(actual.redeemQr)
    mockSessionDetails()
    renderPage('/customer/qr#token=A', true)
    expect(await screen.findByText('โต๊ะ T01 · Standard')).toBeTruthy()
    expect(post).toHaveBeenCalledTimes(1)
    expect(post).toHaveBeenCalledWith('/dining-sessions/qr-exchange', { token: 'A' })
    expect(screen.getByTestId('fragment').textContent).toBe('')
  })

  it('replaces A with B in the same tab and clears A menu and cart', async () => {
    vi.mocked(api.redeemQr).mockImplementation(async (token) => token === 'A' ? sessionA : sessionB)
    mockSessionDetails()
    vi.mocked(api.getOrders).mockImplementation(async (id) => id === 1 ? [{ orderId: 7, sessionId: 1, tableNumber: 'T01', status: 'RECEIVED', createdAt: '', items: [{ menuItemId: 10, name: 'ไก่ทอด', quantity: 1 }] }] : [])
    renderPage('/customer/qr#token=A')
    expect(await screen.findByText('โต๊ะ T01 · Standard')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
    expect(screen.getByText('1 รายการ · ไก่ทอด × 1')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'สแกน B' }))
    expect(await screen.findByText('โต๊ะ T02 · Premium')).toBeTruthy()
    expect(screen.getByTestId('fragment').textContent).toBe('')
    expect(screen.queryByText('ไก่ทอด')).toBeNull()
    expect(screen.queryByText('คำสั่งซื้อ #7')).toBeNull()
    expect(screen.getByText('ยังไม่ได้เลือกเมนู')).toBeTruthy()
    expect(api.redeemQr).toHaveBeenCalledWith('B')
  })

  it('ignores A when it completes after B', async () => {
    const delayedA = deferred<SessionContext>()
    vi.mocked(api.redeemQr).mockImplementation((token) => token === 'A' ? delayedA.promise : Promise.resolve(sessionB))
    mockSessionDetails()
    renderPage('/customer/qr#token=A')
    fireEvent.click(screen.getByRole('button', { name: 'สแกน B' }))
    expect(await screen.findByText('โต๊ะ T02 · Premium')).toBeTruthy()
    delayedA.resolve(sessionA)
    await waitFor(() => expect(screen.queryByText('โต๊ะ T01 · Standard')).toBeNull())
    expect(screen.getByText('โต๊ะ T02 · Premium')).toBeTruthy()
  })

  it('shows a failed B scan without falling back to A cookie or A data', async () => {
    vi.mocked(api.redeemQr).mockImplementation((token) => token === 'A' ? Promise.resolve(sessionA) : Promise.reject({ response: { data: { message: 'QR นี้ใช้ไม่ได้แล้ว' } } }))
    mockSessionDetails()
    renderPage('/customer/qr#token=A')
    expect(await screen.findByText('โต๊ะ T01 · Standard')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'สแกน B' }))
    expect(await screen.findByText('QR นี้ใช้ไม่ได้แล้ว')).toBeTruthy()
    expect(screen.queryByText('โต๊ะ T01 · Standard')).toBeNull()
    expect(screen.queryByText('ไก่ทอด')).toBeNull()
    expect(api.getCustomerContext).not.toHaveBeenCalled()
  })

  it('tries the same QR again after its first exchange has finished', async () => {
    vi.mocked(api.redeemQr)
      .mockResolvedValueOnce(sessionA)
      .mockRejectedValueOnce({ response: { status: 404, data: { message: 'QR นี้ใช้ไม่ได้แล้ว' } } })
    mockSessionDetails()
    renderPage('/customer/qr#token=A')
    expect(await screen.findByText('โต๊ะ T01 · Standard')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'สแกน A' }))
    expect(await screen.findByText('QR นี้ใช้ไม่ได้แล้ว')).toBeTruthy()
    expect(screen.queryByText('โต๊ะ T01 · Standard')).toBeNull()
    expect(api.redeemQr).toHaveBeenCalledTimes(2)
  })

  it('shows menu images and creates an order with duplicate-submit protection', async () => {
    vi.mocked(api.getMenu).mockResolvedValue([{ id: 10, categoryId: 1, categoryName: 'ของทอด', name: 'ไก่ทอด', description: 'ทอดใหม่ทุกจาน', available: true, packageIds: [1], imageUrl: '/images/chicken.jpg' }])
    vi.mocked(api.redeemQr).mockResolvedValue({ sessionId: 1, packageId: 1, tableNumber: 'T01', sessionStatus: 'ACTIVE' })
    vi.mocked(api.getCustomerPackage).mockResolvedValue({ id: 1, name: 'Standard', price: 299, description: null, active: true })
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
    expect(api.getCategories).not.toHaveBeenCalled()
    expect(await screen.findByText('รับออเดอร์แล้ว')).toBeTruthy()
  })

  it('shows an actionable empty state when no menu is available', async () => {
    vi.mocked(api.redeemQr).mockResolvedValue({ sessionId: 1, packageId: 1, tableNumber: 'T01', sessionStatus: 'ACTIVE' })
    vi.mocked(api.getCustomerPackage).mockResolvedValue({ id: 1, name: 'Standard', price: 299, description: null, active: true })
    vi.mocked(api.getMenu).mockResolvedValue([]); vi.mocked(api.getOrders).mockResolvedValue([])
    renderPage()
    expect(await screen.findByText('ยังไม่มีเมนูในหมวดนี้')).toBeTruthy()
    expect(screen.getByText('ลองเลือกหมวดอื่นหรือสอบถามพนักงานได้ค่ะ')).toBeTruthy()
  })
})

it('confirms bill request once and disables ordering for the session', async () => {
  vi.mocked(api.redeemQr).mockResolvedValue(sessionA)
  mockSessionDetails()
  const pending = deferred<api.CustomerBillStatus>()
  vi.mocked(api.requestBill).mockReturnValue(pending.promise)
  renderPage()
  await screen.findByText('โต๊ะ T01 · Standard')
  fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  fireEvent.click(screen.getByRole('button', { name: 'ขอคิดบิล' }))
  const confirm = screen.getByRole('button', { name: 'ยืนยัน' })
  fireEvent.click(confirm); fireEvent.click(confirm)
  expect(api.requestBill).toHaveBeenCalledTimes(1)
  pending.resolve({ sessionId: 1, status: 'REQUESTED', requestedAt: '2026-10-06T06:00:00Z', dueAmount: 299, paidAmount: 0, bill: { sessionId: 1, subtotalAmount: 299, discountAmount: 0, totalAmount: 299 } })
  await screen.findByText('ขอคิดบิลแล้ว · รอพนักงานรับชำระ')
  expect(screen.getByText('ยอดค้างชำระ ฿299.00')).toBeTruthy()
  expect((screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }) as HTMLButtonElement).disabled).toBe(true)
  expect(screen.getByText('ยังไม่ได้เลือกเมนู')).toBeTruthy()
})
it('shows paid status and fails closed when bill lookup fails', async () => {
  vi.mocked(api.redeemQr).mockResolvedValue(sessionA); mockSessionDetails()
  vi.mocked(api.getBillStatus).mockResolvedValue({ sessionId: 1, status: 'PAID', requestedAt: '2026-10-06T06:00:00Z', dueAmount: 0, paidAmount: 299, bill: { sessionId: 1, subtotalAmount: 299, discountAmount: 0, totalAmount: 299 } })
  renderPage(); await screen.findByText('ชำระแล้ว · รอพนักงานปิดรอบกิน')
  expect(screen.getByText('ยอดรวม ฿299.00')).toBeTruthy()
  expect(screen.getByText('ยอดค้างชำระ ฿0.00')).toBeTruthy()
  expect(screen.getByText('ชำระแล้ว ฿299.00')).toBeTruthy()
  expect((screen.getByRole('button', { name: 'ขอคิดบิล' }) as HTMLButtonElement).disabled).toBe(true)
  cleanup()
  vi.mocked(api.getBillStatus).mockRejectedValue({ response: { data: { message: 'อ่านบิลไม่ได้' } } })
  renderPage(); await screen.findByText('อ่านบิลไม่ได้')
  expect(screen.queryByRole('button', { name: 'เพิ่ม ไก่ทอด' })).toBeNull()
})
