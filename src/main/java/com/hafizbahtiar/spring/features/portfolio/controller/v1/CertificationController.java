package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.CertificationRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.CertificationResponse;
import com.hafizbahtiar.spring.features.portfolio.service.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for certification management endpoints.
 * Handles certification CRUD operations, filtering, expiry management, and display order management.
 */
@RestController
@RequestMapping("/api/v1/portfolio/certifications")
@RequiredArgsConstructor
@Slf4j
public class CertificationController {

    private final CertificationService certificationService;
    private final SecurityService securityService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Create a new certification
     * POST /api/v1/portfolio/certifications
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificationResponse>> createCertification(
            @Valid @RequestBody CertificationRequest request) {
        Long userId = getCurrentUserId();
        log.info("Certification creation request received for user ID: {}, certification name: {}", userId,
                request.getName());
        CertificationResponse response = certificationService.createCertification(userId, request);
        return ResponseUtils.created(response, "Certification created successfully");
    }

    /**
     * Get all certifications for current user
     * GET /api/v1/portfolio/certifications
     * Requires: Authenticated user
     * Query params: issuer, expired, expiringSoon, verifiedOnly
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CertificationResponse>>> getUserCertifications(
            @RequestParam(required = false) String issuer,
            @RequestParam(required = false, defaultValue = "false") Boolean expired,
            @RequestParam(required = false, defaultValue = "false") Boolean expiringSoon,
            @RequestParam(required = false) Integer expiringDays,
            @RequestParam(required = false, defaultValue = "false") Boolean verifiedOnly) {
        Long userId = getCurrentUserId();
        log.debug("Fetching certifications for user ID: {}, issuer: {}, expired: {}, expiringSoon: {}, verifiedOnly: {}",
                userId, issuer, expired, expiringSoon, verifiedOnly);

        List<CertificationResponse> certifications;
        if (verifiedOnly) {
            certifications = certificationService.getVerifiedCertifications(userId);
            // Apply additional filters if provided
            if (issuer != null && !issuer.isEmpty()) {
                certifications = certifications.stream()
                        .filter(c -> issuer.equalsIgnoreCase(c.getIssuer()))
                        .toList();
            }
            if (expired) {
                certifications = certifications.stream()
                        .filter(c -> Boolean.TRUE.equals(c.getIsExpired()))
                        .toList();
            }
        } else if (expired) {
            certifications = certificationService.getExpiredCertifications(userId);
            // Filter by issuer if provided
            if (issuer != null && !issuer.isEmpty()) {
                certifications = certifications.stream()
                        .filter(c -> issuer.equalsIgnoreCase(c.getIssuer()))
                        .toList();
            }
        } else if (expiringSoon) {
            int days = expiringDays != null && expiringDays > 0 ? expiringDays : 90;
            certifications = certificationService.getExpiringSoonCertifications(userId, days);
            // Filter by issuer if provided
            if (issuer != null && !issuer.isEmpty()) {
                certifications = certifications.stream()
                        .filter(c -> issuer.equalsIgnoreCase(c.getIssuer()))
                        .toList();
            }
        } else if (issuer != null && !issuer.isEmpty()) {
            certifications = certificationService.getUserCertificationsByIssuer(userId, issuer);
        } else {
            certifications = certificationService.getUserCertifications(userId);
        }

        return ResponseUtils.ok(certifications);
    }

    /**
     * Get certification by ID
     * GET /api/v1/portfolio/certifications/{id}
     * Requires: User owns the certification OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificationResponse>> getCertification(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching certification ID: {} for user ID: {}", id, userId);

        CertificationResponse certification = certificationService.getCertification(id, userId);

        // Verify ownership (service already validates, but double-check for security)
        if (!certification.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own certifications");
        }

        return ResponseUtils.ok(certification);
    }

    /**
     * Update certification
     * PUT /api/v1/portfolio/certifications/{id}
     * Requires: User owns the certification OR ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificationResponse>> updateCertification(
            @PathVariable Long id,
            @Valid @RequestBody CertificationRequest request) {
        Long userId = getCurrentUserId();
        log.info("Certification update request received for certification ID: {}, user ID: {}", id, userId);
        CertificationResponse response = certificationService.updateCertification(id, userId, request);
        return ResponseUtils.ok(response, "Certification updated successfully");
    }

    /**
     * Delete certification
     * DELETE /api/v1/portfolio/certifications/{id}
     * Requires: User owns the certification OR ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteCertification(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Certification deletion request received for certification ID: {}, user ID: {}", id, userId);
        certificationService.deleteCertification(id, userId);
        return ResponseUtils.noContent();
    }

    /**
     * Reorder certifications
     * PUT /api/v1/portfolio/certifications/reorder
     * Requires: Authenticated user
     */
    @PutMapping("/reorder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reorderCertifications(
            @RequestBody Map<Long, Integer> certificationOrderMap) {
        Long userId = getCurrentUserId();
        log.info("Certification reorder request received for user ID: {}", userId);
        certificationService.reorderCertifications(userId, certificationOrderMap);
        return ResponseUtils.ok(null, "Certifications reordered successfully");
    }

    /**
     * Search certifications by name
     * GET /api/v1/portfolio/certifications/search?name={name}
     * Requires: Authenticated user
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CertificationResponse>>> searchCertificationsByName(
            @RequestParam String name) {
        Long userId = getCurrentUserId();
        log.debug("Searching certifications for user ID: {} with name: {}", userId, name);
        List<CertificationResponse> certifications = certificationService.searchCertificationsByName(userId, name);
        return ResponseUtils.ok(certifications);
    }

    /**
     * Search certifications by issuer
     * GET /api/v1/portfolio/certifications/search/issuer?issuer={issuer}
     * Requires: Authenticated user
     */
    @GetMapping("/search/issuer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CertificationResponse>>> searchCertificationsByIssuer(
            @RequestParam String issuer) {
        Long userId = getCurrentUserId();
        log.debug("Searching certifications for user ID: {} with issuer: {}", userId, issuer);
        List<CertificationResponse> certifications = certificationService.searchCertificationsByIssuer(userId, issuer);
        return ResponseUtils.ok(certifications);
    }
}

