# Auth Service - Architecture & Design Documentation

## Table of Contents

- [Overview](#overview)
- [Architecture Diagram](#architecture-diagram)
- [Layered Architecture](#layered-architecture)
- [Component Design](#component-design)
- [Data Flow](#data-flow)
- [Security Architecture](#security-architecture)
- [Database Design](#database-design)
- [Authentication Flow](#authentication-flow)
- [Token Management](#token-management)
- [Error Handling Strategy](#error-handling-strategy)
- [Configuration Management](#configuration-management)
- [Scheduled Tasks](#scheduled-tasks)
- [Integration Points](#integration-points)
- [Design Patterns Used](#design-patterns-used)
- [Scalability Considerations](#scalability-considerations)
- [Performance Optimization](#performance-optimization)

---

## Overview

The `auth-service` is a microservice responsible for authentication and authorization in the WHO Cloud platform. It implements JWT-based stateless authentication with refresh token support, providing secure user management and token validation capabilities.

### Key Characteristics

- **Type**: REST API Microservice
- **Port**: 8081 (direct), 8080/api/auth (via API Gateway)
- **Framework**: Spring Boot 3.4.1
- **Java Version**: 21 LTS
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL 16
- **Session Management**: Stateless
- **Token Type**: JWT (HS256) + UUID Refresh Tokens

### Primary Responsibilities

1. **User Management**: Registration, profile management
2. **Authentication**: Login, logout, session management
3. **Authorization**: Token generation, validation
4. **Token Lifecycle**: Refresh, revocation, cleanup
5. **Security**: Password hashing, token encryption

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Auth Service (Port 8081)                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Controller Layer                            │   │
│  │  ┌──────────────────────────────────────────────────┐   │   │
│  │  │  AuthController                                   │   │   │
│  │  │  - POST /register                                 │   │   │
│  │  │  - POST /login                                    │   │   │
│  │  │  - POST /refresh                                  │   │   │
│  │  │  - POST /validate                                 │   │   │
│  │  │  - POST /logout                                   │   │   │
│  │  │  - GET  /me                                       │   │   │
│  │  └──────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                             ↓                                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Service Layer                               │   │
│  │  ┌────────────────┐  ┌─────────────────────────────┐   │   │
│  │  │  AuthService   │  │ UserDetailsServiceImpl       │   │   │
│  │  │  - register    │  │ - loadUserByUsername         │   │   │
│  │  │  - login       │  └─────────────────────────────┘   │   │
│  │  │  - refresh     │                                     │   │
│  │  │  - validate    │  ┌─────────────────────────────┐   │   │
│  │  │  - logout      │  │  JwtUtil                     │   │   │
│  │  │  - cleanup     │  │  - generateToken             │   │   │
│  │  └────────────────┘  │  - validateToken             │   │   │
│  │                      │  - extractClaims             │   │   │
│  │                      └─────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                             ↓                                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Repository Layer                            │   │
│  │  ┌────────────────────┐  ┌─────────────────────────┐   │   │
│  │  │  UserRepository    │  │ RefreshTokenRepository   │   │   │
│  │  │  (JPA)             │  │ (JPA)                    │   │   │
│  │  └────────────────────┘  └─────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                             ↓                                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Entity Layer                                │   │
│  │  ┌────────────────────┐  ┌─────────────────────────┐   │   │
│  │  │  User              │  │ RefreshToken             │   │   │
│  │  │  - id              │  │ - id                     │   │   │
│  │  │  - username        │  │ - token                  │   │   │
│  │  │  - email           │  │ - user_id (FK)           │   │   │
│  │  │  - password        │  │ - expires_at             │   │   │
│  │  │  - role            │  │ - revoked                │   │   │
│  │  └────────────────────┘  └─────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                             ↓                                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              PostgreSQL Database                         │   │
│  │              (authdb)                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Cross-Cutting Concerns                      │   │
│  │  - Spring Security (SecurityConfig)                      │   │
│  │  - CORS Configuration (CorsConfig)                       │   │
│  │  - Exception Handling (GlobalExceptionHandler)           │   │
│  │  - Scheduled Tasks (TokenCleanupScheduler)               │   │
│  │  - Validation (Bean Validation)                          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Layered Architecture

The auth-service follows a classic **4-tier layered architecture** with clear separation of concerns:

### 1. **Presentation Layer** (Controller)

**Purpose**: Handle HTTP requests/responses, input validation, response formatting

**Components**:
- `AuthController` - REST endpoints

**Responsibilities**:
- HTTP method mapping (`@PostMapping`, `@GetMapping`)
- Request body binding with `@RequestBody`, `@Valid`
- Response entity creation with appropriate HTTP status codes
- Minimal business logic (delegates to service layer)

**Example**:
```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
    AuthResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
}
```

### 2. **Business Logic Layer** (Service)

**Purpose**: Implement core business logic, orchestrate operations

**Components**:
- `AuthService` - Authentication operations
- `UserDetailsServiceImpl` - Spring Security integration
- `JwtUtil` - Token operations

**Responsibilities**:
- Business rule enforcement (e.g., duplicate username check)
- Transaction management (`@Transactional`)
- Coordination between repositories
- Token generation and validation logic
- Password encoding/verification

**Example**:
```java
@Transactional
public AuthResponse register(RegisterRequest request) {
    // Business rule: Check duplicates
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new IllegalArgumentException("Username already exists");
    }
    
    // Create and save user
    User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
    user = userRepository.save(user);
    
    // Generate tokens
    String accessToken = jwtUtil.generateToken(user);
    String refreshToken = createRefreshToken(user);
    
    return buildAuthResponse(user, accessToken, refreshToken);
}
```

### 3. **Data Access Layer** (Repository)

**Purpose**: Abstract database operations, provide CRUD functionality

**Components**:
- `UserRepository` - User entity operations
- `RefreshTokenRepository` - Refresh token operations

**Responsibilities**:
- JPA repository methods (Spring Data)
- Custom query methods
- Database transaction handling (via JPA)

**Example**:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### 4. **Domain Layer** (Entity/DTO)

**Purpose**: Define domain models, data transfer objects

**Components**:
- **Entities**: `User`, `RefreshToken`
- **DTOs**: `LoginRequest`, `RegisterRequest`, `AuthResponse`, etc.

**Responsibilities**:
- Data structure definition
- JPA annotations for ORM mapping
- Validation constraints
- Business domain representation

**Example**:
```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}
```

---

## Component Design

### 1. User Entity

**File**: `entity/User.java`

**Design Decisions**:

1. **Implements UserDetails**: Integrates with Spring Security
   - Provides `getAuthorities()`, `getUsername()`, `getPassword()`
   - Enables direct use in authentication

2. **Account Status Flags**: Granular control
   - `isEnabled`: Account activation
   - `isAccountNonExpired`: Account validity
   - `isAccountNonLocked`: Lock/unlock capability
   - `isCredentialsNonExpired`: Force password change

3. **Timestamp Tracking**: Audit trail
   - `createdAt`, `updatedAt`: JPA lifecycle hooks (`@PrePersist`, `@PreUpdate`)
   - `lastLogin`: Updated on successful authentication

4. **Role Enumeration**: Type-safe role management
   ```java
   public enum Role {
       USER,    // Standard user
       ADMIN,   // Administrator
       MODERATOR // Moderator
   }
   ```

**UML Diagram**:
```
┌─────────────────────────────┐
│          User               │
├─────────────────────────────┤
│ - id: Long                  │
│ - username: String          │
│ - email: String             │
│ - password: String          │
│ - fullName: String          │
│ - role: Role                │
│ - isEnabled: boolean        │
│ - isAccountNonExpired: bool │
│ - isAccountNonLocked: bool  │
│ - isCredentialsNonExp: bool │
│ - createdAt: LocalDateTime  │
│ - updatedAt: LocalDateTime  │
│ - lastLogin: LocalDateTime  │
├─────────────────────────────┤
│ + getAuthorities()          │
│ + isAccountNonExpired()     │
│ + isAccountNonLocked()      │
│ + isCredentialsNonExpired() │
│ + isEnabled()               │
└─────────────────────────────┘
```

### 2. RefreshToken Entity

**File**: `entity/RefreshToken.java`

**Design Decisions**:

1. **UUID Token**: Random, unpredictable, no sequential pattern
2. **Expiration**: 30 days from creation
3. **Revocation Support**: Soft delete via `revoked` flag
4. **User Association**: Many-to-One relationship with User
5. **Lazy Loading**: User loaded only when needed

**Lifecycle**:
```
[Created] → [Active] → [Expired/Revoked] → [Deleted (cleanup)]
```

### 3. JwtUtil Component

**File**: `util/JwtUtil.java`

**Responsibilities**:
- Token generation
- Token validation
- Claims extraction
- Expiration checking

**Key Methods**:

```java
// Generate token with user details
public String generateToken(UserDetails userDetails)

// Validate token against user
public boolean validateToken(String token, UserDetails userDetails)

// Extract username from token
public String extractUsername(String token)

// Extract role from token
public String extractRole(String token)

// Check if token expired
public boolean isTokenExpired(String token)
```

**Token Structure**:
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "role": "USER",
    "iat": 1735731000,
    "exp": 1735817400
  },
  "signature": "..."
}
```

**Security Features**:
- **Algorithm**: HS256 (HMAC with SHA-256)
- **Secret Key**: Configurable via `jwt.secret`
- **Minimum Key Size**: 256 bits
- **Signature Verification**: Prevents tampering

### 4. AuthService Component

**File**: `service/AuthService.java`

**Core Operations**:

#### 4.1 Registration Flow
```
Input: RegisterRequest
  ↓
Validate: Check username/email uniqueness
  ↓
Encode: BCrypt password
  ↓
Create: User entity
  ↓
Save: Database
  ↓
Generate: Access token + Refresh token
  ↓
Output: AuthResponse
```

#### 4.2 Login Flow
```
Input: LoginRequest
  ↓
Authenticate: Spring Security AuthenticationManager
  ↓
Verify: Username + Password
  ↓
Update: lastLogin timestamp
  ↓
Revoke: Old refresh tokens
  ↓
Generate: New access token + Refresh token
  ↓
Output: AuthResponse
```

#### 4.3 Token Refresh Flow
```
Input: RefreshTokenRequest
  ↓
Find: Refresh token in database
  ↓
Validate: Not revoked, not expired
  ↓
Load: Associated user
  ↓
Generate: New access token
  ↓
Output: AuthResponse (with same refresh token)
```

#### 4.4 Logout Flow
```
Input: Username
  ↓
Find: User by username
  ↓
Revoke: All user's refresh tokens
  ↓
Output: Success message
```

### 5. SecurityConfig Component

**File**: `config/SecurityConfig.java`

**Configuration Hierarchy**:
```
SecurityFilterChain
  ├─ CSRF: Disabled (stateless API)
  ├─ Authorization Rules:
  │    ├─ /api/auth/register: Permit all
  │    ├─ /api/auth/login: Permit all
  │    ├─ /api/auth/refresh: Permit all
  │    ├─ /api/auth/validate: Permit all
  │    ├─ /actuator/**: Permit all
  │    └─ All others: Authenticated
  ├─ Session Management: STATELESS
  └─ Authentication Provider: DaoAuthenticationProvider
       ├─ UserDetailsService: UserDetailsServiceImpl
       └─ PasswordEncoder: BCryptPasswordEncoder
```

**Security Filter Chain**:
```
HTTP Request
  ↓
DisableEncodeUrlFilter
  ↓
SecurityContextHolderFilter
  ↓
HeaderWriterFilter
  ↓
CorsFilter
  ↓
LogoutFilter
  ↓
RequestCacheAwareFilter
  ↓
SecurityContextHolderAwareRequestFilter
  ↓
AnonymousAuthenticationFilter
  ↓
SessionManagementFilter
  ↓
ExceptionTranslationFilter
  ↓
AuthorizationFilter
  ↓
Controller
```

### 6. GlobalExceptionHandler

**File**: `exception/GlobalExceptionHandler.java`

**Exception Mapping**:

| Exception | HTTP Status | Response |
|-----------|-------------|----------|
| `IllegalArgumentException` | 400 Bad Request | Error message |
| `BadCredentialsException` | 401 Unauthorized | "Invalid username or password" |
| `UsernameNotFoundException` | 404 Not Found | Error message |
| `AuthException` | 401 Unauthorized | Error message |
| `MethodArgumentNotValidException` | 400 Bad Request | Field errors map |
| `Exception` | 500 Internal Server Error | Generic error |

**Error Response Structure**:
```json
{
  "success": false,
  "error": "Error message",
  "timestamp": "2026-01-01T10:30:00"
}
```

**Validation Error Response**:
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

## Data Flow

### Complete Registration Flow

```
┌────────┐                                    ┌──────────────┐
│ Client │                                    │ Auth Service │
└───┬────┘                                    └──────┬───────┘
    │                                                 │
    │ POST /api/auth/register                        │
    │ {username, email, password, fullName}          │
    ├────────────────────────────────────────────────>│
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ AuthController  │
    │                                        │ @Valid validates│
    │                                        │ RegisterRequest │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │  AuthService    │
    │                                        │ - Check         │
    │                                        │   duplicates    │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ UserRepository  │
    │                                        │ existsByUsername│
    │                                        │ existsByEmail   │
    │                                        └────────┬────────┘
    │                                                 │
    │                                                 │ No duplicates
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │PasswordEncoder  │
    │                                        │ BCrypt hash     │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ UserRepository  │
    │                                        │ save(user)      │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ PostgreSQL      │
    │                                        │ INSERT INTO     │
    │                                        │ users           │
    │                                        └────────┬────────┘
    │                                                 │
    │                                                 │ User saved
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ JwtUtil         │
    │                                        │ generateToken() │
    │                                        └────────┬────────┘
    │                                                 │
    │                                                 │ Access token
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ AuthService     │
    │                                        │ createRefresh   │
    │                                        │ Token()         │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │RefreshTokenRepo │
    │                                        │ save(token)     │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ PostgreSQL      │
    │                                        │ INSERT INTO     │
    │                                        │ refresh_tokens  │
    │                                        └────────┬────────┘
    │                                                 │
    │                                                 │ Token saved
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ AuthService     │
    │                                        │ buildAuthResp() │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ AuthController  │
    │                                        │ HTTP 201 Created│
    │                                        └────────┬────────┘
    │                                                 │
    │ 201 Created                                    │
    │ {accessToken, refreshToken, user}              │
    │<────────────────────────────────────────────────┤
    │                                                 │
```

### Login Flow with Authentication

```
┌────────┐                                    ┌──────────────┐
│ Client │                                    │ Auth Service │
└───┬────┘                                    └──────┬───────┘
    │                                                 │
    │ POST /api/auth/login                           │
    │ {username, password}                           │
    ├────────────────────────────────────────────────>│
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ AuthController  │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ AuthService     │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │Authentication   │
    │                                        │Manager          │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │DaoAuthentication│
    │                                        │Provider         │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │UserDetailsSvc   │
    │                                        │loadUserByName() │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ UserRepository  │
    │                                        │ findByUsername()│
    │                                        └────────┬────────┘
    │                                                 │
    │                                                 │ User found
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │PasswordEncoder  │
    │                                        │ matches()       │
    │                                        └────────┬────────┘
    │                                                 │
    │                                                 │ Password OK
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ AuthService     │
    │                                        │ Update lastLogin│
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │RefreshTokenRepo │
    │                                        │ revokeAllByUser │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ JwtUtil         │
    │                                        │ generateToken() │
    │                                        └────────┬────────┘
    │                                                 │
    │                                        ┌────────▼────────┐
    │                                        │ AuthService     │
    │                                        │createRefreshTkn │
    │                                        └────────┬────────┘
    │                                                 │
    │ 200 OK                                         │
    │ {accessToken, refreshToken, user}              │
    │<────────────────────────────────────────────────┤
    │                                                 │
```

---

## Security Architecture

### Password Security

**Hashing Algorithm**: BCrypt

**Characteristics**:
- **Adaptive**: Configurable cost factor (default: 10)
- **Salted**: Automatic random salt per password
- **Slow**: Intentionally slow to resist brute-force
- **One-way**: Cannot decrypt, only verify

**Implementation**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Usage
String hashedPassword = passwordEncoder.encode(plainPassword);
boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);
```

**Storage Format**:
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
│  │  │                     │
│  │  │                     └─ Hash (31 chars)
│  │  └─ Salt (22 chars)
│  └─ Cost factor
└─ BCrypt version
```

### JWT Token Security

**Token Components**:

1. **Header**:
   ```json
   {
     "alg": "HS256",
     "typ": "JWT"
   }
   ```

2. **Payload** (Claims):
   ```json
   {
     "sub": "username",      // Subject (username)
     "role": "USER",         // User role
     "iat": 1735731000,      // Issued at
     "exp": 1735817400       // Expiration
   }
   ```

3. **Signature**:
   ```
   HMACSHA256(
     base64UrlEncode(header) + "." +
     base64UrlEncode(payload),
     secret
   )
   ```

**Security Properties**:
- **Integrity**: Signature prevents tampering
- **Authenticity**: Only server with secret can create valid tokens
- **Stateless**: No server-side storage needed
- **Expiration**: 24-hour validity

**Threat Mitigation**:

| Threat | Mitigation |
|--------|------------|
| Token Theft | Short expiration (24h), HTTPS required |
| Token Tampering | HMAC signature verification |
| Replay Attack | Token expiration, logout revokes refresh tokens |
| Secret Exposure | Configurable secret, environment variables |
| Brute Force | Rate limiting (TODO), account lockout (TODO) |

### Refresh Token Security

**Design Decisions**:

1. **UUID Format**: Random, unpredictable
2. **Database Storage**: Can be revoked
3. **Long Expiration**: 30 days (user convenience)
4. **One-per-User**: Old tokens revoked on login
5. **Soft Delete**: Revoked flag instead of deletion

**Security Trade-offs**:

| Aspect | Choice | Rationale |
|--------|--------|-----------|
| Storage | Database | Enables revocation, tracking |
| Format | UUID | Unpredictable, no info leakage |
| Expiration | 30 days | Balance security/UX |
| Revocation | On logout | Clean session termination |
| Rotation | On login only | Simpler implementation |

---

## Database Design

### Entity Relationship Diagram

```
┌─────────────────────────────┐
│          users              │
├─────────────────────────────┤
│ id (PK)                     │
│ username (UNIQUE)           │
│ email (UNIQUE)              │
│ password                    │
│ full_name                   │
│ role                        │
│ is_enabled                  │
│ is_account_non_expired      │
│ is_account_non_locked       │
│ is_credentials_non_expired  │
│ created_at                  │
│ updated_at                  │
│ last_login                  │
└─────────────┬───────────────┘
              │
              │ 1:N
              │
┌─────────────▼───────────────┐
│      refresh_tokens         │
├─────────────────────────────┤
│ id (PK)                     │
│ token (UNIQUE)              │
│ user_id (FK) ───────────────┤
│ expires_at                  │
│ created_at                  │
│ revoked                     │
└─────────────────────────────┘
```

### Table: users

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

-- Indexes for performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_last_login ON users(last_login);
```

**Index Justification**:
- `username`: Frequent lookups during login
- `email`: Used for duplicate checks, password reset
- `role`: Authorization queries
- `last_login`: Analytics, inactive user queries

### Table: refresh_tokens

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

-- Indexes for performance
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);
```

**Index Justification**:
- `token`: Lookup during refresh operations
- `user_id`: Find all user tokens for revocation
- `expires_at`: Cleanup expired tokens
- `revoked`: Filter active tokens

**Cascade Deletion**:
- When user deleted → all refresh tokens deleted
- Maintains referential integrity
- Prevents orphaned tokens

---

## Authentication Flow

### Detailed Authentication Sequence

```
┌───────┐           ┌──────────┐         ┌─────────┐        ┌──────────┐
│Client │           │Controller│         │ Service │        │Repository│
└───┬───┘           └────┬─────┘         └────┬────┘        └────┬─────┘
    │                    │                    │                  │
    │ 1. POST /login     │                    │                  │
    │ {user,pass}        │                    │                  │
    ├───────────────────>│                    │                  │
    │                    │                    │                  │
    │                    │ 2. login(request)  │                  │
    │                    ├───────────────────>│                  │
    │                    │                    │                  │
    │                    │                    │ 3. authenticate()│
    │                    │                    ├─────────┐        │
    │                    │                    │         │        │
    │                    │                    │ AuthenticationManager
    │                    │                    │         │        │
    │                    │                    │<────────┘        │
    │                    │                    │                  │
    │                    │                    │ 4. loadUserByUsername()
    │                    │                    ├─────────────────>│
    │                    │                    │                  │
    │                    │                    │                  │ 5. SELECT
    │                    │                    │                  │ FROM users
    │                    │                    │<─────────────────┤
    │                    │                    │ User             │
    │                    │                    │                  │
    │                    │                    │ 6. Password      │
    │                    │                    │    Verification  │
    │                    │                    ├─────────┐        │
    │                    │                    │         │        │
    │                    │                    │<────────┘        │
    │                    │                    │                  │
    │                    │                    │ 7. UPDATE        │
    │                    │                    │    last_login    │
    │                    │                    ├─────────────────>│
    │                    │                    │                  │
    │                    │                    │ 8. Revoke old    │
    │                    │                    │    refresh tokens│
    │                    │                    ├─────────────────>│
    │                    │                    │                  │
    │                    │                    │ 9. Generate JWT  │
    │                    │                    ├─────────┐        │
    │                    │                    │         │        │
    │                    │                    │<────────┘        │
    │                    │                    │                  │
    │                    │                    │ 10. Create       │
    │                    │                    │     refresh token│
    │                    │                    ├─────────────────>│
    │                    │                    │                  │
    │                    │                    │                  │ 11. INSERT
    │                    │                    │                  │ refresh_token
    │                    │                    │<─────────────────┤
    │                    │                    │                  │
    │                    │ AuthResponse       │                  │
    │                    │<───────────────────┤                  │
    │                    │                    │                  │
    │ 200 OK             │                    │                  │
    │ {tokens, user}     │                    │                  │
    │<───────────────────┤                    │                  │
    │                    │                    │                  │
```

---

## Token Management

### Token Lifecycle

```
┌─────────────┐
│   CREATED   │
│  (Generate) │
└──────┬──────┘
       │
       ▼
┌─────────────┐      24 hours elapsed
│   ACTIVE    ├──────────────────────┐
│   (Valid)   │                      │
└──────┬──────┘                      │
       │                             │
       │ User logout                 │
       │ or refresh                  │
       ▼                             ▼
┌─────────────┐              ┌─────────────┐
│  REVOKED    │              │   EXPIRED   │
│ (Blacklist) │              │  (Invalid)  │
└─────────────┘              └─────────────┘
```

### Token Types Comparison

| Property | Access Token (JWT) | Refresh Token (UUID) |
|----------|-------------------|----------------------|
| **Format** | JWT (3 parts) | UUID v4 |
| **Storage** | Client-side only | Database + Client |
| **Expiration** | 24 hours | 30 days |
| **Size** | ~200-300 bytes | 36 characters |
| **Revocable** | No (stateless) | Yes (database) |
| **Purpose** | API authentication | Token renewal |
| **Claims** | Username, role, exp | Just UUID |
| **Validation** | Signature check | Database lookup |

### Token Refresh Mechanism

**Why Refresh Tokens?**

1. **Security**: Short-lived access tokens limit exposure
2. **User Experience**: No frequent re-authentication
3. **Revocation**: Can invalidate sessions
4. **Flexibility**: Different expiration policies

**Refresh Flow**:

```
Client has expired access token
  ↓
Client sends refresh token
  ↓
Server validates refresh token
  ├─ Not found → 400 Bad Request
  ├─ Revoked → 400 Bad Request
  ├─ Expired → 400 Bad Request (delete from DB)
  └─ Valid → Generate new access token
       ↓
       Return new access token + same refresh token
```

**Implementation**:
```java
public AuthResponse refreshToken(RefreshTokenRequest request) {
    // Find token
    RefreshToken refreshToken = refreshTokenRepository
            .findByToken(request.getRefreshToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    
    // Check revoked
    if (refreshToken.isRevoked()) {
        throw new IllegalArgumentException("Refresh token has been revoked");
    }
    
    // Check expired
    if (refreshToken.isExpired()) {
        refreshTokenRepository.delete(refreshToken);
        throw new IllegalArgumentException("Refresh token has expired");
    }
    
    // Generate new access token
    User user = refreshToken.getUser();
    String newAccessToken = jwtUtil.generateToken(user);
    
    return buildAuthResponse(user, newAccessToken, refreshToken.getToken());
}
```

---

## Error Handling Strategy

### Exception Hierarchy

```
RuntimeException
  │
  ├─ IllegalArgumentException
  │    ├─ "Username already exists"
  │    ├─ "Email already exists"
  │    ├─ "Invalid refresh token"
  │    ├─ "Refresh token has expired"
  │    └─ "Refresh token has been revoked"
  │
  ├─ AuthException (Custom)
  │    └─ Authentication-specific errors
  │
  ├─ BadCredentialsException (Spring Security)
  │    └─ "Invalid username or password"
  │
  └─ UsernameNotFoundException (Spring Security)
       └─ "User not found with username: {username}"

MethodArgumentNotValidException (Validation)
  └─ Bean validation errors (e.g., @NotBlank, @Email)
```

### Error Response Strategy

**Consistent Format**:
```json
{
  "success": false,
  "error": "Error message",
  "timestamp": "2026-01-01T10:30:00"
}
```

**Benefits**:
1. **Predictable**: Clients always know response structure
2. **Parseable**: Easy to extract error information
3. **Debuggable**: Timestamp helps with log correlation
4. **User-friendly**: Clear error messages

**Logging Strategy**:
- `ERROR` level: Internal server errors, unexpected exceptions
- `WARN` level: Business rule violations, authentication failures
- `INFO` level: Successful operations, audit events
- `DEBUG` level: Detailed flow information

---

## Configuration Management

### Application Configuration

**File**: `application.yml`

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:authdb}
    username: ${DB_USER:whocloud}
    password: ${DB_PASSWORD:whocloud123}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: ${JWT_SECRET:default-secret-key}
  expiration: 86400000  # 24 hours

logging:
  level:
    com.whocloud.auth: DEBUG
    org.springframework.security: DEBUG
```

### Configuration Externalization

**Environment Variables**:
- `DB_HOST`: Database hostname
- `DB_PORT`: Database port
- `DB_NAME`: Database name
- `DB_USER`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: JWT signing secret

**Best Practices**:
1. **Defaults**: Provide sensible defaults for development
2. **Override**: Production values via environment variables
3. **Security**: Never commit secrets to version control
4. **Validation**: Fail fast on missing critical configs

---

## Scheduled Tasks

### Token Cleanup Scheduler

**File**: `scheduler/TokenCleanupScheduler.java`

**Purpose**: Remove expired refresh tokens from database

**Schedule**: Daily at 2:00 AM
```java
@Scheduled(cron = "0 0 2 * * ?")
public void cleanupExpiredTokens()
```

**Cron Expression Breakdown**:
```
 *    *    *    *    *    *
 │    │    │    │    │    │
 │    │    │    │    │    └─ Day of week (0-7) [? = any]
 │    │    │    │    └────── Month (1-12)
 │    │    │    └─────────── Day of month (1-31)
 │    │    └──────────────── Hour (0-23) [2 = 2 AM]
 │    └───────────────────── Minute (0-59) [0 = on the hour]
 └────────────────────────── Second (0-59) [0 = on the minute]
```

**Implementation**:
```java
@Modifying
@Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
void deleteExpiredTokens(LocalDateTime now);
```

**Benefits**:
- **Automatic**: No manual intervention needed
- **Off-peak**: Runs during low-traffic hours
- **Database Cleanup**: Prevents table bloat
- **Performance**: Indexed `expires_at` column

---

## Integration Points

### 1. API Gateway Integration

**Configuration Required**:

```yaml
# API Gateway application.yml
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
            - name: Retry
              args:
                retries: 3
```

**Header Forwarding**:
- `Authorization`: JWT token
- `Content-Type`: application/json
- `Accept`: application/json

### 2. Service-to-Service Authentication

**Validation Endpoint**: `/api/auth/validate`

**Usage by Other Services**:
```java
@Component
public class AuthClient {
    
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
}
```

### 3. Database Connection

**Connection Pool** (HikariCP):
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**Connection Lifecycle**:
1. Application starts → Pool initialized
2. Request arrives → Connection borrowed
3. Query executed → Transaction committed/rolled back
4. Connection returned → Pool available
5. Idle timeout → Connection closed
6. Application stops → Pool shutdown

---

## Design Patterns Used

### 1. Repository Pattern

**Purpose**: Abstract data access layer

**Implementation**: Spring Data JPA

**Benefits**:
- Decouples business logic from persistence
- Easy to test (mock repositories)
- Consistent data access interface

### 2. Service Layer Pattern

**Purpose**: Encapsulate business logic

**Implementation**: `@Service` classes

**Benefits**:
- Centralized business rules
- Reusable across controllers
- Transaction management

### 3. DTO Pattern (Data Transfer Object)

**Purpose**: Transfer data between layers

**Implementation**: Request/Response DTOs

**Benefits**:
- Decouples API from domain model
- Validation on boundaries
- API versioning support

### 4. Facade Pattern

**Purpose**: Simplify complex subsystem

**Implementation**: `AuthService` hides complexity

**Example**:
```java
// Simple interface
public AuthResponse register(RegisterRequest request)

// Hidden complexity:
// - Duplicate checking
// - Password encoding
// - User creation
// - Token generation
// - Refresh token creation
```

### 5. Strategy Pattern

**Purpose**: Encapsulate algorithms

**Implementation**: `PasswordEncoder`, `JwtUtil`

**Benefits**:
- Easy to swap implementations
- Testable independently
- Single responsibility

### 6. Template Method Pattern

**Purpose**: Define algorithm skeleton

**Implementation**: Spring Security filter chain

**Benefits**:
- Consistent security flow
- Extensible via custom filters
- Framework-managed lifecycle

---

## Scalability Considerations

### Horizontal Scaling

**Stateless Design**: ✅ Supported
- No server-side session storage
- JWT tokens contain all needed info
- Database is single source of truth

**Load Balancing**: Compatible
```
         ┌──────────────┐
         │ Load Balancer│
         └──────┬───────┘
                │
     ┌──────────┼──────────┐
     │          │          │
┌────▼───┐ ┌───▼────┐ ┌───▼────┐
│ Auth-1 │ │ Auth-2 │ │ Auth-3 │
└────┬───┘ └───┬────┘ └───┬────┘
     │         │          │
     └─────────┼──────────┘
               │
        ┌──────▼──────┐
        │  PostgreSQL │
        └─────────────┘
```

**Considerations**:
- Database connection pool per instance
- Shared PostgreSQL (bottleneck)
- JWT secret must be same across instances
- Refresh tokens in shared database

### Vertical Scaling

**Resource Requirements**:
- **CPU**: JWT signing/verification
- **Memory**: Connection pools, Spring context
- **Disk**: Minimal (stateless)

**Tuning Parameters**:
```yaml
# JVM options
-Xms512m
-Xmx1024m
-XX:+UseG1GC

# Connection pool
maximum-pool-size: 20
```

### Caching Strategy

**Cacheable Data**:
- User details (frequently accessed)
- JWT validation results (short TTL)

**Implementation** (TODO):
```java
@Cacheable(value = "users", key = "#username")
public User findByUsername(String username) {
    return userRepository.findByUsername(username)
            .orElseThrow(...);
}
```

**Cache Invalidation**:
- On password change
- On role change
- On user update

---

## Performance Optimization

### Database Optimization

**Indexes**: ✅ Implemented
- `users(username)` - Login lookups
- `users(email)` - Duplicate checks
- `refresh_tokens(token)` - Token validation
- `refresh_tokens(user_id)` - Revocation queries

**Query Optimization**:
```java
// Efficient: Single query with join
@EntityGraph(attributePaths = {"user"})
Optional<RefreshToken> findByToken(String token);

// Inefficient: N+1 queries
// fetch token, then fetch user separately
```

**Connection Pooling**: ✅ HikariCP
- Fast, reliable
- Proper connection lifecycle
- Leak detection

### Application Optimization

**Bean Caching**:
- Spring singleton beans (default)
- JwtUtil, repositories cached
- No per-request instantiation

**Lazy Initialization**: ✅ Used
```java
@ManyToOne(fetch = FetchType.LAZY)
private User user;
```

**Password Hashing**:
- BCrypt cost factor: 10 (balanced)
- Higher = more secure, slower
- Lower = faster, less secure

### Monitoring Recommendations

**Metrics to Track**:
1. **Request Latency**:
   - /login: Target < 500ms
   - /register: Target < 800ms
   - /validate: Target < 100ms

2. **Database Connections**:
   - Active connections
   - Wait time for connection
   - Connection errors

3. **Token Metrics**:
   - Tokens generated per hour
   - Token validation failures
   - Expired token rate

4. **Error Rates**:
   - Authentication failures
   - Validation errors
   - 5xx errors

**Tools**:
- Spring Boot Actuator (metrics endpoint)
- Prometheus + Grafana
- Database monitoring (pg_stat_statements)

---

## Summary

The auth-service is a well-architected microservice following industry best practices:

### ✅ **Strengths**

1. **Clean Architecture**: Clear separation of concerns
2. **Security**: BCrypt + JWT + Refresh tokens
3. **Scalability**: Stateless, horizontally scalable
4. **Maintainability**: Modular, testable code
5. **Documentation**: Comprehensive API docs
6. **Standards**: Spring Boot, JPA, REST conventions

### ⚠️ **Areas for Future Enhancement**

1. **Rate Limiting**: Prevent brute-force attacks
2. **Caching**: Reduce database load
3. **OAuth2**: Social login integration
4. **2FA**: Two-factor authentication
5. **Audit Logging**: Track all authentication events
6. **Token Rotation**: Refresh token rotation on use
7. **Account Lockout**: After N failed attempts
8. **Password Policy**: Complexity requirements

---

**Document Version**: 1.0  
**Last Updated**: January 1, 2026  
**Author**: WHO Cloud Team
