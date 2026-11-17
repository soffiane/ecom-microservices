package com.ecommerce.order.repository;

import com.ecommerce.order.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    //Optional<CartItem> findByUserIdAndProduct(Long userId, Product product);
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    //void deleteByUserAndProduct(User user, Product product);

    //Optional<List<CartItem>> findByUser(User user);
    Optional<List<CartItem>> findByUserId(Long userId);

    //void deleteByUser(User user);
    void deleteByUserId(Long userId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
