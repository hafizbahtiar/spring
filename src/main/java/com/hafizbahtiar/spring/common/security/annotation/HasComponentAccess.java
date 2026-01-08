package com.hafizbahtiar.spring.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation annotation for component access checks.
 * 
 * This annotation serves as documentation. To actually check component access,
 * use @PreAuthorize with SecurityService methods directly:
 * 
 * <pre>
 * {@code @PreAuthorize("@securityUtils.hasComponentAccess('support.chat', 'edit_button')")}
 * public ResponseEntity<?> editMessage() { ... }
 * </pre>
 * 
 * Checks Layer 1 (static role) first, then Layer 2 (group permissions).
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasComponentAccess {

    /**
     * Page key (e.g., "support.chat", "finance.dashboard")
     */
    String pageKey();

    /**
     * Component key (e.g., "edit_button", "delete_button")
     */
    String componentKey();
}

