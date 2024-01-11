package com.example.stockconcurrencylockexample.service;

import com.example.stockconcurrencylockexample.domain.Stock;
import com.example.stockconcurrencylockexample.repository.StockRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockOptimisticLockService implements StockCommand {

    private final StockRepository stockRepository;

    public StockOptimisticLockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Decrease stock quantity.
     * @param id Product ID
     * @param quantity Quantity to decrease
     * @throws IllegalArgumentException If the stock quantity is less than the quantity to decrease
     * @throws IllegalStateException If the stock is not found
     * @throws ObjectOptimisticLockingFailureException If too much requests come at the same time
     */
    @Transactional
    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(id)
            .orElseThrow(IllegalStateException::new);

        stock.decrease(quantity);
    }
}
