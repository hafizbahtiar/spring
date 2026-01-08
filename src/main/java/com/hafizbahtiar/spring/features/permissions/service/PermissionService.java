package com.hafizbahtiar.spring.features.permissions.service;

import com.hafizbahtiar.spring.features.permissions.dto.AddPermissionRequest;
import com.hafizbahtiar.spring.features.permissions.dto.CreateGroupRequest;
import com.hafizbahtiar.spring.features.permissions.dto.GroupResponse;
import com.hafizbahtiar.spring.features.permissions.dto.PermissionResponse;
import com.hafizbahtiar.spring.features.permissions.dto.UpdateGroupRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UpdatePermissionRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UserPermissionsResponse;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionComponent;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionModule;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionPage;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import com.hafizbahtiar.spring.features.user.entity.User;

import java.util.List;

/**
 * Service interface for permission management.
 * Handles permission groups, permissions, user assignments, and permission
 * evaluation.
 */
public interface PermissionService {

    // ==========================================
    // Group Management
    // ==========================================

    /**
     * Create a new permission group.
     * Validates that group name is unique and creator has permission to create
     * groups.
     *
     * @param request   Create group request
     * @param createdBy User ID who is creating the group
     * @return Created GroupResponse
     * @throws PermissionException if group name already exists
     */
    GroupResponse createGroup(CreateGroupRequest request, Long createdBy);

    /**
     * Update an existing permission group.
     * Validates that group exists and user has permission to update it.
     *
     * @param groupId Permission group ID
     * @param request Update group request
     * @return Updated GroupResponse
     * @throws PermissionGroupNotFoundException if group not found
     * @throws PermissionException              if group name already exists (if
     *                                          name is being updated)
     */
    GroupResponse updateGroup(Long groupId, UpdateGroupRequest request);

    /**
     * Delete a permission group.
     * Cascades to permissions and user assignments (all will be deleted).
     *
     * @param groupId Permission group ID
     * @throws PermissionGroupNotFoundException if group not found
     */
    void deleteGroup(Long groupId);

    /**
     * Get all permission groups.
     * Returns all groups regardless of active status.
     *
     * @return List of GroupResponse
     */
    List<GroupResponse> getAllGroups();

    /**
     * Get permission group by ID.
     *
     * @param groupId Permission group ID
     * @return GroupResponse
     * @throws PermissionGroupNotFoundException if group not found
     */
    GroupResponse getGroupById(Long groupId);

    // ==========================================
    // User Assignment
    // ==========================================

    /**
     * Assign a user to a permission group.
     * Validates that user and group exist, and user is not already in the group.
     *
     * @param groupId    Permission group ID
     * @param userId     User ID to assign
     * @param assignedBy User ID who is assigning (for audit)
     * @throws PermissionGroupNotFoundException                                      if
     *                                                                               group
     *                                                                               not
     *                                                                               found
     * @throws com.hafizbahtiar.spring.features.user.exception.UserNotFoundException if
     *                                                                               user
     *                                                                               not
     *                                                                               found
     * @throws PermissionException                                                   if
     *                                                                               user
     *                                                                               is
     *                                                                               already
     *                                                                               in
     *                                                                               the
     *                                                                               group
     */
    void assignUserToGroup(Long groupId, Long userId, Long assignedBy);

    /**
     * Remove a user from a permission group.
     * Validates that user and group exist, and user is actually in the group.
     *
     * @param groupId Permission group ID
     * @param userId  User ID to remove
     * @throws PermissionGroupNotFoundException                                      if
     *                                                                               group
     *                                                                               not
     *                                                                               found
     * @throws com.hafizbahtiar.spring.features.user.exception.UserNotFoundException if
     *                                                                               user
     *                                                                               not
     *                                                                               found
     * @throws PermissionException                                                   if
     *                                                                               user
     *                                                                               is
     *                                                                               not
     *                                                                               in
     *                                                                               the
     *                                                                               group
     */
    void removeUserFromGroup(Long groupId, Long userId);

    /**
     * Get all users assigned to a permission group.
     *
     * @param groupId Permission group ID
     * @return List of User entities
     * @throws PermissionGroupNotFoundException if group not found
     */
    List<User> getGroupMembers(Long groupId);

