// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom'
import StockPage from './StockPage'
import UsersPage from './UsersPage'
import { stockApi, usersApi } from './api'
import type { StockItem, StockTransaction, UserContext, UserRecord } from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return {
    ...actual,
    stockApi: { overview: vi.fn(), history: vi.fn(), stockIn: vi.fn(), adjust: vi.fn() },
    usersApi: { list: vi.fn(), create: vi.fn() },
  }
})

const item: StockItem = {
  id: 4, sku: 'RICE-01', name: 'ข้าวหอมมะลิ', unit: 'กก.', quantity: 12,
  lowStockThreshold: 3, updatedAt: '2026-09-30T10:00:00Z',
}
const history: StockTransaction[] = [{
  id: 8, stockItemId: 4, itemName: 'ข้าวหอมมะลิ', transactionType: 'IN', quantityDelta: 2,
  balanceAfter: 12, reason: 'รับจากผู้ขาย', actorUsername: 'manager', createdAt: '2026-09-30T10:00:00Z',
}]
const manager: UserContext = { userId: 1, username: 'manager', displayName: 'ผู้จัดการ', role: 'MANAGER' }
const staff: UserContext = { userId: 2, username: 'staff', displayName: 'พนักงาน', role: 'SERVICE_STAFF' }
const account: UserRecord = { id: 2, username: 'staff', displayName: 'พนักงานบริการ', email: null, role: 'SERVICE_STAFF' }

afterEach(() => { cleanup(); vi.clearAllMocks() })

function renderInShell(page: React.ReactNode, user: UserContext) {
  function ContextOutlet() { return <Outlet context={user} /> }
  return render(
    <MemoryRouter initialEntries={['/admin']}>
      <Routes><Route path="/admin" element={<ContextOutlet />}><Route index element={page} /></Route></Routes>
    </MemoryRouter>,
  )
}

describe('Admin stock and user pages', () => {
  it('shows stock history and permits supervisor stock-in with a required reason', async () => {
    vi.mocked(stockApi.overview).mockResolvedValue([item])
    vi.mocked(stockApi.history).mockResolvedValue(history)
    vi.mocked(stockApi.stockIn).mockResolvedValue(history[0])

    renderInShell(<StockPage />, { ...manager, role: 'SUPERVISOR' })

    expect(await screen.findByRole('heading', { name: 'ภาพรวมสต็อก' })).toBeTruthy()
    expect(screen.getByText('รับจากผู้ขาย')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'รับเข้า' }))
    fireEvent.change(screen.getByLabelText('จำนวนที่รับเข้า'), { target: { value: '2.5' } })
    fireEvent.change(screen.getByLabelText('เหตุผล'), { target: { value: 'รับจากผู้ขาย' } })
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกรายการ' }))

    await waitFor(() => expect(stockApi.stockIn).toHaveBeenCalledWith(4, '2.5', 'รับจากผู้ขาย'))
  })

  it('keeps stock read-only for service staff', async () => {
    vi.mocked(stockApi.overview).mockResolvedValue([item])
    vi.mocked(stockApi.history).mockResolvedValue(history)

    renderInShell(<StockPage />, staff)

    expect(await screen.findAllByText('ข้าวหอมมะลิ')).toHaveLength(2)
    expect(screen.queryByRole('button', { name: 'รับเข้า' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'ปรับยอด' })).toBeNull()
  })

  it('renders the user list and offers account creation to the manager page', async () => {
    vi.mocked(usersApi.list).mockResolvedValue([account])
    vi.mocked(usersApi.create).mockResolvedValue(account)

    renderInShell(<UsersPage />, manager)

    expect(await screen.findByRole('heading', { name: 'พนักงาน' })).toBeTruthy()
    expect(screen.getByRole('row', { name: /พนักงานบริการ/ })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'สร้างบัญชี' })).toBeTruthy()
  })
})