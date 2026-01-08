# Role-Based Access Control (RBAC) System - Complete Documentation

## Overview

This document describes the two-layer RBAC system for the application:

1. **Layer 1: Static Roles** (Current Implementation) - Fixed roles with predefined permissions
2. **Layer 2: Dynamic Groups & Permissions** (Proposed) - Flexible, configurable permissions system

---

## Layer 1: Static Roles (Current System)

### Role Hierarchy

```
OWNER (Unique - Only 1 user)
  └─ Can do everything (full system access)
  
ADMIN (Multiple users)
  └─ Can do admin tasks but NOT owner-specific tasks
  └─ Cannot access owner-only features (e.g., portfolio management)
  
USER (Default role)
  └─ Standard user with basic permissions
```

### Current Role Definitions

| Role | Count | Permissions | Notes |
|------|-------|-------------|-------|
| **OWNER** | 1 (unique) | Full system access | Primary user, can manage everything |
| **ADMIN** | Multiple | Admin features only | Assistants/help staff, cannot access owner features |
| **USER** | Multiple | Basic user features | Default role for regular users |

### Current Implementation

- **Entity**: `User.role` (String, max 20 chars)
- **Enum**: `Role.java` (USER, OWNER, ADMIN)
- **Authorization**: `@PreAuthorize` annotations with `hasRole()`, `hasAnyRole()`
- **Security**: `UserPrincipal` with role-based authorities

---

## Layer 2: Dynamic Groups & Permissions (Proposed System)

### Concept

A flexible permission system where **OWNER/ADMIN** can create **Groups** and assign fine-grained permissions at three levels:

1. **Module Level** - Access to entire feature modules (e.g., "Support Module", "Finance Module")
2. **Page Level** - Access to specific pages within modules (e.g., "Support Chat Page", "Support Tickets Page")
3. **Component Level** - Access to specific UI components/actions (e.g., "Edit Button", "Delete Button", "Export Button")

### Key Principles

1. **Creator Access Restriction**: Groups can only include modules/pages/components that the creator (OWNER/ADMIN) has access to
2. **Hierarchical Permissions**: Module access implies page access, page access implies component access (unless explicitly denied)
3. **User Assignment**: Multiple users can be assigned to a group
4. **Multiple Groups**: Users can belong to multiple groups (permissions are combined with OR logic)
5. **Static Role Priority**: Static roles (OWNER/ADMIN) always take precedence over group permissions

---

## Architecture Design

### Database Schema

#### 1. `permission_groups` Table

```sql
CREATE TABLE permission_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    UNIQUE(name)
);

CREATE INDEX idx_permission_groups_created_by ON permission_groups(created_by);
CREATE INDEX idx_permission_groups_active ON permission_groups(active);
```

#### 2. `group_permissions` Table

```sql
CREATE TABLE group_permissions (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES permission_groups(id) ON DELETE CASCADE,
    permission_type VARCHAR(20) NOT NULL, -- 'MODULE', 'PAGE', 'COMPONENT'
    resource_type VARCHAR(50) NOT NULL,   -- e.g., 'support', 'finance', 'portfolio'
    resource_identifier VARCHAR(200) NOT NULL, -- e.g., 'chat', 'tickets', 'edit_button'
    action VARCHAR(20) NOT NULL,           -- 'READ', 'WRITE', 'DELETE', 'EXECUTE'
    granted BOOLEAN NOT NULL DEFAULT true, -- true = allow, false = deny
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(group_id, permission_type, resource_type, resource_identifier, action)
);

CREATE INDEX idx_group_permissions_group_id ON group_permissions(group_id);
CREATE INDEX idx_group_permissions_resource ON group_permissions(resource_type, resource_identifier);
```

#### 3. `user_groups` Table (Many-to-Many)

```sql
CREATE TABLE user_groups (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES permission_groups(id) ON DELETE CASCADE,
    assigned_by BIGINT NOT NULL REFERENCES users(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, group_id)
);

CREATE INDEX idx_user_groups_user_id ON user_groups(user_id);
CREATE INDEX idx_user_groups_group_id ON user_groups(group_id);
```

