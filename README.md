# 🔒 Auth Microservice

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker Pulls](https://img.shields.io/docker/pulls/joorgeeml/auth-microservice?style=for-the-badge&color=blue)

Microservicio de autenticación ligero, robusto y escalable desarrollado en **Java**. Diseñado para gestionar el registro de usuarios, login de forma segura y la emisión/validación de tokens JWT en arquitecturas distribuidas.

## Contenidos

- [Stack Tecnológico](#stack-tecnológico)
- [Prerrequisitos](#prerrequisitos)
- [Configuración y Variables de Entorno](#configuración-y-variables-de-entorno)
- [Despliegue Rápido](#despliegue-rápido)
- [Roadmap V2](#roadmap-v2)

## 🛠️ Stack Tecnológico
- Java 21/Maven
- JWT (Json Web Tokens) & Bcrypt 
- Docker & Docker Compose
- PostgresSQL

## Prerrequisitos
Para levantar este servicio es necesario:
- Docker y Docker Compose
- Una instancia de PostgreSQL levantada
- Java JDK 21 (solo si vas a compilar en local)
- Maven (solo si vas a compilar en local)

## Configuración y Variables de Entorno
El microservicio utiliza variables de entorno para no dejar credenciales expuestas en el codigo fuente. Crea un  archivo .env en la raiz del proyecto basandote en el archivo .env.example:

| Variable | Descipcion | Valor por Defecto |
| :--- | :--- | :---|
| `SERVER_PORT` | Puerto donde corre el microservicio | `8080` |
| `DB_URL` | URL de conexión a la BD | `jdbc:postgresql://localhost:5432/auth_db` |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | `root` |

> [!WARNING]
> **⚠️ Atención usuarios de Docker (Conexión a BD local):**
> Si vas a ejecutar el microservicio usando Docker pero tu instancia de PostgreSQL está instalada directamente **en tu máquina local** (fuera de Docker), el contenedor no podrá acceder a ella usando `localhost`.
> 
> * **En Windows y Mac:** Debes cambiar la URL en tu archivo `.env` para usar el DNS interno de Docker:
>   `DB_URL=jdbc:postgresql://host.docker.internal:5432/auth_db`
> * **En Linux:** Usa la IP de tu red local (ej. `192.168.x.x`) o la IP del gateway de Docker (`172.17.0.1`).

## Despliegue Rápido

### 🔑 Generar Certificados RSA (Obligatorio)

Este microservicio utiliza RS256 para firmar los tokens. Antes de levantar el contenedor, necesitas generar las llaves pública y privada. Ejecuta estos comandos en la raíz del proyecto:

```bash
mkdir certs
cd certs

# Generar llave privada
openssl genpkey -algorithm RSA -out private_key_temp.pem -pkeyopt rsa_keygen_bits:2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private_key_temp.pem -out private_key.pem
rm private_key_temp.pem

# Generar llave pública
openssl rsa -pubout -in private_key.pem -out public_key.pem
cd ..
```
### Opcion A: Imagen oficial de Docker Hub (Recomendado)
Crea un archivo docker-compose.yml en un directorio vacío, asegúrate de tener tu archivo .env y la carpeta certs/ generada, y pega esto:
```yml
services:
  auth-service:
    image: joorgeeml/auth-microservice:0.1.0
    container_name: auth-microservice
    restart: unless-stopped
    ports:
      - "${SERVER_PORT:-8080}:${SERVER_PORT:-8080}"
    environment:
      - SPRING_DATASOURCE_URL=${DB_URL}
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - JWT_PRIVATE_KEY_PATH=file:/app/certs/private_key.pem
      - JWT_PUBLIC_KEY_PATH=file:/app/certs/public_key.pem
    volumes:
      - ./certs:/app/certs:ro
    env_file:
      - .env
```

Para arrancar el proyecto:
```bash
docker-compose up -d
```

### Opción B: Clonar y ejecutar en local (Modo Developer)

Si quieres explorar el código fuente, modificarlo o contribuir al proyecto, puedes levantar la aplicación directamente con Maven:

1. Clona el repositorio:
   ```bash
   git clone [https://github.com/joorgeeml/auth-microservice.git](https://github.com/joorgeeml/auth-microservice.git)
   cd auth-microservice
   ```
2. Asegúrate de haber generado los certificados RSA (sigue los pasos de la sección anterior) y de tener tu instancia de PostgreSQL corriendo.
3. Crea tu archivo .env en la raíz del proyecto para que Spring Boot pille las credenciales de la base de datos.

4. Compila y arranca la aplicación:
    ```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```
## Roadmap V2
Futuros objetivos a implementar en el proyecto:
- [ ] Rate Limiting: Añadir protección contra ataques de fuerza bruta en los endpoints de autenticación utilizando Redis o Resilience4j.

- [ ] Federación de Identidades (SSO): Integrar OIDC/OAuth2 para permitir login social (Google, GitHub, etc.).