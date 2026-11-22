package com.ecommerce.order.service;

import com.ecommerce.order.clients.ProductClient;
import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.exception.CartItemNotFoundException;
import com.ecommerce.order.exception.OutOfStockException;
import com.ecommerce.order.exception.ProductNotFoundException;
import com.ecommerce.order.exception.UserNotFoundException;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.users.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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

    @Mock
    private ProductClient productClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartItemRequest cartItemRequest;
    private CartItem existingCartItem;
    private ProductResponse product;
    private UserResponse user;

    @BeforeEach
    void setUp() {
        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(100L);
        cartItemRequest.setQuantity(2);

        existingCartItem = CartItem.builder()
                .id(1L)
                .userId("10")
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("1000.00"))
                .build();

        product = ProductResponse.builder()
                .id(100L)
                .price(new BigDecimal("1000.00"))
                .stock(10)
                .active(true)
                .build();

        user = UserResponse.builder()
                .id("10")
                .build();
    }

    @Test
    void addToCart_shouldAddNewItemWithUnitPriceWhenNotExists() {
        //given
        String userId = "10";
        when(productClient.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(product));
        when(userClient.getUserById(userId)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdAndProductId(userId, 100L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);

        //when
        cartService.addToCart(userId, cartItemRequest);

        //then
        verify(cartItemRepository).findByUserIdAndProductId(userId, 100L);
        verify(cartItemRepository).save(captor.capture());
        CartItem saved = captor.getValue();
        assertEquals("10", saved.getUserId());
        assertEquals(100L, saved.getProductId());
        assertEquals(2, saved.getQuantity());
        assertEquals(new BigDecimal("1000.00"), saved.getPrice());
    }

    @Test
    void addToCart_shouldUpdateQuantityAndKeepUnitPriceWhenItemExists() {
        //given
        String userId = "10";
        when(productClient.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(product));
        when(userClient.getUserById(userId)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdAndProductId(userId, 100L)).thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        cartService.addToCart(userId, cartItemRequest);

        //then
        verify(cartItemRepository).findByUserIdAndProductId(userId, 100L);
        verify(cartItemRepository).save(existingCartItem);
        assertEquals(4, existingCartItem.getQuantity());
        assertEquals(new BigDecimal("1000.00"), existingCartItem.getPrice());
    }

    @Test
    void addToCart_shouldThrowWhenProductNotFound() {
        //given
        when(productClient.findByIdAndActiveTrue(100L)).thenReturn(Optional.empty());

        //when & then
        assertThrows(ProductNotFoundException.class, () -> cartService.addToCart("10", cartItemRequest));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_shouldThrowProductNotFoundWhenProductServiceReturns404() {
        //given
        when(productClient.findByIdAndActiveTrue(100L))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        //when & then
        assertThrows(ProductNotFoundException.class, () -> cartService.addToCart("10", cartItemRequest));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_shouldThrowProductNotFoundWhenProductIsInactive() {
        //given
        product.setActive(false);
        when(productClient.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(product));

        //when & then
        assertThrows(ProductNotFoundException.class, () -> cartService.addToCart("10", cartItemRequest));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_shouldThrowUserNotFoundWhenUserServiceReturns404() {
        //given
        when(productClient.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(product));
        when(userClient.getUserById("10"))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        //when & then
        assertThrows(UserNotFoundException.class, () -> cartService.addToCart("10", cartItemRequest));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_shouldPropagateOtherRemoteErrors() {
        //given
        when(productClient.findByIdAndActiveTrue(100L))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        //when & then
        assertThrows(HttpClientErrorException.BadRequest.class, () -> cartService.addToCart("10", cartItemRequest));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_shouldThrowWhenStockIsInsufficient() {
        //given
        product.setStock(1);
        when(productClient.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(product));

        //when & then
        assertThrows(OutOfStockException.class, () -> cartService.addToCart("10", cartItemRequest));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_shouldThrowWhenUserNotFound() {
        //given
        when(productClient.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(product));
        when(userClient.getUserById("10")).thenReturn(Optional.empty());

        //when & then
        assertThrows(UserNotFoundException.class, () -> cartService.addToCart("10", cartItemRequest));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void removeFromCart_shouldRemoveItemSuccessfully() {
        //given
        String userId = "10";
        Long productId = 100L;
        when(userClient.getUserById(userId)).thenReturn(Optional.of(user));
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
        String userId = "10";
        Long productId = 100L;
        when(userClient.getUserById(userId)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());

        //when & then
        assertThrows(CartItemNotFoundException.class, () -> cartService.removeFromCart(userId, productId));
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
        verify(cartItemRepository, never()).deleteByUserIdAndProductId(any(), any());
    }

    @Test
    void getUserCartItems_shouldReturnCartItemsWhenExists() {
        //given
        String userId = "10";
        List<CartItem> cartItems = List.of(existingCartItem);
        when(userClient.getUserById(userId)).thenReturn(Optional.of(user));
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
        String userId = "10";
        when(userClient.getUserById(userId)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(userId)).thenReturn(Optional.empty());

        //when
        Optional<List<CartItem>> result = cartService.getUserCartItems(userId);

        //then
        assertTrue(result.isEmpty());
        verify(cartItemRepository).findByUserId(userId);
    }

    @Test
    void clearCart_shouldDeleteAllItemsForUserWithoutCallingUserService() {
        //given - l'utilisateur a déjà été vérifié par placeOrder
        String userId = "10";
        doNothing().when(cartItemRepository).deleteByUserId(userId);

        //when
        cartService.clearCart(userId);

        //then
        verify(cartItemRepository).deleteByUserId(userId);
        verifyNoInteractions(userClient);
    }

    @Test
    void getUserCartItems_shouldReturnEmptyWhenCartListIsEmpty() {
        //given
        String userId = "10";
        when(userClient.getUserById(userId)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(userId)).thenReturn(Optional.of(List.of()));

        //when
        Optional<List<CartItem>> result = cartService.getUserCartItems(userId);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void removeFromCart_shouldThrowWhenUserNotFound() {
        //given
        when(userClient.getUserById("10"))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        //when & then
        assertThrows(UserNotFoundException.class, () -> cartService.removeFromCart("10", 100L));
        verify(cartItemRepository, never()).deleteByUserIdAndProductId(any(), any());
    }

    @Test
    void getUserCartItems_shouldThrowWhenUserNotFound() {
        //given
        when(userClient.getUserById("10")).thenReturn(Optional.empty());

        //when & then
        assertThrows(UserNotFoundException.class, () -> cartService.getUserCartItems("10"));
        verify(cartItemRepository, never()).findByUserId(any());
    }

    @Test
    void removeFromCart_shouldNotCallProductService() {
        //given - un produit désactivé ou supprimé doit pouvoir être retiré du panier
        String userId = "10";
        when(userClient.getUserById(userId)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdAndProductId(userId, 100L)).thenReturn(Optional.of(existingCartItem));

        //when
        cartService.removeFromCart(userId, 100L);

        //then
        verify(cartItemRepository).deleteByUserIdAndProductId(userId, 100L);
        verifyNoInteractions(productClient);
    }
}
