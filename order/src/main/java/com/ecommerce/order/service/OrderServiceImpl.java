package com.ecommerce.order.service;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.exception.EmptyCartException;
import com.ecommerce.order.mapper.OrderResponseMapper;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderResponseMapper orderResponseMapper;

    public OrderServiceImpl(OrderRepository orderRepository, CartService cartService, OrderResponseMapper orderResponseMapper) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.orderResponseMapper = orderResponseMapper;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(String userId) {
        //validate user (via user-service) and cart items: seule vérification distante, faite avant l'enregistrement
        List<CartItem> cartItems = cartService.getUserCartItems(userId).orElseThrow(() -> new EmptyCartException("Cart not found or empty"));
        //calculate total amount
        //save order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setItems(cartItems.stream()
                .map(cartItem ->
                        new OrderItem(null,
                                cartItem.getProductId(),
                                cartItem.getQuantity(),
                                cartItem.getPrice(),
                                order))
                .toList());
        order.setTotalAmount(cartItems.stream()
                .map(cartItem -> cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        Order saved = orderRepository.save(order);
        //clear cart
        cartService.clearCart(userId);
        return orderResponseMapper.mapToOrderResponse(saved);
    }
}
