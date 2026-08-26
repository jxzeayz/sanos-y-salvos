import { useState } from 'react'
import {
  Box, TextField, Button, Typography, Alert, CircularProgress,
  Paper, MenuItem, Select, InputLabel, FormControl, InputAdornment, IconButton, LinearProgress
} from '@mui/material'
import PersonAddIcon from '@mui/icons-material/PersonAdd'
import Visibility from '@mui/icons-material/Visibility'
import VisibilityOff from '@mui/icons-material/VisibilityOff'
import { useAuth } from '../../hooks/useAuth.js'
import { useNavigate, Link } from 'react-router-dom'
import { useSnackbar } from '../../context/SnackbarContext.jsx'

const ROLES = [
  { value: 'DUEÑO',       label: 'Dueño de mascota' },
  { value: 'CIUDADANO',   label: 'Ciudadano colaborador' },
  { value: 'VETERINARIO', label: 'Veterinario / Clínica' },
]

function calcularFortaleza(password) {
  let score = 0
  if (password.length >= 8) score++
  if (password.length >= 12) score++
  if (/[A-Z]/.test(password)) score++
  if (/[a-z]/.test(password)) score++
  if (/[0-9]/.test(password)) score++
  if (/[^A-Za-z0-9]/.test(password)) score++
  return Math.min(score, 5)
}

const FORTALEZA_COLOR = { 0: 'error', 1: 'error', 2: 'warning', 3: 'warning', 4: 'success', 5: 'success' }
const FORTALEZA_LABEL = { 0: 'Muy débil', 1: 'Débil', 2: 'Regular', 3: 'Buena', 4: 'Fuerte', 5: 'Muy fuerte' }

export default function RegisterForm() {
  const { register } = useAuth()
  const navigate      = useNavigate()
  const showSnackbar  = useSnackbar()

  const [form, setForm] = useState({
    nombre: '', email: '', password: '', telefono: '', rol: 'DUEÑO'
  })
  const [error,     setError]     = useState('')
  const [loading,   setLoading]   = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const fortaleza = calcularFortaleza(form.password)

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const validarForm = () => {
    if (form.password.length < 8) return 'La contraseña debe tener al menos 8 caracteres'
    if (!/[A-Z]/.test(form.password)) return 'La contraseña debe tener al menos una mayúscula'
    if (!/[0-9]/.test(form.password)) return 'La contraseña debe tener al menos un número'
    if (form.telefono && !/^\d*$/.test(form.telefono)) return 'El teléfono solo debe contener números'
    return null
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const validationError = validarForm()
    if (validationError) {
      setError(validationError)
      return
    }
    setLoading(true)
    try {
      await register(form)
      showSnackbar('Cuenta creada exitosamente', 'success')
      navigate('/mascotas')
    } catch (err) {
      setError(err.response?.data?.mensaje || 'Error al registrarse')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Paper elevation={3} sx={{ p: 4, maxWidth: 450, mx: 'auto', mt: 8 }}>
      <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
        <PersonAddIcon color="primary" fontSize="large" />
        <Typography variant="h5" fontWeight={700}>Crear cuenta</Typography>

        {error && <Alert severity="error" sx={{ width: '100%' }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }} noValidate>
          <TextField
            fullWidth margin="normal" label="Nombre completo"
            name="nombre" value={form.nombre} onChange={handleChange} required autoFocus
          />
          <TextField
            fullWidth margin="normal" label="Correo electrónico"
            name="email" type="email" value={form.email} onChange={handleChange} required
          />
          <TextField
            fullWidth margin="normal" label="Contraseña"
            name="password" type={showPassword ? 'text' : 'password'}
            value={form.password} onChange={handleChange} required
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    onClick={() => setShowPassword(!showPassword)}
                    edge="end" size="small"
                    aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
          {form.password && (
            <Box sx={{ width: '100%', mt: 0.5 }}>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <LinearProgress
                  variant="determinate"
                  value={(fortaleza / 5) * 100}
                  color={FORTALEZA_COLOR[fortaleza]}
                  sx={{ flexGrow: 1, mr: 1, height: 6, borderRadius: 1 }}
                />
                <Typography variant="caption" color={FORTALEZA_COLOR[fortaleza] + '.main'} fontWeight={600}>
                  {FORTALEZA_LABEL[fortaleza]}
                </Typography>
              </Box>
              <Typography variant="caption" color="text.secondary">
                Mínimo 8 caracteres, 1 mayúscula, 1 número
              </Typography>
            </Box>
          )}
          <TextField
            fullWidth margin="normal" label="Teléfono (opcional)"
            name="telefono" value={form.telefono} onChange={(e) => {
              const val = e.target.value.replace(/[^0-9]/g, '')
              setForm((prev) => ({ ...prev, telefono: val }))
            }} inputMode="numeric" pattern="[0-9]*"
          />
          <FormControl fullWidth margin="normal">
            <InputLabel>Tipo de usuario</InputLabel>
            <Select name="rol" value={form.rol} label="Tipo de usuario" onChange={handleChange}>
              {ROLES.map((r) => (
                <MenuItem key={r.value} value={r.value}>{r.label}</MenuItem>
              ))}
            </Select>
          </FormControl>
          <Button
            type="submit" fullWidth variant="contained" size="large"
            sx={{ mt: 2 }} disabled={loading}
          >
            {loading ? <CircularProgress size={24} color="inherit" /> : 'Registrarse'}
          </Button>
        </Box>

        <Typography variant="body2">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" style={{ color: '#1a3a5c', fontWeight: 600 }}>
            Inicia sesión
          </Link>
        </Typography>
      </Box>
    </Paper>
  )
}
