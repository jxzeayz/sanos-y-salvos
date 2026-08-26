# Cumplimiento de Requisitos de Evaluación
## Proyecto Sanos y Salvos — DSY1106 Desarrollo Fullstack III
**Estudiantes:** Julio Soto | Armando Calderón
**Docente:** Jorge Canales Soto
**Fecha:** Agosto 2026

---

## 1. Introducción

Este documento verifica, punto por punto, que el proyecto **Sanos y Salvos** cumple con las características mínimas exigidas para el software que se usará durante el semestre en las distintas evaluaciones:

1. Mínimo 20 Requisitos de Software.
2. Un login de acceso.
3. Un menú principal.
4. Al menos 1 mantenedor completo (CRUD: Insertar, Consultar, Actualizar, Borrar).
5. Al menos 1 proceso.
6. Al menos un reporte o consulta por pantalla.

Para cada punto se indica **dónde** está implementado en el repositorio (archivo/clase concreta) y **cómo funciona**, de modo que cualquier evaluador pueda verificarlo directamente en el código sin ambigüedad.

---

## 2. Mínimo 20 Requisitos de Software ✅

El detalle completo de los requisitos (funcionales y no funcionales) está en un documento aparte: **[requisitos-software.md](./requisitos-software.md)**.

Resumen:

- **28 Requisitos Funcionales** (RF-01 a RF-28), agrupados en: autenticación y usuarios, gestión de mascotas (mantenedor CRUD), motor de coincidencias (proceso), notificaciones y auditoría, geolocalización y reportes.
- **7 Requisitos No Funcionales** (RNF-01 a RNF-07): arquitectura de microservicios, mensajería asíncrona, tolerancia a fallos, cifrado de contraseñas, autenticación JWT, rate limiting y despliegue con Docker.
- **Total: 35 requisitos**, todos verificables en el código actual — ninguno describe funcionalidad futura o planificada.

Esto **supera ampliamente** el mínimo de 20 exigido.

---

## 3. Login de Acceso ✅

### ¿Dónde está?
- Backend: [`ms-auth`](../ms-auth) — microservicio dedicado exclusivamente a autenticación.
  - [`AuthController.java`](../ms-auth/src/main/java/cl/duocuc/sanosysalvos/auth/controller/AuthController.java) — endpoints `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/refresh`.
  - [`JwtService.java`](../ms-auth/src/main/java/cl/duocuc/sanosysalvos/auth/service/JwtService.java) — genera y valida tokens JWT firmados (HMAC-SHA256).
  - [`SecurityConfig.java`](../ms-auth/src/main/java/cl/duocuc/sanosysalvos/auth/config/SecurityConfig.java) — usa `BCryptPasswordEncoder` para las contraseñas (nunca se guardan en texto plano).
- Frontend: [`LoginForm.jsx`](../frontend/src/components/auth/LoginForm.jsx) + [`LoginPage.jsx`](../frontend/src/pages/LoginPage.jsx).

### ¿Cómo funciona?
1. El usuario ingresa email y contraseña en el formulario de login.
2. El frontend llama a `POST /bff/auth/login` (a través del BFF, que reenvía a `ms-auth`).
3. `ms-auth` valida las credenciales contra la contraseña cifrada en base de datos (BCrypt) y, si son correctas, genera un JWT con el `usuarioId` y el `rol` del usuario como claims.
4. El token se guarda en el navegador (`localStorage`) y se adjunta automáticamente como header `Authorization: Bearer <token>` en cada solicitud posterior (interceptor de Axios en [`bffClient.js`](../frontend/src/api/bffClient.js)).
5. Todas las rutas privadas del frontend (`PrivateRoute` en [`App.jsx`](../frontend/src/App.jsx)) verifican la existencia del token y redirigen a `/login` si no está presente.
6. Si el token expira, el propio cliente Axios intenta refrescarlo automáticamente contra `POST /api/auth/refresh` antes de forzar un nuevo login.

También existe una pantalla de registro ([`RegisterForm.jsx`](../frontend/src/components/auth/RegisterForm.jsx)) para crear una cuenta nueva con validación de fortaleza de contraseña.

---

## 4. Menú Principal ✅

### ¿Dónde está?
- [`Navbar.jsx`](../frontend/src/components/ui/Navbar.jsx) — se renderiza en **todas** las páginas de la aplicación (está fuera de las rutas, directamente en `App.jsx`).

