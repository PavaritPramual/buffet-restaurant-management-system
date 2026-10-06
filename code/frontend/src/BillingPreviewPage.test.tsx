// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { AxiosError } from 'axios'
import BillingPreviewPage from './BillingPreviewPage'
import { apiClient } from './api/client'

vi.mock('./api/client', () => ({ apiClient: { post: vi.fn(), get: vi.fn() } }))
const bill = { sessionId: 12, subtotalAmount: 997.5, discountAmount: 0,
  totalAmount: 997.5 }
function renderPage(path = '/billing/preview') {
  return render(<MemoryRouter initialEntries={[path]}><Routes>
    <Route path="/billing/preview" element={<BillingPreviewPage />} />
    <Route path="/staff/sessions/:sessionId/billing" element={<BillingPreviewPage />} />
  </Routes></MemoryRouter>)
}
function submit(id: string) {
  fireEvent.change(screen.getByLabelText('รหัสรอบการรับประทาน'), { target: { value: id } })
  fireEvent.click(screen.getByRole('button', { name: 'ดูบิล' }))
}
afterEach(cleanup)
beforeEach(() => {
  vi.resetAllMocks()
  const missing = new AxiosError('not found')
  Object.assign(missing, { response: { status: 404 } })
  vi.mocked(apiClient.get).mockImplementation(async (url) => {
    if (url === '/dining-sessions/12') return { data: { sessionId: 12, sessionStatus: 'ACTIVE', billRequestedAt: '2026-10-06T00:00:00Z' } }
    throw missing
  })
})
describe('Billing preview', () => {
  it('disables payment before a bill request even though preview succeeds', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: bill })
    vi.mocked(apiClient.get).mockResolvedValue({ data: { sessionId: 12, sessionStatus: 'ACTIVE', billRequestedAt: null } })
    renderPage()
    submit('12')
    await screen.findByText('ยอดสุทธิ: 997.50 บาท')
    expect((screen.getByRole('button', { name: 'บันทึกการชำระ' }) as HTMLButtonElement).disabled).toBe(true)
    expect(apiClient.get).not.toHaveBeenCalledWith('/payments/sessions/12')
  })
  it('sends only sessionId and displays the server total', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: bill })
    renderPage()
    submit('12')
    expect(await screen.findByText('ยอดสุทธิ: 997.50 บาท')).toBeTruthy()
    expect(apiClient.post).toHaveBeenCalledWith('/billing/preview', { sessionId: 12 })
    await waitFor(() => expect((screen.getByRole('button', { name: 'บันทึกการชำระ' }) as HTMLButtonElement).disabled).toBe(false))
    expect(apiClient.post).toHaveBeenCalledTimes(1)
  })
  it('rejects invalid input without calling the API', async () => {
    renderPage()
    submit('-1')
    expect(await screen.findByRole('alert')).toBeTruthy()
    expect(apiClient.post).not.toHaveBeenCalled()
  })
  it('uses the session from the staff route', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: bill })
    renderPage('/staff/sessions/12/billing')
    expect((screen.getByLabelText('รหัสรอบการรับประทาน') as HTMLInputElement).readOnly).toBe(true)
    fireEvent.click(screen.getByRole('button', { name: 'ดูบิล' }))
    await screen.findByText('ยอดสุทธิ: 997.50 บาท')
    expect(apiClient.post).toHaveBeenCalledWith('/billing/preview', { sessionId: 12 })
  })
  it('clears the previous bill when the selected ID changes', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: bill })
    renderPage()
    submit('12')
    await screen.findByText('ยอดสุทธิ: 997.50 บาท')
    fireEvent.change(screen.getByLabelText('รหัสรอบการรับประทาน'), { target: { value: '13' } })
    expect(screen.queryByText('สรุปบิล')).toBeNull()
  })
  it('does not display a response for a different session', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { ...bill, sessionId: 99 } })
    renderPage()
    submit('12')
    expect(await screen.findByText('ข้อมูลบิลไม่ตรงกับรอบที่เลือก กรุณาลองใหม่')).toBeTruthy()
    expect(screen.queryByText('สรุปบิล')).toBeNull()
  })
  it('explains provider unavailability without showing a fabricated bill', async () => {
    const error = new AxiosError('unavailable')
    Object.assign(error, { response: { status: 503 } })
    vi.mocked(apiClient.post).mockRejectedValue(error)
    renderPage()
    submit('12')
    expect(await screen.findByText('ระบบดูบิลยังไม่พร้อมให้บริการ กรุณาลองใหม่ภายหลัง')).toBeTruthy()
    await waitFor(() => expect((screen.getByRole('button', { name: 'ดูบิล' }) as HTMLButtonElement).disabled).toBe(false))
    expect(screen.queryByText('สรุปบิล')).toBeNull()
  })
})
