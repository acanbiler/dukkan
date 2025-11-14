package com.dukkan.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller for handling service unavailability.
 * Provides fallback responses when downstream services are down.
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/product-service")
    public ResponseEntity<Map<String, Object>> productServiceFallback() {
        log.warn("Product Service is currently unavailable - fallback triggered");

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Product Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "product-service");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/general")
    public ResponseEntity<Map<String, Object>> generalFallback() {
        log.warn("Service unavailable - general fallback triggered");

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "The requested service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
