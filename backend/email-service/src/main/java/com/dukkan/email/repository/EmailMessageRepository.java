package com.dukkan.email.repository;

import com.dukkan.email.model.EmailMessage;
import com.dukkan.email.model.EmailStatus;
import com.dukkan.email.model.EmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailMessage entity.
 * Provides data access methods for email tracking and history.
 */
@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {

    /**
     * Find all emails by recipient email address with pagination
     */
    Page<EmailMessage> findByRecipientEmailOrderByCreatedAtDesc(
        String recipientEmail,
        Pageable pageable
    );

    /**
     * Find all emails by status
     */
    List<EmailMessage> findByStatus(EmailStatus status);

    /**
     * Find all emails by type and status
     */
    List<EmailMessage> findByEmailTypeAndStatus(EmailType emailType, EmailStatus status);

    /**
     * Find emails by reference (e.g., all emails for a specific order)
     */
    List<EmailMessage> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType);

    /**
     * Find failed emails that can be retried
     */
    @Query("SELECT e FROM EmailMessage e WHERE e.status = :status AND e.sendAttempts < :maxAttempts")
    List<EmailMessage> findRetryableEmails(
        @Param("status") EmailStatus status,
        @Param("maxAttempts") Integer maxAttempts
    );

    /**
     * Find emails sent within a date range
     */
    @Query("SELECT e FROM EmailMessage e WHERE e.sentAt BETWEEN :startDate AND :endDate")
    List<EmailMessage> findEmailsSentBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count emails by status
     */
    long countByStatus(EmailStatus status);

    /**
     * Count emails by type
     */
    long countByEmailType(EmailType emailType);

    /**
     * Check if an email already exists for a specific reference
     * (to prevent duplicate emails)
     */
    boolean existsByReferenceIdAndReferenceTypeAndEmailType(
        UUID referenceId,
        String referenceType,
        EmailType emailType
    );
}
