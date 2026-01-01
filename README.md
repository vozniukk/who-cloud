# WHO Cloud Microservices

A modern microservices architecture built with **Spring Boot 3.4.1**, **Java 21 LTS**, **Gradle 8.11**, and **PostgreSQL 16**. This project demonstrates enterprise-grade microservices patterns with API Gateway routing, Docker containerization, and Kubernetes-ready deployment.

## 🏗️ Architecture

### Technology Stack
- **Java**: 21 LTS (Eclipse Temurin)
- **Spring Boot**: 3.4.1
- **Spring Cloud Gateway**: 2024.0.0
- **Build Tool**: Gradle 8.11.1 with Kotlin DSL
- **Database**: PostgreSQL 16-alpine
- **Containerization**: Docker + Docker Compose
- **Target Platform**: Kubernetes on VPS

### Microservices
- **API Gateway** (8080) - Spring Cloud Gateway routing all requests
- **Auth Service** (8081) - JWT authentication + Google SSO (planned)
- **User Management Service** (8082) - User profiles and role management
- **Information Service A** (8083) - Information management endpoint
- **Information Service B** (8084) - Information management endpoint
- **Business Service 1** (8085) - Business logic service
- **Business Service 2** (8086) - Business logic service
- **Business Service 3** (8087) - Business logic service
- **Admin Portal Service** (8088) - Administrative dashboard
- **Public Web Service** (8089) - Public-facing APIs

### User Roles (Planned)
- `USER` - Basic authenticated user
- `MANAGER` - Manager level access
- `ADMINISTRATOR` - Full administrative access
- `OBSERVER` - Read-only observer access

## 🚀 Quick Start

