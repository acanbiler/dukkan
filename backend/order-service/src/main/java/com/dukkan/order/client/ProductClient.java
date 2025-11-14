package com.dukkan.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Client for communicating with Product Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8081}")
    private String productServiceUrl;

    /**
     * Get product by ID
     */
    public ProductDTO getProduct(UUID productId) {
        try {
            String url = productServiceUrl + "/api/v1/products/" + productId;
            log.debug("Fetching product from: {}", url);

            ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            }

            throw new RuntimeException("Failed to fetch product: " + productId);
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    /**
     * Reduce product stock
     */
    public void reduceStock(UUID productId, Integer quantity) {
        try {
            String url = productServiceUrl + "/api/v1/products/" + productId + "/stock/reduce";
            log.debug("Reducing stock for product {} by {}", productId, quantity);

            Map<String, Integer> request = Map.of("quantity", quantity);

            restTemplate.postForObject(url, request, String.class);

            log.info("Stock reduced for product {} by {}", productId, quantity);
        } catch (Exception e) {
            log.error("Error reducing stock for product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to reduce stock", e);
        }
    }

    /**
     * Generic API response wrapper
     */
    @lombok.Data
    private static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;
    }
}
