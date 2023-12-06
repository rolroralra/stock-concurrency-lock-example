package com.example.stockconcurrencylockexample.common;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public abstract class RedisTest {
    private final static String TEST_CONTAINER_IMAGE_TAG = "redis:7.2.3-alpine";

    private final static int REDIS_PORT = 6379;

    static GenericContainer<?> redis = new GenericContainer<>(TEST_CONTAINER_IMAGE_TAG)
        .withExposedPorts(REDIS_PORT);

    @BeforeAll
    static void beforeAll() {
        redis.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(REDIS_PORT));
    }
}
