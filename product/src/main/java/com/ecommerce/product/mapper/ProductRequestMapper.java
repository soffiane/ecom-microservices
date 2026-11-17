package com.ecommerce.product.mapper;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductRequest;
import org.springframework.stereotype.Service;

@Service
public class ProductRequestMapper {

    public Product mapToProductRequest(ProductRequest product) {
        return Product.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .quantity(product.getStock() != null ? product.getStock() : 0)
                .image(product.getImage())
                .active(true)
                .build();
    }
}
