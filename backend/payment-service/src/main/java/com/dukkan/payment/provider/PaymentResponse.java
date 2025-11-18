package com.dukkan.payment.provider;

import lombok.*;

/**
 * Payment response from payment providers
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String transactionId; // Provider's transaction ID
    private String status;
    private String message;
    private String errorCode;
    private String errorMessage;
    private String providerRawResponse; // Full response from provider for logging
}
