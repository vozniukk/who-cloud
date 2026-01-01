# nginx SSL Directory

This directory should contain your SSL/TLS certificates for HTTPS.

## Development (Self-Signed Certificate)

For local testing, generate a self-signed certificate:

```bash
# Generate self-signed certificate (valid for 365 days)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout key.pem \
  -out cert.pem \
  -subj "/C=US/ST=State/L=City/O=WHO-Cloud/CN=localhost"

# Generate Diffie-Hellman parameters (takes a few minutes)
openssl dhparam -out dhparam.pem 2048
```

## Production (Let's Encrypt)

For production, use Let's Encrypt:

```bash
# Using certbot
certbot certonly --standalone \
  -d yourdomain.com \
  --email your-email@example.com \
  --agree-tos

# Copy certificates
cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem ./cert.pem
cp /etc/letsencrypt/live/yourdomain.com/privkey.pem ./key.pem
```

## Required Files

- `cert.pem` - SSL certificate (public key)
- `key.pem` - Private key (keep secure!)
- `dhparam.pem` - Diffie-Hellman parameters (optional but recommended)

## Security Notes

⚠️ **IMPORTANT**:
- Never commit SSL private keys to version control
- Keep `key.pem` secure with proper file permissions (600)
- Use strong passwords for private keys in production
- Rotate certificates before expiry
- Monitor certificate expiration dates

## .gitignore

These files should be added to `.gitignore`:
```
nginx/ssl/*.pem
nginx/ssl/*.key
nginx/ssl/*.crt
```