### ¿Cómo funciona?
El menú principal es una barra de navegación superior (`AppBar` de Material UI) que:

- Muestra el nombre de la plataforma y un enlace a **Inicio**, visibles siempre.
- Cuando el usuario **no** está autenticado, muestra los accesos a **Ingresar** y **Registrarse**.
- Cuando el usuario **está** autenticado, muestra dinámicamente:
  - **Mascotas** → listado y gestión de mascotas (el mantenedor CRUD, ver punto 5).
  - **Coincidencias** → consulta de coincidencias detectadas por el motor de matching.
  - **Notificaciones** → con un contador (`Badge`) de notificaciones no leídas en tiempo real.
  - **Mapa** → reporte geoespacial de mascotas (ver punto 6).
  - **Admin** → solo visible si el rol del usuario es `ADMIN`.
  - Un menú de cuenta (avatar) con acceso a **Mi Perfil** y **Cerrar sesión**.

El menú se adapta según el estado de sesión y el rol, cumpliendo la función de punto único de navegación de toda la aplicación.

---

## 5. Mantenedor Completo CRUD ✅

### ¿Cuál es el mantenedor?
El mantenedor de **Mascotas** (perdidas/encontradas), que es el núcleo funcional de la plataforma.

### Insertar (Create)
- Frontend: [`NuevaMascotaPage.jsx`](../frontend/src/pages/NuevaMascotaPage.jsx) + [`MascotaForm.jsx`](../frontend/src/components/mascotas/MascotaForm.jsx) — formulario con nombre, especie, raza, color, tamaño, descripción, estado y ubicación (con geolocalización del navegador).
- Backend: `POST /api/mascotas` en [`MascotaController.java`](../ms-mascotas/src/main/java/cl/duocuc/sanosysalvos/mascotas/controller/MascotaController.java) → [`MascotaService.registrar()`](../ms-mascotas/src/main/java/cl/duocuc/sanosysalvos/mascotas/service/MascotaService.java).
- Al insertar, además se puede subir una fotografía (`ms-archivos`) y se dispara automáticamente el proceso de matching (ver punto 6).

### Consultar (Read)
- Frontend: [`MascotasPage.jsx`](../frontend/src/pages/MascotasPage.jsx) — listado con filtro por estado (Todas / Mis mascotas / Perdidas / Encontradas / Reunificadas) y búsqueda por nombre, raza o color.
- Backend: `GET /api/mascotas`, `GET /api/mascotas/{id}`, `GET /api/mascotas/usuario/{usuarioId}` en `MascotaController.java`.

### Actualizar (Update)
- Frontend: la misma pantalla `NuevaMascotaPage.jsx`/`MascotaForm.jsx` reutilizada en modo edición (`/mascotas/editar/:id`), y el cambio rápido de estado a "Reunificada" desde el menú contextual de cada tarjeta en [`MascotaCard.jsx`](../frontend/src/components/mascotas/MascotaCard.jsx).
- Backend: `PUT /api/mascotas/{id}` (edición completa) y `PATCH /api/mascotas/{id}/estado` (cambio de estado) en `MascotaController.java` → `MascotaService.actualizar()` / `actualizarEstado()`.
- Ambas operaciones validan que quien edita sea el dueño del registro (`AccesoNoAutorizadoException` si no lo es).

### Borrar (Delete)
- Frontend: opción **Eliminar** en el menú contextual de `MascotaCard.jsx`, con confirmación previa.
- Backend: `DELETE /api/mascotas/{id}` en `MascotaController.java` → `MascotaService.eliminar()`, restringido también al usuario propietario.

Las cuatro operaciones (Insertar, Consultar, Actualizar, Borrar) están **completas de punta a punta**: interfaz gráfica, endpoint REST, capa de servicio y persistencia en base de datos (PostgreSQL, tabla `mascotas`).

---

## 6. Al menos 1 Proceso ✅

### ¿Cuál es el proceso?
El **motor de coincidencias (matching)**, implementado en el microservicio [`ms-matching`](../ms-matching). No es una operación CRUD manual: se ejecuta **automáticamente en segundo plano** cada vez que ocurre un evento relevante sobre una mascota.

