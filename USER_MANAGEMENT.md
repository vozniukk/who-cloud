# User Management System Documentation

## Overview

User Management система предоставляет администраторам полный контроль над пользователями приложения, включая управление ролями, статусами и удаление пользователей.

## Architecture

### Backend (auth-service)

#### DTOs

**1. UserManagementResponse**
```java
Location: com.whocloud.auth.dto.UserManagementResponse

Fields:
- Long id
- String username
- String email
- String fullName
- String googleId
- String role (GUEST|USER|MODERATOR|ADMIN)
- boolean isEnabled
- boolean isAccountNonLocked
- LocalDateTime createdAt
- LocalDateTime lastLogin
- String provider (Google|Local)

Factory Method:
- static UserManagementResponse fromUser(User user)
```

**2. UpdateUserRoleRequest**
```java
Location: com.whocloud.auth.dto.UpdateUserRoleRequest

Fields:
- Long userId (required)
- String role (required, pattern: GUEST|USER|MODERATOR|ADMIN)

Validation:
- @NotNull for userId
- @NotBlank and @Pattern for role
```

**3. UpdateUserStatusRequest**
```java
Location: com.whocloud.auth.dto.UpdateUserStatusRequest

Fields:
- Long userId (required)
- Boolean enabled (required)

Validation:
- @NotNull for both fields
```

#### Service Methods (AuthService)

**1. getAllUsers()**
```java
public List<UserManagementResponse> getAllUsers()

Description: Возвращает список всех пользователей в системе
Returns: List of UserManagementResponse
Security: Вызывается только из контроллера с @PreAuthorize("hasRole('ADMIN')")
```

**2. updateUserRole()**
```java
public UserManagementResponse updateUserRole(UpdateUserRoleRequest request)

Description: Изменяет роль пользователя
Parameters:
  - request: содержит userId и новую роль
Actions:
  - Находит пользователя по ID
  - Обновляет роль
  - Обновляет updatedAt timestamp
  - Сохраняет изменения
Returns: Обновлённый UserManagementResponse
Throws: UsernameNotFoundException если пользователь не найден
```

**3. updateUserStatus()**
```java
public UserManagementResponse updateUserStatus(UpdateUserStatusRequest request)

Description: Включает/отключает пользователя
Parameters:
  - request: содержит userId и enabled status
Actions:
  - Находит пользователя по ID
  - Обновляет isEnabled флаг
  - Если disabled: отзывает все refresh tokens пользователя
  - Обновляет updatedAt timestamp
  - Сохраняет изменения
Returns: Обновлённый UserManagementResponse
Security Feature: Автоматическая деактивация всех сессий при отключении
```

**4. deleteUser()**
```java
public void deleteUser(Long userId)

Description: Удаляет пользователя из системы
Parameters:
  - userId: ID пользователя для удаления
Actions:
  - Находит пользователя по ID
  - Отзывает все refresh tokens
  - Удаляет пользователя из БД
Returns: void
Throws: UsernameNotFoundException если пользователь не найден
Warning: Необратимая операция
```

#### REST Endpoints (AuthController)

**Base Path:** `/auth`
**Full Gateway Path:** `/api/auth` → (StripPrefix=1) → `/auth` (routed to auth-service)

**Important:** Controller uses `/auth` as base path because API Gateway strips `/api` prefix.

**1. GET /api/auth/admin/users** (via Gateway)
```
Description: Получить список всех пользователей
Method: GET
Security: @PreAuthorize("hasRole('ADMIN')")
Headers:
  - Authorization: Bearer <JWT_TOKEN>
  - Origin: http://localhost:3000 (for CORS)
Response: ApiResponse<List<UserManagementResponse>>
Status Codes:
  - 200 OK: Успешно получен список
  - 401 Unauthorized: Нет токена или токен невалидный
  - 403 Forbidden: Роль не ADMIN

Controller Path: /auth/admin/users (after StripPrefix)
Gateway Path: /api/auth/admin/users
Direct (testing): http://localhost:8081/auth/admin/users
```

