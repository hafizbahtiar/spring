# Authentication Feature

This document explains the authentication feature implementation, architecture, and usage.

## Overview

The authentication feature (`features/auth/`) provides secure user authentication using JWT (JSON Web Tokens). It is implemented as a separate feature module following the features-first architecture, maintaining clear separation from the user management feature.

## Architecture

### Feature Separation

- **`features/user/`** - User Management

  - User CRUD operations
  - Profile management
  - User data access

- **`features/auth/`** - Authentication (This Feature)
  - Login/logout operations
  - JWT token generation and validation
  - Authentication logic
  - Uses `features/user/` for user data access

### Component Structure

```
features/auth/
├── dto/
│   ├── LoginRequest.java      # Login credentials (identifier + password)
│   └── LoginResponse.java      # JWT token + user info response
├── service/
│   ├── AuthService.java        # Authentication service interface
│   └── AuthServiceImpl.java    # Authentication service implementation
└── controller/v1/
    └── AuthController.java     # REST API endpoints

security/
├── JwtTokenProvider.java       # JWT token generation and validation utilities
└── JwtAuthenticationFilter.java  # Spring Security filter for JWT validation
```

## Components Explained

### 1. DTOs (Data Transfer Objects)

#### LoginRequest

- **Purpose**: Receives login credentials from client
- **Fields**:
  - `identifier` (String): Email or username (case-insensitive)
  - `password` (String): User password
- **Validation**: Both fields are required (`@NotBlank`)

#### LoginResponse

- **Purpose**: Returns authentication result to client
- **Fields**:
  - `token` (String): JWT token for subsequent requests
  - `type` (String): Token type, defaults to "Bearer"
  - `expiresAt` (LocalDateTime): Token expiration timestamp
  - `user` (UserResponse): Authenticated user information

### 2. Service Layer

#### AuthService Interface

Defines authentication operations:

- `login(LoginRequest)` - Authenticate user and return JWT token
- `getCurrentUser()` - Get current authenticated user from security context
- `validateToken(String)` - Validate JWT token

#### AuthServiceImpl

Implementation details:

- **Login Process**:

  1. Find user by email or username (case-insensitive)
  2. Validate password using BCrypt
  3. Update last login timestamp
  4. Generate JWT token with user claims (id, email, username, role)
  5. Return token and user information

- **Current User**:

  - Extracts authentication from Spring Security context
  - Retrieves user from database
  - Returns user information

- **Token Validation**:
  - Delegates to JwtTokenProvider
  - Validates token signature and expiration

### 3. Controller

#### AuthController

REST API endpoints:

- **POST `/api/v1/auth/login`**

  - Authenticate user with credentials
  - Returns JWT token and user info
  - **Request Body**: `LoginRequest`
  - **Response**: `LoginResponse`
  - **Status**: 200 OK

- **GET `/api/v1/auth/me`**

  - Get current authenticated user
  - Requires valid JWT token in Authorization header
  - **Response**: `UserResponse`
  - **Status**: 200 OK

- **POST `/api/v1/auth/validate`**
  - Validate JWT token
  - **Query Parameter**: `token`
  - **Response**: `boolean`
  - **Status**: 200 OK

### 4. JWT Utilities

#### JwtTokenProvider

Located in `security/` package (shared security component):

- **Token Generation**:

  - Creates JWT with user claims (userId, role, email)
  - Signs token with HMAC-SHA256
  - Sets expiration time (configurable, default 24 hours)

- **Token Validation**:

  - Verifies token signature
  - Checks token expiration
  - Extracts user information from claims

- **Key Methods**:
  - `generateToken(String username, Long userId, String role)` - Generate token
  - `validateToken(String token)` - Validate token
  - `getUsernameFromToken(String token)` - Extract username
  - `getUserIdFromToken(String token)` - Extract user ID
  - `getRoleFromToken(String token)` - Extract role

#### JwtAuthenticationFilter

Spring Security filter that:

- Intercepts HTTP requests
- Extracts JWT token from `Authorization: Bearer {token}` header
- Validates token using JwtTokenProvider
- Sets authentication in Spring Security context
- Allows request to proceed if token is valid

## Authentication Flow

### Login Flow

```
1. Client sends POST /api/v1/auth/login
   {
     "identifier": "user@example.com",
     "password": "password123"
   }

2. AuthController receives request
   ↓
3. AuthService.login() is called
   ↓
4. UserRepository finds user by email/username
   ↓
5. PasswordEncoder validates password (BCrypt)
   ↓
6. User's lastLoginAt is updated
   ↓
7. JwtTokenProvider generates JWT token
   ↓
8. LoginResponse is returned with token and user info
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "type": "Bearer",
     "expiresAt": "2024-12-23T12:00:00",
     "user": { ... }
   }
```

