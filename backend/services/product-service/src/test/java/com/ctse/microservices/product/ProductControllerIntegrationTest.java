package com.ctse.microservices.product;

import com.ctse.microservice.product.ProductServiceApplication;
import com.ctse.microservice.product.model.Product;
import com.ctse.microservice.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = ProductServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerIntegrationTest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Product testProduct;
    
    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        
        // Create a test product in the database
        testProduct = Product.builder()
                .name("Test iPhone")
                .description("Test iPhone Description")
                .price(new BigDecimal("999.99"))
                .build();
        testProduct = productRepository.save(testProduct);
    }
    
    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
    }
    
    static {
        mongoDBContainer.start();
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
    }

    @Test
    void shouldCreateProduct() {
        String requestBody = """
                {
                    "name":"Macbook Pro M3",
                    "description":"The latest MacBook Pro with Apple Silicon",
                    "price": 2499.00
                }
                """;
        
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/product")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Macbook Pro M3"))
                .body("description", equalTo("The latest MacBook Pro with Apple Silicon"))
                .body("price", equalTo(2499.00f));
    }
    
    @Test
    void shouldGetAllProducts() {
        given()
                .when()
                .get("/api/product")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThanOrEqualTo(1))
                .body("name", hasItem("Test iPhone"));
    }
    
    @Test
    void shouldGetProductById() {
        given()
                .when()
                .get("/api/product/{id}", testProduct.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(testProduct.getId()))
                .body("name", equalTo("Test iPhone"))
                .body("description", equalTo("Test iPhone Description"))
                .body("price", equalTo(999.99f));
    }
    
    @Test
    void shouldReturnNotFoundForNonExistentProduct() {
        given()
                .when()
                .get("/api/product/nonexistentid")
                .then()
                .statusCode(404);
    }
    
    @Test
    void shouldUpdateProduct() {
        String requestBody = """
                {
                    "name":"Updated iPhone",
                    "description":"Updated iPhone Description",
                    "price": 1099.99
                }
                """;
        
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/api/product/{id}", testProduct.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(testProduct.getId()))
                .body("name", equalTo("Updated iPhone"))
                .body("description", equalTo("Updated iPhone Description"))
                .body("price", equalTo(1099.99f));
    }
    
    @Test
    void shouldDeleteProduct() {
        // First delete the product
        given()
                .when()
                .delete("/api/product/{id}", testProduct.getId())
                .then()
                .statusCode(204);
        
        // Then verify it's gone
        given()
                .when()
                .get("/api/product/{id}", testProduct.getId())
                .then()
                .statusCode(404);
    }
    
    @Test
    void shouldSearchProductsByName() {
        // Create additional products with similar names
        Product iphone12 = Product.builder()
                .name("iPhone 12")
                .description("iPhone 12 Description")
                .price(new BigDecimal("799.99"))
                .build();
        productRepository.save(iphone12);
        
        Product iphone13 = Product.builder()
                .name("iPhone 13")
                .description("iPhone 13 Description")
                .price(new BigDecimal("899.99"))
                .build();
        productRepository.save(iphone13);
        
        // Search for products with "iPhone" in the name
        given()
                .when()
                .get("/api/product/search?name=iPhone")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(3))
                .body("name", hasItems("Test iPhone", "iPhone 12", "iPhone 13"));
    }
    
    @Test
    void shouldReturnBadRequestForInvalidInput() {
        String invalidRequestBody = """
                {
                    "name":"",
                    "description":"Invalid product with empty name",
                    "price": 499.99
                }
                """;
        
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestBody)
                .when()
                .post("/api/product")
                .then()
                .statusCode(400);
    }
}