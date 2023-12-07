package com.example.stockconcurrencylockexample.facade;

import com.example.stockconcurrencylockexample.repository.NamedLockRepository;
import com.example.stockconcurrencylockexample.service.StockCommand;
import com.example.stockconcurrencylockexample.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class StockNamedLockFacade implements StockCommand {

    private final NamedLockRepository namedLockRepository;

    private final StockService stockService;

    public StockNamedLockFacade(NamedLockRepository namedLockRepository, StockService stockService) {
        this.namedLockRepository = namedLockRepository;
        this.stockService = stockService;
    }

    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        try {
            namedLockRepository.lock(id.toString());
            stockService.decreaseStockQuantity(id, quantity);
        } finally {
            namedLockRepository.unlock(id.toString());
        }
    }
}
