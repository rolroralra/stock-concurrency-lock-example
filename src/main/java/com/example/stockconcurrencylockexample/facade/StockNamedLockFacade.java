package com.example.stockconcurrencylockexample.facade;

import com.example.stockconcurrencylockexample.repository.LockRepository;
import com.example.stockconcurrencylockexample.repository.StockRepository;
import com.example.stockconcurrencylockexample.service.StockCommand;
import com.example.stockconcurrencylockexample.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class StockNamedLockFacade implements StockCommand {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public StockNamedLockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decreaseStockQuantity(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
