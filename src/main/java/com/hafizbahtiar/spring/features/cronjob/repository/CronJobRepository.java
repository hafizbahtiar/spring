package com.hafizbahtiar.spring.features.cronjob.repository;

import com.hafizbahtiar.spring.features.cronjob.entity.CronJob;
import com.hafizbahtiar.spring.features.cronjob.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CronJob entity.
 * Provides CRUD operations and query methods for cron job management.
 */
@Repository
public interface CronJobRepository extends JpaRepository<CronJob, Long> {

    /**
     * Find all enabled cron jobs.
     * Used to load and schedule jobs on application startup.
     *
     * @return List of enabled cron jobs
     */
    List<CronJob> findByEnabledTrue();

    /**
     * Find cron jobs by job type.
     *
     * @param jobType Job type (APPLICATION or DATABASE)
     * @return List of cron jobs of the specified type
     */
    List<CronJob> findByJobType(JobType jobType);

    /**
     * Find enabled cron jobs by job type.
     *
     * @param jobType Job type (APPLICATION or DATABASE)
     * @return List of enabled cron jobs of the specified type
     */
    List<CronJob> findByJobTypeAndEnabledTrue(JobType jobType);

    /**
     * Check if a cron job exists with the given name.
     *
     * @param name Job name
     * @return true if job exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Check if a cron job exists with the given name (case-insensitive).
     *
     * @param name Job name
     * @return true if job exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find cron job by name.
     *
     * @param name Job name
     * @return Optional CronJob
     */
    Optional<CronJob> findByName(String name);

    /**
     * Find cron job by name (case-insensitive).
     *
     * @param name Job name
     * @return Optional CronJob
     */
    Optional<CronJob> findByNameIgnoreCase(String name);

    /**
     * Check if a cron job exists with the given name for another job (for updates).
     *
     * @param name Job name
     * @param id   Current job ID (to exclude from check)
     * @return true if another job with the same name exists, false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Check if a cron job exists with the given name for another job (case-insensitive, for updates).
     *
     * @param name Job name
     * @param id   Current job ID (to exclude from check)
     * @return true if another job with the same name exists, false otherwise
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
