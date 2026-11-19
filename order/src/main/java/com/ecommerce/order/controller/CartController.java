package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.mapper.CartItemResponseMapper;
import com.ecommerce.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart of the user identified by the X-User-ID header")
public class CartController {

    private static final String USER_ID_HEADER_DESCRIPTION =
            "Id of the calling user. Not authenticated: the value is trusted as sent.";

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @Operation(summary = "Add a product to the cart",
            description = "Adds the quantity to the existing line when the product is already in the cart. "
                    + "The product id is not checked against product-service and the unit price is a placeholder.")
    @ApiResponse(responseCode = "201", description = "Product added; the response has no body", content = @Content)
    @ApiResponse(responseCode = "400", description = "Validation failed or X-User-ID missing/not a number", content = @Content)
    @ApiResponse(responseCode = "409", description = "Concurrent modification of the same cart line; safe to retry",
            content = @Content(mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "Concurrent modification, please retry")))
    public ResponseEntity<CartItemResponse> addToCart(
            @Parameter(in = ParameterIn.HEADER, description = USER_ID_HEADER_DESCRIPTION, required = true)
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody @Valid CartItemRequest cartItemRequest
    ) {
        cartService.addToCart(userId, cartItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove a product from the cart", description = "Removes the whole line, whatever its quantity.")
    @ApiResponse(responseCode = "204", description = "Line removed")
    @ApiResponse(responseCode = "400", description = "The product is not in the user's cart",
            content = @Content(mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "Cart item not found")))
    public ResponseEntity<Void> removeFromCart(@Parameter(in = ParameterIn.HEADER, description = USER_ID_HEADER_DESCRIPTION, required = true)
                                               @RequestHeader("X-User-ID") Long userId,
                                               @Parameter(description = "Product id") @PathVariable Long productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/items")
    @Operation(summary = "Get the cart content")
    @ApiResponse(responseCode = "200", description = "Cart lines; `price` is a unit price")
    @ApiResponse(responseCode = "204", description = "The cart is empty", content = @Content)
    public ResponseEntity<List<CartItemResponse>> getCartForUser(@Parameter(in = ParameterIn.HEADER, description = USER_ID_HEADER_DESCRIPTION, required = true)
                                                                 @RequestHeader("X-User-ID") Long userId) {
        return cartService.getUserCartItems(userId)
                .map(cartItems -> ResponseEntity.ok(cartItems.stream().map(CartItemResponseMapper::toCartItemResponse).toList()))
                .orElse(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }
}
