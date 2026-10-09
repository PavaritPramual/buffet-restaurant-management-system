import { describe, expect, it } from 'vitest'
import { getApiError } from './errors'

describe('getApiError', () => {
  it.each([
    'โต๊ะไม่ว่าง กรุณาเลือกโต๊ะที่พร้อมใช้งาน',
    'จำนวนผู้ใช้บริการเกินความจุของโต๊ะ (สูงสุด 4 คน)',
    'เมนูนี้มีประวัติการสั่งซื้อ จึงลบถาวรไม่ได้',
  ])('surfaces the backend message without rewriting it: %s', (message) => {
    expect(getApiError({ response: { data: { message } } })).toBe(message)
  })
})
