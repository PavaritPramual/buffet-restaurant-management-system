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
  it('resets pagination when sorting changes and sends the selected API sort', async () => {
    mockCatalog()
    vi.mocked(api.getMenuItems).mockResolvedValue({ content: [menuItem], page: 0, size: 10, totalElements: 11, totalPages: 2 })
    render(<MenuAdminPage />)
    await screen.findByText('หน้า 1 / 2')
    fireEvent.click(screen.getByRole('button', { name: 'ถัดไป' }))
    await screen.findByText('หน้า 2 / 2')
    fireEvent.change(screen.getByLabelText('เรียงเมนู'), { target: { value: 'name,desc' } })
    await waitFor(() => expect(api.getMenuItems).toHaveBeenLastCalledWith(0, 10, 'name,desc'))
    expect(screen.getByText('หน้า 1 / 2')).toBeTruthy()
    expect(screen.getAllByText('Standard').length).toBeGreaterThan(0)
  })

  it('allows retrying a failed catalog load and clears the stale error', async () => {
    mockCatalog()
    vi.mocked(api.getMenuItems).mockRejectedValueOnce({ response: { data: { message: 'โหลดรายการไม่สำเร็จ' } } })
      .mockResolvedValue({ content: [menuItem], page: 0, size: 10, totalElements: 1, totalPages: 1 })
    render(<MenuAdminPage />)
    expect(await screen.findByRole('alert')).toHaveProperty('textContent', 'โหลดรายการไม่สำเร็จ')
    fireEvent.click(screen.getByRole('button', { name: 'โหลดข้อมูลใหม่' }))
    expect(await screen.findByText('ไก่ทอด')).toBeTruthy()
    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('retains a category save error and draft across a successful catalog refresh', async () => {
    mockCatalog()
    vi.mocked(api.getMenuItems).mockResolvedValue({ content: [menuItem], page: 0, size: 10, totalElements: 1, totalPages: 1 })
    vi.mocked(api.saveCategory).mockRejectedValueOnce({ response: { data: { message: 'บันทึกหมวดหมู่ไม่สำเร็จ' } } })
      .mockResolvedValue(category)
    render(<MenuAdminPage />)
    await screen.findByText('ไก่ทอด')
    fireEvent.change(screen.getByLabelText('ชื่อหมวดหมู่'), { target: { value: 'ของทอดใหม่' } })
    fireEvent.click(screen.getByRole('button', { name: 'เพิ่มหมวดหมู่' }))
    expect((await screen.findByRole('alert')).textContent).toBe('บันทึกหมวดหมู่ไม่สำเร็จ')
    expect(screen.queryByRole('button', { name: 'โหลดข้อมูลใหม่' })).toBeNull()
    fireEvent.change(screen.getByLabelText('เรียงเมนู'), { target: { value: 'name,desc' } })
    await waitFor(() => expect(api.getMenuItems).toHaveBeenLastCalledWith(0, 10, 'name,desc'))
    expect(screen.getByRole('alert').textContent).toBe('บันทึกหมวดหมู่ไม่สำเร็จ')
    expect((screen.getByLabelText('ชื่อหมวดหมู่') as HTMLInputElement).value).toBe('ของทอดใหม่')
    fireEvent.click(screen.getByRole('button', { name: 'เพิ่มหมวดหมู่' }))
    await screen.findByText('บันทึกหมวดหมู่แล้ว')
    expect(screen.queryByRole('alert')).toBeNull()
    expect(api.saveCategory).toHaveBeenCalledTimes(2)
  })

  it('retries only the catalog when saving succeeded but its subsequent reload failed', async () => {
    mockCatalog()
    const result = { content: [menuItem], page: 0, size: 10, totalElements: 1, totalPages: 1 }
    vi.mocked(api.getMenuItems).mockResolvedValueOnce(result)
      .mockRejectedValueOnce({ response: { data: { message: 'โหลดข้อมูลหลังบันทึกไม่สำเร็จ' } } })
      .mockResolvedValue(result)
    vi.mocked(api.saveCategory).mockResolvedValue(category)
    render(<MenuAdminPage />)
    await screen.findByText('ไก่ทอด')
    fireEvent.change(screen.getByLabelText('ชื่อหมวดหมู่'), { target: { value: 'ของทอดใหม่' } })
    fireEvent.click(screen.getByRole('button', { name: 'เพิ่มหมวดหมู่' }))
    await screen.findByText('บันทึกหมวดหมู่แล้ว')
    expect(screen.getAllByRole('alert')).toHaveLength(1)
    expect((screen.getByLabelText('ชื่อหมวดหมู่') as HTMLInputElement).value).toBe('')
    fireEvent.click(screen.getByRole('button', { name: 'โหลดข้อมูลใหม่' }))
    await waitFor(() => expect(screen.queryByRole('alert')).toBeNull())
    expect(api.saveCategory).toHaveBeenCalledTimes(1)
  })

  it('retains the item draft after a failed save without offering catalog retry', async () => {
    mockCatalog()
    vi.mocked(api.getMenuItems).mockResolvedValue({ content: [menuItem], page: 0, size: 10, totalElements: 1, totalPages: 1 })
    vi.mocked(api.saveMenuItem).mockRejectedValue({ response: { data: { message: 'บันทึกเมนูไม่สำเร็จ' } } })
    render(<MenuAdminPage />)
    await screen.findByText('ไก่ทอด')
    fireEvent.change(screen.getByLabelText('ชื่อเมนู'), { target: { value: 'เมนูใหม่' } })
    fireEvent.click(screen.getByLabelText('Standard'))
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกเมนู' }))
    expect((await screen.findByRole('alert')).textContent).toBe('บันทึกเมนูไม่สำเร็จ')
    expect(screen.queryByRole('button', { name: 'โหลดข้อมูลใหม่' })).toBeNull()
    expect((screen.getByLabelText('ชื่อเมนู') as HTMLInputElement).value).toBe('เมนูใหม่')
    expect((screen.getByLabelText('Standard') as HTMLInputElement).checked).toBe(true)
  })

  it('keeps a failed delete available for confirmation again without catalog retry', async () => {
    mockCatalog()
    vi.mocked(api.getMenuItems).mockResolvedValue({ content: [menuItem], page: 0, size: 10, totalElements: 1, totalPages: 1 })
    vi.mocked(api.deleteMenuItem).mockRejectedValueOnce({ response: { data: { message: 'ลบเมนูไม่สำเร็จ' } } })
      .mockResolvedValue(undefined)
    render(<MenuAdminPage />)
    const row = (await screen.findByText('ไก่ทอด')).closest('tr')!
    fireEvent.click(within(row).getByRole('button', { name: 'ลบ' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    expect((await screen.findByRole('alert')).textContent).toBe('ลบเมนูไม่สำเร็จ')
    expect(screen.queryByRole('button', { name: 'โหลดข้อมูลใหม่' })).toBeNull()
    expect(screen.getByRole('dialog')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByText('ลบข้อมูลแล้ว')
    expect(screen.queryByRole('alert')).toBeNull()
    expect(api.deleteMenuItem).toHaveBeenCalledTimes(2)
  })

  it('shows package validation as a form error without loading retry', async () => {
    mockCatalog()
    vi.mocked(api.getMenuItems).mockResolvedValue({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 })
    render(<MenuAdminPage />)
    await screen.findByLabelText('Standard')
    fireEvent.change(screen.getByLabelText('ชื่อเมนู'), { target: { value: 'เมนูใหม่' } })
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกเมนู' }))
    expect((await screen.findByRole('alert')).textContent).toBe('กรุณาเลือกแพ็กเกจอย่างน้อย 1 รายการ')
    expect(screen.queryByRole('button', { name: 'โหลดข้อมูลใหม่' })).toBeNull()
    expect(api.saveMenuItem).not.toHaveBeenCalled()
  })

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
    window.scrollTo = vi.fn()
    fireEvent.click(within(menuRow!).getByRole('button', { name: 'แก้ไข' }))
    expect(screen.getByRole('heading', { name: 'แก้ไขเมนู' })).toBeTruthy()
    fireEvent.click(within(menuRow!).getByRole('button', { name: 'ลบ' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))

    expect(await screen.findByText('หน้า 1 / 1')).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'เพิ่มเมนู' })).toBeTruthy()
    expect(api.deleteMenuItem).toHaveBeenCalledWith(10)
  })

  it('selects a remaining category and clears category editing after deletion', async () => {
    let categories = [category, { id: 4, name: 'เครื่องดื่ม' }]
    vi.mocked(api.getCategories).mockImplementation(async () => categories)
    vi.mocked(api.getBuffetPackages).mockResolvedValue([buffetPackage])
    vi.mocked(api.getMenuItems).mockResolvedValue({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 })
    vi.mocked(api.deleteCategory).mockImplementation(async () => { categories = [{ id: 4, name: 'เครื่องดื่ม' }] })

    render(<MenuAdminPage />)
    const categoryRow = (await screen.findByText('ของทอด', { selector: 'span' })).closest('li')
    expect(categoryRow).not.toBeNull()
    fireEvent.click(within(categoryRow!).getByRole('button', { name: 'แก้ไข' }))
    fireEvent.click(within(categoryRow!).getByRole('button', { name: 'ลบ' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))

    await waitFor(() => expect((screen.getByLabelText('หมวดหมู่') as HTMLSelectElement).value).toBe('4'))
    expect((screen.getByLabelText('ชื่อหมวดหมู่') as HTMLInputElement).value).toBe('')
    expect(screen.getByRole('button', { name: 'เพิ่มหมวดหมู่' })).toBeTruthy()
  })
})
