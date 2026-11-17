package com.ecommerce.order.dto;

import java.math.BigDecimal;

public record CartItemResponse(Long id,
        Long userId,
        Long productId,
        Integer quantity,
        BigDecimal price) {
}
