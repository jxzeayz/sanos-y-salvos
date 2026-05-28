import { Box, Typography, Button, Grid, Paper } from '@mui/material'
import PetsIcon from '@mui/icons-material/Pets'
import MapIcon from '@mui/icons-material/Map'
import SearchIcon from '@mui/icons-material/Search'
import { Link } from 'react-router-dom'

const cards = [
  { icon: <PetsIcon fontSize="large" color="primary" />, title: 'Reportar mascota',
    desc: 'Registra una mascota perdida o encontrada con sus características y ubicación.',
    to: '/mascotas/nueva', label: 'Registrar' },
  { icon: <MapIcon fontSize="large" color="primary" />, title: 'Ver mapa',
    desc: 'Visualiza los reportes en el mapa y detecta zonas con mayor incidencia.',
    to: '/mapa', label: 'Abrir mapa' },
  { icon: <SearchIcon fontSize="large" color="primary" />, title: 'Coincidencias',
    desc: 'El sistema detecta automáticamente posibles coincidencias entre mascotas.',
    to: '/coincidencias', label: 'Ver coincidencias' },
]

export default function HomePage() {
  return (
    <Box>
      <Box
        sx={{
          bgcolor: 'primary.main', color: 'white',
          py: 8, px: 4, textAlign: 'center',
        }}
      >
        <PetsIcon sx={{ fontSize: 64, mb: 2 }} />
        <Typography variant="h3" fontWeight={700} gutterBottom>
          Sanos y Salvos
        </Typography>
        <Typography variant="h6" sx={{ opacity: 0.85, mb: 4, maxWidth: 600, mx: 'auto' }}>
          Plataforma centralizada para la localización y recuperación de mascotas perdidas
        </Typography>
        <Box display="flex" gap={2} justifyContent="center" flexWrap="wrap">
          <Button variant="contained" color="secondary" size="large" component={Link} to="/register">
            Crear cuenta
          </Button>
          <Button variant="outlined" color="inherit" size="large" component={Link} to="/mapa">
            Ver mapa público
          </Button>
        </Box>
      </Box>

      <Box sx={{ maxWidth: 900, mx: 'auto', py: 6, px: 2 }}>
        <Typography variant="h5" fontWeight={700} textAlign="center" gutterBottom>
          ¿Cómo funciona?
        </Typography>
        <Grid container spacing={3} mt={1}>
          {cards.map((c) => (
            <Grid item xs={12} sm={4} key={c.title}>
              <Paper elevation={2} sx={{ p: 3, textAlign: 'center', height: '100%' }}>
                {c.icon}
                <Typography variant="h6" fontWeight={600} mt={1} gutterBottom>{c.title}</Typography>
                <Typography variant="body2" color="text.secondary" mb={2}>{c.desc}</Typography>
                <Button variant="outlined" size="small" component={Link} to={c.to}>{c.label}</Button>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Box>
    </Box>
  )
}
