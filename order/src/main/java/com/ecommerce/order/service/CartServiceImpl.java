package com.ecommerce.order.service;

import com.ecommerce.order.clients.ProductClient;
import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.exception.CartItemNotFoundException;
import com.ecommerce.order.exception.OutOfStockException;
import com.ecommerce.order.exception.ProductNotFoundException;
import com.ecommerce.order.exception.UserNotFoundException;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.users.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductClient productClient;
    private final UserClient userClient;

    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public void addToCart(String userId, CartItemRequest cartItemRequest) {
        findActiveProduct(cartItemRequest.getProductId()).ifPresentOrElse(product -> {
            if (product.getStock() < cartItemRequest.getQuantity()) {
                throw new OutOfStockException("Article is out of stock");
            }
            findUser(userId).ifPresentOrElse(user -> {
                // price = prix unitaire : les totaux sont calculés price × quantity
                CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                        .map(existing -> {
                            existing.setQuantity(existing.getQuantity() + cartItemRequest.getQuantity());
                            return existing;
                        })
                        .orElseGet(() -> CartItem.builder()
                                .userId(user.getId())
                                .productId(product.getId())
                                .quantity(cartItemRequest.getQuantity())
                                .build());
                cartItem.setPrice(product.getPrice());
                cartItemRepository.save(cartItem);
            }, () -> {
                throw new UserNotFoundException("User not found");
            });
        }, () -> {
            throw new ProductNotFoundException("Product not found");
        });
    }

    // product-service renvoie aussi les produits inactifs : on les traite comme introuvables
    private Optional<ProductResponse> findActiveProduct(Long productId) {
        try {
            return productClient.findByIdAndActiveTrue(productId).filter(ProductResponse::isActive);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    // un 404 du service distant lève une exception au lieu de renvoyer un Optional vide
    private Optional<UserResponse> findUser(String userId) {
        try {
            return userClient.getUserById(userId);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void removeFromCart(String userId, Long productId) {
        requireUser(userId);
        // pas de vérification côté product-service : on doit pouvoir retirer un produit désactivé ou supprimé depuis
        cartItemRepository.findByUserIdAndProductId(userId, productId).ifPresentOrElse(
                cartItem -> cartItemRepository.deleteByUserIdAndProductId(userId, productId),
                () -> {
                    throw new CartItemNotFoundException("Cart item not found");
                });
    }

    @Override
    public Optional<List<CartItem>> getUserCartItems(String userId) {
        requireUser(userId);
        return cartItemRepository.findByUserId(userId)
                .filter(cartItems -> !cartItems.isEmpty());
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        // pas d'appel à user-service : seul placeOrder appelle clearCart, après avoir déjà vérifié l'utilisateur
        // (via getUserCartItems). Un nouvel appel distant ici pourrait annuler une commande déjà enregistrée.
        cartItemRepository.deleteByUserId(userId);
    }

    private void requireUser(String userId) {
        if (findUser(userId).isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
    }
}
