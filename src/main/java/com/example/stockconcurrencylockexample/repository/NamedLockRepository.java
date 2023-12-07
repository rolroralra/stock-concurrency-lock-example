package com.example.stockconcurrencylockexample.repository;

import com.example.stockconcurrencylockexample.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NamedLockRepository extends JpaRepository<Stock, Long> {

    @Query(value = "SELECT GET_LOCK(:key, 3000)", nativeQuery = true)
    void lock(@Param("key") String key);

    @Query(value = "SELECT RELEASE_LOCK(:key)", nativeQuery = true)
    void unlock(@Param("key") String key);
}
