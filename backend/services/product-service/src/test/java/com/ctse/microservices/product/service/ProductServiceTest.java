package com.ctse.microservices.product.service;

import com.ctse.microservice.product.dto.ProductRequest;
import com.ctse.microservice.product.dto.ProductResponse;
import com.ctse.microservice.product.exception.ProductNotFoundException;
import com.ctse.microservice.product.model.Product;
import com.ctse.microservice.product.repository.ProductRepository;
import com.ctse.microservice.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testProduct = Product.builder()
                .id("1")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .build();

        testProductRequest = new ProductRequest(
                null,
                "Test Product",
                "Test Description",
                new BigDecimal("99.99")
        );
    }

    @Test
    void createProduct_ShouldCreateAndReturnProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse result = productService.createProduct(testProductRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testProduct.getId());
        assertThat(result.name()).isEqualTo(testProduct.getName());
        assertThat(result.description()).isEqualTo(testProduct.getDescription());
        assertThat(result.price()).isEqualTo(testProduct.getPrice());
        
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_WithInvalidData_ShouldThrowException() {
        // Arrange
        ProductRequest invalidRequest = new ProductRequest(
                null,
                "",  // Empty name
                "Test Description",
                new BigDecimal("99.99")
        );

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product name cannot be empty");
        
        verify(productRepository, never()).save(any());
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        Product anotherProduct = Product.builder()
                .id("2")
                .name("Another Product")
                .description("Another Description")
                .price(new BigDecimal("49.99"))
                .build();
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, anotherProduct));

        // Act
        List<ProductResponse> results = productService.getAllProducts();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).id()).isEqualTo("1");
        assertThat(results.get(1).id()).isEqualTo("2");
        
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));

        // Act
        ProductResponse result = productService.getProductById("1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        
        verify(productRepository, times(1)).findById("1");
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById("999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");
        
        verify(productRepository, times(1)).findById("999");
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() {
        // Arrange
        ProductRequest updateRequest = new ProductRequest(
                null,
                "Updated Name",
                "Updated Description",
                new BigDecimal("149.99")
        );
        
        Product updatedProduct = Product.builder()
                .id("1")
                .name("Updated Name")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .build();
        
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ProductResponse result = productService.updateProduct("1", updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.description()).isEqualTo("Updated Description");
        assertThat(result.price()).isEqualTo(new BigDecimal("149.99"));
        
        verify(productRepository, times(1)).findById("1");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct("999", testProductRequest))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");
        
        verify(productRepository, times(1)).findById("999");
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
        // Arrange
        when(productRepository.existsById("1")).thenReturn(true);
        doNothing().when(productRepository).deleteById("1");

        // Act
        productService.deleteProduct("1");

        // Assert
        verify(productRepository, times(1)).existsById("1");
        verify(productRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.existsById("999")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct("999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");
        
        verify(productRepository, times(1)).existsById("999");
        verify(productRepository, never()).deleteById(anyString());
    }

    @Test
    void searchProductsByName_ShouldReturnMatchingProducts() {
        // Arrange
        Product product1 = Product.builder()
                .id("1")
                .name("iPhone 15")
                .description("Description 1")
                .price(new BigDecimal("999.99"))
                .build();
        
        Product product2 = Product.builder()
                .id("2")
                .name("iPhone 15 Pro")
                .description("Description 2")
                .price(new BigDecimal("1299.99"))
                .build();
        
        when(productRepository.findByNameContainingIgnoreCase("iPhone"))
                .thenReturn(Arrays.asList(product1, product2));

        // Act
        List<ProductResponse> results = productService.searchProductsByName("iPhone");

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).contains("iPhone");
        assertThat(results.get(1).name()).contains("iPhone");
        
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("iPhone");
    }
}