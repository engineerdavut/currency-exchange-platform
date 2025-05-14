package com.accountservice.controller;

import com.accountservice.dto.AccountInfoDto;
import com.accountservice.dto.TransactionDto;
import com.accountservice.manager.WalletManager;
import com.accountservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private WalletManager walletManager;

    @InjectMocks
    private AccountController accountController;

    @Test
    public void testGetAccountInfo() {
        String username = "testUser";
        List<AccountInfoDto> expectedAccounts = Arrays.asList(
                new AccountInfoDto(1L, "TRY", new BigDecimal("1000")),
                new AccountInfoDto(2L, "USD", new BigDecimal("100")));
        when(accountService.getAccountInfo(username)).thenReturn(expectedAccounts);

        ResponseEntity<List<AccountInfoDto>> response = accountController.getAccountInfo(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAccounts, response.getBody());
        verify(accountService).getAccountInfo(username);
    }

    @Test
    public void testGetWallet() {
        String username = "testUser";
        List<AccountInfoDto> expectedWallet = Arrays.asList(
                new AccountInfoDto(1L, "TRY", new BigDecimal("1000")),
                new AccountInfoDto(2L, "USD", new BigDecimal("100")));
        when(walletManager.getWallet(username)).thenReturn(expectedWallet);

        ResponseEntity<List<AccountInfoDto>> response = accountController.getWallet(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedWallet, response.getBody());
        verify(walletManager).getWallet(username);
    }

    @Test
    public void testDeposit() {
        String username = "testUser";
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setCurrencyType("TRY");
        transactionDto.setAmount(new BigDecimal("100"));

        ResponseEntity<String> response = accountController.deposit(transactionDto, username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().contains("Deposit successful"));
        verify(accountService).deposit(transactionDto);
        assertEquals(username, transactionDto.getUsername());
    }

    @Test
    public void testWithdraw() {
        String username = "testUser";
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setCurrencyType("TRY");
        transactionDto.setAmount(new BigDecimal("50"));

        ResponseEntity<String> response = accountController.withdraw(transactionDto, username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Withdrawal successful"));
        verify(accountService).withdraw(transactionDto);
        assertEquals(username, transactionDto.getUsername());
    }

    @Test
    public void testGetRecentTransactions() {

        String username = "testUser";
        String currencyType = "TRY";
        List<TransactionDto> expectedTransactions = Arrays.asList(
                createTransactionDto("testUser", "TRY", new BigDecimal("100"), "Deposit"),
                createTransactionDto("testUser", "TRY", new BigDecimal("50"), "Withdraw"));
        when(accountService.getRecentTransactions(username, currencyType)).thenReturn(expectedTransactions);

        ResponseEntity<List<TransactionDto>> response = accountController.getRecentTransactions(currencyType, username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTransactions, response.getBody());
        verify(accountService).getRecentTransactions(username, currencyType);
    }

    private TransactionDto createTransactionDto(String username, String currencyType, BigDecimal amount,
            String transactionType) {
        TransactionDto dto = new TransactionDto();
        dto.setUsername(username);
        dto.setCurrencyType(currencyType);
        dto.setAmount(amount);
        dto.setTransactionType(transactionType);
        return dto;
    }

    @Test
    public void testCheckBalance() {

        String username = "testUser";
        String currency = "TRY";
        BigDecimal amount = new BigDecimal("500");
        when(accountService.hasEnoughBalance(username, currency, amount)).thenReturn(true);

        ResponseEntity<Boolean> response = accountController.checkBalance(currency, amount, username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(accountService).hasEnoughBalance(username, currency, amount);
    }
}
