package com.ecommerce.product.service;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductRequest;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findAllProducts();

    Product createProduct(ProductRequest product);

    Optional<Product> findProductById(Long id);

    List<Product> searchProducts(String name);

    Product updateProduct(Long id, ProductRequest product);

    void deleteProduct(Long id);
}
