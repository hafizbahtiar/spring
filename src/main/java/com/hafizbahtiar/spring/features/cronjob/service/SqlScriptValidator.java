package com.hafizbahtiar.spring.features.cronjob.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validator for SQL scripts used in DATABASE type cron jobs.
 * Performs basic validation to ensure SQL scripts are safe and well-formed.
 */
@Component
@Slf4j
public class SqlScriptValidator {

    /**
     * Set of dangerous SQL keywords that should be avoided or used with caution
     */
    private static final Set<String> DANGEROUS_KEYWORDS = new HashSet<>(Arrays.asList(
            "DROP DATABASE",
            "DROP SCHEMA",
            "TRUNCATE",
            "DROP TABLE",
            "DELETE FROM", // Without WHERE clause
            "ALTER DATABASE",
            "ALTER SCHEMA"));

    /**
     * Pattern to match SQL keywords (case-insensitive)
     */
    private static final Pattern SQL_KEYWORD_PATTERN = Pattern.compile(
            "\\b(SELECT|INSERT|UPDATE|DELETE|CALL|DO|PERFORM|EXECUTE|CREATE|ALTER|DROP|TRUNCATE)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Validate SQL script for basic safety and structure.
     *
     * @param sqlScript SQL script to validate
     * @return Validation result with error message if invalid
     */
    public ValidationResult validate(String sqlScript) {
        if (sqlScript == null || sqlScript.trim().isEmpty()) {
            return ValidationResult.invalid("SQL script cannot be empty");
        }

        String trimmedScript = sqlScript.trim();

        // Check for dangerous operations
        String upperScript = trimmedScript.toUpperCase();
        for (String dangerousKeyword : DANGEROUS_KEYWORDS) {
            if (upperScript.contains(dangerousKeyword)) {
                log.warn("SQL script contains potentially dangerous operation: {}", dangerousKeyword);
                // Don't fail, just warn - admin should review
            }
        }

        // Check for basic SQL structure (must contain at least one SQL keyword)
        if (!SQL_KEYWORD_PATTERN.matcher(trimmedScript).find()) {
            return ValidationResult.invalid(
                    "SQL script does not appear to contain valid SQL statements. Must contain at least one SQL keyword.");
        }

        // Check for balanced quotes
        if (!hasBalancedQuotes(trimmedScript)) {
            return ValidationResult.invalid("SQL script has unbalanced quotes (single or double)");
        }

        // Check for balanced parentheses
        if (!hasBalancedParentheses(trimmedScript)) {
            return ValidationResult.invalid("SQL script has unbalanced parentheses");
        }

        // Check for balanced brackets (PostgreSQL array syntax)
        if (!hasBalancedBrackets(trimmedScript)) {
            return ValidationResult.invalid("SQL script has unbalanced brackets");
        }

        return ValidationResult.valid();
    }

    /**
     * Check if SQL script has balanced single and double quotes
     */
    private boolean hasBalancedQuotes(String script) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;

        for (char c : script.toCharArray()) {
            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }
        }

        return !inSingleQuote && !inDoubleQuote;
    }

    /**
     * Check if SQL script has balanced parentheses
     */
    private boolean hasBalancedParentheses(String script) {
        int count = 0;
        boolean inString = false;
        char stringChar = 0;

        for (char c : script.toCharArray()) {
            if (c == '\'' || c == '"') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                    stringChar = 0;
                }
            }

            if (!inString) {
                if (c == '(') {
                    count++;
                } else if (c == ')') {
                    count--;
                    if (count < 0) {
                        return false; // Closing parenthesis before opening
                    }
                }
            }
        }

        return count == 0;
    }

    /**
     * Check if SQL script has balanced brackets
     */
    private boolean hasBalancedBrackets(String script) {
        int count = 0;
        boolean inString = false;
        char stringChar = 0;

        for (char c : script.toCharArray()) {
            if (c == '\'' || c == '"') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                    stringChar = 0;
                }
            }

            if (!inString) {
                if (c == '[') {
                    count++;
                } else if (c == ']') {
                    count--;
                    if (count < 0) {
                        return false; // Closing bracket before opening
                    }
                }
            }
        }

        return count == 0;
    }

    /**
     * Validation result for SQL script validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String error;

        private ValidationResult(boolean valid, String error) {
            this.valid = valid;
            this.error = error;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String error) {
            return new ValidationResult(false, error);
        }

        public boolean isValid() {
            return valid;
        }

        public String getError() {
            return error;
        }
    }
}
