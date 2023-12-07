package com.example.stockconcurrencylockexample.facade;

import com.example.stockconcurrencylockexample.service.StockCommand;
import com.example.stockconcurrencylockexample.service.StockOptimisticLockService;
import org.springframework.stereotype.Component;

@Component
public class StockOptimisticLockFacade implements StockCommand {
    private final StockOptimisticLockService stockOptimisticLockService;

    public StockOptimisticLockFacade(StockOptimisticLockService stockOptimisticLockService) {
        this.stockOptimisticLockService = stockOptimisticLockService;
    }

    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        try {
            // Retry until success
            retryUntilSuccess(() ->
                stockOptimisticLockService.decreaseStockQuantity(id, quantity)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void retryUntilSuccess(Runnable runnable) throws InterruptedException {
        while (true) {
            try {
                runnable.run();
                break;
            } catch (RuntimeException e) {
                // retry
                Thread.sleep(500);
            }
        }
    }
}
