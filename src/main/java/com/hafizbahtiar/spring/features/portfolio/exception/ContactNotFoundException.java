package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when a contact is not found.
 */
public class ContactNotFoundException extends RuntimeException {

    public ContactNotFoundException(String message) {
        super(message);
    }

    public static ContactNotFoundException byId(Long contactId) {
        return new ContactNotFoundException("Contact not found with ID: " + contactId);
    }

    public static ContactNotFoundException byIdAndUser(Long contactId, Long userId) {
        return new ContactNotFoundException("Contact not found with ID: " + contactId + " for user ID: " + userId);
    }
}
