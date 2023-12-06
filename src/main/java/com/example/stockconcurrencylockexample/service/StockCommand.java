package com.example.stockconcurrencylockexample.service;

public interface StockCommand {
    void decreaseStockQuantity(Long id, Long quantity);
}
