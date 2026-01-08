package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Certification entity.
 * Provides CRUD operations and custom queries for certification management.
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /**
     * Find all certifications for a specific user, ordered by display order
     */
    List<Certification> findByUserIdOrderByDisplayOrderAsc(Long userId);

    /**
     * Find all certifications for a specific user, ordered by issue date (newest
     * first)
     */
    List<Certification> findByUserIdOrderByIssueDateDesc(Long userId);

    /**
     * Find all verified certifications for a specific user, ordered by display
     * order
     */
    List<Certification> findByUserIdAndIsVerifiedTrueOrderByDisplayOrderAsc(Long userId);

    /**
     * Find certifications by user ID and issuer, ordered by display order
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND c.issuer = :issuer ORDER BY c.displayOrder ASC")
    List<Certification> findByUserIdAndIssuerOrderByDisplayOrderAsc(
            @Param("userId") Long userId,
            @Param("issuer") String issuer);

    /**
     * Find certifications by user ID and issuer, ordered by issue date (newest
     * first)
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND c.issuer = :issuer ORDER BY c.issueDate DESC")
    List<Certification> findByUserIdAndIssuerOrderByIssueDateDesc(
            @Param("userId") Long userId,
            @Param("issuer") String issuer);

    /**
     * Find expired certifications for a specific user, ordered by expiry date (most
     * recent first)
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND c.isExpired = true ORDER BY c.expiryDate DESC")
    List<Certification> findByUserIdAndIsExpiredTrueOrderByExpiryDateDesc(Long userId);

    /**
     * Find non-expired certifications for a specific user, ordered by display order
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND (c.isExpired = false OR c.expiryDate IS NULL) ORDER BY c.displayOrder ASC")
    List<Certification> findByUserIdAndIsExpiredFalseOrderByDisplayOrderAsc(Long userId);

    /**
     * Find certifications expiring soon (within specified days) for a specific user
     * Ordered by expiry date (soonest first)
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND c.expiryDate IS NOT NULL AND c.isExpired = false AND c.expiryDate BETWEEN :today AND :futureDate ORDER BY c.expiryDate ASC")
    List<Certification> findExpiringSoonByUserId(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("futureDate") LocalDate futureDate);

    /**
     * Find verified certifications by user ID and issuer, ordered by display order
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND c.issuer = :issuer AND c.isVerified = true ORDER BY c.displayOrder ASC")
    List<Certification> findByUserIdAndIssuerAndIsVerifiedTrueOrderByDisplayOrderAsc(
            @Param("userId") Long userId,
            @Param("issuer") String issuer);

    /**
     * Find certification by user ID and certification ID (for ownership validation)
     */
    Optional<Certification> findByUserIdAndId(Long userId, Long id);

    /**
     * Count certifications by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count verified certifications by user ID
     */
    long countByUserIdAndIsVerifiedTrue(Long userId);

    /**
     * Count expired certifications by user ID
     */
    long countByUserIdAndIsExpiredTrue(Long userId);

    /**
     * Count certifications by user ID and issuer
     */
    @Query("SELECT COUNT(c) FROM Certification c WHERE c.user.id = :userId AND c.issuer = :issuer")
    long countByUserIdAndIssuer(@Param("userId") Long userId, @Param("issuer") String issuer);

    /**
     * Count verified certifications by user ID and issuer
     */
    @Query("SELECT COUNT(c) FROM Certification c WHERE c.user.id = :userId AND c.issuer = :issuer AND c.isVerified = true")
    long countByUserIdAndIssuerAndIsVerifiedTrue(@Param("userId") Long userId, @Param("issuer") String issuer);

    /**
     * Find certifications by user ID within a date range (by issue date)
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND c.issueDate BETWEEN :startDate AND :endDate ORDER BY c.displayOrder ASC")
    List<Certification> findByUserIdAndIssueDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find certifications by name (case-insensitive search) for a specific user
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.displayOrder ASC")
    List<Certification> findByUserIdAndNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("name") String name);

    /**
     * Find certifications by issuer (case-insensitive search) for a specific user
     */
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND LOWER(c.issuer) LIKE LOWER(CONCAT('%', :issuer, '%')) ORDER BY c.displayOrder ASC")
    List<Certification> findByUserIdAndIssuerContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("issuer") String issuer);
}
