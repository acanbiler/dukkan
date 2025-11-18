package com.dukkan.email.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an email message in the system.
 * Stores email history for tracking and auditing purposes.
 */
@Entity
@Table(name = "email_messages", indexes = {
    @Index(name = "idx_email_recipient", columnList = "recipient_email"),
    @Index(name = "idx_email_status", columnList = "status"),
    @Index(name = "idx_email_type", columnList = "email_type"),
    @Index(name = "idx_email_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Email address of the recipient
     */
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    /**
     * Name of the recipient (for personalization)
     */
    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    /**
     * Email subject line
     */
    @Column(nullable = false, length = 500)
    private String subject;

    /**
     * Email body content (can be HTML)
     */
    @Column(columnDefinition = "TEXT")
    private String body;

    /**
     * Type of email being sent
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;

    /**
     * Current status of the email
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    /**
     * Language code for the email (e.g., "en", "tr")
     */
    @Column(length = 10)
    @Builder.Default
    private String language = "en";

    /**
     * Reference ID to related entity (e.g., order ID, payment ID)
     */
    @Column(name = "reference_id")
    private UUID referenceId;

    /**
     * Reference type (e.g., "order", "payment", "user")
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * Number of send attempts
     */
    @Column(name = "send_attempts")
    @Builder.Default
    private Integer sendAttempts = 0;

    /**
     * Error message if email failed to send
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * External provider message ID (e.g., SendGrid message ID)
     */
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    /**
     * When the email was sent
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * When the email was delivered (if webhook available)
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Business logic: Mark email as sent
     */
    public void markAsSent(String providerMessageId) {
        this.status = EmailStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = LocalDateTime.now();
        this.sendAttempts++;
    }

    /**
     * Business logic: Mark email as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = EmailStatus.FAILED;
        this.errorMessage = errorMessage;
        this.sendAttempts++;
    }

    /**
     * Business logic: Mark email as delivered
     */
    public void markAsDelivered() {
        this.status = EmailStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Business logic: Check if email should be retried
     */
    public boolean canRetry() {
        return this.sendAttempts < 3 &&
               (this.status == EmailStatus.FAILED || this.status == EmailStatus.PENDING);
    }
}
