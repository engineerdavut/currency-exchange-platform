package com.exchangeservice.repository;

import com.exchangeservice.entity.ExchangeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeTransactionRepository extends JpaRepository<ExchangeTransaction, Long> {
    // Additional query methods can be defined here if needed.
}
