import { Box, Typography } from '@mui/material'
import MapaReportes from '../components/mapa/MapaReportes.jsx'

export default function MapaPage() {
  return (
    <Box sx={{ maxWidth: 1000, mx: 'auto', py: 4, px: 2 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Mapa de reportes
      </Typography>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        Visualiza las mascotas perdidas y encontradas en tu zona.
      </Typography>
      <MapaReportes />
    </Box>
  )
}
