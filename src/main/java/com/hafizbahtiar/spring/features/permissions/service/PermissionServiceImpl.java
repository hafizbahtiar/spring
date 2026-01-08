package com.hafizbahtiar.spring.features.permissions.service;

import com.hafizbahtiar.spring.common.security.Role;
import com.hafizbahtiar.spring.features.permissions.dto.AddPermissionRequest;
import com.hafizbahtiar.spring.features.permissions.dto.CreateGroupRequest;
import com.hafizbahtiar.spring.features.permissions.dto.CreateComponentRequest;
import com.hafizbahtiar.spring.features.permissions.dto.CreateModuleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.CreatePageRequest;
import com.hafizbahtiar.spring.features.permissions.dto.ComponentResponse;
import com.hafizbahtiar.spring.features.permissions.dto.GroupResponse;
import com.hafizbahtiar.spring.features.permissions.dto.ModuleResponse;
import com.hafizbahtiar.spring.features.permissions.dto.PageResponse;
import com.hafizbahtiar.spring.features.permissions.dto.PermissionResponse;
import com.hafizbahtiar.spring.features.permissions.dto.BulkCreateComponentRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkCreateModuleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkCreatePageRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkDeleteRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse;
import com.hafizbahtiar.spring.features.permissions.dto.BulkUpdateComponentRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkUpdateModuleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkUpdatePageRequest;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryCleanupResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryExportResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryHealthResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryImportRequest;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryImportResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryValidationResponse;
import com.hafizbahtiar.spring.features.permissions.dto.UpdateComponentRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UpdateGroupRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UpdateModuleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UpdatePageRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UpdatePermissionRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UserPermissionsResponse;
import com.hafizbahtiar.spring.features.permissions.entity.GroupPermission;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionComponent;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionGroup;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionModule;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionPage;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import com.hafizbahtiar.spring.features.permissions.entity.UserGroup;
import com.hafizbahtiar.spring.features.permissions.exception.PermissionComponentNotFoundException;
import com.hafizbahtiar.spring.features.permissions.exception.PermissionException;
import com.hafizbahtiar.spring.features.permissions.exception.PermissionGroupNotFoundException;
import com.hafizbahtiar.spring.features.permissions.exception.PermissionModuleNotFoundException;
import com.hafizbahtiar.spring.features.permissions.exception.PermissionNotFoundException;
import com.hafizbahtiar.spring.features.permissions.exception.PermissionPageNotFoundException;
import com.hafizbahtiar.spring.features.permissions.repository.GroupPermissionRepository;
import com.hafizbahtiar.spring.features.permissions.repository.PermissionComponentRepository;
import com.hafizbahtiar.spring.features.permissions.repository.PermissionGroupRepository;
import com.hafizbahtiar.spring.features.permissions.repository.PermissionModuleRepository;
import com.hafizbahtiar.spring.features.permissions.repository.PermissionPageRepository;
import com.hafizbahtiar.spring.features.permissions.repository.UserGroupRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PermissionService.
 * Handles permission groups, user assignments, and permission evaluation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionGroupRepository permissionGroupRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupPermissionRepository groupPermissionRepository;
    private final UserRepository userRepository;
    private final PermissionModuleRepository permissionModuleRepository;
    private final PermissionPageRepository permissionPageRepository;
    private final PermissionComponentRepository permissionComponentRepository;
    private final PermissionCacheService permissionCacheService;

    // ==========================================
    // Group Management
    // ==========================================

    @Override
    public GroupResponse createGroup(CreateGroupRequest request, Long createdBy) {
        log.debug("Creating permission group: {} by user ID: {}", request.getName(), createdBy);

        // Validate creator exists
        User creator = userRepository.findById(createdBy)
                .orElseThrow(() -> UserNotFoundException.byId(createdBy));

        // Validate group name is unique
        if (permissionGroupRepository.existsByName(request.getName())) {
            throw PermissionException.groupNameAlreadyExists(request.getName());
        }

        // Create group entity
        PermissionGroup group = new PermissionGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);
        group.setActive(request.getActive() != null ? request.getActive() : true);

        PermissionGroup savedGroup = permissionGroupRepository.save(group);
        log.info("Permission group created successfully with ID: {} by user ID: {}", savedGroup.getId(), createdBy);

        return toGroupResponse(savedGroup);
    }

    @Override
    public GroupResponse updateGroup(Long groupId, UpdateGroupRequest request) {
        log.debug("Updating permission group ID: {}", groupId);

        // Find group
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> PermissionGroupNotFoundException.byId(groupId));

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            // Check if new name conflicts with existing group (excluding current group)
            if (permissionGroupRepository.existsByName(request.getName())) {
                // Check if it's the same group
                permissionGroupRepository.findByName(request.getName())
                        .ifPresent(existingGroup -> {
                            if (!existingGroup.getId().equals(groupId)) {
                                throw PermissionException.groupNameAlreadyExists(request.getName());
                            }
                        });
            }
            group.setName(request.getName());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        // Update active status if provided
        if (request.getActive() != null) {
            if (request.getActive()) {
                group.activate();
            } else {
                group.deactivate();
            }
        }

        PermissionGroup updatedGroup = permissionGroupRepository.save(group);
        log.info("Permission group updated successfully with ID: {}", updatedGroup.getId());

        return toGroupResponse(updatedGroup);
    }

    @Override
    public void deleteGroup(Long groupId) {
        log.debug("Deleting permission group ID: {}", groupId);

        // Find group
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> PermissionGroupNotFoundException.byId(groupId));

        // Get all user IDs in this group before deletion (for cache invalidation)
        List<Long> userIds = userGroupRepository.findByGroupId(groupId).stream()
                .map(ug -> ug.getUser().getId())
                .collect(Collectors.toList());

        // Delete group (cascade will delete permissions and user assignments)
        permissionGroupRepository.delete(group);
        log.info("Permission group deleted successfully with ID: {}", groupId);

        // Invalidate cache for all users who were in this group
        userIds.forEach(permissionCacheService::invalidateUserPermissions);
        permissionCacheService.invalidateGroupPermissions(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getAllGroups() {
        log.debug("Fetching all permission groups");
        List<PermissionGroup> groups = permissionGroupRepository.findAll();
        return groups.stream()
                .map(this::toGroupResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long groupId) {
        log.debug("Fetching permission group ID: {}", groupId);
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> PermissionGroupNotFoundException.byId(groupId));
        return toGroupResponse(group);
    }

    // ==========================================
    // User Assignment
    // ==========================================

    @Override
    public void assignUserToGroup(Long groupId, Long userId, Long assignedBy) {
        log.debug("Assigning user ID: {} to group ID: {} by user ID: {}", userId, groupId, assignedBy);

        // Validate group exists
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> PermissionGroupNotFoundException.byId(groupId));

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate assigner exists
        User assigner = userRepository.findById(assignedBy)
                .orElseThrow(() -> UserNotFoundException.byId(assignedBy));

        // Check if user is already in group
        if (userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw PermissionException.userAlreadyInGroup(userId, groupId);
        }

        // Create user-group assignment
        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setAssignedBy(assigner);

        userGroupRepository.save(userGroup);
        log.info("User ID: {} assigned to group ID: {} by user ID: {}", userId, groupId, assignedBy);

        // Invalidate user permissions cache
        permissionCacheService.invalidateUserPermissions(userId);
    }

    @Override
    public void removeUserFromGroup(Long groupId, Long userId) {
        log.debug("Removing user ID: {} from group ID: {}", userId, groupId);

        // Validate group exists
        permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> PermissionGroupNotFoundException.byId(groupId));

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Find and delete user-group assignment
        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> PermissionException.userNotInGroup(userId, groupId));

        userGroupRepository.delete(userGroup);
        log.info("User ID: {} removed from group ID: {}", userId, groupId);

        // Invalidate user permissions cache
        permissionCacheService.invalidateUserPermissions(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getGroupMembers(Long groupId) {
        log.debug("Fetching members for group ID: {}", groupId);

        // Validate group exists
        permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> PermissionGroupNotFoundException.byId(groupId));

        // Get all user-group assignments for this group
        List<UserGroup> userGroups = userGroupRepository.findByGroupId(groupId);
        return userGroups.stream()
                .map(UserGroup::getUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(Long userId) {
        log.debug("Fetching groups for user ID: {}", userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get all user-group assignments for this user
        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);
        return userGroups.stream()
                .map(userGroup -> toGroupResponse(userGroup.getGroup()))
                .collect(Collectors.toList());
    }

    // ==========================================
    // Permission Management
    // ==========================================

    @Override
    public PermissionResponse addPermission(Long groupId, AddPermissionRequest request) {
        log.debug("Adding permission to group ID: {}, type: {}, resource: {}:{}",
                groupId, request.getPermissionType(), request.getResourceType(), request.getResourceIdentifier());

        // Validate group exists
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> PermissionGroupNotFoundException.byId(groupId));

        // Check if permission already exists
        List<GroupPermission> existing = groupPermissionRepository.findByGroupAndPermission(
                groupId, request.getPermissionType(), request.getResourceType(),
                request.getResourceIdentifier(), request.getAction());

        if (!existing.isEmpty()) {
            throw PermissionException.invalidPermission(
                    "Permission already exists for group ID: " + groupId);
        }

        // Create permission entity
        GroupPermission permission = new GroupPermission();
        permission.setGroup(group);
        permission.setPermissionType(request.getPermissionType());
        permission.setResourceType(request.getResourceType());
        permission.setResourceIdentifier(request.getResourceIdentifier());
        permission.setAction(request.getAction());
        permission.setGranted(request.getGranted() != null ? request.getGranted() : true);

        GroupPermission savedPermission = groupPermissionRepository.save(permission);
        log.info("Permission added successfully with ID: {} to group ID: {}", savedPermission.getId(), groupId);

        // Invalidate cache for all users in this group
        invalidateGroupCache(groupId);

        return toPermissionResponse(savedPermission);
    }

    @Override
    public void removePermission(Long permissionId) {
        log.debug("Removing permission ID: {}", permissionId);

        GroupPermission permission = groupPermissionRepository.findById(permissionId)
                .orElseThrow(() -> PermissionNotFoundException.byId(permissionId));

        Long groupId = permission.getGroup().getId();
        groupPermissionRepository.delete(permission);
        log.info("Permission removed successfully with ID: {}", permissionId);

        // Invalidate cache for all users in this group
        invalidateGroupCache(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getGroupPermissions(Long groupId) {
        log.debug("Fetching permissions for group ID: {}", groupId);

        // Validate group exists
        permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> PermissionGroupNotFoundException.byId(groupId));

        List<GroupPermission> permissions = groupPermissionRepository.findByGroupId(groupId);
        return permissions.stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse updatePermission(Long permissionId, UpdatePermissionRequest request) {
        log.debug("Updating permission ID: {}", permissionId);

        GroupPermission permission = groupPermissionRepository.findById(permissionId)
                .orElseThrow(() -> PermissionNotFoundException.byId(permissionId));

        // Update fields if provided
        if (request.getPermissionType() != null) {
            permission.setPermissionType(request.getPermissionType());
        }
        if (request.getResourceType() != null && !request.getResourceType().isBlank()) {
            permission.setResourceType(request.getResourceType());
        }
        if (request.getResourceIdentifier() != null && !request.getResourceIdentifier().isBlank()) {
            permission.setResourceIdentifier(request.getResourceIdentifier());
        }
        if (request.getAction() != null) {
            permission.setAction(request.getAction());
        }
        if (request.getGranted() != null) {
            permission.setGranted(request.getGranted());
        }

        GroupPermission updatedPermission = groupPermissionRepository.save(permission);
        log.info("Permission updated successfully with ID: {}", updatedPermission.getId());

        // Invalidate cache for all users in this group
        invalidateGroupCache(updatedPermission.getGroup().getId());

        return toPermissionResponse(updatedPermission);
    }

    // ==========================================
    // Permission Evaluation
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, PermissionType permissionType, String resourceType,
            String resourceIdentifier, PermissionAction action) {
        log.debug("Checking permission for user ID: {}, type: {}, resource: {}:{}, action: {}",
                userId, permissionType, resourceType, resourceIdentifier, action);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Layer 1: Check static role - OWNER has all permissions
        String userRole = user.getRole();
        if (Role.OWNER.getValue().equalsIgnoreCase(userRole)) {
            log.debug("User ID: {} is OWNER, granting permission", userId);
            return true;
        }

        // Layer 2: Check group permissions
        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);
        if (userGroups.isEmpty()) {
            log.debug("User ID: {} has no groups, denying permission", userId);
            return false;
        }

        // Get all permissions from user's groups
        List<GroupPermission> allPermissions = new ArrayList<>();
        for (UserGroup userGroup : userGroups) {
            if (userGroup.getGroup().isActive()) {
                List<GroupPermission> groupPermissions = groupPermissionRepository
                        .findByGroupId(userGroup.getGroup().getId());
                allPermissions.addAll(groupPermissions);
            }
        }

        // Evaluate permission with hierarchy, OR logic, deny override, and highest
        // permission
        return evaluatePermission(allPermissions, permissionType, resourceType,
                resourceIdentifier, action);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasModuleAccess(Long userId, String moduleKey) {
        log.debug("Checking module access for user ID: {}, module: {}", userId, moduleKey);

        // Check MODULE-level permission
        if (hasPermission(userId, PermissionType.MODULE, moduleKey, moduleKey, PermissionAction.READ)) {
            return true;
        }

        // Check if user has any PAGE or COMPONENT permissions within the module
        // (inheritance check - if they have page/component access, they have module
        // access)
        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);
        for (UserGroup userGroup : userGroups) {
            if (userGroup.getGroup().isActive()) {
                List<GroupPermission> permissions = groupPermissionRepository
                        .findByGroupId(userGroup.getGroup().getId());
                for (GroupPermission perm : permissions) {
                    if (perm.getResourceType().equals(moduleKey) && perm.isGranted()) {
                        // User has some permission in this module
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPageAccess(Long userId, String moduleKey, String pageKey) {
        log.debug("Checking page access for user ID: {}, module: {}, page: {}", userId, moduleKey, pageKey);

        // Check PAGE-level permission
        if (hasPermission(userId, PermissionType.PAGE, moduleKey, pageKey, PermissionAction.READ)) {
            return true;
        }

        // Check MODULE-level permission (inheritance - module access grants page
        // access)
        if (hasPermission(userId, PermissionType.MODULE, moduleKey, moduleKey, PermissionAction.READ)) {
            return true;
        }

        // Check if user has any COMPONENT permissions within the page
        String fullPageKey = moduleKey + "." + pageKey;
        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);
        for (UserGroup userGroup : userGroups) {
            if (userGroup.getGroup().isActive()) {
                List<GroupPermission> permissions = groupPermissionRepository
                        .findByGroupId(userGroup.getGroup().getId());
                for (GroupPermission perm : permissions) {
                    if (perm.getPermissionType() == PermissionType.COMPONENT
                            && perm.getResourceIdentifier().startsWith(fullPageKey + ".")
                            && perm.isGranted()) {
                        // User has component permission in this page
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasComponentAccess(Long userId, String pageKey, String componentKey) {
        log.debug("Checking component access for user ID: {}, page: {}, component: {}",
                userId, pageKey, componentKey);

        // Extract module key from page key (format: "module.page")
        String[] pageParts = pageKey.split("\\.");
        if (pageParts.length < 2) {
            log.warn("Invalid page key format: {}", pageKey);
            return false;
        }
        String moduleKey = pageParts[0];

        // Check COMPONENT-level permission
        if (hasPermission(userId, PermissionType.COMPONENT, moduleKey, componentKey, PermissionAction.READ)) {
            return true;
        }

        // Check PAGE-level permission (inheritance - page access grants component
        // access)
        String pageKeyOnly = pageParts[1];
        if (hasPermission(userId, PermissionType.PAGE, moduleKey, pageKeyOnly, PermissionAction.READ)) {
            return true;
        }

        // Check MODULE-level permission (inheritance - module access grants component
        // access)
        if (hasPermission(userId, PermissionType.MODULE, moduleKey, moduleKey, PermissionAction.READ)) {
            return true;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPermissionsResponse getUserPermissions(Long userId) {
        log.debug("Fetching all permissions for user ID: {}", userId);

        // Try to get from cache first
        UserPermissionsResponse cached = permissionCacheService.getCachedUserPermissions(userId);
        if (cached != null) {
            log.debug("Returning cached permissions for user ID: {}", userId);
            return cached;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get user's groups
        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);
        List<GroupResponse> groups = userGroups.stream()
                .map(ug -> toGroupResponse(ug.getGroup()))
                .collect(Collectors.toList());

        // Get all permissions from user's groups
        List<GroupPermission> allPermissions = new ArrayList<>();
        for (UserGroup userGroup : userGroups) {
            if (userGroup.getGroup().isActive()) {
                List<GroupPermission> groupPermissions = groupPermissionRepository
                        .findByGroupId(userGroup.getGroup().getId());
                allPermissions.addAll(groupPermissions);
            }
        }

        // Aggregate permissions by resource
        Map<String, Map<PermissionAction, Boolean>> effectivePermissions = new HashMap<>();
        int moduleCount = 0, pageCount = 0, componentCount = 0, grantedCount = 0, deniedCount = 0;

        for (GroupPermission perm : allPermissions) {
            String key = String.format("%s:%s:%s",
                    perm.getPermissionType(),
                    perm.getResourceType(),
                    perm.getResourceIdentifier());

            effectivePermissions.computeIfAbsent(key, k -> new HashMap<>())
                    .put(perm.getAction(), perm.isGranted());

            // Count by type
            if (perm.getPermissionType() == PermissionType.MODULE)
                moduleCount++;
            else if (perm.getPermissionType() == PermissionType.PAGE)
                pageCount++;
            else if (perm.getPermissionType() == PermissionType.COMPONENT)
                componentCount++;

            // Count by granted/denied
            if (perm.isGranted())
                grantedCount++;
            else
                deniedCount++;
        }

        UserPermissionsResponse.PermissionSummary summary = UserPermissionsResponse.PermissionSummary.builder()
                .totalModulePermissions(moduleCount)
                .totalPagePermissions(pageCount)
                .totalComponentPermissions(componentCount)
                .totalGranted(grantedCount)
                .totalDenied(deniedCount)
                .build();

        UserPermissionsResponse response = UserPermissionsResponse.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userRole(user.getRole())
                .groups(groups)
                .effectivePermissions(effectivePermissions)
                .summary(summary)
                .build();

        // Cache the response
        permissionCacheService.cacheUserPermissions(userId, response);

        return response;
    }

    // ==========================================
    // Registry
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<PermissionModule> getAvailableModules(Long userId) {
        log.debug("Fetching available modules for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        String userRole = user.getRole();
        List<PermissionModule> allModules = permissionModuleRepository.findAll();

        // Filter modules based on user's role and availableToRoles
        return allModules.stream()
                .filter(module -> {
                    String availableToRoles = module.getAvailableToRoles();
                    if (availableToRoles == null || availableToRoles.isBlank()) {
                        return true; // Available to all if not specified
                    }
                    String[] roles = availableToRoles.split(",");
                    for (String role : roles) {
                        if (role.trim().equalsIgnoreCase(userRole)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionModule getModuleById(Long moduleId) {
        log.debug("Fetching permission module by ID: {}", moduleId);
        return permissionModuleRepository.findById(moduleId)
                .orElseThrow(() -> PermissionModuleNotFoundException.byId(moduleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionPage> getModulePages(String moduleKey) {
        log.debug("Fetching pages for module: {}", moduleKey);
        return permissionPageRepository.findByModuleKey(moduleKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionComponent> getPageComponents(String pageKey) {
        log.debug("Fetching components for page: {}", pageKey);
        return permissionComponentRepository.findByPageKey(pageKey);
    }

    // ==========================================
    // Registry Management (CRUD)
    // ==========================================

    @Override
    public PermissionModule createModule(CreateModuleRequest request, Long createdBy) {
        log.debug("Creating permission module: {} by user ID: {}", request.getModuleKey(), createdBy);

        // Validate user exists (for audit trail, though not stored in module entity)
        userRepository.findById(createdBy)
                .orElseThrow(() -> UserNotFoundException.byId(createdBy));

        // Validate module key is unique
        if (permissionModuleRepository.existsByModuleKey(request.getModuleKey())) {
            throw PermissionException.moduleKeyAlreadyExists(request.getModuleKey());
        }

        // Create module entity
        PermissionModule module = new PermissionModule();
        module.setModuleKey(request.getModuleKey());
        module.setModuleName(request.getModuleName());
        module.setDescription(request.getDescription());
        module.setAvailableToRoles(request.getAvailableToRoles());

        PermissionModule savedModule = permissionModuleRepository.save(module);
        log.info("Permission module created successfully with ID: {} and key: {}", savedModule.getId(),
                savedModule.getModuleKey());

        return savedModule;
    }

    @Override
    public PermissionModule updateModule(Long moduleId, UpdateModuleRequest request) {
        log.debug("Updating permission module ID: {}", moduleId);

        // Validate module exists
        PermissionModule module = permissionModuleRepository.findById(moduleId)
                .orElseThrow(() -> PermissionModuleNotFoundException.byId(moduleId));

        // Update fields (only non-null fields)
        if (request.getModuleName() != null) {
            module.setModuleName(request.getModuleName());
        }
        if (request.getDescription() != null) {
            module.setDescription(request.getDescription());
        }
        if (request.getAvailableToRoles() != null) {
            module.setAvailableToRoles(request.getAvailableToRoles());
        }

        PermissionModule updatedModule = permissionModuleRepository.save(module);
        log.info("Permission module updated successfully with ID: {} and key: {}", updatedModule.getId(),
                updatedModule.getModuleKey());

        return updatedModule;
    }

    @Override
    public void deleteModule(Long moduleId) {
        log.debug("Deleting permission module ID: {}", moduleId);

        // Validate module exists
        PermissionModule module = permissionModuleRepository.findById(moduleId)
                .orElseThrow(() -> PermissionModuleNotFoundException.byId(moduleId));

        // Check if module has dependent pages
        List<PermissionPage> pages = permissionPageRepository.findByModuleKey(module.getModuleKey());
        if (!pages.isEmpty()) {
            throw PermissionException.invalidPermission(
                    "Cannot delete module with key: " + module.getModuleKey()
                            + " because it has " + pages.size() + " dependent page(s). Delete pages first.");
        }

        // Delete module
        permissionModuleRepository.delete(module);
        log.info("Permission module deleted successfully with ID: {} and key: {}", moduleId, module.getModuleKey());
    }

    @Override
    public PermissionPage createPage(CreatePageRequest request) {
        log.debug("Creating permission page: {}.{}", request.getModuleKey(), request.getPageKey());

        // Validate module exists
        permissionModuleRepository.findByModuleKey(request.getModuleKey())
                .orElseThrow(() -> PermissionModuleNotFoundException.byKey(request.getModuleKey()));

        // Validate page key is unique within module
        if (permissionPageRepository.existsByModuleKeyAndPageKey(request.getModuleKey(), request.getPageKey())) {
            throw PermissionException.pageKeyAlreadyExists(request.getModuleKey(), request.getPageKey());
        }

        // Create page entity
        PermissionPage page = new PermissionPage();
        page.setModuleKey(request.getModuleKey());
        page.setPageKey(request.getPageKey());
        page.setPageName(request.getPageName());
        page.setRoutePath(request.getRoutePath());
        page.setDescription(request.getDescription());

        PermissionPage savedPage = permissionPageRepository.save(page);
        log.info("Permission page created successfully with ID: {} and key: {}.{}", savedPage.getId(),
                savedPage.getModuleKey(), savedPage.getPageKey());

        return savedPage;
    }

    @Override
    public PermissionPage updatePage(Long pageId, UpdatePageRequest request) {
        log.debug("Updating permission page ID: {}", pageId);

        // Validate page exists
        PermissionPage page = permissionPageRepository.findById(pageId)
                .orElseThrow(() -> PermissionPageNotFoundException.byId(pageId));

        // Update fields (only non-null fields)
        if (request.getPageName() != null) {
            page.setPageName(request.getPageName());
        }
        if (request.getRoutePath() != null) {
            page.setRoutePath(request.getRoutePath());
        }
        if (request.getDescription() != null) {
            page.setDescription(request.getDescription());
        }

        PermissionPage updatedPage = permissionPageRepository.save(page);
        log.info("Permission page updated successfully with ID: {} and key: {}.{}", updatedPage.getId(),
                updatedPage.getModuleKey(), updatedPage.getPageKey());

        return updatedPage;
    }

    @Override
    public void deletePage(Long pageId) {
        log.debug("Deleting permission page ID: {}", pageId);

        // Validate page exists
        PermissionPage page = permissionPageRepository.findById(pageId)
                .orElseThrow(() -> PermissionPageNotFoundException.byId(pageId));

        // Check if page has dependent components
        List<PermissionComponent> components = permissionComponentRepository.findByPageKey(page.getPageKey());
        if (!components.isEmpty()) {
            throw PermissionException.invalidPermission(
                    "Cannot delete page with key: " + page.getModuleKey() + "." + page.getPageKey()
                            + " because it has " + components.size()
                            + " dependent component(s). Delete components first.");
        }

        // Delete page
        permissionPageRepository.delete(page);
        log.info("Permission page deleted successfully with ID: {} and key: {}.{}", pageId, page.getModuleKey(),
                page.getPageKey());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionPage getPageById(Long pageId) {
        log.debug("Fetching permission page by ID: {}", pageId);
        return permissionPageRepository.findById(pageId)
                .orElseThrow(() -> PermissionPageNotFoundException.byId(pageId));
    }

    @Override
    public PermissionComponent createComponent(CreateComponentRequest request) {
        log.debug("Creating permission component: {}.{}", request.getPageKey(), request.getComponentKey());

        // Validate page exists
        // Page key format is "module.page", so we need to find it
        String[] pageKeyParts = request.getPageKey().split("\\.");
        if (pageKeyParts.length != 2) {
            throw PermissionException.invalidPermission(
                    "Invalid page key format. Expected format: 'module.page' (e.g., 'support.chat')");
        }
        String moduleKey = pageKeyParts[0];
        String pageKeyOnly = pageKeyParts[1];

        permissionPageRepository.findByModuleKeyAndPageKey(moduleKey, pageKeyOnly)
                .orElseThrow(() -> PermissionPageNotFoundException.byKey(moduleKey, pageKeyOnly));

        // Validate component key is unique within page
        if (permissionComponentRepository.existsByPageKeyAndComponentKey(request.getPageKey(),
                request.getComponentKey())) {
            throw PermissionException.componentKeyAlreadyExists(request.getPageKey(), request.getComponentKey());
        }

        // Create component entity
        PermissionComponent component = new PermissionComponent();
        component.setPageKey(request.getPageKey());
        component.setComponentKey(request.getComponentKey());
        component.setComponentName(request.getComponentName());
        component.setComponentType(request.getComponentType());
        component.setDescription(request.getDescription());

        PermissionComponent savedComponent = permissionComponentRepository.save(component);
        log.info("Permission component created successfully with ID: {} and key: {}.{}", savedComponent.getId(),
                savedComponent.getPageKey(), savedComponent.getComponentKey());

        return savedComponent;
    }

    @Override
    public PermissionComponent updateComponent(Long componentId, UpdateComponentRequest request) {
        log.debug("Updating permission component ID: {}", componentId);

        // Validate component exists
        PermissionComponent component = permissionComponentRepository.findById(componentId)
                .orElseThrow(() -> PermissionComponentNotFoundException.byId(componentId));

        // Update fields (only non-null fields)
        if (request.getComponentName() != null) {
            component.setComponentName(request.getComponentName());
        }
        if (request.getComponentType() != null) {
            component.setComponentType(request.getComponentType());
        }
        if (request.getDescription() != null) {
            component.setDescription(request.getDescription());
        }

        PermissionComponent updatedComponent = permissionComponentRepository.save(component);
        log.info("Permission component updated successfully with ID: {} and key: {}.{}", updatedComponent.getId(),
                updatedComponent.getPageKey(), updatedComponent.getComponentKey());

        return updatedComponent;
    }

    @Override
    public void deleteComponent(Long componentId) {
        log.debug("Deleting permission component ID: {}", componentId);

        // Validate component exists
        PermissionComponent component = permissionComponentRepository.findById(componentId)
                .orElseThrow(() -> PermissionComponentNotFoundException.byId(componentId));

        // Delete component (no dependencies to check)
        permissionComponentRepository.delete(component);
        log.info("Permission component deleted successfully with ID: {} and key: {}.{}", componentId,
                component.getPageKey(), component.getComponentKey());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionComponent getComponentById(Long componentId) {
        log.debug("Fetching permission component by ID: {}", componentId);
        return permissionComponentRepository.findById(componentId)
                .orElseThrow(() -> PermissionComponentNotFoundException.byId(componentId));
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    /**
     * Convert PermissionGroup entity to GroupResponse DTO.
     */
    private GroupResponse toGroupResponse(PermissionGroup group) {
        // Get permission count
        int permissionCount = groupPermissionRepository.findByGroupId(group.getId()).size();

        // Get member count
        int memberCount = userGroupRepository.findByGroupId(group.getId()).size();

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdById(group.getCreatedBy().getId())
                .createdByEmail(group.getCreatedBy().getEmail())
                .active(group.isActive())
                .permissionCount(permissionCount)
                .memberCount(memberCount)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    /**
     * Convert GroupPermission entity to PermissionResponse DTO.
     */
    private PermissionResponse toPermissionResponse(GroupPermission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .groupId(permission.getGroup().getId())
                .groupName(permission.getGroup().getName())
                .permissionType(permission.getPermissionType())
                .resourceType(permission.getResourceType())
                .resourceIdentifier(permission.getResourceIdentifier())
                .action(permission.getAction())
                .granted(permission.isGranted())
                .createdAt(permission.getCreatedAt())
                .build();
    }

    /**
     * Evaluate permission with hierarchy, OR logic, deny override, and highest
     * permission wins.
     * 
     * Rules:
     * 1. Hierarchy: MODULE → PAGE → COMPONENT (automatic inheritance)
     * 2. OR logic: If any group allows, user has access
     * 3. Deny override: Explicit deny (granted=false) overrides allow
     * 4. Highest permission: READ < WRITE < DELETE (if user has WRITE, they have
     * READ)
     * 
     * @param permissions        All permissions from user's groups
     * @param permissionType     Requested permission type
     * @param resourceType       Resource type
     * @param resourceIdentifier Resource identifier
     * @param action             Requested action
     * @return true if user has permission, false otherwise
     */
    private boolean evaluatePermission(List<GroupPermission> permissions,
            PermissionType permissionType, String resourceType, String resourceIdentifier,
            PermissionAction action) {

        // Check for explicit deny first (deny overrides allow)
        for (GroupPermission perm : permissions) {
            if (matchesPermission(perm, permissionType, resourceType, resourceIdentifier, action, true)) {
                if (perm.isDenied()) {
                    log.debug("Explicit deny found, denying permission");
                    return false; // Explicit deny overrides everything
                }
            }
        }

        // Check for explicit allow
        for (GroupPermission perm : permissions) {
            if (matchesPermission(perm, permissionType, resourceType, resourceIdentifier, action, false)) {
                if (perm.isGranted()) {
                    log.debug("Explicit allow found, granting permission");
                    return true; // OR logic - if any group allows, user has access
                }
            }
        }

        // Check hierarchy inheritance
        if (permissionType == PermissionType.PAGE || permissionType == PermissionType.COMPONENT) {
            // Check MODULE-level permission (inheritance)
            for (GroupPermission perm : permissions) {
                if (perm.getPermissionType() == PermissionType.MODULE
                        && perm.getResourceType().equals(resourceType)
                        && perm.isGranted()
                        && hasActionAccess(perm.getAction(), action)) {
                    log.debug("Module-level permission found (inheritance), granting permission");
                    return true;
                }
            }
        }

        if (permissionType == PermissionType.COMPONENT) {
            // Check PAGE-level permission (inheritance)
            // Extract page key from resource identifier (format: "module.page.component")
            String[] parts = resourceIdentifier.split("\\.");
            if (parts.length >= 2) {
                String pageKey = parts[0] + "." + parts[1];
                for (GroupPermission perm : permissions) {
                    if (perm.getPermissionType() == PermissionType.PAGE
                            && perm.getResourceType().equals(resourceType)
                            && perm.getResourceIdentifier().equals(pageKey)
                            && perm.isGranted()
                            && hasActionAccess(perm.getAction(), action)) {
                        log.debug("Page-level permission found (inheritance), granting permission");
                        return true;
                    }
                }
            }
        }

        log.debug("No matching permission found, denying");
        return false;
    }

    /**
     * Check if a permission matches the requested permission.
     * 
     * @param perm               Permission to check
     * @param permissionType     Requested permission type
     * @param resourceType       Requested resource type
     * @param resourceIdentifier Requested resource identifier
     * @param action             Requested action
     * @param exactMatch         If true, requires exact match. If false, allows
     *                           hierarchy inheritance.
     * @return true if permission matches
     */
    private boolean matchesPermission(GroupPermission perm, PermissionType permissionType,
            String resourceType, String resourceIdentifier, PermissionAction action, boolean exactMatch) {

        if (exactMatch) {
            // Exact match required
            return perm.getPermissionType() == permissionType
                    && perm.getResourceType().equals(resourceType)
                    && perm.getResourceIdentifier().equals(resourceIdentifier)
                    && hasActionAccess(perm.getAction(), action);
        } else {
            // Allow hierarchy inheritance
            if (permissionType == PermissionType.MODULE) {
                return perm.getPermissionType() == PermissionType.MODULE
                        && perm.getResourceType().equals(resourceType)
                        && hasActionAccess(perm.getAction(), action);
            } else if (permissionType == PermissionType.PAGE) {
                return (perm.getPermissionType() == PermissionType.PAGE
                        && perm.getResourceType().equals(resourceType)
                        && perm.getResourceIdentifier().equals(resourceIdentifier)
                        && hasActionAccess(perm.getAction(), action))
                        || (perm.getPermissionType() == PermissionType.MODULE
                                && perm.getResourceType().equals(resourceType)
                                && hasActionAccess(perm.getAction(), action));
            } else { // COMPONENT
                return (perm.getPermissionType() == PermissionType.COMPONENT
                        && perm.getResourceType().equals(resourceType)
                        && perm.getResourceIdentifier().equals(resourceIdentifier)
                        && hasActionAccess(perm.getAction(), action))
                        || (perm.getPermissionType() == PermissionType.PAGE
                                && perm.getResourceType().equals(resourceType)
                                && perm.getResourceIdentifier().startsWith(resourceIdentifier.split("\\.")[0] + ".")
                                && hasActionAccess(perm.getAction(), action))
                        || (perm.getPermissionType() == PermissionType.MODULE
                                && perm.getResourceType().equals(resourceType)
                                && hasActionAccess(perm.getAction(), action));
            }
        }
    }

    /**
     * Check if the permission action grants access to the requested action.
     * Highest permission wins: READ < WRITE < DELETE
     * EXECUTE is independent.
     * 
     * @param permissionAction Action in the permission
     * @param requestedAction  Requested action
     * @return true if permission action grants access to requested action
     */
    private boolean hasActionAccess(PermissionAction permissionAction, PermissionAction requestedAction) {
        if (permissionAction == requestedAction) {
            return true; // Exact match
        }

        // EXECUTE is independent
        if (permissionAction == PermissionAction.EXECUTE || requestedAction == PermissionAction.EXECUTE) {
            return permissionAction == requestedAction;
        }

        // Highest permission wins: DELETE > WRITE > READ
        // If user has DELETE, they have WRITE and READ
        // If user has WRITE, they have READ
        if (permissionAction == PermissionAction.DELETE) {
            return requestedAction == PermissionAction.READ || requestedAction == PermissionAction.WRITE;
        }
        if (permissionAction == PermissionAction.WRITE) {
            return requestedAction == PermissionAction.READ;
        }

        return false;
    }

    // ==========================================
    // Registry Validation & Constraints
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public RegistryValidationResponse validateRegistry() {
        log.debug("Validating permission registry");

        List<RegistryValidationResponse.ValidationIssue> issues = new ArrayList<>();

        // Check for orphaned pages (pages without valid modules)
        List<PermissionPage> allPages = permissionPageRepository.findAll();
        for (PermissionPage page : allPages) {
            if (!permissionModuleRepository.existsByModuleKey(page.getModuleKey())) {
                issues.add(RegistryValidationResponse.ValidationIssue.builder()
                        .type("ORPHANED_PAGE")
                        .severity("ERROR")
                        .message("Page has invalid or missing module: " + page.getModuleKey())
                        .resourceType("PAGE")
                        .resourceId(page.getId())
                        .resourceIdentifier(page.getModuleKey() + "." + page.getPageKey())
                        .details("Module key '" + page.getModuleKey() + "' does not exist")
                        .build());
            }
        }

        // Check for orphaned components (components without valid pages)
        List<PermissionComponent> allComponents = permissionComponentRepository.findAll();
        for (PermissionComponent component : allComponents) {
            String[] pageKeyParts = component.getPageKey().split("\\.");
            if (pageKeyParts.length != 2) {
                issues.add(RegistryValidationResponse.ValidationIssue.builder()
                        .type("INVALID_PAGE_KEY_FORMAT")
                        .severity("ERROR")
                        .message("Component has invalid page key format: " + component.getPageKey())
                        .resourceType("COMPONENT")
                        .resourceId(component.getId())
                        .resourceIdentifier(component.getPageKey() + "." + component.getComponentKey())
                        .details("Page key must be in format 'module.page'")
                        .build());
            } else {
                String moduleKey = pageKeyParts[0];
                String pageKeyOnly = pageKeyParts[1];
                if (!permissionPageRepository.existsByModuleKeyAndPageKey(moduleKey, pageKeyOnly)) {
                    issues.add(RegistryValidationResponse.ValidationIssue.builder()
                            .type("ORPHANED_COMPONENT")
                            .severity("ERROR")
                            .message("Component has invalid or missing page: " + component.getPageKey())
                            .resourceType("COMPONENT")
                            .resourceId(component.getId())
                            .resourceIdentifier(component.getPageKey() + "." + component.getComponentKey())
                            .details("Page '" + component.getPageKey() + "' does not exist")
                            .build());
                }
            }
        }

        // Check for duplicate route paths
        Map<String, List<PermissionPage>> routeMap = new HashMap<>();
        for (PermissionPage page : allPages) {
            if (page.getRoutePath() != null && !page.getRoutePath().isBlank()) {
                routeMap.computeIfAbsent(page.getRoutePath(), k -> new ArrayList<>()).add(page);
            }
        }
        for (Map.Entry<String, List<PermissionPage>> entry : routeMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                String pageKeys = entry.getValue().stream()
                        .map(p -> p.getModuleKey() + "." + p.getPageKey())
                        .collect(Collectors.joining(", "));
                for (PermissionPage page : entry.getValue()) {
                    issues.add(RegistryValidationResponse.ValidationIssue.builder()
                            .type("DUPLICATE_ROUTE")
                            .severity("WARNING")
                            .message("Duplicate route path: " + entry.getKey())
                            .resourceType("PAGE")
                            .resourceId(page.getId())
                            .resourceIdentifier(page.getModuleKey() + "." + page.getPageKey())
                            .details("Route path '" + entry.getKey() + "' is used by: " + pageKeys)
                            .build());
                }
            }
        }

        // Check for component type consistency (optional - can be enhanced)
        // This is a placeholder for future validation rules

        boolean isValid = issues.stream().noneMatch(issue -> "ERROR".equals(issue.getSeverity()));

        log.info("Registry validation completed. Valid: {}, Issues found: {}", isValid, issues.size());

        return RegistryValidationResponse.builder()
                .isValid(isValid)
                .issueCount(issues.size())
                .issues(issues)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RegistryHealthResponse checkRegistryHealth() {
        log.debug("Checking registry health");

        long moduleCount = permissionModuleRepository.count();
        long pageCount = permissionPageRepository.count();
        long componentCount = permissionComponentRepository.count();

        // Count orphaned pages
        long orphanedPageCount = permissionPageRepository.findAll().stream()
                .filter(page -> !permissionModuleRepository.existsByModuleKey(page.getModuleKey()))
                .count();

        // Count orphaned components
        long orphanedComponentCount = permissionComponentRepository.findAll().stream()
                .filter(component -> {
                    String[] pageKeyParts = component.getPageKey().split("\\.");
                    if (pageKeyParts.length != 2) {
                        return true;
                    }
                    return !permissionPageRepository.existsByModuleKeyAndPageKey(pageKeyParts[0], pageKeyParts[1]);
                })
                .count();

        // Count duplicate routes
        Map<String, Long> routeCounts = permissionPageRepository.findAll().stream()
                .filter(page -> page.getRoutePath() != null && !page.getRoutePath().isBlank())
                .collect(Collectors.groupingBy(PermissionPage::getRoutePath, Collectors.counting()));
        long duplicateRouteCount = routeCounts.values().stream()
                .filter(count -> count > 1)
                .count();

        // Determine health status
        String status;
        String message;
        if (orphanedPageCount > 0 || orphanedComponentCount > 0) {
            status = "UNHEALTHY";
            message = String.format("Registry has %d orphaned page(s) and %d orphaned component(s)",
                    orphanedPageCount, orphanedComponentCount);
        } else if (duplicateRouteCount > 0) {
            status = "DEGRADED";
            message = String.format("Registry has %d duplicate route path(s)", duplicateRouteCount);
        } else {
            status = "HEALTHY";
            message = "Registry is healthy with no issues detected";
        }

        log.info(
                "Registry health check completed. Status: {}, Orphaned pages: {}, Orphaned components: {}, Duplicate routes: {}",
                status, orphanedPageCount, orphanedComponentCount, duplicateRouteCount);

        return RegistryHealthResponse.builder()
                .status(status)
                .moduleCount(moduleCount)
                .pageCount(pageCount)
                .componentCount(componentCount)
                .orphanedPageCount(orphanedPageCount)
                .orphanedComponentCount(orphanedComponentCount)
                .duplicateRouteCount(duplicateRouteCount)
                .message(message)
                .checkedAt(java.time.LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public RegistryCleanupResponse cleanupOrphanedRecords() {
        log.debug("Cleaning up orphaned records");

        List<RegistryCleanupResponse.RemovedResource> removedResources = new ArrayList<>();
        int orphanedPagesRemoved = 0;
        int orphanedComponentsRemoved = 0;

        // Remove orphaned pages (pages without valid modules)
        List<PermissionPage> allPages = permissionPageRepository.findAll();
        for (PermissionPage page : allPages) {
            if (!permissionModuleRepository.existsByModuleKey(page.getModuleKey())) {
                removedResources.add(RegistryCleanupResponse.RemovedResource.builder()
                        .resourceType("PAGE")
                        .resourceId(page.getId())
                        .resourceIdentifier(page.getModuleKey() + "." + page.getPageKey())
                        .reason("Orphaned page: module '" + page.getModuleKey() + "' does not exist")
                        .build());
                permissionPageRepository.delete(page);
                orphanedPagesRemoved++;
                log.info("Removed orphaned page: {}.{} (module '{}' does not exist)",
                        page.getModuleKey(), page.getPageKey(), page.getModuleKey());
            }
        }

        // Remove orphaned components (components without valid pages)
        List<PermissionComponent> allComponents = permissionComponentRepository.findAll();
        for (PermissionComponent component : allComponents) {
            String[] pageKeyParts = component.getPageKey().split("\\.");
            boolean shouldRemove = false;
            String reason = "";

            if (pageKeyParts.length != 2) {
                shouldRemove = true;
                reason = "Invalid page key format: " + component.getPageKey();
            } else {
                String moduleKey = pageKeyParts[0];
                String pageKeyOnly = pageKeyParts[1];
                if (!permissionPageRepository.existsByModuleKeyAndPageKey(moduleKey, pageKeyOnly)) {
                    shouldRemove = true;
                    reason = "Orphaned component: page '" + component.getPageKey() + "' does not exist";
                }
            }

            if (shouldRemove) {
                removedResources.add(RegistryCleanupResponse.RemovedResource.builder()
                        .resourceType("COMPONENT")
                        .resourceId(component.getId())
                        .resourceIdentifier(component.getPageKey() + "." + component.getComponentKey())
                        .reason(reason)
                        .build());
                permissionComponentRepository.delete(component);
                orphanedComponentsRemoved++;
                log.info("Removed orphaned component: {}.{} ({})",
                        component.getPageKey(), component.getComponentKey(), reason);
            }
        }

        int totalRemoved = orphanedPagesRemoved + orphanedComponentsRemoved;
        boolean success = totalRemoved >= 0; // Always true, but tracks if cleanup ran

        String message = String.format("Cleanup completed. Removed %d orphaned page(s) and %d orphaned component(s)",
                orphanedPagesRemoved, orphanedComponentsRemoved);

        log.info("Registry cleanup completed. Removed {} orphaned record(s)", totalRemoved);

        return RegistryCleanupResponse.builder()
                .success(success)
                .orphanedPagesRemoved(orphanedPagesRemoved)
                .orphanedComponentsRemoved(orphanedComponentsRemoved)
                .totalRemoved(totalRemoved)
                .removedResources(removedResources)
                .message(message)
                .build();
    }

    // ==========================================
    // Bulk Operations
    // ==========================================

    @Override
    @Transactional
    public BulkOperationResponse<ModuleResponse> bulkCreateModules(BulkCreateModuleRequest request, Long createdBy) {
        log.debug("Bulk creating {} modules by user ID: {}", request.getModules().size(), createdBy);

        List<ModuleResponse> successfulItems = new ArrayList<>();
        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();

        for (int i = 0; i < request.getModules().size(); i++) {
            CreateModuleRequest moduleRequest = request.getModules().get(i);
            try {
                PermissionModule module = createModule(moduleRequest, createdBy);
                successfulItems.add(toModuleResponse(module));
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier(moduleRequest.getModuleKey())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to create module at index {}: {}", i, e.getMessage());
            }
        }

        int totalCount = request.getModules().size();
        int successCount = successfulItems.size();
        int failureCount = failures.size();

        log.info("Bulk module creation completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<ModuleResponse>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(successfulItems)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResponse<ModuleResponse> bulkUpdateModules(BulkUpdateModuleRequest request) {
        log.debug("Bulk updating {} modules", request.getModules().size());

        List<ModuleResponse> successfulItems = new ArrayList<>();
        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();

        for (int i = 0; i < request.getModules().size(); i++) {
            BulkUpdateModuleRequest.ModuleUpdate moduleUpdate = request.getModules().get(i);
            try {
                PermissionModule module = updateModule(moduleUpdate.getId(), moduleUpdate.getRequest());
                successfulItems.add(toModuleResponse(module));
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier("ID: " + moduleUpdate.getId())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to update module at index {} (ID: {}): {}", i, moduleUpdate.getId(),
                        e.getMessage());
            }
        }

        int totalCount = request.getModules().size();
        int successCount = successfulItems.size();
        int failureCount = failures.size();

        log.info("Bulk module update completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<ModuleResponse>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(successfulItems)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResponse<Void> bulkDeleteModules(BulkDeleteRequest request) {
        log.debug("Bulk deleting {} modules", request.getIds().size());

        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < request.getIds().size(); i++) {
            Long moduleId = request.getIds().get(i);
            try {
                deleteModule(moduleId);
                successCount++;
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier("ID: " + moduleId)
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to delete module at index {} (ID: {}): {}", i, moduleId, e.getMessage());
            }
        }

        int totalCount = request.getIds().size();
        int failureCount = failures.size();

        log.info("Bulk module deletion completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<Void>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(null) // No items returned for delete operations
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResponse<PageResponse> bulkCreatePages(BulkCreatePageRequest request) {
        log.debug("Bulk creating {} pages", request.getPages().size());

        List<PageResponse> successfulItems = new ArrayList<>();
        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();

        for (int i = 0; i < request.getPages().size(); i++) {
            CreatePageRequest pageRequest = request.getPages().get(i);
            try {
                PermissionPage page = createPage(pageRequest);
                successfulItems.add(toPageResponse(page));
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier(pageRequest.getModuleKey() + "." + pageRequest.getPageKey())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to create page at index {}: {}", i, e.getMessage());
            }
        }

        int totalCount = request.getPages().size();
        int successCount = successfulItems.size();
        int failureCount = failures.size();

        log.info("Bulk page creation completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<PageResponse>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(successfulItems)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResponse<PageResponse> bulkUpdatePages(BulkUpdatePageRequest request) {
        log.debug("Bulk updating {} pages", request.getPages().size());

        List<PageResponse> successfulItems = new ArrayList<>();
        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();

        for (int i = 0; i < request.getPages().size(); i++) {
            BulkUpdatePageRequest.PageUpdate pageUpdate = request.getPages().get(i);
            try {
                PermissionPage page = updatePage(pageUpdate.getId(), pageUpdate.getRequest());
                successfulItems.add(toPageResponse(page));
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier("ID: " + pageUpdate.getId())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to update page at index {} (ID: {}): {}", i, pageUpdate.getId(), e.getMessage());
            }
        }

        int totalCount = request.getPages().size();
        int successCount = successfulItems.size();
        int failureCount = failures.size();

        log.info("Bulk page update completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<PageResponse>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(successfulItems)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResponse<Void> bulkDeletePages(BulkDeleteRequest request) {
        log.debug("Bulk deleting {} pages", request.getIds().size());

        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < request.getIds().size(); i++) {
            Long pageId = request.getIds().get(i);
            try {
                deletePage(pageId);
                successCount++;
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier("ID: " + pageId)
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to delete page at index {} (ID: {}): {}", i, pageId, e.getMessage());
            }
        }

        int totalCount = request.getIds().size();
        int failureCount = failures.size();

        log.info("Bulk page deletion completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<Void>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(null)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResponse<ComponentResponse> bulkCreateComponents(BulkCreateComponentRequest request) {
        log.debug("Bulk creating {} components", request.getComponents().size());

        List<ComponentResponse> successfulItems = new ArrayList<>();
        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();

        for (int i = 0; i < request.getComponents().size(); i++) {
            CreateComponentRequest componentRequest = request.getComponents().get(i);
            try {
                PermissionComponent component = createComponent(componentRequest);
                successfulItems.add(toComponentResponse(component));
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier(componentRequest.getPageKey() + "." + componentRequest.getComponentKey())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to create component at index {}: {}", i, e.getMessage());
            }
        }

        int totalCount = request.getComponents().size();
        int successCount = successfulItems.size();
        int failureCount = failures.size();

        log.info("Bulk component creation completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<ComponentResponse>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(successfulItems)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResponse<ComponentResponse> bulkUpdateComponents(BulkUpdateComponentRequest request) {
        log.debug("Bulk updating {} components", request.getComponents().size());

        List<ComponentResponse> successfulItems = new ArrayList<>();
        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();

        for (int i = 0; i < request.getComponents().size(); i++) {
            BulkUpdateComponentRequest.ComponentUpdate componentUpdate = request.getComponents().get(i);
            try {
                PermissionComponent component = updateComponent(componentUpdate.getId(),
                        componentUpdate.getRequest());
                successfulItems.add(toComponentResponse(component));
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier("ID: " + componentUpdate.getId())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to update component at index {} (ID: {}): {}", i, componentUpdate.getId(),
                        e.getMessage());
            }
        }

        int totalCount = request.getComponents().size();
        int successCount = successfulItems.size();
        int failureCount = failures.size();

        log.info("Bulk component update completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<ComponentResponse>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(successfulItems)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResponse<Void> bulkDeleteComponents(BulkDeleteRequest request) {
        log.debug("Bulk deleting {} components", request.getIds().size());

        List<BulkOperationResponse.OperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < request.getIds().size(); i++) {
            Long componentId = request.getIds().get(i);
            try {
                deleteComponent(componentId);
                successCount++;
            } catch (Exception e) {
                failures.add(BulkOperationResponse.OperationFailure.builder()
                        .index(i)
                        .resourceIdentifier("ID: " + componentId)
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to delete component at index {} (ID: {}): {}", i, componentId, e.getMessage());
            }
        }

        int totalCount = request.getIds().size();
        int failureCount = failures.size();

        log.info("Bulk component deletion completed. Success: {}/{}, Failures: {}", successCount, totalCount,
                failureCount);

        return BulkOperationResponse.<Void>builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successfulItems(null)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RegistryExportResponse exportRegistry(String format, Long userId) {
        log.debug("Exporting registry in {} format by user ID: {}", format, userId);

        // Fetch all registry data
        List<PermissionModule> modules = permissionModuleRepository.findAll();
        List<PermissionPage> pages = permissionPageRepository.findAll();
        List<PermissionComponent> components = permissionComponentRepository.findAll();

        // Convert to DTOs
        List<ModuleResponse> moduleResponses = modules.stream()
                .map(this::toModuleResponse)
                .collect(Collectors.toList());
        List<PageResponse> pageResponses = pages.stream()
                .map(this::toPageResponse)
                .collect(Collectors.toList());
        List<ComponentResponse> componentResponses = components.stream()
                .map(this::toComponentResponse)
                .collect(Collectors.toList());

        RegistryExportResponse.RegistryData data = RegistryExportResponse.RegistryData.builder()
                .modules(moduleResponses)
                .pages(pageResponses)
                .components(componentResponses)
                .build();

        RegistryExportResponse.ExportMetadata metadata = RegistryExportResponse.ExportMetadata.builder()
                .exportedAt(java.time.LocalDateTime.now())
                .exportedBy(userId)
                .moduleCount(moduleResponses.size())
                .pageCount(pageResponses.size())
                .componentCount(componentResponses.size())
                .build();

        log.info("Registry export completed. Modules: {}, Pages: {}, Components: {}", moduleResponses.size(),
                pageResponses.size(), componentResponses.size());

        return RegistryExportResponse.builder()
                .format(format != null ? format.toUpperCase() : "JSON")
                .data(data)
                .metadata(metadata)
                .build();
    }

    @Override
    @Transactional
    public RegistryImportResponse importRegistry(RegistryImportRequest request, Long userId) {
        log.debug("Importing registry by user ID: {}, conflict resolution: {}", userId,
                request.getConflictResolution());

        int modulesImported = 0;
        int pagesImported = 0;
        int componentsImported = 0;
        int modulesSkipped = 0;
        int pagesSkipped = 0;
        int componentsSkipped = 0;
        List<RegistryImportResponse.ImportError> errors = new ArrayList<>();

        // Import modules
        for (CreateModuleRequest moduleRequest : request.getData().getModules()) {
            try {
                if (permissionModuleRepository.existsByModuleKey(moduleRequest.getModuleKey())) {
                    if (request.getConflictResolution() == RegistryImportRequest.ConflictResolution.SKIP) {
                        modulesSkipped++;
                        continue;
                    } else if (request.getConflictResolution() == RegistryImportRequest.ConflictResolution.OVERWRITE) {
                        // Find and update existing module
                        PermissionModule existing = permissionModuleRepository
                                .findByModuleKey(moduleRequest.getModuleKey()).orElse(null);
                        if (existing != null) {
                            UpdateModuleRequest updateRequest = UpdateModuleRequest.builder()
                                    .moduleName(moduleRequest.getModuleName())
                                    .description(moduleRequest.getDescription())
                                    .availableToRoles(moduleRequest.getAvailableToRoles())
                                    .build();
                            updateModule(existing.getId(), updateRequest);
                            modulesImported++;
                        }
                    }
                } else {
                    createModule(moduleRequest, userId);
                    modulesImported++;
                }
            } catch (Exception e) {
                errors.add(RegistryImportResponse.ImportError.builder()
                        .resourceType("MODULE")
                        .resourceIdentifier(moduleRequest.getModuleKey())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to import module: {}", e.getMessage());
            }
        }

        // Import pages
        for (CreatePageRequest pageRequest : request.getData().getPages()) {
            try {
                if (permissionPageRepository.existsByModuleKeyAndPageKey(pageRequest.getModuleKey(),
                        pageRequest.getPageKey())) {
                    if (request.getConflictResolution() == RegistryImportRequest.ConflictResolution.SKIP) {
                        pagesSkipped++;
                        continue;
                    } else if (request.getConflictResolution() == RegistryImportRequest.ConflictResolution.OVERWRITE) {
                        // Find and update existing page
                        PermissionPage existing = permissionPageRepository
                                .findByModuleKeyAndPageKey(pageRequest.getModuleKey(), pageRequest.getPageKey())
                                .orElse(null);
                        if (existing != null) {
                            UpdatePageRequest updateRequest = UpdatePageRequest.builder()
                                    .pageName(pageRequest.getPageName())
                                    .routePath(pageRequest.getRoutePath())
                                    .description(pageRequest.getDescription())
                                    .build();
                            updatePage(existing.getId(), updateRequest);
                            pagesImported++;
                        }
                    }
                } else {
                    createPage(pageRequest);
                    pagesImported++;
                }
            } catch (Exception e) {
                errors.add(RegistryImportResponse.ImportError.builder()
                        .resourceType("PAGE")
                        .resourceIdentifier(pageRequest.getModuleKey() + "." + pageRequest.getPageKey())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to import page: {}", e.getMessage());
            }
        }

        // Import components
        for (CreateComponentRequest componentRequest : request.getData().getComponents()) {
            try {
                String[] pageKeyParts = componentRequest.getPageKey().split("\\.");
                if (pageKeyParts.length != 2) {
                    throw new IllegalArgumentException("Invalid page key format: " + componentRequest.getPageKey());
                }

                if (permissionComponentRepository.existsByPageKeyAndComponentKey(componentRequest.getPageKey(),
                        componentRequest.getComponentKey())) {
                    if (request.getConflictResolution() == RegistryImportRequest.ConflictResolution.SKIP) {
                        componentsSkipped++;
                        continue;
                    } else if (request.getConflictResolution() == RegistryImportRequest.ConflictResolution.OVERWRITE) {
                        // Find and update existing component
                        PermissionComponent existing = permissionComponentRepository
                                .findByPageKeyAndComponentKey(componentRequest.getPageKey(),
                                        componentRequest.getComponentKey())
                                .orElse(null);
                        if (existing != null) {
                            UpdateComponentRequest updateRequest = UpdateComponentRequest.builder()
                                    .componentName(componentRequest.getComponentName())
                                    .componentType(componentRequest.getComponentType())
                                    .description(componentRequest.getDescription())
                                    .build();
                            updateComponent(existing.getId(), updateRequest);
                            componentsImported++;
                        }
                    }
                } else {
                    createComponent(componentRequest);
                    componentsImported++;
                }
            } catch (Exception e) {
                errors.add(RegistryImportResponse.ImportError.builder()
                        .resourceType("COMPONENT")
                        .resourceIdentifier(componentRequest.getPageKey() + "." + componentRequest.getComponentKey())
                        .error(e.getMessage())
                        .errorType(e.getClass().getSimpleName())
                        .build());
                log.warn("Failed to import component: {}", e.getMessage());
            }
        }

        int totalImported = modulesImported + pagesImported + componentsImported;
        int totalSkipped = modulesSkipped + pagesSkipped + componentsSkipped;
        boolean success = errors.isEmpty();

        String message = String.format(
                "Import completed. Imported: %d modules, %d pages, %d components. Skipped: %d modules, %d pages, %d components. Errors: %d",
                modulesImported, pagesImported, componentsImported, modulesSkipped, pagesSkipped,
                componentsSkipped, errors.size());

        log.info("Registry import completed. Imported: {}, Skipped: {}, Errors: {}", totalImported, totalSkipped,
                errors.size());

        return RegistryImportResponse.builder()
                .success(success)
                .modulesImported(modulesImported)
                .pagesImported(pagesImported)
                .componentsImported(componentsImported)
                .modulesSkipped(modulesSkipped)
                .pagesSkipped(pagesSkipped)
                .componentsSkipped(componentsSkipped)
                .totalImported(totalImported)
                .totalSkipped(totalSkipped)
                .errors(errors)
                .message(message)
                .build();
    }

    // ==========================================
    // Helper Methods for DTO Conversion
    // ==========================================

    /**
     * Convert PermissionModule to ModuleResponse DTO.
     */
    private ModuleResponse toModuleResponse(PermissionModule module) {
        return ModuleResponse.builder()
                .id(module.getId())
                .moduleKey(module.getModuleKey())
                .moduleName(module.getModuleName())
                .description(module.getDescription())
                .availableToRoles(module.getAvailableToRoles())
                .createdAt(module.getCreatedAt())
                .build();
    }

    /**
     * Convert PermissionPage to PageResponse DTO.
     * Made package-private for use in controller.
     */
    PageResponse toPageResponse(PermissionPage page) {
        return PageResponse.builder()
                .id(page.getId())
                .moduleKey(page.getModuleKey())
                .pageKey(page.getPageKey())
                .pageName(page.getPageName())
                .routePath(page.getRoutePath())
                .description(page.getDescription())
                .createdAt(page.getCreatedAt())
                .build();
    }

    /**
     * Convert PermissionComponent to ComponentResponse DTO.
     * Made package-private for use in controller.
     */
    ComponentResponse toComponentResponse(PermissionComponent component) {
        return ComponentResponse.builder()
                .id(component.getId())
                .pageKey(component.getPageKey())
                .componentKey(component.getComponentKey())
                .componentName(component.getComponentName())
                .componentType(component.getComponentType())
                .description(component.getDescription())
                .createdAt(component.getCreatedAt())
                .build();
    }

    // ==========================================
    // Search & Filtering
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ModuleResponse> searchModules(String query,
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Searching modules with query: '{}', page: {}, size: {}", query, pageable.getPageNumber(),
                pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionModule> modules = permissionModuleRepository.searchModules(query,
                pageable);

        return modules.map(this::toModuleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ModuleResponse> filterModulesByRole(String role,
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Filtering modules by role: '{}', page: {}, size: {}", role, pageable.getPageNumber(),
                pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionModule> modules = permissionModuleRepository
                .findByAvailableToRolesContaining(role, pageable);

        return modules.map(this::toModuleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PageResponse> searchPages(String query,
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Searching pages with query: '{}', page: {}, size: {}", query, pageable.getPageNumber(),
                pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionPage> pages = permissionPageRepository.searchPages(query,
                pageable);

        return pages.map(this::toPageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PageResponse> filterPagesByModule(String moduleKey,
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Filtering pages by module: '{}', page: {}, size: {}", moduleKey, pageable.getPageNumber(),
                pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionPage> pages = permissionPageRepository.findByModuleKey(moduleKey,
                pageable);

        return pages.map(this::toPageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ComponentResponse> searchComponents(String query,
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Searching components with query: '{}', page: {}, size: {}", query, pageable.getPageNumber(),
                pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionComponent> components = permissionComponentRepository
                .searchComponents(query, pageable);

        return components.map(this::toComponentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ComponentResponse> filterComponentsByPage(String pageKey,
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Filtering components by page: '{}', page: {}, size: {}", pageKey, pageable.getPageNumber(),
                pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionComponent> components = permissionComponentRepository
                .findByPageKey(pageKey, pageable);

        return components.map(this::toComponentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ComponentResponse> filterComponentsByType(String componentType,
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Filtering components by type: '{}', page: {}, size: {}", componentType, pageable.getPageNumber(),
                pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionComponent> components = permissionComponentRepository
                .findByComponentType(componentType, pageable);

        return components.map(this::toComponentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ModuleResponse> getAllModules(
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Getting all modules, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionModule> modules = permissionModuleRepository.findAll(pageable);
        return modules.map(this::toModuleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PageResponse> getAllPages(
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Getting all pages, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionPage> pages = permissionPageRepository.findAll(pageable);
        return pages.map(this::toPageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ComponentResponse> getAllComponents(
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Getting all components, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        org.springframework.data.domain.Page<PermissionComponent> components = permissionComponentRepository
                .findAll(pageable);
        return components.map(this::toComponentResponse);
    }

    /**
     * Invalidate cache for all users in a group.
     * Helper method to invalidate user caches when group permissions change.
     */
    private void invalidateGroupCache(Long groupId) {
        try {
            List<UserGroup> userGroups = userGroupRepository.findByGroupId(groupId);
            for (UserGroup userGroup : userGroups) {
                permissionCacheService.invalidateUserPermissions(userGroup.getUser().getId());
            }
            permissionCacheService.invalidateGroupPermissions(groupId);
        } catch (Exception e) {
            log.warn("Failed to invalidate group cache for group ID: {}", groupId, e);
        }
    }
}
