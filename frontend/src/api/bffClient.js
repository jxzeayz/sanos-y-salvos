import axios from 'axios'

const bff = axios.create({
  baseURL: '/bff',
  headers: { 'Content-Type': 'application/json' },
})

let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error)
    else prom.resolve(token)
  })
  failedQueue = []
}

bff.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
  
bff.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config

    if (err.response?.status === 401 && !originalRequest._retry) {
      if (originalRequest.url === '/auth/refresh') {
        localStorage.removeItem('token')
        localStorage.removeItem('usuario')
        window.location.href = '/login'
        return Promise.reject(err)
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return bff(originalRequest)
          })
          .catch((err2) => Promise.reject(err2))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const oldToken = localStorage.getItem('token')
        const { data } = await bff.post('/auth/refresh', { token: oldToken })
        const newToken = data.token
        localStorage.setItem('token', newToken)
        localStorage.setItem('usuario', JSON.stringify(data))
        processQueue(null, newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return bff(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        localStorage.removeItem('token')
        localStorage.removeItem('usuario')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(err)
  }
)

// Auth
export const login           = (data) => bff.post('/auth/login', data)
export const register        = (data) => bff.post('/auth/register', data)
export const refreshTokenAPI = (data) => bff.post('/auth/refresh', data)
export const changePassword  = (data) => bff.put('/auth/password', data)
export const updateProfile   = (data) => bff.put('/auth/perfil', data)
export const getProfile      = ()    => bff.get('/auth/perfil')

// Mascotas
export const getMascotas       = (estado) => bff.get('/mascotas', { params: estado ? { estado } : {} })
export const getMisMascotas    = ()       => bff.get('/mascotas/mis-mascotas')
export const getMascota        = (id)     => bff.get(`/mascotas/${id}`)
export const createMascota     = (data)   => bff.post('/mascotas', data)
export const updateMascota     = (id, data) => bff.put(`/mascotas/${id}`, data)
export const deleteMascota     = (id)     => bff.delete(`/mascotas/${id}`)
export const updateMascotaEstado = (id, estado) => bff.patch(`/mascotas/${id}/estado?estado=${estado}`)
export const getCoincidencias  = (id)     => bff.get(`/mascotas/${id}/coincidencias`)

// Mapa
export const getReportesMapa   = ()       => bff.get('/mascotas/mapa')

// Notificaciones
export const getNotificaciones         = (soloNoLeidas) =>
  bff.get('/notificaciones', { params: soloNoLeidas ? { soloNoLeidas: 'true' } : {} })
export const getNotificacion           = (id) => bff.get(`/notificaciones/${id}`)
export const marcarNotificacionLeida   = (id) => bff.patch(`/notificaciones/${id}/leer`)
export const marcarTodasLeidas         = ()   => bff.patch('/notificaciones/leer-todas')
export const crearNotificacion         = (data) => bff.post('/notificaciones', data)

// Archivos
export const subirArchivo              = (archivo, mascotaId) => {
  const formData = new FormData()
  formData.append('archivo', archivo)
  return bff.post('/archivos/subir', formData, {
    params: { mascotaId },
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
export const getArchivo                = (id) => bff.get(`/archivos/${id}`)
export const getArchivosMascota        = (mascotaId) => bff.get(`/archivos/mascota/${mascotaId}`)
export const descargarArchivo          = (id) => bff.get(`/archivos/${id}/descargar`, { responseType: 'blob' })
export const deleteArchivo             = (id) => bff.delete(`/archivos/${id}`)

// Auditoría (solo ADMIN)
export const getAuditoriaLogs          = (params) => bff.get('/auditoria/logs', { params })
export const getAuditoriaLog           = (id)   => bff.get(`/auditoria/log/${id}`)
export const getAuditoriaEstadisticas  = (params) => bff.get('/auditoria/estadisticas', { params })

// Contacto (solo con match confirmado)
export const getContactoMascota        = (mascotaId) => bff.get(`/contacto/mascota/${mascotaId}`)

// Usuarios (solo ADMIN)
export const getUsuarios               = ()     => bff.get('/auth/usuarios')
export const eliminarUsuario           = (id)   => bff.delete(`/auth/usuarios/${id}`)
