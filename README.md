# Sanos y Salvos — Plataforma de localización de mascotas perdidas

**DSY1106 — Desarrollo Fullstack III | DuocUC**
**Estudiantes:** Julio Soto | Armando Calderón
**Docente:** Jorge Canales Soto

---

## Descripción

Sanos y Salvos es una plataforma centralizada para el registro, visualización y detección de coincidencias entre mascotas perdidas y encontradas. La solución integra ciudadanos, clínicas veterinarias, refugios y municipalidades bajo una arquitectura de microservicios desacoplada.

## Arquitectura

```
Frontend (React + Vite)
    │
    └── BFF-Web (Spring Boot :8080)
            ├── ms-auth         (:8084)  JWT + BCrypt
            ├── ms-mascotas     (:8081)  CRUD + RabbitMQ
            ├── ms-geolocalizacion (:8082)  PostGIS
            └── ms-matching     (:8083)  Motor de coincidencias
```

**Infraestructura de soporte:**
- PostgreSQL (x4 bases de datos independientes)
- PostGIS (geolocalización)
- RabbitMQ (mensajería asíncrona)
- MinIO (almacenamiento de imágenes)

## Componentes

| Componente | Tecnología | Puerto | README |
|---|---|---|---|
| [frontend](./frontend) | React 18 + Vite + MUI | 3000 | [README](./frontend/README.md) |
| [bff-web](./bff-web) | Spring Boot 3.2 + WebFlux | 8080 | [README](./bff-web/README.md) |
| [ms-auth](./ms-auth) | Spring Boot 3.2 + Spring Security | 8084 | [README](./ms-auth/README.md) |
| [ms-mascotas](./ms-mascotas) | Spring Boot 3.2 + JPA + AMQP | 8081 | [README](./ms-mascotas/README.md) |
| [ms-geolocalizacion](./ms-geolocalizacion) | Spring Boot 3.2 + PostGIS | 8082 | [README](./ms-geolocalizacion/README.md) |
| [ms-matching](./ms-matching) | Spring Boot 3.2 + RabbitMQ | 8083 | [README](./ms-matching/README.md) |

## Inicio rápido

### Requisitos
- Docker Desktop 4.x
- Docker Compose v2

### Levantar todo el sistema

```bash
docker compose up --build
```

Esto levanta **todos los servicios** automáticamente en orden, con healthchecks.

| Servicio | URL |
|---|---|
| Frontend | http://localhost:3000 |
| BFF API | http://localhost:8080 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| MinIO Console | http://localhost:9001 (minioadmin/minioadmin) |

### Detener el sistema

```bash
docker compose down
```

## Estrategia de branching

El proyecto usa **Git Flow** simplificado:

- `main` — versiones estables (releases)
- `develop` — integración continua
- `feature/*` — desarrollo de cada componente

Ver [docs/plan-branching.md](./docs/plan-branching.md) para detalle completo.

## Patrones de diseño utilizados

- **Repository** (todos los microservicios)
- **Builder** (entidades con Lombok)
- **BFF / Proxy** (bff-web)
- **Event-Driven / Publisher-Subscriber** (ms-mascotas → ms-matching vía RabbitMQ)
- **Circuit Breaker** (Resilience4j en ms-mascotas)
- **Strategy** (MatchingAlgorithm en ms-matching)

Ver [docs/analisis-patrones-arquetipos.md](./docs/analisis-patrones-arquetipos.md) para análisis completo.

Posterior mente nevesitamos de un .env para que el programa pueda ejecutarse 
# ─── Base de datos ───
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# ─── JWT (clave de mínimo 32 caracteres) ───
JWT_SECRET=sanos-y-salvos-clave-super-secreta-256-bits-minimo-2026
JWT_EXPIRATION_MS=3600000

# ─── RabbitMQ ───
RABBITMQ_USER=guest
RABBITMQ_PASS=guest

# ─── MinIO ───
MINIO_USER=minioadmin
MINIO_PASS=minioadmin

# ─── Correo (dummy local, no envía correos reales) ───
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=notificaciones@sanosysalvos.com
SMTP_PASS=dummy-password-local

# ─── CORS ───
CORS_ORIGINS=http://localhost:3000,https://localhost