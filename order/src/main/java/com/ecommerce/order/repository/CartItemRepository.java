package com.ecommerce.order.repository;

import com.ecommerce.order.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    //Optional<CartItem> findByUserIdAndProduct(Long userId, Product product);
    Optional<CartItem> findByUserIdAndProductId(String userId, Long productId);

    //void deleteByUserAndProduct(User user, Product product);

    //Optional<List<CartItem>> findByUser(User user);
    Optional<List<CartItem>> findByUserId(String userId);

    //void deleteByUser(User user);
    void deleteByUserId(String userId);

    void deleteByUserIdAndProductId(String userId, Long productId);
}
