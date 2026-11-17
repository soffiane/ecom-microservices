package com.ecommerce.product.mapper;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductResponseMapper {


    public ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .stock(product.getQuantity())
                .image(product.getImage())
                .price(product.getPrice())
                .active(product.isActive())
                .build();
    }
}
