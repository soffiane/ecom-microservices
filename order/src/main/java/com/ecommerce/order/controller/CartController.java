package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.mapper.CartItemResponseMapper;
import com.ecommerce.order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody @Valid CartItemRequest cartItemRequest
    ) {
        cartService.addToCart(userId, cartItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(@RequestHeader("X-User-ID") Long userId,
                                               @PathVariable Long productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getCartForUser(@RequestHeader("X-User-ID") Long userId) {
        return cartService.getUserCartItems(userId)
                .map(cartItems -> ResponseEntity.ok(cartItems.stream().map(CartItemResponseMapper::toCartItemResponse).toList()))
                .orElse(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }
}
