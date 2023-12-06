package com.example.stockconcurrencylockexample.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class TestExecutors {
    public static void testWithMultiThreads(int threadCount, Runnable runnable) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        IntStream.range(0, threadCount)
            .forEach(i ->
                executorService.submit(() -> {
                    try {
                        runnable.run();
                    } finally {
                        countDownLatch.countDown();
                    }
                })
            );

        countDownLatch.await();
    }
}
