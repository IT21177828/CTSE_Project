package com.ctse.microservice.inventoryService.service;

import com.ctse.microservice.inventoryService.model.Inventory;
import com.ctse.microservice.inventoryService.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    /**
     * Checks if the requested quantity of a product is in stock.
     *
     * @param skuCode  The SKU code of the product.
     * @param quantity The requested quantity.
     * @return true if the requested quantity is available, false otherwise.
     */
    public boolean isInStock(String skuCode, Integer quantity) {
        log.info("Checking stock for SKU: {}, requested quantity: {}\n", skuCode, quantity);

        if (quantity <= 0) {
            log.warn("Invalid quantity: {}. Quantity must be greater than 0.\n", quantity);
            return false;
        }

        Optional<Inventory> inventoryOptional = inventoryRepository.findBySkuCode(skuCode);

        if (inventoryOptional.isPresent()) {
            Inventory inventory = inventoryOptional.get();
            int availableQty = inventory.getQuantity();
            log.info("Current stock for {} is {}\n", skuCode, availableQty);

            if (availableQty >= quantity) {
                inventory.setQuantity(availableQty - quantity);
                inventoryRepository.save(inventory);
                log.info("Stock updated. New quantity for {}: {}\n", skuCode, inventory.getQuantity());
                return true;
            } else {
                log.warn("Not enough stock. Requested: {}, Available: {}\n", quantity, availableQty);
                return false;
            }
        } else {
            log.warn("SKU '{}' not found in inventory.\n", skuCode);
            return false;
        }
    }


    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory addInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Inventory updateQuantity(Long id, Integer quantity) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    public Inventory getBySkuCode(String skuCode) {
        return inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }
}
