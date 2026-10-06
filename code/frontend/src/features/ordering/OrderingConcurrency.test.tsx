// @vitest-environment jsdom
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, useNavigate } from 'react-router-dom'
import { beforeEach, afterEach, expect, it, vi } from 'vitest'
import MenuAdminPage from './MenuAdminPage'
import CustomerOrderingPage from './CustomerOrderingPage'
import FoundationApp from '../../FoundationApp'
import * as api from './api'
import { authApi, stockApi, usersApi } from '../admin/api'
import { customerApiClient } from '../../api/client'

vi.mock('./api', async () => ({
  ...await vi.importActual<typeof import('./api')>('./api'),
  getCategories: vi.fn(), getBuffetPackages: vi.fn(), getMenuItems: vi.fn(),
  redeemQr: vi.fn(), getCustomerContext: vi.fn(), getCustomerPackage: vi.fn(),
  getBillStatus: vi.fn(), requestBill: vi.fn(), getMenu: vi.fn(), getOrders: vi.fn(), placeOrder: vi.fn(),
}))
vi.mock('../admin/api', async () => ({
  ...await vi.importActual<typeof import('../admin/api')>('../admin/api'),
  authApi: { current: vi.fn(), login: vi.fn(), logout: vi.fn() },
  stockApi: { overview: vi.fn(), history: vi.fn() }, usersApi: { list: vi.fn(), create: vi.fn() },
}))
beforeEach(() => { vi.mocked(api.getBillStatus).mockImplementation(async id => ({ sessionId: id, status: "NOT_REQUESTED", requestedAt: null, dueAmount: 299, paidAmount: 0, bill: { sessionId: id, subtotalAmount: 299, discountAmount: 0, totalAmount: 299 } })) })
afterEach(() => { cleanup(); vi.restoreAllMocks(); vi.resetAllMocks() })
const category = { id: 3, name: 'ของทอด' }
const buffetPackage = { id: 7, name: 'Standard', price: 299, description: null, active: true }
const item: api.MenuItem = { id: 10, categoryId: 3, categoryName: 'ของทอด', name: 'ไก่ทอด', description: null, available: true, packageIds: [7], imageUrl: null }
const page = (name: string): api.PageResult<api.MenuItem> => ({ content: [{ ...item, name }], page: 0, size: 10, totalElements: 1, totalPages: 1 })
function deferred<T>() {
  let resolve!: (v: T) => void
  let reject!: (v: unknown) => void
  const promise = new Promise<T>((done, fail) => { resolve = done; reject = fail })
  return { promise, resolve, reject }
}
function catalog() {
  vi.mocked(api.getCategories).mockResolvedValue([category])
  vi.mocked(api.getBuffetPackages).mockResolvedValue([buffetPackage])
}

it('keeps the latest sorted catalog when an older request completes last', async () => {
  catalog()
  const old = deferred<api.PageResult<api.MenuItem>>()
  vi.mocked(api.getMenuItems).mockReturnValueOnce(old.promise).mockResolvedValue(page('Latest descending result'))
  render(<MenuAdminPage />)
  fireEvent.change(screen.getByLabelText('เรียงเมนู'), { target: { value: 'name,desc' } })
  await screen.findByText('Latest descending result')
  await act(async () => old.resolve(page('Stale ascending result')))
  expect((screen.getByLabelText('เรียงเมนู') as HTMLSelectElement).value).toBe('name,desc')
  expect(screen.queryByText('Stale ascending result')).toBeNull()
  expect(screen.getByText('Latest descending result')).toBeTruthy()
})

it('does not lose a confirmed order when an earlier refresh returns afterwards', async () => {
  vi.mocked(api.getCustomerContext).mockResolvedValue({ sessionId: 1, packageId: 7, tableNumber: 'T01', sessionStatus: 'ACTIVE' })
  vi.mocked(api.getCustomerPackage).mockResolvedValue(buffetPackage)
  vi.mocked(api.getMenu).mockResolvedValue([item])
  const refresh = deferred<api.Order[]>()
  vi.mocked(api.getOrders).mockResolvedValueOnce([]).mockReturnValueOnce(refresh.promise)
  vi.mocked(api.placeOrder).mockResolvedValue({ orderId: 99, sessionId: 1, tableNumber: 'T01', status: 'RECEIVED', createdAt: '', items: [{ menuItemId: 10, name: 'ไก่ทอด', quantity: 1 }] })
  render(<MemoryRouter initialEntries={['/customer/qr']}><CustomerOrderingPage /></MemoryRouter>)
  fireEvent.click(await screen.findByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }))
  fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
  await screen.findByText('คำสั่งซื้อ #99')
  await act(async () => refresh.resolve([]))
  expect(screen.queryByText('คำสั่งซื้อ #99')).not.toBeNull()
})


