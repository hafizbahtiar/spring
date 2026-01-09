package com.hafizbahtiar.spring.features.cronjob.exception;

/**
 * Exception thrown when a cron job is not found.
 */
public class CronJobNotFoundException extends RuntimeException {

    public CronJobNotFoundException(String message) {
        super(message);
    }

    public static CronJobNotFoundException byId(Long id) {
        return new CronJobNotFoundException("Cron job not found with ID: " + id);
    }

    public static CronJobNotFoundException byName(String name) {
        return new CronJobNotFoundException("Cron job not found with name: " + name);
    }
}
