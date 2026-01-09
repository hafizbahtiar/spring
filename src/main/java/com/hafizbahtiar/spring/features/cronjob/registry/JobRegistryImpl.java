package com.hafizbahtiar.spring.features.cronjob.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of JobRegistry.
 * Scans Spring context for available service beans and their executable
 * methods.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobRegistryImpl implements JobRegistry {

    private final ApplicationContext applicationContext;
    private final Map<String, JobDefinition> jobCache = new ConcurrentHashMap<>();

    /**
     * Initialize registry on startup
     */
    @PostConstruct
    public void initialize() {
        refresh();
    }

    @Override
    public List<JobDefinition> getAvailableJobs() {
        return new ArrayList<>(jobCache.values());
    }

    @Override
    public List<JobDefinition> getJobsByService(String serviceName) {
        return jobCache.values().stream()
                .filter(job -> job.getServiceName().equalsIgnoreCase(serviceName))
                .collect(Collectors.toList());
    }

    @Override
    public JobDefinition getJob(String serviceName, String methodName) {
        String key = buildKey(serviceName, methodName);
        return jobCache.get(key);
    }

    @Override
    public void refresh() {
        log.info("Refreshing job registry...");
        jobCache.clear();

        // Get all Spring beans
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);

        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);

                // Skip Spring internal beans and non-service beans
                if (shouldSkipBean(beanName, bean)) {
                    continue;
                }

                // Find executable methods (public, no parameters)
                List<Method> executableMethods = findExecutableMethods(bean.getClass());

                for (Method method : executableMethods) {
                    JobDefinition jobDef = JobDefinition.builder()
                            .serviceName(beanName)
                            .serviceClassName(bean.getClass().getName())
                            .methodName(method.getName())
                            .jobClass(beanName + "." + method.getName())
                            .description(extractDescription(method))
                            .available(true)
                            .build();

                    String key = buildKey(beanName, method.getName());
                    jobCache.put(key, jobDef);
                    log.debug("Registered job: {}", jobDef.getJobClass());
                }
            } catch (Exception e) {
                log.debug("Error scanning bean {} for jobs: {}", beanName, e.getMessage());
            }
        }

        log.info("Job registry refreshed. Found {} available jobs", jobCache.size());
    }

    /**
     * Check if a bean should be skipped during scanning
     */
    private boolean shouldSkipBean(String beanName, Object bean) {
        // Skip Spring internal beans
        if (beanName.startsWith("org.springframework.") ||
                beanName.startsWith("org.hibernate.") ||
                beanName.startsWith("org.apache.") ||
                beanName.startsWith("com.zaxxer.") ||
                beanName.startsWith("org.apache.tomcat.") ||
                beanName.startsWith("org.apache.catalina.")) {
            return true;
        }

        // Skip if bean is not a service/component (optional check)
        // We'll scan all beans and filter by method signature instead

        return false;
    }

    /**
     * Find executable methods in a class.
     * Methods must be:
     * - Public
     * - No parameters
     * - Not static (instance methods only)
     * - Not from Object class
     */
    private List<Method> findExecutableMethods(Class<?> clazz) {
        List<Method> executableMethods = new ArrayList<>();

        // Get all public methods
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            // Skip methods from Object class
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            // Skip static methods
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            // Only include methods with no parameters
            if (method.getParameterCount() == 0) {
                // Skip getters, setters, and common methods
                if (isSkippableMethod(method)) {
                    continue;
                }

                executableMethods.add(method);
            }
        }

        return executableMethods;
    }

    /**
     * Check if a method should be skipped (getters, setters, etc.)
     */
    private boolean isSkippableMethod(Method method) {
        String methodName = method.getName();

        // Skip getters
        if (methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
            return true;
        }

        // Skip setters
        if (methodName.startsWith("set") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
            return true;
        }

        // Skip boolean getters (isXxx)
        if (methodName.startsWith("is") && methodName.length() > 2 && Character.isUpperCase(methodName.charAt(2))) {
            return true;
        }

        // Skip common methods
        if (methodName.equals("toString") ||
                methodName.equals("equals") ||
                methodName.equals("hashCode") ||
                methodName.equals("clone") ||
                methodName.equals("wait") ||
                methodName.equals("notify") ||
                methodName.equals("notifyAll") ||
                methodName.equals("getClass")) {
            return true;
        }

        // Skip methods that return void but are likely not job methods
        // (we'll include void methods as they might be job methods)
        return false;
    }

    /**
     * Extract description from method (JavaDoc or annotation)
     */
    private String extractDescription(Method method) {
        // Try to get description from annotations or JavaDoc
        // For now, return a default description
        // Could be enhanced to read JavaDoc comments or custom annotations

        // Check for @Scheduled annotation (common for cron jobs)
        if (method.isAnnotationPresent(org.springframework.scheduling.annotation.Scheduled.class)) {
            org.springframework.scheduling.annotation.Scheduled scheduled = method
                    .getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
            if (scheduled.cron() != null && !scheduled.cron().isEmpty()) {
                return "Scheduled job (cron: " + scheduled.cron() + ")";
            }
            return "Scheduled job";
        }

        // Default description
        return "Executable method: " + method.getName();
    }

    /**
     * Build cache key for job definition
     */
    private String buildKey(String serviceName, String methodName) {
        return serviceName.toLowerCase() + "." + methodName.toLowerCase();
    }
}
