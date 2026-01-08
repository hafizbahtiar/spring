package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.CompanyRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.CompanyResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for company management.
 * Handles CRUD operations and display order management for user companies.
 */
public interface CompanyService {

    /**
     * Create a new company for a user.
     *
     * @param userId  User ID
     * @param request Company creation request
     * @return Created CompanyResponse
     */
    CompanyResponse createCompany(Long userId, CompanyRequest request);

    /**
     * Update an existing company.
     *
     * @param companyId Company ID
     * @param userId    User ID (for ownership validation)
     * @param request   Update request
     * @return Updated CompanyResponse
     */
    CompanyResponse updateCompany(Long companyId, Long userId, CompanyRequest request);

    /**
     * Delete a company.
     *
     * @param companyId Company ID
     * @param userId    User ID (for ownership validation)
     */
    void deleteCompany(Long companyId, Long userId);

    /**
     * Get company by ID.
     *
     * @param companyId Company ID
     * @param userId    User ID (for ownership validation)
     * @return CompanyResponse
     */
    CompanyResponse getCompany(Long companyId, Long userId);

    /**
     * Get all companies for a user.
     *
     * @param userId User ID
     * @return List of CompanyResponse
     */
    List<CompanyResponse> getUserCompanies(Long userId);

    /**
     * Get companies for a user filtered by industry.
     *
     * @param userId   User ID
     * @param industry Industry (optional)
     * @return List of CompanyResponse
     */
    List<CompanyResponse> getUserCompaniesByIndustry(Long userId, String industry);

    /**
     * Get verified companies only for a user.
     *
     * @param userId User ID
     * @return List of CompanyResponse
     */
    List<CompanyResponse> getVerifiedUserCompanies(Long userId);

    /**
     * Search companies by name for a user.
     *
     * @param userId User ID
     * @param name   Company name (partial match, case-insensitive)
     * @return List of CompanyResponse
     */
    List<CompanyResponse> searchUserCompaniesByName(Long userId, String name);

    /**
     * Reorder companies by updating display order.
     *
     * @param userId         User ID
     * @param companyOrderMap Map of company ID to display order
     */
    void reorderCompanies(Long userId, Map<Long, Integer> companyOrderMap);
}