    /**
     * Get all permission groups for a specific user.
     *
     * @param userId User ID
     * @return List of GroupResponse
     * @throws com.hafizbahtiar.spring.features.user.exception.UserNotFoundException if
     *                                                                               user
     *                                                                               not
     *                                                                               found
     */
    List<GroupResponse> getUserGroups(Long userId);

    // ==========================================
    // Permission Management
    // ==========================================

    /**
     * Add a permission to a permission group.
     * Validates that group exists and permission doesn't already exist.
     *
     * @param groupId Permission group ID
     * @param request Add permission request
     * @return Created PermissionResponse
     * @throws PermissionGroupNotFoundException if group not found
     * @throws PermissionException              if permission already exists
     */
    PermissionResponse addPermission(Long groupId, AddPermissionRequest request);

    /**
     * Remove a permission from a group.
     *
     * @param permissionId Permission ID
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionNotFoundException if
     *                                                                                            permission
     *                                                                                            not
     *                                                                                            found
     */
    void removePermission(Long permissionId);

    /**
     * Get all permissions for a specific group.
     *
     * @param groupId Permission group ID
     * @return List of PermissionResponse
     * @throws PermissionGroupNotFoundException if group not found
     */
    List<PermissionResponse> getGroupPermissions(Long groupId);

    /**
     * Update an existing permission.
     *
     * @param permissionId Permission ID
     * @param request      Update permission request
     * @return Updated PermissionResponse
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionNotFoundException if
     *                                                                                            permission
     *                                                                                            not
     *                                                                                            found
     */
    PermissionResponse updatePermission(Long permissionId, UpdatePermissionRequest request);

    // ==========================================
    // Permission Evaluation
    // ==========================================

    /**
     * Check if a user has a specific permission.
     * Evaluates permissions from:
     * 1. Static role (Layer 1) - OWNER has all permissions
     * 2. Group permissions (Layer 2) - Checks all user's groups
     * 
     * Permission hierarchy: MODULE → PAGE → COMPONENT (automatic inheritance)
     * Multiple groups: OR logic (if any group allows, user has access)
     * Explicit deny: Deny overrides allow
     * Highest permission: READ < WRITE < DELETE
     *
     * @param userId             User ID
     * @param permissionType     Permission type (MODULE, PAGE, COMPONENT)
     * @param resourceType       Resource type (e.g., "support", "finance")
     * @param resourceIdentifier Resource identifier (e.g., "chat", "tickets",
     *                           "edit_button")
     * @param action             Permission action (READ, WRITE, DELETE, EXECUTE)
     * @return true if user has permission, false otherwise
     */
    boolean hasPermission(Long userId, PermissionType permissionType, String resourceType,
            String resourceIdentifier, PermissionAction action);

    /**
     * Check if a user has access to a module.
     * Checks for MODULE-level permission or any PAGE/COMPONENT permissions within
     * the module.
     *
     * @param userId    User ID
     * @param moduleKey Module key (e.g., "support", "finance")
     * @return true if user has module access, false otherwise
     */
    boolean hasModuleAccess(Long userId, String moduleKey);

    /**
     * Check if a user has access to a page.
     * Checks for PAGE-level permission, MODULE-level permission (inheritance), or
     * COMPONENT permissions within the page.
     *
     * @param userId    User ID
     * @param moduleKey Module key
     * @param pageKey   Page key (e.g., "chat", "tickets")
     * @return true if user has page access, false otherwise
     */
    boolean hasPageAccess(Long userId, String moduleKey, String pageKey);

    /**
     * Check if a user has access to a component.
     * Checks for COMPONENT-level permission, PAGE-level permission (inheritance),
     * or MODULE-level permission (inheritance).
     *
     * @param userId       User ID
     * @param pageKey      Page key (e.g., "support.chat", "finance.dashboard")
     * @param componentKey Component key (e.g., "edit_button", "delete_button")
     * @return true if user has component access, false otherwise
     */
    boolean hasComponentAccess(Long userId, String pageKey, String componentKey);

    /**
     * Get all effective permissions for a user.
     * Aggregates permissions from all groups the user belongs to.
     *
     * @param userId User ID
     * @return UserPermissionsResponse with all effective permissions
     * @throws com.hafizbahtiar.spring.features.user.exception.UserNotFoundException if
     *                                                                               user
     *                                                                               not
     *                                                                               found
     */
    UserPermissionsResponse getUserPermissions(Long userId);

