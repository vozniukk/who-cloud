# Authentication & Authorization Flow

## Overview

This document defines the complete authentication and authorization architecture for the WHO Cloud platform, including Google OAuth2 integration and role-based access control.

---

## Access Control Matrix

| Service | Public Access | GUEST Role | USER/ADMIN Role |
|---------|---------------|------------|-----------------|
| **public-web-service** | ✅ Yes | ✅ Yes | ✅ Yes |
| **information-service-a** | ❌ No | ✅ Yes | ✅ Yes |
| **information-service-b** | ❌ No | ❌ No | ✅ Yes |
| **business-service-1** | ❌ No | ❌ No | ✅ Yes |
| **business-service-2** | ❌ No | ❌ No | ✅ Yes |
| **business-service-3** | ❌ No | ❌ No | ✅ Yes |
| **admin-portal-service** | ❌ No | ❌ No | ✅ Admin Only |
| **auth-service** | ⚠️ Endpoints vary | - | - |

---

## User Roles Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│                    Role Hierarchy                        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│   PUBLIC (No Authentication)                             │
│     └─ Access: public-web-service only                  │
│                                                          │
│   ↓ [Google OAuth Login]                                │
│                                                          │
│   GUEST (Authenticated, Auto-registered)                 │
│     ├─ Access: information-service-a                    │
│     ├─ Created: First Google OAuth login                │
│     └─ Upgrade: By administrator via admin-portal       │
│                                                          │
│   ↓ [Administrator promotes to USER]                    │
│                                                          │
│   USER (Full Application Access)                         │
│     ├─ Access: All information & business services      │
│     │   - information-service-a                         │
│     │   - information-service-b                         │
│     │   - business-service-1                            │
│     │   - business-service-2                            │
│     │   - business-service-3                            │
│     └─ Upgrade: By administrator to MODERATOR/ADMIN     │
│                                                          │
│   MODERATOR (User + Moderation Rights)                   │
│     └─ Access: USER permissions + content moderation    │
│                                                          │
│   ADMIN (Full Platform Access)                           │
│     └─ Access: All services + admin-portal-service      │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## Authentication Flow

### 1. Public Access (No Authentication)

```
┌────────┐                           ┌─────────────────┐
│ Client │                           │  API Gateway    │
└───┬────┘                           └────────┬────────┘
    │                                         │
    │ GET /api/public/**                      │
    ├────────────────────────────────────────>│
    │                                         │
    │                           ┌─────────────▼──────────┐
    │                           │ Route to                │
    │                           │ public-web-service      │
    │                           └─────────────┬──────────┘
    │                                         │
    │ 200 OK                                  │
    │<────────────────────────────────────────┤
    │                                         │
```

**Rules**:
- No authentication required
- No JWT token needed
- Direct access to public-web-service

---

### 2. Google OAuth2 Authentication (First Time User)

```
┌────────┐      ┌────────────┐      ┌──────────┐      ┌────────┐
│ Client │      │API Gateway │      │   Auth   │      │ Google │
│        │      │            │      │ Service  │      │  OAuth │
└───┬────┘      └─────┬──────┘      └────┬─────┘      └───┬────┘
    │                 │                   │                │
    │ Click "Login with Google"           │                │
    ├────────────────>│                   │                │
    │                 │                   │                │
    │                 │ GET /api/auth/oauth2/google        │
    │                 ├──────────────────>│                │
    │                 │                   │                │
    │                 │                   │ Redirect to    │
    │                 │                   │ Google OAuth   │
    │                 │<──────────────────┤                │
    │                 │                   │                │
    │ Redirect to Google OAuth            │                │
    │<────────────────┤                   │                │
    │                 │                   │                │
    │ User enters Google credentials      │                │
    ├─────────────────────────────────────────────────────>│
    │                 │                   │                │
    │                 │                   │  User approves │
    │                 │                   │  consent       │
    │<─────────────────────────────────────────────────────┤
    │                 │                   │                │
    │ GET /api/auth/oauth2/callback?code=xxx              │
    ├────────────────>│                   │                │
    │                 │                   │                │
    │                 │ Forward callback  │                │
    │                 ├──────────────────>│                │
    │                 │                   │                │
    │                 │                   │ Exchange code  │
    │                 │                   │ for tokens     │
    │                 │                   ├───────────────>│
    │                 │                   │                │
    │                 │                   │ Access token + │
    │                 │                   │ User profile   │
    │                 │                   │<───────────────┤
    │                 │                   │                │
    │                 │                   │ Check if user  │
    │                 │                   │ exists by email│
    │                 │                   ├─────┐          │
    │                 │                   │     │          │
    │                 │                   │ Not found      │
    │                 │                   │<────┘          │
    │                 │                   │                │
    │                 │                   │ Auto-register: │
    │                 │                   │ - username: email│
    │                 │                   │ - email: from Google│
    │                 │                   │ - fullName: from Google│
    │                 │                   │ - role: GUEST  │
    │                 │                   │ - googleId: sub│
    │                 │                   ├─────┐          │
    │                 │                   │     │          │
    │                 │                   │<────┘          │
    │                 │                   │                │
    │                 │                   │ Generate JWT   │
    │                 │                   │ (role=GUEST)   │
    │                 │                   ├─────┐          │
    │                 │                   │     │          │
    │                 │                   │<────┘          │
    │                 │                   │                │
    │                 │ 200 OK            │                │
    │                 │ {accessToken,     │                │
    │                 │  refreshToken,    │                │
    │                 │  user: {role:GUEST}}               │
    │<────────────────┴───────────────────┤                │
    │                                     │                │
```

