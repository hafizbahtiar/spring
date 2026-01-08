package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.CertificationRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.CertificationResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Certification;
import com.hafizbahtiar.spring.features.portfolio.exception.CertificationNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.PortfolioException;
import com.hafizbahtiar.spring.features.portfolio.mapper.CertificationMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.CertificationRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Implementation of CertificationService.
 * Handles certification CRUD operations, expiry management, and display order
 * management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository certificationRepository;
    private final CertificationMapper certificationMapper;
    private final UserRepository userRepository;
    private final PortfolioLoggingService portfolioLoggingService;

    /**
     * Get current HttpServletRequest for logging context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not retrieve current request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate expiry date is after issue date
     */
    private void validateExpiryDate(LocalDate issueDate, LocalDate expiryDate) {
        if (expiryDate != null && issueDate != null && expiryDate.isBefore(issueDate)) {
            throw new PortfolioException("Expiry date must be after issue date");
        }
    }

    @Override
    public CertificationResponse createCertification(Long userId, CertificationRequest request) {
        log.debug("Creating certification for user ID: {}, certification name: {}", userId, request.getName());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate expiry date
        validateExpiryDate(request.getIssueDate(), request.getExpiryDate());

        // Map request to entity
        Certification certification = certificationMapper.toEntity(request);
        certification.setUser(user);

        // Update expiry status
        certification.isExpired();

        // Set display order if not provided
        if (certification.getDisplayOrder() == null) {
            long maxOrder = certificationRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                    .mapToLong(Certification::getDisplayOrder)
                    .max()
                    .orElse(-1);
            certification.setDisplayOrder((int) (maxOrder + 1));
        }

        long startTime = System.currentTimeMillis();
        Certification savedCertification = certificationRepository.save(certification);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Certification created successfully with ID: {} for user ID: {}", savedCertification.getId(), userId);

        // Log certification creation
        portfolioLoggingService.logCertificationCreated(
                savedCertification.getId(),
                userId,
                savedCertification.getName(),
                savedCertification.getIssuer(),
                getCurrentRequest(),
                responseTime);

        return certificationMapper.toResponse(savedCertification);
    }

    @Override
    public CertificationResponse updateCertification(Long certificationId, Long userId, CertificationRequest request) {
        log.debug("Updating certification ID: {} for user ID: {}", certificationId, userId);

        // Validate certification exists and belongs to user
        Certification certification = certificationRepository.findByUserIdAndId(userId, certificationId)
                .orElseThrow(() -> CertificationNotFoundException.byIdAndUser(certificationId, userId));

        // Validate expiry date
        LocalDate issueDate = request.getIssueDate() != null ? request.getIssueDate() : certification.getIssueDate();
        validateExpiryDate(issueDate, request.getExpiryDate());

        // Update entity from request
        certificationMapper.updateEntityFromRequest(request, certification);

        // Update expiry status
        certification.isExpired();

        long startTime = System.currentTimeMillis();
        Certification updatedCertification = certificationRepository.save(certification);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Certification updated successfully with ID: {}", updatedCertification.getId());

        // Log certification update
        portfolioLoggingService.logCertificationUpdated(
                updatedCertification.getId(),
                userId,
                updatedCertification.getName(),
                updatedCertification.getIssuer(),
                getCurrentRequest(),
                responseTime);

        return certificationMapper.toResponse(updatedCertification);
    }

    @Override
    public void deleteCertification(Long certificationId, Long userId) {
        log.debug("Deleting certification ID: {} for user ID: {}", certificationId, userId);

        // Validate certification exists and belongs to user
        Certification certification = certificationRepository.findByUserIdAndId(userId, certificationId)
                .orElseThrow(() -> CertificationNotFoundException.byIdAndUser(certificationId, userId));

        String certificationName = certification.getName();
        String issuer = certification.getIssuer();
        certificationRepository.delete(certification);
        log.info("Certification deleted successfully with ID: {}", certificationId);

        // Log certification deletion
        portfolioLoggingService.logCertificationDeleted(certificationId, userId, certificationName, issuer,
                getCurrentRequest());
    }

    @Override
    @Transactional(readOnly = true)
    public CertificationResponse getCertification(Long certificationId, Long userId) {
        log.debug("Fetching certification ID: {} for user ID: {}", certificationId, userId);

        Certification certification = certificationRepository.findByUserIdAndId(userId, certificationId)
                .orElseThrow(() -> CertificationNotFoundException.byIdAndUser(certificationId, userId));

        // Update expiry status before returning
        certification.isExpired();

        return certificationMapper.toResponse(certification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getUserCertifications(Long userId) {
        log.debug("Fetching all certifications for user ID: {}", userId);
        List<Certification> certifications = certificationRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        // Update expiry status for all certifications
        certifications.forEach(Certification::isExpired);
        return certificationMapper.toResponseList(certifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getUserCertificationsByIssuer(Long userId, String issuer) {
        log.debug("Fetching certifications for user ID: {} by issuer: {}", userId, issuer);
        List<Certification> certifications = issuer != null && !issuer.isEmpty()
                ? certificationRepository.findByUserIdAndIssuerOrderByDisplayOrderAsc(userId, issuer)
                : certificationRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        // Update expiry status for all certifications
        certifications.forEach(Certification::isExpired);
        return certificationMapper.toResponseList(certifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getExpiredCertifications(Long userId) {
        log.debug("Fetching expired certifications for user ID: {}", userId);
        List<Certification> certifications = certificationRepository
                .findByUserIdAndIsExpiredTrueOrderByExpiryDateDesc(userId);
        // Update expiry status for all certifications
        certifications.forEach(Certification::isExpired);
        return certificationMapper.toResponseList(certifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getNonExpiredCertifications(Long userId) {
        log.debug("Fetching non-expired certifications for user ID: {}", userId);
        List<Certification> certifications = certificationRepository
                .findByUserIdAndIsExpiredFalseOrderByDisplayOrderAsc(userId);
        // Update expiry status for all certifications
        certifications.forEach(Certification::isExpired);
        return certificationMapper.toResponseList(certifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getExpiringSoonCertifications(Long userId, int days) {
        log.debug("Fetching certifications expiring soon (within {} days) for user ID: {}", days, userId);
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        List<Certification> certifications = certificationRepository.findExpiringSoonByUserId(userId, today,
                futureDate);
        // Update expiry status for all certifications
        certifications.forEach(Certification::isExpired);
        return certificationMapper.toResponseList(certifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getExpiringSoonCertifications(Long userId) {
        return getExpiringSoonCertifications(userId, 90);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getVerifiedCertifications(Long userId) {
        log.debug("Fetching verified certifications for user ID: {}", userId);
        List<Certification> certifications = certificationRepository
                .findByUserIdAndIsVerifiedTrueOrderByDisplayOrderAsc(userId);
        // Update expiry status for all certifications
        certifications.forEach(Certification::isExpired);
        return certificationMapper.toResponseList(certifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> searchCertificationsByName(Long userId, String name) {
        log.debug("Searching certifications for user ID: {} by name: {}", userId, name);
        List<Certification> certifications = certificationRepository.findByUserIdAndNameContainingIgnoreCase(userId,
                name);
        // Update expiry status for all certifications
        certifications.forEach(Certification::isExpired);
        return certificationMapper.toResponseList(certifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> searchCertificationsByIssuer(Long userId, String issuer) {
        log.debug("Searching certifications for user ID: {} by issuer: {}", userId, issuer);
        List<Certification> certifications = certificationRepository.findByUserIdAndIssuerContainingIgnoreCase(userId,
                issuer);
        // Update expiry status for all certifications
        certifications.forEach(Certification::isExpired);
        return certificationMapper.toResponseList(certifications);
    }

    @Override
    public void reorderCertifications(Long userId, Map<Long, Integer> certificationOrderMap) {
        log.debug("Reordering certifications for user ID: {}", userId);

        certificationOrderMap.forEach((certificationId, displayOrder) -> {
            Certification certification = certificationRepository.findByUserIdAndId(userId, certificationId)
                    .orElseThrow(() -> CertificationNotFoundException.byIdAndUser(certificationId, userId));
            certification.setDisplayOrder(displayOrder);
            certificationRepository.save(certification);
        });

        log.info("Certifications reordered successfully for user ID: {}", userId);

        // Log certifications reorder
        portfolioLoggingService.logCertificationsReordered(userId, getCurrentRequest());
    }
}
