package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.CertificationRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.CertificationResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for certification management.
 * Handles CRUD operations, expiry management, and display order management for
 * user certifications.
 */
public interface CertificationService {

    /**
     * Create a new certification for a user.
     *
     * @param userId  User ID
     * @param request Certification creation request
     * @return Created CertificationResponse
     */
    CertificationResponse createCertification(Long userId, CertificationRequest request);

    /**
     * Update an existing certification.
     *
     * @param certificationId Certification ID
     * @param userId          User ID (for ownership validation)
     * @param request         Update request
     * @return Updated CertificationResponse
     */
    CertificationResponse updateCertification(Long certificationId, Long userId, CertificationRequest request);

    /**
     * Delete a certification.
     *
     * @param certificationId Certification ID
     * @param userId          User ID (for ownership validation)
     */
    void deleteCertification(Long certificationId, Long userId);

    /**
     * Get certification by ID.
     *
     * @param certificationId Certification ID
     * @param userId          User ID (for ownership validation)
     * @return CertificationResponse
     */
    CertificationResponse getCertification(Long certificationId, Long userId);

    /**
     * Get all certifications for a user.
     *
     * @param userId User ID
     * @return List of CertificationResponse
     */
    List<CertificationResponse> getUserCertifications(Long userId);

    /**
     * Get certifications for a user filtered by issuer.
     *
     * @param userId User ID
     * @param issuer Issuer (optional)
     * @return List of CertificationResponse
     */
    List<CertificationResponse> getUserCertificationsByIssuer(Long userId, String issuer);

    /**
     * Get expired certifications for a user.
     *
     * @param userId User ID
     * @return List of CertificationResponse
     */
    List<CertificationResponse> getExpiredCertifications(Long userId);

    /**
     * Get non-expired certifications for a user.
     *
     * @param userId User ID
     * @return List of CertificationResponse
     */
    List<CertificationResponse> getNonExpiredCertifications(Long userId);

    /**
     * Get certifications expiring soon (within specified days) for a user.
     *
     * @param userId User ID
     * @param days   Number of days to check ahead (default: 90)
     * @return List of CertificationResponse
     */
    List<CertificationResponse> getExpiringSoonCertifications(Long userId, int days);

    /**
     * Get certifications expiring soon (within 90 days) for a user.
     *
     * @param userId User ID
     * @return List of CertificationResponse
     */
    List<CertificationResponse> getExpiringSoonCertifications(Long userId);

    /**
     * Get verified certifications only for a user.
     *
     * @param userId User ID
     * @return List of CertificationResponse
     */
    List<CertificationResponse> getVerifiedCertifications(Long userId);

    /**
     * Search certifications by name for a user.
     *
     * @param userId User ID
     * @param name   Certification name (partial match, case-insensitive)
     * @return List of CertificationResponse
     */
    List<CertificationResponse> searchCertificationsByName(Long userId, String name);

    /**
     * Search certifications by issuer for a user.
     *
     * @param userId User ID
     * @param issuer Issuer (partial match, case-insensitive)
     * @return List of CertificationResponse
     */
    List<CertificationResponse> searchCertificationsByIssuer(Long userId, String issuer);

    /**
     * Reorder certifications by updating display order.
     *
     * @param userId                User ID
     * @param certificationOrderMap Map of certification ID to display order
     */
    void reorderCertifications(Long userId, Map<Long, Integer> certificationOrderMap);
}
