package com.hafizbahtiar.spring.common.exception;

import java.util.Map;

public class ValidationException extends RuntimeException {

    private final Map<String, String> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = null;
    }

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationErrors = null;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }
}
