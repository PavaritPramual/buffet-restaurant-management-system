// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import MenuAdminPage from './MenuAdminPage'
import * as api from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return {
    ...actual,
    getCategories: vi.fn(),
    getBuffetPackages: vi.fn(),
    getMenuItems: vi.fn(),
    saveCategory: vi.fn(),
    saveMenuItem: vi.fn(),
    deleteCategory: vi.fn(),
    deleteMenuItem: vi.fn(),
  }
})

const category = { id: 3, name: 'ของทอด' }
const buffetPackage = { id: 7, name: 'Standard', price: 299, description: null, active: true }
const menuItem = { id: 10, categoryId: 3, categoryName: 'ของทอด', name: 'ไก่ทอด', description: null, available: true, packageIds: [7], imageUrl: null }

afterEach(() => { cleanup(); vi.clearAllMocks() })

function mockCatalog() {
  vi.mocked(api.getCategories).mockResolvedValue([category])
  vi.mocked(api.getBuffetPackages).mockResolvedValue([buffetPackage])
}

describe('MenuAdminPage', () => {
  it('loads active packages and submits selected package ids', async () => {
    mockCatalog()
    vi.mocked(api.getMenuItems).mockResolvedValue({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 })
    vi.mocked(api.saveMenuItem).mockResolvedValue(menuItem)

    render(<MenuAdminPage />)
    await screen.findByLabelText('Standard')
    fireEvent.change(screen.getByLabelText('ชื่อเมนู'), { target: { value: 'ไก่ทอด' } })
    fireEvent.click(screen.getByLabelText('Standard'))
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกเมนู' }))

    await waitFor(() => expect(api.saveMenuItem).toHaveBeenCalledWith(null, expect.objectContaining({
      categoryId: 3,
      name: 'ไก่ทอด',
      packageIds: [7],
    })))
    expect(api.getBuffetPackages).toHaveBeenCalledWith(true)
  })

  it('returns to the previous page after deleting the last item on the final page', async () => {
    mockCatalog()
    let deleted = false
    vi.mocked(api.deleteMenuItem).mockImplementation(async () => { deleted = true })
    vi.mocked(api.getMenuItems).mockImplementation(async (page) => {
      if (page === 1) return { content: [menuItem], page: 1, size: 10, totalElements: 11, totalPages: 2 }
      return { content: [], page: 0, size: 10, totalElements: deleted ? 10 : 11, totalPages: deleted ? 1 : 2 }
    })

    render(<MenuAdminPage />)
    await screen.findByText('หน้า 1 / 2')
    fireEvent.click(screen.getByRole('button', { name: 'ถัดไป' }))
    await screen.findByText('หน้า 2 / 2')
    const menuRow = screen.getByText('ไก่ทอด').closest('tr')
    expect(menuRow).not.toBeNull()
    fireEvent.click(within(menuRow!).getByRole('button', { name: 'ลบ' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))

    expect(await screen.findByText('หน้า 1 / 1')).toBeTruthy()
    expect(api.deleteMenuItem).toHaveBeenCalledWith(10)
  })
})