### ¿Cómo funciona?
1. Cuando `ms-mascotas` registra, actualiza o elimina una mascota, publica un evento asíncrono en RabbitMQ (`MascotaEventPublisher.java`) — el usuario no espera a que termine el matching, la respuesta HTTP es inmediata.
2. `ms-matching` consume ese evento con [`MascotaEventConsumer.java`](../ms-matching/src/main/java/cl/duocuc/sanosysalvos/matching/consumer/MascotaEventConsumer.java) y guarda una foto (snapshot) de la mascota.
3. Si la mascota está en estado `PERDIDA` o `ENCONTRADA`, [`MatchingService.procesarNuevaMascota()`](../ms-matching/src/main/java/cl/duocuc/sanosysalvos/matching/service/MatchingService.java) busca automáticamente todas las mascotas candidatas de la especie opuesta (perdida ↔ encontrada).
4. Para cada candidata, [`MatchingAlgorithm.java`](../ms-matching/src/main/java/cl/duocuc/sanosysalvos/matching/service/MatchingAlgorithm.java) calcula un **puntaje de compatibilidad** ponderando especie, raza, color, tamaño, distancia geográfica y proximidad de fechas.
5. Si el puntaje supera el umbral mínimo configurado (`matching.score-minimo`), se crea automáticamente un registro de **Coincidencia** y se publica un nuevo evento que dispara la notificación al usuario (en la plataforma y por correo electrónico, vía `ms-notificaciones`).
6. Si una mascota se marca como reunificada o se elimina, el proceso también la excluye automáticamente de futuras comparaciones y limpia su huella en el mapa.

Este es un ejemplo claro de **proceso de negocio automatizado**: no lo dispara un usuario haciendo clic en "ejecutar", sino que corre como reacción a eventos del sistema, con lógica de cálculo propia (el algoritmo de scoring) y efectos en cadena (coincidencia → notificación → correo).

---

## 7. Al menos un Reporte o Consulta por Pantalla ✅

El proyecto cuenta con **tres** pantallas de este tipo (con cualquiera de las tres se cumple el requisito):

### 7.1 Panel de Administración (reporte estadístico)
- [`AdminPage.jsx`](../frontend/src/pages/AdminPage.jsx), accesible solo para usuarios `ADMIN`.
- Muestra un resumen con tarjetas (total de mascotas, perdidas, encontradas, reunificadas) calculado en tiempo real, más una tabla detallada con todas las mascotas del sistema (ID, nombre, especie, estado, usuario, fecha).

### 7.2 Mapa de Reportes (consulta geoespacial)
- [`MapaPage.jsx`](../frontend/src/pages/MapaPage.jsx) + [`MapaReportes.jsx`](../frontend/src/components/mapa/MapaReportes.jsx), usando Leaflet.
- Consulta `GET /bff/mascotas/mapa`, que combina datos de `ms-geolocalizacion` (ubicaciones anonimizadas) con los datos de la mascota, y los grafica como marcadores interactivos.
- `ms-geolocalizacion` también expone una consulta de **zonas calientes** (`GeoService.obtenerZonasCalientes()`), que agrupa los reportes por concentración geográfica.

### 7.3 Consulta de Coincidencias
- [`CoincidenciasPage.jsx`](../frontend/src/pages/CoincidenciasPage.jsx) + [`CoincidenciaList.jsx`](../frontend/src/components/coincidencias/CoincidenciaList.jsx).
- Permite consultar, para una mascota específica, todas las coincidencias detectadas por el motor de matching, con su puntaje de compatibilidad, estado (pendiente/confirmada/rechazada) y fecha de detección.

---

## 8. Conclusión

| Requisito exigido | Cumplimiento | Evidencia principal |
|---|---|---|
| Mínimo 20 Requisitos de Software | ✅ 35 requisitos documentados | [requisitos-software.md](./requisitos-software.md) |
| Login de Acceso | ✅ | `ms-auth` + JWT + BCrypt |
| Menú Principal | ✅ | `Navbar.jsx` |
| Mantenedor CRUD completo | ✅ | Mascotas (Insertar/Consultar/Actualizar/Borrar) |
| Al menos 1 proceso | ✅ | Motor de matching asíncrono (`ms-matching`) |
| Reporte o consulta por pantalla | ✅ (3 pantallas) | Panel Admin, Mapa de reportes, Consulta de coincidencias |

El proyecto **Sanos y Salvos cumple con todas las características mínimas exigidas**, con margen adicional en varios puntos (35 requisitos en vez de 20, y 3 pantallas de reporte/consulta en vez de 1).