**2. PUT /api/auth/admin/users/role** (via Gateway)
```
Description: Изменить роль пользователя
Method: PUT
Security: @PreAuthorize("hasRole('ADMIN')")
Headers:
  - Authorization: Bearer <JWT_TOKEN>
  - Content-Type: application/json
  - Origin: http://localhost:3000 (for CORS)
Body:
  {
    "userId": 123,
    "role": "USER"
  }
Response: ApiResponse<UserManagementResponse>
Status Codes:
  - 200 OK: Роль успешно обновлена
  - 400 Bad Request: Невалидные данные
  - 401 Unauthorized: Нет токена
  - 403 Forbidden: Роль не ADMIN
  - 404 Not Found: Пользователь не найден

Controller Path: /auth/admin/users/role
Gateway Path: /api/auth/admin/users/role
Direct (testing): http://localhost:8081/auth/admin/users/role
```

**3. PUT /api/auth/admin/users/status** (via Gateway)
```
Description: Включить/отключить пользователя
Method: PUT
Security: @PreAuthorize("hasRole('ADMIN')")
Headers:
  - Authorization: Bearer <JWT_TOKEN>
  - Content-Type: application/json
  - Origin: http://localhost:3000 (for CORS)
Body:
  {
    "userId": 123,
    "enabled": false
  }
Response: ApiResponse<UserManagementResponse>
Status Codes:
  - 200 OK: Статус успешно обновлён
  - 400 Bad Request: Невалидные данные
  - 401 Unauthorized: Нет токена
  - 403 Forbidden: Роль не ADMIN
  - 404 Not Found: Пользователь не найден
Side Effect: При enabled=false отзываются все refresh tokens

Controller Path: /auth/admin/users/status
Gateway Path: /api/auth/admin/users/status
Direct (testing): http://localhost:8081/auth/admin/users/status
```

**4. DELETE /api/auth/admin/users/{userId}** (via Gateway)
```
Description: Удалить пользователя
Method: DELETE
Security: @PreAuthorize("hasRole('ADMIN')")
Headers:
  - Authorization: Bearer <JWT_TOKEN>
  - Origin: http://localhost:3000 (for CORS)
Path Parameters:
  - userId: ID пользователя для удаления
Response: ApiResponse<String>
Status Codes:
  - 200 OK: Пользователь успешно удалён
  - 401 Unauthorized: Нет токена
  - 403 Forbidden: Роль не ADMIN
  - 404 Not Found: Пользователь не найден
Warning: Необратимая операция, требует подтверждения

Controller Path: /auth/admin/users/{userId}
Gateway Path: /api/auth/admin/users/{userId}
Direct (testing): http://localhost:8081/auth/admin/users/{userId}
```

### JWT Authentication Filter

**Location:** `com.whocloud.auth.security.JwtAuthenticationFilter`

**Purpose:** Обрабатывает Bearer JWT токены и устанавливает SecurityContext для Spring Security

**Process Flow:**
1. Перехватывает каждый HTTP запрос
2. Проверяет наличие `Authorization: Bearer <token>` заголовка
3. Если токен присутствует:
   - Извлекает и валидирует токен через JwtUtil
   - Извлекает username, userId и roles из JWT payload
   - Создаёт SimpleGrantedAuthority с префиксом "ROLE_" для каждой роли
   - Создаёт UsernamePasswordAuthenticationToken с authorities
   - Устанавливает authentication в SecurityContextHolder
   - Сохраняет userId в request.attribute для использования в контроллерах
4. Пропускает запрос дальше по цепочке фильтров

**Key Methods:**
```java
protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
) throws ServletException, IOException
```

**Integration:**
```java
// SecurityConfig.java
http.addFilterBefore(
    jwtAuthenticationFilter, 
    UsernamePasswordAuthenticationFilter.class
);
```

### JWT Utility Enhancements

**Location:** `com.whocloud.auth.util.JwtUtil`

**New Methods:**

