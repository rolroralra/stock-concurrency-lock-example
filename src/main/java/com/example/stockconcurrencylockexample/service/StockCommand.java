package com.example.stockconcurrencylockexample.service;

public interface StockCommand {
    /**
     * Decrease stock quantity
     * @param id Product ID
     * @param quantity Quantity to decrease
     * @throws IllegalStateException If the stock quantity is less than the quantity to decrease
     */
    void decreaseStockQuantity(Long id, Long quantity);
}