    // ==========================================
    // Registry
    // ==========================================

    /**
     * Get available modules that a user can assign to groups.
     * Only returns modules that the user (creator) has access to.
     *
     * @param userId User ID (creator)
     * @return List of PermissionModule
     */
    List<PermissionModule> getAvailableModules(Long userId);

    /**
     * Get all pages for a specific module.
     *
     * @param moduleKey Module key
     * @return List of PermissionPage
     */
    List<PermissionPage> getModulePages(String moduleKey);

    /**
     * Get all components for a specific page.
     *
     * @param pageKey Page key (e.g., "support.chat", "finance.dashboard")
     * @return List of PermissionComponent
     */
    List<PermissionComponent> getPageComponents(String pageKey);

    /**
     * Get permission module by ID.
     * Used for retrieving module details.
     *
     * @param moduleId Module ID
     * @return PermissionModule
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionModuleNotFoundException if
     *                                                                                                  module
     *                                                                                                  not
     *                                                                                                  found
     */
    PermissionModule getModuleById(Long moduleId);

    // ==========================================
    // Registry Management (CRUD)
    // ==========================================

    /**
     * Create a new permission module.
     * Validates that module key is unique and user has permission to create
     * modules.
     *
     * @param request   Create module request
     * @param createdBy User ID who is creating the module
     * @return Created PermissionModule
     * @throws PermissionException if module key already exists
     */
    PermissionModule createModule(com.hafizbahtiar.spring.features.permissions.dto.CreateModuleRequest request,
            Long createdBy);

    /**
     * Update an existing permission module.
     * Validates that module exists and user has permission to update it.
     *
     * @param moduleId Permission module ID
     * @param request  Update module request
     * @return Updated PermissionModule
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionModuleNotFoundException if
     *                                                                                                  module
     *                                                                                                  not
     *                                                                                                  found
     */
    PermissionModule updateModule(Long moduleId,
            com.hafizbahtiar.spring.features.permissions.dto.UpdateModuleRequest request);

    /**
     * Delete a permission module.
     * Validates that module exists and has no dependent pages/components.
     *
     * @param moduleId Permission module ID
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionModuleNotFoundException if
     *                                                                                                  module
     *                                                                                                  not
     *                                                                                                  found
     * @throws PermissionException                                                                      if
     *                                                                                                  module
     *                                                                                                  has
     *                                                                                                  dependent
     *                                                                                                  pages/components
     */
    void deleteModule(Long moduleId);

    /**
     * Create a new permission page.
     * Validates that module exists, page key is unique within module, and user has
     * permission to create pages.
     *
     * @param request Create page request
     * @return Created PermissionPage
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionModuleNotFoundException if
     *                                                                                                  module
     *                                                                                                  not
     *                                                                                                  found
     * @throws PermissionException                                                                      if
     *                                                                                                  page
     *                                                                                                  key
     *                                                                                                  already
     *                                                                                                  exists
     *                                                                                                  in
     *                                                                                                  module
     */
    PermissionPage createPage(com.hafizbahtiar.spring.features.permissions.dto.CreatePageRequest request);

    /**
     * Update an existing permission page.
     * Validates that page exists and user has permission to update it.
     *
     * @param pageId  Permission page ID
     * @param request Update page request
     * @return Updated PermissionPage
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionPageNotFoundException if
     *                                                                                                page
     *                                                                                                not
     *                                                                                                found
     */
    PermissionPage updatePage(Long pageId, com.hafizbahtiar.spring.features.permissions.dto.UpdatePageRequest request);

    /**
     * Delete a permission page.
     * Validates that page exists and has no dependent components.
     *
     * @param pageId Permission page ID
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionPageNotFoundException if
     *                                                                                                page
     *                                                                                                not
     *                                                                                                found
     * @throws PermissionException                                                                    if
     *                                                                                                page
     *                                                                                                has
     *                                                                                                dependent
     *                                                                                                components
     */
    void deletePage(Long pageId);

    /**
     * Get permission page by ID.
     *
     * @param pageId Page ID
     * @return PermissionPage
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionPageNotFoundException if
     *                                                                                                page
     *                                                                                                not
     *                                                                                                found
     */
    PermissionPage getPageById(Long pageId);