**1. extractUserId(String token)**
```java
Description: Извлекает ID пользователя из JWT payload
Parameters: token - JWT токен
Returns: Long userId (или null если отсутствует)
Usage: Для идентификации пользователя без запроса к БД
```

**2. extractRoles(String token)**
```java
Description: Извлекает список ролей из JWT payload
Parameters: token - JWT токен
Returns: List<String> roles
Fallback: Если "roles" отсутствует, возвращает single "role" как список
Usage: Для создания GrantedAuthority в фильтре
```

**Updated Token Generation:**
```java
JWT Payload теперь содержит:
{
  "sub": "username",
  "role": "ADMIN",          // Одиночная роль (для совместимости)
  "roles": ["ADMIN"],       // Список ролей (для фильтра)
  "userId": 1,              // ID пользователя
  "iat": 1735937826,        // Issued at
  "exp": 1736024226         // Expiration (24 часа)
}
```

### Security Configuration

**Location:** `com.whocloud.auth.config.SecurityConfig`

**Key Configuration:**
```java
@EnableMethodSecurity  // Включает @PreAuthorize аннотации

SecurityFilterChain:
1. OAuth2 endpoints - permitAll
2. /api/auth/** - permitAll (регистрация, логин)
3. /actuator/** - permitAll (health checks)
4. anyRequest() - authenticated

Filter Order:
- OAuth2AuthorizationRequestRedirectFilter
- OAuth2LoginAuthenticationFilter
- JwtAuthenticationFilter (НОВЫЙ)
- ... остальные фильтры
- AuthorizationFilter (проверяет @PreAuthorize)
```

## Frontend

### User Management Page

**Location:** `/admin/users` (`app/admin/users/page.tsx`)

**Features:**

**1. User List Table**
- Columns:
  - ID
  - Username (с Google ID если есть)
  - Full Name
  - Email
  - Provider (Google/Local с иконками)
  - Role (dropdown для изменения)
  - Status (toggle button Enable/Disable)
  - Last Login (timestamp)
  - Actions (Delete button)

**2. Interactive Elements**
- **Role Dropdown:** Выбор из GUEST/USER/MODERATOR/ADMIN
  - Цветные бейджи (Gray/Blue/Purple/Red)
  - onChang trigger API call для немедленного обновления
  
- **Status Toggle:** Enable/Disable button
  - Зелёный (✅ Enabled) / Красный (❌ Disabled)
  - onClick trigger API call
  
- **Delete Button:** 🗑️ Delete
  - Показывает confirm dialog с предупреждением
  - Необратимая операция

**3. Statistics Card**
- Отображает общее количество пользователей
- Иконка 👥 с градиентным фоном

**4. Notifications**
- Success messages (зелёный, auto-hide через 3 секунды)
- Error messages (красный, auto-hide через 3 секунды)

**5. Refresh Button**
- Кнопка 🔄 Refresh
- Обновляет список пользователей по требованию

**6. Empty State**
- Показывается когда нет пользователей
- Иконка 👥 с сообщением "No Users Found"

### API Integration

**Fetch Users:**
```javascript
fetch('http://localhost:8080/api/auth/admin/users', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
```

**Update Role:**
```javascript
fetch('http://localhost:8080/api/auth/admin/users/role', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ userId, role })
})
```

**Update Status:**
```javascript
fetch('http://localhost:8080/api/auth/admin/users/status', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ userId, enabled })
})
```

**Delete User:**
```javascript
fetch(`http://localhost:8080/api/auth/admin/users/${userId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
```

### Admin Layout Integration

**Location:** `app/admin/layout.tsx`

**Navigation Link:**
```tsx
<Link href="/admin/users" className="...">
  <span className="text-lg">👥 User Management</span>
</Link>
```

**Security:**
- Layout проверяет роль ADMIN
- Редиректит не-админов на dashboard
- Использует Next.js middleware для route protection

## Authentication Flow for Admin Operations

```
1. User Login
   └─> OAuth2 / Manual Login
       └─> JWT Token generated with userId, username, role
           └─> Token stored in localStorage + cookie

