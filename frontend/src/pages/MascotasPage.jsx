import { useEffect, useState } from 'react'
import {
  Box, Typography, Grid, Button, ToggleButtonGroup,
  ToggleButton, CircularProgress, Alert
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import { Link } from 'react-router-dom'
import { getMascotas, getCoincidencias } from '../api/bffClient.js'
import MascotaCard from '../components/mascotas/MascotaCard.jsx'
import CoincidenciaList from '../components/coincidencias/CoincidenciaList.jsx'

export default function MascotasPage() {
  const [mascotas,      setMascotas]      = useState([])
  const [filtro,        setFiltro]        = useState('')
  const [loading,       setLoading]       = useState(true)
  const [error,         setError]         = useState('')
  const [coincidencias, setCoincidencias] = useState(null)
  const [loadingCoinc,  setLoadingCoinc]  = useState(false)
  const [errorCoinc,    setErrorCoinc]    = useState('')

  useEffect(() => {
    setLoading(true)
    getMascotas(filtro || undefined)
      .then(({ data }) => setMascotas(data || []))
      .catch(() => setError('Error al cargar las mascotas'))
      .finally(() => setLoading(false))
  }, [filtro])

  const verCoincidencias = (mascotaId) => {
    setCoincidencias(null)
    setErrorCoinc('')
    setLoadingCoinc(true)
    getCoincidencias(mascotaId)
      .then(({ data }) => setCoincidencias(data || []))
      .catch(() => setErrorCoinc('Error al cargar coincidencias'))
      .finally(() => setLoadingCoinc(false))
  }

  return (
    <Box sx={{ maxWidth: 1000, mx: 'auto', py: 4, px: 2 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3} flexWrap="wrap" gap={2}>
        <Typography variant="h5" fontWeight={700}>Mascotas reportadas</Typography>
        <Button variant="contained" startIcon={<AddIcon />} component={Link} to="/mascotas/nueva">
          Nueva mascota
        </Button>
      </Box>

      <ToggleButtonGroup
        value={filtro} exclusive
        onChange={(_, v) => setFiltro(v ?? '')}
        sx={{ mb: 3 }}
      >
        <ToggleButton value="">Todas</ToggleButton>
        <ToggleButton value="PERDIDA">Perdidas</ToggleButton>
        <ToggleButton value="ENCONTRADA">Encontradas</ToggleButton>
        <ToggleButton value="REUNIFICADA">Reunificadas</ToggleButton>
      </ToggleButtonGroup>

      {loading && <Box display="flex" justifyContent="center"><CircularProgress /></Box>}
      {error   && <Alert severity="error">{error}</Alert>}

      <Grid container spacing={2}>
        {mascotas.map((m) => (
          <Grid item xs={12} sm={6} md={4} key={m.id}>
            <MascotaCard mascota={m} onVerCoincidencias={verCoincidencias} />
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
