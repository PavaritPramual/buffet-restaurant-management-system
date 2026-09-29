import { useRef, useState } from 'react'
import './features/billing/billing-preview.css'
import type { FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { useParams } from 'react-router-dom'
import { previewBill } from './api/billing'
import type { BillSummary } from './api/billing'
import { Button, Card, ErrorAlert, PageHeader, TextField } from './components/common'

function errorMessage(error: unknown): string {
  if (isAxiosError(error)) {
    switch (error.response?.status) {
      case 400: return 'ไม่สามารถดูบิลได้ กรุณาตรวจสอบรหัสรอบและว่ารอบยังเปิดอยู่'
      case 401: return 'กรุณาเข้าสู่ระบบพนักงานก่อนดูบิล'
      case 403: return 'คุณไม่มีสิทธิ์ดูบิลนี้'
      case 404: return 'ไม่พบรอบการรับประทานนี้ กรุณาตรวจสอบรหัสรอบ'
      case 503: return 'ระบบดูบิลยังไม่พร้อมให้บริการ กรุณาลองใหม่ภายหลัง'
      default: return 'โหลดบิลไม่สำเร็จ กรุณาตรวจสอบการเชื่อมต่อแล้วลองอีกครั้ง'
    }
  }
  return 'โหลดบิลไม่สำเร็จ กรุณาลองอีกครั้ง'
}

export default function BillingPreviewPage() {
  const { sessionId: routeSessionId } = useParams<{ sessionId: string }>()
  // Remount when navigating between sessions so the previous bill cannot remain visible.
  return <BillingPreviewForm key={routeSessionId ?? 'manual'} routeSessionId={routeSessionId} />
}

function BillingPreviewForm({ routeSessionId }: { routeSessionId?: string }) {
  const [sessionInput, setSessionInput] = useState(routeSessionId ?? '')
  const [summary, setSummary] = useState<BillSummary | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const inFlight = useRef(false)

  async function handleCalculate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (inFlight.current) return
    setError('')
    setSummary(null)
    const input = sessionInput.trim()
    const sessionId = Number(input)
    if (!/^[1-9]\d*$/.test(input) || !Number.isSafeInteger(sessionId)) {
      setError('กรุณากรอกรหัสรอบเป็นจำนวนเต็มบวก')
      return
    }
    inFlight.current = true
    setLoading(true)
    try {
      const result = await previewBill(sessionId)
      if (result.sessionId !== sessionId) {
        setError('ข้อมูลบิลไม่ตรงกับรอบที่เลือก กรุณาลองใหม่')
        return
      }
      setSummary(result)
    } catch (error) {
      setError(errorMessage(error))
    } finally {
      inFlight.current = false
      setLoading(false)
    }
  }

  return (
    <main className="billing-preview-page">
      <PageHeader title="ดูบิลตามรอบการรับประทาน" description="ตรวจสอบยอดก่อนรับชำระเงิน" />
      <Card>
        <form onSubmit={handleCalculate} className="billing-preview-form" noValidate>
          <TextField
            label="รหัสรอบการรับประทาน"
            inputMode="numeric"
            value={sessionInput}
            readOnly={routeSessionId !== undefined}
            disabled={loading}
            onChange={(event) => {
              setSessionInput(event.target.value)
              setSummary(null)
              setError('')
            }}
          />
          <p>ราคาที่ใช้เป็นราคาตอนเปิดรอบ การดูบิลยังไม่มีการบันทึกชำระเงินหรือปิดรอบ</p>
          <Button type="submit" loading={loading}>ดูบิล</Button>
        </form>
      </Card>
      {error && <ErrorAlert message={error} />}
      {summary && (
        <Card>
          <h2>สรุปบิล</h2>
          <p>รหัสรอบ: {summary.sessionId}</p>
          <p>ยอดก่อนลด: {summary.subtotalNoneDiscount} บาท</p>
          <p>ส่วนลด: {summary.discountAmount} บาท</p>
          <p>ยอดหลังลด ก่อนปรับเศษ: {summary.totalBeforeRounding} บาท</p>
          {summary.roundingAdjustment !== 0 && (
            <p>ปรับเศษ: {summary.roundingAdjustment > 0 ? '+' : ''}{summary.roundingAdjustment} บาท</p>
          )}
          <p>ยอดสุทธิ: {summary.totalAmount.toFixed(2)} บาท</p>
        </Card>
      )}
    </main>
  )
}
