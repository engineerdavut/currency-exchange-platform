package com.accountservice.service;

import com.accountservice.dto.AccountInfoDto;
import com.accountservice.dto.ExchangeTransactionDto;
import com.accountservice.dto.TransactionDto;
import com.accountservice.entity.Account;
import com.accountservice.entity.CurrencyType;
import com.accountservice.entity.Transaction;
import com.accountservice.entity.User;
import com.accountservice.exception.InsufficientBalanceException;
import com.accountservice.repository.AccountRepository;
import com.accountservice.repository.TransactionRepository;
import com.accountservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void testGetAccountInfo() {

        String username = "testUser";
        User user = new User(username, "password");
        Account account1 = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));
        Account account2 = new Account(user, CurrencyType.USD, new BigDecimal("100"));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(Arrays.asList(account1, account2));

        List<AccountInfoDto> result = accountService.getAccountInfo(username);

        assertEquals(2, result.size());
        assertEquals("TRY", result.get(0).getCurrencyType());
        assertEquals(new BigDecimal("1000"), result.get(0).getBalance());
        assertEquals("USD", result.get(1).getCurrencyType());
        assertEquals(new BigDecimal("100"), result.get(1).getBalance());
    }

    @Test
    public void testDeposit() {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setUsername("testUser");
        transactionDto.setCurrencyType("TRY");
        transactionDto.setAmount(new BigDecimal("100"));

        User user = new User("testUser", "password");
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY))
               .thenReturn(Optional.of(account));


        accountService.deposit(transactionDto);


        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
        assertEquals(new BigDecimal("1100"), account.getBalance());
    }

    @Test
    public void testWithdrawSuccess() {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setUsername("testUser");
        transactionDto.setCurrencyType("TRY");
        transactionDto.setAmount(new BigDecimal("100"));

        User user = new User("testUser", "password");
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY)).thenReturn(Optional.of(account));


        accountService.withdraw(transactionDto);


        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
        assertEquals(new BigDecimal("900"), account.getBalance());
    }

    @Test
    public void testWithdrawInsufficientBalance() {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setUsername("testUser");
        transactionDto.setCurrencyType("TRY");
        transactionDto.setAmount(new BigDecimal("2000"));

        User user = new User("testUser", "password");
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY)).thenReturn(Optional.of(account));


        assertThrows(InsufficientBalanceException.class, () -> {
            accountService.withdraw(transactionDto);
        });
    }

    @Test
    public void testExchangeCurrency() {
        // Arrange
        ExchangeTransactionDto exchangeDto = new ExchangeTransactionDto();
        exchangeDto.setUsername("testUser");
        exchangeDto.setFromCurrency("TRY");
        exchangeDto.setToCurrency("USD");
        exchangeDto.setFromAmount(new BigDecimal("100"));
        exchangeDto.setToAmount(new BigDecimal("10"));

        User user = new User("testUser", "password");
        Account fromAccount = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));
        Account toAccount = new Account(user, CurrencyType.USD, new BigDecimal("50"));

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.USD)).thenReturn(Optional.of(toAccount));

        // Act
        accountService.exchangeCurrency(exchangeDto);

        // Assert
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        assertEquals(new BigDecimal("900"), fromAccount.getBalance());
        assertEquals(new BigDecimal("60"), toAccount.getBalance());
    }

    @Test
    public void testExchangeCurrencyInsufficientBalance() {
        // Arrange
        ExchangeTransactionDto exchangeDto = new ExchangeTransactionDto();
        exchangeDto.setUsername("testUser");
        exchangeDto.setFromCurrency("TRY");
        exchangeDto.setToCurrency("USD");
        exchangeDto.setFromAmount(new BigDecimal("2000"));
        exchangeDto.setToAmount(new BigDecimal("200"));

        User user = new User("testUser", "password");
        Account fromAccount = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));
        Account toAccount = new Account(user, CurrencyType.USD, new BigDecimal("50"));

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.USD)).thenReturn(Optional.of(toAccount));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> {
            accountService.exchangeCurrency(exchangeDto);
        });
    }

    @Test
    public void testGetRecentTransactions_WithCurrencyType() {
        // Arrange
        String username = "testUser";
        String currencyType = "TRY";

        User user = new User(username, "password");
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));

        Transaction tx1 = new Transaction(account, LocalDateTime.now(), new BigDecimal("100"), "Deposit", "DEPOSIT");
        Transaction tx2 = new Transaction(account, LocalDateTime.now(), new BigDecimal("-50"), "Withdrawal",
                "WITHDRAW");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY)).thenReturn(Optional.of(account));
        when(transactionRepository.findTop5ByAccountOrderByTimestampDesc(account)).thenReturn(Arrays.asList(tx1, tx2));

        // Act
        List<TransactionDto> result = accountService.getRecentTransactions(username, currencyType);

        // Assert
        assertEquals(2, result.size());
        assertEquals("TRY", result.get(0).getCurrencyType());
        assertEquals(new BigDecimal("100"), result.get(0).getAmount());
        assertEquals("DEPOSIT", result.get(0).getTransactionType());
        assertEquals("TRY", result.get(1).getCurrencyType());
        assertEquals(new BigDecimal("-50"), result.get(1).getAmount());
        assertEquals("WITHDRAW", result.get(1).getTransactionType());
    }

    @Test
    public void testGetRecentTransactions_AllCurrencies() {
        // Arrange
        String username = "testUser";

        User user = new User(username, "password");
        Account tryAccount = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));
        Account usdAccount = new Account(user, CurrencyType.USD, new BigDecimal("100"));

        Transaction tx1 = new Transaction(tryAccount, LocalDateTime.now(), new BigDecimal("100"), "Deposit", "DEPOSIT");
        Transaction tx2 = new Transaction(usdAccount, LocalDateTime.now(), new BigDecimal("10"), "Deposit", "DEPOSIT");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(Arrays.asList(tryAccount, usdAccount));
        when(transactionRepository.findTop5ByAccountOrderByTimestampDesc(tryAccount)).thenReturn(Arrays.asList(tx1));
        when(transactionRepository.findTop5ByAccountOrderByTimestampDesc(usdAccount)).thenReturn(Arrays.asList(tx2));

        List<TransactionDto> result = accountService.getRecentTransactions(username, null);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(tx -> tx.getCurrencyType().equals("TRY")));
        assertTrue(result.stream().anyMatch(tx -> tx.getCurrencyType().equals("USD")));
    }

    @Test
    public void testHasEnoughBalance_True() {
        String username = "testUser";
        String currency = "TRY";
        BigDecimal amount = new BigDecimal("500");

        User user = new User(username, "password");
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY)).thenReturn(Optional.of(account));

        boolean result = accountService.hasEnoughBalance(username, currency, amount);

        assertTrue(result);
    }

    @Test
    public void testHasEnoughBalance_False() {
        String username = "testUser";
        String currency = "TRY";
        BigDecimal amount = new BigDecimal("1500");

        User user = new User(username, "password");
        Account account = new Account(user, CurrencyType.TRY, new BigDecimal("1000"));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrencyType(user, CurrencyType.TRY)).thenReturn(Optional.of(account));

        boolean result = accountService.hasEnoughBalance(username, currency, amount);

        assertFalse(result);
    }

    @Test
    public void testGetUsernameFromRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = new Cookie[] { new Cookie("username", "testUser") };
        when(request.getCookies()).thenReturn(cookies);

        String result = accountService.getUsernameFromRequest(request);

        assertEquals("testUser", result);
    }

    @Test
    public void testGetUsernameFromRequest_NoCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        String result = accountService.getUsernameFromRequest(request);

        assertNull(result);
    }
}
