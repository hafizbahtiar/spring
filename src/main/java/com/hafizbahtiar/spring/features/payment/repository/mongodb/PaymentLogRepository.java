package com.hafizbahtiar.spring.features.payment.repository.mongodb;

import com.hafizbahtiar.spring.features.payment.model.PaymentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for PaymentLog documents.
 * Provides data access methods for payment event logs.
 */
@Repository
public interface PaymentLogRepository extends MongoRepository<PaymentLog, String> {

    /**
     * Find payment logs by payment ID, ordered by timestamp descending
     */
    List<PaymentLog> findByPaymentIdOrderByTimestampDesc(Long paymentId);

    /**
     * Find payment logs by user ID, ordered by timestamp descending
     */
    Page<PaymentLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Find payment logs by event type, ordered by timestamp descending
     */
    List<PaymentLog> findByEventTypeOrderByTimestampDesc(String eventType);

    /**
     * Find payment logs by provider, ordered by timestamp descending
     */
    List<PaymentLog> findByProviderOrderByTimestampDesc(String provider);

    /**
     * Find payment logs by provider payment ID, ordered by timestamp descending
     */
    List<PaymentLog> findByProviderPaymentIdOrderByTimestampDesc(String providerPaymentId);

    /**
     * Find payment logs by status, ordered by timestamp descending
     */
    List<PaymentLog> findByStatusOrderByTimestampDesc(String status);

    /**
     * Find payment logs within a date range, ordered by timestamp descending
     */
    List<PaymentLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find payment logs by user ID and event type, ordered by timestamp descending
     */
    List<PaymentLog> findByUserIdAndEventTypeOrderByTimestampDesc(Long userId, String eventType);

    /**
     * Find payment logs by user ID and provider, ordered by timestamp descending
     */
    List<PaymentLog> findByUserIdAndProviderOrderByTimestampDesc(Long userId, String provider);

    /**
     * Find payment logs by user ID within a date range, ordered by timestamp
     * descending
     */
    Page<PaymentLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Count payment logs by event type
     */
    long countByEventType(String eventType);

    /**
     * Count payment logs by provider
     */
    long countByProvider(String provider);

    /**
     * Count payment logs by user ID
     */
    long countByUserId(Long userId);
}