### Prerequisites
- **Java 21 LTS**: [Download Eclipse Temurin](https://adoptium.net/)
- **Gradle 8.11+**: Included via Gradle Wrapper
- **Docker & Docker Compose**: [Install Docker](https://www.docker.com/get-started)
- **Git**: For version control

### Setup

1. **Start PostgreSQL**
   ```bash
   docker-compose up -d
   ```

2. **Configure Google OAuth 2.0**

### Local Development (Without Docker)

1. **Clone the repository**
   ```bash
   git clone https://github.com/vozniukk/who-cloud.git
   cd who-cloud
   ```

2. **Start PostgreSQL** (if not using Docker)
   ```bash
   docker run -d --name whocloud-postgres \
     -e POSTGRES_USER=whocloud \
     -e POSTGRES_PASSWORD=whocloud123 \
     -e POSTGRES_DB=whocloud \
     -p 5432:5432 \
     postgres:16-alpine
   ```

3. **Build all services**
   ```bash
   ./gradlew build -x test
   ```
   Windows:
   ```cmd
   .\gradlew.bat build -x test
   ```

4. **Run services individually**
   ```bash
   # API Gateway
   ./gradlew :api-gateway:bootRun
   
   # Auth Service
   ./gradlew :auth-service:bootRun
   
   # Public Web Service
   ./gradlew :public-web-service:bootRun
   ```

### Docker Deployment (Recommended)

**Prerequisites**: Build JARs first before running Docker Compose
```bash
./gradlew build -x test
```

**Start all services**:
```bash
docker-compose up -d
```

**View logs**:
```bash
docker-compose logs -f
```

**Stop all services**:
```bash
docker-compose down
```

For detailed Docker instructions, see [DOCKER.md](DOCKER.md)

### Environment Variables (Optional)

Create `.env` file in project root:
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=whocloud
DB_USER=whocloud
DB_PASSWORD=whocloud123

# JWT (for production, use strong secret)
JWT_SECRET=your-secure-256-bit-secret-key-change-this-in-production

# Google OAuth (when implemented)
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
```

### Access Points

- **API Gateway**: http://localhost:8080
- **Public Service**: http://localhost:8089
- **Auth Service**: http://localhost:8081
- **User Management**: http://localhost:8082
- **Admin Portal**: http://localhost:8088

### Testing Endpoints

```bash
# Public endpoint (no auth required)
curl http://localhost:8080/api/public/welcome

# Information Service A
curl http://localhost:8080/api/information/a

# Business Service 1
curl http://localhost:8080/api/business/service1

# Health check
curl http://localhost:8080/actuator/health
```

## 📁 Project Structure

```
who-cloud/
├── api-gateway/              # Port 8080 - Spring Cloud Gateway
├── auth-service/             # Port 8081 - JWT + Google SSO
├── user-management-service/  # Port 8082 - User CRUD
├── information-service-a/    # Port 8083 - Info endpoint
├── information-service-b/    # Port 8084 - Info endpoint
├── business-service-1/       # Port 8085 - Business logic
├── business-service-2/       # Port 8086 - Business logic
├── business-service-3/       # Port 8087 - Business logic
├── admin-portal-service/     # Port 8088 - Admin dashboard
├── public-web-service/       # Port 8089 - Public APIs
├── common/                   # Shared library (DTOs, exceptions)
├── gradle/                   # Gradle wrapper
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts       # Multi-module project settings
├── docker-compose.yml        # Docker orchestration
├── init-db.sql              # Database initialization
├── DOCKER.md                # Docker deployment guide
└── README.md                # This file
```

### Module Dependencies
- All services depend on `common` module for shared DTOs
- Services use Spring Boot starters for web, JPA, security
- API Gateway routes to all backend services

## 🔧 Development

### Building Specific Modules

```bash
# Build only API Gateway
./gradlew :api-gateway:build

# Build common library
./gradlew :common:build

# Clean and rebuild
./gradlew clean build
```

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :auth-service:test

# Skip tests during build
./gradlew build -x test
```

### Database

PostgreSQL configuration:
- **Host**: localhost (or `postgres` in Docker network)
- **Port**: 5432
- **Databases**: `authdb`, `userdb`
- **Username**: `whocloud`
- **Password**: `whocloud123` (development only)

Connect to database:
```bash
docker exec -it whocloud-postgres psql -U whocloud -d authdb
```

### Code Style
- Java 21 features enabled
- Lombok for boilerplate reduction
- Spring Boot conventions
- RESTful API design

## 🐳 Docker Architecture

### Multi-Stage Builds (Deprecated)
Original Dockerfiles used multi-stage builds but encountered SSL issues. Current approach:
1. Build JARs locally with Gradle
2. Copy pre-built JARs into lightweight JRE containers

### Current Dockerfile Pattern
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY service-name/build/libs/service-name-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose Network
- Network: `whocloud-network`
- All services communicate via service names
- PostgreSQL with health checks
- Persistent volume for database

## 📊 Monitoring & Health

All services expose actuator endpoints:
```bash
# API Gateway health
curl http://localhost:8080/actuator/health

# Individual service health
curl http://localhost:8089/actuator/health
```

## 🚀 Deployment

### Kubernetes (Planned)
Target deployment: VPS with Kubernetes

**Next Steps**:
1. Create Kubernetes manifests (Deployments, Services, ConfigMaps)
2. Set up Ingress controller
3. Configure PostgreSQL as StatefulSet
4. Implement secrets management
5. Set up CI/CD pipeline

See [KUBERNETES.md](KUBERNETES.md) (coming soon)

## 🔐 Security Notes

**Current State** (Development):
- Database credentials are hardcoded (change for production)
- JWT secret should be externalized
- No HTTPS/TLS configured (add in production)
- CORS configured for development

**Production Recommendations**:
- Use Kubernetes Secrets for sensitive data
- Enable HTTPS with proper certificates
- Implement rate limiting
- Add API authentication/authorization
- Enable Spring Security for all services
- Use environment-specific configurations

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <process_id> /F

# Linux/Mac
lsof -i :8080
kill -9 <process_id>
```

### Gradle Build Issues
```bash
# Clean Gradle cache
./gradlew clean --refresh-dependencies

# Delete .gradle folder
rm -rf .gradle

# Re-download wrapper
./gradlew wrapper --gradle-version 8.11.1
```

### Docker Issues
```bash
# Remove all containers and volumes
docker-compose down -v

# Rebuild without cache
docker-compose build --no-cache

# Check logs
docker-compose logs -f service-name
```

### Database Connection Issues
- Ensure PostgreSQL is running: `docker ps`
- Check network connectivity: `docker network inspect who-cloud_whocloud-network`
- Verify credentials in application.yml

## 📚 API Documentation

### Public Endpoints (No Auth)
- `GET /api/public/welcome` - Welcome message
- `GET /api/public/health` - Health status

### Information Services (Auth Required - TODO)
- `GET /api/information/a` - Information Service A
- `GET /api/information/b` - Information Service B

### Business Services (Auth Required - TODO)
- `GET /api/business/service1` - Business Service 1
- `GET /api/business/service2` - Business Service 2
- `GET /api/business/service3` - Business Service 3

### User Management (Auth Required - TODO)
- `GET /api/users` - List users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Admin Portal (Admin Role Required - TODO)
- `GET /api/admin/dashboard` - Admin dashboard
- `GET /api/admin/stats` - System statistics

### Authentication (TODO - Implementation Pending)
- `POST /api/auth/login` - Login with credentials
- `POST /api/auth/register` - Register new user
- `GET /api/auth/validate` - Validate JWT token
- `POST /api/auth/google` - Google OAuth flow

## 🎯 Roadmap

### Phase 1: Core Infrastructure ✅
- [x] Multi-module Gradle project with Kotlin DSL
- [x] Spring Boot 3.4.1 with Java 21
- [x] API Gateway with Spring Cloud Gateway
- [x] PostgreSQL database setup
- [x] Docker containerization
- [x] Basic REST endpoints

### Phase 2: Authentication & Authorization (In Progress)
- [ ] Implement JWT authentication
- [ ] Google OAuth 2.0 integration
- [ ] User entity and repository
- [ ] Role-based access control
- [ ] JWT filter for protected endpoints

### Phase 3: Business Logic
- [ ] Implement user management CRUD
- [ ] Add business logic to services
- [ ] Implement admin portal features
- [ ] Add data validation and error handling

### Phase 4: Production Ready
- [ ] Kubernetes manifests
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Monitoring and logging (Prometheus/Grafana)
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Integration tests
- [ ] Performance testing

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👥 Team

- **Repository**: https://github.com/vozniukk/who-cloud
- **Branch**: feature/google-sso

## 📞 Support

For issues and questions:
- Open an issue on GitHub
- Check [DOCKER.md](DOCKER.md) for deployment issues
- Review application logs: `docker-compose logs -f`
4. Add role-based endpoint security with `@PreAuthorize`
5. Create REST controllers for each service
6. Add Swagger/OpenAPI documentation
7. Implement circuit breakers with Resilience4j
8. Add distributed tracing

## License

Proprietary