import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import MenuAdminPage from './MenuAdminPage'
import { getCategories, getMenuItems, saveMenuItem } from './api'

vi.mock('./api', () => ({
  getCategories: vi.fn(), getMenuItems: vi.fn(), saveMenuItem: vi.fn(),
  saveCategory: vi.fn(), deleteMenuItem: vi.fn(), deleteCategory: vi.fn(),
  getApiError: () => 'API error',
}))

beforeEach(() => {
  vi.mocked(getCategories).mockResolvedValue([{ id: 1, name: 'อาหารหลัก' }])
  vi.mocked(getMenuItems).mockResolvedValue({ content: [], page: 0, size: 10, totalElements: 0 })
  vi.mocked(saveMenuItem).mockResolvedValue({ id: 4, categoryId: 1, name: 'ผักรวม',
    available: true, packageIds: [1, 2], imageUrl: 'https://example.com/vegetables.jpg' })
})
afterEach(() => { cleanup(); vi.clearAllMocks() })

it('sends image URL and package IDs when adding a menu item', async () => {
  const user = userEvent.setup()
  render(<MenuAdminPage />)
  await screen.findByRole('option', { name: 'อาหารหลัก' })
  await user.type(screen.getByRole('textbox', { name: 'ชื่อเมนู' }), 'ผักรวม')
  await user.clear(screen.getByRole('textbox', { name: 'รหัสแพ็กเกจที่สั่งได้' }))
  await user.type(screen.getByRole('textbox', { name: 'รหัสแพ็กเกจที่สั่งได้' }), '1,2')
  await user.type(screen.getByRole('textbox', { name: 'URL ภาพเมนู (ถ้ามี)' }), 'https://example.com/vegetables.jpg')
  await user.click(screen.getByRole('button', { name: 'บันทึกเมนู' }))
  await waitFor(() => expect(saveMenuItem).toHaveBeenCalledWith(null, {
    categoryId: 1, name: 'ผักรวม', available: true, packageIds: [1, 2],
    imageUrl: 'https://example.com/vegetables.jpg',
  }))
})
