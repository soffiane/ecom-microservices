package com.ecommerce.order.clients;

import com.ecommerce.order.dto.ProductResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Optional;

@HttpExchange("/api/v1/products")
public interface ProductClient {

    @GetExchange("/{id}")
    Optional<ProductResponse> findByIdAndActiveTrue(@PathVariable Long id);
}
