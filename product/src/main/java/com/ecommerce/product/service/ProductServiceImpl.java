package com.ecommerce.product.service;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductRequestMapper;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductRequestMapper productRequestMapper;

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .toList();
    }

    @Override
    public Product createProduct(ProductRequest product) {
        return productRepository.save(productRequestMapper.mapToProductRequest(product));
    }

    @Override
    public Optional<Product> findProductById(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Product> searchProducts(String name) {
        return productRepository.searchProducts(name)
                .stream()
                .toList();
    }

    @Override
    public Product updateProduct(Long id, ProductRequest product) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isEmpty()) {
            throw new ProductNotFoundException(String.format("Product with id %s not found", id));
        }
        Product updatedProduct = existingProduct.get();
        updatedProduct.setName(product.getName());
        updatedProduct.setDescription(product.getDescription());
        updatedProduct.setPrice(product.getPrice());
        updatedProduct.setCategory(product.getCategory());
        // stock is optional in the request: keep the current stock when it is not provided
        if (product.getStock() != null) {
            updatedProduct.setQuantity(product.getStock());
        }
        updatedProduct.setImage(product.getImage());
        return productRepository.save(updatedProduct);

    }

    @Override
    public void deleteProduct(Long id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isEmpty()) {
            throw new ProductNotFoundException(String.format("Product with id %s not found", id));
        }
        productRepository.deleteById(id);
    }
}
