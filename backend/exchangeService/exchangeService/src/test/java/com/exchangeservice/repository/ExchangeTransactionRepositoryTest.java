package com.exchangeservice.repository;

import com.exchangeservice.entity.ExchangeTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ExchangeTransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExchangeTransactionRepository repository;

    @Test
    void saveAndFindById_Success() {
        // Arrange
        ExchangeTransaction transaction = new ExchangeTransaction();
        transaction.setAccountId(1L);
        transaction.setFromCurrency("TRY");
        transaction.setToCurrency("USD");
        transaction.setFromAmount(BigDecimal.valueOf(1000));
        transaction.setToAmount(BigDecimal.valueOf(35.09));
        transaction.setTransactionType("BUY");
        transaction.setTimestamp(LocalDateTime.now());
        
        // Act
        ExchangeTransaction savedTransaction = repository.save(transaction);
        entityManager.flush();
        entityManager.clear();
        
        Optional<ExchangeTransaction> foundTransaction = repository.findById(savedTransaction.getId());
        
        // Assert
        assertTrue(foundTransaction.isPresent());
        assertEquals(1L, foundTransaction.get().getAccountId());
        assertEquals("TRY", foundTransaction.get().getFromCurrency());
        assertEquals("USD", foundTransaction.get().getToCurrency());
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(foundTransaction.get().getFromAmount()));
        assertEquals(0, BigDecimal.valueOf(35.09).compareTo(foundTransaction.get().getToAmount()));
        assertEquals("BUY", foundTransaction.get().getTransactionType());
    }
}