**First-Time User Registration**:
1. User clicks "Login with Google"
2. Redirected to Google OAuth consent screen
3. User approves access
4. Google returns authorization code
5. Auth-service exchanges code for access token
6. Auth-service retrieves user profile from Google
7. **Auto-registration** if user doesn't exist:
   - Email from Google profile
   - Username = email
   - Full name from Google profile
   - **Role = GUEST** (automatic)
   - Google ID stored for future logins
8. Generate JWT with role=GUEST
9. Return tokens to client

---

### 3. Returning Google OAuth User

```
┌────────┐      ┌──────────┐      ┌────────┐
│ Client │      │   Auth   │      │ Google │
│        │      │ Service  │      │  OAuth │
└───┬────┘      └────┬─────┘      └───┬────┘
    │                │                │
    │ Login with Google               │
    ├───────────────>│                │
    │                │                │
    │                │ OAuth flow     │
    │                ├───────────────>│
    │                │                │
    │                │ User profile   │
    │                │<───────────────┤
    │                │                │
    │                │ Find user by   │
    │                │ googleId OR    │
    │                │ email          │
    │                ├─────┐          │
    │                │     │          │
    │                │ Found          │
    │                │ role: USER     │
    │                │<────┘          │
    │                │                │
    │                │ Generate JWT   │
    │                │ (role=USER)    │
    │                ├─────┐          │
    │                │     │          │
    │                │<────┘          │
    │                │                │
    │ 200 OK         │                │
    │ {tokens,       │                │
    │  user: {role:USER}}             │
    │<───────────────┤                │
    │                │                │
```

**Returning User**:
1. Same OAuth flow
2. User found in database (by Google ID or email)
3. JWT generated with **current role** (GUEST, USER, ADMIN, etc.)
4. No role change during login

---

### 4. Access to Protected Services

#### Scenario A: GUEST User Accessing information-service-a ✅

```
┌────────┐      ┌────────────┐      ┌────────────────┐
│ Client │      │API Gateway │      │ information-   │
│        │      │            │      │ service-a      │
└───┬────┘      └─────┬──────┘      └────────┬───────┘
    │                 │                      │
    │ GET /api/info-a/** + JWT (role=GUEST)  │
    ├────────────────>│                      │
    │                 │                      │
    │                 │ Validate JWT         │
    │                 ├─────┐                │
    │                 │     │                │
    │                 │ Valid, role=GUEST    │
    │                 │<────┘                │
    │                 │                      │
    │                 │ Check: GUEST allowed │
    │                 │ for /api/info-a/**   │
    │                 ├─────┐                │
    │                 │     │                │
    │                 │ ✅ Allowed           │
    │                 │<────┘                │
    │                 │                      │
    │                 │ Forward request      │
    │                 ├─────────────────────>│
    │                 │                      │
    │                 │ Response             │
    │                 │<─────────────────────┤
    │                 │                      │
    │ 200 OK          │                      │
    │<────────────────┤                      │
    │                 │                      │
```

#### Scenario B: GUEST User Accessing business-service-1 ❌

```
┌────────┐      ┌────────────┐
│ Client │      │API Gateway │
│        │      │            │
└───┬────┘      └─────┬──────┘
    │                 │
    │ GET /api/business-1/** + JWT (role=GUEST)
    ├────────────────>│
    │                 │
    │                 │ Validate JWT
    │                 ├─────┐
    │                 │     │
    │                 │ Valid, role=GUEST
    │                 │<────┘
    │                 │
    │                 │ Check: GUEST allowed?
    │                 │ for /api/business-1/**
    │                 ├─────┐
    │                 │     │
    │                 │ ❌ Forbidden
    │                 │ (requires USER/ADMIN)
    │                 │<────┘
    │                 │
    │ 403 Forbidden   │
    │ {error: "Insufficient permissions"}
    │<────────────────┤
    │                 │
```

