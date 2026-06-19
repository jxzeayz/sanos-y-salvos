import { useEffect, useState } from 'react'
import { Box, Typography, CircularProgress, Alert } from '@mui/material'
import MascotaForm from '../components/mascotas/MascotaForm.jsx'
import { useParams } from 'react-router-dom'
import { getMascota } from '../api/bffClient.js'

export default function NuevaMascotaPage() {
  const { id } = useParams()
  const [mascota, setMascota] = useState(null)
  const [loading, setLoading] = useState(Boolean(id))
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    setLoading(true)
    getMascota(id)
      .then(({ data }) => setMascota(data))
      .catch(() => setError('Error al cargar los datos de la mascota'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <Box display="flex" justifyContent="center" py={4}><CircularProgress /></Box>
  if (error) return <Alert severity="error" sx={{ maxWidth: 700, mx: 'auto', mt: 4 }}>{error}</Alert>

  return (
    <Box sx={{ maxWidth: 700, mx: 'auto', py: 4, px: 2 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        {id ? 'Editar mascota' : 'Registrar nueva mascota'}
      </Typography>
      <MascotaForm initialData={mascota} />
    </Box>
  )
}