#### 4. `permission_modules` Table (Reference/Registry)

```sql
CREATE TABLE permission_modules (
    id BIGSERIAL PRIMARY KEY,
    module_key VARCHAR(50) NOT NULL UNIQUE, -- e.g., 'support', 'finance', 'portfolio'
    module_name VARCHAR(100) NOT NULL,
    description TEXT,
    available_to_roles VARCHAR(50) NOT NULL, -- 'OWNER', 'ADMIN', 'OWNER,ADMIN'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permission_modules_key ON permission_modules(module_key);
```

#### 5. `permission_pages` Table (Reference/Registry)

```sql
CREATE TABLE permission_pages (
    id BIGSERIAL PRIMARY KEY,
    module_key VARCHAR(50) NOT NULL REFERENCES permission_modules(module_key),
    page_key VARCHAR(100) NOT NULL, -- e.g., 'chat', 'tickets', 'dashboard'
    page_name VARCHAR(200) NOT NULL,
    route_path VARCHAR(500), -- e.g., '/support/chat', '/support/tickets'
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(module_key, page_key)
);

CREATE INDEX idx_permission_pages_module ON permission_pages(module_key);
```

#### 6. `permission_components` Table (Reference/Registry)

```sql
CREATE TABLE permission_components (
    id BIGSERIAL PRIMARY KEY,
    page_key VARCHAR(100) NOT NULL, -- References permission_pages.page_key
    component_key VARCHAR(100) NOT NULL, -- e.g., 'edit_button', 'delete_button', 'export_button'
    component_name VARCHAR(200) NOT NULL,
    component_type VARCHAR(50) NOT NULL, -- 'BUTTON', 'LINK', 'MENU_ITEM', 'TAB'
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(page_key, component_key)
);

CREATE INDEX idx_permission_components_page ON permission_components(page_key);
```

---

## Entity Models

### 1. PermissionGroup Entity

```java
@Entity
@Table(name = "permission_groups")
public class PermissionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupPermission> permissions = new ArrayList<>();
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGroup> userGroups = new ArrayList<>();
    
    // Getters, setters, business methods...
}
```

### 2. GroupPermission Entity

```java
@Entity
@Table(name = "group_permissions")
public class GroupPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PermissionGroup group;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionType permissionType; // MODULE, PAGE, COMPONENT
    
    @Column(nullable = false, length = 50)
    private String resourceType; // 'support', 'finance', etc.
    
    @Column(nullable = false, length = 200)
    private String resourceIdentifier; // 'chat', 'tickets', 'edit_button', etc.
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionAction action; // READ, WRITE, DELETE, EXECUTE
    
    @Column(nullable = false)
    private Boolean granted = true; // true = allow, false = deny
    
    // Getters, setters...
}
```

### 3. UserGroup Entity (Join Table)

```java
@Entity
@Table(name = "user_groups")
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PermissionGroup group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;
    
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime assignedAt;
    
    // Getters, setters...
}
```

### 4. Enums

```java
public enum PermissionType {
    MODULE,    // Access to entire module
    PAGE,      // Access to specific page
    COMPONENT  // Access to specific component
}

public enum PermissionAction {
    READ,      // View/read access
    WRITE,     // Create/update access
    DELETE,    // Delete access
    EXECUTE    // Execute/run access (e.g., export, run report)
}
```

---

## Permission Evaluation Logic

### Permission Check Flow

```
1. Check Static Role (Layer 1)
   ├─ If OWNER → Allow (full access)
   ├─ If ADMIN → Check if feature is admin-accessible
   └─ If USER → Continue to Layer 2

2. Check Group Permissions (Layer 2)
   ├─ Get all groups for user
   ├─ For each group, check permissions
   ├─ Check hierarchy: MODULE → PAGE → COMPONENT
   └─ Combine with OR logic (if any group allows, user has access)

3. Final Decision
   ├─ If static role allows → Allow
   ├─ If any group allows → Allow
   └─ Otherwise → Deny
```

### Permission Hierarchy Rules

