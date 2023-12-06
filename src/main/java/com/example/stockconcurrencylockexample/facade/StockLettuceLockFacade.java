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

    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        try {
            while (!redisLockRepository.lock(id)) {
                sleep(100);
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
            throw new RuntimeException(e);
        }
    }
}
