package com.dukkan.payment.dto;

import com.dukkan.payment.model.PaymentMethod;
import com.dukkan.payment.model.PaymentProvider;
import com.dukkan.payment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Payment entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private UUID id;

    private String paymentReference;

    private UUID orderId;

    private UUID userId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private PaymentProvider provider;

    private PaymentMethod method;

    private String providerTransactionId;

    private String customerEmail;

    private String failureReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    // Computed fields
    private boolean isFinal;
    private boolean isSuccessful;
    private boolean canRetry;
}
