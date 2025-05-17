package com.ctse.microservice.inventoryService.repository;

import com.ctse.microservice.inventoryService.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsBySkuCodeAndQuantityGreaterThanEqual(String skuCode, Integer quantity);
    Optional<Inventory> findBySkuCode(String skuCode);
}
