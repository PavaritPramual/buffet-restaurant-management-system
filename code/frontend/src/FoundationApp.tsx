import { Route, Routes } from 'react-router-dom'
import { API_BASE_URL } from './api/client'

function FoundationReadyPage() {
  return (
    <main className="min-h-screen bg-slate-950 px-6 py-16 text-slate-100">
      <section className="mx-auto max-w-3xl rounded-3xl border border-emerald-400/20 bg-slate-900 p-8 shadow-2xl shadow-emerald-950/30">
        <p className="mb-3 text-sm font-semibold uppercase tracking-[0.25em] text-emerald-400">
          Shared Foundation
        </p>
        <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">
          Buffet Restaurant Management System
        </h1>
        <p className="mt-5 max-w-2xl text-lg leading-8 text-slate-300">
          React, Vite, TypeScript, Tailwind CSS, Axios และ React Router พร้อมสำหรับให้แต่ละ Module เริ่มพัฒนาแยกกัน
        </p>
        <dl className="mt-8 grid gap-4 sm:grid-cols-2">
          <div className="rounded-2xl bg-slate-800 p-5">
            <dt className="text-sm text-slate-400">API base URL</dt>
            <dd className="mt-2 break-all font-mono text-emerald-300">{API_BASE_URL}</dd>
          </div>
          <div className="rounded-2xl bg-slate-800 p-5">
            <dt className="text-sm text-slate-400">Contract mode</dt>
            <dd className="mt-2 font-semibold text-white">Shared enums + canonical fixtures</dd>
          </div>
        </dl>
      </section>
    </main>
  )
}

export default function FoundationApp() {
  return (
    <Routes>
      <Route path="*" element={<FoundationReadyPage />} />
    </Routes>
  )
}
