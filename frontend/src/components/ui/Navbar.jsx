import { AppBar, Toolbar, Typography, Button, Box, Chip } from '@mui/material'
import PetsIcon from '@mui/icons-material/Pets'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth.js'

export default function Navbar() {
  const { token, usuario, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <AppBar position="static">
      <Toolbar sx={{ gap: 2 }}>
        <PetsIcon />
        <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>
          Sanos y Salvos
        </Typography>

        <Button color="inherit" component={Link} to="/">Inicio</Button>
        <Button color="inherit" component={Link} to="/mapa">Mapa</Button>

        {token ? (
          <>
            <Button color="inherit" component={Link} to="/mascotas">Mis Mascotas</Button>
            <Button color="inherit" component={Link} to="/coincidencias">Coincidencias</Button>
            <Chip
              label={usuario?.nombre}
              color="secondary"
              size="small"
              sx={{ color: 'white', fontWeight: 600 }}
            />
            <Button color="inherit" onClick={handleLogout}>Salir</Button>
          </>
        ) : (
          <>
            <Button color="inherit" component={Link} to="/login">Ingresar</Button>
            <Button variant="outlined" color="inherit" component={Link} to="/register">
              Registrarse
            </Button>
          </>
        )}
      </Toolbar>
    </AppBar>
  )
}
