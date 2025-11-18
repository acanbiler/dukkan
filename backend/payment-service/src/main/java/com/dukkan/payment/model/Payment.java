package com.dukkan.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment entity representing a payment transaction
 * Follows DDD with rich business behavior
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String paymentReference; // Unique reference for this payment

    @Column(nullable = false)
    private UUID orderId; // Reference to order

    @Column(nullable = false)
    private UUID userId; // User who made the payment

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency; // ISO currency code (e.g., TRY, USD)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(length = 500)
    private String providerTransactionId; // Transaction ID from payment provider

    @Column(length = 1000)
    private String providerResponse; // Response from payment provider

    @Column(length = 500)
    private String failureReason; // Reason if payment failed

    @Column(length = 500)
    private String customerEmail;

    @Column(length = 100)
    private String customerName;

    @Column(length = 45)
    private String ipAddress; // Customer IP address

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt; // When payment was completed

    private LocalDateTime failedAt; // When payment failed

    // Business logic methods (DDD - rich domain behavior)

    /**
     * Mark payment as processing
     */
    public void markAsProcessing() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                "Payment can only be marked as processing from PENDING status. Current status: " + this.status
            );
        }
        this.status = PaymentStatus.PROCESSING;
    }

    /**
     * Complete the payment successfully
     */
    public void complete(String providerTransactionId, String providerResponse) {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException(
                "Payment can only be completed from PROCESSING status. Current status: " + this.status
            );
        }
        this.status = PaymentStatus.COMPLETED;
        this.providerTransactionId = providerTransactionId;
        this.providerResponse = providerResponse;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Fail the payment
     */
    public void fail(String reason, String providerResponse) {
        if (this.status == PaymentStatus.COMPLETED || this.status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException(
                "Cannot fail a payment that is already " + this.status
            );
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.providerResponse = providerResponse;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Cancel the payment
     */
    public void cancel() {
        if (this.status == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed payment. Use refund instead.");
        }
        if (this.status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot cancel an already refunded payment.");
        }
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * Refund the payment
     */
    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                "Can only refund a completed payment. Current status: " + this.status
            );
        }
        this.status = PaymentStatus.REFUNDED;
    }

    /**
     * Partially refund the payment
     */
    public void partialRefund() {
        if (this.status != PaymentStatus.COMPLETED && this.status != PaymentStatus.PARTIAL_REFUND) {
            throw new IllegalStateException(
                "Can only partially refund a completed or partial-refund payment. Current status: " + this.status
            );
        }
        this.status = PaymentStatus.PARTIAL_REFUND;
    }

    /**
     * Check if payment is in a final state
     */
    public boolean isFinal() {
        return this.status == PaymentStatus.COMPLETED
            || this.status == PaymentStatus.FAILED
            || this.status == PaymentStatus.CANCELLED
            || this.status == PaymentStatus.REFUNDED;
    }

    /**
     * Check if payment is successful
     */
    public boolean isSuccessful() {
        return this.status == PaymentStatus.COMPLETED;
    }

    /**
     * Check if payment can be retried
     */
    public boolean canRetry() {
        return this.status == PaymentStatus.FAILED || this.status == PaymentStatus.CANCELLED;
    }
}
