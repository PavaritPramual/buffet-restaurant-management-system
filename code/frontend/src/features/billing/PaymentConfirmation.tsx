import { Card, ErrorAlert, StatusBadge } from '../../components/common'
import type { PaymentResult } from '../../contracts/shared'
import { paymentMethodLabels } from './paymentLabels'

interface PaymentConfirmationProps {
  result: PaymentResult
}

export default function PaymentConfirmation({
  result,
}: PaymentConfirmationProps) {
  if (result.paymentStatus === 'FAILED') {
    return <ErrorAlert message="บันทึกการชำระไม่สำเร็จ กรุณาตรวจสอบรายการก่อนทำรายการใหม่" />
  }

  if (result.paymentStatus !== 'PAID') {
    return (
      <Card>
        <StatusBadge tone="warning">รอยืนยันการชำระ</StatusBadge>
        <p>ยังไม่มีผลยืนยันว่าชำระสำเร็จ</p>
      </Card>
    )
  }

  const paidAtText = new Date(result.paidAt ?? '').toLocaleString('th-TH', {
    timeZone: 'Asia/Bangkok',
  })

  return (
    <Card>
      <h2>ยืนยันการชำระเงิน</h2>
      <StatusBadge tone="success">ชำระแล้ว</StatusBadge>
      <p>เลขที่รายการชำระ: {result.paymentId}</p>
      <p>เลขที่รอบใช้บริการ: {result.sessionId}</p>
      <p>ยอดชำระจริง: {result.amount.toFixed(2)} บาท</p>
      <p>วิธีชำระ: {paymentMethodLabels[result.paymentMethod]}</p>
      <p>เวลาชำระ: {paidAtText}</p>
      <p>บันทึกการชำระแล้ว ยังต้องดำเนินการปิดโต๊ะผ่านระบบ</p>
    </Card>
  )
}