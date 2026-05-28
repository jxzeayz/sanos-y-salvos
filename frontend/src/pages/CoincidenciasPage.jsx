import { useEffect, useState } from 'react'
import { Box, Typography, TextField, Button } from '@mui/material'
import { getCoincidencias } from '../api/bffClient.js'
import CoincidenciaList from '../components/coincidencias/CoincidenciaList.jsx'

export default function CoincidenciasPage() {
  const [mascotaId,     setMascotaId]     = useState('')
  const [coincidencias, setCoincidencias] = useState([])
  const [loading,       setLoading]       = useState(false)
  const [error,         setError]         = useState('')
  const [buscado,       setBuscado]       = useState(false)

  const buscar = () => {
    if (!mascotaId) return
    setLoading(true)
    setError('')
    setBuscado(true)
    getCoincidencias(mascotaId)
      .then(({ data }) => setCoincidencias(data || []))
      .catch(() => setError('Error al buscar coincidencias'))
      .finally(() => setLoading(false))
  }

  return (
    <Box sx={{ maxWidth: 700, mx: 'auto', py: 4, px: 2 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Coincidencias
      </Typography>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        Ingresa el ID de tu mascota perdida para ver las posibles coincidencias detectadas.
      </Typography>

      <Box display="flex" gap={2} mt={2} mb={3}>
        <TextField
          label="ID de mascota"
          value={mascotaId}
          onChange={(e) => setMascotaId(e.target.value)}
          type="number"
          size="small"
          sx={{ width: 200 }}
        />
        <Button variant="contained" onClick={buscar} disabled={!mascotaId}>
          Buscar
        </Button>
      </Box>

      {buscado && (
        <CoincidenciaList
          coincidencias={coincidencias}
          loading={loading}
          error={error}
        />
      )}
    </Box>
  )
}
