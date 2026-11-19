package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CartItemResponse(Long id,
        Long userId,
        Long productId,
        Integer quantity,
        @Schema(description = "Unit price (placeholder value until prices come from product-service)")
        BigDecimal price) {
}
