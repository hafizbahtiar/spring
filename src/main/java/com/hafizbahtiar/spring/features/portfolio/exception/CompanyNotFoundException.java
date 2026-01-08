package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when a company is not found.
 */
public class CompanyNotFoundException extends RuntimeException {

    public CompanyNotFoundException(String message) {
        super(message);
    }

    public static CompanyNotFoundException byId(Long companyId) {
        return new CompanyNotFoundException("Company not found with ID: " + companyId);
    }

    public static CompanyNotFoundException byIdAndUser(Long companyId, Long userId) {
        return new CompanyNotFoundException("Company not found with ID: " + companyId + " for user ID: " + userId);
    }
}

