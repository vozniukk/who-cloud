# Google OAuth2 Setup Guide

Complete guide for configuring Google OAuth 2.0 authentication in WHO Cloud microservices.

## 🎯 Overview

WHO Cloud uses Google OAuth 2.0 for user authentication with automatic user registration. New users are automatically assigned the `GUEST` role and can be promoted to `USER`, `ADMIN`, or `MODERATOR` by administrators.

## 📋 Prerequisites

- Google account
- Access to [Google Cloud Console](https://console.cloud.google.com/)
- WHO Cloud running locally or accessible via public URL

## 🚀 Step-by-Step Configuration

### 1. Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click **Select a project** → **NEW PROJECT**
3. Enter project details:
   - **Project name**: `WHO Cloud` (or your preferred name)
   - **Organization**: (optional)
   - **Location**: (optional)
4. Click **CREATE**
5. Wait for project creation (10-20 seconds)
6. Select the newly created project from the dropdown

### 2. Enable Required APIs

1. In the Google Cloud Console, go to **APIs & Services > Library**
2. Search for and enable:
   - **Google+ API** (for profile information)
   - **Google OAuth2 API** (for authentication)

### 3. Configure OAuth Consent Screen

1. Navigate to **APIs & Services > OAuth consent screen**
2. Select **User Type**:
   - **External**: For any Google account (recommended for development)
   - **Internal**: Only for Google Workspace users in your organization
3. Click **CREATE**

4. **App information**:
   - **App name**: `WHO Cloud` (visible to users during login)
   - **User support email**: Your email address
   - **App logo**: (optional) Upload a logo (120x120px minimum)
   - **Application home page**: `http://localhost:8080` (or your domain)
   - **Application privacy policy link**: (optional but recommended)
   - **Application terms of service link**: (optional but recommended)

5. **Developer contact information**:
   - Enter your email address
   - Click **SAVE AND CONTINUE**

6. **Scopes**:
   - Click **ADD OR REMOVE SCOPES**
   - Select the following scopes:
     - `.../auth/userinfo.email` - See your email address
     - `.../auth/userinfo.profile` - See your personal info
     - `openid` - Associate you with your personal info
   - Click **UPDATE**
   - Click **SAVE AND CONTINUE**

7. **Test users** (for External apps):
   - Add email addresses of users who can test your app
   - Click **ADD USERS** → Enter email → Click **ADD**
   - Click **SAVE AND CONTINUE**

8. **Summary**:
   - Review your configuration
   - Click **BACK TO DASHBOARD**

### 4. Create OAuth 2.0 Credentials

1. Navigate to **APIs & Services > Credentials**
2. Click **CREATE CREDENTIALS** → **OAuth 2.0 Client ID**
3. If prompted, configure the consent screen first (see step 3)
4. **Application type**: Select **Web application**
5. **Name**: `WHO Cloud Auth Service` (or your preferred name)

6. **Authorized JavaScript origins** (optional):
   ```
   http://localhost:8080
   http://localhost:8081
   ```

7. **Authorized redirect URIs** (REQUIRED):
   ```
   http://localhost:8081/login/oauth2/code/google
   ```
   
   **Important**: 
   - The redirect URI must exactly match what's configured in your application
   - For production, use your actual domain (e.g., `https://yourdomain.com/login/oauth2/code/google`)
   - You can add multiple redirect URIs for different environments

8. Click **CREATE**

9. **Copy your credentials**:
   - **Client ID**: `329578610455-xxxxxxxxxxxxxxxxxx.apps.googleusercontent.com`
   - **Client Secret**: `GOCSPX-xxxxxxxxxxxxxxxxxxxxx`
   - Download JSON file (optional, for backup)
   - Click **OK**

### 5. Configure WHO Cloud Environment

Create or update `.env` file in your project root:

```env
# Google OAuth 2.0 Configuration
GOOGLE_CLIENT_ID=329578610455-xxxxxxxxxxxxxxxxxx.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-xxxxxxxxxxxxxxxxxxxxx

# JWT Secret (alphanumeric only, min 256 bits)
JWT_SECRET=n1DgZNcLCVixhobxWStmxXjI0LN1yRABYwUt2yNBtE-secure-jwt-secret-key-256bits

# Database Configuration
DB_HOST=postgres
DB_PORT=5432
DB_NAME=authdb
DB_USER=whocloud
DB_PASSWORD=whocloud123
```

**Important Notes**:
- Keep `.env` file secure and never commit to version control
- `JWT_SECRET` must be alphanumeric (no `$`, `{`, `}` characters)
- Use different credentials for production

### 6. Restart Services

```bash
# Stop current services
docker-compose down

# Rebuild and start with new environment variables
docker-compose up -d --build
```

### 7. Test OAuth2 Login

**Option 1: Browser Test**
1. Open: http://localhost:8081/login/oauth2/authorization/google
2. You'll be redirected to Google login page
3. Sign in with your Google account
4. Grant permissions to WHO Cloud app
5. You'll be redirected back with JWT tokens displayed

**Option 2: cURL Test**
```bash
# Initiate OAuth2 flow (will return redirect)
curl -L http://localhost:8081/login/oauth2/authorization/google
```

**Expected Success Response** (HTML page with):
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "your-username",
    "email": "your-email@gmail.com",
    "fullName": "Your Name",
    "role": "GUEST",
    "googleId": "113418038115658957172"
  }
}
```

### 8. Use JWT Token

Copy the `accessToken` from the response and use it in subsequent requests:

```bash
TOKEN="your-jwt-token-here"

# Test authenticated endpoint
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/information/a

