import { Box } from '@mui/material'
import LoginForm from '../components/auth/LoginForm.jsx'

export default function LoginPage() {
  return (
    <Box sx={{ minHeight: '80vh', display: 'flex', alignItems: 'center', px: 2 }}>
      <LoginForm />
    </Box>
  )
}
