package com.ecommerce.product.service;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductRequestMapper;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductRequestMapper productRequestMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .quantity(10)
                .image("laptop.jpg")
                .active(true)
                .build();

        productRequest = ProductRequest.builder()
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .stock(10)
                .image("laptop.jpg")
                .build();
    }

    @Test
    void findAllProducts_shouldReturnAllProducts() {
        //given
        Product product2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("29.99"))
                .category("Electronics")
                .quantity(50)
                .image("mouse.jpg")
                .active(true)
                .build();

        when(productRepository.findByActiveTrue()).thenReturn(List.of(product, product2));

        //when
        List<Product> result = productService.findAllProducts();

        //then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("Mouse", result.get(1).getName());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void createProduct_shouldReturnProduct() {
        //given
        when(productRequestMapper.mapToProductRequest(productRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);

        //when
        Product result = productService.createProduct(productRequest);

        //then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(new BigDecimal("999.99"), result.getPrice());
        verify(productRequestMapper).mapToProductRequest(productRequest);
        verify(productRepository).save(product);
    }

    @Test
    void findProductById_shouldReturnProductWhenExists() {
        //given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        //when
        Optional<Product> result = productService.findProductById(1L);

        //then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Laptop", result.get().getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void findProductById_shouldReturnEmptyWhenNotExists() {
        //given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        //when
        Optional<Product> result = productService.findProductById(999L);

        //then
        assertFalse(result.isPresent());
        verify(productRepository).findById(999L);
    }

    @Test
    void updateProduct_shouldReturnUpdatedProductWhenExists() {
        //given
        ProductRequest updateRequest = ProductRequest.builder()
                .name("Laptop Pro")
                .description("Updated high performance laptop")
                .price(new BigDecimal("1099.99"))
                .category("Electronics")
                .stock(15)
                .image("laptop-pro.jpg")
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Laptop Pro")
                .description("Updated high performance laptop")
                .price(new BigDecimal("1099.99"))
                .category("Electronics")
                .quantity(15)
                .image("laptop-pro.jpg")
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        Product result = productService.updateProduct(1L, updateRequest);

        //then
        assertNotNull(result);
        assertEquals("Laptop Pro", result.getName());
        assertEquals(new BigDecimal("1099.99"), result.getPrice());
        assertEquals(15, result.getQuantity());
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldKeepExistingStockWhenStockNotProvided() {
        //given
        ProductRequest updateRequest = ProductRequest.builder()
                .name("Laptop Pro")
                .description("Updated high performance laptop")
                .price(new BigDecimal("1099.99"))
                .category("Electronics")
                .stock(null)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        Product result = productService.updateProduct(1L, updateRequest);

        //then
        assertEquals(10, result.getQuantity());
        assertEquals("Laptop Pro", result.getName());
    }

    @Test
    void updateProduct_shouldThrowExceptionWhenNotExists() {
        //given
        ProductRequest updateRequest = ProductRequest.builder()
                .name("Laptop Pro")
                .description("Updated high performance laptop")
                .price(new BigDecimal("1099.99"))
                .category("Electronics")
                .stock(15)
                .image("laptop-pro.jpg")
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(999L, updateRequest));
        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
        verify(productRequestMapper, never()).mapToProductRequest(any());
    }
}
