package com.dukkan.payment.provider;

import lombok.*;

/**
 * Credit/Debit card details for payment
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDetails {
    private String cardHolderName;
    private String cardNumber;
    private String expireMonth;
    private String expireYear;
    private String cvc;
}
