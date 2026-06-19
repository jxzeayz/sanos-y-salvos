import { useEffect, useState } from 'react'
import {
  Box, Typography, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, CircularProgress, Alert, Button, Tabs, Tab
} from '@mui/material'
import { getMascotas } from '../api/bffClient.js'

export default function AdminPage() {
  const [tab, setTab] = useState(0)
  const [mascotas, setMascotas] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    getMascotas()
      .then(({ data }) => setMascotas(data || []))
      .catch(() => setError('Error al cargar datos'))
      .finally(() => setLoading(false))
  }, [])

  const resumen = {
    total: mascotas.length,
    perdidas: mascotas.filter((m) => m.estado === 'PERDIDA').length,
    encontradas: mascotas.filter((m) => m.estado === 'ENCONTRADA').length,
    reunificadas: mascotas.filter((m) => m.estado === 'REUNIFICADA').length,
  }

  if (loading) return <Box display="flex" justifyContent="center" py={4}><CircularProgress /></Box>
  if (error) return <Alert severity="error" sx={{ maxWidth: 1000, mx: 'auto', mt: 4 }}>{error}</Alert>

  return (
    <Box sx={{ maxWidth: 1000, mx: 'auto', py: 4, px: 2 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>Panel de Administración</Typography>

      <Box display="flex" gap={2} flexWrap="wrap" mb={3}>
        <Paper elevation={2} sx={{ p: 2, textAlign: 'center', minWidth: 120 }}>
          <Typography variant="h4" fontWeight={700}>{resumen.total}</Typography>
          <Typography variant="body2" color="text.secondary">Total mascotas</Typography>
        </Paper>
        <Paper elevation={2} sx={{ p: 2, textAlign: 'center', minWidth: 120, borderTop: 3, borderColor: 'error.main' }}>
          <Typography variant="h4" fontWeight={700} color="error">{resumen.perdidas}</Typography>
          <Typography variant="body2" color="text.secondary">Perdidas</Typography>
        </Paper>
        <Paper elevation={2} sx={{ p: 2, textAlign: 'center', minWidth: 120, borderTop: 3, borderColor: 'success.main' }}>
          <Typography variant="h4" fontWeight={700} color="success.main">{resumen.encontradas}</Typography>
          <Typography variant="body2" color="text.secondary">Encontradas</Typography>
        </Paper>
        <Paper elevation={2} sx={{ p: 2, textAlign: 'center', minWidth: 120, borderTop: 3, borderColor: 'info.main' }}>
          <Typography variant="h4" fontWeight={700} color="info.main">{resumen.reunificadas}</Typography>
          <Typography variant="body2" color="text.secondary">Reunificadas</Typography>
        </Paper>
      </Box>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="Todas las mascotas" />
      </Tabs>

      <TableContainer component={Paper} elevation={2}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Nombre</TableCell>
              <TableCell>Especie</TableCell>
              <TableCell>Estado</TableCell>
              <TableCell>Usuario ID</TableCell>
              <TableCell>Fecha</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {mascotas.map((m) => (
              <TableRow key={m.id}>
                <TableCell>{m.id}</TableCell>
                <TableCell>{m.nombre}</TableCell>
                <TableCell>{m.especie}</TableCell>
                <TableCell>
                  <Chip
                    label={m.estado}
                    size="small"
                    color={m.estado === 'PERDIDA' ? 'error' : m.estado === 'ENCONTRADA' ? 'success' : 'info'}
                  />
                </TableCell>
                <TableCell>{m.usuarioId}</TableCell>
                <TableCell>{m.fechaReporte ? new Date(m.fechaReporte).toLocaleDateString('es-CL', { timeZone: 'America/Santiago' }) : '-'}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  )
}