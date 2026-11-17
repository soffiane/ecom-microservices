package com.ecommerce.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void builder_shouldDefaultRoleToCustomer() {
        //when
        User user = User.builder().email("john.doe@example.com").build();

        //then
        assertEquals(UserRole.CUSTOMER, user.getRole());
    }
}
