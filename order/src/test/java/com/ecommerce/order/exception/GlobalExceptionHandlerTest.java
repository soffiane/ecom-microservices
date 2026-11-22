package com.ecommerce.order.exception;

import com.ecommerce.order.controller.CartController;
import com.ecommerce.order.controller.OrderController;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.service.CartService;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Goes through MockMvc so Spring's real exception-handler argument resolution is exercised:
 * a handler parameter type that does not match the thrown exception would fail here.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CartController(cartService), new OrderController(orderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnBadRequestWhenCartItemNotFoundExceptionThrown() throws Exception {
        //given
        doThrow(new CartItemNotFoundException("Cart item not found"))
                .when(cartService).removeFromCart("10", 100L);

        //when //then
        mockMvc.perform(delete("/api/v1/cart/items/100").header("X-User-ID", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cart item not found"));
    }

    @Test
    void shouldReturnBadRequestWhenEmptyCartExceptionThrown() throws Exception {
        //given
        when(orderService.placeOrder("10")).thenThrow(new EmptyCartException("Cart not found or empty"));

        //when //then
        mockMvc.perform(post("/api/v1/orders").header("X-User-ID", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cart not found or empty"));
    }

    @Test
    void shouldReturnConflictWhenDataIntegrityViolationThrown() throws Exception {
        //given - e.g. concurrent add of the same product hitting the unique constraint
        doThrow(new DataIntegrityViolationException("uk_cart_item_user_product"))
                .when(cartService).addToCart(eq("10"), any(CartItemRequest.class));

        //when //then
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":100,\"quantity\":1}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Concurrent modification, please retry"));
    }

    @Test
    void shouldReturnBadRequestWhenOutOfStockExceptionThrown() throws Exception {
        //given
        when(orderService.placeOrder("10")).thenThrow(new OutOfStockException("Article is out of stock"));

        //when //then
        mockMvc.perform(post("/api/v1/orders").header("X-User-ID", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Article is out of stock"));
    }

    @Test
    void shouldReturnNotFoundWhenProductNotFoundExceptionThrown() throws Exception {
        //given
        doThrow(new ProductNotFoundException("Product not found"))
                .when(cartService).addToCart(eq("10"), any(CartItemRequest.class));

        //when //then
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":100,\"quantity\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found"));
    }

    @Test
    void shouldReturnNotFoundWhenUserNotFoundExceptionThrown() throws Exception {
        //given
        doThrow(new UserNotFoundException("User not found"))
                .when(cartService).addToCart(eq("10"), any(CartItemRequest.class));

        //when //then
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":100,\"quantity\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }
}