# Expected response:
# {"success":true,"data":"Information from Service A","timestamp":"2026-01-02T..."}
```

## 🔐 User Roles & Permissions

### Role Hierarchy

| Role | Auto-Assigned | Promotion Required | Access Level |
|------|--------------|-------------------|--------------|
| GUEST | ✅ Yes (OAuth2) | → USER | Information services (read-only) |
| USER | ❌ No | → ADMIN | All business services |
| ADMIN | ❌ No | → MODERATOR | Full access + user management |
| MODERATOR | ❌ No | N/A | Content moderation |

### Access Control Matrix

| Endpoint | GUEST | USER | ADMIN | MODERATOR |
|----------|-------|------|-------|-----------|
| `/api/public/*` | ✅ | ✅ | ✅ | ✅ |
| `/api/information/*` | ✅ | ✅ | ✅ | ✅ |
| `/api/business/*` | ❌ | ✅ | ✅ | ✅ |
| `/api/admin/*` | ❌ | ❌ | ✅ | ⚠️ |
| `/api/users/*` | ❌ | ❌ | ✅ | ⚠️ |

**Legend**: ✅ Allowed | ❌ Denied | ⚠️ Limited

### Role Promotion

To promote a GUEST to USER (requires ADMIN access):

```bash
# TODO: Admin portal endpoint for role management
PATCH /api/admin/users/{userId}/role
{
  "role": "USER"
}
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Error 400: redirect_uri_mismatch

**Error Message**:
```
Error 400: redirect_uri_mismatch
Request details: redirect_uri=http://localhost:8081/login/oauth2/code/google
```

**Solution**:
1. Go to Google Cloud Console → Credentials
2. Edit your OAuth 2.0 Client ID
3. Add exactly: `http://localhost:8081/login/oauth2/code/google` to Authorized redirect URIs
4. Save changes (may take 5 minutes to propagate)

#### 2. Error 500: Internal Server Error

**Possible Causes**:
- Database constraint violation (check `users_role_check`)
- Missing transaction context
- Invalid JWT secret

**Solution**:
```bash
# Check auth-service logs
docker logs whocloud-auth --tail 100

# Verify database constraint
docker exec -it whocloud-postgres psql -U whocloud -d authdb \
  -c "SELECT conname, pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = 'users_role_check';"

# Should show: CHECK (role IN ('GUEST', 'USER', 'ADMIN', 'MODERATOR'))
```

#### 3. Blank Page After Google Login

**Possible Causes**:
- OAuth2 configuration not loaded
- Security filter chain issue
- Missing form login disable

**Solution**:
```bash
# Check environment variables are loaded
docker exec whocloud-auth env | grep GOOGLE

# Restart services
docker-compose restart auth-service
```

#### 4. JWT Secret with Special Characters

**Error**: Environment variable interpolation issues

**Solution**:
- Use only alphanumeric characters in `JWT_SECRET`
- Avoid: `$`, `{`, `}`, `(`, `)`, `!`, etc.
- Example: `n1DgZNcLCVixhobxWStmxXjI0LN1yRABYwUt2yNBtE-secure-jwt-secret-key-256bits`

#### 5. Database Connection Failed

**Solution**:
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check database exists
docker exec -it whocloud-postgres psql -U whocloud -l

# Verify authdb exists with users table
docker exec -it whocloud-postgres psql -U whocloud -d authdb -c "\dt"
```

### Debug Logging

Enable debug logging for OAuth2:

```yaml
# auth-service/src/main/resources/application.yml
logging:
  level:
    org.springframework.security.oauth2: TRACE
    org.springframework.security.web: DEBUG
    com.whocloud.auth: DEBUG
```

## 🔒 Security Best Practices

### Development Environment

✅ **Do**:
- Use test Google accounts
- Keep `.env` file in `.gitignore`
- Use localhost redirect URIs
- Enable debug logging

❌ **Don't**:
- Commit OAuth credentials to Git
- Share client secrets publicly
- Use production credentials

### Production Environment

✅ **Do**:
- Use HTTPS for all OAuth redirect URIs
- Rotate client secrets regularly
- Enable OAuth consent screen verification
- Use strong JWT secrets (256+ bits)
- Implement rate limiting
- Monitor authentication logs
- Use Kubernetes Secrets for credentials

❌ **Don't**:
- Expose client secrets in logs
- Use HTTP redirect URIs
- Share credentials across environments
- Use default/weak JWT secrets

### OAuth Consent Screen

For production apps:
1. Complete OAuth consent screen verification (required for >100 users)
2. Add privacy policy and terms of service
3. Limit OAuth scopes to minimum required
4. Display app logo and branding
5. Keep support email updated

## 📚 Additional Resources

- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [JWT.io - JWT Debugger](https://jwt.io/)
- [Google Cloud Console](https://console.cloud.google.com/)

## 🆘 Support

If you encounter issues:

1. **Check logs**: `docker logs whocloud-auth --tail 100`
2. **Verify configuration**: Review `.env` file and Google Console settings
3. **Test connectivity**: Ensure auth-service is accessible at port 8081
4. **Database check**: Verify users table and constraint exist
5. **Review documentation**: See [README.md](README.md) and [DOCKER.md](DOCKER.md)

## 📝 Configuration Checklist

- [ ] Google Cloud project created
- [ ] OAuth consent screen configured
- [ ] OAuth 2.0 Client ID created
- [ ] Redirect URI added: `http://localhost:8081/login/oauth2/code/google`
- [ ] Client ID and Secret copied
- [ ] `.env` file created with credentials
- [ ] `JWT_SECRET` is alphanumeric
- [ ] Services restarted with new configuration
- [ ] OAuth2 login tested successfully
- [ ] JWT token obtained
- [ ] Authenticated request tested
- [ ] User auto-registered with GUEST role
- [ ] Database constraint verified

---

**Last Updated**: January 2, 2026  
**Version**: 1.0.0  
**Status**: ✅ Production Ready
