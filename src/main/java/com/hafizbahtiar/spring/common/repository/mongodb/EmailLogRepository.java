package com.hafizbahtiar.spring.common.repository.mongodb;

import com.hafizbahtiar.spring.common.model.EmailLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for email event logs.
 * Provides query methods for email log analysis and audit trails.
 */
@Repository
public interface EmailLogRepository extends MongoRepository<EmailLog, String> {

    /**
     * Find all logs for a specific recipient email
     */
    List<EmailLog> findByToOrderBySentAtDesc(String to);

    /**
     * Find logs by status (SENT, FAILED)
     */
    List<EmailLog> findByStatusOrderBySentAtDesc(String status);

    /**
     * Find logs by template name
     */
    List<EmailLog> findByTemplateNameOrderBySentAtDesc(String templateName);

    /**
     * Find logs for a specific user
     */
    List<EmailLog> findByUserIdOrderBySentAtDesc(Long userId);

    /**
     * Find logs for a specific user and template name
     */
    List<EmailLog> findByUserIdAndTemplateNameOrderBySentAtDesc(Long userId, String templateName);

    /**
     * Find logs within a date range
     */
    List<EmailLog> findBySentAtBetweenOrderBySentAtDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Find logs for a specific recipient within a date range
     */
    List<EmailLog> findByToAndSentAtBetweenOrderBySentAtDesc(String to, LocalDateTime start, LocalDateTime end);

    /**
     * Find failed email attempts for a specific recipient
     */
    List<EmailLog> findByToAndStatusOrderBySentAtDesc(String to, String status);

    /**
     * Count failed email attempts for a recipient within a time window
     * Useful for detecting email delivery issues
     */
    @Query("{ 'to': ?0, 'status': 'FAILED', 'sentAt': { $gte: ?1 } }")
    long countFailedAttemptsByRecipientSince(String to, LocalDateTime since);

    /**
     * Find logs by request ID (for request tracing)
     */
    List<EmailLog> findByRequestIdOrderBySentAtDesc(String requestId);

    /**
     * Find logs by email type (PLAIN_TEXT, HTML, TEMPLATED)
     */
    List<EmailLog> findByEmailTypeOrderBySentAtDesc(String emailType);
}
