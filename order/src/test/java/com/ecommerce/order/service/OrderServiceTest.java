package com.ecommerce.order.service;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.exception.EmptyCartException;
import com.ecommerce.order.mapper.OrderResponseMapper;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private OrderResponseMapper orderResponseMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CartItem cartItem;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        cartItem = CartItem.builder()
                .id(1L)
                .userId(10L)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("2000.00"))
                .build();

        order = Order.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("2000.00"))
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("2000.00"))
                .build();
    }

    @Test
    void placeOrder_shouldCreateOrderSuccessfully() {
        //given
        Long userId = 10L;
        when(cartService.getUserCartItems(userId)).thenReturn(Optional.of(List.of(cartItem)));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderResponseMapper.mapToOrderResponse(order)).thenReturn(orderResponse);

        //when
        OrderResponse result = orderService.placeOrder(userId);

        //then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(cartService).getUserCartItems(userId);
        verify(orderRepository).save(any(Order.class));
        verify(orderResponseMapper).mapToOrderResponse(order);
        verify(cartService).clearCart(userId);
    }

    @Test
    void placeOrder_shouldThrowEmptyCartExceptionWhenCartIsEmpty() {
        //given
        Long userId = 10L;
        when(cartService.getUserCartItems(userId)).thenReturn(Optional.empty());

        //when & then
        assertThrows(EmptyCartException.class, () -> orderService.placeOrder(userId));
        verify(cartService).getUserCartItems(userId);
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clearCart(userId);
    }

    @Test
    void placeOrder_shouldCalculateTotalAmountCorrectly() {
        //given
        Long userId = 10L;
        CartItem item1 = CartItem.builder()
                .id(1L)
                .userId(10L)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("1000.00"))
                .build();
        CartItem item2 = CartItem.builder()
                .id(2L)
                .userId(10L)
                .productId(200L)
                .quantity(3)
                .price(new BigDecimal("3000.00"))
                .build();

        when(cartService.getUserCartItems(userId)).thenReturn(Optional.of(List.of(item1, item2)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderResponseMapper.mapToOrderResponse(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            return OrderResponse.builder()
                    .id(savedOrder.getId())
                    .status(savedOrder.getStatus())
                    .totalAmount(savedOrder.getTotalAmount())
                    .build();
        });

        //when
        OrderResponse result = orderService.placeOrder(userId);

        //then
        assertNotNull(result);
        assertEquals(new BigDecimal("11000.00"), result.getTotalAmount());
        verify(cartService).clearCart(userId);
    }

    @Test
    void placeOrder_shouldSetOrderStatusToConfirmed() {
        //given
        Long userId = 10L;
        when(cartService.getUserCartItems(userId)).thenReturn(Optional.of(List.of(cartItem)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderResponseMapper.mapToOrderResponse(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            return OrderResponse.builder()
                    .id(savedOrder.getId())
                    .status(savedOrder.getStatus())
                    .totalAmount(savedOrder.getTotalAmount())
                    .build();
        });

        //when
        OrderResponse result = orderService.placeOrder(userId);

        //then
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
    }
}
