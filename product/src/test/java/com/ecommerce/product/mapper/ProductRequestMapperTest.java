package com.ecommerce.product.mapper;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductRequestMapperTest {

    private ProductRequestMapper productRequestMapper;

    @BeforeEach
    void setUp() {
        productRequestMapper = new ProductRequestMapper();
    }

    @Test
    void mapToProductRequest_shouldMapAllFields() {
        //given
        ProductRequest productRequest = ProductRequest.builder()
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .stock(10)
                .image("laptop.jpg")
                .build();

        //when
        Product result = productRequestMapper.mapToProductRequest(productRequest);

        //then
        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        assertEquals("High performance laptop", result.getDescription());
        assertEquals(new BigDecimal("999.99"), result.getPrice());
        assertEquals("Electronics", result.getCategory());
        assertEquals(10, result.getQuantity());
        assertEquals("laptop.jpg", result.getImage());
    }

    @Test
    void mapToProductRequest_shouldHandleNullFields() {
        //given
        ProductRequest productRequest = ProductRequest.builder()
                .name(null)
                .description(null)
                .price(null)
                .category(null)
                .stock(0)
                .image(null)
                .build();

        //when
        Product result = productRequestMapper.mapToProductRequest(productRequest);

        //then
        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getPrice());
        assertNull(result.getCategory());
        assertEquals(0, result.getQuantity());
        assertNull(result.getImage());
    }

    @Test
    void mapToProductRequest_shouldDefaultStockToZeroWhenStockNotProvided() {
        //given
        ProductRequest productRequest = ProductRequest.builder()
                .name("Laptop")
                .price(new BigDecimal("999.99"))
                .stock(null)
                .build();

        //when
        Product result = productRequestMapper.mapToProductRequest(productRequest);

        //then
        assertEquals(0, result.getQuantity());
        assertTrue(result.isActive());
    }
}
