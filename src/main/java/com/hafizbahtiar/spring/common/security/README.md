# Authorization & Role-Based Access Control (RBAC)

This package contains the authorization infrastructure for role-based access control (RBAC) in the Spring Boot application.

## Components

### 1. Role Enum (`Role.java`)

Defines the available user roles in the system:

- **USER**: Standard user role (default)
- **ADMIN**: Administrator role with elevated privileges

**Usage:**

```java
Role role = Role.USER;
Role adminRole = Role.ADMIN;
Role fromString = Role.fromString("ADMIN"); // Converts string to Role enum
```

### 2. UserPrincipal (`UserPrincipal.java`)

Implements Spring Security's `UserDetails` interface. Stores user information extracted from JWT tokens and provides:

- User ID, username, email, and role
- Authority management (converts role to `GrantedAuthority`)
- Helper methods: `isAdmin()`, `isUser()`, `ownsResource()`

**Key Methods:**

- `isAdmin()`: Check if user has ADMIN role
- `ownsResource(Long userId)`: Check if user owns a resource by user ID

### 3. SecurityUtils (`SecurityUtils.java`)

Static utility class for security operations:

- `getCurrentUser()`: Get current authenticated UserPrincipal
- `getCurrentUserId()`: Get current user's ID
- `isAdmin()`: Check if current user is admin
- `ownsResource(Long resourceUserId)`: Check if current user owns resource or is admin
- `isAuthenticated()`: Check if user is authenticated

**Usage:**

```java
Long currentUserId = SecurityUtils.getCurrentUserId();
boolean isAdmin = SecurityUtils.isAdmin();
boolean owns = SecurityUtils.ownsResource(resourceUserId);
```

### 4. SecurityService (`SecurityService.java`)

Spring service bean that can be used in SpEL expressions for `@PreAuthorize` annotations:

- `ownsResource(Long resourceUserId)`: Check resource ownership
- `isAdmin()`: Check admin status

**Why needed?** `@PreAuthorize` annotations use SpEL (Spring Expression Language) which can reference Spring beans using `@beanName` syntax, but cannot directly call static methods.

## Authorization Flow

### 1. Authentication (JWT Token)

When a user logs in:

1. `AuthService.login()` generates a JWT token with user ID, email, and role
2. Token is returned to the client

### 2. Request Processing

When a protected endpoint is called:

1. **JwtAuthenticationFilter** intercepts the request
2. Extracts JWT token from `Authorization: Bearer <token>` header
3. Validates token and extracts user information (userId, username, role)
4. Creates `UserPrincipal` with user details
5. Sets authentication in `SecurityContext` with authorities (`ROLE_USER` or `ROLE_ADMIN`)

### 3. Authorization Check

When a method with `@PreAuthorize` is called:

1. Spring Security evaluates the SpEL expression
2. Checks if the condition is met (role, ownership, etc.)
3. Allows or denies access based on the expression result

## Method-Level Security Annotations

### @PreAuthorize

Used to check authorization **before** method execution.

**Common Patterns:**

1. **Role-based:**

   ```java
   @PreAuthorize("hasRole('ADMIN')")
   ```

2. **Authenticated users:**

   ```java
   @PreAuthorize("isAuthenticated()")
   ```

3. **Combined (OR):**

   ```java
   @PreAuthorize("hasRole('ADMIN') or @securityUtils.ownsResource(#id)")
   ```

4. **Combined (AND):**
   ```java
   @PreAuthorize("hasRole('ADMIN') and isAuthenticated()")
   ```

### SpEL Expression Reference

- `hasRole('ROLE_NAME')`: Check if user has specific role
- `hasAuthority('AUTHORITY')`: Check if user has specific authority
- `isAuthenticated()`: Check if user is authenticated
- `#parameterName`: Reference method parameter
- `@beanName.methodName()`: Call method on Spring bean

## Endpoint Protection Examples

### UserController

```java
// Admin only
@PreAuthorize("hasRole('ADMIN')")
@GetMapping
public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() { ... }

// Admin only
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) { ... }

// User can update own profile OR admin can update any
@PreAuthorize("hasRole('ADMIN') or @securityUtils.ownsResource(#id)")
@PutMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponse>> updateUser(
    @PathVariable Long id,
    @RequestBody UserUpdateRequest request) { ... }

// Authenticated users only
@PreAuthorize("isAuthenticated()")
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) { ... }
```

### AuthController

```java
// Authenticated users only
@PreAuthorize("isAuthenticated()")
@GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() { ... }
```

## Security Configuration

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize
public class SecurityConfig {
    // ...
}
```

**Key Points:**

- `@EnableMethodSecurity(prePostEnabled = true)`: Enables `@PreAuthorize` and `@PostAuthorize` annotations
- Method-level security works alongside URL-based security rules

## Authorization Rules Summary

### Public Endpoints (No Authentication Required)

- `POST /api/v1/auth/login` - Login
- `POST /api/v1/users` - User registration
- `GET /api/v1/users/check/email` - Check email exists
- `GET /api/v1/users/check/username` - Check username exists

### Authenticated Users Only

- `GET /api/v1/users/{id}` - Get user by ID
- `GET /api/v1/users/{id}/profile` - Get user profile
- `GET /api/v1/auth/me` - Get current user
- `POST /api/v1/auth/validate` - Validate token

### Admin Only

- `GET /api/v1/users` - Get all users
- `DELETE /api/v1/users/{id}` - Delete user

### User Own Resource OR Admin

- `PUT /api/v1/users/{id}` - Update user (user can update own profile, admin can update any)
- `POST /api/v1/users/{id}/verify` - Verify email (user can verify own email, admin can verify any)

## Testing Authorization

### Test Admin Access

```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier": "admin@example.com", "password": "password"}'

# Use token in subsequent requests
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <token>"
```

### Test User Access

```bash
# Login as regular user
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier": "user@example.com", "password": "password"}'

# Try to access admin endpoint (should fail with 403)
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <token>"
```

## Error Responses

### 401 Unauthorized

Returned when:

- No authentication token provided
- Invalid or expired token

### 403 Forbidden

Returned when:

- User is authenticated but doesn't have required role
- User tries to access resource they don't own (and is not admin)

## Best Practices

1. **Always use method-level security** for fine-grained control
2. **Combine URL-based and method-based security** for defense in depth
3. **Use SpEL expressions** for complex authorization logic
4. **Test authorization** for all roles and scenarios
5. **Log authorization failures** for security monitoring
6. **Default to deny** - only allow what's explicitly permitted

## Future Enhancements

- [ ] Add more granular roles (MODERATOR, MANAGER, etc.)
- [ ] Implement permission-based access control (PBAC)
- [ ] Add resource-level permissions
- [ ] Implement role hierarchy
- [ ] Add audit logging for authorization decisions
