# Requisitos de Software
## Proyecto Sanos y Salvos — DSY1106 Desarrollo Fullstack III
**Estudiantes:** Julio Soto | Armando Calderón
**Docente:** Jorge Canales Soto
**Fecha:** Agosto 2026

---

## 1. Introducción

Este documento lista los requisitos de software del proyecto **Sanos y Salvos**, plataforma centralizada para el registro, visualización y detección de coincidencias entre mascotas perdidas y encontradas. Los requisitos se dividen en **Funcionales (RF)** — qué debe hacer el sistema — y **No Funcionales (RNF)** — bajo qué condiciones y restricciones técnicas debe hacerlo.

Cada requisito está directamente respaldado por una funcionalidad ya implementada en el repositorio (frontend React, BFF y los 6 microservicios Spring Boot), referenciada entre paréntesis.

---

## 2. Requisitos Funcionales (RF)

### Autenticación y usuarios

| ID | Requisito |
|---|---|
| RF-01 | El sistema debe permitir a un nuevo usuario registrarse indicando nombre, email, teléfono, contraseña y rol (Dueño, Ciudadano Colaborador o Veterinario). *(ms-auth, RegisterForm.jsx)* |
| RF-02 | El sistema debe permitir iniciar sesión con email y contraseña, generando un token JWT firmado con una expiración configurable. *(ms-auth — JwtService, LoginForm.jsx)* |
| RF-03 | El sistema debe permitir refrescar el token de sesión antes de que expire, sin solicitar nuevamente las credenciales. *(AuthService.refreshToken, bffClient.js)* |
| RF-04 | El sistema debe permitir a un usuario autenticado consultar y actualizar los datos de su propio perfil (nombre, email, teléfono). *(PerfilPage.jsx)* |
| RF-05 | El sistema debe permitir a un usuario autenticado cambiar su contraseña, validando previamente la contraseña actual. *(AuthService.cambiarPassword)* |
| RF-06 | El sistema debe restringir el acceso a las secciones internas de la plataforma únicamente a usuarios autenticados, redirigiendo al login en caso contrario. *(PrivateRoute, App.jsx)* |
| RF-07 | El sistema debe restringir el acceso al panel de administración únicamente a usuarios con rol ADMIN. *(AdminRoute, App.jsx)* |

### Gestión de mascotas (mantenedor CRUD)

| ID | Requisito |
|---|---|
| RF-08 | El sistema debe permitir registrar una mascota perdida o encontrada indicando nombre, especie, raza, color, tamaño, descripción y ubicación geográfica. *(NuevaMascotaPage.jsx, ms-mascotas)* |
| RF-09 | El sistema debe permitir listar todas las mascotas registradas, con filtro por estado (perdida, encontrada, reunificada). *(MascotasPage.jsx)* |
| RF-10 | El sistema debe permitir a un usuario consultar únicamente las mascotas que él mismo registró ("Mis mascotas"). *(getMisMascotas, MascotaController)* |
| RF-11 | El sistema debe permitir editar los datos de una mascota ya registrada, restringiendo la edición solo al usuario propietario. *(MascotaService.actualizar)* |
| RF-12 | El sistema debe permitir eliminar el registro de una mascota, restringiendo la eliminación solo al usuario propietario. *(MascotaService.eliminar)* |
| RF-13 | El sistema debe permitir marcar una mascota como "Reunificada" una vez que ha sido recuperada por su dueño. *(actualizarEstado, MascotaCard.jsx)* |
| RF-14 | El sistema debe permitir asociar una o más fotografías a una mascota registrada. *(ArchivoController, MascotaForm.jsx)* |
| RF-15 | El sistema debe validar que el archivo subido sea una imagen de un tipo permitido y no exceda el tamaño máximo configurado (5 MB), rechazando cualquier otro archivo. *(ArchivoFotoService.validarYLeerArchivo)* |
| RF-16 | El sistema debe permitir eliminar una fotografía previamente asociada a una mascota. *(ArchivoFotoService.eliminarArchivo)* |

### Motor de coincidencias (proceso)

