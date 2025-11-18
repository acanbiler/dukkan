package com.dukkan.payment.mapper;

import com.dukkan.payment.dto.InitiatePaymentRequest;
import com.dukkan.payment.dto.PaymentDTO;
import com.dukkan.payment.model.Payment;
import com.dukkan.payment.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between Payment entities and DTOs
 */
@Component
public class PaymentMapper {

    /**
     * Converts Payment entity to PaymentDTO
     */
    public PaymentDTO toDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentDTO.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .provider(payment.getProvider())
                .method(payment.getMethod())
                .providerTransactionId(payment.getProviderTransactionId())
                .customerEmail(payment.getCustomerEmail())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                // Computed fields from domain logic
                .isFinal(payment.isFinal())
                .isSuccessful(payment.isSuccessful())
                .canRetry(payment.canRetry())
                .build();
    }

    /**
     * Converts InitiatePaymentRequest DTO to Payment entity
     */
    public Payment toEntity(InitiatePaymentRequest request) {
        if (request == null) {
            return null;
        }

        return Payment.builder()
                .paymentReference(generatePaymentReference())
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .provider(request.getProvider())
                .method(request.getMethod())
                .customerEmail(request.getCustomerEmail())
                .ipAddress(request.getIpAddress())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generates a unique payment reference
     * Format: PAY-{timestamp}-{random}
     */
    private String generatePaymentReference() {
        long timestamp = System.currentTimeMillis();
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("PAY-%d-%s", timestamp, randomPart);
    }
}
