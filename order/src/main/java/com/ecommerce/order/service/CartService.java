package com.ecommerce.order.service;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.CartItemRequest;

import java.util.List;
import java.util.Optional;

public interface CartService {
    void addToCart(Long userId, CartItemRequest cartItemRequest);

    void removeFromCart(Long userId, Long productId);

    Optional<List<CartItem>> getUserCartItems(Long userId);

    void clearCart(Long userId);
}
