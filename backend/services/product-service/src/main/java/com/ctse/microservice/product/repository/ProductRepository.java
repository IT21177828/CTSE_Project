package com.ctse.microservice.product.repository;

import com.ctse.microservice.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    /**
     * Find products by name containing the given string, case-insensitive
     * @param name name to search for
     * @return list of matching products
     */
    List<Product> findByNameContainingIgnoreCase(String name);
}
