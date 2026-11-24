package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
@Tag(name = "Orders", description = "Orders of the user identified by the X-User-ID header")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place an order from the cart",
            description = "Turns every cart line into an order item, computes the total as the sum of "
                    + "unit price x quantity, saves the order with status CONFIRMED and empties the cart. "
                    + "No stock reservation, payment or product/user check is performed. Not idempotent.")
    @ApiResponse(responseCode = "201", description = "Order created")
    @ApiResponse(responseCode = "400", description = "The cart is empty, or X-User-ID is missing",
            content = @Content(mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "Cart not found or empty")))
    @ApiResponse(responseCode = "404", description = "User not found in user-service",
            content = @Content(mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "User not found")))
    public ResponseEntity<OrderResponse> placeOrder(
            @Parameter(in = ParameterIn.HEADER, required = true,
                    description = "Id of the calling user. Not authenticated: the value is trusted as sent.")
            @RequestHeader("X-User-ID") String userId) {
        log.info("Placing order for user {}", userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.placeOrder(userId));
    }
}
