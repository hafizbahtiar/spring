package com.hafizbahtiar.spring.config;

import com.hafizbahtiar.spring.features.navigation.entity.NavigationMenuItem;
import com.hafizbahtiar.spring.features.navigation.repository.NavigationMenuItemRepository;
import com.hafizbahtiar.spring.features.permissions.dto.AddPermissionRequest;
import com.hafizbahtiar.spring.features.permissions.dto.CreateGroupRequest;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import com.hafizbahtiar.spring.features.permissions.repository.PermissionGroupRepository;
import com.hafizbahtiar.spring.features.permissions.service.PermissionService;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Data initializer component that runs on application startup.
 * Creates initial data if it doesn't exist:
 * - Owner user
 * - Navigation menu items
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run early, before other initializers
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final NavigationMenuItemRepository navigationMenuItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionService permissionService;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data initialization...");

        initializeOwnerUser();
        initializeNavigationMenuItems();
        initializePermissionGroups();

        log.info("Data initialization completed.");
    }

    /**
     * Initialize owner user if it doesn't exist.
     */
    private void initializeOwnerUser() {
        // Check if owner user already exists
        if (userRepository.existsOwner()) {
            log.info("Owner user already exists. Skipping owner creation.");
            return;
        }

        // Create owner user
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setUsername("owner");
        owner.setPasswordHash(passwordEncoder.encode("owner123")); // Change this in production!
        owner.setFirstName("System");
        owner.setLastName("Owner");
        owner.setRole("OWNER");
        owner.setEmailVerified(true);
        owner.setActive(true);

        userRepository.save(owner);
        log.info("Owner user created successfully: {}", owner.getEmail());
        log.warn("⚠️  IMPORTANT: Change the owner password in production!");
    }

    /**
     * Initialize navigation menu items if they don't exist.
     */
    private void initializeNavigationMenuItems() {
        // Check if menu items already exist
        long menuItemCount = navigationMenuItemRepository.count();
        if (menuItemCount > 0) {
            log.info("Navigation menu items already exist ({} items). Skipping menu initialization.", menuItemCount);
            return;
        }

        log.info("Creating navigation menu items...");

        // ==========================================
        // ALL ITEMS AS ROOT LEVEL (Level 0)
        // ==========================================

        List<NavigationMenuItem> menuItems = new ArrayList<>();

        // Dashboard - No group, at the top
        menuItems.add(createRootMenuItem(
                "Dashboard",
                "/dashboard",
                "home",
                "", // Empty group - will be displayed at top without group label
                0, // Display order 0 to ensure it's at the top
                null)); // Available to all authenticated users

        // Portfolio Group (all root level)
        menuItems.add(createRootMenuItem(
                "Profile",
                "/portfolio/profile",
                "userCircle",
                "Portfolio",
                1,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Blog",
                "/portfolio/blog",
                "fileText",
                "Portfolio",
                2,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Projects",
                "/portfolio/projects",
                "folder",
                "Portfolio",
                3,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Experiences",
                "/portfolio/experiences",
                "briefcase",
                "Portfolio",
                4,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Education",
                "/portfolio/education",
                "graduationCap",
                "Portfolio",
                5,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Certifications",
                "/portfolio/certifications",
                "award",
                "Portfolio",
                6,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Skills",
                "/portfolio/skills",
                "code",
                "Portfolio",
                7,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Testimonials",
                "/portfolio/testimonials",
                "messageSquare",
                "Portfolio",
                8,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Companies",
                "/portfolio/companies",
                "building",
                "Portfolio",
                9,
                "OWNER"));

        menuItems.add(createRootMenuItem(
                "Contacts",
                "/portfolio/contacts",
                "mail",
                "Portfolio",
                10,
                "OWNER"));

        // Settings Group (all root level)
        menuItems.add(createRootMenuItem(
                "Profile",
                "/settings/profile",
                "userCircle",
                "Settings",
                1,
                null));

        menuItems.add(createRootMenuItem(
                "Account",
                "/settings/account",
                "userCircle",
                "Settings",
                2,
                null));

        menuItems.add(createRootMenuItem(
                "Notifications",
                "/settings/notifications",
                "bell",
                "Settings",
                3,
                null));

        menuItems.add(createRootMenuItem(
                "Preferences",
                "/settings/preferences",
                "sliders",
                "Settings",
                4,
                null));

        menuItems.add(createRootMenuItem(
                "Security",
                "/settings/security",
                "shield",
                "Settings",
                5,
                null));

        menuItems.add(createRootMenuItem(
                "Sessions",
                "/settings/sessions",
                "monitor",
                "Settings",
                6,
                null));

        // Admin Group (all root level)
        // Admin can access: Health, Metrics, Queues, Cron Jobs
        // Owner can access: All (including Navigation management)
        // Note: Dashboard is shared for all users, filtered by role on the page itself
        menuItems.add(createRootMenuItem(
                "Navigation",
                "/navigation",
                "menu",
                "Admin",
                1,
                "OWNER")); // Only OWNER can access (menu management)

        menuItems.add(createRootMenuItem(
                "Health",
                "/admin/health",
                "activity",
                "Admin",
                2,
                "ADMIN")); // ADMIN and OWNER can access

        menuItems.add(createRootMenuItem(
                "Metrics",
                "/admin/metrics",
                "server",
                "Admin",
                3,
                "ADMIN")); // ADMIN and OWNER can access

        menuItems.add(createRootMenuItem(
                "Queues",
                "/admin/queues",
                "barChart3",
                "Admin",
                4,
                "ADMIN")); // ADMIN and OWNER can access

        menuItems.add(createRootMenuItem(
                "Cron Jobs",
                "/admin/cron-jobs",
                "clock",
                "Admin",
                5,
                "ADMIN")); // ADMIN and OWNER can access

        menuItems.add(createRootMenuItem(
                "Permission Groups",
                "/permissions/groups",
                "users",
                "Admin",
                6,
                "OWNER")); // Only OWNER can access (permission management)

        menuItems.add(createRootMenuItem(
                "Permission Modules",
                "/permissions/modules",
                "package",
                "Admin",
                7,
                "OWNER")); // Only OWNER can access (module management)

        menuItems.add(createRootMenuItem(
                "Permission Pages",
                "/permissions/pages",
                "fileText",
                "Admin",
                8,
                "ADMIN")); // OWNER and ADMIN can access (page management)

        menuItems.add(createRootMenuItem(
                "Permission Components",
                "/permissions/components",
                "puzzle",
                "Admin",
                9,
                "ADMIN")); // OWNER and ADMIN can access (component management)

        menuItems.add(createRootMenuItem(
                "Permission Registry",
                "/permissions/registry",
                "bookOpen",
                "Admin",
                10,
                "OWNER")); // Only OWNER can access (permission registry)

        // Save all menu items (all root level)
        navigationMenuItemRepository.saveAll(menuItems);
        log.info("Created {} navigation menu items successfully (all root level).", menuItems.size());
    }

    /**
     * Helper method to create a root NavigationMenuItem (level 0).
     */
    private NavigationMenuItem createRootMenuItem(
            String title,
            String url,
            String iconName,
            String groupLabel,
            int displayOrder,
            String requiredRole) {

        NavigationMenuItem item = new NavigationMenuItem();
        item.setTitle(title);
        item.setUrl(url);
        item.setIconName(iconName);
        item.setGroupLabel(groupLabel);
        item.setDisplayOrder(displayOrder);
        item.setLevel(0);
        item.setRequiredRole(requiredRole);
        item.setActive(true);

        return item;
    }

    /**
     * Initialize default permission groups if they don't exist.
     * Creates example groups for demonstration purposes.
     */
    private void initializePermissionGroups() {
        // Get owner user for creating groups
        User owner = userRepository.findOwner()
                .orElse(null);
        if (owner == null) {
            log.warn("Owner user not found. Skipping permission group initialization.");
            return;
        }

        // Check if groups already exist
        long groupCount = permissionGroupRepository.count();
        if (groupCount > 0) {
            log.info("Permission groups already exist ({} groups). Skipping group initialization.", groupCount);
            return;
        }

        log.info("Creating default permission groups...");

        // Create "Administrators" group with full admin access
        if (!permissionGroupRepository.existsByName("Administrators")) {
            CreateGroupRequest adminGroupRequest = CreateGroupRequest.builder()
                    .name("Administrators")
                    .description(
                            "Full system administration access. Can manage all admin features including health, metrics, queues, and cron jobs.")
                    .active(true)
                    .build();

            var adminGroup = permissionService.createGroup(adminGroupRequest, owner.getId());
            log.info("Created Administrators permission group: ID {}", adminGroup.getId());

            // Add admin module permissions
            addModulePermission(adminGroup.getId(), "admin", PermissionAction.READ);
            addModulePermission(adminGroup.getId(), "admin", PermissionAction.WRITE);
            addModulePermission(adminGroup.getId(), "admin", PermissionAction.DELETE);
            addModulePermission(adminGroup.getId(), "admin", PermissionAction.EXECUTE);

            log.info("Added admin module permissions to Administrators group");
        }

        // Create "Portfolio Managers" group with portfolio access
        if (!permissionGroupRepository.existsByName("Portfolio Managers")) {
            CreateGroupRequest portfolioGroupRequest = CreateGroupRequest.builder()
                    .name("Portfolio Managers")
                    .description(
                            "Can manage portfolio content including projects, blog posts, experiences, and skills.")
                    .active(true)
                    .build();

            var portfolioGroup = permissionService.createGroup(portfolioGroupRequest, owner.getId());
            log.info("Created Portfolio Managers permission group: ID {}", portfolioGroup.getId());

            // Add portfolio module permissions
            addModulePermission(portfolioGroup.getId(), "portfolio", PermissionAction.READ);
            addModulePermission(portfolioGroup.getId(), "portfolio", PermissionAction.WRITE);
            addModulePermission(portfolioGroup.getId(), "portfolio", PermissionAction.DELETE);

            // Add specific page permissions for portfolio
            // Page keys should be just "projects" and "blog", not "portfolio.projects" or "portfolio.blog"
            addPagePermission(portfolioGroup.getId(), "portfolio", "projects", PermissionAction.READ);
            addPagePermission(portfolioGroup.getId(), "portfolio", "projects", PermissionAction.WRITE);
            addPagePermission(portfolioGroup.getId(), "portfolio", "projects", PermissionAction.DELETE);

            addPagePermission(portfolioGroup.getId(), "portfolio", "blog", PermissionAction.READ);
            addPagePermission(portfolioGroup.getId(), "portfolio", "blog", PermissionAction.WRITE);
            addPagePermission(portfolioGroup.getId(), "portfolio", "blog", PermissionAction.DELETE);

            log.info("Added portfolio permissions to Portfolio Managers group");
        }

        // Create "Support Team" group with support module access
        if (!permissionGroupRepository.existsByName("Support Team")) {
            CreateGroupRequest supportGroupRequest = CreateGroupRequest.builder()
                    .name("Support Team")
                    .description("Can access support features including chat and ticket management.")
                    .active(true)
                    .build();

            var supportGroup = permissionService.createGroup(supportGroupRequest, owner.getId());
            log.info("Created Support Team permission group: ID {}", supportGroup.getId());

            // Add support module permissions
            addModulePermission(supportGroup.getId(), "support", PermissionAction.READ);
            addModulePermission(supportGroup.getId(), "support", PermissionAction.WRITE);
            addModulePermission(supportGroup.getId(), "support", PermissionAction.EXECUTE);

            log.info("Added support module permissions to Support Team group");
        }

        log.info("Permission group initialization completed.");
    }

    /**
     * Helper method to add a module-level permission to a group.
     */
    private void addModulePermission(Long groupId, String moduleKey, PermissionAction action) {
        try {
            AddPermissionRequest request = AddPermissionRequest.builder()
                    .permissionType(PermissionType.MODULE)
                    .resourceType(moduleKey)
                    .resourceIdentifier(moduleKey)
                    .action(action)
                    .granted(true)
                    .build();

            permissionService.addPermission(groupId, request);
        } catch (Exception e) {
            log.warn("Failed to add module permission {}:{} to group {}: {}", moduleKey, action, groupId,
                    e.getMessage());
        }
    }

    /**
     * Helper method to add a page-level permission to a group.
     */
    private void addPagePermission(Long groupId, String moduleKey, String pageKey, PermissionAction action) {
        try {
            AddPermissionRequest request = AddPermissionRequest.builder()
                    .permissionType(PermissionType.PAGE)
                    .resourceType(moduleKey)
                    .resourceIdentifier(pageKey)
                    .action(action)
                    .granted(true)
                    .build();

            permissionService.addPermission(groupId, request);
        } catch (Exception e) {
            log.warn("Failed to add page permission {}:{}:{} to group {}: {}", moduleKey, pageKey, action, groupId,
                    e.getMessage());
        }
    }

}