it.each([
  ['/admin/menu', 'จัดการเมนูอาหาร'],
  ['/admin/menu/', 'จัดการเมนูอาหาร'],
  ['/ADMIN/MENU', 'จัดการเมนูอาหาร'],
  ['/admin/users', 'พนักงาน'],
  ['/admin/users/', 'พนักงาน'],
  ['/ADMIN/USERS', 'พนักงาน'],
])('blocks Supervisor from protected editor at %s', async (url, heading) => {
  catalog()
  vi.mocked(api.getMenuItems).mockResolvedValue(page('ไก่ทอด'))
  vi.mocked(stockApi.overview).mockResolvedValue([])
  vi.mocked(stockApi.history).mockResolvedValue([])
  vi.mocked(usersApi.list).mockResolvedValue([])
  vi.mocked(authApi.current).mockResolvedValue({ userId: 1, username: 'supervisor', displayName: 'Supervisor', role: 'SUPERVISOR' })
  render(<MemoryRouter initialEntries={[url]}><FoundationApp /></MemoryRouter>)
  await screen.findByText('Supervisor')
  expect(screen.queryByRole('heading', { name: heading })).toBeNull()
  expect(screen.queryByRole('link', { name: 'เมนูอาหาร' })).toBeNull()
})

it('ignores a stale catalog failure after the selected sort succeeds', async () => {
  catalog()
  const old = deferred<api.PageResult<api.MenuItem>>()
  vi.mocked(api.getMenuItems).mockReturnValueOnce(old.promise).mockResolvedValue(page('Latest descending result'))
  render(<MenuAdminPage />)
  fireEvent.change(screen.getByLabelText('เรียงเมนู'), { target: { value: 'name,desc' } })
  await screen.findByText('Latest descending result')
  await act(async () => old.reject({ response: { data: { message: 'Stale request failed' } } }))
  expect(screen.queryByRole('alert')).toBeNull()
})

it('keeps the latest page when an earlier page request completes afterwards', async () => {
  catalog()
  const firstPage = deferred<api.PageResult<api.MenuItem>>()
  vi.mocked(api.getMenuItems).mockResolvedValueOnce({ ...page('Initial page'), totalPages: 2, totalElements: 11 })
    .mockReturnValueOnce(firstPage.promise)
    .mockResolvedValueOnce({ ...page('Latest page zero'), totalPages: 2, totalElements: 11 })
  render(<MenuAdminPage />)
  await screen.findByText('Initial page')
  fireEvent.click(screen.getByRole('button', { name: 'ถัดไป' }))
  await waitFor(() => expect(api.getMenuItems).toHaveBeenLastCalledWith(1, 10, 'id,asc'))
  fireEvent.click(screen.getByRole('button', { name: 'ก่อนหน้า' }))
  await screen.findByText('Latest page zero')
  await act(async () => firstPage.resolve({ ...page('Stale page one'), page: 1, totalPages: 2, totalElements: 11 }))
  expect(screen.getByText('หน้า 1 / 2')).toBeTruthy()
  expect(screen.queryByText('Stale page one')).toBeNull()
})

function ScanControls() {
  const navigate = useNavigate()
  return <><button onClick={() => navigate('/customer/qr#token=B')}>Scan B</button><button onClick={() => navigate('/customer/qr#token=A')}>Scan A</button></>
}

it('rejects a reused A scan without displaying an obsolete session after A-B-A', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  vi.mocked(api.redeemQr).mockImplementation(actual.redeemQr)
  vi.mocked(api.getCustomerPackage).mockResolvedValue(buffetPackage)
  vi.mocked(api.getMenu).mockResolvedValue([item])
  vi.mocked(api.getOrders).mockResolvedValue([])
  const responseA = deferred<{ data: api.SessionContext }>()
  const responseB = deferred<{ data: api.SessionContext }>()
  let cookieSession = 0
  let requestsForA = 0
  const post = vi.spyOn(customerApiClient, 'post').mockImplementation(async (_url, body) => {
    if ((body as { token: string }).token === 'A') {
      if (++requestsForA > 1) throw { response: { status: 404, data: { message: 'QR นี้ใช้ไม่ได้แล้ว' } } }
      const result = await responseA.promise; cookieSession = 1; return result
    }
    const result = await responseB.promise; cookieSession = 2; return result
  })
  try {
    render(<MemoryRouter initialEntries={['/customer/qr#token=A']}><ScanControls /><CustomerOrderingPage /></MemoryRouter>)
    await waitFor(() => expect(post).toHaveBeenCalledTimes(1))
    fireEvent.click(screen.getByRole('button', { name: 'Scan B' }))
    await waitFor(() => expect(api.redeemQr).toHaveBeenCalledWith('B'))
    fireEvent.click(screen.getByRole('button', { name: 'Scan A' }))
    await waitFor(() => expect(api.redeemQr).toHaveBeenCalledTimes(3))
    await act(async () => responseA.resolve({ data: { sessionId: 1, packageId: 7, tableNumber: 'T01', sessionStatus: 'ACTIVE' } }))
    await waitFor(() => expect(post).toHaveBeenCalledTimes(2))
    expect(screen.queryByText('โต๊ะ T01 · Standard')).toBeNull()
    await act(async () => responseB.resolve({ data: { sessionId: 2, packageId: 8, tableNumber: 'T02', sessionStatus: 'ACTIVE' } }))
    expect((await screen.findByRole('alert')).textContent).toBe('QR นี้ใช้ไม่ได้แล้ว')
    expect(post).toHaveBeenCalledTimes(3)
    expect(api.getMenu).not.toHaveBeenCalled()
    expect(screen.queryByText('โต๊ะ T02 · Standard')).toBeNull()
    expect(cookieSession).toBe(2)
    expect(screen.queryByText('โต๊ะ T01 · Standard')).toBeNull()
  } finally { post.mockRestore() }
})

