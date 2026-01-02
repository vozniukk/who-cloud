# WHO Cloud Microservices

A modern microservices architecture built with **Spring Boot 3.4.1**, **Java 21 LTS**, **Gradle 8.11**, and **PostgreSQL 16**. This project demonstrates enterprise-grade microservices patterns with API Gateway routing, Docker containerization, and Kubernetes-ready deployment.

## 🏗️ Architecture

### Technology Stack
- **Java**: 21 LTS (Eclipse Temurin)
- **Spring Boot**: 3.4.1
- **Spring Cloud Gateway**: 2024.0.0
- **Reverse Proxy**: nginx alpine (production entry point)
- **Build Tool**: Gradle 8.11.1 with Kotlin DSL
- **Database**: PostgreSQL 16-alpine
- **Containerization**: Docker + Docker Compose
- **Target Platform**: Kubernetes on VPS

### Microservices
- **nginx** (80/443) - Reverse proxy with rate limiting & SSL/TLS ✅
- **Next.js Dashboard** (3001) - Modern React dashboard with SSR ✅
- **API Gateway** (8080) - Spring Cloud Gateway routing all requests ✅
- **Auth Service** (8081) - JWT authentication + Google OAuth2 ✅
- **User Management Service** (8082) - User profiles and role management
- **Information Service A** (8083) - Information management endpoint
- **Information Service B** (8084) - Information management endpoint
- **Business Service 1** (8085) - Business logic service
- **Business Service 2** (8086) - Business logic service
- **Business Service 3** (8087) - Business logic service
- **Admin Portal Service** (8088) - Administrative dashboard
- **Public Web Service** (8089) - Public-facing APIs + database statistics ✅

### User Roles & Access Control ✅
- `GUEST` - Auto-registered OAuth2 users (read-only access to information services)
- `USER` - Full access to all business services
- `ADMIN` - Administrative access + user management
- `MODERATOR` - Content moderation capabilities

## 🚀 Quick Start

