package com.hafizbahtiar.spring.common.service;

import com.hafizbahtiar.spring.config.EmailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service implementation for sending emails.
 * Uses Spring Mail with Thymeleaf templates for HTML emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailConfig emailConfig;
    private final EmailLoggingService emailLoggingService;

    @Override
    @Async
    public void sendEmail(String to, String subject, String body) {
        long startTime = System.currentTimeMillis();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            helper.setFrom(emailConfig.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // false = plain text

            mailSender.send(message);
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("Email sent successfully to: {}", to);

            // Log successful email sending
            emailLoggingService.logEmailSent(to, subject, null, "PLAIN_TEXT", null, responseTime, null);
        } catch (MessagingException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to send email to: {}", to, e);

            // Log failed email sending
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("exceptionType", e.getClass().getName());
            metadata.put("responseTimeMs", responseTime);
            emailLoggingService.logEmailFailed(to, subject, null, "PLAIN_TEXT", null, e.getMessage(), metadata);

            throw new RuntimeException("Failed to send email", e);
        } catch (MailException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Mail exception when sending email to: {}", to, e);

            // Log failed email sending
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("exceptionType", e.getClass().getName());
            metadata.put("responseTimeMs", responseTime);
            emailLoggingService.logEmailFailed(to, subject, null, "PLAIN_TEXT", null, e.getMessage(), metadata);

            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        long startTime = System.currentTimeMillis();
        try {
            sendHtmlEmailInternal(to, subject, htmlBody);
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("HTML email sent successfully to: {}", to);

            // Log successful email sending
            emailLoggingService.logEmailSent(to, subject, null, "HTML", null, responseTime, null);
        } catch (MessagingException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to send HTML email to: {}", to, e);

            // Log failed email sending
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("exceptionType", e.getClass().getName());
            metadata.put("responseTimeMs", responseTime);
            emailLoggingService.logEmailFailed(to, subject, null, "HTML", null, e.getMessage(), metadata);

            throw new RuntimeException("Failed to send HTML email", e);
        } catch (MailException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Mail exception when sending HTML email to: {}", to, e);

            // Log failed email sending
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("exceptionType", e.getClass().getName());
            metadata.put("responseTimeMs", responseTime);
            emailLoggingService.logEmailFailed(to, subject, null, "HTML", null, e.getMessage(), metadata);

            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Internal method to send HTML email without logging.
     * Used by template methods to avoid duplicate logging.
     */
    private void sendHtmlEmailInternal(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setFrom(emailConfig.getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML

        mailSender.send(message);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String token, String name) {
        long startTime = System.currentTimeMillis();
        try {
            // Prepare template context
            Context context = new Context();
            context.setVariable("name", name != null ? name : "User");
            context.setVariable("resetLink", emailConfig.getFrontendUrl() + "/reset-password?token=" + token);
            context.setVariable("appName", emailConfig.getAppName());
            context.setVariable("expirationHours", 1); // 1 hour expiration

            // Process template
            String htmlBody = templateEngine.process("email/password-reset", context);

            // Send email (using internal method to avoid duplicate logging)
            sendHtmlEmailInternal(to, "Reset Your Password", htmlBody);
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("Password reset email sent to: {}", to);

            // Log successful email sending (userId will be logged by PasswordResetService
            // if available)
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("token", token);
            metadata.put("recipientName", name);
            emailLoggingService.logEmailSent(to, "Reset Your Password", "password-reset", "TEMPLATED", null,
                    responseTime, metadata);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to send password reset email to: {}", to, e);

            // Log failed email sending
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("exceptionType", e.getClass().getName());
            metadata.put("token", token);
            metadata.put("responseTimeMs", responseTime);
            emailLoggingService.logEmailFailed(to, "Reset Your Password", "password-reset", "TEMPLATED", null,
                    e.getMessage(), metadata);

            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    @Async
    public void sendEmailVerificationEmail(String to, String token, String name) {
        long startTime = System.currentTimeMillis();
        try {
            // Prepare template context
            Context context = new Context();
            context.setVariable("name", name != null ? name : "User");
            context.setVariable("verificationLink", emailConfig.getFrontendUrl() + "/verify-email?token=" + token);
            context.setVariable("appName", emailConfig.getAppName());

            // Process template
            String htmlBody = templateEngine.process("email/email-verification", context);

            // Send email (using internal method to avoid duplicate logging)
            sendHtmlEmailInternal(to, "Verify Your Email Address", htmlBody);
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("Email verification email sent to: {}", to);

            // Log successful email sending (userId will be logged by
            // EmailVerificationService if available)
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("token", token);
            metadata.put("recipientName", name);
            emailLoggingService.logEmailSent(to, "Verify Your Email Address", "email-verification", "TEMPLATED", null,
                    responseTime, metadata);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Failed to send email verification email to: {}", to, e);

            // Log failed email sending
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("exceptionType", e.getClass().getName());
            metadata.put("token", token);
            metadata.put("responseTimeMs", responseTime);
            emailLoggingService.logEmailFailed(to, "Verify Your Email Address", "email-verification", "TEMPLATED", null,
                    e.getMessage(), metadata);

            throw new RuntimeException("Failed to send email verification email", e);
        }
    }
}