it('keeps loading until the latest catalog request completes', async () => {
  catalog()
  const old = deferred<api.PageResult<api.MenuItem>>()
  const latest = deferred<api.PageResult<api.MenuItem>>()
  vi.mocked(api.getMenuItems).mockReturnValueOnce(old.promise).mockReturnValueOnce(latest.promise)
  render(<MenuAdminPage />)
  fireEvent.change(screen.getByLabelText('เรียงเมนู'), { target: { value: 'name,desc' } })
  await act(async () => old.resolve(page('Stale ascending result')))
  expect(screen.getByText('กำลังโหลดข้อมูล…')).toBeTruthy()
  expect(screen.queryByText('Stale ascending result')).toBeNull()
  await act(async () => latest.resolve(page('Latest descending result')))
  expect(screen.getByText('Latest descending result')).toBeTruthy()
  expect(screen.queryByText('กำลังโหลดข้อมูล…')).toBeNull()
})

it('ignores an earlier refresh failure after placing an order and allows a fresh status update', async () => {
  vi.mocked(api.getCustomerContext).mockResolvedValue({ sessionId: 1, packageId: 7, tableNumber: 'T01', sessionStatus: 'ACTIVE' })
  vi.mocked(api.getCustomerPackage).mockResolvedValue(buffetPackage)
  vi.mocked(api.getMenu).mockResolvedValue([item])
  const refresh = deferred<api.Order[]>()
  const order: api.Order = { orderId: 99, sessionId: 1, tableNumber: 'T01', status: 'RECEIVED', createdAt: '', items: [{ menuItemId: 10, name: 'ไก่ทอด', quantity: 1 }] }
  vi.mocked(api.getOrders).mockResolvedValueOnce([]).mockReturnValueOnce(refresh.promise)
    .mockResolvedValueOnce([{ ...order, status: 'PREPARING' }])
  vi.mocked(api.placeOrder).mockResolvedValue(order)
  render(<MemoryRouter initialEntries={['/customer/qr']}><CustomerOrderingPage /></MemoryRouter>)
  fireEvent.click(await screen.findByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }))
  fireEvent.click(screen.getByRole('button', { name: 'เพิ่ม ไก่ทอด' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยันการสั่ง' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
  await screen.findByText('คำสั่งซื้อ #99')
  await act(async () => refresh.reject({ response: { data: { message: 'Stale refresh failed' } } }))
  expect(screen.queryByRole('alert')).toBeNull()
  expect(screen.getByText('คำสั่งซื้อ #99')).toBeTruthy()
  fireEvent.click(screen.getByRole('button', { name: 'อัปเดตสถานะคำสั่งซื้อ' }))
  expect(await screen.findByText('กำลังเตรียม')).toBeTruthy()
  expect(api.getOrders).toHaveBeenCalledTimes(3)
})

it.each([
  ['/admin/menu', 'จัดการเมนูอาหาร'],
  ['/admin/menu/', 'จัดการเมนูอาหาร'],
  ['/ADMIN/MENU', 'จัดการเมนูอาหาร'],
  ['/admin/users', 'พนักงาน'],
  ['/admin/users/', 'พนักงาน'],
  ['/ADMIN/USERS', 'พนักงาน'],
])('allows Manager into the protected editor at %s', async (url, heading) => {
  catalog()
  vi.mocked(api.getMenuItems).mockResolvedValue(page('ไก่ทอด'))
  vi.mocked(usersApi.list).mockResolvedValue([])
  vi.mocked(authApi.current).mockResolvedValue({ userId: 1, username: 'manager', displayName: 'Manager', role: 'MANAGER' })
  render(<MemoryRouter initialEntries={[url]}><FoundationApp /></MemoryRouter>)
  expect(await screen.findByRole('heading', { name: heading })).toBeTruthy()
})
