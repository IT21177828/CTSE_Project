package com.ctse.microservice.product.controller;

import com.ctse.microservice.product.dto.ProductRequest;
import com.ctse.microservice.product.dto.ProductResponse;
import com.ctse.microservice.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product API", description = "Endpoints for product management")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
        log.info("Request to create product: {}", productRequest);
        return productService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all products", description = "Returns a list of all products")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    public List<ProductResponse> getAllProducts() {
        log.info("Request to fetch all products");
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns a single product by its ID")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        log.info("Request to fetch product with ID: {}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product with the provided details")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @RequestBody ProductRequest productRequest) {
        log.info("Request to update product with ID: {} and details: {}", id, productRequest);
        return ResponseEntity.ok(productService.updateProduct(id, productRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product", description = "Deletes a product by its ID")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public void deleteProduct(@PathVariable String id) {
        log.info("Request to delete product with ID: {}", id);
        productService.deleteProduct(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name", description = "Returns products containing the provided name")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<List<ProductResponse>> searchProductsByName(@RequestParam String name) {
        log.info("Request to search products with name containing: {}", name);
        return ResponseEntity.ok(productService.searchProductsByName(name));
    }
}
