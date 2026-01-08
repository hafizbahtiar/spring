package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.Contact;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactSource;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Contact entity.
 * Provides CRUD operations and custom queries for contact management.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * Find all contacts for a specific user, ordered by created date (newest first)
     */
    List<Contact> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find contacts by user ID and status, ordered by created date (newest first)
     */
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId AND c.status = :status ORDER BY c.createdAt DESC")
    List<Contact> findByUserIdAndStatusOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("status") ContactStatus status);

    /**
     * Find contacts by user ID and source, ordered by created date (newest first)
     */
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId AND c.source = :source ORDER BY c.createdAt DESC")
    List<Contact> findByUserIdAndSourceOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("source") ContactSource source);

    /**
     * Find contacts by user ID, status, and source, ordered by created date (newest
     * first)
     */
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId AND c.status = :status AND c.source = :source ORDER BY c.createdAt DESC")
    List<Contact> findByUserIdAndStatusAndSourceOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("status") ContactStatus status,
            @Param("source") ContactSource source);

    /**
     * Find new (unread) contacts for a specific user, ordered by created date
     * (newest first)
     */
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId AND c.status = 'NEW' ORDER BY c.createdAt DESC")
    List<Contact> findNewContactsByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find contact by user ID and contact ID (for ownership validation)
     */
    Optional<Contact> findByUserIdAndId(Long userId, Long id);

    /**
     * Find contacts by email (case-insensitive search) for a specific user
     */
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId AND LOWER(c.email) = LOWER(:email) ORDER BY c.createdAt DESC")
    List<Contact> findByUserIdAndEmailIgnoreCase(
            @Param("userId") Long userId,
            @Param("email") String email);

    /**
     * Find contacts by user ID within a date range (by created date)
     */
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<Contact> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count contacts by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count contacts by user ID and status
     */
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.user.id = :userId AND c.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ContactStatus status);

    /**
     * Count new (unread) contacts by user ID
     */
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.user.id = :userId AND c.status = 'NEW'")
    long countNewContactsByUserId(Long userId);

    /**
     * Count contacts by user ID and source
     */
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.user.id = :userId AND c.source = :source")
    long countByUserIdAndSource(@Param("userId") Long userId, @Param("source") ContactSource source);

    /**
     * Count contacts by user ID, status, and source
     */
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.user.id = :userId AND c.status = :status AND c.source = :source")
    long countByUserIdAndStatusAndSource(
            @Param("userId") Long userId,
            @Param("status") ContactStatus status,
            @Param("source") ContactSource source);
}
