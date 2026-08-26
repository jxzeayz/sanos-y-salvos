import {
  Box, Typography, Card, CardContent, Chip, LinearProgress,
  Alert, CircularProgress
} from '@mui/material'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty'

const ESTADO_CONFIG = {
  PENDIENTE:   { color: 'warning', icon: <HourglassEmptyIcon fontSize="small" /> },
  CONFIRMADA:  { color: 'success', icon: <CheckCircleIcon   fontSize="small" /> },
  RECHAZADA:   { color: 'error',   icon: <CancelIcon        fontSize="small" /> },
}

function ScoreBar({ score }) {
  const pct     = Math.round((score ?? 0) * 100)
  const color   = pct >= 80 ? 'success' : pct >= 60 ? 'warning' : 'error'
  return (
    <Box>
      <Box display="flex" justifyContent="space-between">
        <Typography variant="caption" color="text.secondary">Puntuación de coincidencia</Typography>
        <Typography variant="caption" fontWeight={700}>{pct}%</Typography>
      </Box>
      <LinearProgress variant="determinate" value={pct} color={color} sx={{ borderRadius: 1 }} />
    </Box>
  )
}

export default function CoincidenciaList({ coincidencias, loading, error }) {
  if (loading) return (
    <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>
  )
  if (error) return <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>
  if (!coincidencias?.length) return (
    <Alert severity="info" sx={{ mt: 2 }}>
      No se encontraron coincidencias para esta mascota.
    </Alert>
  )

  return (
    <Box display="flex" flexDirection="column" gap={2}>
      {coincidencias.map((c) => {
        const cfg = ESTADO_CONFIG[c.estado] || ESTADO_CONFIG.PENDIENTE
        return (
          <Card key={c.id} elevation={2}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Typography variant="subtitle1" fontWeight={600}>
                  Coincidencia #{c.id}
                </Typography>
                <Chip
                  icon={cfg.icon}
                  label={c.estado}
                  color={cfg.color}
                  size="small"
                />
              </Box>

              <ScoreBar score={c.scoreMatch} />

              <Box display="flex" gap={2} mt={1.5} flexWrap="wrap">
                <Typography variant="body2" color="text.secondary">
                  Mascota perdida: <strong>#{c.mascotaPerdidaId}</strong>
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Mascota encontrada: <strong>#{c.mascotaEncontradaId}</strong>
                </Typography>
              </Box>

              {c.fechaDeteccion && (
                <Typography variant="caption" color="text.secondary" display="block" mt={1}>
                  Detectada: {new Date(c.fechaDeteccion).toLocaleString('es-CL', { timeZone: 'America/Santiago' })}
                </Typography>
              )}
            </CardContent>
          </Card>
        )
      })}
    </Box>
  )
}