#### Scenario C: USER Accessing business-service-2 ✅

```
┌────────┐      ┌────────────┐      ┌────────────────┐
│ Client │      │API Gateway │      │ business-      │
│        │      │            │      │ service-2      │
└───┬────┘      └─────┬──────┘      └────────┬───────┘
    │                 │                      │
    │ GET /api/business-2/** + JWT (role=USER)│
    ├────────────────>│                      │
    │                 │                      │
    │                 │ Validate JWT         │
    │                 ├─────┐                │
    │                 │     │                │
    │                 │ Valid, role=USER     │
    │                 │<────┘                │
    │                 │                      │
    │                 │ Check: USER allowed  │
    │                 │ for /api/business-2/**│
    │                 ├─────┐                │
    │                 │     │                │
    │                 │ ✅ Allowed           │
    │                 │<────┘                │
    │                 │                      │
    │                 │ Forward request      │
    │                 ├─────────────────────>│
    │                 │                      │
    │                 │ Response             │
    │                 │<─────────────────────┤
    │                 │                      │
    │ 200 OK          │                      │
    │<────────────────┤                      │
    │                 │                      │
```

---

## Role Upgrade Flow (Admin Portal)

### Administrator Promotes GUEST → USER

```
┌────────┐      ┌────────────┐      ┌────────────────┐
│ Admin  │      │API Gateway │      │ admin-portal-  │
│ Client │      │            │      │ service        │
└───┬────┘      └─────┬──────┘      └────────┬───────┘
    │                 │                      │
    │ POST /api/admin/users/{id}/promote     │
    │ Authorization: JWT (role=ADMIN)        │
    │ {newRole: "USER"}                      │
    ├────────────────>│                      │
    │                 │                      │
    │                 │ Validate JWT         │
    │                 ├─────┐                │
    │                 │     │                │
    │                 │ Valid, role=ADMIN    │
    │                 │<────┘                │
    │                 │                      │
    │                 │ Forward request      │
    │                 ├─────────────────────>│
    │                 │                      │
    │                 │           ┌──────────▼────────┐
    │                 │           │ Verify admin auth │
    │                 │           └──────────┬────────┘
    │                 │                      │
    │                 │           ┌──────────▼────────┐
    │                 │           │ Find user by ID   │
    │                 │           │ Current: role=GUEST│
    │                 │           └──────────┬────────┘
    │                 │                      │
    │                 │           ┌──────────▼────────┐
    │                 │           │ Update:           │
    │                 │           │ user.role = USER  │
    │                 │           │ Save to database  │
    │                 │           └──────────┬────────┘
    │                 │                      │
    │                 │           ┌──────────▼────────┐
    │                 │           │ Audit log:        │
    │                 │           │ "User promoted    │
    │                 │           │  from GUEST to    │
    │                 │           │  USER by admin"   │
    │                 │           └──────────┬────────┘
    │                 │                      │
    │                 │ 200 OK               │
    │                 │ {user: {role: USER}} │
    │                 │<─────────────────────┤
    │                 │                      │
    │ 200 OK          │                      │
    │ User promoted successfully             │
    │<────────────────┤                      │
    │                 │                      │
```

**Next Login**:
- User logs in with Google OAuth
- JWT now contains `role: USER`
- Can access information-service-b and business services

---

## API Gateway Route Configuration

