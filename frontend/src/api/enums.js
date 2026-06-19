export const ESTADOS_MASCOTA = {
  PERDIDA: 'Perdida',
  ENCONTRADA: 'Encontrada',
  REUNIFICADA: 'Reunificada',
}

export const ESTADO_MASCOTA_OPTIONS = [
  { value: 'PERDIDA', label: 'Perdida' },
  { value: 'ENCONTRADA', label: 'Encontrada' },
  { value: 'REUNIFICADA', label: 'Reunificada' },
]

export const ESPECIES_MASCOTA = ['Perro', 'Gato', 'Otro']

export const TAMANOS_MASCOTA = ['Pequeño', 'Mediano', 'Grande']

export const ROLES_USUARIO = {
  DUEÑO: 'DUEÑO',
  CIUDADANO: 'CIUDADANO',
  VETERINARIO: 'VETERINARIO',
  ADMIN: 'ADMIN',
}

export const ROLES_OPTIONS = [
  { value: 'DUEÑO', label: 'Dueño' },
  { value: 'CIUDADANO', label: 'Ciudadano' },
  { value: 'VETERINARIO', label: 'Veterinario' },
  { value: 'ADMIN', label: 'Administrador' },
]

export const TIPO_NOTIFICACION = {
  COINCIDENCIA: 'COINCIDENCIA',
  MASCOTA_ENCONTRADA: 'MASCOTA_ENCONTRADA',
  MASCOTA_REUNIFICADA: 'MASCOTA_REUNIFICADA',
  COMENTARIO: 'COMENTARIO',
  SISTEMA: 'SISTEMA',
}

export const TIPO_NOTIFICACION_LABEL = {
  COINCIDENCIA: 'Coincidencia detectada',
  MASCOTA_ENCONTRADA: 'Mascota encontrada',
  MASCOTA_REUNIFICADA: 'Mascota reunificada',
  COMENTARIO: 'Nuevo comentario',
  SISTEMA: 'Notificación del sistema',
}

export const ESTADO_COLOR = {
  PERDIDA: 'error',
  ENCONTRADA: 'success',
  REUNIFICADA: 'info',
}

export const COINCIDENCIA_ESTADO = {
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  RECHAZADA: 'Rechazada',
}

export const COINCIDENCIA_ESTADO_COLOR = {
  PENDIENTE: 'warning',
  CONFIRMADA: 'success',
  RECHAZADA: 'error',
}