1. **Module Permission** grants access to all pages and components within that module
2. **Page Permission** grants access to all components within that page
3. **Component Permission** grants access only to that specific component
4. **Deny Permission** (granted = false) explicitly denies access, overriding any allow permissions

### Example: "Support" Group

**Group Configuration:**
- **Name**: "Support"
- **Module**: `support` (READ, WRITE)
- **Page**: `support.chat` (READ, WRITE)
- **Component**: `support.chat.delete_button` (DENY - cannot delete messages)
- **Users**: [user1, user2, user3]

**Permission Evaluation:**
- User can access Support Module → ✅
- User can access Support Chat Page → ✅
- User can read/write chat messages → ✅
- User CANNOT delete chat messages → ❌ (explicitly denied)

---

## API Endpoints (Proposed)

### Group Management

```
POST   /api/v1/permissions/groups              - Create group (OWNER/ADMIN only)
GET    /api/v1/permissions/groups               - List all groups (OWNER/ADMIN only)
GET    /api/v1/permissions/groups/{id}          - Get group details (OWNER/ADMIN only)
PUT    /api/v1/permissions/groups/{id}          - Update group (OWNER/ADMIN only)
DELETE /api/v1/permissions/groups/{id}          - Delete group (OWNER/ADMIN only)
```

### Permission Management

```
POST   /api/v1/permissions/groups/{groupId}/permissions  - Add permission to group
GET    /api/v1/permissions/groups/{groupId}/permissions  - List group permissions
PUT    /api/v1/permissions/groups/{groupId}/permissions/{id} - Update permission
DELETE /api/v1/permissions/groups/{groupId}/permissions/{id} - Remove permission
```

### User Assignment

```
POST   /api/v1/permissions/groups/{groupId}/users        - Assign user to group
GET    /api/v1/permissions/groups/{groupId}/users        - List group members
DELETE /api/v1/permissions/groups/{groupId}/users/{userId} - Remove user from group
GET    /api/v1/permissions/users/{userId}/groups         - Get user's groups
```

### Permission Registry

```
GET    /api/v1/permissions/modules              - List available modules
GET    /api/v1/permissions/modules/{key}/pages  - List pages in module
GET    /api/v1/permissions/pages/{key}/components - List components in page
```

### Permission Check

```
POST   /api/v1/permissions/check               - Check if user has permission
GET    /api/v1/permissions/me                  - Get current user's permissions
```

---

## Service Layer

### PermissionService

```java
public interface PermissionService {
    // Group Management
    PermissionGroup createGroup(CreateGroupRequest request, Long createdBy);
    PermissionGroup updateGroup(Long groupId, UpdateGroupRequest request);
    void deleteGroup(Long groupId);
    List<PermissionGroup> getAllGroups();
    PermissionGroup getGroupById(Long groupId);
    
    // Permission Management
    GroupPermission addPermission(Long groupId, AddPermissionRequest request);
    void removePermission(Long permissionId);
    List<GroupPermission> getGroupPermissions(Long groupId);
    
    // User Assignment
    void assignUserToGroup(Long groupId, Long userId, Long assignedBy);
    void removeUserFromGroup(Long groupId, Long userId);
    List<User> getGroupMembers(Long groupId);
    List<PermissionGroup> getUserGroups(Long userId);
    
    // Permission Evaluation
    boolean hasPermission(Long userId, PermissionType type, String resourceType, 
                         String resourceIdentifier, PermissionAction action);
    boolean hasModuleAccess(Long userId, String moduleKey);
    boolean hasPageAccess(Long userId, String moduleKey, String pageKey);
    boolean hasComponentAccess(Long userId, String pageKey, String componentKey);
    UserPermissionsResponse getUserPermissions(Long userId);
    
    // Registry
    List<PermissionModule> getAvailableModules(Long userId); // Only modules user can assign
    List<PermissionPage> getModulePages(String moduleKey);
    List<PermissionComponent> getPageComponents(String pageKey);
}
```

---

## Frontend Integration

### Permission Check Hook

