package com.ecommerce.order.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

    @Test
    void builder_shouldApplyFieldDefaults() {
        //when
        Order order = Order.builder().build();

        //then
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());
    }
}
