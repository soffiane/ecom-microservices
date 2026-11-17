package com.ecommerce.order.service;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.exception.CartItemNotFoundException;
import com.ecommerce.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    /*private final ProductRepository productRepository;
    private final UserRepository userRepository;*/

    @Override
    public void addToCart(Long userId, CartItemRequest cartItemRequest) {
        /*productRepository.findById(cartItemRequest.getProductId()).ifPresentOrElse(product -> {
            if (product.getQuantity() < cartItemRequest.getQuantity()) {
                throw new OutOfStockException("Article is out of stock");
            }
            userRepository.findById(userId).ifPresentOrElse(user -> {
                //si le produit est deja ajouté au panier, on modifie la quantité
                cartItemRepository.findByUserIdAndProduct(userId, product).ifPresent(cartItem -> {
                    cartItem.setQuantity(cartItem.getQuantity() + cartItemRequest.getQuantity());
                    cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                    cartItemRepository.save(cartItem);
                });
                //sinon on l'ajoute au panier
                cartItemRepository.save(CartItem.builder()
                        .userId(user)
                        .product(product)
                        .quantity(cartItemRequest.getQuantity())
                        .price(product.getPrice().multiply(BigDecimal.valueOf(cartItemRequest.getQuantity())))
                        .build());
            }, () -> {
                throw new UserNotFoundException("User not found");
            });
        }, () -> {
            throw new ProductNotFoundException("Product not found");
        });*/
        cartItemRepository.findByUserIdAndProductId(userId, cartItemRequest.getProductId()).ifPresentOrElse(cartItem -> {
            cartItem.setQuantity(cartItem.getQuantity() + cartItemRequest.getQuantity());
            cartItem.setPrice(BigDecimal.valueOf(cartItem.getQuantity() * 1000L));
            cartItemRepository.save(cartItem);
        }, () -> cartItemRepository.save(CartItem.builder()
                .userId(userId)
                .productId(cartItemRequest.getProductId())
                .quantity(cartItemRequest.getQuantity())
                .price(BigDecimal.valueOf(cartItemRequest.getQuantity() * 1000L))
                .build()));
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        /*productRepository.findById(productId).ifPresentOrElse(product -> {
            userRepository.findById(userId).ifPresentOrElse(user -> {
                cartItemRepository.deleteByUserAndProduct(user, product);
            }, () -> {
                throw new UserNotFoundException("User not found");
            });
        }, () -> {
            throw new ProductNotFoundException("Product not found");
        });*/
        cartItemRepository.findByUserIdAndProductId(userId, productId).ifPresentOrElse(cartItem -> {
            cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        }, () -> {
            throw new CartItemNotFoundException("Cart item not found");
        });
    }

    @Override
    public Optional<List<CartItem>> getUserCartItems(Long userId) {
        /*return userRepository.findById(userId)
                .map(cartItemRepository::findByUser)
                .orElseThrow(() -> new UserNotFoundException("User not found"));*/
        return cartItemRepository.findByUserId(userId)
                .filter(cartItems -> !cartItems.isEmpty());
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        /*userRepository.findById(userId).ifPresentOrElse(cartItemRepository::deleteByUser, () -> {
            throw new UserNotFoundException("User not found");
        });*/
        cartItemRepository.deleteByUserId(userId);
    }
}
