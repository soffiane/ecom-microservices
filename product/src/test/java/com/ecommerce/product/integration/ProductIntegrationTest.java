package com.ecommerce.product.integration;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ProductIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void testContainerIsRunning() {
        assertTrue(postgres.isCreated());
        assertTrue(postgres.isRunning());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void shouldCreateAndRetrieveProduct() throws Exception {
        //given
        String productJson = """
                {
                    "name": "Laptop",
                    "description": "High performance laptop",
                    "price": 999.99,
                    "category": "Electronics",
                    "stock": 10,
                    "image": "laptop.jpg"
                }
                """;

        //when - create product
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("High performance laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stock").value(10));

        //then - verify product was saved
        Product savedProduct = productRepository.findAll().get(0);
        assertEquals("Laptop", savedProduct.getName());
        assertEquals(new BigDecimal("999.99"), savedProduct.getPrice());
    }

    @Test
    @WithMockUser
    void shouldGetAllProducts() throws Exception {
        //given
        Product product1 = Product.builder()
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .quantity(10)
                .image("laptop.jpg")
                .active(true)
                .build();

        Product product2 = Product.builder()
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("29.99"))
                .category("Electronics")
                .quantity(50)
                .image("mouse.jpg")
                .active(true)
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        //when & then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser
    void shouldGetProductById() throws Exception {
        //given
        Product product = Product.builder()
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .quantity(10)
                .image("laptop.jpg")
                .active(true)
                .build();
        Product savedProduct = productRepository.save(product);

        //when & then
        mockMvc.perform(get("/api/v1/products/" + savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundForNonExistentProduct() throws Exception {
        //when & then
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldUpdateProduct() throws Exception {
        //given
        Product product = Product.builder()
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .quantity(10)
                .image("laptop.jpg")
                .active(true)
                .build();
        Product savedProduct = productRepository.save(product);

        String updateJson = """
                {
                    "name": "Laptop Pro",
                    "description": "Updated high performance laptop",
                    "price": 1099.99,
                    "category": "Electronics",
                    "stock": 15,
                    "image": "laptop-pro.jpg"
                }
                """;

        //when & then
        mockMvc.perform(put("/api/v1/products/" + savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Laptop Pro"))
                .andExpect(jsonPath("$.price").value(1099.99))
                .andExpect(jsonPath("$.stock").value(15));

        //verify in database
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals("Laptop Pro", updatedProduct.getName());
        assertEquals(new BigDecimal("1099.99"), updatedProduct.getPrice());
        assertEquals(15, updatedProduct.getQuantity());
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenUpdatingNonExistentProduct() throws Exception {
        //given
        String updateJson = """
                {
                    "name": "Laptop Pro",
                    "description": "Updated high performance laptop",
                    "price": 1099.99,
                    "category": "Electronics",
                    "stock": 15,
                    "image": "laptop-pro.jpg"
                }
                """;

        //when & then
        mockMvc.perform(put("/api/v1/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void apiDocs_shouldBeServedWithoutAuthenticationWhenRequested() throws Exception {
        //given - no authenticated user

        //when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Product Service API"))
                .andExpect(jsonPath("$.paths['/api/v1/products/{id}']").exists());
    }
}
