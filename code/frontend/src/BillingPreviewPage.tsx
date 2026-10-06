import { useRef, useState } from 'react'
import './features/billing/billing-preview.css'
import type { FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { useNavigate, useParams } from 'react-router-dom'
import { previewBill } from './api/billing'
import type { BillSummary } from './api/billing'
import { Button, Card, ErrorAlert, PageHeader, TextField } from './components/common'
import PaymentPanel from './features/billing/PaymentPanel'
import { getDiningSession } from './features/staff-tables/api'

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
  const navigate = useNavigate()
  const [sessionInput, setSessionInput] = useState(routeSessionId ?? '')
  const [summary, setSummary] = useState<BillSummary | null>(null)
  const [paymentEnabled, setPaymentEnabled] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const inFlight = useRef(false)

  async function handleCalculate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (inFlight.current) return
    setError('')
    setSummary(null)
    setPaymentEnabled(false)
    const input = sessionInput.trim()
    const sessionId = Number(input)
    if (!/^[1-9]\d*$/.test(input) || !Number.isSafeInteger(sessionId)) {
      setError('กรุณากรอกรหัสรอบเป็นจำนวนเต็มบวก')
      return
    }
    inFlight.current = true
    setLoading(true)
    try {
      const [result, session] = await Promise.all([previewBill(sessionId), getDiningSession(sessionId)])
      if (result.sessionId !== sessionId || session.sessionId !== sessionId) {
        setError('ข้อมูลบิลไม่ตรงกับรอบที่เลือก กรุณาลองใหม่')
        return
      }
      setSummary(result)
      setPaymentEnabled(session.sessionStatus === 'ACTIVE' && !!session.billRequestedAt)
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
      {routeSessionId !== undefined && (
        <Button
          type="button"
          variant="secondary"
          onClick={() => navigate(`/staff/sessions/${routeSessionId}`)}
        >
          กลับไปรายละเอียดรอบกิน
        </Button>
      )}
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
              setPaymentEnabled(false)
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
          <p>ยอดก่อนลด: {summary.subtotalAmount} บาท</p>
          <p>ส่วนลด: {summary.discountAmount} บาท</p>
          <p>ยอดสุทธิ: {summary.totalAmount.toFixed(2)} บาท</p>
          {!paymentEnabled && <p>รับชำระได้หลังลูกค้าขอคิดบิล กรุณาให้ลูกค้าขอคิดบิลแล้วกดดูบิลอีกครั้ง</p>}
          <PaymentPanel
            key={summary.sessionId}
            bill={summary}
            enabled={paymentEnabled}
          />
        </Card>
      )}
    </main>
  )
}
