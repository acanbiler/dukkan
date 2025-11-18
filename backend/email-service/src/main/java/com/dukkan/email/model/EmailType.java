package com.dukkan.email.model;

/**
 * Types of emails that can be sent by the system.
 * Each type corresponds to a specific email template.
 */
public enum EmailType {
    /**
     * Sent when a user successfully registers
     */
    WELCOME,

    /**
     * Sent when an order is successfully placed
     */
    ORDER_CONFIRMATION,

    /**
     * Sent when a payment is completed successfully
     */
    PAYMENT_SUCCESS,

    /**
     * Sent when a payment fails
     */
    PAYMENT_FAILED,

    /**
     * Sent when an order is shipped
     */
    ORDER_SHIPPED,

    /**
     * Sent when an order is delivered
     */
    ORDER_DELIVERED,

    /**
     * Sent when an order is cancelled
     */
    ORDER_CANCELLED,

    /**
     * Sent for password reset requests
     */
    PASSWORD_RESET,

    /**
     * Sent for email verification
     */
    EMAIL_VERIFICATION
}
