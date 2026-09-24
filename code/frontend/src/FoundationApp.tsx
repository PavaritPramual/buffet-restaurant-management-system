import { Route, Routes } from 'react-router-dom'
import DesignSystemPage from './DesignSystemPage'
import CustomerOrderingPage from './features/ordering/CustomerOrderingPage'
import MenuAdminPage from './features/ordering/MenuAdminPage'

export default function FoundationApp() {
  return <Routes>
    <Route path="/" element={<DesignSystemPage />} />
    <Route path="/customer/sessions/:sessionId" element={<CustomerOrderingPage />} />
    <Route path="/admin/menu" element={<MenuAdminPage />} />
    <Route path="*" element={<DesignSystemPage />} />
  </Routes>
}
