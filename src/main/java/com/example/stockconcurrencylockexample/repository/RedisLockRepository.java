package com.example.stockconcurrencylockexample.repository;

import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLockRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Lock product by ID with Redis Lock (Lettuce)
     * @param id Product ID
     * @return true if lock is acquired, false otherwise
     */
    public Boolean lock(Long id) {
        return redisTemplate
            .opsForValue()
            .setIfAbsent(id.toString(), "lock", Duration.ofSeconds(3));
    }

    /**
     * Unlock product by ID with Redis Lock (Lettuce)
     * @param id Product ID
     * @return true if lock is released, false otherwise
     */
    public Boolean unlock(Long id) {
        return redisTemplate.delete(id.toString());
    }
}