| ID | Requisito |
|---|---|
| RF-17 | El sistema debe ejecutar automáticamente, en segundo plano, un proceso de comparación (matching) cada vez que se registra o actualiza una mascota, buscando coincidencias entre mascotas perdidas y encontradas de la misma especie. *(MascotaEventConsumer, MatchingService.procesarNuevaMascota)* |
| RF-18 | El sistema debe calcular un puntaje de compatibilidad entre dos mascotas en base a especie, raza, color, tamaño, cercanía geográfica y proximidad de fechas, y descartar automáticamente las coincidencias por debajo del umbral mínimo configurado. *(MatchingAlgorithm)* |
| RF-19 | El sistema debe excluir del proceso de matching a las mascotas cuyo estado sea "Reunificada" o que hayan sido eliminadas. *(MatchingService, MascotaEventConsumer)* |
| RF-20 | El sistema debe permitir a un usuario confirmar o rechazar una coincidencia sugerida por el motor de matching. *(MatchingService.actualizarEstado)* |
| RF-21 | El sistema debe revelar los datos de contacto asociados a una mascota únicamente cuando exista una coincidencia confirmada entre ambas partes. *(ContactoProxyController)* |

### Notificaciones y auditoría

| ID | Requisito |
|---|---|
| RF-22 | El sistema debe notificar automáticamente, dentro de la plataforma y por correo electrónico, a los usuarios involucrados cuando se detecta una coincidencia relevante. *(CoincidenciaEventConsumer, EmailService)* |
| RF-23 | El sistema debe permitir a un usuario consultar sus notificaciones y marcarlas como leídas, individualmente o todas a la vez. *(NotificacionesPage.jsx)* |
| RF-24 | El sistema debe registrar de forma automática un historial de auditoría de los eventos relevantes ocurridos en la plataforma (registro, actualización y eliminación de mascotas, coincidencias detectadas, etc.). *(AuditEventConsumer, ms-auditoria)* |

### Geolocalización y reportes

| ID | Requisito |
|---|---|
| RF-25 | El sistema debe permitir visualizar en un mapa geográfico la ubicación aproximada (anonimizada dentro de un radio) de las mascotas reportadas. *(MapaPage.jsx, GeoService.anonimizarCoordenadas)* |
| RF-26 | El sistema debe permitir consultar zonas con mayor concentración de reportes ("zonas calientes") sobre el mapa. *(GeoService.obtenerZonasCalientes)* |
| RF-27 | El sistema debe eliminar del mapa los reportes de una mascota cuando esta es marcada como reunificada o es eliminada. *(MascotaEventListener)* |
| RF-28 | El sistema debe presentar en un panel de administración un resumen estadístico de mascotas por estado (total, perdidas, encontradas, reunificadas) junto con el detalle tabulado. *(AdminPage.jsx)* |

---

## 3. Requisitos No Funcionales (RNF)

| ID | Requisito |
|---|---|
| RNF-01 | El sistema debe estar compuesto por microservicios independientes, cada uno con su propia base de datos, comunicados mediante un Backend For Frontend (BFF) como único punto de entrada del cliente web. |
| RNF-02 | El sistema debe usar mensajería asíncrona (RabbitMQ) para desacoplar el registro de mascotas del procesamiento de matching, notificaciones y auditoría. |
| RNF-03 | El sistema debe implementar un mecanismo de tolerancia a fallos (Circuit Breaker) para que una caída del servicio de mensajería no bloquee el registro de mascotas. |
| RNF-04 | El sistema debe almacenar las contraseñas de los usuarios cifradas mediante un algoritmo de hash seguro (BCrypt), nunca en texto plano. |
| RNF-05 | El sistema debe autenticar y autorizar las solicitudes entre el frontend y el backend mediante tokens JWT firmados con expiración. |
| RNF-06 | El sistema debe limitar la cantidad de solicitudes por dirección IP en una ventana de tiempo (rate limiting) para prevenir abuso de la API. |
| RNF-07 | El sistema completo (frontend, BFF, microservicios e infraestructura de soporte) debe poder desplegarse mediante contenedores Docker orquestados con Docker Compose, a partir de un único comando. |

---

## 4. Resumen

- **28 Requisitos Funcionales** (RF-01 a RF-28)
- **7 Requisitos No Funcionales** (RNF-01 a RNF-07)
- **Total: 35 requisitos**, superando el mínimo de 20 solicitado.

Todos los requisitos listados corresponden a funcionalidad ya implementada y verificable en el repositorio; ninguno describe trabajo futuro o planificado.
