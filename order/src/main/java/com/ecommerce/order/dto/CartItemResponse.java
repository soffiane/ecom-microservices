package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CartItemResponse(Long id,
        String userId,
        Long productId,
        Integer quantity,
        @Schema(description = "Unit price, taken from product-service when the item is added")
        BigDecimal price) {
}
