package com.dukkan.payment.model;

/**
 * Supported payment providers
 */
public enum PaymentProvider {
    IYZICO,    // Iyzico (Turkish payment gateway) - MVP
    STRIPE,    // Stripe (International)
    PAYPAL     // PayPal (Future)
}
