package com.hafizbahtiar.spring.features.cronjob.executor;

import com.hafizbahtiar.spring.features.cronjob.entity.CronJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Executor for application-level cron jobs.
 * Executes Spring service methods using reflection.
 * 
 * Job class format: "ServiceName.methodName" or
 * "com.package.ServiceName.methodName"
 * Example: "SessionCleanupService.cleanupExpiredSessions"
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationJobExecutor {

    private final ApplicationContext applicationContext;

    /**
     * Execute a cron job by calling the specified Spring service method.
     *
     * @param cronJob The cron job to execute
     * @throws JobExecutionException if the job cannot be executed
     */
    public void execute(CronJob cronJob) throws JobExecutionException {
        if (cronJob.getJobClass() == null || cronJob.getJobClass().trim().isEmpty()) {
            throw new JobExecutionException("Job class is not specified for job: " + cronJob.getName());
        }

        String jobClass = cronJob.getJobClass().trim();
        log.debug("Executing application job: {} with class: {}", cronJob.getName(), jobClass);

        try {
            // Parse job class and method name
            String[] parts = jobClass.split("\\.");
            if (parts.length < 2) {
                throw new JobExecutionException(
                        "Invalid job class format. Expected 'ServiceName.methodName' or 'com.package.ServiceName.methodName', got: "
                                + jobClass);
            }

            String methodName = parts[parts.length - 1];
            String serviceName = parts[parts.length - 2];

            // Find the bean in Spring context
            Object bean = findBean(serviceName);
            if (bean == null) {
                throw new JobExecutionException("Service bean not found: " + serviceName);
            }

            // Find the method
            Method method = findMethod(bean.getClass(), methodName);
            if (method == null) {
                throw new JobExecutionException(
                        "Method not found: " + methodName + " in class " + bean.getClass().getName());
            }

            // Invoke the method
            log.debug("Invoking method: {}.{}()", bean.getClass().getSimpleName(), methodName);
            method.invoke(bean);

            log.info("Successfully executed application job: {}", cronJob.getName());

        } catch (JobExecutionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing application job: {}", cronJob.getName(), e);
            throw new JobExecutionException(
                    "Failed to execute job: " + cronJob.getName() + ". Error: " + e.getMessage(), e);
        }
    }

    /**
     * Validate that the job class and method exist and are accessible.
     *
     * @param cronJob The cron job to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(CronJob cronJob) {
        if (cronJob.getJobClass() == null || cronJob.getJobClass().trim().isEmpty()) {
            return false;
        }

        try {
            String jobClass = cronJob.getJobClass().trim();
            String[] parts = jobClass.split("\\.");
            if (parts.length < 2) {
                return false;
            }

            String methodName = parts[parts.length - 1];
            String serviceName = parts[parts.length - 2];

            Object bean = findBean(serviceName);
            if (bean == null) {
                return false;
            }

            Method method = findMethod(bean.getClass(), methodName);
            return method != null;

        } catch (Exception e) {
            log.debug("Job validation failed for: {}", cronJob.getName(), e);
            return false;
        }
    }

    /**
     * Find a Spring bean by name (case-insensitive).
     * Tries exact match first, then case-insensitive match.
     */
    private Object findBean(String beanName) {
        // Try exact match first
        try {
            return applicationContext.getBean(beanName);
        } catch (Exception e) {
            // Try case-insensitive match
            String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
            for (String name : beanNames) {
                if (name.equalsIgnoreCase(beanName)) {
                    return applicationContext.getBean(name);
                }
            }
            return null;
        }
    }

    /**
     * Find a method by name in the given class.
     * Looks for public methods with no parameters.
     */
    private Method findMethod(Class<?> clazz, String methodName) {
        try {
            // Try exact match first
            Method method = clazz.getMethod(methodName);
            if (method != null && method.getParameterCount() == 0) {
                return method;
            }
        } catch (NoSuchMethodException e) {
            // Try case-insensitive match
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(methodName) && method.getParameterCount() == 0) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Exception thrown when job execution fails.
     */
    public static class JobExecutionException extends Exception {
        public JobExecutionException(String message) {
            super(message);
        }

        public JobExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
