import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './hooks/useAuth.js'
import Navbar from './components/ui/Navbar.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import HomePage from './pages/HomePage.jsx'
import MascotasPage from './pages/MascotasPage.jsx'
import NuevaMascotaPage from './pages/NuevaMascotaPage.jsx'
import MapaPage from './pages/MapaPage.jsx'
import CoincidenciasPage from './pages/CoincidenciasPage.jsx'

function PrivateRoute({ children }) {
  const { token } = useAuth()
  return token ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/"           element={<HomePage />} />
        <Route path="/login"      element={<LoginPage />} />
        <Route path="/register"   element={<RegisterPage />} />
        <Route path="/mapa"       element={<MapaPage />} />
        <Route path="/mascotas"   element={<PrivateRoute><MascotasPage /></PrivateRoute>} />
        <Route path="/mascotas/nueva" element={<PrivateRoute><NuevaMascotaPage /></PrivateRoute>} />
        <Route path="/coincidencias" element={<PrivateRoute><CoincidenciasPage /></PrivateRoute>} />
        <Route path="*"           element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}
