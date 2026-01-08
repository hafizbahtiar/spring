package com.hafizbahtiar.spring.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation annotation for permission checks.
 * 
 * This annotation serves as documentation. To actually check permissions,
 * use @PreAuthorize with SecurityService methods directly:
 * 
 * <pre>
 * {@code @PreAuthorize("@securityUtils.hasPermission('MODULE', 'support', 'chat', 'READ')")}
 * public ResponseEntity<?> getChatMessages() { ... }
 * </pre>
 * 
 * Checks Layer 1 (static role) first, then Layer 2 (group permissions).
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

    /**
     * Permission type: MODULE, PAGE, or COMPONENT
     */
    String type();

    /**
     * Resource type (e.g., "support", "finance")
     */
    String resourceType();

    /**
     * Resource identifier (e.g., "chat", "tickets", "edit_button")
     */
    String resourceIdentifier();

    /**
     * Permission action: READ, WRITE, DELETE, or EXECUTE
     */
    String action();
}
