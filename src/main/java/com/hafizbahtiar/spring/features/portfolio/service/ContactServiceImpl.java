package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.ContactRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ContactResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Contact;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactSource;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactStatus;
import com.hafizbahtiar.spring.features.portfolio.exception.ContactNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.mapper.ContactMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.ContactRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ContactService.
 * Handles contact CRUD operations, status management, and contact form
 * submissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final UserRepository userRepository;
    private final PortfolioLoggingService portfolioLoggingService;

    /**
     * Get current HttpServletRequest for logging context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not retrieve current request: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ContactResponse createContact(Long userId, ContactRequest request) {
        log.debug("Creating contact for user ID: {}, from: {}", userId, request.getEmail());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Map request to entity
        Contact contact = contactMapper.toEntity(request);
        contact.setUser(user);

        // Set source if not provided (default to FORM)
        if (contact.getSource() == null) {
            contact.setSource(ContactSource.FORM);
        }

        // Set status to NEW if not provided
        if (contact.getStatus() == null) {
            contact.setStatus(ContactStatus.NEW);
        }

        long startTime = System.currentTimeMillis();
        Contact savedContact = contactRepository.save(contact);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Contact created successfully with ID: {} for user ID: {}", savedContact.getId(), userId);

        // Log contact creation
        portfolioLoggingService.logContactCreated(
                savedContact.getId(),
                userId,
                savedContact.getEmail(),
                savedContact.getSubject(),
                getCurrentRequest(),
                responseTime);

        return contactMapper.toResponse(savedContact);
    }

    @Override
    public ContactResponse updateContact(Long contactId, Long userId, ContactRequest request) {
        log.debug("Updating contact ID: {} for user ID: {}", contactId, userId);

        // Validate contact exists and belongs to user
        Contact contact = contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(() -> ContactNotFoundException.byIdAndUser(contactId, userId));

        // Update entity from request
        contactMapper.updateEntityFromRequest(request, contact);

        long startTime = System.currentTimeMillis();
        Contact updatedContact = contactRepository.save(contact);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Contact updated successfully with ID: {}", updatedContact.getId());

        // Log contact update
        portfolioLoggingService.logContactUpdated(
                updatedContact.getId(),
                userId,
                updatedContact.getEmail(),
                updatedContact.getSubject(),
                getCurrentRequest(),
                responseTime);

        return contactMapper.toResponse(updatedContact);
    }

    @Override
    public void deleteContact(Long contactId, Long userId) {
        log.debug("Deleting contact ID: {} for user ID: {}", contactId, userId);

        // Validate contact exists and belongs to user
        Contact contact = contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(() -> ContactNotFoundException.byIdAndUser(contactId, userId));

        String email = contact.getEmail();
        String subject = contact.getSubject();
        contactRepository.delete(contact);
        log.info("Contact deleted successfully with ID: {}", contactId);

        // Log contact deletion
        portfolioLoggingService.logContactDeleted(contactId, userId, email, subject, getCurrentRequest());
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContact(Long contactId, Long userId) {
        log.debug("Fetching contact ID: {} for user ID: {}", contactId, userId);

        Contact contact = contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(() -> ContactNotFoundException.byIdAndUser(contactId, userId));

        return contactMapper.toResponse(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getUserContacts(Long userId) {
        log.debug("Fetching all contacts for user ID: {}", userId);
        List<Contact> contacts = contactRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return contactMapper.toResponseList(contacts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getUserContactsByStatus(Long userId, ContactStatus status) {
        log.debug("Fetching contacts for user ID: {} by status: {}", userId, status);
        List<Contact> contacts = status != null
                ? contactRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                : contactRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return contactMapper.toResponseList(contacts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getUserContactsBySource(Long userId, ContactSource source) {
        log.debug("Fetching contacts for user ID: {} by source: {}", userId, source);
        List<Contact> contacts = source != null
                ? contactRepository.findByUserIdAndSourceOrderByCreatedAtDesc(userId, source)
                : contactRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return contactMapper.toResponseList(contacts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getNewContacts(Long userId) {
        log.debug("Fetching new contacts for user ID: {}", userId);
        List<Contact> contacts = contactRepository.findNewContactsByUserIdOrderByCreatedAtDesc(userId);
        return contactMapper.toResponseList(contacts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getUserContactsByDateRange(Long userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.debug("Fetching contacts for user ID: {} between {} and {}", userId, startDate, endDate);
        List<Contact> contacts = contactRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        return contactMapper.toResponseList(contacts);
    }

    @Override
    public ContactResponse markAsRead(Long contactId, Long userId) {
        log.debug("Marking contact ID: {} as read for user ID: {}", contactId, userId);

        // Validate contact exists and belongs to user
        Contact contact = contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(() -> ContactNotFoundException.byIdAndUser(contactId, userId));

        contact.markAsRead();

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact marked as read successfully with ID: {}", updatedContact.getId());

        // Log status change
        portfolioLoggingService.logContactStatusChanged(
                updatedContact.getId(),
                userId,
                updatedContact.getEmail(),
                ContactStatus.NEW,
                ContactStatus.READ,
                getCurrentRequest());

        return contactMapper.toResponse(updatedContact);
    }

    @Override
    public ContactResponse markAsReplied(Long contactId, Long userId) {
        log.debug("Marking contact ID: {} as replied for user ID: {}", contactId, userId);

        // Validate contact exists and belongs to user
        Contact contact = contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(() -> ContactNotFoundException.byIdAndUser(contactId, userId));

        ContactStatus previousStatus = contact.getStatus();
        contact.markAsReplied();

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact marked as replied successfully with ID: {}", updatedContact.getId());

        // Log status change
        portfolioLoggingService.logContactStatusChanged(
                updatedContact.getId(),
                userId,
                updatedContact.getEmail(),
                previousStatus,
                ContactStatus.REPLIED,
                getCurrentRequest());

        return contactMapper.toResponse(updatedContact);
    }

    @Override
    public ContactResponse archiveContact(Long contactId, Long userId) {
        log.debug("Archiving contact ID: {} for user ID: {}", contactId, userId);

        // Validate contact exists and belongs to user
        Contact contact = contactRepository.findByUserIdAndId(userId, contactId)
                .orElseThrow(() -> ContactNotFoundException.byIdAndUser(contactId, userId));

        ContactStatus previousStatus = contact.getStatus();
        contact.archive();

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact archived successfully with ID: {}", updatedContact.getId());

        // Log status change
        portfolioLoggingService.logContactStatusChanged(
                updatedContact.getId(),
                userId,
                updatedContact.getEmail(),
                previousStatus,
                ContactStatus.ARCHIVED,
                getCurrentRequest());

        return contactMapper.toResponse(updatedContact);
    }
}
