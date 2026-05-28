import { useEffect, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet'
import { Box, Typography, CircularProgress, Alert, Chip } from '@mui/material'
import L from 'leaflet'
import { getReportesMapa } from '../../api/bffClient.js'

// Íconos diferenciados por estado
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

const RADIO_ANONIMIZACION = 500  // metros — privacidad por diseño

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
              <strong>{r.nombre || 'Mascota'}</strong><br />
              {r.especie} {r.raza ? `· ${r.raza}` : ''}<br />
              Estado: {r.estado}<br />
              {r.descripcion && <span>{r.descripcion}</span>}
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
