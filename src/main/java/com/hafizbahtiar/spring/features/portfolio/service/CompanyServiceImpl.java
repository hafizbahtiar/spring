package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.CompanyRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.CompanyResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Company;
import com.hafizbahtiar.spring.features.portfolio.exception.CompanyNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.mapper.CompanyMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.CompanyRepository;
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

import java.util.List;
import java.util.Map;

/**
 * Implementation of CompanyService.
 * Handles company CRUD operations and display order management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
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

    @Override
    public CompanyResponse createCompany(Long userId, CompanyRequest request) {
        log.debug("Creating company for user ID: {}, company name: {}", userId, request.getName());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Map request to entity
        Company company = companyMapper.toEntity(request);
        company.setUser(user);

        // Set display order if not provided
        if (company.getDisplayOrder() == null) {
            long maxOrder = companyRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                    .mapToLong(Company::getDisplayOrder)
                    .max()
                    .orElse(-1);
            company.setDisplayOrder((int) (maxOrder + 1));
        }

        long startTime = System.currentTimeMillis();
        Company savedCompany = companyRepository.save(company);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Company created successfully with ID: {} for user ID: {}", savedCompany.getId(), userId);

        // Log company creation
        portfolioLoggingService.logCompanyCreated(
                savedCompany.getId(),
                userId,
                savedCompany.getName(),
                getCurrentRequest(),
                responseTime);

        return companyMapper.toResponse(savedCompany);
    }

    @Override
    public CompanyResponse updateCompany(Long companyId, Long userId, CompanyRequest request) {
        log.debug("Updating company ID: {} for user ID: {}", companyId, userId);

        // Validate company exists and belongs to user
        Company company = companyRepository.findByUserIdAndId(userId, companyId)
                .orElseThrow(() -> CompanyNotFoundException.byIdAndUser(companyId, userId));

        // Update entity from request
        companyMapper.updateEntityFromRequest(request, company);

        long startTime = System.currentTimeMillis();
        Company updatedCompany = companyRepository.save(company);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Company updated successfully with ID: {}", updatedCompany.getId());

        // Log company update
        portfolioLoggingService.logCompanyUpdated(
                updatedCompany.getId(),
                userId,
                updatedCompany.getName(),
                getCurrentRequest(),
                responseTime);

        return companyMapper.toResponse(updatedCompany);
    }

    @Override
    public void deleteCompany(Long companyId, Long userId) {
        log.debug("Deleting company ID: {} for user ID: {}", companyId, userId);

        // Validate company exists and belongs to user
        Company company = companyRepository.findByUserIdAndId(userId, companyId)
                .orElseThrow(() -> CompanyNotFoundException.byIdAndUser(companyId, userId));

        String companyName = company.getName();
        companyRepository.delete(company);
        log.info("Company deleted successfully with ID: {}", companyId);

        // Log company deletion
        portfolioLoggingService.logCompanyDeleted(companyId, userId, companyName, getCurrentRequest());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompany(Long companyId, Long userId) {
        log.debug("Fetching company ID: {} for user ID: {}", companyId, userId);

        Company company = companyRepository.findByUserIdAndId(userId, companyId)
                .orElseThrow(() -> CompanyNotFoundException.byIdAndUser(companyId, userId));

        return companyMapper.toResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getUserCompanies(Long userId) {
        log.debug("Fetching all companies for user ID: {}", userId);
        List<Company> companies = companyRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return companyMapper.toResponseList(companies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getUserCompaniesByIndustry(Long userId, String industry) {
        log.debug("Fetching companies for user ID: {} by industry: {}", userId, industry);
        List<Company> companies = industry != null && !industry.isEmpty()
                ? companyRepository.findByUserIdAndIndustryOrderByDisplayOrderAsc(userId, industry)
                : companyRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return companyMapper.toResponseList(companies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getVerifiedUserCompanies(Long userId) {
        log.debug("Fetching verified companies for user ID: {}", userId);
        List<Company> companies = companyRepository.findByUserIdAndIsVerifiedTrueOrderByDisplayOrderAsc(userId);
        return companyMapper.toResponseList(companies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> searchUserCompaniesByName(Long userId, String name) {
        log.debug("Searching companies for user ID: {} by name: {}", userId, name);
        List<Company> companies = companyRepository.findByUserIdAndNameContainingIgnoreCase(userId, name);
        return companyMapper.toResponseList(companies);
    }

    @Override
    public void reorderCompanies(Long userId, Map<Long, Integer> companyOrderMap) {
        log.debug("Reordering companies for user ID: {}", userId);

        companyOrderMap.forEach((companyId, displayOrder) -> {
            Company company = companyRepository.findByUserIdAndId(userId, companyId)
                    .orElseThrow(() -> CompanyNotFoundException.byIdAndUser(companyId, userId));
            company.setDisplayOrder(displayOrder);
            companyRepository.save(company);
        });

        log.info("Companies reordered successfully for user ID: {}", userId);

        // Log companies reorder
        portfolioLoggingService.logCompaniesReordered(userId, getCurrentRequest());
    }
}
