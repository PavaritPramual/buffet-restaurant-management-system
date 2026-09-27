import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './styles/tokens.css'
import './components/common/common.css'
import FoundationApp from './FoundationApp.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <FoundationApp />
    </BrowserRouter>
  </StrictMode>,
)
