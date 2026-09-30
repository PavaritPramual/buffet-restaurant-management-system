// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import AdminShell from './AdminShell'
import { authApi } from './api'
import type { UserContext } from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return {
    ...actual,
    authApi: { current: vi.fn(), login: vi.fn(), logout: vi.fn() },
  }
})

const manager: UserContext = { userId: 1, username: 'manager', displayName: 'ผู้จัดการ', role: 'MANAGER' }
const staff: UserContext = { userId: 2, username: 'staff', displayName: 'พนักงาน', role: 'SERVICE_STAFF' }

afterEach(() => { cleanup(); vi.clearAllMocks() })

function renderShell(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/admin" element={<AdminShell />}>
          <Route path="stock" element={<p>หน้า stock</p>} />
          <Route path="users" element={<p>หน้าพนักงาน</p>} />
          <Route path="menu" element={<p>หน้าเมนู</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  )
}

describe('AdminShell', () => {
  it('shows manager navigation and the requested page after restoring a session', async () => {
    vi.mocked(authApi.current).mockResolvedValue(manager)

    renderShell('/admin/users')

    expect(await screen.findByText('หน้าพนักงาน')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'พนักงาน' })).toBeTruthy()
    expect(screen.getByRole('link', { name: 'เมนูอาหาร' })).toBeTruthy()
  })

  it('limits staff navigation to stock and redirects manager-only routes', async () => {
    vi.mocked(authApi.current).mockResolvedValue(staff)

    renderShell('/admin/users')

    expect(await screen.findByText('หน้า stock')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'สต็อก' })).toBeTruthy()
    expect(screen.queryByRole('link', { name: 'พนักงาน' })).toBeNull()
    expect(screen.queryByRole('link', { name: 'เมนูอาหาร' })).toBeNull()
  })

  it('logs in with the submitted credentials and exposes the role navigation', async () => {
    vi.mocked(authApi.current).mockRejectedValue(new Error('not signed in'))
    vi.mocked(authApi.login).mockResolvedValue(manager)

    renderShell('/admin/stock')
    fireEvent.change(await screen.findByLabelText('ชื่อผู้ใช้'), { target: { value: 'manager' } })
    fireEvent.change(screen.getByLabelText('รหัสผ่าน'), { target: { value: 'password123' } })
    fireEvent.click(screen.getByRole('button', { name: 'เข้าสู่ระบบ' }))

    await waitFor(() => expect(authApi.login).toHaveBeenCalledWith('manager', 'password123'))
    expect(await screen.findByText('หน้า stock')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'พนักงาน' })).toBeTruthy()
  })
})