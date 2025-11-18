package com.dukkan.payment.provider;

import lombok.*;

/**
 * Billing address for payment
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingAddress {
    private String contactName;
    private String city;
    private String country;
    private String address;
    private String zipCode;
}
