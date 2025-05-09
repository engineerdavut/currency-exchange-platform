package com.accountservice.controller;

import com.accountservice.dto.AccountInfoDto;
import com.accountservice.dto.ExchangeTransactionDto;
import com.accountservice.dto.TransactionDto;
import com.accountservice.manager.WalletManager;
import com.accountservice.service.AccountService;

// *** GEREKLİ IMPORTLAR ***
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// **************************
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    // *** LOGGER TANIMLAMASI ***
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    // **************************

    private final AccountService accountService;
    private final WalletManager walletManager;

    @Autowired
    public AccountController(AccountService accountService, WalletManager walletManager) {
        this.accountService = accountService;
        this.walletManager = walletManager;
    }

    @PostMapping("/exchange")
    public ResponseEntity<String> exchangeCurrency(@RequestBody ExchangeTransactionDto request, @RequestHeader("X-User") String username) {
        logger.info("[ACCOUNT CTRL] /exchange endpoint called. Request: {}", request); 
        logger.info("Exchange called by {}", username);
        accountService.exchangeCurrency(request);
        return ResponseEntity.ok("{\"message\": \"Exchange request submitted\"}");
    }

    @GetMapping("/info")
    public ResponseEntity<List<AccountInfoDto>> getAccountInfo(@RequestHeader("X-User") String username) {
        return ResponseEntity.ok(accountService.getAccountInfo(username));
    }
    
    @GetMapping("/wallet")
    public ResponseEntity<List<AccountInfoDto>> getWallet(@RequestHeader("X-User") String username) {
        logger.info("[ACCOUNT CTRL] /wallet endpoint called for user: {}", username); // Log ekle
        try {
            List<AccountInfoDto> walletInfo = walletManager.getWallet(username);
            logger.debug("[ACCOUNT CTRL] /wallet data fetched successfully for user: {}", username);
            return ResponseEntity.ok(walletInfo);
        } catch (Exception e) {
             logger.error("[ACCOUNT CTRL] /wallet error for user {}: {}", username, e.getMessage(), e);
             throw e; // Hatanın global handler tarafından yakalanmasını sağla (varsa)
        }
    }
     @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getRecentTransactions(
            @RequestParam(required = false) String currencyType,
            @RequestHeader("X-User") String username) {
        logger.info("[ACCOUNT CTRL] /transactions endpoint called for user: {}, currencyType: {}", username, currencyType); // Log ekle
        try {
            List<TransactionDto> transactions = accountService.getRecentTransactions(username, currencyType);
            logger.debug("[ACCOUNT CTRL] /transactions data fetched successfully for user: {}", username);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("[ACCOUNT CTRL] /transactions error for user {}: {}", username, e.getMessage(), e);
            throw e; // Hatanın global handler tarafından yakalanmasını sağla (varsa)
        }
    }   
    
    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody TransactionDto transactionDto, @RequestHeader("X-User") String username) {
        transactionDto.setUsername(username);
        accountService.deposit(transactionDto);
        return ResponseEntity.ok("{\"message\": \"Deposit successful\"}");
    }
    
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody TransactionDto transactionDto, 
                                          @RequestHeader("X-User") String username) {
        transactionDto.setUsername(username);
        accountService.withdraw(transactionDto);
        return ResponseEntity.ok("{\"message\": \"Withdrawal successful\"}");
    }
    
    @GetMapping("/check-balance")
    public ResponseEntity<Boolean> checkBalance(
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestHeader("X-User") String username) {
        return ResponseEntity.ok(accountService.hasEnoughBalance(username, currency, amount));
    }
}
