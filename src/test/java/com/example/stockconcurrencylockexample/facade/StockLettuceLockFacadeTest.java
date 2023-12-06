package com.example.stockconcurrencylockexample.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.stockconcurrencylockexample.common.RedisTest;
import com.example.stockconcurrencylockexample.common.TestExecutors;
import com.example.stockconcurrencylockexample.domain.Stock;
import com.example.stockconcurrencylockexample.repository.StockRepository;
import com.example.stockconcurrencylockexample.service.StockCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StockLettuceLockFacadeTest extends RedisTest {
    @Qualifier("stockLettuceLockFacade")
    @Autowired
    private StockCommand stockCommand;

    @Autowired
    protected StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        stockRepository.save(new Stock(1L, 100L));
    }

    @Order(1)
    @Test
    void decreaseStockQuantity() {
        stockCommand.decreaseStockQuantity(1L, 10L);

        Stock stock = stockRepository.findById(1L).orElseThrow(IllegalStateException::new);

        assertEquals(90L, stock.getQuantity());
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(ints = {100})
    void decreaseStockQuantityWithMultiThreads(int threadCount) throws InterruptedException {
        TestExecutors.testWithMultiThreads(
            threadCount,
            () -> stockCommand.decreaseStockQuantity(1L, 1L)
        );

        Stock stock = stockRepository.findById(1L).orElseThrow(IllegalStateException::new);
        assertThat(stock.getQuantity()).isZero();
    }
}
