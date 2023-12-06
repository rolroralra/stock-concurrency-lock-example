package com.example.stockconcurrencylockexample.service;

import com.example.stockconcurrencylockexample.domain.Stock;
import com.example.stockconcurrencylockexample.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockSynchronizedService implements StockCommand {
    private final StockRepository stockRepository;

    public StockSynchronizedService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * <code>@Transactional</code> is not required here because the method is synchronized.
     * If <code>@Transactional</code></code> is used, the code will be weaved by Spring AOP.
     * And then race condition will occur.
     */
//    @Transactional
    @Override
    public synchronized void decreaseStockQuantity(Long id, Long quantity) {
        Stock stock = stockRepository.findById(id).orElseThrow(IllegalStateException::new);

        stock.decrease(quantity);

        stockRepository.save(stock);
    }
}
