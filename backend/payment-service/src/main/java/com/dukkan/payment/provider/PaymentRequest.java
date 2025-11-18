package com.dukkan.payment.provider;

import com.dukkan.payment.model.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;

/**
 * Payment request data transferred to payment providers
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String paymentReference;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    private CardDetails cardDetails;
    private BillingAddress billingAddress;
    private String callbackUrl; // URL to receive payment result
    private String ipAddress; // Customer IP address
}
