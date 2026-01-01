# Authentication Service API Documentation

## Overview

The auth-service provides comprehensive JWT-based authentication and authorization for the WHO Cloud microservices platform. It handles user registration, login, token management, and validation.

**Base URL**: `http://localhost:8081` (direct) or `http://localhost:8080/api/auth` (via API Gateway)

**Authentication**: Most endpoints are public. Protected endpoints require a valid JWT token in the Authorization header.

---

## Table of Contents

- [Authentication Flow](#authentication-flow)
- [API Endpoints](#api-endpoints)
  - [Register User](#register-user)
  - [Login](#login)
  - [Refresh Token](#refresh-token)
  - [Validate Token](#validate-token)
  - [Logout](#logout)
  - [Get Current User](#get-current-user)
- [Data Models](#data-models)
- [Error Handling](#error-handling)
- [Security Considerations](#security-considerations)
- [Testing Examples](#testing-examples)

---

## Authentication Flow

### 1. Registration & Login Flow

```
Client                    Auth Service                Database
  |                            |                          |
  |-- POST /register --------->|                          |
  |                            |-- Save User ------------->|
  |                            |<- User Created ----------|
  |                            |-- Generate Tokens ------>|
  |<- 201 (tokens + user) -----|                          |
  |                            |                          |
  |-- POST /login ------------>|                          |
  |                            |-- Verify Credentials --->|
  |                            |<- User Found ------------|
  |                            |-- Generate Tokens ------>|
  |<- 200 (tokens + user) -----|                          |
```

### 2. Token Usage Flow

```
Client                    Auth Service                Protected Service
  |                            |                          |
  |-- Request + JWT Token ---->|                          |
  |                            |-- Validate Token ------->|
  |                            |<- Token Valid -----------|
  |<- Protected Resource ------|                          |
  |                            |                          |
  |-- POST /refresh (refresh)->|                          |
  |<- 200 (new access token)---|                          |
```

---

## API Endpoints

### Register User

Register a new user account.

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "fullName": "John Doe"
}
```

**Validation Rules**:
- `username`: 3-50 characters, required
- `email`: Valid email format, required
- `password`: Minimum 8 characters, required
- `fullName`: Optional

**Success Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "role": "USER"
    }
  },
  "timestamp": "2026-01-01T10:30:00"
}
```

**Error Responses**:

400 Bad Request - Username exists:
```json
{
  "success": false,
  "error": "Username already exists",
  "timestamp": "2026-01-01T10:30:00"
}
```

400 Bad Request - Validation failed:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username is required",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2026-01-01T10:30:00"
}
```

---

### Login

Authenticate user and receive JWT tokens.

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "username": "john_doe",
  "password": "SecurePassword123!"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "role": "USER"
    }
  },
  "timestamp": "2026-01-01T10:30:00"
}
```

**Error Responses**:

401 Unauthorized - Invalid credentials:
```json
{
  "success": false,
  "error": "Invalid username or password",
  "timestamp": "2026-01-01T10:30:00"
}
```

---

### Refresh Token

Obtain a new access token using a refresh token.

**Endpoint**: `POST /api/auth/refresh`

**Request Body**:
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "role": "USER"
    }
  },
  "timestamp": "2026-01-01T10:30:00"
}
```

**Error Responses**:

400 Bad Request - Invalid token:
```json
{
  "success": false,
  "error": "Invalid refresh token",
  "timestamp": "2026-01-01T10:30:00"
}
```

400 Bad Request - Token expired:
```json
{
  "success": false,
  "error": "Refresh token has expired",
  "timestamp": "2026-01-01T10:30:00"
}
```

400 Bad Request - Token revoked:
```json
{
  "success": false,
  "error": "Refresh token has been revoked",
  "timestamp": "2026-01-01T10:30:00"
}
```

---

### Validate Token

Validate a JWT access token (used by other services).

**Endpoint**: `POST /api/auth/validate`

**Request Body**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "valid": true,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER",
    "message": "Token is valid"
  },
  "timestamp": "2026-01-01T10:30:00"
}
```

**Invalid Token Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "valid": false,
    "message": "Invalid or expired token"
  },
  "timestamp": "2026-01-01T10:30:00"
}
```

---

### Logout

Logout user and revoke all refresh tokens.

**Endpoint**: `POST /api/auth/logout`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "data": "Logged out successfully",
  "timestamp": "2026-01-01T10:30:00"
}
```

**Error Response**:

401 Unauthorized - No token provided:
```json
{
  "success": false,
  "error": "Full authentication is required to access this resource",
  "timestamp": "2026-01-01T10:30:00"
}
```

---

### Get Current User

Get currently authenticated user details.

**Endpoint**: `GET /api/auth/me`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "username": "john_doe",
    "authorities": [
      {
        "authority": "ROLE_USER"
      }
    ],
    "accountNonExpired": true,
    "accountNonLocked": true,
    "credentialsNonExpired": true,
    "enabled": true
  },
  "timestamp": "2026-01-01T10:30:00"
}
```

---

## Data Models

### User Entity

```java
{
  "id": Long,
  "username": String,          // Unique, 3-50 characters
  "email": String,             // Unique, valid email
  "password": String,          // BCrypt hashed
  "fullName": String,          // Optional
  "role": "USER|ADMIN|MODERATOR",
  "isEnabled": Boolean,        // Default: true
  "isAccountNonExpired": Boolean,
  "isAccountNonLocked": Boolean,
  "isCredentialsNonExpired": Boolean,
  "createdAt": DateTime,
  "updatedAt": DateTime,
  "lastLogin": DateTime
}
```

### Refresh Token Entity

```java
{
  "id": Long,
  "token": String,             // UUID
  "user": User,                // FK to users table
  "expiresAt": DateTime,       // 30 days from creation
  "createdAt": DateTime,
  "revoked": Boolean           // Default: false
}
```

### JWT Token Claims

```json
{
  "sub": "john_doe",           // Username
  "role": "USER",              // User role
  "iat": 1735731000,           // Issued at (timestamp)
  "exp": 1735817400            // Expiration (timestamp)
}
```

---

## Error Handling

### HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success - Request completed successfully |
| 201 | Created - User registered successfully |
| 400 | Bad Request - Invalid input or validation error |
| 401 | Unauthorized - Invalid credentials or token |
| 404 | Not Found - User not found |
| 500 | Internal Server Error - Unexpected error |

### Error Response Structure

```json
{
  "success": false,
  "error": "Error message",
  "timestamp": "2026-01-01T10:30:00"
}
```

### Common Error Messages

| Error Message | Cause | Solution |
|--------------|-------|----------|
| "Username already exists" | Duplicate username | Use different username |
| "Email already exists" | Duplicate email | Use different email |
| "Invalid username or password" | Wrong credentials | Check credentials |
| "Invalid refresh token" | Token not found in DB | Login again |
| "Refresh token has expired" | Token older than 30 days | Login again |
| "Refresh token has been revoked" | User logged out | Login again |
| "Token validation failed" | Malformed or expired JWT | Get new token |

---

## Security Considerations

### Password Security

- **Hashing Algorithm**: BCrypt with strength 10
- **Minimum Length**: 8 characters
- **Recommendations**: 
  - Use mix of uppercase, lowercase, numbers, symbols
  - Avoid common passwords
  - Change passwords periodically

### Token Security

- **Access Token**:
  - Type: JWT (JSON Web Token)
  - Algorithm: HS256 (HMAC with SHA-256)
  - Expiration: 24 hours
  - Storage: Memory or secure storage (never localStorage)
  
- **Refresh Token**:
  - Type: UUID v4
  - Expiration: 30 days
  - Storage: Database with revocation support
  - Single active token per user

### JWT Secret

⚠️ **Production**: Change the default JWT secret in environment variables!

```bash
# Development (default)
JWT_SECRET=who-cloud-secret-key-2026-change-this-in-production-minimum-256-bits

# Production (example)
JWT_SECRET=$(openssl rand -base64 32)
```

### CORS Configuration

Allowed origins (configurable):
- `http://localhost:3000` (React)
- `http://localhost:8080` (API Gateway)
- `http://localhost:4200` (Angular)

### Rate Limiting

⚠️ **TODO**: Implement rate limiting for:
- Registration: 5 requests per hour per IP
- Login: 10 requests per 15 minutes per IP
- Token refresh: 20 requests per hour per token

---

## Testing Examples

### Using cURL

#### Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPassword123!",
    "fullName": "Test User"
  }'
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPassword123!"
  }'
```

#### Access Protected Endpoint

```bash
# Save token from login response
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

#### Refresh Token

```bash
REFRESH_TOKEN="550e8400-e29b-41d4-a716-446655440000"

curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "'$REFRESH_TOKEN'"
  }'
```

#### Validate Token (Service-to-Service)

```bash
curl -X POST http://localhost:8080/api/auth/validate \
  -H "Content-Type: application/json" \
  -d '{
    "token": "'$TOKEN'"
  }'
```

#### Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

---

### Using PowerShell

#### Register

```powershell
$body = @{
    username = "testuser"
    email = "test@example.com"
    password = "TestPassword123!"
    fullName = "Test User"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

#### Login and Save Token

```powershell
$body = @{
    username = "testuser"
    password = "TestPassword123!"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

$token = $response.data.accessToken
$refreshToken = $response.data.refreshToken
```

#### Use Token for Protected Request

```powershell
$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/me" `
    -Method Get `
    -Headers $headers
```

---

### Using Postman

1. **Create Environment**:
   - Variable: `base_url` = `http://localhost:8080`
   - Variable: `access_token` = (will be set automatically)
   - Variable: `refresh_token` = (will be set automatically)

2. **Register User**:
   - Method: POST
   - URL: `{{base_url}}/api/auth/register`
   - Body (JSON):
     ```json
     {
       "username": "testuser",
       "email": "test@example.com",
       "password": "TestPassword123!",
       "fullName": "Test User"
     }
     ```
   - Tests (save tokens):
     ```javascript
     pm.environment.set("access_token", pm.response.json().data.accessToken);
     pm.environment.set("refresh_token", pm.response.json().data.refreshToken);
     ```

3. **Login**:
   - Method: POST
   - URL: `{{base_url}}/api/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "testuser",
       "password": "TestPassword123!"
     }
     ```
   - Tests (save tokens):
     ```javascript
     pm.environment.set("access_token", pm.response.json().data.accessToken);
     pm.environment.set("refresh_token", pm.response.json().data.refreshToken);
     ```

4. **Protected Request**:
   - Method: GET
   - URL: `{{base_url}}/api/auth/me`
   - Authorization: Bearer Token
   - Token: `{{access_token}}`

---

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_enabled BOOLEAN DEFAULT TRUE,
    is_account_non_expired BOOLEAN DEFAULT TRUE,
    is_account_non_locked BOOLEAN DEFAULT TRUE,
    is_credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

### Refresh Tokens Table

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
```

---

## Scheduled Tasks

### Token Cleanup

Expired refresh tokens are automatically cleaned up daily at 2 AM.

```java
@Scheduled(cron = "0 0 2 * * ?")
public void cleanupExpiredTokens()
```

This removes all refresh tokens where `expires_at < NOW()`.

---

## Integration with Other Services

### Service-to-Service Authentication

Other microservices can validate tokens by calling the `/validate` endpoint:

```java
// Example: User Management Service validating a token
@Autowired
private RestTemplate restTemplate;

public ValidateTokenResponse validateToken(String token) {
    ValidateTokenRequest request = new ValidateTokenRequest(token);
    
    ResponseEntity<ApiResponse<ValidateTokenResponse>> response = 
        restTemplate.postForEntity(
            "http://auth-service:8081/api/auth/validate",
            request,
            new ParameterizedTypeReference<>() {}
        );
    
    return response.getBody().getData();
}
```

### API Gateway Integration

The API Gateway should forward authentication tokens to backend services:

```yaml
# API Gateway Configuration
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://auth-service:8081
          predicates:
            - Path=/api/auth/**
          filters:
            - PreserveHostHeader
```

---

## Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_HOST` | PostgreSQL host | localhost | No |
| `DB_PORT` | PostgreSQL port | 5432 | No |
| `DB_NAME` | Database name | authdb | No |
| `DB_USER` | Database username | whocloud | No |
| `DB_PASSWORD` | Database password | whocloud123 | No |
| `JWT_SECRET` | JWT signing secret | (default key) | **Yes (Production)** |

**Production Example**:

```bash
export DB_HOST=postgres-prod.example.com
export DB_PASSWORD=$(vault read -field=password secret/db/auth)
export JWT_SECRET=$(vault read -field=secret secret/jwt)
```

---

## Troubleshooting

### Issue: "Invalid username or password"

**Cause**: Incorrect credentials or user doesn't exist

**Solution**:
1. Verify username is correct (case-sensitive)
2. Check if user is registered
3. Ensure password matches registration

### Issue: "Username already exists"

**Cause**: Attempting to register with existing username

**Solution**: Choose a different username

### Issue: "Token validation failed"

**Causes**:
- Token expired (> 24 hours)
- Token malformed
- Wrong JWT secret

**Solution**:
1. Get new token via refresh endpoint
2. Check token format (should be JWT)
3. Verify JWT_SECRET matches

### Issue: "Refresh token has expired"

**Cause**: Refresh token older than 30 days

**Solution**: Login again to get new tokens

### Issue: Database connection failed

**Cause**: PostgreSQL not running or wrong credentials

**Solution**:
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs who-cloud-postgres

# Verify connection
docker exec -it who-cloud-postgres psql -U whocloud -d authdb -c "\dt"
```

---

## Future Enhancements

- [ ] OAuth2 integration (Google, GitHub, Microsoft)
- [ ] Two-factor authentication (2FA)
- [ ] Password reset via email
- [ ] Rate limiting
- [ ] Account lockout after failed login attempts
- [ ] Password complexity requirements
- [ ] User roles and permissions management
- [ ] Audit logging for authentication events
- [ ] Token blacklisting
- [ ] Remember me functionality

---

**Version**: 1.0.0  
**Last Updated**: January 1, 2026  
**Maintainer**: WHO Cloud Team
