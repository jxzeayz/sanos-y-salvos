# MS-Auth — Microservicio de Autenticación

**Puerto:** 8084
**Tecnología:** Spring Boot 3.2.5 + Spring Security + JWT + BCrypt + PostgreSQL

## Descripción

Microservicio responsable del registro y autenticación de usuarios. Genera tokens JWT firmados con HMAC-SHA256 que son validados por el BFF-Web en cada solicitud protegida.

## Responsabilidades

- Registrar nuevos usuarios con contraseña cifrada (BCrypt, factor 12)
- Autenticar usuarios y emitir tokens JWT
- Gestionar roles: DUEÑO, CIUDADANO, VETERINARIO, ADMIN

## Estructura

```
ms-auth/
├── src/main/java/cl/duocuc/sanosysalvos/auth/
│   ├── MsAuthApplication.java
│   ├── config/
│   │   └── SecurityConfig.java         # Spring Security sin sesión (stateless)
│   ├── controller/
│   │   └── AuthController.java         # POST /auth/register, POST /auth/login
│   ├── dto/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   └── AuthResponse.java
│   ├── model/
│   │   ├── Usuario.java                # Entidad JPA
│   │   └── RolUsuario.java             # Enum de roles
│   ├── repository/
│   │   └── UsuarioRepository.java
│   └── service/
│       ├── AuthService.java            # Lógica de registro y login
│       └── JwtService.java             # Generación y validación de tokens
├── src/test/java/.../service/
│   ├── AuthServiceTest.java
│   └── JwtServiceTest.java
└── pom.xml
```

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Registrar nuevo usuario |
| POST | `/auth/login` | Autenticar y obtener JWT |

### Ejemplo de registro

```json
POST /auth/register
{
  "nombre": "Juan Pérez",
  "email": "juan@example.cl",
  "password": "miPassword123",
  "telefono": "+56912345678",
  "rol": "DUEÑO"
}
```

### Ejemplo de login

```json
POST /auth/login
{
  "email": "juan@example.cl",
  "password": "miPassword123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuarioId": 1,
  "nombre": "Juan Pérez",
  "email": "juan@example.cl",
  "rol": "DUEÑO"
}
```

## Requisitos

- Java 17
- Maven 3.8+
- PostgreSQL 16 (base de datos `db_auth`)

## Ejecución local

```bash
cd ms-auth
mvn spring-boot:run
```

## Variables de entorno (Docker)

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/db_auth
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

## Ejecutar pruebas

```bash
mvn test
```

## Generación desde arquetipo Maven

```bash
mvn archetype:generate \
  -DgroupId=cl.duocuc.sanosysalvos \
  -DartifactId=ms-auth \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.4
```
