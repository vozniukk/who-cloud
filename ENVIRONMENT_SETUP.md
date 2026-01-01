# Environment Setup Guide

## 📋 Quick Start

### 1. Configure Environment Variables

The `.env` file has been created with your Google Client ID. **You need to add your Google Client Secret:**

```bash
# Edit .env file and replace this line:
GOOGLE_CLIENT_SECRET=PASTE_YOUR_GOOGLE_CLIENT_SECRET_HERE
```

### 2. Where to Find Each Value

| Variable | Source | Instructions |
|----------|--------|--------------|
| `GOOGLE_CLIENT_ID` | ✅ Already set | From Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | ❌ **REQUIRED** | Get from [Google Cloud Console](https://console.cloud.google.com/apis/credentials) → Your OAuth 2.0 Client → Client Secret |
| `JWT_SECRET` | ✅ Already generated | Random string for signing JWT tokens (already set in `.env`) |
| `DB_*` | ✅ Already set | Database configuration for Docker |

### 3. Get Google Client Secret

1. Go to: https://console.cloud.google.com/apis/credentials
2. Find your OAuth 2.0 Client ID: `329578610455-qfq28s2ud0lsesrhfrqmvk4odfvp6fi9.apps.googleusercontent.com`
3. Click on it to view details
4. Copy the **Client Secret** value
5. Paste it into `.env` file replacing `PASTE_YOUR_GOOGLE_CLIENT_SECRET_HERE`

### 4. Start the Application

Once `.env` is configured:

```bash
# Rebuild and start all services
docker-compose up -d --build

# Check logs
docker-compose logs -f auth-service
docker-compose logs -f api-gateway
```

### 5. Test OAuth2 Flow

1. Open browser: http://localhost:8080/login/oauth2/authorization/google
2. Login with your Google account
3. You should be redirected with a JWT token
4. User will be auto-registered with **GUEST** role

### 6. Verify Environment Variables in Containers

```bash
# Check auth-service environment
docker exec whocloud-auth env | grep -E "GOOGLE|JWT"

# Check api-gateway environment
docker exec whocloud-gateway env | grep JWT
```

## 🔒 Security Notes

- ✅ `.env` is already in `.gitignore` - **never commit it to git**
- ✅ `.env.example` is safe to commit (no real secrets)
- 🔄 Rotate `JWT_SECRET` periodically in production
- 🔐 Keep `GOOGLE_CLIENT_SECRET` secure

## ❓ Troubleshooting

### Issue: "Invalid OAuth2 credentials"
- Verify `GOOGLE_CLIENT_SECRET` is correctly pasted in `.env`
- Ensure no extra spaces or newlines

### Issue: "JWT signature does not match"
- Ensure `JWT_SECRET` is identical in both auth-service and api-gateway
- Docker Compose automatically loads from `.env` file

### Issue: Environment variables not loaded
```bash
# Recreate containers with new environment
docker-compose down
docker-compose up -d --build
```
