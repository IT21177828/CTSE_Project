package com.ctse.microservice.orderService.exception;

public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(String skuCode) {
        super("Product with SKU code '" + skuCode + "' is not in stock.");
    }
}
