package com.dukkan.email.dto;

import com.dukkan.email.model.EmailStatus;
import com.dukkan.email.model.EmailType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing an email message.
 * Used for responses to avoid exposing entity directly.
 */
public record EmailMessageDTO(
    UUID id,
    String recipientEmail,
    String recipientName,
    String subject,
    EmailType emailType,
    EmailStatus status,
    String language,
    UUID referenceId,
    String referenceType,
    Integer sendAttempts,
    String errorMessage,
    LocalDateTime sentAt,
    LocalDateTime deliveredAt,
    LocalDateTime createdAt
) {}