### Prerequisites
- **Java 21 LTS**: [Download Eclipse Temurin](https://adoptium.net/)
- **Gradle 8.11+**: Included via Gradle Wrapper
- **Docker & Docker Compose**: [Install Docker](https://www.docker.com/get-started)
- **Git**: For version control

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/vozniukk/who-cloud.git
   cd who-cloud
   ```

2. **Configure Google OAuth 2.0** (see [OAUTH2_SETUP.md](OAUTH2_SETUP.md))
   - Create Google OAuth 2.0 credentials
   - Add `.env` file with client ID/secret

3. **Generate SSL Certificates** (see [REVERSE_PROXY.md](REVERSE_PROXY.md))
   ```bash
   cd nginx/ssl
   # For development (self-signed)
   openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
     -keyout key.pem -out cert.pem \
     -subj "/C=US/ST=State/L=City/O=WHO-Cloud/CN=localhost"
   ```

4. **Build and start all services**
   ```bash
   docker-compose up -d --build
   ```

5. **Access application**
   - **Public Portal**: http://localhost (through nginx reverse proxy)
   - **Next.js Dashboard**: http://localhost:3001 (database statistics)
   - **Database Stats API**: http://localhost/api/public/database-stats
   - **Direct API**: http://localhost:8080 (development/testing)
   - **OAuth2 Login**: http://localhost/login/oauth2/authorization/google
   - **nginx Status**: http://localhost/nginx_status (monitoring)

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

### Environment Variables

Create `.env` file in project root (automatically loaded by Docker Compose):
```env
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=authdb
DB_USER=whocloud
DB_PASSWORD=whocloud123

# JWT Secret (REQUIRED - use alphanumeric only, min 256 bits)
JWT_SECRET=n1DgZNcLCVixhobxWStmxXjI0LN1yRABYwUt2yNBtE-secure-jwt-secret-key-256bits

# Google OAuth 2.0 (REQUIRED for OAuth2 login)
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

**Important**: 
- `JWT_SECRET` must be alphanumeric (no special characters like `$`, `{`, `}` that Docker interprets)
- Google OAuth credentials must be configured in Google Cloud Console
- `.env` file is git-ignored for security

### Access Points

- **Next.js Dashboard**: http://localhost:3001 (database statistics)
- **nginx Reverse Proxy**: http://localhost (routes to Next.js & API Gateway)
- **API Gateway**: http://localhost:8080
- **Public Service**: http://localhost:8089
- **Auth Service**: http://localhost:8081
- **User Management**: http://localhost:8082
- **Admin Portal**: http://localhost:8088

### Testing Authentication & Endpoints

**1. OAuth2 Login (Google)**
```bash
# Open in browser
http://localhost:8081/login/oauth2/authorization/google

# After authentication, you'll receive:
# - Access Token (JWT)
# - Refresh Token
# - User info with GUEST role
```

**2. Test with JWT Token**
```bash
# Replace YOUR_TOKEN with the access token from OAuth2 response
TOKEN="your-jwt-token-here"

# Public endpoint (no auth required)
curl http://localhost:8080/api/public/welcome

# Information Service A (GUEST can access)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/information/a

# Business Service 1 (GUEST denied - requires USER role)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/business/service1
# Returns: 403 Forbidden

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

## 🎨 Frontend Integration

### Next.js Dashboard (Port 3001)

**Technology Stack:**
- **Next.js 15** with App Router
- **React 19** with TypeScript
- **Tailwind CSS** for styling
- **Server-Side Rendering (SSR)** for optimal performance

**Features:**
- 📊 Real-time database statistics display
- 🔄 Automatic data refresh on page load
- 📱 Responsive design with modern UI
- 🌐 Integrated with nginx reverse proxy

**Access Points:**
- **Dashboard**: http://localhost:3001
- **Via nginx**: http://localhost (routes to Next.js)
- **Database Stats API**: http://localhost/api/public/database-stats

**Architecture Flow:**
```
Browser → nginx (80) → Next.js (3001) → nginx → API Gateway (8080) → public-web-service (8089) → PostgreSQL
```

### Database Statistics Endpoint ✅

**Endpoint**: `GET /api/public/database-stats`

**Response Example:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "totalTables": 1,
    "tables": [
      {
        "tableName": "test_data",
        "recordCount": 12
      }
    ],
    "timestamp": "2026-01-02T22:41:06.357165612"
  },
  "error": null,
  "timestamp": "2026-01-02T22:41:06..."
}
```

**Implementation:**
- Service: `public-web-service` (Port 8089)
- Queries: `information_schema.tables` + `pg_stat_user_tables`
- Database: `whocloud` (dedicated database for public data)
- Dependencies: `spring-boot-starter-jdbc` + PostgreSQL driver ✅

### Database Architecture

**Multi-Database Design** (Database-per-Service Pattern):

| Service | Database | Tables | Purpose |
|---------|----------|--------|---------|
| **public-web-service** | `whocloud` | `test_data` | Public statistics and test data |
| **auth-service** | `authdb` | `users`, `refresh_tokens`, `audit_logs` | Authentication & OAuth2 |
| **user-management-service** | `userdb` | User management tables | User profiles & settings |

**Test Data:**
- Table: `test_data` (4 fields: id, name, category, value, created_at)
- Records: 12 test entries with varied categories
- Categories: Electronics, Clothing, Food, Healthcare, Software, Shipping, Manufacturing, Automotive, Infrastructure, Cloud
- Purpose: Verify dashboard displays multi-table statistics correctly

**Connection Details:**
```yaml
# public-web-service
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/whocloud
    
# auth-service  
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/authdb
```

## � Authentication Setup

### Google OAuth 2.0 Configuration

**1. Create Google OAuth 2.0 Credentials**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Navigate to **APIs & Services > Credentials**
4. Click **Create Credentials > OAuth 2.0 Client ID**
5. Configure OAuth consent screen:
   - User Type: External
   - App name: WHO Cloud
   - Support email: your-email@example.com
   - Scopes: email, profile
6. Create OAuth 2.0 Client ID:
   - Application type: Web application
   - Name: WHO Cloud Auth Service
   - Authorized redirect URIs:
     - `http://localhost:8081/login/oauth2/code/google`

**2. Configure Environment Variables**

Add to `.env` file:
```env
GOOGLE_CLIENT_ID=329578610455-xxxxxxxxxxxxxxxxxx.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-xxxxxxxxxxxxxxxxxxxxx
```

**3. Database Schema**

The `users` table enforces role constraint:
```sql
ALTER TABLE users ADD CONSTRAINT users_role_check 
CHECK (role IN ('GUEST', 'USER', 'ADMIN', 'MODERATOR'));
```

**4. OAuth2 Flow**

1. **User initiates login**: `GET http://localhost:8081/login/oauth2/authorization/google`
2. **Redirects to Google** for authentication
3. **Google callback**: `GET http://localhost:8081/login/oauth2/code/google?code=...`
4. **Auto-registration**: New users created with `GUEST` role
5. **JWT tokens returned**: HTML page displays access token and refresh token
6. **Token usage**: Include in Authorization header: `Bearer {token}`

**5. User Management**

- **Auto-registration**: First-time Google login → `GUEST` role
- **Role upgrade**: Admin promotes `GUEST` → `USER` via admin portal
- **Database lookup**: By `google_id` first, then `email`
- **Last login tracking**: Updated on each successful authentication

### JWT Token Structure

```json
{
  "sub": "username",
  "role": "GUEST",
  "iat": 1735776000,
  "exp": 1735862400
}
```

**Token Validity**: 24 hours (86400000 ms)

### Role-Based Access Control

| Role | Information Services | Business Services | Admin Portal |
|------|---------------------|-------------------|-------------|
| GUEST | ✅ Read-only | ❌ Denied | ❌ Denied |
| USER | ✅ Full access | ✅ Full access | ❌ Denied |
| ADMIN | ✅ Full access | ✅ Full access | ✅ Full access |
| MODERATOR | ✅ Full access | ✅ Full access | ⚠️ Limited |

## �🔧 Development

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
- **Databases**: 
  - `whocloud` - Public service database (test_data table with 12 records) ✅
  - `authdb` - Authentication database (users, refresh_tokens, audit_logs)
  - `userdb` - User management database
- **Username**: `whocloud`
- **Password**: `whocloud123` (development only)

Connect to database:
```bash
# Connect to whocloud database
docker exec -it whocloud-postgres psql -U whocloud -d whocloud

# List all databases
docker exec -it whocloud-postgres psql -U whocloud -c "\l"

# View test data
docker exec -it whocloud-postgres psql -U whocloud -d whocloud -c "SELECT * FROM test_data;"
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

# Database statistics (via API Gateway)
curl http://localhost/api/public/database-stats

# Database statistics (direct)
curl http://localhost:8089/api/public/database-stats
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "totalTables": 1,
    "tables": [{"tableName": "test_data", "recordCount": 12}],
    "timestamp": "2026-01-02T22:41:06..."
  }
}
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
- `GET /api/public/database-stats` - Database statistics (tables & record counts) ✅

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

### Authentication ✅
- `GET /login/oauth2/authorization/google` - Initiate Google OAuth2 login
- `GET /login/oauth2/code/google` - OAuth2 callback (handled automatically)
- `POST /api/auth/login` - Login with credentials (TODO)
- `POST /api/auth/register` - Register new user (TODO)
- `POST /api/auth/refresh` - Refresh JWT token (TODO)
- `GET /api/auth/validate` - Validate JWT token (TODO)

**OAuth2 Response Example**:
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "kvoznjuk",
    "email": "kvoznjuk@gmail.com",
    "fullName": "Konstantin Voznjuk",
    "role": "GUEST",
    "googleId": "113418038115658957172"
  }
}
```

## 🎯 Roadmap

### Phase 1: Core Infrastructure ✅
- [x] Multi-module Gradle project with Kotlin DSL
- [x] Spring Boot 3.4.1 with Java 21
- [x] API Gateway with Spring Cloud Gateway
- [x] PostgreSQL database setup with multi-database architecture
- [x] Docker containerization
- [x] Basic REST endpoints
- [x] nginx reverse proxy with routing
- [x] Next.js frontend with SSR
- [x] Database statistics endpoint
- [x] Full stack integration (Frontend → nginx → API Gateway → Services → PostgreSQL)

### Phase 2: Authentication & Authorization ✅
- [x] Implement JWT authentication
- [x] Google OAuth 2.0 integration with auto-registration
- [x] User entity with GUEST/USER/ADMIN/MODERATOR roles
- [x] Role-based access control (@PreAuthorize)
- [x] JWT filter for protected endpoints (API Gateway)
- [x] Database constraint for role validation
- [x] Transactional OAuth2 success handler
- [ ] Traditional username/password login
- [ ] JWT refresh token endpoint
- [ ] Admin portal for user management

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
- **Branch**: docker-success (OAuth2 implemented)

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