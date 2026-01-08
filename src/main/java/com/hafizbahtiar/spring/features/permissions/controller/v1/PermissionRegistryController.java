package com.hafizbahtiar.spring.features.permissions.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.permissions.dto.BulkCreateComponentRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkCreateModuleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkCreatePageRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkDeleteRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkOperationResponse;
import com.hafizbahtiar.spring.features.permissions.dto.BulkUpdateComponentRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkUpdateModuleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.BulkUpdatePageRequest;
import com.hafizbahtiar.spring.features.permissions.dto.ComponentResponse;
import com.hafizbahtiar.spring.features.permissions.dto.CreateComponentRequest;
import com.hafizbahtiar.spring.features.permissions.dto.CreateModuleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.CreatePageRequest;
import com.hafizbahtiar.spring.features.permissions.dto.ModuleResponse;
import com.hafizbahtiar.spring.features.permissions.dto.PageResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryCleanupResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryExportResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryHealthResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryImportRequest;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryImportResponse;
import com.hafizbahtiar.spring.features.permissions.dto.RegistryValidationResponse;
import com.hafizbahtiar.spring.features.permissions.dto.UpdateComponentRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UpdateModuleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.UpdatePageRequest;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionComponent;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionModule;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionPage;
import com.hafizbahtiar.spring.features.permissions.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for permission registry endpoints.
 * Provides access to available modules, pages, and components for permission
 * assignment.
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionRegistryController {

    private final PermissionService permissionService;
    private final com.hafizbahtiar.spring.features.permissions.service.PermissionLoggingService permissionLoggingService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Get available modules that the current user can assign to groups
     * GET /api/v1/permissions/modules
     * Requires: Authenticated user
     */
    @GetMapping("/modules")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> getAvailableModules() {
        Long userId = getCurrentUserId();
        log.debug("Fetching available modules for user ID: {}", userId);
        List<PermissionModule> modules = permissionService.getAvailableModules(userId);
        List<ModuleResponse> response = modules.stream()
                .map(this::toModuleResponse)
                .collect(Collectors.toList());
        return ResponseUtils.ok(response);
    }

    /**
     * Get all pages for a specific module
     * GET /api/v1/permissions/modules/{key}/pages
     * Requires: Authenticated user
     */
    @GetMapping("/modules/{key}/pages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PageResponse>>> getModulePages(
            @PathVariable String key) {
        log.debug("Fetching pages for module: {}", key);
        List<PermissionPage> pages = permissionService.getModulePages(key);
        List<PageResponse> response = pages.stream()
                .map(this::toPageResponse)
                .collect(Collectors.toList());
        return ResponseUtils.ok(response);
    }

    /**
     * Get all components for a specific page
     * GET /api/v1/permissions/pages/{key}/components
     * Requires: Authenticated user
     */
    @GetMapping("/pages/{key}/components")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ComponentResponse>>> getPageComponents(
            @PathVariable String key) {
        log.debug("Fetching components for page: {}", key);
        List<PermissionComponent> components = permissionService.getPageComponents(key);
        List<ComponentResponse> response = components.stream()
                .map(this::toComponentResponse)
                .collect(Collectors.toList());
        return ResponseUtils.ok(response);
    }

    /**
     * Create a new permission module
     * POST /api/v1/permissions/modules
     * Requires: OWNER role only
     */
    @PostMapping("/modules")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ModuleResponse>> createModule(
            @Valid @RequestBody CreateModuleRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Creating permission module: {} by user ID: {}", request.getModuleKey(), userId);

        long startTime = System.currentTimeMillis();
        try {
            com.hafizbahtiar.spring.features.permissions.entity.PermissionModule module = permissionService
                    .createModule(request, userId);
            ModuleResponse response = toModuleResponse(module);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log module creation
            permissionLoggingService.logModuleCreated(module.getId(), userId, module.getModuleKey(),
                    module.getModuleName(), httpRequest, responseTime);

            log.info("Permission module created successfully with ID: {} and key: {}", module.getId(),
                    module.getModuleKey());
            return ResponseUtils.created(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            permissionLoggingService.logModuleEvent("MODULE_CREATED", null, userId, request.getModuleKey(),
                    request.getModuleName(), httpRequest, responseTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing permission module
     * PUT /api/v1/permissions/modules/{id}
     * Requires: OWNER role only
     */
    @PutMapping("/modules/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ModuleResponse>> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateModuleRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Updating permission module ID: {} by user ID: {}", id, userId);

        long startTime = System.currentTimeMillis();
        try {
            com.hafizbahtiar.spring.features.permissions.entity.PermissionModule module = permissionService
                    .updateModule(id, request);
            ModuleResponse response = toModuleResponse(module);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log module update
            permissionLoggingService.logModuleUpdated(module.getId(), userId, module.getModuleKey(),
                    module.getModuleName(), httpRequest, responseTime);

            log.info("Permission module updated successfully with ID: {} and key: {}", module.getId(),
                    module.getModuleKey());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            permissionLoggingService.logModuleEvent("MODULE_UPDATED", id, userId, null, null, httpRequest,
                    responseTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a permission module
     * DELETE /api/v1/permissions/modules/{id}
     * Requires: OWNER role only
     */
    @DeleteMapping("/modules/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Deleting permission module ID: {} by user ID: {}", id, userId);

        long startTime = System.currentTimeMillis();
        try {
            // Get module info before deletion for logging
            com.hafizbahtiar.spring.features.permissions.entity.PermissionModule module = permissionService
                    .getModuleById(id);

            String moduleKey = module.getModuleKey();
            String moduleName = module.getModuleName();

            permissionService.deleteModule(id);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log module deletion
            permissionLoggingService.logModuleDeleted(id, userId, moduleKey, moduleName, httpRequest, responseTime);

            log.info("Permission module deleted successfully with ID: {}", id);
            return ResponseUtils.noContent();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            permissionLoggingService.logModuleEvent("MODULE_DELETED", id, userId, null, null, httpRequest,
                    responseTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * Convert PermissionModule entity to ModuleResponse DTO
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
     * Convert PermissionPage entity to PageResponse DTO
     */
    private PageResponse toPageResponse(PermissionPage page) {
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
     * Convert PermissionComponent entity to ComponentResponse DTO
     */
    private ComponentResponse toComponentResponse(PermissionComponent component) {
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

    /**
     * Create a new permission page
     * POST /api/v1/permissions/modules/{moduleKey}/pages
     * Requires: OWNER or ADMIN role
     */
    @PostMapping("/modules/{moduleKey}/pages")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse>> createPage(
            @PathVariable String moduleKey,
            @Valid @RequestBody CreatePageRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Creating permission page: {}.{} by user ID: {}", moduleKey, request.getPageKey(), userId);

        // Ensure moduleKey in path matches request
        request.setModuleKey(moduleKey);

        long startTime = System.currentTimeMillis();
        try {
            PermissionPage page = permissionService.createPage(request);
            PageResponse response = toPageResponse(page);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log page creation
            permissionLoggingService.logPageCreated(page.getId(), userId, page.getModuleKey(), page.getPageKey(),
                    page.getPageName(), httpRequest, responseTime);

            log.info("Permission page created successfully with ID: {} and key: {}.{}", page.getId(),
                    page.getModuleKey(), page.getPageKey());
            return ResponseUtils.created(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            permissionLoggingService.logPageEvent("PAGE_CREATED", null, userId, moduleKey, request.getPageKey(),
                    request.getPageName(), httpRequest, responseTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing permission page
     * PUT /api/v1/permissions/pages/{id}
     * Requires: OWNER or ADMIN role
     */
    @PutMapping("/pages/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse>> updatePage(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePageRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Updating permission page ID: {} by user ID: {}", id, userId);

        long startTime = System.currentTimeMillis();
        try {
            PermissionPage page = permissionService.updatePage(id, request);
            PageResponse response = toPageResponse(page);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log page update
            permissionLoggingService.logPageUpdated(page.getId(), userId, page.getModuleKey(), page.getPageKey(),
                    page.getPageName(), httpRequest, responseTime);

            log.info("Permission page updated successfully with ID: {} and key: {}.{}", page.getId(),
                    page.getModuleKey(), page.getPageKey());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            try {
                PermissionPage page = permissionService.getPageById(id);
                permissionLoggingService.logPageEvent("PAGE_UPDATED", id, userId, page.getModuleKey(),
                        page.getPageKey(), page.getPageName(), httpRequest, responseTime, false, e.getMessage());
            } catch (Exception ex) {
                permissionLoggingService.logPageEvent("PAGE_UPDATED", id, userId, null, null, null, httpRequest,
                        responseTime, false, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Delete a permission page
     * DELETE /api/v1/permissions/pages/{id}
     * Requires: OWNER or ADMIN role
     */
    @DeleteMapping("/pages/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePage(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Deleting permission page ID: {} by user ID: {}", id, userId);

        long startTime = System.currentTimeMillis();
        try {
            // Get page info before deletion for logging
            PermissionPage page = permissionService.getPageById(id);

            String moduleKey = page.getModuleKey();
            String pageKey = page.getPageKey();
            String pageName = page.getPageName();

            permissionService.deletePage(id);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log page deletion
            permissionLoggingService.logPageDeleted(id, userId, moduleKey, pageKey, pageName, httpRequest,
                    responseTime);

            log.info("Permission page deleted successfully with ID: {}", id);
            return ResponseUtils.noContent();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            try {
                PermissionPage page = permissionService.getPageById(id);
                permissionLoggingService.logPageEvent("PAGE_DELETED", id, userId, page.getModuleKey(),
                        page.getPageKey(), page.getPageName(), httpRequest, responseTime, false, e.getMessage());
            } catch (Exception ex) {
                permissionLoggingService.logPageEvent("PAGE_DELETED", id, userId, null, null, null, httpRequest,
                        responseTime, false, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Create a new permission component
     * POST /api/v1/permissions/pages/{pageKey}/components
     * Requires: OWNER or ADMIN role
     */
    @PostMapping("/pages/{pageKey}/components")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComponentResponse>> createComponent(
            @PathVariable String pageKey,
            @Valid @RequestBody CreateComponentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Creating permission component: {}.{} by user ID: {}", pageKey, request.getComponentKey(), userId);

        // Ensure pageKey in path matches request
        request.setPageKey(pageKey);

        long startTime = System.currentTimeMillis();
        try {
            PermissionComponent component = permissionService.createComponent(request);
            ComponentResponse response = toComponentResponse(component);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log component creation
            permissionLoggingService.logComponentCreated(component.getId(), userId, component.getPageKey(),
                    component.getComponentKey(), component.getComponentName(), httpRequest, responseTime);

            log.info("Permission component created successfully with ID: {} and key: {}.{}", component.getId(),
                    component.getPageKey(), component.getComponentKey());
            return ResponseUtils.created(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            permissionLoggingService.logComponentEvent("COMPONENT_CREATED", null, userId, pageKey,
                    request.getComponentKey(), request.getComponentName(), httpRequest, responseTime, false,
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing permission component
     * PUT /api/v1/permissions/components/{id}
     * Requires: OWNER or ADMIN role
     */
    @PutMapping("/components/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComponentResponse>> updateComponent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComponentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Updating permission component ID: {} by user ID: {}", id, userId);

        long startTime = System.currentTimeMillis();
        try {
            PermissionComponent component = permissionService.updateComponent(id, request);
            ComponentResponse response = toComponentResponse(component);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log component update
            permissionLoggingService.logComponentUpdated(component.getId(), userId, component.getPageKey(),
                    component.getComponentKey(), component.getComponentName(), httpRequest, responseTime);

            log.info("Permission component updated successfully with ID: {} and key: {}.{}", component.getId(),
                    component.getPageKey(), component.getComponentKey());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            try {
                PermissionComponent component = permissionService.getComponentById(id);
                permissionLoggingService.logComponentEvent("COMPONENT_UPDATED", id, userId, component.getPageKey(),
                        component.getComponentKey(), component.getComponentName(), httpRequest, responseTime, false,
                        e.getMessage());
            } catch (Exception ex) {
                permissionLoggingService.logComponentEvent("COMPONENT_UPDATED", id, userId, null, null, null,
                        httpRequest, responseTime, false, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Delete a permission component
     * DELETE /api/v1/permissions/components/{id}
     * Requires: OWNER or ADMIN role
     */
    @DeleteMapping("/components/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteComponent(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Deleting permission component ID: {} by user ID: {}", id, userId);

        long startTime = System.currentTimeMillis();
        try {
            // Get component info before deletion for logging
            PermissionComponent component = permissionService.getComponentById(id);

            String pageKey = component.getPageKey();
            String componentKey = component.getComponentKey();
            String componentName = component.getComponentName();

            permissionService.deleteComponent(id);
            long responseTime = System.currentTimeMillis() - startTime;

            // Log component deletion
            permissionLoggingService.logComponentDeleted(id, userId, pageKey, componentKey, componentName,
                    httpRequest, responseTime);

            log.info("Permission component deleted successfully with ID: {}", id);
            return ResponseUtils.noContent();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            // Log failure
            try {
                PermissionComponent component = permissionService.getComponentById(id);
                permissionLoggingService.logComponentEvent("COMPONENT_DELETED", id, userId, component.getPageKey(),
                        component.getComponentKey(), component.getComponentName(), httpRequest, responseTime, false,
                        e.getMessage());
            } catch (Exception ex) {
                permissionLoggingService.logComponentEvent("COMPONENT_DELETED", id, userId, null, null, null,
                        httpRequest, responseTime, false, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Validate the entire permission registry
     * GET /api/v1/permissions/registry/validate
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/registry/validate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RegistryValidationResponse>> validateRegistry(
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Validating registry by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            RegistryValidationResponse response = permissionService.validateRegistry();
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Registry validation completed in {}ms. Valid: {}, Issues: {}", responseTime,
                    response.getIsValid(), response.getIssueCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to validate registry after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Check the health of the permission registry
     * GET /api/v1/permissions/registry/health
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/registry/health")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RegistryHealthResponse>> checkRegistryHealth(
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Checking registry health by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            RegistryHealthResponse response = permissionService.checkRegistryHealth();
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Registry health check completed in {}ms. Status: {}", responseTime, response.getStatus());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to check registry health after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Clean up orphaned records from the registry
     * POST /api/v1/permissions/registry/cleanup
     * Requires: OWNER role only
     */
    @PostMapping("/registry/cleanup")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<RegistryCleanupResponse>> cleanupOrphanedRecords(
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Cleaning up orphaned records by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            RegistryCleanupResponse response = permissionService.cleanupOrphanedRecords();
            long responseTime = System.currentTimeMillis() - startTime;

            // Log cleanup event
            permissionLoggingService.logRegistryCleanup(userId, response.getTotalRemoved(),
                    response.getOrphanedPagesRemoved(), response.getOrphanedComponentsRemoved(),
                    httpRequest, responseTime);

            log.info("Registry cleanup completed in {}ms. Removed {} record(s)", responseTime,
                    response.getTotalRemoved());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to cleanup orphaned records after {}ms", responseTime, e);
            throw e;
        }
    }

    // ==========================================
    // Bulk Operations Endpoints
    // ==========================================

    /**
     * Bulk create permission modules
     * POST /api/v1/permissions/modules/bulk
     * Requires: OWNER role only
     */
    @PostMapping("/modules/bulk")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<ModuleResponse>>> bulkCreateModules(
            @Valid @RequestBody BulkCreateModuleRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk creating modules by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<ModuleResponse> response = permissionService.bulkCreateModules(request, userId);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk module creation completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk create modules after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Bulk update permission modules
     * PUT /api/v1/permissions/modules/bulk
     * Requires: OWNER role only
     */
    @PutMapping("/modules/bulk")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<ModuleResponse>>> bulkUpdateModules(
            @Valid @RequestBody BulkUpdateModuleRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk updating modules by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<ModuleResponse> response = permissionService.bulkUpdateModules(request);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk module update completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk update modules after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Bulk delete permission modules
     * DELETE /api/v1/permissions/modules/bulk
     * Requires: OWNER role only
     */
    @DeleteMapping("/modules/bulk")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<Void>>> bulkDeleteModules(
            @Valid @RequestBody BulkDeleteRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk deleting modules by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<Void> response = permissionService.bulkDeleteModules(request);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk module deletion completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk delete modules after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Bulk create permission pages
     * POST /api/v1/permissions/pages/bulk
     * Requires: OWNER or ADMIN role
     */
    @PostMapping("/pages/bulk")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<PageResponse>>> bulkCreatePages(
            @Valid @RequestBody BulkCreatePageRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk creating pages by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<PageResponse> response = permissionService.bulkCreatePages(request);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk page creation completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk create pages after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Bulk update permission pages
     * PUT /api/v1/permissions/pages/bulk
     * Requires: OWNER or ADMIN role
     */
    @PutMapping("/pages/bulk")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<PageResponse>>> bulkUpdatePages(
            @Valid @RequestBody BulkUpdatePageRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk updating pages by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<PageResponse> response = permissionService.bulkUpdatePages(request);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk page update completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk update pages after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Bulk delete permission pages
     * DELETE /api/v1/permissions/pages/bulk
     * Requires: OWNER or ADMIN role
     */
    @DeleteMapping("/pages/bulk")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<Void>>> bulkDeletePages(
            @Valid @RequestBody BulkDeleteRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk deleting pages by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<Void> response = permissionService.bulkDeletePages(request);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk page deletion completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk delete pages after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Bulk create permission components
     * POST /api/v1/permissions/components/bulk
     * Requires: OWNER or ADMIN role
     */
    @PostMapping("/components/bulk")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<ComponentResponse>>> bulkCreateComponents(
            @Valid @RequestBody BulkCreateComponentRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk creating components by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<ComponentResponse> response = permissionService.bulkCreateComponents(request);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk component creation completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk create components after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Bulk update permission components
     * PUT /api/v1/permissions/components/bulk
     * Requires: OWNER or ADMIN role
     */
    @PutMapping("/components/bulk")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<ComponentResponse>>> bulkUpdateComponents(
            @Valid @RequestBody BulkUpdateComponentRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk updating components by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<ComponentResponse> response = permissionService.bulkUpdateComponents(request);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk component update completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk update components after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Bulk delete permission components
     * DELETE /api/v1/permissions/components/bulk
     * Requires: OWNER or ADMIN role
     */
    @DeleteMapping("/components/bulk")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BulkOperationResponse<Void>>> bulkDeleteComponents(
            @Valid @RequestBody BulkDeleteRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Bulk deleting components by user ID: {}", userId);

        long startTime = System.currentTimeMillis();
        try {
            BulkOperationResponse<Void> response = permissionService.bulkDeleteComponents(request);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Bulk component deletion completed in {}ms. Success: {}/{}, Failures: {}", responseTime,
                    response.getSuccessCount(), response.getTotalCount(), response.getFailureCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to bulk delete components after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Export the entire permission registry
     * GET /api/v1/permissions/registry/export?format=JSON
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/registry/export")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RegistryExportResponse>> exportRegistry(
            @RequestParam(defaultValue = "JSON") String format, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Exporting registry in {} format by user ID: {}", format, userId);

        long startTime = System.currentTimeMillis();
        try {
            RegistryExportResponse response = permissionService.exportRegistry(format, userId);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Registry export completed in {}ms. Modules: {}, Pages: {}, Components: {}", responseTime,
                    response.getMetadata().getModuleCount(), response.getMetadata().getPageCount(),
                    response.getMetadata().getComponentCount());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to export registry after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Import permission registry from data
     * POST /api/v1/permissions/registry/import
     * Requires: OWNER role only
     */
    @PostMapping("/registry/import")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<RegistryImportResponse>> importRegistry(
            @Valid @RequestBody RegistryImportRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Importing registry by user ID: {}, conflict resolution: {}", userId,
                request.getConflictResolution());

        long startTime = System.currentTimeMillis();
        try {
            RegistryImportResponse response = permissionService.importRegistry(request, userId);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Registry import completed in {}ms. Imported: {}, Skipped: {}, Errors: {}", responseTime,
                    response.getTotalImported(), response.getTotalSkipped(), response.getErrors().size());
            return ResponseUtils.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to import registry after {}ms", responseTime, e);
            throw e;
        }
    }

    // ==========================================
    // Search & Filtering Endpoints
    // ==========================================

    /**
     * Search permission modules
     * GET /api/v1/permissions/modules/search?q={query}
     * Requires: Authenticated user
     */
    @GetMapping("/modules/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.hafizbahtiar.spring.common.dto.PaginatedResponse<ModuleResponse>>> searchModules(
            @RequestParam("q") String query,
            @PageableDefault(size = 20, sort = "moduleName") Pageable pageable,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Searching modules with query: '{}' by user ID: {}", query, userId);

        long startTime = System.currentTimeMillis();
        try {
            org.springframework.data.domain.Page<ModuleResponse> page = permissionService.searchModules(query,
                    pageable);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Module search completed in {}ms. Found {} results", responseTime, page.getTotalElements());
            return com.hafizbahtiar.spring.common.util.ResponseUtils.okPage(page);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to search modules after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Filter permission modules by available roles
     * GET /api/v1/permissions/modules?availableToRoles={role}
     * Requires: Authenticated user
     */
    @GetMapping("/modules")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.hafizbahtiar.spring.common.dto.PaginatedResponse<ModuleResponse>>> getModules(
            @RequestParam(required = false) String availableToRoles,
            @PageableDefault(size = 20, sort = "moduleName") Pageable pageable,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Getting modules by user ID: {}, filter by role: {}", userId, availableToRoles);

        long startTime = System.currentTimeMillis();
        try {
            org.springframework.data.domain.Page<ModuleResponse> page;
            if (availableToRoles != null && !availableToRoles.isBlank()) {
                page = permissionService.filterModulesByRole(availableToRoles, pageable);
            } else {
                page = permissionService.getAllModules(pageable);
            }
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Modules retrieved in {}ms. Found {} results", responseTime, page.getTotalElements());
            return com.hafizbahtiar.spring.common.util.ResponseUtils.okPage(page);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to get modules after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Search permission pages
     * GET /api/v1/permissions/pages/search?q={query}
     * Requires: Authenticated user
     */
    @GetMapping("/pages/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.hafizbahtiar.spring.common.dto.PaginatedResponse<PageResponse>>> searchPages(
            @RequestParam("q") String query,
            @PageableDefault(size = 20, sort = "pageName") Pageable pageable,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Searching pages with query: '{}' by user ID: {}", query, userId);

        long startTime = System.currentTimeMillis();
        try {
            org.springframework.data.domain.Page<PageResponse> page = permissionService.searchPages(query, pageable);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Page search completed in {}ms. Found {} results", responseTime, page.getTotalElements());
            return com.hafizbahtiar.spring.common.util.ResponseUtils.okPage(page);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to search pages after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Filter permission pages by module key
     * GET /api/v1/permissions/pages?moduleKey={key}
     * Requires: Authenticated user
     */
    @GetMapping("/pages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.hafizbahtiar.spring.common.dto.PaginatedResponse<PageResponse>>> getPages(
            @RequestParam(required = false) String moduleKey,
            @PageableDefault(size = 20, sort = "pageName") Pageable pageable,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Getting pages by user ID: {}, filter by module: {}", userId, moduleKey);

        long startTime = System.currentTimeMillis();
        try {
            org.springframework.data.domain.Page<PageResponse> page;
            if (moduleKey != null && !moduleKey.isBlank()) {
                page = permissionService.filterPagesByModule(moduleKey, pageable);
            } else {
                page = permissionService.getAllPages(pageable);
            }
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Pages retrieved in {}ms. Found {} results", responseTime, page.getTotalElements());
            return com.hafizbahtiar.spring.common.util.ResponseUtils.okPage(page);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to get pages after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Search permission components
     * GET /api/v1/permissions/components/search?q={query}
     * Requires: Authenticated user
     */
    @GetMapping("/components/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.hafizbahtiar.spring.common.dto.PaginatedResponse<ComponentResponse>>> searchComponents(
            @RequestParam("q") String query,
            @PageableDefault(size = 20, sort = "componentName") Pageable pageable,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Searching components with query: '{}' by user ID: {}", query, userId);

        long startTime = System.currentTimeMillis();
        try {
            org.springframework.data.domain.Page<ComponentResponse> page = permissionService.searchComponents(query,
                    pageable);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Component search completed in {}ms. Found {} results", responseTime, page.getTotalElements());
            return com.hafizbahtiar.spring.common.util.ResponseUtils.okPage(page);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to search components after {}ms", responseTime, e);
            throw e;
        }
    }

    /**
     * Filter permission components by page key or component type
     * GET /api/v1/permissions/components?pageKey={key}&componentType={type}
     * Requires: Authenticated user
     */
    @GetMapping("/components")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.hafizbahtiar.spring.common.dto.PaginatedResponse<ComponentResponse>>> getComponents(
            @RequestParam(required = false) String pageKey,
            @RequestParam(required = false) String componentType,
            @PageableDefault(size = 20, sort = "componentName") Pageable pageable,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Getting components by user ID: {}, filter by page: {}, type: {}", userId, pageKey, componentType);

        long startTime = System.currentTimeMillis();
        try {
            org.springframework.data.domain.Page<ComponentResponse> page;
            if (pageKey != null && !pageKey.isBlank()) {
                page = permissionService.filterComponentsByPage(pageKey, pageable);
            } else if (componentType != null && !componentType.isBlank()) {
                page = permissionService.filterComponentsByType(componentType, pageable);
            } else {
                page = permissionService.getAllComponents(pageable);
            }
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Components retrieved in {}ms. Found {} results", responseTime, page.getTotalElements());
            return com.hafizbahtiar.spring.common.util.ResponseUtils.okPage(page);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to get components after {}ms", responseTime, e);
            throw e;
        }
    }
}
