package com.example.stockconcurrencylockexample.service;

import com.example.stockconcurrencylockexample.domain.Stock;
import com.example.stockconcurrencylockexample.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockOptimisticLockService implements StockCommand {

    private final StockRepository stockRepository;

    public StockOptimisticLockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(id)
            .orElseThrow(IllegalStateException::new);

        stock.decrease(quantity);
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

    private void _decreaseStockQuantity(Long id, Long quantity) {

    }



}
