package com.dukkan.payment.model;

/**
 * Payment status enumeration
 */
public enum PaymentStatus {
    PENDING,      // Payment initiated but not yet processed
    PROCESSING,   // Payment being processed by provider
    COMPLETED,    // Payment successfully completed
    FAILED,       // Payment failed
    CANCELLED,    // Payment cancelled by user or system
    REFUNDED,     // Payment refunded
    PARTIAL_REFUND // Payment partially refunded
}
