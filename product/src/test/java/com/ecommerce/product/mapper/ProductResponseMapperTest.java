package com.ecommerce.product.mapper;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductResponseMapperTest {

    private ProductResponseMapper productResponseMapper;

    @BeforeEach
    void setUp() {
        productResponseMapper = new ProductResponseMapper();
    }

    @Test
    void mapToProductResponse_shouldMapBasicFields() {
        //given
        Product product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .quantity(10)
                .image("laptop.jpg")
                .active(true)
                .build();

        //when
        ProductResponse result = productResponseMapper.mapToProductResponse(product);

        //then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals("High performance laptop", result.getDescription());
        assertEquals(new BigDecimal("999.99"), result.getPrice());
        assertEquals("Electronics", result.getCategory());
        assertEquals(10, result.getStock());
        assertEquals("laptop.jpg", result.getImage());
        assertEquals(true, result.isActive());
    }

    @Test
    void mapToProductResponse_shouldHandleNullFields() {
        //given
        Product product = Product.builder()
                .id(null)
                .name(null)
                .description(null)
                .price(null)
                .category(null)
                .quantity(0)
                .image(null)
                .active(false)
                .build();

        //when
        ProductResponse result = productResponseMapper.mapToProductResponse(product);

        //then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getPrice());
        assertNull(result.getCategory());
        assertEquals(0, result.getStock());
        assertNull(result.getImage());
        assertEquals(false, result.isActive());
    }

    @Test
    void mapToProductResponse_shouldHandleInactiveProduct() {
        //given
        Product product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .quantity(10)
                .image("laptop.jpg")
                .active(false)
                .build();

        //when
        ProductResponse result = productResponseMapper.mapToProductResponse(product);

        //then
        assertNotNull(result);
        assertEquals(false, result.isActive());
    }
}
