package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.CompanyRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.CompanyResponse;
import com.hafizbahtiar.spring.features.portfolio.service.CompanyService;
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
 * REST controller for company management endpoints.
 * Handles company CRUD operations, filtering, and display order management.
 */
@RestController
@RequestMapping("/api/v1/portfolio/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;
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
     * Create a new company
     * POST /api/v1/portfolio/companies
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CompanyRequest request) {
        Long userId = getCurrentUserId();
        log.info("Company creation request received for user ID: {}, company name: {}", userId, request.getName());
        CompanyResponse response = companyService.createCompany(userId, request);
        return ResponseUtils.created(response, "Company created successfully");
    }

    /**
     * Get all companies for current user
     * GET /api/v1/portfolio/companies
     * Requires: Authenticated user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getUserCompanies(
            @RequestParam(required = false) String industry,
            @RequestParam(required = false, defaultValue = "false") Boolean verifiedOnly) {
        Long userId = getCurrentUserId();
        log.debug("Fetching companies for user ID: {}, industry: {}, verifiedOnly: {}",
                userId, industry, verifiedOnly);

        List<CompanyResponse> companies;
        if (verifiedOnly) {
            companies = companyService.getVerifiedUserCompanies(userId);
            // Filter by industry if provided
            if (industry != null && !industry.isEmpty()) {
                companies = companies.stream()
                        .filter(c -> industry.equalsIgnoreCase(c.getIndustry()))
                        .toList();
            }
        } else if (industry != null && !industry.isEmpty()) {
            companies = companyService.getUserCompaniesByIndustry(userId, industry);
        } else {
            companies = companyService.getUserCompanies(userId);
        }

        return ResponseUtils.ok(companies);
    }

    /**
     * Get company by ID
     * GET /api/v1/portfolio/companies/{id}
     * Requires: User owns the company OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching company ID: {} for user ID: {}", id, userId);

        CompanyResponse company = companyService.getCompany(id, userId);

        // Verify ownership (service already validates, but double-check for security)
        if (!company.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own companies");
        }

        return ResponseUtils.ok(company);
    }

    /**
     * Update company
     * PUT /api/v1/portfolio/companies/{id}
     * Requires: User owns the company OR ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        Long userId = getCurrentUserId();
        log.info("Company update request received for company ID: {}, user ID: {}", id, userId);
        CompanyResponse response = companyService.updateCompany(id, userId, request);
        return ResponseUtils.ok(response, "Company updated successfully");
    }

    /**
     * Delete company
     * DELETE /api/v1/portfolio/companies/{id}
     * Requires: User owns the company OR ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Company deletion request received for company ID: {}, user ID: {}", id, userId);
        companyService.deleteCompany(id, userId);
        return ResponseUtils.noContent();
    }

    /**
     * Reorder companies
     * PUT /api/v1/portfolio/companies/reorder
     * Requires: Authenticated user
     */
    @PutMapping("/reorder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reorderCompanies(@RequestBody Map<Long, Integer> companyOrderMap) {
        Long userId = getCurrentUserId();
        log.info("Company reorder request received for user ID: {}", userId);
        companyService.reorderCompanies(userId, companyOrderMap);
        return ResponseUtils.ok(null, "Companies reordered successfully");
    }

    /**
     * Search companies by name
     * GET /api/v1/portfolio/companies/search
     * Requires: Authenticated user
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> searchCompanies(@RequestParam String name) {
        Long userId = getCurrentUserId();
        log.debug("Searching companies for user ID: {} with name: {}", userId, name);
        List<CompanyResponse> companies = companyService.searchUserCompaniesByName(userId, name);
        return ResponseUtils.ok(companies);
    }
}