2. Admin Page Access
   └─> Next.js middleware checks auth_token cookie
       └─> Validates role === 'ADMIN'
           └─> Allows access to /admin/*

3. API Request
   └─> Frontend sends: Authorization: Bearer <JWT>
       └─> API Gateway forwards to auth-service
           └─> JwtAuthenticationFilter intercepts
               └─> Validates token via JwtUtil
                   └─> Extracts userId, username, roles
                       └─> Creates Authentication with ROLE_ADMIN
                           └─> SecurityContext updated
                               └─> @PreAuthorize("hasRole('ADMIN')") checks
                                   └─> If ADMIN: Controller method executes
                                   └─> If not: 403 Forbidden

4. Service Layer
   └─> Performs business logic (e.g., updateUserRole)
       └─> Updates database
           └─> Returns response

5. Frontend Response
   └─> Success: Show notification, refresh list
   └─> Error: Show error message
```

## Security Considerations

### Authentication & Authorization

**Multi-Layer Security:**
1. **Frontend Middleware:** Prevents unauthorized route access
2. **JWT Token:** Must be valid and non-expired
3. **Spring Security Filter:** JwtAuthenticationFilter validates token
4. **Method Security:** @PreAuthorize("hasRole('ADMIN')") on each endpoint
5. **Service Layer:** Business logic validation

### Token Management

**Access Token (JWT):**
- Stored in: localStorage + HttpOnly cookie
- Lifetime: 24 hours
- Contains: userId, username, role, roles
- Validation: Signature check + expiration check

**Refresh Token:**
- Stored in: Database (auth-service)
- Lifetime: 7 days
- Revoked: On user disable or delete
- Purpose: Renew access tokens

### Data Protection

**User Operations:**
- **Role Change:** Logged with admin username, target userId, and new role
- **Status Change:** Logged + automatic token revocation on disable
- **Delete:** Logged + cascade delete of all user tokens

**Audit Trail:**
All admin operations logged with:
- Admin username (from JWT)
- Target user ID
- Operation type
- Timestamp
- Operation result

## Testing Guide

### Prerequisites

1. **Database:** PostgreSQL with users table
2. **Services Running:**
   - auth-service (port 8081)
   - api-gateway (port 8080)
   - frontend-service (port 3000)
3. **Admin Account:**
   ```sql
   -- Update user role to ADMIN
   UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';
   ```

### Manual Testing Steps

**1. Access User Management**
```
1. Login as ADMIN user
2. Navigate to Admin Panel
3. Click "👥 User Management"
4. Verify: User list loads successfully
```

**2. Test Role Change**
```
1. Find user with role GUEST
2. Click role dropdown
3. Select "USER"
4. Verify: Success message appears
5. Verify: Role badge updates immediately
6. Refresh page
7. Verify: Role persists
```

**3. Test User Disable**
```
1. Find enabled user
2. Click "✅ Enabled" button
3. Verify: Confirmation or immediate toggle
4. Verify: Button changes to "❌ Disabled"
5. Verify: Success message
6. Check logs: Refresh tokens should be revoked
```

**4. Test User Delete**
```
1. Find test user
2. Click "🗑️ Delete" button
3. Verify: Confirmation dialog appears
4. Click OK/Confirm
5. Verify: User removed from list
6. Verify: Success message
7. Check database: User should be deleted
```

**5. Test Error Handling**
```
1. Logout
2. Try to access /admin/users directly
3. Verify: Redirected to login
4. Login as non-ADMIN (USER/GUEST)
5. Try to access /admin/users
6. Verify: Redirected to dashboard with error
```

### API Testing with cURL

**Get All Users:**
```bash
curl -X GET http://localhost:8080/api/auth/admin/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Update Role:**
```bash
curl -X PUT http://localhost:8080/api/auth/admin/users/role \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "role": "MODERATOR"
  }'
```

**Update Status:**
```bash
curl -X PUT http://localhost:8080/api/auth/admin/users/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "enabled": false
  }'
```

**Delete User:**
```bash
curl -X DELETE http://localhost:8080/api/auth/admin/users/2 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Expected Responses

**Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User",
    "googleId": null,
    "role": "USER",
    "isEnabled": true,
    "isAccountNonLocked": true,
    "createdAt": "2026-01-03T10:00:00",
    "lastLogin": "2026-01-03T20:00:00",
    "provider": "Local"
  },
  "message": null,
  "timestamp": "2026-01-03T20:45:00"
}
```

**Error (403 Forbidden):**
```json
{
  "success": false,
  "data": null,
  "message": "Access Denied",
  "timestamp": "2026-01-03T20:45:00"
}
```

**Error (404 Not Found):**
```json
{
  "success": false,
  "data": null,
  "message": "User not found with id: 999",
  "timestamp": "2026-01-03T20:45:00"
}
```

## Troubleshooting

### Common Issues

**1. "Failed to fetch" Error**

**Symptoms:** Frontend shows "❌ Failed to fetch" message

**Causes:**
- auth-service не запущен
- JWT токен отсутствует или невалиден
- CORS проблемы

**Solutions:**
```bash
# Проверить статус auth-service
docker ps --filter "name=whocloud-auth"

# Проверить логи
docker logs whocloud-auth --tail 50

# Проверить JWT в localStorage
console.log(localStorage.getItem('auth_token'))

# Проверить что JwtAuthenticationFilter в цепочке
docker logs whocloud-auth 2>&1 | grep "JwtAuthenticationFilter"
```

**2. 403 Forbidden**

**Symptoms:** API возвращает 403 даже с валидным токеном

**Causes:**
- Роль в JWT не ADMIN
- @PreAuthorize не работает (не включён @EnableMethodSecurity)
- JWT токен не содержит правильный формат ролей

**Solutions:**
```sql
-- Проверить роль в БД
SELECT id, username, email, role FROM users WHERE email = 'your-email';

-- Обновить роль
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email';
```

```bash
# Перелогиниться чтобы получить новый JWT с обновлённой ролью
# Logout → Login → Проверить JWT payload
```

**3. JWT Not Parsed**

**Symptoms:** SecurityContext остаётся anonymous

**Causes:**
- JwtAuthenticationFilter не добавлен в SecurityFilterChain
- JwtUtil.validateToken() возвращает false
- Exception при парсинге токена

**Solutions:**
```java
// Проверить SecurityConfig
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

// Включить debug логи
logging.level.com.whocloud.auth.security=DEBUG
```

**4. Role Not Recognized**

**Symptoms:** @PreAuthorize("hasRole('ADMIN')") не срабатывает

**Cause:** GrantedAuthority не имеет префикса "ROLE_"

**Solution:**
```java
// JwtAuthenticationFilter должен добавлять префикс
List<SimpleGrantedAuthority> authorities = roles.stream()
    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
    .toList();
```

## Performance Considerations

### Database Queries

**getAllUsers():**
- Single query: `SELECT * FROM users`
- No pagination (потенциальная проблема для >1000 users)
- Recommendation: Добавить pagination в будущем

**updateUserRole/Status:**
- 2 queries: SELECT + UPDATE
- Wrapped in @Transactional
- Optimistic locking не используется

**deleteUser:**
- 2+ queries: SELECT + DELETE refresh_tokens + DELETE user
- Cascade delete через repository
- Wrapped in @Transactional

### Caching Strategy

**Current:** No caching
**Recommendation:** 
- Cache user list на 30 секунд
- Invalidate при любой модификации
- Use Spring Cache with @Cacheable

### Frontend Performance

**Current:** Full list refresh после каждой операции
**Optimization:**
- Оптимистичное обновление UI
- Debounce role dropdown changes
- Virtual scrolling для больших списков

## Future Enhancements

### Phase 1: Basic Improvements
- [ ] Pagination для списка пользователей
- [ ] Search/Filter по username, email, role
- [ ] Sort по колонкам
- [ ] Bulk operations (multi-select + bulk role update)

### Phase 2: Advanced Features
- [ ] User creation из Admin Panel
- [ ] Password reset functionality
- [ ] Email verification management
- [ ] Session management (view active sessions, force logout)

### Phase 3: Audit & Compliance
- [ ] Detailed audit logs page
- [ ] Export users to CSV/Excel
- [ ] User activity timeline
- [ ] Compliance reports (GDPR, etc.)

### Phase 4: Security Enhancements
- [ ] Two-factor authentication management
- [ ] IP whitelist/blacklist
- [ ] Rate limiting per user
- [ ] Account lockout policies

## Critical Fixes & Lessons Learned

### Issue 1: Path Mapping Mismatch (RESOLVED ✅)

**Problem:**
```
API Gateway StripPrefix=1 removes /api prefix
Controller had @RequestMapping("/api/auth")
Gateway sent: /auth/admin/users
Controller expected: /api/auth/admin/users
Result: 404 Not Found, empty response body (Content-Length: 0)
```

**Solution:**
```java
// Before (INCORRECT):
@RequestMapping("/api/auth")
public class AuthController {
    @GetMapping("/admin/users")  // Expected: /api/auth/admin/users
}

// After (CORRECT):
@RequestMapping("/auth")
public class AuthController {
    @GetMapping("/admin/users")  // Expected: /auth/admin/users ✅
}
```

**Lesson:** When using StripPrefix in API Gateway, backend controllers must expect paths WITHOUT the stripped prefix.

---

### Issue 2: Authentication Principal Type Mismatch (RESOLVED ✅)

**Problem:**
```
JwtAuthenticationFilter creates UsernamePasswordAuthenticationToken with String principal
Controller used @AuthenticationPrincipal UserDetails
Result: NullPointerException - "Cannot invoke UserDetails.getUsername() because userDetails is null"
```

**Solution:**
```java
// Before (INCORRECT):
@GetMapping("/admin/users")
public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
    log.info("Admin requesting all users: {}", userDetails.getUsername()); // NPE!
}

// After (CORRECT):
@GetMapping("/admin/users")
public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal String username) {
    log.info("Admin requesting all users: {}", username); // ✅
}
```

**Lesson:** @AuthenticationPrincipal type must match the principal type used in UsernamePasswordAuthenticationToken.

---

### Issue 3: Duplicate CORS Headers (RESOLVED ✅)

**Problem:**
```
Browser error: "The 'Access-Control-Allow-Origin' header contains multiple values 
'http://localhost:3000, http://localhost:3000', but only one is allowed"

Backend returned 200 OK with correct data, but browser blocked the response due to CORS violation.
```

**Root Cause:**
Both `auth-service` (CorsConfig.java) and `api-gateway` (application.yml) were adding CORS headers:
- auth-service: Added "Access-Control-Allow-Origin: http://localhost:3000"
- api-gateway: Added "Access-Control-Allow-Origin: http://localhost:3000"
- Result: Duplicate header → Browser blocked response

**Solution:**
```java
// auth-service/src/main/java/com/whocloud/auth/config/CorsConfig.java

/**
 * CORS Configuration - DISABLED
 * 
 * CORS is handled at the API Gateway level (api-gateway/src/main/resources/application.yml)
 * Having CORS configuration in both places causes duplicate headers.
 * 
 * If you need to test auth-service directly (bypassing the gateway), 
 * temporarily uncomment this configuration.
 */
