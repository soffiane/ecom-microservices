package com.ecommerce.order.mapper;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.CartItemResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartItemResponseMapperTest {

    @Test
    void toCartItemResponse_shouldMapAllFields() {
        //given
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .userId("10")
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("2000.00"))
                .build();

        //when
        CartItemResponse result = CartItemResponseMapper.toCartItemResponse(cartItem);

        //then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("10", result.userId());
        assertEquals(100L, result.productId());
        assertEquals(2, result.quantity());
        assertEquals(new BigDecimal("2000.00"), result.price());
    }

    @Test
    void toCartItemResponse_shouldHandleZeroQuantity() {
        //given
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .userId("10")
                .productId(100L)
                .quantity(0)
                .price(BigDecimal.ZERO)
                .build();

        //when
        CartItemResponse result = CartItemResponseMapper.toCartItemResponse(cartItem);

        //then
        assertNotNull(result);
        assertEquals(0, result.quantity());
        assertEquals(BigDecimal.ZERO, result.price());
    }

    @Test
    void toCartItemResponse_shouldHandleLargeQuantity() {
        //given
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .userId("10")
                .productId(100L)
                .quantity(1000)
                .price(new BigDecimal("1000000.00"))
                .build();

        //when
        CartItemResponse result = CartItemResponseMapper.toCartItemResponse(cartItem);

        //then
        assertNotNull(result);
        assertEquals(1000, result.quantity());
        assertEquals(new BigDecimal("1000000.00"), result.price());
    }
}
