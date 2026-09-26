import { Route, Routes } from 'react-router-dom'
import DesignSystemPage from './DesignSystemPage'
import CustomerOrderingPage from './features/ordering/CustomerOrderingPage'
import MenuAdminPage from './features/ordering/MenuAdminPage'
import StaffTablesPage from './features/staff-tables/StaffTablesPage'
import DiningSessionPage from './features/staff-tables/DiningSessionPage'

export default function FoundationApp() {
  return <Routes>
    <Route path="/" element={<DesignSystemPage />} />
    <Route path="/customer/qr/:token" element={<CustomerOrderingPage />} />
    <Route path="/staff/tables" element={<StaffTablesPage />} />
    <Route path="/staff/sessions/:sessionId" element={<DiningSessionPage />} />
    <Route path="/admin/menu" element={<MenuAdminPage />} />
    <Route path="*" element={<DesignSystemPage />} />
  </Routes>
}
