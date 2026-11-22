package com.ecommerce.order.controller;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderResponse = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("2000.00"))
                .build();
    }

    @Test
    void placeOrder_shouldReturnCreatedStatus() {
        //given
        String userId = "10";
        when(orderService.placeOrder(userId)).thenReturn(orderResponse);

        //when
        ResponseEntity<OrderResponse> response = orderController.placeOrder(userId);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(OrderStatus.CONFIRMED, response.getBody().getStatus());
    }

    @Test
    void placeOrder_shouldCallServiceWithCorrectUserId() {
        //given
        String userId = "10";
        when(orderService.placeOrder(userId)).thenReturn(orderResponse);

        //when
        orderController.placeOrder(userId);

        //then
        verify(orderService).placeOrder(userId);
    }
}
