import { Box, Typography } from '@mui/material'
import MascotaForm from '../components/mascotas/MascotaForm.jsx'

export default function NuevaMascotaPage() {
  return (
    <Box sx={{ maxWidth: 700, mx: 'auto', py: 4, px: 2 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Registrar nueva mascota
      </Typography>
      <MascotaForm />
    </Box>
  )
}
