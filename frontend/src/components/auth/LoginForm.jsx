import { useState } from 'react'
import {
  Box, TextField, Button, Typography, Alert, CircularProgress, Paper, InputAdornment, IconButton
} from '@mui/material'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import Visibility from '@mui/icons-material/Visibility'
import VisibilityOff from '@mui/icons-material/VisibilityOff'
import { useAuth } from '../../hooks/useAuth.js'
import { useNavigate, Link } from 'react-router-dom'
import { useSnackbar } from '../../context/SnackbarContext.jsx'

export default function LoginForm() {
  const { login } = useAuth()
  const navigate   = useNavigate()
  const showSnackbar = useSnackbar()

  const [form,    setForm]    = useState({ email: '', password: '' })
  const [error,   setError]   = useState('')
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(form.email, form.password)
      showSnackbar('Inicio de sesión exitoso', 'success')
      navigate('/mascotas')
    } catch (err) {
      setError(err.response?.data?.mensaje || 'Credenciales incorrectas')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Paper elevation={3} sx={{ p: 4, maxWidth: 400, mx: 'auto', mt: 8 }}>
      <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
        <LockOutlinedIcon color="primary" fontSize="large" />
        <Typography variant="h5" fontWeight={700}>Iniciar sesión</Typography>

        {error && <Alert severity="error" sx={{ width: '100%' }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }} noValidate>
          <TextField
            fullWidth margin="normal" label="Correo electrónico"
            name="email" type="email" value={form.email}
            onChange={handleChange} required autoFocus
          />
          <TextField
            fullWidth margin="normal" label="Contraseña"
            name="password" type={showPassword ? 'text' : 'password'} value={form.password}
            onChange={handleChange} required
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
          <Button
            type="submit" fullWidth variant="contained" size="large"
            sx={{ mt: 2 }} disabled={loading}
          >
            {loading ? <CircularProgress size={24} color="inherit" /> : 'Ingresar'}
          </Button>
        </Box>

        <Typography variant="body2">
          ¿No tienes cuenta?{' '}
          <Link to="/register" style={{ color: '#1a3a5c', fontWeight: 600 }}>
            Regístrate aquí
          </Link>
        </Typography>
      </Box>
    </Paper>
  )
}
