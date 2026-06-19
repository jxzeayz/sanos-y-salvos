import { useState } from 'react'
import {
  Box, Typography, Paper, TextField, Button, Alert, CircularProgress,
  Grid, Divider, Avatar, InputAdornment, IconButton, Chip
} from '@mui/material'
import PersonIcon from '@mui/icons-material/Person'
import EmailIcon from '@mui/icons-material/Email'
import PhoneIcon from '@mui/icons-material/Phone'
import BadgeIcon from '@mui/icons-material/Badge'
import EditIcon from '@mui/icons-material/Edit'
import LockIcon from '@mui/icons-material/Lock'
import Visibility from '@mui/icons-material/Visibility'
import VisibilityOff from '@mui/icons-material/VisibilityOff'
import { useAuth } from '../hooks/useAuth.js'
import { updateProfile, changePassword } from '../api/bffClient.js'
import { useSnackbar } from '../context/SnackbarContext.jsx'

const ROL_LABEL = {
  DUEÑO: 'Dueño de mascota',
  CIUDADANO: 'Ciudadano colaborador',
  VETERINARIO: 'Veterinario / Clínica',
  ADMIN: 'Administrador',
}

export default function PerfilPage() {
  const { usuario, updateUsuario } = useAuth()
  const showSnackbar = useSnackbar()

  const [profile, setProfile] = useState({
    nombre: usuario?.nombre || '',
    email: usuario?.email || '',
    telefono: usuario?.telefono || '',
  })
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [showCurrent, setShowCurrent] = useState(false)
  const [showNew, setShowNew] = useState(false)
  const [loadingProfile, setLoadingProfile] = useState(false)
  const [loadingPassword, setLoadingPassword] = useState(false)
  const [errorProfile, setErrorProfile] = useState('')
  const [errorPassword, setErrorPassword] = useState('')

  const handleSaveProfile = async (e) => {
    e.preventDefault()
    setErrorProfile('')
    if (profile.telefono && !/^\d*$/.test(profile.telefono)) {
      setErrorProfile('El teléfono solo debe contener números')
      return
    }
    setLoadingProfile(true)
    try {
      await updateProfile(profile)
      updateUsuario(profile)
      showSnackbar('Perfil actualizado correctamente', 'success')
    } catch (err) {
      setErrorProfile(err.response?.data?.mensaje || 'Error al actualizar perfil')
    } finally {
      setLoadingProfile(false)
    }
  }

  const handleChangePassword = async (e) => {
    e.preventDefault()
    setErrorPassword('')
    if (passwords.newPassword !== passwords.confirmPassword) {
      setErrorPassword('Las contraseñas no coinciden')
      return
    }
    if (passwords.newPassword.length < 8) {
      setErrorPassword('La nueva contraseña debe tener al menos 8 caracteres')
      return
    }
    setLoadingPassword(true)
    try {
      await changePassword(passwords)
      showSnackbar('Contraseña cambiada exitosamente', 'success')
      setPasswords({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch (err) {
      setErrorPassword(err.response?.data?.mensaje || 'Error al cambiar contraseña')
    } finally {
      setLoadingPassword(false)
    }
  }

  return (
    <Box sx={{ maxWidth: 700, mx: 'auto', py: 4, px: 2 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>Mi Perfil</Typography>

      {/* Tarjeta de datos actuales del usuario */}
      <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
        <Box display="flex" alignItems="center" gap={2} mb={2}>
          <Avatar sx={{ width: 64, height: 64, bgcolor: 'secondary.main' }}>
            <PersonIcon fontSize="large" />
          </Avatar>
          <Box>
            <Typography variant="h6" fontWeight={700}>{usuario?.nombre}</Typography>
            <Typography variant="body2" color="text.secondary">
              <Chip
                label={ROL_LABEL[usuario?.rol] || usuario?.rol}
                size="small"
                color="primary"
                variant="outlined"
                sx={{ mr: 1 }}
              />
            </Typography>
          </Box>
        </Box>
        <Divider sx={{ my: 2 }} />
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <Box display="flex" alignItems="center" gap={1}>
              <EmailIcon fontSize="small" color="action" />
              <Box>
                <Typography variant="caption" color="text.secondary">Correo electrónico</Typography>
                <Typography variant="body1" fontWeight={500}>{usuario?.email}</Typography>
              </Box>
            </Box>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Box display="flex" alignItems="center" gap={1}>
              <PhoneIcon fontSize="small" color="action" />
              <Box>
                <Typography variant="caption" color="text.secondary">Teléfono</Typography>
                <Typography variant="body1" fontWeight={500}>{usuario?.telefono || 'No registrado'}</Typography>
              </Box>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Formulario editar perfil */}
      <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
        <Box display="flex" alignItems="center" gap={1} mb={2}>
          <EditIcon color="action" />
          <Typography variant="h6" fontWeight={600}>Editar perfil</Typography>
        </Box>
        {errorProfile && <Alert severity="error" sx={{ mb: 2 }}>{errorProfile}</Alert>}
        <Box component="form" onSubmit={handleSaveProfile}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Nombre" value={profile.nombre}
                onChange={(e) => setProfile({ ...profile, nombre: e.target.value })} required />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Email" value={profile.email}
                onChange={(e) => setProfile({ ...profile, email: e.target.value })} required type="email" />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Teléfono" value={profile.telefono}
                onChange={(e) => setProfile({ ...profile, telefono: e.target.value.replace(/[^0-9]/g, '') })} inputMode="numeric" pattern="[0-9]*" />
            </Grid>
          </Grid>
          <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={loadingProfile}>
            {loadingProfile ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}
            Guardar cambios
          </Button>
        </Box>
      </Paper>

      {/* Formulario cambiar contraseña */}
      <Paper elevation={2} sx={{ p: 3 }}>
        <Box display="flex" alignItems="center" gap={1} mb={2}>
          <LockIcon color="action" />
          <Typography variant="h6" fontWeight={600}>Cambiar contraseña</Typography>
        </Box>
        {errorPassword && <Alert severity="error" sx={{ mb: 2 }}>{errorPassword}</Alert>}
        <Box component="form" onSubmit={handleChangePassword}>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField fullWidth label="Contraseña actual" type={showCurrent ? 'text' : 'password'}
                value={passwords.currentPassword}
                onChange={(e) => setPasswords({ ...passwords, currentPassword: e.target.value })}
                required
                InputProps={{
                  endAdornment: <InputAdornment position="end">
                    <IconButton onClick={() => setShowCurrent(!showCurrent)} size="small" edge="end">
                      {showCurrent ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>,
                }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Nueva contraseña" type={showNew ? 'text' : 'password'}
                value={passwords.newPassword}
                onChange={(e) => setPasswords({ ...passwords, newPassword: e.target.value })}
                required
                InputProps={{
                  endAdornment: <InputAdornment position="end">
                    <IconButton onClick={() => setShowNew(!showNew)} size="small" edge="end">
                      {showNew ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>,
                }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Confirmar contraseña" type="password"
                value={passwords.confirmPassword}
                onChange={(e) => setPasswords({ ...passwords, confirmPassword: e.target.value })}
                required
              />
            </Grid>
          </Grid>
          <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={loadingPassword}>
            {loadingPassword ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}
            Cambiar contraseña
          </Button>
        </Box>
      </Paper>
    </Box>
  )
}
