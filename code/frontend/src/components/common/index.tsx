import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from 'react'
import type { OrderStatus } from '../../contracts/shared'

export function Button({ variant = 'primary', size = 'md', loading = false, children, disabled, className = '', ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' | 'danger' | 'ghost'; size?: 'sm' | 'md' | 'lg'; loading?: boolean }) {
  return <button className={`ui-button ${variant} ${size} ${className}`} disabled={disabled || loading} {...props}>{loading ? 'กำลังดำเนินการ…' : children}</button>
}
export function TextField({ label, error, ...props }: InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string }) {
  return <label className="ui-field"><span>{label}</span><input aria-invalid={Boolean(error)} {...props} />{error && <small className="ui-field-error">{error}</small>}</label>
}
export function SelectField({ label, children, ...props }: SelectHTMLAttributes<HTMLSelectElement> & { label: string; children: ReactNode }) {
  return <label className="ui-field"><span>{label}</span><select {...props}>{children}</select></label>
}
const statusLabels: Record<OrderStatus, string> = { RECEIVED: 'รับออเดอร์แล้ว', PREPARING: 'กำลังเตรียม', READY: 'พร้อมเสิร์ฟ', SERVED: 'เสิร์ฟแล้ว' }
export function StatusBadge({ status }: { status: OrderStatus }) { return <span className={`ui-badge status-${status.toLowerCase()}`}>{statusLabels[status]}</span> }
export function Card({ children, className = '' }: { children: ReactNode; className?: string }) { return <section className={`ui-card ${className}`}>{children}</section> }
export function PageHeader({ eyebrow, title, description, action }: { eyebrow?: string; title: string; description?: string; action?: ReactNode }) {
  return <header className="ui-page-header"><div>{eyebrow && <span>{eyebrow}</span>}<h1>{title}</h1>{description && <p>{description}</p>}</div>{action}</header>
}
export function LoadingState({ label = 'กำลังโหลดข้อมูล…' }: { label?: string }) { return <div className="ui-state" role="status">{label}</div> }
export function EmptyState({ title, description }: { title: string; description?: string }) { return <div className="ui-state"><strong>{title}</strong>{description && <p>{description}</p>}</div> }
export function ErrorAlert({ message }: { message: string }) { return <div className="ui-alert" role="alert">{message}</div> }
export function ConfirmDialog({ open, title, description, busy, onConfirm, onCancel }: { open: boolean; title: string; description: string; busy?: boolean; onConfirm: () => void; onCancel: () => void }) {
  if (!open) return null
  return <div className="ui-dialog-backdrop" role="presentation"><div className="ui-dialog" role="dialog" aria-modal="true" aria-labelledby="dialog-title"><h2 id="dialog-title">{title}</h2><p>{description}</p><div className="ui-dialog-actions"><Button variant="secondary" onClick={onCancel} disabled={busy}>ยกเลิก</Button><Button variant="danger" onClick={onConfirm} loading={busy}>ยืนยัน</Button></div></div></div>
}
export function DataTable({ headers, children }: { headers: string[]; children: ReactNode }) { return <div className="ui-table-wrap"><table className="ui-table"><thead><tr>{headers.map((header) => <th key={header}>{header}</th>)}</tr></thead><tbody>{children}</tbody></table></div> }
