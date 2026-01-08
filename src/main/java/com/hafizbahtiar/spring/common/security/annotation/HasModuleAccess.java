package com.hafizbahtiar.spring.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation annotation for module access checks.
 * 
 * This annotation serves as documentation. To actually check module access,
 * use @PreAuthorize with SecurityService methods directly:
 * 
 * <pre>
 * {@code @PreAuthorize("@securityUtils.hasModuleAccess('support')")}
 * public ResponseEntity<?> getSupportDashboard() { ... }
 * </pre>
 * 
 * Checks Layer 1 (static role) first, then Layer 2 (group permissions).
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasModuleAccess {

    /**
     * Module key (e.g., "support", "finance")
     */
    String moduleKey();
}
