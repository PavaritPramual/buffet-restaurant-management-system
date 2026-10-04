import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import PaymentPanel from './PaymentPanel'
import { createPayment, findPayment } from '../../api/payments'

vi.mock('../../api/payments', () => ({ createPayment: vi.fn(), findPayment: vi.fn() }))
const bill = { sessionId: 12, subtotalNoneDiscount: 997.5, discountAmount: 0,
  totalBeforeRounding: 997.5, roundingAdjustment: 0, totalAmount: 997.5 }
const paid = { paymentId: 8, sessionId: 12, paymentMethod: 'CASH' as const,
  paymentStatus: 'PAID' as const, paidAt: '2026-10-04T09:00:00+07:00' }
afterEach(cleanup)
beforeEach(() => { vi.resetAllMocks(); vi.mocked(findPayment).mockResolvedValue(null) })

describe('Payment status and recovery', () => {
  it('loads an existing payment without posting again', async () => {
    vi.mocked(findPayment).mockResolvedValue(paid)
    render(<PaymentPanel bill={bill} enabled />)
    expect(await screen.findByText('ชำระแล้ว')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'บันทึกการชำระ' })).toBeNull()
    expect(createPayment).not.toHaveBeenCalled()
  })

  it('blocks payment when the initial status cannot be read', async () => {
    vi.mocked(findPayment).mockRejectedValue(new Error('offline'))
    render(<PaymentPanel bill={bill} enabled />)
    await screen.findByRole('alert')
    expect((screen.getByRole('button', { name: 'บันทึกการชำระ' }) as HTMLButtonElement).disabled).toBe(true)
    expect(createPayment).not.toHaveBeenCalled()
  })

  it('recovers an ambiguous payment by reading its status without reposting', async () => {
    vi.mocked(createPayment).mockRejectedValue(new Error('connection lost'))
    render(<PaymentPanel bill={bill} enabled />)
    const pay = screen.getByRole('button', { name: 'บันทึกการชำระ' })
    await waitFor(() => expect((pay as HTMLButtonElement).disabled).toBe(false))
    fireEvent.click(pay)
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByRole('alert')
    vi.mocked(findPayment).mockResolvedValue(paid)
    fireEvent.click(screen.getByRole('button', { name: 'ตรวจสอบสถานะการชำระ' }))
    await screen.findByText('ชำระแล้ว')
    expect(createPayment).toHaveBeenCalledTimes(1)
    expect(createPayment).toHaveBeenCalledWith({ sessionId: 12, paymentMethod: 'CASH' })
  })

  it('rejects a status response for another session', async () => {
    vi.mocked(findPayment).mockResolvedValue({ ...paid, sessionId: 99 })
    render(<PaymentPanel bill={bill} enabled />)
    await screen.findByRole('alert')
    expect(screen.queryByText('ชำระแล้ว')).toBeNull()
    expect(createPayment).not.toHaveBeenCalled()
  })
})
