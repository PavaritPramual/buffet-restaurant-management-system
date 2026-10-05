import { useState } from 'react'
import { Button, Card, ConfirmDialog } from '../../components/common'
import { PAYMENT_METHODS } from '../../contracts/shared'
import type { PaymentMethod } from '../../contracts/shared'
import { paymentMethodLabels } from './paymentLabels'


interface PaymentFormProps {
  totalAmount: number
  busy: boolean
  disabled?: boolean
  onConfirm: (method: PaymentMethod) => void
}

export default function PaymentForm({
  totalAmount,
  busy,
  disabled = false,
  onConfirm,
}: PaymentFormProps) {
  const [method, setMethod] = useState<PaymentMethod>('CASH')
  const [confirmOpen, setConfirmOpen] = useState(false)

  const blocked = busy || disabled

  function handleConfirm() {
    if (blocked) return

    setConfirmOpen(false)
    onConfirm(method)
  }

  return (
    <Card>
      <h2>บันทึกการชำระเงิน</h2>
      <p>ยอดชำระ: {totalAmount.toFixed(2)} บาท</p>

      <fieldset disabled={blocked}>
        <legend>วิธีชำระเงิน</legend>

        {PAYMENT_METHODS.map((value) => (
          <Button
            key={value}
            type="button"
            variant={method === value ? 'primary' : 'secondary'}
            aria-pressed={method === value}
            disabled={blocked}
            onClick={() => setMethod(value)}
          >
            {paymentMethodLabels[value]}
          </Button>
        ))}
      </fieldset>

      <Button
        type="button"
        loading={busy}
        disabled={blocked}
        onClick={() => setConfirmOpen(true)}
      >
        บันทึกการชำระ
      </Button>

      <ConfirmDialog
        open={confirmOpen}
        title="ยืนยันการรับชำระ"
        description={`ยืนยันว่าได้รับชำระ ${totalAmount.toFixed(2)} บาท ด้วย${paymentMethodLabels[method]} แล้ว`}
        busy={busy}
        onCancel={() => setConfirmOpen(false)}
        onConfirm={handleConfirm}
      />
    </Card>
  )
}