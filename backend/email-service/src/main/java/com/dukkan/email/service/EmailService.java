package com.dukkan.email.service;

import com.dukkan.email.dto.EmailMessageDTO;
import com.dukkan.email.dto.SendEmailRequest;
import com.dukkan.email.model.EmailStatus;
import com.dukkan.email.model.EmailType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for email operations.
 */
public interface EmailService {

    /**
     * Send an email based on the request
     * @param request Email details and template variables
     * @return EmailMessageDTO with tracking information
     */
    EmailMessageDTO sendEmail(SendEmailRequest request);

    /**
     * Get email message by ID
     */
    EmailMessageDTO getEmailById(UUID id);

    /**
     * Get all emails for a recipient with pagination
     */
    Page<EmailMessageDTO> getEmailsByRecipient(String recipientEmail, int page, int size);

    /**
     * Get all emails by status
     */
    List<EmailMessageDTO> getEmailsByStatus(EmailStatus status);

    /**
     * Get all emails for a specific reference (e.g., all emails for an order)
     */
    List<EmailMessageDTO> getEmailsByReference(UUID referenceId, String referenceType);

    /**
     * Retry failed emails
     */
    void retryFailedEmails();

    /**
     * Get email statistics
     */
    EmailStatistics getEmailStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Statistics record for email metrics
     */
    record EmailStatistics(
        long totalSent,
        long totalFailed,
        long totalDelivered,
        long pendingCount
    ) {}
}
