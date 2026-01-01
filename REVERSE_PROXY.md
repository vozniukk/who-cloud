# Reverse Proxy Setup with nginx

Complete guide for deploying WHO Cloud with nginx as a reverse proxy for enhanced security and performance.

## 🎯 Overview

This setup adds **nginx** as the entry point for all public traffic, providing:
- **SSL/TLS termination** for HTTPS
- **Rate limiting** to prevent abuse
- **Security headers** for enhanced protection
- **Request filtering** and size limits
- **Static content serving** (optional)
- **Load balancing** (when scaling horizontally)
- **Centralized logging** and monitoring

## 🏗️ Architecture

### Before (Development)
```
Internet → API Gateway (8080) → Microservices → PostgreSQL
```

### After (Production)
```
Internet (Public)
    ↓
nginx (443 HTTPS, 80 HTTP)
    ↓ (internal network)
Spring Cloud Gateway (8080 - internal only)
    ↓ (internal network)
Auth Service (8081 - internal only)
Other Microservices (8082-8089 - internal only)
    ↓ (internal network)
PostgreSQL (5432 - internal only)
```

**Key Benefits**:
- Only nginx is exposed to internet (ports 80/443)
- All internal services communicate within Docker network
- nginx acts as security gateway before reaching application
- Easy to add WAF, CDN, or additional security layers

## 📁 Directory Structure

```
who-cloud/
├── nginx/
│   ├── nginx.conf              # Main nginx configuration
│   ├── conf.d/
│   │   ├── default.conf        # Default server block
│   │   ├── api-gateway.conf    # API Gateway proxy
│   │   └── security.conf       # Security headers
│   ├── ssl/
│   │   ├── cert.pem            # SSL certificate (production)
│   │   ├── key.pem             # SSL private key (production)
│   │   └── dhparam.pem         # Diffie-Hellman parameters
│   ├── html/
│   │   ├── 50x.html            # Error pages
│   │   └── rate-limit.html     # Rate limit error page
│   └── logs/                   # nginx access/error logs
├── docker-compose.yml
└── .env
```

## 🚀 Quick Start

### 1. Current Configuration Applied

The nginx setup has been integrated with:
- ✅ Reasonable rate limits (100 req/s with 200 burst)
- ✅ Security headers (HSTS, XSS protection, etc.)
- ✅ Request size limits (50MB max)
- ✅ Proxy configuration for API Gateway
- ✅ Direct proxy for Auth Service OAuth2
- ✅ Custom error pages
- ✅ HTTPS ready (certificate required)

### 2. Start Services

```bash
# Build and start all services including nginx
docker-compose up -d --build

# View nginx logs
docker logs whocloud-nginx -f

# Check nginx configuration
docker exec whocloud-nginx nginx -t
```

### 3. Access Points

**Development (HTTP)**:
- http://localhost → nginx → API Gateway
- http://localhost/api/public/welcome → Public endpoint
- http://localhost/login/oauth2/authorization/google → OAuth2 login

**Production (HTTPS)**:
- https://yourdomain.com → nginx (SSL) → API Gateway
- All traffic encrypted with TLS 1.2/1.3

## 🔧 Configuration Details

### Rate Limiting

**Current Settings** (Moderate - suitable for production start):
```nginx
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=100r/s;
limit_req_zone $binary_remote_addr zone=oauth_limit:10m rate=20r/s;

# Burst capacity: 200 requests (api), 50 requests (oauth)
```

**What this means**:
- **Normal API calls**: 100 requests per second per IP
- **Burst**: Additional 200 requests allowed temporarily
- **OAuth endpoints**: 20 requests per second (prevents brute force)
- **Burst**: Additional 50 requests for OAuth flow

**Adjust for your needs**:
- **Higher traffic**: Increase `rate=100r/s` to `rate=500r/s`
- **Stricter limits**: Decrease to `rate=10r/s`
- **Per-endpoint limits**: Add custom zones in `nginx.conf`

### Connection Limits

```nginx
limit_conn_zone $binary_remote_addr zone=conn_limit:10m;
limit_conn conn_limit 20;  # Max 20 concurrent connections per IP
```

### Request Size Limits

```nginx
client_max_body_size 50M;          # Maximum request body
client_body_buffer_size 128k;      # Buffer size for request body
large_client_header_buffers 4 16k; # Header buffer size
```

**Suitable for**:
- File uploads (images, documents)
- Large JSON payloads
- Multipart form data

**Adjust**:
- **File uploads**: Increase to `100M` or `500M`
- **API-only**: Decrease to `10M`

### Security Headers

Automatically added to all responses:

