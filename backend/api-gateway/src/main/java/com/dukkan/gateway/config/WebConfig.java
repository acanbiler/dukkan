package com.dukkan.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Web configuration for the API Gateway.
 * Spring Cloud Gateway uses WebFlux (reactive stack).
 */
@Configuration
@EnableWebFlux
public class WebConfig {
    // Additional web configuration can be added here if needed
}
