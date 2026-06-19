import { useState, useEffect } from 'react'
import { AppBar, Toolbar, Typography, Button, Box, Badge, Avatar, Menu, MenuItem, IconButton } from '@mui/material'
import PetsIcon from '@mui/icons-material/Pets'
import AccountCircleIcon from '@mui/icons-material/AccountCircle'
import NotificationsIcon from '@mui/icons-material/Notifications'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth.js'
import { useNotificaciones } from '../../hooks/useNotificaciones.js'

export default function Navbar() {
  const { token, usuario, logout } = useAuth()
  const { noLeidasCount } = useNotificaciones()
  const navigate = useNavigate()
  const [anchorEl, setAnchorEl] = useState(null)

  const handleLogout = () => {
    setAnchorEl(null)
    logout()
    navigate('/login')
  }

  return (
    <AppBar position="static">
      <Toolbar sx={{ gap: 1 }}>
        <PetsIcon />
        <Typography variant="h6" sx={{ fontWeight: 700, mr: 2 }}>
          <Link to="/" style={{ color: 'inherit', textDecoration: 'none' }}>
            Sanos y Salvos
          </Link>
        </Typography>

        <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
          <Button color="inherit" component={Link} to="/">Inicio</Button>
          {token && (
            <>
              <Button color="inherit" component={Link} to="/mascotas">Mascotas</Button>
              <Button color="inherit" component={Link} to="/coincidencias">Coincidencias</Button>
              <Badge badgeContent={noLeidasCount} color="error" overlap="circular" anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
                <Button color="inherit" component={Link} to="/notificaciones" startIcon={<NotificationsIcon fontSize="small" />}>
                  Notificaciones
                </Button>
              </Badge>
              <Button color="inherit" component={Link} to="/mapa">Mapa</Button>
            </>
          )}
        </Box>

        {token ? (
          <>
            {usuario?.rol === 'ADMIN' && (
              <Button color="inherit" component={Link} to="/admin" size="small">
                Admin
              </Button>
            )}
            <IconButton
              color="inherit"
              onClick={(e) => setAnchorEl(e.currentTarget)}
              size="small"
            >
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', fontSize: 14 }}>
                {usuario?.nombre?.charAt(0)?.toUpperCase() || 'U'}
              </Avatar>
            </IconButton>
            <Menu
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              onClose={() => setAnchorEl(null)}
            >
              <MenuItem disabled>
                <Typography variant="body2" fontWeight={600}>{usuario?.nombre}</Typography>
              </MenuItem>
              <MenuItem onClick={() => { setAnchorEl(null); navigate('/perfil') }}>
                Mi Perfil
              </MenuItem>
              <MenuItem onClick={handleLogout}>Cerrar Sesión</MenuItem>
            </Menu>
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
