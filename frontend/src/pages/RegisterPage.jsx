import { Box } from '@mui/material'
import RegisterForm from '../components/auth/RegisterForm.jsx'

export default function RegisterPage() {
  return (
    <Box sx={{ minHeight: '80vh', display: 'flex', alignItems: 'center', px: 2 }}>
      <RegisterForm />
    </Box>
  )
}
