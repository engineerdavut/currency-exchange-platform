package com.accountservice.controller;

import com.accountservice.dto.AccountInfoDto;
import com.accountservice.dto.ExchangeTransactionDto;
import com.accountservice.dto.TransactionDto;
import com.accountservice.exception.ResourceNotFoundException;
import com.accountservice.manager.WalletManager;
import com.accountservice.service.AccountService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// java.net.URLDecoder, java.nio.charset.StandardCharsets, java.io.UnsupportedEncodingException
// ve org.springframework.web.server.ResponseStatusException importları buradan kaldırılabilir.

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final WalletManager walletManager;

    public AccountController(AccountService accountService, WalletManager walletManager) {
        this.accountService = accountService;
        this.walletManager = walletManager;
    }

    // decodeUsernameFromHeader metodu SİLİNECEK.

    @PostMapping("/exchange")
    public ResponseEntity<String> exchangeCurrency(@RequestBody ExchangeTransactionDto request,
                                                   @RequestHeader("X-User") String username) { // username artık decode edilmiş gelir
        request.setUsername(username); // Eğer DTO içinde username alanı varsa
        logger.info("[ACCOUNT CTRL] /exchange endpoint called for user: {}. Request: {}", username, request);
        // Eğer AccountService.exchangeCurrency metodu (String username) parametresi alıyorsa:
        accountService.exchangeCurrency(request); // Bu satırı kendi servis imzanıza göre güncelleyin
        // Eğer AccountService.exchangeCurrency sadece DTO alıyorsa ve DTO içinde username set edildiyse:
        // accountService.exchangeCurrency(request); 
        return ResponseEntity.ok("{\"message\": \"Exchange request submitted\"}");
    }

    @GetMapping("/info")
    public ResponseEntity<List<AccountInfoDto>> getAccountInfo(@RequestHeader("X-User") String username) { // username artık decode edilmiş
        logger.info("[ACCOUNT CTRL] /info endpoint called for user: {}", username);
        return ResponseEntity.ok(accountService.getAccountInfo(username));
    }

    @GetMapping("/wallet")
    public ResponseEntity<List<AccountInfoDto>> getWallet(@RequestHeader("X-User") String username) { // username artık decode edilmiş
        logger.info("[ACCOUNT CTRL] /wallet endpoint called for user: {}", username);
        try {
            List<AccountInfoDto> walletInfo = walletManager.getWallet(username);
            logger.debug("[ACCOUNT CTRL] /wallet data fetched successfully for user: {}", username);
            return ResponseEntity.ok(walletInfo);
        } catch (ResourceNotFoundException e) {
            logger.warn("[ACCOUNT CTRL] /wallet error for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("[ACCOUNT CTRL] /wallet unexpected error for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getRecentTransactions(
            @RequestParam(required = false) String currencyType,
            @RequestHeader("X-User") String username) { // username artık decode edilmiş
        logger.info("[ACCOUNT CTRL] /transactions endpoint called for user: {}, currencyType: {}", username, currencyType);
        try {
            List<TransactionDto> transactions = accountService.getRecentTransactions(username, currencyType);
            logger.debug("[ACCOUNT CTRL] /transactions data fetched successfully for user: {}", username);
            return ResponseEntity.ok(transactions);
        } catch (ResourceNotFoundException e) {
            logger.warn("[ACCOUNT CTRL] /transactions error for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("[ACCOUNT CTRL] /transactions unexpected error for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody TransactionDto transactionDto,
                                          @RequestHeader("X-User") String username) { // username artık decode edilmiş
        transactionDto.setUsername(username);
        logger.info("[ACCOUNT CTRL] /deposit endpoint called for user: {}. Request: {}", username, transactionDto);
        accountService.deposit(transactionDto);
        return ResponseEntity.ok("{\"message\": \"Deposit successful\"}");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody TransactionDto transactionDto,
                                          @RequestHeader("X-User") String username) { // username artık decode edilmiş
        transactionDto.setUsername(username);
        logger.info("[ACCOUNT CTRL] /withdraw endpoint called for user: {}. Request: {}", username, transactionDto);
        accountService.withdraw(transactionDto);
        return ResponseEntity.ok("{\"message\": \"Withdrawal successful\"}");
    }

    @GetMapping("/check-balance")
    public ResponseEntity<Boolean> checkBalance(
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestHeader("X-User") String username) { // username artık decode edilmiş
        logger.info("[ACCOUNT CTRL] /check-balance endpoint called for user: {}, currency: {}, amount: {}", username, currency, amount);
        return ResponseEntity.ok(accountService.hasEnoughBalance(username, currency, amount));
    }
}