    /**
     * Create a new permission component.
     * Validates that page exists, component key is unique within page, and user has
     * permission to create components.
     *
     * @param request Create component request
     * @return Created PermissionComponent
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionPageNotFoundException if
     *                                                                                                page
     *                                                                                                not
     *                                                                                                found
     * @throws PermissionException                                                                    if
     *                                                                                                component
     *                                                                                                key
     *                                                                                                already
     *                                                                                                exists
     *                                                                                                in
     *                                                                                                page
     */
    PermissionComponent createComponent(
            com.hafizbahtiar.spring.features.permissions.dto.CreateComponentRequest request);

    /**
     * Update an existing permission component.
     * Validates that component exists and user has permission to update it.
     *
     * @param componentId Permission component ID
     * @param request     Update component request
     * @return Updated PermissionComponent
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionComponentNotFoundException if
     *                                                                                                     component
     *                                                                                                     not
     *                                                                                                     found
     */
    PermissionComponent updateComponent(Long componentId,
            com.hafizbahtiar.spring.features.permissions.dto.UpdateComponentRequest request);

    /**
     * Delete a permission component.
     * Validates that component exists.
     *
     * @param componentId Permission component ID
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionComponentNotFoundException if
     *                                                                                                     component
     *                                                                                                     not
     *                                                                                                     found
     */
    void deleteComponent(Long componentId);

    /**
     * Get permission component by ID.
     *
     * @param componentId Component ID
     * @return PermissionComponent
     * @throws com.hafizbahtiar.spring.features.permissions.exception.PermissionComponentNotFoundException if
     *                                                                                                     component
     *                                                                                                     not
     *                                                                                                     found
     */
    PermissionComponent getComponentById(Long componentId);

    // ==========================================
    // Registry Validation & Constraints
    // ==========================================

    /**
     * Validate the entire permission registry.
     * Checks for orphaned records, duplicate routes, and other integrity issues.
     *
     * @return RegistryValidationResponse with validation results
     */
    com.hafizbahtiar.spring.features.permissions.dto.RegistryValidationResponse validateRegistry();

    /**
     * Check the health of the permission registry.
     * Provides a summary of registry status and potential issues.
     *
     * @return RegistryHealthResponse with health status
     */
    com.hafizbahtiar.spring.features.permissions.dto.RegistryHealthResponse checkRegistryHealth();

    /**
     * Clean up orphaned records from the registry.
     * Removes pages without valid modules and components without valid pages.
     *
     * @return RegistryCleanupResponse with cleanup results
     */
    com.hafizbahtiar.spring.features.permissions.dto.RegistryCleanupResponse cleanupOrphanedRecords();

    // ==========================================
    // Bulk Operations
    // ==========================================

    /**
     * Bulk create permission modules.
     *
     * @param request   Bulk create request
     * @param createdBy User ID who is creating the modules
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<com.hafizbahtiar.spring.features.permissions.dto.ModuleResponse> bulkCreateModules(
            com.hafizbahtiar.spring.features.permissions.dto.BulkCreateModuleRequest request, Long createdBy);

    /**
     * Bulk update permission modules.
     *
     * @param request Bulk update request
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<com.hafizbahtiar.spring.features.permissions.dto.ModuleResponse> bulkUpdateModules(
            com.hafizbahtiar.spring.features.permissions.dto.BulkUpdateModuleRequest request);

    /**
     * Bulk delete permission modules.
     *
     * @param request Bulk delete request
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<Void> bulkDeleteModules(
            com.hafizbahtiar.spring.features.permissions.dto.BulkDeleteRequest request);

    /**
     * Bulk create permission pages.
     *
     * @param request Bulk create request
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<com.hafizbahtiar.spring.features.permissions.dto.PageResponse> bulkCreatePages(
            com.hafizbahtiar.spring.features.permissions.dto.BulkCreatePageRequest request);

    /**
     * Bulk update permission pages.
     *
     * @param request Bulk update request
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<com.hafizbahtiar.spring.features.permissions.dto.PageResponse> bulkUpdatePages(
            com.hafizbahtiar.spring.features.permissions.dto.BulkUpdatePageRequest request);

    /**
     * Bulk delete permission pages.
     *
     * @param request Bulk delete request
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<Void> bulkDeletePages(
            com.hafizbahtiar.spring.features.permissions.dto.BulkDeleteRequest request);

    /**
     * Bulk create permission components.
     *
     * @param request Bulk create request
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<com.hafizbahtiar.spring.features.permissions.dto.ComponentResponse> bulkCreateComponents(
            com.hafizbahtiar.spring.features.permissions.dto.BulkCreateComponentRequest request);

    /**
     * Bulk update permission components.
     *
     * @param request Bulk update request
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<com.hafizbahtiar.spring.features.permissions.dto.ComponentResponse> bulkUpdateComponents(
            com.hafizbahtiar.spring.features.permissions.dto.BulkUpdateComponentRequest request);

    /**
     * Bulk delete permission components.
     *
     * @param request Bulk delete request
     * @return BulkOperationResponse with results
     */
    com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse<Void> bulkDeleteComponents(
            com.hafizbahtiar.spring.features.permissions.dto.BulkDeleteRequest request);

