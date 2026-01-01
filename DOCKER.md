# Docker Deployment Guide

Complete guide for deploying WHO Cloud microservices with Docker and Docker Compose.

## 📋 Prerequisites

- **Docker**: 20.10 or higher ([Install Docker](https://docs.docker.com/get-docker/))
- **Docker Compose**: 2.0 or higher (included with Docker Desktop)
- **System Requirements**: 
  - At least 4GB RAM available for Docker
  - 10GB disk space for images and volumes
- **Java 21 LTS**: For building JARs locally ([Download](https://adoptium.net/))

## 🚀 Quick Start

### Step 1: Configure Environment Variables

Create `.env` file in project root:

```env
# Database Configuration
DB_HOST=postgres
DB_PORT=5432
DB_NAME=authdb
DB_USER=whocloud
DB_PASSWORD=whocloud123

# JWT Configuration (REQUIRED - alphanumeric only)
JWT_SECRET=n1DgZNcLCVixhobxWStmxXjI0LN1yRABYwUt2yNBtE-secure-jwt-secret-key-256bits

# Google OAuth 2.0 (REQUIRED for OAuth2 login)
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

**Important Notes**:
- `JWT_SECRET` must be alphanumeric (avoid `$`, `{`, `}` as Docker Compose interprets them)
- Get Google OAuth credentials from [Google Cloud Console](https://console.cloud.google.com/)
- Authorized redirect URI: `http://localhost:8081/login/oauth2/code/google`
- `.env` file is automatically loaded by Docker Compose

### Step 2: Build JARs Locally

**IMPORTANT**: You must build the JARs before running Docker Compose, as the Dockerfiles copy pre-built JARs.

```bash
# Linux/Mac
./gradlew build -x test

# Windows
.\gradlew.bat build -x test
```

**Build Output**: JARs will be created in each service's `build/libs/` directory:
- `api-gateway/build/libs/api-gateway-1.0.0-SNAPSHOT.jar`
- `auth-service/build/libs/auth-service-1.0.0-SNAPSHOT.jar`
- ... and so on for all services

### Step 3: Start All Services

```bash
# Build images and start containers
docker-compose up --build

# Or run in detached mode (background)
docker-compose up -d --build
```

**First Startup**: Takes 2-3 minutes as Docker pulls base images and starts all services.

### Step 4: Verify Deployment

```bash
# Check all containers are running
docker ps

# Test public endpoint
curl http://localhost:8080/api/public/welcome

# Expected response:
# {"success":true,"data":"Welcome to WHO Cloud Public Service","timestamp":"..."}

# Test OAuth2 authentication (open in browser)
http://localhost:8081/login/oauth2/authorization/google
```

## 📊 Services Overview

| Service | Container Name | Port | Dockerfile | JAR Size | Health Check |
|---------|---------------|------|------------|----------|--------------|
| PostgreSQL | whocloud-postgres | 5432 | N/A (postgres:16-alpine) | - | ✅ pg_isready |
| API Gateway | whocloud-gateway | 8080 | api-gateway/Dockerfile | ~38MB | ✅ HTTP |
| Auth Service | whocloud-auth | 8081 | auth-service/Dockerfile | ~59MB | ✅ HTTP |
| User Management | whocloud-user-mgmt | 8082 | user-management-service/Dockerfile | ~55MB | ✅ HTTP |
| Information Service A | whocloud-info-a | 8083 | information-service-a/Dockerfile | ~24MB | ✅ HTTP |
| Information Service B | whocloud-info-b | 8084 | information-service-b/Dockerfile | ~24MB | ✅ HTTP |
| Business Service 1 | whocloud-business-1 | 8085 | business-service-1/Dockerfile | ~24MB | ✅ HTTP |
| Business Service 2 | whocloud-business-2 | 8086 | business-service-2/Dockerfile | ~24MB | ✅ HTTP |
| Business Service 3 | whocloud-business-3 | 8087 | business-service-3/Dockerfile | ~24MB | ✅ HTTP |
| Admin Portal | whocloud-admin | 8088 | admin-portal-service/Dockerfile | ~24MB | ✅ HTTP |
| Public Web Service | whocloud-public-web | 8089 | public-web-service/Dockerfile | ~24MB | ✅ HTTP |

**Total Containers**: 11 (1 database + 10 microservices)

## 🔍 Managing Containers

### View Logs

```bash
# All services (follow mode)
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway
docker-compose logs -f postgres

# Last 50 lines
docker-compose logs --tail=50 auth-service

# Logs since 10 minutes ago
docker-compose logs --since 10m
```

### Start/Stop Services

```bash
# Stop all services (keeps containers)
docker-compose stop

# Start stopped services
docker-compose start

# Restart specific service
docker-compose restart api-gateway

# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes (clean slate)
docker-compose down -v
```

### Scale Services (Optional)

```bash
# Scale business service to 3 instances
docker-compose up -d --scale business-service-1=3

# Note: Requires load balancer configuration
```

## 🧪 Testing the Deployment

### Health Checks

```bash
# Check all containers status
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# API Gateway health
curl http://localhost:8080/actuator/health

# Individual service health
curl http://localhost:8089/actuator/health
```

### Test Endpoints via API Gateway

```bash
# Public endpoint (no auth required)
curl http://localhost:8080/api/public/welcome

# OAuth2 Login (open in browser to get JWT token)
http://localhost:8081/login/oauth2/authorization/google

# After OAuth2 login, use the JWT token:
TOKEN="your-jwt-token-from-oauth2-response"

# Information Service A (GUEST can access)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/information/a

# Information Service B (GUEST can access)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/information/b

# Business Service 1 (GUEST denied - requires USER role)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/business/service1
# Returns: 403 Forbidden
curl http://localhost:8080/api/business/service1

# Admin Dashboard (TODO: requires auth)
curl http://localhost:8080/api/admin/dashboard
```

### Expected Responses

All endpoints return JSON with this structure:
```json
{
  "success": true,
  "message": null,
  "data": "Service-specific data",
  "timestamp": "2026-01-01T19:30:10.682328053"
}
```

## 🗄️ Database Configuration

### Connection Details

- **Host**: `postgres` (inside Docker network) or `localhost` (from host)
- **Port**: 5432
- **Databases**: `authdb`, `userdb`
- **Default Credentials** (Development Only):
  - Username: `whocloud`
  - Password: `whocloud123`
- **Data Persistence**: Docker volume `postgres-data`

### Connect to Database

```bash
# PostgreSQL CLI
docker exec -it whocloud-postgres psql -U whocloud -d authdb

# Run SQL query
docker exec -it whocloud-postgres psql -U whocloud -d authdb -c "SELECT 1"

# List databases
docker exec -it whocloud-postgres psql -U whocloud -c "\l"

# List tables in authdb
docker exec -it whocloud-postgres psql -U whocloud -d authdb -c "\dt"
```

### Database Initialization

The `init-db.sql` script runs automatically on first startup:
```sql
-- Creates authdb and userdb databases
-- Grants permissions to whocloud user
```

### Backup and Restore

```bash
# Backup
docker exec whocloud-postgres pg_dump -U whocloud authdb > backup_authdb.sql

# Restore
cat backup_authdb.sql | docker exec -i whocloud-postgres psql -U whocloud authdb

# Backup all databases
docker exec whocloud-postgres pg_dumpall -U whocloud > backup_all.sql
```

## 🌐 Docker Networking

### Network Details
- **Network Name**: `who-cloud_whocloud-network`
- **Driver**: bridge
- **Subnet**: Auto-assigned by Docker

### Service Discovery
Services communicate using container names:
```yaml
# API Gateway routes to:
http://whocloud-auth:8081/api/auth/**
http://whocloud-user-mgmt:8082/api/users/**
```

### Inspect Network

```bash
# List networks
docker network ls

# Inspect whocloud network
docker network inspect who-cloud_whocloud-network

# Show which containers are connected
docker network inspect who-cloud_whocloud-network --format='{{range .Containers}}{{.Name}} {{end}}'
```

## 🐛 Troubleshooting

### Common Issues and Solutions

#### 1. Port Already in Use

**Error**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solution (Windows)**:
```powershell
# Find process using port 8080
Get-NetTCPConnection -LocalPort 8080 | Select-Object -ExpandProperty OwningProcess

# Stop the process
Stop-Process -Id <process_id> -Force

# Or stop all processes on microservice ports
Get-NetTCPConnection -LocalPort 8080,8081,8082,8083,8084,8085,8086,8087,8088,8089 -ErrorAction SilentlyContinue | 
  Select-Object -ExpandProperty OwningProcess | 
  ForEach-Object { Stop-Process -Id $_ -Force }
```

**Solution (Linux/Mac)**:
```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use fuser
fuser -k 8080/tcp
```

#### 2. Container Fails to Start

**Error**: Container exits with code 1 or "no main manifest attribute"

**Cause**: JAR files weren't built before running docker-compose

**Solution**:
```bash
# Build JARs first
./gradlew build -x test

# Then start Docker Compose
docker-compose up --build
```

#### 3. Database Connection Failed

**Error**: Services can't connect to PostgreSQL

**Check**:
```bash
# Verify postgres is running
docker ps | grep postgres

# Check postgres logs
docker logs whocloud-postgres

# Verify network
docker network inspect who-cloud_whocloud-network

# Test connection from another container
docker exec -it whocloud-auth nc -zv postgres 5432
```

**Solution**:
```bash
# Restart postgres
docker-compose restart postgres

# Or recreate with fresh data
docker-compose down -v
docker-compose up -d postgres
```

#### 4. Service Shows "Unhealthy" Status

**Check health**:
```bash
# View health check status
docker inspect whocloud-auth --format='{{json .State.Health}}'

# Check service logs
docker logs whocloud-auth --tail 50
```

**Common causes**:
- Service still starting up (wait 30-60 seconds)
- Application error (check logs)
- Wrong health check endpoint

#### 5. Out of Memory

**Error**: Containers restart frequently or Docker Desktop shows high memory usage

**Solution**:
```bash
# Increase Docker Desktop memory limit (Settings > Resources)
# Recommended: 6-8GB for all services

# Or reduce number of running services
docker-compose up -d postgres api-gateway public-web-service
```

#### 6. Image Build Fails

**Error**: `failed to compute cache key` or `not found`

**Cause**: Build context doesn't include required files

**Check**:
```bash
# Verify JARs exist
ls -la api-gateway/build/libs/
ls -la auth-service/build/libs/

# Ensure all services have JARs
find . -name "*-SNAPSHOT.jar" -type f
```

**Solution**:
```bash
# Clean and rebuild
./gradlew clean build -x test

# Then rebuild Docker images
docker-compose build --no-cache
docker-compose up -d
```

#### 7. Gradle Build Locked

**Error**: `Unable to delete directory` during clean build

**Cause**: File is locked by another process (often Docker or IDE)

**Solution**:
```bash
# Stop Docker containers
docker-compose down

# Close IDE/file explorer

# Build without clean
./gradlew build -x test

# If still fails, restart system
```

#### 8. Network Issues

**Error**: Services can't communicate with each other

**Check**:
```bash
# Verify network exists
docker network ls | grep whocloud

# Check which containers are connected
docker network inspect who-cloud_whocloud-network
```

**Solution**:
```bash
# Recreate network
docker-compose down
docker network prune
docker-compose up -d
```

## 🔧 Advanced Configuration

### Environment Variables

Override default values using `.env` file:

```env
# Database
POSTGRES_USER=whocloud
POSTGRES_PASSWORD=whocloud123
POSTGRES_DB=whocloud

# Service Ports (if needed to change)
API_GATEWAY_PORT=8080
AUTH_SERVICE_PORT=8081
```

Apply environment variables:
```bash
docker-compose --env-file .env up -d
```

### Custom Build Context

Modify `docker-compose.yml` for specific scenarios:

```yaml
services:
  api-gateway:
    build:
      context: .
      dockerfile: api-gateway/Dockerfile
      args:
        - BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
```

### Resource Limits

Add resource constraints:

```yaml
services:
  api-gateway:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 512M
        reservations:
          memory: 256M
```

### Volume Mounts for Development

Mount source code for live reload (requires additional setup):

```yaml
services:
  api-gateway:
    volumes:
      - ./api-gateway/src:/app/src
```

## 📈 Monitoring

### Container Stats

```bash
# Real-time resource usage
docker stats

# Specific containers
docker stats whocloud-gateway whocloud-postgres

# One-time snapshot
docker stats --no-stream
```

### Disk Usage

```bash
# Overall Docker disk usage
docker system df

# Detailed breakdown
docker system df -v

# Remove unused resources
docker system prune -a --volumes
```

### Health Check Status

```bash
# Check all container health
docker ps --format "table {{.Names}}\t{{.Status}}"

# Detailed health info
docker inspect whocloud-auth --format='{{json .State.Health}}' | jq
```

## 🧹 Cleanup

### Remove Everything

```bash
# Stop and remove containers, networks, volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Nuclear option: Remove all Docker resources
docker system prune -a --volumes -f
```

### Selective Cleanup

```bash
# Remove stopped containers
docker container prune

# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune

# Remove unused networks
docker network prune
```

## 🚀 Production Considerations

### DO NOT Use in Production As-Is

Current setup is for **development only**. For production:

1. **Security**:
   - Change database credentials (use secrets)
   - Enable HTTPS/TLS
   - Add rate limiting
   - Enable Spring Security
   - Use strong JWT secrets

2. **Scalability**:
   - Use Kubernetes instead of Docker Compose
   - Add load balancer
   - Implement service mesh (Istio, Linkerd)
   - Use managed database (AWS RDS, Azure Database)

3. **Reliability**:
   - Add circuit breakers (Resilience4j)
   - Implement retry logic
   - Add distributed tracing (Zipkin, Jaeger)
   - Set up proper logging (ELK stack)

4. **Monitoring**:
   - Prometheus + Grafana
   - Application Performance Monitoring (APM)
   - Log aggregation
   - Alerting system

### Docker Compose Production Template

See `docker-compose.prod.yml` (create separately) with:
- Read-only root filesystem
- Non-root user
- Health checks with proper intervals
- Resource limits
- Restart policies
- Secret management

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)

## 🔗 Related Documentation

- [README.md](README.md) - Project overview and local development
- [KUBERNETES.md](KUBERNETES.md) - Kubernetes deployment (coming soon)

## 💡 Tips and Best Practices

1. **Always build JARs before Docker**: `./gradlew build -x test`
2. **Use detached mode for development**: `docker-compose up -d`
3. **Check logs frequently**: `docker-compose logs -f`
4. **Clean restart when in doubt**: `docker-compose down -v && docker-compose up -d`
5. **Monitor resource usage**: `docker stats`
6. **Keep Docker updated**: Update Docker Desktop regularly
7. **Use .dockerignore**: Exclude unnecessary files from build context
8. **Tag images properly**: Use versioning for images in CI/CD

---

**Last Updated**: January 1, 2026  
**Version**: 1.0.0  
**Tested With**: Docker 24.0+, Docker Compose 2.23+

# Stop the service and change port in docker-compose.yml
```

### Database connection issues
```bash
# Verify PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Connect to PostgreSQL
docker exec -it whocloud-postgres psql -U whocloud
```

### Clean rebuild
```bash
# Remove all containers, networks, and volumes
docker-compose down -v

# Remove built images
docker-compose down --rmi all -v

# Rebuild from scratch
docker-compose up --build
```

## Production Considerations

⚠️ **Before deploying to production:**

1. Change database credentials in `docker-compose.yml`
2. Update JWT secret in auth-service environment
3. Use proper secrets management (Docker Secrets, K8s Secrets)
4. Configure proper logging and monitoring
5. Set up backup strategy for PostgreSQL data
6. Use proper SSL/TLS certificates
7. Configure resource limits for containers
