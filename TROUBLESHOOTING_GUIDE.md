# Troubleshooting Guide - WHO Cloud Authentication

## Table of Contents
1. [Path Mapping Issues](#path-mapping-issues)
2. [CORS Errors](#cors-errors)
3. [Authentication Errors](#authentication-errors)
4. [Authorization Errors](#authorization-errors)
5. [Empty Response Body](#empty-response-body)
6. [JWT Token Issues](#jwt-token-issues)
7. [Quick Diagnostic Commands](#quick-diagnostic-commands)

---

## Path Mapping Issues

### Symptom
- Backend returns empty response (Content-Length: 0)
- Logs don't show controller execution
- 404 Not Found errors

### Root Cause
API Gateway `StripPrefix` removes path segments before routing to services. Controllers must expect paths WITHOUT the stripped prefix.

### Solution

**API Gateway Configuration:**
```yaml
- id: auth-service
  uri: http://localhost:8081
  predicates:
    - Path=/api/auth/**
  filters:
    - StripPrefix=1  # Removes first segment (/api)
```

**Controller Configuration:**
```java
// CORRECT ✅
@RestController
@RequestMapping("/auth")  // NOT "/api/auth"
public class AuthController {
    @GetMapping("/admin/users")  // Receives: /auth/admin/users
}

// WRONG ❌
@RestController
@RequestMapping("/api/auth")  // Won't match after StripPrefix!
```

**Request Flow:**
```
Frontend → http://localhost:8080/api/auth/admin/users
Gateway  → StripPrefix removes /api
Backend  → http://localhost:8081/auth/admin/users ✅
```

### Diagnostic Commands
```powershell
# Test direct to service (bypass gateway)
curl http://localhost:8081/auth/admin/users -H "Authorization: Bearer $token"

# Test through gateway
curl http://localhost:8080/api/auth/admin/users -H "Authorization: Bearer $token"

# Check controller logs
docker logs whocloud-auth --tail 20 | Select-String -Pattern "Admin requesting"
```

---

## CORS Errors

### Symptom
```
Browser Error: The 'Access-Control-Allow-Origin' header contains multiple values 
'http://localhost:3000, http://localhost:3000', but only one is allowed.

Status: 200 OK (backend succeeds)
Result: Browser blocks response
```

### Root Cause
Both API Gateway and individual services adding CORS headers, causing duplicates.

### Solution

**Rule:** Configure CORS only at API Gateway level.

**1. API Gateway (ACTIVE) ✅**
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

**2. Individual Services (DISABLED) ✅**
```java
// auth-service/src/main/java/com/whocloud/auth/config/CorsConfig.java

/**
 * CORS Configuration - DISABLED
 * CORS is handled at API Gateway level
 */
//@Configuration  // ← COMMENTED OUT
public class CorsConfig {
    //@Bean
    public CorsConfigurationSource corsConfigurationSource() { }
}
```

### Diagnostic Commands
```powershell
# Check CORS headers (should see only ONE value)
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/admin/users" `
  -Headers @{"Authorization"="Bearer $token"; "Origin"="http://localhost:3000"} `
  -Method GET -UseBasicParsing

$response.Headers['Access-Control-Allow-Origin']
# Should output: http://localhost:3000 (single value)
```

---

## Authentication Errors

### Symptom
- 401 Unauthorized
- "JWT token validation failed"
- "Cannot invoke UserDetails.getUsername() because userDetails is null"

### Causes & Solutions

#### 1. Token Expired
```powershell
# Check token expiration
$token = "eyJhbGc..."
$payload = [System.Text.Encoding]::UTF8.GetString(
    [System.Convert]::FromBase64String(
        ($token.Split('.')[1] -replace '-','+' -replace '_','/')
    )
) | ConvertFrom-Json
$exp = [DateTimeOffset]::FromUnixTimeSeconds($payload.exp).LocalDateTime
Write-Host "Token expires: $exp"
Write-Host "Current time: $(Get-Date)"
```

**Solution:** Get fresh token from `/dashboard/profile` or login again.

#### 2. JWT Signature Mismatch
```
Error: JWT signature does not match locally computed signature
```

**Cause:** Container restarted with different JWT_SECRET.

**Solution:** 
```yaml
# docker-compose.yml - Set persistent JWT_SECRET
services:
  auth-service:
    environment:
      - JWT_SECRET=your-persistent-secret-key-here
```

#### 3. Authentication Principal Type Mismatch
```java
// WRONG ❌ - Causes NullPointerException
@GetMapping("/admin/users")
public ResponseEntity<?> getAllUsers(
    @AuthenticationPrincipal UserDetails userDetails  // NPE!
) { }

// CORRECT ✅
@GetMapping("/admin/users")
public ResponseEntity<?> getAllUsers(
    @AuthenticationPrincipal String username  // Works!
) { }
```

**Reason:** JwtAuthenticationFilter creates `UsernamePasswordAuthenticationToken` with String principal, not UserDetails.

---

## Authorization Errors

### Symptom
- 403 Forbidden
- User is authenticated but can't access admin endpoints

### Causes & Solutions

#### 1. Missing ROLE_ Prefix
```java
// WRONG ❌
List<SimpleGrantedAuthority> authorities = roles.stream()
    .map(SimpleGrantedAuthority::new)  // Creates "ADMIN"
    .toList();

// CORRECT ✅
List<SimpleGrantedAuthority> authorities = roles.stream()
    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))  // Creates "ROLE_ADMIN"
    .toList();
```

#### 2. SecurityConfig Too Permissive
```java
// WRONG ❌ - Allows everyone to access admin endpoints
.requestMatchers("/api/auth/**").permitAll()

// CORRECT ✅ - Granular permissions
.requestMatchers(
    "/api/auth/register",
    "/api/auth/login",
    "/api/auth/refresh"
).permitAll()
.requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
```

#### 3. Wrong @PreAuthorize Expression
```java
// WRONG ❌
@PreAuthorize("hasAuthority('ADMIN')")  // Expects "ADMIN"

// CORRECT ✅
@PreAuthorize("hasRole('ADMIN')")  // Expects "ROLE_ADMIN"
```

### Diagnostic Commands
```powershell
# Decode JWT token and check roles
$token = "eyJhbGc..."
$payload = [System.Text.Encoding]::UTF8.GetString(
    [System.Convert]::FromBase64String(
        ($token.Split('.')[1] -replace '-','+' -replace '_','/')
    )
) | ConvertFrom-Json
Write-Host "User: $($payload.sub)"
Write-Host "Roles: $($payload.roles)"
Write-Host "UserId: $($payload.userId)"
```

---

## Empty Response Body

### Symptom
- Status: 200 OK
- Content-Length: 0
- No data in response

### Checklist
1. ✅ Check path mapping (see [Path Mapping Issues](#path-mapping-issues))
2. ✅ Check controller logs for execution
3. ✅ Check CORS headers (see [CORS Errors](#cors-errors))
4. ✅ Verify method returns data

### Diagnostic Commands
```powershell
# Check if controller executed
docker logs whocloud-auth --tail 50 | Select-String -Pattern "Admin requesting|getAllUsers"

# Test endpoint with verbose output
curl -X GET "http://localhost:8080/api/auth/admin/users" `
  -H "Authorization: Bearer $token" `
  -H "Origin: http://localhost:3000" `
  -v

# Check response size
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/admin/users" `
  -Headers @{"Authorization"="Bearer $token"} -UseBasicParsing
Write-Host "Content-Length: $($response.RawContentLength)"
Write-Host "Status: $($response.StatusCode)"
```

---

## JWT Token Issues

### Issue 1: Token Not Sent from Frontend
```javascript
// Check localStorage in browser console (F12)
console.log(localStorage.getItem('token'));

// Update token if stale
localStorage.setItem('token', 'eyJhbGc...');
```

### Issue 2: Token in Wrong Format
```javascript
// WRONG ❌
Authorization: "eyJhbGc..."

// CORRECT ✅
Authorization: "Bearer eyJhbGc..."
```

### Issue 3: Token Stored Incorrectly
```typescript
// Check lib/auth.ts
export function saveToken(token: string) {
  localStorage.setItem('token', token);  // Should NOT include "Bearer "
}

export function getToken(): string | null {
  return localStorage.getItem('token');
}

// In API call
const token = getToken();
const response = await fetch(url, {
  headers: {
    'Authorization': `Bearer ${token}`  // Add "Bearer " here
  }
});
```

---

## Quick Diagnostic Commands

### Check Service Status
```powershell
# All containers
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Specific service
docker ps --filter "name=whocloud-auth" --format "{{.Status}}"
```

### Check Logs
```powershell
# Last 50 lines
docker logs whocloud-auth --tail 50

# Follow logs in real-time
docker logs whocloud-auth -f

# Search for errors
docker logs whocloud-auth 2>&1 | Select-String -Pattern "ERROR|Exception|Failed"

# Search for specific endpoint
docker logs whocloud-auth 2>&1 | Select-String -Pattern "admin/users"

# Check authentication success
docker logs whocloud-auth 2>&1 | Select-String -Pattern "JWT Authentication successful"
```

### Test Endpoints
```powershell
# Set token variable
$token = "eyJhbGc..."

# Test with PowerShell
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/admin/users" `
  -Headers @{"Authorization"="Bearer $token"; "Origin"="http://localhost:3000"} `
  -Method GET -UseBasicParsing

# View response
$response.Content | ConvertFrom-Json | Format-Table -AutoSize

# Check CORS headers
$response.Headers['Access-Control-Allow-Origin']
$response.Headers['Access-Control-Allow-Credentials']
```

### Decode JWT Token
```powershell
function Decode-JwtToken {
    param([string]$token)
    
    $parts = $token.Split('.')
    $payload = [System.Text.Encoding]::UTF8.GetString(
        [System.Convert]::FromBase64String(
            ($parts[1] -replace '-','+' -replace '_','/')
        )
    ) | ConvertFrom-Json
    
    $exp = [DateTimeOffset]::FromUnixTimeSeconds($payload.exp).LocalDateTime
    $iat = [DateTimeOffset]::FromUnixTimeSeconds($payload.iat).LocalDateTime
    
    Write-Host "=== JWT Token Details ==="
    Write-Host "User: $($payload.sub)"
    Write-Host "UserId: $($payload.userId)"
    Write-Host "Role: $($payload.role)"
    Write-Host "Roles: $($payload.roles -join ', ')"
    Write-Host "Issued: $iat"
    Write-Host "Expires: $exp"
    Write-Host "Current: $(Get-Date)"
    Write-Host "Valid: $($exp -gt (Get-Date))"
}

# Usage
Decode-JwtToken -token "eyJhbGc..."
```

### Rebuild and Restart Service
```powershell
# Quick restart
docker-compose restart auth-service

# Full rebuild (after code changes)
.\gradlew :auth-service:build -x test
docker-compose build auth-service
docker-compose up -d auth-service

# Wait and verify
Start-Sleep -Seconds 30
docker logs whocloud-auth --tail 5 | Select-String -Pattern "Started|Tomcat started"
```

---

## Common Error Patterns

### Pattern 1: "Failed to fetch"
**Causes:**
1. CORS error (check browser console)
2. Network error (service down)
3. Wrong URL (typo in endpoint)

**Solution:** Check browser DevTools → Network tab for actual error.

### Pattern 2: Empty Response with 200 OK
**Causes:**
1. Path mapping mismatch
2. Controller not executing
3. CORS blocking response

**Solution:** Check logs for controller execution, test with curl.

### Pattern 3: 401 Unauthorized
**Causes:**
1. Token expired
2. Token signature mismatch
3. Token not sent
4. Token format wrong

**Solution:** Decode token, check expiration, verify Bearer prefix.

### Pattern 4: 403 Forbidden
**Causes:**
1. Missing ROLE_ prefix in authorities
2. Wrong @PreAuthorize expression
3. User doesn't have required role

**Solution:** Check JWT payload roles, verify filter adds ROLE_ prefix.

---

## Prevention Checklist

### Before Deploying Changes
- [ ] SecurityConfig uses specific permitAll() paths (not wildcards)
- [ ] CORS enabled only in API Gateway (disabled in services)
- [ ] Controller @RequestMapping matches gateway path after StripPrefix
- [ ] @AuthenticationPrincipal type matches filter's principal type
- [ ] JwtAuthenticationFilter adds "ROLE_" prefix to authorities
- [ ] @PreAuthorize uses hasRole() not hasAuthority()
- [ ] JWT_SECRET is set in docker-compose.yml
- [ ] Test endpoints with curl before testing in browser

### After Deployment
- [ ] Check logs for startup errors
- [ ] Verify service health: `docker ps`
- [ ] Test authentication flow end-to-end
- [ ] Check browser console for errors
- [ ] Verify CORS headers (should be single value)
- [ ] Test admin endpoints with ADMIN token
- [ ] Decode JWT token to verify claims

---

## Contact & Support

**Documentation:**
- [USER_MANAGEMENT.md](./USER_MANAGEMENT.md) - User management details
- [AUTH_IMPLEMENTATION.md](./AUTH_IMPLEMENTATION.md) - Authentication details
- [TESTING_GUIDE.md](./TESTING_GUIDE.md) - Testing procedures

**Logs Location:**
- Auth Service: `docker logs whocloud-auth`
- API Gateway: `docker logs whocloud-gateway`
- Frontend: Browser DevTools → Console

**Useful Commands Script:**
See `test-auth.ps1` for automated testing commands.

---

**Last Updated:** January 3, 2026  
**Version:** 1.0.0  
**Status:** ✅ Production Ready
