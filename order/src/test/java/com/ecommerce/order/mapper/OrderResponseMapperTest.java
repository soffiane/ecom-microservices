package com.ecommerce.order.mapper;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderResponseMapperTest {

    private OrderResponseMapper orderResponseMapper;

    @BeforeEach
    void setUp() {
        orderResponseMapper = new OrderResponseMapper();
    }

    @Test
    void mapToOrderResponse_shouldMapAllFields() {
        //given
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();

        Order order = Order.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.CONFIRMED)
                .items(List.of(orderItem))
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();
        orderItem.setOrder(order);

        //when
        OrderResponse result = orderResponseMapper.mapToOrderResponse(order);

        //then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
        assertNotNull(result.getOrderItems());
        assertEquals(1, result.getOrderItems().size());
        
        OrderItemDTO itemDTO = result.getOrderItems().get(0);
        assertEquals(1L, itemDTO.getId());
        assertEquals(100L, itemDTO.getProductId());
        assertEquals(2, itemDTO.getQuantity());
        assertEquals(new BigDecimal("50.00"), itemDTO.getPrice());
        assertEquals(new BigDecimal("100.00"), itemDTO.getSubTotal());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void mapToOrderResponse_shouldHandleEmptyItems() {
        //given
        Order order = Order.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.PENDING)
                .items(List.of())
                .totalAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        //when
        OrderResponse result = orderResponseMapper.mapToOrderResponse(order);

        //then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getOrderItems().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
    }

    @Test
    void mapToOrderResponse_shouldHandleMultipleItems() {
        //given
        OrderItem item1 = OrderItem.builder()
                .id(1L)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();

        OrderItem item2 = OrderItem.builder()
                .id(2L)
                .productId(200L)
                .quantity(1)
                .price(new BigDecimal("30.00"))
                .build();

        Order order = Order.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.CONFIRMED)
                .items(List.of(item1, item2))
                .totalAmount(new BigDecimal("130.00"))
                .createdAt(LocalDateTime.now())
                .build();
        item1.setOrder(order);
        item2.setOrder(order);

        //when
        OrderResponse result = orderResponseMapper.mapToOrderResponse(order);

        //then
        assertNotNull(result);
        assertEquals(2, result.getOrderItems().size());
        assertEquals(new BigDecimal("130.00"), result.getTotalAmount());
    }
}
