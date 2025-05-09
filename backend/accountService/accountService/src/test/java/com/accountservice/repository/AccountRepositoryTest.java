package com.accountservice.repository;

import com.accountservice.entity.Account;
import com.accountservice.entity.CurrencyType;
import com.accountservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByUserAndCurrencyType_ShouldReturnAccount() {
        // Arrange
        User user = new User("testUser", "password");
        entityManager.persist(user);
        
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));
        entityManager.persist(account);
        entityManager.flush();

        // Act
        Optional<Account> result = accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(account.getId(), result.get().getId());
        assertEquals(CurrencyType.TRY, result.get().getCurrencyType());
        assertEquals(new BigDecimal("1000"), result.get().getBalance());
    }

    @Test
    void findByUserAndCurrencyType_WithNonExistentCurrency_ShouldReturnEmpty() {
        // Arrange
        User user = new User("testUser", "password");
        entityManager.persist(user);
        
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));
        entityManager.persist(account);
        entityManager.flush();

        // Act
        Optional<Account> result = accountRepository.findByUserAndCurrencyType(user, CurrencyType.USD);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByUser_ShouldReturnAllUserAccounts() {
        // Arrange
        User user1 = new User("user1", "password");
        User user2 = new User("user2", "password");
        entityManager.persist(user1);
        entityManager.persist(user2);
        
        Account account1 = new Account(user1, CurrencyType.TRY, new BigDecimal("1000"));
        Account account2 = new Account(user1, CurrencyType.USD, new BigDecimal("500"));
        Account account3 = new Account(user2, CurrencyType.EUR, new BigDecimal("200"));
        
        entityManager.persist(account1);
        entityManager.persist(account2);
        entityManager.persist(account3);
        entityManager.flush();

        // Act
        List<Account> user1Accounts = accountRepository.findByUser(user1);
        List<Account> user2Accounts = accountRepository.findByUser(user2);

        // Assert
        assertEquals(2, user1Accounts.size());
        assertEquals(1, user2Accounts.size());
    }
}
