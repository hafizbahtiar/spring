package com.hafizbahtiar.spring.features.permissions.config;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionComponent;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionModule;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionPage;
import com.hafizbahtiar.spring.features.permissions.repository.PermissionComponentRepository;
import com.hafizbahtiar.spring.features.permissions.repository.PermissionModuleRepository;
import com.hafizbahtiar.spring.features.permissions.repository.PermissionPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes permission registry tables (modules, pages, components).
 * Seeds the database with available permission resources.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after DataInitializer (owner user must exist first)
public class PermissionRegistryInitializer implements CommandLineRunner {

    private final PermissionModuleRepository permissionModuleRepository;
    private final PermissionPageRepository permissionPageRepository;
    private final PermissionComponentRepository permissionComponentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting permission registry initialization...");

        initializePermissionModules();
        initializePermissionPages();
        initializePermissionComponents();

        log.info("Permission registry initialization completed.");
    }

    /**
     * Initialize permission modules.
     */
    private void initializePermissionModules() {
        List<PermissionModule> modules = new ArrayList<>();

        // Support Module
        modules.add(createModule("support", "Support", "Customer support and help desk module", "OWNER,ADMIN"));

        // Finance Module
        modules.add(createModule("finance", "Finance", "Financial management and accounting module", "OWNER,ADMIN"));

        // Portfolio Module
        modules.add(createModule("portfolio", "Portfolio", "Portfolio management module", "OWNER"));

        // Admin Module
        modules.add(createModule("admin", "Admin", "System administration module", "OWNER,ADMIN"));

        // Blog Module
        modules.add(createModule("blog", "Blog", "Blog management module", "OWNER"));

        // Navigation Module
        modules.add(createModule("navigation", "Navigation", "Navigation menu management module", "OWNER"));

        // User Management Module
        modules.add(createModule("users", "User Management", "User and role management module", "OWNER,ADMIN"));

        // Settings Module
        modules.add(createModule("settings", "Settings", "Application settings module", "OWNER,ADMIN"));

        // Save modules (skip if already exists)
        int createdCount = 0;
        int existingCount = 0;
        for (PermissionModule module : modules) {
            if (!permissionModuleRepository.existsByModuleKey(module.getModuleKey())) {
                permissionModuleRepository.save(module);
                createdCount++;
            } else {
                existingCount++;
            }
        }
        if (createdCount > 0 || existingCount > 0) {
            log.info("Permission modules: {} created, {} already existed", createdCount, existingCount);
        }

        log.info("Initialized {} permission modules", modules.size());
    }

    /**
     * Initialize permission pages.
     */
    private void initializePermissionPages() {
        List<PermissionPage> pages = new ArrayList<>();

        // Support Module Pages
        pages.add(createPage("support", "chat", "Support Chat", "/support/chat", "Customer support chat interface"));
        pages.add(createPage("support", "tickets", "Support Tickets", "/support/tickets", "Support ticket management"));
        pages.add(
                createPage("support", "knowledge", "Knowledge Base", "/support/knowledge", "Knowledge base articles"));

        // Finance Module Pages
        pages.add(createPage("finance", "dashboard", "Finance Dashboard", "/finance/dashboard",
                "Financial overview dashboard"));
        pages.add(createPage("finance", "transactions", "Transactions", "/finance/transactions",
                "Transaction management"));
        pages.add(createPage("finance", "reports", "Financial Reports", "/finance/reports", "Financial reporting"));

        // Portfolio Module Pages
        pages.add(createPage("portfolio", "profile", "Profile", "/portfolio/profile", "Portfolio profile management"));
        pages.add(createPage("portfolio", "blog", "Blog", "/portfolio/blog", "Blog post management"));
        pages.add(createPage("portfolio", "projects", "Projects", "/portfolio/projects", "Project management"));
        pages.add(createPage("portfolio", "experiences", "Experiences", "/portfolio/experiences",
                "Work experience management"));

        // Admin Module Pages
        pages.add(createPage("admin", "dashboard", "Admin Dashboard", "/admin/dashboard", "Administrative dashboard"));
        pages.add(createPage("admin", "health", "System Health", "/admin/health", "System health monitoring"));
        pages.add(createPage("admin", "metrics", "Metrics", "/admin/metrics", "System metrics"));
        pages.add(createPage("admin", "queues", "Queues", "/admin/queues", "Background job queues"));
        pages.add(createPage("admin", "cron-jobs", "Cron Jobs", "/admin/cron-jobs", "Scheduled job management"));
        pages.add(createPage("admin", "navigation", "Navigation", "/admin/navigation", "Navigation menu management"));

        // User Management Module Pages
        pages.add(createPage("users", "list", "User List", "/admin/users", "User management"));
        pages.add(createPage("users", "roles", "Roles", "/admin/roles", "Role management"));
        pages.add(createPage("users", "groups", "Permission Groups", "/admin/permissions/groups",
                "Permission group management"));

        // Settings Module Pages
        pages.add(createPage("settings", "profile", "Profile Settings", "/settings/profile", "User profile settings"));
        pages.add(createPage("settings", "security", "Security Settings", "/settings/security",
                "Security and password settings"));
        pages.add(createPage("settings", "preferences", "Preferences", "/settings/preferences", "User preferences"));

        // Save pages (skip if already exists)
        int createdCount = 0;
        int existingCount = 0;
        for (PermissionPage page : pages) {
            if (!permissionPageRepository.existsByModuleKeyAndPageKey(page.getModuleKey(), page.getPageKey())) {
                permissionPageRepository.save(page);
                createdCount++;
            } else {
                existingCount++;
            }
        }
        if (createdCount > 0 || existingCount > 0) {
            log.info("Permission pages: {} created, {} already existed", createdCount, existingCount);
        }

        log.info("Initialized {} permission pages", pages.size());
    }

    /**
     * Initialize permission components.
     */
    private void initializePermissionComponents() {
        List<PermissionComponent> components = new ArrayList<>();

        // Support Chat Components
        components.add(createComponent("support.chat", "send_message", "Send Message", "BUTTON"));
        components.add(createComponent("support.chat", "delete_message", "Delete Message", "BUTTON"));
        components.add(createComponent("support.chat", "edit_message", "Edit Message", "BUTTON"));
        components.add(createComponent("support.chat", "export_chat", "Export Chat", "BUTTON"));

        // Support Tickets Components
        components.add(createComponent("support.tickets", "create_ticket", "Create Ticket", "BUTTON"));
        components.add(createComponent("support.tickets", "edit_ticket", "Edit Ticket", "BUTTON"));
        components.add(createComponent("support.tickets", "delete_ticket", "Delete Ticket", "BUTTON"));
        components.add(createComponent("support.tickets", "assign_ticket", "Assign Ticket", "BUTTON"));
        components.add(createComponent("support.tickets", "close_ticket", "Close Ticket", "BUTTON"));

        // Finance Transactions Components
        components.add(createComponent("finance.transactions", "create_transaction", "Create Transaction", "BUTTON"));
        components.add(createComponent("finance.transactions", "edit_transaction", "Edit Transaction", "BUTTON"));
        components.add(createComponent("finance.transactions", "delete_transaction", "Delete Transaction", "BUTTON"));
        components.add(createComponent("finance.transactions", "export_transactions", "Export Transactions", "BUTTON"));

        // Portfolio Blog Components
        components.add(createComponent("portfolio.blog", "create_post", "Create Post", "BUTTON"));
        components.add(createComponent("portfolio.blog", "edit_post", "Edit Post", "BUTTON"));
        components.add(createComponent("portfolio.blog", "delete_post", "Delete Post", "BUTTON"));
        components.add(createComponent("portfolio.blog", "publish_post", "Publish Post", "BUTTON"));

        // Portfolio Projects Components
        components.add(createComponent("portfolio.projects", "create_project", "Create Project", "BUTTON"));
        components.add(createComponent("portfolio.projects", "edit_project", "Edit Project", "BUTTON"));
        components.add(createComponent("portfolio.projects", "delete_project", "Delete Project", "BUTTON"));

        // Admin Navigation Components
        components.add(createComponent("admin.navigation", "create_menu_item", "Create Menu Item", "BUTTON"));
        components.add(createComponent("admin.navigation", "edit_menu_item", "Edit Menu Item", "BUTTON"));
        components.add(createComponent("admin.navigation", "delete_menu_item", "Delete Menu Item", "BUTTON"));
        components.add(createComponent("admin.navigation", "reorder_menu_items", "Reorder Menu Items", "BUTTON"));

        // User Management Components
        components.add(createComponent("users.list", "create_user", "Create User", "BUTTON"));
        components.add(createComponent("users.list", "edit_user", "Edit User", "BUTTON"));
        components.add(createComponent("users.list", "delete_user", "Delete User", "BUTTON"));
        components.add(createComponent("users.list", "change_role", "Change Role", "BUTTON"));

        // Permission Groups Components
        components.add(createComponent("users.groups", "create_group", "Create Group", "BUTTON"));
        components.add(createComponent("users.groups", "edit_group", "Edit Group", "BUTTON"));
        components.add(createComponent("users.groups", "delete_group", "Delete Group", "BUTTON"));
        components.add(createComponent("users.groups", "assign_permissions", "Assign Permissions", "BUTTON"));
        components.add(createComponent("users.groups", "assign_users", "Assign Users", "BUTTON"));

        // Save components (skip if already exists)
        int createdCount = 0;
        int existingCount = 0;
        for (PermissionComponent component : components) {
            if (!permissionComponentRepository.existsByPageKeyAndComponentKey(
                    component.getPageKey(), component.getComponentKey())) {
                permissionComponentRepository.save(component);
                createdCount++;
            } else {
                existingCount++;
            }
        }
        if (createdCount > 0 || existingCount > 0) {
            log.info("Permission components: {} created, {} already existed", createdCount, existingCount);
        }

        log.info("Initialized {} permission components", components.size());
    }

    /**
     * Helper method to create a PermissionModule.
     */
    private PermissionModule createModule(String moduleKey, String moduleName, String description,
            String availableToRoles) {
        PermissionModule module = new PermissionModule();
        module.setModuleKey(moduleKey);
        module.setModuleName(moduleName);
        module.setDescription(description);
        module.setAvailableToRoles(availableToRoles);
        return module;
    }

    /**
     * Helper method to create a PermissionPage.
     */
    private PermissionPage createPage(String moduleKey, String pageKey, String pageName, String routePath,
            String description) {
        PermissionPage page = new PermissionPage();
        page.setModuleKey(moduleKey);
        page.setPageKey(pageKey);
        page.setPageName(pageName);
        page.setRoutePath(routePath);
        page.setDescription(description);
        return page;
    }

    /**
     * Helper method to create a PermissionComponent.
     */
    private PermissionComponent createComponent(String pageKey, String componentKey, String componentName,
            String componentType) {
        PermissionComponent component = new PermissionComponent();
        component.setPageKey(pageKey);
        component.setComponentKey(componentKey);
        component.setComponentName(componentName);
        component.setComponentType(componentType);
        component.setDescription("Component: " + componentName);
        return component;
    }
}
