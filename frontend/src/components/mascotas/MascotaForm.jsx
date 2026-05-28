import { useState } from 'react'
import {
  Box, TextField, Button, Typography, Alert, CircularProgress,
  MenuItem, Select, InputLabel, FormControl, Grid, Paper
} from '@mui/material'
import { createMascota } from '../../api/bffClient.js'
import { useAuth } from '../../hooks/useAuth.js'
import { useNavigate } from 'react-router-dom'

const ESPECIES  = ['Perro', 'Gato', 'Otro']
const ESTADOS   = [{ value: 'PERDIDA', label: 'Perdida' }, { value: 'ENCONTRADA', label: 'Encontrada' }]
const TAMANIOS  = ['Pequeño', 'Mediano', 'Grande']

export default function MascotaForm() {
  const { usuario } = useAuth()
  const navigate     = useNavigate()

  const [form, setForm] = useState({
    nombre: '', especie: 'Perro', raza: '', color: '',
    descripcion: '', tamano: 'Mediano', estado: 'PERDIDA',
    latitud: '', longitud: '',
  })
  const [error,   setError]   = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleGeolocate = () => {
    if (!navigator.geolocation) return
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => setForm((prev) => ({
        ...prev,
        latitud:  coords.latitude.toString(),
        longitud: coords.longitude.toString(),
      })),
      () => setError('No se pudo obtener la ubicación')
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await createMascota({
        ...form,
        latitud:    form.latitud  ? parseFloat(form.latitud)  : null,
        longitud:   form.longitud ? parseFloat(form.longitud) : null,
        usuarioId:  usuario.usuarioId,
      })
      navigate('/mascotas')
    } catch (err) {
      setError(err.response?.data?.message || 'Error al registrar la mascota')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Paper elevation={2} sx={{ p: 4, maxWidth: 600, mx: 'auto' }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Registrar mascota
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box component="form" onSubmit={handleSubmit} noValidate>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Nombre" name="nombre"
              value={form.nombre} onChange={handleChange} required />
          </Grid>
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Estado</InputLabel>
              <Select name="estado" value={form.estado} label="Estado" onChange={handleChange}>
                {ESTADOS.map((e) => <MenuItem key={e.value} value={e.value}>{e.label}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Especie</InputLabel>
              <Select name="especie" value={form.especie} label="Especie" onChange={handleChange}>
                {ESPECIES.map((e) => <MenuItem key={e} value={e}>{e}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Raza" name="raza"
              value={form.raza} onChange={handleChange} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Color" name="color"
              value={form.color} onChange={handleChange} required />
          </Grid>
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Tamaño</InputLabel>
              <Select name="tamano" value={form.tamano} label="Tamaño" onChange={handleChange}>
                {TAMANIOS.map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12}>
            <TextField fullWidth multiline rows={3} label="Descripción"
              name="descripcion" value={form.descripcion} onChange={handleChange} />
          </Grid>
          <Grid item xs={12} sm={5}>
            <TextField fullWidth label="Latitud" name="latitud"
              value={form.latitud} onChange={handleChange} type="number" />
          </Grid>
          <Grid item xs={12} sm={5}>
            <TextField fullWidth label="Longitud" name="longitud"
              value={form.longitud} onChange={handleChange} type="number" />
          </Grid>
          <Grid item xs={12} sm={2} display="flex" alignItems="center">
            <Button variant="outlined" size="small" onClick={handleGeolocate} fullWidth>
              Mi ubicación
            </Button>
          </Grid>
        </Grid>

        <Button
          type="submit" variant="contained" size="large" fullWidth
          sx={{ mt: 3 }} disabled={loading}
        >
          {loading ? <CircularProgress size={24} color="inherit" /> : 'Registrar mascota'}
        </Button>
      </Box>
    </Paper>
  )
}
