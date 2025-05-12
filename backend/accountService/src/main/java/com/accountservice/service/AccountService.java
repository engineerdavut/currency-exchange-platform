package com.accountservice.service;

import com.accountservice.dto.AccountInfoDto;
import com.accountservice.dto.ExchangeTransactionDto;
import com.accountservice.dto.TransactionDto;
import com.accountservice.entity.Account;
import com.accountservice.entity.CurrencyType;
import com.accountservice.entity.Transaction;
import com.accountservice.entity.User;
import com.accountservice.exception.InsufficientBalanceException;
import com.accountservice.exception.ResourceNotFoundException;
import com.accountservice.repository.AccountRepository;
import com.accountservice.repository.TransactionRepository;
import com.accountservice.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public List<AccountInfoDto> getAccountInfo(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Account> accounts = accountRepository.findByUser(user);
        
        return accounts.stream()
            .map(acc -> new AccountInfoDto(acc.getId(), acc.getCurrencyType().name(), acc.getBalance()))
            .collect(Collectors.toList());
    }
    
    public List<TransactionDto> getRecentTransactions(String username, String currencyType) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (currencyType == null || currencyType.isEmpty() || currencyType.equals("ALL")) {
            return getAllRecentTransactions(username);
        } else {
            Account account = accountRepository.findByUserAndCurrencyType(user, CurrencyType.valueOf(currencyType))
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for currency: " + currencyType));
            
            List<Transaction> transactions = transactionRepository.findTop5ByAccountOrderByTimestampDesc(account);
            
            return transactions.stream()
                .map(tx -> new TransactionDto(
                    tx.getId(), 
                    username,
                    account.getCurrencyType().name(),
                    tx.getTimestamp(), 
                    tx.getAmount(), 
                    tx.getDescription(),
                    tx.getTransactionType(),
                    tx.getExchangeType(),
                    tx.getRelatedCurrency(),
                    tx.getRelatedTransactionId()))
                .collect(Collectors.toList());
        }
    }
    
    public List<TransactionDto> getAllRecentTransactions(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Account> accounts = accountRepository.findByUser(user);
        
        List<Transaction> allTransactions = accounts.stream()
            .flatMap(account -> transactionRepository.findTop5ByAccountOrderByTimestampDesc(account).stream())
            .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
            .limit(5)
            .collect(Collectors.toList());
        
        return allTransactions.stream()
            .map(tx -> new TransactionDto(
                tx.getId(), 
                username,
                tx.getAccount().getCurrencyType().name(),
                tx.getTimestamp(), 
                tx.getAmount(), 
                tx.getDescription(),
                tx.getTransactionType(),
                tx.getExchangeType(),
                tx.getRelatedCurrency(),
                tx.getRelatedTransactionId()))
            .collect(Collectors.toList());
    }
    
    // YENİ: DTO alan deposit metodu
    public void deposit(TransactionDto transactionDto) {
        deposit(
            transactionDto.getUsername(),
            CurrencyType.valueOf(transactionDto.getCurrencyType()),
            transactionDto.getAmount()
        );
    }
    
    // Eski deposit metodu (iç kullanım için)
    private void deposit(String username, CurrencyType currencyType, BigDecimal amount) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Account account = accountRepository.findByUserAndCurrencyType(user, currencyType)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found for currency: " + currencyType));
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        
        Transaction transaction = new Transaction(
            account, LocalDateTime.now(), amount, "Deposit", "DEPOSIT");
        transactionRepository.save(transaction);
    }
    
    // YENİ: DTO alan withdraw metodu
    public void withdraw(TransactionDto transactionDto) {
        withdraw(
            transactionDto.getUsername(),
            CurrencyType.valueOf(transactionDto.getCurrencyType()),
            transactionDto.getAmount()
        );
    }
    
    // Eski withdraw metodu (iç kullanım için)
    private void withdraw(String username, CurrencyType currencyType, BigDecimal amount) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Account account = accountRepository.findByUserAndCurrencyType(user, currencyType)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found for currency: " + currencyType));
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        
        Transaction transaction = new Transaction(
            account, LocalDateTime.now(), amount.negate(), "Withdrawal", "WITHDRAW");
        transactionRepository.save(transaction);
    }
    
    // YENİ: DTO alan exchangeCurrency metodu
    public void exchangeCurrency(ExchangeTransactionDto request) {
        exchangeCurrency(
            request.getUsername(),
            CurrencyType.valueOf(request.getFromCurrency()),
            CurrencyType.valueOf(request.getToCurrency()),
            request.getFromAmount(),
            request.getToAmount()
        );
    }
    
    // Eski exchangeCurrency metodu (iç kullanım için)
    @Transactional
    private void exchangeCurrency(String username, CurrencyType fromCurrency, CurrencyType toCurrency, 
                               BigDecimal fromAmount, BigDecimal toAmount) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Account fromAccount = accountRepository.findByUserAndCurrencyType(user, fromCurrency)
            .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));
        
        Account toAccount = accountRepository.findByUserAndCurrencyType(user, toCurrency)
            .orElseThrow(() -> new ResourceNotFoundException("Target account not found"));
        
        if (fromAmount.compareTo(BigDecimal.ZERO) <= 0 || toAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amounts must be positive");
        }
        
        if (fromAccount.getBalance().compareTo(fromAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        
        fromAccount.setBalance(fromAccount.getBalance().subtract(fromAmount));
        toAccount.setBalance(toAccount.getBalance().add(toAmount));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        String description = "Exchange from " + fromCurrency + " to " + toCurrency;
        
        Transaction fromTransaction = new Transaction(
            fromAccount, 
            LocalDateTime.now(), 
            fromAmount.negate(), 
            description, 
            "EXCHANGE_OUT",
            "FROM",
            toCurrency.name(),
            null
        );
        transactionRepository.save(fromTransaction);
        
        Transaction toTransaction = new Transaction(
            toAccount, 
            LocalDateTime.now(), 
            toAmount, 
            description, 
            "EXCHANGE_IN",
            "TO",
            fromCurrency.name(),
            fromTransaction.getId()
        );
        transactionRepository.save(toTransaction);
        
        fromTransaction.setRelatedTransactionId(toTransaction.getId());
        //transactionRepository.save(fromTransaction);
    }
    
    public boolean hasEnoughBalance(String username, String currency, BigDecimal amount) {
        return hasEnoughBalance(username, CurrencyType.valueOf(currency), amount);
    }
    
    private boolean hasEnoughBalance(String username, CurrencyType currencyType, BigDecimal amount) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Account account = accountRepository.findByUserAndCurrencyType(user, currencyType)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found for currency: " + currencyType));
        
        return account.getBalance().compareTo(amount) >= 0;
    }

    public String getUsernameFromRequest(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if ("username".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
    }
    return null;
}
}

