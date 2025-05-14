package com.accountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDto {
    private Long transactionId;
    private String username;
    private String currencyType;
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private String description;
    private String transactionType;
    private String exchangeType;
    private String relatedCurrency;
    private Long relatedTransactionId;

    public TransactionDto() {
    }

    public TransactionDto(Long transactionId, LocalDateTime timestamp, BigDecimal amount, String description) {
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.amount = amount;
        this.description = description;
    }

    public TransactionDto(Long transactionId, String username, String currencyType,
            LocalDateTime timestamp, BigDecimal amount, String description,
            String transactionType) {
        this.transactionId = transactionId;
        this.username = username;
        this.currencyType = currencyType;
        this.timestamp = timestamp;
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
    }

    public TransactionDto(Long transactionId, String username, String currencyType,
            LocalDateTime timestamp, BigDecimal amount, String description,
            String transactionType, String exchangeType, String relatedCurrency,
            Long relatedTransactionId) {
        this.transactionId = transactionId;
        this.username = username;
        this.currencyType = currencyType;
        this.timestamp = timestamp;
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
        this.exchangeType = exchangeType;
        this.relatedCurrency = relatedCurrency;
        this.relatedTransactionId = relatedTransactionId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getRelatedCurrency() {
        return relatedCurrency;
    }

    public void setRelatedCurrency(String relatedCurrency) {
        this.relatedCurrency = relatedCurrency;
    }

    public Long getRelatedTransactionId() {
        return relatedTransactionId;
    }

    public void setRelatedTransactionId(Long relatedTransactionId) {
        this.relatedTransactionId = relatedTransactionId;
    }

}
