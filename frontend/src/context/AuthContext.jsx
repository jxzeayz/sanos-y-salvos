import { createContext, useState, useCallback } from 'react'
import { login as apiLogin, register as apiRegister } from '../api/bffClient.js'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token,   setToken]   = useState(() => localStorage.getItem('token'))
  const [usuario, setUsuario] = useState(() => {
    const u = localStorage.getItem('usuario')
    return u ? JSON.parse(u) : null
  })

  const login = useCallback(async (email, password) => {
    const { data } = await apiLogin({ email, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('usuario', JSON.stringify(data))
    setToken(data.token)
    setUsuario(data)
    return data
  }, [])

  const register = useCallback(async (formData) => {
    const { data } = await apiRegister(formData)
    localStorage.setItem('token', data.token)
    localStorage.setItem('usuario', JSON.stringify(data))
    setToken(data.token)
    setUsuario(data)
    return data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('usuario')
    setToken(null)
    setUsuario(null)
  }, [])

  return (
    <AuthContext.Provider value={{ token, usuario, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