```typescript
// hooks/usePermission.ts
export function usePermission() {
    const { user } = useAuth()
    
    const hasPermission = (
        type: 'MODULE' | 'PAGE' | 'COMPONENT',
        resourceType: string,
        resourceIdentifier: string,
        action: 'READ' | 'WRITE' | 'DELETE' | 'EXECUTE'
    ): boolean => {
        // Check static role first
        if (user?.role === 'OWNER') return true
        if (user?.role === 'ADMIN' && isAdminAccessible(resourceType)) return true
        
        // Check group permissions
        return checkGroupPermission(user?.id, type, resourceType, resourceIdentifier, action)
    }
    
    return { hasPermission }
}
```

### Component Usage

```typescript
// Example: Conditionally render delete button
function ChatPage() {
    const { hasPermission } = usePermission()
    
    return (
        <div>
            {hasPermission('COMPONENT', 'support', 'chat.delete_button', 'DELETE') && (
                <Button onClick={handleDelete}>Delete</Button>
            )}
        </div>
    )
}
```

---

## Implementation Phases

### Phase 1: Database & Entities (Week 1-2)
- [ ] Create database tables
- [ ] Create entity models
- [ ] Create repositories
- [ ] Create enums (PermissionType, PermissionAction)

### Phase 2: Service Layer (Week 2-3)
- [ ] Implement PermissionService
- [ ] Implement permission evaluation logic
- [ ] Add permission caching (Redis)
- [ ] Create permission registry initialization

### Phase 3: API Layer (Week 3-4)
- [ ] Create PermissionGroupController
- [ ] Create PermissionController
- [ ] Create DTOs
- [ ] Add validation and error handling

### Phase 4: Security Integration (Week 4-5)
- [ ] Update SecurityService to check group permissions
- [ ] Create custom @PreAuthorize expressions
- [ ] Update UserPrincipal to include group permissions
- [ ] Add permission checks to existing endpoints

### Phase 5: Frontend Integration (Week 5-6)
- [ ] Create permission API client
- [ ] Create usePermission hook
- [ ] Update components to use permission checks
- [ ] Create group management UI

### Phase 6: Testing & Documentation (Week 6-7)
- [ ] Unit tests
- [ ] Integration tests
- [ ] API documentation
- [ ] User guide

---

## Example Use Cases

### Use Case 1: Support Team Group

**Scenario**: Create a "Support Team" group that can access support chat but cannot delete messages.

**Steps:**
1. OWNER creates group "Support Team"
2. Add module permission: `support` (READ, WRITE)
3. Add page permission: `support.chat` (READ, WRITE)
4. Add component permission: `support.chat.delete_button` (DENY)
5. Assign users: [support_user1, support_user2]

**Result**: Support team members can read/write chat messages but cannot delete them.

### Use Case 2: Finance Viewer Group

**Scenario**: Create a "Finance Viewers" group that can only view finance data, not edit.

**Steps:**
1. ADMIN creates group "Finance Viewers"
2. Add module permission: `finance` (READ only)
3. Assign users: [finance_viewer1, finance_viewer2]

**Result**: Finance viewers can see all finance pages but cannot create/edit/delete anything.

### Use Case 3: Limited Admin Group

**Scenario**: Create a "Limited Admin" group that can access admin health/metrics but not cron jobs.

**Steps:**
1. OWNER creates group "Limited Admin"
2. Add module permission: `admin` (READ)
3. Add page permission: `admin.health` (READ, EXECUTE)
4. Add page permission: `admin.metrics` (READ, EXECUTE)
5. Explicitly deny: `admin.cron-jobs` (all actions)

**Result**: Limited admins can view health and metrics but cannot access cron jobs.

---

## Security Considerations

