package com.ecommerce.product.controller;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductRequestMapper;
import com.ecommerce.product.mapper.ProductResponseMapper;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductRequestMapper productRequestMapper;

    @MockitoBean
    private ProductResponseMapper productResponseMapper;

    @Test
    void shouldGetAllProducts() throws Exception {
        //given
        Product product1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .quantity(10)
                .image("laptop.jpg")
                .active(true)
                .build();

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

        ProductResponse response1 = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .stock(10)
                .image("laptop.jpg")
                .active(true)
                .build();

        ProductResponse response2 = ProductResponse.builder()
                .id(2L)
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("29.99"))
                .category("Electronics")
                .stock(50)
                .image("mouse.jpg")
                .active(true)
                .build();

        when(productService.findAllProducts()).thenReturn(List.of(product1, product2));
        when(productResponseMapper.mapToProductResponse(product1)).thenReturn(response1);
        when(productResponseMapper.mapToProductResponse(product2)).thenReturn(response2);

        //when & then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(999.99))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Mouse"))
                .andExpect(jsonPath("$[1].price").value(29.99));
    }

    @Test
    void shouldCreateProduct() throws Exception {
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

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .stock(10)
                .image("laptop.jpg")
                .active(true)
                .build();

        when(productService.createProduct(any(ProductRequest.class))).thenReturn(product);
        when(productResponseMapper.mapToProductResponse(product)).thenReturn(response);

        //when & then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"description\":\"High performance laptop\",\"price\":999.99,\"category\":\"Electronics\",\"stock\":10,\"image\":\"laptop.jpg\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("High performance laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.image").value("laptop.jpg"))
                .andExpect(jsonPath("$.active").value(true));

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void shouldGetProductById() throws Exception {
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

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .stock(10)
                .image("laptop.jpg")
                .active(true)
                .build();

        when(productService.findProductById(1L)).thenReturn(Optional.of(product));
        when(productResponseMapper.mapToProductResponse(product)).thenReturn(response);

        //when & then
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("High performance laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.image").value("laptop.jpg"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenProductNotExists() throws Exception {
        //given
        when(productService.findProductById(999L)).thenReturn(Optional.empty());

        //when & then
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        //given
        ProductRequest productRequest = ProductRequest.builder()
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

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Laptop Pro")
                .description("Updated high performance laptop")
                .price(new BigDecimal("1099.99"))
                .category("Electronics")
                .stock(15)
                .image("laptop-pro.jpg")
                .active(true)
                .build();

        when(productService.updateProduct(1L, productRequest)).thenReturn(updatedProduct);
        when(productResponseMapper.mapToProductResponse(updatedProduct)).thenReturn(response);

        //when & then
        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop Pro\",\"description\":\"Updated high performance laptop\",\"price\":1099.99,\"category\":\"Electronics\",\"stock\":15,\"image\":\"laptop-pro.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro"))
                .andExpect(jsonPath("$.description").value("Updated high performance laptop"))
                .andExpect(jsonPath("$.price").value(1099.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stock").value(15))
                .andExpect(jsonPath("$.image").value("laptop-pro.jpg"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentProduct() throws Exception {
        //given
        ProductRequest productRequest = ProductRequest.builder()
                .name("Laptop Pro")
                .description("Updated high performance laptop")
                .price(new BigDecimal("1099.99"))
                .category("Electronics")
                .stock(15)
                .image("laptop-pro.jpg")
                .build();

        when(productService.updateProduct(999L, productRequest)).thenThrow(new ProductNotFoundException("Product with id 999 not found"));

        //when & then
        mockMvc.perform(put("/api/v1/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop Pro\",\"description\":\"Updated high performance laptop\",\"price\":1099.99,\"category\":\"Electronics\",\"stock\":15,\"image\":\"laptop-pro.jpg\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithMissingRequiredFields() throws Exception {
        //when & then - only "name" provided: description, price and category are missing
        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop Pro\"}"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithNegativePrice() throws Exception {
        //when & then
        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop Pro\",\"description\":\"desc\",\"price\":-1,\"category\":\"Electronics\",\"stock\":15}"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithNegativePrice() throws Exception {
        //when & then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"description\":\"desc\",\"price\":-10,\"category\":\"Electronics\",\"stock\":1}"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        //when & then
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
        verify(productService).deleteProduct(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentProduct() throws Exception {
        //given
        doThrow(new ProductNotFoundException("Product with id 999 not found"))
                .when(productService).deleteProduct(999L);

        //when & then
        mockMvc.perform(delete("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchProductsByName() throws Exception {
        //given
        Product product = Product.builder().id(1L).name("Laptop").active(true).build();
        ProductResponse productResponse = ProductResponse.builder().id(1L).name("Laptop").active(true).build();
        when(productService.searchProducts("Lap")).thenReturn(List.of(product));
        when(productResponseMapper.mapToProductResponse(product)).thenReturn(productResponse);

        //when & then
        mockMvc.perform(get("/api/v1/products/search").param("name", "Lap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void shouldReturnNotFoundWhenSearchMatchesNothing() throws Exception {
        //given
        when(productService.searchProducts("Unknown")).thenReturn(List.of());

        //when & then
        mockMvc.perform(get("/api/v1/products/search").param("name", "Unknown"))
                .andExpect(status().isNotFound());
    }
}
