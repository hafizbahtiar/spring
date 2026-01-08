package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * General exception for portfolio-related errors.
 */
public class PortfolioException extends RuntimeException {

    public PortfolioException(String message) {
        super(message);
    }

    public PortfolioException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PortfolioException invalidDateRange(String message) {
        return new PortfolioException("Invalid date range: " + message);
    }

    public static PortfolioException invalidUrl(String url, String fieldName) {
        return new PortfolioException(fieldName + " is not a valid URL: " + url);
    }

    public static PortfolioException ownershipViolation(Long resourceId, Long userId) {
        return new PortfolioException("Resource with ID: " + resourceId + " does not belong to user ID: " + userId);
    }

    public static PortfolioException displayOrderError(String message) {
        return new PortfolioException("Display order error: " + message);
    }

    public static PortfolioException invalidInput(String message) {
        return new PortfolioException("Invalid input: " + message);
    }
}
