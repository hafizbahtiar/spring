package com.hafizbahtiar.spring.common.service;

/**
 * Service interface for sending emails.
 */
public interface EmailService {

    /**
     * Send a simple text email
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param body    Email body (plain text)
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Send an HTML email
     *
     * @param to       Recipient email address
     * @param subject  Email subject
     * @param htmlBody Email body (HTML)
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Send password reset email with reset link
     *
     * @param to    Recipient email address
     * @param token Password reset token
     * @param name  User's name (optional, for personalization)
     */
    void sendPasswordResetEmail(String to, String token, String name);

    /**
     * Send email verification email with verification link
     *
     * @param to    Recipient email address
     * @param token Email verification token
     * @param name  User's name (optional, for personalization)
     */
    void sendEmailVerificationEmail(String to, String token, String name);
}
