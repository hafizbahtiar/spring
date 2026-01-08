package com.hafizbahtiar.spring.features.auth.repository.mongodb;

import com.hafizbahtiar.spring.features.auth.model.AuthLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for authentication event logs.
 * Provides query methods for authentication log analysis and audit trails.
 */
@Repository
public interface AuthLogRepository extends MongoRepository<AuthLog, String> {

    /**
     * Find all logs for a specific user
     */
    List<AuthLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find logs by event type
     */
    List<AuthLog> findByEventTypeOrderByTimestampDesc(String eventType);

    /**
     * Find logs by success status
     */
    List<AuthLog> findBySuccessOrderByTimestampDesc(Boolean success);

    /**
     * Find logs for a specific user and event type
     */
    List<AuthLog> findByUserIdAndEventTypeOrderByTimestampDesc(Long userId, String eventType);

    /**
     * Find logs within a date range
     */
    List<AuthLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Find logs for a specific user within a date range
     */
    List<AuthLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Find failed login attempts for a specific identifier (email/username)
     */
    List<AuthLog> findByIdentifierAndSuccessFalseOrderByTimestampDesc(String identifier);

    /**
     * Count failed login attempts for an identifier within a time window
     * Useful for detecting brute force attacks
     */
    @Query("{ 'identifier': ?0, 'success': false, 'timestamp': { $gte: ?1 } }")
    long countFailedAttemptsByIdentifierSince(String identifier, LocalDateTime since);

    /**
     * Find logs by session ID
     */
    List<AuthLog> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find logs by request ID (for request tracing)
     */
    AuthLog findByRequestId(String requestId);
}
