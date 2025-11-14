package com.dukkan.order.client;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product DTO for inter-service communication
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private UUID id;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
}
