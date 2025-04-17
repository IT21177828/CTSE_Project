package com.ctse.microservice.inventoryService.controller;

import com.ctse.microservice.inventoryService.model.Inventory;
import com.ctse.microservice.inventoryService.service.InventoryService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@RequestParam  String skuCode,@RequestParam Integer quantity) {
        return inventoryService.isInStock(skuCode, quantity);
    }

    @GetMapping("/all")
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory addInventory(@RequestBody Inventory inventory) {
        return inventoryService.addInventory(inventory);
    }

    @PutMapping("/{id}")
    public Inventory updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        return inventoryService.updateQuantity(id, quantity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
    }

    @GetMapping("/item/{skuCode}")
    public Inventory getBySkuCode(@PathVariable String skuCode) {
        return inventoryService.getBySkuCode(skuCode);
    }


}
