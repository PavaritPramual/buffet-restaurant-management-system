// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom'
import StockPage from './StockPage'
import UsersPage from './UsersPage'
import { removedMessage, stockApi, usersApi } from './api'
import type { StockItem, UserContext, UserRecord } from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return {
    ...actual,
    stockApi: { overview: vi.fn(), history: vi.fn(), stockIn: vi.fn(), adjust: vi.fn(), archived: vi.fn(), remove: vi.fn(), restore: vi.fn(), setActive: vi.fn() },
    usersApi: { list: vi.fn(), create: vi.fn(), updateProfile: vi.fn(), archived: vi.fn(), remove: vi.fn(), restore: vi.fn(), setActive: vi.fn() },
  }
})

const item: StockItem = {
  id: 4, sku: 'RICE-01', name: 'ข้าวหอมมะลิ', unit: 'กก.', quantity: 12, lowStockThreshold: 3,
  openingTargetStock: 20, shortfall: 8, active: true, archivedAt: null, updatedAt: '2026-09-30T10:00:00Z',
}
const archivedItem: StockItem = { ...item, id: 9, sku: 'OLD-01', name: 'วัตถุดิบเก่า', active: false, archivedAt: '2026-10-01T10:00:00Z' }
const manager: UserContext = { userId: 1, username: 'manager', displayName: 'ผู้จัดการ', role: 'MANAGER' }
const staff: UserRecord = { id: 2, username: 'staff', displayName: 'พนักงานบริการ', email: null, role: 'SERVICE_STAFF', firstName: 'ก', lastName: 'ข', phoneNumber: null, active: true, archivedAt: null }
const self: UserRecord = { ...staff, id: 1, username: 'manager', displayName: 'ผู้จัดการ', role: 'MANAGER' }

beforeEach(() => {
  vi.mocked(stockApi.overview).mockResolvedValue([item])
  vi.mocked(stockApi.history).mockResolvedValue([])
  vi.mocked(stockApi.archived).mockResolvedValue([archivedItem])
  vi.mocked(usersApi.list).mockResolvedValue([self, staff])
  vi.mocked(usersApi.archived).mockResolvedValue([])
})
afterEach(() => { cleanup(); vi.clearAllMocks() })

function renderInShell(page: React.ReactNode, user: UserContext) {
  function ContextOutlet() { return <Outlet context={user} /> }
  return render(
    <MemoryRouter initialEntries={['/admin']}>
      <Routes><Route path="/admin" element={<ContextOutlet />}><Route index element={page} /></Route></Routes>
    </MemoryRouter>,
  )
}

