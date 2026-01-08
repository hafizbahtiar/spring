package com.hafizbahtiar.spring.features.portfolio.repository.mongodb;

import com.hafizbahtiar.spring.features.portfolio.model.PortfolioLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for PortfolioLog documents.
 * Provides data access methods for portfolio event logs.
 */
@Repository
public interface PortfolioLogRepository extends MongoRepository<PortfolioLog, String> {

    /**
     * Find portfolio logs by entity ID, ordered by timestamp descending
     */
    List<PortfolioLog> findByEntityIdOrderByTimestampDesc(Long entityId);

    /**
     * Find portfolio logs by user ID, ordered by timestamp descending
     */
    Page<PortfolioLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Find portfolio logs by entity type, ordered by timestamp descending
     */
    List<PortfolioLog> findByEntityTypeOrderByTimestampDesc(String entityType);

    /**
     * Find portfolio logs by event type, ordered by timestamp descending
     */
    List<PortfolioLog> findByEventTypeOrderByTimestampDesc(String eventType);

    /**
     * Find portfolio logs by user ID and entity type, ordered by timestamp
     * descending
     */
    List<PortfolioLog> findByUserIdAndEntityTypeOrderByTimestampDesc(Long userId, String entityType);

    /**
     * Find portfolio logs by user ID and event type, ordered by timestamp
     * descending
     */
    List<PortfolioLog> findByUserIdAndEventTypeOrderByTimestampDesc(Long userId, String eventType);

    /**
     * Find portfolio logs by entity type and event type, ordered by timestamp
     * descending
     */
    List<PortfolioLog> findByEntityTypeAndEventTypeOrderByTimestampDesc(String entityType, String eventType);

    /**
     * Find portfolio logs by user ID, entity type, and event type, ordered by
     * timestamp descending
     */
    List<PortfolioLog> findByUserIdAndEntityTypeAndEventTypeOrderByTimestampDesc(
            Long userId, String entityType, String eventType);

    /**
     * Find portfolio logs within a date range, ordered by timestamp descending
     */
    List<PortfolioLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find portfolio logs by user ID within a date range, ordered by timestamp
     * descending
     */
    Page<PortfolioLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find portfolio logs by entity type within a date range, ordered by timestamp
     * descending
     */
    List<PortfolioLog> findByEntityTypeAndTimestampBetweenOrderByTimestampDesc(
            String entityType, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count portfolio logs by event type
     */
    long countByEventType(String eventType);

    /**
     * Count portfolio logs by entity type
     */
    long countByEntityType(String entityType);

    /**
     * Count portfolio logs by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count portfolio logs by user ID and entity type
     */
    long countByUserIdAndEntityType(Long userId, String entityType);

    /**
     * Count portfolio logs by user ID and event type
     */
    long countByUserIdAndEventType(Long userId, String eventType);
}
