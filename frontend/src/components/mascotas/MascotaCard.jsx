import { useState, useEffect } from 'react'
import {
  Card, CardContent, CardActions, Typography, Chip, Button, Box,
  IconButton, Menu, MenuItem, Divider, CardMedia
} from '@mui/material'
import PetsIcon from '@mui/icons-material/Pets'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import CalendarTodayIcon from '@mui/icons-material/CalendarToday'
import MoreVertIcon from '@mui/icons-material/MoreVert'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import { Link } from 'react-router-dom'
import { getArchivosMascota } from '../../api/bffClient.js'

const ESTADO_COLOR = {
  PERDIDA:      'error',
  ENCONTRADA:   'success',
  REUNIFICADA:  'info',
}

const ESTADO_LABEL = {
  PERDIDA:      'Perdida',
  ENCONTRADA:   'Encontrada',
  REUNIFICADA:  'Reunificada',
}

const ESPECIE_LABEL = {
  PERRO: 'Perro',
  GATO:  'Gato',
  OTRO:  'Otro',
}

const TAMANO_LABEL = {
  PEQUENO: 'Pequeño',
  MEDIANO: 'Mediano',
  GRANDE:  'Grande',
}

const DEFAULT_IMG = 'data:image/svg+xml,' + encodeURIComponent(
  `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="160" viewBox="0 0 400 160">
    <rect fill="#e8eaf6" width="400" height="160"/>
    <text fill="#7986cb" font-family="sans-serif" font-size="48" x="200" y="85" text-anchor="middle" dominant-baseline="central">🐾</text>
  </svg>`
)

export default function MascotaCard({ mascota, usuarioId, onVerCoincidencias, onEliminar, onCambiarEstado }) {
  const [anchorEl, setAnchorEl] = useState(null)
  const [fotoUrl, setFotoUrl] = useState(null)
  const esDueno = usuarioId != null && String(mascota.usuarioId) === String(usuarioId)
  const fecha = mascota.fechaReporte
    ? new Date(mascota.fechaReporte).toLocaleDateString('es-CL', { timeZone: 'America/Santiago' })
    : '—'

  useEffect(() => {
    if (!mascota.id) return
    let cancelled = false
    getArchivosMascota(mascota.id)
      .then(({ data }) => {
        if (!cancelled && data && data.length > 0) {
          setFotoUrl(`/bff/archivos/${data[0].id}/descargar`)
        }
      })
      .catch(() => {})
    return () => { cancelled = true }
  }, [mascota.id])

  return (
    <Card elevation={2} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardMedia
        component="img"
        height="200"
        image={fotoUrl || DEFAULT_IMG}
        alt={mascota.nombre}
        sx={{ objectFit: 'contain', bgcolor: '#f5f5f5' }}
      />
      <CardContent sx={{ flexGrow: 1 }}>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography variant="h6" fontWeight={700}>
              <PetsIcon fontSize="small" sx={{ mr: 0.5, verticalAlign: 'middle' }} />
              {mascota.nombre}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              #{mascota.id}
            </Typography>
          </Box>
          <Box display="flex" alignItems="center" gap={0.5}>
            <Chip
              label={ESTADO_LABEL[mascota.estado] || mascota.estado}
              color={ESTADO_COLOR[mascota.estado] || 'default'}
              size="small"
            />
            {esDueno && (
              <IconButton size="small" onClick={(e) => setAnchorEl(e.currentTarget)} aria-label="Más opciones">
                <MoreVertIcon fontSize="small" />
              </IconButton>
            )}
            <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
              <MenuItem
                component={Link}
                to={`/mascotas/editar/${mascota.id}`}
                onClick={() => setAnchorEl(null)}
              >
                <EditIcon fontSize="small" sx={{ mr: 1 }} /> Editar
              </MenuItem>
              {mascota.estado !== 'REUNIFICADA' && (
                <MenuItem onClick={() => { setAnchorEl(null); onCambiarEstado?.(mascota.id, 'REUNIFICADA') }}>
                  <CheckCircleIcon fontSize="small" sx={{ mr: 1 }} /> Marcar Reunificada
                </MenuItem>
              )}
              <Divider />
              <MenuItem onClick={() => { setAnchorEl(null); onEliminar?.(mascota.id) }} sx={{ color: 'error.main' }}>
                <DeleteIcon fontSize="small" sx={{ mr: 1 }} /> Eliminar
              </MenuItem>
            </Menu>
          </Box>
        </Box>

        <Typography variant="body2" color="text.secondary" gutterBottom sx={{ mt: 0.5 }}>
          {ESPECIE_LABEL[mascota.especie] || mascota.especie} {mascota.raza ? `· ${mascota.raza}` : ''} {mascota.color ? `· ${mascota.color}` : ''}
        </Typography>

        {mascota.descripcion && (
          <Typography variant="body2" sx={{ mt: 1 }}>
            {mascota.descripcion}
          </Typography>
        )}

        <Box display="flex" gap={1} mt={1.5} flexWrap="wrap">
          {mascota.tamano && (
            <Chip label={`Tamaño: ${TAMANO_LABEL[mascota.tamano] || mascota.tamano}`} size="small" variant="outlined" />
          )}
          {mascota.latitud && (
            <Chip
              icon={<LocationOnIcon />}
              label="Con ubicación"
              size="small"
              variant="outlined"
              color="primary"
            />
          )}
        </Box>

        <Box display="flex" alignItems="center" mt={1.5} gap={0.5}>
          <CalendarTodayIcon fontSize="small" color="disabled" />
          <Typography variant="caption" color="text.secondary">
            Reportado: {fecha}
          </Typography>
        </Box>
      </CardContent>

      {(mascota.estado === 'PERDIDA' || mascota.estado === 'ENCONTRADA') && onVerCoincidencias && (
        <CardActions>
          <Button size="small" variant="outlined" onClick={() => onVerCoincidencias(mascota.id)}>
            Ver coincidencias
          </Button>
        </CardActions>
      )}
    </Card>
  )
}
