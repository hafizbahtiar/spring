package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.ContactRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ContactResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactSource;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for contact management.
 * Handles CRUD operations, status management, and contact form submissions.
 */
public interface ContactService {

    /**
     * Create a new contact (submit contact form).
     *
     * @param userId  User ID (the person receiving the contact)
     * @param request Contact creation request
     * @return Created ContactResponse
     */
    ContactResponse createContact(Long userId, ContactRequest request);

    /**
     * Update an existing contact.
     *
     * @param contactId Contact ID
     * @param userId    User ID (for ownership validation)
     * @param request   Update request
     * @return Updated ContactResponse
     */
    ContactResponse updateContact(Long contactId, Long userId, ContactRequest request);

    /**
     * Delete a contact.
     *
     * @param contactId Contact ID
     * @param userId    User ID (for ownership validation)
     */
    void deleteContact(Long contactId, Long userId);

    /**
     * Get contact by ID.
     *
     * @param contactId Contact ID
     * @param userId    User ID (for ownership validation)
     * @return ContactResponse
     */
    ContactResponse getContact(Long contactId, Long userId);

    /**
     * Get all contacts for a user.
     *
     * @param userId User ID
     * @return List of ContactResponse
     */
    List<ContactResponse> getUserContacts(Long userId);

    /**
     * Get contacts for a user filtered by status.
     *
     * @param userId User ID
     * @param status Contact status (optional)
     * @return List of ContactResponse
     */
    List<ContactResponse> getUserContactsByStatus(Long userId, ContactStatus status);

    /**
     * Get contacts for a user filtered by source.
     *
     * @param userId User ID
     * @param source Contact source (optional)
     * @return List of ContactResponse
     */
    List<ContactResponse> getUserContactsBySource(Long userId, ContactSource source);

    /**
     * Get new (unread) contacts for a user.
     *
     * @param userId User ID
     * @return List of ContactResponse
     */
    List<ContactResponse> getNewContacts(Long userId);

    /**
     * Get contacts for a user within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of ContactResponse
     */
    List<ContactResponse> getUserContactsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Mark contact as read.
     *
     * @param contactId Contact ID
     * @param userId    User ID (for ownership validation)
     * @return Updated ContactResponse
     */
    ContactResponse markAsRead(Long contactId, Long userId);

    /**
     * Mark contact as replied.
     *
     * @param contactId Contact ID
     * @param userId    User ID (for ownership validation)
     * @return Updated ContactResponse
     */
    ContactResponse markAsReplied(Long contactId, Long userId);

    /**
     * Archive contact.
     *
     * @param contactId Contact ID
     * @param userId    User ID (for ownership validation)
     * @return Updated ContactResponse
     */
    ContactResponse archiveContact(Long contactId, Long userId);
}
