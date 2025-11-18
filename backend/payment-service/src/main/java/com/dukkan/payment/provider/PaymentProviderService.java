package com.dukkan.payment.provider;

/**
 * Payment provider interface - Strategy pattern
 * Allows plugging in different payment providers (Iyzico, Stripe, PayPal, etc.)
 */
public interface PaymentProviderService {

    /**
     * Process a payment through this provider
     *
     * @param request Payment request details
     * @return Payment response from provider
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * Refund a payment
     *
     * @param transactionId Provider's transaction ID
     * @param amount Amount to refund
     * @return Refund response
     */
    PaymentResponse refundPayment(String transactionId, java.math.BigDecimal amount);

    /**
     * Get the provider name (IYZICO, STRIPE, etc.)
     */
    com.dukkan.payment.model.PaymentProvider getProviderName();

    /**
     * Verify if this provider supports the given payment method
     */
    boolean supportsPaymentMethod(com.dukkan.payment.model.PaymentMethod method);
}
