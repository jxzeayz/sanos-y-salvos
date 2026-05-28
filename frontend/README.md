# Frontend — Sanos y Salvos

Aplicación web construida con **React 18 + Vite** que consume el BFF-Web para gestionar mascotas perdidas y encontradas.

## Tecnologías y paquetes NPM

| Paquete | Versión | Uso |
|---|---|---|
| react | ^18.3.1 | UI declarativa con componentes |
| react-dom | ^18.3.1 | Renderizado en el DOM |
| react-router-dom | ^6.24.0 | Enrutamiento SPA (rutas protegidas) |
| @mui/material | ^5.15.20 | Sistema de diseño Material UI |
| @mui/icons-material | ^5.15.20 | Íconos Material Design |
| @emotion/react | ^11.11.4 | Motor CSS-in-JS de MUI |
| @emotion/styled | ^11.11.5 | Componentes estilizados |
| axios | ^1.7.2 | Cliente HTTP hacia el BFF |
| leaflet | ^1.9.4 | Mapa interactivo |
| react-leaflet | ^4.2.1 | Integración React de Leaflet |

## Estructura del proyecto

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── api/
│   │   └── bffClient.js          # Cliente Axios configurado
│   ├── components/
│   │   ├── auth/
│   │   │   ├── LoginForm.jsx
│   │   │   └── RegisterForm.jsx
│   │   ├── mascotas/
│   │   │   ├── MascotaCard.jsx
│   │   │   └── MascotaForm.jsx
│   │   ├── coincidencias/
│   │   │   └── CoincidenciaList.jsx
│   │   ├── mapa/
│   │   │   └── MapaReportes.jsx
│   │   └── ui/
│   │       └── Navbar.jsx
│   ├── context/
│   │   └── AuthContext.jsx       # Estado global de autenticación
│   ├── hooks/
│   │   └── useAuth.js            # Hook de acceso al contexto
│   ├── pages/
│   │   ├── HomePage.jsx
│   │   ├── LoginPage.jsx
│   │   ├── RegisterPage.jsx
│   │   ├── MascotasPage.jsx
│   │   ├── NuevaMascotaPage.jsx
│   │   ├── MapaPage.jsx
│   │   └── CoincidenciasPage.jsx
│   ├── App.jsx                   # Rutas principales
│   └── main.jsx                  # Punto de entrada
├── package.json
└── vite.config.js
```

## Requisitos

- Node.js 18+
- npm 9+

## Instalación y ejecución local

```bash
# Instalar dependencias
npm install

# Modo desarrollo (requiere BFF corriendo en :8080)
npm run dev
```

La app quedará disponible en http://localhost:5173

## Build de producción

```bash
npm run build
```

Los archivos estáticos se generan en `dist/`.

## Ejecución con Docker

El frontend se sirve con **nginx** en el puerto 3000:

```bash
docker build -t sanos-frontend .
docker run -p 3000:3000 sanos-frontend
```

O usar Docker Compose desde la raíz del proyecto:

```bash
docker compose up frontend
```

## Variables de entorno

El BFF URL se configura en `vite.config.js`:

```js
// Para desarrollo local
proxy: { '/api': 'http://localhost:8080' }
```

## Páginas disponibles

| Ruta | Descripción | Protegida |
|---|---|---|
| `/` | Página de inicio | No |
| `/login` | Formulario de autenticación | No |
| `/register` | Registro de usuario | No |
| `/mapa` | Mapa de reportes (Leaflet) | No |
| `/mascotas` | Listado de mis mascotas | Sí |
| `/mascotas/nueva` | Registrar mascota perdida/encontrada | Sí |
| `/coincidencias` | Ver coincidencias detectadas | Sí |
