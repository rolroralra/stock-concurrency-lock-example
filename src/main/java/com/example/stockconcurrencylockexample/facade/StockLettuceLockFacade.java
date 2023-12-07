package com.example.stockconcurrencylockexample.facade;

import com.example.stockconcurrencylockexample.repository.RedisLockRepository;
import com.example.stockconcurrencylockexample.service.StockCommand;
import com.example.stockconcurrencylockexample.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class StockLettuceLockFacade implements StockCommand {
    private final RedisLockRepository redisLockRepository;

    private final StockService stockService;

    public StockLettuceLockFacade(
        RedisLockRepository redisLockRepository,
        StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    /**
     * Decrease stock quantity with Redis Lock (Lettuce)
     * @param id Product ID
     * @param quantity Quantity to decrease
     */
    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        try {
            while (Boolean.FALSE.equals(redisLockRepository.lock(id))) {
                sleep(100);  // Spin Lock
            }

            stockService.decreaseStockQuantity(id, quantity);
        } finally {
            redisLockRepository.unlock(id);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
