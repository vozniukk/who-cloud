# Docker Deployment Guide

## Prerequisites
- Docker and Docker Compose installed
- At least 4GB RAM available for Docker

## Quick Start

### Build and Start All Services
```bash
docker-compose up --build
```

### Start in Detached Mode
```bash
docker-compose up -d --build
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway
docker-compose logs -f postgres
```

### Stop All Services
```bash
docker-compose down
```

### Stop and Remove Volumes (Clean Start)
```bash
docker-compose down -v
```

## Services Overview

| Service | Container Name | Port | Health Check |
|---------|---------------|------|--------------|
| PostgreSQL | whocloud-postgres | 5432 | ✅ |
| API Gateway | whocloud-gateway | 8080 | ✅ |
| Auth Service | whocloud-auth | 8081 | ✅ |
| User Management | whocloud-user-mgmt | 8082 | ✅ |
| Information Service A | whocloud-info-a | 8083 | ✅ |
| Information Service B | whocloud-info-b | 8084 | ✅ |
| Business Service 1 | whocloud-business-1 | 8085 | ✅ |
| Business Service 2 | whocloud-business-2 | 8086 | ✅ |
| Business Service 3 | whocloud-business-3 | 8087 | ✅ |
| Admin Portal | whocloud-admin | 8088 | ✅ |
| Public Web Service | whocloud-public-web | 8089 | ✅ |

## Testing the Deployment

### Check Service Health
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Public Web Service
curl http://localhost:8089/api/public/welcome

# Information Service A
curl http://localhost:8083/api/information/a

# Via API Gateway (recommended)
curl http://localhost:8080/api/public/welcome
curl http://localhost:8080/api/information/a
```

### Check Database Connection
```bash
docker exec -it whocloud-postgres psql -U whocloud -d authdb -c "SELECT 1"
```

## Database Configuration

- **Default databases**: `authdb`, `userdb`
- **Default credentials**: 
  - Username: `whocloud`
  - Password: `whocloud123`
- **Data persistence**: Uses Docker volume `postgres-data`

## Troubleshooting

### Service won't start
```bash
# Check container logs
docker-compose logs service-name

# Restart specific service
docker-compose restart service-name
```

### Port already in use
```bash
# Check what's using the port
netstat -ano | findstr :8080

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