### Route Rules

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Public - No authentication required
        - id: public-web-service
          uri: http://public-web-service:8082
          predicates:
            - Path=/api/public/**
          filters:
            - StripPrefix=2
        
        # Auth service - Mixed access
        - id: auth-service
          uri: http://auth-service:8081
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2
        
        # Information Service A - Requires authentication (GUEST+)
        - id: information-service-a
          uri: http://information-service-a:8083
          predicates:
            - Path=/api/info-a/**
          filters:
            - StripPrefix=2
            - name: JwtAuthenticationFilter
              args:
                requiredRoles: GUEST,USER,MODERATOR,ADMIN
        
        # Information Service B - Requires USER role or higher
        - id: information-service-b
          uri: http://information-service-b:8084
          predicates:
            - Path=/api/info-b/**
          filters:
            - StripPrefix=2
            - name: JwtAuthenticationFilter
              args:
                requiredRoles: USER,MODERATOR,ADMIN
        
        # Business Services - Require USER role or higher
        - id: business-service-1
          uri: http://business-service-1:8085
          predicates:
            - Path=/api/business-1/**
          filters:
            - StripPrefix=2
            - name: JwtAuthenticationFilter
              args:
                requiredRoles: USER,MODERATOR,ADMIN
        
        - id: business-service-2
          uri: http://business-service-2:8086
          predicates:
            - Path=/api/business-2/**
          filters:
            - StripPrefix=2
            - name: JwtAuthenticationFilter
              args:
                requiredRoles: USER,MODERATOR,ADMIN
        
        - id: business-service-3
          uri: http://business-service-3:8087
          predicates:
            - Path=/api/business-3/**
          filters:
            - StripPrefix=2
            - name: JwtAuthenticationFilter
              args:
                requiredRoles: USER,MODERATOR,ADMIN
        
        # Admin Portal - Requires ADMIN role only
        - id: admin-portal-service
          uri: http://admin-portal-service:8088
          predicates:
            - Path=/api/admin/**
          filters:
            - StripPrefix=2
            - name: JwtAuthenticationFilter
              args:
                requiredRoles: ADMIN
```

---

## Implementation Checklist

### Phase 1: Google OAuth2 Integration

- [ ] **Auth Service**:
  - [ ] Add GUEST role to Role enum
  - [ ] Add `googleId` field to User entity
  - [ ] Install spring-boot-starter-oauth2-client dependency
  - [ ] Configure Google OAuth2 credentials
  - [ ] Create OAuth2SuccessHandler for auto-registration
  - [ ] Create OAuth2UserService for user lookup/creation
  - [ ] Add endpoints:
    - [ ] `GET /api/auth/oauth2/google` - Initiate OAuth
    - [ ] `GET /api/auth/oauth2/callback` - Handle callback
  - [ ] Update SecurityConfig for OAuth2 login

- [ ] **Database Schema**:
  ```sql
  ALTER TABLE users ADD COLUMN google_id VARCHAR(255) UNIQUE;
  CREATE INDEX idx_users_google_id ON users(google_id);
  ```

### Phase 2: API Gateway JWT Filter

- [ ] **API Gateway**:
  - [ ] Create JwtAuthenticationFilter
  - [ ] Implement JWT validation logic
  - [ ] Implement role-based authorization
  - [ ] Add filter to routes (see configuration above)
  - [ ] Configure CORS for frontend

### Phase 3: Admin Portal User Management

- [ ] **Admin Portal Service**:
  - [ ] Create UserManagementController
  - [ ] Add endpoints:
    - [ ] `GET /admin/users` - List all users
    - [ ] `GET /admin/users/{id}` - Get user details
    - [ ] `PUT /admin/users/{id}/role` - Update user role
    - [ ] `GET /admin/users/pending` - List GUEST users
  - [ ] Add role change validation
  - [ ] Add audit logging for role changes

### Phase 4: Frontend Integration

- [ ] **Public Web Service** (Frontend):
  - [ ] Add "Login with Google" button
  - [ ] Handle OAuth redirect flow
  - [ ] Store JWT token in localStorage/cookies
  - [ ] Add JWT to all API requests (Authorization header)
  - [ ] Handle 401/403 errors (redirect to login)
  - [ ] Show different UI based on user role

- [ ] **Admin Portal Frontend**:
  - [ ] User management dashboard
  - [ ] Role assignment interface
  - [ ] Audit log viewer

---

## Security Considerations

### 1. Token Security

- **Access Token**: 24 hours expiration (stateless)
- **Refresh Token**: 30 days, stored in database (revocable)
- **Google OAuth Token**: Not stored (only used during authentication)

### 2. Session Management

- **Stateless**: No server-side sessions
- **JWT Contains**: username, email, role, expiration
- **Role Changes**: Require new login to get updated JWT

### 3. Google OAuth Security

- **Client Secret**: Stored in environment variables
- **Redirect URI**: Whitelist in Google Console
- **State Parameter**: CSRF protection
- **HTTPS Required**: Production environment

### 4. CORS Configuration

```yaml
cors:
  allowed-origins:
    - https://whocloud.com
    - http://localhost:3000  # Development only
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
  allowed-headers:
    - Authorization
    - Content-Type
  allow-credentials: true
```

---

## User Journey Examples

### Example 1: New User (First Visit)

1. ✅ User visits `https://whocloud.com` (public-web-service)
2. ✅ User clicks "Login with Google"
3. ✅ Redirected to Google OAuth consent screen
4. ✅ User approves access
5. ✅ **Auto-registered** with role=GUEST
6. ✅ JWT generated with role=GUEST
7. ✅ User can access information-service-a
8. ❌ User cannot access information-service-b (403 Forbidden)
9. ❌ User cannot access business services (403 Forbidden)

### Example 2: Administrator Promotes User

1. ✅ Admin logs in to admin-portal-service
2. ✅ Admin views list of GUEST users
3. ✅ Admin selects user and promotes to USER role
4. ✅ Database updated: role=GUEST → role=USER
5. ✅ Audit log created
6. ⏳ User logs out and logs in again (or waits for token expiration)
7. ✅ New JWT generated with role=USER
8. ✅ User can now access all information and business services

### Example 3: Returning USER

1. ✅ User visits `https://whocloud.com`
2. ✅ User clicks "Login with Google"
3. ✅ Google OAuth completes
4. ✅ User found in database with role=USER
5. ✅ JWT generated with role=USER
6. ✅ User can access all information and business services

---

## Database Schema Changes Required

### User Table (auth-service)

```sql
-- Add new column for Google ID
ALTER TABLE users ADD COLUMN google_id VARCHAR(255);
CREATE UNIQUE INDEX idx_users_google_id ON users(google_id);

-- Add GUEST role support (enum in Java, but stored as VARCHAR in PostgreSQL)
-- No schema change needed, just Java enum update
```

### User Entity Changes

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    // ... existing fields ...
    
    @Column(name = "google_id", unique = true)
    private String googleId;  // Google OAuth sub claim
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.GUEST;  // Default to GUEST instead of USER
    
    // ... rest of the class ...
}

public enum Role {
    GUEST,      // Auto-registered Google OAuth users
    USER,       // Promoted by administrator
    MODERATOR,  // Elevated privileges
    ADMIN       // Full access
}
```

---

## Testing Scenarios

### Test 1: Public Access

```bash
# No authentication required
curl http://localhost:8080/api/public/health
# Expected: 200 OK
```

### Test 2: Google OAuth Registration

```bash
# Browser-based (cannot test with curl)
# 1. Visit http://localhost:8080/api/auth/oauth2/google
# 2. Login with Google account
# 3. Should receive JWT with role=GUEST
# 4. Check database: SELECT * FROM users WHERE email='...'
# 5. Verify: role='GUEST', google_id is populated
```

### Test 3: GUEST Access to information-service-a

```bash
# With JWT token (role=GUEST)
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8080/api/info-a/data
# Expected: 200 OK
```

### Test 4: GUEST Denied Access to business-service-1

```bash
# With JWT token (role=GUEST)
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8080/api/business-1/data
# Expected: 403 Forbidden
```

### Test 5: Admin Promotes GUEST to USER

```bash
# With JWT token (role=ADMIN)
curl -X PUT \
     -H "Authorization: Bearer <ADMIN_JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"newRole": "USER"}' \
     http://localhost:8080/api/admin/users/1/role
# Expected: 200 OK, user role updated
```

### Test 6: USER Access to business-service-2

```bash
# User logs in again, gets new JWT with role=USER
curl -H "Authorization: Bearer <NEW_JWT_TOKEN>" \
     http://localhost:8080/api/business-2/data
# Expected: 200 OK
```

---

## Summary

### Access Rules

| User Type | Authentication | information-service-a | information-service-b | business-services | admin-portal |
|-----------|----------------|----------------------|----------------------|------------------|--------------|
| **Anonymous** | ❌ None | ❌ | ❌ | ❌ | ❌ |
| **GUEST** | ✅ Google OAuth | ✅ | ❌ | ❌ | ❌ |
| **USER** | ✅ Google OAuth | ✅ | ✅ | ✅ | ❌ |
| **MODERATOR** | ✅ Google OAuth | ✅ | ✅ | ✅ | ❌ |
| **ADMIN** | ✅ Google OAuth | ✅ | ✅ | ✅ | ✅ |

### Key Points

1. **Public Access**: Only public-web-service is accessible without authentication
2. **Google OAuth Required**: All authenticated access requires Google login
3. **Auto-Registration**: First-time Google users automatically get GUEST role
4. **Role-Based Access**: API Gateway enforces role-based routing
5. **Admin Promotion**: Only admins can upgrade users from GUEST to USER/MODERATOR/ADMIN
6. **JWT Contains Role**: All authorization decisions based on JWT role claim
7. **Logout & Re-login**: Required to get updated JWT after role change

---

**Document Version**: 1.0  
**Last Updated**: January 1, 2026  
**Status**: Architecture defined, implementation pending
