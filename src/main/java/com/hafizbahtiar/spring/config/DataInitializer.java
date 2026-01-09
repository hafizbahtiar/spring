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
import com.hafizbahtiar.spring.features.portfolio.entity.*;
import com.hafizbahtiar.spring.features.portfolio.repository.*;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectRequest;
import com.hafizbahtiar.spring.features.portfolio.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
        private final SkillRepository skillRepository;
        private final ExperienceRepository experienceRepository;
        private final EducationRepository educationRepository;
        private final ProjectRepository projectRepository;
        private final ProjectService projectService;

        @Override
        @Transactional
        public void run(String... args) {
                log.info("Starting data initialization...");

                initializeOwnerUser();
                initializeNavigationMenuItems();
                initializePermissionGroups();
                initializePortfolioData();

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
                owner.setEmail("hafiz@hafizbahtiar.com");
                owner.setUsername("hafizbahtiar");
                owner.setPasswordHash(passwordEncoder.encode("Owner123!")); // Change this in production!
                owner.setFirstName("Hafiz");
                owner.setLastName("Bahtiar");
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
                        log.info("Navigation menu items already exist ({} items). Skipping menu initialization.",
                                        menuItemCount);

                        // Log existing groups for reference
                        List<String> existingGroups = navigationMenuItemRepository.findDistinctGroupLabels();
                        if (!existingGroups.isEmpty()) {
                                log.info("Existing navigation groups: {}", String.join(", ", existingGroups));
                        }
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
                // Admin can access: Health, Metrics, Queues, Cron Jobs (read-only monitoring)
                // Owner can access: All (including Navigation management, full CRUD for Cron
                // Jobs)
                // Note: Dashboard is shared for all users, filtered by role on the page itself
                // Note: Cron Jobs - Dynamic cron job management system (Phase 7 completed)
                // - Application-level jobs: Spring @Scheduled tasks
                // - Database-level jobs: PostgreSQL pg_cron extension
                // - Full CRUD API: /api/v1/cron-jobs (OWNER only)
                // - Management UI: /cron-jobs (OWNER only) - Full CRUD interface
                // - Monitoring UI: /admin/cron-jobs (ADMIN/OWNER) - Read-only monitoring
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

                // Cron Jobs parent (no page, just a parent for grouping)
                NavigationMenuItem cronJobsParent = createRootMenuItem(
                                "Cron Jobs",
                                "#", // No page, just a parent
                                "clock",
                                "Admin",
                                5,
                                "OWNER"); // Only OWNER can access (parent for cron job management)
                menuItems.add(cronJobsParent);

                // Permission parent (no page, just a parent for grouping)
                NavigationMenuItem permissionsParent = createRootMenuItem(
                                "Permissions",
                                "#", // No page, just a parent
                                "shield",
                                "Admin",
                                6,
                                "OWNER"); // Only OWNER can access (parent for permission management)
                menuItems.add(permissionsParent);

                // Save all root menu items first (including parents)
                // This will assign IDs to all items including the parent items
                List<NavigationMenuItem> savedRootItems = navigationMenuItemRepository.saveAll(menuItems);
                navigationMenuItemRepository.flush(); // Flush to ensure IDs are available
                log.info("Created {} root navigation menu items successfully.", savedRootItems.size());

                // Find the saved parent items by their titles (they now have IDs)
                NavigationMenuItem savedCronJobsParent = savedRootItems.stream()
                                .filter(item -> "Cron Jobs".equals(item.getTitle()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Cron Jobs parent not found after save"));

                NavigationMenuItem savedPermissionsParent = savedRootItems.stream()
                                .filter(item -> "Permissions".equals(item.getTitle()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "Permissions parent not found after save"));

                // Now create child items for Cron Jobs and Permissions
                List<NavigationMenuItem> childItems = new ArrayList<>();

                // Cron Jobs children
                childItems.add(createChildMenuItem(
                                "Management",
                                "/cron-jobs",
                                "clock",
                                "Admin",
                                1,
                                "OWNER",
                                savedCronJobsParent)); // Only OWNER can access (full CRUD management interface)

                childItems.add(createChildMenuItem(
                                "Monitoring",
                                "/admin/cron-jobs",
                                "activity",
                                "Admin",
                                2,
                                "ADMIN",
                                savedCronJobsParent)); // ADMIN and OWNER can access (read-only monitoring)

                // Permissions children
                childItems.add(createChildMenuItem(
                                "Groups",
                                "/permissions/groups",
                                "users",
                                "Admin",
                                1,
                                "OWNER",
                                savedPermissionsParent)); // Only OWNER can access (permission management)

                childItems.add(createChildMenuItem(
                                "Modules",
                                "/permissions/modules",
                                "package",
                                "Admin",
                                2,
                                "OWNER",
                                savedPermissionsParent)); // Only OWNER can access (module management)

                childItems.add(createChildMenuItem(
                                "Pages",
                                "/permissions/pages",
                                "fileText",
                                "Admin",
                                3,
                                "ADMIN",
                                savedPermissionsParent)); // OWNER and ADMIN can access (page management)

                childItems.add(createChildMenuItem(
                                "Components",
                                "/permissions/components",
                                "puzzle",
                                "Admin",
                                4,
                                "ADMIN",
                                savedPermissionsParent)); // OWNER and ADMIN can access (component management)

                childItems.add(createChildMenuItem(
                                "Registry",
                                "/permissions/registry",
                                "bookOpen",
                                "Admin",
                                5,
                                "OWNER",
                                savedPermissionsParent)); // Only OWNER can access (permission registry)

                // Save all child items
                navigationMenuItemRepository.saveAll(childItems);
                log.info("Created {} child navigation menu items successfully.", childItems.size());

                // Log summary of created groups
                List<String> createdGroups = navigationMenuItemRepository.findDistinctGroupLabels();
                if (!createdGroups.isEmpty()) {
                        log.info("Created navigation groups ({}): {}", createdGroups.size(),
                                        String.join(", ", createdGroups));

                        // Log item count per group
                        for (String group : createdGroups) {
                                long groupItemCount = navigationMenuItemRepository
                                                .findByGroupLabelAndActiveTrueOrderByDisplayOrderAsc(group).size();
                                log.debug("  - {}: {} items", group, groupItemCount);
                        }
                } else {
                        log.warn("No groups found after menu initialization. This may indicate an issue.");
                }
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
         * Helper method to create a child NavigationMenuItem (level 1).
         * The parent must be saved first before creating children.
         */
        private NavigationMenuItem createChildMenuItem(
                        String title,
                        String url,
                        String iconName,
                        String groupLabel,
                        int displayOrder,
                        String requiredRole,
                        NavigationMenuItem parent) {

                NavigationMenuItem item = new NavigationMenuItem();
                item.setTitle(title);
                item.setUrl(url);
                item.setIconName(iconName);
                item.setGroupLabel(groupLabel);
                item.setDisplayOrder(displayOrder);
                item.setRequiredRole(requiredRole);
                item.setActive(true);
                item.setParent(parent); // This will automatically set level to 1

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
                        log.info("Permission groups already exist ({} groups). Skipping group initialization.",
                                        groupCount);
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
                        // Page keys should be just "projects" and "blog", not "portfolio.projects" or
                        // "portfolio.blog"
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
                        log.warn("Failed to add page permission {}:{}:{} to group {}: {}", moduleKey, pageKey, action,
                                        groupId,
                                        e.getMessage());
                }
        }

        /**
         * Initialize portfolio data for owner user if it doesn't exist.
         * Creates initial skills, experiences, education, and projects.
         */
        private void initializePortfolioData() {
                // Get owner user
                User owner = userRepository.findOwner()
                                .orElse(null);
                if (owner == null) {
                        log.warn("Owner user not found. Skipping portfolio data initialization.");
                        return;
                }

                Long ownerId = owner.getId();

                // Check if portfolio data already exists
                long skillCount = skillRepository.countByUserId(ownerId);
                long projectCount = projectRepository.countByUserId(ownerId);
                if (skillCount > 0 || projectCount > 0) {
                        log.info("Portfolio data already exists ({} skills, {} projects). Skipping portfolio initialization.",
                                        skillCount, projectCount);
                        return;
                }

                log.info("Creating initial portfolio data for owner user...");

                // Initialize Skills
                initializeSkills(owner);

                // Initialize Experiences
                initializeExperiences(owner);

                // Initialize Education
                initializeEducation(owner);

                // Initialize Projects
                initializeProjects(owner);

                log.info("Portfolio data initialization completed.");
        }

        /**
         * Initialize skills for the owner user.
         */
        private void initializeSkills(User owner) {
                List<Skill> skills = new ArrayList<>();

                skills.add(createSkill(owner, "Flutter", SkillCategory.TECHNICAL, ProficiencyLevel.EXPERT,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/flutter/flutter-original.svg",
                                "Cross-platform mobile development", 1));
                skills.add(createSkill(owner, "Dart", SkillCategory.TECHNICAL, ProficiencyLevel.EXPERT,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/dart/dart-original.svg",
                                "Programming language for Flutter", 2));
                skills.add(createSkill(owner, "React Native", SkillCategory.TECHNICAL, ProficiencyLevel.ADVANCED,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg",
                                "Cross-platform mobile development", 3));
                skills.add(createSkill(owner, "Next.js", SkillCategory.TECHNICAL, ProficiencyLevel.ADVANCED,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nextjs/nextjs-original.svg",
                                "React framework for production", 4));
                skills.add(createSkill(owner, "Node.js", SkillCategory.TECHNICAL, ProficiencyLevel.ADVANCED,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nodejs/nodejs-original.svg",
                                "Backend JavaScript runtime", 5));
                skills.add(createSkill(owner, "PostgreSQL", SkillCategory.TECHNICAL, ProficiencyLevel.ADVANCED,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg",
                                "Relational database management system", 6));
                skills.add(createSkill(owner, "MongoDB", SkillCategory.TECHNICAL, ProficiencyLevel.INTERMEDIATE,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mongodb/mongodb-original.svg",
                                "NoSQL database", 7));
                skills.add(createSkill(owner, "Docker", SkillCategory.TECHNICAL, ProficiencyLevel.INTERMEDIATE,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg",
                                "Containerization platform", 8));
                skills.add(createSkill(owner, "Git", SkillCategory.TECHNICAL, ProficiencyLevel.ADVANCED,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg",
                                "Version control system", 9));
                skills.add(createSkill(owner, "Kotlin", SkillCategory.TECHNICAL, ProficiencyLevel.INTERMEDIATE,
                                "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kotlin/kotlin-original.svg",
                                "Android native development", 10));

                skillRepository.saveAll(skills);
                log.info("Created {} skills for owner user.", skills.size());
        }

        /**
         * Helper method to create a Skill entity.
         */
        private Skill createSkill(User owner, String name, SkillCategory category, ProficiencyLevel proficiency,
                        String icon, String description, int displayOrder) {
                Skill skill = new Skill();
                skill.setUser(owner);
                skill.setName(name);
                skill.setCategory(category);
                skill.setProficiency(proficiency);
                skill.setIcon(icon);
                skill.setDescription(description);
                skill.setDisplayOrder(displayOrder);
                skill.setIsActive(true);
                return skill;
        }

        /**
         * Initialize experiences for the owner user.
         */
        private void initializeExperiences(User owner) {
                List<Experience> experiences = new ArrayList<>();

                Experience exp1 = new Experience();
                exp1.setUser(owner);
                exp1.setCompany("Freelance Developer");
                exp1.setPosition("Flutter Developer");
                exp1.setDescription(
                                "Developing cross-platform mobile applications for various clients using Flutter and Dart. Building robust backend systems and APIs.");
                exp1.setStartDate(LocalDate.of(2020, 1, 1));
                exp1.setEndDate(null); // Current
                exp1.setAddress("Kuala Lumpur, Malaysia");
                exp1.setLatitude(3.1390);
                exp1.setLongitude(101.6869);
                exp1.setEmploymentType(EmploymentType.FREELANCE);
                exp1.setIsCurrent(true);
                exp1.setDisplayOrder(1);
                experiences.add(exp1);

                experienceRepository.saveAll(experiences);
                log.info("Created {} experiences for owner user.", experiences.size());
        }

        /**
         * Initialize education for the owner user.
         */
        private void initializeEducation(User owner) {
                List<Education> educations = new ArrayList<>();

                Education edu1 = new Education();
                edu1.setUser(owner);
                edu1.setInstitution("University");
                edu1.setDegree(DegreeType.BACHELOR);
                edu1.setFieldOfStudy("Computer Science");
                edu1.setDescription("Studied computer science with focus on software engineering and mobile development.");
                edu1.setStartDate(LocalDate.of(2015, 1, 1));
                edu1.setEndDate(LocalDate.of(2019, 12, 31));
                edu1.setGrade("First Class Honours");
                edu1.setAddress("Kuala Lumpur, Malaysia");
                edu1.setLatitude(3.1390);
                edu1.setLongitude(101.6869);
                edu1.setIsCurrent(false);
                edu1.setDisplayOrder(1);
                educations.add(edu1);

                educationRepository.saveAll(educations);
                log.info("Created {} education records for owner user.", educations.size());
        }

        /**
         * Initialize projects for the owner user.
         * Projects are based on portfolio-next/src/lib/projects.ts
         * Only non-null fields are set (no roadmap, no caseStudy if not in source)
         */
        private void initializeProjects(User owner) {
                Long ownerId = owner.getId();

                // Project 1: QIUBBX - Zero Commission Halal Marketplace
                ProjectRequest proj1Request = new ProjectRequest();
                proj1Request.setTitle("QIUBBX - Zero Commission Halal Marketplace");
                proj1Request.setDescription(
                                "A zero-commission Halal marketplace platform designed to empower businesses by eliminating traditional fees associated with e-commerce. The platform democratizes global commerce, enabling sellers to thrive without the burden of commissions while providing AI-powered analytics, global payment support, and integrated logistics.");
                proj1Request.setTechnologies(new ArrayList<>(Arrays.asList("Next.js", "TypeScript", "Tailwind CSS", "React", "AI Analytics", "Payment Integration", "Progressive Web App")));
                proj1Request.setGithubUrl(null); // Empty in source
                proj1Request.setLiveUrl("https://qiubbx.com");
                proj1Request.setImages(new ArrayList<>(Arrays.asList(
                                "https://www.qiubbx.com/_next/image?url=%2Fassets%2Fimages%2Flogos%2Flogo_text_light.png&w=256&q=75")));
                proj1Request.setPlatform(PlatformType.WEB);
                proj1Request.setStartDate(LocalDate.of(2025, 1, 1));
                proj1Request.setEndDate(LocalDate.of(2025, 12, 31));
                proj1Request.setType(ProjectType.BUSINESS);
                proj1Request.setStatus(ProjectStatus.COMPLETED);
                proj1Request.setIsFeatured(true);
                proj1Request.setDisplayOrder(1);
                projectService.createProject(ownerId, proj1Request);

                // Project 2: Invois - Invoice Management App
                ProjectRequest proj2Request = new ProjectRequest();
                proj2Request.setTitle("Invois - Invoice Management App");
                proj2Request.setDescription(
                                "A clean, lightweight mobile app built using Flutter to help freelancers, small business owners, and entrepreneurs create and manage invoices on the go. The goal is to simplify billing processes with an intuitive, fast, and minimal interface — even for non-tech-savvy users. 100% functional without internet, optimized for performance and mobility.");
                proj2Request.setTechnologies(new ArrayList<>(Arrays.asList("Flutter", "Dart", "ObjectBox", "Riverpod", "Shared Preferences", "ProGuard", "PDF Generation", "Android")));
                proj2Request.setGithubUrl("https://github.com/hafizbahtiar/invoice");
                proj2Request.setLiveUrl("https://play.google.com/store/apps/details?id=com.invois");
                proj2Request.setImages(new ArrayList<>(Arrays.asList("/images/projects/invois/invois.png")));
                proj2Request.setPlatform(PlatformType.ANDROID);
                proj2Request.setStartDate(LocalDate.of(2025, 1, 1));
                proj2Request.setEndDate(LocalDate.of(2025, 12, 31));
                proj2Request.setType(ProjectType.HOBBY);
                proj2Request.setStatus(ProjectStatus.COMPLETED);
                proj2Request.setIsFeatured(true);
                proj2Request.setDisplayOrder(2);
                projectService.createProject(ownerId, proj2Request);

                // Project 3: ePerolehan DBKL
                ProjectRequest proj3Request = new ProjectRequest();
                proj3Request.setTitle("ePerolehan DBKL");
                proj3Request.setDescription(
                                "Government procurement platform for DBKL that streamlines the tender process. Registered users can browse, purchase tenders, and monitor project progress through an intuitive dashboard.");
                proj3Request.setTechnologies(new ArrayList<>(Arrays.asList("Laravel", "Flutter", "PostgreSQL", "Postman", "RESTful API")));
                proj3Request.setGithubUrl(null); // Empty in source
                proj3Request.setLiveUrl(null); // Empty in source
                proj3Request.setImages(new ArrayList<>(Arrays.asList(
                                "https://eperolehan.dbkl.gov.my/assets-metronic/media/logos/open-sidebar-logo_headerhome.png")));
                proj3Request.setPlatform(PlatformType.MULTI_PLATFORM);
                proj3Request.setStartDate(LocalDate.of(2024, 1, 1));
                proj3Request.setEndDate(null); // In progress
                proj3Request.setType(ProjectType.WORK);
                proj3Request.setStatus(ProjectStatus.IN_PROGRESS);
                proj3Request.setIsFeatured(true);
                proj3Request.setDisplayOrder(3);
                projectService.createProject(ownerId, proj3Request);

                // Project 4: CIT - Full Manage Receipt Book
                ProjectRequest proj4Request = new ProjectRequest();
                proj4Request.setTitle("CIT - Full Manage Receipt Book");
                proj4Request.setDescription(
                                "A comprehensive web application for security service providers to manage Cash-In-Transit (CIT) orders. The system digitizes the entire receipt book process, enabling every transaction and receipt page to be tracked and audited efficiently for compliance.");
                proj4Request.setTechnologies(new ArrayList<>(Arrays.asList("CodeIgniter 3", "PostgreSQL", "Kotlin", "Postman", "RESTful API", "Docker")));
                proj4Request.setGithubUrl(null); // Empty in source
                proj4Request.setLiveUrl(null); // Empty in source
                proj4Request.setImages(new ArrayList<>(Arrays.asList(
                                "https://cit.securiforce.net/app-assets/logo/9a6cefbceff3cf135b0b90cc9058c0c1.png")));
                proj4Request.setPlatform(PlatformType.MULTI_PLATFORM);
                proj4Request.setStartDate(LocalDate.of(2023, 1, 1));
                proj4Request.setEndDate(LocalDate.of(2023, 12, 31));
                proj4Request.setType(ProjectType.WORK);
                proj4Request.setStatus(ProjectStatus.COMPLETED);
                proj4Request.setIsFeatured(false);
                proj4Request.setDisplayOrder(4);
                projectService.createProject(ownerId, proj4Request);

                // Project 5: Jom Dapur - Food Delivery Platform
                ProjectRequest proj5Request = new ProjectRequest();
                proj5Request.setTitle("Jom Dapur - Food Delivery Platform");
                proj5Request.setDescription(
                                "A comprehensive food delivery platform similar to Grab and Foodpanda, connecting local restaurants with customers. Built with React Native for mobile apps and Node.js/Express.js for backend API. The system enables seamless online food ordering, real-time delivery tracking, and integrated payment processing for a complete food delivery experience.");
                proj5Request.setTechnologies(new ArrayList<>(Arrays.asList("React Native", "Node.js", "Express.js", "MongoDB", "Socket.io", "Stripe Payment", "Google Maps API")));
                proj5Request.setGithubUrl(null); // Empty in source
                proj5Request.setLiveUrl(null); // Empty in source
                proj5Request.setImages(new ArrayList<>(Arrays.asList("/images/projects/jom-dapur.jpg")));
                proj5Request.setPlatform(PlatformType.MULTI_PLATFORM);
                proj5Request.setStartDate(LocalDate.of(2021, 1, 1));
                proj5Request.setEndDate(LocalDate.of(2021, 12, 31));
                proj5Request.setType(ProjectType.WORK);
                proj5Request.setStatus(ProjectStatus.COMPLETED);
                proj5Request.setIsFeatured(false);
                proj5Request.setDisplayOrder(5);
                projectService.createProject(ownerId, proj5Request);

                // Project 6: JD Management - Administrative Dashboard
                ProjectRequest proj6Request = new ProjectRequest();
                proj6Request.setTitle("JD Management - Administrative Dashboard");
                proj6Request.setDescription(
                                "Comprehensive administrative management system for the Jom Dapur food delivery platform. Built with React for frontend and Node.js/Express.js for backend API. Provides operational oversight, real-time analytics, performance monitoring, and investor dashboards. Enables data-driven decision making with detailed KPIs, financial reporting, and business intelligence tools.");
                proj6Request.setTechnologies(new ArrayList<>(Arrays.asList("React", "Node.js", "Express.js", "MongoDB", "Chart.js", "Socket.io", "JWT Authentication", "Role-based Access Control")));
                proj6Request.setGithubUrl(null); // Empty in source
                proj6Request.setLiveUrl(null); // Empty in source
                proj6Request.setImages(new ArrayList<>(Arrays.asList("/images/projects/jd-management.png")));
                proj6Request.setPlatform(PlatformType.WEB);
                proj6Request.setStartDate(LocalDate.of(2021, 1, 1));
                proj6Request.setEndDate(LocalDate.of(2021, 12, 31));
                proj6Request.setType(ProjectType.WORK);
                proj6Request.setStatus(ProjectStatus.COMPLETED);
                proj6Request.setIsFeatured(false);
                proj6Request.setDisplayOrder(6);
                projectService.createProject(ownerId, proj6Request);

                // Project 7: JD Delivery - Rider Mobile App
                ProjectRequest proj7Request = new ProjectRequest();
                proj7Request.setTitle("JD Delivery - Rider Mobile App");
                proj7Request.setDescription(
                                "Mobile application for delivery riders/drivers in the Jom Dapur food delivery ecosystem. Built with React Native and connected to Node.js/Express.js backend API. Provides real-time order management, GPS navigation, earnings tracking, and communication tools. Enables efficient delivery operations with route optimization, customer notifications, and performance analytics for delivery personnel.");
                proj7Request.setTechnologies(new ArrayList<>(Arrays.asList("React Native", "Node.js", "Express.js", "MongoDB", "Google Maps API", "Socket.io", "Push Notifications", "Geolocation Services")));
                proj7Request.setGithubUrl(null); // Empty in source
                proj7Request.setLiveUrl(null); // Empty in source
                proj7Request.setImages(new ArrayList<>(Arrays.asList("/images/projects/jom-dapur.jpg")));
                proj7Request.setPlatform(PlatformType.ANDROID);
                proj7Request.setStartDate(LocalDate.of(2021, 1, 1));
                proj7Request.setEndDate(LocalDate.of(2021, 12, 31));
                proj7Request.setType(ProjectType.WORK);
                proj7Request.setStatus(ProjectStatus.COMPLETED);
                proj7Request.setIsFeatured(false);
                proj7Request.setDisplayOrder(7);
                projectService.createProject(ownerId, proj7Request);

                // Project 8: Wetrack System
                ProjectRequest proj8Request = new ProjectRequest();
                proj8Request.setTitle("Wetrack System");
                proj8Request.setDescription(
                                "As a security provider for cash-in-transit operations between cash centers and ATM branches, this system eliminates the need for technicians to call the command center for secure lock codes at each security layer. The system automates secure code delivery and tracking, improving operational efficiency and security.");
                proj8Request.setTechnologies(new ArrayList<>(Arrays.asList("CodeIgniter 3", "PostgreSQL", "Kotlin", "Postman", "RESTful API", "Docker")));
                proj8Request.setGithubUrl(null); // Empty in source
                proj8Request.setLiveUrl(null); // Empty in source
                proj8Request.setImages(new ArrayList<>(Arrays.asList(
                                "https://cit.securiforce.net/app-assets/logo/9a6cefbceff3cf135b0b90cc9058c0c1.png")));
                proj8Request.setPlatform(PlatformType.MULTI_PLATFORM);
                proj8Request.setStartDate(LocalDate.of(2022, 1, 1));
                proj8Request.setEndDate(LocalDate.of(2022, 12, 31));
                proj8Request.setType(ProjectType.WORK);
                proj8Request.setStatus(ProjectStatus.COMPLETED);
                proj8Request.setIsFeatured(false);
                proj8Request.setDisplayOrder(8);
                projectService.createProject(ownerId, proj8Request);

                // Project 9: Fasttrack System
                ProjectRequest proj9Request = new ProjectRequest();
                proj9Request.setTitle("Fasttrack System");
                proj9Request.setDescription(
                                "A client-facing web application that enables users to conveniently order a variety of services, such as cash-in-transit, through a streamlined and user-friendly interface.");
                proj9Request.setTechnologies(new ArrayList<>(Arrays.asList("CodeIgniter 4", "PostgreSQL", "Kotlin", "Postman", "RESTful API", "Docker")));
                proj9Request.setGithubUrl("https://github.com/hafizbahtiar/fasttrack-system");
                proj9Request.setLiveUrl("https://fasttrack-system.com");
                proj9Request.setImages(new ArrayList<>(Arrays.asList(
                                "https://cit.securiforce.net/app-assets/logo/9a6cefbceff3cf135b0b90cc9058c0c1.png")));
                proj9Request.setPlatform(PlatformType.WEB);
                proj9Request.setStartDate(LocalDate.of(2022, 1, 1));
                proj9Request.setEndDate(LocalDate.of(2022, 12, 31));
                proj9Request.setType(ProjectType.WORK);
                proj9Request.setStatus(ProjectStatus.COMPLETED);
                proj9Request.setIsFeatured(false);
                proj9Request.setDisplayOrder(9);
                projectService.createProject(ownerId, proj9Request);

                // Project 10: My Console - Terminal Portfolio
                ProjectRequest proj10Request = new ProjectRequest();
                proj10Request.setTitle("My Console - Terminal Portfolio");
                proj10Request.setDescription(
                                "An interactive terminal-style portfolio website that provides a unique command-line interface experience. Users can navigate through different sections using familiar terminal commands, creating an engaging and memorable way to showcase skills and projects.");
                proj10Request.setTechnologies(new ArrayList<>(Arrays.asList("Next.js", "TypeScript", "Tailwind CSS", "React", "Terminal Interface", "Command Parser", "Shadcn/ui", "Appwrite", "OpenRouter", "Bun")));
                proj10Request.setGithubUrl("https://github.com/hafizbahtiar/my-console");
                proj10Request.setLiveUrl("https://console.hafizbahtiar.com");
                proj10Request.setImages(new ArrayList<>(Arrays.asList("/images/projects/hafiz-logo.png")));
                proj10Request.setPlatform(PlatformType.WEB);
                proj10Request.setStartDate(LocalDate.of(2025, 1, 1));
                proj10Request.setEndDate(LocalDate.of(2025, 12, 31));
                proj10Request.setType(ProjectType.HOBBY);
                proj10Request.setStatus(ProjectStatus.COMPLETED);
                proj10Request.setIsFeatured(true);
                proj10Request.setDisplayOrder(10);
                projectService.createProject(ownerId, proj10Request);
                
                log.info("Created 10 projects for owner user based on portfolio-next projects.");
        }


}
