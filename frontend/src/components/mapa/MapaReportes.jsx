import { useEffect, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet'
import { Box, Typography, CircularProgress, Alert, Chip } from '@mui/material'
import L from 'leaflet'
import { getReportesMapa, getArchivosMascota } from '../../api/bffClient.js'

const iconoPerdida = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34],
})
const iconoEncontrada = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34],
})

const RADIO_ANONIMIZACION = 500

const ESTADO_LABEL = { PERDIDA: 'Perdida', ENCONTRADA: 'Encontrada', REUNIFICADA: 'Reunificada' }
const ESPECIE_LABEL = { PERRO: 'Perro', GATO: 'Gato', OTRO: 'Otro' }

function PopupMascota({ reporte }) {
  const [fotoUrl, setFotoUrl] = useState(null)

  useEffect(() => {
    const mascotaId = reporte.mascotaId || reporte.id
    if (!mascotaId) return
    let cancelled = false
    getArchivosMascota(mascotaId)
      .then(({ data }) => {
        if (!cancelled && data && data.length > 0) {
          setFotoUrl(`/bff/archivos/${data[0].id}/descargar`)
        }
      })
      .catch(() => {})
    return () => { cancelled = true }
  }, [reporte.mascotaId, reporte.id])

  return (
    <div style={{ minWidth: 200, maxWidth: 260 }}>
      {fotoUrl && (
        <img
          src={fotoUrl}
          alt={reporte.nombre}
          style={{ width: '100%', height: 120, objectFit: 'contain', borderRadius: 4, marginBottom: 8, bgcolor: '#f5f5f5' }}
        />
      )}
      <strong style={{ fontSize: 14 }}>{reporte.nombre || 'Mascota'}</strong>
      <br />
      <span style={{ fontSize: 12, color: '#666' }}>
        {ESPECIE_LABEL[reporte.especie] || reporte.especie} {reporte.raza ? `· ${reporte.raza}` : ''}
      </span>
      <br />
      <span style={{
        display: 'inline-block', fontSize: 11, padding: '2px 6px', borderRadius: 4,
        background: reporte.estado === 'PERDIDA' ? '#ffebee' : '#e8f5e9',
        color: reporte.estado === 'PERDIDA' ? '#c62828' : '#2e7d32',
        marginTop: 4, marginBottom: 4
      }}>
        {ESTADO_LABEL[reporte.estado] || reporte.estado}
      </span>
      <br />
      {reporte.color && (
        <span style={{ fontSize: 12, color: '#666' }}>Color: {reporte.color}<br /></span>
      )}
      {reporte.descripcion && (
        <span style={{ fontSize: 12, color: '#444', fontStyle: 'italic' }}>
          "{reporte.descripcion}"
        </span>
      )}
    </div>
  )
}

export default function MapaReportes() {
  const [reportes, setReportes] = useState([])
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState('')

  useEffect(() => {
    getReportesMapa()
      .then(({ data }) => setReportes(data || []))
      .catch(() => setError('No se pudieron cargar los reportes del mapa'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return (
    <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>
  )
  if (error) return <Alert severity="warning" sx={{ m: 2 }}>{error}</Alert>

  const centro = reportes.length > 0
    ? [reportes[0].latitud ?? -33.45, reportes[0].longitud ?? -70.65]
    : [-33.45, -70.65]

  return (
    <Box>
      <Box display="flex" gap={1} mb={1} flexWrap="wrap">
        <Chip color="error"   size="small" label="Perdida" />
        <Chip color="success" size="small" label="Encontrada" />
        <Typography variant="caption" color="text.secondary" alignSelf="center">
          · Las ubicaciones se muestran con imprecisión de 500m para proteger la privacidad
        </Typography>
      </Box>

      <MapContainer
        center={centro}
        zoom={13}
        style={{ height: '500px', width: '100%', borderRadius: 8 }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {reportes.map((r) => r.latitud && r.longitud ? (
          <Marker
            key={r.id}
            position={[r.latitud, r.longitud]}
            icon={r.estado === 'PERDIDA' ? iconoPerdida : iconoEncontrada}
          >
            <Popup>
              <PopupMascota reporte={r} />
            </Popup>
            <Circle
              center={[r.latitud, r.longitud]}
              radius={RADIO_ANONIMIZACION}
              pathOptions={{ color: r.estado === 'PERDIDA' ? 'red' : 'green', fillOpacity: 0.05 }}
            />
          </Marker>
        ) : null)}
      </MapContainer>
    </Box>
  )
}
