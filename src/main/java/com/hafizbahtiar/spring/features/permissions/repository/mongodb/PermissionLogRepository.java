package com.hafizbahtiar.spring.features.permissions.repository.mongodb;

import com.hafizbahtiar.spring.features.permissions.model.PermissionLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for PermissionLog documents.
 * Provides methods for querying permission event logs.
 */
@Repository
public interface PermissionLogRepository extends MongoRepository<PermissionLog, String> {

    /**
     * Find permission logs by user ID
     *
     * @param userId User ID
     * @return List of permission logs
     */
    List<PermissionLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find permission logs by group ID
     *
     * @param groupId Group ID
     * @return List of permission logs
     */
    List<PermissionLog> findByGroupIdOrderByTimestampDesc(Long groupId);

    /**
     * Find permission logs by event type
     *
     * @param eventType Event type
     * @return List of permission logs
     */
    List<PermissionLog> findByEventTypeOrderByTimestampDesc(String eventType);

    /**
     * Find permission logs by user ID and event type
     *
     * @param userId    User ID
     * @param eventType Event type
     * @return List of permission logs
     */
    List<PermissionLog> findByUserIdAndEventTypeOrderByTimestampDesc(Long userId, String eventType);

    /**
     * Find permission logs within a time range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of permission logs
     */
    List<PermissionLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Find permission logs by group ID and event type
     *
     * @param groupId   Group ID
     * @param eventType Event type
     * @return List of permission logs
     */
    List<PermissionLog> findByGroupIdAndEventTypeOrderByTimestampDesc(Long groupId, String eventType);
}