```nginx
X-Frame-Options: SAMEORIGIN                 # Prevent clickjacking
X-Content-Type-Options: nosniff             # Prevent MIME sniffing
X-XSS-Protection: 1; mode=block             # XSS protection
Referrer-Policy: strict-origin-when-cross-origin
Strict-Transport-Security: max-age=31536000 # Force HTTPS (1 year)
```

### Timeout Settings

```nginx
proxy_connect_timeout 60s;  # Connection to backend
proxy_send_timeout 60s;     # Sending request to backend  
proxy_read_timeout 60s;     # Reading response from backend
```

**Adjust for**:
- **Long-running APIs**: Increase to `300s` or `600s`
- **Fast APIs**: Decrease to `30s`

## 🔒 SSL/TLS Configuration

### Development (Self-Signed Certificate)

For local testing, generate self-signed certificate:

```bash
# Create SSL directory
mkdir -p nginx/ssl

# Generate self-signed certificate (valid for 365 days)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/key.pem \
  -out nginx/ssl/cert.pem \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"

# Generate Diffie-Hellman parameters (takes a few minutes)
openssl dhparam -out nginx/ssl/dhparam.pem 2048
```

### Production (Let's Encrypt)

**Option 1: Certbot Manual**

```bash
# Install certbot
sudo apt-get install certbot

# Generate certificate
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# Copy certificates
cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem nginx/ssl/cert.pem
cp /etc/letsencrypt/live/yourdomain.com/privkey.pem nginx/ssl/key.pem

# Auto-renewal (cron job)
0 0 * * 0 certbot renew --quiet
```

**Option 2: Certbot with nginx**

```bash
# Use certbot nginx plugin
docker run -it --rm \
  -v $(pwd)/nginx/ssl:/etc/letsencrypt \
  -p 80:80 -p 443:443 \
  certbot/certbot certonly --standalone \
  -d yourdomain.com \
  --email your-email@example.com \
  --agree-tos
```

**Option 3: Traefik (Alternative)**

If you prefer automatic SSL, consider switching to Traefik (see [TRAEFIK_SETUP.md](TRAEFIK_SETUP.md) - coming soon).

### Update nginx Configuration for SSL

Edit `nginx/conf.d/api-gateway.conf`:

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    # SSL Configuration
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_dhparam /etc/nginx/ssl/dhparam.pem;
    
    # SSL Settings
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # Rest of configuration...
}
```

## 📊 Monitoring & Logging

### Access Logs

```bash
# Real-time access log
docker exec whocloud-nginx tail -f /var/log/nginx/access.log

# Filter successful requests
docker exec whocloud-nginx grep " 200 " /var/log/nginx/access.log

# Count requests per IP
docker exec whocloud-nginx awk '{print $1}' /var/log/nginx/access.log | sort | uniq -c | sort -rn
```

### Error Logs

```bash
# Real-time error log
docker exec whocloud-nginx tail -f /var/log/nginx/error.log

# Rate limit errors
docker exec whocloud-nginx grep "limiting requests" /var/log/nginx/error.log
```

### Metrics

Check nginx status (if enabled):

```bash
# Add to nginx.conf:
location /nginx_status {
    stub_status on;
    access_log off;
    allow 127.0.0.1;
    deny all;
}

