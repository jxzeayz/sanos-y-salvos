import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'

const theme = createTheme({
  palette: {
    primary:   { main: '#1a3a5c' },
    secondary: { main: '#f5a623' },
  },
  typography: {
    fontFamily: '"Segoe UI", Roboto, sans-serif',
  },
})

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>
          <App />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </React.StrictMode>
)
