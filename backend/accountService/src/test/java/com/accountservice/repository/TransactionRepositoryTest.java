package com.accountservice.repository;


import com.accountservice.entity.Account;
import com.accountservice.entity.CurrencyType;
import com.accountservice.entity.Transaction;
import com.accountservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest

public class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findTop5ByAccountOrderByTimestampDesc_ShouldReturnLatestTransactions() {
        // Arrange
        User user = new User("testUser", "password");
        entityManager.persist(user);
        
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));
        entityManager.persist(account);
        
        // Create 7 transactions with different timestamps
        for (int i = 0; i < 7; i++) {
            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setAmount(new BigDecimal(100 * (i + 1)));
            transaction.setDescription("Transaction " + (i + 1));
            transaction.setTransactionType("DEPOSIT");
            transaction.setTimestamp(LocalDateTime.now().minusHours(i)); // Older as i increases
            entityManager.persist(transaction);
        }
        entityManager.flush();

        // Act
        List<Transaction> result = transactionRepository.findTop5ByAccountOrderByTimestampDesc(account);

        // Assert
        assertEquals(5, result.size());
        // Verify they're in descending order (newest first)
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getTimestamp().isAfter(result.get(i + 1).getTimestamp()));
        }
    }

    @Test
    void findRecentTransactionsByUsername_ShouldReturnUserTransactions() {
        // Arrange
        User user1 = new User("user1", "password");
        User user2 = new User("user2", "password");
        entityManager.persist(user1);
        entityManager.persist(user2);
        
        Account account1 = new Account(user1, CurrencyType.TRY, new BigDecimal("1000"));
        Account account2 = new Account(user2, CurrencyType.TRY, new BigDecimal("2000"));
        entityManager.persist(account1);
        entityManager.persist(account2);
        
        // Create transactions for user1
        for (int i = 0; i < 3; i++) {
            Transaction transaction = new Transaction();
            transaction.setAccount(account1);
            transaction.setAmount(new BigDecimal(100 * (i + 1)));
            transaction.setDescription("User1 Transaction " + (i + 1));
            transaction.setTimestamp(LocalDateTime.now().minusHours(i));
            transaction.setTransactionType("DEPOSIT");
            entityManager.persist(transaction);
        }
        
        // Create transactions for user2
        for (int i = 0; i < 2; i++) {
            Transaction transaction = new Transaction();
            transaction.setAccount(account2);
            transaction.setAmount(new BigDecimal(200 * (i + 1)));
            transaction.setDescription("User2 Transaction " + (i + 1));
            transaction.setTimestamp(LocalDateTime.now().minusHours(i));
            transaction.setTransactionType("DEPOSIT");
            entityManager.persist(transaction);
        }
        entityManager.flush();

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> user1Transactions = transactionRepository.findRecentTransactionsByUsername(
            "user1", pageable);
        List<Transaction> user2Transactions = transactionRepository.findRecentTransactionsByUsername(
            "user2", pageable);

        // Assert
        assertEquals(3, user1Transactions.size());
        assertEquals(2, user2Transactions.size());
        
        // Verify they belong to the right user
        for (Transaction t : user1Transactions) {
            assertEquals("user1", t.getAccount().getUser().getUsername());
        }
        
        for (Transaction t : user2Transactions) {
            assertEquals("user2", t.getAccount().getUser().getUsername());
        }
    }
}

