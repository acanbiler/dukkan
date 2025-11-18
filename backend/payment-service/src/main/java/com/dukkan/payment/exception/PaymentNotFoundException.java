package com.dukkan.payment.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(UUID id) {
        super("Payment not found with id: " + id);
    }

    public PaymentNotFoundException(String reference) {
        super("Payment not found with reference: " + reference);
    }
}