//@Configuration  // ← COMMENTED OUT
public class CorsConfig {
    //@Bean  // ← COMMENTED OUT
    public CorsConfigurationSource corsConfigurationSource() {
        // Configuration kept for reference
    }
}
```

**API Gateway CORS Configuration (ACTIVE):**
```yaml
# api-gateway/src/main/resources/application.yml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:3000"
              - "http://localhost:8080"
              - "http://localhost:4200"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders:
              - "*"
            allowCredentials: true
```

**Lesson:** In microservices architecture with API Gateway, configure CORS only once at the gateway level, not in individual services.

---

### Issue 4: SecurityConfig Too Permissive (RESOLVED ✅)

**Problem:**
```java
// Before (INCORRECT):
.requestMatchers("/api/auth/**").permitAll()  // ← ALL admin endpoints were public!
```

This allowed anyone to access `/api/auth/admin/users` without authentication.

**Solution:**
```java
// After (CORRECT):
.requestMatchers(
    "/",
    "/api/auth/register",
    "/api/auth/login",
    "/api/auth/refresh",
    "/api/auth/validate",
    "/oauth2/**",
    "/login/**",
    "/actuator/**",
    "/error"
).permitAll()
.requestMatchers("/api/auth/admin/**").hasRole("ADMIN")  // ← Explicit admin protection
.anyRequest().authenticated()
```

**Lesson:** Be specific with permitAll() rules. Use granular permissions instead of wildcards.

---

### Issue 5: JWT Token Signature Mismatch (RESOLVED ✅)

**Problem:**
After rebuilding Docker containers, old JWT tokens became invalid:
```
JWT signature does not match locally computed signature
```

**Root Cause:**
- JWT secret key was not persisted
- Docker container rebuild generated new ephemeral secret
- Old tokens signed with old secret couldn't be validated with new secret

**Solution:**
1. Set JWT_SECRET environment variable in docker-compose.yml
2. Or configure persistent secret in application.yml
3. User must obtain fresh token after container rebuild

**Lesson:** In production, JWT secret MUST be persistent and stored securely (env vars, secrets manager, etc.)

---

## Related Documentation

- [FRONTEND_ARCHITECTURE.md](./FRONTEND_ARCHITECTURE.md) - Frontend authentication architecture
- [AUTH_IMPLEMENTATION.md](./AUTH_IMPLEMENTATION.md) - Backend auth implementation
- [OAUTH2_IMPLEMENTATION_SUMMARY.md](./OAUTH2_IMPLEMENTATION_SUMMARY.md) - OAuth2 flow details
- [TESTING_GUIDE.md](./TESTING_GUIDE.md) - General testing procedures

## Support

For issues or questions:
1. Check logs: `docker logs whocloud-auth --tail 100`
2. Verify JWT token in browser DevTools → Application → Local Storage
3. Test endpoints with cURL to isolate frontend/backend issues
4. Check Spring Security filter chain in startup logs
5. Verify CORS headers: Add `-v` to curl or check browser Network tab
6. Check path mappings: Ensure controller paths match gateway routes

## Debugging Checklist

### Backend Not Responding (Empty Body)
- [ ] Check controller @RequestMapping matches gateway path after StripPrefix
- [ ] Verify logs show "Admin requesting all users: {username}"
- [ ] Test directly to auth-service: `curl http://localhost:8081/auth/admin/users`
- [ ] Test through gateway: `curl http://localhost:8080/api/auth/admin/users`

### Authentication Errors
- [ ] Verify token in localStorage is not expired (check exp claim)
- [ ] Ensure token signature matches current JWT_SECRET
- [ ] Check @AuthenticationPrincipal type matches filter's principal type
- [ ] Verify SecurityConfig has correct @PreAuthorize rules

### CORS Errors
- [ ] Check browser console for "Access-Control-Allow-Origin" errors
- [ ] Verify only ONE service has CORS configuration (should be gateway)
- [ ] Test with curl + Origin header: `-H "Origin: http://localhost:3000"`
- [ ] Check response headers for duplicate CORS values

### Authorization Errors (403 Forbidden)
- [ ] Verify user has ADMIN role (check JWT payload roles array)
- [ ] Ensure JwtAuthenticationFilter adds "ROLE_" prefix to authorities
- [ ] Check SecurityConfig uses `.hasRole("ADMIN")` not `.hasAuthority("ADMIN")`
- [ ] Verify @PreAuthorize annotation is on controller method

---

**Last Updated:** January 3, 2026  
**Version:** 1.1.0  
**Author:** WHO Cloud Development Team  
**Status:** ✅ All Issues Resolved - Production Ready
