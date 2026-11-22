package com.ecommerce.order.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ProductClient / UserClient désérialisent les réponses de product-service et user-service dans ces DTO :
 * sans constructeur utilisable par Jackson, chaque appel distant échoue.
 */
class RemoteResponseDeserializationTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void productResponse_shouldDeserializeWhenProductServiceReturnsProduct() {
        //given - forme réelle renvoyée par GET /api/v1/products/{id}
        String json = """
                {"active":true,"category":"Electronics","createdAt":"2026-11-19T10:00:09.121355",
                 "description":"Latest iPhone","id":13,"image":null,"name":"iPhone 16 Pro",
                 "price":1099.99,"stock":3,"updatedAt":"2026-11-19T10:00:09.121355"}
                """;

        //when
        ProductResponse product = jsonMapper.readValue(json, ProductResponse.class);

        //then
        assertEquals(13L, product.getId());
        assertEquals(new BigDecimal("1099.99"), product.getPrice());
        assertEquals(3, product.getStock());
        assertTrue(product.isActive());
    }

    @Test
    void userResponse_shouldDeserializeWhenUserServiceReturnsUser() {
        //given
        String json = """
                {"id":"6740a1b2c3d4e5f601234567","firstName":"John","lastName":"Doe",
                 "email":"john@example.com","phone":"0600000000",
                 "address":{"street":null,"city":null,"state":null,"country":null,"zipcode":null}}
                """;

        //when
        UserResponse user = jsonMapper.readValue(json, UserResponse.class);

        //then
        assertEquals("6740a1b2c3d4e5f601234567", user.getId());
        assertEquals("John", user.getFirstName());
    }
}
