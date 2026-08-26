import { useState, useRef } from 'react'
import {
  Box, TextField, Button, Typography, Alert, CircularProgress,
  MenuItem, Select, InputLabel, FormControl, Grid, Paper,
  Card, CardMedia, IconButton
} from '@mui/material'
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera'
import DeleteIcon from '@mui/icons-material/Delete'
import { createMascota, updateMascota, subirArchivo } from '../../api/bffClient.js'
import { useNavigate, useParams } from 'react-router-dom'
import { useSnackbar } from '../../context/SnackbarContext.jsx'

const ESPECIES  = [{ value: 'PERRO', label: 'Perro' }, { value: 'GATO', label: 'Gato' }, { value: 'OTRO', label: 'Otro' }]
const ESTADOS   = [{ value: 'PERDIDA', label: 'Perdida' }, { value: 'ENCONTRADA', label: 'Encontrada' }]
const TAMANIOS  = [{ value: 'PEQUENO', label: 'Pequeño' }, { value: 'MEDIANO', label: 'Mediano' }, { value: 'GRANDE', label: 'Grande' }]

export default function MascotaForm({ initialData }) {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const showSnackbar = useSnackbar()

  const [form, setForm] = useState({
    nombre: initialData?.nombre || '',
    especie: initialData?.especie || 'PERRO',
    raza: initialData?.raza || '',
    color: initialData?.color || '',
    descripcion: initialData?.descripcion || '',
    tamano: initialData?.tamano || 'MEDIANO',
    estado: initialData?.estado || 'PERDIDA',
    latitud: initialData?.latitud?.toString() || '',
    longitud: initialData?.longitud?.toString() || '',
  })
  const [error,   setError]   = useState('')
  const [loading, setLoading] = useState(false)
  const [foto, setFoto] = useState(null)
  const [fotoPreview, setFotoPreview] = useState(null)
  const fileInputRef = useRef(null)
  const submittingRef = useRef(false)

  const onSubmit = async (e) => {
    e.preventDefault()
    if (submittingRef.current) return
    submittingRef.current = true
    setError('')
    setLoading(true)
    try {
      const payload = {
        ...form,
        latitud:  form.latitud  ? parseFloat(form.latitud)  : null,
        longitud: form.longitud ? parseFloat(form.longitud) : null,
      }
      if (isEdit) {
        await updateMascota(id, payload)
        if (foto) {
          await subirArchivo(foto, id)
        }
        showSnackbar('Mascota actualizada correctamente', 'success')
      } else {
        const { data: nuevaMascota } = await createMascota(payload)
        if (foto && nuevaMascota?.id) {
          try {
            await subirArchivo(foto, nuevaMascota.id)
          } catch {
            showSnackbar('Mascota creada, pero hubo un error al subir la foto', 'warning')
          }
        }
        showSnackbar('Mascota registrada correctamente', 'success')
      }
      navigate('/mascotas')
    } catch (err) {
      setError(err.response?.data?.mensaje || `Error al ${isEdit ? 'actualizar' : 'registrar'} la mascota`)
    } finally {
      setLoading(false)
      submittingRef.current = false
    }
  }

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleFotoChange = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    if (!file.type.startsWith('image/')) {
      setError('Solo se permiten archivos de imagen')
      return
    }
    setFoto(file)
    setFotoPreview(URL.createObjectURL(file))
  }

  const handleRemoveFoto = () => {
    setFoto(null)
    setFotoPreview(null)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

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

  return (
    <Paper elevation={2} sx={{ p: 4, maxWidth: 600, mx: 'auto' }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        {isEdit ? 'Editar mascota' : 'Registrar mascota'}
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box component="form" onSubmit={onSubmit} noValidate>
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
                {ESPECIES.map((e) => <MenuItem key={e.value} value={e.value}>{e.label}</MenuItem>)}
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
                {TAMANIOS.map((t) => <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12}>
            <TextField fullWidth multiline rows={3} label="Descripción"
              name="descripcion" value={form.descripcion} onChange={handleChange} />
          </Grid>
          <Grid item xs={12} sm={5}>
            <TextField fullWidth label="Latitud" name="latitud"
              value={form.latitud} onChange={handleChange} type="number" inputProps={{ step: 'any' }} />
          </Grid>
          <Grid item xs={12} sm={5}>
            <TextField fullWidth label="Longitud" name="longitud"
              value={form.longitud} onChange={handleChange} type="number" inputProps={{ step: 'any' }} />
          </Grid>
          <Grid item xs={12} sm={2} display="flex" alignItems="center">
            <Button variant="outlined" size="small" onClick={handleGeolocate} fullWidth>
              Mi ubicación
            </Button>
          </Grid>
        </Grid>

        {/* Subida de foto */}
        <Box mt={2} mb={1}>
          <input
            type="file"
            accept="image/*"
            hidden
            ref={fileInputRef}
            onChange={handleFotoChange}
            aria-label="Subir foto de la mascota"
          />
          {fotoPreview ? (
            <Card elevation={1} sx={{ position: 'relative', overflow: 'hidden' }}>
              <Box
                component="img"
                src={fotoPreview}
                alt="Vista previa"
                sx={{
                  width: '100%',
                  maxHeight: 250,
                  objectFit: 'contain',
                  display: 'block',
                  bgcolor: '#f5f5f5',
                }}
              />
              <IconButton
                size="small"
                onClick={handleRemoveFoto}
                aria-label="Quitar foto"
                sx={{
                  position: 'absolute', top: 8, right: 8,
                  bgcolor: 'rgba(0,0,0,0.6)', color: 'white',
                  '&:hover': { bgcolor: 'rgba(0,0,0,0.8)' },
                }}
              >
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Card>
          ) : (
            <Button
              variant="outlined"
              fullWidth
              startIcon={<PhotoCameraIcon />}
              onClick={() => fileInputRef.current?.click()}
              sx={{ py: 3, borderStyle: 'dashed' }}
            >
              Subir foto de la mascota
            </Button>
          )}
        </Box>

        <Button
          type="submit" variant="contained" size="large" fullWidth
          sx={{ mt: 3 }} disabled={loading}
        >
          {loading ? <CircularProgress size={24} color="inherit" /> : isEdit ? 'Actualizar mascota' : 'Registrar mascota'}
        </Button>
      </Box>
    </Paper>
  )
}
