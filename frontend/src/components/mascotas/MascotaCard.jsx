import {
  Card, CardContent, CardActions, Typography, Chip, Button, Box
} from '@mui/material'
import PetsIcon from '@mui/icons-material/Pets'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import CalendarTodayIcon from '@mui/icons-material/CalendarToday'

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

export default function MascotaCard({ mascota, onVerCoincidencias }) {
  const fecha = mascota.fechaReporte
    ? new Date(mascota.fechaReporte).toLocaleDateString('es-CL')
    : '—'

  return (
    <Card elevation={2} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flexGrow: 1 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
          <Typography variant="h6" fontWeight={700}>
            <PetsIcon fontSize="small" sx={{ mr: 0.5, verticalAlign: 'middle' }} />
            {mascota.nombre}
          </Typography>
          <Chip
            label={ESTADO_LABEL[mascota.estado] || mascota.estado}
            color={ESTADO_COLOR[mascota.estado] || 'default'}
            size="small"
          />
        </Box>

        <Typography variant="body2" color="text.secondary" gutterBottom>
          {mascota.especie} {mascota.raza ? `· ${mascota.raza}` : ''} {mascota.color ? `· ${mascota.color}` : ''}
        </Typography>

        {mascota.descripcion && (
          <Typography variant="body2" sx={{ mt: 1 }}>
            {mascota.descripcion}
          </Typography>
        )}

        <Box display="flex" gap={1} mt={1.5} flexWrap="wrap">
          {mascota.tamano && (
            <Chip label={`Tamaño: ${mascota.tamano}`} size="small" variant="outlined" />
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

      {mascota.estado === 'PERDIDA' && onVerCoincidencias && (
        <CardActions>
          <Button size="small" variant="outlined" onClick={() => onVerCoincidencias(mascota.id)}>
            Ver coincidencias
          </Button>
        </CardActions>
      )}
    </Card>
  )
}
