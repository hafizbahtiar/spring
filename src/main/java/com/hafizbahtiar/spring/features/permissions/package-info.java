/**
 * Permissions feature module for dynamic groups and permissions system (Layer
 * 2).
 * 
 * <p>
 * This feature provides a flexible permission system where OWNER/ADMIN can
 * create Groups
 * and assign fine-grained permissions at three levels:
 * <ul>
 * <li><b>Module Level</b> - Access to entire feature modules (e.g., "support",
 * "finance", "portfolio")</li>
 * <li><b>Page Level</b> - Access to specific pages within modules (e.g.,
 * "support.chat", "support.tickets")</li>
 * <li><b>Component Level</b> - Access to specific UI components/actions (e.g.,
 * "edit_button", "delete_button")</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Key features:
 * <ul>
 * <li>Permission groups with fine-grained access control</li>
 * <li>Automatic permission inheritance (MODULE → PAGE → COMPONENT)</li>
 * <li>Explicit deny support (deny overrides allow)</li>
 * <li>Multiple groups per user (OR logic with deny override)</li>
 * <li>Permission registry for modules, pages, and components</li>
 * <li>Creator access validation (users can only grant permissions they
 * have)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This extends the static role system (Layer 1: OWNER, ADMIN, USER) with
 * dynamic,
 * configurable permissions (Layer 2).
 * </p>
 */
package com.hafizbahtiar.spring.features.permissions;
