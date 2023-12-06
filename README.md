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

## Optimistic Lock
```java
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Optional<Stock> findByIdWithOptimisticLock(@Param("id") Long id);
}
```

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

# Redis

## Lettuce
- `setnx` 명령어를 사용하여 락을 획득한다.
- spin lock 방식으로 락을 획득한다.
  - 동시에 많은 쓰레드가 Lock 획득 대기 상태일 경우, Redis에 부하가 걸릴 수 있다.

## Redisson
- `pub/sub` 방식으로 락을 획득한다.
- Lettuce에 비해 Redis에 대한 부하가 적다.
- 별도의 라이브러리를 사용해야한다.
  - `implementation 'org.redisson:redisson-spring-boot-starter:3.24.3'`
