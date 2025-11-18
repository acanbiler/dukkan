package com.dukkan.payment.dto;

import com.dukkan.payment.model.PaymentMethod;
import com.dukkan.payment.model.PaymentProvider;
import com.dukkan.payment.provider.BillingAddress;
import com.dukkan.payment.provider.CardDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for initiating a payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal digits")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    private String currency;

    @NotNull(message = "Payment provider is required")
    private PaymentProvider provider;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @Valid
    private CardDetails cardDetails;

    @Valid
    @NotNull(message = "Billing address is required")
    private BillingAddress billingAddress;

    private String callbackUrl;

    private String ipAddress;

    /**
     * Validates that card details are provided for card payments
     */
    public boolean isCardDetailsRequired() {
        return method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD;
    }
}
