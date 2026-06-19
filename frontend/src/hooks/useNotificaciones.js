import { useEffect, useState, useCallback } from 'react'
import { getNotificaciones, marcarNotificacionLeida, marcarTodasLeidas } from '../api/bffClient.js'

export function useNotificaciones(pollingInterval = 30000) {
  const [notificaciones, setNotificaciones] = useState([])
  const [noLeidasCount, setNoLeidasCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const cargarNotificaciones = useCallback(async () => {
    try {
      const { data } = await getNotificaciones()
      const lista = data || []
      setNotificaciones(lista)
      setNoLeidasCount(lista.filter((n) => !n.leida).length)
      setError(null)
    } catch (err) {
      setError(err.response?.data?.mensaje || 'Error al cargar notificaciones')
    } finally {
      setLoading(false)
    }
  }, [])

  const marcarLeida = useCallback(async (id) => {
    try {
      await marcarNotificacionLeida(id)
      setNotificaciones((prev) =>
        prev.map((n) => (n.id === id ? { ...n, leida: true } : n))
      )
      setNoLeidasCount((prev) => Math.max(0, prev - 1))
    } catch (err) {
      console.error('Error al marcar notificación como leída:', err)
    }
  }, [])

  const marcarTodas = useCallback(async () => {
    try {
      await marcarTodasLeidas()
      setNotificaciones((prev) => prev.map((n) => ({ ...n, leida: true })))
      setNoLeidasCount(0)
    } catch (err) {
      console.error('Error al marcar todas como leídas:', err)
    }
  }, [])

  useEffect(() => {
    if (!localStorage.getItem('token')) {
      setLoading(false)
      return
    }
    cargarNotificaciones()
    const interval = setInterval(() => {
      if (localStorage.getItem('token')) {
        cargarNotificaciones()
      }
    }, pollingInterval)
    return () => clearInterval(interval)
  }, [cargarNotificaciones, pollingInterval])

  return {
    notificaciones,
    noLeidasCount,
    loading,
    error,
    recargar: cargarNotificaciones,
    marcarLeida,
    marcarTodas,
  }
}