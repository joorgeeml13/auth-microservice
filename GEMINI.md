# Contexto del Proyecto: Auth Microservice

## Descripción
Este proyecto es un **microservicio de autenticación y autorización** desarrollado con **Spring Boot (Java 21)**.

## Dependencias Clave
- **Spring Boot 4.0.6** (Web MVC, Data JPA, Validation, Actuator).
- **Spring Security**: Autenticación, autorización y configuración de seguridad.
- **JJWT (io.jsonwebtoken: 0.12.5)**: Generación, firma (RSA RS256) y lectura de JSON Web Tokens (Access Token / Refresh Token).
- **Flyway & PostgreSQL**: Migración e interacción con base de datos relacional.
- **SpringDoc OpenAPI (Swagger)**: Documentación e interfaz de la API.
- **Testcontainers & JUnit 5**: Contenedores de integración para PostgreSQL en entornos de pruebas.

## Reglas de Desarrollo y Testing

***IMPORTANTE***
- Si un requerimiento de código es ambiguo, le faltan detalles técnicos o no estás 100% seguro de la regla de negocio que debo aplicar, DETENTE INMEDIATAMENTE.
- No inventes lógicas de seguridad, ni asumas la estructura de una base de datos o entidad si no la tienes en tu contexto actual. En su lugar, escríbeme una lista de preguntas aclaratorias antes de generar una sola línea de código.

### 1. Verificación y Compilación
- **NUNCA usar `javac` directamente.**
- Usar **únicamente** `./mvnw compile` o `./mvnw test` para compilar y validar el código.

### 2. Estándar de Testing
- **Estrategia Estricta**: Todas las pruebas de integración de controladores y endpoints deben realizarse utilizando **MockMvc** y **JUnit 5**, heredando de `AbstractIntegrationTest`.
- **Referencia de Patrón**: Seguir el patrón implementado en [`LogOutTests.java`](file:///home/jorge/dev/projects/auth-microservice/src/test/java/jorge/matias/auth_microservice/LogOutTests.java) y [`LogInTests.java`](file:///home/jorge/dev/projects/auth-microservice/src/test/java/jorge/matias/auth_microservice/LogInTests.java):
  - Uso de `@AutoConfigureMockMvc`.
  - Inyección de `MockMvc` para realizar peticiones HTTP (`mockMvc.perform(...)`).
  - Validación de cabeceras custom (`X-Client-Type`, `X-Device-ID`), cookies de sesión (`refresh_token`) y códigos de estado HTTP.
  - Limpieza de datos en cada test (`@AfterEach` limpiando repositorios).

### 3. Reglas de arquitectura y delegacion
- Regla de oro: El `Controller` es únicamente un policía de tráfico. Su única responsabilidad es recibir la petición HTTP, mapear los datos, delegar la ejecución a la capa Service y devolver un ResponseEntity.
- Prohibido: No puedes escribir lógica de negocio, cálculos complejos, transformaciones profundas de datos ni instanciar repositorios directamente dentro de un Controller.
- Excepciones permitidas en el Controller: Validaciones de entrada rápidas (Early Returns como comprobar si un token viene vacío), extracción de datos del contexto de seguridad (Headers/Cookies) y manejo de excepciones a nivel de enrutamiento.
- Jamás expongas una Entidad de Base de Datos (JPA/Hibernate) ni un Modelo de dominio en las respuestas o peticiones de los endpoints.
- Todo el tráfico de entrada debe recibirse en un DTO o un `Record` de Java (ej. `LogoutRequest`, `RegisterRequest`).

