package com.dukkan.payment.exception;

import com.dukkan.payment.model.PaymentProvider;

public class PaymentProviderNotSupportedException extends RuntimeException {
    public PaymentProviderNotSupportedException(PaymentProvider provider) {
        super("Payment provider not supported: " + provider);
    }
}
