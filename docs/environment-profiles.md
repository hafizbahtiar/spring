# Spring Boot Environment Profiles Guide

This guide explains how to differentiate between development (local test) and production (server) environments in Spring Boot.

## Overview

Spring Boot uses **profiles** to manage environment-specific configurations. The application supports two main profiles:

- **`dev`** - Development/Local testing (default)
- **`prod`** - Production server deployment

## Configuration Files

### Main Configuration

- `application.properties` - Base configuration (applies to all profiles)
- Default profile: `dev`

### Profile-Specific Configurations

- `application-dev.properties` - Development overrides
- `application-prod.properties` - Production overrides

## How to Activate Profiles

### Method 1: Environment Variable (Recommended for Production)

```bash
# Development (default)
export SPRING_PROFILES_ACTIVE=dev

# Production
export SPRING_PROFILES_ACTIVE=prod
```

### Method 2: Command Line Argument

```bash
# Development
./mvnw spring-boot:run --spring.profiles.active=dev

# Production
./mvnw spring-boot:run --spring.profiles.active=prod

# Or with java -jar
java -jar target/spring-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Method 3: Application Properties

In `application.properties`:

```properties
spring.profiles.active=dev  # or prod
```

### Method 4: IDE Configuration (IntelliJ IDEA / Eclipse)

**IntelliJ IDEA:**

1. Run → Edit Configurations
2. Select your Spring Boot application
3. In "Environment variables" or "Program arguments":
   - Add: `SPRING_PROFILES_ACTIVE=dev` (or `prod`)

**Eclipse:**

1. Run → Run Configurations
2. Select your Spring Boot application
3. Arguments tab → Program arguments:
   - Add: `--spring.profiles.active=dev` (or `prod`)

## Checking Current Profile in Code

### Using EnvironmentConfig (Recommended)

```java
@Autowired
private EnvironmentConfig environmentConfig;

public void someMethod() {
    if (environmentConfig.isDevelopment()) {
        // Development-specific code
        log.debug("Running in development mode");
    }

    if (environmentConfig.isProduction()) {
        // Production-specific code
        log.info("Running in production mode");
    }
}
```

### Using Environment Directly

```java
@Autowired
private Environment environment;

public void someMethod() {
    String[] activeProfiles = environment.getActiveProfiles();
    boolean isDev = environment.acceptsProfiles(Profiles.of("dev"));
    boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));
}
```

### Using @Value Annotation

```java
@Value("${app.environment:development}")
private String appEnvironment;

@Value("${app.debug:false}")
private boolean appDebug;
```

## Key Differences Between Dev and Prod

### Development (`dev`)

- ✅ Debug logging enabled
- ✅ SQL queries logged
- ✅ Hibernate auto-updates schema (`ddl-auto=update`)
- ✅ Template caching disabled
- ✅ All actuator endpoints exposed
- ✅ Localhost CORS allowed
- ✅ Smaller connection pool

### Production (`prod`)

- ❌ Debug logging disabled
- ❌ SQL queries not logged
- ❌ Hibernate validates schema only (`ddl-auto=validate`)
- ✅ Template caching enabled
- ✅ Limited actuator endpoints
- ✅ Restricted CORS (production URLs only)
- ✅ Larger connection pool
- ✅ Compression enabled

## Environment Variables

### Development (.env file)

```env
SPRING_PROFILES_ACTIVE=dev
POSTGRES_URL=jdbc:postgresql://localhost:5432/console
POSTGRES_USERNAME=console
POSTGRES_PASSWORD=console_2025!
MONGODB_URI=mongodb://localhost:27017/console
JWT_SECRET=dev-secret-key-minimum-32-characters
```

### Production (System Environment Variables)

```bash
export SPRING_PROFILES_ACTIVE=prod
export POSTGRES_URL=jdbc:postgresql://prod-db:5432/console
export POSTGRES_USERNAME=console
export POSTGRES_PASSWORD=secure_production_password
export MONGODB_URI=mongodb://prod-mongo:27017/console
export JWT_SECRET=strong-production-secret-key-minimum-32-characters
export CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

## Docker Deployment

### Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/spring-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]
```

### Docker Compose

```yaml
services:
  spring-app:
    image: your-app:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - POSTGRES_URL=jdbc:postgresql://db:5432/console
      - MONGODB_URI=mongodb://mongo:27017/console
    ports:
      - "8080:8080"
```

## Verification

### Check Active Profile on Startup

When the application starts, you'll see:

```
========================================
Application Environment: development
Active Profiles: dev
Debug Mode: true
========================================
```

### Check via Actuator (if enabled)

```bash
# Development
curl http://localhost:8080/actuator/env

# Production
curl https://yourdomain.com/actuator/env
```

## Best Practices

1. **Never commit production secrets** - Use environment variables
2. **Use `.env` for local development** - Add `.env` to `.gitignore`
3. **Validate production settings** - Ensure `ddl-auto=validate` in production
4. **Use strong secrets in production** - Generate with `openssl rand -base64 32`
5. **Restrict CORS in production** - Only allow your production domains
6. **Enable compression in production** - Better performance
7. **Limit actuator endpoints in production** - Security best practice

## Troubleshooting

### Profile Not Activating

- Check environment variable: `echo $SPRING_PROFILES_ACTIVE`
- Check application logs for active profiles
- Verify profile-specific properties file exists

### Configuration Not Loading

- Profile-specific properties override base `application.properties`
- Check property precedence: Profile > Environment Variable > application.properties

### Wrong Environment Detected

- Verify `SPRING_PROFILES_ACTIVE` is set correctly
- Check if multiple profiles are active (comma-separated)
- Review `EnvironmentConfig` logs on startup
