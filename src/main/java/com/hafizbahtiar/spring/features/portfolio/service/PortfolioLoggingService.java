package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.model.PortfolioLog;
import com.hafizbahtiar.spring.features.portfolio.repository.mongodb.PortfolioLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for logging portfolio events to MongoDB.
 * Provides methods to log various portfolio-related events for audit and
 * analytics purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioLoggingService {

    private final PortfolioLogRepository portfolioLogRepository;

    /**
     * Log skill creation event
     */
    @Async
    public void logSkillCreated(Long skillId, Long userId, String skillName, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/skills")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName(skillName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("SKILL")
                    .entityId(skillId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged skill creation for skillId: {}", skillId);
        } catch (Exception e) {
            log.error("Failed to log skill creation event", e);
        }
    }

    /**
     * Log skill update event
     */
    @Async
    public void logSkillUpdated(Long skillId, Long userId, String skillName, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/skills/" + skillId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName(skillName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("SKILL")
                    .entityId(skillId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged skill update for skillId: {}", skillId);
        } catch (Exception e) {
            log.error("Failed to log skill update event", e);
        }
    }

    /**
     * Log skill deletion event
     */
    @Async
    public void logSkillDeleted(Long skillId, Long userId, String skillName, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/skills/" + skillId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName(skillName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("SKILL")
                    .entityId(skillId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged skill deletion for skillId: {}", skillId);
        } catch (Exception e) {
            log.error("Failed to log skill deletion event", e);
        }
    }

    /**
     * Log skill reorder event
     */
    @Async
    public void logSkillsReordered(Long userId, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/skills/reorder")
                    .method("PUT")
                    .responseStatus(200)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("SKILL")
                    .eventType("REORDERED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged skills reorder for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log skills reorder event", e);
        }
    }

    /**
     * Log experience creation event
     */
    @Async
    public void logExperienceCreated(Long experienceId, Long userId, String company, String position,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/experiences")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName(company + " - " + position)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("EXPERIENCE")
                    .entityId(experienceId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged experience creation for experienceId: {}", experienceId);
        } catch (Exception e) {
            log.error("Failed to log experience creation event", e);
        }
    }

    /**
     * Log experience update event
     */
    @Async
    public void logExperienceUpdated(Long experienceId, Long userId, String company, String position,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/experiences/" + experienceId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName(company + " - " + position)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("EXPERIENCE")
                    .entityId(experienceId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged experience update for experienceId: {}", experienceId);
        } catch (Exception e) {
            log.error("Failed to log experience update event", e);
        }
    }

    /**
     * Log experience deletion event
     */
    @Async
    public void logExperienceDeleted(Long experienceId, Long userId, String company, String position,
            HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/experiences/" + experienceId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName(company + " - " + position)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("EXPERIENCE")
                    .entityId(experienceId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged experience deletion for experienceId: {}", experienceId);
        } catch (Exception e) {
            log.error("Failed to log experience deletion event", e);
        }
    }

    /**
     * Log experience reorder event
     */
    @Async
    public void logExperiencesReordered(Long userId, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/experiences/reorder")
                    .method("PUT")
                    .responseStatus(200)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("EXPERIENCE")
                    .eventType("REORDERED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged experiences reorder for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log experiences reorder event", e);
        }
    }

    /**
     * Log project creation event
     */
    @Async
    public void logProjectCreated(Long projectId, Long userId, String projectTitle, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/projects")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName(projectTitle)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("PROJECT")
                    .entityId(projectId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged project creation for projectId: {}", projectId);
        } catch (Exception e) {
            log.error("Failed to log project creation event", e);
        }
    }

    /**
     * Log project update event
     */
    @Async
    public void logProjectUpdated(Long projectId, Long userId, String projectTitle, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/projects/" + projectId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName(projectTitle)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("PROJECT")
                    .entityId(projectId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged project update for projectId: {}", projectId);
        } catch (Exception e) {
            log.error("Failed to log project update event", e);
        }
    }

    /**
     * Log project deletion event
     */
    @Async
    public void logProjectDeleted(Long projectId, Long userId, String projectTitle, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/projects/" + projectId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName(projectTitle)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("PROJECT")
                    .entityId(projectId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged project deletion for projectId: {}", projectId);
        } catch (Exception e) {
            log.error("Failed to log project deletion event", e);
        }
    }

    /**
     * Log project featured status change event
     */
    @Async
    public void logProjectFeatured(Long projectId, Long userId, String projectTitle, boolean featured,
            HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/projects/" + projectId + "/feature")
                    .method("PUT")
                    .responseStatus(200)
                    .entityName(projectTitle)
                    .previousFeaturedStatus(!featured)
                    .newFeaturedStatus(featured)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("PROJECT")
                    .entityId(projectId)
                    .eventType(featured ? "FEATURED" : "UNFEATURED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged project featured status change for projectId: {}, featured: {}", projectId, featured);
        } catch (Exception e) {
            log.error("Failed to log project featured status change event", e);
        }
    }

    /**
     * Log project reorder event
     */
    @Async
    public void logProjectsReordered(Long userId, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/projects/reorder")
                    .method("PUT")
                    .responseStatus(200)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("PROJECT")
                    .eventType("REORDERED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged projects reorder for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log projects reorder event", e);
        }
    }

    /**
     * Log education creation event
     */
    @Async
    public void logEducationCreated(Long educationId, Long userId, String institution, String fieldOfStudy,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/educations")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName(institution + " - " + fieldOfStudy)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("EDUCATION")
                    .entityId(educationId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged education creation for educationId: {}", educationId);
        } catch (Exception e) {
            log.error("Failed to log education creation event", e);
        }
    }

    /**
     * Log education update event
     */
    @Async
    public void logEducationUpdated(Long educationId, Long userId, String institution, String fieldOfStudy,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/educations/" + educationId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName(institution + " - " + fieldOfStudy)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("EDUCATION")
                    .entityId(educationId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged education update for educationId: {}", educationId);
        } catch (Exception e) {
            log.error("Failed to log education update event", e);
        }
    }

    /**
     * Log education deletion event
     */
    @Async
    public void logEducationDeleted(Long educationId, Long userId, String institution, String fieldOfStudy,
            HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/educations/" + educationId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName(institution + " - " + fieldOfStudy)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("EDUCATION")
                    .entityId(educationId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged education deletion for educationId: {}", educationId);
        } catch (Exception e) {
            log.error("Failed to log education deletion event", e);
        }
    }

    /**
     * Log education reorder event
     */
    @Async
    public void logEducationsReordered(Long userId, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/educations/reorder")
                    .method("PUT")
                    .responseStatus(200)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("EDUCATION")
                    .eventType("REORDERED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged educations reorder for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log educations reorder event", e);
        }
    }

    /**
     * Log company creation event
     */
    @Async
    public void logCompanyCreated(Long companyId, Long userId, String companyName, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/companies")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName(companyName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("COMPANY")
                    .entityId(companyId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged company creation for companyId: {}", companyId);
        } catch (Exception e) {
            log.error("Failed to log company creation event", e);
        }
    }

    /**
     * Log company update event
     */
    @Async
    public void logCompanyUpdated(Long companyId, Long userId, String companyName, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/companies/" + companyId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName(companyName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("COMPANY")
                    .entityId(companyId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged company update for companyId: {}", companyId);
        } catch (Exception e) {
            log.error("Failed to log company update event", e);
        }
    }

    /**
     * Log company deletion event
     */
    @Async
    public void logCompanyDeleted(Long companyId, Long userId, String companyName, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/companies/" + companyId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName(companyName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("COMPANY")
                    .entityId(companyId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged company deletion for companyId: {}", companyId);
        } catch (Exception e) {
            log.error("Failed to log company deletion event", e);
        }
    }

    /**
     * Log company reorder event
     */
    @Async
    public void logCompaniesReordered(Long userId, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/companies/reorder")
                    .method("PUT")
                    .responseStatus(200)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("COMPANY")
                    .eventType("REORDERED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged companies reorder for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log companies reorder event", e);
        }
    }

    /**
     * Log certification creation event
     */
    @Async
    public void logCertificationCreated(Long certificationId, Long userId, String certificationName, String issuer,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/certifications")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName(certificationName + " (" + issuer + ")")
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("CERTIFICATION")
                    .entityId(certificationId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged certification creation for certificationId: {}", certificationId);
        } catch (Exception e) {
            log.error("Failed to log certification creation event", e);
        }
    }

    /**
     * Log certification update event
     */
    @Async
    public void logCertificationUpdated(Long certificationId, Long userId, String certificationName, String issuer,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/certifications/" + certificationId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName(certificationName + " (" + issuer + ")")
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("CERTIFICATION")
                    .entityId(certificationId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged certification update for certificationId: {}", certificationId);
        } catch (Exception e) {
            log.error("Failed to log certification update event", e);
        }
    }

    /**
     * Log certification deletion event
     */
    @Async
    public void logCertificationDeleted(Long certificationId, Long userId, String certificationName, String issuer,
            HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/certifications/" + certificationId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName(certificationName + " (" + issuer + ")")
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("CERTIFICATION")
                    .entityId(certificationId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged certification deletion for certificationId: {}", certificationId);
        } catch (Exception e) {
            log.error("Failed to log certification deletion event", e);
        }
    }

    /**
     * Log certification reorder event
     */
    @Async
    public void logCertificationsReordered(Long userId, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/certifications/reorder")
                    .method("PUT")
                    .responseStatus(200)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("CERTIFICATION")
                    .eventType("REORDERED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged certifications reorder for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log certifications reorder event", e);
        }
    }

    /**
     * Log testimonial creation event
     */
    @Async
    public void logTestimonialCreated(Long testimonialId, Long userId, String authorName, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/testimonials")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName("Testimonial by " + authorName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("TESTIMONIAL")
                    .entityId(testimonialId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged testimonial creation for testimonialId: {}", testimonialId);
        } catch (Exception e) {
            log.error("Failed to log testimonial creation event", e);
        }
    }

    /**
     * Log testimonial update event
     */
    @Async
    public void logTestimonialUpdated(Long testimonialId, Long userId, String authorName, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/testimonials/" + testimonialId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName("Testimonial by " + authorName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("TESTIMONIAL")
                    .entityId(testimonialId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged testimonial update for testimonialId: {}", testimonialId);
        } catch (Exception e) {
            log.error("Failed to log testimonial update event", e);
        }
    }

    /**
     * Log testimonial deletion event
     */
    @Async
    public void logTestimonialDeleted(Long testimonialId, Long userId, String authorName, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/testimonials/" + testimonialId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName("Testimonial by " + authorName)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("TESTIMONIAL")
                    .entityId(testimonialId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged testimonial deletion for testimonialId: {}", testimonialId);
        } catch (Exception e) {
            log.error("Failed to log testimonial deletion event", e);
        }
    }

    /**
     * Log testimonial featured status change event
     */
    @Async
    public void logTestimonialFeatured(Long testimonialId, Long userId, String authorName, boolean featured,
            HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/testimonials/" + testimonialId + "/feature")
                    .method("PUT")
                    .responseStatus(200)
                    .entityName("Testimonial by " + authorName)
                    .previousFeaturedStatus(!featured)
                    .newFeaturedStatus(featured)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("TESTIMONIAL")
                    .entityId(testimonialId)
                    .eventType(featured ? "FEATURED" : "UNFEATURED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged testimonial featured status change for testimonialId: {}, featured: {}", testimonialId,
                    featured);
        } catch (Exception e) {
            log.error("Failed to log testimonial featured status change event", e);
        }
    }

    /**
     * Log testimonial approval event
     */
    @Async
    public void logTestimonialApproved(Long testimonialId, Long userId, String authorName, boolean previousApproved,
            HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/testimonials/" + testimonialId + "/approve")
                    .method("PUT")
                    .responseStatus(200)
                    .entityName("Testimonial by " + authorName)
                    .previousActiveStatus(previousApproved)
                    .newActiveStatus(true)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("TESTIMONIAL")
                    .entityId(testimonialId)
                    .eventType("APPROVED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged testimonial approval for testimonialId: {}", testimonialId);
        } catch (Exception e) {
            log.error("Failed to log testimonial approval event", e);
        }
    }

    /**
     * Log testimonial reorder event
     */
    @Async
    public void logTestimonialsReordered(Long userId, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/testimonials/reorder")
                    .method("PUT")
                    .responseStatus(200)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("TESTIMONIAL")
                    .eventType("REORDERED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged testimonials reorder for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log testimonials reorder event", e);
        }
    }

    // ============================================================================
    // CONTACT LOGGING METHODS
    // ============================================================================

    /**
     * Log contact creation event
     */
    @Async
    public void logContactCreated(Long contactId, Long userId, String email, String subject,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/contacts")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName("Contact from " + email + ": " + subject)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("CONTACT")
                    .entityId(contactId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged contact creation for contactId: {}, email: {}", contactId, email);
        } catch (Exception e) {
            log.error("Failed to log contact creation event", e);
        }
    }

    /**
     * Log contact update event
     */
    @Async
    public void logContactUpdated(Long contactId, Long userId, String email, String subject,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/contacts/" + contactId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName("Contact from " + email + ": " + subject)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("CONTACT")
                    .entityId(contactId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged contact update for contactId: {}, email: {}", contactId, email);
        } catch (Exception e) {
            log.error("Failed to log contact update event", e);
        }
    }

    /**
     * Log contact deletion event
     */
    @Async
    public void logContactDeleted(Long contactId, Long userId, String email, String subject,
            HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/contacts/" + contactId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName("Contact from " + email + ": " + subject)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("CONTACT")
                    .entityId(contactId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged contact deletion for contactId: {}, email: {}", contactId, email);
        } catch (Exception e) {
            log.error("Failed to log contact deletion event", e);
        }
    }

    /**
     * Log contact status change event (read, replied, archived)
     */
    @Async
    public void logContactStatusChanged(Long contactId, Long userId, String email,
            com.hafizbahtiar.spring.features.portfolio.entity.ContactStatus previousStatus,
            com.hafizbahtiar.spring.features.portfolio.entity.ContactStatus newStatus,
            HttpServletRequest request) {
        try {
            String endpoint = "/api/v1/portfolio/contacts/" + contactId;
            String eventType = switch (newStatus) {
                case READ -> "MARKED_READ";
                case REPLIED -> "MARKED_REPLIED";
                case ARCHIVED -> "ARCHIVED";
                default -> "STATUS_CHANGED";
            };

            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint(endpoint)
                    .method("PUT")
                    .responseStatus(200)
                    .entityName("Contact from " + email)
                    .previousStatus(previousStatus != null ? previousStatus.name() : null)
                    .newStatus(newStatus != null ? newStatus.name() : null)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("CONTACT")
                    .entityId(contactId)
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged contact status change for contactId: {}, from {} to {}", contactId, previousStatus,
                    newStatus);
        } catch (Exception e) {
            log.error("Failed to log contact status change event", e);
        }
    }

    /**
     * Log portfolio profile update event
     */
    @Async
    public void logPortfolioProfileUpdated(Long profileId, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/profile")
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName("Portfolio Profile")
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("PORTFOLIO_PROFILE")
                    .entityId(profileId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged portfolio profile update for profileId: {}", profileId);
        } catch (Exception e) {
            log.error("Failed to log portfolio profile update event", e);
        }
    }

    /**
     * Log blog creation event
     */
    @Async
    public void logBlogCreated(Long blogId, Long userId, String blogTitle, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/blogs")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .entityName(blogTitle)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("BLOG")
                    .entityId(blogId)
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged blog creation for blogId: {}", blogId);
        } catch (Exception e) {
            log.error("Failed to log blog creation event", e);
        }
    }

    /**
     * Log blog update event
     */
    @Async
    public void logBlogUpdated(Long blogId, Long userId, String blogTitle, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/blogs/" + blogId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName(blogTitle)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("BLOG")
                    .entityId(blogId)
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged blog update for blogId: {}", blogId);
        } catch (Exception e) {
            log.error("Failed to log blog update event", e);
        }
    }

    /**
     * Log blog deletion event
     */
    @Async
    public void logBlogDeleted(Long blogId, Long userId, String blogTitle, HttpServletRequest request) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/blogs/" + blogId)
                    .method("DELETE")
                    .responseStatus(204)
                    .entityName(blogTitle)
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId)
                    .entityType("BLOG")
                    .entityId(blogId)
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged blog deletion for blogId: {}", blogId);
        } catch (Exception e) {
            log.error("Failed to log blog deletion event", e);
        }
    }

    /**
     * Log blog view tracking event
     */
    @Async
    public void logBlogViewTracked(Long blogId, Long userId, boolean tracked, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PortfolioLog.PortfolioEventDetails details = PortfolioLog.PortfolioEventDetails.builder()
                    .endpoint("/api/v1/portfolio/blogs/" + blogId + "/views")
                    .method("POST")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .entityName("Blog View")
                    .build();

            PortfolioLog portfolioLog = PortfolioLog.builder()
                    .userId(userId) // May be null for anonymous views
                    .entityType("BLOG")
                    .entityId(blogId)
                    .eventType(tracked ? "VIEW_TRACKED" : "VIEW_DUPLICATE")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(tracked)
                    .details(details)
                    .build();

            portfolioLogRepository.save(portfolioLog);
            log.debug("Logged blog view tracking for blogId: {}, tracked: {}", blogId, tracked);
        } catch (Exception e) {
            log.error("Failed to log blog view tracking event", e);
        }
    }

    // Helper methods for extracting request information

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private String getSessionId(HttpServletRequest request) {
        return request != null && request.getSession(false) != null ? request.getSession().getId() : null;
    }

    private String getRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = request.getHeader("X-Correlation-ID");
        }
        return requestId;
    }
}
