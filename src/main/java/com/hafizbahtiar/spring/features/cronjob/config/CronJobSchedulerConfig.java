package com.hafizbahtiar.spring.features.cronjob.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration for cron job scheduling.
 * Provides TaskScheduler bean for dynamic job scheduling.
 */
@Configuration
public class CronJobSchedulerConfig {

    /**
     * Create a ThreadPoolTaskScheduler bean for dynamic cron job scheduling.
     * 
     * @return TaskScheduler instance
     */
    @Bean(name = "cronJobTaskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // Allow up to 10 concurrent scheduled tasks
        scheduler.setThreadNamePrefix("cron-job-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }
}
