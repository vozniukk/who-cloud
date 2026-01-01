# WHO Cloud Microservices

Spring Boot microservice application with Google SSO authentication and role-based access control.

## Architecture

### Microservices
- **API Gateway** (8080) - Routes requests to all services
- **Auth Service** (8081) - Google SSO + JWT token generation
- **User Management Service** (8082) - User profiles and role management
- **Information Service A** (8083) - Protected information endpoint
- **Information Service B** (8084) - Protected information endpoint
- **Business Service 1** (8085) - Business logic service
- **Business Service 2** (8086) - Business logic service
- **Business Service 3** (8087) - Business logic service
- **Admin Portal Service** (8088) - Administrative functions
- **Public Web Service** (8089) - Public pages (no auth required)

### User Roles
- `USER` - Basic authenticated user
- `MANAGER` - Manager level access
- `ADMINISTRATOR` - Full administrative access
- `OBSERVER` - Read-only observer access

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Google OAuth 2.0 credentials

### Setup

1. **Start PostgreSQL**
   ```bash
   docker-compose up -d
   ```

2. **Configure Google OAuth 2.0**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing
   - Enable Google+ API
   - Create OAuth 2.0 credentials
   - Set redirect URI: `http://localhost:8081/login/oauth2/code/google`
   - Copy Client ID and Client Secret

3. **Set Environment Variables**
   ```bash
   export GOOGLE_CLIENT_ID=your-client-id
   export GOOGLE_CLIENT_SECRET=your-client-secret
   export JWT_SECRET=your-secure-256-bit-secret-key
   ```

4. **Build All Services**
   ```bash
   mvn clean install
   ```

5. **Run Services**
   
   You can run services individually:
   ```bash
   # API Gateway
   cd api-gateway && mvn spring-boot:run
   
   # Auth Service
   cd auth-service && mvn spring-boot:run
   
   # User Management Service
   cd user-management-service && mvn spring-boot:run
   
   # ... and so on for other services
   ```

### Access Points

- API Gateway: http://localhost:8080
- Auth Service: http://localhost:8081
- User Management: http://localhost:8082
- Public Pages: http://localhost:8089/api/public

### Authentication Flow

1. User navigates to login page
2. Redirects to Google OAuth 2.0
3. User authenticates with Google
4. Auth service creates/updates user in PostgreSQL
5. JWT token generated with user roles
6. Client includes JWT in subsequent requests

## Development

### Project Structure
```
who-cloud/
├── api-gateway/              # Spring Cloud Gateway
├── auth-service/             # Google SSO + JWT
├── user-management-service/  # User CRUD + roles
├── information-service-a/    # Protected info endpoint
├── information-service-b/    # Protected info endpoint
├── business-service-1/       # Business logic
├── business-service-2/       # Business logic
├── business-service-3/       # Business logic
├── admin-portal-service/     # Admin functions
├── public-web-service/       # Public pages
├── common/                   # Shared DTOs, utilities
└── docker-compose.yml        # Local PostgreSQL
```

### Database

PostgreSQL runs on port 5432:
- Database: `whocloud`
- Username: `whocloud_user`
- Password: `whocloud_pass`

### Testing

Run tests for all modules:
```bash
mvn test
```

## Next Steps

1. Implement JWT filter for protected services
2. Create user entities and repositories
3. Implement Google SSO controller in auth-service
4. Add role-based endpoint security with `@PreAuthorize`
5. Create REST controllers for each service
6. Add Swagger/OpenAPI documentation
7. Implement circuit breakers with Resilience4j
8. Add distributed tracing

## License

Proprietary