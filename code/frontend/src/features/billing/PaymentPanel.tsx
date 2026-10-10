import { useEffect, useRef, useState } from 'react'
import { isAxiosError } from 'axios'
import { createPayment, findPayment } from '../../api/payments'
import type { BillSummary } from '../../api/billing'
import type { PaymentMethod, PaymentResult } from '../../contracts/shared'
import { Button, ErrorAlert } from '../../components/common'
import PaymentForm from './PaymentForm'
import PaymentConfirmation from './PaymentConfirmation'

interface PaymentPanelProps {
  bill: BillSummary
  enabled?: boolean
}

export default function PaymentPanel({
  bill,
  enabled = false,
}: PaymentPanelProps) {
  const [busy, setBusy] = useState(false)
  const [attempted, setAttempted] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState<PaymentResult | null>(null)
  const requestStarted = useRef(false)
  const [checking, setChecking] = useState(true)
  const [checked, setChecked] = useState(false)

  useEffect(() => {
    let active = true
    if (!enabled) return () => { active = false }
    findPayment(bill.sessionId).then((existing) => {
      if (!active) return
      if (existing && existing.sessionId !== bill.sessionId) throw new Error('Session mismatch')
      setResult(existing)
      setChecked(true)
    }).catch(() => {
      if (active) setError('ตรวจสถานะการชำระไม่ได้ กรุณากดตรวจสอบสถานะอีกครั้ง')
    }).finally(() => {
      if (active) setChecking(false)
    })
    return () => { active = false }
  }, [bill.sessionId, enabled])

  async function checkPayment() {
    if (checking || busy) return
    setChecking(true)
    setError('')
    try {
      const existing = await findPayment(bill.sessionId)
      if (existing && existing.sessionId !== bill.sessionId) throw new Error('Session mismatch')
      setResult(existing)
      setChecked(true)
      if (!existing && attempted) {
        // Only an explicit successful status check unlocks a manual retry.
        requestStarted.current = false
        setAttempted(false)
      }
    } catch {
      setChecked(false)
      setError('ตรวจสถานะการชำระไม่ได้ กรุณาลองตรวจสอบอีกครั้ง')
    } finally {
      setChecking(false)
    }
  }

  async function handlePayment(method: PaymentMethod) {
    if (!enabled || !checked || checking || result || requestStarted.current) return

    requestStarted.current = true
    setAttempted(true)
    setBusy(true)
    setError('')

    try {
      const response = await createPayment({
        sessionId: bill.sessionId,
        paymentMethod: method,
      })

      if (
        response.sessionId !== bill.sessionId ||
        response.paymentMethod !== method
      ) {
        setError('ผลการชำระไม่ตรงกับคำขอ กรุณาตรวจสอบรายการก่อนทำต่อ')
        return
      }

      setResult(response)
    } catch (cause) {
      if (isAxiosError(cause) && cause.response?.status === 409) {
        setError('ข้อมูลการชำระขัดแย้งกับรายการเดิม กรุณาตรวจสอบสถานะการชำระ')
      } else if (isAxiosError(cause) && cause.response?.status === 503) {
        setError('ระบบรับชำระยังไม่พร้อมใช้งาน กรุณาติดต่อผู้ดูแลระบบ')
      } else {
        setError('ยังยืนยันผลการชำระไม่ได้ กรุณาตรวจสอบรายการก่อนส่งคำขอซ้ำ')
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      {!enabled && <p>การรับชำระยังไม่เปิดใช้งาน</p>}

      {error && <ErrorAlert message={error} />}
      {enabled && (
        <Button type="button" variant="secondary" loading={checking}
          disabled={busy} onClick={checkPayment}>
          ตรวจสอบสถานะการชำระ
        </Button>
      )}

      {result ? (
        <PaymentConfirmation result={result} />
      ) : (
        <PaymentForm
          totalAmount={bill.totalAmount}
          busy={busy}
          disabled={!enabled || attempted || checking || !checked}
          onConfirm={handlePayment}
        />
      )}
    </>
  )
}
