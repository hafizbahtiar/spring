# Authorization & Role-Based Access Control (RBAC) - Complete Guide

## 📚 Table of Contents

1. [What is Authorization?](#what-is-authorization)
2. [What is a Principal?](#what-is-a-principal)
3. [Dependencies Used](#dependencies-used)
4. [Complete Flow with File Names](#complete-flow-with-file-names)
5. [Step-by-Step Explanation](#step-by-step-explanation)
6. [Code Examples](#code-examples)
7. [Common Questions](#common-questions)

---

## What is Authorization?

**Authorization** is the process of determining **what a user is allowed to do** after they have been authenticated (logged in).

### Simple Analogy

Think of a building:
- **Authentication** = Showing your ID card at the entrance (proving who you are)
- **Authorization** = Having the right key card to access specific floors (determining what you can do)

### In Our Application

- **Authentication**: User logs in with email/password → Gets JWT token
- **Authorization**: User tries to delete a user → System checks: "Are you an ADMIN?" → Allows or denies

---

## What is a Principal?

### Definition

A **Principal** is an object that represents the **currently authenticated user** in Spring Security. It contains all the information about the user that the security system needs.

### Think of it as...

A **Principal** is like a **user profile card** that contains:
- User ID
- Username/Email
- Role (USER or ADMIN)
- Permissions/Authorities

### In Our Code

We created `UserPrincipal` class that implements Spring Security's `UserDetails` interface. This is our custom "user profile card" that stores:

```java
public class UserPrincipal {
    private Long id;           // User's unique ID
    private String username;   // User's username
    private String email;      // User's email
    private String role;       // USER or ADMIN
    private Collection<GrantedAuthority> authorities; // Permissions
}
```

### Why Do We Need It?

Spring Security needs a standard way to represent users. By creating `UserPrincipal`, we:
1. Store user information from JWT token
2. Provide it to Spring Security
3. Use it in authorization checks

---

## Dependencies Used

### 1. Spring Security (`spring-boot-starter-security`)

**Location**: `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**What it provides:**
- Security framework for authentication and authorization
- `@PreAuthorize` annotation support
- `SecurityContext` for storing authentication
- `UserDetails` interface for user representation

**Why we need it:**
- Without this, we can't use `@PreAuthorize` annotations
- Provides the security infrastructure

### 2. JWT Dependencies (Already Installed)

**Location**: `pom.xml`

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
```

**What it provides:**
- JWT token creation and validation
- Token claims extraction (userId, role, etc.)

**Why we need it:**
- We extract user information from JWT tokens to create `UserPrincipal`

### 3. No Additional Dependencies Needed!

The authorization system uses only what's already installed. Spring Security's method-level security is included in `spring-boot-starter-security`.

---

## Complete Flow with File Names

### Overview Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    AUTHORIZATION FLOW                           │
└─────────────────────────────────────────────────────────────────┘

1. USER LOGIN
   └─> AuthServiceImpl.java (login method)
       └─> JwtTokenProvider.java (generateToken)
           └─> Returns JWT token with userId, email, role

2. USER MAKES REQUEST
   └─> Request includes: Authorization: Bearer <token>

3. JWT FILTER INTERCEPTS
   └─> JwtAuthenticationFilter.java (doFilterInternal)
       └─> Extracts token from header
       └─> Validates token
       └─> Extracts userId, username, role
       └─> Creates UserPrincipal.java
       └─> Sets authentication in SecurityContext

4. REQUEST REACHES CONTROLLER
   └─> UserController.java or AuthController.java
       └─> @PreAuthorize annotation checked
           └─> SecurityService.java (ownsResource method)
               └─> Gets UserPrincipal from SecurityContext
               └─> Checks role or ownership
               └─> Returns true/false

5. AUTHORIZATION RESULT
   └─> If true: Method executes
   └─> If false: 403 Forbidden error
```

---

## Step-by-Step Explanation

### Step 1: User Login (Authentication)

**File**: `features/auth/service/AuthServiceImpl.java`

```java
public LoginResponse login(LoginRequest request) {
    // 1. Find user by email/username
    User user = userRepository.findByEmailOrUsernameAndActive(...)
    
    // 2. Validate password
    if (!passwordEncoder.matches(...)) {
        throw InvalidCredentialsException...
    }
    
    // 3. Generate JWT token with user info
    String token = jwtTokenProvider.generateToken(
        user.getEmail(),    // username
        user.getId(),       // userId
        user.getRole()      // role (USER or ADMIN)
    );
    
    // 4. Return token to client
    return LoginResponse.builder().token(token).build();
}
```

**What happens:**
- User provides email/password
- System validates credentials
- System creates JWT token containing: `{userId: 123, email: "user@example.com", role: "USER"}`
- Token is returned to client

**JWT Token Structure:**
```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "user@example.com",  // subject (username)
  "userId": 123,
  "role": "USER",
  "iat": 1234567890,          // issued at
  "exp": 1234654290            // expiration
}
Signature: <encrypted signature>
```

---

### Step 2: User Makes Protected Request

**Example Request:**
```bash
PUT /api/v1/users/123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**What the client sends:**
- HTTP method and URL
- JWT token in `Authorization` header

---

### Step 3: JWT Filter Intercepts Request

**File**: `security/JwtAuthenticationFilter.java`

**Method**: `doFilterInternal()`

```java
protected void doFilterInternal(HttpServletRequest request, ...) {
    // 1. Extract token from header
    String jwt = getJwtFromRequest(request);
    // Result: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    
    // 2. Validate token
    if (tokenProvider.validateToken(jwt)) {
        
        // 3. Extract user information from token
        String username = tokenProvider.getUsernameFromToken(jwt);
        // Result: "user@example.com"
        
        Long userId = tokenProvider.getUserIdFromToken(jwt);
        // Result: 123
        
        String role = tokenProvider.getRoleFromToken(jwt);
        // Result: "USER"
        
        // 4. Create UserPrincipal object
        UserPrincipal userPrincipal = UserPrincipal.create(
            userId,    // 123
            username,  // "user@example.com"
            username,  // email (same as username)
            role       // "USER"
        );
        
        // 5. Create Authentication object
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userPrincipal,              // Principal (the user)
                null,                       // Credentials (not needed)
                userPrincipal.getAuthorities() // Authorities: ["ROLE_USER"]
            );
        
        // 6. Store in SecurityContext
        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
    
    // 7. Continue to next filter/controller
    filterChain.doFilter(request, response);
}
```

**What happens:**
1. Filter extracts token from `Authorization` header
2. Validates token (not expired, valid signature)
3. Extracts user info from token payload
4. Creates `UserPrincipal` object with user data
5. Creates `Authentication` object with `UserPrincipal` and authorities
6. Stores `Authentication` in `SecurityContext` (like a global variable)
7. Request continues to controller

**Key Concept**: `SecurityContext` is like a **global storage** that holds the current user's authentication information. Any code can access it.

---

### Step 4: Request Reaches Controller

**File**: `features/user/controller/v1/UserController.java`

**Example Method:**
```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN') or @securityUtils.ownsResource(#id)")
public ResponseEntity<ApiResponse<UserResponse>> updateUser(
    @PathVariable Long id,
    @RequestBody UserUpdateRequest request) {
    
    // Method body executes only if @PreAuthorize returns true
    UserResponse response = userService.updateUser(id, request);
    return ResponseUtils.ok(response);
}
```

**What happens:**
1. Request reaches `updateUser` method
2. **BEFORE** method executes, Spring Security checks `@PreAuthorize`
3. Evaluates expression: `hasRole('ADMIN') or @securityUtils.ownsResource(#id)`
4. If expression is `true` → method executes
5. If expression is `false` → throws `AccessDeniedException` (403 Forbidden)

---

### Step 5: @PreAuthorize Expression Evaluation

**File**: `common/security/SecurityService.java`

**Bean Name**: `securityUtils` (used in SpEL expressions)

When `@PreAuthorize` evaluates `@securityUtils.ownsResource(#id)`:

```java
public boolean ownsResource(Long resourceUserId) {
    // 1. Get current authentication from SecurityContext
    Authentication authentication = SecurityContextHolder
        .getContext()
        .getAuthentication();
    
    // 2. Get UserPrincipal from authentication
    Object principal = authentication.getPrincipal();
    // principal is UserPrincipal object created in Step 3
    
    if (principal instanceof UserPrincipal userPrincipal) {
        // 3. Check if user is admin OR owns the resource
        return userPrincipal.isAdmin() || 
               userPrincipal.ownsResource(resourceUserId);
    }
    
    return false;
}
```

**Example Scenario:**
- Request: `PUT /api/v1/users/123`
- Current user ID: `123` (from UserPrincipal)
- Resource user ID: `123` (from `#id` parameter)
- Check: `userPrincipal.ownsResource(123)` → `123 == 123` → `true`
- Result: Authorization granted ✅

**Another Scenario:**
- Request: `PUT /api/v1/users/456`
- Current user ID: `123` (from UserPrincipal)
- Resource user ID: `456` (from `#id` parameter)
- Check: `userPrincipal.ownsResource(456)` → `123 != 456` → `false`
- Check: `userPrincipal.isAdmin()` → `false` (user is not admin)
- Result: Authorization denied ❌ (403 Forbidden)

---

### Step 6: Authorization Result

**If Authorized (true):**
```
@PreAuthorize returns true
    ↓
Method executes
    ↓
UserService.updateUser() called
    ↓
Response returned to client
```

**If Not Authorized (false):**
```
@PreAuthorize returns false
    ↓
AccessDeniedException thrown
    ↓
GlobalExceptionHandler catches it
    ↓
403 Forbidden response returned
    ↓
Client receives error
```

---

## Code Examples

### Example 1: Admin-Only Endpoint

**File**: `features/user/controller/v1/UserController.java`

```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
    // This method only executes if user has ROLE_ADMIN
    List<UserResponse> users = userService.getAllUsers();
    return ResponseUtils.ok(users);
}
```

**Flow:**
1. User makes request: `GET /api/v1/users`
2. JWT filter sets `UserPrincipal` with role "USER"
3. `@PreAuthorize("hasRole('ADMIN')")` checks: Does user have `ROLE_ADMIN`?
4. User has `ROLE_USER` → `false`
5. 403 Forbidden returned

**If user is ADMIN:**
1. JWT filter sets `UserPrincipal` with role "ADMIN"
2. `@PreAuthorize("hasRole('ADMIN')")` checks: Does user have `ROLE_ADMIN`?
3. User has `ROLE_ADMIN` → `true`
4. Method executes ✅

---

### Example 2: Own Resource OR Admin

**File**: `features/user/controller/v1/UserController.java`

```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN') or @securityUtils.ownsResource(#id)")
public ResponseEntity<ApiResponse<UserResponse>> updateUser(
    @PathVariable Long id,
    @RequestBody UserUpdateRequest request) {
    
    // Method executes if:
    // 1. User is ADMIN, OR
    // 2. User owns the resource (id matches user's ID)
    
    UserResponse response = userService.updateUser(id, request);
    return ResponseUtils.ok(response);
}
```

**Scenario A: User updates own profile**
- Request: `PUT /api/v1/users/123`
- Current user ID: `123`
- Resource ID: `123`
- Check: `hasRole('ADMIN')` → `false`
- Check: `@securityUtils.ownsResource(123)` → `true` (123 == 123)
- Result: `false OR true` → `true` ✅
- Method executes

**Scenario B: Admin updates any user**
- Request: `PUT /api/v1/users/456`
- Current user ID: `999` (admin)
- Resource ID: `456`
- Check: `hasRole('ADMIN')` → `true`
- Result: `true OR ...` → `true` ✅
- Method executes (doesn't even check ownership)

**Scenario C: User tries to update another user**
- Request: `PUT /api/v1/users/456`
- Current user ID: `123`
- Resource ID: `456`
- Check: `hasRole('ADMIN')` → `false`
- Check: `@securityUtils.ownsResource(456)` → `false` (123 != 456)
- Result: `false OR false` → `false` ❌
- 403 Forbidden returned

---

### Example 3: Authenticated Users Only

**File**: `features/auth/controller/v1/AuthController.java`

```java
@GetMapping("/me")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
    // This method only executes if user is authenticated
    UserResponse user = authService.getCurrentUser();
    return ResponseUtils.ok(user);
}
```

**Flow:**
1. User makes request: `GET /api/v1/auth/me`
2. JWT filter validates token and sets authentication
3. `@PreAuthorize("isAuthenticated()")` checks: Is user authenticated?
4. User is authenticated → `true` ✅
5. Method executes

**If no token provided:**
1. User makes request without `Authorization` header
2. JWT filter doesn't set authentication
3. `@PreAuthorize("isAuthenticated()")` checks: Is user authenticated?
4. User is NOT authenticated → `false` ❌
5. 401 Unauthorized returned

---

## File Structure Overview

```
src/main/java/com/hafizbahtiar/spring/
│
├── config/
│   └── SecurityConfig.java              # Enables method-level security
│
├── security/
│   ├── JwtTokenProvider.java            # Creates/validates JWT tokens
│   └── JwtAuthenticationFilter.java     # Intercepts requests, creates UserPrincipal
│
├── common/security/
│   ├── Role.java                        # USER, ADMIN enum
│   ├── UserPrincipal.java               # User representation (Principal)
│   ├── SecurityUtils.java               # Static utility methods
│   ├── SecurityService.java             # Bean for SpEL expressions
│   └── README.md                        # Documentation
│
├── features/user/controller/v1/
│   └── UserController.java              # Endpoints with @PreAuthorize
│
└── features/auth/controller/v1/
    └── AuthController.java               # Endpoints with @PreAuthorize
```

---

## Key Concepts Explained

### 1. Principal vs Authentication vs SecurityContext

**Principal** (`UserPrincipal`):
- The **user object** itself
- Contains: id, username, email, role, authorities
- Like a "user profile card"

**Authentication**:
- A **wrapper** around the Principal
- Contains: Principal, credentials, authorities
- Like a "security badge" that includes the profile card

**SecurityContext**:
- A **storage container** for Authentication
- Global storage accessible from anywhere
- Like a "security checkpoint" that holds the badge

**Flow:**
```
UserPrincipal (user data)
    ↓
Authentication (wraps Principal)
    ↓
SecurityContext (stores Authentication)
    ↓
@PreAuthorize accesses SecurityContext to get Authentication to get Principal
```

### 2. Authorities vs Roles

**Authorities**:
- Fine-grained permissions
- Example: `READ_USER`, `WRITE_USER`, `DELETE_USER`
- Format: `"READ_USER"`

**Roles**:
- Coarse-grained permissions
- Example: `USER`, `ADMIN`
- Format: `"ROLE_USER"`, `"ROLE_ADMIN"`

**In our code:**
- We use **Roles** (simpler)
- Spring Security automatically prefixes with `ROLE_`
- `hasRole('ADMIN')` checks for `ROLE_ADMIN` authority

### 3. SpEL (Spring Expression Language)

**What is SpEL?**
- A language for querying and manipulating objects
- Used in `@PreAuthorize` annotations

**Common SpEL Expressions:**

| Expression | Meaning | Example |
|------------|---------|---------|
| `hasRole('ADMIN')` | Check if user has role | `hasRole('ADMIN')` |
| `isAuthenticated()` | Check if user is logged in | `isAuthenticated()` |
| `#parameterName` | Reference method parameter | `#id` refers to `@PathVariable Long id` |
| `@beanName.method()` | Call Spring bean method | `@securityUtils.ownsResource(#id)` |
| `or` | Logical OR | `hasRole('ADMIN') or ...` |
| `and` | Logical AND | `hasRole('ADMIN') and ...` |

---

## Common Questions

### Q1: Why do we need UserPrincipal? Can't we just use the User entity?

**Answer:**
- `UserPrincipal` implements `UserDetails` interface (Spring Security requirement)
- It's lightweight (only security-related fields)
- It's stored in memory (not database)
- It's created from JWT token (no database query needed)
- `User` entity is for database operations, `UserPrincipal` is for security

### Q2: Why do we need SecurityService? Can't we use SecurityUtils directly?

**Answer:**
- `SecurityUtils` has static methods
- `@PreAuthorize` SpEL expressions can't call static methods directly
- SpEL can only call Spring bean methods using `@beanName.method()`
- `SecurityService` is a Spring bean, so we can use `@securityUtils.ownsResource()`

### Q3: What happens if JWT token is expired?

**Answer:**
1. `JwtAuthenticationFilter` calls `tokenProvider.validateToken(jwt)`
2. Validation checks expiration date
3. If expired → validation returns `false`
4. Authentication is NOT set in SecurityContext
5. `@PreAuthorize("isAuthenticated()")` returns `false`
6. 401 Unauthorized returned

### Q4: Can a user change their role in the JWT token?

**Answer:**
- **No!** JWT tokens are **signed** with a secret key
- If user modifies the token, signature becomes invalid
- `validateToken()` will fail
- Token will be rejected
- User must login again to get a new token

### Q5: What's the difference between 401 and 403?

**Answer:**
- **401 Unauthorized**: User is NOT authenticated (no token or invalid token)
- **403 Forbidden**: User IS authenticated but doesn't have permission

**Example:**
- No token → 401
- Invalid token → 401
- Valid token, but user tries admin endpoint → 403

### Q6: How does Spring Security know which user is making the request?

**Answer:**
1. Request includes JWT token in header
2. `JwtAuthenticationFilter` extracts token
3. Validates and extracts user info
4. Creates `UserPrincipal` with user ID
5. Stores in `SecurityContext`
6. `@PreAuthorize` reads from `SecurityContext`

### Q7: What if multiple requests come at the same time?

**Answer:**
- Each request has its own thread
- Each thread has its own `SecurityContext`
- `SecurityContext` is stored in `ThreadLocal`
- No interference between requests ✅

---

## Summary

### What We Built

1. **Role-Based Access Control (RBAC)**
   - Two roles: USER and ADMIN
   - Admin can do everything
   - Users can only modify their own resources

2. **Method-Level Security**
   - `@PreAuthorize` annotations on endpoints
   - Fine-grained control per endpoint

3. **Principal System**
   - `UserPrincipal` represents authenticated user
   - Created from JWT token
   - Stored in SecurityContext

4. **Authorization Flow**
   - Login → JWT token
   - Request → JWT filter → UserPrincipal → SecurityContext
   - Controller → @PreAuthorize → Check → Allow/Deny

### Key Files

| File | Purpose |
|------|---------|
| `SecurityConfig.java` | Enables method-level security |
| `JwtAuthenticationFilter.java` | Creates UserPrincipal from token |
| `UserPrincipal.java` | Represents authenticated user |
| `SecurityService.java` | Bean for SpEL expressions |
| `UserController.java` | Endpoints with @PreAuthorize |
| `AuthController.java` | Endpoints with @PreAuthorize |

### Dependencies

- `spring-boot-starter-security` - Security framework
- `jjwt-api` - JWT token handling (already installed)

That's it! No additional dependencies needed.

---

## Next Steps

1. **Test the authorization**:
   - Login as regular user
   - Try to access admin endpoints (should fail)
   - Try to update own profile (should succeed)
   - Try to update another user's profile (should fail)

2. **Add more roles** (if needed):
   - Add to `Role.java` enum
   - Use in `@PreAuthorize` annotations

3. **Add more fine-grained permissions** (if needed):
   - Create permission-based system
   - Use `hasAuthority()` instead of `hasRole()`

---

**End of Documentation**

