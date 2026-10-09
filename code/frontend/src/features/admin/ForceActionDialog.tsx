import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Button, ErrorAlert, TextField } from '../../components/common'

export default function ForceActionDialog({ title, description, confirmLabel, busy, error, onConfirm, onCancel }: {
  title: string; description: string; confirmLabel: string; busy: boolean; error: string
  onConfirm: (reason: string) => Promise<void>; onCancel: () => void
}) {
  const [reason, setReason] = useState('')
  const pending = useRef(false)
  async function submit(event: FormEvent) {
    event.preventDefault()
    if (pending.current || busy || !reason.trim()) return
    pending.current = true
    try { await onConfirm(reason.trim()) } finally { pending.current = false }
  }
  return <div className="ui-dialog-backdrop" role="presentation">
    <div className="ui-dialog" role="dialog" aria-modal="true" aria-labelledby="force-dialog-title">
      <h2 id="force-dialog-title">{title}</h2><p>{description}</p>
      <form onSubmit={(event) => void submit(event)}>
        <TextField label="เหตุผลในการบังคับดำเนินการ" required maxLength={500} autoFocus disabled={busy} value={reason} onChange={(event) => setReason(event.target.value)} />
        {error && <ErrorAlert message={error} />}
        <div className="ui-dialog-actions">
          <Button type="button" variant="secondary" onClick={onCancel} disabled={busy}>ยกเลิก</Button>
          <Button type="submit" variant="danger" loading={busy} disabled={!reason.trim()}>{confirmLabel}</Button>
        </div>
      </form>
    </div>
  </div>
}
