package com.hafizbahtiar.spring.features.ipaddress.repository.mongodb;

import com.hafizbahtiar.spring.features.ipaddress.model.IPLookupLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for IP address lookup event logs.
 * Provides query methods for IP lookup log analysis and audit trails.
 */
@Repository
public interface IPLookupLogRepository extends MongoRepository<IPLookupLog, String> {

    /**
     * Find all logs for a specific user
     */
    List<IPLookupLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find logs by event type
     */
    List<IPLookupLog> findByEventTypeOrderByTimestampDesc(String eventType);

    /**
     * Find logs by success status
     */
    List<IPLookupLog> findBySuccessOrderByTimestampDesc(Boolean success);

    /**
     * Find logs for a specific user and event type
     */
    List<IPLookupLog> findByUserIdAndEventTypeOrderByTimestampDesc(Long userId, String eventType);

    /**
     * Find logs within a date range
     */
    List<IPLookupLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Find logs for a specific user within a date range
     */
    List<IPLookupLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Find logs by looked up IP address
     */
    List<IPLookupLog> findByLookedUpIpOrderByTimestampDesc(String lookedUpIp);

    /**
     * Find logs by session ID
     */
    List<IPLookupLog> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find logs by request ID (for request tracing)
     */
    IPLookupLog findByRequestId(String requestId);
}
