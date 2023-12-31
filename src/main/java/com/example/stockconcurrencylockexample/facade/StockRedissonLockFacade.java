package com.example.stockconcurrencylockexample.facade;

import com.example.stockconcurrencylockexample.service.StockCommand;
import com.example.stockconcurrencylockexample.service.StockService;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class StockRedissonLockFacade implements StockCommand {
    private final RedissonClient redissonClient;

    private final StockService stockService;

    public StockRedissonLockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    /**
     * Decrease stock quantity with Redisson Lock
     * @param id Product ID
     * @param quantity Quantity to decrease
     */
    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        RLock lock = redissonClient.getLock(id.toString());

        try {
            // pub-sub Lock
            boolean isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new IllegalStateException("Failed to acquire lock");
            }

            stockService.decreaseStockQuantity(id, quantity);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

}
