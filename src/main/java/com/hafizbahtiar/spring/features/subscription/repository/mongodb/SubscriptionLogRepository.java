package com.hafizbahtiar.spring.features.subscription.repository.mongodb;

import com.hafizbahtiar.spring.features.subscription.model.SubscriptionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for SubscriptionLog documents.
 * Provides data access methods for subscription event logs.
 */
@Repository
public interface SubscriptionLogRepository extends MongoRepository<SubscriptionLog, String> {

    /**
     * Find subscription logs by subscription ID, ordered by timestamp descending
     */
    List<SubscriptionLog> findBySubscriptionIdOrderByTimestampDesc(Long subscriptionId);

    /**
     * Find subscription logs by user ID, ordered by timestamp descending
     */
    Page<SubscriptionLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Find subscription logs by event type, ordered by timestamp descending
     */
    List<SubscriptionLog> findByEventTypeOrderByTimestampDesc(String eventType);

    /**
     * Find subscription logs by provider, ordered by timestamp descending
     */
    List<SubscriptionLog> findByProviderOrderByTimestampDesc(String provider);

    /**
     * Find subscription logs by provider subscription ID, ordered by timestamp
     * descending
     */
    List<SubscriptionLog> findByProviderSubscriptionIdOrderByTimestampDesc(String providerSubscriptionId);

    /**
     * Find subscription logs by status, ordered by timestamp descending
     */
    List<SubscriptionLog> findByStatusOrderByTimestampDesc(String status);

    /**
     * Find subscription logs within a date range, ordered by timestamp descending
     */
    List<SubscriptionLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find subscription logs by user ID and event type, ordered by timestamp
     * descending
     */
    List<SubscriptionLog> findByUserIdAndEventTypeOrderByTimestampDesc(Long userId, String eventType);

    /**
     * Find subscription logs by user ID and provider, ordered by timestamp
     * descending
     */
    List<SubscriptionLog> findByUserIdAndProviderOrderByTimestampDesc(Long userId, String provider);

    /**
     * Find subscription logs by subscription plan ID, ordered by timestamp
     * descending
     */
    List<SubscriptionLog> findBySubscriptionPlanIdOrderByTimestampDesc(Long subscriptionPlanId);

    /**
     * Find subscription logs by user ID within a date range, ordered by timestamp
     * descending
     */
    Page<SubscriptionLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Count subscription logs by event type
     */
    long countByEventType(String eventType);

    /**
     * Count subscription logs by provider
     */
    long countByProvider(String provider);

    /**
     * Count subscription logs by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count subscription logs by subscription plan ID
     */
    long countBySubscriptionPlanId(Long subscriptionPlanId);
}
