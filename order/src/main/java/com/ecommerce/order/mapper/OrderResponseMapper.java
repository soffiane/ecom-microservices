package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.domain.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderResponseMapper {

    public OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderItems(order.getItems().stream()
                        .map(item ->
                                new OrderItemDTO(
                                        item.getId(),
                                        item.getProductId(),
                                        item.getQuantity(),
                                        item.getPrice(),
                                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                        .toList())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
