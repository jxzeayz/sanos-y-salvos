import { useState, useCallback } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Snackbar, Alert as MuiAlert } from '@mui/material'
import { useAuth } from './hooks/useAuth.js'
import { SnackbarContext } from './context/SnackbarContext.jsx'
import Navbar from './components/ui/Navbar.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import HomePage from './pages/HomePage.jsx'
import MascotasPage from './pages/MascotasPage.jsx'
import NuevaMascotaPage from './pages/NuevaMascotaPage.jsx'
import MapaPage from './pages/MapaPage.jsx'
import CoincidenciasPage from './pages/CoincidenciasPage.jsx'
import PerfilPage from './pages/PerfilPage.jsx'
import NotificacionesPage from './pages/NotificacionesPage.jsx'
import AdminPage from './pages/AdminPage.jsx'

function PrivateRoute({ children }) {
  const { token } = useAuth()
  return token ? children : <Navigate to="/login" replace />
}

function AdminRoute({ children }) {
  const { token, usuario } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  if (usuario?.rol !== 'ADMIN') return <Navigate to="/" replace />
  return children
}

function PublicRoute({ children }) {
  const { token } = useAuth()
  if (token) return <Navigate to="/mascotas" replace />
  return children
}

export default function App() {
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' })

  const showSnackbar = useCallback((message, severity = 'success') => {
    setSnackbar({ open: true, message, severity })
  }, [])

  const closeSnackbar = useCallback(() => {
    setSnackbar((prev) => ({ ...prev, open: false }))
  }, [])

  return (
    <SnackbarContext.Provider value={showSnackbar}>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
        <Route path="/mapa" element={<PrivateRoute><MapaPage /></PrivateRoute>} />
        <Route path="/mascotas" element={<PrivateRoute><MascotasPage /></PrivateRoute>} />
        <Route path="/mascotas/nueva" element={<PrivateRoute><NuevaMascotaPage /></PrivateRoute>} />
        <Route path="/mascotas/editar/:id" element={<PrivateRoute><NuevaMascotaPage /></PrivateRoute>} />
        <Route path="/coincidencias" element={<PrivateRoute><CoincidenciasPage /></PrivateRoute>} />
        <Route path="/notificaciones" element={<PrivateRoute><NotificacionesPage /></PrivateRoute>} />
        <Route path="/perfil" element={<PrivateRoute><PerfilPage /></PrivateRoute>} />
        <Route path="/admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={closeSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <MuiAlert onClose={closeSnackbar} severity={snackbar.severity} variant="filled">
          {snackbar.message}
        </MuiAlert>
      </Snackbar>
    </SnackbarContext.Provider>
  )
}
