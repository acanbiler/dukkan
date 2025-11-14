package com.dukkan.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routing configuration.
 * Routes are also configured in application.yml, this class provides programmatic configuration.
 */
@Configuration
public class GatewayConfig {

    /**
     * Configure routes programmatically (alternative to YAML configuration).
     * Currently using YAML configuration in application.yml.
     * This bean is here as a reference for programmatic route configuration.
     */
    // @Bean
    // public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    //     return builder.routes()
    //             .route("product-service", r -> r
    //                     .path("/api/v1/products/**", "/api/v1/categories/**")
    //                     .uri("http://localhost:8081"))
    //             .build();
    // }
}
