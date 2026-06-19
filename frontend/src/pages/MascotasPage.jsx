import { useEffect, useState, useCallback } from 'react'
import {
  Box, Typography, Grid, Button, ToggleButtonGroup,
  ToggleButton, CircularProgress, Alert, TextField, InputAdornment
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import SearchIcon from '@mui/icons-material/Search'
import { Link } from 'react-router-dom'
import { getMisMascotas, getMascotas, getCoincidencias, deleteMascota, updateMascotaEstado } from '../api/bffClient.js'
import MascotaCard from '../components/mascotas/MascotaCard.jsx'
import CoincidenciaList from '../components/coincidencias/CoincidenciaList.jsx'
import { useSnackbar } from '../context/SnackbarContext.jsx'
import { useAuth } from '../hooks/useAuth.js'

export default function MascotasPage() {
  const showSnackbar = useSnackbar()
  const { usuario } = useAuth()
  const [mascotas,      setMascotas]      = useState([])
  const [filtro,        setFiltro]        = useState('mis')
  const [busqueda,      setBusqueda]      = useState('')
  const [loading,       setLoading]       = useState(true)
  const [error,         setError]         = useState('')
  const [coincidencias, setCoincidencias] = useState(null)
  const [loadingCoinc,  setLoadingCoinc]  = useState(false)
  const [errorCoinc,    setErrorCoinc]    = useState('')

  const cargarMascotas = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      let data = []
      if (filtro === 'mis') {
        const res = await getMisMascotas()
        data = res.data || []
      } else if (filtro === 'todas') {
        const res = await getMascotas()
        data = res.data || []
      } else {
        const res = await getMascotas(filtro)
        data = res.data || []
      }

      if (busqueda) {
        const q = busqueda.toLowerCase()
        data = data.filter((m) =>
          m.nombre?.toLowerCase().includes(q) ||
          m.especie?.toLowerCase().includes(q) ||
          m.raza?.toLowerCase().includes(q) ||
          m.color?.toLowerCase().includes(q)
        )
      }
      setMascotas(data)
    } catch {
      setError('Error al cargar las mascotas')
    } finally {
      setLoading(false)
    }
  }, [filtro, busqueda])

  useEffect(() => { cargarMascotas() }, [cargarMascotas])

  const verCoincidencias = (mascotaId) => {
    setCoincidencias(null)
    setErrorCoinc('')
    setLoadingCoinc(true)
    getCoincidencias(mascotaId)
      .then(({ data }) => setCoincidencias(data || []))
      .catch(() => setErrorCoinc('Error al cargar coincidencias'))
      .finally(() => setLoadingCoinc(false))
  }

  const handleEliminar = async (id) => {
    if (!window.confirm('¿Estás seguro de eliminar esta mascota?')) return
    try {
      await deleteMascota(id)
      showSnackbar('Mascota eliminada correctamente', 'success')
      cargarMascotas()
    } catch {
      showSnackbar('Error al eliminar la mascota', 'error')
    }
  }

  const handleCambiarEstado = async (id, nuevoEstado) => {
    try {
      await updateMascotaEstado(id, nuevoEstado)
      showSnackbar(`Estado actualizado a ${nuevoEstado}`, 'success')
      cargarMascotas()
    } catch {
      showSnackbar('Error al actualizar estado', 'error')
    }
  }

  return (
    <Box sx={{ maxWidth: 1000, mx: 'auto', py: 4, px: 2 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3} flexWrap="wrap" gap={2}>
        <Typography variant="h5" fontWeight={700}>Mascotas</Typography>
        <Button variant="contained" startIcon={<AddIcon />} component={Link} to="/mascotas/nueva">
          Nueva mascota
        </Button>
      </Box>

      <Box display="flex" gap={2} flexWrap="wrap" mb={3} alignItems="center">
        <TextField
          size="small"
          placeholder="Buscar por nombre, raza, color..."
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          InputProps={{
            startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>,
          }}
          sx={{ minWidth: 250 }}
        />
        <ToggleButtonGroup
          value={filtro} exclusive
          onChange={(_, v) => setFiltro(v ?? 'mis')}
          size="small"
        >
          <ToggleButton value="todas">Todas</ToggleButton>
          <ToggleButton value="mis">Mis mascotas</ToggleButton>
          <ToggleButton value="PERDIDA">Perdidas</ToggleButton>
          <ToggleButton value="ENCONTRADA">Encontradas</ToggleButton>
          <ToggleButton value="REUNIFICADA">Reunificadas</ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {loading && <Box display="flex" justifyContent="center" py={4}><CircularProgress /></Box>}
      {error   && <Alert severity="error">{error}</Alert>}

      {!loading && !error && mascotas.length === 0 && (
        <Alert severity="info" sx={{ mt: 2 }}>
          {filtro === 'mis'
            ? <>No tienes mascotas registradas.{' '}
              <Link to="/mascotas/nueva" style={{ fontWeight: 600 }}>Registra tu primera mascota</Link></>
            : 'No se encontraron mascotas con este filtro.'}
        </Alert>
      )}

      <Grid container spacing={2}>
        {mascotas.map((m) => (
          <Grid item xs={12} sm={6} md={4} key={m.id}>
            <MascotaCard
              mascota={m}
              usuarioId={usuario?.usuarioId}
              onVerCoincidencias={verCoincidencias}
              onEliminar={handleEliminar}
              onCambiarEstado={handleCambiarEstado}
            />
          </Grid>
        ))}
      </Grid>

      {coincidencias !== null && (
        <Box mt={4}>
          <Typography variant="h6" fontWeight={600} gutterBottom>
            Coincidencias encontradas
          </Typography>
          <CoincidenciaList
            coincidencias={coincidencias}
            loading={loadingCoinc}
            error={errorCoinc}
          />
        </Box>
      )}
    </Box>
  )
}
