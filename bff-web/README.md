# BFF-Web — Backend For Frontend

**Patrón:** Backend For Frontend (BFF)
**Puerto:** 8080
**Tecnología:** Spring Boot 3.2.5 + WebFlux + JWT

## Descripción

El BFF-Web actúa como punto de entrada único del frontend React hacia los microservicios internos. Valida los tokens JWT en cada request y reenvía las peticiones al servicio correspondiente usando `WebClient` (reactivo).

## Responsabilidades

- Validar el token JWT antes de reenviar al microservicio
- Proxy de autenticación → `ms-auth` (:8084)
- Proxy de mascotas → `ms-mascotas` (:8081)
- Ocultar la topología interna de microservicios al frontend

## Estructura

```
bff-web/
├── src/main/java/cl/duocuc/sanosysalvos/bff/
│   ├── BffWebApplication.java
│   ├── config/
│   │   ├── JwtValidator.java        # Valida tokens entrantes
│   │   └── WebClientConfig.java     # Clientes reactivos hacia microservicios
│   └── controller/
│       ├── AuthProxyController.java    # /api/auth/**
│       └── MascotaProxyController.java # /api/mascotas/**
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## Endpoints expuestos

| Método | Ruta | Destino | Auth requerida |
|---|---|---|---|
| POST | `/api/auth/register` | ms-auth | No |
| POST | `/api/auth/login` | ms-auth | No |
| GET | `/api/mascotas` | ms-mascotas | Sí |
| POST | `/api/mascotas` | ms-mascotas | Sí |
| GET | `/api/mascotas/{id}` | ms-mascotas | Sí |
| PUT | `/api/mascotas/{id}/estado` | ms-mascotas | Sí |

## Requisitos

- Java 17
- Maven 3.8+

## Ejecución local

```bash
# Requiere que ms-auth y ms-mascotas estén corriendo
cd bff-web
mvn spring-boot:run
```

## Variables de entorno (Docker)

```properties
SERVICES_AUTH_URL=http://ms-auth:8084
SERVICES_MASCOTAS_URL=http://ms-mascotas:8081
SERVICES_GEO_URL=http://ms-geolocalizacion:8082
SERVICES_MATCHING_URL=http://ms-matching:8083
```

## Ejecutar pruebas

```bash
mvn test
```

## Construir con Maven (arquetipo)

Este proyecto fue generado usando el arquetipo **spring-boot-starter-parent** de Maven:

```bash
mvn archetype:generate \
  -DgroupId=cl.duocuc.sanosysalvos \
  -DartifactId=bff-web \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.4
```
