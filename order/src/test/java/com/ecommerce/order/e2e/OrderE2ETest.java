package com.ecommerce.order.e2e;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class OrderE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @Transactional
    void completeOrderFlow_shouldWorkEndToEnd() throws Exception {
        //given
        Long userId = 10L;
        
        // Step 1: Add first item to cart
        String firstItemJson = """
                {
                    "productId": 100,
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstItemJson))
                .andExpect(status().isCreated());

        // Verify item added
        assertEquals(1, cartItemRepository.findByUserId(userId).orElseThrow().size());

        // Step 2: Add second item to cart
        String secondItemJson = """
                {
                    "productId": 200,
                    "quantity": 1
                }
                """;

        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondItemJson))
                .andExpect(status().isCreated());

        // Verify two items in cart
        assertEquals(2, cartItemRepository.findByUserId(userId).orElseThrow().size());

        // Step 3: Get cart items
        mockMvc.perform(get("/api/v1/cart/items")
                        .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].productId").exists())
                .andExpect(jsonPath("$[1].productId").exists());

        // Step 4: Place order
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-ID", userId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").exists())
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.createdAt").exists());

        // Verify order created
        assertEquals(1, orderRepository.count());
        Order order = orderRepository.findAll().getFirst();
        assertEquals(userId, order.getUserId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertTrue(order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(2, order.getItems().size());

        // Verify cart cleared
        assertTrue(cartItemRepository.findByUserId(userId).orElseThrow().isEmpty());
    }

    @Test
    void addRemoveAddPlaceOrder_shouldWorkEndToEnd() throws Exception {
        //given
        Long userId = 10L;
        String itemJson = """
                {
                    "productId": 100,
                    "quantity": 3
                }
                """;

        // Step 1: Add item
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated());

        // Step 2: Remove item
        mockMvc.perform(delete("/api/v1/cart/items/100")
                        .header("X-User-ID", userId))
                .andExpect(status().isNoContent());

        // Verify cart empty
        assertTrue(cartItemRepository.findByUserId(userId).orElseThrow().isEmpty());

        // Step 3: Add item again
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated());

        // Step 4: Place order
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-ID", userId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Verify order created
        assertEquals(1, orderRepository.count());
    }

    @Test
    void multipleUsers_shouldHaveSeparateCarts() throws Exception {
        //given
        Long user1 = 10L;
        Long user2 = 20L;
        String itemJson = """
                {
                    "productId": 100,
                    "quantity": 1
                }
                """;

        // Step 1: User1 adds item
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", user1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated());

        // Step 2: User2 adds different item
        String itemJson2 = """
                {
                    "productId": 200,
                    "quantity": 2
                }
                """;
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", user2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson2))
                .andExpect(status().isCreated());

        // Verify separate carts
        assertEquals(1, cartItemRepository.findByUserId(user1).orElseThrow().size());
        assertEquals(1, cartItemRepository.findByUserId(user2).orElseThrow().size());

        // Step 3: User1 places order
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-ID", user1))
                .andExpect(status().isCreated());

        // Verify only user1's cart cleared
        assertTrue(cartItemRepository.findByUserId(user1).orElseThrow().isEmpty());
        assertEquals(1, cartItemRepository.findByUserId(user2).orElseThrow().size());
    }

    @Test
    @Transactional
    void updateQuantityByAddingSameProduct_shouldWorkEndToEnd() throws Exception {
        //given
        Long userId = 10L;
        String itemJson = """
                {
                    "productId": 100,
                    "quantity": 2
                }
                """;

        // Step 1: Add item
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated());

        // Step 2: Add same item again
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated());

        // Verify quantity updated
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, 100L).orElseThrow();
        assertEquals(4, cartItem.getQuantity());

        // Step 3: Place order
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-ID", userId))
                .andExpect(status().isCreated());

        // Verify order has correct total
        Order order = orderRepository.findAll().getFirst();
        assertEquals(1, order.getItems().size());
        assertEquals(4, order.getItems().getFirst().getQuantity());
        // 4 x placeholder unit price (1000): quantity must not be applied twice
        assertEquals(0, new BigDecimal("4000").compareTo(order.getTotalAmount()));
    }
}