### 1. Creator Access Validation
- When creating a group, validate that creator has access to all selected modules/pages/components
- Prevent privilege escalation (users cannot grant permissions they don't have)

### 2. Permission Caching
- Cache user permissions in Redis for performance
- Invalidate cache when:
  - User is added/removed from group
  - Group permissions are modified
  - Group is deleted

### 3. Audit Logging
- Log all permission changes (MongoDB)
- Track who created/modified groups
- Track user assignments/removals

### 4. Default Permissions
- New users have no group permissions by default
- Only static role permissions apply
- Explicit assignment required

---

## Migration Strategy

### Step 1: Add Tables (Non-Breaking)
- Create new tables alongside existing system
- No changes to existing User entity or role system
- Backward compatible

### Step 2: Initialize Registry
- Populate `permission_modules`, `permission_pages`, `permission_components` tables
- Define all available modules/pages/components in the system

### Step 3: Gradual Rollout
- Start with new features using group permissions
- Keep existing features using static roles
- Migrate features one by one

### Step 4: Full Migration
- All features support group permissions
- Static roles still work (Layer 1)
- Groups provide additional flexibility (Layer 2)

---

## Questions & Decisions Needed

### 1. Permission Inheritance: Automatic or Explicit?

**Penjelasan (Bahasa Melayu):**
- **Automatic Inheritance (Warisan Automatik)**: Jika user dapat akses ke "Support Module", secara automatik dia dapat akses ke semua pages dan components dalam module tu.
- **Explicit Assignment (Tugasan Eksplisit)**: Mesti assign satu-satu untuk setiap page dan component, walaupun dah dapat module access.

**Contoh:**
```
Group: "Support Team"
├─ Module: support (READ, WRITE) ✅

Dengan Automatic Inheritance:
├─ Automatically dapat: support.chat (READ, WRITE) ✅
├─ Automatically dapat: support.tickets (READ, WRITE) ✅
└─ Automatically dapat: support.chat.edit_button (READ, WRITE) ✅

Dengan Explicit Assignment:
├─ Mesti assign: support.chat (READ, WRITE) ❌ (kalau tak assign, tak dapat)
├─ Mesti assign: support.tickets (READ, WRITE) ❌
└─ Mesti assign: support.chat.edit_button (READ, WRITE) ❌
```

**Keputusan: Automatic Inheritance** ✅
- Lebih mudah dan intuitif
- Kurang kerja untuk setup
- Kalau nak restrict specific page/component, boleh guna explicit deny

---

### 2. Deny vs Allow: Explicit Deny or "Not Granted = Denied"?

**Penjelasan (Bahasa Melayu):**
- **Explicit Deny (Tolak Eksplisit)**: Kalau kita set `granted = false`, user TIDAK boleh akses walaupun ada permission lain yang allow.
- **"Not Granted = Denied" (Tak Diberi = Ditolak)**: Kalau tak assign permission, automatik user tak dapat akses.

**Contoh:**
```
User dalam 2 groups:
- Group A: support.chat.delete_button (ALLOW) ✅
- Group B: support.chat.delete_button (DENY) ❌

Dengan Explicit Deny:
- Result: User TIDAK boleh delete (deny override allow) ❌

Tanpa Explicit Deny (hanya "not granted"):
- Result: User BOLEH delete (any group allow = allow) ✅
- Masalah: Tak boleh restrict specific action
```

**Keputusan: Support Both** ✅
- Default: "Not granted = denied" (lebih selamat)
- Explicit deny untuk kes-kes khas (override allow permissions)
- Contoh: Support team boleh chat tapi TIDAK boleh delete messages

---

### 3. Multiple Groups: How to Handle Conflicts?

**Penjelasan:**
User boleh berada dalam multiple groups. Kalau groups tu ada conflicting permissions, macam mana nak handle?

**Contoh Konflik:**

**Scenario 1: Conflicting Allow/Deny**
```
User: John
├─ Group A: "Support Team"
│   └─ support.chat.delete_button (ALLOW) ✅
└─ Group B: "Support Viewer"
    └─ support.chat.delete_button (DENY) ❌

Keputusan: DENY wins (explicit deny override allow)
Result: John TIDAK boleh delete messages ❌
```

**Scenario 2: Different Permissions**
```
User: Sarah
├─ Group A: "Finance Viewer"
│   └─ finance module (READ only) 👁️
└─ Group B: "Finance Editor"
    └─ finance module (READ, WRITE) ✏️

Keputusan: OR logic (combine permissions)
Result: Sarah dapat READ dan WRITE (highest permission wins) ✏️
```

**Scenario 3: Partial Access**
```
User: Mike
├─ Group A: "Support Team"
│   ├─ support.chat (READ, WRITE) ✅
│   └─ support.tickets (READ, WRITE) ✅
└─ Group B: "Support Chat Only"
    └─ support.chat (READ only) 👁️

Keputusan: OR logic (combine permissions)
Result: Mike dapat:
  - support.chat (READ, WRITE) ✅ (highest permission)
  - support.tickets (READ, WRITE) ✅ (from Group A)
```

**Keputusan: OR Logic dengan Deny Override** ✅
- **OR Logic**: Kalau mana-mana group allow, user dapat akses
- **Deny Override**: Explicit deny akan override semua allow permissions
- **Highest Permission**: Kalau multiple groups allow, ambil permission tertinggi (READ < WRITE < DELETE)

---

### 4. Permission Granularity: Resource-Level Permissions Needed?

**Penjelasan (Bahasa Melayu):**
- **Current System**: Module/Page/Component level (semua resources dalam page tu)
- **Resource-Level**: Boleh control access untuk specific resource (contoh: "boleh edit post sendiri sahaja")

**Contoh Perbezaan:**

**Tanpa Resource-Level (Current):**
```
Group: "Blog Editors"
└─ blog.edit_button (ALLOW) ✅

Result: User boleh edit SEMUA blog posts (termasuk post orang lain) ❌
Masalah: Tak boleh restrict untuk post sendiri sahaja
```

**Dengan Resource-Level:**
```
Group: "Blog Editors"
├─ blog.edit_button (ALLOW) ✅
└─ blog.edit_button.own_posts_only (RESOURCE_FILTER) ✅

Result: User boleh edit button, tapi hanya untuk post sendiri sahaja ✅
```

**Contoh Lain:**
- **Finance**: Boleh view transactions, tapi hanya untuk department sendiri
- **Support**: Boleh reply tickets, tapi hanya untuk tickets yang assigned kepada dia
- **Portfolio**: Boleh edit projects, tapi hanya projects sendiri

**Keputusan: Start Simple, Add Later** ✅
- **Phase 1**: Module/Page/Component level sahaja (lebih mudah)
- **Phase 2**: Add resource-level permissions kalau diperlukan
- **Reason**: Resource-level lebih complex, boleh tambah later based on actual needs

---

### 5. Frontend Checks: Frontend, Backend, or Both?

**Penjelasan (Bahasa Melayu):**
- **Frontend Check**: Check permission dalam React component untuk hide/show buttons
- **Backend Check**: Check permission dalam Spring Boot controller untuk security
- **Both**: Check kedua-dua tempat

**Kenapa Perlu Both:**

**Frontend Check (UX):**
```typescript
// Hide button kalau user tak ada permission
{hasPermission('COMPONENT', 'support', 'chat.delete_button', 'DELETE') && (
    <Button onClick={handleDelete}>Delete</Button>
)}
```
- **Tujuan**: Better user experience (user tak nampak button yang dia tak boleh guna)
- **Masalah**: Boleh bypass dengan inspect element atau direct API call ❌

**Backend Check (Security):**
```java
@PreAuthorize("@permissionService.hasPermission(authentication.principal.id, 'COMPONENT', 'support', 'chat.delete_button', 'DELETE')")
@DeleteMapping("/chat/{messageId}")
public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
    // ...
}
```
- **Tujuan**: Real security (walaupun user bypass frontend, backend tetap block) ✅
- **Penting**: Backend check adalah MANDATORY untuk security

**Keputusan: BOTH** ✅
- **Frontend**: Untuk UX (hide/show UI elements)
- **Backend**: Untuk security (mandatory, cannot bypass)
- **Rule**: Backend check adalah source of truth, frontend check adalah untuk convenience sahaja

---

## Next Steps

1. **Review & Approval**: Review this design document
2. **Database Design**: Finalize table schemas
3. **API Design**: Finalize endpoint specifications
4. **Implementation Plan**: Create detailed task breakdown
5. **Prototype**: Build minimal viable implementation for one module

---

_Last Updated: January 2026_
_Status: Design Phase - Pending Review_

