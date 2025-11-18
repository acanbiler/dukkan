package com.dukkan.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for refunding a payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPaymentRequest {

    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal digits")
    private BigDecimal amount;

    @NotBlank(message = "Refund reason is required")
    private String reason;

    private String ipAddress;
}
