package com.dukkan.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for payment providers
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment.providers")
public class PaymentProviderProperties {

    private IyzicoProperties iyzico = new IyzicoProperties();
    private StripeProperties stripe = new StripeProperties();
    private PaypalProperties paypal = new PaypalProperties();

    @Data
    public static class IyzicoProperties {
        private boolean enabled = true;
        private String apiKey;
        private String secretKey;
        private String baseUrl = "https://sandbox-api.iyzipay.com";
    }

    @Data
    public static class StripeProperties {
        private boolean enabled = false;
        private String apiKey;
        private String secretKey;
    }

    @Data
    public static class PaypalProperties {
        private boolean enabled = false;
        private String clientId;
        private String clientSecret;
    }
}
