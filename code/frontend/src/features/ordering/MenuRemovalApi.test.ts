import { afterEach, expect, it, vi } from 'vitest'
import { apiClient, customerApiClient } from '../../api/client'
import { menuRemovalGateway } from './api'

afterEach(() => vi.restoreAllMocks())

it('reads archived catalog through staff client with the normal page parameters', async () => {
  const get = vi.spyOn(apiClient, 'get').mockResolvedValue({ data: { content: [], totalElements: 0 } })
  const customerGet = vi.spyOn(customerApiClient, 'get')
  await menuRemovalGateway.listItems(2, 10, 'name,desc')
  expect(get).toHaveBeenCalledWith('/menu-items/archived', { params: { page: 2, size: 10, sort: 'name,desc' } })
  await menuRemovalGateway.listCategories()
  expect(get).toHaveBeenLastCalledWith('/menu-categories/archived')
  expect(customerGet).not.toHaveBeenCalled()
})

it('restores explicitly and removes with DELETE without force or history flags', async () => {
  const post = vi.spyOn(apiClient, 'post').mockResolvedValue({ data: { id: 4 } })
  const remove = vi.spyOn(apiClient, 'delete').mockResolvedValue({})
  expect(await menuRemovalGateway.restoreItem(4)).toEqual({ id: 4 })
  expect(post).toHaveBeenLastCalledWith('/menu-items/4/restore')
  await menuRemovalGateway.restoreCategory(7)
  expect(post).toHaveBeenLastCalledWith('/menu-categories/7/restore')
  await menuRemovalGateway.removeItem(4)
  expect(remove).toHaveBeenLastCalledWith('/menu-items/4')
  await menuRemovalGateway.removeCategory(7)
  expect(remove).toHaveBeenLastCalledWith('/menu-categories/7')
})

it('propagates server conflicts so the UI retains the item and confirmation', async () => {
  const conflict = { response: { status: 409, data: { message: 'คืนหมวดหมู่ก่อน' } } }
  vi.spyOn(apiClient, 'post').mockRejectedValue(conflict)
  await expect(menuRemovalGateway.restoreItem(4)).rejects.toBe(conflict)
})
