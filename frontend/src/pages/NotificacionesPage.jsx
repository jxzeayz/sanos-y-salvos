import { useState } from 'react'
import {
  Box, Typography, List, ListItem, ListItemText, ListItemSecondaryAction,
  IconButton, Chip, CircularProgress, Alert, Button, Divider
} from '@mui/material'
import DoneAllIcon from '@mui/icons-material/DoneAll'
import ContactPhoneIcon from '@mui/icons-material/ContactPhone'
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone'
import { useNotificaciones } from '../hooks/useNotificaciones.js'
import { crearNotificacion, getMisMascotas } from '../api/bffClient.js'
import { useAuth } from '../hooks/useAuth.js'
import { useSnackbar } from '../context/SnackbarContext.jsx'

const TIPO_COLOR = {
  COINCIDENCIA: 'success',
  CONTACTO: 'info',
  ALERTA: 'warning',
  SISTEMA: 'info',
}

export default function NotificacionesPage() {
  const { notificaciones, loading, error, marcarTodas, marcarLeida, recargar } = useNotificaciones()
  const { usuario } = useAuth()
  const showSnackbar = useSnackbar()
  const [contactando, setContactando] = useState(null)

  const handleContactar = async (notificacion) => {
    if (!notificacion.origenUsuarioId || !usuario?.usuarioId) return
    setContactando(notificacion.id)
    try {
      let nombreMascotas = 'mis mascotas'
      try {
        const { data: mascotas } = await getMisMascotas()
        const perdidas = mascotas?.filter(m => m.estado === 'PERDIDA') || []
        if (perdidas.length > 0) {
          nombreMascotas = perdidas.length === 1
            ? `mi mascota "${perdidas[0].nombre}"`
            : `mis mascotas "${perdidas.map(m => m.nombre).join('", "')}"`
        }
      } catch {}

      const nombreUsuario = usuario.nombre || 'un usuario'
      const contacto = usuario.telefono || usuario.email || ''
      const lineaContacto = contacto ? ` Mi datos de contacto: ${contacto}.` : ''
      await crearNotificacion({
        usuarioId: notificacion.origenUsuarioId,
        origenUsuarioId: usuario.usuarioId,
        tipo: 'CONTACTO',
        titulo: 'Solicitud de contacto',
        mensaje: `Hola, soy ${nombreUsuario} y me gustaría ponerme en contacto contigo sobre ${nombreMascotas}.${lineaContacto}`,
      })

      await marcarLeida(notificacion.id)

      showSnackbar('Solicitud de contacto enviada', 'success')
    } catch {
      showSnackbar('Error al enviar solicitud', 'error')
    } finally {
      setContactando(null)
    }
  }

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" py={6}>
        <CircularProgress />
      </Box>
    )
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ maxWidth: 800, mx: 'auto', mt: 4 }}>
        {error}
      </Alert>
    )
  }

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto', py: 4, px: 2 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>
          Notificaciones
        </Typography>
        <Button
          variant="outlined"
          size="small"
          startIcon={<DoneAllIcon />}
          onClick={marcarTodas}
          disabled={notificaciones.every((n) => n.leida)}
        >
          Marcar todas leídas
        </Button>
      </Box>

      {notificaciones.length === 0 ? (
        <Box textAlign="center" py={6}>
          <NotificationsNoneIcon sx={{ fontSize: 64, color: 'text.disabled', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">
            No hay notificaciones
          </Typography>
          <Typography variant="body2" color="text.disabled">
            Cuando se detecten coincidencias, aparecerán aquí.
          </Typography>
        </Box>
      ) : (
        <List disablePadding>
          {notificaciones.map((n, idx) => (
            <Box key={n.id}>
              {idx > 0 && <Divider />}
              <ListItem
                sx={{
                  opacity: n.leida ? 0.6 : 1,
                  bgcolor: n.leida ? 'transparent' : 'action.hover',
                  borderRadius: 1,
                  transition: 'background-color 0.2s',
                }}
              >
                <ListItemText
                  primary={
                    <Box display="flex" alignItems="center" gap={1} mb={0.5}>
                      <Typography variant="subtitle1" fontWeight={n.leida ? 400 : 700}>
                        {n.titulo}
                      </Typography>
                      <Chip
                        label={n.tipo}
                        size="small"
                        color={TIPO_COLOR[n.tipo] || 'default'}
                        variant="outlined"
                      />
                    </Box>
                  }
                  secondary={
                    <>
                      <Typography variant="body2" color="text.secondary" display="block">
                        {n.mensaje}
                      </Typography>
                      <Typography variant="caption" color="text.disabled">
                        {n.enviadaEn
                          ? new Date(n.enviadaEn).toLocaleString('es-CL', {
                              day: '2-digit', month: 'short', year: 'numeric',
                              hour: '2-digit', minute: '2-digit',
                              timeZone: 'America/Santiago',
                            })
                          : ''}
                      </Typography>
                    </>
                  }
                />
                <ListItemSecondaryAction>
                  {n.tipo === 'COINCIDENCIA' && n.origenUsuarioId && !n.leida && (
                    <Button
                      variant="contained"
                      size="small"
                      startIcon={<ContactPhoneIcon />}
                      onClick={() => handleContactar(n)}
                      disabled={contactando === n.id}
                      sx={{ textTransform: 'none' }}
                    >
                      {contactando === n.id ? 'Enviando...' : 'Contactar usuario'}
                    </Button>
                  )}
                </ListItemSecondaryAction>
              </ListItem>
            </Box>
          ))}
        </List>
      )}
    </Box>
  )
}
