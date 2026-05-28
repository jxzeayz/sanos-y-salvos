import { useState } from 'react'
import {
  Box, TextField, Button, Typography, Alert, CircularProgress,
  Paper, MenuItem, Select, InputLabel, FormControl
} from '@mui/material'
import PersonAddIcon from '@mui/icons-material/PersonAdd'
import { useAuth } from '../../hooks/useAuth.js'
import { useNavigate, Link } from 'react-router-dom'

const ROLES = [
  { value: 'DUEÑO',       label: 'Dueño de mascota' },
  { value: 'CIUDADANO',   label: 'Ciudadano colaborador' },
  { value: 'VETERINARIO', label: 'Veterinario / Clínica' },
]

export default function RegisterForm() {
  const { register } = useAuth()
  const navigate      = useNavigate()

  const [form, setForm] = useState({
    nombre: '', email: '', password: '', telefono: '', rol: 'DUEÑO'
  })
  const [error,   setError]   = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form)
      navigate('/mascotas')
    } catch (err) {
      setError(err.response?.data?.message || 'Error al registrarse')
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
            name="password" type="password" value={form.password} onChange={handleChange} required
          />
          <TextField
            fullWidth margin="normal" label="Teléfono (opcional)"
            name="telefono" value={form.telefono} onChange={handleChange}
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
