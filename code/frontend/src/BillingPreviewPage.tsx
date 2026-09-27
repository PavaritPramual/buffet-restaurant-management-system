import { useState } from 'react'
import { previewBill } from './api/billing'
import type { BillSummary } from './api/billing'
import type { BillingContext } from './contracts/shared'

const exampleContext: BillingContext = {
  sessionId: 1,
  packagePrice: 399,
  adultCount: 2,
  childCount: 1,
  discountContext: { percentage: 10 },
  sessionStatus: 'ACTIVE',
}

export default function BillingPreviewPage() {
  const [summary, setSummary] = useState<BillSummary | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleCalculate() {
    setLoading(true)
    setError('')
    setSummary(null)

    try {
      const result = await previewBill(exampleContext)
      setSummary(result)
    } catch {
      setError('คำนวณบิลไม่สำเร็จ กรุณาตรวจสอบข้อมูลหรือการเชื่อมต่อ')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="mx-auto max-w-xl space-y-6 p-6">
      <h1 className="text-2xl font-bold">ทดลองคำนวณบิล</h1>

      <p>แพ็กเกจ 399 บาท · ผู้ใหญ่ 2 คน · เด็ก 1 คน · ส่วนลด 10%</p>
      <p>ข้อมูลตัวอย่าง ยังไม่มีการบันทึกหรือรับชำระเงิน</p>

      <button
        type="button"
        onClick={handleCalculate}
        disabled={loading}
        className="rounded bg-emerald-700 px-4 py-2 text-white disabled:opacity-50"
      >
        {loading ? 'กำลังคำนวณ…' : 'คำนวณบิล'}
      </button>

      {error && <p role="alert">{error}</p>}

      {summary && (
        <section className="space-y-2 rounded border p-4">
          <h2 className="text-xl font-semibold">สรุปบิล</h2>
          <p>Session: {summary.sessionId}</p>
          <p>ยอดก่อนลด: {summary.subtotalNoneDiscount} บาท</p>
          <p>ส่วนลด: {summary.discountAmount} บาท</p>
          <p>ยอดหลังลด ก่อนปรับเศษ: {summary.totalBeforeRounding} บาท</p>

          {summary.roundingAdjustment !== 0 && (
            <p>
              ปรับเศษ: {summary.roundingAdjustment > 0 ? '+' : ''}
              {summary.roundingAdjustment} บาท
            </p>
          )}
          <p>ยอดสุทธิ: {summary.totalAmount.toFixed(2)} บาท</p>
        </section>
      )}
    </main>
  )
}