### Protected Request Flow

```
1. Client sends request with JWT token
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. JwtAuthenticationFilter intercepts request
   ↓
3. Extracts token from Authorization header
   ↓
4. JwtTokenProvider validates token
   ↓
5. If valid, sets authentication in SecurityContext
   ↓
6. Request proceeds to controller
   ↓
7. Controller can access authenticated user via SecurityContext
```

## Configuration

### Application Properties

Add to `application.properties` or environment variables:

```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-change-in-production-minimum-32-characters
jwt.expiration=86400000  # 24 hours in milliseconds
```

**Important**:

- Use a strong, random secret key in production (minimum 32 characters)
- Store secret in environment variables, not in code
- Use different secrets for different environments

### Security Configuration

The `SecurityConfig` class configures:

- **Public Endpoints**: `/api/v1/auth/**` (login, validate)
- **Public Endpoints**: `/api/v1/users` (registration)
- **Protected Endpoints**: All other endpoints require authentication
- **JWT Filter**: Added to Spring Security filter chain
- **Stateless Sessions**: No server-side session storage

## Usage Examples

### 1. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "password123"
  }'
```

**Response**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresAt": "2024-12-23T12:00:00",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "username": "johndoe",
    "role": "USER",
    ...
  }
}
```

### 2. Access Protected Endpoint

```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response**:

```json
{
  "id": 1,
  "email": "user@example.com",
  "username": "johndoe",
  "role": "USER",
  ...
}
```

### 3. Validate Token

```bash
curl -X POST "http://localhost:8080/api/v1/auth/validate?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response**: `true` or `false`

## Security Considerations

### Password Security

- Passwords are hashed using BCrypt (strength: 12)
- Never store plain text passwords
- Password validation happens server-side only

### Token Security

- Tokens are signed with HMAC-SHA256
- Tokens contain user claims (id, email, role)
- Tokens have expiration time (default 24 hours)
- Use HTTPS in production to prevent token interception

### Best Practices

1. **Strong Secret Key**: Use a cryptographically secure random key (minimum 32 characters)
2. **Token Expiration**: Set appropriate expiration times based on security requirements
3. **HTTPS Only**: Always use HTTPS in production
4. **Token Storage**: Store tokens securely on client (httpOnly cookies or secure storage)
5. **Refresh Tokens**: Consider implementing refresh tokens for longer sessions (future enhancement)

## Error Handling

### Custom Exceptions

All auth-related exceptions are located in `features/auth/exception/`:

- `InvalidCredentialsException` - Invalid login credentials

**Common Exceptions** (shared across features, located in `common/exception/`):

- `ValidationException` - Used for validation errors

**Spring Security Exceptions:**

- `BadCredentialsException` - Spring Security's exception for invalid credentials

### Error Responses

#### Invalid Credentials

- **Status**: 401 Unauthorized
- **Exception**: `InvalidCredentialsException` or `BadCredentialsException`
- **Message**: "Invalid credentials"

#### Invalid/Expired Token

- **Status**: 401 Unauthorized
- **Response**: Token validation fails, request is rejected

#### User Not Found

- **Status**: 401 Unauthorized
- **Exception**: `BadCredentialsException` (for security, doesn't reveal if user exists)
- **Message**: "Invalid credentials"

All exceptions are handled by `GlobalExceptionHandler` and return appropriate HTTP status codes.

## Dependencies

### Required Dependencies (in pom.xml)

- `spring-boot-starter-security` - Spring Security framework
- `jjwt-api` (0.12.3) - JWT API
- `jjwt-impl` (0.12.3) - JWT implementation
- `jjwt-jackson` (0.12.3) - JWT Jackson support

### Internal Dependencies

- `features/user` - User repository and entity
- `security/PasswordEncoder` - Password encoding (from user feature)

## Testing

### Unit Tests

- Test `AuthService` with mocked dependencies
- Test `JwtTokenProvider` token generation and validation
- Test `JwtAuthenticationFilter` token extraction

### Integration Tests

- Test login endpoint with valid/invalid credentials
- Test protected endpoints with/without token
- Test token expiration scenarios

## Future Enhancements

1. **Refresh Tokens**: Implement refresh token mechanism for longer sessions
2. **Token Blacklist**: Implement token blacklist for logout functionality
3. **OAuth2 Integration**: Add OAuth2 provider support (Google, GitHub, etc.)
4. **Two-Factor Authentication**: Add 2FA support
5. **Rate Limiting**: Add rate limiting for login attempts
6. **Account Lockout**: Lock accounts after failed login attempts

## Related Documentation

- [Authentication Implementation Guide](../docs/authentication-implementation.md)
- [API Structure Documentation](../docs/api-structure.md)
- [Security Configuration](../../config/SecurityConfig.java)
