package com.dukkan.email.model;

/**
 * Status of an email message in the system.
 */
public enum EmailStatus {
    /**
     * Email is queued for sending
     */
    PENDING,

    /**
     * Email is being sent
     */
    SENDING,

    /**
     * Email was sent successfully
     */
    SENT,

    /**
     * Email failed to send
     */
    FAILED,

    /**
     * Email was delivered to recipient
     */
    DELIVERED,

    /**
     * Email bounced
     */
    BOUNCED
}
