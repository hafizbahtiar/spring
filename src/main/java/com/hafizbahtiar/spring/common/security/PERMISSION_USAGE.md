# Permission System Usage Guide

## Overview

The permission system integrates with Spring Security's `@PreAuthorize` annotations to provide fine-grained access control. It supports two layers:

- **Layer 1**: Static roles (OWNER, ADMIN, USER) - checked first
- **Layer 2**: Dynamic group permissions - checked if Layer 1 doesn't grant access

## Backward Compatibility

✅ **All existing `@PreAuthorize` annotations continue to work unchanged:**
- `@PreAuthorize("hasRole('OWNER')")` - Still works
- `@PreAuthorize("hasRole('ADMIN')")` - Still works
- `@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")` - Still works
- `@PreAuthorize("@securityUtils.ownsResource(#id)")` - Still works

## Using Permission Checks

### Basic Pattern

Use `@PreAuthorize` with `SecurityService` methods:

```java
@PreAuthorize("@securityUtils.hasPermission('MODULE', 'support', 'chat', 'READ')")
@GetMapping("/chat")
public ResponseEntity<?> getChatMessages() {
    // ...
}
```

### Available Methods

#### 1. Check Specific Permission

```java
@PreAuthorize("@securityUtils.hasPermission('MODULE', 'support', 'chat', 'READ')")
```

Parameters:
- `permissionType`: `"MODULE"`, `"PAGE"`, or `"COMPONENT"`
- `resourceType`: Resource type (e.g., `"support"`, `"finance"`)
- `resourceIdentifier`: Resource identifier (e.g., `"chat"`, `"tickets"`, `"edit_button"`)
- `action`: `"READ"`, `"WRITE"`, `"DELETE"`, or `"EXECUTE"`

#### 2. Check Module Access

```java
@PreAuthorize("@securityUtils.hasModuleAccess('support')")
```

#### 3. Check Page Access

```java
@PreAuthorize("@securityUtils.hasPageAccess('support', 'support.chat')")
```

#### 4. Check Component Access

```java
@PreAuthorize("@securityUtils.hasComponentAccess('support.chat', 'edit_button')")
```

## Examples

### Example 1: Module-Level Permission

```java
@RestController
@RequestMapping("/api/v1/support")
public class SupportController {

    // Anyone with READ access to support module can view tickets
    @PreAuthorize("@securityUtils.hasModuleAccess('support')")
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponse>> getTickets() {
        // ...
    }

    // Requires WRITE permission for support module
    @PreAuthorize("@securityUtils.hasPermission('MODULE', 'support', 'tickets', 'WRITE')")
    @PostMapping("/tickets")
    public ResponseEntity<TicketResponse> createTicket(@RequestBody CreateTicketRequest request) {
        // ...
    }
}
```

### Example 2: Page-Level Permission

```java
@RestController
@RequestMapping("/api/v1/support")
public class SupportController {

    // Requires access to support.chat page
    @PreAuthorize("@securityUtils.hasPageAccess('support', 'support.chat')")
    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> getChat() {
        // ...
    }
}
```

### Example 3: Component-Level Permission

```java
@RestController
@RequestMapping("/api/v1/support")
public class SupportController {

    // Requires access to edit_button component on support.chat page
    @PreAuthorize("@securityUtils.hasComponentAccess('support.chat', 'edit_button')")
    @PutMapping("/chat/messages/{id}")
    public ResponseEntity<MessageResponse> editMessage(
            @PathVariable Long id,
            @RequestBody EditMessageRequest request) {
        // ...
    }
}
```

### Example 4: Combining with Static Roles

```java
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    // OWNER/ADMIN OR users with finance module READ access
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasModuleAccess('finance')")
    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponse>> getReports() {
        // ...
    }
}
```

### Example 5: Multiple Conditions

```java
@RestController
@RequestMapping("/api/v1/support")
public class SupportController {

    // Must be authenticated AND have support module access
    @PreAuthorize("isAuthenticated() and @securityUtils.hasModuleAccess('support')")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        // ...
    }
}
```

## Permission Hierarchy

The permission system supports automatic inheritance:

1. **MODULE permission** → Grants access to all pages and components in that module
2. **PAGE permission** → Grants access to all components on that page
3. **COMPONENT permission** → Grants access only to that specific component

Example:
- If user has `MODULE: support: *: READ`, they automatically have access to:
  - All pages in support module (e.g., `support.chat`, `support.tickets`)
  - All components in those pages

## Best Practices

1. **Use the most specific permission level needed:**
   - Prefer COMPONENT-level for fine-grained control
   - Use MODULE-level for broad access

2. **Combine with static roles when appropriate:**
   ```java
   @PreAuthorize("hasRole('OWNER') or @securityUtils.hasModuleAccess('support')")
   ```

3. **Use action levels appropriately:**
   - `READ`: View/list operations
   - `WRITE`: Create/update operations
   - `DELETE`: Delete operations
   - `EXECUTE`: Special operations (export, run reports, etc.)

4. **Document permission requirements:**
   - Add comments explaining why specific permissions are required
   - Use the documentation annotations (`@HasPermission`, etc.) for reference

## Migration Guide

### Before (Static Roles Only)

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/reports")
public ResponseEntity<?> getReports() {
    // ...
}
```

### After (With Group Permissions)

```java
// Option 1: Keep static role check (backward compatible)
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
@GetMapping("/reports")
public ResponseEntity<?> getReports() {
    // ...
}

// Option 2: Add group permission support
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasModuleAccess('finance')")
@GetMapping("/reports")
public ResponseEntity<?> getReports() {
    // ...
}

// Option 3: Use only group permissions (if you want fine-grained control)
@PreAuthorize("@securityUtils.hasPermission('MODULE', 'finance', 'reports', 'READ')")
@GetMapping("/reports")
public ResponseEntity<?> getReports() {
    // ...
}
```

## Notes

- **OWNER role**: Always has all permissions (checked first in Layer 1)
- **Performance**: Permission checks are cached in Redis (configurable TTL)
- **Error handling**: Failed permission checks return 403 Forbidden
- **Logging**: All permission checks are logged to MongoDB for audit purposes

