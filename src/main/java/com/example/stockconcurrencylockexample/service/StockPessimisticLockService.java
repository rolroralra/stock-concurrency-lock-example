package com.example.stockconcurrencylockexample.service;

import com.example.stockconcurrencylockexample.domain.Stock;
import com.example.stockconcurrencylockexample.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockPessimisticLockService implements StockCommand {

    private final StockRepository stockRepository;

    public StockPessimisticLockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(id)
            .orElseThrow(IllegalStateException::new);

        stock.decrease(quantity);
    }
}
