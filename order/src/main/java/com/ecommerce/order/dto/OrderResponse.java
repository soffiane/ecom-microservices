package com.ecommerce.order.dto;

import com.ecommerce.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime createdAt;
}