describe('Stock archive UI', () => {
  it('confirms before removing, shows the contract notice, and lists archived items with restore', async () => {
    vi.mocked(stockApi.remove).mockResolvedValue(undefined)
    vi.mocked(stockApi.restore).mockResolvedValue({ ...archivedItem, archivedAt: null })
    renderInShell(<StockPage />, manager)
    expect(await screen.findByText('วัตถุดิบเก่า')).toBeTruthy()

    fireEvent.click(screen.getByRole('button', { name: 'ลบ/เก็บออก' }))
    expect(stockApi.remove).not.toHaveBeenCalled()
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await waitFor(() => expect(stockApi.remove).toHaveBeenCalledWith(4))
    expect(await screen.findByText(removedMessage)).toBeTruthy()

    fireEvent.click(screen.getByRole('button', { name: 'กู้คืน' }))
    await waitFor(() => expect(stockApi.restore).toHaveBeenCalledWith(9))
  })

  it('lets a manager archive, restore, activate and then stock in an item', async () => {
    let current: StockItem[] = [item]
    let archivedList: StockItem[] = []
    vi.mocked(stockApi.overview).mockImplementation(async () => current)
    vi.mocked(stockApi.archived).mockImplementation(async () => archivedList)
    vi.mocked(stockApi.remove).mockImplementation(async () => {
      archivedList = [{ ...item, active: false, archivedAt: '2026-10-01T10:00:00Z' }]
      current = []
    })
    vi.mocked(stockApi.restore).mockImplementation(async () => {
      archivedList = []
      current = [{ ...item, active: false }]
      return current[0]
    })
    vi.mocked(stockApi.setActive).mockImplementation(async () => {
      current = [{ ...item, active: true }]
      return current[0]
    })
    vi.mocked(stockApi.stockIn).mockResolvedValue({} as never)
    renderInShell(<StockPage />, manager)

    fireEvent.click(await screen.findByRole('button', { name: 'ลบ/เก็บออก' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    fireEvent.click(await screen.findByRole('button', { name: 'กู้คืน' }))

    const activate = await screen.findByRole('button', { name: 'เปิดใช้งาน' })
    expect((screen.getByRole('button', { name: 'รับเข้า' }) as HTMLButtonElement).disabled).toBe(true)
    fireEvent.click(activate)
    await waitFor(() => expect(stockApi.setActive).toHaveBeenCalledWith(4, true))

    await waitFor(() => expect((screen.getByRole('button', { name: 'รับเข้า' }) as HTMLButtonElement).disabled).toBe(false))
    expect(screen.queryByRole('button', { name: 'เปิดใช้งาน' })).toBeNull()
    fireEvent.click(screen.getByRole('button', { name: 'รับเข้า' }))
    fireEvent.change(screen.getByLabelText(/จำนวน/), { target: { value: '2' } })
    fireEvent.change(screen.getByLabelText(/เหตุผล|หมายเหตุ/), { target: { value: 'รับของ' } })
    fireEvent.submit(screen.getByLabelText(/จำนวน/).closest('form')!)
    await waitFor(() => expect(stockApi.stockIn).toHaveBeenCalledWith(4, 2, 'รับของ'))
  })

  it('hides remove and archived list from supervisors', async () => {
    renderInShell(<StockPage />, { ...manager, role: 'SUPERVISOR' })
    await screen.findByText('RICE-01')
    expect(screen.queryByRole('button', { name: /ลบ\/เก็บออก/ })).toBeNull()
    expect(screen.queryByText('รายการที่เก็บออก')).toBeNull()
    expect(stockApi.archived).not.toHaveBeenCalled()
  })

  it('shows a conflict from the server without hiding the row', async () => {
    vi.mocked(stockApi.remove).mockRejectedValue({ response: { data: { message: 'รายการนี้ถูกเก็บออกแล้ว' } } })
    renderInShell(<StockPage />, manager)
    fireEvent.click(await screen.findByRole('button', { name: 'ลบ/เก็บออก' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    expect((await screen.findByRole('alert')).textContent).toContain('รายการนี้ถูกเก็บออกแล้ว')
    expect(screen.getByText('RICE-01')).toBeTruthy()
  })
})

describe('User archive UI', () => {
  it('does not offer remove or disable for the signed-in manager', async () => {
    renderInShell(<UsersPage />, manager)
    const selfRow = await screen.findByRole('row', { name: /manager/ })
    expect(selfRow.textContent).not.toContain('ลบ/เก็บออก')
    const staffRow = screen.getByRole('row', { name: /staff/ })
    expect(staffRow.textContent).toContain('ลบ/เก็บออก')
  })

  it('removes another account after confirmation and restores archived accounts', async () => {
    vi.mocked(usersApi.remove).mockResolvedValue(undefined)
    vi.mocked(usersApi.restore).mockResolvedValue({ ...staff, active: false })
    vi.mocked(usersApi.archived).mockResolvedValue([{ ...staff, id: 7, username: 'old', displayName: 'บัญชีเก่า', active: false, archivedAt: '2026-10-01T10:00:00Z' }])
    renderInShell(<UsersPage />, manager)
    const staffRow = await screen.findByRole('row', { name: /staff/ })
    fireEvent.click(staffRow.querySelectorAll('button')[2])
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await waitFor(() => expect(usersApi.remove).toHaveBeenCalledWith(2))
    expect(await screen.findByText(removedMessage)).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: /กู้คืน/ }))
    await waitFor(() => expect(usersApi.restore).toHaveBeenCalledWith(7))
  })
})
