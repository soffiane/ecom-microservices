package com.ecommerce.order.integration;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class OrderIntegrationTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void shouldAddToCartAndPlaceOrder() throws Exception {
        //given
        Long userId = 10L;
        String cartItemJson = """
                {
                    "productId": 100,
                    "quantity": 2
                }
                """;

        //when - add to cart
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartItemJson))
                .andExpect(status().isCreated());

        //then - verify cart item was saved
        assertEquals(1, cartItemRepository.findByUserId(userId).orElseThrow().size());

        //when - place order
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-ID", userId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                // 2 x placeholder unit price (1000)
                .andExpect(jsonPath("$.totalAmount").value(2000));

        //then - verify order was saved and cart was cleared
        assertEquals(1, orderRepository.count());
        assertTrue(cartItemRepository.findByUserId(userId).orElseThrow().isEmpty());
    }

    @Test
    void shouldGetCartItems() throws Exception {
        //given
        Long userId = 10L;
        CartItem cartItem = CartItem.builder()
                .userId(userId)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("1000.00"))
                .build();
        cartItemRepository.save(cartItem);

        //when & then
        mockMvc.perform(get("/api/v1/cart/items")
                        .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId").value(100))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    void shouldRemoveFromCart() throws Exception {
        //given
        Long userId = 10L;
        CartItem cartItem = CartItem.builder()
                .userId(userId)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("1000.00"))
                .build();
        cartItemRepository.save(cartItem);

        //when
        mockMvc.perform(delete("/api/v1/cart/items/100")
                        .header("X-User-ID", userId))
                .andExpect(status().isNoContent());

        //then
        assertTrue(cartItemRepository.findByUserId(userId).orElseThrow().isEmpty());
    }

    @Test
    void shouldReturnNoContentForEmptyCart() throws Exception {
        //given
        Long userId = 10L;

        //when & then
        mockMvc.perform(get("/api/v1/cart/items")
                        .header("X-User-ID", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateQuantityWhenAddingExistingProduct() throws Exception {
        //given
        Long userId = 10L;
        CartItem cartItem = CartItem.builder()
                .userId(userId)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("1000.00"))
                .build();
        cartItemRepository.save(cartItem);

        String cartItemJson = """
                {
                    "productId": 100,
                    "quantity": 3
                }
                """;

        //when
        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartItemJson))
                .andExpect(status().isCreated());

        //then
        CartItem updatedItem = cartItemRepository.findByUserIdAndProductId(userId, 100L).orElseThrow();
        assertEquals(5, updatedItem.getQuantity());
        assertEquals(0, new BigDecimal("1000").compareTo(updatedItem.getPrice()));
    }

    @Test
    void shouldRollbackOrderWhenClearingCartFails() {
        //given
        Long userId = 10L;
        cartItemRepository.save(CartItem.builder()
                .userId(userId)
                .productId(100L)
                .quantity(2)
                .price(new BigDecimal("1000.00"))
                .build());
        doThrow(new IllegalStateException("simulated failure")).when(cartService).clearCart(userId);

        //when
        assertThrows(Exception.class, () -> mockMvc.perform(post("/api/v1/orders")
                .header("X-User-ID", userId)));

        //then - order creation is rolled back and the cart is untouched
        assertEquals(0, orderRepository.count());
        assertEquals(1, cartItemRepository.findByUserId(userId).orElseThrow().size());
    }

    @Test
    void shouldRejectDuplicateCartLineForSameUserAndProduct() {
        //given
        Long userId = 10L;
        cartItemRepository.save(CartItem.builder()
                .userId(userId)
                .productId(100L)
                .quantity(1)
                .price(new BigDecimal("1000.00"))
                .build());

        //when & then
        assertThrows(DataIntegrityViolationException.class, () -> cartItemRepository.save(CartItem.builder()
                .userId(userId)
                .productId(100L)
                .quantity(1)
                .price(new BigDecimal("1000.00"))
                .build()));
    }
}
