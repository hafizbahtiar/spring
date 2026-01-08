# Environment Configuration Guide

This project uses environment variables for sensitive configuration values to avoid committing secrets to git.

## Quick Start

1. **Copy the example file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` file** with your actual values:
   ```bash
   # Edit .env file with your actual credentials
   nano .env  # or use your preferred editor
   ```

3. **The `.env` file is automatically loaded** when you run the application.

## Configuration Methods

### Method 1: Using `.env` file (Recommended for Local Development)

1. Copy `.env.example` to `.env`
2. Fill in your actual values in `.env`
3. The application will automatically load these values

**Note:** The `.env` file is already in `.gitignore` and will never be committed to git.

### Method 2: Using System Environment Variables (Recommended for Production)

Set environment variables directly in your system:

**Linux/macOS:**
```bash
export POSTGRES_URL=jdbc:postgresql://localhost:5432/console
export POSTGRES_USERNAME=console
export POSTGRES_PASSWORD=your_password
export JWT_SECRET=your-secret-key-here
export STRIPE_SECRET_KEY=sk_test_your_key_here
# ... etc
```

**Windows (PowerShell):**
```powershell
$env:POSTGRES_URL="jdbc:postgresql://localhost:5432/console"
$env:POSTGRES_USERNAME="console"
$env:POSTGRES_PASSWORD="your_password"
# ... etc
```

**Windows (CMD):**
```cmd
set POSTGRES_URL=jdbc:postgresql://localhost:5432/console
set POSTGRES_USERNAME=console
set POSTGRES_PASSWORD=your_password
# ... etc
```

### Method 3: IDE Run Configuration

You can also set environment variables in your IDE's run configuration:

**IntelliJ IDEA:**
- Run → Edit Configurations → Environment variables → Add variables

**VS Code:**
- Add to `.vscode/launch.json`:
```json
{
  "env": {
    "POSTGRES_URL": "jdbc:postgresql://localhost:5432/console",
    "POSTGRES_USERNAME": "console",
    "POSTGRES_PASSWORD": "your_password"
  }
}
```

## Available Environment Variables

### Database Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/console` |
| `POSTGRES_USERNAME` | PostgreSQL username | `console` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `console_2025!` |
| `MONGODB_URI` | MongoDB connection URI | `mongodb://127.0.0.1:27017/console` |
| `MONGODB_DATABASE` | MongoDB database name | `console` |
| `MONGODB_USERNAME` | MongoDB username (optional) | (empty) |
| `MONGODB_PASSWORD` | MongoDB password (optional) | (empty) |
| `MONGODB_AUTH_DATABASE` | MongoDB auth database (optional) | (empty) |

### JWT Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | JWT secret key (min 32 chars) | (see default in properties) |
| `JWT_EXPIRATION` | JWT expiration in milliseconds | `86400000` (24 hours) |

**Generate a secure JWT secret:**
```bash
openssl rand -base64 32
```

### Stripe Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `STRIPE_SECRET_KEY` | Stripe secret key | (empty) |
| `STRIPE_PUBLISHABLE_KEY` | Stripe publishable key | (empty) |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret | (empty) |

**Get Stripe keys from:** https://dashboard.stripe.com/test/apikeys

**Get webhook secret:**
- Local: Run `stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe`
- Production: Stripe Dashboard → Developers → Webhooks → Endpoint → Signing secret

## Production Deployment

For production deployments, always use system environment variables or your platform's secret management:

### Docker
```dockerfile
ENV POSTGRES_URL=jdbc:postgresql://db:5432/console
ENV JWT_SECRET=your-production-secret
# ... etc
```

### Kubernetes
```yaml
env:
  - name: POSTGRES_URL
    valueFrom:
      secretKeyRef:
        name: app-secrets
        key: postgres-url
```

### Heroku
```bash
heroku config:set JWT_SECRET=your-secret-key
heroku config:set STRIPE_SECRET_KEY=sk_live_xxx
```

### AWS/Cloud Platforms
Use your platform's secret management service (AWS Secrets Manager, Azure Key Vault, etc.)

## Security Best Practices

1. ✅ **Never commit `.env` file** - It's already in `.gitignore`
2. ✅ **Use strong secrets** - Generate random keys for production
3. ✅ **Use different keys** - Separate keys for development, staging, and production
4. ✅ **Rotate secrets regularly** - Change keys periodically
5. ✅ **Use secret management** - For production, use proper secret management services
6. ✅ **Limit access** - Only give access to secrets to those who need it

## Troubleshooting

### Environment variables not loading?

1. **Check `.env` file exists** in the project root
2. **Verify variable names** match exactly (case-sensitive)
3. **Restart the application** after changing `.env`
4. **Check for typos** in variable names

### Application.properties vs Environment Variables

- Environment variables **override** values in `application.properties`
- Format: `${ENV_VAR_NAME:default_value}`
- If `ENV_VAR_NAME` is set, it's used; otherwise, `default_value` is used

### Example

In `application.properties`:
```properties
jwt.secret=${JWT_SECRET:default-secret-key}
```

- If `JWT_SECRET` env var is set → uses that value
- If `JWT_SECRET` env var is NOT set → uses `default-secret-key`

