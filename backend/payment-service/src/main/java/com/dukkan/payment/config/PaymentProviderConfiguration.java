package com.dukkan.payment.config;

import com.dukkan.payment.model.PaymentProvider;
import com.dukkan.payment.provider.PaymentProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for payment provider beans
 * <p>
 * This configuration creates a map of PaymentProvider enum to PaymentProviderService implementations
 * for dependency injection into PaymentServiceImpl
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentProviderConfiguration {

    /**
     * Creates a map of payment providers for the Strategy pattern
     * <p>
     * This allows PaymentServiceImpl to look up the correct provider implementation
     * based on the PaymentProvider enum value from the payment request
     *
     * @param providers List of all available PaymentProviderService beans (auto-wired by Spring)
     * @return Map of PaymentProvider to PaymentProviderService
     */
    @Bean
    public Map<PaymentProvider, PaymentProviderService> paymentProviders(
            List<PaymentProviderService> providers) {

        Map<PaymentProvider, PaymentProviderService> providerMap = new HashMap<>();

        for (PaymentProviderService provider : providers) {
            providerMap.put(provider.getProviderName(), provider);
            log.info("Registered payment provider: {}", provider.getProviderName());
        }

        log.info("Total payment providers registered: {}", providerMap.size());
        return providerMap;
    }
}
