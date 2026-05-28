import axios from 'axios'

const bff = axios.create({
  baseURL: '/bff',
  headers: { 'Content-Type': 'application/json' },
})

bff.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

bff.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// Auth
export const login    = (data) => bff.post('/auth/login', data)
export const register = (data) => bff.post('/auth/register', data)

// Mascotas
export const getMascotas       = (estado) => bff.get('/mascotas', { params: estado ? { estado } : {} })
export const getMascota        = (id)     => bff.get(`/mascotas/${id}`)
export const createMascota     = (data)   => bff.post('/mascotas', data)
export const getCoincidencias  = (id)     => bff.get(`/mascotas/${id}/coincidencias`)

// Mapa
export const getReportesMapa   = ()       => bff.get('/mascotas/mapa')
