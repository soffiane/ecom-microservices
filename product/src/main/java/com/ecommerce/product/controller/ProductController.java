package com.ecommerce.product.controller;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.mapper.ProductResponseMapper;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductResponseMapper productResponseMapper;

    public ProductController(ProductService productService, ProductResponseMapper productResponseMapper) {
        this.productService = productService;
        this.productResponseMapper = productResponseMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.findAllProducts().stream()
                .map(productResponseMapper::mapToProductResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseMapper.mapToProductResponse(productService.createProduct(product)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.findProductById(id)
                .map(productResponseMapper::mapToProductResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody @Valid ProductRequest product) {
        return ResponseEntity.status(HttpStatus.OK).body(productResponseMapper.mapToProductResponse(productService.updateProduct(id, product)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProducts(name);
        if(products.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(products.stream()
                .map(productResponseMapper::mapToProductResponse)
                .toList());
    }

}
