package com.ecommerce.order.controller;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private CartItemRequest cartItemRequest;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(100L);
        cartItemRequest.setQuantity(2);

        cartItem = CartItem.builder()
                .id(1L)
                .userId(10L)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("2000.00"))
                .build();
    }

    @Test
    void addToCart_shouldReturnCreatedStatus() {
        //given
        Long userId = 10L;
        doNothing().when(cartService).addToCart(userId, cartItemRequest);

        //when
        ResponseEntity<CartItemResponse> response = cartController.addToCart(userId, cartItemRequest);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(cartService).addToCart(userId, cartItemRequest);
    }

    @Test
    void removeFromCart_shouldReturnNoContentStatus() {
        //given
        Long userId = 10L;
        Long productId = 100L;
        doNothing().when(cartService).removeFromCart(userId, productId);

        //when
        ResponseEntity<Void> response = cartController.removeFromCart(userId, productId);

        //then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cartService).removeFromCart(userId, productId);
    }

    @Test
    void getCartForUser_shouldReturnCartItemsWhenExists() {
        //given
        Long userId = 10L;
        List<CartItem> cartItems = List.of(cartItem);
        when(cartService.getUserCartItems(userId)).thenReturn(Optional.of(cartItems));

        //when
        ResponseEntity<List<CartItemResponse>> response = cartController.getCartForUser(userId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(cartService).getUserCartItems(userId);
    }

    @Test
    void getCartForUser_shouldReturnNoContentWhenEmpty() {
        //given
        Long userId = 10L;
        when(cartService.getUserCartItems(userId)).thenReturn(Optional.empty());

        //when
        ResponseEntity<List<CartItemResponse>> response = cartController.getCartForUser(userId);

        //then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cartService).getUserCartItems(userId);
    }
}
