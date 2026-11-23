package com.ecommerce.order.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void prePersist_shouldSetCreatedAtOnly() {
        //given
        Order order = Order.builder().build();

        //when
        order.prePersist();

        //then
        assertNotNull(order.getCreatedAt());
        assertNull(order.getUpdatedAt());
    }

    @Test
    void preUpdate_shouldSetUpdatedAtAndKeepCreatedAt() {
        //given
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        Order order = Order.builder().createdAt(createdAt).build();

        //when
        order.preUpdate();

        //then
        assertEquals(createdAt, order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertTrue(order.getUpdatedAt().isAfter(createdAt));
    }
}
