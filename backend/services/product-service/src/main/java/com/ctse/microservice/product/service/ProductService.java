package com.ctse.microservice.product.service;

import com.ctse.microservice.product.dto.ProductRequest;
import com.ctse.microservice.product.dto.ProductResponse;
import com.ctse.microservice.product.exception.ProductNotFoundException;
import com.ctse.microservice.product.model.Product;
import com.ctse.microservice.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * Creates a new product
     * @param productRequest DTO containing product details
     * @return the created product
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.debug("Creating product with details: {}", productRequest);

        validateProductRequest(productRequest);

        Product product = mapToEntity(productRequest);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return mapToResponse(savedProduct);
    }

    /**
     * Retrieves all products
     * @return List of products
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productsCache")
    public List<ProductResponse> getAllProducts() {
        log.debug("Fetching all products");
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets a product by ID
     * @param id the product ID
     * @return the product if found
     * @throws ProductNotFoundException if product not found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productCache", key = "#id")
    public ProductResponse getProductById(String id) {
        log.debug("Fetching product with ID: {}", id);
        return productRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    /**
     * Updates an existing product
     * @param id the product ID
     * @param productRequest DTO with updated product details
     * @return the updated product
     * @throws ProductNotFoundException if product not found
     */
    @Transactional
    @CacheEvict(value = {"productCache", "productsCache"}, allEntries = true)
    public ProductResponse updateProduct(String id, ProductRequest productRequest) {
        log.debug("Updating product with ID: {} and details: {}", id, productRequest);

        validateProductRequest(productRequest);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        // Update fields
        existingProduct.setName(productRequest.name());
        existingProduct.setDescription(productRequest.description());
        existingProduct.setPrice(productRequest.price());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());

        return mapToResponse(updatedProduct);
    }

    /**
     * Deletes a product by ID
     * @param id the product ID
     * @throws ProductNotFoundException if product not found
     */
    @Transactional
    @CacheEvict(value = {"productCache", "productsCache"}, allEntries = true)
    public void deleteProduct(String id) {
        log.debug("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    /**
     * Search products by name
     * @param name the name to search
     * @return list of matching products
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProductsByName(String name) {
        log.debug("Searching products with name containing: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper methods for mapping and validation

    private Product mapToEntity(ProductRequest productRequest) {
        return Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .build();
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }

    private void validateProductRequest(ProductRequest productRequest) {
        if (productRequest == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }

        if (!StringUtils.hasText(productRequest.name())) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        if (productRequest.price() == null || productRequest.price().doubleValue() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
    }
}
