# Security Package

This document describes the security components that provide authentication and authorization capabilities across the application.

## Overview

The security package contains shared security utilities used by authentication features and Spring Security configuration. These components are framework-level security infrastructure, not feature-specific business logic.

## Components

### JWT Token Provider

#### JwtTokenProvider (`JwtTokenProvider.java`)

Utility class for JWT token generation, validation, and information extraction.

**Key Features:**

- Token generation with user claims (userId, role, email)
- Token validation (signature and expiration)
- User information extraction from tokens
- HMAC-SHA256 signing algorithm
- Configurable expiration time

**Configuration:**

- `jwt.secret` - Secret key for signing tokens (from application.properties)
- `jwt.expiration` - Token expiration in milliseconds (default: 86400000 = 24 hours)

**Key Methods:**

- `generateToken(String username, Long userId, String role)` - Generate JWT token
- `validateToken(String token)` - Validate token signature and expiration
- `getUsernameFromToken(String token)` - Extract username from token
- `getUserIdFromToken(String token)` - Extract user ID from token
- `getRoleFromToken(String token)` - Extract role from token
- `getExpirationDateFromToken(String token)` - Get expiration date

**Token Structure:**

```json
{
  "sub": "user@example.com",
  "userId": 1,
  "role": "USER",
  "iat": 1234567890,
  "exp": 1234654290
}
```

### JWT Authentication Filter

#### JwtAuthenticationFilter (`JwtAuthenticationFilter.java`)

Spring Security filter that intercepts HTTP requests and validates JWT tokens.

**How It Works:**

1. Intercepts all HTTP requests
2. Extracts JWT token from `Authorization: Bearer {token}` header
3. Validates token using JwtTokenProvider
4. If valid, sets authentication in Spring Security context
5. Allows request to proceed to controller

**Integration:**

- Registered in SecurityConfig filter chain
- Executes before UsernamePasswordAuthenticationFilter
- Works with stateless session management

**Security Context:**

- Sets UsernamePasswordAuthenticationToken with:
  - Principal: username (email)
  - Authorities: Role-based (ROLE_USER, ROLE_ADMIN, etc.)
  - Details: userId (stored in authentication details)

## Usage

### Token Generation

Used by AuthService during login:

```java
String token = jwtTokenProvider.generateToken(
    user.getEmail(),
    user.getId(),
    user.getRole()
);
```

### Token Validation

Automatic validation on every request via JwtAuthenticationFilter, or manual validation:

```java
boolean isValid = jwtTokenProvider.validateToken(token);
```

### Extracting User Information

From authenticated requests:

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName(); // Email
Long userId = (Long) auth.getDetails(); // User ID
```

## Configuration

### Application Properties

```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-change-in-production-minimum-32-characters
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Security Configuration

The filter is integrated in `config/SecurityConfig.java`:

```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```

## Security Considerations

### Token Security

- **Strong Secret Key**: Use cryptographically secure random key (minimum 32 characters)
- **HTTPS Only**: Always use HTTPS in production to prevent token interception
- **Token Expiration**: Set appropriate expiration times based on security requirements
- **Token Storage**: Clients should store tokens securely (httpOnly cookies or secure storage)

### Best Practices

1. **Secret Management**: Store JWT secret in environment variables, not in code
2. **Token Rotation**: Consider implementing refresh tokens for longer sessions
3. **Token Blacklist**: Implement token blacklist for logout (optional enhancement)
4. **Rate Limiting**: Add rate limiting for token generation endpoints
5. **Monitoring**: Monitor token validation failures for security threats

## Integration Points

### With Authentication Feature

- Used by `features/auth/service/AuthServiceImpl` for token generation
- Used by `features/auth/service/AuthServiceImpl` for token validation
- Used by `features/auth/controller/v1/AuthController` for authentication endpoints

### With Spring Security

- Integrated into Spring Security filter chain
- Works with SecurityContext for authentication state
- Supports role-based authorization

## Token Lifecycle

1. **Generation**: User logs in → AuthService generates token → Token returned to client
2. **Usage**: Client includes token in Authorization header → Filter validates → Request proceeds
3. **Expiration**: Token expires → Validation fails → Client must login again
4. **Invalidation**: (Future) Token blacklist for logout

## Error Handling

### Invalid Token

- Filter catches exceptions during validation
- Request proceeds without authentication
- Protected endpoints return 401 Unauthorized

### Expired Token

- Token validation fails
- Client receives 401 Unauthorized
- Client must login again to get new token

## Future Enhancements

- Refresh token mechanism
- Token blacklist/revocation
- Token rotation
- Multi-device token management
- Token encryption for sensitive claims
- Token introspection endpoint

## Related Documentation

- [Authentication Feature README](../features/auth/README.md)
- [Security Configuration](../../config/SecurityConfig.java)
- [Authentication Implementation Guide](../../../docs/authentication-implementation.md)
