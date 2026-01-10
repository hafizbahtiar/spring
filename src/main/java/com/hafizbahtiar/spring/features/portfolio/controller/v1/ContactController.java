package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.ContactRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ContactResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactSource;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactStatus;
import com.hafizbahtiar.spring.features.portfolio.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for contact management endpoints.
 * Handles contact CRUD operations, status management, and contact form
 * submissions.
 */
@RestController
@RequestMapping("/api/v1/portfolio/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactService contactService;
    private final SecurityService securityService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Create a new contact (submit contact form) - Public endpoint
     * POST /api/v1/portfolio/contacts/public
     * Allows anonymous users to submit contact forms when userId is provided
     * Requires: userId as query parameter
     */
    @PostMapping("/public")
    public ResponseEntity<ApiResponse<ContactResponse>> createPublicContact(
            @Valid @RequestBody ContactRequest request,
            @RequestParam Long userId) {
        log.info("Public contact creation request received for user ID: {}, from: {}", userId, request.getEmail());
        ContactResponse response = contactService.createContact(userId, request);
        return ResponseUtils.created(response, "Contact message sent successfully");
    }

    /**
     * Create a new contact (submit contact form) - Authenticated endpoint
     * POST /api/v1/portfolio/contacts
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContactResponse>> createContact(@Valid @RequestBody ContactRequest request) {
        Long userId = getCurrentUserId();
        log.info("Authenticated contact creation request received for user ID: {}, from: {}", userId,
                request.getEmail());
        ContactResponse response = contactService.createContact(userId, request);
        return ResponseUtils.created(response, "Contact message sent successfully");
    }

    /**
     * Get all contacts for current user
     * GET /api/v1/portfolio/contacts
     * Requires: Authenticated user
     * Query params: status, source, startDate, endDate
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ContactResponse>>> getUserContacts(
            @RequestParam(required = false) ContactStatus status,
            @RequestParam(required = false) ContactSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Long userId = getCurrentUserId();
        log.debug("Fetching contacts for user ID: {}, status: {}, source: {}, dateRange: {} to {}",
                userId, status, source, startDate, endDate);

        List<ContactResponse> contacts;
        if (startDate != null && endDate != null) {
            contacts = contactService.getUserContactsByDateRange(userId, startDate, endDate);
            // Apply additional filters if provided
            if (status != null) {
                contacts = contacts.stream()
                        .filter(c -> status.equals(c.getStatus()))
                        .toList();
            }
            if (source != null) {
                contacts = contacts.stream()
                        .filter(c -> source.equals(c.getSource()))
                        .toList();
            }
        } else if (status != null && source != null) {
            // Both filters - would need repository method, for now filter in memory
            contacts = contactService.getUserContactsByStatus(userId, status);
            contacts = contacts.stream()
                    .filter(c -> source.equals(c.getSource()))
                    .toList();
        } else if (status != null) {
            contacts = contactService.getUserContactsByStatus(userId, status);
        } else if (source != null) {
            contacts = contactService.getUserContactsBySource(userId, source);
        } else {
            contacts = contactService.getUserContacts(userId);
        }

        return ResponseUtils.ok(contacts);
    }

    /**
     * Get new (unread) contacts for current user
     * GET /api/v1/portfolio/contacts/new
     * Requires: Authenticated user
     */
    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ContactResponse>>> getNewContacts() {
        Long userId = getCurrentUserId();
        log.debug("Fetching new contacts for user ID: {}", userId);
        List<ContactResponse> contacts = contactService.getNewContacts(userId);
        return ResponseUtils.ok(contacts);
    }

    /**
     * Get contact by ID
     * GET /api/v1/portfolio/contacts/{id}
     * Requires: User owns the contact OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContactResponse>> getContact(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching contact ID: {} for user ID: {}", id, userId);

        ContactResponse contact = contactService.getContact(id, userId);

        // Verify ownership (service already validates, but double-check for security)
        if (!contact.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own contacts");
        }

        return ResponseUtils.ok(contact);
    }

    /**
     * Update contact
     * PUT /api/v1/portfolio/contacts/{id}
     * Requires: User owns the contact OR ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContactResponse>> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {
        Long userId = getCurrentUserId();
        log.info("Contact update request received for contact ID: {}, user ID: {}", id, userId);
        ContactResponse response = contactService.updateContact(id, userId, request);
        return ResponseUtils.ok(response, "Contact updated successfully");
    }

    /**
     * Mark contact as read
     * PUT /api/v1/portfolio/contacts/{id}/read
     * Requires: User owns the contact OR ADMIN role
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContactResponse>> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Mark as read request received for contact ID: {}, user ID: {}", id, userId);
        ContactResponse response = contactService.markAsRead(id, userId);
        return ResponseUtils.ok(response, "Contact marked as read");
    }

    /**
     * Mark contact as replied
     * PUT /api/v1/portfolio/contacts/{id}/replied
     * Requires: User owns the contact OR ADMIN role
     */
    @PutMapping("/{id}/replied")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContactResponse>> markAsReplied(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Mark as replied request received for contact ID: {}, user ID: {}", id, userId);
        ContactResponse response = contactService.markAsReplied(id, userId);
        return ResponseUtils.ok(response, "Contact marked as replied");
    }

    /**
     * Archive contact
     * PUT /api/v1/portfolio/contacts/{id}/archive
     * Requires: User owns the contact OR ADMIN role
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContactResponse>> archiveContact(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Archive contact request received for contact ID: {}, user ID: {}", id, userId);
        ContactResponse response = contactService.archiveContact(id, userId);
        return ResponseUtils.ok(response, "Contact archived successfully");
    }

    /**
     * Delete contact
     * DELETE /api/v1/portfolio/contacts/{id}
     * Requires: User owns the contact OR ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Contact deletion request received for contact ID: {}, user ID: {}", id, userId);
        contactService.deleteContact(id, userId);
        return ResponseUtils.noContent();
    }
}
