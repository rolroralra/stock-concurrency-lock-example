# Environment
- Java 17
- Docker, Docker-Compose
- Gradle
- SpringBoot 3.2.0

# MySQL Setting
```bash
cd docker;

docker-compose -f docker-compose-mysql.yaml up -d;

docker exec -it $(docker ps | grep mysql | awk -F" " '{ print $1}') bash;

mysql -u root

create database test;
```

# DB Lock

## Pessimistic Lock
```java
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id = :id")
    Optional<Stock> findByIdWithPessimisticLock(@Param("id") Long id);
}
```

<details>
  <summary>재고 관리시스템 구현 코드 예시</summary>
  <p>

```java
@Service
public class StockPessimisticLockService implements StockCommand {

    private final StockRepository stockRepository;

    public StockPessimisticLockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Decrease stock quantity.
     * @param id Product ID
     * @param quantity Quantity to decrease
     * @throws IllegalStateException If the stock quantity is less than the quantity to decrease
     */
    @Transactional
    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(id)
            .orElseThrow(IllegalStateException::new);

        stock.decrease(quantity);
    }
}
```
        
  </p>
</details>

## Optimistic Lock
```java
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Optional<Stock> findByIdWithOptimisticLock(@Param("id") Long id);
}
```

<details>
  <summary>재고 관리시스템 구현 코드 예시</summary>
  <p>

```java
@Component
public class StockOptimisticLockFacade implements StockCommand {
    private final StockOptimisticLockService stockOptimisticLockService;

    public StockOptimisticLockFacade(StockOptimisticLockService stockOptimisticLockService) {
        this.stockOptimisticLockService = stockOptimisticLockService;
    }

    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        try {
            // Retry until success
            retryUntilSuccess(() ->
                stockOptimisticLockService.decreaseStockQuantity(id, quantity)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
}
```

  </p>
</details>

## Named Lock
```sql
SELECT GET_LOCK('lock_name', 1000);
SELECT RELEASE_LOCK('lock_name');
```

```java
@Query(value = "SELECT GET_LOCK(:key, 3000)", nativeQuery = true)
void getLock(@Param("key") String key);

@Query(value = "SELECT RELEASE_LOCK(:key)", nativeQuery = true)
void releaseLock(@Param("key") String key);
```

<details>
  <summary>재고 관리시스템 구현 코드 예시</summary>
  <p>

```java
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
```

  </p>
</details>

# Redis

## Lettuce
- `setnx` 명령어를 사용하여 락을 획득한다.
- spin lock 방식으로 락을 획득한다.
  - 동시에 많은 쓰레드가 Lock 획득 대기 상태일 경우, Redis에 부하가 걸릴 수 있다.

### Lettuce로 구현한 RedisLockRepository 
```java
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
```

<details>
  <summary>재고 관리시스템 구현 코드 예시</summary>
  <p>

```java
@Component
public class StockLettuceLockFacade implements StockCommand {
    private final RedisLockRepository redisLockRepository;

    private final StockService stockService;

    public StockLettuceLockFacade(
        RedisLockRepository redisLockRepository,
        StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    /**
     * Decrease stock quantity with Redis Lock (Lettuce)
     * @param id Product ID
     * @param quantity Quantity to decrease
     */
    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        try {
            while (Boolean.FALSE.equals(redisLockRepository.lock(id))) {
                sleep(100);  // Spin Lock
            }

            stockService.decreaseStockQuantity(id, quantity);
        } finally {
            redisLockRepository.unlock(id);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

  </p>
</details>

## Redisson
- `pub/sub` 방식으로 락을 획득한다.
- Lettuce에 비해 Redis에 대한 부하가 적다.
- 별도의 라이브러리를 사용해야한다.
  - `implementation 'org.redisson:redisson-spring-boot-starter:3.24.3'`

<details>
  <summary>재고 관리시스템 구현 코드 예시</summary>
  <p>
      
```java
@Component
public class StockRedissonLockFacade implements StockCommand {
    private final RedissonClient redissonClient;

    private final StockService stockService;

    public StockRedissonLockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    /**
     * Decrease stock quantity with Redisson Lock
     * @param id Product ID
     * @param quantity Quantity to decrease
     */
    @Override
    public void decreaseStockQuantity(Long id, Long quantity) {
        RLock lock = redissonClient.getLock(id.toString());

        try {
            // pub-sub Lock
            boolean isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new IllegalStateException("Failed to acquire lock");
            }

            stockService.decreaseStockQuantity(id, quantity);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
}
```

  </p>
</details>
