import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getErrorMessage, removedMessage, usersApi } from './api'
import { roleLabels } from './api'
import type { UserRecord, UserRole, UserContext, UserProfileInput } from './api'
import { useOutletContext } from 'react-router-dom'
import { ConfirmDialog } from '../../components/common'

const roles: UserRole[] = ['SERVICE_STAFF', 'KITCHEN_STAFF', 'SUPERVISOR', 'MANAGER']

export default function UsersPage() {
  const currentUser = useOutletContext<UserContext>()
  const [users, setUsers] = useState<UserRecord[]>([])
  const [archived, setArchived] = useState<UserRecord[]>([])
  const [removing, setRemoving] = useState<UserRecord | null>(null)
  const [notice, setNotice] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [email, setEmail] = useState('')
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [draft, setDraft] = useState<UserProfileInput>({ firstName: '', lastName: '', phoneNumber: '' })
  const [role, setRole] = useState<UserRole>('SERVICE_STAFF')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  async function loadUsers() {
    setLoading(true)
    try {
      const [active, archivedUsers] = await Promise.all([usersApi.list(), usersApi.archived()])
      setUsers(active)
      setArchived(archivedUsers)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void loadUsers() }, [])

  async function runAction(action: () => Promise<unknown>, successMessage = '') {
    setSaving(true)
    setError('')
    setNotice('')
    try {
      await action()
      setNotice(successMessage)
      await loadUsers()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setRemoving(null)
      setSaving(false)
    }
  }

  async function createUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      await usersApi.create({ username, password, displayName, email: email.trim() || null, role, firstName: firstName.trim(), lastName: lastName.trim(), phoneNumber: phoneNumber.trim() })
      setUsername('')
      setPassword('')
      setDisplayName('')
      setEmail('')
      setFirstName('')
      setLastName('')
      setPhoneNumber('')
      setRole('SERVICE_STAFF')
      await loadUsers()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  function startEdit(user: UserRecord) {
    setEditingId(user.id)
    setDraft({ firstName: user.firstName ?? '', lastName: user.lastName ?? '', phoneNumber: user.phoneNumber ?? '' })
  }

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (editingId === null) return
    setSaving(true)
    setError('')
    try {
      await usersApi.updateProfile(editingId, { firstName: draft.firstName.trim(), lastName: draft.lastName.trim(), phoneNumber: draft.phoneNumber.trim() })
      setEditingId(null)
      await loadUsers()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading"><div><p className="admin-eyebrow">พนักงาน / สิทธิ์ใช้งาน</p><h1>พนักงาน</h1><p className="admin-subtitle">บัญชีพนักงานและบทบาทการทำงาน</p></div><span className="admin-count">{users.length} บัญชี</span></div>
    {error && <p className="admin-error" role="alert">{error}</p>}
    {notice && <p className="admin-notice" role="status">{notice}</p>}
    <ConfirmDialog open={removing !== null} title="ลบ/เก็บออกบัญชีพนักงาน" description={`ต้องการลบหรือเก็บออกบัญชี ${removing?.username ?? ''} หรือไม่ บัญชีจะเข้าสู่ระบบไม่ได้และเซสชันเดิมจะถูกเพิกถอนทันที บัญชีที่มีประวัติสต็อกจะถูกเก็บออกโดยชื่อผู้ทำรายการยังอยู่`} busy={saving} onCancel={() => setRemoving(null)} onConfirm={() => removing && void runAction(() => usersApi.remove(removing.id), removedMessage)} />
    <section className="admin-section">
      <div className="admin-section-heading"><h2>รายชื่อพนักงาน</h2><span>{loading ? 'กำลังโหลด...' : `${users.length} คน`}</span></div>
      <div className="admin-table-wrap"><table className="admin-table">
        <thead><tr><th>ชื่อที่แสดง</th><th>ชื่อ-นามสกุล</th><th>โทรศัพท์</th><th>ชื่อผู้ใช้</th><th>อีเมล</th><th>บทบาท</th><th>สถานะ</th><th>โปรไฟล์</th><th>จัดการ</th></tr></thead>
        <tbody>{users.map((user) => <tr key={user.id}><td><strong>{user.displayName}</strong></td><td>{user.firstName || user.lastName ? `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() : <span className="table-secondary">ยังไม่มีชื่อ-นามสกุล (ใช้ชื่อที่แสดงเดิม)</span>}</td><td>{user.phoneNumber || '—'}</td><td className="table-code">{user.username}</td><td>{user.email || '—'}</td><td><span className="role-tag">{roleLabels[user.role]}</span></td><td>{user.active ? 'ใช้งาน' : 'ปิดใช้งาน'}</td><td><button type="button" onClick={() => startEdit(user)}>{user.firstName && user.lastName ? 'แก้ไข' : 'เติมข้อมูล'}</button></td><td><div className="stock-actions">{user.id !== currentUser.userId && <button type="button" disabled={saving} onClick={() => void runAction(() => usersApi.setActive(user.id, !user.active))}>{user.active ? 'ปิดใช้งาน' : 'เปิดใช้งาน'}</button>}{user.id !== currentUser.userId && <button type="button" className="admin-danger-button" disabled={saving} onClick={() => { setNotice(''); setRemoving(user) }}>ลบ/เก็บออก</button>}</div></td></tr>)}
          {!loading && users.length === 0 && <tr><td colSpan={9} className="admin-empty">ไม่พบข้อมูลพนักงาน</td></tr>}
        </tbody>
      </table></div>
    </section>
    <section className="admin-section">
      <div className="admin-section-heading"><h2>บัญชีที่เก็บออก</h2><span>{archived.length} บัญชี</span></div>
      <div className="admin-table-wrap"><table className="admin-table">
        <thead><tr><th>ชื่อที่แสดง</th><th>ชื่อผู้ใช้</th><th>บทบาท</th><th>เก็บออกเมื่อ</th><th>จัดการ</th></tr></thead>
        <tbody>{archived.map((user) => <tr key={user.id}><td><strong>{user.displayName}</strong></td><td className="table-code">{user.username}</td><td><span className="role-tag">{roleLabels[user.role]}</span></td><td>{user.archivedAt ? new Intl.DateTimeFormat('th-TH', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(user.archivedAt)) : '—'}</td><td><button type="button" disabled={saving} onClick={() => void runAction(() => usersApi.restore(user.id))}>กู้คืน (ยังปิดใช้งาน)</button></td></tr>)}
          {!loading && archived.length === 0 && <tr><td colSpan={5} className="admin-empty">ไม่มีบัญชีที่เก็บออก</td></tr>}
        </tbody>
      </table></div>
    </section>
    {editingId !== null && <section className="admin-section">
      <div className="admin-section-heading"><h2>เติม/แก้ไขข้อมูลโปรไฟล์</h2></div>
      <form className="user-create-form" onSubmit={saveProfile}>
        <label>ชื่อ<input required maxLength={100} value={draft.firstName} onChange={(event) => setDraft({ ...draft, firstName: event.target.value })} /></label>
        <label>นามสกุล<input required maxLength={100} value={draft.lastName} onChange={(event) => setDraft({ ...draft, lastName: event.target.value })} /></label>
        <label>โทรศัพท์<input maxLength={20} value={draft.phoneNumber} onChange={(event) => setDraft({ ...draft, phoneNumber: event.target.value })} /></label>
        <button className="admin-primary-button" disabled={saving}>บันทึกโปรไฟล์</button>
        <button type="button" onClick={() => setEditingId(null)}>ยกเลิก</button>
      </form>
    </section>}
    <section className="admin-section admin-create-user">
      <div className="admin-section-heading"><div><p className="admin-eyebrow">บัญชีใหม่</p><h2>เพิ่มพนักงาน</h2></div></div>
      <form className="user-create-form" onSubmit={createUser}>
        <label>ชื่อที่แสดง<input required maxLength={120} value={displayName} onChange={(event) => setDisplayName(event.target.value)} /></label>
        <label>ชื่อ<input required maxLength={100} value={firstName} onChange={(event) => setFirstName(event.target.value)} /></label>
        <label>นามสกุล<input required maxLength={100} value={lastName} onChange={(event) => setLastName(event.target.value)} /></label>
        <label>โทรศัพท์<input maxLength={20} value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} /></label>
        <label>ชื่อผู้ใช้<input required maxLength={80} autoComplete="off" value={username} onChange={(event) => setUsername(event.target.value)} /></label>
        <label>รหัสผ่านเริ่มต้น<input required type="password" minLength={8} maxLength={72} autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        <label>อีเมล<input type="email" maxLength={254} value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>บทบาท<select value={role} onChange={(event) => setRole(event.target.value as UserRole)}>{roles.map((option) => <option key={option} value={option}>{roleLabels[option]}</option>)}</select></label>
        <button className="admin-primary-button" disabled={saving}>{saving ? 'กำลังสร้าง...' : 'สร้างบัญชี'}</button>
      </form>
    </section>
  </section>
}