    /**
     * Export the entire permission registry.
     *
     * @param format Export format (JSON, CSV)
     * @param userId User ID performing the export
     * @return RegistryExportResponse with export data
     */
    com.hafizbahtiar.spring.features.permissions.dto.RegistryExportResponse exportRegistry(String format, Long userId);

    /**
     * Import permission registry from data.
     *
     * @param request Import request
     * @param userId  User ID performing the import
     * @return RegistryImportResponse with import results
     */
    com.hafizbahtiar.spring.features.permissions.dto.RegistryImportResponse importRegistry(
            com.hafizbahtiar.spring.features.permissions.dto.RegistryImportRequest request, Long userId);

    // ==========================================
    // Search & Filtering
    // ==========================================

    /**
     * Search permission modules by query.
     *
     * @param query    Search query (searches in moduleKey, moduleName, description)
     * @param pageable Pagination parameters
     * @return Page of matching modules
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.ModuleResponse> searchModules(
            String query, org.springframework.data.domain.Pageable pageable);

    /**
     * Filter permission modules by available roles.
     *
     * @param role     Role to filter by (e.g., "OWNER", "ADMIN")
     * @param pageable Pagination parameters
     * @return Page of matching modules
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.ModuleResponse> filterModulesByRole(
            String role, org.springframework.data.domain.Pageable pageable);

    /**
     * Search permission pages by query.
     *
     * @param query    Search query (searches in pageKey, pageName, routePath,
     *                 description)
     * @param pageable Pagination parameters
     * @return Page of matching pages
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.PageResponse> searchPages(
            String query, org.springframework.data.domain.Pageable pageable);

    /**
     * Filter permission pages by module key.
     *
     * @param moduleKey Module key to filter by
     * @param pageable  Pagination parameters
     * @return Page of matching pages
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.PageResponse> filterPagesByModule(
            String moduleKey, org.springframework.data.domain.Pageable pageable);

    /**
     * Search permission components by query.
     *
     * @param query    Search query (searches in componentKey, componentName,
     *                 description)
     * @param pageable Pagination parameters
     * @return Page of matching components
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.ComponentResponse> searchComponents(
            String query, org.springframework.data.domain.Pageable pageable);

    /**
     * Filter permission components by page key.
     *
     * @param pageKey  Page key to filter by
     * @param pageable Pagination parameters
     * @return Page of matching components
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.ComponentResponse> filterComponentsByPage(
            String pageKey, org.springframework.data.domain.Pageable pageable);

    /**
     * Filter permission components by component type.
     *
     * @param componentType Component type to filter by (e.g., "BUTTON", "LINK")
     * @param pageable      Pagination parameters
     * @return Page of matching components
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.ComponentResponse> filterComponentsByType(
            String componentType, org.springframework.data.domain.Pageable pageable);

    /**
     * Get all permission modules with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of all modules
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.ModuleResponse> getAllModules(
            org.springframework.data.domain.Pageable pageable);

    /**
     * Get all permission pages with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of all pages
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.PageResponse> getAllPages(
            org.springframework.data.domain.Pageable pageable);

    /**
     * Get all permission components with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of all components
     */
    org.springframework.data.domain.Page<com.hafizbahtiar.spring.features.permissions.dto.ComponentResponse> getAllComponents(
            org.springframework.data.domain.Pageable pageable);
}
