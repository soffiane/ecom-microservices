package com.ecommerce.order.service;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.CartItemRequest;

import java.util.List;
import java.util.Optional;

public interface CartService {
    void addToCart(String userId, CartItemRequest cartItemRequest);

    void removeFromCart(String userId, Long productId);

    Optional<List<CartItem>> getUserCartItems(String userId);

    void clearCart(String userId);
}
