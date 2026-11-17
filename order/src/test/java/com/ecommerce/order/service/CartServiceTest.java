package com.ecommerce.order.service;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.exception.CartItemNotFoundException;
import com.ecommerce.order.repository.CartItemRepository;
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
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartItemRequest cartItemRequest;
    private CartItem existingCartItem;

    @BeforeEach
    void setUp() {
        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(100L);
        cartItemRequest.setQuantity(2);

        existingCartItem = CartItem.builder()
                .id(1L)
                .userId(10L)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("2000.00"))
                .build();
    }

    @Test
    void addToCart_shouldAddNewItemWhenNotExists() {
        //given
        Long userId = 10L;
        when(cartItemRepository.findByUserIdAndProductId(userId, 100L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        cartService.addToCart(userId, cartItemRequest);

        //then
        verify(cartItemRepository).findByUserIdAndProductId(userId, 100L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_shouldUpdateQuantityWhenItemExists() {
        //given
        Long userId = 10L;
        when(cartItemRepository.findByUserIdAndProductId(userId, 100L)).thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        cartService.addToCart(userId, cartItemRequest);

        //then
        verify(cartItemRepository).findByUserIdAndProductId(userId, 100L);
        verify(cartItemRepository).save(existingCartItem);
        assertEquals(4, existingCartItem.getQuantity());
    }

    @Test
    void removeFromCart_shouldRemoveItemSuccessfully() {
        //given
        Long userId = 10L;
        Long productId = 100L;
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(existingCartItem));
        doNothing().when(cartItemRepository).deleteByUserIdAndProductId(userId, productId);

        //when
        cartService.removeFromCart(userId, productId);

        //then
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
        verify(cartItemRepository).deleteByUserIdAndProductId(userId, productId);
    }

    @Test
    void removeFromCart_shouldThrowExceptionWhenItemNotFound() {
        //given
        Long userId = 10L;
        Long productId = 100L;
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());

        //when & then
        assertThrows(CartItemNotFoundException.class, () -> cartService.removeFromCart(userId, productId));
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
        verify(cartItemRepository, never()).deleteByUserIdAndProductId(any(), any());
    }

    @Test
    void getUserCartItems_shouldReturnCartItemsWhenExists() {
        //given
        Long userId = 10L;
        List<CartItem> cartItems = List.of(existingCartItem);
        when(cartItemRepository.findByUserId(userId)).thenReturn(Optional.of(cartItems));

        //when
        Optional<List<CartItem>> result = cartService.getUserCartItems(userId);

        //then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        verify(cartItemRepository).findByUserId(userId);
    }

    @Test
    void getUserCartItems_shouldReturnEmptyWhenNoItems() {
        //given
        Long userId = 10L;
        when(cartItemRepository.findByUserId(userId)).thenReturn(Optional.empty());

        //when
        Optional<List<CartItem>> result = cartService.getUserCartItems(userId);

        //then
        assertTrue(result.isEmpty());
        verify(cartItemRepository).findByUserId(userId);
    }

    @Test
    void clearCart_shouldDeleteAllItemsForUser() {
        //given
        Long userId = 10L;
        doNothing().when(cartItemRepository).deleteByUserId(userId);

        //when
        cartService.clearCart(userId);

        //then
        verify(cartItemRepository).deleteByUserId(userId);
    }
}