# Query status
curl http://localhost/nginx_status
```

### Log Rotation

nginx logs can grow large. Configure rotation:

```bash
# /etc/logrotate.d/nginx
/var/log/nginx/*.log {
    daily
    missingok
    rotate 14
    compress
    delaycompress
    notifempty
    sharedscripts
    postrotate
        docker exec whocloud-nginx nginx -s reload
    endscript
}
```

## 🔧 Tuning & Optimization

### Worker Processes

```nginx
# nginx.conf
worker_processes auto;  # Auto-detect CPU cores
worker_connections 1024; # Connections per worker
```

**Adjust for**:
- **High traffic**: `worker_connections 2048` or `4096`
- **Low resources**: `worker_processes 2`

### Buffer Sizes

```nginx
proxy_buffering on;
proxy_buffer_size 4k;
proxy_buffers 8 4k;
proxy_busy_buffers_size 8k;
```

**Increase for**:
- Large responses from backend
- File downloads/uploads

### Caching (Optional)

Enable caching for static content:

```nginx
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=api_cache:10m max_size=100m;

location /api/public/ {
    proxy_cache api_cache;
    proxy_cache_valid 200 5m;
    proxy_cache_key "$scheme$request_method$host$request_uri";
    add_header X-Cache-Status $upstream_cache_status;
    
    proxy_pass http://api_gateway;
}
```

## 🐛 Troubleshooting

### nginx Won't Start

```bash
# Check configuration syntax
docker exec whocloud-nginx nginx -t

# View error logs
docker logs whocloud-nginx

# Common issues:
# - Port 80/443 already in use
# - SSL certificate files not found
# - Invalid configuration syntax
```

### Rate Limiting Too Strict

```bash
# Edit nginx/nginx.conf
# Increase rate: rate=100r/s → rate=500r/s
# Increase burst: burst=200 → burst=1000

# Reload nginx (no downtime)
docker exec whocloud-nginx nginx -s reload
```

### Backend Connection Errors

```bash
# Check API Gateway is running
docker ps | grep gateway

# Check nginx can reach backend
docker exec whocloud-nginx curl http://api-gateway:8080/actuator/health

# Check network
docker network inspect who-cloud_whocloud-network
```

### SSL Certificate Issues

```bash
# Verify certificate
openssl x509 -in nginx/ssl/cert.pem -text -noout

# Check certificate expiry
openssl x509 -in nginx/ssl/cert.pem -noout -enddate

# Test SSL connection
openssl s_client -connect localhost:443
```

### High Memory Usage

```bash
# Check nginx memory
docker stats whocloud-nginx

# Reduce buffer sizes in nginx.conf
# Reduce worker_processes
# Disable caching if not needed
```

## 🚀 Advanced Configuration

### Load Balancing (Multiple Gateway Instances)

```nginx
upstream api_gateway_cluster {
    least_conn;  # Load balancing algorithm
    server api-gateway-1:8080 weight=1;
    server api-gateway-2:8080 weight=1;
    server api-gateway-3:8080 weight=1;
}

server {
    location / {
        proxy_pass http://api_gateway_cluster;
    }
}
```

### Geographic Restrictions

```nginx
# Block specific countries (requires GeoIP module)
geo $blocked_country {
    default 0;
    # Example: block specific IP ranges
    192.168.1.0/24 1;
}

server {
    if ($blocked_country) {
        return 403;
    }
}
```

### IP Whitelisting

```nginx
# Allow only specific IPs
location /api/admin/ {
    allow 192.168.1.0/24;  # Internal network
    allow 10.0.0.5;         # Specific admin IP
    deny all;
    
    proxy_pass http://api_gateway;
}
```

### Custom Error Pages

```nginx
error_page 404 /404.html;
error_page 500 502 503 504 /50x.html;
error_page 429 /rate-limit.html;

location = /50x.html {
    root /usr/share/nginx/html;
}
```

### WebSocket Support

```nginx
location /ws/ {
    proxy_pass http://api_gateway;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_read_timeout 86400;
}
```

## 📈 Performance Benchmarks

Test nginx performance:

```bash
# Install Apache Bench
apt-get install apache2-utils

# Test 1000 requests with 10 concurrent
ab -n 1000 -c 10 http://localhost/api/public/welcome

# Test with rate limiting
ab -n 10000 -c 100 http://localhost/api/public/welcome
```

**Expected Results** (with current config):
- Throughput: ~5000-10000 req/s
- Latency: <10ms (nginx overhead)
- Rate limiting: Kicks in at 100 req/s per IP

## 🔐 Security Checklist

- [x] nginx as reverse proxy
- [x] Rate limiting configured
- [x] Security headers enabled
- [x] Request size limits
- [x] Connection limits per IP
- [ ] SSL/TLS certificates (requires setup)
- [ ] Firewall rules (only 80/443 open)
- [ ] DDoS protection (Cloudflare/AWS Shield)
- [ ] WAF integration (optional)
- [ ] IP whitelisting for admin (optional)
- [ ] Geographic blocking (optional)
- [ ] Log monitoring & alerts
- [ ] Regular security audits

## 📚 Additional Resources

- [nginx Documentation](https://nginx.org/en/docs/)
- [nginx Security Best Practices](https://www.nginx.com/blog/nginx-security-best-practices/)
- [Let's Encrypt](https://letsencrypt.org/)
- [SSL Labs Test](https://www.ssllabs.com/ssltest/)
- [nginx Rate Limiting](https://www.nginx.com/blog/rate-limiting-nginx/)

## 🆘 Support

For issues:
1. Check nginx logs: `docker logs whocloud-nginx`
2. Verify configuration: `docker exec whocloud-nginx nginx -t`
3. Review [Troubleshooting](#-troubleshooting) section
4. Check [README.md](README.md) for general setup

---

**Last Updated**: January 2, 2026  
**Version**: 1.0.0  
**Status**: ✅ Production Ready
