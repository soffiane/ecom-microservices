package com.ecommerce.product.controller;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.mapper.ProductResponseMapper;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;
    private final ProductResponseMapper productResponseMapper;

    public ProductController(ProductService productService, ProductResponseMapper productResponseMapper) {
        this.productService = productService;
        this.productResponseMapper = productResponseMapper;
    }

    @GetMapping
    @Operation(summary = "List active products",
            description = "Returns every product whose `active` flag is true. No pagination, filtering or sorting.")
    @ApiResponse(responseCode = "200", description = "List of active products (possibly empty)")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.findAllProducts().stream()
                .map(productResponseMapper::mapToProductResponse)
                .toList());
    }

    @PostMapping
    @Operation(summary = "Create a product",
            description = "Creates an active product. `stock` defaults to 0 when omitted.")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Validation failed (body uses the default Spring Boot error format)",
            content = @Content)
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseMapper.mapToProductResponse(productService.createProduct(product)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id",
            description = "Returns the product whatever its `active` flag.")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "No product with this id", content = @Content)
    public ResponseEntity<ProductResponse> getProductById(@Parameter(description = "Product id") @PathVariable Long id) {
        return productService.findProductById(id)
                .map(productResponseMapper::mapToProductResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a product",
            description = "Overwrites name, description, price, category and image. "
                    + "`stock` is only updated when provided. The `active` flag is not changed.")
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "400", description = "Validation failed (body uses the default Spring Boot error format)",
            content = @Content)
    @ApiResponse(responseCode = "404", description = "No product with this id", content = @Content)
    public ResponseEntity<ProductResponse> updateProduct(@Parameter(description = "Product id") @PathVariable Long id,
                                                         @RequestBody @Valid ProductRequest product) {
        return ResponseEntity.status(HttpStatus.OK).body(productResponseMapper.mapToProductResponse(productService.updateProduct(id, product)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product",
            description = "Permanently deletes the product (hard delete; the `active` flag is not used).")
    @ApiResponse(responseCode = "204", description = "Product deleted")
    @ApiResponse(responseCode = "404", description = "No product with this id", content = @Content)
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "Product id") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name",
            description = "Case-insensitive substring match on the name, restricted to active products with stock > 0.")
    @ApiResponse(responseCode = "200", description = "Matching products")
    @ApiResponse(responseCode = "404", description = "No product matches (returned instead of an empty list)",
            content = @Content)
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @Parameter(description = "Substring to look for in the product name", required = true) @RequestParam String name) {
        List<Product> products = productService.searchProducts(name);
        if(products.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(products.stream()
                .map(productResponseMapper::mapToProductResponse)
                .toList());
    }

}
