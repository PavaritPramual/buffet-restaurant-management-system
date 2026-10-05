// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import PaymentPanel from './PaymentPanel'
import { createPayment, findPayment } from '../../api/payments'

vi.mock('../../api/payments', () => ({ createPayment: vi.fn(), findPayment: vi.fn() }))
const bill = { sessionId: 12, subtotalAmount: 997.5, discountAmount: 0,
  totalAmount: 997.5 }
const paid = { paymentId: 8, sessionId: 12, amount: 997.5, paymentMethod: 'CASH' as const,
  paymentStatus: 'PAID' as const, paidAt: '2026-10-04T09:00:00+07:00' }
afterEach(cleanup)
beforeEach(() => { vi.resetAllMocks(); vi.mocked(findPayment).mockResolvedValue(null) })

describe('Payment status and recovery', () => {
  it('allows a confirmed manual retry only after a successful no-payment lookup', async () => {
    vi.mocked(createPayment).mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce(paid)
    render(<PaymentPanel bill={bill} enabled />)
    const pay = screen.getByRole('button', { name: 'บันทึกการชำระ' })
    await waitFor(() => expect((pay as HTMLButtonElement).disabled).toBe(false))
    fireEvent.click(pay)
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByRole('alert')
    expect((pay as HTMLButtonElement).disabled).toBe(true)
    fireEvent.click(screen.getByRole('button', { name: 'ตรวจสอบสถานะการชำระ' }))
    await waitFor(() => expect((pay as HTMLButtonElement).disabled).toBe(false))
    expect(createPayment).toHaveBeenCalledTimes(1)
    fireEvent.click(pay)
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByText('ชำระแล้ว')
    expect(createPayment).toHaveBeenCalledTimes(2)
  })

  it('keeps retries blocked when reconciliation fails', async () => {
    vi.mocked(createPayment).mockRejectedValueOnce(new Error('offline'))
    render(<PaymentPanel bill={bill} enabled />)
    const pay = screen.getByRole('button', { name: 'บันทึกการชำระ' })
    await waitFor(() => expect((pay as HTMLButtonElement).disabled).toBe(false))
    fireEvent.click(pay)
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByRole('alert')
    vi.mocked(findPayment).mockRejectedValueOnce(new Error('offline'))
    fireEvent.click(screen.getByRole('button', { name: 'ตรวจสอบสถานะการชำระ' }))
    await screen.findByText('ตรวจสถานะการชำระไม่ได้ กรุณาลองตรวจสอบอีกครั้ง')
    expect((pay as HTMLButtonElement).disabled).toBe(true)
    expect(createPayment).toHaveBeenCalledTimes(1)
  })

  it('displays the recorded amount even when the preview amount differs', async () => {
    vi.mocked(findPayment).mockResolvedValue({ ...paid, amount: 800 })
    render(<PaymentPanel bill={bill} enabled />)
    await screen.findByText('ยอดชำระจริง: 800.00 บาท')
    expect(createPayment).not.toHaveBeenCalled()
  })

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
